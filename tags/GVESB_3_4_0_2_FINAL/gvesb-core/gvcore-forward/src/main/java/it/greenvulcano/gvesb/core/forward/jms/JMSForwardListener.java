/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.core.forward.jms;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.gvesb.core.GreenVulcano;
import it.greenvulcano.gvesb.core.forward.JMSForwardException;
import it.greenvulcano.gvesb.core.forward.preprocess.Validator;
import it.greenvulcano.gvesb.core.forward.util.ErrorHandlerManager;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.gvesb.j2ee.XAHelper;
import it.greenvulcano.gvesb.j2ee.XAHelperException;
import it.greenvulcano.gvesb.j2ee.jms.JMSMessageDecorator;
import it.greenvulcano.gvesb.j2ee.jms.JMSMessageDump;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadMap;

import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.XAQueueConnection;
import javax.jms.XAQueueSession;
import javax.jms.XASession;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * This is a Message Listener that gets a JMS message from a destination and
 * puts it in a GVBuffer instance, then invoke the forward() method of
 * GreenVulcano.
 * The forward name is retrieved from the forward-name environment entry,
 * the system and service are retrieved the properties of the message,
 * or from forward configuration.
 * 
 * @version 3.2.0 Gen 11, 2012
 * @author GreenVulcano Developer Team
 * 
 */
public class JMSForwardListener implements Runnable
{
    private static final Logger logger          = GVLogger.getLogger(JMSForwardListener.class);

    private JMSForwardData      data            = null;

    /**
     * Forward configuration name.
     */
    private String              name        = "";
    /**
     * Forward name invoked in GreenVulcano.
     */
    private String              forwardName     = "";
    /**
     * Application server name.
     */
    private String              serverName      = "";

    //private Connection          connection       = null;
    private Session             session         = null;
    private MessageConsumer     messageConsumer = null;
    private Destination         destination     = null;
    private XAHelper            xaHelper        = null;
    private JNDIHelper          initialContext  = null;

    /**
     * The configured error handler.
     */
    private ErrorHandlerManager errorHandlerMgr = null;

    private boolean             initialized;
    private boolean             sessionRollBack;

    private boolean             run             = false;
    private boolean             connected       = false;
    private int                 readCount       = 0;


    public JMSForwardListener()
    {
        // do nothing
    }

    public void init(JMSForwardData data) throws JMSForwardException
    {
        NMDC.push();
        NMDC.clear();
        NMDC.setSubSystem(JMSForwardData.SUBSYSTEM);

        try {
            this.data = data;
            name = data.getName();
            forwardName = data.getForwardName();
            serverName = data.getServerName();

            NMDC.setServer(serverName);

            createErrorHandlerManager(data.getCfgNode());

            Node fdNode = XMLConfig.getNode(data.getCfgNode(), "ForwardDeployment");
            initialContext = new JNDIHelper(XMLConfig.getNode(fdNode, "JNDIHelper"));

            Node xaHNode = null;
            try {
                xaHNode = XMLConfig.getNode(fdNode, "XAHelper");
            }
            catch (Exception exc) {
                xaHNode = null;
            }
            try {
                xaHelper = new XAHelper(xaHNode);
                if (data.isDebug()) {
                    xaHelper.setLogger(logger);
                }
            }
            catch (Exception exc) {
                throw new JMSForwardException("GVJMS_INIT_ERROR", new String[][]{{"forward", forwardName}}, exc);
            }

            initialized = true;
            run = true;
        }
        catch (JMSForwardException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new JMSForwardException("GVJMS_APPLICATION_INIT_ERROR", new String[][]{{"forward", forwardName}}, exc);
        }
        finally {
            NMDC.pop();
        }

        if (data.isDebug()) {
            logger.info("Forward [" + forwardName + "] instance created");
        }
    }

    /**
     * Destroy the GreenVulcano instance.
     */
    public void destroy()
    {
        if (data.isDebug() || data.isShutdown()) {
            logger.info("Forward [" + forwardName + "] instance destroyed");
        }
        run = false;
        disconnect();
    }

    public String getName()
    {
        return this.name;
    }

    public String getForwardName()
    {
        return this.forwardName;
    }

    public boolean isActive()
    {
        return (data.isActive() && run && !data.isShutdown());
    }

    @Override
    public void run()
    {
        NMDC.push();
        NMDC.clear();
        NMDC.setServer(serverName);
        NMDC.setSubSystem(JMSForwardData.SUBSYSTEM);
        
        try {
            Thread.sleep(1000);
        }
        catch (Exception exc) {
            // do nothing
        }

        if (data.isDebug()) {
            logger.debug("Started Forward [" + name + "/" + forwardName + "] instance");
        }
        try {
            while (isActive() && ((data.getReadBlockCount() < 0) || (readCount < data.getReadBlockCount()))) {
                try {
                    if (data.isDebug()) {
                        logger.debug("Begin receiving message...");
                    }
                    if (!checkValidators()) {
                        if (data.isDebug()) {
                            logger.debug("Pre-process validation failed for Forward [" + name + "/" + forwardName + "] instance... retrying");
                        }
                        try {
                            Thread.sleep(data.getReconnectInterval());
                        }
                        catch (Exception exc2) {
                            // do nothing
                        }
                        continue;
                    }
                    try {
                        try {
                            connect();
                        }
                        catch (Exception exc) {
                            logger.error(
                                    "Connection error on Forward [" + name + "/" + forwardName + "]. Retry after "
                                            + data.getReconnectInterval() + " ms.", exc);
                            try {
                                Thread.sleep(data.getReconnectInterval());
                            }
                            catch (Exception exc2) {
                                // do nothing
                            }
                            continue;
                        }
                        if (!data.isShutdown()) {
                            try {
                                beginTX();
                                try {
                                    if (data.isTransacted() && !xaHelper.isAutoEnlist()
                                            && xaHelper.isTransactionActive()) {
                                        if (data.isDebug()) {
                                            logger.debug("Enlisting Session in TX...");
                                        }
                                        xaHelper.enlistResource(((XASession) session).getXAResource());
                                    }
                                }
                                catch (Exception exc) {
                                    logger.error("Error enlisting XA Resource for Forward[" + name + "/" + forwardName + "]", exc);
                                    if (exc instanceof JMSException) {
                                        disconnect();
                                        sleep();
                                        continue;
                                    }
                                    throw new JMSForwardException("Error enlisting XA Resource for Forward["
                                            + name + "/" + forwardName + "]", exc);
                                }
                                if (data.isDebug()) {
                                    logger.debug("Forward [" + name + "/" + forwardName + "] listening...");
                                }

                                try {
                                    Message msg = messageConsumer.receive(data.getReceiveTimeout());
                                    readCount++;
                                    if ((msg != null) && !data.isShutdown()) {
                                        NMDC.push();
                                        try {
                                            data.beginWork();
                                            processMessage(msg);
                                        }
                                        finally {
                                            NMDC.pop();
                                            data.endWork();
                                        }
                                    }
                                }
                                finally {
                                    try {
                                        if (!data.isShutdown()
                                                && (!xaHelper.isAutoEnlist() && xaHelper.isTransactionActive())) {
                                        	if (data.isDebug()) {
                                                logger.debug("Delisting Session from TX...");
                                            }
                                            xaHelper.delistResource();
                                        }
                                    }
                                    catch (Exception exc) {
                                        logger.error("Error delisting XA Resource for Forward[" + name + "/" + forwardName + "]",
                                                exc);
                                        rollbackTX();
                                        if (exc instanceof JMSException) {
                                            disconnect();
                                            sleep();
                                        }
                                    }
                                }
                            }
                            catch (Throwable exc) {
                                logger.error("Error processing Forward [" + name + "/" + forwardName + "]... rolling back", exc);
                                if (!data.isShutdown()) {
                                    rollbackTX();
                                    if (exc instanceof JMSException) {
                                        disconnect();
                                    }
                                    sleep();
                                }
                            }
                            if (!data.isShutdown()) {
                                if (!getRollbackOnly()) {
                                    try {
                                        commitTX();
                                    }
                                    catch (Throwable exc) {
                                        rollbackTX();
                                    }
                                }
                                else {
                                    try {
                                        rollbackTX();
                                    }
                                    catch (Throwable exc) {
                                        logger.error(
                                                "Error rolling back transaction for Forward [" + name + "/" + forwardName + "]", exc);
                                    }
                                }
                            }
                        }
                    }
                    catch (Throwable exc) {
                        logger.error("Error processing Forward [" + name + "/" + forwardName + "]", exc);
                        if (!data.isShutdown()) {
                            sleep();
                        }
                    }
                }
                catch (Throwable exc) {
                    logger.error("Error processing Forward [" + name + "/" + forwardName + "]", exc);
                    /*if (!data.isShutdown()) {
                        sleep();
                        rollbackTX();
                    }*/
                }
                finally {
                	if (data.isDebug()) {
                        logger.debug("End receiving message...");
                    }
                }
            }
        }
        finally {
            destroy();
            NMDC.pop();
            data.stopListener(this);
            ThreadMap.clean();
        }
    }

    /**
     * @throws JMSForwardException 
     * 
     */
    private boolean checkValidators() throws JMSForwardException {
        List<Validator> validators = data.getValidators();
        for (Validator validator : validators) {
            if (!validator.isValid()) {
                return false;
            }
        }
        return true;
    }

    private void connect() throws Exception
    {
        if (connected || data.isShutdown()) {
            return;
        }
        if (data.isDebug()) {
            logger.debug("Conneting to JMS server...");
        }
        try {
            if (data.isDebug()) {
                logger.debug("Creating Destination: " + data.getDestinationName());
            }
            destination = (Destination) initialContext.lookup(data.getDestinationName());

            Connection connection = data.getConnectionHolder().getConnection(initialContext);

            if (data.isTransacted() && (connection instanceof XAQueueConnection)) {
                session = ((XAQueueConnection) connection).createXAQueueSession();
            }
            else {
                session = ((QueueConnection) connection).createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
            }

            String localSelector = data.getMessageSelector();
            if (!"".equals(localSelector)) {
                localSelector = PropertiesHandler.expand(localSelector);
                if (data.isDebug()) {
                    logger.debug("Using message selector: [" + localSelector + "]");
                }
            }

            if (data.isTransacted() && (session instanceof XAQueueSession)) {
                messageConsumer = ((XAQueueSession) session).createConsumer(destination, localSelector);
            }
            else {
                messageConsumer = ((QueueSession) session).createReceiver((Queue) destination, localSelector);
            }

            connection.start();

            connected = true;
            if (data.isDebug()) {
                logger.debug("Connected Forward [" + name + "/" + forwardName + "] instance");
            }
        }
        finally {
            try {
                initialContext.close();
            }
            catch (Exception exc) {
                logger.warn("Exception closing the context for Forward [" + name + "/" + forwardName + "] instance", exc);
            }
        }
    }

    private void disconnect()
    {
        if (data.isDebug()) {
            logger.debug("Disconneting from JMS server...");
        }
        if (messageConsumer != null) {
            try {
                messageConsumer.close();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        messageConsumer = null;
        if (session != null) {
            try {
                session.close();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        session = null;

        connected = false;
    }

    private void beginTX() throws Exception
    {
        if (data.isDebug()) {
            logger.debug("Starting TX...");
        }
        sessionRollBack = false;
        if (data.isTransacted()) {
            try {
                xaHelper.setTransactionTimeout(data.getTransactionTimeout());
                xaHelper.begin();
            }
            catch (Exception exc) {
                logger.error("Error beginning transaction on Forward [" + name + "/" + forwardName + "]", exc);
                throw exc;
            }
        }
    }

    private void commitTX() throws Exception
    {
        Transaction tx = null;
        if (data.isDebug()) {
            logger.debug("Committing TX...");
        }
        try {
            if (data.isTransacted()) {
                if (xaHelper.isTransactionActive()) {
                    tx = xaHelper.getTransaction();
                    xaHelper.commit();
                }
            }
            else if ((session != null) && session.getTransacted()) {
            	sessionRollBack = false;
                session.commit();
            }
        }
        catch (Exception exc) {
            logger.error("Error committing transaction on Forward [" + name + "/" + forwardName + "]"
                    + ((tx != null) ? ": " + tx : ""), exc);
            throw exc;
        }
    }

    private void rollbackTX()
    {
        Transaction tx = null;
        if (data.isDebug()) {
            logger.debug("Rolling back TX...");
        }
        try {
            if (data.isTransacted()) {
                if (xaHelper.isTransactionActive()) {
                    tx = xaHelper.getTransaction();
                    xaHelper.rollback();
                }
            }
            else if ((session != null) && session.getTransacted()) {
            	sessionRollBack = true;
                session.rollback();
            }
        }
        catch (Exception exc) {
            logger.error("Error rolling-back transaction on Forward [" + name + "/" + forwardName + "]"
                    + ((tx != null) ? ": " + tx : ""), exc);
        }
    }

    /**
     * The message normally is inserted on the GVBuffer object field, in order
     * to be processed by the workflow engine. The standard properties, if
     * presents,
     * are used by {@link it.greenvulcano.gvesb.j2ee.jms.JMSMessageDecorator} to
     * populate the
     * GVBuffer fields.
     * If configured a {@link it.greenvulcano.gvesb.gvdp.IDataProvider} capable
     * of preprocess the
     * message, it's used to populate the GVBuffer fields.
     * 
     * @param msg
     *        message read from the queue
     */
    private void processMessage(Message msg) throws Exception
    {
        if (!initialized) {
            throw new JMSForwardException("Forward [" + name + "/" + forwardName + "] NOT initialized");
        }
        String cid = msg.getJMSCorrelationID();
        logger.debug("Forward [" + name + "/" + forwardName + "]: Processing incoming message [" + cid + "]");

        GVBuffer gvBuffer = null;
        long startTime = System.currentTimeMillis();

        String flowSystem = data.getFlowSystem();
        String flowService = data.getFlowService();

        try {
            try {
                if (data.isDumpMessage() && logger.isDebugEnabled()) {
                    logger.debug("Forward [" + name + "/" + forwardName + "] Received message :\n" + new JMSMessageDump(msg, null));
                }
                gvBuffer = new GVBuffer(flowSystem, flowService);

                JMSMessageDecorator.decorateGVBuffer(msg, gvBuffer);
                gvBuffer.setObject(msg);

                String refDP = data.getRefDP();
                if ((refDP != null) && (refDP.length() > 0)) {
                    DataProviderManager dataProviderManager = DataProviderManager.instance();
                    IDataProvider dataProvider = dataProviderManager.getDataProvider(refDP);
                    try {
                        if (data.isDebug()) {
                            logger.debug("Working on data provider[" + refDP + "]: " + dataProvider.getClass());
                        }
                        dataProvider.setObject(gvBuffer);
                        Object result = dataProvider.getResult();
                        if (result != null) {
                            gvBuffer.setObject(result);
                        }
                    }
                    finally {
                        dataProviderManager.releaseDataProvider(refDP, dataProvider);
                    }
                }
            }
            catch (Exception exc) {
                throw new JMSForwardException("The forward [" + name + "/" + forwardName + "] received a unmanageable message", exc);
            }
            GVBufferMDC.put(gvBuffer);
            if (data.isDebug()) {
                logger.debug("Begin Core call");
            }
            execute(gvBuffer, flowSystem, flowService);
            if (data.isDebug()) {
                logger.debug("End Core call");
            }
        }
        finally {
            long endTime = System.currentTimeMillis();
            logger.debug("Forward [" + name + "/" + forwardName + "]: Processed message  [" + cid + "] - ExecutionTime ("
                        + (endTime - startTime) + ")");
        }
    }

    /**
     * GreenVulcano ESB invocation
     * 
     * @param gvBuffer
     */
    private void execute(GVBuffer gvBuffer, String flowSystem, String flowService) throws Exception
    {
        GreenVulcano greenVulcano = data.getGreenVulcanoPool().getGreenVulcano(gvBuffer);
        if (greenVulcano == null) {
            throw new GVException("Timeout occurred in GreenVulcanoPool.getGreenVulcano()");
        }

        try {
            greenVulcano.forward(gvBuffer, forwardName, flowSystem, flowService);
            if (getRollbackOnly()) {
                logger.warn("The transaction started by the Forward [" + name + "/" + forwardName
                        + "] was rolled back by an external system");
            }
        }
        catch (GVPublicException exc) {
            if ((errorHandlerMgr == null) || !errorHandlerMgr.mustHandleError(exc)) {
                throw exc;
            }
            logger.warn("Forward [" + name + "/" + forwardName + "] - Calling error handler (" + errorHandlerMgr.getName()
                    + ") on exception", exc);
            gvBuffer = errorHandlerMgr.prepareBuffer(gvBuffer);
            GVBufferMDC.put(gvBuffer);
            greenVulcano.forward(gvBuffer, errorHandlerMgr.getErrorOperation());
        }
        finally {
            try {
                data.getGreenVulcanoPool().releaseGreenVulcano(greenVulcano);
            }
            catch (Exception exc) {
                // do nothing
            }
        }
    }

    /**
     * Test if the transaction has been marked for rollback only. An enterprise
     * bean instance can use this operation, for example, to test after an
     * exception has been caught, whether it is useless to continue
     * computation on behalf of the current transaction.
     * 
     * @return True if the current transaction is marked for rollback, false
     *         otherwise.
     * @throws SystemException
     * @throws XAHelperException
     */
    private boolean getRollbackOnly() throws SystemException, XAHelperException
    {
        Transaction tx = xaHelper.getTransaction();
        if (tx == null) {
        	return sessionRollBack;
        }
        int status = tx.getStatus();
        if ((status == Status.STATUS_MARKED_ROLLBACK) || (status == Status.STATUS_ROLLEDBACK)
                || (status == Status.STATUS_UNKNOWN) || (status == Status.STATUS_ROLLING_BACK)) {
            logger.warn("Forward [" + name + "/" + forwardName + "] transaction rolled back: " + tx);
            return true;
        }
        return false;
    }

    /**
     * Create the Error Handler Manager.
     * 
     * @throws Exception
     */
    private void createErrorHandlerManager(Node node) throws Exception
    {
        errorHandlerMgr = null;
        try {
            node = XMLConfig.getNode(node, "ErrorHandler");
        }
        catch (Exception exc) {
            logger.error("Forward [" + name + "/" + forwardName + "]: Error reading ErrorHandlerManager data ", exc);
            node = null;
        }
        if (node != null) {
            try {
                errorHandlerMgr = new ErrorHandlerManager();
                errorHandlerMgr.init(node.getParentNode());
                logger.info("Forward [" + name + "/" + forwardName + "]: Using ErrorHandlerManager");
            }
            catch (Exception exc) {
                logger.error("Error initializing ErrorHandlerManager ", exc);
                throw new Exception("Error initializing ErrorHandlerManager ", exc);
            }
        }
        else {
            errorHandlerMgr = null;
        }
    }

    /**
     *
     */
    private void sleep()
    {
        if (!run || (data.getSleepTimeout() <= 0)) {
            return;
        }
        if (data.isDebug()) {
            logger.debug("Sleeping after error...");
        }
        try {
            Thread.sleep(data.getSleepTimeout());
        }
        catch (InterruptedException exc2) {
            // do nothing
        }
    }
}
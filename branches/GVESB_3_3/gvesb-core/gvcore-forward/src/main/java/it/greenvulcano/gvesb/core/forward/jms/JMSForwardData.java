/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.core.forward.jms;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.core.forward.JMSForwardException;
import it.greenvulcano.gvesb.core.forward.preprocess.Validator;
import it.greenvulcano.gvesb.core.forward.preprocess.ValidatorManager;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.jmx.JMXEntryPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.2.0 15/gen/2012
 * @author GreenVulcano Developer Team
 */
public class JMSForwardData
{
    public static final int        DEFAULT_INITIAL_SIZE = 1;
    public static final int        DEFAULT_MAXIMUM_SIZE = 10;
    /**
     * The subsystem name.
     */
    public static final String     SUBSYSTEM            = "JMSForward";

    /**
     * The Logger instance must be provided by the creator.
     */
    private Logger                 logger               = null;

    private Node                   cfgNode              = null;

    private JMSForwardListenerPool pool                 = null;

    /**
     * Initial size pool.
     */
    private int                    initialSize          = DEFAULT_INITIAL_SIZE;
    /**
     * Max size pool, -1 = unlimited.
     */
    private int                    maximumSize          = DEFAULT_MAXIMUM_SIZE;

    /**
     * Forward configuration name.
     */
    private String                 name;
    /**
     * Forward name.
     */
    private String                 forwardName;
    /**
     * Server instance.
     */
    private String                 serverName;

    /**
     * The id_system for the flow to invoke.
     */
    private String                 flowSystem           = null;
    /**
     * The id_service for the flow to invoke.
     */
    private String                 flowService          = null;

    /**
     * If true the operation will use queues, otherwise will use topics.
     */
    private boolean                isQueue              = true;

    private JMSConnectionHolder    connectionHolder;
    private String                 connectionFactory;
    //private Connection             connection         = null;
    private String                 destinationName;
    private String                 messageSelector      = "";
    private long                   reconnectInterval    = -1;
    private int                    readBlockCount       = -1;

    /**
     * Keeps reference to <code>IDataProvider</code> implementation.
     */
    private String                 refDP                = null;

    private GreenVulcanoPool       greenVulcanoPool     = null;
    /**
     * Sleep timeout on errors.
     */
    private long                   sleepTimeout         = -1;
    private long                   receiveTimeout       = 1000;

    private boolean                transacted           = false;
    private int                    transactionTimeout   = 0;

    /**
     * If true shutdown in progress.
     */
    private AtomicBoolean          inShutdown           = new AtomicBoolean(false);

    /**
     * If true the pool is active.
     */
    private AtomicBoolean          isActive             = new AtomicBoolean(true);

    /**
     * If true the incoming message is dumped on log.
     */
    private boolean                dumpMessage          = false;
    private boolean                debug                = false;

    private AtomicInteger          working              = new AtomicInteger(0);
    private String                 descr                = "";
    
    private List<Validator>        validators           = new ArrayList<Validator>();


    /**
     * @param node
     * @throws JMSForwardException
     */
    public JMSForwardData(Node node, JMSForwardListenerPool pool, Logger logger) throws JMSForwardException
    {
        try {
            cfgNode = node;
            this.pool = pool;
            this.logger = logger;
            name = XMLConfig.get(node, "@name", XMLConfig.get(node, "concat(@forwardName, '_', position())"));
            forwardName = XMLConfig.get(node, "@forwardName", "UNDEFINED");
            serverName = JMXEntryPoint.getServerName();

            flowSystem = XMLConfig.get(node, "@flow-system", "");
            flowService = XMLConfig.get(node, "@flow-service", "");
            refDP = XMLConfig.get(node, "@ref-dp", "");
            sleepTimeout = XMLConfig.getLong(node, "@sleep-timeout", 5000);

            debug = XMLConfig.getBoolean(node, "@full-debug", false);
            dumpMessage = XMLConfig.getBoolean(node, "@dump-message", false);

            NodeList vnl = XMLConfig.getNodeList(node, "PreProcessor/Validators/*[@type='validator']");
            if (vnl.getLength() > 0) {
                ValidatorManager vm = ValidatorManager.instance();
                for (int i = 0; i < vnl.getLength(); i++) {
                    Node vn = vnl.item(i);
                    validators.add(vm.getValidator(vn));
                }
            }

            Node fdNode = XMLConfig.getNode(node, "ForwardDeployment");
            connectionFactory = XMLConfig.get(fdNode, "@connection-factory");
            transacted = XMLConfig.getBoolean(fdNode, "@transacted", false);

            connectionHolder = new JMSConnectionHolder(connectionFactory, transacted);
            connectionHolder.setDebug(debug);

            initialSize = XMLConfig.getInteger(fdNode, "@initial-size", DEFAULT_INITIAL_SIZE);
            maximumSize = XMLConfig.getInteger(fdNode, "@maximum-size", DEFAULT_MAXIMUM_SIZE);
            if (initialSize < 0) {
                throw new IllegalArgumentException("initialSize < 0, forwardName=" + forwardName);
            }
            if ((maximumSize > 0) && (initialSize > maximumSize)) {
                throw new IllegalArgumentException("initialSize(" + initialSize + ") > maximumSize(" + maximumSize
                        + "), forwardName=" + forwardName);
            }

            String destinationType = XMLConfig.get(fdNode, "@destination-type", "queue");
            isQueue = destinationType.equals("queue");
            destinationName = XMLConfig.get(fdNode, "@destination");
            messageSelector = XMLConfig.get(fdNode, "message-selector", "");
            reconnectInterval = XMLConfig.getLong(fdNode, "@reconnect-interval-sec", 10) * 1000;
            transacted = XMLConfig.getBoolean(fdNode, "@transacted", false);
            transactionTimeout = XMLConfig.getInteger(fdNode, "@transaction-timeout-sec", 30);
            receiveTimeout = XMLConfig.getInteger(fdNode, "@receive-timeout-sec", 1) * 1000;
            readBlockCount = XMLConfig.getInteger(fdNode, "@read-block-count", 60);

            greenVulcanoPool = GreenVulcanoPoolManager.instance().getGreenVulcanoPool(SUBSYSTEM);
            if (greenVulcanoPool == null) {
                throw new JMSForwardException("GVJMS_GREENVULCANOPOOL_NOT_CONFIGURED", new String[][]{{"forward",
                        forwardName}});
            }

            StringBuffer sb = new StringBuffer();
            sb.append("Forward [").append(forwardName);
            sb.append("] - flowSystem [").append(flowSystem);
            sb.append("] - flowService [").append(flowService);
            if (!refDP.equals("")) {
                sb.append("] - refDP [").append(refDP);
            }
            sb.append("] - using destinationType [").append(destinationType);
            sb.append("] - destinationName [").append(destinationName);
            if (!"".equals(messageSelector)) {
                sb.append("] - using messageSelector [").append(messageSelector);
            }
            sb.append("] - connectionFactory [").append(connectionFactory);
            sb.append("] - transacted [").append(transacted);
            sb.append("] - transactionTimeout [").append(transactionTimeout);
            sb.append("] - readBlockCount [").append(readBlockCount);
            sb.append("] - receiveTimeout [").append(receiveTimeout);
            sb.append("] - reconnectInterval [").append(reconnectInterval);
            sb.append("] - using on error sleepTimeout [").append(sleepTimeout);
            sb.append("] - pool initialSize [").append(initialSize);
            sb.append("] - pool maximumSize [").append(maximumSize).append("]");

            descr = sb.toString();
        }
        catch (JMSForwardException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new JMSForwardException("GVJMSPOOL_APPLICATION_INIT_ERROR", new String[][]{{"forward", forwardName}},
                    exc);
        }
    }


    /**
     * @return the cfgNode
     */
    public Node getCfgNode()
    {
        return this.cfgNode;
    }

    /**
     * @return the inShutdown
     */
    public boolean isShutdown()
    {
        return this.inShutdown.get();
    }


    /**
     * @return the initialSize
     */
    public int getInitialSize()
    {
        return this.initialSize;
    }

    /**
     * Set the initialSize
     */
    public void setInitialSize(int initialSize)
    {
        this.initialSize = initialSize;
    }

    /**
     * @return the maximumSize
     */
    public int getMaximumSize()
    {
        return this.maximumSize;
    }

    /**
     * Set the maximumSize
     */
    public void setMaximumSize(int maximumSize)
    {
        this.maximumSize = maximumSize;
    }

    /**
     * @return Returns the maxCreated.
     */
    public int getMaxCreated()
    {
        return pool.getMaxCreated();
    }

    /**
     * @return Returns the pooled instance count.
     */
    public int getPooledCount()
    {
        return pool.getPooledCount();
    }


    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the forwardName
     */
    public String getForwardName()
    {
        return this.forwardName;
    }


    /**
     * @return the serverName
     */
    public String getServerName()
    {
        return this.serverName;
    }


    /**
     * @return the flowSystem
     */
    public String getFlowSystem()
    {
        return this.flowSystem;
    }


    /**
     * @return the flowService
     */
    public String getFlowService()
    {
        return this.flowService;
    }


    /**
     * @return the isQueue
     */
    public boolean isQueue()
    {
        return this.isQueue;
    }

    /**
     * @return the destinationName
     */
    public String getDestinationName()
    {
        return this.destinationName;
    }

    /**
     * @return the connectionFactory
     */
    public String getConnectionFactory()
    {
        return this.connectionFactory;
    }

    /**
     * @return the connectionHolder
     */
    public JMSConnectionHolder getConnectionHolder()
    {
        return this.connectionHolder;
    }

    /**
     * @return the messageSelector
     */
    public String getMessageSelector()
    {
        return this.messageSelector;
    }

    /**
     * @return the reconnectInterval
     */
    public long getReconnectInterval()
    {
        return this.reconnectInterval;
    }

    /**
     * Set the reconnectInterval
     */
    public void setReconnectInterval(long reconnectInterval)
    {
        this.reconnectInterval = reconnectInterval;
    }

    /**
     * @return the readBlockCount
     */
    public int getReadBlockCount()
    {
        return this.readBlockCount;
    }

    /**
     * Set the readBlockCount
     */
    public void setReadBlockCount(int readBlockCount)
    {
        this.readBlockCount = readBlockCount;
    }

    /**
     * @return the refDP
     */
    public String getRefDP()
    {
        return this.refDP;
    }


    /**
     * @return the greenVulcanoPool
     */
    public GreenVulcanoPool getGreenVulcanoPool()
    {
        return this.greenVulcanoPool;
    }


    /**
     * @return the sleepTimeout
     */
    public long getSleepTimeout()
    {
        return this.sleepTimeout;
    }

    /**
     * Set the sleepTimeout
     */
    public void setSleepTimeout(long sleepTimeout)
    {
        this.sleepTimeout = sleepTimeout;
    }

    /**
     * @return the receiveTimeout
     */
    public long getReceiveTimeout()
    {
        return this.receiveTimeout;
    }

    /**
     * Set the receiveTimeout
     */
    public void setReceiveTimeout(long receiveTimeout)
    {
        this.receiveTimeout = receiveTimeout;
    }

    /**
     * @return the transacted
     */
    public boolean isTransacted()
    {
        return this.transacted;
    }


    /**
     * @return the transactionTimeout
     */
    public int getTransactionTimeout()
    {
        return this.transactionTimeout;
    }

    /**
     * Set the transactionTimeout
     */
    public void setTransactionTimeout(int transactionTimeout)
    {
        this.transactionTimeout = transactionTimeout;
    }

    /**
     * @return the dumpMessage
     */
    public boolean isDumpMessage()
    {
        return this.dumpMessage;
    }

    public void setDumpMessage(boolean dumpMessage)
    {
        this.dumpMessage = dumpMessage;
    }

    public List<Validator> getValidators() {
        return this.validators;
    }

    /**
     * @return the active
     */
    public boolean isActive()
    {
        return this.isActive.get();
    }

    public void start()
    {
        if (!inShutdown.get()) {
            logger.info("Forward [" + forwardName + "] - Restarting Listeners...");
            this.isActive.set(true);
            this.pool.incrementListeners();
        }
    }

    public void stop()
    {
        logger.info("Forward [" + forwardName + "] - Arresting Listeners...");
        this.isActive.set(false);
    }

    /**
     * @return the debug
     */
    public boolean isDebug()
    {
        return this.debug;
    }


    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    public void beginWork()
    {
        working.incrementAndGet();
        if (pool != null) {
            pool.incrementListeners();
        }
    }

    public void endWork()
    {
        working.decrementAndGet();
    }

    public int getWorkingCount()
    {
        return working.get();
    }

    public void stopListener(JMSForwardListener jmsFwd)
    {
        if (pool != null) {
            pool.rescheduleListeners(jmsFwd);
        }
    }

    @Override
    public String toString()
    {
        return descr;
    }

    public void destroy()
    {
        isActive.set(false);
        inShutdown.set(true);
        pool = null;
        if (connectionHolder != null) {
            connectionHolder.setDebug(true);
            connectionHolder.destroy();
        }
        connectionHolder = null;
        validators.clear();
    }
}

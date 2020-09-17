/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.axis2.receivers;

import it.greenvulcano.gvesb.axis2.config.Binding;
import it.greenvulcano.gvesb.axis2.config.WSOperation;
import it.greenvulcano.gvesb.axis2.config.WebServiceConf;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolException;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.internal.data.ChangeGVBuffer;
import it.greenvulcano.gvesb.j2ee.XAHelper;
import it.greenvulcano.gvesb.j2ee.XAHelperException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;

import java.util.HashMap;
import java.util.Map;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.log4j.Logger;

/**
 * @version 3.0.0 Apr 17, 2010
 * @author GreenVulcano Developer Team
 * 
 */
public class GVInOutMessageReceiver extends AbstractInOutMessageReceiver
{
    private static final Logger           logger         = GVLogger.getLogger(GVInOutMessageReceiver.class);

    private GVMessageReceiverConfigurator mrConfigurator = null;

    /**
     * @throws Exception
     * 
     */
    public GVInOutMessageReceiver() throws Exception
    {
        mrConfigurator = new GVMessageReceiverConfigurator();
        mrConfigurator.initWebServicesConfig();
    }

    /**
     * @see org.apache.axis2.receivers.AbstractInOutMessageReceiver#invokeBusinessLogic(org.apache.axis2.context.MessageContext,
     *      org.apache.axis2.context.MessageContext)
     */
    @Override
    public void invokeBusinessLogic(MessageContext input, MessageContext output) throws AxisFault
    {
        XAHelper xaHelper = null;
        boolean inError = true;
        boolean forceTxRollBack = false;
        boolean outputIsFault = false;
        String serviceName = null;
        String operationName = null;
        try {
            serviceName = input.getAxisService().getName();
            operationName = getLocalName(input.getAxisOperation().getName().toString());
            logger.info("GVInOutMessageReceiver - BEGIN service [" + serviceName + "/" + operationName + "]");
            if (logger.isDebugEnabled()) {
            	logger.debug("INPUT Envelope:\n" + input.getEnvelope().toString());
            }
            else {
            	input.getEnvelope().toString();
            }
            String inputNS = input.getEnvelope().getNamespace().getNamespaceURI();
            logger.debug("Input version URI: " + inputNS);

            try {
                mrConfigurator.checkConfig();
            }
            catch (Exception exc) {
                String errStr = "Cannot read configuration when receiving AXIS service " + serviceName;
                logger.error(errStr, exc);
                throw new AxisFault("GreenVulcano ESB: " + errStr, exc);
            }
            Map<String, WebServiceConf> webServicesConfig = mrConfigurator.getWebServicesConfig();
            if (webServicesConfig.containsKey(serviceName)) {
                WebServiceConf webService = webServicesConfig.get(serviceName);
                WSOperation selectedOperation = null;
                WSOperation[] wsOperations = webService.getWSOperations();
                if (wsOperations != null) {
                    for (WSOperation wsOperation : wsOperations) {
                        if (getLocalName(wsOperation.getOperationQname().toString()).equals(operationName)) {
                            selectedOperation = wsOperation;
                            break;
                        }
                    }
                }
                if (selectedOperation == null) {
                    String errStr = "Unknown operation for service " + serviceName + ": " + operationName;
                    logger.error(errStr);
                    throw new AxisFault("GreenVulcano ESB: " + errStr);
                }
                GreenVulcanoPool greenVulcanoPool = mrConfigurator.getGreenVulcanoPool();
                if (greenVulcanoPool == null) {
                    String errStr = "GreenVulcano ESB pool not configured for service " + serviceName;
                    logger.error(errStr);
                    throw new AxisFault("GreenVulcano ESB: " + errStr);
                }
                try {
                    Binding binding = selectedOperation.getBinding();
                    GVBuffer currentGVBuffer = new GVBuffer();

                    currentGVBuffer.setSystem(binding.getGvSystem());
                    currentGVBuffer.setService(binding.getGvService());

                    String inputType = binding.getInputType();
                    Object inObj = input;

                    if (inputType.equals("context")) {
                        // do nothing
                    }
                    else if (inputType.equals("envelope")) {
                        inObj = input.getEnvelope().toString();
                    }
                    else if (inputType.equals("body")) {
                        inObj = input.getEnvelope().getBody().toString();
                    }
                    else if (inputType.equals("body-element")) {
                        inObj = input.getEnvelope().getBody().getFirstElement().toString();
                    }
                    else if (inputType.equals("header")) {
                        inObj = input.getEnvelope().getHeader().toString();
                    }
                    else if (inputType.equals("envelope-om")) {
                        inObj = input.getEnvelope();
                    }
                    else if (inputType.equals("body-om")) {
                        inObj = input.getEnvelope().getBody();
                    }
                    else if (inputType.equals("body-element-om")) {
                        inObj = input.getEnvelope().getBody().getFirstElement();
                    }
                    else { // returnType.equals("header-om")
                        inObj = input.getEnvelope().getHeader();
                    }

                    currentGVBuffer.setObject(inObj);

                    currentGVBuffer.setProperty("WS_SERVICE", serviceName);
                    currentGVBuffer.setProperty("WS_OPERATION", operationName);
                    currentGVBuffer.setProperty("WS_REQ_SOAP_VERSION", inputNS);
                    currentGVBuffer.setProperty("WS_CONTENT_TYPE",
                            (String) input.getProperty(Constants.Configuration.MESSAGE_TYPE));
                    // get remote transport address...
                    String remAddr = (String) input.getProperty(MessageContext.REMOTE_ADDR);
                    currentGVBuffer.setProperty("WS_REMOTE_ADDR", (remAddr != null ? remAddr : ""));

                    ChangeGVBuffer cGVBuffer = binding.getcGVBuffer();
                    if (cGVBuffer != null) {
                        try {
                            currentGVBuffer = cGVBuffer.execute(currentGVBuffer, new HashMap<String, Object>());
                        }
                        catch (Exception exc) {
                            cGVBuffer.cleanUp();
                        }
                    }

                    if (binding.isTransacted()) {
                        xaHelper = new XAHelper();
                        xaHelper.begin();
                        if (binding.getTxTimeout() > 0) {
                            xaHelper.setTransactionTimeout(binding.getTxTimeout());
                        }
                        logger.debug("Begin transaction: " + xaHelper.getTransaction());
                    }

                    GVBuffer outputGVBuffer = null;
                    String gvOperation = binding.getGvOperation();
                    outputGVBuffer = greenVulcanoPool.forward(currentGVBuffer, gvOperation);

                    String refDP = selectedOperation.getRefDp();
                    if ((refDP != null) && (refDP.length() > 0)) {
                    	//logger.debug("Output version URI(in): " + (output.getEnvelope() != null ? output.getEnvelope().getNamespace().getNamespaceURI() : ""));
                        output.getOptions().setSoapVersionURI(inputNS);
                        DataProviderManager dataProviderManager = DataProviderManager.instance();
                        IDataProvider dataProvider = dataProviderManager.getDataProvider(refDP);
                        logger.debug("Calling configured Data Provider: " + dataProvider);
                        try {
                            dataProvider.setContext(output);
                            dataProvider.setObject(outputGVBuffer);
                        }
                        catch (Exception exc) {
                            String errStr = "Cannot obtain DataProvider output for service " + serviceName + ": "
                                    + refDP;
                            logger.error(errStr, exc);
                            throw new AxisFault("GreenVulcano ESB: " + errStr, exc);
                        }
                        finally {
                            dataProviderManager.releaseDataProvider(refDP, dataProvider);
                        }
                    }
                    else {
                        output.setEnvelope((SOAPEnvelope) outputGVBuffer.getObject());
                    }
                    //logger.debug("Output version URI(out): " + output.getEnvelope().getNamespace().getNamespaceURI());

                    if ("Y".equalsIgnoreCase(outputGVBuffer.getProperty("WS_FORCE_TX_ROLLBACK"))) {
                        logger.warn("Output contains WS_FORCE_TX_ROLLBACK=Y : prepare to roll back transaction");
                        forceTxRollBack = true;
                    }
                    else if (output.isFault()) {
                        logger.warn("Output is Fault : prepare to roll back transaction");
                        outputIsFault = true;
                    }
                    
                    if (logger.isDebugEnabled()) {
                    	logger.debug("OUTPUT Envelope:\n" + output.getEnvelope().toString());
                    }
                    inError = false;
                }
                catch (GVException exc) {
                    String errStr = "Cannot execute service " + serviceName + " on GreenVulcano ESB.";
                    logger.error(errStr, exc);
                    throw new AxisFault("GreenVulcano ESB: " + errStr, exc);
                }
                catch (GreenVulcanoPoolException exc) {
                    String errStr = "Cannot execute service " + serviceName + " on GreenVulcano ESB Pool.";
                    logger.error(errStr, exc);
                    throw new AxisFault("GreenVulcano ESB: " + errStr, exc);
                }
            }
            else {
                String errStr = "Service " + serviceName + " is not configured on GreenVulcanoESB.";
                logger.error(errStr);
                throw new AxisFault("GreenVulcanoESB: " + errStr);
            }
        }
        catch (AxisFault exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Unhandled exception executing web service", exc);
            throw new AxisFault("Unhandled exception executing web service", exc);
        }
        finally {
            if (xaHelper != null) {
                try {
                    if (inError || outputIsFault || forceTxRollBack) {
                        logger.warn("Rolling back transaction: " + xaHelper.getTransaction());
                        xaHelper.rollback();
                    }
                    else {
                        logger.debug("Commiting transaction: " + xaHelper.getTransaction());
                        xaHelper.commit();
                    }
                }
                catch (XAHelperException exc) {
                    logger.error("Error handling tansaction", exc);
                    throw new AxisFault("Error handling tansaction", exc);
                }
            }
            logger.info("GVInOutMessageReceiver - END service [" + serviceName + "/" + operationName + "]");
            NMDC.remove("MASTER_SERVICE");
        }
    }

    private String getNameSpace(String qname)
    {
        if (qname == null) {
            return "";
        }
        if (qname.startsWith("{") && (qname.indexOf('}') > 0)) {
            qname = qname.substring(1, qname.indexOf('}'));
        }
        return qname;
    }

    private String getLocalName(String qname)
    {
        if (qname == null) {
            return "";
        }
        if (qname.startsWith("{") && (qname.indexOf('}') > 0)) {
            qname = qname.substring(qname.indexOf('}') + 1);
        }
        return qname;
    }
}

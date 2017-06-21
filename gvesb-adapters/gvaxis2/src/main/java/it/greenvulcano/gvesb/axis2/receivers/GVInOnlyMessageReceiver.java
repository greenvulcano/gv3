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
import it.greenvulcano.gvesb.internal.data.ChangeGVBuffer;
import it.greenvulcano.gvesb.j2ee.XAHelper;
import it.greenvulcano.gvesb.j2ee.XAHelperException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.log4j.Logger;

/**
 * @version 3.0.0 Apr 17, 2010
 * @author GreenVulcano Developer Team
 * 
 */
public class GVInOnlyMessageReceiver extends AbstractInMessageReceiver
{
    private static final Logger           logger         = GVLogger.getLogger(GVInOnlyMessageReceiver.class);

    private GVMessageReceiverConfigurator mrConfigurator = null;

    /**
     * @throws Exception
     * 
     */
    public GVInOnlyMessageReceiver() throws Exception
    {
        mrConfigurator = new GVMessageReceiverConfigurator();
        mrConfigurator.initWebServicesConfig();
    }

    /**
     * @see org.apache.axis2.receivers.AbstractInMessageReceiver#invokeBusinessLogic(org.apache.axis2.context.MessageContext)
     */
    @Override
    public void invokeBusinessLogic(MessageContext input) throws AxisFault
    {
        XAHelper xaHelper = null;
        boolean inError = true;
        boolean forceTxRollBack = false;
        String serviceName = null;
        String operationName = null;
        try {
            serviceName = input.getAxisService().getName();
            operationName = getLocalName(input.getAxisOperation().getName().toString());
            logger.info("GVInOnlyMessageReceiver - BEGIN service [" + serviceName + "/" + operationName + "]");
            if (logger.isDebugEnabled()) {
            	logger.debug("INPUT Envelope:\n" + input.getEnvelope().toString());
            }
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
                for (WSOperation wsOperation : wsOperations) {
                    if (getLocalName(wsOperation.getOperationQname().toString()).equals(operationName)) {
                        selectedOperation = wsOperation;
                        break;
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
                    else if (inputType.equals("header")) { // inputType.equals("header")
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
                    else { // returnType.equals("header")
                        inObj = input.getEnvelope().getHeader();
                    }

                    currentGVBuffer.setObject(inObj);

                    currentGVBuffer.setProperty("WS_SERVICE", serviceName);
                    currentGVBuffer.setProperty("WS_OPERATION", operationName);
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

                    if ("Y".equalsIgnoreCase(outputGVBuffer.getProperty("WS_FORCE_TX_ROLLBACK"))) {
                        logger.warn("Output contains WS_FORCE_TX_ROLLBACK=Y : prepare to roll back transaction");
                        forceTxRollBack = true;
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
                    if (inError || forceTxRollBack) {
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
            logger.info("GVInOnlyMessageReceiver - END service [" + serviceName + "/" + operationName + "]");
            NMDC.remove("MASTER_SERVICE");
        }
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

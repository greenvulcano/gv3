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
package it.greenvulcano.gvesb.axis2.receivers.gvws;

import it.greenvulcano.gvesb.axis2.config.gvws.WSGreenVulcanoService;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolException;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.j2ee.XAHelper;
import it.greenvulcano.gvesb.j2ee.XAHelperException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.Map;

import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.3.0 Feb 2, 2013
 * @author GreenVulcano Developer Team
 * 
 */
public class GVMessageReceiver extends AbstractInOutMessageReceiver
{
    private static final Logger           logger           = GVLogger.getLogger(GVMessageReceiver.class);

    public static final String            GV_REQUEST_REPLY = "RequestReply";

    /**
     * GreenVulcano communication <tt>Request</tt> paradigm
     */
    public static final String            GV_REQUEST       = "Request";

    /**
     * GreenVulcano communication <tt>SendReply</tt> paradigm
     */
    public static final String            GV_SEND_REPLY    = "SendReply";

    /**
     * GreenVulcano communication <tt>GetRequest</tt> paradigm
     */
    public static final String            GV_GET_REQUEST   = "GetRequest";

    /**
     * GreenVulcano communication <tt>GetReply</tt> paradigm
     */
    public static final String            GV_GET_REPLY     = "GetReply";

    public static final String            PREFIX           = "gvesbws";
    public static final String            NAMESPACE        = "http://www.greenvulcano.it/greenvulcano";

    private GVMessageReceiverConfigurator mrConfigurator   = null;
    private Boolean                       isText           = true;

    /**
     * @throws Exception
     * 
     */
    public GVMessageReceiver() throws Exception {
        mrConfigurator = new GVMessageReceiverConfigurator();
        mrConfigurator.initWebServicesConfig();
    }

    /**
     * @see org.apache.axis2.receivers.AbstractInOutMessageReceiver#invokeBusinessLogic(org.apache.axis2.context.MessageContext,
     *      org.apache.axis2.context.MessageContext)
     */
    @Override
    public void invokeBusinessLogic(MessageContext input, MessageContext output) throws AxisFault {
        XAHelper xaHelper = null;
        boolean inError = true;
        boolean forceTxRollBack = false;
        boolean outputIsFault = false;
        try {
            String wsServiceName = input.getAxisService().getName();
            String operationName = getLocalName(input.getAxisOperation().getName().toString());
            logger.debug("operationName=" + operationName);
            try {
                mrConfigurator.checkConfig();
            }
            catch (Exception exc) {
                String errStr = "Cannot read configuration when receiving AXIS service ";
                logger.error(errStr, exc);
                throw new AxisFault("GreenVulcano ESB: " + errStr, exc);
            }

            Map<String, WSGreenVulcanoService> webServicesConfig = mrConfigurator.getWebServiceConf();

            GVBuffer currentGVBuffer = setGVBuffer(input, operationName, wsServiceName);
            String gvOperation = getGvOperation(currentGVBuffer.getProperty("GV_OPERATION"));
            logger.debug("gvoperation=" + gvOperation);
            String gvService = currentGVBuffer.getService();
            
            WSGreenVulcanoService serviceConfig = webServicesConfig.get(gvService + "--" + gvOperation);
            logger.debug("Selezionato servizio " + gvService + "--" + gvOperation);
            if (serviceConfig == null) {
                String errStr = "Service/operation " + gvService + "/" + gvOperation + " not enabled on GreenVulcano ESB WebService.";
                logger.error(errStr, null);
                throw new AxisFault("GreenVulcano ESB: " + errStr);
            }
            GreenVulcanoPool greenVulcanoPool = mrConfigurator.getGreenVulcanoPool();
            if (greenVulcanoPool == null) {
                String errStr = "GreenVulcano ESB pool not configured for service " + wsServiceName;
                logger.error(errStr);
                throw new AxisFault("GreenVulcano ESB: " + errStr);
            }

            try {
                if (serviceConfig.getTransacted()) {
                    xaHelper = new XAHelper();
                    xaHelper.begin();
                    if (serviceConfig.getTxTimeout() > 0) {
                        xaHelper.setTransactionTimeout(serviceConfig.getTxTimeout());
                    }
                    logger.debug("Begin transaction: " + xaHelper.getTransaction());
                }
                if (serviceConfig.getForceHttps() && (MessageContext.getCurrentMessageContext().getTo().toString().indexOf("https://") == -1)) {
                    throw new AxisFault("Service " + gvService + " must be called using a secure protocol on GreenVulcanoWebsservice");
                }
                String inputOdp = serviceConfig.getInputOdp();
                if ((inputOdp != null) && (inputOdp.length() > 0)) {
                    logger.debug("Calling Input configured Data Provider: " + inputOdp);
                    DataProviderManager dataProviderManager = DataProviderManager.instance();
                    IDataProvider dataProvider = dataProviderManager.getDataProvider(inputOdp);
                    try {
                        dataProvider.setContext(currentGVBuffer);
                        dataProvider.setObject(currentGVBuffer);
                    }
                    catch (Exception exc) {
                        String errStr = "Cannot obtain DataProvider input for service " + gvService + ": " + inputOdp;
                        logger.error(errStr, exc);
                        throw new AxisFault("GreenVulcano ESB: " + errStr, exc);
                    }
                    finally {
                        dataProviderManager.releaseDataProvider(inputOdp, dataProvider);
                    }
                }

                GVBuffer outputGVBuffer = greenVulcanoPool.forward(currentGVBuffer, gvOperation);

                String outputOdp = serviceConfig.getOutputOdp();
                if ((outputOdp != null) && (outputOdp.length() > 0)) {
                    logger.debug("Calling Output configured Data Provider: " + outputOdp);
                    logger.debug("Object: " + outputGVBuffer.getObject());
                    DataProviderManager dataProviderManager = DataProviderManager.instance();
                    IDataProvider dataProvider = dataProviderManager.getDataProvider(outputOdp);
                    try {
                        dataProvider.setContext(outputGVBuffer);
                        dataProvider.setObject(outputGVBuffer);
                    }
                    catch (Exception exc) {
                        String errStr = "Cannot obtain DataProvider output for service " + gvService + ": " + outputOdp;
                        logger.error(errStr, exc);
                        throw new AxisFault("GreenVulcano ESB: " + errStr, exc);
                    }
                    finally {
                        dataProviderManager.releaseDataProvider(outputOdp, dataProvider);
                    }
                }

                SOAPFactory factory = (SOAPFactory) input.getEnvelope().getOMFactory();
                SOAPEnvelope outSoapEnvelope = factory.createSOAPEnvelope();
                factory.createSOAPHeader(outSoapEnvelope);
                OMNode omNode = setOutputMessage(outputGVBuffer, operationName);
                factory.createSOAPBody(outSoapEnvelope).addChild(omNode);
                output.setEnvelope(outSoapEnvelope);

                if ("Y".equalsIgnoreCase(outputGVBuffer.getProperty("WS_FORCE_TX_ROLLBACK"))) {
                    logger.warn("Output contains WS_FORCE_TX_ROLLBACK=Y : prepare to roll back transaction");
                    forceTxRollBack = true;
                }
                else if (output.isFault()) {
                    logger.warn("Output is Fault : prepare to roll back transaction");
                    outputIsFault = true;
                }
                inError = false;
            }
            catch (GVException exc) {
                String errStr = "Cannot execute service/operation " + gvService + "/" + gvOperation + " on GreenVulcano ESB.";
                logger.error(errStr, exc);
                throw new AxisFault("GreenVulcano ESB: " + errStr, exc);
            }
            catch (GreenVulcanoPoolException exc) {
                String errStr = "Cannot execute service/operation " + gvService + "/" + gvOperation + " on GreenVulcano ESB Pool.";
                logger.error(errStr, exc);
                throw new AxisFault("GreenVulcano ESB: " + errStr, exc);
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
            NMDC.remove("MASTER_SERVICE");
        }
    }

    /**
     * This method is used by to setGVBuffer for perform.
     * 
     * @param MessageContext
     *        input The GreenVulcano data coming from the client
     * @param operation
     *        name : RequestReply, Request...
     * @param wsServiceName
     * @return GVBuffer
     * @throws Exception
     */
    private GVBuffer setGVBuffer(MessageContext input, String operation, String wsServiceName) throws Exception {
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            Element element = org.apache.axis2.util.XMLUtils.toDOM(input.getEnvelope().getBody().getFirstElement());
            GVBuffer currentGVBuffer = new GVBuffer();
            logger.debug("INPUT=" + element.toString());
            String baseXpath = "/" + PREFIX + ":" + operation + "/" + PREFIX + ":";
            currentGVBuffer.setSystem(parser.get(element, baseXpath + "system", GVBuffer.DEFAULT_SYS));
            currentGVBuffer.setService(parser.get(element, baseXpath + "service"));
            String id = parser.get(element, baseXpath + "id");
            if ((id != null) && (id.length() > 0)) {
                currentGVBuffer.setId(new Id(id));
            }
            currentGVBuffer.setRetCode(Integer.parseInt(parser.get(element, baseXpath + "retCode", "0")));
            String text = parser.get(element, baseXpath + "object/" + PREFIX + ":Text");
            String binary = parser.get(element, baseXpath + "object/" + PREFIX + ":Binary");
            if ((text != null) && (text.length() > 0)) {
                logger.debug("Text=" + text);
                currentGVBuffer.setObject(text);
            }
            else if ((binary != null) && (binary.length() > 0)) {
                logger.debug("Binary=" + binary);
                isText = false;
                currentGVBuffer.setObject(Base64.decodeBase64(binary));
            }
            logger.debug("Object=" + currentGVBuffer.getObject());
            NodeList properties = parser.selectNodeList(element, baseXpath + "property");
            for (int i = 0; i < properties.getLength(); i++) {
                Node n = properties.item(i);
                String name = parser.get(n, PREFIX + ":name");
                String value = parser.get(n, PREFIX + ":value");
                logger.debug("Property[" + name + "]=[" + value + "]");
                currentGVBuffer.setProperty(name, value);
            }
            
            currentGVBuffer.setProperty("GV_OPERATION", parser.get(element, baseXpath + "operation", operation));

            currentGVBuffer.setProperty("WS_SERVICE", wsServiceName);
            currentGVBuffer.setProperty("WS_OPERATION", operation);
            currentGVBuffer.setProperty("WS_CONTENT_TYPE",
                    (String) input.getProperty(Constants.Configuration.MESSAGE_TYPE));
            // get remote transport address...
            String remAddr = (String) input.getProperty(MessageContext.REMOTE_ADDR);
            currentGVBuffer.setProperty("WS_REMOTE_ADDR", (remAddr != null ? remAddr : ""));

            return currentGVBuffer;
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    /**
     * This method is used by create Soap Message output.
     * 
     * @param GVBuffer
     *        output GreenVulcano call
     * @throws Exception
     */
    private OMNode setOutputMessage(GVBuffer gvBuffer, String inputOperation) throws Exception {
        XMLUtils parser = null;
        try {
            logger.debug("OUTPUT" + gvBuffer.toString());
            
            parser = XMLUtils.getParserInstance();
            Document doc = parser.newDocument(inputOperation + "Response", PREFIX, NAMESPACE);

            if (haveResponse(inputOperation)) {
                Element root = doc.getDocumentElement();
    
                Element service = parser.insertElement(root, "service");
                parser.insertText(service, gvBuffer.getService());
    
                Element system = parser.insertElement(root, "system");
                parser.insertText(system, gvBuffer.getSystem());
    
                Element id = parser.insertElement(root, "id");
                parser.insertText(id, gvBuffer.getId().toString());
    
                Element retCode = parser.insertElement(root, "retCode");
                parser.insertText(retCode, Integer.toString(gvBuffer.getRetCode()));
    
                Element object = parser.insertElement(root, "object");
                Object gvObject = gvBuffer.getObject();
                if (gvObject == null) {
                    if (isText) {
                        parser.insertElement(object, "Text");
                    }
                    else {
                        parser.insertElement(object, "Binary");
                    }
                }
                else if (gvObject instanceof byte[]) {
                    Element binary = parser.insertElement(object, "Binary");
                    parser.insertText(binary, new String(Base64.encodeBase64((byte[]) gvObject)));
                }
                else if (gvObject instanceof String) {
                    Element text = parser.insertElement(object, "Text");
                    parser.insertCDATA(text, (String) gvObject);
                }
                else if (gvObject instanceof Node) {
                    Element text = parser.insertElement(object, "Text");
                    parser.insertCDATA(text, XMLUtils.serializeDOM_S((Node) gvObject));
                }
                else {
                    Element text = parser.insertElement(object, "Text");
                    parser.insertCDATA(text, gvObject.toString());
                }
    
                if (gvBuffer.getPropertyNames().length > 0) {
                    for (String name : gvBuffer.getPropertyNames()) {
                        Element property = parser.insertElement(root, "property");
                        Element pName = parser.insertElement(property, "name");
                        parser.insertText(pName, name);
                        Element pValue = parser.insertElement(property, "value");
                        parser.insertText(pValue, gvBuffer.getProperty(name));
                    }
                }
            }

            OMNode omNode = org.apache.axis2.util.XMLUtils.toOM(doc.getDocumentElement());
            return omNode;
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }


    private String getLocalName(String qname) {
        if (qname == null) {
            return "";
        }
        if (qname.startsWith("{") && (qname.indexOf('}') > 0)) {
            qname = qname.substring(qname.indexOf('}') + 1);
        }
        return qname;
    }

    /**
     * @return the gvOperation
     */
    private String getGvOperation(String operation) {
        String gvOperation = null;
        if (operation.equals("requestReply"))
            gvOperation = GV_REQUEST_REPLY;
        else if (operation.equals("request"))
            gvOperation = GV_REQUEST;
        else if (operation.equals("getReply"))
            gvOperation = GV_GET_REPLY;
        else if (operation.equals("sendReply"))
            gvOperation = GV_SEND_REPLY;
        else if (operation.equals("getRequest"))
            gvOperation = GV_GET_REQUEST;
        else
            gvOperation = operation;
        return gvOperation;
    }
    
    /**
     * @return the gvOperation
     */
    private boolean haveResponse(String operation) {
        if (operation.equals("requestReply"))
            return true;
        else if (operation.equals("getReply"))
            return true;
        else if (operation.equals("getRequest"))
            return true;
        else if (operation.equals("execute"))
            return true;
        return false;
    }

    /**
     * @return isText
     */
    public Boolean getIsText() {
        return isText;
    }

    public void setIsText(Boolean isText) {
        this.isText = isText;
    }
}
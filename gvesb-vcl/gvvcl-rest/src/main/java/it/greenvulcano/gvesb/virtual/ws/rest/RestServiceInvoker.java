/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.virtual.ws.rest;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.http.auth.HttpAuth;
import it.greenvulcano.gvesb.http.auth.HttpAuthFactory;
import it.greenvulcano.gvesb.http.proxy.HttpProxy;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.ws.invoker.ResponseMode;
import it.greenvulcano.gvesb.virtual.ws.invoker.RestDynamicInvoker;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.net.URL;
import java.util.Map;

import org.apache.axiom.attachments.Attachments;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @version 3.4.0 Jul 17, 2013
 * @author GreenVulcano Developer Team
 * 
 */
public class RestServiceInvoker
{
    /**
     * The log4j logger
     */
    private static final Logger logger       = GVLogger.getLogger(RestServiceInvoker.class);

    /**
     * The reference to the data provider configured
     */
    private String              refDP        = null;

    private String              endPoint     = "";

    private String              mediaType    = "";

    private String              contentType  = "";

    private String              method       = "";

    private ResponseMode        responseMode = ResponseMode.OUT_IN;

    private long                timeout      = -1;

    private String              returnType   = "";

    private boolean             throwsFault  = false;
    
    private HttpProxy           proxy        = null;
    private HttpAuth            auth         = null;

    /**
     * It initializes the object whit the his configuration.
     * 
     * @param configNode
     *        org.w3c.dom.Node configuration
     * @throws WSCallException
     *         if an error occurred.
     */
    public void init(Node configNode) throws WSCallException
    {
        logger.debug("BEGIN init(Configuration config)");

        try {
            timeout = XMLConfig.getLong(configNode, "@timeout", -1);
            returnType = XMLConfig.get(configNode, "@returnType", "context");
            throwsFault = XMLConfig.getBoolean(configNode, "@throwsFault", false);

            endPoint = XMLConfig.get(configNode, "@endpoint");
            mediaType = XMLConfig.get(configNode, "@mediaType", "application/x-www-form-urlencoded");
            contentType = XMLConfig.get(configNode, "@contentType", mediaType);
            String respModeStr = XMLConfig.get(configNode, "@responseMode");
            if (respModeStr != null) {
                responseMode = ResponseMode.valueOf(respModeStr);
            }
            method = XMLConfig.get(configNode, "@method", "GET");
            if (logger.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("The RestInvoker initialization parameters value:\n'");
                sb.append("\treturnType '").append(returnType).append("'\n");
                logger.debug(sb.toString());
            }

            refDP = XMLConfig.get(configNode, "@ref-dp", "");
            
            proxy = new HttpProxy();
            proxy.init(XMLConfig.getNode(configNode, "Proxy"));
            
            auth = HttpAuthFactory.getInstance(XMLConfig.getNode(configNode, "*[@type='http-auth']"));
        }
        catch (Exception exc) {
            logger.error("Exception: " + exc, exc);
            throw new WSCallException("GVVM_WS_INIT_ERROR:" + exc, exc);
        }
        logger.debug("END init(Node configNode)");
    }

    /**
     * It performs the invocation of the web service.
     * 
     * @param gvBuffer
     *        the input GVBuffer
     * @return the output GVBuffer
     * @throws WSCallException
     *         if an error occurred.
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws WSCallException
    {
        logger.debug("BEGIN perform(GVBuffer data)");
        GVBuffer output = null;

        RestDynamicInvoker invoker = null;
        try {
            logger.debug("Create and initialize the Axis2 REST Invoker");
            // Axis2 REST Invoker
            invoker = RestDynamicInvoker.getInvoker(endPoint);
            invoker.setContentType(contentType);
            invoker.setMediaType(mediaType);
            invoker.setMethod(method);
            invoker.setResponseMode(responseMode);
            invoker.setTimeout(timeout);
            invoker.setThrowsFault(throwsFault);

            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            Object result = null;

            MessageContext messageContext = null;
            OperationClient operationClient = invoker.prepareOperationClient();

            if ((refDP != null) && (refDP.length() > 0)) {
                logger.debug("Calling configured Data Provider: " + refDP);
                DataProviderManager dataProviderManager = DataProviderManager.instance();
                IDataProvider dataProvider = dataProviderManager.getDataProvider(refDP);
                try {
                    dataProvider.setContext(operationClient.getOptions());
                    dataProvider.setObject(gvBuffer);
                    messageContext = (MessageContext) dataProvider.getResult();
                }
                finally {
                    dataProviderManager.releaseDataProvider(refDP, dataProvider);
                }
            }
            else {
                messageContext = (MessageContext) gvBuffer.getObject();
            }

            String wsEp = gvBuffer.getProperty("WS_ENDPOINT_URL");
            if (wsEp == null) {
                wsEp = operationClient.getOptions().getTo().getAddress();
            }
            wsEp = PropertiesHandler.expand(wsEp, params, gvBuffer);

            URL ws = new URL(wsEp);
            String host = ws.getHost();
            /*int port = ws.getPort();
            if (port == -1) {
                port = ws.getDefaultPort();
            }*/
            auth.setAuthentication(messageContext.getOptions(), host, gvBuffer, params);
            proxy.setProxy(messageContext.getOptions(), gvBuffer, params);

            // Invoke the web service
            result = invoker.execute(operationClient, messageContext, wsEp);
            if (result != null) {
                MessageContext resMCtx = (MessageContext) result;
                if (!resMCtx.getAttachmentMap().getMap().isEmpty()) {
                    Attachments atts = resMCtx.getAttachmentMap();
                    atts.getAllContentIDs();
                }

                if (returnType.equals("context")) {
                    // do nothing
                }
                else if (returnType.equals("envelope")) {
                    result = resMCtx.getEnvelope().toString();
                }
                else if (returnType.equals("body")) {
                    result = resMCtx.getEnvelope().getBody().toString();
                }
                else if (returnType.equals("body-element")) {
                    result = resMCtx.getEnvelope().getBody().getFirstElement().toString();
                }
                else if (returnType.equals("header")) {
                    result = resMCtx.getEnvelope().getHeader().toString();
                }
                else if (returnType.equals("envelope-om")) {
                    result = resMCtx.getEnvelope();
                }
                else if (returnType.equals("body-om")) {
                    result = resMCtx.getEnvelope().getBody();
                }
                else if (returnType.equals("body-element-om")) {
                    result = resMCtx.getEnvelope().getBody().getFirstElement();
                }
                else { // returnType.equals("header-om")
                    result = resMCtx.getEnvelope().getHeader();
                }
            }
            output = new GVBuffer(gvBuffer);
            output.setObject(result);
        }
        catch (Exception exc) {
            logger.error("Exception on perform the operation call: " + exc, exc);
            throw new WSCallException("GVM_WS_ERROR", new String[][]{{"exception", "" + exc}}, exc);
        }
        finally {
            RestDynamicInvoker.returnInvoker(invoker);
        }
        logger.debug("END perform(GVBuffer data)");
        return output;
    }

    /**
     * Used to force <code>DynamicInvoker</code> to clean up invokers using the
     * current WSDL
     */
    public void destroy()
    {
        RestDynamicInvoker.reload(endPoint);
    }

    /**
     * Does nothing
     */
    public void cleanUp()
    {
        // do nothing
    }
}
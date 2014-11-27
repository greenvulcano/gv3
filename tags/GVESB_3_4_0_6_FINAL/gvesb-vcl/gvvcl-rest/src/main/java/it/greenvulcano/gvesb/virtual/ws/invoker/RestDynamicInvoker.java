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
package it.greenvulcano.gvesb.virtual.ws.invoker;

import it.greenvulcano.gvesb.virtual.ws.rest.WSCallException;
import it.greenvulcano.gvesb.ws.axis2.context.Axis2ConfigurationContextHelper;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.log4j.Logger;


/**
 * @version 3.4.0 Jul 19, 2013
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class RestDynamicInvoker
{
    private final static Logger                            logger        = GVLogger.getLogger(RestDynamicInvoker.class);

    private static Map<String, Stack<RestDynamicInvoker>>  invokersCache = new LinkedHashMap<String, Stack<RestDynamicInvoker>>();
    private static Map<String, Vector<RestDynamicInvoker>> inUseInvokers = new LinkedHashMap<String, Vector<RestDynamicInvoker>>();

    private String                                         _restEPR      = null;
    private String                                         _contentType;
    private String                                         _mediaType;
    private String                                         _method;
    private boolean                                        throwsFault   = false;

    private long                                           timeout       = -1;

    private ServiceClient                                  client        = null;

    private ResponseMode                                   responseMode;

    /**
     * Class constructor.
     * 
     * @param restEPR
     *        The REST End Point Reference
     * @throws WSCallException
     *         if an error occurred
     */
    private RestDynamicInvoker(String restEPR)
    {
        _restEPR = restEPR;
    }

    /**
     * @param restEpr
     * @return the cached invoker
     * @throws WSCallException
     */
    public static synchronized RestDynamicInvoker getInvoker(String restEpr) throws WSCallException
    {
        if (restEpr == null) {
            throw new WSCallException("EPR_LOCATION_NULL", new String[][]{{"cause",
                    "Rest EPR Location parameter cannot be null."}});
        }
        Stack<RestDynamicInvoker> invokersStack = invokersCache.get(restEpr);
        if (invokersStack == null) {
            invokersStack = new Stack<RestDynamicInvoker>();
            invokersCache.put(restEpr, invokersStack);
        }

        // Get from cache if present
        RestDynamicInvoker invoker = null;
        if (!invokersStack.isEmpty()) {
            invoker = invokersStack.pop();
        }
        // else create new
        if (invoker == null) {
            invoker = new RestDynamicInvoker(restEpr);
        }

        // put in inUse map
        Vector<RestDynamicInvoker> inUseVector = inUseInvokers.get(restEpr);
        if (inUseVector == null) {
            inUseVector = new Vector<RestDynamicInvoker>();
            inUseInvokers.put(restEpr, inUseVector);
        }
        inUseVector.add(invoker);

        return invoker;
    }

    /**
     * @param invoker
     */
    public synchronized static void returnInvoker(RestDynamicInvoker invoker)
    {
        if (invoker != null) {
            invoker.cleanup();
            String wsdlLocation = invoker.getEndPointReference();
            Vector<RestDynamicInvoker> inUseVector = inUseInvokers.get(wsdlLocation);
            if (inUseVector != null) {
                // if invoker was used and has not been discarded
                // must be reinserted in invokers stack to get reused
                if (inUseVector.remove(invoker)) { // && !invoker.discard
                    Stack<RestDynamicInvoker> invokersStack = invokersCache.get(wsdlLocation);
                    if (invokersStack == null) {
                        invokersStack = new Stack<RestDynamicInvoker>();
                        invokersCache.put(invoker.getEndPointReference(), invokersStack);
                    }
                    invokersStack.push(invoker);
                }
            }
        }
    }

    private String getEndPointReference()
    {
        return _restEPR;
    }

    /**
     * @param mediaType
     * @throws WSCallException
     */
    public void setMediaType(String mediaType) throws WSCallException
    {
        if (mediaType == null) {
            throw new WSCallException("MEDIA_TYPE_NULL", new String[][]{{"cause", "Media type cannot be null"}});
        }
        _mediaType = mediaType;
    }

    /**
     * @param mediaType
     * @throws WSCallException
     */
    public void setContentType(String contentType) throws WSCallException
    {
        if (contentType == null) {
            throw new WSCallException("CONTENT_TYPE_NULL", new String[][]{{"cause", "Content type cannot be null"}});
        }
        _contentType = contentType;
    }

    /**
     * @param method
     * @throws WSCallException
     */
    public void setMethod(String method) throws WSCallException
    {
        if (method == null) {
            throw new WSCallException("HTTP_METHOD_NULL", new String[][]{{"cause", "HTTP method cannot be null"}});
        }
        _method = method;
    }

    /**
     * @param timeout
     */
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    /**
     * @return the loaded WSDLs
     */
    public synchronized static String[] getLoadedRESTEPR()
    {
        try {
            Set<String> keys = invokersCache.keySet();
            String[] arr = new String[keys.size()];
            keys.toArray(arr);
            return arr;
        }
        catch (Exception exc) {
            logger.warn("JMX: Error while returning the list of loaded WSDL files", exc);
            return new String[0];
        }
    }

    /**
     * @return the number of invokers actually in use.
     */
    public synchronized static Map<String, Integer> getInUseInvokers()
    {
        try {
            Map<String, Integer> ret = new HashMap<String, Integer>();
            Iterator<String> itr = inUseInvokers.keySet().iterator();
            while (itr.hasNext()) {
                String key = itr.next();
                Vector<?> value = inUseInvokers.get(key);
                ret.put(key, new Integer(value.size()));
            }
            return ret;
        }
        catch (Exception exc) {
            logger.warn("JMX: Error while returning the list of in-use invokers", exc);
            return new HashMap<String, Integer>();
        }
    }

    /**
     * @return the number of invokers actually in cache.
     */
    public synchronized static Map<String, Integer> getInCacheInvokers()
    {
        try {
            HashMap<String, Integer> ret = new HashMap<String, Integer>();
            Iterator<String> itr = invokersCache.keySet().iterator();
            while (itr.hasNext()) {
                String key = itr.next();
                Vector<?> value = invokersCache.get(key);
                ret.put(key, new Integer(value.size()));
            }
            return ret;
        }
        catch (Exception exc) {
            logger.warn("JMX: Error while returning the list of cached invokers", exc);
            return new LinkedHashMap<String, Integer>();
        }
    }

    /**
     * @param endpoint
     */
    public synchronized static void reload(String endpoint)
    {
        inUseInvokers.remove(endpoint);
        invokersCache.remove(endpoint);
    }

    /**
     *
     */
    public synchronized static void reloadAll()
    {
        inUseInvokers.clear();
        invokersCache.clear();
    }

    /**
     * @param messageContext
     * @return the result of the execution
     * @throws WSCallException
     */
    public MessageContext execute(MessageContext messageContext) throws WSCallException
    {
        return execute(messageContext, null);
    }

    /**
     * @return the new <code>OperationClient</code> to invoke the WebService
     * @throws WSCallException
     * @throws AxisFault
     * @throws MalformedURLException
     * @throws PropertiesHandlerException
     */
    public OperationClient prepareOperationClient() throws WSCallException, AxisFault, MalformedURLException,
            PropertiesHandlerException
    {
        client = new ServiceClient(Axis2ConfigurationContextHelper.getConfigurationContext(), null);
        QName respMode = null;
        switch (responseMode) {
            case OUT_ONLY : {
                respMode = ServiceClient.ANON_OUT_ONLY_OP;
            }
                break;
            default :
                respMode = ServiceClient.ANON_OUT_IN_OP;
        }
        logger.debug("Setting operation client response mode: " + respMode);
        OperationClient operationClient = client.createClient(respMode);
        Options options = operationClient.getOptions();

        if (timeout != -1) {
            long timeoutInMilliseconds = timeout * 1000;
            options.setTimeOutInMilliSeconds(timeoutInMilliseconds);
        }
        logger.debug("Setting REST mode");
        options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
        logger.debug("Setting REST Verb: " + _method);
        options.setProperty(Constants.Configuration.HTTP_METHOD, _method);
        logger.debug("Setting REST ContentType: " + _contentType);
        options.setProperty(Constants.Configuration.CONTENT_TYPE, _contentType);
        logger.debug("Setting REST MessageType: " + _mediaType);
        options.setProperty(Constants.Configuration.MESSAGE_TYPE, _mediaType);
        logger.debug("Setting EPR: " + _restEPR);
        options.setTo(new EndpointReference(_restEPR));

        return operationClient;
    }

    /**
     * @param messageContext
     * @param modules
     * @return the result of the execution
     * @throws WSCallException
     */
    public MessageContext execute(MessageContext messageContext, String wsEp) throws WSCallException
    {
        return execute(null, messageContext, wsEp);
    }

    /**
     * @param operationClient
     * @param messageContext
     * @param modules
     * @return the result of the execution
     * @throws WSCallException
     */
    public MessageContext execute(OperationClient operationClient, MessageContext messageContext, String wsEp)
            throws WSCallException
    {
        logger.debug("Invoking REST operation " + getEndPointReference());

        MessageContext result = null;
        debugMessageContext(messageContext, "INPUT");
        try {
            if (operationClient == null) {
                operationClient = prepareOperationClient();
            }

            Exception fault = null;
            operationClient.addMessageContext(messageContext);

            if ((wsEp != null) && !wsEp.equals("")) {
                operationClient.getOptions().setTo(new EndpointReference(wsEp));
                logger.debug("DynamicInvoker - Forced EndPoint: " + wsEp);
            }

            try {
                operationClient.execute(true);
            }
            catch (AxisFault exc) {
                fault = exc;
                logger.error("Error invoking REST operation " + getEndPointReference(), exc);
                if (throwsFault) {
                    throw new WSCallException("ERROR_INVOKING_OPERATION", new String[][]{
                            {"cause", "Error invoking REST operation " + getEndPointReference()}, {"fault", "" + exc}},
                            exc);
                }
            }
            if (responseMode != ResponseMode.OUT_ONLY) {
                result = operationClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            }

            if ((result != null) && (result.getEnvelope() != null)) {
                logger.debug("OUTPUT: " + result);
            }
            else {
                logger.debug("Service request returned with NULL message!");
                if (fault != null) {
                    throw new WSCallException("ERROR_INVOKING_OPERATION",
                            new String[][]{{"cause", "Error invoking REST operation " + getEndPointReference()},
                                    {"fault", "" + fault}}, fault);
                }
            }
        }
        catch (RemoteException re) {
            logger.error("Cannot execute service", re);
            throw new WSCallException("REMOTE_EXCEPTION_OCCURRED", new String[][]{{"cause", "Cannot execute service"}},
                    re);
        }
        catch (WSCallException e) {
            throw e;
        }
        catch (Exception e) {
            logger.error("Cannot execute service", e);
            throw new WSCallException("EXCEPTION_OCCURRED", new String[][]{{"cause", "Cannot execute service"}}, e);
        }

        if (result != null) {
            debugMessageContext(result, "OUTPUT");
        }
        else if (logger.isDebugEnabled()) {
            logger.debug("Service request returned with NULL message!");
        }
        return result;
    }

    private void debugMessageContext(MessageContext current, String desc)
    {
        if (logger.isDebugEnabled() && (current != null)) {
            StringBuilder sb = new StringBuilder(desc);
            sb.append(": {\n\tEnvelope: ").append(current.getEnvelope());
            Options options = current.getOptions();
            if (options != null) {
                Map<String, Object> properties = options.getProperties();
                if (properties != null && properties.size() > 0) {
                    sb.append("\n\tProperties:");
                    for (Map.Entry<String, Object> entry : properties.entrySet()) {
                        sb.append("\n\t\t").append(entry.getKey()).append(": ").append(entry.getValue());
                    }
                }
            }
            sb.append("\n}");
            logger.debug(sb.toString());
        }
    }

    /**
     *
     */
    public void cleanup()
    {
        try {
            if (client != null) {
                client.cleanupTransport();
            }
        }
        catch (AxisFault exc) {
            exc.printStackTrace();
        }
    }

    public boolean isThrowsFault()
    {
        return throwsFault;
    }

    public void setThrowsFault(boolean throwsFault)
    {
        this.throwsFault = throwsFault;
    }

    public void setResponseMode(ResponseMode responseMode)
    {
        this.responseMode = responseMode;
    }
}

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
package it.greenvulcano.gvesb.axis2.dispatchers;

import it.greenvulcano.log.GVLogger;

import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.dispatchers.AbstractServiceDispatcher;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.Utils;
import org.apache.log4j.Logger;

/**
 * @version 3.2.0 17/feb/2012
 * @author GreenVulcano Developer Team
 */
public class RequestURIBasedServiceDispatcher extends AbstractServiceDispatcher
{
    public static final String  NAME   = "GVRequestURIBasedServiceDispatcher";
    private static final Logger logger = GVLogger.getLogger(RequestURIBasedServiceDispatcher.class);

    /**
     * 
     */
    public RequestURIBasedServiceDispatcher()
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.dispatchers.AbstractServiceDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    @Override
    public AxisService findService(MessageContext messageContext) throws AxisFault
    {
        EndpointReference toEPR = messageContext.getTo();
        if (toEPR != null) {
            if (logger.isDebugEnabled()) {
                logger.debug((new StringBuilder()).append(messageContext.getLogIDString()).append(
                        " Checking for Service using target endpoint address : ").append(toEPR.getAddress()).toString());
            }
            String filePart = toEPR.getAddress();
            ConfigurationContext configurationContext = messageContext.getConfigurationContext();
            String values[] = Utils.parseRequestURLForServiceAndOperation(filePart,
                    messageContext.getConfigurationContext().getServicePath());
            if ((values.length >= 1) && (values[0] != null)) {
                AxisConfiguration registry = configurationContext.getAxisConfiguration();
                AxisService axisService = registry.getService(values[0]);
                if (axisService != null) {
                    Map<String, AxisEndpoint> endpoints = axisService.getEndpoints();
                    if (endpoints != null) {
                        if (endpoints.size() == 1) {
                            messageContext.setProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME, endpoints.get(axisService.getEndpointName()));
                        }
                        else {
                            String endpointName = values[0].substring(values[0].indexOf(".") + 1);
                            messageContext.setProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME, endpoints.get(endpointName));
                        }
                    }
                }
                return axisService;
            }
            if (logger.isDebugEnabled()) {
                logger.debug((new StringBuilder()).append(messageContext.getLogIDString()).append(
                        " Attempted to check for Service using target endpoint URI, but the service fragment was missing").toString());
            }
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug((new StringBuilder()).append(messageContext.getLogIDString()).append(
                    " Attempted to check for Service using null target endpoint URI").toString());
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.dispatchers.AbstractServiceDispatcher#initDispatcher()
     */
    @Override
    public void initDispatcher()
    {
        init(new HandlerDescription(NAME));
    }

}

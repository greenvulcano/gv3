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
package it.greenvulcano.gvesb.axis2.servlets;

import it.greenvulcano.gvesb.identity.GVIdentityHelper;
import it.greenvulcano.gvesb.identity.impl.HTTPIdentityInfo;
import it.greenvulcano.gvesb.ws.axis2.context.Axis2ConfigurationContextHelper;
import it.greenvulcano.log.GVLogger;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.AxisServlet;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;

/**
 * GVAxisServlet class.
 * 
 * @version 3.0.0 Apr 1, 2010
 * @author nunzio
 * 
 */
public class GVAxisServlet extends AxisServlet
{
    private static final long     serialVersionUID = -5175240846209837248L;

    private static final Logger   logger           = GVLogger.getLogger(GVAxisServlet.class);
    private static boolean        isFirst          = true;

    private Map<String, String[]> eprMap           = new HashMap<String, String[]>();

    /**
     * @see org.apache.axis2.transport.http.AxisServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(new ServletConfigWrapper(config));
        if (isFirst && (this.configContext != null)) {
            synchronized (GVAxisServlet.class) {
                if (isFirst) {
                    ConfigurationContext context = this.configContext;
                    context.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, true);
                    context.setProperty(HTTPConstants.AUTO_RELEASE_CONNECTION, false);
                    MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
                    HttpConnectionManagerParams params = new HttpConnectionManagerParams();
                    params.setDefaultMaxConnectionsPerHost(50); //Set this value as per your need.
                    params.setMaxTotalConnections(200);
                    multiThreadedHttpConnectionManager.setParams(params);
                    HttpClient httpClient = new HttpClient(multiThreadedHttpConnectionManager);
                    context.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);
                    isFirst = false;
                }
            }
        }
        Axis2ConfigurationContextHelper.setConfigurationContext(this.configContext);

        Enumeration<?> initParameterNames = this.servletConfig.getInitParameterNames();
        while (initParameterNames.hasMoreElements()) {
            String paramName = (String) initParameterNames.nextElement();
            if (paramName != null && paramName.startsWith("gv.url.remap")) {
                String paramValue = servletConfig.getInitParameter(paramName);
                if (paramValue != null && paramValue.indexOf(';') > -1 && paramValue.indexOf(';') < paramValue.length()) {
                    String target = paramValue.substring(0, paramValue.indexOf(';'));
                    String mapList = paramValue.substring(paramValue.indexOf(';') + 1);
                    eprMap.put(target, mapList.split(":"));
                }
            }
        }
    }

    /**
     * @see org.apache.axis2.transport.http.AxisServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException
    {
        try {
            // Create and insert the caller in the security context
            GVIdentityHelper.push(new HTTPIdentityInfo(request));

            super.doPost(request, response);
        }
        finally {
            // Remove the caller in the security context
            GVIdentityHelper.pop();
        }
    }

    /**
     * @see org.apache.axis2.transport.http.AxisServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try {
            // Create and insert the caller in the security context
            GVIdentityHelper.push(new HTTPIdentityInfo(request));

            super.doGet(request, response);
        }
        finally {
            // Remove the caller in the security context
            GVIdentityHelper.pop();
        }
    }

    protected MessageContext createMessageContext(HttpServletRequest request, HttpServletResponse response,
            boolean invocationType) throws IOException
    {
        MessageContext messageContext = super.createMessageContext(request, response, invocationType);
        EndpointReference to = messageContext.getTo();
        if (to != null) {
            String toAddr = to.getAddress();
            to.setAddress(rewrite(toAddr, messageContext.getConfigurationContext().getContextRoot()));
        }
        return messageContext;
    }

    private String rewrite(String toAddr, String contextRoot)
    {
        String ret = toAddr;
        for (Entry<String, String[]> entry : eprMap.entrySet()) {
            String[] mapList = entry.getValue();
            for (String url : mapList) {
                if (toAddr.startsWith(contextRoot + url)) {
                    ret = contextRoot + entry.getKey() + toAddr.substring((contextRoot + url).length());
                    logger.debug("Remapped URL from [" + toAddr + "] to [" + ret + "]");
                    break;
                }
            }
        }
        return ret;
    }
}
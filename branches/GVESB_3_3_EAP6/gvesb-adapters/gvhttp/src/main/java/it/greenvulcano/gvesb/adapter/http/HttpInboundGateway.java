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
package it.greenvulcano.gvesb.adapter.http;

import it.greenvulcano.gvesb.adapter.http.exc.InboundHttpResponseException;
import it.greenvulcano.gvesb.adapter.http.formatters.handlers.AdapterHttpConfigurationException;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpConstants;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpException;
import it.greenvulcano.gvesb.identity.GVIdentityHelper;
import it.greenvulcano.gvesb.identity.impl.HTTPIdentityInfo;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This class implements an entry point into GreenVulcano for external systems
 * which communicate via HTTP protocol.
 * <p>
 * There will be an instance of this class for each external system
 * communicating via HTTP.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class HttpInboundGateway extends HttpServlet
{
    private static final long         serialVersionUID = -6337761728589142613L;

    private static Logger             logWriter        = GVLogger.getLogger(HttpInboundGateway.class);

    private HttpServletMappingManager mappingManager   = null;

    /**
     * Initialization method.
     * 
     * @param config
     *        the <tt>ServletConfig</tt> object.
     * @throws ServletException
     *         if any error occurs.
     */
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        NMDC.push();
        NMDC.clear();
        NMDC.setSubSystem(AdapterHttpConstants.SUBSYSTEM);
        NMDC.setServer(JMXEntryPoint.getServerName());
        try {
            super.init(config);
            logWriter.debug("HttpInboundGateway - BEGIN init");

            mappingManager = new HttpServletMappingManager();

            logWriter.debug("HttpInboundGateway - END init");
        }
        catch (ServletException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            if (mappingManager != null) {
                mappingManager.destroy();
                mappingManager = null;
            }
            logWriter.error("HttpInboundGateway - Unexpected error: ", exc);
            throw new ServletException("Unexpected error: ", exc);
        }
        finally {
            NMDC.pop();
        }
    }

    /**
     * Cleanup method.
     */
    @Override
    public void destroy()
    {
        if (mappingManager != null) {
            mappingManager.destroy();
            mappingManager = null;
        }
        logWriter.debug("AdapterHTTP inbound gateway stopped");
    }

    /**
     * Handle HTTP requests from external system submitted with method GET.
     * 
     * @param req
     *        An HttpServletRequest object
     * @param resp
     *        An HttpServletResponse object
     * @throws ServletException
     *         if any error occurs.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        perform("doGet", req, resp);
    }

    /**
     * Handle HTTP requests from external system submitted with method POST.
     * 
     * @param req
     *        An HttpServletRequest object
     * @param resp
     *        An HttpServletResponse object
     * @throws ServletException
     *         if any error occurs.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        perform("doPost", req, resp);
    }

    /**
     * @param req
     * @param resp
     * @throws ServletException
     */
    private void perform(String method, HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        long startTime = 0;
        long endTime = 0;
        long totalTime = 0;

        NMDC.push();
        NMDC.clear();
        NMDC.setServer(JMXEntryPoint.getServerName());
        NMDC.setSubSystem(AdapterHttpConstants.SUBSYSTEM);
        String mapping = req.getServletPath().substring(1);
        String gvAction = req.getParameter("GV_ACTION");
        gvAction = (gvAction != null) ? gvAction : "NULL";
        Level level = Level.INFO;
        AdapterHttpException exception = null;
        HttpServletMapping smapping = null;

        try {
            startTime = System.currentTimeMillis();
            NMDC.put("HTTP_ACTION", gvAction);
            logWriter.info(method + " - BEGIN " + mapping + "/" + gvAction);

            // Create and insert the caller in the security context
            GVIdentityHelper.push(new HTTPIdentityInfo(req));

            smapping = mappingManager.getMapping(gvAction);
            if (!smapping.handleRequest(req, resp)) {
                level = Level.ERROR;
            }
        }
        catch (AdapterHttpConfigurationException exc) {
            level = Level.ERROR;
            logWriter.error(method + " " + mapping + "/" + gvAction + " - Can't handle request from client system", exc);
            exception = exc;
            throw new ServletException("Can't handle request from client system - " + mapping + "/" + gvAction, exc);
        }
        catch (InboundHttpResponseException exc) {
            level = Level.ERROR;
            logWriter.error(method + " " + mapping + "/" + gvAction + " - Can't send response to client system: " + exc);
            exception = exc;
            throw new ServletException("Can't send response to client system - " + mapping + "/" + gvAction, exc);
        }
        finally {
        	try {
	            endTime = System.currentTimeMillis();
	            totalTime = endTime - startTime;
	            if (exception != null) {
	            	GVFormatLog gvFormatLog = GVFormatLog.formatENDOperation(exception, totalTime);
	                logWriter.log(level, gvFormatLog);
	            }
	            logWriter.log(level, method + " - END " + mapping + "/" + gvAction + " - ExecutionTime (" + totalTime + ")");
        	}
        	finally {
        		// Remove the caller from the security context
        		GVIdentityHelper.pop();

        		NMDC.pop();
        	}
        }
    }
}

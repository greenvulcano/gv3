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

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis2.transport.http.AxisServlet;

/**
 * GVAxisServlet class.
 * 
 * @version 3.0.0 Apr 1, 2010
 * @author nunzio
 * 
 */
public class GVAxisServlet extends AxisServlet
{
    private static final long serialVersionUID = -5175240846209837248L;

    /**
     * @see org.apache.axis2.transport.http.AxisServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(new ServletConfigWrapper(config));
        Axis2ConfigurationContextHelper.setConfigurationContext(this.configContext);
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException
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

}
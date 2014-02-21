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
package it.greenvulcano.gvesb.gvconsole.workbench.servlet;

import it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestManager;
import it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPluginWrapper;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <code>SetParameters</code> servelt get the Ejb parameters inserted in the
 * setEjb.jsp <br/>
 * and set them in the plugin Object and in the configuration file.
 * <p>
 * The action required by the user is :
 * <p>
 * - Confirm set the parameters in the xml configuration file with the parameter
 * values of JSP
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class SetParameters extends HttpServlet
{

    private static final long serialVersionUID = 300L;

    /**
     * Do post method
     *
     * @param request
     *        The HttpServletRequest object
     * @param response
     *        The HttpServletResponse object
     * @throws ServletException
     *         If an error occurred
     * @throws IOException
     *         If an error occurred
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        TestManager testManager = new TestManager(request);
        TestPluginWrapper wrapper = null;
        try {
            /**
             * Clean the previous Throwable object.
             */
            wrapper = testManager.getWrapper();
            wrapper.setThrowable(null);

            /**
             * Set the Ejb parameters in the Plugin Object getting value from
             * jsp
             */
            setEjbParameters(testManager, request);

            response.sendRedirect("testing/index.jsp");
        }
        catch (Throwable exc) {
            // Set the Throwable object of the wrapper class to shows it.
            //
            wrapper.setThrowable(exc);
        }
    }

    /**
     * Set the EJB parameters calling the set method which captures the set
     * method to invoke for each parameter.
     *
     * @param testManager
     *        Utility class to invoke the set method required
     * @param request
     *        HttpServletRequest to get the ejb parameters
     * @throws Throwable
     *         If an error occurred
     */
    public void setEjbParameters(TestManager testManager, HttpServletRequest request) throws Throwable
    {
        testManager.set("jndiName", request.getParameter("jndiName"));
        testManager.set("jndiFactory", request.getParameter("jndiFactory"));
        testManager.set("providerUrl", request.getParameter("providerUrl"));
        testManager.set("user", request.getParameter("user"));
        testManager.set("password", request.getParameter("password"));
    }
}
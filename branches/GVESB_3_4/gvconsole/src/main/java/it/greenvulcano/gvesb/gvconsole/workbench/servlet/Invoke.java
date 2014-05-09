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

import it.greenvulcano.jmx.JMXUtils;

import java.io.IOException;
import java.net.URLEncoder;

import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <code>Invoke</code> servelt get the action requested form user and invokes
 * the specific method.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class Invoke extends HttpServlet
{
    /**
     *
     */
    private static final long serialVersionUID = 300L;

    /**
     * The <code>doPost</code> method of this <code>Invoke</code> servlet invoke
     * a requested method for the xml configuration object
     *
     * @param request
     *        The HttpServletRequest
     * @param response
     *        The HttpServletResponse
     * @throws ServletException
     *         If an error occurred
     * @throws IOException
     *         If an error occurred
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try {
            // Get parameters from jsp
            //
            String method = request.getParameter("method");
            String onameStr = request.getParameter("objectName");
            String inputFile = request.getParameter("inputFile");

            // Query String
            //
            ObjectName oname = new ObjectName(onameStr);

            if (method != null) {
                if (!method.equals("reloadPage")) {

                    // Create the array for the server.invoke method
                    //
                    Object[] param = null; // Configuration files
                    String[] sign = null; // Input type

                    if (inputFile != null) {
                        if (inputFile.equals("")) {
                            param = new Object[0];
                            sign = new String[0];
                        }
                        else {
                            param = new Object[1];
                            param[0] = inputFile;

                            sign = new String[1];
                            sign[0] = "java.lang.String";
                        }
                    }
                    else {
                        request.setAttribute("exc", "Input File is null.");
                        request.getRequestDispatcher("/gvcon/jsp/exceptionJmx.jsp").forward(request, response);
                    }

                    // Object result = JMXUtils.invoke(onameStr, method, param,
                    // sign, true, null);
                    JMXUtils.invoke(onameStr, method, param, sign, true, null);
                }
            }
            else {
                request.setAttribute("exc", "Method is null.");
                request.getRequestDispatcher("../gvesb/jsp/exceptionJmx.jsp").forward(request, response);
            }

            String encodeOname = URLEncoder.encode("" + oname, "UTF-8");
            response.sendRedirect("/gvcon/jsp/jmxObject.jsp?oname=" + encodeOname);

        }
        catch (Throwable exc) {
            String exception = "" + exc;
            request.setAttribute("exc", exception);
            request.getRequestDispatcher("/gvcon/jsp/exceptionJmx.jsp").forward(request, response);
        }
    }
}
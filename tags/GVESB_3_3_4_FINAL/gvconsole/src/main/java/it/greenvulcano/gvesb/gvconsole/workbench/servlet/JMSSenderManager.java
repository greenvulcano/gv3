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

/**
 * Standard Java imports.
 */
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * JMSSenderManager class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class JMSSenderManager extends HttpServlet
{
    /**
     *
     */
    private static final long serialVersionUID = 300L;
    private String            bodyObjectName   = "";

    /**
     * This method do :</br> <li>call the Plugin method requested by user.</li>
     * <li>Prepare the Initial context</li> <li>Start the transaction</li>
     *
     * @param request
     *        HttpServletRequest object
     * @param response
     *        HttpServletResponse object
     * @throws ServletException
     *         If an error occurred
     * @throws IOException
     *         If an error occurred
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // HttpSession session = request.getSession();

        bodyObjectName = request.getParameter("bodyObjectName");
        valorizeObject(request);
        response.sendRedirect("jsp/body.jsp");
    }

    /**
     * This method manage the GVMonitorin plugin.
     *
     * @param request
     *        The HttpServletRequest object
     */
    private void valorizeObject(HttpServletRequest request)
    {
        Class<?> classObject;

        try {
            classObject = Class.forName(bodyObjectName);

            Object object = classObject.newInstance();
            Class<?> obtainedClass = object.getClass();
            obtainedClass.getDeclaredMethods();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
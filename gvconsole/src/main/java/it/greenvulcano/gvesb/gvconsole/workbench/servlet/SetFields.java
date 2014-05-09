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

import it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestGVBufferObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This class set values for properties.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class SetFields extends HttpServlet
{
    /**
     *
     */
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
        HttpSession session = request.getSession();
        String fields = request.getParameter("fields");
        if (fields != null) {
            if (fields.equals("property")) {
                manageProperties(request, session);
            }
            else if (fields.equals("internal")) {
                manageInternalFields(request, session);
            }
        }

        response.sendRedirect("testing/reloadAndClose.jsp");
    }

    /**
     * Manage the properties for GVBuffer object
     *
     * @param request
     *        the HttpServletRequest object
     * @param session
     *        the HttpSession object
     */
    private void manageProperties(HttpServletRequest request, HttpSession session)
    {
        List<String> listName = new ArrayList<String>();
        List<String> listValue = new ArrayList<String>();
        int i = 0;

        while (request.getParameter("property" + i) != null) {
            listName.add(request.getParameter("property" + i));
            listValue.add(request.getParameter("propValue" + i));
            i++;
        }

        i = 1;
        while (request.getParameter("propertyName" + i) != null) {
            listName.add(request.getParameter("propertyName" + i));
            listValue.add(request.getParameter("propertyValue" + i));
            i++;
        }

        String[] extName = new String[listName.size()];
        String[] extValue = new String[listValue.size()];
        listName.toArray(extName);
        listValue.toArray(extValue);

        String ind = request.getParameter("ind");
        Map<?, ?> mapTestObject = (Map<?, ?>) session.getAttribute("mapTestObject");
        TestGVBufferObject testObject = (TestGVBufferObject) mapTestObject.get(new Integer(ind));
        testObject.setExtName(extName);
        testObject.setExtValue(extValue);
    }

    /**
     * Manage the Internal fields for the Internal GVBuffer object
     *
     * @param request
     *        the HttpServletRequest request
     * @param session
     *        the HTTPSession object
     */
    private void manageInternalFields(HttpServletRequest request, HttpSession session)
    {
        List<String> listName = new ArrayList<String>();
        List<String> listValue = new ArrayList<String>();
        int i = 0;
        while (request.getParameter("intField" + i) != null) {
            listName.add(request.getParameter("intField" + i));
            listValue.add(request.getParameter("intValue" + i));
            i++;
        }

        i = 1;
        while (request.getParameter("intFieldName" + i) != null) {
            listName.add(request.getParameter("intFieldName" + i));
            listValue.add(request.getParameter("intFieldValue" + i));
            i++;
        }

        String[] intName = new String[listName.size()];
        String[] intValue = new String[listValue.size()];
        listName.toArray(intName);
        listValue.toArray(intValue);

        String ind = request.getParameter("ind");
        Map<?, ?> mapTestObject = (Map<?, ?>) session.getAttribute("mapTestObject");
        TestGVBufferObject testObject = (TestGVBufferObject) mapTestObject.get(new Integer(ind));
        testObject.setIntName(intName);
        testObject.setIntValue(intValue);
    }
}

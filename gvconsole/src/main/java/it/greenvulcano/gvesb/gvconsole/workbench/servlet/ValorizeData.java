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
import it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestManager;
import it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestObject;
import it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin;
import it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPluginWrapper;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <code>Upload</code> servlet executes the upload function to get the data
 * value from a file.
 *
 * @version 2.1.0 17 Feb 2010
 * @author GreenVulcano Developer Team
 */
public class ValorizeData extends HttpServlet
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

        try {

            TestPlugin plugin = testManager.getPlugin();

            try {
                String ind = request.getParameter("ind");
                String data = request.getParameter("encData");
                String encoding = request.getParameter("encoding");

                HttpSession session = request.getSession();
                Map map = (Map) session.getAttribute("mapTestObject");
                TestObject testObject = (TestObject) map.get(new Integer(ind));
                String currentTest = (String) session.getAttribute("currentTest");

                if (currentTest.equals("gvHttpInbound")) {
                    testObject.setParameters("requestContent", data);
                }
                else {
                    testObject.setParameters("dataValue", data);
                    testObject.setParameters("byteData", data.getBytes());
                }
                plugin.updateDataInput(data, encoding);
            }
            catch (Throwable exc) {
                TestPluginWrapper wrapper = testManager.getWrapper();
                wrapper.setThrowable(exc);
            }
        }
        catch (Throwable thr) {
            thr.printStackTrace();
        }

        response.sendRedirect("testing/reloadAndClose.jsp");
    }
}
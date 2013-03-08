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

import it.greenvulcano.gvesb.gvconsole.workbench.plugin.MultipartFormDataParser;
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
 * <code>Upload</code> servelt executes the upload function to get the data
 * value from a file.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class Upload extends HttpServlet
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
        String testType = "";
        // String dump = "";
        String ind = "";

        try {
            try {
                // Instantiate the <code>MultipartFormDataParser</code> class
                // to execute the upload required.
                //
                MultipartFormDataParser multipartFormDataParser = new MultipartFormDataParser(request);

                // Get the Plugin Object of the current test
                //
                TestPlugin plugin = testManager.getPlugin();

                // Invoke the upload method of plugin class
                //
                testType = request.getParameter("testType");
                if (testType != null) {
                    if (testType.equals("multipleTest")) {
                        ind = request.getParameter("ind");
                        HttpSession session = request.getSession();
                        Map map = (Map) session.getAttribute("mapTestObject");
                        TestObject testObject = (TestObject) map.get(new Integer(ind));
                        plugin.uploadMultiple(multipartFormDataParser, testObject);
                    }
                }
                else {
                    plugin.upload(multipartFormDataParser);
                    request.setAttribute("firstTime", "no");
                }
            }
            catch (Throwable exc) {
                TestPluginWrapper wrapper = testManager.getWrapper();
                wrapper.setThrowable(exc);
            }
        }
        catch (Throwable thr) {
            thr.printStackTrace();
        }

        // Redirect to an utility jsp to reload the opener page (
        // <code>index.jsp</code>)
        //
        response.sendRedirect("testing/reloadAndClose.jsp");
    }
}
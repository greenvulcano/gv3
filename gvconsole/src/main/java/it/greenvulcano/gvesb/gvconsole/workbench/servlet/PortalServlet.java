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

import it.greenvulcano.configuration.XMLConfig;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

import org.w3c.dom.Node;


/**
 * PortalServlet class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class PortalServlet extends HttpServlet
{

    private static final long serialVersionUID = 300L;
    String                    configFileName   = "GVWorkbenchConfig.xml";
    String                    xslFile          = "portalConfig.xsl";

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init()
    {
        try {
            super.init();
        }
        catch (Throwable exc) {
            exc.printStackTrace();
        }
    }

    /**
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
    {
        try {
            HttpSession session = request.getSession(true);
            session.removeAttribute("currentMenu");
            session.setAttribute("currentMenu", "testing");
            Node configNode = XMLConfig.getNode(configFileName, "/GVWorkbenchConfig/Portal");
            PageContext pageContext = JspFactory.getDefaultFactory().getPageContext(this, request, response, null,
                    true, 8192, true);
            Portal portal = new Portal(configNode, pageContext);
            portal.writePage();
        }
        catch (Throwable exc) {
            exc.printStackTrace();
        }
    }
}

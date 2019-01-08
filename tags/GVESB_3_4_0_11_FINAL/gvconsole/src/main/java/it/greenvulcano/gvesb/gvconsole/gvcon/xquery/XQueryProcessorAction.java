/*
 * Copyright (c) 2005 Maxime Informatica s.n.c. - All right reserved
 *
 * Created on 3-giu-2005
 */
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
package it.greenvulcano.gvesb.gvconsole.gvcon.xquery;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import max.util.Parameter;
import max.xml.MenuAction;
import max.xml.XMLBuilder;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class XQueryProcessorAction extends MenuAction
{

    /**
     * @param key
     * @param label
     * @param description
     * @param target
     */
    public XQueryProcessorAction(String key, String label, String description, String target)
    {
        super(key, label, description, target);
    }

    /**
     * @see max.xml.MenuAction#doAction(max.xml.XMLBuilder,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, max.util.Parameter)
     */
    @Override
    public void doAction(XMLBuilder builder, HttpServletRequest req, HttpServletResponse resp, Parameter params)
            throws Exception
    {
        forward(req, resp, "/def/xquery/xQueryProcessorCurrentDocument.jsp");
    }

    /**
     * @param request
     * @param response
     * @param page
     */
    private void forward(HttpServletRequest request, HttpServletResponse response, String page) throws Exception
    {
        HttpSession session = request.getSession();
        ServletContext context = session.getServletContext();
        RequestDispatcher dispatcher = context.getRequestDispatcher(page);
        dispatcher.forward(request, response);
    }
}
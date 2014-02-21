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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * This listener is called when the GreenVulcano Workbench application starts.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class WorkbenchServletCtxListener implements ServletContextListener
{

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent evt)
    {

        // Print the startup banner
        //
        ServletContext context = evt.getServletContext();
        int maj = context.getMajorVersion();
        int min = context.getMinorVersion();
        String server = context.getServerInfo();
        String cntxName = context.getServletContextName();

        System.out.println("+--------------------------------------------------------------------------");
        System.out.println("| GreenVulcano Administration Console");
        System.out.println("+--------------------------------------------------------------------------");
        System.out.println("| CONTEXT............: " + cntxName);
        System.out.println("| SERVER.............: " + server);
        System.out.println("| SERVLET API VERSION: " + maj + "." + min);
        System.out.println("+--------------------------------------------------------------------------");

    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent evt)
    {
    }

}

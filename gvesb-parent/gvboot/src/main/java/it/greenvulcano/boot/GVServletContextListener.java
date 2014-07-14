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
package it.greenvulcano.boot;

import it.greenvulcano.event.internal.shutdown.ShutdownHandler;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.GVLogger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

/**
 * This listener is called when the web application starts.
 * <p>
 * This listener will perform initialization of objects needed by GreenVulcano.<br>
 * Because this listener perform initialization of objects that must 'live'
 * before GreenVulcano, it must be called before GreenVulcano functionalities:
 * use the deployment order parameter in order to ensure this requirement.
 * </p>
 *
 * @version 3.0.0 Apr 2, 2010
 * @author GreenVulcano Developer Team
 */
public class GVServletContextListener implements ServletContextListener
{
    private static final Logger logger = GVLogger.getLogger(GVServletContextListener.class);

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent evt)
    {
        // Print the startup banner
        ServletContext context = evt.getServletContext();
        int maj = context.getMajorVersion();
        int min = context.getMinorVersion();
        String server = context.getServerInfo();
        String cntxName = context.getServletContextName();

        System.out.println("+--------------------------------------------------------------------------");
        System.out.println("| GreenVulcanoESB Boot");
        System.out.println("+--------------------------------------------------------------------------");
        System.out.println("| CONTEXT............: " + cntxName);
        System.out.println("| SERVER.............: " + server);
        System.out.println("| SERVLET API VERSION: " + maj + "." + min);
        System.out.println("+--------------------------------------------------------------------------");

        try {
            // only for dumping license information
            Class.forName("it.greenvulcano.gvesb.datahandling.factory.DHFactory");
        }
        catch (Exception exc) {
            // do nothing
        }

        // Initialize the JMX functionalities
        try {
            logger.info("Booting GreenVulcano.");
            JMXEntryPoint.instance();
        }
        catch (Throwable exc) {
            logger.error("Error booting GreenVulcano.", exc);
            exc.printStackTrace();
        }
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent evt)
    {
        System.out.println("+--------------------------------------------------------------------------");
        System.out.println("| GreenVulcanoESB Boot - Shutting down");
        System.out.println("+--------------------------------------------------------------------------");
        (new ShutdownHandler()).run();
    }
}

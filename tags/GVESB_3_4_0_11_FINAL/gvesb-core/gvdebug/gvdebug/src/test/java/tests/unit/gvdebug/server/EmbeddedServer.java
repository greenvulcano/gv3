/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package tests.unit.gvdebug.server;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.webapp.WebAppContext;

public class EmbeddedServer
{
    private static EmbeddedServer instance = new EmbeddedServer();
    private Server                server;

    private EmbeddedServer()
    {

    }

    public static EmbeddedServer getInstance()
    {
        return instance;
    }

    public Server getServer()
    {
        return server;
    }

    public void start(int webPort, String contextPath) throws Exception
    {
        // setup web app
        WebAppContext context = new WebAppContext();
        String warPath = System.getProperty("basedir") + "/src/main/webapp";
        context.setWar(warPath);

        // start the server
        context.setServletHandler(new ServletHandler());
        context.setContextPath(contextPath);
        server = new Server(webPort);
        server.addHandler(context);

        server.start();
    }

}
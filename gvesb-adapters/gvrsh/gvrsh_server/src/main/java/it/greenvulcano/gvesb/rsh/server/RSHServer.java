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
package it.greenvulcano.gvesb.rsh.server;


import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.rsh.server.rmi.RSHService;
import it.greenvulcano.gvesb.rsh.server.rmi.RSHServiceImpl;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.ArgsManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

/**
 * @version 3.2.0 06/10/2011
 * @author GreenVulcano Developer Team
 */
public class RSHServer implements ShutdownEventListener
{
    private static Logger    logger   = GVLogger.getLogger(RSHServer.class);

    private static RSHServer instance = null;

    private Registry         registry = null;
    private RSHService       srvc     = null;
    private int              port     = 1099;

    public RSHServer(int port) throws RemoteException
    {
        this.port = port;
    }

    public void startUp() throws RemoteException
    {
        ShutdownEventLauncher.addEventListener(this);

        try {
            registry = LocateRegistry.createRegistry(port);
        }
        catch (Exception exc) {
            // do nothing
            exc.printStackTrace();
        }
        if (registry == null) {
            registry = LocateRegistry.getRegistry(port);
        }
        srvc = new RSHServiceImpl("RSHService");
        registry.rebind(RSHService.class.getName(), srvc);
    }

    public void shutDown()
    {
        ShutdownEventLauncher.removeEventListener(this);

        try {
            logger.info("Unregistering " + srvc);
            if (registry != null) {
                registry.unbind(RSHService.class.getName());
            }
            UnicastRemoteObject.unexportObject(srvc, true);
        }
        catch (Exception exc) {
            logger.warn("Error unregistering " + srvc, exc);
        }
    }


    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
        shutDown();
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        logger.info("Starting RSH Server");
        try {
            ArgsManager am = new ArgsManager("p:", args);
            int port = am.getInteger("p", 1099);
            instance = new RSHServer(port);
            instance.startUp();
        }
        catch (Exception exc) {
            logger.error("Starting RSH Server failed", exc);
            System.exit(-1); // can't just return, rmi threads may not exit
        }
        logger.info("Started RSH Server");
    }

}

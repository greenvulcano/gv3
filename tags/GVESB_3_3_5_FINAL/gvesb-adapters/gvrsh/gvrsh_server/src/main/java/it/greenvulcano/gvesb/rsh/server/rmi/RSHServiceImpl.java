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
package it.greenvulcano.gvesb.rsh.server.rmi;


import it.greenvulcano.gvesb.rsh.RSHException;
import it.greenvulcano.gvesb.rsh.server.cmd.ShellCommand;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandDef;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandResult;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import com.healthmarketscience.rmiio.GZIPRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;

/**
 * @version 3.2.0 06/10/2011
 * @author GreenVulcano Developer Team
 */
public class RSHServiceImpl extends UnicastRemoteObject implements RSHService
{
    /**
     *
     */
    private static final long serialVersionUID = -6709474954479434080L;

    private static Logger     logger           = GVLogger.getLogger(RSHServiceImpl.class);
    private String            id;

    /**
     * @throws RemoteException
     */
    public RSHServiceImpl(String id) throws RemoteException
    {
        this.id = id;
        NMDC.push();
        try {
            NMDC.put("RSH_ID", id);
            logger.info("Created RSHServiceImpl[" + id + "]");
        }
        finally {
            NMDC.pop();
        }
    }

    /**
     * @param port
     * @throws RemoteException
     */
    public RSHServiceImpl(String id, int port) throws RemoteException
    {
        super(port);
        this.id = id;
    }

    /**
     * @param port
     * @param csf
     * @param ssf
     * @throws RemoteException
     */
    public RSHServiceImpl(String id, int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf)
            throws RemoteException
    {
        super(port, csf, ssf);
        this.id = id;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#shellExec(it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandDef)
     */
    @Override
    public ShellCommandResult shellExec(ShellCommandDef commandDef) throws RemoteException, RSHException
    {
        ShellCommandResult cmdR = null;

        NMDC.push();
        try {
            NMDC.put("RSH_ID", id);
            ShellCommand cmd = new ShellCommand(commandDef);
            cmdR = cmd.execute();
        }
        catch (RSHException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new RSHException("Error executing shell command", exc);
        }
        finally {
            NMDC.pop();
        }

        return cmdR;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#getFile(java.lang.String)
     */
    @Override
    public RemoteInputStream getFile(String fileName) throws RemoteException, RSHException
    {
        // create a RemoteStreamServer (note the finally block which only releases
        // the RMI resources if the method fails before returning.)
        RemoteInputStreamServer istream = null;

        NMDC.push();
        try {
            NMDC.put("RSH_ID", id);
            logger.info("BEGIN - Read file: " + fileName);
            istream = new GZIPRemoteInputStream(new BufferedInputStream(new FileInputStream(fileName)));
            // export the final stream for returning to the client
            RemoteInputStream result = istream.export();
            // after all the hard work, discard the local reference (we are passing
            // responsibility to the client)
            istream = null;
            logger.info("END - Read file: " + fileName);
            return result;
        }
        catch (Exception exc) {
            logger.error("Error reading file: " + fileName, exc);
            throw new RSHException("Error reading file: " + fileName, exc);
        }
        finally {
            // we will only close the stream here if the server fails before
            // returning an exported stream
            if (istream != null) {
                try {
                    istream.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }

            NMDC.pop();
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#sendFile(java.lang.String, com.healthmarketscience.rmiio.RemoteInputStream)
     */
    @Override
    public void sendFile(String fileName, RemoteInputStream ristream) throws RemoteException, RSHException
    {
        FileOutputStream ostream = null;
        InputStream istream = null;

        NMDC.push();
        try {
            NMDC.put("RSH_ID", id);
            logger.info("BEGIN - Write file: " + fileName);
            istream = RemoteInputStreamClient.wrap(ristream);

            File file = new File(fileName);
            ostream = new FileOutputStream(file);

            byte[] buf = new byte[1024];

            int bytesRead = 0;
            while ((bytesRead = istream.read(buf)) >= 0) {
                ostream.write(buf, 0, bytesRead);
            }
            ostream.flush();

            logger.info("END - Write file: " + fileName);
        }
        catch (Exception exc) {
            logger.error("Error writing file: " + fileName, exc);
            throw new RSHException("Error writing file: " + fileName, exc);
        }
        finally {
            try {
                if (istream != null) {
                    try {
                        istream.close();
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }
            }
            finally {
                if (ostream != null) {
                    try {
                        ostream.close();
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }
            }

            NMDC.pop();
        }
    }

    @Override
    public String toString()
    {
        return "RSHServiceImpl[" + id + "]";
    }
}

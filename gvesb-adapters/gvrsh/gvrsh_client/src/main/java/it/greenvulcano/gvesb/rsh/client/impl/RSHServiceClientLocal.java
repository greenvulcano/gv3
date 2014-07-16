/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.rsh.client.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.rsh.RSHException;
import it.greenvulcano.gvesb.rsh.client.RSHServiceClient;
import it.greenvulcano.gvesb.rsh.server.cmd.ShellCommand;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandDef;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandResult;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.healthmarketscience.rmiio.GZIPRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;

/**
 * @version 3.2.0 16/10/2011
 * @author GreenVulcano Developer Team
 */
public class RSHServiceClientLocal implements RSHServiceClient
{
    private static Logger logger = GVLogger.getLogger(RSHServiceClientLocal.class);

    private String        name   = null;
    private boolean       valid  = false;

    public RSHServiceClientLocal()
    {
        // do nothing
    }

    @Override
    public void init(Node node) throws RSHException
    {
        try {
            name = XMLConfig.get(node, "@name");
            valid = true;
            logger.info("RSHServiceClientLocal[" + name + "] is initialized");
        }
        catch (Exception exc) {
            throw new RSHException("Error initializing RSHServiceClientLocal", exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#getFile(java.lang.String)
     */
    @Override
    public RemoteInputStream getFile(String fileName) throws RemoteException, RSHException
    {
        if (!valid) {
            throw new RSHException("RSHServiceClientLocal[" + name + "] is invalidated");
        }
        // create a RemoteStreamServer (note the finally block which only releases
        // the RMI resources if the method fails before returning.)
        RemoteInputStreamServer istream = null;

        NMDC.push();
        try {
            logger.info("RSHServiceClientLocal[" + name + "] BEGIN - Read file: " + fileName);
            istream = new GZIPRemoteInputStream(new BufferedInputStream(new FileInputStream(fileName)));
            // export the final stream for returning to the client
            RemoteInputStream result = istream.export();
            // after all the hard work, discard the local reference (we are passing
            // responsibility to the client)
            istream = null;
            logger.info("RSHServiceClientLocal[" + name + "] END - Read file: " + fileName);
            return result;
        }
        catch (Exception exc) {
            logger.error("RSHServiceClientLocal[" + name + "] Error reading file: " + fileName, exc);
            throw new RSHException("RSHServiceClientLocal[" + name + "] Error reading file: " + fileName, exc);
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

        if (!valid) {
            throw new RSHException("RSHServiceClientLocal[" + name + "] is invalidated");
        }

        NMDC.push();
        try {
            logger.info("RSHServiceClientLocal[" + name + "] BEGIN - Write file: " + fileName);
            istream = RemoteInputStreamClient.wrap(ristream);

            File file = new File(fileName);
            ostream = new FileOutputStream(file);

            byte[] buf = new byte[1024];

            int bytesRead = 0;
            while ((bytesRead = istream.read(buf)) >= 0) {
                ostream.write(buf, 0, bytesRead);
            }
            ostream.flush();

            logger.info("RSHServiceClientLocal[" + name + "] END - Write file: " + fileName);
        }
        catch (Exception exc) {
            logger.error("RSHServiceClientLocal[" + name + "] Error writing file: " + fileName, exc);
            throw new RSHException("RSHServiceClientLocal[" + name + "] Error writing file: " + fileName, exc);
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
    public byte[] getFileB(String fileName) throws RemoteException, RSHException
    {
        if (!valid) {
            throw new RSHException("RSHServiceClientLocal[" + name + "] is invalidated");
        }

        InputStream istream = null;
        try {
            logger.info("RSHServiceClientLocal[" + name + "] BEGIN - Read file: " + fileName);
            istream = new BufferedInputStream(new FileInputStream(fileName));

            ByteArrayOutputStream ostream = new ByteArrayOutputStream();

            byte[] buf = new byte[1024];

            int bytesRead = 0;
            while ((bytesRead = istream.read(buf)) >= 0) {
                ostream.write(buf, 0, bytesRead);
            }

            logger.info("RSHServiceClientLocal[" + name + "] END - Read file: " + fileName);
            return ostream.toByteArray();
        }
        catch (IOException exc) {
            logger.error("RSHServiceClientLocal[" + name + "] Error reading file: " + fileName, exc);
            throw new RSHException("RSHServiceClientLocal[" + name + "] Error reading file: " + fileName, exc);
        }
        finally {
            if (istream != null) {
                try {
                    istream.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }

    @Override
    public void sendFileB(String fileName, byte[] content) throws RemoteException, RSHException
    {
        if (!valid) {
            throw new RSHException("RSHServiceClientLocal[" + name + "] is invalidated");
        }

        OutputStream ostream = null;
        try {
            //LoggerContext.put("RSH_ID", id);
            logger.info("RSHServiceClientLocal[" + name + "] BEGIN - Write file: " + fileName);

            File file = new File(fileName);
            ostream = new FileOutputStream(file);

            ostream.write(content, 0, content.length);

            ostream.flush();
            logger.info("RSHServiceClientLocal[" + name + "] END - Write file: " + fileName);
        }
        catch (IOException exc) {
            logger.error("RSHServiceClientLocal[" + name + "] Error writing file: " + fileName, exc);
            throw new RSHException("RSHServiceClientLocal[" + name + "] Error writing file: " + fileName, exc);
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
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#shellExec(it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandDef)
     */
    @Override
    public ShellCommandResult shellExec(ShellCommandDef commandDef) throws RemoteException, RSHException
    {
        if (!valid) {
            throw new RSHException("RSHServiceClientLocal[" + name + "] is invalidated");
        }

        NMDC.push();
        try {
            logger.info("RSHServiceClientLocal[" + name + "] BEGIN - Execute command");
            ShellCommand cmd = new ShellCommand(commandDef);
            ShellCommandResult result = cmd.execute();
            logger.info("RSHServiceClientRMI[" + name + "] END - Execute command");
            return result;
        }
        catch (RSHException exc) {
            logger.error("RSHServiceClientLocal[" + name + "] Error executing command", exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("RSHServiceClientLocal[" + name + "] Error executing command", exc);
            throw new RSHException("RSHServiceClientLocal[" + name + "] Error executing command", exc);
        }
        finally {
            NMDC.pop();
        }
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public void invalidate()
    {
        logger.info("RSHServiceClientLocal[" + name + "] is invalidated");
        valid = false;
        cleanup();
    }

    @Override
    public boolean isValid()
    {
        return valid;
    }

    @Override
    public void cleanup()
    {
        //do nothing
    }

}

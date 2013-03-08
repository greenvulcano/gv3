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
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandDef;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandResult;
import it.greenvulcano.gvesb.rsh.server.rmi.RSHService;
import it.greenvulcano.log.GVLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;

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
public class RSHServiceClientRMI implements RSHServiceClient
{
    private static Logger logger        = GVLogger.getLogger(RSHServiceClientRMI.class);

    private RSHService    service       = null;
    private String        name          = null;
    private String        regSvcName    = null;
    private String        regURL        = null;
    private String        regCtxFactory = null;
    private boolean       valid         = false;

    public RSHServiceClientRMI()
    {
        // do nothing
    }

    @Override
    public void init(Node node) throws RSHException
    {
        try {
            name = XMLConfig.get(node, "@name");
            regURL = XMLConfig.get(node, "@regURL");
            regSvcName = XMLConfig.get(node, "@regSvcName", RSHService.class.getName());
            regCtxFactory = XMLConfig.get(node, "@regCtxFactory", "com.sun.jndi.rmi.registry.RegistryContextFactory");
            valid = true;
            logger.info("RSHServiceClientRMI[" + name + "] is initialized: regURL=" + regURL + " - regSvcName="
                    + regSvcName + " - regCtxFactory=" + regCtxFactory);
        }
        catch (Exception exc) {
            throw new RSHException("Error initializing RSHServiceClientRMI", exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#getFile(java.lang.String)
     */
    @Override
    public RemoteInputStream getFile(String fileName) throws RemoteException, RSHException
    {
        if (!valid) {
            throw new RSHException("RSHServiceClientRMI[" + name + "] is invalidated");
        }
        try {
            logger.info("RSHServiceClientRMI[" + name + "] BEGIN - Read file: " + fileName);
            RemoteInputStream istream = getService().getFile(fileName);
            logger.info("RSHServiceClientRMI[" + name + "] END - Read file: " + fileName);
            return istream;
        }
        catch (RSHException exc) {
            logger.error("RSHServiceClientRMI[" + name + "] Error reading file: " + fileName, exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("RSHServiceClientRMI[" + name + "] Error reading file: " + fileName, exc);
            throw new RSHException("RSHServiceClientRMI[" + name + "] Error reading file: " + fileName, exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#sendFile(java.lang.String, com.healthmarketscience.rmiio.RemoteInputStream)
     */
    @Override
    public void sendFile(String fileName, RemoteInputStream ristream) throws RemoteException, RSHException
    {
        if (!valid) {
            throw new RSHException("RSHServiceClientRMI[" + name + "] is invalidated");
        }
        try {
            logger.info("RSHServiceClientRMI[" + name + "] BEGIN - Write file: " + fileName);
            getService().sendFile(fileName, ristream);
            logger.info("RSHServiceClientRMI[" + name + "] END - Write file: " + fileName);
        }
        catch (RSHException exc) {
            logger.error("RSHServiceClientRMI[" + name + "] Error writing file: " + fileName, exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("RSHServiceClientRMI[" + name + "] Error writing file: " + fileName, exc);
            throw new RSHException("RSHServiceClientRMI[" + name + "] Error writing file: " + fileName, exc);
        }

    }

    @Override
    public byte[] getFileB(String fileName) throws RemoteException, RSHException
    {
        if (!valid) {
            throw new RSHException("RSHServiceClientRMI[" + name + "] is invalidated");
        }

        InputStream istream = null;
        try {
            logger.info("RSHServiceClientRMI[" + name + "] BEGIN - Read file: " + fileName);
            istream = RemoteInputStreamClient.wrap(getService().getFile(fileName));

            ByteArrayOutputStream ostream = new ByteArrayOutputStream();

            byte[] buf = new byte[1024];

            int bytesRead = 0;
            while ((bytesRead = istream.read(buf)) >= 0) {
                ostream.write(buf, 0, bytesRead);
            }

            logger.info("RSHServiceClientRMI[" + name + "] END - Read file: " + fileName);
            return ostream.toByteArray();
        }
        catch (RSHException exc) {
            logger.error("RSHServiceClientRMI[" + name + "] Error reading file: " + fileName, exc);
            throw exc;
        }
        catch (RemoteException exc) {
            logger.error("RSHServiceClientRMI[" + name + "] Error reading file: " + fileName, exc);
            this.invalidate();
            throw exc;
        }
        catch (Exception exc) {
            logger.error("RSHServiceClientRMI[" + name + "] Error reading file: " + fileName, exc);
            throw new RSHException("RSHServiceClientRMI[" + name + "] Error reading file: " + fileName, exc);
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
            throw new RSHException("RSHServiceClientRMI[" + name + "] is invalidated");
        }

        RemoteInputStreamServer ristream = null;
        try {
            logger.info("RSHServiceClientRMI[" + name + "] BEGIN - Write file: " + fileName);
            ristream = new GZIPRemoteInputStream(new ByteArrayInputStream(content));
            getService().sendFile(fileName, ristream.export());
            logger.info("RSHServiceClientRMI[" + name + "] END - Write file: " + fileName);
        }
        catch (RSHException exc) {
            logger.error("RSHServiceClientRMI[" + name + "] Error writing file: " + fileName, exc);
            throw exc;
        }
        catch (RemoteException exc) {
            logger.error("RSHServiceClientRMI[" + name + "] Error writing file: " + fileName, exc);
            this.invalidate();
            throw exc;
        }
        catch (Exception exc) {
            logger.error("RSHServiceClientRMI[" + name + "] Error writing file: " + fileName, exc);
            throw new RSHException("RSHServiceClientRMI[" + name + "] Error writing file: " + fileName, exc);
        }
        finally {
            if (ristream != null) {
                try {
                    ristream.close();
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
            throw new RSHException("RSHServiceClientRMI[" + name + "] is invalidated");
        }
        try {
            logger.info("RSHServiceClientRMI[" + name + "] BEGIN - Execute command");
            ShellCommandResult result = getService().shellExec(commandDef);
            logger.info("RSHServiceClientRMI[" + name + "] END - Execute command");
            return result;
        }
        catch (RSHException exc) {
            logger.error("RSHServiceClientRMI[" + name + "] Error executing command", exc);
            throw exc;
        }
        catch (RemoteException exc) {
            logger.error("RSHServiceClientRMI[" + name + "] Error executing command", exc);
            this.invalidate();
            throw exc;
        }
        catch (Exception exc) {
            logger.error("RSHServiceClientRMI[" + name + "] Error executing command", exc);
            throw new RSHException("RSHServiceClientRMI[" + name + "] Error executing command", exc);
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
        logger.info("RSHServiceClientRMI[" + name + "] is invalidated");
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
        service = null;
    }

    private RSHService getService() throws RSHException
    {
        Context ctx = null;
        try {
            if (service != null) {
                return service;
            }

            Hashtable env = new Hashtable();
            env.put(Context.PROVIDER_URL, regURL);
            env.put(Context.INITIAL_CONTEXT_FACTORY, regCtxFactory);

            ctx = new InitialContext(env);

            service = (RSHService) ctx.lookup(regSvcName);

            return service;
        }
        catch (Exception exc) {
            throw new RSHException("Error contacting RMI Registry", exc);
        }
        finally {
            if (ctx != null) {
                try {
                    ctx.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }
}

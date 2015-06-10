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
package it.greenvulcano.gvesb.virtual.file.remote;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.gvesb.virtual.file.remote.command.GVRemoteCommand;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.remotefs.RemoteManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Executes sequences of Command on remote file system.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class RemoteManagerCall implements CallOperation
{
    private static final Logger   logger   = GVLogger.getLogger(RemoteManagerCall.class);

    /**
     * The module instance's name.
     */
    private String                name     = null;

    /**
     * A private instance of <code>RemoteManager</code> class to perform FTP
     * operations.
     */
    private RemoteManager         manager  = null;

    private List<GVRemoteCommand> commands = new ArrayList<GVRemoteCommand>();

    /**
     * The configured operation's key.
     */
    protected OperationKey        key      = null;

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            name = XMLConfig.get(node, "@name");

            Node nm = XMLConfig.getNode(node, "*[@type='remote-manager']");
            manager = (RemoteManager) Class.forName(XMLConfig.get(nm, "@class")).newInstance();
            manager.init(nm);

            logger.debug("BEGIN RemoteManagerCall[" + name + "] initialization");
            NodeList nl = XMLConfig.getNodeList(node, "RemoteCommands/*[@type='remote-command']");
            for (int i = 0; i < nl.getLength(); i++) {
                Node cmdNode = nl.item(i);
                GVRemoteCommand comm = (GVRemoteCommand) Class.forName(XMLConfig.get(cmdNode, "@class")).newInstance();
                comm.init(cmdNode);
                logger.debug("Initialized Command: " + comm);
                commands.add(comm);
            }
            logger.debug("END RemoteManagerCall[" + name + "] initialization");
        }
        catch (Exception exc) {
            logger.error("ERROR RemoteManagerCall[" + name + "] initialization", exc);
            throw new InitializationException("GV_CONF_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        Map<String, String> props = GVBufferPropertiesHelper.getPropertiesMapSS(gvBuffer, true);
        try {
            manager.connect(props);

            for (GVRemoteCommand command : commands) {
                try {
                    command.execute(manager, gvBuffer);
                }
                catch (Exception exc) {
                    if (command.isCritical()) {
                        logger.error("CRITICAL Command " + command.getClass().getSimpleName()
                                + " failed execution, exiting");
                        throw exc;
                    }
                    continue;
                }
            }

            return gvBuffer;
        }
        catch (Exception exc) {
            logger.error("ERROR RemoteManagerCall[" + name + "] execution", exc);
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
        finally {
            manager.disconnect(props);
        }
    }


    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    @Override
    public OperationKey getKey()
    {
        return key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer data)
    {
        return data.getService();
    }
}
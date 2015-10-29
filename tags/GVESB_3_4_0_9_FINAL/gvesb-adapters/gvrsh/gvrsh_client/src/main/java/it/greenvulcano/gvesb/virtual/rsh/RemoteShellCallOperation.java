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
package it.greenvulcano.gvesb.virtual.rsh;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.rsh.RSHException;
import it.greenvulcano.gvesb.rsh.client.RSHServiceClient;
import it.greenvulcano.gvesb.rsh.client.RSHServiceClientManager;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandDef;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandResult;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.2.0 06/10/2011
 * @author GreenVulcano Developer Team
 */
public class RemoteShellCallOperation implements CallOperation
{
    private static final Logger logger        = GVLogger.getLogger(RemoteShellCallOperation.class);

    /**
     * The configured operation's key.
     */
    protected OperationKey      key           = null;

    /**
     * The directory in which the shell command must be executed.
     * This value can contains placeholders which will be resolved at runtime.
     */
    private String              baseDirectory = null;

    /**
     * The command (or command sequence) to be executed.
     * This value contains placeholders which will be resolved at runtime.
     */
    private List<String>        commandList   = null;

    /**
     * The environment properties to be set before executing the command(s).
     * This value contains placeholders which will be resolved at runtime.
     */
    private Map<String, String> propsList     = null;

    private String              clientName    = "";

    /**
     * The output encoding.
     */
    private String              encoding      = System.getProperty("file.encoding");

    private boolean             dumpOutput    = false;

    /**
     * 
     * @param node
     *        configuration node.
     * @exception InitializationException
     *            if an error occurs during initialization
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            clientName = XMLConfig.get(node, "@rsh-client-name");
            dumpOutput = XMLConfig.getBoolean(node, "@dump-output", false);

            if (XMLConfig.exists(node, "@directory")) {
                baseDirectory = XMLConfig.get(node, "@directory");
                logger.debug("Configured base directory for command execution: " + baseDirectory);
            }

            if (XMLConfig.exists(node, "@encoding")) {
                encoding = XMLConfig.get(node, "@encoding");
                logger.debug("Configured encoding is:" + encoding);
            }

            initCommand(node);
            initProperties(node);
        }
        catch (Exception exc) {
            throw new InitializationException("GVVCL_SHELL_INIT_ERROR", new String[][]{{"node", node.getLocalName()}},
                    exc);
        }
    }

    /**
     * Initializes the command string.
     * 
     * @param node
     *        the configuration node.
     * @throws XMLConfigException
     *         if an error occurs while reading the configuration.
     */
    private void initCommand(Node node) throws XMLConfigException
    {
        commandList = new ArrayList<String>();
        if (XMLConfig.exists(node, "cmd")) {
            String currCommand = XMLConfig.get(node, "cmd/text()", "").trim();
            commandList.add(currCommand);
            logger.debug("Configured command: " + currCommand);
        }
        else {
            NodeList list = XMLConfig.getNodeList(node, "cmd-array-elem");
            int size = list.getLength();
            StringBuilder cmdL = new StringBuilder();
            for (int i = 0; i < size; i++) {
                String currCommand = XMLConfig.get(list.item(i), "text()", "").trim();
                commandList.add(currCommand);
                cmdL.append(currCommand).append(" ");
            }
            logger.debug("Configured command: " + cmdL);
        }
    }

    /**
     * Initializes the environment properties to be set before invoking the
     * shell command.
     * 
     * @param node
     *        the configuration node.
     * @throws XMLConfigException
     *         if an error occurs while reading the configuration.
     */
    private void initProperties(Node node) throws XMLConfigException
    {
        NodeList list = XMLConfig.getNodeList(node, "env-property");
        int size = list.getLength();
        if (size > 0) {
            propsList = new HashMap<String, String>();
            for (int i = 0; i < size; i++) {
                Node item = list.item(i);
                String name = XMLConfig.get(item, "@name");
                String value = XMLConfig.get(item, "@value");
                propsList.put(name, value);
                logger.debug("Configured environment property: " + name + "=" + value);
            }
        }
    }

    /**
     * Execute the operation using an <code>GVBuffer</code>. Usually this method
     * is used in order to call external systems.
     * 
     * @param gvBuffer
     *        input data for the operation.
     * 
     * @return an <code>GVBuffer</code> containing the operation result.
     * 
     * 
     * @exception Exception
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        try {
            List<String> realCommand = new ArrayList<String>();
            Map<String, String> realProps = null;
            String realDirectory = null;

            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);

            for (int i = 0; i < commandList.size(); i++) {
                realCommand.add(PropertiesHandler.expand(commandList.get(i), params, gvBuffer));
            }

            if (propsList != null) {
                realProps = new HashMap<String, String>();
                Iterator<String> it = propsList.keySet().iterator();
                while (it.hasNext()) {
                    String name = it.next();
                    String value = propsList.get(name);
                    realProps.put(name, PropertiesHandler.expand(value, params, gvBuffer));
                }
            }

            if (baseDirectory != null) {
                realDirectory = PropertiesHandler.expand(baseDirectory, params, gvBuffer);
            }

            if (logger.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                for (String currCommand : realCommand) {
                    sb.append(currCommand).append('\n');
                }
                logger.debug("Executing the remote shell command(s):\n" + sb.toString());
                logger.debug("within "
                        + (realDirectory != null ? "directory " + realDirectory : "current working directory"));

                if (realProps != null) {
                    logger.debug("Environment property settings:");
                    Iterator<String> it = realProps.keySet().iterator();
                    while (it.hasNext()) {
                        String name = it.next();
                        String value = realProps.get(name);
                        logger.debug(name + "=" + value);
                    }
                }
            }

            String output = executeCommand(gvBuffer.getId().toString(), realCommand, realProps, realDirectory);
            //if (logger.isDebugEnabled()) {
            //    logger.debug("Shell command [output]:\n[" + output + "]");
            //}
            gvBuffer.setObject(output);

            return gvBuffer;
        }
        catch (GVException gve) {
            throw new CallException("GVVCL_SHELL_GVDATA_ERROR", new String[][]{{"message", gve.getMessage()}}, gve);
        }
        catch (UnsupportedEncodingException uee) {
            throw new CallException("GVVCL_SHELL_ENCODING_ERROR", new String[][]{{"message", uee.getMessage()}}, uee);
        }
        catch (IOException ioe) {
            throw new CallException("GVVCL_SHELL_IO_ERROR", new String[][]{{"message", ioe.getMessage()}}, ioe);
        }
        catch (Exception e) {
            throw new CallException("GVVCL_SHELL_GENERIC_ERROR", new String[][]{{"message", e.getMessage()}}, e);
        }
    }

    /**
     * Executes the shell command.
     * 
     * @param id
     *        The GV transaction ID
     * @param cmds
     *        The commands to be executed.
     * @param props
     *        The environment properties. Can be null if not needed
     * @param directory
     *        The execution directory. If null, command will be executed within
     *        current working directory.
     * 
     * @return the shell command output, as read from the standard output.
     * 
     * @throws IOException
     *         if an error occurs while performing the shell command.
     * @throws CallException
     *         if the shell command returns an error.
     * @throws NotBoundException
     * @throws RSHException
     */
    private String executeCommand(String id, List<String> cmds, Map<String, String> props, String directory)
            throws IOException, InterruptedException, CallException, NotBoundException, RSHException
    {
        RSHServiceClient svcClient = null;
        try {
            svcClient = RSHServiceClientManager.instance().getRSHServiceClient(clientName);

            ShellCommandDef cmdD = new ShellCommandDef(id, cmds, directory, props);

            ShellCommandResult cmdR = svcClient.shellExec(cmdD);

            if (logger.isDebugEnabled() && dumpOutput) {
                logger.debug("Remote Shell command execution terminated:");
                logger.debug("ExitCode: " + cmdR.getExitCode());
                logger.debug("StdOut:\n" + cmdR.getStdOut());
                logger.debug("StdError:\n" + cmdR.getStdErr());
            }

            if (cmdR.getExitCode() != 0) {
                String stderr = cmdR.getStdErr();
                logger.error("An error occurred while executing the shell command - ExitCode: " + cmdR.getExitCode()
                        + " StdError:\n" + stderr);
                throw new CallException("GVVCL_RSH_PROCESS_ERROR", new String[][]{{"stderr", stderr}});
            }
            return cmdR.getStdOut();
        }
        finally {
            RSHServiceClientManager.instance().releaseRSHServiceClient(svcClient);
        }
    }


    /**
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        // do nothing
    }

    /**
     * Sets the Operation key.
     * 
     * @param key
     *        the key to set
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
     * Return the alias for the given service
     * 
     * @param gvBuffer
     *        the input service data
     * @return the configured alias
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

}

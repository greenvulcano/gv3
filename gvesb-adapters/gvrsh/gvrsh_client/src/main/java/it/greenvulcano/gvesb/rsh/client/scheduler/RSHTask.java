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
package it.greenvulcano.gvesb.rsh.client.scheduler;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.rsh.RSHException;
import it.greenvulcano.gvesb.rsh.client.RSHServiceClient;
import it.greenvulcano.gvesb.rsh.client.RSHServiceClientManager;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandDef;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandResult;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.scheduler.Task;
import it.greenvulcano.scheduler.TaskException;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class implements a task enabled to invoke a remote shell command.
 * 
 * @version 3.2.0 09/11/2011
 * @author GreenVulcano Developer Team
 */
public class RSHTask extends Task
{

    private static final Logger logger        = GVLogger.getLogger(RSHTask.class);

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

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.scheduler.Task#getLogger()
     */
    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.scheduler.Task#initTask(org.w3c.dom.Node)
     */
    @Override
    protected void initTask(Node node) throws TaskException
    {
        try {
            clientName = XMLConfig.get(node, "@rsh-client-name");

            if (XMLConfig.exists(node, "@directory")) {
                baseDirectory = XMLConfig.get(node, "@directory");
                logger.debug("Configured base directory for command execution: " + baseDirectory);
            }

            initCommand(node);
            initProperties(node);
        }
        catch (Exception exc) {
            logger.error("Error initializing Task(" + getFullName() + ")", exc);
            throw new TaskException("Error initializing Task(" + getFullName() + ")", exc);
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

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.scheduler.Task#executeTask(java.lang.String, Date, java.util.Map<java.lang.String, java.lang.String>, booolean)
     */
    @Override
    protected boolean executeTask(String name, Date fireTime, Map<String, String> locProperties, boolean isLast)
    {
        boolean success = false;
        try {
            logger.debug("Executing the RSH task: (" + getFullName() + ") - (" + name + ")");
            List<String> realCommand = new ArrayList<String>();
            Map<String, String> realProps = null;
            String realDirectory = null;

            for (int i = 0; i < commandList.size(); i++) {
                realCommand.add(PropertiesHandler.expand(commandList.get(i), null));
            }

            if (propsList != null) {
                realProps = new HashMap<String, String>();
                Iterator<String> it = propsList.keySet().iterator();
                while (it.hasNext()) {
                    String n = it.next();
                    String v = propsList.get(n);
                    realProps.put(n, PropertiesHandler.expand(v, null));
                }
            }

            if (baseDirectory != null) {
                realDirectory = PropertiesHandler.expand(baseDirectory, null);
            }

            if (logger.isInfoEnabled()) {
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
                        String n = it.next();
                        String v = realProps.get(n);
                        logger.debug(n + "=" + v);
                    }
                }
            }

            success = executeCommand(realCommand, realProps, realDirectory);
        }
        catch (Exception exc) {
            logger.error("An error occurs executing the RSH Task(" + getFullName() + ") - (" + name + ")", exc);
        }
        return success;
    }


    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.scheduler.Task#destroyTask()
     */
    @Override
    protected void destroyTask()
    {
        logger.debug("Destroying the RSH task: " + getFullName());
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.scheduler.Task#sendHeartBeat()
     */
    @Override
    protected boolean sendHeartBeat()
    {
        return true;
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
    private boolean executeCommand(List<String> cmds, Map<String, String> props, String directory) throws IOException,
            InterruptedException, NotBoundException, RSHException
    {
        boolean success = false;
        RSHServiceClient svcClient = null;
        try {
            svcClient = RSHServiceClientManager.instance().getRSHServiceClient(clientName);

            ShellCommandDef cmdD = new ShellCommandDef(cmds, directory, props);

            ShellCommandResult cmdR = svcClient.shellExec(cmdD);

            logger.debug("Remote Shell command execution terminated:");
            logger.debug("Exit status: " + cmdR.getExitCode());
            logger.debug("StdOut:\n" + cmdR.getStdOut());

            success = cmdR.getExitCode() == 0;
            if (!success) {
                logger.error("An error occurred while executing the shell command - ExitCode: " + cmdR.getExitCode());
                logger.error("StdErr:\n" + cmdR.getStdErr());
            }
        }
        finally {
            RSHServiceClientManager.instance().releaseRSHServiceClient(svcClient);
        }
        return success;
    }
}

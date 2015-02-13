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
package it.greenvulcano.gvesb.rsh.server.cmd.helper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @version 3.2.0 06/10/2011
 * @author GreenVulcano Developer Team
 */
public class ShellCommandResult implements Serializable
{
    /**
     *
     */
    private static final long   serialVersionUID = 4077330935066815813L;

    public static int           NO_EXIT_CODE     = -9999;

    private String              id               = null;
    private String              workDir          = null;
    private String              command          = null;
    private List<String>        commands         = null;
    private Map<String, String> env              = null;
    private String              stdOut;
    private String              stdErr;
    private int                 exitCode         = NO_EXIT_CODE;

    /**
     * @param exitCode
     * @param stdOut
     * @param stdErr
     */
    public ShellCommandResult(ShellCommandDef commandDef, int exitCode, String stdOut, String stdErr)
    {
        this.id = commandDef.getId();
        this.command = commandDef.getCommand();
        this.commands = commandDef.getCommands();
        this.workDir = commandDef.getWorkDir();
        this.env = commandDef.getEnv();
        this.exitCode = exitCode;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @return the workDir
     */
    public String getWorkDir()
    {
        return this.workDir;
    }

    /**
     * @return the command
     */
    public String getCommand()
    {
        return this.command;
    }

    /**
     * @return the commands
     */
    public List<String> getCommands()
    {
        return this.commands;
    }

    /**
     * @return the env
     */
    public Map<String, String> getEnv()
    {
        return this.env;
    }

    /**
     * @return the stdOut
     */
    public String getStdOut()
    {
        return this.stdOut;
    }

    /**
     * @return the stdErr
     */
    public String getStdErr()
    {
        return this.stdErr;
    }

    /**
     * @return the exitCode
     */
    public int getExitCode()
    {
        return this.exitCode;
    }

    @Override
    public String toString()
    {
        return "ShellCommandDef[" + exitCode + "]: " + command;
    }
}

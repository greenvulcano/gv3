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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @version 3.2.0 06/10/2011
 * @author GreenVulcano Developer Team
 */
public class ShellCommandDef implements Serializable
{
    /**
     *
     */
    private static final long   serialVersionUID = 4702460896433023141L;

    private String              id               = null;
    private boolean             isDaemon         = false;
    private String              workDir          = null;
    private String              command          = null;
    private List<String>        commands         = null;
    private Map<String, String> env              = null;
    private String[]            envA             = null;

    /**
     * @param workDir
     * @param command
     * @param env
     */
    public ShellCommandDef(String id, String command, String workDir, Map<String, String> env)
    {
        this(id, command, workDir, env, false);
    }

    public ShellCommandDef(String id, String command, String workDir, Map<String, String> env, boolean isDaemon)
    {
        this.id = "" + System.currentTimeMillis();
        this.isDaemon = isDaemon;
        this.command = command;
        this.workDir = workDir;
        this.env = env;

        if ((env != null) && (!env.isEmpty())) {
            Set<String> names = env.keySet();
            envA = new String[names.size()];
            Iterator<String> it = names.iterator();
            int i = 0;
            while (it.hasNext()) {
                String name = it.next();
                envA[i] = name + "=" + env.get(name);
            }
        }
    }

    /**
     * @param workDir
     * @param command
     */
    public ShellCommandDef(String command, String workDir)
    {
        this(command, workDir, false);
    }

    public ShellCommandDef(String command, String workDir, boolean isDaemon)
    {
        this.id = "" + System.currentTimeMillis();
        this.isDaemon = isDaemon;
        this.command = command;
        this.workDir = workDir;
    }

    /**
     * @param workDir
     */
    public ShellCommandDef(String command)
    {
        this(command, false);
    }

    public ShellCommandDef(String command, boolean isDaemon)
    {
        this.id = "" + System.currentTimeMillis();
        this.isDaemon = isDaemon;
        this.command = command;
    }

    public ShellCommandDef(List<String> commands, String workDir, Map<String, String> env)
    {
        this("" + System.currentTimeMillis(), commands, workDir, env, false);
    }

    public ShellCommandDef(String id, List<String> commands, String workDir, Map<String, String> env)
    {
        this(id, commands, workDir, env, false);
    }

    public ShellCommandDef(List<String> commands, String workDir, Map<String, String> env, boolean isDaemon)
    {
        this("" + System.currentTimeMillis(), commands, workDir, env, false);
    }


    public ShellCommandDef(String id, List<String> commands, String workDir, Map<String, String> env, boolean isDaemon)
    {
        this.id = id;
        this.isDaemon = isDaemon;
        this.commands = commands;
        this.workDir = workDir;
        this.env = env;

        if ((env != null) && (!env.isEmpty())) {
            Set<String> names = env.keySet();
            envA = new String[names.size()];
            Iterator<String> it = names.iterator();
            int i = 0;
            while (it.hasNext()) {
                String name = it.next();
                envA[i] = name + "=" + env.get(name);
            }
        }
    }

    public ShellCommandDef(List<String> commands, String workDir)
    {
        this(commands, workDir, false);
    }

    public ShellCommandDef(String id, List<String> commands, String workDir)
    {
        this(id, commands, workDir, false);
    }

    public ShellCommandDef(List<String> commands, String workDir, boolean isDaemon)
    {
        this("" + System.currentTimeMillis(), commands, workDir, false);
    }

    public ShellCommandDef(String id, List<String> commands, String workDir, boolean isDaemon)
    {
        this.id = id;
        this.isDaemon = isDaemon;
        this.commands = commands;
        this.workDir = workDir;
    }

    /**
     * @param commands
     */
    public ShellCommandDef(List<String> commands)
    {
        this(commands, false);
    }

    public ShellCommandDef(List<String> commands, boolean isDaemon)
    {
        this.id = "" + System.currentTimeMillis();
        this.isDaemon = isDaemon;
        this.commands = commands;
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
     * @return the isDaemon
     */
    public boolean isDaemon()
    {
        return this.isDaemon;
    }

    /**
     * @return the env
     */
    public String[] getEnvArray()
    {
        return envA;
    }

    @Override
    public String toString()
    {
        StringBuffer str = new StringBuffer("ShellCommandDef[deamon(").append(isDaemon).append(")]: \n").append(
                "WorkDir: ").append((workDir != null) ? workDir : ".").append("\n").append(
                (command != null) ? command : ("" + commands)).append("\n").append("Environment: ").append(
                (env != null) ? env.toString() : "");

        return str.toString();
    }
}

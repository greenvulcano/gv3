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
package it.greenvulcano.gvesb.core.debug.ejb3;

import it.greenvulcano.gvesb.core.debug.DebuggerException;
import it.greenvulcano.gvesb.core.debug.model.DebuggerObject;

/**
 * Remote interface of the GVDebugger EJB3.
 * 
 * 
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
public interface GVDebugger
{
    public DebuggerObject stack(String threadName) throws DebuggerException;

    public DebuggerObject var(String threadName, String stackFrame, String varEnv, String varID)
            throws DebuggerException;

    public DebuggerObject set_var(String threadName, String stackFrame, String varEnv, String varID, String varValue)
            throws DebuggerException;

    public DebuggerObject set(String threadName, String subflow, String sBreakpoint) throws DebuggerException;

    public DebuggerObject data();

    public DebuggerObject clear(String threadName, String subflow, String cBreakpoint) throws DebuggerException;

    public DebuggerObject stepOver(String threadName) throws DebuggerException;

    public DebuggerObject stepInto(String threadName) throws DebuggerException;

    public DebuggerObject stepReturn(String threadName) throws DebuggerException;

    public DebuggerObject resume(String threadName) throws DebuggerException;

    public DebuggerObject exit() throws DebuggerException;

    public DebuggerObject connect(String version, String service, String operation) throws DebuggerException;

    public DebuggerObject start() throws DebuggerException;

    public DebuggerObject event();
}

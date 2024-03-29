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

import it.greenvulcano.gvesb.core.debug.DebuggerAdapter;
import it.greenvulcano.gvesb.core.debug.DebuggerException;
import it.greenvulcano.gvesb.core.debug.model.DebuggerObject;
import it.greenvulcano.log.GVLogger;

import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.apache.log4j.Logger;

/**
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
@Stateful(name = "gvesb/core/GVDebugger", mappedName = "gvesb/core/GVDebugger")
@Remote(GVDebugger.class)
public class DebuggerBean implements GVDebugger
{
    private static final Logger logger = GVLogger.getLogger(DebuggerBean.class);

    private String              clientInfo;
    private DebuggerAdapter     debugger;

    @SuppressWarnings("unused")
    @PrePassivate
    private void prePassivate()
    {
        logger.info("In PrePassivate method");
    }

    @SuppressWarnings("unused")
    @PostActivate
    private void postActivate()
    {
        logger.info("In PostActivate method");
    }

    public String getBeanInfo()
    {
        return this.toString();
    }

    public String getClientInfo()
    {
        return clientInfo;
    }

    public void setClientInfo(String clientInfo)
    {
        this.clientInfo = clientInfo;
    }

    public DebuggerObject stack(String threadName) throws DebuggerException
    {
        return debugger.stack(threadName);
    }

    public DebuggerObject var(String threadName, String stackFrame, String varEnv, String varID)
            throws DebuggerException
    {
        return debugger.var(threadName, stackFrame, varEnv, varID);
    }

    public DebuggerObject set_var(String threadName, String stackFrame, String varEnv, String varID, String varValue)
            throws DebuggerException
    {
        return debugger.set_var(threadName, stackFrame, varEnv, varID, varValue);
    }

    public DebuggerObject set(String threadName, String subflow, String sBreakpoint) throws DebuggerException
    {
        return debugger.set(threadName, subflow, sBreakpoint);
    }

    public DebuggerObject data()
    {
        return debugger.data();
    }

    public DebuggerObject clear(String threadName, String subflow, String cBreakpoint) throws DebuggerException
    {
        return debugger.clear(threadName, subflow, cBreakpoint);
    }

    public DebuggerObject stepOver(String threadName) throws DebuggerException
    {
        return debugger.stepOver(threadName);
    }

    public DebuggerObject stepInto(String threadName) throws DebuggerException
    {
        return debugger.stepInto(threadName);
    }

    @Override
    public DebuggerObject stepReturn(String threadName) throws DebuggerException
    {
        return debugger.stepReturn(threadName);
    }

    public DebuggerObject resume(String threadName) throws DebuggerException
    {
        return debugger.resume(threadName);
    }

    public DebuggerObject skipAllBreakpoints(boolean enabled) throws DebuggerException
    {
        return debugger.skipAllBreakpoints(enabled);
    }

    @Remove
    public DebuggerObject exit() throws DebuggerException
    {
        DebuggerObject object = null;
        if (debugger != null) {
            object = debugger.exit();
            debugger = null;
        }
        return object;
    }

    public DebuggerObject connect(String version, String service, String operation) throws DebuggerException
    {
        debugger = new DebuggerAdapter();
        return debugger.connect(version, service, operation);
    }

    public DebuggerObject start() throws DebuggerException
    {
        return debugger.start();
    }

    public DebuggerObject event()
    {
        return debugger.event();
    }

}
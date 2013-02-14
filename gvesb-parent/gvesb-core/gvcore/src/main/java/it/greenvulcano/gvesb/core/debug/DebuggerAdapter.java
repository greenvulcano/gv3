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
package it.greenvulcano.gvesb.core.debug;

import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.debug.model.DebuggerObject;
import it.greenvulcano.gvesb.core.debug.model.Service;
import it.greenvulcano.gvesb.core.debug.model.ThreadInfo;

import java.util.Set;

/**
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
public class DebuggerAdapter
{

    /**
     * <p>
     * The stack of stack frames (the control stack)
     * </p>
     * <p>
     * Each stack frame is a mapping of variable names to values.
     * </p>
     * <p>
     * There are a number of special variable names:
     * </p>
     * <p>
     * _pc_ is the current program counter in the frame
     * </p>
     * <p>
     * the pc points to the next instruction to be executed
     * </p>
     * <p>
     * _func_ is the name of the function in this frame
     * </p>
     */
    private Service          debugService;
    private DebugSynchObject synchObject;

    public DebuggerAdapter() throws DebuggerException
    {
    }

    public DebuggerObject start() throws DebuggerException
    {
        try {
            ExecutionInfo execInfo = new ExecutionInfo(debugService.getServiceName(), debugService.getOperationName(),
                    null, null, null);
            synchObject = DebugSynchObject.waitNew(execInfo);
            String id = synchObject.getFlowId();
            debugService.setId(id);
            Set<String> set = synchObject.getOnDebugThreads();
            if (set != null && set.size() > 0) {
                debugService.loadInfo();

                return debugService;
            }
        }
        catch (Exception exc) {
            throw new DebuggerException(exc);
        }
        return null;
    }

    public DebuggerObject stack(String threadName)
    {
        ThreadInfo thread = debugService.getThread(threadName);
        thread.loadInfo(synchObject);
        return thread.getStackFrame();
    }

    public DebuggerObject var(String threadName, String stackFrame, String parent, String varName)
    {
        ThreadInfo thread = debugService.getThread(threadName);
        thread.loadInfo(synchObject);
        return thread.getStackFrame().getVar(stackFrame, parent, varName);
    }

    public DebuggerObject set_var(String threadName, String stackFrame, String parent, String varName, String varValue)
            throws DebuggerException
    {
        ThreadInfo thread = debugService.getThread(threadName);
        thread.loadInfo(synchObject);
        try {
            thread.getStackFrame().setVar(stackFrame, parent, varName, varValue);
        }
        catch (GVException e) {
            throw new DebuggerException(e);
        }
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject set(String threadName, String subflow, String nodeId) throws DebuggerException
    {
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        ExecutionInfo bp = new ExecutionInfo(synchObject.getExecutionInfo());
        bp.setNodeId(nodeId);
        bp.setSubflow(subflow);
        synchObject.setBreakpoint(bp);
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject data()
    {
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject clear(String threadName, String subflow, String nodeId) throws DebuggerException
    {
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        ExecutionInfo bp = new ExecutionInfo(synchObject.getExecutionInfo());
        bp.setNodeId(nodeId);
        bp.setSubflow(subflow);
        synchObject.clearBreakpoint(bp);
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject stepOver(String threadName) throws DebuggerException
    {
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        synchObject.stepOver();
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject stepInto(String threadName) throws DebuggerException
    {
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        synchObject.stepInto();
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject stepReturn(String threadName) throws DebuggerException
    {
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        synchObject.stepReturn();
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject resume(String threadName) throws DebuggerException
    {
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        synchObject.resume();
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject exit()
    {
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        synchObject.stop();
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject connect(String service, String operation)
    {
        debugService = new Service(service, operation);
        return debugService;
    }

    public DebuggerObject event()
    {
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        return synchObject != null ? synchObject.event() : null;
    }

}

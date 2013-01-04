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

import it.greenvulcano.gvesb.core.debug.model.DebuggerObject;
import it.greenvulcano.gvesb.core.debug.model.Service;
import it.greenvulcano.gvesb.core.debug.model.ThreadInfo;
import it.greenvulcano.gvesb.core.jmx.OperationInfo;
import it.greenvulcano.gvesb.core.jmx.ServiceOperationInfo;
import it.greenvulcano.gvesb.core.jmx.ServiceOperationInfoManager;

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
    private Service             debugService;
    private OperationInfo       operationInfo;
    private DebuggerEventThread debuggerEventThread;

    public DebuggerAdapter() throws DebuggerException
    {
    }

    public DebuggerObject start() throws DebuggerException
    {
        try {
            ServiceOperationInfoManager serviceOperationInfoManager = ServiceOperationInfoManager.instance();
            ServiceOperationInfo serviceOperationInfo = serviceOperationInfoManager.getServiceOperationInfo(
                    debugService.getServiceName(), false);

            operationInfo = serviceOperationInfo.getOperationInfo(debugService.getOperationName(), false);
            String id = operationInfo.markForDebug();

            debugService.setId(id);
            Set<String> set = operationInfo.getOnDebugIDs().get(id);
            if (set != null && set.size() > 0) {
                String threadName = set.iterator().next();
                String currentFlowNode = operationInfo.getFlowStatus(threadName, id);

                debugService.loadInfo(operationInfo);

                debuggerEventThread = new DebuggerEventThread(operationInfo, threadName, id, currentFlowNode);
                new Thread(debuggerEventThread).start();
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
        thread.loadInfo(operationInfo, debugService.getId());
        return thread.getStackFrame();
    }

    public DebuggerObject var(String threadName, String stackFrame, String varName)
    {
        ThreadInfo thread = debugService.getThread(threadName);
        thread.loadInfo(operationInfo, debugService.getId());
        return thread.getStackFrame().getVar(stackFrame, varName);
    }

    public DebuggerObject set(String threadName, String sBreakpoint)
    {
        operationInfo.setBreakpoint(threadName, debugService.getId(), sBreakpoint);
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject data()
    {
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject clear(String threadName, String cBreakpoint)
    {
        operationInfo.clearBreakpoint(threadName, debugService.getId(), cBreakpoint);
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject step(String threadName)
    {
        String currentNode = operationInfo.getFlowStatus(threadName, debugService.getId());
        operationInfo.step(threadName, debugService.getId());
        debuggerEventThread.step(currentNode);
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject resume(String threadName)
    {
        if (!debuggerEventThread.stopInStart()) {
            operationInfo.resume(threadName, debugService.getId());
        }
        debuggerEventThread.resume();
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject exit()
    {
        debuggerEventThread.stop();
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject connect(String service, String operation)
    {
        debugService = new Service(service, operation);
        return debugService;
    }

    public DebuggerObject event()
    {
        return debuggerEventThread.event();

    }
}

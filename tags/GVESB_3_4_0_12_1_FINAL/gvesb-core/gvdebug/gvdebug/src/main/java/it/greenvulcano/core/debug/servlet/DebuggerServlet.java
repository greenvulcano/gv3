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
package it.greenvulcano.core.debug.servlet;

import it.greenvulcano.gvesb.core.debug.ejb3.GVDebugger;
import it.greenvulcano.gvesb.core.debug.model.DebuggerObject;

import java.io.IOException;
import java.io.PrintWriter;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DebuggerServlet extends HttpServlet
{
    /**
     * 
     */
    private static final long   serialVersionUID  = 1L;

    private static final String STATEFUL_BEAN_KEY = "STATEFUL_BEAN_KEY";

    public enum DebugCommand {
        CONNECT, START, STACK, VAR, SET_VAR, DATA, SET, CLEAR, STEP_OVER, STEP_INTO, STEP_RETURN, RESUME, EXIT, EVENT, SKIP_ALL_BP
    }

    public enum DebugKey {
        debugOperation, service, operation, debuggerVersion, threadName, stackFrame, varEnv, varID, varValue, breakpoint, subflow, enabled
    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException
    {
        try {
            String dOp = request.getParameter(DebugKey.debugOperation.name());
            DebugCommand debugOperation = DebugCommand.valueOf(dOp.toUpperCase());
            PrintWriter writer = response.getWriter();
            DebuggerObject dObj = null;
            GVDebugger debuggerBean = null;
            if (debugOperation == DebugCommand.CONNECT) {
                debuggerBean = getDebuggerBean(request, true);
            }
            else {
                debuggerBean = getDebuggerBean(request);
            }
            synchronized (debuggerBean) {
                switch (debugOperation) {
                    case CONNECT : {
                        String service = request.getParameter(DebugKey.service.name());
                        String operation = request.getParameter(DebugKey.operation.name());
                        String version = request.getParameter(DebugKey.debuggerVersion.name());
                        dObj = debuggerBean.connect(version, service, operation);
                    }
                        break;
                    case START : {
                        dObj = debuggerBean.start();
                    }
                        break;
                    case STACK : {
                        String threadName = request.getParameter(DebugKey.threadName.name());
                        dObj = debuggerBean.stack(threadName);
                    }
                        break;
                    case VAR : {
                        String stackFrame = request.getParameter(DebugKey.stackFrame.name());
                        String varEnv = request.getParameter(DebugKey.varEnv.name());
                        String varID = request.getParameter(DebugKey.varID.name());
                        String threadName = request.getParameter(DebugKey.threadName.name());
                        dObj = debuggerBean.var(threadName, stackFrame, varEnv, varID);
                    }
                        break;
                    case SET_VAR : {
                        String stackFrame = request.getParameter(DebugKey.stackFrame.name());
                        String varEnv = request.getParameter(DebugKey.varEnv.name());
                        String varID = request.getParameter(DebugKey.varID.name());
                        String varValue = request.getParameter(DebugKey.varValue.name());
                        String threadName = request.getParameter(DebugKey.threadName.name());
                        dObj = debuggerBean.set_var(threadName, stackFrame, varEnv, varID, varValue);
                    }
                        break;
                    case DATA :
                        dObj = debuggerBean.data();
                        break;
                    case SET : {
                        String threadName = request.getParameter(DebugKey.threadName.name());
                        String sBreakpoint = request.getParameter(DebugKey.breakpoint.name());
                        String subflow = request.getParameter(DebugKey.subflow.name());
                        dObj = debuggerBean.set(threadName, subflow, sBreakpoint);
                    }
                        break;
                    case CLEAR : {
                        String threadName = request.getParameter(DebugKey.threadName.name());
                        String cBreakpoint = request.getParameter(DebugKey.breakpoint.name());
                        String subflow = request.getParameter(DebugKey.subflow.name());
                        dObj = debuggerBean.clear(threadName, subflow, cBreakpoint);
                    }
                        break;
                    case SKIP_ALL_BP : {
                        boolean enabled = Boolean.parseBoolean(request.getParameter(DebugKey.enabled.name()));
                        dObj = debuggerBean.skipAllBreakpoints(enabled);
                    }
                        break;
                    case STEP_OVER : {
                        String threadName = request.getParameter(DebugKey.threadName.name());
                        dObj = debuggerBean.stepOver(threadName);
                    }
                        break;
                    case STEP_INTO : {
                        String threadName = request.getParameter(DebugKey.threadName.name());
                        dObj = debuggerBean.stepInto(threadName);
                    }
                        break;
                    case STEP_RETURN : {
                        String threadName = request.getParameter(DebugKey.threadName.name());
                        dObj = debuggerBean.stepReturn(threadName);
                    }
                        break;
                    case RESUME : {
                        String threadName = request.getParameter(DebugKey.threadName.name());
                        dObj = debuggerBean.resume(threadName);
                    }
                        break;
                    case EXIT :
                        dObj = debuggerBean.exit();
                        break;
                    case EVENT :
                        dObj = debuggerBean.event();
                        break;

                    default :
                        break;
                }

            }
            if (dObj == null) {
                dObj = DebuggerObject.FAIL_DEBUGGER_OBJECT;
            }
            writer.println(dObj.toXML());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    private GVDebugger getDebuggerBean(HttpServletRequest request) throws ServletException
    {
        return getDebuggerBean(request, false);
    }

    private GVDebugger getDebuggerBean(HttpServletRequest request, boolean start) throws ServletException
    {
        HttpSession httpSession = request.getSession(true);

        GVDebugger debuggerBean = (GVDebugger) httpSession.getAttribute(STATEFUL_BEAN_KEY);
        if (debuggerBean == null || start) {
            try {
                InitialContext ic = new InitialContext();
                debuggerBean = (GVDebugger) ic.lookup("gvesb/core/GVDebugger");
                httpSession.setAttribute(STATEFUL_BEAN_KEY, debuggerBean);
            }
            catch (NamingException e) {
                e.printStackTrace();
                throw new ServletException(e);
            }
        }
        return debuggerBean;
    }
}

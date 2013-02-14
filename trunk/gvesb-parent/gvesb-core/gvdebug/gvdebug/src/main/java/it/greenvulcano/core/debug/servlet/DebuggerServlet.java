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
        CONNECT, START, STACK, VAR, SET_VAR, DATA, SET, CLEAR, STEP_OVER, STEP_INTO, STEP_RETURN, RESUME, EXIT, EVENT
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
            String dOp = request.getParameter("debugOperation");
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
                        String service = request.getParameter("service");
                        String operation = request.getParameter("operation");
                        dObj = debuggerBean.connect(service, operation);
                    }
                        break;
                    case START : {
                        dObj = debuggerBean.start();
                    }
                        break;
                    case STACK : {
                        String threadName = request.getParameter("threadName");
                        dObj = debuggerBean.stack(threadName);
                    }
                        break;
                    case VAR : {
                        String stackFrame = request.getParameter("stackFrame");
                        String varParent = request.getParameter("varParent");
                        String varName = request.getParameter("varName");
                        String threadName = request.getParameter("threadName");
                        dObj = debuggerBean.var(threadName, stackFrame, varParent, varName);
                    }
                        break;
                    case SET_VAR : {
                        String stackFrame = request.getParameter("stackFrame");
                        String varParent = request.getParameter("varParent");
                        String varName = request.getParameter("varName");
                        String varValue = request.getParameter("varValue");
                        String threadName = request.getParameter("threadName");
                        dObj = debuggerBean.set_var(threadName, stackFrame, varParent, varName, varValue);
                    }
                        break;
                    case DATA :
                        dObj = debuggerBean.data();
                        break;
                    case SET : {
                        String threadName = request.getParameter("threadName");
                        String sBreakpoint = request.getParameter("breakpoint");
                        String subflow = request.getParameter("subflow");
                        dObj = debuggerBean.set(threadName, subflow, sBreakpoint);
                    }
                        break;
                    case CLEAR : {
                        String threadName = request.getParameter("threadName");
                        String cBreakpoint = request.getParameter("breakpoint");
                        String subflow = request.getParameter("subflow");
                        dObj = debuggerBean.clear(threadName, subflow, cBreakpoint);
                    }
                        break;
                    case STEP_OVER : {
                        String threadName = request.getParameter("threadName");
                        dObj = debuggerBean.stepOver(threadName);
                    }
                        break;
                    case STEP_INTO : {
                        String threadName = request.getParameter("threadName");
                        dObj = debuggerBean.stepInto(threadName);
                    }
                        break;
                    case STEP_RETURN : {
                        String threadName = request.getParameter("threadName");
                        dObj = debuggerBean.stepReturn(threadName);
                    }
                        break;
                    case RESUME : {
                        String threadName = request.getParameter("threadName");
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

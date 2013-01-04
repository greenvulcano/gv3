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

import it.greenvulcano.gvesb.core.debug.model.DebuggerEventObject;
import it.greenvulcano.gvesb.core.debug.model.DebuggerEventObject.Event;
import it.greenvulcano.gvesb.core.debug.model.DebuggerObject;
import it.greenvulcano.gvesb.core.jmx.OperationInfo;

import java.util.Deque;
import java.util.LinkedList;

/**
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
public class DebuggerEventThread implements Runnable
{

    private Deque<DebuggerObject> events;
    private boolean               run;
    private boolean               started;
    private boolean               step;
    private boolean               suspend;
    private boolean               stopInStart;
    private boolean               brokepoint;
    private String                currentFlowNode;
    private OperationInfo         operationInfo;
    private String                flowId;
    private String                threadName;

    public DebuggerEventThread(OperationInfo operationInfo, String threadName, String flowId, String currentFlowNode)
    {
        events = new LinkedList<DebuggerObject>();
        run = false;
        suspend = true;
        stopInStart = true;
        this.operationInfo = operationInfo;
        this.threadName = threadName;
        this.flowId = flowId;
        this.currentFlowNode = currentFlowNode;
    }

    @Override
    public void run()
    {
        try {
            sendDebugEvent(Event.STARTED);
            run = true;
            do {
                checkForBreakpoint();
                debugUI();
                if (suspend) {
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            while (run && currentFlowNode != null);
        }
        finally {
            try {
                sendDebugEvent(Event.TERMINATED);
            }
            catch (Exception exc) {
            }
        }
    }

    public void step(String currentFlowNode)
    {
        step = true;
        suspend = false;
        this.currentFlowNode = currentFlowNode;
    }

    public void resume()
    {
        suspend = false;
        if (stopInStart) {
            stopInStart = false;
            suspend = true;
            step = true;
        }
    }

    private void sendDebugEvent(Event event, String... props)
    {
        DebuggerObject dObj = new DebuggerEventObject(event, props);
        events.add(dObj);
    }

    public DebuggerObject event()
    {
        return events.isEmpty() ? DebuggerEventObject.NO_EVENT : events.poll();
    }

    public void stop()
    {
        run = false;
    }


    private void debugUI()
    {
        if (!suspend) {
            return;
        }
        if (!started) {
            if (step) {
                sendDebugEvent(Event.SUSPENDED, "step");
            }
            if (!step && brokepoint) {
                sendDebugEvent(Event.SUSPENDED, "breakpoint", currentFlowNode);
            }
            if (!step && !brokepoint) {
                sendDebugEvent(Event.SUSPENDED, "client");
            }
        }
        else {
            started = false;
        }
        step = false;
        brokepoint = false;
        while (run && suspend) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (step) {
            sendDebugEvent(Event.RESUMED, "step");
        }
        else {
            sendDebugEvent(Event.RESUMED, "client");
        }
    }

    private void checkForBreakpoint()
    {
        String nextFlowNode = null;
        while (run
                && (currentFlowNode == (nextFlowNode = operationInfo.getFlowStatus(threadName, flowId)) || !operationInfo.mustStop(
                        threadName, flowId, nextFlowNode))) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        currentFlowNode = nextFlowNode;
        if (nextFlowNode != null && !"".equals(nextFlowNode)) {
            suspend = true;
            brokepoint = true;
        }
    }

    public boolean stopInStart()
    {
        return stopInStart;
    }
}

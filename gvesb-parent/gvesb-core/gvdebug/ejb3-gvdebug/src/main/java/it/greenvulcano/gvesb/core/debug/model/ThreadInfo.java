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
package it.greenvulcano.gvesb.core.debug.model;

import it.greenvulcano.gvesb.core.jmx.OperationInfo;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
public class ThreadInfo extends DebuggerObject
{

    private String     tName;
    private FrameStack frameStack;
    private String     serviceName;

    public ThreadInfo(String tName, String serviceName)
    {
        this.tName = tName;
        this.serviceName = serviceName;
        frameStack = new FrameStack();
    }

    /**
     * @see it.greenvulcano.gvesb.core.debug.model.DebuggerObject#getXML(it.greenvulcano.util.xml.XMLUtils,
     *      org.w3c.dom.Document)
     */
    @Override
    protected Node getXML(XMLUtils xml, Document doc) throws XMLUtilsException
    {
        Element thread = xml.createElement(doc, "Thread");
        thread.setAttribute(NAME_ATTR, tName);
        thread.appendChild(frameStack.getXML(xml, doc));
        return thread;
    }

    public void loadInfo(OperationInfo operationInfo, String flowId)
    {
        String flowNode = operationInfo.getFlowStatus(tName, flowId);

        if (flowNode != null) {
            String opName = operationInfo.getOperation();
            String frameName = serviceName + "/" + opName;
            Frame f = new Frame(frameName, serviceName, opName, flowNode);
            frameStack.add(f);
            frameStack.loadInfo(operationInfo, tName, flowId);
        }
    }

    public FrameStack getStackFrame()
    {
        return frameStack;
    }

}

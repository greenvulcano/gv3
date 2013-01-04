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

import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
public class FrameStack extends DebuggerObject
{

    private Map<String, Frame> frames;
    private Frame              currentFrame;

    public FrameStack()
    {
        frames = new LinkedHashMap<String, Frame>();
    }

    public Frame getCurrentFrame()
    {
        return currentFrame;
    }

    @Override
    protected Node getXML(XMLUtils xml, Document doc) throws XMLUtilsException
    {
        Element frameStack = xml.createElement(doc, "FrameStack");
        if (!frames.isEmpty()) {
            for (Frame f : frames.values()) {
                frameStack.appendChild(f.getXML(xml, doc));
            }
        }
        if (currentFrame != null) {
            frameStack.appendChild(currentFrame.getXML(xml, doc));
        }
        return frameStack;
    }

    public DebuggerObject getVar(String stackFrame, String varName)
    {
        Frame f = frames.get(stackFrame);
        if (f == null) {
            return currentFrame.getVar(varName);
        }
        return f.getVar(varName);
    }

    public void add(Frame f)
    {
//        if (currentFrame != null && f != null && !f.getName().equals(currentFrame.getName())
//                && !frames.containsKey(currentFrame.getName())) {
//            frames.put(currentFrame.getName(), currentFrame);
//        }
        currentFrame = f;
    }

    public void loadInfo(OperationInfo operationInfo, String threadName, String flowId)
    {
        for (Frame f : frames.values()) {
            f.loadInfo(operationInfo, threadName, flowId);
        }
        currentFrame.loadInfo(operationInfo, threadName, flowId);
    }
}

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

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.jmx.OperationInfo;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
public class Frame extends DebuggerObject
{
    private Map<String, Variable> vars = null;
    private String                serviceName;
    private String                operationName;
    private String                flowNode;
    private String                frameName;

    public Frame(String frameName, String serviceName, String operationName, String flowNode)
    {
        super();
        this.serviceName = serviceName;
        this.operationName = operationName;
        this.flowNode = flowNode;
        this.frameName = frameName;
        vars = new LinkedHashMap<String, Variable>();
    }

    public Variable getVar(String key)
    {
        return vars.get(key);
    }

    public Map<String, Variable> getVars()
    {
        return vars;
    }

    public void setVars(Map<String, Variable> vars)
    {
        this.vars = vars;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public void setServiceName(String sName)
    {
        this.serviceName = sName;
    }

    public String getOperationName()
    {
        return operationName;
    }

    public void setOperationName(String opName)
    {
        this.operationName = opName;
    }

    public String getFlowNode()
    {
        return flowNode;
    }

    public void setFlowNode(String flowNode)
    {
        this.flowNode = flowNode;
    }

    /**
     * @see it.greenvulcano.gvesb.core.debug.model.DebuggerObject#getXML(it.greenvulcano.util.xml.XMLUtils,
     *      org.w3c.dom.Document)
     */
    @Override
    protected Node getXML(XMLUtils xml, Document doc) throws XMLUtilsException
    {
        Element frame = xml.createElement(doc, "Frame");
        frame.setAttribute("service_name", serviceName);
        frame.setAttribute("operation_name", operationName);
        frame.setAttribute("flow_node", flowNode);
        frame.setAttribute("name", frameName);
        if (vars != null && vars.size() > 0) {
            Element varEl = xml.insertElement(frame, "Variables");
            for (String e : vars.keySet()) {
                Element var = xml.insertElement(varEl, Variable.ELEMENT_TAG);
                xml.setAttribute(var, NAME_ATTR, e);
            }
        }
        return frame;
    }

    public void loadInfo(OperationInfo operationInfo, String threadName, String flowId)
    {
        Set<String> envEntryKeys = operationInfo.getEnvEntryKeys(threadName, flowId);
        if (envEntryKeys != null) {
            for (String key : envEntryKeys) {
                Object envEntry = operationInfo.getEnvEntry(threadName, flowId, key);
                if (envEntry instanceof GVBuffer) {
                    vars.putAll(fromGVBuffer(key, (GVBuffer) envEntry));
                }
            }
        }
    }

    public Map<String, Variable> fromGVBuffer(String envKey, GVBuffer gvBuffer)
    {
        Map<String, Variable> vMap = new LinkedHashMap<String, Variable>();
        String vName = envKey + "#$SYSTEM";
        Variable v = new Variable(vName, gvBuffer.getSystem());
        vMap.put(vName, v);
        vName = envKey + "#$SERVICE";
        v = new Variable(vName, gvBuffer.getService());
        vMap.put(vName, v);
        vName = envKey + "#$ID";
        v = new Variable(vName, gvBuffer.getId().toString());
        vMap.put(vName, v);
        vName = envKey + "#$RETCODE";
        v = new Variable(vName, Integer.toString(gvBuffer.getRetCode()));
        vMap.put(vName, v);
        vName = envKey + "#$OBJECT";
        v = new Variable(vName, gvBuffer.getObject());
        vMap.put(vName, v);
        Set<String> namesSet = gvBuffer.getPropertyNamesSet();
        for (String name : namesSet) {
            vName = envKey + "#" + name;
            v = new Variable(vName, gvBuffer.getProperty(name));
            vMap.put(vName, v);
        }
        return vMap;
    }

}

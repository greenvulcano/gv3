/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
import it.greenvulcano.gvesb.buffer.GVBuffer.Field;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
public class Variable extends DebuggerObject
{
    /**
     * 
     */
    private static final long     serialVersionUID = 1L;
    public static final String    ELEMENT_TAG      = "Variable";
    private String                name             = null;
    private Object                value            = null;
    private Map<String, Variable> values           = null;
    private boolean               isGVBuffer       = false;
    private GVBuffer              origGVBuffer     = null;
    private Class<?>              type;
    private Field                 gvField;

    public Variable(String name, Class<?> type)
    {
        this.name = name;
        this.type = type;
    }

    public Variable(String name, Class<?> type, Object object)
    {
        this(name, type);
        isGVBuffer = object instanceof GVBuffer;
        if (isGVBuffer) {
            values = new HashMap<String, Variable>();
            origGVBuffer = (GVBuffer) object;

            Variable v = new Variable(Field.SYSTEM, String.class, origGVBuffer.getSystem());
            values.put(v.name, v);
            v = new Variable(Field.SERVICE, String.class, origGVBuffer.getService());
            values.put(v.name, v);
            v = new Variable(Field.ID, Id.class, origGVBuffer.getId());
            values.put(v.name, v);
            v = new Variable(Field.RETCODE, Integer.class, origGVBuffer.getRetCode());
            values.put(v.name, v);
            v = new Variable(Field.OBJECT, origGVBuffer.getObject().getClass(), origGVBuffer.getObject());
            values.put(v.name, v);
            Set<String> namesSet = origGVBuffer.getPropertyNamesSet();
            for (String pname : namesSet) {
                v = new Variable(Field.PROPERTY, pname, String.class, origGVBuffer.getProperty(pname));
                values.put(pname, v);
            }
        }
        else {
            this.value = object;
        }
    }

    public Variable(Field field, Class<?> type, Object object)
    {
        this("$" + field, type, object);
        this.gvField = field;
    }

    public Variable(Field field, String name, Class<?> type, Object object)
    {
        this(name, type, object);
        this.gvField = field;
    }

    /**
     * @see it.greenvulcano.gvesb.core.debug.model.DebuggerObject#getXML(it.greenvulcano.util.xml.XMLUtils,
     *      org.w3c.dom.Document)
     */
    @Override
    protected Node getXML(XMLUtils xml, Document doc) throws XMLUtilsException
    {
        Element var = xml.createElement(doc, ELEMENT_TAG);
        xml.setAttribute(var, NAME_ATTR, name);
        xml.setAttribute(var, TYPE_ATTR, getTypeName());
        if (isGVBuffer) {
            for (Variable v : values.values()) {
                var.appendChild(v.getXML(xml, doc));
            }
        }
        else {
            if (value != null) {
                var.setTextContent(value.toString());
            }
        }
        return var;
    }

    public String getTypeName()
    {
        return type.getName();
    }

    public Variable getVar(String key)
    {
        return isGVBuffer ? values.get(key) : null;
    }

    public void setVar(String varName, String varValue) throws GVException
    {
        if (isGVBuffer) {
            Variable variable = values.get(varName);
            switch (variable.gvField) {
                case SYSTEM :
                    origGVBuffer.setSystem(varValue);
                    break;
                case SERVICE :
                    origGVBuffer.setService(varValue);
                    break;
                case OBJECT :
                    origGVBuffer.setObject(varValue);
                    break;
                case PROPERTY :
                    origGVBuffer.setProperty(varName, varValue);
                    break;
            }
            variable.setVar(varName, varValue);
        }
        else {
            value = varValue;
        }
    }
}

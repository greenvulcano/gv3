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

import it.greenvulcano.gvesb.core.debug.DebuggerException;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
public class DebuggerObject implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public enum Result {
        OK, FAIL
    }

    public static final String   NAME_ATTR            = "name";
    public static final String   TYPE_ATTR            = "type";
    public static final String   VALUE_ATTR           = "value";

    public static DebuggerObject OK_DEBUGGER_OBJECT   = new DebuggerObject(Result.OK);
    public static DebuggerObject FAIL_DEBUGGER_OBJECT = new DebuggerObject(Result.FAIL);

    private Result               result;

    public DebuggerObject()
    {
        this(Result.OK);
    }

    public DebuggerObject(Result result)
    {
        this.result = result;
    }

    protected Node getXML(XMLUtils xml, Document doc) throws XMLUtilsException
    {
        return null;
    }

    public String toXML() throws DebuggerException
    {
        XMLUtils xml = null;
        try {
            xml = XMLUtils.getParserInstance();
            Document doc = xml.newDocument("GVDebugger");
            Element root = doc.getDocumentElement();
            root.setAttribute("result", result.toString());
            Node node = getXML(xml, doc);
            if (node != null) {
                root.appendChild(node);
            }
            return xml.serializeDOM(doc);
        }
        catch (XMLUtilsException e) {
            throw new DebuggerException(e);
        }
        finally {
            XMLUtils.releaseParserInstance(xml);
        }

    }
}

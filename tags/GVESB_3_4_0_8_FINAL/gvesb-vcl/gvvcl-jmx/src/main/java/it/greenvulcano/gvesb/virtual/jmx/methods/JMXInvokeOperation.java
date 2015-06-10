/*
 * Copyright (c) 2009-2011 GreenVulcano ESB Open Source Project. All rights
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

package it.greenvulcano.gvesb.virtual.jmx.methods;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.jmx.JMXMethodOperation;
import it.greenvulcano.jmx.JMXUtils;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.1.0 May 2, 2011
 * @author GreenVulcano Developer Team
 * 
 *         REVISION OK
 */
public class JMXInvokeOperation implements JMXMethodOperation
{
    /**
     * The GVLogger instance
     */
    private static final Logger logger = GVLogger.getLogger(JMXInvokeOperation.class);

    private String              method;
    private String[]            types;

    private String[]            values;

    /**
     * @see it.greenvulcano.gvesb.virtual.jmx.JMXMethodOperation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node config) throws InitializationException
    {
        try {
            method = XMLConfig.get(config, "@method");
            initParameters(XMLConfig.getNodeList(config, "invoke-parameter"));
        }
        catch (Exception exc) {
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
    }

    /**
     * @param nodeList
     * @throws XMLConfigException
     */
    private void initParameters(NodeList paramList) throws XMLConfigException
    {
        int paramLength = paramList == null ? 0 : paramList.getLength();
        types = new String[paramLength];
        values = new String[paramLength];

        for (int i = 0; i < paramLength; ++i) {
            Node paramNode = paramList.item(i);
            String type = XMLConfig.get(paramNode, "@type");
            Class<?> cls = null;
            if (type.equals("byte")) {
                cls = Byte.TYPE;
            }
            else if (type.equals("boolean")) {
                cls = Boolean.TYPE;
            }
            else if (type.equals("char")) {
                cls = Character.TYPE;
            }
            else if (type.equals("double")) {
                cls = Double.TYPE;
            }
            else if (type.equals("float")) {
                cls = Float.TYPE;
            }
            else if (type.equals("int")) {
                cls = Integer.TYPE;
            }
            else if (type.equals("long")) {
                cls = Long.TYPE;
            }
            else if (type.equals("short")) {
                cls = Short.TYPE;
            }
            else if (type.equals("String")) {
                cls = String.class;
            }
            types[i] = cls.getName();
            values[i] = XMLConfig.get(paramNode, "@value");
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.jmx.JMXMethodOperation#perform(java.lang.String,
     *      java.util.Map, it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public Object perform(String objectName, Map<String, Object> props, GVBuffer gvBuffer) throws Exception
    {
        Object[] params = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            if (!PropertiesHandler.isExpanded(value)) {
                value = PropertiesHandler.expand(value, props, gvBuffer);
            }
            params[i] = JMXCallUtils.cast(value, types[i]);
        }
        return JMXUtils.invoke(objectName, method, params, types, logger);
    }
}

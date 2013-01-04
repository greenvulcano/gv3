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
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.jmx.JMXMethodOperation;
import it.greenvulcano.jmx.JMXUtils;
import it.greenvulcano.log.GVLogger;

import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @version 3.1.0 May 2, 2011
 * @author GreenVulcano Developer Team
 * 
 *         REVISION OK
 */
public class JMXGetOperation implements JMXMethodOperation
{
    /**
     * The GVLogger instance
     */
    private static final Logger logger = GVLogger.getLogger(JMXGetOperation.class);

    private String              attributeName;

    /**
     * @see it.greenvulcano.gvesb.virtual.jmx.JMXMethodOperation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node config) throws InitializationException
    {
        try {
            attributeName = XMLConfig.get(config, "@attribute");
        }
        catch (Exception exc) {
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.jmx.JMXMethodOperation#perform(java.lang.String,
     *      java.util.Map, it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public Object perform(String objectName, Map<String, Object> props, GVBuffer gvBuffer) throws Exception
    {
        return JMXUtils.get(objectName, attributeName, logger);
    }

}

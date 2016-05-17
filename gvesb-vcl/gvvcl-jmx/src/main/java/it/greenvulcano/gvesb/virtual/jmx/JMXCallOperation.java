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
package it.greenvulcano.gvesb.virtual.jmx;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.CreateException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @version 3.1.0 May 02, 2011
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class JMXCallOperation implements CallOperation
{
    /**
     * The GVLogger instance
     */
    private static final Logger logger       = GVLogger.getLogger(JMXCallOperation.class);

    private OperationKey        key          = null;
    private JMXMethodOperation  jmxOperation = null;

    private String              objectName;

    /**
     * Invoked from <code>OperationFactory</code> when an <code>Operation</code>
     * needs initialization.<br>
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node config) throws InitializationException
    {
        try {
            objectName = XMLConfig.get(config, "@object-name");
            initMethod(XMLConfig.getNode(config, "*[@type='method']"));
        }
        catch (InitializationException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
    }

    private void initMethod(Node methodConf) throws XMLConfigException, CreateException, InitializationException
    {
        if (methodConf == null) {
            throw new NullPointerException("No method configuration found");
        }

        // The class name is contained in the 'class' attribute
        String className = XMLConfig.get(methodConf, "@class");
        logger.debug("Class name: " + className);

        // Obtains the Class
        Class<?> cls = null;
        try {
            cls = Class.forName(className);
        }
        catch (ClassNotFoundException exc) {
            throw new CreateException("GVVCL_CLASS_NOT_FOUND_ERROR", new String[][]{{"className", className}}, exc);
        }

        // Instantiate an Object that must be a JMXMethodOperation
        try {
            jmxOperation = (JMXMethodOperation) cls.newInstance();
        }
        catch (Exception exc) {
            throw new CreateException("GVVCL_INSTANTIATION_ERROR", new String[][]{{"className", className},
                    {"exc", exc.toString()}}, exc);
        }

        // Initialize the new created object from it's configuration node
        jmxOperation.init(methodConf);

    }

    /**
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        logger.debug("BEGIN perform(GVBuffer gvBuffer)");
        String expandedObjectName;
        try {
            Map<String, Object> props = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            expandedObjectName = PropertiesHandler.expand(objectName, props, gvBuffer);
            GVBuffer result = new GVBuffer(gvBuffer);
            result.setObject(jmxOperation.perform(expandedObjectName, props, gvBuffer));
            return result;
        }
        catch (Exception exc) {
            logger.error("ERROR perform(GVBuffer gvBuffer)", exc);
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    @Override
    public OperationKey getKey()
    {
        return key;
    }
}

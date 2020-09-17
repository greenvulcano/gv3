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
package it.greenvulcano.gvesb.core.forward.util;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.internal.condition.ExceptionCondition;

import org.w3c.dom.Node;

/**
 * ErrorHandler class.
 *
 * @version 3.2.0 Gen 11, 2012
 * @author GreenVulcano Developer Team
 */
public class ErrorHandler
{
    /**
     * The handler name.
     */
    private String             name                 = "";
    /**
     * The operation to invoke.
     */
    private String             errorOperation       = "";
    /**
     * The id_system to set.
     */
    private String             errorID_SYSTEM       = "";
    /**
     * The id_service to set.
     */
    private String             errorID_SERVICE      = "";
    /**
     * The property name that hold the original system.
     */
    private String             originalSystemField  = "";
    /**
     * The property name that hold the original service.
     */
    private String             originalServiceField = "";
    /**
     * The condition to test.
     */
    private ExceptionCondition condition            = null;

    /**
     * Constructor.
     */
    public ErrorHandler()
    {
        // do nothing
    }

    /**
     * Initialize the instance.
     *
     * @param node
     *        the configuration node
     * @throws GVCoreConfException
     *         if error occurs
     */
    public void init(Node node) throws GVCoreConfException
    {
        try {
            name = XMLConfig.get(node, "@name");
            errorOperation = XMLConfig.get(node, "@error-operation");
            errorID_SYSTEM = XMLConfig.get(node, "@error-id_system", GVBuffer.DEFAULT_SYS);
            errorID_SERVICE = XMLConfig.get(node, "@error-id_service");
            originalSystemField = XMLConfig.get(node, "@original-system-field");
            originalServiceField = XMLConfig.get(node, "@original-service-field");
            Node condNode = XMLConfig.getNode(node, "ExceptionCondition");
            if (condNode != null) {
                condition = new ExceptionCondition();
                condition.init(condNode);
            }
        }
        catch (Exception exc) {
            throw new GVCoreConfException("Error initializing ErrorHandler", exc);
        }
    }

    /**
     * Check if the error must be handled.
     *
     * @param exception
     *        the exception to check
     * @return true if the error can be handled
     * @throws Exception
     */
    public boolean mustHandleError(Exception exception) throws Exception
    {
        if (condition == null) {
            return true;
        }
        return condition.check(exception);
    }

    /**
     * Change the GVBuffer fields.
     *
     * @param data
     *        the GVBuffer to change
     * @return the changed GVBuffer
     * @throws GVCoreException
     *         if error occurs
     */
    public final GVBuffer prepareBuffer(GVBuffer data) throws GVCoreException
    {
        try {
            data.setProperty(originalSystemField, data.getSystem());
            data.setProperty(originalServiceField, data.getService());
            data.setSystem(GVBuffer.DEFAULT_SYS.equals(errorID_SYSTEM) ? data.getSystem() : errorID_SYSTEM);
            data.setService(errorID_SERVICE);
            return data;
        }
        catch (Exception exc) {
            throw new GVCoreException("Error preparing GVBuffer", exc);
        }
    }

    /**
     * @return the handler name
     */
    public final String getName()
    {
        return name;
    }

    /**
     * @return the operation to invoke
     */
    public final String getErrorOperation()
    {
        return errorOperation;
    }

}

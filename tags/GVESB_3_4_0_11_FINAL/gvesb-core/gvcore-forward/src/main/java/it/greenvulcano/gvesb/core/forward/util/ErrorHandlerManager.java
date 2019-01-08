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
import it.greenvulcano.log.GVLogger;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The ErrorHandlerManager class.
 *
 * @version 3.2.0 Gen 11, 2012
 * @author GreenVulcano Developer Team
 */
public class ErrorHandlerManager
{
    /**
     * Logger.
     */
    private static Logger        logger         = GVLogger.getLogger(ErrorHandlerManager.class);
    /**
     * The list of error handlers.
     */
    private Vector<ErrorHandler> errorHandlers  = new Vector<ErrorHandler>();
    /**
     * The matching error handler.
     */
    private ErrorHandler         currentHandler = null;

    /**
     * Constructor.
     */
    public ErrorHandlerManager()
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
        NodeList nl = null;
        try {
            nl = XMLConfig.getNodeList(node, "ErrorHandler");
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    ErrorHandler errorHandler = new ErrorHandler();
                    errorHandler.init(nl.item(i));
                    logger.debug("Configured ErrorHandler '" + errorHandler.getName() + "'");
                    errorHandlers.add(errorHandler);
                }
            }
        }
        catch (GVCoreConfException exc) {
            errorHandlers.clear();
            throw exc;
        }
        catch (Exception exc) {
            errorHandlers.clear();
            throw new GVCoreConfException("Error initializing ErrorHandlerManager", exc);
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
        currentHandler = null;
        if (errorHandlers.isEmpty()) {
            return false;
        }
        Iterator<ErrorHandler> i = errorHandlers.iterator();
        while (i.hasNext()) {
            currentHandler = i.next();
            if (currentHandler.mustHandleError(exception)) {
                return true;
            }
        }
        currentHandler = null;
        return false;
    }

    /**
     * Change the GVBuffer fields.
     *
     * @param gvBuffer
     *        the GVBuffer to change
     * @return the changed GVBuffer
     * @throws GVCoreException
     *         if error occurs
     */
    public final GVBuffer prepareBuffer(GVBuffer gvBuffer) throws GVCoreException
    {
        if (currentHandler == null) {
            throw new GVCoreException("Invalid state, no ErrorHandler valid.");
        }
        return currentHandler.prepareBuffer(gvBuffer);
    }

    /**
     * @return the matching handler name
     * @throws GVCoreException
     *         if error occurs
     */
    public final String getName() throws GVCoreException
    {
        if (currentHandler == null) {
            throw new GVCoreException("Invalid state, no ErrorHandler valid.");
        }
        return currentHandler.getName();
    }

    /**
     * @return the matching operation to invoke
     * @throws GVCoreException
     *         if error occurs
     */
    public final String getErrorOperation() throws GVCoreException
    {
        if (currentHandler == null) {
            throw new GVCoreException("Invalid state, no ErrorHandler valid.");
        }
        return currentHandler.getErrorOperation();
    }
}

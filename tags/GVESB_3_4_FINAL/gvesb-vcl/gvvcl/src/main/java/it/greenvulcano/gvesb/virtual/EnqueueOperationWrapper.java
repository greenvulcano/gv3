/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.virtual;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.ThreadUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * <code>EnqueueOperationWrapper</code> is a wrapper for an <code>EnqueueOperation</code>
 * instances that provides standard functionalities to all enqueue operations.
 * <p>
 * In particular this version add standard logs for initialization,
 * execution and destruction.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class EnqueueOperationWrapper implements EnqueueOperation
{
    private static final Logger logger       = GVLogger.getLogger(EnqueueOperationWrapper.class);

    /**
     * Wrapped operation.
     */
    private EnqueueOperation    operation;

    /**
     * The operation key.
     */
    private OperationKey        opKey;

    /**
     * Description of the wrapped operation.
     */
    private String              description;

    /**
     * The service name aliasing manager.
     */
    private ServiceAlias        serviceAlias = null;

    /**
     * Constructor.
     *
     * @param operation
     *        wrapped operation.
     * @param description
     *        used in the log files.
     */
    public EnqueueOperationWrapper(EnqueueOperation operation, String description)
    {
        this.operation = operation;
        this.description = description;
    }

    /**
     * Add standard logging functionalities and delegates to the wrapped
     * operation.
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws InitializationException
    {
        logger.debug("BEGIN INITIALIZATION: " + description);
        try {
            operation.init(node);
            serviceAlias = new ServiceAlias();
            serviceAlias.init(node);
        }
        catch (InitializationException exc) {
            logger.error("INITIALIZATION ERROR: " + description, exc);
            throw exc;
        }
        finally {
            logger.debug("END INITIALIZATION: " + description);
        }
    }

    /**
     * Add standard logging functionalities and delegates to the wrapped
     * operation.
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, EnqueueException, InvalidDataException,
            InterruptedException {
        logger.debug("BEGIN PERFORM: " + description);

        try {
            ThreadUtils.checkInterrupted(description, logger);
            serviceAlias.manageAliasInput(gvBuffer);
            GVBuffer returnData = null;
            NMDC.push();
            try {
                returnData = operation.perform(gvBuffer);
            }
            finally {
                NMDC.pop();
            }
            serviceAlias.manageAliasOutput(returnData);
            return returnData;
        }
        catch (ConnectionException exc) {
            logger.error("PERFORM ERROR: " + description, exc);
            throw exc;
        }
        catch (EnqueueException exc) {
            logger.error("PERFORM ERROR: " + description, exc);
            throw exc;
        }
        catch (InvalidDataException exc) {
            logger.error("PERFORM ERROR: " + description, exc);
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("PERFORM ERROR: " + description, exc);
            ThreadUtils.checkInterrupted(exc);
            throw new EnqueueException("GVVM_EXECUTION_ERROR", new String[][]{{"exc", "" + exc},
                    {"key", opKey.toString()}}, exc);
        }
        finally {
            logger.debug("END PERFORM: " + description);
        }
    }

    /**
     * Add standard logging functionalities and delegates to the wrapped
     * operation.
     */
    public void cleanUp()
    {
        logger.debug("BEGIN CLEANUP: " + description);
        try {
            operation.cleanUp();
        }
        finally {
            logger.debug("END CLEANUP: " + description);
        }
    }

    /**
     * Add standard logging functionalities and delegates to the wrapped
     * operation.
     */
    public void destroy()
    {
        logger.debug("BEGIN DESTROY: " + description);
        try {
            operation.destroy();
        }
        finally {
            logger.debug("END DESTROY: " + description);
        }
    }

    /**
     * @return The wrapped operation.
     */
    public EnqueueOperation getWrappedOperation()
    {
        return operation;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    public void setKey(OperationKey key)
    {
        opKey = key;
        operation.setKey(key);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    public OperationKey getKey()
    {
        return opKey;
    }

    /**
     * Return the alias for the given service
     *
     * @param data
     *        the input service data
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer data)
    {
        if (serviceAlias == null) {
            return data.getService();
        }
        return serviceAlias.getAlias(data.getService());
    }
}
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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * <code>OperationFactory</code> is the factory for the GVVCL implementation
 * objects.
 * <p>
 * Each time the <code>OperationFactory</code> is called, an object is created.
 * <p>
 *
 * In general GreenVulcano ESB will not invoke directly
 * <code>OperationFactory</code>, but it will invoke
 * <code>OperationManager</code> to maximize the performances.
 * <p>
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public final class OperationFactory
{

    /**
     * The GVLogger instance
     */
    private static final Logger logger          = GVLogger.getLogger(OperationFactory.class);

    /**
     * Defines the attribute to be used in order to obtains the operation type.
     */
    public static final String  TYPE_ATTRIBUTE  = "@type";

    /**
     * Defines the attribute to be used in order to obtains the operation class.
     */
    public static final String  CLASS_ATTRIBUTE = "@class";

    /**
     * Private Constructor
     */
    private OperationFactory()
    {
        // do nothing
    }

    /**
     * @param key
     *        the operation key
     * @return a new instance of the requested call operation.
     *
     * @throws CreateException
     *         if an error occurs during the operation instantiation.
     * @throws InitializationException
     *         if an error occurs during operation initialization.
     */
    public static CallOperation createCall(OperationKey key) throws CreateException, InitializationException
    {
        return (CallOperation) createOperation(key, CallOperation.TYPE);
    }

    /**
     * @param key
     *        the operation key
     * @return a new instance of the requested enqueue operation.
     *
     * @throws CreateException
     *         if an error occurs during the operation instantiation.
     * @throws InitializationException
     *         if an error occurs during operation initialization.
     */
    public static EnqueueOperation createEnqueue(OperationKey key) throws CreateException, InitializationException
    {
        return (EnqueueOperation) createOperation(key, EnqueueOperation.TYPE);
    }

    /**
     * @param key
     *        the operation key
     * @return a new instance of the requested dequeue operation.
     *
     * @throws CreateException
     *         if an error occurs during the operation instantiation.
     * @throws InitializationException
     *         if an error occurs during operation initialization.
     */
    public static DequeueOperation createDequeue(OperationKey key) throws CreateException, InitializationException
    {
        return (DequeueOperation) createOperation(key, DequeueOperation.TYPE);
    }

    /**
     * Creates an operation from an OperationKey, checks for the correct type,
     * Initializes the created operation.
     *
     * @param key
     *        the operation key
     * @param type
     *        the operation type
     * @return a new instance of the requested operation.
     *
     * @throws CreateException
     *         if an error occurs during the operation instantiation.
     * @throws InitializationException
     *         if an error occurs during operation initialization.
     */
    public static Operation createOperation(OperationKey key, String type) throws CreateException,
            InitializationException
    {
        logger.debug("BEGIN creating '" + type + "' operation: " + key);
        try {

            Node node = null;

            try {
                // Obtains the node containing the configuration for the call
                //
                node = key.getNode();
            }
            catch (GVException exc) {
                throw new CreateException("GVVCL_KEY_ERROR", new String[][]{{"key", key.toString()}}, exc);
            }

            // Check for the correct type
            //
            String typeFound = XMLConfig.get(node, TYPE_ATTRIBUTE);
            if (typeFound == null) {
                throw new CreateException("GVVCL_NO_TYPE_DEFINED_ERROR", new String[][]{{"key", key.toString()}});
            }
            if (!typeFound.equals(type)) {
                throw new CreateException("GVVCL_INVALID_TYPE_ERROR", new String[][]{{"typeFound", typeFound},
                        {"typeRequired", type}, {"key", key.toString()}});
            }

            // The class name is contained in the 'class' attribute
            //
            String className = XMLConfig.get(node, CLASS_ATTRIBUTE);
            logger.debug("Class name: " + className);

            // Obtains the Class
            //
            Class<?> cls = null;
            try {
                cls = Class.forName(className);
            }
            catch (ClassNotFoundException exc) {
                throw new CreateException("GVVCL_CLASS_NOT_FOUND_ERROR", new String[][]{{"className", className},
                        {"key", key.toString()}}, exc);
            }

            // Instantiate an Object that must be a Operation
            //
            Operation operation = null;
            try {
                Operation wrappedOperation = (Operation) cls.newInstance();
                operation = createOperationWrapper(wrappedOperation, type, key);
            }
            catch (Exception exc) {
                throw new CreateException("GVVCL_INSTANTIATION_ERROR", new String[][]{{"className", className},
                        {"exc", exc.toString()}, {"key", key.toString()}}, exc);
            }

            // Initialize the new created object from it's configuration node
            //
            operation.init(node);

            // Return the new created Operation
            //
            return operation;
        }
        catch (XMLConfigException xmle) {
            logger.error("ERROR creating '" + type + "' operation: " + key, xmle);
            throw new CreateException("GVVCL_XML_CONFIG_ERROR", new String[][]{{"exc", xmle.getMessage()},
                    {"key", key.toString()}}, xmle);
        }
        finally {
            logger.debug("END creating '" + type + "' operation: " + key);
        }
    }

    /**
     * Creates an appropriate operation wrapper around the given operation.
     *
     * @param wrappedOperation
     *        the operation to wrap
     * @param key
     *        the operation key
     * @param type
     *        the operation type
     * @return a new instance of the requested operation.
     *
     * @throws CreateException
     *         if an error occurs during the operation instantiation.
     */
    private static Operation createOperationWrapper(Operation wrappedOperation, String type, OperationKey key)
            throws CreateException
    {

        Operation retOperation = null;

        if (type.equals(CallOperation.TYPE)) {
            retOperation = new CallOperationWrapper((CallOperation) wrappedOperation, "type=" + type + ", key=" + key);
        }
        else if (type.equals(EnqueueOperation.TYPE)) {
            retOperation = new EnqueueOperationWrapper((EnqueueOperation) wrappedOperation, "type=" + type + ", key="
                    + key);
        }
        else if (type.equals(DequeueOperation.TYPE)) {
            retOperation = new DequeueOperationWrapper((DequeueOperation) wrappedOperation, "type=" + type + ", key="
                    + key);
        }
        else {
            throw new CreateException(
                    "GVVCL_INVALID_TYPE_ERROR",
                    new String[][]{
                            {"typeFound", type},
                            {"typeRequired",
                                    CallOperation.TYPE + " | " + EnqueueOperation.TYPE + " | " + DequeueOperation.TYPE},
                            {"key", key.toString()}});
        }

        retOperation.setKey(key);
        return retOperation;
    }
}

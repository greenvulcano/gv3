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
package it.greenvulcano.gvesb.virtual.db;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import it.greenvulcano.gvesb.j2ee.db.GVDBOperation;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * This class is the virtual communication layer plug-in working with a DataBase
 * executing a call operation. The connection can be specified in the
 * configuration file and can be a data source connection or a JDBC connection.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DBCallOperation implements CallOperation
{
    private static final Logger logger        = GVLogger.getLogger(DBCallOperation.class);

    /**
     * The DataBase operation
     */
    private GVDBOperation       gvDBOperation = null;

    private OperationKey        key           = null;

    /**
     * Empty constructor
     *
     * @throws DBCallException
     *         if an error occurred
     */
    public DBCallOperation() throws DBCallException
    {
        // do nothing
    }

    /**
     * The initialization method creates the connection object requested.
     *
     * @param node
     *        The configuration db-call node
     * @throws InitializationException
     *         if an error occurred
     */
    public void init(Node node) throws InitializationException
    {
        logger.debug("INIT DBCallOperation");

        gvDBOperation = new GVDBOperation();

        try {
            gvDBOperation.setLogger(DBCallOperation.logger);
            gvDBOperation.init(node);
        }
        catch (GVDBException exc) {
            logger.error("An error occurred initializing the DB Call Operation", exc);
            throw new InitializationException("GVVCL_XML_CONFIG_ERROR", new String[][]{{"exc", "" + exc}}, exc);
        }
    }

    /**
     * This method execute the Data Base call operation
     *
     * @param gvBuffer
     *        The GVBuffer object input
     * @return The GVBuffer object output
     * @throws DBCallException
     *         If an error occurred in the execution
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws DBCallException
    {
        try {
            return gvDBOperation.performSQL(gvBuffer);
        }
        catch (GVDBException exc) {
            logger.error(exc.getMessage());
        	throw new DBCallException("GVVCL_DB_ERROR", exc);
        }
    }

    /**
     *
     */
    public void cleanUp()
    {
        try {
            gvDBOperation.cleanUp();
        }
        catch (GVDBException exc) {
            logger.warn("Exception during cleanup", exc);
        }
    }

    /**
     *
     */
    public void destroy()
    {
        // do nothing
    }

    /**
     *
     * @param key
     *        The operation key
     */
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    public OperationKey getKey()
    {
        return key;
    }

    /**
     *
     */
    public String getServiceAlias(GVBuffer data)
    {
        return data.getService();
    }
}
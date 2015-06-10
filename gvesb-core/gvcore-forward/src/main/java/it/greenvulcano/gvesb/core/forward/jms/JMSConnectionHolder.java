/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.core.forward.jms;

import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.log.GVLogger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.XAConnectionFactory;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * @version 3.2.0 12/gen/2012
 * @author GreenVulcano Developer Team
 */
public class JMSConnectionHolder implements ExceptionListener
{
    private static final Logger logger     = GVLogger.getLogger(JMSConnectionHolder.class);

    private String              connectionFactory;
    private boolean             transacted = false;
    private Connection          connection = null;
    private String              key;
    private boolean             debug      = false;

    public JMSConnectionHolder(String connectionFactory, boolean transacted)
    {
        this.connectionFactory = connectionFactory;
        this.transacted = transacted;
        this.key = connectionFactory + "#" + transacted;
    }

    /**
     * Retrieve Connection for a given connectionFactory looking in the
     * given context.
     * 
     * @param initialContext
     *        the JNDIHelper instance to be used for lookups
     * @return the JMS Connection instance
     * @exception Exception
     *            if error occurs
     */
    public Connection getConnection(JNDIHelper initialContext) throws Exception
    {
        /*if (debug) {
            logger.debug("BEGIN getConnection: factory: " + key);
        }*/
        try {
            if (connection == null) {
                synchronized (this) {
                    if (connection == null) {
                        connection = createConnection(initialContext, connectionFactory, transacted, debug);
                        connection.setExceptionListener(this);
                    }
                }
            }

            return connection;
        }
        catch (Exception exc) {
            logger.error("EXCEPTION on getConnection: factory: " + key, exc);
            throw exc;
        }
        finally {
            /*if (debug) {
                logger.debug("END getConnection: factory: " + key);
            }*/
        }
    }

    @Override
    public void onException(JMSException exc)
    {
        logger.warn("Error on JMS Connection: " + key + " - Closing...", exc);

        closeAll();
    }

    public void destroy()
    {
        closeAll();
    }

    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    private synchronized void closeAll()
    {
        logger.debug("Closing Connection - factory: " + key
                + ((connection != null) ? " - connection: " + connection : ""));
        if (connection != null) {
            try {
                connection.close();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        connection = null;
    }

    /**
     * @param initialContext
     * @param connectionFactory
     * @return
     * @throws NamingException
     * @throws JMSException
     */
    private static Connection createConnection(JNDIHelper initialContext, String connectionFactory, boolean transacted,
            boolean debug) throws Exception
    {
        if (debug) {
            logger.debug("Creating Connection - factory: " + connectionFactory + "#" + transacted);
        }
        try {
            ConnectionFactory conFactory = (ConnectionFactory) initialContext.lookup(connectionFactory);
            Connection connection = null;

            if (transacted && (conFactory instanceof XAConnectionFactory)) {
                connection = ((XAConnectionFactory) conFactory).createXAConnection();
            }
            else {
                connection = conFactory.createConnection();
            }
            logger.debug("Created Connection - factory: " + connectionFactory + "#" + transacted + " - connection: "
                    + connection);
            return connection;
        }
        catch (Exception exc) {
            logger.error("Error creating Connection - factory: " + connectionFactory + "#" + transacted, exc);
            throw exc;
        }
    }

}

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
package it.greenvulcano.gvesb.j2ee.db.connections.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 *
 * @version 3.1.0 Gen 29, 2011
 * @author GreenVulcano Developer Team
 *
 */
public class DriverConnectionBuilder implements ConnectionBuilder
{
    private static Logger logger        = GVLogger.getLogger(DriverConnectionBuilder.class);

    private String        url           = null;
    private String        className     = null;
    private String        user          = null;
    private String        password      = null;
    private String        name          = null;
    private boolean       debugJDBCConn = false;

    @Override
    public void init(Node node) throws GVDBException
    {
        try {
            this.name = XMLConfig.get(node, "@name");
            this.className = XMLConfig.get(node, "@driver-class");
            this.user = PropertiesHandler.expand(XMLConfig.get(node, "@user", null));
            this.password = PropertiesHandler.expand(XMLConfig.getDecrypted(node, "@password", null));
            this.url = PropertiesHandler.expand(XMLConfig.get(node, "@url"));

            Class.forName(this.className);
        }
        catch (Exception exc) {
            throw new GVDBException("DriverConnectionBuilder - Initialization error", exc);
        }
        try {
        	this.debugJDBCConn = Boolean.getBoolean("it.greenvulcano.gvesb.j2ee.db.connections.impl.ConnectionBuilder.debugJDBCConn");
        }
        catch (Exception exc) {
        	this.debugJDBCConn = false;
        }
        logger.debug("Crated DriverConnectionBuilder(" + this.name + "). className: " + this.className + " - user: " + this.user
                + " - password: ********* - url: " + this.url);
    }

    @Override
    public Connection getConnection() throws GVDBException
    {
        try {
            Connection conn = null;
            if (!this.user.equals("")) {
                conn = DriverManager.getConnection(this.url, this.user, this.password);
            }
            else {
                conn = DriverManager.getConnection(this.url);
            }
            if (this.debugJDBCConn && (conn != null)) {
                logger.debug("Created JDBC Connection [" + this.name + "]: [" + conn + "/" + conn.hashCode() + "]");
            }
            return conn;
        }
        catch (Exception exc) {
            throw new GVDBException("DriverConnectionBuilder - Error while creating Connection[" + this.name + "]", exc);
        }
    }

    @Override
    public void releaseConnection(Connection conn) throws GVDBException
    {
        if (conn != null) {
            if (this.debugJDBCConn) {
                logger.debug("Closed JDBC Connection [" + this.name + "]: [" + conn + "/" + conn.hashCode() + "]");
            }
            try {
                conn.close();
            }
            catch (Exception exc) {
                logger.error("DriverConnectionBuilder - Error while closing Connection[" + this.name + "]: [" + conn + "/" + conn.hashCode() + "]",
                        exc);
            }
        }
    }

    @Override
    public void destroy()
    {
        logger.debug("Destroyed DriverConnectionBuilder(" + name + ")");
    }
}

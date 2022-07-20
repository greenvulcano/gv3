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

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;

/**
 *
 * @version 3.1.0 Gen 29, 2011
 * @author GreenVulcano Developer Team
 *
 */
public class DriverPoolConnectionBuilder implements ConnectionBuilder
{
    private static Logger     logger          = GVLogger.getLogger(DriverPoolConnectionBuilder.class);

    private String            url             = null;
    private String            className       = null;
    private String            user            = null;
    private String            password        = null;
    private String            name            = null;
    private String            validationQuery = null;
    private PoolingDataSource<PoolableConnection> dataSource      = null;
    private GenericObjectPool<PoolableConnection> connectionPool  = null;
    private boolean           debugJDBCConn   = false;
    private boolean           isFirst         = true;

    public void init(Node node) throws GVDBException
    {
        try {
            name = XMLConfig.get(node, "@name");
            className = PropertiesHandler.expand(XMLConfig.get(node, "@driver-class"));
            user = PropertiesHandler.expand(XMLConfig.get(node, "@user", null));
            password = PropertiesHandler.expand(XMLConfig.getDecrypted(node, "@password", null));
            url = PropertiesHandler.expand(XMLConfig.get(node, "@url"));
            try {
                debugJDBCConn = Boolean.getBoolean("it.greenvulcano.gvesb.j2ee.db.connections.impl.ConnectionBuilder.debugJDBCConn");
            }
            catch (Exception exc) {
                debugJDBCConn = false;
            }
            Class.forName(className);

            ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, user, password);
            PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);

            Node poolNode = XMLConfig.getNode(node, "PoolParameters");
            if (XMLConfig.exists(poolNode, "validationQuery")) {
                validationQuery = XMLConfig.get(poolNode, "validationQuery");
            }
            poolableConnectionFactory.setValidationQuery(validationQuery);

            connectionPool = new GenericObjectPool<PoolableConnection>(poolableConnectionFactory);
            poolableConnectionFactory.setPool(connectionPool);
            connectionPool.setBlockWhenExhausted(true);
            connectionPool.setMaxWaitMillis(XMLConfig.getLong(poolNode, "@maxWait", 30) * 1000);

            connectionPool.setMinIdle(XMLConfig.getInteger(poolNode, "@minIdle", 5));
            connectionPool.setMaxIdle(XMLConfig.getInteger(poolNode, "@maxIdle", 10));
            connectionPool.setMaxTotal(XMLConfig.getInteger(poolNode, "@maxActive", 15));
            connectionPool.setTimeBetweenEvictionRunsMillis(XMLConfig.getLong(poolNode,
                    "@timeBetweenEvictionRuns", 300) * 1000);
            connectionPool.setMinEvictableIdleTimeMillis(XMLConfig.getLong(poolNode, "@minEvictableIdleTime",
                    300) * 1000);
            connectionPool.setNumTestsPerEvictionRun(XMLConfig.getInteger(poolNode, "@numTestsPerEvictionRun", 3));
            connectionPool.setTestOnCreate(true);
            connectionPool.setTestOnBorrow(true);
            connectionPool.setTestOnReturn(true);
            
            dataSource = new PoolingDataSource<PoolableConnection>(connectionPool);
        }
        catch (Exception exc) {
            throw new GVDBException("DriverPoolConnectionBuilder - Initialization error", exc);
        }

        logger.debug("Crated DriverPoolConnectionBuilder(" + name + "). className: " + className + " - user: " + user
                + " - password: ********* - url: " + url + " - Pool: [" + connectionPool.getMinIdle() + "/"
                + connectionPool.getMaxIdle() + "/" + connectionPool.getMaxTotal() + "]");
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Connection getConnection() throws GVDBException
    {
        try {
            Connection conn = dataSource.getConnection();
            if (debugJDBCConn) {
                logger.debug("Created JDBC Connection [" + name + "]: [" + conn + "]");
                if (isFirst) {
                    isFirst = false;
                    DatabaseMetaData dbmd = conn.getMetaData();  
                    
                    logger.debug("=====  Database info =====");  
                    logger.debug("DatabaseProductName: " + dbmd.getDatabaseProductName() );  
                    logger.debug("DatabaseProductVersion: " + dbmd.getDatabaseProductVersion() );  
                    logger.debug("DatabaseMajorVersion: " + dbmd.getDatabaseMajorVersion() );  
                    logger.debug("DatabaseMinorVersion: " + dbmd.getDatabaseMinorVersion() );  
                    logger.debug("=====  Driver info =====");  
                    logger.debug("DriverName: " + dbmd.getDriverName() );  
                    logger.debug("DriverVersion: " + dbmd.getDriverVersion() );  
                    logger.debug("DriverMajorVersion: " + dbmd.getDriverMajorVersion() );  
                    logger.debug("DriverMinorVersion: " + dbmd.getDriverMinorVersion() );  
                    logger.debug("=====  JDBC/DB attributes =====");  
                    if (dbmd.supportsGetGeneratedKeys() )  
                        logger.debug("Supports getGeneratedKeys(): true");  
                    else  
                        logger.debug("Supports getGeneratedKeys(): false");  
                }
            }

            return conn;
        }
        catch (Exception exc) {
            throw new GVDBException("DriverPoolConnectionBuilder - Error while creating Connection[" + name + "]", exc);
        }
    }

    public void releaseConnection(Connection conn) throws GVDBException
    {
        if (debugJDBCConn) {
            logger.debug("Closed JDBC Connection [" + name + "]: [" + conn + "]");
        }
        if (conn != null) {
            try {
                conn.close();
            }
            catch (Exception exc) {
                logger.error("DriverPoolConnectionBuilder - Error while closing Connection[" + name + "]: [" + conn
                        + "]", exc);
            }
        }
    }

    public void destroy()
    {
        if (connectionPool != null) {
            try {
                connectionPool.close();
                connectionPool.clear();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        logger.debug("Destroyed DriverPoolConnectionBuilder(" + name + ")");
    }

    @Override
    public String statInfo() {
        if (connectionPool != null) {
            StringBuffer buf = new StringBuffer();
            buf.append("Min Idle: ").append(connectionPool.getMinIdle()).append("\n");
            buf.append("Max Idle: ").append(connectionPool.getMaxIdle()).append("\n");
            buf.append("Max Total: ").append(connectionPool.getMaxTotal()).append("\n");
            buf.append("Active: ").append(connectionPool.getNumActive()).append("\n");
            buf.append("Idle: ").append(connectionPool.getNumIdle());
            return buf.toString();
        }
        return "";
    }

    @Override
    public JSONObject toJSON() {
        JSONObject cbJ = new JSONObject();
        cbJ.put("name", getName());
        cbJ.put("minIdle", connectionPool.getMinIdle());
        cbJ.put("maxIdle", connectionPool.getMaxIdle());
        cbJ.put("maxTotal", connectionPool.getMaxTotal());
        cbJ.put("active", connectionPool.getNumActive());
        cbJ.put("idle", connectionPool.getNumIdle());
        return cbJ;
    }
}

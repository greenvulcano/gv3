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
import java.util.Properties;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import it.greenvulcano.log.GVLogger;

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
    private Properties        props           = null;
    private PoolingDataSource dataSource      = null;
    private GenericObjectPool connectionPool  = null;
    private boolean           debugJDBCConn   = false;

    @Override
	public void init(Node node) throws GVDBException
    {
        try {
            this.name = XMLConfig.get(node, "@name");
            this.className = XMLConfig.get(node, "@driver-class");
            this.user = XMLConfig.get(node, "@user", null);
            this.password = XMLConfig.getDecrypted(node, "@password", null);
            this.url = XMLConfig.get(node, "@url");
            try {
                this.debugJDBCConn = Boolean.getBoolean("it.greenvulcano.gvesb.j2ee.db.connections.impl.ConnectionBuilder.debugJDBCConn");
            }
            catch (Exception exc) {
                this.debugJDBCConn = false;
            }
            Class.forName(this.className);

            Node poolNode = XMLConfig.getNode(node, "PoolParameters");
            this.connectionPool = new GenericObjectPool(null);
            this.connectionPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
            this.connectionPool.setMaxWait(XMLConfig.getLong(poolNode, "@maxWait", 30) * 1000);

            this.connectionPool.setMinIdle(XMLConfig.getInteger(poolNode, "@minIdle", 5));
            this.connectionPool.setMaxIdle(XMLConfig.getInteger(poolNode, "@maxIdle", 10));
            this.connectionPool.setMaxActive(XMLConfig.getInteger(poolNode, "@maxActive", 15));
            this.connectionPool.setTimeBetweenEvictionRunsMillis(XMLConfig.getLong(poolNode,
                    "@timeBetweenEvictionRuns", 300) * 1000);
            this.connectionPool.setMinEvictableIdleTimeMillis(XMLConfig.getLong(poolNode, "@minEvictableIdleTime",
                    300) * 1000);
            this.connectionPool.setNumTestsPerEvictionRun(XMLConfig.getInteger(poolNode, "@numTestsPerEvictionRun", 3));
            if (XMLConfig.exists(poolNode, "validationQuery")) {
                this.validationQuery = XMLConfig.get(poolNode, "validationQuery");
                this.connectionPool.setTestOnBorrow(true);
                this.connectionPool.setTestOnReturn(true);
            }
            NodeList nl = XMLConfig.getNodeList(node, "ConnectionProperties/PropertyDef");
            if (nl.getLength() > 0) {
            	this.props = new Properties();
            	if (this.user != null) {
            		this.props.put("user", this.user);
            	}
            	if (this.password != null) {
            		this.props.put("password", this.password);
            	}
            	for (int i = 0; i < nl.getLength(); i++) {
					this.props.put(XMLConfig.get(nl.item(i), "@name"), XMLConfig.get(nl.item(i), "@value"));
				}
            }
        }
        catch (Exception exc) {
            throw new GVDBException("DriverPoolConnectionBuilder - Initialization error", exc);
        }

        ConnectionFactory connectionFactory = null;
        if (this.props != null) {
        	connectionFactory = new DriverManagerConnectionFactory(this.url, this.props);
        }
        else {
        	connectionFactory = new DriverManagerConnectionFactory(this.url, this.user, this.password);
        }
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,
                this.connectionPool, null, this.validationQuery, false, true);
        this.dataSource = new PoolingDataSource(this.connectionPool);

        if (this.props != null) {
        	Properties ps = new Properties();
        	ps.putAll(this.props);
        	ps .remove("password");
        	logger.debug("Crated DriverPoolConnectionBuilder(" + this.name + "). className: " + this.className + " - properties: " + ps
                + " - url: " + this.url + " - Pool: [" + this.connectionPool.getMinIdle() + "/"
                + this.connectionPool.getMaxIdle() + "/" + this.connectionPool.getMaxActive() + "]");
        }
        else {
        	logger.debug("Crated DriverPoolConnectionBuilder(" + this.name + "). className: " + this.className + " - user: " + this.user
                + " - password: ********* - url: " + this.url + " - Pool: [" + this.connectionPool.getMinIdle() + "/"
                + this.connectionPool.getMaxIdle() + "/" + this.connectionPool.getMaxActive() + "]");
        }
    }

    @Override
	public Connection getConnection() throws GVDBException
    {
        try {
            Connection conn = this.dataSource.getConnection();
            if (this.debugJDBCConn && (conn != null)) {
                logger.debug("Created JDBC Connection [" + this.name + "]: [" + conn + "/" + conn.hashCode() + "] [" + this.connectionPool.getNumActive() + "/" + this.connectionPool.getNumIdle() + "]");
            }

            return conn;
        }
        catch (Exception exc) {
            throw new GVDBException("DriverPoolConnectionBuilder - Error while creating Connection[" + this.name + "]", exc);
        }
    }

    @Override
	public void releaseConnection(Connection conn) throws GVDBException
    {
        if (conn != null) {
	    	String msg = "";
	    	if (this.debugJDBCConn) {
	    		msg = "Closed JDBC Connection [" + this.name + "]: [" + conn + "/" + conn.hashCode() + "]";
	    	}
            try {
                conn.close();
            }
            catch (Exception exc) {
                logger.error("DriverPoolConnectionBuilder - Error while closing Connection[" + this.name + "]: [" + conn
                		 + "/" + conn.hashCode() + "]", exc);
            }
	        if (this.debugJDBCConn) {
	            logger.debug(msg + " [" + this.connectionPool.getNumActive() + "/" + this.connectionPool.getNumIdle() + "]");
	        }
        }
    }

    @Override
	public void destroy()
    {
        if (this.connectionPool != null) {
            try {
                this.connectionPool.close();
                this.connectionPool.clear();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        logger.debug("Destroyed DriverPoolConnectionBuilder(" + this.name + ")");
    }
}

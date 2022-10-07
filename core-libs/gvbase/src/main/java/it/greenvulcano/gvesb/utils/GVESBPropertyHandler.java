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
package it.greenvulcano.gvesb.utils;

import java.io.Reader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Scriptable;

import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.metadata.PropertyHandler;
import it.greenvulcano.util.thread.ThreadMap;
import it.greenvulcano.util.txt.DateUtils;

/**
 * @version 3.0.0 10/giu/2010
 * @author GreenVulcano Developer Team
 */
public class GVESBPropertyHandler implements PropertyHandler
{

    static {
        GVESBPropertyHandler handler = new GVESBPropertyHandler();
        PropertiesHandler.registerHandler("sql", handler);
        PropertiesHandler.registerHandler("sqllist", handler);
        PropertiesHandler.registerHandler("sqltable", handler);
    }

    private static final Logger logger = GVLogger.getLogger(GVESBPropertyHandler.class);

    /**
     * This method insert the correct values for the dynamic parameter found in
     * the input string. The property value can be a combination of:
     *
     * <pre>
     *  - sql{{[conn::]statement}}  : execute a select sql statement and return the value of
     *                                the first field of the first selected record.
     *                                The 'conn' parameter is the name of a connection provided by
     *                                it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder
     *                                If 'conn' isn't defined then 'extra' must be a java.sql.Connection instance.
     *  - sqllist{{[conn::[::sep]]statement}}
     *                              : execute a select sql statement and return the value of
     *                                the first field of all selected records as a 'sep' (default to comma) separated list.
     *                                The 'conn' parameter is the name of a connection provided by
     *                                it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder
     *                                If 'conn' isn't defined then 'extra' must be a java.sql.Connection instance.
     *  - sqltable{{[conn::]statement}}
     *                              : execute a select sql statement and return all values of returned cursor as an XML.
     *                                For instance, if the cursor has 3 values, the returned XML will have following fields:
     *                                <RowSet>
     *                                  <data>
     *                                    <row>
     *                                      <col>value1</col>
     *                                      <col>value2</col>
     *                                      <col>value3</col>
     *                                    </row>
     *                                    <row>
     *                                      <col>value4</col>
     *                                      <col>value5</col>
     *                                      <col>value6</col>
     *                                    </row>
     *                                  ..
     *                                    <row>
     *                                      <col>valuex</col>
     *                                      <col>valuey</col>
     *                                      <col>valuez</col>
     *                                    </row>
     *                                  </data>
     *                                </RowSet>
     *                                The 'conn' parameter is the name of a connection provided by
     *                                it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder
     *                                If 'conn' isn't defined then 'extra' must be a java.sql.Connection instance.
     * </pre>
     *
     * @param type
     * @param str
     * @param inProperties
     * @param object
     * @param scope
     * @param extra
     */
    @Override
    public String expand(String type, int boundary, String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra) throws PropertiesHandlerException
    {
        if (type.startsWith("sqllist")) {
            return expandSQLListProperties(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("sqltable")) {
            return expandSQLTableProperties(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("sql")) {
            return expandSQLProperties(str, inProperties, object, scope, extra, boundary);
        }
        return str;
    }

    @Override
    public void cleanupResources() {
    	if (PropertiesHandler.isResourceLocalStorage()) {
    		Map<String, Connection> jdbcConns = (Map<String, Connection>) ThreadMap.get("PH_JDBC_CONN");
    		if (jdbcConns != null) {
    			for (String cn : jdbcConns.keySet()) {
        			try {
						JDBCConnectionBuilder.releaseConnection(cn, jdbcConns.get(cn));
					} catch (GVDBException exc) {
						// do nothing
					}
				}
    			jdbcConns.clear();
    			ThreadMap.remove("PH_JDBC_CONN");
    		}
    	}
    }

    private String expandSQLProperties(String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlStatement = null;
        Connection conn = null;
        String connName = "";
        boolean intConn = false;
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, scope, extra);
            }
            int pIdx = str.indexOf("::");
            if (pIdx != -1) {
                connName = str.substring(0, pIdx);
                sqlStatement = str.substring(pIdx + 2);
                intConn = true;
            }
            else {
                sqlStatement = str;
            }
            if (intConn) {
            	if (PropertiesHandler.isResourceLocalStorage()) {
            		Map<String, Connection> jdbcConns = (Map<String, Connection>) ThreadMap.get("PH_JDBC_CONN");
            		if (jdbcConns == null) {
            			jdbcConns = new HashMap<String, Connection>();
            			ThreadMap.put("PH_JDBC_CONN", jdbcConns);
            			conn = JDBCConnectionBuilder.getConnection(connName);
            			jdbcConns.put(connName, conn);
            		}
            		else {
            			conn = jdbcConns.get(connName);
            			if (conn == null) {
                			conn = JDBCConnectionBuilder.getConnection(connName);
                			jdbcConns.put(connName, conn);
            			}
            		}
            	}
            	if (conn == null) {
            		conn = JDBCConnectionBuilder.getConnection(connName);
            	}
            }
            else if ((extra != null) && (extra instanceof Connection)) {
                conn = (Connection) extra;
            }
            else {
                throw new PropertiesHandlerException("Error handling 'sql' metadata '" + str
                        + "', Connection undefined.");
            }
            logger.debug("Executing SQL statement {" + sqlStatement + "} on connection [" + connName + "]");
            ps = conn.prepareStatement(sqlStatement);
            rs = ps.executeQuery();

            String paramValue = null;

            if (rs.next()) {
                ResultSetMetaData rsmeta = rs.getMetaData();
                switch (rsmeta.getColumnType(1)) {
	                case Types.DATE :
	                case Types.TIME :
	                case Types.TIMESTAMP : {
	                    Timestamp dateVal = rs.getTimestamp(1);
	                    if (dateVal != null) {
	                    	paramValue = DateUtils.dateToString(dateVal, "yyyyMMdd HH:mm:ss");
	                    }
	                }
                    	break;
	                case Types.CLOB :{
                        Clob clob = rs.getClob(1);
                        if (clob != null) {
    	                    Reader is = clob.getCharacterStream();
                            StringWriter strW = new StringWriter();

                            IOUtils.copy(is, strW);
                            is.close();
                            paramValue = strW.toString();
                        }
                    }
                        break;
                    default: {
                    	paramValue = rs.getString(1);
                    }
                }
            }

            return (paramValue != null) ? paramValue.trim() : "";
        }
        catch (Exception exc) {
            logger.warn("Error handling 'sql' metadata '" + sqlStatement + "'", exc);
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'sql' metadata '" + str + "'", exc);
            }
            return "sql" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (intConn && (conn != null) && !PropertiesHandler.isResourceLocalStorage()) {
                try {
                    JDBCConnectionBuilder.releaseConnection(connName, conn);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }

    private String expandSQLListProperties(String str, Map<String, Object> inProperties, Object object,
            Scriptable scope, Object extra, int boundary) throws PropertiesHandlerException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlStatement = null;
        Connection conn = null;
        String connName = "";
        String separator = ",";
        boolean intConn = false;
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, scope, extra);
            }
            int pIdx = str.indexOf("::");
            if (pIdx != -1) {
                connName = str.substring(0, pIdx);
                sqlStatement = str.substring(pIdx + 2);
                int pIdx2 = str.indexOf("::", pIdx + 2);
                if (pIdx2 != -1) {
                    separator = str.substring(pIdx + 2, pIdx2);
                    sqlStatement = str.substring(pIdx2 + 2);
                }
                intConn = true;
            }
            else {
                sqlStatement = str;
            }
            if (intConn) {
                if (PropertiesHandler.isResourceLocalStorage()) {
            		Map<String, Connection> jdbcConns = (Map<String, Connection>) ThreadMap.get("PH_JDBC_CONN");
            		if (jdbcConns == null) {
            			jdbcConns = new HashMap<String, Connection>();
            			ThreadMap.put("PH_JDBC_CONN", jdbcConns);
            			conn = JDBCConnectionBuilder.getConnection(connName);
            			jdbcConns.put(connName, conn);
            		}
            		else {
            			conn = jdbcConns.get(connName);
            			if (conn == null) {
                			conn = JDBCConnectionBuilder.getConnection(connName);
                			jdbcConns.put(connName, conn);
            			}
            		}
            	}
            	if (conn == null) {
            		conn = JDBCConnectionBuilder.getConnection(connName);
            	}
            }
            else if ((extra != null) && (extra instanceof Connection)) {
                conn = (Connection) extra;
            }
            else {
                throw new PropertiesHandlerException("Error handling 'sqllist' metadata '" + str
                        + "', Connection undefined.");
            }
            ps = conn.prepareStatement(sqlStatement);
            rs = ps.executeQuery();

            String paramValue = "";

            int type = rs.getMetaData().getColumnType(1);

            while (rs.next()) {
            	switch (type) {
	                case Types.DATE :
	                case Types.TIME :
	                case Types.TIMESTAMP : {
	                    Timestamp dateVal = rs.getTimestamp(1);
	                    if (dateVal != null) {
	                    	paramValue += separator + DateUtils.dateToString(dateVal, "yyyyMMdd HH:mm:ss");
	                    }
	                    else {
	                    	paramValue += separator + "null";
	                    }
	                }
	                	break;
	                case Types.CLOB :{
	                    Clob clob = rs.getClob(1);
	                    if (clob != null) {
		                    Reader is = clob.getCharacterStream();
	                        StringWriter strW = new StringWriter();

	                        IOUtils.copy(is, strW);
	                        is.close();
	                        paramValue += separator + strW.toString();
	                    }
	                    else {
	                    	paramValue += separator + "null";
	                    }
	                }
	                    break;
	                default: {
	                	paramValue += separator + rs.getString(1);
	                }
	            }
            }

            if (!paramValue.equals("")) {
                paramValue = paramValue.substring(separator.length());
            }

            return (paramValue != null) ? paramValue.trim() : "";
        }
        catch (Exception exc) {
            logger.warn("Error handling 'sqllist' metadata '" + sqlStatement + "'", exc);
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'sqllist' metadata '" + str + "'", exc);
            }
            return "sqllist" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (intConn && (conn != null) && !PropertiesHandler.isResourceLocalStorage()) {
                try {
                    JDBCConnectionBuilder.releaseConnection(connName, conn);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }

    private String expandSQLTableProperties(String str, Map<String, Object> inProperties, Object object,
            Scriptable scope, Object extra, int boundary) throws PropertiesHandlerException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlStatement = null;
        Connection conn = null;
        String connName = "";
        boolean intConn = false;
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, scope, extra);
            }
            int pIdx = str.indexOf("::");
            if (pIdx != -1) {
                connName = str.substring(0, pIdx);
                sqlStatement = str.substring(pIdx + 2);
                intConn = true;
            }
            else {
                sqlStatement = str;
            }
            if (intConn) {
            	if (PropertiesHandler.isResourceLocalStorage()) {
            		Map<String, Connection> jdbcConns = (Map<String, Connection>) ThreadMap.get("PH_JDBC_CONN");
            		if (jdbcConns == null) {
            			jdbcConns = new HashMap<String, Connection>();
            			ThreadMap.put("PH_JDBC_CONN", jdbcConns);
            			conn = JDBCConnectionBuilder.getConnection(connName);
            			jdbcConns.put(connName, conn);
            		}
            		else {
            			conn = jdbcConns.get(connName);
            			if (conn == null) {
                			conn = JDBCConnectionBuilder.getConnection(connName);
                			jdbcConns.put(connName, conn);
            			}
            		}
            	}
            	if (conn == null) {
            		conn = JDBCConnectionBuilder.getConnection(connName);
            	}
            }
            else if ((extra != null) && (extra instanceof Connection)) {
                conn = (Connection) extra;
            }
            else {
                throw new PropertiesHandlerException("Error handling 'sqltable' metadata '" + str
                        + "', Connection undefined.");
            }
            logger.debug("Esecuzione select: " + sqlStatement + ".");
            ps = conn.prepareStatement(sqlStatement);
            rs = ps.executeQuery();
            return ResultSetUtils.getResultSetAsXMLString(rs);
        }
        catch (Exception exc) {
            logger.warn("Error handling 'sqltable' metadata '" + sqlStatement + "'", exc);
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'sqltable' metadata '" + str + "'", exc);
            }
            return "sqltable" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (intConn && (conn != null) && !PropertiesHandler.isResourceLocalStorage()) {
                try {
                    JDBCConnectionBuilder.releaseConnection(connName, conn);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }
}

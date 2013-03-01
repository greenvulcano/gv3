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
package it.greenvulcano.log.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * 
 * @version 3.1.0 24/gen/2011
 * @author GreenVulcano Developer Team
 */
public class JDBCWriter
{

    // All columns of the log-table
    private List<Column> logCols      = null;

    // Only columns which will be provided by logging
    private String       columnList   = null;

    private boolean      isConfigured = false;
    private boolean      isReady      = false;

    private String       errorMsg     = "";

    /**
     * Stores a database url
     */
    private String       url          = null;

    /**
     * Stores the database user
     */
    private String       username     = null;

    /**
     * Stores the database password
     */
    private String       password     = null;

    private Connection   con          = null;

    private Statement    stmt         = null;

    private String       table        = null;

    private String       procedure    = null;

    // cached prepared statement
    private String       preparedSql  = null;

    /**
     * Sets a connection. Throws an exception, if the connection is not open !
     * 
     * @param con
     *        The new Connection value
     * @exception Exception
     *            Description of Exception
     */
    public void setConnection(Connection con) throws Exception
    {
        this.con = con;

        if (!isConnected()) {
            throw new Exception("JDBCWriter::setConnection(), Given connection isnt connected to database!");
        }
    }

    /**
     * Sets log columns.
     * 
     * @param columns
     *        The new log columns
     * @exception Exception
     *            Description of Exception
     */
    public void setLogColumns(List<Column> columns) throws Exception
    {
        if (!isConfigured) {
            throw new Exception("JDBCWriter::setLogType(), Not configured !");
        }

        this.logCols = columns;
    }

    /**
     * Configures this class, by reading in the structure of the log-table
     * Throws an exception, if an database-error occurs !
     * 
     * @param _table
     *        Description of Parameter
     * @exception Exception
     *            Description of Exception
     */
    public void setTable(String table) throws Exception
    {
        if (isConfigured) {
            return;
        }

        this.table = table;
        isConfigured = true;
    }

    /**
     * 
     * @param url
     * @param username
     * @param password
     */
    public void setConnectionParams(String url, String username, String password)
    {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Return true, if this class is configured, else false.
     * 
     * @return The Configured value
     */
    public boolean isConfigured()
    {
        return isConfigured;
    }

    /**
     * Return true, if this connection is open, else false.
     * 
     * @return The Connected value
     */
    public boolean isConnected()
    {
        try {
            return (con != null && !con.isClosed());
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Return the internal error message stored in instance variable msg.
     * 
     * @return The ErrorMsg value
     */
    public String getErrorMsg()
    {
        if (errorMsg == null)
            return "";
        String r = errorMsg;
        errorMsg = null;
        return r;
    }

    /**
     * Description of the Method
     * 
     * @exception Exception
     *            Description of Exception
     */
    public void freeConnection() throws Exception
    {
        if (stmt != null) {
            try {
                stmt.close();
            }
            catch (Exception exc) {
                // do nothing
            }
            stmt = null;
        }
        if (con != null) {
            try {
                con.close();
            }
            catch (Exception exc) {
                // do nothing
            }
            con = null;
        }
    }

    /**
     * prepare connection
     * 
     * @return Description of the Returned Value
     * @exception Exception
     *            Description of Exception
     */
    public boolean prepareConnection() throws Exception
    {
        con = DriverManager.getConnection(url, username, password);

        try {
            con.setAutoCommit(true);
        }
        catch (Exception exc) {
            // do nothing
        }

        if (!isConnected()) {
            throw new Exception("JDBCWriter::prepareConnection(), Given connection isnt connected to database!");
        }

        if (procedure != null) {
            // procedure call
            stmt = this.createCallableStatement(logCols.size());
        }
        else {
            // prepared statement
            stmt = this.createStatement();
        }

        return true;
    }

    /**
     * Writes a message into the database table. Throws an exception, if an
     * database error occurs !
     * 
     * @param event
     *        the LoggingEvent to log
     * @param layout
     *        layout to use for message
     * @exception Exception
     *            Description of Exception
     */
    public void append(LoggingEvent event, Layout layout) throws Exception
    {
        boolean errorOccurred = false;
        boolean useCallStmts = procedure != null;
        PreparedStatement prepStmt = null;
        CallableStatement callStmt = null;

        if (!isReady) {
            if (!isReady()) {
                System.out.println("JDBCWriter::append(), Not ready to append !");
                throw new Exception("JDBCWriter::append(), Not ready to append!");
            }
        }

        try {
            // prepareConnection();
            Column logcol;
            int paramIndex = 1;

            if (useCallStmts) {
                callStmt = (CallableStatement) stmt;
            }
            prepStmt = (PreparedStatement) stmt;

            int msgSize = 0;
            String msg = event.getRenderedMessage();
            if (msg != null) {
                msgSize = msg.length();
            }

            int num = logCols.size();
            for (int i = 0; i < num; i++) {
                logcol = logCols.get(i);
                int indexToUse = paramIndex;
                if (useCallStmts) {
                    indexToUse = i + 1;
                }

                if (logcol.logtype == ColumnType.MSG) {
                    if (msg == null) {
                        prepStmt.setNull(indexToUse, Types.CLOB);
                    }
                    else {
                        prepStmt.setString(indexToUse, msg);
                    }
                    paramIndex = paramIndex + 1;
                }
                else if (logcol.logtype == ColumnType.MSG_SIZE) {
                    prepStmt.setInt(indexToUse, msgSize);
                    paramIndex = paramIndex + 1;
                }
                else if (logcol.logtype == ColumnType.PRIO) {
                    String parameter = event.getLevel().toString();
                    prepStmt.setString(indexToUse, parameter);
                    paramIndex = paramIndex + 1;
                }
                else if (logcol.logtype == ColumnType.CAT) {
                    String parameter = event.getLoggerName();
                    if (parameter == null) {
                        prepStmt.setNull(indexToUse, Types.VARCHAR);
                    }
                    else {
                        prepStmt.setString(indexToUse, parameter);
                    }
                    paramIndex = paramIndex + 1;
                }
                else if (logcol.logtype == ColumnType.THREAD) {
                    String parameter = event.getThreadName();
                    if (parameter == null) {
                        prepStmt.setNull(indexToUse, Types.VARCHAR);
                    }
                    else {
                        prepStmt.setString(indexToUse, parameter);
                    }
                    paramIndex = paramIndex + 1;
                }
                else if (logcol.logtype == ColumnType.STATIC) {
                    Object parameter = logcol.value;
                    if (parameter == null) {
                        prepStmt.setNull(indexToUse, Types.VARCHAR);
                    }
                    else {
                        prepStmt.setObject(indexToUse, parameter);
                    }
                    paramIndex = paramIndex + 1;
                }
                else if (logcol.logtype == ColumnType.TIMESTAMP) {
                    Timestamp parameter = new Timestamp(event.timeStamp);
                    prepStmt.setTimestamp(indexToUse, parameter);
                    paramIndex = paramIndex + 1;
                }
                else if (logcol.logtype == ColumnType.THROWABLE) {
                    String parameter = getThrowable(event);
                    if (parameter == null) {
                        prepStmt.setNull(indexToUse, Types.CLOB);
                    }
                    else {
                        prepStmt.setString(indexToUse, parameter);
                    }
                    paramIndex = paramIndex + 1;
                }
                else if (logcol.logtype == ColumnType.NDC) {
                    String parameter = event.getNDC();
                    if (parameter == null) {
                        prepStmt.setNull(indexToUse, Types.VARCHAR);
                    }
                    else {
                        prepStmt.setString(indexToUse, parameter);
                    }
                    paramIndex = paramIndex + 1;
                }
                else if (logcol.logtype == ColumnType.MDC) {
                    Object mdcObject = event.getMDC(logcol.value.toString());
                    String parameter = mdcObject == null ? null : mdcObject.toString();
                    if (parameter == null) {
                        prepStmt.setNull(indexToUse, Types.VARCHAR);
                    }
                    else {
                        prepStmt.setObject(indexToUse, parameter);
                    }
                    paramIndex = paramIndex + 1;
                }
                else if (logcol.logtype == ColumnType.IPRIO) {
                    int parameter = event.getLevel().toInt();
                    prepStmt.setInt(indexToUse, parameter);
                    paramIndex = paramIndex + 1;
                }
                else if (logcol.logtype == ColumnType.SEQUENCE) {
                    // do nothing
                }
            }

            if (useCallStmts) {
                callStmt.execute();
            }
            else {
                prepStmt.executeUpdate();
                // prepStmt.addBatch();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            errorOccurred = true;
            throw (e);
        }
        finally {
            /*
             * try { if (stmt != null) { stmt.close(); } freeConnection(); }
             * catch (Exception exception) { if (errorOccurred) { // consume
             * exception } else { throw (exception); } }
             */
        }
    }

    /*
     * public void appendConfirm() throws Exception { if (!con.isClosed()) {
     * con.commit(); } }
     */

    /*
     * public void appendConfirm() throws Exception { boolean usePrepStmts =
     * this.isUsePreparedStatements(); boolean useCallStmts = procedure != null;
     * PreparedStatement prepStmt = null;
     * 
     * try { if (usePrepStmts && !useCallStmts) { prepStmt = (PreparedStatement)
     * stmt; prepStmt.executeBatch(); } } }
     */


    /**
     * Return true, if this class is ready to append(), else false. When not
     * ready, a reason-String is stored in the instance-variable msg.
     * 
     * @return Description of the Returned Value
     */
    public boolean isReady()
    {
        if (isReady) {
            return true;
        }

        if (!isConfigured) {
            errorMsg = "Not ready to append, because not configured!";
            return false;
        }

        boolean msgcolDefined = false;

        Column logcol;

        for (int i = 0; i < logCols.size(); i++) {
            logcol = logCols.get(i);

            if (logcol.logtype == ColumnType.STATIC && logcol.value == null) {
                errorMsg = "Not ready to append! Column " + logcol.name
                        + " is specified as a static field, and a value has to be set!";
                return false;
            }
            else if (logcol.logtype == ColumnType.MDC && logcol.value == null) {
                errorMsg = "Not ready to append! Column " + logcol.name
                        + " is specified as a MDC field, and a key has to be set!";
                return false;
            }
            else if (logcol.logtype == ColumnType.MSG) {
                msgcolDefined = true;
            }
        }

        // create the column_list
        for (int i = 0; i < logCols.size(); i++) {
            logcol = logCols.get(i);

            if (logcol.logtype != ColumnType.EMPTY) {
                if (columnList == null) {
                    columnList = logcol.name;
                }
                else {
                    columnList += ", " + logcol.name;
                }
            }
        }

        isReady = true;

        return true;
    }


    private Statement createStatement() throws Exception
    {
        Statement retVal = null;

        // create sql statement
        if (this.preparedSql == null) {
            String sql = "insert into " + table + " (" + columnList + ") values (";

            for (int i = 0; i < logCols.size(); i++) {
                Column logcol = logCols.get(i);

                // only required columns
                if (!logcol.ignore) {
                    if (sql.endsWith(" ")) {
                        sql += ", ";
                    }

                    if (logcol.logtype == ColumnType.SEQUENCE) {
                        sql += logcol.value.toString() + " ";
                    }
                    else {
                        sql += "? ";
                    }
                }
            }
            sql += ")";
            LogLog.debug("prepared statement: " + sql);

            this.preparedSql = sql;
        }
        retVal = con.prepareStatement(this.preparedSql);

        return retVal;
    }

    private CallableStatement createCallableStatement(int numberOfParameters) throws Exception
    {
        CallableStatement cStmt = null;

        String callString = "{call " + procedure + "( ";
        for (int i = 0; i < numberOfParameters - 1; i++) {
            callString = callString + "?, ";
        }
        if (numberOfParameters > 0) {
            callString = callString + "? ";
        }
        callString = callString + " )}";
        cStmt = con.prepareCall(callString);

        return cStmt;
    }

    /**
     * Extracts Stack trace of Throwable contained in LogginEvent, if there is
     * any
     * 
     * @param aLoggingEvent
     *        logging event
     * @return stack trace of throwable
     */
    public String getThrowable(LoggingEvent event)
    {
        // extract throwable information from loggingEvent if available
        ThrowableInformation throwableinfo = event.getThrowableInformation();
        StringBuffer tsb = new StringBuffer();

        if (throwableinfo != null) {
            String[] lines = throwableinfo.getThrowableStrRep();
            for (int index = 0; index < lines.length; index++) {
                tsb.append(lines[index]).append("\r\n");
            }
        }
        return tsb.toString();
    }

    /**
     * @return Returns the procedure.
     */
    public String getProcedure()
    {
        return procedure;
    }

    /**
     * @param procedure
     *        The procedure to set.
     * @param columns
     *        columns
     */
    public void setProcedure(String procedure, List<Column> columns) throws Exception
    {
        if (isConfigured) {
            return;
        }

        /*
         * if (poolConnectionHandler != null) { con =
         * poolConnectionHandler.getConnection();
         * 
         * if (!isConnected()) { throw new Exception(
         * "JDBCWriter::setProcedure(), Given connection isnt connected to database !"
         * ); } }
         * 
         * this.procedure = procedure; // prepare call CallableStatement cStmt =
         * this.createCallableStatement(columns.size());
         * 
         * JDBCLogColumn logcol;
         * 
         * ParameterMetaData pmd;
         * 
         * // 2.6.2005 jschmied // ParameterMetaData is supported on different
         * levels by Oracle try { // J2SDK 1.4+; limited support by Oracle
         * drivers 10.x and 9.x pmd = cStmt.getParameterMetaData(); num =
         * pmd.getParameterCount(); if (num >= 1) { // oracle 10.1.0.4 has some
         * stubs in ParameterMetaData, // try if a function throws a
         * UnsupportedFeature exception pmd.getParameterType(1);
         * pmd.getParameterTypeName(1); pmd.isNullable(1); } } catch (Exception
         * e) { pmd = null; num = columns.size(); }
         * 
         * logcols = new ArrayList(num);
         * 
         * for (int i = 1; i <= num; i++) { logcol = new JDBCLogColumn();
         * JDBCColumnStorage col = (JDBCColumnStorage) columns.get(i - 1);
         * logcol.name = col.column.toUpperCase(); if (pmd == null) {
         * logcol.type = col.type; logcol.sqlType = col.sqlType; logcol.nullable
         * = true; // assume true } else { logcol.type =
         * pmd.getParameterTypeName(i); logcol.sqlType =
         * pmd.getParameterType(i); logcol.nullable = (pmd.isNullable(i) ==
         * ParameterMetaData.parameterNullable); } logcol.isWritable = true;
         * logcols.add(logcol); }
         * 
         * cStmt.close(); freeConnection();
         */
        isConfigured = true;

    }

    public JDBCWriter getCopy() {
        JDBCWriter clone = new JDBCWriter();
        
        clone.logCols = new ArrayList<Column>(this.logCols);
        clone.columnList = this.columnList;
        clone.isConfigured = this.isConfigured;
        clone.isReady      = this.isReady;
        clone.errorMsg     = this.errorMsg;
        clone.url          = this.url;
        clone.username     = this.username;
        clone.password     = this.password;
        clone.table        = this.table;
        clone.procedure    = this.procedure;
        clone.preparedSql  = this.preparedSql;
        
        return clone;
    }
}

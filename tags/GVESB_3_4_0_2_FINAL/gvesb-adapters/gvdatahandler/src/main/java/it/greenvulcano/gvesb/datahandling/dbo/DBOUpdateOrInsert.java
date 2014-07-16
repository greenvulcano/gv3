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
package it.greenvulcano.gvesb.datahandling.dbo;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.datahandling.DBOException;
import it.greenvulcano.gvesb.datahandling.utils.DiscardCause;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleError;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * IDBO Class specialized to parse the input RowSet document and in
 * updating or inserting data to DB.
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 */
public class DBOUpdateOrInsert extends AbstractDBO
{

    StatementInfo                     sqlStatementInfoUpdate;

    private final Map<String, String> statements_update;

    /**
     * Statement parameter values, reported when error occurs.
     */
    protected Vector<Object>          localCurrentRowFields = null;
    /**
     *
     */
    protected Vector<Object>          currentInsertRowFields;
    /**
     *
     */
    protected Vector<Object>          currentUpdateRowFields;

    private static final Logger       logger                = GVLogger.getLogger(DBOUpdateOrInsert.class);

    /**
     *
     */
    public DBOUpdateOrInsert()
    {
        super();
        statements_update = new HashMap<String, String>();
        currentInsertRowFields = new Vector<Object>(10);
        currentUpdateRowFields = new Vector<Object>(10);
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node config) throws DBOException
    {
        super.init(config);
        try {
            forcedMode = XMLConfig.get(config, "@force-mode", MODE_XML2DB);
            NodeList stmts = XMLConfig.getNodeList(config, "statement[@type='insert']");
            String id;
            Node stmt;
            for (int i = 0; i < stmts.getLength(); i++) {
                stmt = stmts.item(i);
                id = XMLConfig.get(stmt, "@id");
                if (id == null) {
                    statements.put(Integer.toString(i), XMLConfig.getNodeValue(stmt));
                }
                else {
                    statements.put(id, XMLConfig.getNodeValue(stmt));
                }
            }
            stmts = XMLConfig.getNodeList(config, "statement[@type='update']");
            for (int i = 0; i < stmts.getLength(); i++) {
                stmt = stmts.item(i);
                id = XMLConfig.get(stmt, "@id");
                if (id == null) {
                    statements_update.put(Integer.toString(i), XMLConfig.getNodeValue(stmt));
                }
                else {
                    statements_update.put(id, XMLConfig.getNodeValue(stmt));
                }
            }

            if (statements.isEmpty() || statements_update.isEmpty()) {
                throw new DBOException("Empty/misconfigured statements list for [" + getName() + "/" + dboclass + "]");
            }
        }
        catch (DBOException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error reading configuration of [" + getName() + "/" + dboclass + "]", exc);
            throw new DBOException("Error reading configuration of [" + getName() + "/" + dboclass + "]", exc);
        }
    }

    /**
     * Unsupported method for this IDBO.
     *
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#execute(java.io.OutputStream,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public void execute(OutputStream data, Connection conn, Map<String, Object> props) throws DBOException,
            InterruptedException {
        prepare();
        throw new DBOException("Unsupported method - DBOUpdateOrInsert::execute(OutputStream, Connection, Map)");
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#cleanup()
     */
    @Override
    public void cleanup()
    {
        super.cleanup();
        localCurrentRowFields = null;
        currentInsertRowFields.clear();
        currentUpdateRowFields.clear();
        if (sqlStatementInfoUpdate != null) {
            try {
                sqlStatementInfoUpdate.close();
            }
            catch (Exception exc) {
                // do nothing
            }
            sqlStatementInfoUpdate = null;
        }
    }

    private int          colIdx    = 0;

    private int          colUpdIdx = 0;

    private String       currType;

    private String       currDateFormat;

    private String       currNumberFormat;

    private String       currGroupSeparator;

    private String       currDecSeparator;

    private StringBuffer textBuffer;

    private boolean      colDataExpecting;

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#getStatement(java.lang.String)
     */
    @Override
    protected void getStatement(String id) throws SAXException
    {
        if (id == null) {
            id = "0";
        }
        if ((sqlStatementInfo == null) || (sqlStatementInfoUpdate == null) || !getCurrentId().equals(id)) {
            try {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), getName(), logger);
                if (sqlStatementInfo != null) {
                    sqlStatementInfo.close();
                    sqlStatementInfo = null;
                }
                if (sqlStatementInfoUpdate != null) {
                    sqlStatementInfoUpdate.close();
                    sqlStatementInfoUpdate = null;
                }
                String expandedSQL = PropertiesHandler.expand(statements.get(id), getCurrentProps(), getInternalConn(),
                        null);
                Statement statement = getInternalConn().prepareStatement(expandedSQL);
                sqlStatementInfo = new StatementInfo(id, expandedSQL, statement);
                expandedSQL = PropertiesHandler.expand(statements_update.get(id), getCurrentProps(), getInternalConn(),
                        null);
                if (expandedSQL != null) {
                    statement = getInternalConn().prepareStatement(expandedSQL);
                    sqlStatementInfoUpdate = new StatementInfo(id, expandedSQL, statement);
                }
                setCurrentId(id);
            }
            catch (SQLException exc) {
                OracleError oerr = OracleExceptionHandler.handleSQLException(exc);
                oerr.printLoggerInfo();
                throw new SAXException(exc);
            }
            catch (Exception exc) {
                throw new SAXException(exc);
            }
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (ROW_NAME.equals(localName)) {
            currentRowFields.clear();
            currentInsertRowFields.clear();
            currentUpdateRowFields.clear();
            colDataExpecting = false;
            colIdx = 0;
            colUpdIdx = 0;
            String id = attributes.getValue(uri, ID_NAME);
            getStatement(id);
            currentXSLMessage = attributes.getValue(uri, XSL_MSG_NAME);
            currCriticalError = "true".equalsIgnoreCase(attributes.getValue(uri, CRITICAL_ERROR));
        }
        else if (COL_NAME.equals(localName) || COL_UPDATE_NAME.equals(localName)) {
            currType = attributes.getValue(uri, TYPE_NAME);
            if (TIMESTAMP_TYPE.equals(currType)) {
                currDateFormat = attributes.getValue(uri, FORMAT_NAME);
                if (currDateFormat == null) {
                    currDateFormat = DEFAULT_DATE_FORMAT;
                }
            }
            else if (FLOAT_TYPE.equals(currType) || DECIMAL_TYPE.equals(currType)) {
                currNumberFormat = attributes.getValue(uri, FORMAT_NAME);
                if (currNumberFormat == null) {
                    currNumberFormat = call_DEFAULT_NUMBER_FORMAT;
                }
                currGroupSeparator = attributes.getValue(uri, GRP_SEPARATOR_NAME);
                if (currGroupSeparator == null) {
                    currGroupSeparator = call_DEFAULT_GRP_SEPARATOR;
                }
                currDecSeparator = attributes.getValue(uri, DEC_SEPARATOR_NAME);
                if (currDecSeparator == null) {
                    currDecSeparator = call_DEFAULT_DEC_SEPARATOR;
                }
            }
            colDataExpecting = true;
            if (COL_NAME.equals(localName)) {
                colIdx++;
            }
            else {
                colUpdIdx++;
            }
            textBuffer = new StringBuffer();
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (colDataExpecting) {
            textBuffer.append(ch, start, length);
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (ROW_NAME.equals(localName)) {
            rowCounter++;
            int updated = -1;
            if (currCriticalError) {
                rowDisc++;
                // aggiunta DiscardCause al dhr...
                String msg = currentXSLMessage;

                dhr.addDiscardCause(new DiscardCause(rowCounter, msg));

                resultMessage.append("Data error on row ").append(rowCounter).append(": ").append(msg).append('\n');
                resultMessage.append("SQL Statement Informations:\n").append(sqlStatementInfo);
                resultMessage.append("Record parameters:\n").append(dumpCurrentRowFields());
                resultStatus = STATUS_PARTIAL;
                return;
            }
            PreparedStatement sqlStatement_update = (PreparedStatement) sqlStatementInfoUpdate.getStatement();
            if (sqlStatement_update != null) {
                localCurrentRowFields = currentUpdateRowFields;
                try {
                    updated = sqlStatement_update.executeUpdate();
                }
                catch (SQLException exc) {
                    rowDisc++;
                    if (isTransacted()) {
                        logger.error("Record update parameters:\n" + dumpCurrentRowFields());
                        logger.error("SQL Statement Informations:\n" + sqlStatementInfoUpdate);
                        resultStatus = STATUS_KO;
                        throw new SAXException(new DBOException("SQLException error on update for row " + rowCounter
                                + ": " + exc.getMessage(), exc));
                    }

                    OracleError oraerr = OracleExceptionHandler.handleSQLException(exc);
                    if (isBlockingError(oraerr.getErrorType())) {
                        resultStatus = STATUS_KO;
                        logger.error("Record update parameters:\n" + dumpCurrentRowFields());
                        logger.error("SQL Statement Informations:\n" + sqlStatementInfoUpdate);
                        logger.error("SQLException configured as blocking error for service '" + serviceName
                                + "' on row " + Long.toString(rowCounter) + ".", exc);
                        throw new SAXException(new DBOException(
                                "SQLException configured as blocking error class on row " + rowCounter + ": "
                                        + exc.getMessage(), exc));
                    }

                    resultMessage.append("SQLException error on update for row ").append(rowCounter).append(": ").append(
                            exc.getMessage()).append('\n');
                    resultMessage.append("SQL Statement Informations:\n").append(sqlStatementInfoUpdate);
                    resultMessage.append("Record update parameters:\n").append(dumpCurrentRowFields());
                    resultStatus = STATUS_PARTIAL;

                    String msg = "";
                    if (onlyXSLErrorMsg && (currentXSLMessage != null)) {
                        msg += currentXSLMessage;
                    }
                    else {
                        msg += exc + " - XSL Message: " + currentXSLMessage;
                    }
                    dhr.addDiscardCause(new DiscardCause(rowCounter, msg));
                }
            }
            if ((updated < 1) && (sqlStatementInfo != null)) {
                try {
                    int actualOk = 0;
                    localCurrentRowFields = currentInsertRowFields;
                    actualOk = ((PreparedStatement) sqlStatementInfo.getStatement()).executeUpdate();
                    rowInsOk += actualOk;
                }
                catch (SQLException exc) {
                    rowDisc++;
                    if (isTransacted()) {
                        logger.error("Record insert parameters:\n" + dumpCurrentRowFields());
                        logger.error("SQL Statement Informations:\n" + sqlStatementInfo);
                        resultStatus = STATUS_KO;
                        throw new SAXException(new DBOException("SQLException error on insert for row " + rowCounter
                                + ": " + exc.getMessage(), exc));
                    }
                    OracleError oraerr = OracleExceptionHandler.handleSQLException(exc);
                    if (isBlockingError(oraerr.getErrorType())) {
                        resultStatus = STATUS_KO;
                        logger.error("Record insert parameters:\n" + dumpCurrentRowFields());
                        logger.error("SQL Statement Informations:\n" + sqlStatementInfo);
                        logger.error("SQLException configured as blocking error for service '" + serviceName
                                + "' alla riga " + Long.toString(rowCounter) + ".", exc);
                        throw new SAXException(new DBOException(
                                "SQLException configured as blocking error class on row " + rowCounter + ": "
                                        + exc.getMessage(), exc));
                    }

                    resultMessage.append("SQLException error on insert for row ").append(rowCounter).append(": ").append(
                            exc.getMessage());
                    resultMessage.append("SQL Statement Informations:\n").append(sqlStatementInfo);
                    resultMessage.append("Record insert parameters:\n").append(dumpCurrentRowFields());
                    resultStatus = STATUS_PARTIAL;

                    String msg = "";
                    if (onlyXSLErrorMsg && (currentXSLMessage != null)) {
                        msg += currentXSLMessage;
                    }
                    else {
                        msg += exc + " - XSL Message: " + currentXSLMessage;
                    }
                    dhr.addDiscardCause(new DiscardCause(rowCounter, msg));

                }
            }
            else {
                rowUpdOk += updated;
            }
        }
        else if (COL_NAME.equals(localName) || COL_UPDATE_NAME.equals(localName)) {
            PreparedStatement stmt = null;
            int idx = 0;
            if (COL_NAME.equals(localName)) {
                stmt = (PreparedStatement) sqlStatementInfo.getStatement();
                idx = colIdx;
                localCurrentRowFields = currentInsertRowFields;
            }
            else {
                stmt = (PreparedStatement) sqlStatementInfoUpdate.getStatement();
                idx = colUpdIdx;
                localCurrentRowFields = currentUpdateRowFields;
            }
            try {
                colDataExpecting = false;
                String text = textBuffer.toString();
                if (TIMESTAMP_TYPE.equals(currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.TIMESTAMP);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        dateFormatter.applyPattern(currDateFormat);
                        Date formattedDate = dateFormatter.parse(text);
                        Timestamp ts = new Timestamp(formattedDate.getTime());
                        stmt.setTimestamp(idx, ts);
                        localCurrentRowFields.add(ts);
                    }
                }
                else if (NUMERIC_TYPE.equals(currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.NUMERIC);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        stmt.setInt(idx, Integer.parseInt(text));
                        localCurrentRowFields.add(Integer.valueOf(text));
                    }
                }
                else if (FLOAT_TYPE.equals(currType) || DECIMAL_TYPE.equals(currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.NUMERIC);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        DecimalFormatSymbols dfs = numberFormatter.getDecimalFormatSymbols();
                        dfs.setDecimalSeparator(currDecSeparator.charAt(0));
                        dfs.setGroupingSeparator(currGroupSeparator.charAt(0));
                        numberFormatter.setDecimalFormatSymbols(dfs);
                        numberFormatter.applyPattern(currNumberFormat);
                        boolean isBigDecimal = numberFormatter.isParseBigDecimal();
                        try {
                            numberFormatter.setParseBigDecimal(true);
                            BigDecimal formattedNumber = (BigDecimal) numberFormatter.parse(text);
                            stmt.setBigDecimal(idx, formattedNumber);
                            localCurrentRowFields.add(formattedNumber);
                        }
                        finally {
                            numberFormatter.setParseBigDecimal(isBigDecimal);
                        }
                    }
                }
                else if (LONG_STRING_TYPE.equals(currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.CLOB);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        byte[] data = text.getBytes();
                        ByteArrayInputStream bais = new ByteArrayInputStream(data);
                        stmt.setAsciiStream(idx, bais, data.length);
                        localCurrentRowFields.add(text);
                    }
                }
                else if (BASE64_TYPE.equals(currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.BLOB);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        byte[] data = text.getBytes();
                        data = Base64.decodeBase64(data);
                        ByteArrayInputStream bais = new ByteArrayInputStream(data);
                        stmt.setBinaryStream(idx, bais, data.length);
                        localCurrentRowFields.add(text);
                    }
                }
                else if (BINARY_TYPE.equals(currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.BLOB);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        byte[] data = text.getBytes();
                        ByteArrayInputStream bais = new ByteArrayInputStream(data);
                        stmt.setBinaryStream(idx, bais, data.length);
                        localCurrentRowFields.add(text);
                    }
                }
                else {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.VARCHAR);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        stmt.setString(idx, text);
                        localCurrentRowFields.add(text);
                    }
                }
            }
            catch (ParseException exc) {
                throw new SAXException(exc);
            }
            catch (SQLException exc) {
                OracleExceptionHandler.handleSQLException(exc);
                throw new SAXException(exc);
            }
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#dumpCurrentRowFields()
     */
    @Override
    protected String dumpCurrentRowFields()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < localCurrentRowFields.size(); i++) {
            sb.append("Field(").append(i + 1).append(") value: [").append(localCurrentRowFields.elementAt(i)).append(
                    "]\n");
        }
        sb.append("XSL Message: ").append(currentXSLMessage).append("\n\n");
        return sb.toString();
    }
}

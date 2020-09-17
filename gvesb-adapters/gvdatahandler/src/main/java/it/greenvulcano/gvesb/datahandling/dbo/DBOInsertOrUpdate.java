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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.datahandling.DBOException;
import it.greenvulcano.gvesb.datahandling.utils.DiscardCause;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleError;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;

/**
 * IDBO Class specialized to parse the input RowSet document and in
 * inserting or updating data to DB.
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 */
public class DBOInsertOrUpdate extends AbstractDBO
{
    private static final int          DEFAULT_DUPLICATE_INSERT_CODE = 1;

    StatementInfo                     sqlStatementInfoUpdate;

    private final Map<String, String> statements_update;

    /**
     * Statement parameter values, reported when error occurs.
     */
    protected Vector<Object>          localCurrentRowFields         = null;
    /**
     *
     */
    protected Vector<Object>          currentInsertRowFields;
    /**
     *
     */
    protected Vector<Object>          currentUpdateRowFields;

    private static final Logger       logger                        = GVLogger.getLogger(DBOInsertOrUpdate.class);

    /**
     *
     */
    public DBOInsertOrUpdate()
    {
        super();
        this.statements_update = new HashMap<String, String>();
        this.currentInsertRowFields = new Vector<Object>(10);
        this.currentUpdateRowFields = new Vector<Object>(10);
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node config) throws DBOException
    {
        super.init(config);
        try {
            this.forcedMode = XMLConfig.get(config, "@force-mode", MODE_XML2DB);
            this.duplicateInsertCode = XMLConfig.getInteger(config, "@duplicate-insert-code", DEFAULT_DUPLICATE_INSERT_CODE);
            NodeList stmts = XMLConfig.getNodeList(config, "statement[@type='insert']");
            String id;
            Node stmt;
            for (int i = 0; i < stmts.getLength(); i++) {
                stmt = stmts.item(i);
                id = XMLConfig.get(stmt, "@id");
                if (id == null) {
                    this.statements.put(Integer.toString(i), XMLConfig.getNodeValue(stmt));
                }
                else {
                    this.statements.put(id, XMLConfig.getNodeValue(stmt));
                }
            }
            stmts = XMLConfig.getNodeList(config, "statement[@type='update']");
            for (int i = 0; i < stmts.getLength(); i++) {
                stmt = stmts.item(i);
                id = XMLConfig.get(stmt, "@id");
                if (id == null) {
                    this.statements_update.put(Integer.toString(i), XMLConfig.getNodeValue(stmt));
                }
                else {
                    this.statements_update.put(id, XMLConfig.getNodeValue(stmt));
                }
            }

            if (this.statements.isEmpty() || this.statements_update.isEmpty()) {
                throw new DBOException("Empty/misconfigured statements list for [" + getName() + "/" + this.dboclass + "]");
            }
        }
        catch (DBOException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error reading configuration of [" + getName() + "/" + this.dboclass + "]", exc);
            throw new DBOException("Error reading configuration of [" + getName() + "/" + this.dboclass + "]", exc);
        }
    }

    /**
     * Unsupported method for this IDBO.
     *
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#executeOut(java.sql.Connection, java.util.Map)
     */
    @Override
    public Object executeOut(Connection conn, Map<String, Object> props) throws DBOException,
            InterruptedException {
        prepare();
        throw new DBOException("Unsupported method - DBOInsertOrUpdate::execute(Connection, HashMap)");
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#cleanup()
     */
    @Override
    public void cleanup()
    {
        super.cleanup();
        this.localCurrentRowFields = null;
        this.currentInsertRowFields.clear();
        this.currentUpdateRowFields.clear();
        if (this.sqlStatementInfoUpdate != null) {
            try {
                this.sqlStatementInfoUpdate.close();
            }
            catch (Exception exc) {
                // do nothing
            }
            this.sqlStatementInfoUpdate = null;
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

    private int          duplicateInsertCode;

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#getStatement(java.lang.String)
     */
    @Override
    protected void getStatement(String id) throws SAXException
    {
        if (id == null) {
            id = "0";
        }
        if ((this.sqlStatementInfo == null) || (this.sqlStatementInfoUpdate == null) || !getCurrentId().equals(id)) {
            try {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), getName(), logger);
                if (this.sqlStatementInfo != null) {
                    this.sqlStatementInfo.close();
                    this.sqlStatementInfo = null;
                }
                if (this.sqlStatementInfoUpdate != null) {
                    this.sqlStatementInfoUpdate.close();
                    this.sqlStatementInfoUpdate = null;
                }
                String expandedSQL = PropertiesHandler.expand(this.statements.get(id), getCurrentProps(), getInternalConn(),
                        null);
                Statement statement = getInternalConn().prepareStatement(expandedSQL);
                this.sqlStatementInfo = new StatementInfo(id, expandedSQL, statement);
                expandedSQL = PropertiesHandler.expand(this.statements_update.get(id), getCurrentProps(), getInternalConn(),
                        null);
                statement = getInternalConn().prepareStatement(expandedSQL);
                this.sqlStatementInfoUpdate = new StatementInfo(id, expandedSQL, statement);
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
            this.currentRowFields.clear();
            this.currentInsertRowFields.clear();
            this.currentUpdateRowFields.clear();
            this.colDataExpecting = false;
            this.colIdx = 0;
            this.colUpdIdx = 0;
            String id = attributes.getValue(uri, ID_NAME);
            getStatement(id);
            this.currentXSLMessage = attributes.getValue(uri, XSL_MSG_NAME);
            this.currCriticalError = "true".equalsIgnoreCase(attributes.getValue(uri, CRITICAL_ERROR));
        }
        else if (COL_NAME.equals(localName) || COL_UPDATE_NAME.equals(localName)) {
            this.currType = attributes.getValue(uri, TYPE_NAME);
            if (TIMESTAMP_TYPE.equals(this.currType)) {
                this.currDateFormat = attributes.getValue(uri, FORMAT_NAME);
                if (this.currDateFormat == null) {
                    this.currDateFormat = DEFAULT_DATE_FORMAT;
                }
            }
            else if (FLOAT_TYPE.equals(this.currType) || DECIMAL_TYPE.equals(this.currType)) {
                this.currNumberFormat = attributes.getValue(uri, FORMAT_NAME);
                if (this.currNumberFormat == null) {
                    this.currNumberFormat = this.call_DEFAULT_NUMBER_FORMAT;
                }
                this.currGroupSeparator = attributes.getValue(uri, GRP_SEPARATOR_NAME);
                if (this.currGroupSeparator == null) {
                    this.currGroupSeparator = this.call_DEFAULT_GRP_SEPARATOR;
                }
                this.currDecSeparator = attributes.getValue(uri, DEC_SEPARATOR_NAME);
                if (this.currDecSeparator == null) {
                    this.currDecSeparator = this.call_DEFAULT_DEC_SEPARATOR;
                }
            }
            this.colDataExpecting = true;
            if (COL_NAME.equals(localName)) {
                this.colIdx++;
            }
            else {
                this.colUpdIdx++;
            }
            this.textBuffer = new StringBuffer();
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (this.colDataExpecting) {
            this.textBuffer.append(ch, start, length);
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
            int actualOk = 0;
            try {
                this.rowCounter++;

                if (this.currCriticalError) {
                    this.rowDisc++;
                    // aggiunta DiscardCause al dhr...
                    String msg = this.currentXSLMessage;

                    this.dhr.addDiscardCause(new DiscardCause(this.rowCounter, msg));

                    this.resultMessage.append("Data error on row ").append(this.rowCounter).append(": ").append(msg);
                    this.resultMessage.append("SQL Statement Informations:\n" + this.sqlStatementInfo);
                    this.resultMessage.append("Record parameters:\n").append(dumpCurrentRowFields());
                    this.resultStatus = STATUS_PARTIAL;
                    return;
                }
                Statement sqlStatement = this.sqlStatementInfo.getStatement();
                if (sqlStatement != null) {
                    this.localCurrentRowFields = this.currentInsertRowFields;
                    actualOk = ((PreparedStatement) sqlStatement).executeUpdate();
                    this.rowInsOk += actualOk;
                }
            }
            catch (SQLException exc) {
                if (exc.getErrorCode() != this.duplicateInsertCode) {
                    logger.warn("Error code received for SQL insert: " + exc.getErrorCode());
                    this.rowDisc++;
                    if (isTransacted()) {
                        logger.error("Record insert parameters:\n" + dumpCurrentRowFields());
                        logger.error("SQL Statement Informations:\n" + this.sqlStatementInfo);
                        this.resultStatus = STATUS_KO;
                        throw new SAXException(new DBOException("SQLException error on insert for row " + this.rowCounter
                                + ": " + exc.getMessage(), exc));
                    }
                    OracleError oraerr = OracleExceptionHandler.handleSQLException(exc);
                    if (isBlockingError(oraerr.getErrorType())) {
                        this.resultStatus = STATUS_KO;
                        logger.error("Record insert parameters:\n" + dumpCurrentRowFields());
                        logger.error("SQL Statement Informations:\n" + this.sqlStatementInfo);
                        logger.error("SQLException configurata come non bloccante per il tipo '" + this.serviceName
                                + "' alla riga " + Long.toString(this.rowCounter) + ".", exc);
                        throw new SAXException(new DBOException(
                                "SQLException configured as blocking error class on row " + this.rowCounter + ": "
                                        + exc.getMessage(), exc));
                    }

                    this.resultMessage.append("SQLException error on insert for row ").append(this.rowCounter).append(": ").append(
                            exc.getMessage());
                    this.resultMessage.append("SQL Statement Informations:\n" + this.sqlStatementInfo);
                    this.resultMessage.append("Record insert parameters:\n").append(dumpCurrentRowFields());
                    this.resultStatus = STATUS_PARTIAL;

                    // aggiunto DiscardCause al dhr...
                    String msg = "";
                    if (this.onlyXSLErrorMsg && (this.currentXSLMessage != null)) {
                        msg += this.currentXSLMessage;
                    }
                    else {
                        msg += exc + " - XSL Message: " + this.currentXSLMessage;
                    }
                    this.dhr.addDiscardCause(new DiscardCause(this.rowCounter, msg));
                }
                else {
                    // duplicate insert...trying to update
                    PreparedStatement sqlStatement_update = (PreparedStatement) this.sqlStatementInfoUpdate.getStatement();
                    if (sqlStatement_update != null) {
                        this.localCurrentRowFields = this.currentUpdateRowFields;
                        try {
                            actualOk = sqlStatement_update.executeUpdate();
                            this.rowUpdOk += actualOk;
                        }
                        catch (SQLException excupd) {
                            this.rowDisc++;
                            if (isTransacted()) {
                                logger.error("Record update parameters:\n" + dumpCurrentRowFields());
                                logger.error("SQL Statement Informations:\n" + this.sqlStatementInfoUpdate);
                                this.resultStatus = STATUS_KO;
                                throw new SAXException(new DBOException("SQLException error on update for row "
                                        + this.rowCounter + ": " + excupd.getMessage(), excupd));
                            }

                            OracleError oraerr = OracleExceptionHandler.handleSQLException(excupd);
                            if (isBlockingError(oraerr.getErrorType())) {
                                this.resultStatus = STATUS_KO;
                                logger.error("Record update parameters:\n" + dumpCurrentRowFields());
                                logger.error("SQL Statement Informations:\n" + this.sqlStatementInfoUpdate);
                                logger.error("SQLException configured as blocking error class for service '"
                                        + this.serviceName + "' on row " + Long.toString(this.rowCounter) + ".", excupd);
                                throw new SAXException(new DBOException(
                                        "SQLException configured as blocking error class on row " + this.rowCounter + ": "
                                                + excupd.getMessage(), excupd));
                            }

                            this.resultMessage.append("SQLException error on update for row ").append(this.rowCounter).append(
                                    ": ").append(excupd.getMessage());
                            this.resultMessage.append("SQL Statement Informations:\n" + this.sqlStatementInfoUpdate);
                            this.resultMessage.append("Record update parameters:\n").append(dumpCurrentRowFields());
                            this.resultStatus = STATUS_PARTIAL;

                            // aggiunto DiscardCause a dhr....???????????????
                            String msg = "";
                            if (this.onlyXSLErrorMsg && (this.currentXSLMessage != null)) {
                                msg += this.currentXSLMessage;
                            }
                            else {
                                msg += excupd + " - XSL Message: " + this.currentXSLMessage;
                            }
                            this.dhr.addDiscardCause(new DiscardCause(this.rowCounter, msg));
                        }
                    }
                }
            }
        }
        else if (COL_NAME.equals(localName) || COL_UPDATE_NAME.equals(localName)) {
            PreparedStatement stmt = null;
            int idx = 0;
            if (COL_NAME.equals(localName)) {
                stmt = (PreparedStatement) this.sqlStatementInfo.getStatement();
                idx = this.colIdx;
                this.localCurrentRowFields = this.currentInsertRowFields;
            }
            else {
                stmt = (PreparedStatement) this.sqlStatementInfoUpdate.getStatement();
                idx = this.colUpdIdx;
                this.localCurrentRowFields = this.currentUpdateRowFields;
            }
            try {
                this.colDataExpecting = false;
                String text = this.textBuffer.toString();
                if (TIMESTAMP_TYPE.equals(this.currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.TIMESTAMP);
                        this.localCurrentRowFields.add(null);
                    }
                    else {
                        this.dateFormatter.applyPattern(this.currDateFormat);
                        Date formattedDate = this.dateFormatter.parse(text);
                        Timestamp ts = new Timestamp(formattedDate.getTime());
                        stmt.setTimestamp(idx, ts);
                        this.localCurrentRowFields.add(ts);
                    }
                }
                else if (NUMERIC_TYPE.equals(this.currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.NUMERIC);
                        this.localCurrentRowFields.add(null);
                    }
                    else {
                        stmt.setInt(idx, Integer.parseInt(text));
                        this.localCurrentRowFields.add(Integer.valueOf(text));
                    }
                }
                else if (FLOAT_TYPE.equals(this.currType) || DECIMAL_TYPE.equals(this.currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.NUMERIC);
                        this.localCurrentRowFields.add(null);
                    }
                    else {
                        DecimalFormatSymbols dfs = this.numberFormatter.getDecimalFormatSymbols();
                        dfs.setDecimalSeparator(this.currDecSeparator.charAt(0));
                        dfs.setGroupingSeparator(this.currGroupSeparator.charAt(0));
                        this.numberFormatter.setDecimalFormatSymbols(dfs);
                        this.numberFormatter.applyPattern(this.currNumberFormat);
                        boolean isBigDecimal = this.numberFormatter.isParseBigDecimal();
                        try {
                            this.numberFormatter.setParseBigDecimal(true);
                            BigDecimal formattedNumber = (BigDecimal) this.numberFormatter.parse(text);
                            stmt.setBigDecimal(idx, formattedNumber);
                            this.localCurrentRowFields.add(formattedNumber);
                        }
                        finally {
                            this.numberFormatter.setParseBigDecimal(isBigDecimal);
                        }
                    }
                }
                else if (LONG_STRING_TYPE.equals(this.currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.CLOB);
                        this.localCurrentRowFields.add(null);
                    }
                    else {
                    	stmt.setCharacterStream(this.colIdx, new StringReader(text));
                        this.localCurrentRowFields.add(text);
                    }
                }
                else if (LONG_NSTRING_TYPE.equals(this.currType)) {
                    if (text.equals("")) {
                    	stmt.setNull(this.colIdx, Types.NCLOB);
                        this.currentRowFields.add(null);
                    }
                    else {
                    	stmt.setCharacterStream(this.colIdx, new StringReader(text));
                        this.currentRowFields.add(text);
                    }
                }
                else if (BASE64_TYPE.equals(this.currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.BLOB);
                        this.localCurrentRowFields.add(null);
                    }
                    else {
                        byte[] data = text.getBytes();
                        data = Base64.decodeBase64(data);
                        ByteArrayInputStream bais = new ByteArrayInputStream(data);
                        stmt.setBinaryStream(idx, bais, data.length);
                        this.localCurrentRowFields.add(text);
                    }
                }
                else if (BINARY_TYPE.equals(this.currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.BLOB);
                        this.localCurrentRowFields.add(null);
                    }
                    else {
                        byte[] data = text.getBytes();
                        ByteArrayInputStream bais = new ByteArrayInputStream(data);
                        stmt.setBinaryStream(idx, bais, data.length);
                        this.localCurrentRowFields.add(text);
                    }
                }
                else if (NSTRING_TYPE.equals(this.currType)) {
                    if (text.equals("")) {
                    	stmt.setNull(this.colIdx, Types.NVARCHAR);
                        this.currentRowFields.add(null);
                    }
                    else {
                    	stmt.setNString(this.colIdx, text);
                        this.currentRowFields.add(text);
                    }
                }
                else {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.VARCHAR);
                        this.localCurrentRowFields.add(null);
                    }
                    else {
                        stmt.setString(idx, text);
                        this.localCurrentRowFields.add(text);
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
        for (int i = 0; i < this.localCurrentRowFields.size(); i++) {
            sb.append("Field(").append(i + 1).append(") value: [").append(this.localCurrentRowFields.elementAt(i)).append(
                    "]\n");
        }
        sb.append("XSL Message: ").append(this.currentXSLMessage).append("\n\n");
        return sb.toString();
    }
}

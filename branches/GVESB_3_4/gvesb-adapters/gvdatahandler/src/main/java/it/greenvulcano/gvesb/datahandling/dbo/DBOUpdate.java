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
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.datahandling.DBOException;
import it.greenvulcano.gvesb.datahandling.utils.DiscardCause;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.log.GVLogger;

/**
 * IDBO Class specialized to parse the input RowSet document and in
 * updating data to DB.
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 */
public class DBOUpdate extends AbstractDBO
{

    private static final Logger logger = GVLogger.getLogger(DBOUpdate.class);

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node config) throws DBOException
    {
        super.init(config);
        this.isInsert = false;
        try {
            this.forcedMode = XMLConfig.get(config, "@force-mode", MODE_XML2DB);
            NodeList stmts = XMLConfig.getNodeList(config, "statement[@type='update']");
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

            if (this.statements.isEmpty()) {
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
        throw new DBOException("Unsupported method - DBOUpdate::executeOut(Connection, Map)");
    }

    private int          colIdx = 0;

    private String       currType;

    private String       currDateFormat;

    private String       currNumberFormat;

    private String       currGroupSeparator;

    private String       currDecSeparator;

    private StringBuffer textBuffer;

    private boolean      colDataExpecting;

    /**
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (ROW_NAME.equals(localName)) {
            this.currentRowFields.clear();
            this.colDataExpecting = false;
            this.colIdx = 0;
            String id = attributes.getValue(uri, ID_NAME);
            getStatement(id);
            this.currentXSLMessage = attributes.getValue(uri, XSL_MSG_NAME);
            this.currCriticalError = "true".equalsIgnoreCase(attributes.getValue(uri, CRITICAL_ERROR));
            this.generatedKeyID = attributes.getValue(uri, "generate-key");
            this.resetGeneratedKeyID = attributes.getValue(uri, "reset-generate-key");
            this.readGeneratedKey = this.autogenerateKeys && (this.generatedKeyID != null);
        }
        else if (COL_NAME.equals(localName)) {
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
            this.colIdx++;
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
            if (!this.currCriticalError) {
                executeStatement();
            }
            else {
                this.rowDisc++;
                // aggiunta DiscardCause al dhr...
                String msg = this.currentXSLMessage;

                this.dhr.addDiscardCause(new DiscardCause(this.rowCounter, msg));

                this.resultMessage.append("Data error on row ").append(this.rowCounter).append(": ").append(msg);
                this.resultMessage.append("SQL Statement Informations:\n").append(this.sqlStatementInfo);
                this.resultMessage.append("Record parameters:\n").append(dumpCurrentRowFields());
                this.resultStatus = STATUS_PARTIAL;
            }
        }
        else if (COL_NAME.equals(localName)) {
            PreparedStatement ps = (PreparedStatement) this.sqlStatementInfo.getStatement();
            try {
                this.colDataExpecting = false;
                boolean autoKeySet = false;
                String text = this.textBuffer.toString();
                if (this.autogenerateKeys) {
                    if (text.startsWith(GENERATED_KEY_ID)) {
                        Object key = this.generatedKeys.get(text);
                        ps.setObject(this.colIdx, key);
                        this.currentRowFields.add(key);
                        autoKeySet = true;
                    }
                }
                if (!autoKeySet) {
                    if (TIMESTAMP_TYPE.equals(this.currType)) {
                        if (text.equals("")) {
                            ps.setNull(this.colIdx, Types.TIMESTAMP);
                            this.currentRowFields.add(null);
                        }
                        else {
                            this.dateFormatter.applyPattern(this.currDateFormat);
                            Date formattedDate = this.dateFormatter.parse(text);
                            Timestamp ts = new Timestamp(formattedDate.getTime());
                            ps.setTimestamp(this.colIdx, ts);
                            this.currentRowFields.add(ts);
                        }
                    }
                    else if (NUMERIC_TYPE.equals(this.currType)) {
                        if (text.equals("")) {
                            ps.setNull(this.colIdx, Types.NUMERIC);
                            this.currentRowFields.add(null);
                        }
                        else {
                            ps.setInt(this.colIdx, Integer.parseInt(text));
                            this.currentRowFields.add(Integer.valueOf(text));
                        }
                    }
                    else if (LONG_TYPE.equals(this.currType)) {
                        if (text.equals("")) {
                            ps.setNull(this.colIdx, Types.BIGINT);
                            this.currentRowFields.add(null);
                        }
                        else {
                            ps.setLong(this.colIdx, Long.parseLong(text));
                            this.currentRowFields.add(Long.valueOf(text));
                        }
                    }
                    else if (FLOAT_TYPE.equals(this.currType) || DECIMAL_TYPE.equals(this.currType)) {
                        if (text.equals("")) {
                            ps.setNull(this.colIdx, Types.NUMERIC);
                            this.currentRowFields.add(null);
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
                                ps.setBigDecimal(this.colIdx, formattedNumber);
                                this.currentRowFields.add(formattedNumber);
                            }
                            finally {
                                this.numberFormatter.setParseBigDecimal(isBigDecimal);
                            }
                        }
                    }
                    else if (LONG_STRING_TYPE.equals(this.currType)) {
                        if (text.equals("")) {
                            ps.setNull(this.colIdx, Types.CLOB);
                            this.currentRowFields.add(null);
                        }
                        else {
                            ps.setCharacterStream(this.colIdx, new StringReader(text));
                            this.currentRowFields.add(text);
                        }
                    }
                    else if (LONG_NSTRING_TYPE.equals(this.currType)) {
                        if (text.equals("")) {
                            ps.setNull(this.colIdx, Types.NCLOB);
                            this.currentRowFields.add(null);
                        }
                        else {
                            ps.setCharacterStream(this.colIdx, new StringReader(text));
                            this.currentRowFields.add(text);
                        }
                    }
                    else if (BASE64_TYPE.equals(this.currType)) {
                        if (text.equals("")) {
                            ps.setNull(this.colIdx, Types.BLOB);
                            this.currentRowFields.add(null);
                        }
                        else {
                            byte[] data = text.getBytes();
                            data = Base64.decodeBase64(data);
                            ByteArrayInputStream bais = new ByteArrayInputStream(data);
                            ps.setBinaryStream(this.colIdx, bais, data.length);
                            this.currentRowFields.add(text);
                        }
                    }
                    else if (BINARY_TYPE.equals(this.currType)) {
                        if (text.equals("")) {
                            ps.setNull(this.colIdx, Types.BLOB);
                            this.currentRowFields.add(null);
                        }
                        else {
                            byte[] data = text.getBytes();
                            ByteArrayInputStream bais = new ByteArrayInputStream(data);
                            ps.setBinaryStream(this.colIdx, bais, data.length);
                            this.currentRowFields.add(text);
                        }
                    }
                    else if (NSTRING_TYPE.equals(this.currType)) {
                        if (text.equals("")) {
                            ps.setNull(this.colIdx, Types.NVARCHAR);
                            this.currentRowFields.add(null);
                        }
                        else {
                            ps.setNString(this.colIdx, text);
                            this.currentRowFields.add(text);
                        }
                    }
                    else {
                        if (text.equals("")) {
                            ps.setNull(this.colIdx, Types.VARCHAR);
                            this.currentRowFields.add(null);
                        }
                        else {
                            ps.setString(this.colIdx, text);
                            this.currentRowFields.add(text);
                        }
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
}

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
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.log.GVLogger;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
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
        isInsert = false;
        try {
            forcedMode = XMLConfig.get(config, "@force-mode", MODE_XML2DB);
            NodeList stmts = XMLConfig.getNodeList(config, "statement[@type='update']");
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

            if (statements.isEmpty()) {
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
        throw new DBOException("Unsupported method - DBOUpdate::execute(OutputStream, Connection, Map)");
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
            currentRowFields.clear();
            colDataExpecting = false;
            colIdx = 0;
            String id = attributes.getValue(uri, ID_NAME);
            getStatement(id);
            currentXSLMessage = attributes.getValue(uri, XSL_MSG_NAME);
            currCriticalError = "true".equalsIgnoreCase(attributes.getValue(uri, CRITICAL_ERROR));
            generatedKeyID = attributes.getValue(uri, "generate-key");
            resetGeneratedKeyID = attributes.getValue(uri, "reset-generate-key");
            readGeneratedKey = autogenerateKeys && (generatedKeyID != null);
        }
        else if (COL_NAME.equals(localName)) {
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
            colIdx++;
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
            if (!currCriticalError) {
                executeStatement();
            }
            else {
                rowDisc++;
                // aggiunta DiscardCause al dhr...
                String msg = currentXSLMessage;

                dhr.addDiscardCause(new DiscardCause(rowCounter, msg));

                resultMessage.append("Data error on row ").append(rowCounter).append(": ").append(msg);
                resultMessage.append("SQL Statement Informations:\n").append(sqlStatementInfo);
                resultMessage.append("Record parameters:\n").append(dumpCurrentRowFields());
                resultStatus = STATUS_PARTIAL;
            }
        }
        else if (COL_NAME.equals(localName)) {
            PreparedStatement ps = (PreparedStatement) sqlStatementInfo.getStatement();
            try {
                colDataExpecting = false;
                boolean autoKeySet = false;
                String text = textBuffer.toString();
                if (autogenerateKeys) {
                    if (text.startsWith(GENERATED_KEY_ID)) {
                        Object key = generatedKeys.get(text);
                        ps.setObject(colIdx, key);
                        currentRowFields.add(key);
                        autoKeySet = true;
                    }
                }
                if (!autoKeySet) {
                    if (TIMESTAMP_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            ps.setNull(colIdx, Types.TIMESTAMP);
                            currentRowFields.add(null);
                        }
                        else {
                            dateFormatter.applyPattern(currDateFormat);
                            Date formattedDate = dateFormatter.parse(text);
                            Timestamp ts = new Timestamp(formattedDate.getTime());
                            ps.setTimestamp(colIdx, ts);
                            currentRowFields.add(ts);
                        }
                    }
                    else if (NUMERIC_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            ps.setNull(colIdx, Types.NUMERIC);
                            currentRowFields.add(null);
                        }
                        else {
                            ps.setInt(colIdx, Integer.parseInt(text));
                            currentRowFields.add(Integer.valueOf(text));
                        }
                    }
                    else if (FLOAT_TYPE.equals(currType) || DECIMAL_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            ps.setNull(colIdx, Types.NUMERIC);
                            currentRowFields.add(null);
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
                                ps.setBigDecimal(colIdx, formattedNumber);
                                currentRowFields.add(formattedNumber);
                            }
                            finally {
                                numberFormatter.setParseBigDecimal(isBigDecimal);
                            }
                        }
                    }
                    else if (LONG_STRING_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            ps.setNull(colIdx, Types.CLOB);
                            currentRowFields.add(null);
                        }
                        else {
                            byte[] data = text.getBytes();
                            ByteArrayInputStream bais = new ByteArrayInputStream(data);
                            ps.setAsciiStream(colIdx, bais, data.length);
                            currentRowFields.add(text);
                        }
                    }
                    else if (BASE64_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            ps.setNull(colIdx, Types.BLOB);
                            currentRowFields.add(null);
                        }
                        else {
                            byte[] data = text.getBytes();
                            data = Base64.decodeBase64(data);
                            ByteArrayInputStream bais = new ByteArrayInputStream(data);
                            ps.setBinaryStream(colIdx, bais, data.length);
                            currentRowFields.add(text);
                        }
                    }
                    else if (BINARY_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            ps.setNull(colIdx, Types.BLOB);
                            currentRowFields.add(null);
                        }
                        else {
                            byte[] data = text.getBytes();
                            ByteArrayInputStream bais = new ByteArrayInputStream(data);
                            ps.setBinaryStream(colIdx, bais, data.length);
                            currentRowFields.add(text);
                        }
                    }
                    else {
                        if (text.equals("")) {
                            ps.setNull(colIdx, Types.VARCHAR);
                            currentRowFields.add(null);
                        }
                        else {
                            ps.setString(colIdx, text);
                            currentRowFields.add(text);
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

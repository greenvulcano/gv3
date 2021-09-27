/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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

import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.datahandling.DBOException;
import it.greenvulcano.gvesb.datahandling.utils.FieldFormatter;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;

/**
 * IDBO Class specialized in selecting data from the DB.
 * The selected data are formatted as CSV text.
 *
 * @version 3.2.0 01/10/2011
 * @author GreenVulcano Developer Team
 */
public class DBOFlatSelect extends AbstractDBO
{
    private static final Logger         logger               = GVLogger.getLogger(DBOFlatSelect.class);

    private final Map<String, FieldFormatter> fieldNameToFormatter = new HashMap<String, FieldFormatter>();
    private final Map<String, FieldFormatter> fieldIdToFormatter   = new HashMap<String, FieldFormatter>();

    private int                         sbRowLength          = 100;
    private String                      endLine              = DEFAULT_END_LINE;
    private String                      encoding             = DEFAULT_ENCODING;

    private String                      statement            = null;
    private String                      stmID                = null;

    private String                      directFilePath       = null;

    public DBOFlatSelect()
    {
        super();
    }

    @Override
    public void init(Node config) throws DBOException
    {
        super.init(config);
        try {
            this.endLine = XMLConfig.get(config, "@end-line", DEFAULT_END_LINE);
            this.encoding = XMLConfig.get(config, "@encoding", DEFAULT_ENCODING);
            this.forcedMode = XMLConfig.get(config, "@force-mode", MODE_DB2XML);
            this.isReturnData = XMLConfig.getBoolean(config, "@return-data", true);
            this.directFilePath = XMLConfig.get(config, "@direct-file-path", null);
            Node stmt = XMLConfig.getNode(config, "statement[@type='select']");
            if (stmt == null) {
                throw new DBOException("Empty/misconfigured statements list for [" + getName() + "/" + this.dboclass + "]");
            }

            this.stmID = XMLConfig.get(stmt, "@id");
            if (this.stmID == null) {
                this.stmID = Integer.toString(0);
            }
            this.statement = XMLConfig.getNodeValue(stmt);

            Node fFrmsL = XMLConfig.getNode(config, "FieldFormatters");
            NodeList fFrms = XMLConfig.getNodeList(fFrmsL, "*[@type='field-formatter']");
            for (int j = 0; j < fFrms.getLength(); j++) {
                Node fF = fFrms.item(j);
                FieldFormatter fForm = new FieldFormatter();
                fForm.init(fF);
                String fName = fForm.getFieldName();
                if (fName != null) {
                    if (fName.indexOf(",") != -1) {
                        StringTokenizer st = new StringTokenizer(fName, " ,");
                        while (st.hasMoreTokens()) {
                            this.fieldNameToFormatter.put(st.nextToken(), fForm);
                        }
                    }
                    else {
                        this.fieldNameToFormatter.put(fForm.getFieldName(), fForm);
                    }
                }
                String fId = fForm.getFieldId();
                if (fId != null) {
                    if (fId.indexOf(",") != -1) {
                        StringTokenizer st = new StringTokenizer(fId, " ,");
                        while (st.hasMoreTokens()) {
                            this.fieldIdToFormatter.put(st.nextToken(), fForm);
                        }
                    }
                    else {
                        this.fieldIdToFormatter.put(fForm.getFieldId(), fForm);
                    }
                }
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
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#executeIn(java.lang.Object,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public void executeIn(Object input, Connection conn, Map<String, Object> props) throws DBOException
    {
        prepare();
        throw new DBOException("Unsupported method - DBOFlatSelect::executeIn(Object, Connection, Map)");
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#executeOut(java.sql.Connection, java.util.Map)
     */
    @Override
    public Object executeOut(Connection conn, Map<String, Object> props) throws DBOException,
            InterruptedException {
        FileWriter fw = null;
        try {
            prepare();
            this.rowCounter = 0;
            logger.debug("Begin execution of DB data read through " + this.dboclass);

            Map<String, Object> localProps = buildProps(props);
            logProps(localProps);

            String localDirectFilePath = PropertiesHandler.expand(this.directFilePath, localProps, conn, null);
            if (localDirectFilePath != null) {
                fw = new FileWriter(localDirectFilePath);
            }

            StringBuilder sb = new StringBuilder(this.sbRowLength);

            if (this.statement != null) {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), getName(), logger);
                String expandedSQL = PropertiesHandler.expand(this.statement, localProps, conn, null);
                Statement sqlStatement = null;
                try {
                    sqlStatement = getInternalConn(conn, localProps).createStatement();
                    logger.debug("Executing select:\n" + expandedSQL);
                    this.sqlStatementInfo = new StatementInfo("0", expandedSQL, sqlStatement);
                    ResultSet rs = sqlStatement.executeQuery(expandedSQL);
                    if (rs != null) {
                        try {
                            ResultSetMetaData metadata = rs.getMetaData();
                            FieldFormatter[] fFormatters = buildFormatterArray(metadata, this.fieldNameToFormatter,
                                    this.fieldIdToFormatter);
                            String textVal = null;
                            while (rs.next()) {
                                if ((this.rowCounter % 10) == 0) {
                                    ThreadUtils.checkInterrupted(getClass().getSimpleName(), getName(), logger);
                                }
                                for (int j = 1; j <= metadata.getColumnCount(); j++) {
                                    FieldFormatter fF = fFormatters[j];
                                    if (fF == null) {
                                        fF = fFormatters[0];
                                    }
                                    switch (metadata.getColumnType(j)) {
                                        case Types.DATE :
                                        case Types.TIME :
                                        case Types.TIMESTAMP :{
                                            Timestamp dateVal = rs.getTimestamp(j);
                                            if (dateVal == null) {
                                                textVal = fF.formatField("");
                                            }
                                            else {
                                                textVal = fF.formatDate(dateVal);
                                            }
                                        }
                                            break;
                                        case Types.DOUBLE :
                                        case Types.FLOAT :
                                        case Types.REAL : {
                                            float numVal = rs.getFloat(j);
                                            textVal = fF.formatNumber(numVal);
                                        }
                                            break;
                                        case Types.BIGINT :
                                        case Types.INTEGER :
                                        case Types.NUMERIC :
                                        case Types.SMALLINT :
                                        case Types.TINYINT : {
                                            BigDecimal bigdecimal = rs.getBigDecimal(j);
                                            if (bigdecimal == null) {
                                                textVal = fF.formatField("");
                                            }
                                            else if (metadata.getScale(j) > 0) {
                                                textVal = fF.formatNumber(bigdecimal);
                                            }
                                            else {
                                                textVal = fF.formatField(bigdecimal.toString());
                                            }
                                        }
                                            break;
                                        case Types.NCHAR :
                                        case Types.NVARCHAR :{
                                            String val = rs.getNString(j);
                                            if (val == null) {
                                                textVal = fF.formatField("");
                                            }
                                            else {
                                                textVal = fF.formatField(val);
                                            }
                                        }
                                            break;
                                        case Types.CHAR :
                                        case Types.VARCHAR :{
                                            String val = rs.getString(j);
                                            if (val == null) {
                                                textVal = fF.formatField("");
                                            }
                                            else {
                                                textVal = fF.formatField(val);
                                            }
                                        }
                                            break;
                                        case Types.NCLOB :{
                                            textVal = "";
                                        }
                                            break;
                                        case Types.CLOB :{
                                            textVal = "";
                                        }
                                            break;
                                        case Types.BLOB :{
                                            textVal = "";
                                        }
                                            break;
                                        default :{
                                            textVal = fF.formatField(rs.getString(j));
                                        }
                                    }
                                    sb.append(textVal);
                                }
                                this.rowCounter++;
                                if (fw != null) {
                                    fw.append(sb).append(this.endLine);
                                    sb.delete(0, sb.length());
                                }
                                else {
                                    sb.append(this.endLine);
                                }
                            }
                        }
                        finally {
                            if (rs != null) {
                                try {
                                    rs.close();
                                }
                                catch (Exception exc) {
                                    // do nothing
                                }
                                rs = null;
                            }
                        }
                    }
                }
                finally {
                	if (this.sqlStatementInfo != null) {
                        try {
                        	this.sqlStatementInfo.close();
                        }
                        catch (Exception exc) {
                            // do nothing
                        }
                        this.sqlStatementInfo = null;
                        sqlStatement = null;
                    }
                }
            }
            this.sbRowLength = sb.length();

            this.dhr.setRead(this.rowCounter);

            logger.debug("End execution of DB data read through " + this.dboclass);
            if (fw == null) {
                Charset cs = Charset.forName(this.encoding);
                ByteBuffer bb = cs.encode(CharBuffer.wrap(sb));
                return new String(bb.array(), 0, sb.length());
            }
            return null;
        }
        catch (SQLException exc) {
            OracleExceptionHandler.handleSQLException(exc);
            throw new DBOException("Error on execution of " + this.dboclass + " with name [" + getName() + "]: "
                        + exc.getMessage(), exc);
        }
        catch (Exception exc) {
            logger.error("Error on execution of " + this.dboclass + " with name [" + getName() + "]", exc);
            throw new DBOException("Error on execution of " + this.dboclass + " with name [" + getName() + "]: "
                        + exc.getMessage(), exc);
        }
        finally {
            // cleanup();
            if (fw != null) {
                try {
                    fw.flush();
                    fw.close();
                }
                catch (Exception exc2) {
                    // do nothing
                }
            }
        }
    }

    private FieldFormatter[] buildFormatterArray(ResultSetMetaData rsm, Map<String, FieldFormatter> fNToFormatter,
            Map<String, FieldFormatter> fIdToFormatter) throws Exception
    {
        FieldFormatter[] fFA = new FieldFormatter[rsm.getColumnCount() + 1];
        fFA[0] = this.fieldIdToFormatter.get("0");

        for (int i = 1; i < fFA.length; i++) {
            FieldFormatter fF = fNToFormatter.get(rsm.getColumnName(i));
            if (fF == null) {
                fF = fIdToFormatter.get("" + i);
            }
            fFA[i] = fF;
        }
        return fFA;
    }
}
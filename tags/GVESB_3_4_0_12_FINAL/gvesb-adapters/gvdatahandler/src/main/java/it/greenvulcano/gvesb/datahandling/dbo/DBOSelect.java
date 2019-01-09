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

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.datahandling.DBOException;
import it.greenvulcano.gvesb.datahandling.dbo.utils.ExtendedRowSetBuilder;
import it.greenvulcano.gvesb.datahandling.dbo.utils.RowSetBuilder;
import it.greenvulcano.gvesb.datahandling.dbo.utils.StandardRowSetBuilder;
import it.greenvulcano.gvesb.datahandling.utils.FieldFormatter;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * IDBO Class specialized in selecting data from the DB.
 * The selected data are formatted as RowSet XML document.
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 */
public class DBOSelect extends AbstractDBO
{
    private final Map<String, Set<Integer>>          keysMap;

    private String                                   numberFormat           = DEFAULT_NUMBER_FORMAT;
    private String                                   groupSeparator         = DEFAULT_GRP_SEPARATOR;
    private String                                   decSeparator           = DEFAULT_DEC_SEPARATOR;
    private final Map<String, Map<String, FieldFormatter>> statIdToNameFormatters = new HashMap<String, Map<String, FieldFormatter>>();
    private final Map<String, Map<String, FieldFormatter>> statIdToIdFormatters   = new HashMap<String, Map<String, FieldFormatter>>();

    private static final Logger                      logger                 = GVLogger.getLogger(DBOSelect.class);

    private RowSetBuilder                            rowSetBuilder          = null;

    /**
     *
     */
    public DBOSelect()
    {
        super();
        this.keysMap = new HashMap<String, Set<Integer>>();
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node config) throws DBOException
    {
        super.init(config);
        try {
            this.forcedMode = XMLConfig.get(config, "@force-mode", MODE_DB2XML);
            this.isReturnData = XMLConfig.getBoolean(config, "@return-data", true);
            String rsBuilder = XMLConfig.get(config, "@rowset-builder", "standard");
            if (rsBuilder.equals("extended")) {
                this.rowSetBuilder = new ExtendedRowSetBuilder();
            }
            else {
                this.rowSetBuilder = new StandardRowSetBuilder();
            }
            this.rowSetBuilder.setName(getName());
            this.rowSetBuilder.setLogger(logger);

            NodeList stmts = XMLConfig.getNodeList(config, "statement[@type='select']");
            String id = null;
            String keys = null;
            Node stmt;
            for (int i = 0; i < stmts.getLength(); i++) {
                stmt = stmts.item(i);
                id = XMLConfig.get(stmt, "@id");
                keys = XMLConfig.get(stmt, "@keys");
                if (id == null) {
                    id = Integer.toString(i);
                }
                this.statements.put(id, XMLConfig.getNodeValue(stmt));
                if (keys != null) {
                    Set<Integer> s = new HashSet<Integer>();
                    StringTokenizer sTok = new StringTokenizer(keys, ",");
                    while (sTok.hasMoreTokens()) {
                        String str = sTok.nextToken();
                        s.add(new Integer(str.trim()));
                    }
                    this.keysMap.put(id, s);
                }
            }

            NodeList fFrmsList = XMLConfig.getNodeList(config, "FieldFormatters");
            for (int i = 0; i < fFrmsList.getLength(); i++) {
                Node fFrmsL = fFrmsList.item(i);
                id = XMLConfig.get(fFrmsL, "@id");
                if (id == null) {
                    id = Integer.toString(i);
                }
                Map<String, FieldFormatter> fieldNameToFormatter = new HashMap<String, FieldFormatter>();
                Map<String, FieldFormatter> fieldIdToFormatter = new HashMap<String, FieldFormatter>();

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
                                fieldNameToFormatter.put(st.nextToken().toUpperCase().trim(), fForm);
                            }
                        }
                        else {
                            fieldNameToFormatter.put(fForm.getFieldName().toUpperCase().trim(), fForm);
                        }
                    }
                    String fId = fForm.getFieldId();
                    if (fId != null) {
                        if (fId.indexOf(",") != -1) {
                            StringTokenizer st = new StringTokenizer(fId, " ,");
                            while (st.hasMoreTokens()) {
                                fieldIdToFormatter.put(st.nextToken().toUpperCase().trim(), fForm);
                            }
                        }
                        else {
                            fieldIdToFormatter.put(fForm.getFieldId().toUpperCase().trim(), fForm);
                        }
                    }
                }
                this.statIdToNameFormatters.put(id, fieldNameToFormatter);
                this.statIdToIdFormatters.put(id, fieldIdToFormatter);
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
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#execute(java.lang.Object,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public void execute(Object input, Connection conn, Map<String, Object> props) throws DBOException
    {
        prepare();
        throw new DBOException("Unsupported method - DBOSelect::execute(Object, Connection, Map)");
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#execute(java.io.OutputStream,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public void execute(OutputStream dataOut, Connection conn, Map<String, Object> props) throws DBOException,
            InterruptedException {
        XMLUtils parser = null;
        String expandedSQL = null;
        try {
            prepare();
            this.rowCounter = 0;
            logger.debug("Begin execution of DB data read through " + this.dboclass);

            Map<String, Object> localProps = buildProps(props);
            logProps(localProps);

            this.numberFormat = (String) localProps.get(FORMAT_NAME);
            if (this.numberFormat == null) {
                this.numberFormat = DEFAULT_NUMBER_FORMAT;
            }
            this.groupSeparator = (String) localProps.get(GRP_SEPARATOR_NAME);
            if (this.groupSeparator == null) {
                this.groupSeparator = DEFAULT_GRP_SEPARATOR;
            }
            this.decSeparator = (String) localProps.get(DEC_SEPARATOR_NAME);
            if (this.decSeparator == null) {
                this.decSeparator = DEFAULT_DEC_SEPARATOR;
            }
            DecimalFormatSymbols dfs = this.numberFormatter.getDecimalFormatSymbols();
            dfs.setDecimalSeparator(this.decSeparator.charAt(0));
            dfs.setGroupingSeparator(this.groupSeparator.charAt(0));
            this.numberFormatter.setDecimalFormatSymbols(dfs);
            this.numberFormatter.applyPattern(this.numberFormat);

            parser = XMLUtils.getParserInstance();
            Document doc = this.rowSetBuilder.createDocument(parser);

            this.rowSetBuilder.setXMLUtils(parser);
            this.rowSetBuilder.setDateFormatter(this.dateFormatter);
            this.rowSetBuilder.setNumberFormatter(this.numberFormatter);
            this.rowSetBuilder.setDecSeparator(this.decSeparator);
            this.rowSetBuilder.setGroupSeparator(this.groupSeparator);
            this.rowSetBuilder.setNumberFormat(this.numberFormat);

            for (Entry<String, String> entry : this.statements.entrySet()) {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), getName(), logger);
                Object key = entry.getKey();
                String stmt = entry.getValue();
                Set<Integer> keyField = this.keysMap.get(key);
                Map<String, FieldFormatter> fieldNameToFormatter = new HashMap<String, FieldFormatter>();
                if (this.statIdToNameFormatters.containsKey(key)) {
                    fieldNameToFormatter = this.statIdToNameFormatters.get(key);
                }
                Map<String, FieldFormatter> fieldIdToFormatter = new HashMap<String, FieldFormatter>();
                if (this.statIdToIdFormatters.containsKey(key)) {
                    fieldIdToFormatter = this.statIdToIdFormatters.get(key);
                }

                if (stmt != null) {
                    expandedSQL = PropertiesHandler.expand(stmt, localProps, conn, null);
                    Statement statement = null;
                    try {
                        statement = getInternalConn(conn, localProps).createStatement();
                        logger.debug("Executing select:\n" + expandedSQL);
                        this.sqlStatementInfo = new StatementInfo(key.toString(), expandedSQL, statement);
                        ResultSet rs = statement.executeQuery(expandedSQL);
                        if (rs != null) {
                            try {
                                this.rowCounter += this.rowSetBuilder.build(doc, "" + key, rs, keyField, fieldNameToFormatter,
                                                                  fieldIdToFormatter);
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
                    catch (SQLException exc) {
                    	logger.error("Error on execution of " + this.dboclass + " with name [" + getName() + "]", exc);
                        logger.error("SQL Statement Informations:\n" + this.sqlStatementInfo);
                        OracleExceptionHandler.handleSQLException(exc);
                        throw new DBOException("Error on execution of " + this.dboclass + " with name [" + getName() + "]: "
                                    + exc.getMessage(), exc);
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
                            statement = null;
                        }
                    }
                }
            }
            byte[] dataDOM = parser.serializeDOMToByteArray(doc);
            dataOut.write(dataDOM);

            this.dhr.setRead(this.rowCounter);

            logger.debug("End execution of DB data read through " + this.dboclass);
        }
        catch (DBOException exc) {
        	throw exc;
        }
        catch (InterruptedException exc) {
            logger.error("DBO[" + getName() + "] interrupted", exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error on execution of " + this.dboclass + " with name [" + getName() + "]", exc);
            throw new DBOException("Error on execution of " + this.dboclass + " with name [" + getName() + "]: "
                        + exc.getMessage(), exc);
        }
        finally {
            // cleanup();
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
            this.rowSetBuilder.cleanup();
        }
    }
}

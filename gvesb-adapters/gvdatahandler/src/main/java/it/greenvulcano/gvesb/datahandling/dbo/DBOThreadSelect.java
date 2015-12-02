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
import it.greenvulcano.gvesb.datahandling.utils.FieldFormatter;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * IDBO Class specialized in selecting data from the DB using multiple Threads.
 * The selected data are formatted as RowSet XML document.
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 */
public class DBOThreadSelect extends AbstractDBO
{

    private class ThreadSelect implements Runnable
    {

        private String              stmt             = null;

        private Document            doc              = null;

        private Object              key              = null;

        private Map<String, Object> props            = null;

        private final static int    NEW              = 0;

        private final static int    RUNNING          = 1;

        private final static int    TERM             = 2;

        private final static int    ERROR            = 3;

        private int                 state            = NEW;

        private boolean             stopNow          = false;

        private Map<Object, Object> context          = null;

        private long                rowThreadCounter = 0;

        private ThreadSelect(Map<Object, Object> ctx)
        {
            context = ctx;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void run()
        {
            MDC.getContext().putAll(context);
            logger.debug("Thread " + Thread.currentThread().getName() + " started.");
            state = RUNNING;
            Connection conn = null;
            Statement sqlStatement = null;
            ResultSet rs = null;
            try {
                Set<Integer> keyField = keysMap.get(key);
                boolean noKey = ((keyField == null) || keyField.isEmpty());
                Map<String, FieldFormatter> fieldNameToFormatter = new HashMap<String, FieldFormatter>();
                if (statIdToNameFormatters.containsKey(key)) {
                    fieldNameToFormatter = statIdToNameFormatters.get(key);
                }
                Map<String, FieldFormatter> fieldIdToFormatter = new HashMap<String, FieldFormatter>();
                if (statIdToIdFormatters.containsKey(key)) {
                    fieldIdToFormatter = statIdToIdFormatters.get(key);
                }

                if (stmt != null) {
                    conn = getConnection();
                    String expandedSQL = PropertiesHandler.expand(stmt, props, conn, null);
                    sqlStatement = conn.createStatement();
                    logger.debug("Executing select statement: " + expandedSQL + ".");
                    rs = sqlStatement.executeQuery(expandedSQL);
                    if (rs != null) {
                        ResultSetMetaData metadata = rs.getMetaData();
                        FieldFormatter[] fFormatters = buildFormatterArray(metadata, fieldNameToFormatter,
                                fieldIdToFormatter);
                        Element data = null;
                        Vector<Element> dataElementVector = new Vector<Element>();
                        Element row = null;
                        Element col = null;
                        Text text = null;
                        String textVal = null;
                        String precKey = null;
                        String colKey = null;
                        Map<String, String> keyAttr = new HashMap<String, String>();
                        Document localDoc = xml.newDocument();
                        while (rs.next()) {
                            if (stopNow) {
                                break;
                            }
                            row = xml.createElement(localDoc, ROW_NAME);

                            xml.setAttribute(row, ID_NAME, (String) key);
                            for (int j = 1; j <= metadata.getColumnCount(); j++) {
                                FieldFormatter fF = fFormatters[j];

                                col = xml.createElement(localDoc, COL_NAME);
                                switch (metadata.getColumnType(j)) {
                                	case Types.DATE :
                                	case Types.TIME :
                                	case Types.TIMESTAMP: {
                                        xml.setAttribute(col, TYPE_NAME, TIMESTAMP_TYPE);
                                        Timestamp dateVal = rs.getTimestamp(j);
                                        if (dateVal == null) {
                                            xml.setAttribute(col, FORMAT_NAME, DEFAULT_DATE_FORMAT);
                                            textVal = "";
                                        }
                                        else {
                                            if (fF != null) {
                                                xml.setAttribute(col, FORMAT_NAME, fF.getDateFormat());
                                                textVal = fF.formatDate(dateVal);
                                            }
                                            else {
                                                xml.setAttribute(col, FORMAT_NAME, DEFAULT_DATE_FORMAT);
                                                textVal = dateFormatter.format(dateVal);
                                            }
                                        }
                                    }
                                        break;
                                    case Types.DOUBLE :
                                    case Types.FLOAT :
                                    case Types.REAL : {
                                        xml.setAttribute(col, TYPE_NAME, FLOAT_TYPE);
                                        float numVal = rs.getFloat(j);
                                        if (fF != null) {
                                            xml.setAttribute(col, FORMAT_NAME, fF.getNumberFormat());
                                            xml.setAttribute(col, GRP_SEPARATOR_NAME, fF.getGroupSeparator());
                                            xml.setAttribute(col, DEC_SEPARATOR_NAME, fF.getDecSeparator());
                                            textVal = fF.formatNumber(numVal);
                                        }
                                        else {
                                            xml.setAttribute(col, FORMAT_NAME, numberFormat);
                                            xml.setAttribute(col, GRP_SEPARATOR_NAME, groupSeparator);
                                            xml.setAttribute(col, DEC_SEPARATOR_NAME, decSeparator);
                                            textVal = numberFormatter.format(numVal);
                                        }
                                    }
                                        break;
                                    case Types.BIGINT :
                                    case Types.INTEGER :
                                    case Types.NUMERIC :
                                    case Types.SMALLINT : 
                                    case Types.TINYINT : {
                                        BigDecimal bigdecimal = rs.getBigDecimal(j);
                                        if (bigdecimal == null) {
                                            if (metadata.getScale(j) > 0) {
                                                xml.setAttribute(col, TYPE_NAME, FLOAT_TYPE);
                                            }
                                            else {
                                                xml.setAttribute(col, TYPE_NAME, NUMERIC_TYPE);
                                            }
                                            textVal = "";
                                        }
                                        else {
                                            if (fF != null) {
                                                xml.setAttribute(col, TYPE_NAME, FLOAT_TYPE);
                                                xml.setAttribute(col, FORMAT_NAME, fF.getNumberFormat());
                                                xml.setAttribute(col, GRP_SEPARATOR_NAME, fF.getGroupSeparator());
                                                xml.setAttribute(col, DEC_SEPARATOR_NAME, fF.getDecSeparator());
                                                textVal = fF.formatNumber(bigdecimal);
                                            }
                                            else if (metadata.getScale(j) > 0) {
                                                xml.setAttribute(col, TYPE_NAME, FLOAT_TYPE);
                                                xml.setAttribute(col, FORMAT_NAME, numberFormat);
                                                xml.setAttribute(col, GRP_SEPARATOR_NAME, groupSeparator);
                                                xml.setAttribute(col, DEC_SEPARATOR_NAME, decSeparator);
                                                textVal = numberFormatter.format(bigdecimal);
                                            }
                                            else {
                                                xml.setAttribute(col, TYPE_NAME, NUMERIC_TYPE);
                                                textVal = bigdecimal.toString();
                                            }
                                        }
                                    }
                                        break;
                                     case Types.NCHAR :
                                     case Types.NVARCHAR : {
                                        xml.setAttribute(col, TYPE_NAME, STRING_TYPE);
                                        textVal = rs.getNString(j);
                                        if (textVal == null) {
                                             textVal = "";
                                        }
                                    }
                                        break;
                                    case Types.CHAR :
                                    case Types.VARCHAR :{
                                        xml.setAttribute(col, TYPE_NAME, STRING_TYPE);
                                        textVal = rs.getString(j);
                                        if (textVal == null) {
                                            textVal = "";
                                        }
                                    }
                                        break;
                                    case Types.NCLOB : {
                                        xml.setAttribute(col, TYPE_NAME, LONG_NSTRING_TYPE);
                                        NClob clob = rs.getNClob(j);
                                        if (clob != null) {
                                            Reader is = clob.getCharacterStream();
                                            StringWriter str = new StringWriter();
                
                                            IOUtils.copy(is, str);
                                            is.close();
                                            textVal = str.toString();
                                        }
                                        else {
                                            textVal = "";
                                        }
                                    }
                                        break;
                                    case Types.CLOB : {
                                        xml.setAttribute(col, TYPE_NAME, LONG_STRING_TYPE);
                                        Clob clob = rs.getClob(j);
                                        if (clob != null) {
                                        	Reader is = clob.getCharacterStream();
                                            StringWriter str = new StringWriter();
                
                                            IOUtils.copy(is, str);
                                            is.close();
                                            textVal = str.toString();
                                        }
                                        else {
                                            textVal = "";
                                        }
                                    }
                                        break;
                                    case Types.BLOB :{
                                        xml.setAttribute(col, TYPE_NAME, BASE64_TYPE);
                                        Blob blob = rs.getBlob(j);
                                        if (blob != null) {
                                            InputStream is = blob.getBinaryStream();
                                            byte[] buffer = new byte[2048];
                                            ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
                                            int size;
                                            while ((size = is.read(buffer)) != -1) {
                                                baos.write(buffer, 0, size);
                                            }
                                            is.close();
                                            textVal = new String(Base64.encodeBase64(buffer));
                                        }
                                        else {
                                            textVal = "";
                                        }
                                    }
                                        break;
                                    default :{
                                        xml.setAttribute(col, TYPE_NAME, DEFAULT_TYPE);
                                        textVal = rs.getString(j);
                                        if (textVal == null) {
                                            textVal = "";
                                        }
                                    }
                                }
                                if (textVal != null) {
                                    text = localDoc.createTextNode(textVal);
                                    col.appendChild(text);
                                }
                                if (!noKey && keyField.contains(new Integer(j))) {
                                    if (textVal != null) {
                                        if (colKey == null) {
                                            colKey = textVal;
                                        }
                                        else {
                                            colKey += textVal;
                                        }
                                        keyAttr.put("key_" + j, textVal);
                                    }
                                }
                                else {
                                    row.appendChild(col);
                                }
                            }
                            if (noKey) {
                                if (data == null) {
                                    data = xml.createElement(localDoc, DATA_NAME);
                                    xml.setAttribute(data, ID_NAME, key.toString());
                                }
                            }
                            else if ((colKey != null) && !colKey.equals(precKey)) {
                                if (data != null) {
                                    dataElementVector.add(data);
                                }
                                data = xml.createElement(localDoc, DATA_NAME);
                                xml.setAttribute(data, ID_NAME, key.toString());
                                for (Entry<String, String> entry : keyAttr.entrySet()) {
                                    xml.setAttribute(data, entry.getKey(), entry.getValue());
                                }
                                keyAttr.clear();
                                precKey = colKey;
                            }
                            colKey = null;
                            data.appendChild(row);
                            rowThreadCounter++;
                        }
                        if (!stopNow) {
                            if (data != null) {
                                dataElementVector.add(data);
                            }
                            synchronized (doc) {
                                Element docRoot = doc.getDocumentElement();
                                for (int i = 0; i < dataElementVector.size(); i++) {
                                    Node dataNode = doc.importNode(dataElementVector.get(i), true);
                                    docRoot.appendChild(dataNode);
                                }
                            }
                        }
                    }
                }
            }
            catch (SQLException exc) {
                OracleExceptionHandler.handleSQLException(exc).printLoggerInfo();
                state = ERROR;
            }
            catch (Throwable exc) {
                logger.error("Thread " + Thread.currentThread().getName() + " terminated with error.", exc);
                state = ERROR;
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
                if (sqlStatement != null) {
                    try {
                        sqlStatement.close();
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }
                if (conn != null) {
                    try {
                        releaseConnection(conn);
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }
                logger.debug("Thread " + Thread.currentThread().getName() + " terminated.");
                if (state != ERROR) {
                    state = TERM;
                }
                synchronized (synchObj) {
                    synchObj.notify();
                }
            }
        }

        private void stopNow()
        {
            stopNow = true;
            state = TERM;
        }

        private void setKey(Object key)
        {
            this.key = key;
        }

        private void setDocument(Document doc)
        {
            this.doc = doc;
        }

        private void setProps(Map<String, Object> props)
        {
            this.props = props;
        }

        private void setStatement(String stmt)
        {
            this.stmt = stmt;
        }

        private Connection getConnection() throws Exception
        {
            return JDBCConnectionBuilder.getConnection(getJdbcConnectionName());
        }

        private void releaseConnection(Connection conn) throws Exception
        {
            JDBCConnectionBuilder.releaseConnection(getJdbcConnectionName(), conn);
        }

        /**
         * @return the rowThreadCounter
         */
        public long getRowThreadCounter()
        {
            return rowThreadCounter;
        }
    }

    private final Map<String, Set<Integer>>          keysMap;

    private final String                             ROWSET_NAME            = "RowSet";

    private final String                             DATA_NAME              = "data";

    private String                                   numberFormat           = DEFAULT_NUMBER_FORMAT;

    private String                                   groupSeparator         = DEFAULT_GRP_SEPARATOR;

    private String                                   decSeparator           = DEFAULT_DEC_SEPARATOR;

    private Map<String, Map<String, FieldFormatter>> statIdToNameFormatters = new HashMap<String, Map<String, FieldFormatter>>();
    private Map<String, Map<String, FieldFormatter>> statIdToIdFormatters   = new HashMap<String, Map<String, FieldFormatter>>();

    private static final Logger                      logger                 = GVLogger.getLogger(DBOThreadSelect.class);

    private XMLUtils                                 xml                    = null;

    private final Object                             synchObj               = new Object();

    /**
     *
     */
    public DBOThreadSelect()
    {
        super();
        keysMap = new HashMap<String, Set<Integer>>();
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node config) throws DBOException
    {
        super.init(config);
        try {
            forcedMode = XMLConfig.get(config, "@force-mode", MODE_DB2XML);
            isReturnData = XMLConfig.getBoolean(config, "@return-data", true);
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
                statements.put(id, XMLConfig.getNodeValue(stmt));
                if (keys != null) {
                    Set<Integer> s = new HashSet<Integer>();
                    StringTokenizer sTok = new StringTokenizer(keys, ",");
                    while (sTok.hasMoreTokens()) {
                        String str = sTok.nextToken();
                        s.add(new Integer(str.trim()));
                    }
                    keysMap.put(id, s);
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
                statIdToNameFormatters.put(id, fieldNameToFormatter);
                statIdToIdFormatters.put(id, fieldIdToFormatter);
            }

            xml = XMLUtils.getParserInstance();

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
    @SuppressWarnings("unchecked")
    @Override
    public void execute(OutputStream dataOut, Connection conn, Map<String, Object> props) throws DBOException
    {
        try {
            prepare();
            rowCounter = 0;
            logger.debug("Begin execution of DB data read through " + dboclass);

            Map<String, Object> localProps = buildProps(props);
            logProps(localProps);

            numberFormat = (String) localProps.get(FORMAT_NAME);
            if (numberFormat == null) {
                numberFormat = DEFAULT_NUMBER_FORMAT;
            }
            groupSeparator = (String) localProps.get(GRP_SEPARATOR_NAME);
            if (groupSeparator == null) {
                groupSeparator = DEFAULT_GRP_SEPARATOR;
            }
            decSeparator = (String) localProps.get(DEC_SEPARATOR_NAME);
            if (decSeparator == null) {
                decSeparator = DEFAULT_DEC_SEPARATOR;
            }
            DecimalFormatSymbols dfs = numberFormatter.getDecimalFormatSymbols();
            dfs.setDecimalSeparator(decSeparator.charAt(0));
            dfs.setGroupingSeparator(groupSeparator.charAt(0));
            numberFormatter.setDecimalFormatSymbols(dfs);
            numberFormatter.applyPattern(numberFormat);

            Document doc = xml.newDocument(ROWSET_NAME);

            Vector<ThreadSelect> thrVector = new Vector<ThreadSelect>();
            for (Entry<String, String> entry : statements.entrySet()) {
                Object key = entry.getKey();
                String stmt = entry.getValue();
                ThreadSelect ts = new ThreadSelect(MDC.getContext());
                ts.setDocument(doc);
                ts.setStatement(stmt);
                ts.setKey(key);
                ts.setProps(localProps);

                Thread t = new Thread(ts);
                t.start();
                thrVector.add(ts);
            }

            // wait for all threads are terminated
            boolean finished = false;
            boolean error = false;
            while (!finished) {
                int s = thrVector.size();
                int idx = 0;
                for (int i = 0; i < s; i++) {
                    ThreadSelect to = thrVector.get(idx);
                    if (error) {
                        to.stopNow();
                    }
                    switch (to.state) {
                        case ThreadSelect.TERM :{
                            thrVector.remove(idx);
                            rowCounter += to.getRowThreadCounter();
                        }
                            break;
                        case ThreadSelect.ERROR :{
                            thrVector.remove(idx);
                            rowCounter += to.getRowThreadCounter();
                            error = true;
                            idx = 0;
                        }
                            break;
                        default :
                            idx++;
                    }
                }
                if (thrVector.size() == 0) {
                    finished = true;
                }
                else {
                    try {
                        synchronized (synchObj) {
                            synchObj.wait(1000);
                        }
                    }
                    catch (InterruptedException e) {
                        // nothing to do
                    }
                }
            }

            byte[] dataDOM = xml.serializeDOMToByteArray(doc);
            dataOut.write(dataDOM);

            dhr.setRead(rowCounter);

            logger.debug("End execution of DB data read through " + dboclass);
        }
        catch (Exception exc) {
            logger.error("Error on execution of " + dboclass + " with name [" + getName() + "]", exc);
            throw new DBOException("Error on execution of " + dboclass + " with name [" + getName() + "]", exc);
        }
        finally {
            // cleanup();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#destroy()
     */
    @Override
    public void destroy()
    {
        super.destroy();
        if (xml != null) {
            XMLUtils.releaseParserInstance(xml);
        }
    }

    private FieldFormatter[] buildFormatterArray(ResultSetMetaData rsm,
            Map<String, FieldFormatter> fieldNameToFormatter, Map<String, FieldFormatter> fieldIdToFormatter)
            throws Exception
    {
        FieldFormatter[] fFA = new FieldFormatter[rsm.getColumnCount() + 1];

        for (int i = 1; i < fFA.length; i++) {
            FieldFormatter fF = fieldNameToFormatter.get(rsm.getColumnName(i));
            if (fF == null) {
                fF = fieldIdToFormatter.get("" + i);
            }
            fFA[i] = fF;
        }
        return fFA;
    }
}

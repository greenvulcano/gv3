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
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.datahandling.DBOException;
import it.greenvulcano.gvesb.datahandling.dbo.utils.ExtendedRowSetBuilder;
import it.greenvulcano.gvesb.datahandling.dbo.utils.RowSetBuilder;
import it.greenvulcano.gvesb.datahandling.dbo.utils.StandardRowSetBuilder;
import it.greenvulcano.gvesb.datahandling.utils.FieldFormatter;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xml.XMLUtils;

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
    	private String              localJdbcConnName = null;
        private String              stmt             = null;
        private Document            doc              = null;
        private Object              key              = null;
        private RowSetBuilder       rowSetBuilder    = null;
        private Map<String, Object> props            = null;

        private final static int    NEW              = 0;
        private final static int    RUNNING          = 1;
        private final static int    TERM             = 2;
        private final static int    ERROR            = 3;

        private int                 state            = NEW;
        private Throwable           throwable        = null;
        private Map<Object, Object> context          = null;
        private long                rowThreadCounter = 0;

        private ThreadSelect(Map<Object, Object> ctx)
        {
            this.context = ctx;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void run()
        {
            MDC.getContext().putAll(this.context);
            Thread thd = Thread.currentThread();
            logger.debug("Thread " + thd.getName() + " started.");
            this.state = RUNNING;
            Connection conn = null;
            Statement sqlStatement = null;
            StatementInfo sqlStatementInfo = null;
            ResultSet rs = null;
            try {
                Set<Integer> keyField = DBOThreadSelect.this.keysMap.get(this.key);
                Map<String, FieldFormatter> fieldNameToFormatter = new HashMap<String, FieldFormatter>();
                if (DBOThreadSelect.this.statIdToNameFormatters.containsKey(this.key)) {
                    fieldNameToFormatter = DBOThreadSelect.this.statIdToNameFormatters.get(this.key);
                }
                Map<String, FieldFormatter> fieldIdToFormatter = new HashMap<String, FieldFormatter>();
                if (DBOThreadSelect.this.statIdToIdFormatters.containsKey(this.key)) {
                    fieldIdToFormatter = DBOThreadSelect.this.statIdToIdFormatters.get(this.key);
                }

                if (this.stmt != null) {
                    conn = getConnection();
                    String expandedSQL = PropertiesHandler.expand(this.stmt, this.props, conn, null);
                    sqlStatement = conn.createStatement();
                    logger.debug("Executing select statement: " + expandedSQL + ".");
                    sqlStatementInfo = new StatementInfo(this.key.toString(), expandedSQL, sqlStatement);
                    rs = sqlStatement.executeQuery(expandedSQL);
                    if (rs != null) {
                        Document localDoc = this.rowSetBuilder.createDocument(null);
                        try {
                            this.rowThreadCounter += this.rowSetBuilder.build(localDoc, "" + this.key, rs, keyField,
                                    fieldNameToFormatter, fieldIdToFormatter);
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
                        if (!thd.isInterrupted()) {
                            synchronized (this.doc) {
                                Element docRoot = this.doc.getDocumentElement();
                                Element localDocRoot = localDoc.getDocumentElement();
                                NodeList nodes = localDocRoot.getChildNodes();
                                for (int i = 0; i < nodes.getLength(); i++) {
                                    Node dataNode = this.doc.importNode(nodes.item(i), true);
                                    docRoot.appendChild(dataNode);
                                }
                            }
                        }
                    }
                }
            }
            catch (SQLException exc) {
            	logger.error("Error on execution of " + DBOThreadSelect.this.dboclass + " with name [" + getName() + "]", exc);
                logger.error("SQL Statement Informations:\n" + sqlStatementInfo);
                OracleExceptionHandler.handleSQLException(exc).printLoggerInfo();
                this.state = ERROR;
                this.throwable = exc;
            }
            catch (Throwable exc) {
                logger.error("Thread " + thd.getName() + " terminated with error.", exc);
                this.state = ERROR;
                this.throwable = exc;
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
                if (sqlStatementInfo != null) {
                    try {
                    	sqlStatementInfo.close();
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                    sqlStatementInfo = null;
                    sqlStatement = null;
                }
                if (conn != null) {
                    try {
                        releaseConnection(conn);
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }
                logger.debug("Thread " + thd.getName() + " terminated.");
                if (this.state != ERROR) {
                    this.state = TERM;
                }
                synchronized (DBOThreadSelect.this.synchObj) {
                    DBOThreadSelect.this.synchObj.notify();
                }
            }
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

        private void setRowSetBuilder(RowSetBuilder rowSetBuilder)
        {
            this.rowSetBuilder = rowSetBuilder;
        }

        private void setLocalJdbcConnName(String localJdbcConnName)
        {
            this.localJdbcConnName = localJdbcConnName;
        }

        private Connection getConnection() throws Exception
        {
        	long startConn = System.currentTimeMillis();
            Connection conn = JDBCConnectionBuilder.getConnection(this.localJdbcConnName);
            long duration = System.currentTimeMillis() - startConn;
            if (duration > DBOThreadSelect.this.timeConn) {
            	DBOThreadSelect.this.timeConn = duration;
            }
            return conn;
        }

        private void releaseConnection(Connection conn) throws Exception
        {
            JDBCConnectionBuilder.releaseConnection(this.localJdbcConnName, conn);
        }

        /**
         * @return the rowThreadCounter
         */
        public long getRowThreadCounter()
        {
            return this.rowThreadCounter;
        }
    }

    private final Map<String, Set<Integer>>          keysMap;

    private String                                   numberFormat           = DEFAULT_NUMBER_FORMAT;
    private String                                   groupSeparator         = DEFAULT_GRP_SEPARATOR;
    private String                                   decSeparator           = DEFAULT_DEC_SEPARATOR;
    private final Map<String, Map<String, FieldFormatter>> statIdToNameFormatters = new HashMap<String, Map<String, FieldFormatter>>();
    private final Map<String, Map<String, FieldFormatter>> statIdToIdFormatters   = new HashMap<String, Map<String, FieldFormatter>>();

    private static final Logger                      logger                 = GVLogger.getLogger(DBOThreadSelect.class);

    private RowSetBuilder                            rowSetBuilder          = null;
    private final Object                             synchObj               = new Object();

    /**
     *
     */
    public DBOThreadSelect()
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
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#executeIn(java.lang.Object,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public void executeIn(Object input, Connection conn, Map<String, Object> props) throws DBOException,
            InterruptedException {
        prepare();
        throw new DBOException("Unsupported method - DBOSelect::executeIn(Object, Connection, Map)");
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#executeOut(java.sql.Connection, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object executeOut(Connection conn, Map<String, Object> props) throws DBOException,
            InterruptedException {
        XMLUtils parser = null;
        boolean error = false;
        Throwable throwable = null;

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

            String localJdbcConnName = PropertiesHandler.expand(getJdbcConnectionName(), props);
            logger.info("Using Connection: " + localJdbcConnName);

            Vector<ThreadSelect> thrSelVector = new Vector<ThreadSelect>();
            Vector<Thread> thrVector = new Vector<Thread>();
            for (Entry<String, String> entry : this.statements.entrySet()) {
                Object key = entry.getKey();
                String stmt = entry.getValue();
                ThreadSelect ts = new ThreadSelect(MDC.getContext());
                ts.setDocument(doc);
                ts.setStatement(stmt);
                ts.setKey(key);
                ts.setProps(localProps);
                ts.setRowSetBuilder(this.rowSetBuilder.getCopy());
                ts.setLocalJdbcConnName(localJdbcConnName);
                thrSelVector.add(ts);

                Thread t = new Thread(ts);
                thrVector.add(t);
                t.start();
            }

            try {
                Thread thd = Thread.currentThread();
                // wait for all threads are terminated
                boolean finished = false;
                while (!finished && !thd.isInterrupted()) {
                    int s = thrSelVector.size();
                    int idx = 0;
                    for (int i = 0; i < s; i++) {
                        ThreadSelect to = thrSelVector.get(idx);
                        if (error) {
                            thrVector.elementAt(i).interrupt();
                        }
                        switch (to.state) {
                            case ThreadSelect.TERM :{
                                thrSelVector.remove(idx);
                                this.rowCounter += to.getRowThreadCounter();
                            }
                                break;
                            case ThreadSelect.ERROR :{
                                thrSelVector.remove(idx);
                                this.rowCounter += to.getRowThreadCounter();
                                error = true;
                                throwable = to.throwable;
                                idx = 0;
                            }
                                break;
                            default :
                                idx++;
                        }
                    }
                    if (thrSelVector.size() == 0) {
                        finished = true;
                    }
                    else {
                        try {
                            synchronized (this.synchObj) {
                                this.synchObj.wait(1000);
                            }
                        }
                        catch (InterruptedException exc) {
                            logger.error("DBOThreadSelect[" + getName() + "] interrupted", exc);
                            throw exc;
                        }
                    }
                }
            }
            finally {
                thrSelVector.clear();
                for (Thread thread : thrVector) {
                    thread.interrupt();
                }
                thrVector.clear();
            }

        	if (throwable != null) {
        		throw throwable;
        	}
            this.dhr.setRead(this.rowCounter);

            logger.debug("End execution of DB data read through " + this.dboclass);
            return doc;
        }
        catch (Throwable exc) {
            logger.error("Error on execution of " + this.dboclass + " with name [" + getName() + "]", exc);
            ThreadUtils.checkInterrupted(exc);
            throw new DBOException("Error on execution of " + this.dboclass + " with name [" + getName() + "]: "
                        + exc.getMessage(), exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#destroy()
     */
    @Override
    public void destroy()
    {
        super.destroy();
    }

}

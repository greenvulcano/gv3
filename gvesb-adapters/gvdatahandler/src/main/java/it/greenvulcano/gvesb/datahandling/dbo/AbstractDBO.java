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
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.datahandling.DBOException;
import it.greenvulcano.gvesb.datahandling.DHResult;
import it.greenvulcano.gvesb.datahandling.IDBO;
import it.greenvulcano.gvesb.datahandling.utils.DiscardCause;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleError;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.txt.DateUtils;

/**
 * Extends the class {@link DefaultHandler} to parse input RowSet document
 * using SAX.
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 */
public abstract class AbstractDBO extends DefaultHandler implements IDBO
{
    /**
     *
     */
    protected static final int    STATUS_OK               = 0;
    /**
     *
     */
    protected static final int    STATUS_PARTIAL          = 1;
    /**
     *
     */
    protected static final int    STATUS_KO               = -1;

    /**
     *
     */
    protected static final String NO_RECORD_TO_UPDATE_MSG = "No records to update";

    /**
     *
     */
    protected int                 resultStatus            = STATUS_OK;

    /**
     *
     */
    protected StringBuffer        resultMessage           = null;

    /**
     * <i>logger</i> private instance.
     */
    private static final Logger   logger                  = GVLogger.getLogger(AbstractDBO.class);

    /**
     * Needed transformation to create meta-data from initial file.
     */
    private String                transformation;

    private boolean               transacted              = true;

    private boolean               isXA                    = false;

    /**
     *
     */
    protected String              serviceName;

    /**
     *
     */
    protected StatementInfo       sqlStatementInfo;

    /**
     *
     */
    protected boolean             statsOnInsert           = true;

    /**
     *
     */
    protected boolean             incrDiscIfUpdKO         = true;

    private String                jdbcConnectionName      = null;

    private String                jdbcConnectionNameInt   = null;
    private String                currentJdbcConnectionNameInt   = null;

    /**
     *
     */
    protected String              forcedMode              = "";

    /**
     *
     */
    protected boolean             ignoreInput             = false;
    private String                inputDataName           = null;
    private String                outputDataName          = null;

    /**
     * SQL Statements cache found in configuration identified by an ID.
     */
    protected Map<String, String> statements;

    /**
     * Unique identifiers cache.
     */
    protected Map<String, String> uuids;

    protected static final String GENERATED_KEY_ID        = "GV_GENERATED_KEY_";
    protected boolean             autogenerateKeys        = false;
    protected boolean             readGeneratedKey        = false;
    protected String              generatedKeyID          = "";
    protected String              resetGeneratedKeyID     = null;
    //protected Set<String>         autogenerateKeyColumns;
    protected Map<String, Object> generatedKeys;

    /**
     * Internal connection to DB to prepare the correct statement.
     */
    private Connection            internalConn;

    protected long timeConn = 0;

    /**
     * Properties to substitute in the statement meta-data bound to single
     * execution.
     */
    private Map<String, Object>   currentProps;

    /**
     * Properties eventually configured to overwrite in the service call.
     */
    private final Map<String, Object>   baseProps;

    private String                currentId               = "0";

    // true if this oracle error class is blocking for IDBO
    private boolean               blockPlatformError;
    private boolean               blockDataError;
    private boolean               blockConstraintError;
    private boolean               blockStatementError;
    private boolean               blockSecurityError;

    /**
     * Configured <code>IDBO</code> name.
     */
    private String                name;

    /**
     * Name of the class that extends this <code>AbstractDBO</code>.
     */
    protected String              dboclass                = null;

    /**
     * Statement parameter values, reported when error occurs.
     */
    protected Vector<Object>      currentRowFields;

    /**
     *
     */
    protected String              currentXSLMessage;

    /**
     *
     */
    protected boolean             currCriticalError       = false;

    /**
     *
     */
    protected boolean             onlyXSLErrorMsg         = false;

    /**
     *
     */
    protected boolean             onlyXSLErrorMsgInTrans  = false;

    /**
     *
     */
    protected DHResult            dhr                     = new DHResult();

    /**
     *
     */
    protected boolean             isReturnData            = false;

    /**
     *
     */
    protected long                rowCounter;

    /**
     *
     */
    protected long                rowInsOk;

    /**
     *
     */
    protected long                rowUpdOk;

    /**
     *
     */
    protected long                rowDisc;

    /**
     *
     */
    protected boolean             isInsert                = true;

    /**
     *
     */
    public static final String ROWSET_NAME                = "RowSet";

    /**
     *
     */
    public static final String DATA_NAME                  = "data";

    /**
     *
     */
    public static final String ROW_NAME                   = "row";

    /**
     *
     */
    public static final String COL_NAME                   = "col";

    /**
    *
    */
    public static final String KEY_NAME                   = "key";

    /**
     *
     */
    public static final String COL_UPDATE_NAME            = "col-update";

    public static final String SP_OUT_PARAMS_NAME         = "SPOutputParameters";
    public static final String SP_OUT_PAR_NAME            = "SPOutputParameter";

    /**
     *
     */
    public static final String TYPE_NAME                  = "type";

    /**
    *
    */
   public static final String NULL_NAME                   = "isNull";

    /**
     *
     */
    public static final String NAME_ATTR                  = "name";

    /**
     *
     */
    public static final String ID_NAME                    = "id";

    /**
     *
     */
    public static final String UUID_NAME                  = "uuid";

    /**
     *
     */
    protected static final String STATS_ON_NAME           = "stats-on";

    /**
     *
     */
    protected static final String INCR_DISC_NAME          = "incr-discard-if-no-update";

    /**
     *
     */
    public static final String XSL_MSG_NAME               = "xsl-message";

    /**
     *
     */
    public static final String CRITICAL_ERROR             = "critical-error";

    /**
     *
     */
    protected static final String STATS_INS_MODE          = "insert";

    /**
     *
     */
    protected static final String STATS_UPD_MODE          = "update";

    /**
     *
     */
    protected static final String INCR_DISC_Y_MODE        = "Y";

    /**
     *
     */
    protected static final String INCR_DISC_N_MODE        = "N";

    /**
     *
     */
    public static final String STRING_TYPE                = "string";

    /**
    *
    */
   public static final String NSTRING_TYPE               = "nstring";

    /**
     *
     */
    public static final String LONG_STRING_TYPE           = "long-string";

    /**
    *
    */
   public static final String LONG_NSTRING_TYPE          = "long-nstring";

    /**
     *
     */
    public static final String BASE64_TYPE                = "base64";

    /**
     *
     */
    public static final String BINARY_TYPE                = "binary";

    /**
     *
     */
    public static final String DEFAULT_TYPE               = STRING_TYPE;

    /**
     *
     */
    public static final String TIMESTAMP_TYPE             = "timestamp";

    /**
     *
     */
    public static final String NUMERIC_TYPE               = "numeric";

    /**
     *
     */
    public static final String FLOAT_TYPE                 = "float";

    /**
    *
    */
    public static final String DECIMAL_TYPE               = "decimal";

    /**
     *
     */
    public static final String FORMAT_NAME                = "format";

    /**
     *
     */
    public static final String GRP_SEPARATOR_NAME         = "grouping-separator";

    /**
     *
     */
    public static final String DEC_SEPARATOR_NAME         = "decimal-separator";

    /**
     *
     */
    public static final String DEFAULT_DATE_FORMAT        = "yyyyMMdd HH:mm:ss";

    /**
     *
     */
    public static final String DEFAULT_NUMBER_FORMAT      = "#,##0.###";

    /**
     *
     */
    public static final String DEFAULT_GRP_SEPARATOR      = ".";

    /**
     *
     */
    public static final String DEFAULT_DEC_SEPARATOR      = ",";

    /**
     *
     */
    public static final String DEFAULT_END_LINE           = "\n";

    /**
     *
     */
    public static final String DEFAULT_ENCODING           = "UTF-8";

    /**
     *
     */
    protected String              call_DEFAULT_NUMBER_FORMAT;

    /**
     *
     */
    protected String              call_DEFAULT_GRP_SEPARATOR;

    /**
     *
     */
    protected String              call_DEFAULT_DEC_SEPARATOR;

    /**
     *
     */
    protected SimpleDateFormat    dateFormatter           = null;

    /**
     *
     */
    protected DecimalFormat       numberFormatter         = new DecimalFormat();

    private XMLReader             xr;
    private boolean               executeQuery;

    /**
     *
     */
    protected AbstractDBO()
    {
        this.dboclass = this.getClass().getName();
        this.dboclass = this.dboclass.substring(this.dboclass.lastIndexOf('.') + 1);
        this.dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT, DateUtils.getDefaultLocale());
        this.currentRowFields = new Vector<Object>(10);
        this.statements = new HashMap<String, String>();
        this.uuids = new HashMap<String, String>();
        this.baseProps = new HashMap<String, Object>();
        this.numberFormatter.setRoundingMode(RoundingMode.FLOOR);
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBO#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node config) throws DBOException
    {
        try {
            this.name = XMLConfig.get(config, "@name");
            this.transformation = XMLConfig.get(config, "@transformation");
            this.ignoreInput = XMLConfig.getBoolean(config, "@ignore-input", false);
            this.forcedMode = XMLConfig.get(config, "@force-mode", MODE_CALLER);
            this.jdbcConnectionNameInt = XMLConfig.get(config, "@jdbc-connection-name", "default");
            this.currentJdbcConnectionNameInt = this.jdbcConnectionNameInt;
            this.inputDataName = XMLConfig.get(config, "@input-data", this.name + "-Input");
            this.outputDataName = XMLConfig.get(config, "@output-data", this.name + "-Output");
            logger.debug("Initializing [" + this.dboclass + "] with name [" + this.name + "] and transformation ["
                    + this.transformation + "].");
            this.onlyXSLErrorMsg = XMLConfig.getBoolean(config, "@onlyXSLErrorMsg", false);
            this.onlyXSLErrorMsgInTrans = XMLConfig.getBoolean(config, "@onlyXSLErrorMsgInTrans", false);
            this.isReturnData = XMLConfig.getBoolean(config, "@return-data", false);
            this.executeQuery = XMLConfig.getBoolean(config, "@execute-query", false);
            this.blockPlatformError = XMLConfig.getBoolean(config, "@blockPlatformError", true);
            this.blockDataError = XMLConfig.getBoolean(config, "@blockDataError", true);
            this.blockConstraintError = XMLConfig.getBoolean(config, "@blockConstraintError", true);
            this.blockStatementError = XMLConfig.getBoolean(config, "@blockStatementError", true);
            this.blockSecurityError = XMLConfig.getBoolean(config, "@blockSecurityError", true);

            NodeList nlv = XMLConfig.getNodeList(config, "DHVariables/DHVariable");
            if (nlv != null) {
                for (int i = 0; i < nlv.getLength(); i++) {
                    Node nv = nlv.item(i);
                    this.baseProps.put(XMLConfig.get(nv, "@name"),
                            XMLConfig.get(nv, "@value", XMLConfig.get(nv, ".", "")).trim());
                }
            }

            this.xr = XMLReaderFactory.createXMLReader();

            this.autogenerateKeys = XMLConfig.getBoolean(config, "@autogenerate-keys", false);
            /*if (autogenerateKeys) {
                autogenerateKeyColumns = new HashSet<String>();
                String keyColums = XMLConfig.get(config, "@key-columns");
                for (String col : keyColums.split(",")) {
                    autogenerateKeyColumns.add(col.trim());
                }
            }*/
        }
        catch (XMLConfigException exc) {
            logger.error("Error reading configuration of [" + this.dboclass + "]", exc);
            throw new DBOException("Error reading configuration of [" + this.dboclass + "]", exc);
        }
        catch (SAXException exc) {
            logger.error("Error reading configuration of [" + this.dboclass + "]", exc);
            throw new DBOException("Error reading configuration of [" + this.dboclass + "]", exc);
        }
    }

    /**
     * @param id
     * @throws SAXException
     */
    protected void getStatement(String id) throws SAXException
    {
        if (id == null) {
            id = "0";
        }
        if ((this.sqlStatementInfo == null) || !getCurrentId().equals(id)) {
            try {
                if (this.sqlStatementInfo != null) {
                    this.sqlStatementInfo.close();
                }
                String expandedSQL = PropertiesHandler.expand(this.statements.get(id), this.currentProps, this.internalConn, null);
                logger.debug("expandedSQL stmt: " + expandedSQL);
                Statement statement = this.internalConn.prepareStatement(expandedSQL);
                this.sqlStatementInfo = new StatementInfo(id, expandedSQL, statement);
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
     * @see it.greenvulcano.gvesb.datahandling.IDBO#execute(java.lang.Object,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public void executeIn(Object input, Connection conn, Map<String, Object> props) throws DBOException,
            InterruptedException {
        try {
            prepare();
            logger.debug("Begin execution of DB data read/update through " + this.dboclass);
            this.call_DEFAULT_NUMBER_FORMAT = (String) props.get(FORMAT_NAME);
            if (this.call_DEFAULT_NUMBER_FORMAT == null) {
                this.call_DEFAULT_NUMBER_FORMAT = DEFAULT_NUMBER_FORMAT;
            }
            this.call_DEFAULT_GRP_SEPARATOR = (String) props.get(GRP_SEPARATOR_NAME);
            if (this.call_DEFAULT_GRP_SEPARATOR == null) {
                this.call_DEFAULT_GRP_SEPARATOR = DEFAULT_GRP_SEPARATOR;
            }
            this.call_DEFAULT_DEC_SEPARATOR = (String) props.get(DEC_SEPARATOR_NAME);
            if (this.call_DEFAULT_DEC_SEPARATOR == null) {
                this.call_DEFAULT_DEC_SEPARATOR = DEFAULT_DEC_SEPARATOR;
            }
            this.resultMessage = new StringBuffer();
            this.resultStatus = STATUS_OK;

            this.currentProps = buildProps(props);
            logProps(this.currentProps);

            getInternalConn(conn, this.currentProps);

            if (this.ignoreInput) {
                input = null;
            }
            if (input != null) {
                if (input instanceof Element) {
                    logger.debug("Input is: Element");
                    parseElement((Element) input);
                }
                else if (input instanceof Document) {
                    logger.debug("Input is: Document");
                    Element rootElement = ((Document) input).getDocumentElement();
                    if (rootElement == null) {
                        logger.warn("Document has NO root element!");
                    }
                    else {
                        parseElement(rootElement);
                    }
                }
                else {
                    InputSource xmlSource = null;
                    if (input instanceof InputStream) {
                        logger.debug("Input is: InputStream");
                        xmlSource = new InputSource((InputStream) input);
                    }
                    else if (input instanceof Reader) {
                        logger.debug("Input is: Reader");
                        xmlSource = new InputSource((Reader) input);
                    }
                    else if (input instanceof Source) {
                        logger.debug("Input is: Source");
                        xmlSource = SAXSource.sourceToInputSource((Source) input);
                    }
                    else if (input instanceof byte[]) {
                        logger.debug("Input is: byte[]");
                        xmlSource = new InputSource(new ByteArrayInputStream((byte[]) input));
                    }
                    else if (input instanceof String) {
                        logger.debug("Input is: String");
                        xmlSource = new InputSource(new ByteArrayInputStream(((String) input).getBytes()));
                    }
                    if (xmlSource == null) {
                        throw new DBOException("Cannot convert " + input.getClass() + " to InputSource");
                    }
                    this.xr.setContentHandler(this);
                    this.xr.setErrorHandler(this);
                    this.xr.parse(xmlSource);
                }
            }
            else {
                getStatement(null);
                executeStatement();
            }
            logger.debug("End execution of DB data read/update through " + this.dboclass);
        }
        catch (IOException exc) {
            logger.error("Error on execution of " + this.dboclass + " with name [" + this.name + "]", exc);
            if (this.currentRowFields != null) {
                logger.error("Record parameters:\n" + dumpCurrentRowFields());
            }
            logger.error("SQL Statement Informations:\n" + this.sqlStatementInfo);
            throw new DBOException("Error on execution of " + this.dboclass + " with name [" + this.name + "]: " + exc.getMessage(), exc);
        }
        catch (SAXException exc) {
            logger.error("Error on execution of " + this.dboclass + " with name [" + this.name + "]", exc);
            if (this.currentRowFields != null) {
                logger.error("Record parameters:\n" + dumpCurrentRowFields());
            }
            logger.error("SQL Statement Informations:\n" + this.sqlStatementInfo);
            ThreadUtils.checkInterrupted(exc);
            throw new DBOException("Error on execution of " + this.dboclass + " with name [" + this.name + "]: " + exc.getMessage(), exc);
        }
        catch (Exception exc) {
            logger.error("Error on execution of " + this.dboclass + " with name [" + this.name + "]", exc);
            if (this.currentRowFields != null) {
                logger.error("Record parameters:\n" + dumpCurrentRowFields());
            }
            logger.error("SQL Statement Informations:\n" + this.sqlStatementInfo);
            String msg = "" + exc.getMessage() + " - XSL Message: " + this.currentXSLMessage;
            this.dhr.addDiscardCause(new DiscardCause(this.rowCounter, msg));
            throw new DBOException(msg, exc);
        }
        finally {
            if (this.resultStatus != STATUS_OK) {
                logger.warn("Partial insert for service " + this.serviceName + ":" + this.resultMessage.toString() + ".");
                logger.warn("Elaboration result for service '" + this.serviceName + "':\nrecord insert  \t[" + this.rowInsOk
                        + "]\nrecord updated\t[" + this.rowUpdOk + "]\nrecord discarded  \t["
                        + Long.toString(this.rowCounter - (this.rowInsOk + this.rowUpdOk)) + "]\nrecord total  \t["
                        + Long.toString(this.rowInsOk + this.rowUpdOk + this.rowDisc) + "].");

            }
            else {
                logger.debug("Elaboration result for service '" + this.serviceName + "':\nrecord insert  \t[" + this.rowInsOk
                        + "]\nrecord updated\t[" + this.rowUpdOk + "]\nrecord total  \t["
                        + Long.toString(this.rowInsOk + this.rowUpdOk + this.rowDisc) + "].");
            }

            this.dhr.setTotal(this.rowInsOk + this.rowUpdOk + this.rowDisc);
            this.dhr.setInsert(this.rowInsOk);
            this.dhr.setUpdate(this.rowUpdOk);
            this.dhr.setDiscard(this.rowDisc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBO#executeOut(java.sql.Connection, java.util.Map)
     */
    @Override
    public Object executeOut(Connection conn, Map<String, Object> props) throws DBOException,
            InterruptedException {
        try {
            prepare();
            logger.debug("Begin execution of DB data read/update through " + this.dboclass);
            this.call_DEFAULT_NUMBER_FORMAT = (String) props.get(FORMAT_NAME);
            if (this.call_DEFAULT_NUMBER_FORMAT == null) {
                this.call_DEFAULT_NUMBER_FORMAT = DEFAULT_NUMBER_FORMAT;
            }
            this.call_DEFAULT_GRP_SEPARATOR = (String) props.get(GRP_SEPARATOR_NAME);
            if (this.call_DEFAULT_GRP_SEPARATOR == null) {
                this.call_DEFAULT_GRP_SEPARATOR = DEFAULT_GRP_SEPARATOR;
            }
            this.call_DEFAULT_DEC_SEPARATOR = (String) props.get(DEC_SEPARATOR_NAME);
            if (this.call_DEFAULT_DEC_SEPARATOR == null) {
                this.call_DEFAULT_DEC_SEPARATOR = DEFAULT_DEC_SEPARATOR;
            }

            this.currentProps = buildProps(props);
            logProps(this.currentProps);

            getInternalConn(conn, this.currentProps);

            getStatement(null);
            executeStatement();
            logger.debug("End execution of DB data read/update through " + this.dboclass);
            return null;
        }
        catch (Exception exc) {
            logger.error("Error on execution of " + this.dboclass + " with name [" + this.name + "]", exc);
            logger.error("SQL Statement Informations:\n" + this.sqlStatementInfo);
            throw new DBOException("Error on execution of " + this.dboclass + " with name [" + this.name + "]", exc);
        }
        finally {
            this.dhr.setTotal(this.rowCounter);
            this.dhr.setRead(this.rowCounter);
            this.dhr.setInsert(this.rowInsOk);
            this.dhr.setUpdate(this.rowUpdOk);
            this.dhr.setDiscard(this.rowCounter - (this.rowInsOk + this.rowUpdOk));
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBO#executeInOut(java.lang.Object,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public Object executeInOut(Object dataIn, Connection conn, Map<String, Object> props)
            throws DBOException, InterruptedException {
        prepare();
        throw new DBOException("Unsupported method - DBOxxx::executeInOut(Object, Connection, Map)");
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBO#setTransacted(boolean)
     */
    @Override
    public void setTransacted(boolean transacted)
    {
        this.transacted = transacted;
    }

    /**
     * @return if should be transacted
     */
    public boolean isTransacted()
    {
        return this.transacted;
    }

    /**
     * @param isXA
     */
    public void setXA(boolean isXA)
    {
        this.isXA = isXA;
    }

    /**
     * @return if should be an XA transaction
     */
    public boolean isXA()
    {
        return this.isXA;
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBO#getName()
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBO#getTransformation()
     */
    @Override
    public String getTransformation()
    {
        return this.transformation;
    }

    /**
     *
     */
    protected void prepare()
    {
        this.currentJdbcConnectionNameInt = this.jdbcConnectionNameInt;
        this.internalConn = null;
        this.dhr.reset();
        resetRowCounter();
        if (this.autogenerateKeys) {
            this.generatedKeys = new HashMap<String, Object>();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBO#cleanup()
     */
    @Override
    public void cleanup()
    {
        this.dhr.reset();
        this.uuids.clear();
        this.currentRowFields.clear();
        if (this.sqlStatementInfo != null) {
            try {
                this.sqlStatementInfo.close();
            }
            catch (Exception exc) {
                // do nothing
            }
            this.sqlStatementInfo = null;
        }
        if (!this.currentJdbcConnectionNameInt.equals("default")) {
            try {
                JDBCConnectionBuilder.releaseConnection(this.currentJdbcConnectionNameInt, this.internalConn);
                this.currentJdbcConnectionNameInt = this.jdbcConnectionNameInt;
                this.internalConn = null;
            }
            catch (Exception exc) {
                // do nothing
            }
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBO#destroy()
     */
    @Override
    public void destroy()
    {
        cleanup();
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBO#setServiceName(java.lang.String)
     */
    @Override
    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBO#getExecutionResult()
     */
    @Override
    public DHResult getExecutionResult()
    {
        return this.dhr;
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBO#isReturnData()
     */
    @Override
    public boolean isReturnData()
    {
        return this.isReturnData;
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBO#getForcedMode()
     */
    @Override
    public String getForcedMode()
    {
        return this.forcedMode;
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBO#getInputDataName()
     */
    @Override
    public String getInputDataName()
    {
        return this.inputDataName;
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBO#getOutputDataName()
     */
    @Override
    public String getOutputDataName()
    {
        return this.outputDataName;
    }

    /**
     * Returns the name of this IDBO object.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return this.name;
    }

    /**
     * @return the row fields dump
     */
    protected String dumpCurrentRowFields()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.currentRowFields.size(); i++) {
            sb.append("Field(").append(i + 1).append(") value: [").append(this.currentRowFields.elementAt(i)).append("]\n");
        }
        sb.append("XSL Message: ").append(this.currentXSLMessage).append("\n\n");
        return sb.toString();
    }

    /**
     *
     */
    protected void resetRowCounter()
    {
        this.rowCounter = 0;
        this.rowInsOk = 0;
        this.rowUpdOk = 0;
        this.rowDisc = 0;
    }

    /**
     * @throws SAXException
     */
    protected void executeStatement() throws SAXException
    {
        try {
            ThreadUtils.checkInterrupted(getClass().getSimpleName(), this.name, logger);
            int actualOk = 0;
            Statement sqlStatement = this.sqlStatementInfo.getStatement();
            if (sqlStatement != null) {
                if (this.executeQuery) {
                    ((PreparedStatement) sqlStatement).executeQuery();
                    actualOk++;
                }
                else {
                    actualOk = ((PreparedStatement) sqlStatement).executeUpdate();
                    if (this.readGeneratedKey) {
                        ResultSet rs = sqlStatement.getGeneratedKeys();
                        if (rs.next()) {
                            /*ResultSetMetaData rsm = rs.getMetaData();

                            for (int i = 1; i <= rsm.getColumnCount(); i++) {
                                String cName = rsm.getColumnName(i);
                                generatedKeys.put(cName, rs.getObject(cName));
                            }*/

                            // handle only one key field
                            this.generatedKeys.put(GENERATED_KEY_ID + this.generatedKeyID, rs.getObject(1));
                            logger.debug("Key generated on row " + this.rowCounter + ": " + this.generatedKeys);
                        }
                        /*
                        if (rs.next()) {
                            for (String col : autogenerateKeyColumns) {
                                generatedKeys.put(col, rs.getObject(col));
                            }
                            logger.debug("Key generated on row " + rowCounter + ": " + generatedKeys);
                        }*/
                    }
                    if (this.resetGeneratedKeyID != null) {
                        this.generatedKeys.remove(GENERATED_KEY_ID + this.resetGeneratedKeyID);
                    }
                }
            }
            // check the chance to force the insert when it's a DBOUpdate.
            if (this.isInsert && this.statsOnInsert) {
                this.rowInsOk += actualOk;
            }
            else {
                if (actualOk > 0) {
                    this.rowUpdOk += actualOk;
                }
                else if ((actualOk == 0) && this.incrDiscIfUpdKO) {
                    this.rowDisc++;
                    this.dhr.addDiscardCause(new DiscardCause(this.rowCounter, NO_RECORD_TO_UPDATE_MSG));
                }
            }
        }
        catch (SQLException exc) {
            this.rowDisc++;
            if (this.transacted) {
                this.resultStatus = STATUS_KO;
                logger.error("SQL Statement Informations:\n" + this.sqlStatementInfo);
                logger.error("Record parameters:\n" + dumpCurrentRowFields());
                if (this.onlyXSLErrorMsgInTrans && (this.currentXSLMessage != null)) {
                    logger.error("SQLException configured as blocking error for the IDBO '" + this.serviceName + "' on row "
                            + Long.toString(this.rowCounter) + ".", exc);
                    throw new SAXException(new DBOException(this.currentXSLMessage));
                }
                throw new SAXException(new DBOException("SQLException error on row " + this.rowCounter + ": "
                        + exc.getMessage(), exc));
            }

            OracleError oraerr = OracleExceptionHandler.handleSQLException(exc);
            if (isBlockingError(oraerr.getErrorType())) {
                this.resultStatus = STATUS_KO;
                logger.error("SQL Statement Informations:\n" + this.sqlStatementInfo);
                logger.error("Record parameters:\n" + dumpCurrentRowFields());
                logger.error("SQLException configured as blocking error for the IDBO '" + this.serviceName + "' on row "
                        + Long.toString(this.rowCounter) + ".", exc);
                throw new SAXException(new DBOException("SQLException configured as blocking error class on row "
                        + this.rowCounter + ": " + exc.getMessage(), exc));
            }

            // adding the DiscardCause to the DHR...
            String msg = "";
            if (this.onlyXSLErrorMsg && (this.currentXSLMessage != null)) {
                msg += this.currentXSLMessage;
            }
            else {
                msg += exc + " - XSL Message: " + this.currentXSLMessage;
            }
            this.dhr.addDiscardCause(new DiscardCause(this.rowCounter, msg));

            this.resultMessage.append("SQLException error on row ").append(this.rowCounter).append(": ").append(exc.getMessage());
            this.resultMessage.append("SQL Statement Informations:\n").append(this.sqlStatementInfo);
            this.resultMessage.append("Record parameters:\n").append(dumpCurrentRowFields());
            this.resultStatus = STATUS_PARTIAL;
        }
        catch (InterruptedException exc) {
            logger.error("DBO[" + this.name + "] interrupted", exc);
            throw new SAXException("DBO[" + this.name + "] interrupted", exc);
        }
        finally {
            this.rowCounter++;
        }
    }

    /**
     * @param errorType
     * @return if error is blocking
     */
    protected boolean isBlockingError(int errorType)
    {
        switch (errorType) {
            case OracleError.PLATFORM_ERROR :
                return this.blockPlatformError;
            case OracleError.DATA_ERROR :
                return this.blockDataError;
            case OracleError.CONSTRAINT_ERROR :
                return this.blockConstraintError;
            case OracleError.STATEMENT_ERROR :
                return this.blockStatementError;
            case OracleError.SECURITY_ERROR :
                return this.blockSecurityError;
        }
        return true;
    }

    /**
     * @return the row counter
     */
    protected long getRowCounter()
    {
        return this.rowCounter;
    }

    /**
     * @return the current IDBO properties
     */
    protected Map<String, Object> getCurrentProps()
    {
        return this.currentProps;
    }

    /**
     * @return the internal connection
     */
    protected Connection getInternalConn()
    {
        return this.internalConn;
    }

    /**
     * @param conn
     * @return the internal connection
     * @throws Exception
     */
    protected Connection getInternalConn(Connection conn, Map<String, Object> props) throws Exception
    {
        if (this.internalConn == null) {
            this.internalConn = conn;
            if (!this.currentJdbcConnectionNameInt.equals("default")) {
            	this.currentJdbcConnectionNameInt = PropertiesHandler.expand(this.currentJdbcConnectionNameInt, props);
                logger.info("Overwriting default Connection with: " + this.currentJdbcConnectionNameInt);
                long startConn = System.currentTimeMillis();
                this.internalConn = JDBCConnectionBuilder.getConnection(this.currentJdbcConnectionNameInt);
                this.timeConn = System.currentTimeMillis() - startConn;
            }
        }

        return this.internalConn;
    }

    @Override
    public long getTimeConn() {
    	return this.timeConn;
    }

    /**
     * @param props
     * @return the whole properties map
     */
    protected Map<String, Object> buildProps(Map<String, Object> props)
    {
        Map<String, Object> allProps = new HashMap<String, Object>(this.baseProps);
        allProps.putAll(props);
        return allProps;
    }

    /**
     * @param props
     */
    protected void logProps(Map<String, Object> props)
    {
        logger.debug("Params: " + props.toString());
    }

    /**
     * @return the connection name
     */
    public String getJdbcConnectionName()
    {
        if (!this.currentJdbcConnectionNameInt.equals("default")) {
            return this.currentJdbcConnectionNameInt;
        }
        return this.jdbcConnectionName;
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBO#setJdbcConnectionName(java.lang.String)
     */
    @Override
    public void setJdbcConnectionName(String jdbcConnectionName)
    {
        this.jdbcConnectionName = jdbcConnectionName;
    }

    /**
     * @return the current id
     */
    protected String getCurrentId()
    {
        return this.currentId;
    }

    /**
     * @param currentId
     */
    protected void setCurrentId(String currentId)
    {
        this.currentId = currentId;
    }

    /**
     * @param handler
     * @param element
     * @throws SAXException
     */
    private void parseElement(Element element) throws SAXException
    {
        String elementLocalPart = getLocalName(element);
        String elementNS = getNamespaceURI(element);
        if (ROW_NAME.equals(elementLocalPart) || COL_NAME.equals(elementLocalPart)
                || COL_UPDATE_NAME.equals(elementLocalPart)
                || SP_OUT_PARAMS_NAME.equals(elementLocalPart)
                || SP_OUT_PAR_NAME.equals(elementLocalPart)) {
            QName qName = new QName(elementNS, elementLocalPart);
            AttributesImpl attrs = new AttributesImpl();
            NamedNodeMap attributes = element.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attr = attributes.item(i);
                String attrNS = getNamespaceURI(attr);
                String attrLocalPart = getLocalName(attr);
                QName attrQName = new QName(attrNS, attrLocalPart);
                attrs.addAttribute(attrNS, attrLocalPart, attrQName.toString(), "CDATA", attr.getNodeValue());
            }

            startElement(elementNS, elementLocalPart, qName.toString(), attrs);
            if (ROW_NAME.equals(elementLocalPart) || SP_OUT_PARAMS_NAME.equals(elementLocalPart)) {
                NodeList childNodes = element.getChildNodes();
                if (childNodes != null) {
                    for (int i = 0; i < childNodes.getLength(); i++) {
                        Node currentNode = childNodes.item(i);
                        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                            parseElement((Element) currentNode);
                        }
                    }
                }
            }
            else {
                String text = getNodeValue(element);
                if (text != null) {
                    characters(text.toCharArray(), 0, text.length());
                }
            }
            endElement(elementNS, elementLocalPart, qName.toString());
        }
        else {
            NodeList childNodes = element.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node currentNode = childNodes.item(i);
                    if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                        parseElement((Element) currentNode);
                    }
                }
            }
        }
    }

    /**
     * @param element
     * @return
     */
    private static String getLocalName(Node node)
    {
        if (node == null) {
            return "";
        }
        String name = node.getLocalName();
        if (name == null) {
            name = node.getNodeName();
        }
        if (name == null) {
            name = "";
        }
        return name;
    }

    /**
     * @param element
     * @return
     */
    private static String getNamespaceURI(Node node)
    {
        if (node == null) {
            return "";
        }
        String name = node.getNamespaceURI();
        if (name == null) {
            name = "";
        }
        return name;
    }

    private static String getNodeValue(Node node)
    {
        Node child = node.getFirstChild();
        if (child != null) {
            StringBuilder buf = new StringBuilder();
            while (child != null) {
                String val = getNodeValue(child);
                if (val != null) {
                    buf.append(val);
                }
                child = child.getNextSibling();
            }
            return buf.toString();
        }
        return node.getNodeValue();
    }

}

/*
 * Copyright (c) 2009-2016 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.datahandling.mongodb.dbo;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.datahandling.DBOException;
import it.greenvulcano.gvesb.datahandling.DHResult;
import it.greenvulcano.gvesb.datahandling.mongodb.IDBOMongo;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.txt.DateUtils;

import java.io.OutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;

import com.mongodb.MongoClient;

/**
 * Abstract class for MongoDB dbo.
 * 
 * @version 3.5.0 Mar 30, 2015
 * @author GreenVulcano Developer Team
 */
public abstract class AbstractMongoDBO extends DefaultHandler implements IDBOMongo
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
	protected int                 resultStatus            = STATUS_OK;

	/**
	 *
	 */
	protected StringBuffer        resultMessage           = null;

	/**
	 * <i>logger</i> private instance.
	 */
	private static final Logger   logger                  = GVLogger.getLogger(AbstractMongoDBO.class);

	/**
	 * Needed transformation to create meta-data from initial file.
	 */
	private String                transformation;

	/**
	 *
	 */
	protected String              serviceName;

	/**
	 *
	 */
	//protected StatementMongoInfo       mongoStatementInfo;
	protected MongoStatementInfo mongoStatementInfo;

	protected Set<String>         jsonStatementsParams     = new HashSet<String>();

	/**
	 *
	 */
	protected boolean             statsOnInsert           = true;

	/**
	 *
	 */
	protected boolean             incrDiscIfUpdKO         = true;

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
	 * MongoDB Statements cache found in configuration identified by an ID.
	 */
	protected Map<String, IDBOOperation> statements;

	/**
	 * Unique identifiers cache.
	 */
	protected Map<String, String> uuids;


	/**
	 * Internal connection to MongoDB to prepare the correct statement.
	 */
	private MongoClient            internalConn;
	//private Connection            internalConn; // java.sql.Connection

	/**
	 * Properties to substitute in the statement meta-data bound to single
	 * execution.
	 */
	private Map<String, Object>   currentProps;

	/**
	 * Properties eventually configured to overwrite in the service call.
	 */
	private Map<String, Object>   baseProps;

	//private String                currentId               = "0";

	protected Pattern             namedParPattern         = Pattern.compile("(\\:[a-zA-Z][a-zA-Z0-9_]*)");

	/**
	 * Configured <code>IDBOMongo</code> name.
	 */
	private String                name;

	/**
	 * Name of the class that extends this <code>AbstractMongoDBO</code>.
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
	protected SimpleDateFormat    dateFormatter           = null;

	/**
	 *
	 */
	protected DecimalFormat       numberFormatter         = new DecimalFormat();

//	private XMLReader             xr;
//	private boolean               executeQuery;


	/**
	 *
	 */
	protected AbstractMongoDBO()
	{
		dboclass = this.getClass().getName();
		dboclass = dboclass.substring(dboclass.lastIndexOf('.') + 1);
		dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT, DateUtils.getDefaultLocale());
		currentRowFields = new Vector<Object>(10);
		uuids = new HashMap<String, String>();
		statements = new HashMap<String, IDBOOperation>();
		baseProps = new HashMap<String, Object>();
		numberFormatter.setRoundingMode(RoundingMode.FLOOR);
	}

	/**
	 * @see it.greenvulcano.gvesb.datahandling.IDBO#init(org.w3c.dom.Node)
	 */
	@Override
	public void init(Node config) throws DBOException
	{
		try {
			name = XMLConfig.get(config, "@name");
			//TODO: transformation = XMLConfig.get(config, "@transformation");
			ignoreInput = XMLConfig.getBoolean(config, "@ignore-input", false);

			inputDataName = XMLConfig.get(config, "@input-data", name + "-Input");
			outputDataName = XMLConfig.get(config, "@output-data", name + "-Output");
			//            logger.debug("Initializing [" + dboclass + "] with name [" + name + "] and transformation ["
			//                    + transformation + "].");
			logger.debug("Initializing [" + dboclass + "] with name [" + name + "]");
			isReturnData = XMLConfig.getBoolean(config, "@return-data", false);
			//executeQuery = XMLConfig.getBoolean(config, "@execute-query", false);

			NodeList nlv = XMLConfig.getNodeList(config, "DHVariables/DHVariable");
			if (nlv != null) {
				for (int i = 0; i < nlv.getLength(); i++) {
					Node nv = nlv.item(i);
					baseProps.put(XMLConfig.get(nv, "@name"),
							XMLConfig.get(nv, "@value", XMLConfig.get(nv, ".", "")).trim());
				}
			}
		}
		catch (XMLConfigException exc) {
			logger.error("Error reading configuration of [" + dboclass + "]", exc);
			throw new DBOException("Error reading configuration of [" + dboclass + "]", exc);
		}
	}


	/*
	 * @see it.greenvulcano.gvesb.datahandling.mongodb.IDBOMongo#execute(java.lang.Object, java.io.OutputStream, com.mongodb.MongoClient, java.util.Map)
	 */
	@Override
	public void execute(Object dataIn, OutputStream dataOut, MongoClient conn, Map<String, Object> props)
			throws DBOException, InterruptedException {
		prepare();
		throw new DBOException("Unsupported method - DBOxxx::execute(InputStream, OutputStream, Connection, Map)");
	}

	/**
	 * @see it.greenvulcano.gvesb.datahandling.IDBO#getName()
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * @see it.greenvulcano.gvesb.datahandling.IDBO#getTransformation()
	 */
	@Override
	public String getTransformation()
	{
		return transformation;
	}

	/**
	 *
	 */
	protected void prepare()
	{
		internalConn = null;
		dhr.reset();
		resetRowCounter();
	}

	/**
	 * @see it.greenvulcano.gvesb.datahandling.IDBO#cleanup()
	 */
	@Override
	public void cleanup()
	{
		uuids.clear();
		currentRowFields.clear();
		if (mongoStatementInfo != null) {
			mongoStatementInfo = null;
		}
		if (internalConn != null) {
			try {
				internalConn.close();
				internalConn = null;
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
		return dhr;
	}

	/**
	 * @see it.greenvulcano.gvesb.datahandling.IDBO#isReturnData()
	 */
	@Override
	public boolean isReturnData()
	{
		return isReturnData;
	}

	/**
	 * @see it.greenvulcano.gvesb.datahandling.IDBO#getForcedMode()
	 */
	@Override
	public String getForcedMode()
	{
		return forcedMode;
	}

	/**
	 * @see it.greenvulcano.gvesb.datahandling.IDBO#getInputDataName()
	 */
	@Override
	public String getInputDataName()
	{
		return inputDataName;
	}

	/**
	 * @see it.greenvulcano.gvesb.datahandling.IDBO#getOutputDataName()
	 */
	@Override
	public String getOutputDataName()
	{
		return outputDataName;
	}

	/**
	 * Returns the name of this IDBO object.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return name;
	}

	/**
	 *
	 */
	protected void resetRowCounter()
	{
		rowCounter = 0;
		rowInsOk = 0;
		rowUpdOk = 0;
		rowDisc = 0;
	}

	/**
	 * @param errorType
	 * @return if error is blocking
	 */
	protected boolean isBlockingError(int errorType)
	{
		return true;
	}

	/**
	 * @return the row counter
	 */
	protected long getRowCounter()
	{
		return rowCounter;
	}

	/**
	 * @return the current IDBO properties
	 */
	protected Map<String, Object> getCurrentProps()
	{
		return currentProps;
	}


	/**
	 * @param conn
	 * @return the internal connection
	 * @throws Exception
	 */
	protected MongoClient getInternalConn(MongoClient conn) throws Exception
	{
		if (internalConn == null) {
			internalConn = conn;
		}

		return internalConn;
	}

	/**
	 * @param props
	 * @return the whole properties map
	 */
	protected Map<String, Object> buildProps(Map<String, Object> props)
	{
		Map<String, Object> allProps = new HashMap<String, Object>(baseProps);
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

}

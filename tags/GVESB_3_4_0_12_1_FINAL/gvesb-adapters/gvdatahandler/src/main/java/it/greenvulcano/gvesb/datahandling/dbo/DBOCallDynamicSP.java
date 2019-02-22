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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.datahandling.DBOException;
import it.greenvulcano.gvesb.datahandling.dbo.utils.ResultSetTransformer;
import it.greenvulcano.gvesb.datahandling.dbo.utils.RowSetBuilder;
import it.greenvulcano.gvesb.datahandling.utils.DiscardCause;
import it.greenvulcano.gvesb.datahandling.utils.ParameterType;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleError;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

/**
 * IDBO Class specialized to parse the input RowSet document and in
 * calling stored procedures in the DB.
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 */
public class DBOCallDynamicSP extends AbstractDBO
{
    /**
     * @version 3.0.0 Mar 30, 2010
     * @author GreenVulcano Developer Team
     */
    public class SPCallDescriptor
    {
        private final String ROWSET_NAME = "RowSet";

        private final String DATA_NAME   = "data";

        /**
         * @version 3.0.0 Mar 30, 2010
         * @author GreenVulcano Developer Team
         *
         */
        public class SPOutputParam
        {
            /**
             * Type of parameter.
             */
            private String  dbType             = null;

            /**
             * Type of parameter.
             */
            private String  javaType           = null;

            /**
             * Type of parameter.
             */
            private String  javaTypeFormat     = null;

            /**
             * Position of parameter in the SQL statement.
             */
            private int     position           = 0;

            /**
             * Precision of numeric parameter.
             */
            private int     precision          = 0;

            /**
             * Return the value in properties map.
             */
            private boolean returnInProperties = false;

            /**
             * Type property name.
             */
            private String  propName           = null;

            private boolean returnInUUID;

            private String  paramName;

            /**
             * SPOutputParam Constructor
             *
             * @param node
             *        the configuration node
             * @throws DBOException
             *         if an error occurred
             */
            public SPOutputParam(Node node) throws DBOException
            {
                try {
                    this.dbType = XMLConfig.get(node, "@db-type");
                    this.javaType = XMLConfig.get(node, "@java-type");
                    this.javaTypeFormat = XMLConfig.get(node, "@java-type-format", "");
                    this.position = XMLConfig.getInteger(node, "@position");
                    this.precision = XMLConfig.getInteger(node, "@precision", 0);
                    this.returnInProperties = XMLConfig.getBoolean(node, "@return-in-prop", false);
                    this.returnInUUID = XMLConfig.getBoolean(node, "@return-in-uuid", false);
                    this.propName = XMLConfig.get(node, "@prop-name", "" + this.position);
                    this.paramName = XMLConfig.get(node, "@param-name", "");
                }
                catch (XMLConfigException exc) {
                    throw new DBOException("Error configuring the output parameter: " + exc, exc);
                }
                catch (Throwable exc) {
                    throw new DBOException("Error initializing the output parameter: " + exc, exc);
                }
            }

            public SPOutputParam(String uri, Attributes attributes) throws DBOException {
            	try {
            		this.dbType = attributes.getValue(uri,  "db-type");
            		this.javaType = attributes.getValue(uri, "java-type");
            		this.javaTypeFormat = attributes.getIndex(uri,  "java-type-format") != -1 ? attributes.getValue(uri,  "java-type-format") : "";
            		this.position = Integer.parseInt(attributes.getValue(uri,  "position"));
            		this.precision = attributes.getIndex(uri,  "precision") != -1 ? Integer.parseInt(attributes.getValue(uri,  "precision")) : 0;
            		this.returnInProperties = Boolean.parseBoolean(attributes.getValue(uri,  "return-in-prop"));
            		this.returnInUUID = Boolean.parseBoolean(attributes.getValue(uri,  "return-in-uuid"));
            		this.propName = attributes.getIndex(uri,  "prop-name") != -1 ? attributes.getValue(uri,  "prop-name") : "" + this.position;
            		this.paramName = attributes.getIndex(uri,  "param-name") != -1 ? attributes.getValue(uri,  "param-name") : "";
                }
                catch (Throwable exc) {
                    throw new DBOException("Error initializing the output parameter: " + exc, exc);
                }
            }

            /**
             * Set the dbType.<br/>
             * <br/>
             *
             * @param dbType
             *        The value to set.
             */
            public void setDBType(String dbType)
            {
                this.dbType = dbType;
            }

            /**
             * Set the javaType.<br/>
             * <br/>
             *
             * @param javaType
             *        The value to set.
             */
            public void setJavaType(String javaType)
            {
                this.javaType = javaType;
            }

            /**
             * Set the javaTypeFormat.<br/>
             * <br/>
             *
             * @param javaTypeFormat
             *        The value to set.
             */
            public void setJavaTypeFormat(String javaTypeFormat)
            {
                this.javaTypeFormat = javaTypeFormat;
            }

            /**
             * Set the position of parameter into the SQL Statement.<br/>
             * <br/>
             *
             * @param position
             *        The value to set.
             */
            public void setPosition(int position)
            {
                this.position = position;
            }

            /**
             * Set the numeric precision of parameter.<br/>
             * <br/>
             *
             * @param precision
             *        The value to set.
             */
            public void setPrecision(int precision)
            {
                this.precision = precision;
            }

            /**
             * Get the dbtype attribute.<br/>
             * <br/>
             *
             * @return the dbtype
             */
            public String getDBType()
            {
                return this.dbType;
            }

            /**
             * Get the java type attribute.<br/>
             * <br/>
             *
             * @return the java type
             */
            public String getJavaType()
            {
                return this.javaType;
            }

            /**
             * Get the java type format attribute.<br/>
             * <br/>
             *
             * @return the java type format
             */
            public String getJavaTypeFormat()
            {
                return this.javaTypeFormat;
            }

            /**
             * Get the precision of numeric parameter.<br/>
             * <br/>
             *
             * @return the precision
             */
            public int getPrecision()
            {
                return this.precision;
            }

            /**
             * Get the position of parameter into the SQL Statement.<br/>
             * <br/>
             *
             * @return the position
             */
            public int getPosition()
            {
                return this.position;
            }

            /**
             * @return if return object should be set as property
             */
            public boolean isReturnInProperties()
            {
                return this.returnInProperties;
            }

            /**
             * @return if return object should be set associated to UUID
             *         specified as row attribute.
             */
            public boolean isReturnInUUID()
            {
                return this.returnInUUID;
            }

            /**
             * Get the propName attribute.<br/>
             * <br/>
             *
             * @return the propName
             */
            public String getPropName()
            {
                return this.propName;
            }

            /**
             * @param paramName
             *        the paramName to set
             */
            public void setParamName(String paramName)
            {
                this.paramName = paramName;
            }

            /**
             * @return the paramName
             */
            public String getParamName()
            {
                return this.paramName;
            }

        }

        private final List<SPOutputParam> spOutputParams = new ArrayList<SPOutputParam>();
        private String                    statement      = "";

        private boolean                   namedParameterMode;

        private final RowSetBuilder             rowSetBuilder      = null;
        private String                    rowSetBuilderType  = null;


		/**
         * @param node
         * @throws DBOException
         */
        public SPCallDescriptor(Node node) throws DBOException
        {
            try {
                this.statement = XMLConfig.get(node, ".", "");
                this.namedParameterMode = XMLConfig.getBoolean(node, "@named-parameter-mode", false);
                this.rowSetBuilderType = XMLConfig.get(node, "../@rowset-builder", "standard");

                if (this.statement.equals("")) {
                    throw new DBOException("Empty/misconfigured statements list for stored procedure call descriptor");
                }
            }
            catch (DBOException exc) {
                throw exc;
            }
            catch (Exception exc) {
                throw new DBOException("Error initializing the stored procedure call descriptor", exc);
            }
        }

        public void addSpOutputParam(String uri, Attributes attributes) throws DBOException {
        	this.spOutputParams.add(new SPOutputParam(uri, attributes));
        }

        /**
         * @return the statement
         */
        public String getStatement()
        {
            return this.statement;
        }

        public String getRowSetBuilderType() {
			return this.rowSetBuilderType;
		}

		public void reset() {
			this.spOutputParams.clear();
		}

        /**
         * Specify the output parameter from stored procedure
         *
         * @param callStmt
         *        the statement
         * @throws DBOException
         *         if an error occurred
         */
        public void specifyOutputParameter(CallableStatement callStmt) throws DBOException
        {
            try {
                for (int i = 0; i < this.spOutputParams.size(); i++) {
                    SPOutputParam outp = this.spOutputParams.get(i);
                    String type = outp.getDBType().trim();
                    int iPos = outp.getPosition();
                    int iPrec = outp.getPrecision();
                    logger.debug("Parameter Output[" + (i + 1) + "] Type [" + type + "] Position [" + iPos
                            + "] Precision [" + iPrec + "]...");

                    if (type.equalsIgnoreCase(ParameterType.ORACLE_STRING)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.VARCHAR);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_INT)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.INTEGER);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_LONG)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.BIGINT);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_NUM)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.NUMERIC, iPrec);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_DATE)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.DATE);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_LONG_RAW)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.LONGVARBINARY);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_CLOB)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.CLOB);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_BLOB)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.BLOB);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_CURSOR)) {
                        callStmt.registerOutParameter(iPos, -10); // OracleTypes.CURSOR
                    }
                    else {
                        logger.error("specifyOutputParameter - "
                                + "Error while registring parameters for CallableStatement: "
                                + "parameter type not supported " + type);

                        throw new DBOException(
                                "Error while registring output parameters for CallableStatement: parameter type not supported "
                                        + type);
                    }
                }
            }
            catch (DBOException exc) {
                throw exc;
            }
            catch (SQLException exc) {
                throw new DBOException("Error while registering output parameters: " + exc, exc);
            }
        }

        /**
         * Specify the output parameter from store procedure
         *
         * @param callStmt
         *        the statement
         * @param props
         * @throws DBOException
         *         if an error occurred
         */
        public void setOutputParameterValuesInMap(CallableStatement callStmt, Map<String, Object> props)
                throws DBOException
        {
            try {
                for (int i = 0; i < this.spOutputParams.size(); i++) {
                    SPOutputParam outp = this.spOutputParams.get(i);
                    if (outp.isReturnInProperties() || outp.isReturnInUUID()) {
                        String dbType = outp.getDBType().trim();
                        String javaType = outp.getJavaType().trim();
                        String propName = outp.getPropName();
                        int iPos = outp.getPosition();
                        String paramName = outp.getParamName();
                        Object value = null;

                        if (javaType.equalsIgnoreCase(ParameterType.JAVA_STRING)) {
                            if (dbType.equalsIgnoreCase(ParameterType.ORACLE_DATE)) {
                                String format = outp.getJavaTypeFormat();
                                if (format.equals("")) {
                                    format = DEFAULT_DATE_FORMAT;
                                }
                                Timestamp ts = this.namedParameterMode
                                        ? callStmt.getTimestamp(paramName)
                                        : callStmt.getTimestamp(iPos);
                                value = DateUtils.dateToString(new Date(ts.getTime()), format);
                            }
                            else if (dbType.equalsIgnoreCase(ParameterType.ORACLE_CLOB)) {
                            	Clob clob = this.namedParameterMode
                                        ? callStmt.getClob(paramName)
                                        : callStmt.getClob(iPos);
                                if (clob != null) {
	                                InputStream is = clob.getAsciiStream();
	                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                                IOUtils.copy(is, baos);
	                                is.close();
	                                try {
	                                	value = new String(baos.toByteArray(), 0, (int) clob.length());
	                                }
	                                catch (SQLFeatureNotSupportedException exc) {
	                                	value = baos.toString();
	                                }
	                            }
                                else {
                                	value = "";
                                }
                            }
                            else if (dbType.equalsIgnoreCase(ParameterType.ORACLE_BLOB)) {
                            	Blob blob = this.namedParameterMode
                                        ? callStmt.getBlob(paramName)
                                        : callStmt.getBlob(iPos);
                            	if (blob != null) {
	                                InputStream is = blob.getBinaryStream();
	                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                                IOUtils.copy(is, baos);
	                                is.close();
	                                try {
	                                    byte[] buffer = Arrays.copyOf(baos.toByteArray(),
	                                            (int) blob.length());
	                                    value = new String(Base64.encodeBase64(buffer));
	                                }
	                                catch (SQLFeatureNotSupportedException exc) {
	                                	value = new String(Base64.encodeBase64(baos.toByteArray()));
	                                }
	                            }
                            	else {
                            		value = "";
                            	}
                            }
                            else {
                                value = this.namedParameterMode ? callStmt.getString(paramName) : callStmt.getString(iPos);
                            }
                        }
                        else if (javaType.equalsIgnoreCase(ParameterType.JAVA_INT)) {
                            int v = this.namedParameterMode ? callStmt.getInt(paramName) : callStmt.getInt(iPos);
                            value = new Integer(v);
                        }
                        else if (javaType.equalsIgnoreCase(ParameterType.JAVA_LONG)) {
                            long v = this.namedParameterMode ? callStmt.getLong(paramName) : callStmt.getLong(iPos);
                            value = new Long(v);
                        }
                        else if (javaType.equalsIgnoreCase(ParameterType.JAVA_DATE)) {
                            Timestamp ts = this.namedParameterMode
                                    ? callStmt.getTimestamp(paramName)
                                    : callStmt.getTimestamp(iPos);
                            value = new Date(ts.getTime());
                        }
                        else if (javaType.equalsIgnoreCase(ParameterType.JAVA_RESULTSET)) {
                            // ResultSet not returned in properties
                        }
                        else {
                            throw new DBOException(
                                    "Error while extracting the CallableStatement output parameters: parameter type not supported ("
                                            + javaType + ")");
                        }
                        if (!javaType.equalsIgnoreCase(ParameterType.JAVA_RESULTSET)) {
                            if (outp.isReturnInProperties()) {
                                logger.debug("Setting out parameter " + propName + " = " + value + " in properties");
                                props.put(propName, value);
                            }
                            if (outp.isReturnInUUID()) {
                                Object uuid = DBOCallDynamicSP.this.currentRowFields.get(iPos - 1);
                                if (uuid != null) {
                                    String uuidStr = uuid.toString();
                                    logger.debug("Setting out parameter " + propName + " = " + value + " in UUID "
                                            + uuidStr);
                                    DBOCallDynamicSP.this.uuids.put(uuidStr, value.toString());
                                }
                            }
                        }
                    }
                }
            }
            catch (DBOException exc) {
                throw exc;
            }
            catch (IOException exc) {
                throw new DBOException("Error while extracting the CallableStatement output parameters: " + exc, exc);
            }
            catch (SQLException exc) {
                throw new DBOException("Error while extracting the CallableStatement output parameters: " + exc, exc);
            }
        }

        public void buildOutDocument(CallableStatement callStmt, Document xmlOut, JSONObject jsonOut, String statementId) throws DBOException
		{
        	if (this.rowSetBuilderType.equals("json")) {
        		buildOutJSON(callStmt, jsonOut, statementId);
        	}
        	else {
        		buildOutXml(callStmt, xmlOut, statementId);
        	}
		}

        /**
         * Specifies the output parameters from store procedure
         *
         * @param callStmt
         *        the statement
         * @param xmlOut
         * @param statementId
         * @throws DBOException
         *         if an error occurred
         */
        private void buildOutXml(CallableStatement callStmt, Document xmlOut, String statementId) throws DBOException
        {
            XMLUtils xml = null;
            try {
                xml = XMLUtils.getParserInstance();
                Element docRoot = xmlOut.getDocumentElement();
                if (docRoot == null) {
                    xmlOut.appendChild(xml.createElement(xmlOut, this.ROWSET_NAME));
                    docRoot = xmlOut.getDocumentElement();
                }
                Element data = xml.createElement(xmlOut, this.DATA_NAME);
                xml.setAttribute(data, ID_NAME, statementId);
                docRoot.appendChild(data);

                Element row = xml.createElement(xmlOut, ROW_NAME);
                xml.setAttribute(row, ID_NAME, DBOCallDynamicSP.this.SP_RESULT);
                data.appendChild(row);
                for (int i = 0; i < this.spOutputParams.size(); i++) {
                    SPOutputParam outp = this.spOutputParams.get(i);
                    String dbType = outp.getDBType().trim();
                    String javaType = outp.getJavaType().trim();
                    String propName = outp.getPropName();
                    String paramName = outp.getParamName();
                    int iPos = outp.getPosition();
                    String value = null;
                    ResultSet resultSet = null;

                    if (javaType.equalsIgnoreCase(ParameterType.JAVA_RESULTSET)) {
                        Object obj = null;
                        try {
                        	obj = this.namedParameterMode ? callStmt.getObject(paramName) : callStmt.getObject(iPos);
                        }
                        catch (SQLException exc) {
							// closed cursor?
                        	logger.warn("Error reading Cursor output parameter... Closed cursor?", exc);
						}
                        value = null;
                        if ((obj != null) && (obj instanceof ResultSet)) {
                            resultSet = (ResultSet) obj;
                        }
                    }
                    else {
                        Element col = xml.createElement(xmlOut, COL_NAME);
                        xml.setAttribute(col, ID_NAME, propName);
                        if (javaType.equalsIgnoreCase(ParameterType.JAVA_STRING)) {
                            if (dbType.equalsIgnoreCase(ParameterType.ORACLE_DATE)) {
                                String format = outp.getJavaTypeFormat();
                                if (format.equals("")) {
                                    format = DEFAULT_DATE_FORMAT;
                                }
                                Timestamp ts = this.namedParameterMode
                                        ? callStmt.getTimestamp(paramName)
                                        : callStmt.getTimestamp(iPos);
                                value = DateUtils.dateToString(new Date(ts.getTime()), format);
                                xml.setAttribute(col, TYPE_NAME, TIMESTAMP_TYPE);
                                xml.setAttribute(col, FORMAT_NAME, format);
                            }
                            else if (dbType.equalsIgnoreCase(ParameterType.ORACLE_CLOB)) {
                            	xml.setAttribute(col, TYPE_NAME, LONG_STRING_TYPE);
                            	Clob clob = this.namedParameterMode
                                        ? callStmt.getClob(paramName)
                                        : callStmt.getClob(iPos);
                                if (clob != null) {
	                                InputStream is = clob.getAsciiStream();
	                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                                IOUtils.copy(is, baos);
	                                is.close();
	                                try {
	                                	value = new String(baos.toByteArray(), 0, (int) clob.length());
	                                }
	                                catch (SQLFeatureNotSupportedException exc) {
	                                	value = baos.toString();
	                                }
	                            }
                                else {
                                	value = "";
                                }
                            }
                            else if (dbType.equalsIgnoreCase(ParameterType.ORACLE_BLOB)) {
                            	xml.setAttribute(col, TYPE_NAME, BASE64_TYPE);
                            	Blob blob = this.namedParameterMode
                                        ? callStmt.getBlob(paramName)
                                        : callStmt.getBlob(iPos);
                            	if (blob != null) {
	                                InputStream is = blob.getBinaryStream();
	                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                                IOUtils.copy(is, baos);
	                                is.close();
	                                try {
	                                    byte[] buffer = Arrays.copyOf(baos.toByteArray(),
	                                            (int) blob.length());
	                                    value = new String(Base64.encodeBase64(buffer));
	                                }
	                                catch (SQLFeatureNotSupportedException exc) {
	                                	value = new String(Base64.encodeBase64(baos.toByteArray()));
	                                }
	                            }
                            	else {
                            		value = "";
                            	}
                            }
                            else {
                                xml.setAttribute(col, TYPE_NAME, STRING_TYPE);
                                value = this.namedParameterMode ? callStmt.getString(paramName) : callStmt.getString(iPos);
                            }
                        }
                        else if (javaType.equalsIgnoreCase(ParameterType.JAVA_INT)) {
                            int v = this.namedParameterMode ? callStmt.getInt(paramName) : callStmt.getInt(iPos);
                            value = Integer.toString(v);
                            xml.setAttribute(col, TYPE_NAME, NUMERIC_TYPE);
                        }
                        else if (javaType.equalsIgnoreCase(ParameterType.JAVA_LONG)) {
                            long v = this.namedParameterMode ? callStmt.getLong(paramName) : callStmt.getLong(iPos);
                            value = Long.toString(v);
                            xml.setAttribute(col, TYPE_NAME, NUMERIC_TYPE);
                        }
                        else if (javaType.equalsIgnoreCase(ParameterType.JAVA_DATE)) {
                            String format = outp.getJavaTypeFormat();
                            if (format.equals("")) {
                                format = DEFAULT_DATE_FORMAT;
                            }
                            Timestamp ts = this.namedParameterMode
                                    ? callStmt.getTimestamp(paramName)
                                    : callStmt.getTimestamp(iPos);
                            value = DateUtils.dateToString(new Date(ts.getTime()), format);
                            xml.setAttribute(col, TYPE_NAME, TIMESTAMP_TYPE);
                            xml.setAttribute(col, FORMAT_NAME, format);
                        }
                        else {
                            throw new DBOException(
                                    "Error while extracting the CallableStatement output parameters: parameter type not supported ("
                                            + javaType + ")");
                        }
                        if (value != null) {
                            Text text = xmlOut.createTextNode(value);
                            col.appendChild(text);
                        }
                        row.appendChild(col);
                    }
                    if (resultSet != null) {
                        try {
                            ResultSetMetaData metadata = resultSet.getMetaData();
                            boolean firstItr = true;
                            Set<Integer> keyField = DBOCallDynamicSP.this.keysMap.get(statementId);
                            boolean noKey = ((keyField == null) || keyField.isEmpty());
                            Map<String, String> keyAttr = new HashMap<String, String>();
                            String colKey = null;
                            String precKey = null;
                            while (resultSet.next()) {
                                if (!firstItr) {
                                    row = xml.createElement(xmlOut, ROW_NAME);
                                    xml.setAttribute(row, ID_NAME, DBOCallDynamicSP.this.SP_RESULT);
                                }
                                for (int j = 1; j <= metadata.getColumnCount(); j++) {
                                    Element col = xml.createElement(xmlOut, "col");
                                    xml.setAttribute(col, ID_NAME, propName + "[" + j + "]");
                                    switch (metadata.getColumnType(j)) {
                                        case Types.CLOB : {
                                            Clob clob = resultSet.getClob(j);
                                            if (clob != null) {
                                                InputStream is = clob.getAsciiStream();
                                                byte[] buffer = new byte[2048];
                                                ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
                                                int size;
                                                while ((size = is.read(buffer)) != -1) {
                                                    baos.write(buffer, 0, size);
                                                }
                                                is.close();
                                                value = baos.toString();
                                            }
                                        }
                                            break;
                                        case Types.BLOB : {
                                            Blob blob = resultSet.getBlob(j);
                                            if (blob != null) {
                                                InputStream is = blob.getBinaryStream();
                                                byte[] buffer = new byte[2048];
                                                ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
                                                int size;
                                                while ((size = is.read(buffer)) != -1) {
                                                    baos.write(buffer, 0, size);
                                                }
                                                is.close();
                                                value = new String(Base64.encodeBase64(baos.toByteArray()));
                                            }
                                        }
                                            break;
                                        default : {
                                            value = resultSet.getString(j);
                                            if (value == null) {
                                                value = "";
                                            }
                                        }
                                    }
                                    if (value != null) {
                                        col.appendChild(xmlOut.createTextNode(value));
                                    }
                                    if (!noKey && keyField.contains(new Integer(j))) {
                                        if (value != null) {
                                            if (colKey == null) {
                                                colKey = value;
                                            }
                                            else {
                                                colKey += "##" + value;
                                            }
                                            keyAttr.put("key_" + j, value);
                                        }
                                    }
                                    else {
                                        row.appendChild(col);
                                    }
                                }
                                if (!noKey && (colKey != null) && !colKey.equals(precKey)) {
                                    if (!firstItr) {
                                        data = xml.createElement(xmlOut, this.DATA_NAME);
                                        xml.setAttribute(data, ID_NAME, statementId);
                                        docRoot.appendChild(data);
                                    }
                                    for (Entry<String, String> keyAttrEntry : keyAttr.entrySet()) {
                                        xml.setAttribute(data, keyAttrEntry.getKey(), keyAttrEntry.getValue());
                                    }
                                    keyAttr.clear();
                                    precKey = colKey;
                                }
                                if (firstItr) {
                                    firstItr = false;
                                }
                                colKey = null;
                                data.appendChild(row);
                            }
                        }
                        finally {
                            resultSet.close();
                        }
                    }
                }
            }
            catch (DBOException exc) {
                throw exc;
            }
            catch (Exception exc) {
                throw new DBOException("Generic error", exc);
            }
            finally {
                XMLUtils.releaseParserInstance(xml);
            }
        }

        private void buildOutJSON(CallableStatement callStmt, JSONObject jsonOut, String statementId) throws DBOException
        {
            try {
            	JSONObject root = null;
                if (!jsonOut.has(this.ROWSET_NAME)) {
                    jsonOut.put(this.ROWSET_NAME, new JSONObject());
                }
                root = jsonOut.getJSONObject(this.ROWSET_NAME);
                JSONObject data = new JSONObject();
                data.put(ID_NAME, statementId);
                root.append(this.DATA_NAME, data);

                JSONObject row = new JSONObject();
                row.put(ID_NAME, DBOCallDynamicSP.this.SP_RESULT);
                data.append(ROW_NAME, row);
                for (int i = 0; i < this.spOutputParams.size(); i++) {
                    SPOutputParam outp = this.spOutputParams.get(i);
                    String dbType = outp.getDBType().trim();
                    String javaType = outp.getJavaType().trim();
                    String propName = outp.getPropName();
                    String paramName = outp.getParamName();
                    int iPos = outp.getPosition();
                    String value = null;
                    ResultSet resultSet = null;

                    if (javaType.equalsIgnoreCase(ParameterType.JAVA_RESULTSET)) {
                        Object obj = null;
                        try {
                        	obj = this.namedParameterMode ? callStmt.getObject(paramName) : callStmt.getObject(iPos);
                        }
                        catch (SQLException exc) {
							// closed cursor?
                        	logger.warn("Error reading Cursor output parameter... Closed cursor?", exc);
						}
                        value = null;
                        if ((obj != null) && (obj instanceof ResultSet)) {
                            resultSet = (ResultSet) obj;
                        }
                    }
                    else {
                    	Object jsonValue = ResultSetTransformer.parseValue(this.namedParameterMode ? callStmt.getObject(paramName) : callStmt.getObject(iPos));
                    	row.put(propName,  (jsonValue != null ? jsonValue : JSONObject.NULL));
                    }
                    if (resultSet != null) {
                        try {
                        	JSONArray rows = ResultSetTransformer.toJSONArray(resultSet);
                        	data.append(ROW_NAME, rows);
                        }
                        finally {
                            resultSet.close();
                        }
                    }
                }
            }
            catch (Exception exc) {
                throw new DBOException("Generic error", exc);
            }
        }
    }

    /**
     * Current <code>SPCallDescriptor</code> to invoke the stored procedure.
     */
    SPCallDescriptor                            spCallDescriptor;

    /**
     * Call descriptors cache of stored procedure calls configured and
     * identified by an ID.
     */
    private final Map<String, SPCallDescriptor> spCallDescriptors;

    private Document                            xmlOut;
    private JSONObject                          jsonOut;
    private String                              rowSetBuilderType  = null;

    /**
     * Private <i>logger</i> instance.
     */
    private static final Logger                 logger = GVLogger.getLogger(DBOCallDynamicSP.class);

    private final Map<String, Set<Integer>>     keysMap;

    /**
     * Default constructor.
     *
     */
    public DBOCallDynamicSP()
    {
        super();
        this.spCallDescriptors = new HashMap<String, SPCallDescriptor>();
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
            this.forcedMode = XMLConfig.get(config, "@force-mode", MODE_CALL);
            this.isReturnData = XMLConfig.getBoolean(config, "@return-data", true);
            this.rowSetBuilderType = XMLConfig.get(config, "@rowset-builder", "standard");

            NodeList stmts = XMLConfig.getNodeList(config, "statement[@type='callsp']");
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
                this.spCallDescriptors.put(id, new SPCallDescriptor(stmt));
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
        }
        catch (Exception exc) {
            logger.error("Error reading configuration of [" + this.dboclass + "]", exc);
            throw new DBOException("Error reading configuration of [" + this.dboclass + "]", exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#execute(java.lang.Object,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public void execute(Object input, Connection conn, Map<String, Object> props) throws DBOException,
            InterruptedException {
        this.dataOut = new ByteArrayOutputStream();
        try {
            createOutDocument();
            super.execute(input, conn, props);
            storeResult();
        }
        finally {
            this.dhr.setRead(0);
            this.dhr.setTotal(0);
            this.dhr.setInsert(0);
            this.dhr.setUpdate(0);
            this.dhr.setDiscard(0);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#execute(java.io.OutputStream,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public void execute(OutputStream data, Connection conn, Map<String, Object> props) throws DBOException,
            InterruptedException {
        this.dataOut = data;
        try {
            createOutDocument();
            super.execute((OutputStream) null, conn, props);
            storeResult();
        }
        finally {
            this.dhr.setRead(0);
            this.dhr.setTotal(0);
            this.dhr.setInsert(0);
            this.dhr.setUpdate(0);
            this.dhr.setDiscard(0);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#execute(java.lang.Object,
     *      java.io.OutputStream, java.sql.Connection, java.util.Map)
     */
    @Override
    public void execute(Object dataIn, OutputStream dataOut, Connection conn, Map<String, Object> props)
            throws DBOException, InterruptedException {
        this.dataOut = dataOut;
        try {
        	createOutDocument();
            super.execute(dataIn, conn, props);
            storeResult();
        }
        finally {
            this.dhr.setRead(0);
            this.dhr.setTotal(0);
            this.dhr.setInsert(0);
            this.dhr.setUpdate(0);
            this.dhr.setDiscard(0);
        }
    }

    /**
     * @throws DBOException
     *
     */
    private void storeResult() throws DBOException
    {
        if (this.dataOut != null) {
        	if (this.rowSetBuilderType.equals("json")) {
        		try {
        			this.dataOut.write(this.jsonOut.toString().getBytes());
	            }
	            catch (Exception exc) {
	                throw new DBOException("Cannot store DBOCallSP JSON result.", exc);
	            }
        	}
        	else {
	            XMLUtils xml = null;
	            try {
	                xml = XMLUtils.getParserInstance();
	                byte[] dataDOM = xml.serializeDOMToByteArray(this.xmlOut);
	                this.dataOut.write(dataDOM);
	            }
	            catch (Exception exc) {
	                throw new DBOException("Cannot store DBOCallSP XML result.", exc);
	            }
	            finally {
	                XMLUtils.releaseParserInstance(xml);
	            }
        	}
        }
        this.xmlOut = null;
        this.jsonOut = null;
    }

    /**
     * @throws DBOException
     *
     */
    private void createOutDocument() throws DBOException
    {
    	for (SPCallDescriptor spc : this.spCallDescriptors.values()) {
			spc.reset();
		}
    	if (this.rowSetBuilderType.equals("json")) {
    		this.xmlOut = null;
    		this.jsonOut = new JSONObject();
    	}
    	else {
    		this.jsonOut = null;
	        XMLUtils xml = null;
	        try {
	            xml = XMLUtils.getParserInstance();
	            this.xmlOut = xml.newDocument();
	        }
	        catch (XMLUtilsException exc) {
	            throw new DBOException("Cannot instantiate XMLUtils.", exc);
	        }
	        finally {
	            XMLUtils.releaseParserInstance(xml);
	        }
    	}
    }

    private void handleOutput() throws DBOException
    {
        try {
            CallableStatement sqlStatement = (CallableStatement) this.sqlStatementInfo.getStatement();
            this.spCallDescriptor.setOutputParameterValuesInMap(sqlStatement, getCurrentProps());
           	this.spCallDescriptor.buildOutDocument(sqlStatement, this.xmlOut, this.jsonOut, this.sqlStatementInfo.getId());
        }
        catch (DBOException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new DBOException("Error processing output parameters: " + exc.getMessage(), exc);
        }
    }

    private final String          SP_RESULT    = "sp_result";

    /**
     *
     */
    protected static final String OUTONLY_ATTR = "out-only";

    private int                   colIdx       = 0;

    private String                currType;

    private String                currDateFormat;

    private String                currNumberFormat;

    private String                currGroupSeparator;

    private String                currDecSeparator;

    private StringBuffer          textBuffer;

    private boolean               colDataExpecting;

    private OutputStream          dataOut;

    private String                currentUUID;

    private boolean               outOnly;

    private String                currName;

    private boolean               useName;

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#getStatement(java.lang.String)
     */
    @Override
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
                this.spCallDescriptor = this.spCallDescriptors.get(id);
                if (this.spCallDescriptor == null) {
                    logger.error("SQL Call descriptor with id " + id + " not found.");
                    throw new SAXException("SQL Call descriptor with id " + id + " not found.");
                }
                String expandedSQL = PropertiesHandler.expand(this.spCallDescriptor.getStatement(), getCurrentProps(),
                        getInternalConn(), null);
                logger.debug("expandedSQL stmt: " + expandedSQL);
                Statement statement = getInternalConn().prepareCall(expandedSQL);
                this.sqlStatementInfo = new StatementInfo(id, expandedSQL, statement);
                this.spCallDescriptor.specifyOutputParameter((CallableStatement) statement);
                setCurrentId(id);
            }
            catch (SAXException exc) {
                throw exc;
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
     *
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
    	if (SP_OUT_PARAMS_NAME.equals(localName)) {
            String id = attributes.getValue(uri, ID_NAME);
            this.spCallDescriptor = this.spCallDescriptors.get(id);
        }
    	else if (SP_OUT_PAR_NAME.equals(localName)) {
    		try {
    			this.spCallDescriptor.addSpOutputParam(uri, attributes);
    		}
    		catch(DBOException exc) {
    			throw new SAXException(exc);
    		}
    	}
    	else if (ROW_NAME.equals(localName)) {
            this.currentRowFields.clear();
            this.colDataExpecting = false;
            this.colIdx = 0;
            String id = attributes.getValue(uri, ID_NAME);
            getStatement(id);
            this.currentXSLMessage = attributes.getValue(uri, XSL_MSG_NAME);
            String statsOn = attributes.getValue(uri, STATS_ON_NAME);
            this.statsOnInsert = !(STATS_UPD_MODE.equals(statsOn));
            String incrDisc = attributes.getValue(uri, INCR_DISC_NAME);
            this.incrDiscIfUpdKO = !(INCR_DISC_N_MODE.equals(incrDisc));
            this.currCriticalError = "true".equalsIgnoreCase(attributes.getValue(uri, CRITICAL_ERROR));
        }
        else if (COL_NAME.equals(localName)) {
            this.currType = attributes.getValue(uri, TYPE_NAME);
            this.currName = attributes.getValue(uri, NAME_ATTR);
            this.useName = (this.currName != null) && (this.currName.trim().length() > 0);
            this.currentUUID = attributes.getValue(uri, UUID_NAME);
            String outOnlyStr = attributes.getValue(uri, OUTONLY_ATTR);
            this.outOnly = outOnlyStr != null ? outOnlyStr.equalsIgnoreCase("true") : false;
            if (!this.outOnly) {
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
                this.textBuffer = new StringBuffer();
            }
            this.colIdx++;
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (this.colDataExpecting) {
            this.textBuffer.append(ch, start, length);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
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
            CallableStatement cs = (CallableStatement) this.sqlStatementInfo.getStatement();
            try {
                if (!this.outOnly) {
                    this.colDataExpecting = false;
                    String text = this.textBuffer.toString();
                    if ((this.currentUUID != null) && (this.currentUUID.trim().length() > 0) && (text.length() == 0)) {
                        text = this.uuids.get(this.currentUUID);
                        if (text == null) {
                            text = this.currentUUID;
                        }
                    }
                    if (TIMESTAMP_TYPE.equals(this.currType)) {
                        if (text.equals("")) {
                            setNull(cs, Types.TIMESTAMP);
                            this.currentRowFields.add(null);
                        }
                        else {
                            this.dateFormatter.applyPattern(this.currDateFormat);
                            Date formattedDate = this.dateFormatter.parse(text);
                            Timestamp ts = new Timestamp(formattedDate.getTime());
                            setTimestamp(cs, ts);
                            this.currentRowFields.add(ts);
                        }
                    }
                    else if (NUMERIC_TYPE.equals(this.currType)) {
                        if (text.equals("")) {
                            setNull(cs, Types.NUMERIC);
                            this.currentRowFields.add(null);
                        }
                        else {
                            setInt(cs, Integer.parseInt(text));
                            this.currentRowFields.add(Integer.valueOf(text));
                        }
                    }
                    else if (FLOAT_TYPE.equals(this.currType) || DECIMAL_TYPE.equals(this.currType)) {
                        if (text.equals("")) {
                            setNull(cs, Types.NUMERIC);
                            this.currentRowFields.add(null);
                        }
                        else {
                            DecimalFormatSymbols dfs = this.numberFormatter.getDecimalFormatSymbols();
                            dfs.setDecimalSeparator(this.currDecSeparator.charAt(0));
                            dfs.setGroupingSeparator(this.currGroupSeparator.charAt(0));
                            this.numberFormatter.setDecimalFormatSymbols(dfs);
                            this.numberFormatter.applyPattern(this.currNumberFormat);
                            Number formattedNumber = this.numberFormatter.parse(text);
                            setFloat(cs, formattedNumber.floatValue());
                            this.currentRowFields.add(new Float(formattedNumber.floatValue()));
                        }
                    }
                    else if (LONG_STRING_TYPE.equals(this.currType)) {
                        if (text.equals("")) {
                            setNull(cs, Types.CLOB);
                            this.currentRowFields.add(null);
                        }
                        else {
                            byte[] data = text.getBytes();
                            ByteArrayInputStream bais = new ByteArrayInputStream(data);
                            setAsciiStream(cs, bais, data.length);
                            this.currentRowFields.add(text);
                        }
                    }
                    else if (BASE64_TYPE.equals(this.currType)) {
                        if (text.equals("")) {
                            setNull(cs, Types.BLOB);
                            this.currentRowFields.add(null);
                        }
                        else {
                            byte[] data = text.getBytes();
                            data = Base64.decodeBase64(data);
                            ByteArrayInputStream bais = new ByteArrayInputStream(data);
                            setBinaryStream(cs, bais, data.length);
                            this.currentRowFields.add(text);
                        }
                    }
                    else if (BINARY_TYPE.equals(this.currType)) {
                        if (text.equals("")) {
                            setNull(cs, Types.BLOB);
                            this.currentRowFields.add(null);
                        }
                        else {
                            byte[] data = text.getBytes();
                            ByteArrayInputStream bais = new ByteArrayInputStream(data);
                            setBinaryStream(cs, bais, data.length);
                            this.currentRowFields.add(text);
                        }
                    }
                    else {
                        if (text.equals("")) {
                            setNull(cs, Types.VARCHAR);
                            this.currentRowFields.add(null);
                        }
                        else {
                            setString(cs, text);
                            this.currentRowFields.add(text);
                        }
                    }
                }
                else {
                    this.currentRowFields.add(this.currentUUID);
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
     * @param cs
     * @param text
     * @throws SQLException
     */
    private void setString(CallableStatement cs, String text) throws SQLException
    {
        if (this.useName) {
            cs.setString(this.currName, text);
        }
        else {
            cs.setString(this.colIdx, text);
        }
    }

    /**
     * @param cs
     * @param bais
     * @param length
     * @throws SQLException
     */
    private void setBinaryStream(CallableStatement cs, ByteArrayInputStream bais, int length) throws SQLException
    {
        if (this.useName) {
            cs.setBinaryStream(this.currName, bais, length);
        }
        else {
            cs.setBinaryStream(this.colIdx, bais, length);
        }
    }

    /**
     * @param cs
     * @param bais
     * @param length
     * @throws SQLException
     */
    private void setAsciiStream(CallableStatement cs, ByteArrayInputStream bais, int length) throws SQLException
    {
        if (this.useName) {
            cs.setAsciiStream(this.currName, bais, length);
        }
        else {
            cs.setAsciiStream(this.colIdx, bais, length);
        }
    }

    /**
     * @param cs
     * @param num
     * @throws SQLException
     */
    private void setFloat(CallableStatement cs, float num) throws SQLException
    {
        if (this.useName) {
            cs.setFloat(this.currName, num);
        }
        else {
            cs.setFloat(this.colIdx, num);
        }
    }

    /**
     * @param cs
     * @param num
     * @throws SQLException
     */
    private void setInt(CallableStatement cs, int num) throws SQLException
    {
        if (this.useName) {
            cs.setInt(this.currName, num);
        }
        else {
            cs.setInt(this.colIdx, num);
        }
    }

    /**
     * @param cs
     * @param ts
     * @throws SQLException
     */
    private void setTimestamp(CallableStatement cs, Timestamp ts) throws SQLException
    {
        if (this.useName) {
            cs.setTimestamp(this.currName, ts);
        }
        else {
            cs.setTimestamp(this.colIdx, ts);
        }
    }

    /**
     * @param cs
     * @throws SQLException
     */
    private void setNull(CallableStatement cs, int type) throws SQLException
    {
        if (this.useName) {
            cs.setNull(this.currName, type);
        }
        else {
            cs.setNull(this.colIdx, type);
        }
    }

    /**
     * @throws SAXException
     */
    @Override
    protected void executeStatement() throws SAXException
    {
        try {
            if (this.sqlStatementInfo != null) {
                super.executeStatement();
                handleOutput();
            }
        }
        catch (DBOException exc) {
            logger.error("Record parameters:\n" + dumpCurrentRowFields());
            logger.error("SQL Statement Informations:\n" + this.sqlStatementInfo);
            logger.error("DBOException error on row " + getRowCounter() + ": " + exc.getMessage());
            throw new SAXException(exc);
        }
    }
}

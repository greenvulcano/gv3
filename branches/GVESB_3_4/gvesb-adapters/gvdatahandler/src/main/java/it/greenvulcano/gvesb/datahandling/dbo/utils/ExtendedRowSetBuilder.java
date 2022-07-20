/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.datahandling.dbo.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO;
import it.greenvulcano.gvesb.datahandling.utils.FieldFormatter;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xml.XMLUtils;

/**
 *
 * @version 3.4.0 06/ago/2013
 * @author GreenVulcano Developer Team
 *
 */
public class ExtendedRowSetBuilder implements RowSetBuilder
{
    private static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";
    private static final String NS          = "http://www.greenvulcano.com/database";
    protected String           name;
    private Logger           logger;
    private String           numberFormat;
    private String           groupSeparator;
    private String           decSeparator;
    private XMLUtils         parser;
    private SimpleDateFormat dateFormatter;
    private DecimalFormat    numberFormatter;
    private FieldFormatter[] fFormatters;
    private String[]         colNames;

    @Override
	public Document createDocument(XMLUtils parser) throws NullPointerException {
        if (parser == null) {
            parser = this.parser;
        }
        if (parser == null) {
            throw new NullPointerException("Parser not set");
        }
        Document doc = parser.newDocument(AbstractDBO.ROWSET_NAME, NS);
        return doc;
    }

    @Override
	public int build(Document doc, String id, ResultSet rs, Set<Integer> keyField,
            Map<String, FieldFormatter> fieldNameToFormatter, Map<String, FieldFormatter> fieldIdToFormatter)
            throws Exception {
        if (rs == null) {
            return 0;
        }
        int rowCounter = 0;
        Element docRoot = doc.getDocumentElement();
        ResultSetMetaData metadata = rs.getMetaData();
        buildFormatterAndNamesArray(metadata, fieldNameToFormatter, fieldIdToFormatter);

        boolean noKey = ((keyField == null) || keyField.isEmpty());
        boolean isKeyCol = false;

        boolean isNull = false;
        Element data = null;
        Element row = null;
        Element col = null;
        Text text = null;
        String textVal = null;
        String precKey = null;
        String colKey = null;
        Map<String, Element> keyCols = new TreeMap<String, Element>();
        while (rs.next()) {
            if ((rowCounter % 10) == 0) {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), this.name, this.logger);
            }
            row = this.parser.createElementNS(doc, AbstractDBO.ROW_NAME, NS);

            this.parser.setAttribute(row, AbstractDBO.ID_NAME, id);
            for (int j = 1; j <= metadata.getColumnCount(); j++) {
                FieldFormatter fF = this.fFormatters[j];
                String colName = this.colNames[j];

                isKeyCol =  (!noKey && keyField.contains(new Integer(j)));
                isNull = false;
                col = this.parser.createElementNS(doc, colName, NS);
                if (isKeyCol) {
                    this.parser.setAttribute(col, AbstractDBO.ID_NAME, String.valueOf(j));
                }
                switch (metadata.getColumnType(j)) {
                    case Types.DATE :
                    case Types.TIME :
                    case Types.TIMESTAMP : {
                        this.parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.TIMESTAMP_TYPE);
                        Timestamp dateVal = rs.getTimestamp(j);
                        isNull = dateVal == null;
                        this.parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            this.parser.setAttribute(col, AbstractDBO.FORMAT_NAME, AbstractDBO.DEFAULT_DATE_FORMAT);
                            textVal = "";
                        }
                        else {
                            if (fF != null) {
                                this.parser.setAttribute(col, AbstractDBO.FORMAT_NAME, fF.getDateFormat());
                                textVal = fF.formatDate(dateVal);
                            }
                            else {
                                this.parser.setAttribute(col, AbstractDBO.FORMAT_NAME, AbstractDBO.DEFAULT_DATE_FORMAT);
                                textVal = this.dateFormatter.format(dateVal);
                            }
                        }
                    }
                        break;
                    case Types.DECIMAL :
                    case Types.DOUBLE :
                    case Types.FLOAT :
                    case Types.REAL : {
                        this.parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.FLOAT_TYPE);
                        float numVal = rs.getFloat(j);
                        this.parser.setAttribute(col, AbstractDBO.NULL_NAME, "false");
                        if (fF != null) {
                            this.parser.setAttribute(col, AbstractDBO.FORMAT_NAME, fF.getNumberFormat());
                            this.parser.setAttribute(col, AbstractDBO.GRP_SEPARATOR_NAME, fF.getGroupSeparator());
                            this.parser.setAttribute(col, AbstractDBO.DEC_SEPARATOR_NAME, fF.getDecSeparator());
                            textVal = fF.formatNumber(numVal);
                        }
                        else {
                            this.parser.setAttribute(col, AbstractDBO.FORMAT_NAME, this.numberFormat);
                            this.parser.setAttribute(col, AbstractDBO.GRP_SEPARATOR_NAME, this.groupSeparator);
                            this.parser.setAttribute(col, AbstractDBO.DEC_SEPARATOR_NAME, this.decSeparator);
                            textVal = this.numberFormatter.format(numVal);
                        }
                    }
                        break;
                    case Types.BIGINT :
                    case Types.INTEGER :
                    case Types.NUMERIC :
                    case Types.SMALLINT :
                    case Types.TINYINT : {
                        BigDecimal bigdecimal = rs.getBigDecimal(j);
                        isNull = bigdecimal == null;
                        this.parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            if (metadata.getScale(j) > 0) {
                                this.parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.FLOAT_TYPE);
                            }
                            else {
                                this.parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.NUMERIC_TYPE);
                            }
                            textVal = "";
                        }
                        else {
                            if (fF != null) {
                                this.parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.FLOAT_TYPE);
                                this.parser.setAttribute(col, AbstractDBO.FORMAT_NAME, fF.getNumberFormat());
                                this.parser.setAttribute(col, AbstractDBO.GRP_SEPARATOR_NAME, fF.getGroupSeparator());
                                this.parser.setAttribute(col, AbstractDBO.DEC_SEPARATOR_NAME, fF.getDecSeparator());
                                textVal = fF.formatNumber(bigdecimal);
                            }
                            else if (metadata.getScale(j) > 0) {
                                this.parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.FLOAT_TYPE);
                                this.parser.setAttribute(col, AbstractDBO.FORMAT_NAME, this.numberFormat);
                                this.parser.setAttribute(col, AbstractDBO.GRP_SEPARATOR_NAME, this.groupSeparator);
                                this.parser.setAttribute(col, AbstractDBO.DEC_SEPARATOR_NAME, this.decSeparator);
                                textVal = this.numberFormatter.format(bigdecimal);
                            }
                            else {
                                this.parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.NUMERIC_TYPE);
                                textVal = bigdecimal.toString();
                            }
                        }
                    }
                        break;
                    case Types.NCHAR :
                    case Types.NVARCHAR : {
                        this.parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.NSTRING_TYPE);
                        textVal = rs.getNString(j);
                        isNull = textVal == null;
                        this.parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                    }
                        break;
                    case Types.CHAR :
                    case Types.VARCHAR : {
                        this.parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.STRING_TYPE);
                        textVal = rs.getString(j);
                        isNull = textVal == null;
                        this.parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                    }
                        break;
                    case Types.NCLOB : {
                    	this.parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.LONG_NSTRING_TYPE);
                        NClob clob = rs.getNClob(j);
                        isNull = clob == null;
                        this.parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                        else {
                            Reader is = clob.getCharacterStream();
                            StringWriter str = new StringWriter();

                            IOUtils.copy(is, str);
                            is.close();
                            textVal = str.toString();
                        }
                    }
                        break;
                    case Types.CLOB : {
                        this.parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.LONG_STRING_TYPE);
                        Clob clob = rs.getClob(j);
                        isNull = clob == null;
                        this.parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                        else {
                        	Reader is = clob.getCharacterStream();
                            StringWriter str = new StringWriter();

                            IOUtils.copy(is, str);
                            is.close();
                            textVal = str.toString();
                        }
                    }
                        break;
                    case Types.BLOB : {
                        this.parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.BASE64_TYPE);
                        Blob blob = rs.getBlob(j);
                        isNull = blob == null;
                        this.parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                        else {
                            InputStream is = blob.getBinaryStream();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            IOUtils.copy(is, baos);
                            is.close();
                            try {
                                byte[] buffer = Arrays.copyOf(baos.toByteArray(), (int) blob.length());
                                textVal = new String(Base64.encodeBase64(buffer));
                            }
                            catch (SQLFeatureNotSupportedException exc) {
                                textVal = new String(Base64.encodeBase64(baos.toByteArray()));
                            }
                        }
                    }
                        break;
                    default : {
                        this.parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.DEFAULT_TYPE);
                        textVal = rs.getString(j);
                        isNull = textVal == null;
                        this.parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                    }
                }
                if (textVal != null) {
                    text = doc.createTextNode(textVal);
                    col.appendChild(text);
                }
                if (isKeyCol) {
                    if (textVal != null) {
                        if (colKey == null) {
                            colKey = textVal;
                        }
                        else {
                            colKey += "##" + textVal;
                        }
                        keyCols.put(String.valueOf(j), col);
                    }
                }
                else {
                    row.appendChild(col);
                }
            }
            if (noKey) {
                if (data == null) {
                    data = this.parser.createElementNS(doc, AbstractDBO.DATA_NAME, NS);
                    this.parser.setAttribute(data, AbstractDBO.ID_NAME, id);
                }
            }
            else if ((colKey != null) && !colKey.equals(precKey)) {
                if (data != null) {
                    docRoot.appendChild(data);
                }
                data = this.parser.createElementNS(doc, AbstractDBO.DATA_NAME, NS);
                this.parser.setAttribute(data, AbstractDBO.ID_NAME, id);
                Element key = this.parser.createElementNS(doc, AbstractDBO.KEY_NAME, NS);
                data.appendChild(key);
                for (Entry<String, Element> keyColsEntry : keyCols.entrySet()) {
                    key.appendChild(keyColsEntry.getValue());
                }
                keyCols.clear();
                precKey = colKey;
            }
            colKey = null;
            data.appendChild(row);
            rowCounter++;
        }
        if (data != null) {
            docRoot.appendChild(data);
        }

        return rowCounter;
    }

    @Override
	public void cleanup() {
        this.numberFormat = null;
        this.groupSeparator = null;
        this.decSeparator = null;
        this.parser = null;
        this.dateFormatter = null;
        this.numberFormatter = null;
        this.fFormatters = null;
        this.colNames = null;
    }

    @Override
	public void setName(String name) {
        this.name = name;
    }

    @Override
	public String getName() {
		return this.name;
	}

    @Override
	public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Logger getLogger() {
    	return this.logger;
    }

    @Override
	public void setDateFormatter(SimpleDateFormat dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    @Override
    public SimpleDateFormat getDateFormatter() {
    	return this.dateFormatter;
    }

    @Override
	public void setNumberFormatter(DecimalFormat numberFormatter) {
        this.numberFormatter = numberFormatter;
    }

    @Override
    public DecimalFormat getNumberFormatter() {
    	return this.numberFormatter;
    }

    @Override
	public void setDecSeparator(String decSeparator) {
        this.decSeparator = decSeparator;
    }

    @Override
    public String getDecSeparator() {
    	return this.decSeparator;
    }

    @Override
	public void setGroupSeparator(String groupSeparator) {
        this.groupSeparator = groupSeparator;
    }

    @Override
    public String getGroupSeparator() {
    	return this.groupSeparator;
    }

    @Override
	public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }

    @Override
    public String getNumberFormat() {
    	return this.numberFormat;
    }

    @Override
	public void setXMLUtils(XMLUtils parser) {
        this.parser = parser;
    }

    @Override
    public XMLUtils getXMLUtils() {
    	return this.parser;
    }

    @Override
    public RowSetBuilder getCopy() {
        ExtendedRowSetBuilder copy = new ExtendedRowSetBuilder();

        copy.setName(getName());
        copy.setLogger(getLogger());
        copy.setNumberFormat(getNumberFormat());
        copy.setGroupSeparator(getGroupSeparator());
        copy.setDecSeparator(getDecSeparator());
        copy.setXMLUtils(getXMLUtils());
        copy.setDateFormatter(getDateFormatter());
        copy.setNumberFormatter(getNumberFormatter());

        return copy;
    }

    private void buildFormatterAndNamesArray(ResultSetMetaData rsm,
            Map<String, FieldFormatter> fieldNameToFormatter, Map<String, FieldFormatter> fieldIdToFormatter)
            throws Exception {
        this.fFormatters = new FieldFormatter[rsm.getColumnCount() + 1];
        this.colNames = new String[rsm.getColumnCount() + 1];

        for (int i = 1; i < this.fFormatters.length; i++) {
        	String cName = getColumnName(rsm, i);
            this.colNames[i] = adaptName(cName);
            FieldFormatter fF = fieldNameToFormatter.get(cName);
            if (fF == null) {
                fF = fieldIdToFormatter.get("" + i);
            }
            this.fFormatters[i] = fF;
        }
    }

	protected String getColumnName(ResultSetMetaData rsm, int i) throws SQLException {
		String cName = rsm.getColumnLabel(i);
		if (cName == null) {
			cName = rsm.getColumnName(i);
		}
		return cName;
	}

    protected String adaptName(String cName) {
        String res = "";
        for (int i = 0; i < cName.length(); i++) {
            String c = cName.substring(i, i+1);
            if (VALID_CHARS.indexOf(c) != -1) {
                res += c;
            }
            else {
                res += "_";
            }
        }
        return res;
    }
}

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

import it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO;
import it.greenvulcano.gvesb.datahandling.utils.FieldFormatter;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * 
 * @version 3.4.0 06/ago/2013
 * @author GreenVulcano Developer Team
 * 
 */
public class StandardRowSetBuilder implements RowSetBuilder
{
    private String           numberFormat;
    private String           groupSeparator;
    private String           decSeparator;
    private XMLUtils         parser;
    private SimpleDateFormat dateFormatter;
    private DecimalFormat    numberFormatter;

    public Document createDocument(XMLUtils parser) {
        Document doc = parser.newDocument(AbstractDBO.ROWSET_NAME);
        return doc;
    }

    public int build(Document doc, String id, ResultSet rs, Set<Integer> keyField,
            Map<String, FieldFormatter> fieldNameToFormatter, Map<String, FieldFormatter> fieldIdToFormatter)
            throws Exception {
        if (rs == null) {
            return 0;
        }
        int rowCounter = 0;
        Element docRoot = doc.getDocumentElement();
        ResultSetMetaData metadata = rs.getMetaData();
        FieldFormatter[] fFormatters = buildFormatterArray(metadata, fieldNameToFormatter, fieldIdToFormatter);

        boolean noKey = ((keyField == null) || keyField.isEmpty());

        boolean isNull = false;
        Element data = null;
        Element row = null;
        Element col = null;
        Text text = null;
        String textVal = null;
        String precKey = null;
        String colKey = null;
        Map<String, String> keyAttr = new HashMap<String, String>();
        while (rs.next()) {
            row = parser.createElement(doc, AbstractDBO.ROW_NAME);

            parser.setAttribute(row, AbstractDBO.ID_NAME, id);
            for (int j = 1; j <= metadata.getColumnCount(); j++) {
                FieldFormatter fF = fFormatters[j];

                isNull = false;
                col = parser.createElement(doc, AbstractDBO.COL_NAME);
                switch (metadata.getColumnType(j)) {
                    case Types.DATE :
                    case Types.TIME :
                    case Types.TIMESTAMP : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.TIMESTAMP_TYPE);
                        Timestamp dateVal = rs.getTimestamp(j);
                        isNull = dateVal == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            parser.setAttribute(col, AbstractDBO.FORMAT_NAME, AbstractDBO.DEFAULT_DATE_FORMAT);
                            textVal = "";
                        }
                        else {
                            if (fF != null) {
                                parser.setAttribute(col, AbstractDBO.FORMAT_NAME, fF.getDateFormat());
                                textVal = fF.formatDate(dateVal);
                            }
                            else {
                                parser.setAttribute(col, AbstractDBO.FORMAT_NAME, AbstractDBO.DEFAULT_DATE_FORMAT);
                                textVal = dateFormatter.format(dateVal);
                            }
                        }
                    }
                        break;
                    case Types.DOUBLE :
                    case Types.FLOAT :
                    case Types.REAL : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.FLOAT_TYPE);
                        float numVal = rs.getFloat(j);
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, "false");
                        if (fF != null) {
                            parser.setAttribute(col, AbstractDBO.FORMAT_NAME, fF.getNumberFormat());
                            parser.setAttribute(col, AbstractDBO.GRP_SEPARATOR_NAME, fF.getGroupSeparator());
                            parser.setAttribute(col, AbstractDBO.DEC_SEPARATOR_NAME, fF.getDecSeparator());
                            textVal = fF.formatNumber(numVal);
                        }
                        else {
                            parser.setAttribute(col, AbstractDBO.FORMAT_NAME, numberFormat);
                            parser.setAttribute(col, AbstractDBO.GRP_SEPARATOR_NAME, groupSeparator);
                            parser.setAttribute(col, AbstractDBO.DEC_SEPARATOR_NAME, decSeparator);
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
                        isNull = bigdecimal == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            if (metadata.getScale(j) > 0) {
                                parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.FLOAT_TYPE);
                            }
                            else {
                                parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.NUMERIC_TYPE);
                            }
                            textVal = "";
                        }
                        else {
                            if (fF != null) {
                                parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.FLOAT_TYPE);
                                parser.setAttribute(col, AbstractDBO.FORMAT_NAME, fF.getNumberFormat());
                                parser.setAttribute(col, AbstractDBO.GRP_SEPARATOR_NAME, fF.getGroupSeparator());
                                parser.setAttribute(col, AbstractDBO.DEC_SEPARATOR_NAME, fF.getDecSeparator());
                                textVal = fF.formatNumber(bigdecimal);
                            }
                            else if (metadata.getScale(j) > 0) {
                                parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.FLOAT_TYPE);
                                parser.setAttribute(col, AbstractDBO.FORMAT_NAME, numberFormat);
                                parser.setAttribute(col, AbstractDBO.GRP_SEPARATOR_NAME, groupSeparator);
                                parser.setAttribute(col, AbstractDBO.DEC_SEPARATOR_NAME, decSeparator);
                                textVal = numberFormatter.format(bigdecimal);
                            }
                            else {
                                parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.NUMERIC_TYPE);
                                textVal = bigdecimal.toString();
                            }
                        }
                    }
                        break;
                    case Types.CHAR :
                    case Types.VARCHAR : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.STRING_TYPE);
                        textVal = rs.getString(j);
                        isNull = textVal == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                    }
                        break;
                    case Types.CLOB : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.LONG_STRING_TYPE);
                        Clob clob = rs.getClob(j);
                        isNull = clob == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                        else {
                            InputStream is = clob.getAsciiStream();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            IOUtils.copy(is, baos);
                            is.close();
                            try {
                                textVal = new String(baos.toByteArray(), 0, (int) clob.length());
                            }
                            catch (SQLFeatureNotSupportedException exc) {
                                textVal = baos.toString();
                            }
                        }
                    }
                        break;
                    case Types.BLOB : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.BASE64_TYPE);
                        Blob blob = rs.getBlob(j);
                        isNull = blob == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
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
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.DEFAULT_TYPE);
                        textVal = rs.getString(j);
                        isNull = textVal == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                    }
                }
                if (textVal != null) {
                    text = doc.createTextNode(textVal);
                    col.appendChild(text);
                }
                if (!noKey && keyField.contains(new Integer(j))) {
                    if (textVal != null) {
                        if (colKey == null) {
                            colKey = textVal;
                        }
                        else {
                            colKey += "##" + textVal;
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
                    data = parser.createElement(doc, AbstractDBO.DATA_NAME);
                    parser.setAttribute(data, AbstractDBO.ID_NAME, id);
                }
            }
            else if ((colKey != null) && !colKey.equals(precKey)) {
                if (data != null) {
                    docRoot.appendChild(data);
                }
                data = parser.createElement(doc, AbstractDBO.DATA_NAME);
                parser.setAttribute(data, AbstractDBO.ID_NAME, id);
                for (Entry<String, String> keyAttrEntry : keyAttr.entrySet()) {
                    parser.setAttribute(data, keyAttrEntry.getKey(), keyAttrEntry.getValue());
                }
                keyAttr.clear();
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

    public void cleanup() {
        numberFormat = null;
        groupSeparator = null;
        decSeparator = null;
        parser = null;
        dateFormatter = null;
        numberFormatter = null;
    }

    public void setDateFormatter(SimpleDateFormat dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    public void setNumberFormatter(DecimalFormat numberFormatter) {
        this.numberFormatter = numberFormatter;
    }

    public void setDecSeparator(String decSeparator) {
        this.decSeparator = decSeparator;
    }

    public void setGroupSeparator(String groupSeparator) {
        this.groupSeparator = groupSeparator;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }

    public void setXMLUtils(XMLUtils parser) {
        this.parser = parser;
    }

    private FieldFormatter[] buildFormatterArray(ResultSetMetaData rsm,
            Map<String, FieldFormatter> fieldNameToFormatter, Map<String, FieldFormatter> fieldIdToFormatter)
            throws Exception {
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

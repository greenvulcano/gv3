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
package it.greenvulcano.gvesb.datahandling.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.txt.TextUtils;

/**
 * FieldFormatter class
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class FieldFormatter
{
    public final String   DEFAULT_NUMBER_FORMAT   = "#,##0.###";
    public final String   DEFAULT_GRP_SEPARATOR   = ".";
    public final String   DEFAULT_DEC_SEPARATOR   = ",";
    public final String   DEFAULT_DATE_FORMAT     = "yyyyMMdd HH:mm:ss";
    public final int      DEFAULT_LENGTH          = 64;
    public final String   DEFAULT_FILLER_CHAR     = " ";
    public final String   DEFAULT_TERMINATOR_CHAR = "";
    public final String   DEFAULT_TRIM            = "none";
    public final String   DEFAULT_PADDING         = "none";
    private final String  DEFAULT_FILLER_STRING   = "                                                   ";
    private final int     DEFAULT_FILLER_LENGTH   = 50;

    private DecimalFormat numberFormatter         = new DecimalFormat();

    private String        fieldName;
    private String        fieldId;
    private String        numberFormat            = this.DEFAULT_NUMBER_FORMAT;
    private String        groupSeparator          = this.DEFAULT_GRP_SEPARATOR;
    private String        decSeparator            = this.DEFAULT_DEC_SEPARATOR;
    private String        dateFormat              = this.DEFAULT_DATE_FORMAT;
    private String        dateTZoneOut            = DateUtils.getDefaultTimeZone().getID();
    private int           fieldLength             = this.DEFAULT_LENGTH;
    private String        fillerChar              = this.DEFAULT_FILLER_CHAR;
    private String        terminatorChar          = this.DEFAULT_TERMINATOR_CHAR;
    private String        trim                    = this.DEFAULT_TRIM;
    private String        padding                 = this.DEFAULT_PADDING;
    private boolean       isDefaultFillerChar     = false;

    /**
     *
     */
    public FieldFormatter()
    {
        this.numberFormatter.setRoundingMode(RoundingMode.FLOOR);
    }

    /**
     * @param node
     * @throws XMLConfigException
     */
    public void init(Node node) throws XMLConfigException
    {
        this.fieldName = XMLConfig.get(node, "@field-name", "NO_FIELD").toUpperCase();
        this.fieldId = XMLConfig.get(node, "@field-id", "-1");
        this.numberFormat = XMLConfig.get(node, "@number-format", this.DEFAULT_NUMBER_FORMAT);
        this.groupSeparator = XMLConfig.get(node, "@grouping-separator", this.DEFAULT_GRP_SEPARATOR);
        this.decSeparator = XMLConfig.get(node, "@decimal-separator", this.DEFAULT_DEC_SEPARATOR);
        this.dateFormat = XMLConfig.get(node, "@date-format", this.DEFAULT_DATE_FORMAT);
        List<String> dateParts = TextUtils.splitByStringSeparator(this.dateFormat, "::");
        this.dateFormat = dateParts.get(0);
        if (dateParts.size() > 1) {
            this.dateTZoneOut = dateParts.get(1);
        }
        this.fieldLength = XMLConfig.getInteger(node, "@field-length", this.DEFAULT_LENGTH);
        this.fillerChar = XMLConfig.get(node, "@filler-char", this.DEFAULT_FILLER_CHAR);
        this.isDefaultFillerChar = this.DEFAULT_FILLER_CHAR.equals(this.fillerChar);
        this.terminatorChar = XMLConfig.get(node, "@terminator-char", this.DEFAULT_TERMINATOR_CHAR).replaceAll("\\\\n", "\n").replaceAll(
                "\\\\r", "\r").replaceAll("\\\\t", "\t");
        this.trim = XMLConfig.get(node, "@trim", this.DEFAULT_TRIM);
        this.padding = XMLConfig.get(node, "@padding", this.DEFAULT_PADDING);
    }

    /**
     * @return the fieldName
     */
    public String getFieldName()
    {
        return this.fieldName;
    }

    /**
     * @return the fieldId
     */
    public String getFieldId()
    {
        return this.fieldId;
    }

    /**
     * @return the numberFormat
     */
    public String getNumberFormat()
    {
        return this.numberFormat;
    }

    /**
     * @return the groupSeparator
     */
    public String getGroupSeparator()
    {
        return this.groupSeparator;
    }

    /**
     * @return the decSeparator
     */
    public String getDecSeparator()
    {
        return this.decSeparator;
    }

    /**
     * @return the dateFormat
     */
    public String getDateFormat()
    {
        return this.dateFormat;
    }

    /**
     * @return the dateTZoneOut
     */
    public String getDateTZoneOut()
    {
        return this.dateTZoneOut;
    }

    /**
     * @return the fieldLength
     */
    public int getFieldLength()
    {
        return this.fieldLength;
    }

    /**
     * @return the fillerChar
     */
    public String getFillerChar()
    {
        return this.fillerChar;
    }

    /**
     * @return the terminatorChar
     */
    public String getTerminatorChar()
    {
        return this.terminatorChar;
    }

    /**
     * @return the trim
     */
    public String getTrim()
    {
        return this.trim;
    }

    /**
     * @return the padding
     */
    public String getPadding()
    {
        return this.padding;
    }

    /**
     *
     * @param number
     * @return the formatted number
     * @throws Exception
     */
    public String formatNumber(BigDecimal number) throws Exception
    {
        return formatField(formatNumber(number, null, null, null));
    }

    /**
     *
     * @param number
     * @return the formatted number
     * @throws Exception
     */
    public String formatNumber(double number) throws Exception
    {
        return formatField(formatNumber(number, null, null, null));
    }

    /**
     *
     * @param number
     * @param currNumberFormat
     * @param currGroupSeparator
     * @param currDecSeparator
     * @return the formatted number
     * @throws Exception
     */
    public String formatNumber(BigDecimal number, String currNumberFormat, String currGroupSeparator,
            String currDecSeparator) throws Exception
    {
        currNumberFormat = (currNumberFormat != null) ? currNumberFormat : this.numberFormat;
        currGroupSeparator = (currGroupSeparator != null) ? currGroupSeparator : this.groupSeparator;
        currDecSeparator = (currDecSeparator != null) ? currDecSeparator : this.decSeparator;

        String formattedNumber = null;

        DecimalFormatSymbols dfs = this.numberFormatter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator(currDecSeparator.charAt(0));
        dfs.setGroupingSeparator(currGroupSeparator.charAt(0));
        this.numberFormatter.setDecimalFormatSymbols(dfs);
        this.numberFormatter.applyPattern(currNumberFormat);
        formattedNumber = this.numberFormatter.format(number);

        return formattedNumber;
    }

    /**
     *
     * @param number
     * @param currNumberFormat
     * @param currGroupSeparator
     * @param currDecSeparator
     * @return the formatted number
     * @throws Exception
     */
    public String formatNumber(double number, String currNumberFormat, String currGroupSeparator, String currDecSeparator)
            throws Exception
    {
        currNumberFormat = (currNumberFormat != null) ? currNumberFormat : this.numberFormat;
        currGroupSeparator = (currGroupSeparator != null) ? currGroupSeparator : this.groupSeparator;
        currDecSeparator = (currDecSeparator != null) ? currDecSeparator : this.decSeparator;

        String formattedNumber = null;

        DecimalFormatSymbols dfs = this.numberFormatter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator(currDecSeparator.charAt(0));
        dfs.setGroupingSeparator(currGroupSeparator.charAt(0));
        this.numberFormatter.setDecimalFormatSymbols(dfs);
        this.numberFormatter.applyPattern(currNumberFormat);
        formattedNumber = this.numberFormatter.format(number);

        return formattedNumber;
    }

    /**
     *
     * @param number
     * @return the parsed String as {@link BigDecimal}
     * @throws Exception
     */
    public BigDecimal parseToBigDecimal(String number) throws Exception
    {
        return parseToBigDecimal(number, null, null, null);
    }

    /**
     *
     * @param number
     * @param currNumberFormat
     * @param currGroupSeparator
     * @param currDecSeparator
     * @return the parsed String as {@link BigDecimal}
     * @throws Exception
     */
    public BigDecimal parseToBigDecimal(String number, String currNumberFormat, String currGroupSeparator,
            String currDecSeparator) throws Exception
    {
        currNumberFormat = (currNumberFormat != null) ? currNumberFormat : this.numberFormat;
        currGroupSeparator = (currGroupSeparator != null) ? currGroupSeparator : this.groupSeparator;
        currDecSeparator = (currDecSeparator != null) ? currDecSeparator : this.decSeparator;

        BigDecimal parsedNumber = null;

        DecimalFormatSymbols dfs = this.numberFormatter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator(currDecSeparator.charAt(0));
        dfs.setGroupingSeparator(currGroupSeparator.charAt(0));
        this.numberFormatter.setDecimalFormatSymbols(dfs);
        this.numberFormatter.applyPattern(currNumberFormat);
        boolean isBigDecimal = this.numberFormatter.isParseBigDecimal();
        try {
            this.numberFormatter.setParseBigDecimal(true);
            parsedNumber = (BigDecimal) this.numberFormatter.parse(number);
        }
        finally {
            this.numberFormatter.setParseBigDecimal(isBigDecimal);
        }
        return parsedNumber;
    }

    /**
     *
     * @param dateTime
     * @return the formatted {@link Date}
     * @throws Exception
     */
    public String formatDate(Date dateTime) throws Exception
    {
        return formatField(formatDate(dateTime, null));
    }

    /**
     *
     * @param dateTime
     * @param currDateFormat
     * @return the formatted {@link Date}
     * @throws Exception
     */
    public String formatDate(Date dateTime, String currDateFormat) throws Exception
    {
        currDateFormat = (currDateFormat != null) ? currDateFormat : this.dateFormat;

        String formattedDate = DateUtils.dateToString(dateTime, currDateFormat, this.dateTZoneOut);

        return formattedDate;
    }

    /**
     *
     * @param dateTime
     * @return the parsed {@link Date}
     * @throws Exception
     */
    public Date parseDate(String dateTime) throws Exception
    {
        return parseDate(dateTime, null);
    }

    /**
     *
     * @param dateTime
     * @param currDateFormat
     * @return the parsed {@link Date}
     * @throws Exception
     */
    public Date parseDate(String dateTime, String currDateFormat) throws Exception
    {
        currDateFormat = (currDateFormat != null) ? currDateFormat : this.dateFormat;

        Date parsedDate = DateUtils.stringToDate(dateTime, currDateFormat, this.dateTZoneOut);

        return parsedDate;
    }

    /**
     *
     * @param field
     * @return
     */
    public String formatField(String field)
    {
        if (field != null) {
            field = trim(field);
            int l = field.length();
            if (l >= this.fieldLength) {
                field.substring(0, this.fieldLength);
            }
            field = pad(field);
        }
        else {
            field = "";
        }
        return field + this.terminatorChar;
    }

    /**
     *
     * @param field
     * @return
     */
    private String trim(String field)
    {
        if (!"none".equals(this.trim)) {
            if ("both".equals(this.trim)) {
                field.trim();
            }
            if ("left".equals(this.trim)) {
                while (" ".equals(field.charAt(0))) {
                    field = field.substring(1, field.length());
                }
            }
            if ("right".equals(this.trim)) {
                while (" ".equals(field.charAt(field.length() - 1))) {
                    field = field.substring(0, field.length() - 1);
                }
            }
        }
        return field;
    }

    /**
     *
     * @param field
     * @return
     */
    private String pad(String field)
    {
        if (!"none".equals(this.padding)) {
            StringBuffer pad = new StringBuffer();

            if (this.isDefaultFillerChar) {
                int l = this.fieldLength - field.length();
                while (pad.length() < l) {
                    pad.append(((pad.length() + this.DEFAULT_FILLER_LENGTH) < l)
                            ? this.DEFAULT_FILLER_STRING
                            : this.DEFAULT_FILLER_STRING.substring(0, l - pad.length()));
                }
            }
            else {
                int l = field.length();
                for (int i = l; i < this.fieldLength; i++) {
                    pad.append(this.fillerChar);
                }
            }
            if ("right".equals(this.padding)) {
                field = field + pad;
            }
            if ("left".equals(this.padding)) {
                field = pad + field;
            }
        }
        return field;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "FieldFormatter: fieldName[" + this.fieldName + "] fieldId[" + this.fieldId + "] numberFormat[" + this.numberFormat
                + "] groupSeparator[" + this.groupSeparator + "] decSeparator[" + this.decSeparator + "] dateFormat["
                + this.dateFormat + "] fieldLength[" + this.fieldLength + "] fillerChar[" + this.fillerChar + "] terminatorChar["
                + this.terminatorChar + "] padding[" + this.padding + "]";
    }
}

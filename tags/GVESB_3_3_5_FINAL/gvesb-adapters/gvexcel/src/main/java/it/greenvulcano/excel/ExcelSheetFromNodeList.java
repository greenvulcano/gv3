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
package it.greenvulcano.excel;

import it.greenvulcano.excel.config.ConfigurationHandler;
import it.greenvulcano.excel.config.WorkbookConfiguration;
import it.greenvulcano.excel.exception.ExcelException;
import it.greenvulcano.excel.format.CellFormat;
import it.greenvulcano.excel.format.ColumnFormat;
import it.greenvulcano.excel.format.FormatCache;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WriteException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class ExcelSheetFromNodeList {
    private static final String  DEFAULT_DATE_FORMAT      = "dd/MM/yyyy hh:mm:ss";
    private static final Locale  DAFAULT_NUMBER_LOCALE    = Locale.US;
    private static final boolean DEFAULT_GROUPING_ENABLED = false;
    private int                  offset                   = 0;
    private CellFormat           titleFormat              = null;
    private int                  titleRowsFromTop         = 0;
    private int                  titleColumnsFromLeft     = 0;
    private CellFormat           headerFormat             = null;
    private String               configName               = null;
    private NumberFormat         nf                       = null;
    private SimpleDateFormat     sdf                      = null;
    private FormatCache   formatCache          = null;

    public ExcelSheetFromNodeList(String confName, Locale locale, String dateFormat, FormatCache formatCache) throws ExcelException
    {
        ConfigurationHandler cf = ConfigurationHandler.getInstance();
        WorkbookConfiguration wbCfg = cf.getWBConf(confName);
        titleFormat = wbCfg.getTitleFormat();
        headerFormat = wbCfg.getHeaderFormat();
        titleRowsFromTop = wbCfg.getTitleRowsFromTop();
        titleColumnsFromLeft = wbCfg.getTitleColumnsFromLeft();
        offset = wbCfg.getSheetOffset();
        configName = confName;
        this.formatCache = formatCache;
        if (locale == null) {
            locale = DAFAULT_NUMBER_LOCALE;
        }
        nf = NumberFormat.getInstance(locale);
        nf.setGroupingUsed(DEFAULT_GROUPING_ENABLED);
        if (dateFormat == null) {
            dateFormat = DEFAULT_DATE_FORMAT;
        }
        sdf = new SimpleDateFormat(dateFormat);
    }

    public void fillSheet(NodeList nl, WritableSheet ws, String title, Set<String> exclude) throws ExcelException
    {
        int i = 0;
        Vector<ColumnFormat> cfVector = new Vector<ColumnFormat>();
        Vector<Integer> aiVector = new Vector<Integer>();
        if (exclude == null) {
            exclude = new TreeSet<String>();
        }
        if (nl == null) {
            throw new ExcelException("The NodeList is null");
        }
        try {
        	if (nl.getLength() == 0) {
        		return;
        	}
            Node node = nl.item(0);
            if (!node.hasAttributes()) {
                throw new ExcelException("The Node has no attributes");
            }
            NamedNodeMap attributes = node.getAttributes();
            int j = 0;
            for (int k = 0; k < attributes.getLength(); k++) {
                Node attNode = attributes.item(k);
                String attName = attNode.getNodeName();
                if (!exclude.contains(attName)) {
                    String attValue = getType(attNode.getNodeValue());
                    //ColumnFormat cf = new ColumnFormat(attValue, attName, configName);
                    ColumnFormat cf = formatCache.getColumnFormat(attValue, attName, configName);
                    Label label = new Label(j, offset, attName, headerFormat.getAsWritableCellFormat());
                    ws.addCell(label);
                    cfVector.add(j, cf);
                    aiVector.add(j, new Integer(k));
                    j++;
                }
            }

            i = j;
        }
        catch (WriteException exc) {
            throw new ExcelException(exc);
        }

        int j = offset + 2;
        try {
            for (int k = 0; k < nl.getLength(); k++) {
                Node node = nl.item(k);
                if (!node.hasAttributes()) {
                    throw new ExcelException("The Node has no attributes");
                }
                NamedNodeMap attributes = node.getAttributes();
                for (int l = 1; l <= i; l++) {
                    ColumnFormat cf = cfVector.get(l - 1);
                    Node attNode = attributes.item(aiVector.get(l - 1).intValue());
                    WritableCell writablecell = getCell(l, j, attNode, cf);
                    if (writablecell != null) {
                        ws.addCell(writablecell);
                    }
                }

                j++;
            }

            for (int l = 1; l <= i; l++) {
                ws.setColumnView(l - 1, cfVector.get(l - 1).getColumnSize());
            }

            setTitle(ws, title);
        }
        catch (WriteException exc) {
            throw new ExcelException(exc);
        }
    }

    private WritableCell getCell(int i, int j, Node node, ColumnFormat cf)
    {
        Object obj = null;
        if ((i < 1) || (j < 1)) {
            return null;
        }
        int type = cf.getType();
        String value = node.getNodeValue();
        if (type == CellFormat.STRING) {
            obj = new Label(i - 1, j - 1, value, cf.getFormat());
        }
        else if (type == CellFormat.DATE) {
            Date date = null;
            try {
                date = sdf.parse(value);
                value = cf.applyPattern(date);
            }
            catch (ParseException exc) {
                // do nothing
            }
            obj = new DateTime(i - 1, j - 1, date, cf.getFormat());
        }
        else if (type == CellFormat.NUMBER) {
            BigDecimal bigdecimal = null;
            try {
                Number number = nf.parse(value);
                bigdecimal = new BigDecimal(number.doubleValue());
            }
            catch (ParseException exc) {
                // do nothing
            }
            if (bigdecimal != null) {
                obj = new jxl.write.Number(i - 1, j - 1, bigdecimal.doubleValue(), cf.getFormat());
                value = cf.applyPattern(bigdecimal);
            }
            else {
                obj = new Label(i - 1, j - 1, null, cf.getFormat());
            }
        }
        if (value != null) {
            cf.setColumnSize(value.length());
        }
        return ((WritableCell) (obj));
    }

    private void setTitle(WritableSheet ws, String title) throws WriteException
    {
        int i = Math.min(titleRowsFromTop, offset);
        int j = Math.max(titleColumnsFromLeft, 0);
        Label label = new Label(j, i, title);
        label.setCellFormat(titleFormat.getAsWritableCellFormat());
        ws.addCell(label);
    }

    private String getType(String s)
    {
        try {
            nf.parse(s);
            return "NUMBER";
        }
        catch (ParseException exc) {
            // do nothing
        }

        try {
            sdf.parse(s);
            return "DATE";
        }
        catch (ParseException exc) {
            // do nothing
        }

        return "VARCHAR";
    }
}

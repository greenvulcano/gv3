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
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.ThreadUtils;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WriteException;

import org.apache.log4j.Logger;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class ExcelSheetFromResultSet {
    private static Logger logger               = GVLogger.getLogger(ExcelSheetFromResultSet.class);
    private int           offset               = 0;
    private CellFormat    titleFormat          = null;
    private int           titleRowsFromTop     = 0;
    private int           titleColumnsFromLeft = 0;
    private CellFormat    headerFormat         = null;
    private String        configName           = null;
    private FormatCache   formatCache          = null;

    public ExcelSheetFromResultSet(String confName, FormatCache formatCache) throws ExcelException
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
    }

    public void fillSheet(ResultSet resultset, WritableSheet ws, String title) throws ExcelException, 
            InterruptedException {
        String name = ws.getName();
        Vector<ColumnFormat> vector = new Vector<ColumnFormat>();
        if (resultset == null) {
            throw new ExcelException("The ResultSet is null");
        }
        int colCount;
        try {
            try {
                if (!resultset.isBeforeFirst()) {
                    resultset.beforeFirst();
                }
            }
            catch (SQLException exc) {
                logger.warn("The resultset has already been scrolled. Sheet = '" + name + "'");
            }
            ResultSetMetaData rsm = resultset.getMetaData();
            colCount = rsm.getColumnCount();
            for (int k = 1; k <= colCount; k++) {
                String cType = rsm.getColumnTypeName(k);
                String cName = rsm.getColumnName(k);
                //ColumnFormat cf = new ColumnFormat(cType, cName, configName);
                ColumnFormat cf = formatCache.getColumnFormat(cType, cName, configName);
                Label label = new Label(k - 1, offset, cName, headerFormat.getAsWritableCellFormat());
                ws.addCell(label);
                vector.add(k - 1, cf);
            }

        }
        catch (SQLException exc) {
            throw new ExcelException("Error fetching ResultSet", exc);
        }
        catch (WriteException exc) {
            throw new ExcelException(exc);
        }

        int j = offset + 2;
        try {
            while (resultset.next()) {
                ThreadUtils.checkInterrupted("ExcelSheetFromResultSet", configName, logger);
                for (int l = 1; l <= colCount; l++) {
                    ColumnFormat cf = vector.get(l - 1);
                    WritableCell wc = getCell(l, j, resultset, cf);
                    if (wc != null) {
                        ws.addCell(wc);
                    }
                }

                j++;
            }
            for (int i1 = 1; i1 <= colCount; i1++) {
                ws.setColumnView(i1 - 1, vector.get(i1 - 1).getColumnSize());
            }

            setTitle(ws, title);
        }
        catch (SQLException exc) {
            throw new ExcelException("Error fetching ResultSet", exc);
        }
        catch (WriteException exc) {
            throw new ExcelException(exc);
        }
    }

    private WritableCell getCell(int i, int j, ResultSet resultset, ColumnFormat cf) throws ExcelException
    {
        Object obj = null;
        if ((i < 1) || (j < 1)) {
            return null;
        }
        int type = cf.getType();
        String value = null;
        try {
            if (type == CellFormat.STRING) {
                value = resultset.getString(i);
                obj = new Label(i - 1, j - 1, value, cf.getFormat());
            }
            else if (type == CellFormat.DATE) {
                java.sql.Timestamp timestamp = resultset.getTimestamp(i);
                if (timestamp != null) {
                    java.sql.Date date = new Date(timestamp.getTime());
                    value = cf.applyPattern(date);
                    obj = new DateTime(i - 1, j - 1, date, cf.getFormat());
                }
                else {
                    obj = new Label(i - 1, j - 1, null, cf.getFormat());
                }
            }
            else if (type == CellFormat.NUMBER) {
                java.math.BigDecimal bigdecimal = resultset.getBigDecimal(i);
                if (bigdecimal != null) {
                    obj = new Number(i - 1, j - 1, bigdecimal.doubleValue(), cf.getFormat());
                    value = cf.applyPattern(bigdecimal);
                }
                else {
                    obj = new Label(i - 1, j - 1, null, cf.getFormat());
                }
            }
            if (value != null) {
                cf.setColumnSize(value.length());
            }
        }
        catch (SQLException exc) {
            throw new ExcelException("Error fetching ResultSet", exc);
        }
        return ((WritableCell) (obj));
    }

    private void setTitle(WritableSheet ws, String title) throws jxl.write.WriteException
    {
        int i = Math.min(titleRowsFromTop, offset);
        int j = Math.max(titleColumnsFromLeft, 0);
        Label label = new Label(j, i, title);
        label.setCellFormat(titleFormat.getAsWritableCellFormat());
        ws.addCell(label);
    }
}

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
package it.greenvulcano.excel.format;

import it.greenvulcano.excel.config.ConfigurationHandler;
import it.greenvulcano.excel.exception.ExcelException;
import jxl.write.WritableCellFormat;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class ColumnFormat {
    public static final String VARCHAR           = "VARCHAR";
    public static final String RAW               = "RAW";
    public static final String CHAR              = "CHAR";
    public static final String DATE              = "DATE";
    public static final String DATETIME          = "DATETIME";
    public static final String TIMESTAMP         = "TIMESTAMP";
    public static final String NUMBER            = "NUMBER";
    private int                columnSize        = 0;
    private String             columnName        = null;
    private CellFormat         format            = null;
    private float              headerScaleFactor = 0;

    public ColumnFormat(String colType, String colName, String configName) throws ExcelException
    {
        columnSize = 0;
        format = null;
        //System.out.println(colType);
        ConfigurationHandler configurationhandler = ConfigurationHandler.getInstance();
        if (colType.startsWith(VARCHAR) || colType.equals(CHAR) || RAW.equals(colType)) {
            format = configurationhandler.getWBConf(configName).getTextColumnFormat();
        }
        else if ((colType.equals(DATE)) || (colType.equals(DATETIME)) || (colType.equals(TIMESTAMP))) {
            format = configurationhandler.getWBConf(configName).getDateColumnFormat();
        }
        else if (colType.equals(NUMBER)) {
            format = configurationhandler.getWBConf(configName).getNumberColumnFormat();
        }
        else {
        	format = configurationhandler.getWBConf(configName).getTextColumnFormat();
        }
        headerScaleFactor = configurationhandler.getWBConf(configName).getHeaderFormat().getScaleFactor();
        columnName = colName;
        columnSize = format.getMinWidth();
    }

    public WritableCellFormat getFormat()
    {
        return format.getAsWritableCellFormat();
    }

    public int getType()
    {
        return format.getType();
    }

    public int getColumnSize()
    {
        return Math.max(Math.round(columnSize * format.getScaleFactor()), Math.round(columnName.length() * headerScaleFactor));
    }

    public void setColumnSize(int i)
    {
        if (i > columnSize) {
            columnSize = Math.min(i, format.getMaxWidth());
        }
    }

    public String applyPattern(Object value)
    {
        return format.applyPattern(value);
    }

}

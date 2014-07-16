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
package it.greenvulcano.excel.config;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.excel.exception.ExcelException;
import it.greenvulcano.excel.format.CellFormat;

import org.w3c.dom.Node;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class WorkbookConfiguration
{
    private static final int    DEF_OFFSET               = 0;
    private static final String DEF_SHEET_NAME           = "Sheet";
    private static final int    DEF_MAX_NUMBER_OF_SHEETS = 30;
    private static final int    DEF_ROWS_FROM_TOP        = 0;
    private static final int    DEF_COLS_FROM_LEFT       = 0;

    private CellFormat          titleFormat              = null;
    private CellFormat          headerFormat             = null;
    private CellFormat          textColumnFormat         = null;
    private CellFormat          numberColumnFormat       = null;
    private CellFormat          dateColumnFormat         = null;
    private String              sheetDefaultName         = null;
    private int                 sheetOffset              = 0;
    private int                 maxNumberOfSheets        = 0;
    private int                 titleRowsFromTop         = 0;
    private int                 titleColumnsFromLeft     = 0;
    private String              configName               = null;

    public WorkbookConfiguration(Node node) throws ExcelException
    {
        try {
            configName = XMLConfig.get(node, "@configName");
            sheetDefaultName = XMLConfig.get(node, "@sheetDefaultName", DEF_SHEET_NAME);
            maxNumberOfSheets = XMLConfig.getInteger(node, "@maxNumberOfSheets", DEF_MAX_NUMBER_OF_SHEETS);
            Node nF = XMLConfig.getNode(node, "SheetConfig/TableHeader/Format");
            headerFormat = new CellFormat(nF, CellFormat.STRING);
            nF = XMLConfig.getNode(node, "SheetConfig/Title/Format");
            titleFormat = new CellFormat(nF, CellFormat.STRING);
            nF = XMLConfig.getNode(node, "SheetConfig/TextColumn/Format");
            textColumnFormat = new CellFormat(nF, CellFormat.STRING);
            nF = XMLConfig.getNode(node, "SheetConfig/NumberColumn/Format");
            numberColumnFormat = new CellFormat(nF, CellFormat.NUMBER);
            nF = XMLConfig.getNode(node, "SheetConfig/DateColumn/Format");
            dateColumnFormat = new CellFormat(nF, CellFormat.DATE);
            sheetOffset = XMLConfig.getInteger(node, "SheetConfig/@offset", DEF_OFFSET);
            titleRowsFromTop = XMLConfig.getInteger(node, "SheetConfig/Title/@rowsFromTop", DEF_ROWS_FROM_TOP);
            titleColumnsFromLeft = XMLConfig.getInteger(node, "SheetConfig/Title/@columnsFromLeft", DEF_COLS_FROM_LEFT);
        }
        catch (Exception exc) {
            throw new ExcelException("Error initializing WorkBookConfiguration", exc);
        }
    }

    public WorkbookConfiguration(it.greenvulcano.excel.format.CellFormat cellformat,
            it.greenvulcano.excel.format.CellFormat cellformat1, it.greenvulcano.excel.format.CellFormat cellformat2,
            it.greenvulcano.excel.format.CellFormat cellformat3, it.greenvulcano.excel.format.CellFormat cellformat4,
            java.lang.String s, int i, int j, int k, int l, java.lang.String s1)
    {
        titleFormat = null;
        headerFormat = null;
        textColumnFormat = null;
        numberColumnFormat = null;
        dateColumnFormat = null;
        titleFormat = cellformat;
        headerFormat = cellformat1;
        textColumnFormat = cellformat2;
        numberColumnFormat = cellformat3;
        dateColumnFormat = cellformat4;
        sheetDefaultName = s;
        sheetOffset = i;
        maxNumberOfSheets = j;
        titleRowsFromTop = k;
        titleColumnsFromLeft = l;
        configName = s1;
    }

    public CellFormat getTitleFormat()
    {
        return new CellFormat(titleFormat);
    }

    public CellFormat getHeaderFormat()
    {
        return new CellFormat(headerFormat);
    }

    public CellFormat getTextColumnFormat()
    {
        return new CellFormat(textColumnFormat);
    }

    public CellFormat getNumberColumnFormat()
    {
        return new CellFormat(numberColumnFormat);
    }

    public CellFormat getDateColumnFormat()
    {
        return new CellFormat(dateColumnFormat);
    }

    public String getSheetDefaultName()
    {
        return sheetDefaultName;
    }

    public int getSheetOffset()
    {
        return sheetOffset;
    }

    public int getMaxNumberOfSheets()
    {
        return maxNumberOfSheets;
    }

    public int getTitleRowsFromTop()
    {
        return titleRowsFromTop;
    }

    public int getTitleColumnsFromLeft()
    {
        return titleColumnsFromLeft;
    }

    public String getConfigName()
    {
        return configName;
    }

}

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
import it.greenvulcano.excel.exception.ExcelException;
import it.greenvulcano.excel.format.FormatCache;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.List;
import java.util.Locale;

import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class ExcelWorkBook {
    private static Logger    logger              = GVLogger.getLogger(ExcelWorkBook.class);
    private static final int MAX_SHEET_NAME_SIZE = 31;
    private WritableWorkbook workbook            = null;
    private String           sheetDefaultName    = null;
    private int              maxNumberOfSheets   = 0;
    private int              currentSheet        = 0;
    private String           defaultFormatName   = null;
    ExcelSheetFromResultSet  excelFromRS         = null;
    private FormatCache      formatCache         = new FormatCache();

    public ExcelWorkBook(File file, String defaultFormatName) throws ExcelException
    {
        workbook = null;
        currentSheet = 0;
        NMDC.push();
        ConfigurationHandler.setLogContext();
        this.defaultFormatName = defaultFormatName;
        try {
            logger.debug("Start workbook creation from file '" + file + "'");
            workbook = jxl.Workbook.createWorkbook(file);
            logger.debug("Workbook created");
            init();
        }
        catch (IOException exc) {
            throw new ExcelException("Error on workbook creation from file '" + file + "'", exc);
        }
        finally {
            NMDC.pop();
        }
    }

    public ExcelWorkBook(OutputStream os, String defaultFormatName) throws ExcelException
    {
        workbook = null;
        currentSheet = 0;
        NMDC.push();
        ConfigurationHandler.setLogContext();
        this.defaultFormatName = defaultFormatName;
        try {
        	logger.debug("Start workbook creation from OutputStream");
            workbook = jxl.Workbook.createWorkbook(os);
            logger.debug("Workbook created");
            init();
        }
        catch (IOException exc) {
            throw new ExcelException("Error writing on stream", exc);
        }
        finally {
            NMDC.pop();
        }
    }

    public void fillWithResultSet(ResultSet resultset, String name, String title) throws ExcelException, 
            InterruptedException {
        fillWithResultSet(resultset, name, title, defaultFormatName);
    }

    public void fillWithResultSet(ResultSet resultset, String name, String title, String confName) throws ExcelException, 
            InterruptedException {
        NMDC.push();
        ConfigurationHandler.setLogContext();
        try {
        	logger.debug("Start creating sheet '" + name + "'");
            if (resultset == null) {
                throw new ExcelException("ResultSet cannot be null");
            }
            WritableSheet ws = getSheet(name);
            ExcelSheetFromResultSet es;
            if (confName.compareTo(defaultFormatName) == 0) {
                es = excelFromRS;
            }
            else {
                es = new ExcelSheetFromResultSet(confName, formatCache);
            }
            es.fillSheet(resultset, ws, title);
            logger.debug("Created sheet '" + name + "'");
        }
        catch (ExcelException exc) {
            throw exc;
        }
        finally {
            NMDC.pop();
        }
    }

    public void fillWithNodeList(NodeList nodelist, List<String> fields, String name, String title, String confName, Locale locale, String dateFormat, boolean forceTextFields)
            throws ExcelException
    {
        NMDC.push();
        ConfigurationHandler.setLogContext();
        try {
            logger.debug("Start creating sheet '" + name + "'");
            WritableSheet writablesheet = getSheet(name);
            if (confName == null) {
                confName = defaultFormatName;
            }
            ExcelSheetFromNodeList es = new ExcelSheetFromNodeList(confName, locale, dateFormat, forceTextFields, formatCache);
            es.fillSheet(nodelist, writablesheet, title, fields);
            logger.debug("Created sheet '" + name + "'");
        }
        catch (ExcelException excelexception) {
            throw excelexception;
        }
        finally {
            NMDC.pop();
        }
    }

    public void closeWorkBook() throws ExcelException
    {
        NMDC.push();
        ConfigurationHandler.setLogContext();
        try {
            workbook.write();
            workbook.close();
            logger.debug("Workbook closed");
        }
        catch (Exception exc) {
            throw new ExcelException("Error closing workbook", exc);
        }
        finally {
            NMDC.pop();
            formatCache.reset();
        }
    }

    private void init() throws ExcelException
    {
        excelFromRS = new ExcelSheetFromResultSet(defaultFormatName, formatCache);
        ConfigurationHandler ch = ConfigurationHandler.getInstance();
        sheetDefaultName = ch.getWBConf(defaultFormatName).getSheetDefaultName();
        maxNumberOfSheets = ch.getWBConf(defaultFormatName).getMaxNumberOfSheets();
    }

    private WritableSheet getSheet(String name) throws ExcelException
    {
        if (currentSheet > maxNumberOfSheets) {
            throw new ExcelException("Maximum number of sheets exceeded");
        }
        if (name == null) {
            name = sheetDefaultName + " " + currentSheet;
        }
        if (name.length() == 0) {
            name = sheetDefaultName + " " + currentSheet;
        }
        if (workbook.getSheet(name) != null) {
            name = sheetDefaultName + " " + currentSheet;
        }
        if (name.length() > MAX_SHEET_NAME_SIZE) {
            name = name.substring(0, MAX_SHEET_NAME_SIZE);
        }
        WritableSheet writablesheet = workbook.createSheet(name, currentSheet);
        currentSheet++;
        return writablesheet;
    }

}

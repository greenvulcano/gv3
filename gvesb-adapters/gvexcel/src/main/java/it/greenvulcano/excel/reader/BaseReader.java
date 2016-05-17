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
package it.greenvulcano.excel.reader;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.excel.exception.ExcelException;
import it.greenvulcano.util.thread.ThreadUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.4.0 20/apr/2013
 * @author GreenVulcano Developer Team
 *
 */
public abstract class BaseReader
{
    public static class ColumnSkipper {
        private boolean                    active = false;
        private Map<Integer, Set<Integer>> sheets = new HashMap<Integer, Set<Integer>>();
        
        public void init(Node node)  throws ExcelException {
            try {
                NodeList shL = XMLConfig.getNodeList(node, "sheet");
                for (int i = 0; i < shL.getLength(); i++) {
                    Node shN = shL.item(i);
                    int shNum = XMLConfig.getInteger(shN, "@id");
                    String col = XMLConfig.get(shN, "@col");
                    Set<Integer> cSet = new HashSet<Integer>();
                    StringTokenizer st = new StringTokenizer(col, " ,");
                    while (st.hasMoreTokens()) {
                        cSet.add(Integer.valueOf(st.nextToken().trim()));
                    }
                    sheets.put(shNum, cSet);
                }
                active = !sheets.isEmpty();
            }
            catch (Exception exc) {
                throw new ExcelException("Error initializing ColumnSkipper", exc);
            }
        }
        
        public boolean skip(int sNum, int cNum) {
            if (active) {
                Set<Integer> sheet = sheets.get(sNum);
                if (sheet != null) {
                    return sheet.contains(cNum);
                }
                return false;
            }
            return false;
        }
        
        public void addFilter(int sNum, int cNum) {
            Set<Integer> sheet = sheets.get(sNum);
            if (sheet == null) {
                sheet = new HashSet<Integer>();
                sheets.put(sNum, sheet);
            }
            sheet.add(cNum);
            active = true;
        }
    }

    protected DataFormatter    formatter  = null;
    protected FormulaEvaluator evaluator  = null;
    protected ColumnSkipper    colSkipper = new ColumnSkipper();
    
    public void init(Node node) throws ExcelException {
        try {
            Node csNode = XMLConfig.getNode(node, "ColumnSkipper");
            if (csNode != null) {
                colSkipper.init(csNode);
            }
        }
        catch (ExcelException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new ExcelException("Error initializing ExcelReader", exc);
        }
    }
    
    public void processExcel(String filePath) throws ExcelException {
        cleanUp();
        try {
            processExcel(new File(filePath));
        }
        catch (ExcelException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new ExcelException("Error parsing Excel", exc);
        }
    }

    public void processExcel(File file) throws ExcelException {
        cleanUp();
        try {
            processExcel(new BufferedInputStream(new FileInputStream(file)));
        }
        catch (ExcelException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new ExcelException("Error parsing Excel", exc);
        }
    }

    public void processExcel(InputStream in) throws ExcelException, InterruptedException {
        cleanUp();
        Workbook workbook = null;

        try {
            // Open the workbook and then create the FormulaEvaluator and
            // DataFormatter instances that will be needed to, respectively,
            // force evaluation of formula found in cells and create a
            // formatted String encapsulating the cells contents.
            workbook = WorkbookFactory.create(in);
            evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            formatter = new DataFormatter(true);

            processExcel(workbook);
        }
        catch (ExcelException exc) {
            throw exc;
        }
        catch (Exception exc) {
            ThreadUtils.checkInterrupted(exc);
            throw new ExcelException("Error parsing WorkBook", exc);
        }
        finally {
            workbook  = null;
            formatter = null;
            evaluator = null;
        }
    }

    public void processExcel(Workbook workbook) throws ExcelException {
        cleanUp();
        try {
            startProcess();

            // Discover how many sheets there are in the workbook....
            int numSheets = workbook.getNumberOfSheets();

            // and then iterate through them.
            for (int i = 0; i < numSheets; i++) {

                // Get a reference to a sheet and check to see if it contains
                // any rows.
                Sheet sheet = workbook.getSheetAt(i);
                if (processSheet(sheet, i)) {
                    // Note down the index number of the bottom-most row and
                    // then iterate through all of the rows on the sheet starting
                    // from the very first row - number 1 - even if it is missing.
                    // Recover a reference to the row and then call another method
                    // which will strip the data from the cells and build lines
                    // for inclusion in the resulting object.
                    int lastRowNum = sheet.getLastRowNum();
                    for (int j = 0; j <= lastRowNum; j++) {
                        if (j % 10 == 0) {
                            ThreadUtils.checkInterrupted(getClass().getSimpleName(), "ExcelFile", null);
                        }
                        Row row = sheet.getRow(j);
                        processRow(row, i, j);
                    }
                }
            }
            
            endProcess();
        }
        catch (Exception exc) {
            throw new ExcelException("Error parsing Excel", exc);
        }
    }

    public ColumnSkipper getColSkipper() {
        return this.colSkipper;
    }
    
    public void setColSkipper(ColumnSkipper colSkipper) {
        this.colSkipper = colSkipper;
    }
    
    // processing methods
    protected abstract void startProcess() throws ExcelException;

    protected abstract boolean processSheet(Sheet sheet, int sNum) throws ExcelException;

    protected abstract void processRow(Row row, int sNum, int rNum) throws ExcelException;

    protected abstract void endProcess() throws ExcelException;

    // result methods
    public abstract Object getAsObject() throws ExcelException;

    public abstract String getAsString() throws ExcelException;

    public abstract byte[] getAsBytes() throws ExcelException;
    
    // cleanup methods
    public abstract void cleanUp();
    
    public abstract void destroy();
    
}

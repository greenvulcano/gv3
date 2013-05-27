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
package it.greenvulcano.excel.reader;

import it.greenvulcano.excel.exception.ExcelException;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.File;
import java.io.InputStream;

import jxl.Cell;
import jxl.CellType;
import jxl.Range;
import jxl.Sheet;
import jxl.Workbook;
import jxl.biff.SheetRangeImpl;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.CellFormat;
import jxl.format.Colour;
import jxl.format.Font;
import jxl.format.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @version 3.0.0 14/ott/2010
 * @author GreenVulcano Developer Team
 *
 */
public class ToXMLReader
{
    /**
     *
     */
    private static final String NUMBER_ATTR = "number";
    private Document            doc         = null;

    public void processWorkBook(String filePath, boolean onlyData) throws ExcelException
    {
        doc = null;
        try {
            processWorkBook(new File(filePath), onlyData);
        }
        catch (ExcelException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new ExcelException("Error parsing WorkBook", exc);
        }
    }

    public void processWorkBook(File file, boolean onlyData) throws ExcelException
    {
        doc = null;
        try {
            processWorkBook(Workbook.getWorkbook(file), onlyData);
        }
        catch (ExcelException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new ExcelException("Error parsing WorkBook", exc);
        }
    }

    public void processWorkBook(InputStream in, boolean onlyData) throws ExcelException
    {
        doc = null;
        try {
            processWorkBook(Workbook.getWorkbook(in), onlyData);
        }
        catch (ExcelException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new ExcelException("Error parsing WorkBook", exc);
        }
    }

    public void processWorkBook(Workbook wb, boolean onlyData) throws ExcelException
    {
        doc = null;
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            doc = parser.newDocument("workbook");
            Element root = doc.getDocumentElement();

            for (int sN = 0; sN < wb.getNumberOfSheets(); sN++) {
                Sheet sh = wb.getSheet(sN);

                Element sheet = parser.createElement(doc, "sheet");
                root.appendChild(sheet);
                parser.setAttribute(sheet, NUMBER_ATTR, String.valueOf(sN));
                Node name = sheet.appendChild(parser.createElement(doc, "name"));
                name.appendChild(doc.createCDATASection(sh.getName()));

                boolean checkRange = false;
                Range[] mergedCells = sh.getMergedCells();
                if (mergedCells != null && mergedCells.length > 0) {
                    checkRange = true;
                    Element mergedCellsEl = parser.createElement(doc, "mergedCells");
                    sheet.appendChild(mergedCellsEl);
                    for (int i = 0; i < mergedCells.length; i++) {
                        Range range = mergedCells[i];
                        Element rangeEl = parser.createElement(doc, "range");
                        parser.setAttribute(rangeEl, NUMBER_ATTR, String.valueOf(i));
                        Cell topLeft = range.getTopLeft();
                        Cell bottomRight = range.getBottomRight();
                        parser.setAttribute(rangeEl, "start_row", String.valueOf(topLeft.getRow()));
                        parser.setAttribute(rangeEl, "start_col", String.valueOf(topLeft.getColumn()));
                        parser.setAttribute(rangeEl, "end_row", String.valueOf(bottomRight.getRow()));
                        parser.setAttribute(rangeEl, "end_col", String.valueOf(bottomRight.getColumn()));
                        mergedCellsEl.appendChild(rangeEl);
                    }
                }

                for (int i = 0; i < sh.getRows(); i++) {
                    Element row = parser.createElement(doc, "row");
                    parser.setAttribute(row, NUMBER_ATTR, String.valueOf(i));
                    sheet.appendChild(row);

                    Cell[] rowCells = sh.getRow(i);

                    for (int j = 0; j < rowCells.length; j++) {
                        if ((rowCells[j].getType() != CellType.EMPTY) || (rowCells[j].getCellFormat() != null)) {
                            Element col = parser.createElement(doc, "col");
                            parser.setAttribute(col, NUMBER_ATTR, String.valueOf(j));
                            row.appendChild(col);
                            Node data = col.appendChild(parser.createElement(doc, "data"));
                            data.appendChild(doc.createCDATASection(rowCells[j].getContents()));

                            if (checkRange) {
                                SheetRangeImpl currentRange = new SheetRangeImpl(sh, j, i, j, i);
                                for (int k = 0; k < mergedCells.length; k++) {
                                    Range range = mergedCells[k];
                                    if (range instanceof SheetRangeImpl) {
                                        if (currentRange.intersects((SheetRangeImpl) range)) {
                                            parser.setAttribute(col, "range", String.valueOf(k));
                                        }
                                    }
                                }
                            }

                            if (!onlyData) {
                                CellFormat format = rowCells[j].getCellFormat();
                                if (format != null) {
                                    Element frmt = parser.createElement(doc, "format");
                                    col.appendChild(frmt);
                                    parser.setAttribute(frmt, "wrap", String.valueOf(format.getWrap()));
                                    parser.setAttribute(frmt, "align",
                                            String.valueOf(format.getAlignment().getDescription()));
                                    parser.setAttribute(frmt, "valign",
                                            String.valueOf(format.getVerticalAlignment().getDescription()));
                                    parser.setAttribute(frmt, "orientation",
                                            String.valueOf(format.getOrientation().getDescription()));

                                    Font font = format.getFont();
                                    Element fnt = parser.createElement(doc, "font");
                                    frmt.appendChild(fnt);
                                    parser.setAttribute(fnt, "name", String.valueOf(font.getName()));
                                    parser.setAttribute(fnt, "point_size", String.valueOf(font.getPointSize()));
                                    parser.setAttribute(fnt, "bold_weight", String.valueOf(font.getBoldWeight()));
                                    parser.setAttribute(fnt, "italic", String.valueOf(font.isItalic()));
                                    parser.setAttribute(fnt, "underline",
                                            String.valueOf(font.getUnderlineStyle().getDescription()));
                                    parser.setAttribute(fnt, "colour",
                                            String.valueOf(font.getColour().getDescription()));
                                    parser.setAttribute(fnt, "script",
                                            String.valueOf(font.getScriptStyle().getDescription()));

                                    if (format.getBackgroundColour() != Colour.DEFAULT_BACKGROUND
                                            || format.getPattern() != Pattern.NONE) {
                                        Element bckg = parser.createElement(doc, "background");
                                        frmt.appendChild(bckg);
                                        parser.setAttribute(bckg, "colour",
                                                String.valueOf(format.getBackgroundColour().getDescription()));
                                        parser.setAttribute(bckg, "pattern",
                                                String.valueOf(format.getPattern().getDescription()));
                                    }

                                    if (format.getBorder(Border.TOP) != BorderLineStyle.NONE
                                            || format.getBorder(Border.BOTTOM) != BorderLineStyle.NONE
                                            || format.getBorder(Border.LEFT) != BorderLineStyle.NONE
                                            || format.getBorder(Border.RIGHT) != BorderLineStyle.NONE) {
                                        Element brd = parser.createElement(doc, "border");
                                        frmt.appendChild(brd);
                                        parser.setAttribute(brd, "top",
                                                String.valueOf(format.getBorder(Border.TOP).getDescription()));
                                        parser.setAttribute(brd, "bottom",
                                                String.valueOf(format.getBorder(Border.BOTTOM).getDescription()));
                                        parser.setAttribute(brd, "left",
                                                String.valueOf(format.getBorder(Border.LEFT).getDescription()));
                                        parser.setAttribute(brd, "right",
                                                String.valueOf(format.getBorder(Border.RIGHT).getDescription()));
                                    }

                                    if (!format.getFormat().getFormatString().equals("")) {
                                        Element frmtS = parser.createElement(doc, "format_string");
                                        frmt.appendChild(frmtS);
                                        parser.setAttribute(frmtS, "string",
                                                String.valueOf(format.getFormat().getFormatString()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception exc) {
            doc = null;
            throw new ExcelException("Error parsing WorkBook", exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    public Document getXML() throws ExcelException
    {
        if (doc == null) {
            throw new ExcelException("No WorkBook parsed");
        }
        return doc;
    }

    public byte[] getXMLAsBytes() throws ExcelException
    {
        try {
            return XMLUtils.serializeDOMToByteArray_S(getXML());
        }
        catch (XMLUtilsException exc) {
            throw new ExcelException("Error serializing Document", exc);
        }
    }

    public String getXMLAsString() throws ExcelException
    {
        try {
            return XMLUtils.serializeDOM_S(getXML());
        }
        catch (XMLUtilsException exc) {
            throw new ExcelException("Error serializing Document", exc);
        }
    }

    public void cleanUp()
    {
        doc = null;
    }

    public void destroy()
    {
        cleanUp();
    }
}

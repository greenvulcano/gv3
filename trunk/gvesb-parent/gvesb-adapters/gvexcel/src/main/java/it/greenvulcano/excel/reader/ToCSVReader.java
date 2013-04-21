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
import it.greenvulcano.util.txt.TextUtils;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.4.0 20/apr/2013
 * @author GreenVulcano Developer Team
 * 
 * Based on ToCSV POI example. Convert an Excel spreadsheet into a CSV
 * file. This class makes the following assumptions; 
 * <list> 
 * <li>1. Where the Excel workbook contains more that one worksheet, then a single
 * CSV file will contain the data from all of the worksheets.</li> 
 * <li>2. The data matrix contained in the CSV file will be square. This
 * means that the number of fields in each record of the CSV file will
 * match the number of cells in the longest row found in the Excel
 * workbook. Any short records will be 'padded' with empty fields - an
 * empty field is represented in the the CSV file in this way - ,,.</li>
 * <li>3. Empty fields will represent missing cells.</li> 
 * <li>4. A record consisting of empty fields will be used to represent an empty
 * row in the Excel workbook.</li> 
 * </list> 
 * Therefore, if the worksheet looked like this;
 * 
 * <pre>
 *  ___________________________________________
 *     |       |       |       |       |       |
 *     |   A   |   B   |   C   |   D   |   E   |
 *  ___|_______|_______|_______|_______|_______|
 *     |       |       |       |       |       |
 *   1 |   1   |   2   |   3   |   4   |   5   |
 *  ___|_______|_______|_______|_______|_______|
 *     |       |       |       |       |       |
 *   2 |       |       |       |       |       |
 *  ___|_______|_______|_______|_______|_______|
 *     |       |       |       |       |       |
 *   3 |       |   A   |       |   B   |       |
 *  ___|_______|_______|_______|_______|_______|
 *     |       |       |       |       |       |
 *   4 |       |       |       |       |   Z   |
 *  ___|_______|_______|_______|_______|_______|
 *     |       |       |       |       |       |
 *   5 | 1,400 |       |  250  |       |       |
 *  ___|_______|_______|_______|_______|_______|
 * 
 * </pre>
 * 
 * Then, the resulting CSV file will contain the following lines (records);
 * <pre>
 * 1,2,3,4,5
 * ,,,,
 * ,A,,B,
 * ,,,,Z
 * "1,400",,250,,
 * </pre>
 * <p>
 * Typically, the comma is used to separate each of the fields that,
 * together, constitute a single record or line within the CSV file.
 * This is not however a hard and fast rule and so this class allows the
 * user to determine which character is used as the field separator and
 * assumes the comma if none other is specified.
 * </p>
 * <p>
 * If a field contains the separator then it will be escaped. If the
 * file should obey Excel's CSV formatting rules, then the field will be
 * surrounded with speech marks whilst if it should obey UNIX
 * conventions, each occurrence of the separator will be preceded by the
 * backslash character.
 * </p>
 * <p>
 * If a field contains an end of line (EOL) character then it too will
 * be escaped. If the file should obey Excel's CSV formatting rules then
 * the field will again be surrounded by speech marks. On the other
 * hand, if the file should follow UNIX conventions then a single
 * backslash will precede the EOL character. There is no single
 * applicable standard for UNIX and some applications replace the CR
 * with \r and the LF with \n but this class will not do so.
 * </p>
 * <p>
 * If the field contains double quotes then that character will be
 * escaped. It seems as though UNIX does not define a standard for this
 * whilst Excel does. Should the CSV file have to obey Excel's
 * formatting rules then the speech mark character will be escaped with
 * a second set of speech marks. Finally, an enclosing set of speah
 * marks will also surround the entire field. Thus, if the following
 * line of text appeared in a cell - "Hello" he said - it would look
 * like this when converted into a field within a CSV file - """Hello""
 * he said".
 * </p>
 * 
 */
public class ToCSVReader extends BaseReader
{
    /**
    *
    */
    protected static final String DEFAULT_END_LINE     = "LF";

    /**
   *
   */
    protected static final String DEFAULT_SEPARATOR    = ",";


    /**
     * Identifies that the CSV file should obey Excel's formatting conventions
     * with regard to escaping certain embedded characters - the field
     * separator, speech mark and end of line (EOL) character
     */
    public static final int       EXCEL_STYLE_ESCAPING = 0;

    /**
     * Identifies that the CSV file should obey UNIX formatting conventions with
     * regard to escaping certain embedded characters - the field separator and
     * end of line (EOL) character
     */
    public static final int       UNIX_STYLE_ESCAPING  = 1;

    private String                endLine;
    private String                separator;
    private int                   formattingConvention = -1;

    private StringBuilder         csvData              = null;
    private List<List<String>>    csvRows              = null;
    private int                   maxRowWidth          = 0;


    public ToCSVReader() {
        setSeparator(DEFAULT_SEPARATOR);
        setEndLine(TextUtils.getEOL(DEFAULT_END_LINE));
        formattingConvention = EXCEL_STYLE_ESCAPING;
    }
    
    @Override
    public void init(Node node) throws ExcelException {
        super.init(node);
        try {
            setSeparator(XMLConfig.get(node, "@separator", DEFAULT_SEPARATOR).replaceAll("\\\\t", "\t"));
            setEndLine(TextUtils.getEOL(XMLConfig.get(node, "@end-line", DEFAULT_END_LINE)));
            String formConv = XMLConfig.get(node, "@formatting-style", "excel");
            if (formConv.equals("excel")) {
                setFormattingConvention(EXCEL_STYLE_ESCAPING);
            }
            else if (formConv.equals("unix")) {
                setFormattingConvention(UNIX_STYLE_ESCAPING);
            }
            else {
                throw new ExcelException("Error initializing ExcelReader: invalid formatting [" + formConv + "]");
            }
        }
        catch (ExcelException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new ExcelException("Error initializing ExcelReader", exc);
        }
    }

    public void setEndLine(String endLine) {
        this.endLine = endLine;
    }
    
    public String getEndLine() {
        return this.endLine;
    }
    
    public void setSeparator(String separator) {
        this.separator = separator;
    }
    
    public String getSeparator() {
        return this.separator;
    }

    public int getFormattingConvention() {
        return this.formattingConvention;
    }

    public void setFormattingConvention(int formattingConvention) {
        this.formattingConvention = formattingConvention;
    }
    
   
    @Override
    protected void startProcess() throws ExcelException {
        csvData = new StringBuilder();
        csvRows = new ArrayList<List<String>>();
    }

    @Override
    protected boolean processSheet(Sheet sheet, int sNum) throws ExcelException {
        return sheet.getPhysicalNumberOfRows() > 0;
    }
    
    /**
     * Called to convert a row of cells into a line of data that can be output
     * to the CSV file.
     * 
     * @param row
     *        An instance of either the HSSFRow or XSSFRow classes that
     *        encapsulates information about a row of cells recovered from an
     *        Excel workbook.
     */
    @Override
    protected void processRow(Row row, int sNum, int rNum) throws ExcelException {
        List<String> csvLine = new ArrayList<String>();

        // Check to ensure that a row was recovered from the sheet as it is
        // possible that one or more rows between other populated rows could be
        // missing - blank. If the row does contain cells then...
        if (row != null) {

            // Get the index for the right most cell on the row and then
            // step along the row from left to right recovering the contents
            // of each cell, converting that into a formatted String and
            // then storing the String into the csvLine ArrayList.
            int lastCellNum = row.getLastCellNum();
            int skipped = 0;
            for (int i = 0; i <= lastCellNum; i++) {
                if (colSkipper.skip(sNum, i)) {
                    skipped++;
                    continue;
                }
                Cell cell = row.getCell(i);
                if (cell == null) {
                    csvLine.add("");
                }
                else {
                    if (cell.getCellType() != Cell.CELL_TYPE_FORMULA) {
                        csvLine.add(formatter.formatCellValue(cell));
                    }
                    else {
                        csvLine.add(formatter.formatCellValue(cell, evaluator));
                    }
                }
            }
            // Make a note of the index number of the right most cell. This value
            // will later be used to ensure that the matrix of data in the CSV
            // file is square.
            if (lastCellNum > maxRowWidth) {
                maxRowWidth = lastCellNum - skipped;
            }
        }
        csvRows.add(csvLine);
    }


    /**
     * Called to actually save the data recovered from the Excel workbook as a
     * CSV file.
     * 
     */
    @Override
    protected void endProcess() throws ExcelException {
        List<String> line = null;
        String csvLineElement = null;

        // Step through the elements of the ArrayList that was used to hold
        // all of the data recovered from the Excel workbooks' sheets, rows
        // and cells.
        for (int i = 0; i < csvRows.size(); i++) {
            // Get an element from the ArrayList that contains the data for
            // the workbook. This element will itself be an ArrayList
            // containing Strings and each String will hold the data recovered
            // from a single cell. The for() loop is used to recover elements
            // from this 'row' ArrayList one at a time and to write the Strings
            // away to a StringBuffer thus assembling a single line for inclusion
            // in the CSV file. If a row was empty or if it was short, then
            // the ArrayList that contains it's data will also be shorter than
            // some of the others. Therefore, it is necessary to check within
            // the for loop to ensure that the ArrayList contains data to be
            // processed. If it does, then an element will be recovered and
            // appended to the StringBuffer.
            line = csvRows.get(i);
            for (int j = 0; j < this.maxRowWidth; j++) {
                if (line.size() > j) {
                    csvLineElement = line.get(j);
                    if (csvLineElement != null) {
                        csvData.append(escapeEmbeddedCharacters(csvLineElement));
                    }
                }
                if (j < (maxRowWidth - 1)) {
                    csvData.append(separator);
                }
            }

            // Condition the inclusion of new line characters so as to
            // avoid an additional, superfluous, new line at the end of
            // the file.
            if (i < (csvRows.size() - 1)) {
                csvData.append(endLine);
            }
        }
    }

    /**
     * Checks to see whether the field - which consists of the formatted
     * contents of an Excel worksheet cell encapsulated within a String -
     * contains any embedded characters that must be escaped. The method is able
     * to comply with either Excel's or UNIX formatting conventions in the
     * following manner;
     * 
     * With regard to UNIX conventions, if the field contains any embedded field
     * separator or EOL characters they will each be escaped by prefixing a
     * leading backspace character. These are the only changes that have yet
     * emerged following some research as being required.
     * 
     * Excel has other embedded character escaping requirements, some that
     * emerged from empirical testing, other through research. Firstly, with
     * regards to any embedded speech marks ("), each occurrence should be
     * escaped with another speech mark and the whole field then surrounded with
     * speech marks. Thus if a field holds <em>"Hello" he said</em> then it
     * should be modified to appear as <em>"""Hello"" he said"</em>.
     * Furthermore, if the field contains either embedded separator or EOL
     * characters, it should also be surrounded with speech marks. As a result
     * <em>1,400</em> would become <em>"1,400"</em> assuming that the comma is
     * the required field separator. This has one consequence in, if a field
     * contains embedded speech marks and embedded separator characters, checks
     * for both are not required as the additional set of speech marks that
     * should be placed around ay field containing embedded speech marks will
     * also account for the embedded separator.
     * 
     * It is worth making one further note with regard to embedded EOL
     * characters. If the data in a worksheet is exported as a CSV file using
     * Excel itself, then the field will be surounded with speech marks. If the
     * resulting CSV file is then re-imports into another worksheet, the EOL
     * character will result in the original simgle field occupying more than
     * one cell. This same 'feature' is replicated in this classes behaviour.
     * 
     * @param field
     *        An instance of the String class encapsulating the formatted
     *        contents of a cell on an Excel worksheet.
     * @return A String that encapsulates the formatted contents of that Excel
     *         worksheet cell but with any embedded separator, EOL or speech
     *         mark characters correctly escaped.
     */
    private String escapeEmbeddedCharacters(String field) {
        StringBuffer buffer = null;

        // If the fields contents should be formatted to confrom with Excel's
        // convention....
        if (this.formattingConvention == EXCEL_STYLE_ESCAPING) {

            // Firstly, check if there are any speech marks (") in the field;
            // each occurrence must be escaped with another set of spech marks
            // and then the entire field should be enclosed within another
            // set of speech marks. Thus, "Yes" he said would become
            // """Yes"" he said"
            if (field.contains("\"")) {
                buffer = new StringBuffer(field.replaceAll("\"", "\\\"\\\""));
                buffer.insert(0, "\"");
                buffer.append("\"");
            }
            else {
                // If the field contains either embedded separator or EOL
                // characters, then escape the whole field by surrounding it
                // with speech marks.
                buffer = new StringBuffer(field);
                if ((buffer.indexOf(separator)) > -1 || (buffer.indexOf("\n")) > -1) {
                    buffer.insert(0, "\"");
                    buffer.append("\"");
                }
            }
            return (buffer.toString().trim());
        }
        // The only other formatting convention this class obeys is the UNIX one
        // where any occurrence of the field separator or EOL character will
        // be escaped by preceding it with a backslash.
        else {
            if (field.contains(this.separator)) {
                field = field.replaceAll(this.separator, ("\\\\" + this.separator));
            }
            if (field.contains("\n")) {
                field = field.replaceAll("\n", "\\\\\n");
            }
            return (field);
        }
    }

    @Override
    public Object getAsObject() throws ExcelException {
        if (csvData == null) {
            throw new ExcelException("No Excel parsed");
        }
        return csvData.toString();
    }

    @Override
    public String getAsString() throws ExcelException {
        return getAsObject().toString();
    }

    @Override
    public byte[] getAsBytes() throws ExcelException {
        return getAsString().getBytes();
    }

    @Override
    public void cleanUp() {
        csvData = null;
        csvRows = null;
    }

    @Override
    public void destroy() {
        cleanUp();
    }

    


}

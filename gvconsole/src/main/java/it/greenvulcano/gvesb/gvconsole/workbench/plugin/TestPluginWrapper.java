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
package it.greenvulcano.gvesb.gvconsole.workbench.plugin;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.internal.GVInternalException;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Node;

/**
 * <code>TestPluginWrapper</code> encapsulates a TestPlugin in order to provide
 * standard functionalities to all plug-ins: exception management, showing
 * results.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class TestPluginWrapper implements ConfigurationListener
{
    /**
     * The configuration file
     */
    private String            configurationFile;

    /**
     * The xpath string
     */
    private String            xpath;

    /**
     * The test plugin object
     */
    private TestPlugin        testPlugin;

    /**
     * The Throwable object
     */
    private Throwable         throwable;

    /**
     * Is <code>true</code> if the result must be showed
     */
    private boolean           showsResult;

    /**
     * The FileWriter writer for the output
     */
    private static FileWriter writer   = null;

    /**
     * Append boolean value
     */
    private boolean           append   = false;

    /**
     * Append boolean value for inputFile
     */
    private boolean           saveData = false;

    /**
     * Constructor to initialize the fields of this object
     *
     * @param configurationFile
     *        The configuration file
     * @param xpath
     *        The XPath string value
     * @param testPlugin
     *        TestPlugin class
     */
    public TestPluginWrapper(String configurationFile, String xpath, TestPlugin testPlugin)
    {
        this.configurationFile = configurationFile;
        this.xpath = xpath;
        this.testPlugin = testPlugin;
        throwable = null;
        showsResult = false;

        XMLConfig.addConfigurationListener(this, configurationFile);
    }

    /**
     * This method execute the Hot reloading of configuration file
     *
     * @param event
     *        the event value
     */
    public void configurationChanged(ConfigurationEvent event)
    {
        try {
            if (event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) {
                Node node = XMLConfig.getNode(configurationFile, xpath);
                if (node != null) {
                    testPlugin.init(node);
                }
            }
        }
        catch (Throwable thr) {
            thr.printStackTrace();
        }
    }

    /**
     * Get the testPlugin Object
     *
     * @return testPlugin TestPlugin class
     */
    public TestPlugin getTestPlugin()
    {
        return testPlugin;
    }

    /**
     * Get the throwable
     *
     * @return the Throwable object
     */
    public Throwable getThrowable()
    {
        return throwable;
    }

    /**
     * Get the showResult value to know if show or not the result
     *
     * @return showResult flag value <code>true/code> if the result has to show.
     */
    public boolean getShowsResult()
    {
        return showsResult;
    }

    /**
     * Set the Throwable object with the exception occurred
     *
     * @param throwable
     *        the throwable object occurred
     */
    public void setThrowable(Throwable throwable)
    {
        this.throwable = throwable;
    }

    /**
     * Set the Show Result object
     *
     * @param showsResult
     *        is <code>true</code> to show the result
     */
    public void setShowsResult(boolean showsResult)
    {
        this.showsResult = showsResult;
    }

    /**
     * Produces a printable version of the throwable
     *
     * @return the throwable occurred in a printable format
     */
    public String getThrowableMessage()
    {
        if (throwable == null) {
            return "";
        }

        Throwable exc = throwable;

        // Prepare buffer
        //
        StringWriter buf = new StringWriter();
        PrintWriter out = new PrintWriter(buf);

        // Write exception on the buffer
        //
        while (exc != null) {
            out.println("================================================");
            out.println(exc.getMessage());
            out.println("------------------------------------------------");
            exc.printStackTrace(out);
            out.println("");
            out.println("");

            // Are there any annidates exceptions?
            //
            if (exc instanceof GVInternalException) {
                exc = ((GVInternalException) exc).getCause();
            }
            else {
                exc = null;
            }
        }

        // Write Buffer and flush the Print Writer out
        //
        out.flush();
        String retStr = buf.toString();

        return "<pre>" + encode(retStr) + "</pre>";
    }

    /**
     * encode a String replacing the <, >, " and & with &lt;, &gt;, &quot; and
     * &amp; <br>
     *
     * @param str
     *        the string to encode
     * @return string the encoded string
     */
    public static String encode(String str)
    {
        return StringEscapeUtils.escapeXml(str);
    }

    /**
     * Execute the dump function
     *
     * @param str
     *        the string to dump
     * @return the dumped string
     */
    public static String dump(String str)
    {
        // Replace \ with /
        //
        StringBuffer output = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(str, "\"", true);
        while (tokenizer.hasMoreTokens()) {
            String tk = tokenizer.nextToken();
            if (tk.equals("\"")) {
                output.append("/");
            }
            else {
                output.append(tk);
            }
        }

        return output.toString();

    }

    /**
     * @param str
     * @return the dumped string
     */
    public static String dumpString(String str)
    {
        // Replace \ with /
        //
        StringBuffer output = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(str, "&apos;", true);

        while (tokenizer.hasMoreTokens()) {
            String tk = tokenizer.nextToken();
            if (tk.equals("&")) {
                while (!tk.equals(";")) {
                    tk = tokenizer.nextToken();
                }
                output.append("'");
            }
            else {
                output.append(tk);
            }
        }
        return output.toString();
    }

    /**
     * This method create the output file writer with the append value
     *
     * @param fileName
     *        The file name
     * @return FileWriter
     * @throws Throwable
     */
    public FileWriter createFile(String fileName) throws Throwable
    {
        FileWriter writer = null;

        writer = new FileWriter(fileName, append);

        return writer;
    }

    /**
     *
     * @return the FileWriter
     */
    public FileWriter getFile()
    {
        return writer;
    }

    /**
     * Set the file requested
     *
     * @param file
     *        The FileWriter object
     */
    public void setFile(FileWriter file)
    {
        writer = file;
    }

    /**
     * If Append is true the file will be create in append mode
     *
     * @return append the append boolean value
     */
    public boolean getAppend()
    {
        return append;
    }

    /**
     * If Append is true the file will be create in append mode
     *
     * @param append
     *        the append boolean value
     */
    public void setAppend(boolean append)
    {
        this.append = append;
    }

    /**
     * If Append is true the file will be create in append mode
     *
     * @return appendI the append boolean value
     */
    public boolean getSaveData()
    {
        return saveData;
    }

    /**
     * @param saveData
     */
    public void setSaveData(boolean saveData)
    {
        this.saveData = saveData;
    }

    /**
     * Execute the dump function
     *
     * @param data
     *        the byteData to dump
     * @return data the dumped data
     */
    public static String dump(byte[] data)
    {

        StringBuffer out = new StringBuffer();
        int idx = 0;
        while (idx < data.length) {
            String row = prepareRow(idx, 16, data);
            out.append(row).append("\n");
            idx += 16;
        }
        return out.toString();
    }

    /**
     * The spaces string
     */
    private static final String sp     = "                                       ";

    /**
     * The spaces string
     */
    private static final String spaces = sp + sp + sp + sp + sp + sp + sp + sp;

    /**
     * Format the binary lines <br>
     * This is the result for each line: <br>
     * <code>00000000: CA FE BA BE 00 03 00 2D 00 EC 0A 00 6D 00 6E 07 ; .......-....m.n.</code>
     *
     * @param idx
     *        The index
     * @param len
     *        the length
     * @param data
     *        the byte data to prepare row
     * @return The row prepared
     *
     */
    public static String prepareRow(int idx, int len, byte[] data)
    {

        int delta = 0;
        if (idx + len > data.length) {
            int newLen = data.length - idx;
            delta = len - newLen;
            len = newLen;
        }

        StringBuffer ret = new StringBuffer();
        StringBuffer ascii = new StringBuffer();

        String idxStr = padd(Long.toHexString(idx).toUpperCase(), 8);
        ret.append(idxStr).append(";");

        for (int i = 0; i < len; ++i) {
            byte b = data[idx + i];
            ret.append(" ").append(byteToHex(b));
            if ((b >= 0x20) && (b <= 127)) {
                ascii.append((char) b);
            }
            else {
                ascii.append(".");
            }
        }

        String deltaSpaces = spaces.substring(0, delta * 3);

        return ret.toString() + deltaSpaces + " ; " + ascii.toString();
    }

    /**
     * The padding string
     *
     * @param str
     *        The string to pad
     * @param len
     *        The length
     * @return the padded string
     */
    public static String padd(String str, int len)
    {
        if (len < str.length()) {
            return str;
        }
        return ("000000000000000".substring(0, len - str.length())) + str;
    }

    /**
     * Transform byte to hexadecimal
     *
     * @param b
     *        the byte
     * @return the string
     */
    public static String byteToHex(int b)
    {
        int h = (b & 0xF0) >> 4;
        int l = b & 0x0F;

        return (Integer.toHexString(h) + Integer.toHexString(l)).toUpperCase();
    }

    /**
     * Write on the file
     *
     * @param fileWriter
     * @param fileName
     * @param toBeInsert
     */
    public void writeFile(FileWriter fileWriter, String fileName, String toBeInsert)
    {

        try {
            if (fileWriter != null) {
                fileWriter.write(toBeInsert);
                fileWriter.flush();
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }

}
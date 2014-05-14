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

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Node;

/**
 *
 * DumpPlugin class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class DumpPlugin implements TestPlugin
{
    /**
     * The input binary.
     */
    protected byte[] dataInput          = null;

    /**
     * The input encoding.
     */
    private String   charEncodingInput  = "UTF-8";
    /**
     * The output encoding.
     */
    private String   charEncodingOutput = "UTF-8";

    /**
     * The binary data.
     */
    private boolean  binaryData         = false;

    /**
     * The output data.
     */
    private byte[]   dataOutput         = null;

    /**
     * The output data.
     */
    private byte[]   dataOutputView     = null;

    /**
     * The output file name.
     */
    private String   outputFileName     = "";

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#init(org.w3c.dom.Node)
     */
    public void init(Node configNode) throws Throwable
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#prepareInput(javax.servlet.http.HttpServletRequest,
     *      it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestObject,
     *      java.lang.String, int)
     */
    public void prepareInput(HttpServletRequest request, TestObject testObject, String testType, int number)
            throws Throwable
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#prepareInput(javax.servlet.http.HttpServletRequest)
     */
    public void prepareInput(HttpServletRequest request) throws Throwable
    {
        charEncodingOutput = request.getParameter("charEncoding");
        String data;
        if (!binaryData) {
            data = request.getParameter("data");
            setInputData(data);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#clear(javax.servlet.http.HttpServletRequest)
     */
    public void clear(HttpServletRequest request) throws Throwable
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#reset(javax.servlet.http.HttpServletRequest)
     */
    public void reset(HttpServletRequest request) throws Throwable
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#savedData(java.lang.String)
     */
    public void savedData(String fileNameI) throws Throwable
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#getAvailableCommands()
     */
    public String[] getAvailableCommands()
    {
        return new String[]{"dump"};
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#upload(it.greenvulcano.gvesb.gvconsole.workbench.plugin.MultipartFormDataParser)
     */
    public void upload(MultipartFormDataParser parameters) throws Throwable
    {
        InputStream inputStream = parameters.getInputStream("data");
        byte[] byteData = readFile(inputStream);
        String encInput = parameters.getString("charEncoding");
        if (encInput.equals("Binary")) {
            binaryData = true;

        }
        else {

            charEncodingInput = encInput;
            binaryData = false;
        }
        prepareRow(new String(byteData, charEncodingInput));
    }

    /**
     * Divide le 3 porzioni di stringa separate da ; presenti in una riga.
     *
     * @param row
     * @return the splitted row
     */
    public String splitRow(String row)
    {
        String output = null;
        int i = row.indexOf(";");
        int j = row.indexOf(";", i + 1);
        if ((i > 0) && (j > 0)) {
            output = row.substring(i + 1, j);
        }
        if ((i < 0) && (j < 0)) {
            output = row.trim();
        }
        return output;
    }

    /**
     * @param s
     * @throws Throwable
     */
    public void prepareRow(String s) throws Throwable
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int rowidxE = s.indexOf("\n", 0);
        int rowidxS = 0;
        if (rowidxE < 0) {
            dataInput = s.getBytes(charEncodingInput);
            return;
        }
        while (rowidxE > 0) {
            String row = s.substring(rowidxS, rowidxE);
            String hex = splitRow(row);

            if (hex == null) {
                throw new Throwable("Errore: formato dati non supportato");
            }

            buffer.write(hex.getBytes());

            rowidxS = rowidxE + 1;
            rowidxE = s.indexOf("\n", rowidxS);
            if (rowidxE < 0) {
                if (s.length() > rowidxS) {
                    rowidxE = s.length();
                }
            }
        }
        dataInput = buffer.toByteArray();
    }

    /**
     * Convert a hex string to a binary. Permits upper or lower case hex.
     *
     * @param input
     *        String must have even number of characters. and be formed only of
     *        digits 0-9 A-F or a-f. No spaces, minus or plus signs.
     * @return corresponding binary.
     */
    private byte[][] HexToByte(String input) throws Throwable
    {
        String s = input.trim();
        int stringLength = s.length();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ByteArrayOutputStream bufferView = new ByteArrayOutputStream();
        for (int j = 0; j < stringLength; j++) {
            if ((j + 1) > (stringLength - 1)) {
                break;
            }
            String couple = (s.substring(j, j + 2)).trim();
            if (couple.length() > 1) {
                byte b = (byte) (Integer.parseInt(couple, 16));
                buffer.write(b);
                if (b < 0x20) {
                    bufferView.write(".".getBytes());
                }
                else {
                    bufferView.write(b);
                }
            }
        }
        return new byte[][]{buffer.toByteArray(), bufferView.toByteArray()};
    }

    /**
     * Get the input data and dump his value in a hex code using a
     * TestPluginWrapper method.
     *
     * @return data value dump or empty string if the buffer is null
     * @see TestPluginWrapper
     */
    public String getInputDataDump()
    {
        if (dataInput == null) {
            return "";
        }
        return TestPluginWrapper.dump(dataInput);
    }

    /**
     * @param s
     */
    public void setInputDataDump(String s)
    {
        // do nothing
    }

    /**
     * Get the input data and dump his value in a hex code using a
     * TestPluginWrapper method.
     *
     * @return data value dump or empty string if the output data is null
     * @see TestPluginWrapper
     */
    public String getOutputDataDump()
    {
        if (dataOutput == null) {
            return "";
        }
        return TestPluginWrapper.dump(dataOutput);
    }

    /**
     * @param s
     */
    public void setOutputDataDump(String s)
    {
        // do nothing
    }

    /**
     * Read the Input Stream and write it in the ByteArrayOutputStream.
     *
     * @param inputStream
     *        file selected by user
     * @return byteArray input file value written in a ByteArray
     * @throws Throwable
     *         If an error occurred
     */
    public byte[] readFile(InputStream inputStream) throws Throwable
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] buf = new byte[2048];
        int len;
        while ((len = inputStream.read(buf)) != -1) {
            byteArrayOutputStream.write(buf, 0, len);
        }
        inputStream.close();

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#uploadMultiple(it.greenvulcano.gvesb.gvconsole.workbench.plugin.MultipartFormDataParser,
     *      it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestObject)
     */
    public void uploadMultiple(MultipartFormDataParser parameters, TestObject testObject) throws Throwable
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#prepare()
     */
    public InitialContext prepare() throws Throwable
    {
        return null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#prepare(it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestObject)
     */
    public InitialContext prepare(TestObject testObject) throws Throwable
    {
        return null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#updateDataInput(java.lang.String,
     *      java.lang.String)
     */
    public void updateDataInput(String data, String encoding) throws Throwable
    {
        setInputData(data);
        if (encoding != null) {
            charEncodingInput = encoding;
        }
    }

    /**
     * @param data
     * @param encoding
     * @throws Throwable
     */
    public void updateDataInput(byte[] data, String encoding) throws Throwable
    {
        dataInput = data;
        if (encoding != null) {
            charEncodingInput = encoding;
        }
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#setResetValue(java.lang.String)
     */
    public void setResetValue(String resetValue)
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#getResetValue()
     */
    public String getResetValue()
    {
        return null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#saveData(java.lang.String)
     */
    public void saveData(String fileNameI) throws Throwable
    {
        FileOutputStream fos = new FileOutputStream(fileNameI);
        fos.write(dataOutput);
        fos.flush();
        fos.close();
    }

    /**
     * @param request
     * @return always true
     * @throws Throwable
     */
    public boolean saveToFile(HttpServletRequest request) throws Throwable
    {
        outputFileName = request.getParameter("fileName");
        saveData(outputFileName);
        return true;
    }

    /**
     * @return the output file name
     */
    public String getOutputFileName()
    {
        return outputFileName;
    }

    /**
     * @param ofn
     */
    public void setOutputFileName(String ofn)
    {
        outputFileName = ofn;
    }

    /**
     * @param request
     * @return always true
     * @throws Throwable
     */
    public boolean dump(HttpServletRequest request) throws Throwable
    {
        String strInput = new String(dataInput, charEncodingInput);
        byte[][] arrays = HexToByte(strInput);
        dataOutput = arrays[0];
        dataOutputView = arrays[1];
        return true;
    }

    /**
     * @return the characters encoding
     */
    public String getCharEncoding()
    {
        return charEncodingInput;
    }

    /**
     * @return the output characters encoding
     */
    public String getCharEncodingOutput()
    {
        return charEncodingOutput;
    }

    /**
     * @return the input data
     * @throws Throwable
     */
    public String getInputData() throws Throwable
    {
        if (dataInput != null) {
            return new String(dataInput, charEncodingInput);
        }
        return new String("");
    }

    /**
     * @return the input data as byte[]
     * @throws Throwable
     */
    public byte[] getInputDataBinary() throws Throwable
    {
        return dataInput;
    }

    /**
     * @return the output data
     * @throws Throwable
     */
    public Object getOutputData() throws Throwable
    {
        if (charEncodingOutput.equals("Binary")) {
            return dataOutput;
        }
        return new String(dataOutputView, charEncodingOutput);
    }

    /**
     * Set the dataInput of the plug-in object.
     *
     * @param input
     *        String input object
     * @throws Throwable
     *         If an error occurred
     */
    public void setInputData(String input) throws Throwable
    {
        if (input == null) {
            return;
        }
        prepareRow(input);
    }

    /**
     * Set the dataInput of the Plugin object.
     *
     * @param dataInput
     *        String input object <br>
     * @throws Throwable
     *         If an error occurred
     */
    public void setInputDataBinary(byte[] dataInput) throws Throwable
    {
        if (dataInput == null) {
            return;
        }
        this.dataInput = dataInput;
    }

    /**
     * Get the input binary data this is <code>true</code> if an upload action
     * requested for a binary file.
     *
     * @return bynaryData boolean flag of the Plugin Object <br>
     */
    public boolean getInputBinaryData()
    {
        return binaryData;
    }

    /**
     * Set charEncoding requested.
     *
     * @param charEncodingOutput
     *        char Encoding requested
     * @throws Throwable
     *         If an error occurred
     */
    public void setCharEncodingOutput(String charEncodingOutput) throws Throwable
    {
        this.charEncodingOutput = charEncodingOutput;
    }

    /**
     * Set the input bynary data in the Plugin object.
     *
     * @param binaryData
     *        binaryData value to set the binaryData boolean object
     */
    public void setInputBinaryData(boolean binaryData)
    {
        this.binaryData = binaryData;
    }

    /**
     * @param data
     */
    public void setOutputData(Object data)
    {
        dataOutput = (byte[]) data;
    }

    /**
     * @param charEncodingInput
     * @throws Throwable
     */
    public void setCharEncoding(String charEncodingInput) throws Throwable
    {
        this.charEncodingInput = charEncodingInput;
    }
}

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
package it.greenvulcano.gvesb.gvconsole.gvcon.xquery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import max.config.Config;
import max.xml.XMLBuilder;

import org.w3c.dom.Node;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class XQueryBean
{

    /**
     *
     */
    public static final String             CHECKED               = "checked";
    /**
     *
     */
    public static final String             START_ROOT            = "root";
    /**
     *
     */
    public static final String             OUTPUT_MODE_SIMPLE    = "simple";
    /**
     *
     */
    public static final String             OUTPUT_MODE_TRANSFORM = "transform";

    /**
     * The XQuery.
     */
    private String                         text;

    /**
     * root, current.
     */
    private String                         startingNode;

    /**
     * simple, trasform.
     */
    private String                         outputMode;

    private boolean                        saveOnFile;

    /**
     * File name.
     */
    private String                         fileName;

    private String                         action;
    private String                         error;

    private XQueryProcessor                xQueryProcessor       = new XQueryProcessor(this);

    private XQueryProcessorCurrentDocument xQueryProcessorCD     = new XQueryProcessorCurrentDocument(this);

    /**
     *
     */
    public XQueryBean()
    {
        text = "";
        action = "";
        saveOnFile = false;
        startingNode = START_ROOT;
        outputMode = OUTPUT_MODE_SIMPLE;
        fileName = "c://XQueryResult.xml";
    }

    /**
     *
     */
    public void resetBooleans()
    {
        saveOnFile = false;
    }

    /**
     * @return
     */
    public String getError()
    {
        if (error != null) {
            return error;
        }
        return "";
    }

    /**
     * @return
     */
    public String getErrorHTML()
    {
        return toHTML(getError());
    }

    /**
     * @param error
     */
    public void setError(String error)
    {
        this.error = error;
    }

    /**
     * @return
     */
    public String getAction()
    {
        return action;
    }

    /**
     * @return
     */
    public String getActionHTML()
    {
        return toHTML(getAction());
    }

    /**
     * @param action
     */
    public void setAction(String action)
    {
        String siteRoot = Config.get("", "max.site.root");
        this.action = siteRoot + action;
    }

    /**
     * @return
     */
    public String getText()
    {
        return text;
    }

    /**
     * @return
     */
    public String getTextHTML()
    {
        return toHTML(getText());
    }

    /**
     * @param text
     */
    public void setText(String text)
    {
        this.text = text;
    }

    /**
     * @return
     */
    public String getStartingNode()
    {
        return startingNode;
    }

    /**
     * @param startingNode
     */
    public void setStartingNode(String startingNode)
    {
        this.startingNode = startingNode;
    }

    /**
     * @return
     */
    public String getOutputMode()
    {
        return outputMode;
    }

    /**
     * @param outputMode
     */
    public void setOutputMode(String outputMode)
    {
        this.outputMode = outputMode;
    }

    public boolean isSaveOnFile()
    {
        return saveOnFile;
    }

    public void setSaveOnFile(boolean saveOnFile)
    {
        this.saveOnFile = saveOnFile;
    }

    /**
     * @param value
     * @return
     */
    public String checkStartingNode(String value)
    {
        if (value.equals(startingNode)) {
            return CHECKED;
        }
        return "";
    }

    /**
     * @param value
     * @return
     */
    public String checkOutputMode(String value)
    {
        if (value.equals(outputMode)) {
            return CHECKED;
        }
        return "";
    }

    /**
     * @return
     */
    public String checkSaveOnFile()
    {
        return saveOnFile ? CHECKED : "";
    }

    /**
     * @return
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * @param fileName
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    /**
     * @param servletRequest
     */
    public void manageBooleans(HttpServletRequest servletRequest)
    {
        if (servletRequest.getParameter("resetBooleans") != null) {
            resetBooleans();
        }
    }

    /**
     * @param request
     * @throws Exception
     */
    public void performXQueryProcessorCurrentDocument(HttpServletRequest request) throws Exception
    {
        error = null;

        XMLBuilder builder = XMLBuilder.getFromSession(request.getSession());

        Node node = builder.getCurrentElement();
        xQueryProcessorCD.performXQueryProcessor(node);
    }

    /**
     * @param request
     * @throws Exception
     */
    public void performXQueryProcessorDocuments(HttpServletRequest request) throws Exception
    {
        error = null;
        xQueryProcessor.performXQueryDocuments(request);
    }

    /**
     * @param stream
     * @param source
     * @return
     * @throws Exception
     */
    public String transform(InputStream stream, StreamSource source) throws Exception
    {
        // Instantiate the TransformerFactory.
        TransformerFactory tFactory = TransformerFactory.newInstance();
        // Get the stylesheet from the XML source.
        Source stylesheet = new StreamSource(stream);

        // Process the stylesheet and generate a Transformer.
        Transformer transformer = tFactory.newTransformer(stylesheet);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        // Use the Transformer to perform the transformation and send the
        // the output to a Result object.
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        return writer.getBuffer().toString();
    }

    /**
     * @return
     * @throws Exception
     */
    public String showFoundDocuments() throws Exception
    {
        ByteArrayOutputStream xQueryResult = xQueryProcessor.getXQueryResult();

        if ((xQueryResult == null) || (xQueryResult.size() == 0)) {
            return "";
        }

        String htmlResult = toHTML(xQueryResult);

        // Save on file
        if (isSaveOnFile()) {
            File file = new File(getFileName());
            file.delete();
            FileOutputStream fos = new FileOutputStream(getFileName());
            fos.write(htmlResult.getBytes());
            fos.flush();
            fos.close();
        }

        return htmlResult;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    public String showFoundMatches(HttpServletRequest request) throws Exception
    {

        ByteArrayOutputStream searchResult = (ByteArrayOutputStream) xQueryProcessorCD.getXQueryResult();

        if ((searchResult == null) || (searchResult.size() == 0)) {
            return "";
        }

        String htmlResult = toHTML(searchResult);

        // Save on file
        if (isSaveOnFile()) {
            File file = new File(getFileName());
            file.delete();
            FileOutputStream fos = new FileOutputStream(getFileName());
            fos.write(htmlResult.getBytes());
            fos.flush();
            fos.close();
        }

        return htmlResult;
    }

    /**
     * @param str
     * @return
     */
    public static String toHTML(String str)
    {
        StringTokenizer tokenizer = new StringTokenizer(str, "<>&\"\n", true);
        StringBuffer buffer = new StringBuffer();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("<")) {
                buffer.append("&lt;");
            }
            else if (token.equals(">")) {
                buffer.append("&gt;");
            }
            else if (token.equals("&")) {
                buffer.append("&amp;");
            }
            else if (token.equals("\"")) {
                buffer.append("&quot;");
            }
            else if (token.equals("\n")) {
                buffer.append("<br/>");
            }
            else {
                buffer.append(token);
            }
        }
        return buffer.toString();
    }

    /**
     * @param baos
     * @return
     */
    public String toHTML(ByteArrayOutputStream baos)
    {
        return toHTML(baos.toString());
    }
}
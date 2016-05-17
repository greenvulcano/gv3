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
package it.greenvulcano.gvesb.virtual.jca.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.Record;
import javax.resource.cci.RecordFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * This class wrap both a <code>XMLToRecord</code> and a
 * <code>RecordToXML</code> objects that performs the transformations
 * XML--&gt;Record and Record--&gt;XML.
 * <p>
 * This class is not thread safe. In a multi-thread environment, each thread
 * should have its own instance if the <code>JCAXML</code> object.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class JCAXML
{

    /**
     *
     */
    public static final String ROOT_NAME       = "record";
    /**
     *
     */
    public static final String NAMESPACE_URI   = "http://www.greenvulcano.it/jca/xml";

    private DocumentBuilder    documentBuilder = null;
    private XMLToRecord        xmlToRecord     = null;
    private RecordToXML        recordToXml     = null;
    private Transformer        transformer     = null;

    /**
     * Build a <code>JCAXML</code> object starting from a
     * <code>javax.resource.cci.ConnectionFactory</code>.
     *
     * @param connectionFactory
     * @throws JCAXMLException
     */
    public JCAXML(ConnectionFactory connectionFactory) throws JCAXMLException
    {
        try {
            initialize(connectionFactory.getRecordFactory());
        }
        catch (ResourceException exc) {
            throw new JCAXMLException("JCAXML_RESOURCE_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }
    }

    /**
     * Build a <code>JCAXML</code> object starting from a
     * <code>javax.resource.cci.RecordFactory</code>.
     *
     * @param recordFactory
     * @throws JCAXMLException
     */
    public JCAXML(RecordFactory recordFactory) throws JCAXMLException
    {
        initialize(recordFactory);
    }

    /**
     * Initialize the <code>JCAXML</code> object storing the given
     * <code>RecordFactory</code> and building a <code>DocumentBuilder</code> to
     * use during parsing.
     */
    private void initialize(RecordFactory recordFactory) throws JCAXMLException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setCoalescing(true);
        dbf.setExpandEntityReferences(true);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);

        try {
            documentBuilder = dbf.newDocumentBuilder();
        }
        catch (ParserConfigurationException exc) {
            throw new JCAXMLException("JCAXML_PARSER_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }

        xmlToRecord = new XMLToRecord(recordFactory, documentBuilder);
        recordToXml = new RecordToXML(documentBuilder);

        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            transformer = tf.newTransformer();
        }
        catch (TransformerConfigurationException e) {
            throw new JCAXMLException("JCAXML_PARSER_EXCEPTION", new String[][]{{"msg", e.getMessage()}}, e);
        }
    }

    /**
     * Return the prefix for the namespace.
     *
     * @return the namespace prefix or null if the prefix is not set.
     */
    public String getNSPrefix()
    {
        return recordToXml.getNSPrefix();
    }

    /**
     * Set the prefix for the namespace.
     *
     * @param prefix
     *        namespace prefix for the producex XML. If null or empty, then the
     *        namespace will have not a prefix.
     */
    public void setNSPrefix(String prefix)
    {
        recordToXml.setNSPrefix(prefix);
    }

    /**
     * Build a <code>javax.resource.cci.Record</code> starting from a XML passed
     * into a file.
     *
     * @param file
     *        the file to read for the XML
     * @return a <code>javax.resource.cci.Record</code>.
     * @throws JCAXMLException
     */
    public Record buildRecord(File file) throws JCAXMLException
    {
        return xmlToRecord.buildRecord(file);
    }

    /**
     * Build a <code>javax.resource.cci.Record</code> starting from a XML passed
     * into an input stream.
     *
     * @param inputStream
     *        the input stream to read for the XML
     * @return a <code>javax.resource.cci.Record</code>.
     * @throws JCAXMLException
     */
    public Record buildRecord(InputStream inputStream) throws JCAXMLException
    {
        return xmlToRecord.buildRecord(inputStream);
    }

    /**
     * Build a <code>javax.resource.cci.Record</code> starting from a XML passed
     * into an input source.
     *
     * @param inputSource
     *        the input source to read for the XML
     * @return a <code>javax.resource.cci.Record</code>.
     * @throws JCAXMLException
     */
    public Record buildRecord(InputSource inputSource) throws JCAXMLException
    {
        return xmlToRecord.buildRecord(inputSource);
    }

    /**
     * Build a <code>javax.resource.cci.Record</code> starting from a XML stored
     * as a String.
     *
     * @param documentStr
     *        the String containing the XML
     * @return a <code>javax.resource.cci.Record</code>.
     * @throws JCAXMLException
     */
    public Record buildRecord(String documentStr) throws JCAXMLException
    {
        return xmlToRecord.buildRecord(documentStr);
    }

    /**
     * Build a <code>javax.resource.cci.Record</code> starting from a XML stored
     * as a array of bytes.
     *
     * @param documentArr
     *        the byte[] containing the XML
     * @return a <code>javax.resource.cci.Record</code>.
     * @throws JCAXMLException
     */
    public Record buildRecord(byte documentArr[]) throws JCAXMLException
    {
        return xmlToRecord.buildRecord(documentArr);
    }

    /**
     * Build a <code>javax.resource.cci.Record</code> starting from a XML.
     *
     * @param node
     *        the <code>org.w3c.dom.Node</code> containing the record
     *        definition.
     * @return a <code>javax.resource.cci.Record</code>.
     * @throws JCAXMLException
     */
    public Record buildRecord(Node node) throws JCAXMLException
    {
        return xmlToRecord.buildRecord(node);
    }

    /**
     *
     */
    public void byteArrayAsEncode64()
    {
        recordToXml.byteArrayAsEncode64();
    }

    /**
     *
     */
    public void byteArrayAsString()
    {
        recordToXml.byteArrayAsString();
    }

    /**
     * @param encoding
     */
    public void byteArrayAsString(String encoding)
    {
        recordToXml.byteArrayAsString(encoding);
    }

    /**
     *
     */
    public void byteArrayAsXML()
    {
        recordToXml.byteArrayAsXML();
    }

    /**
     * Produce un Document a partire da un Record JCA.
     *
     * @param record
     * @return a <code>org.w3c.dom.Document</code>.
     * @throws JCAXMLException
     */
    public Document buildDocument(Record record) throws JCAXMLException
    {
        return recordToXml.buildDocument(record);
    }

    /**
     * Produce un document come array di bytes a partire da un Record JCA.
     *
     * @param record
     * @return a <code>org.w3c.dom.Document</code> as byte[].
     * @throws JCAXMLException
     */
    public byte[] buildDocumentAsBytes(Record record) throws JCAXMLException
    {
        Document document = recordToXml.buildDocument(record);
        return documentAsBytes(document);
    }

    /**
     * Serializza un Document.
     *
     * @param document
     * @return a <code>org.w3c.dom.Document</code> as byte[].
     */
    public byte[] documentAsBytes(Document document)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            transformer.transform(new DOMSource(document), new StreamResult(out));
        }
        catch (TransformerException e) {
            // never happen
        }
        return out.toByteArray();
    }
}

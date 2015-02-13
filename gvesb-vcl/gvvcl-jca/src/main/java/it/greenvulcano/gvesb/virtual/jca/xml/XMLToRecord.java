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

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.resource.cci.RecordFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class build a <code>javax.resource.cci.Record</code> starting from a
 * XML. The given XML <b>must</b> follow the specification as described in the
 * <b>"<i>to be defined</i>"</b> document.
 * <p>
 * This class is not thread safe. In a multi-thread environment, each thread
 * should have its own instance if the <code>XMLToRecord</code> object.
 * <p>
 * The <code>XMLToRecord</code> should not be directly used. Use
 * <code>JCAXML</code> instead that wrap both a <code>XMLToRecord</code> and a
 * <code>RecordToXML</code> objects.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class XMLToRecord
{
    private RecordFactory   recordFactory   = null;
    private DocumentBuilder documentBuilder = null;
    private Transformer     transformer     = null;

    /**
     * Build a <code>XMLToRecord</code> object starting from a
     * <code>javax.resource.cci.RecordFactory</code>.
     *
     * @param recordFactory
     * @param documentBuilder
     * @throws JCAXMLException
     */
    public XMLToRecord(RecordFactory recordFactory, DocumentBuilder documentBuilder) throws JCAXMLException
    {
        this.recordFactory = recordFactory;
        this.documentBuilder = documentBuilder;

        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        catch (TransformerConfigurationException e) {
            throw new JCAXMLException("JCAXML_PARSER_EXCEPTION", new String[][]{{"msg", e.getMessage()}}, e);
        }
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
        try {
            Document document = documentBuilder.parse(file);
            return buildRecord(document);
        }
        catch (SAXException exc) {
            throw new JCAXMLException("JCAXML_SAX_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }
        catch (IOException exc) {
            throw new JCAXMLException("JCAXML_IO_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }
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
        try {
            Document document = documentBuilder.parse(inputStream);
            return buildRecord(document);
        }
        catch (SAXException exc) {
            throw new JCAXMLException("JCAXML_SAX_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }
        catch (IOException exc) {
            throw new JCAXMLException("JCAXML_IO_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }
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
        try {
            Document document = documentBuilder.parse(inputSource);
            return buildRecord(document);
        }
        catch (SAXException exc) {
            throw new JCAXMLException("JCAXML_SAX_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }
        catch (IOException exc) {
            throw new JCAXMLException("JCAXML_IO_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }
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
        try {
            Document document = documentBuilder.parse(new InputSource(new StringReader(documentStr)));
            return buildRecord(document);
        }
        catch (SAXException exc) {
            throw new JCAXMLException("JCAXML_SAX_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }
        catch (IOException exc) {
            throw new JCAXMLException("JCAXML_IO_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }
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
        try {
            Document document = documentBuilder.parse(new ByteArrayInputStream(documentArr));
            return buildRecord(document);
        }
        catch (SAXException exc) {
            throw new JCAXMLException("JCAXML_SAX_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }
        catch (IOException exc) {
            throw new JCAXMLException("JCAXML_IO_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }
    }

    /**
     * Build a <code>javax.resource.cci.Record</code> starting from a XML.
     *
     * @param rootElement
     *        the <code>org.w3c.dom.Node</code> containing the record
     *        definition.
     * @return a <code>javax.resource.cci.Record</code>.
     * @throws JCAXMLException
     */
    public Record buildRecord(Node rootElement) throws JCAXMLException
    {
        String rootName = rootElement.getLocalName();
        String namespaceURI = rootElement.getNamespaceURI();

        if (!namespaceURI.equals(JCAXML.NAMESPACE_URI)) {
            throw new JCAXMLException("JCAXML_INVALID_NAMESPACE", new String[][]{{"namespace", JCAXML.NAMESPACE_URI},
                    {"found", namespaceURI}});
        }

        if (!rootName.equals(JCAXML.ROOT_NAME)) {
            throw new JCAXMLException("JCAXML_INVALID_DOCUMENT", new String[][]{{"root", JCAXML.ROOT_NAME},
                    {"found", rootName}});
        }

        Element recordElement = getRecordElement(rootElement);

        return buildRecord(recordElement);
    }

    private Record buildRecord(Element element) throws JCAXMLException
    {
        if (element == null) {
            throw new JCAXMLException("JCAXML_INVALID_ELEMENT", new String[][]{{"required", "'map', 'list', 'custom'"},
                    {"found", "null"}});
        }

        String name = element.getLocalName();
        if (name.equals("map")) {
            return buildMappedRecord(element);
        }
        else if (name.equals("list")) {
            return buildIndexedRecord(element);
        }
        else if (name.equals("custom")) {
            return buildCustomRecord(element);
        }
        else {
            throw new JCAXMLException("JCAXML_INVALID_ELEMENT", new String[][]{{"required", "'map', 'list', 'custom'"},
                    {"found", name}});
        }
    }

    @SuppressWarnings("unchecked")
    private Record buildMappedRecord(Element element) throws JCAXMLException
    {
        String recordName = element.getAttribute("name");
        MappedRecord record = null;
        try {
            record = recordFactory.createMappedRecord(recordName);
        }
        catch (ResourceException exc) {
            throw new JCAXMLException("JCAXML_RESOURCE_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }

        List<Element> entries = getChildElements(element, new String[]{"entry"});
        for (Element entry : entries) {
            Object object = processValue(entry);
            String key = entry.getAttribute("key");
            record.put(key, object);
        }

        return record;
    }

    @SuppressWarnings("unchecked")
    private Record buildIndexedRecord(Element element) throws JCAXMLException
    {
        String recordName = element.getAttribute("name");
        IndexedRecord record = null;
        try {
            record = recordFactory.createIndexedRecord(recordName);
        }
        catch (ResourceException exc) {
            throw new JCAXMLException("JCAXML_RESOURCE_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }

        List<Element> items = getChildElements(element, new String[]{"item"});
        for (Element item : items) {
            Object object = processValue(item);
            record.add(object);
        }

        return record;
    }

    private Record buildCustomRecord(Element element) throws JCAXMLException
    {
        String className = element.getAttribute("class");
        Record record = null;
        Class<?> recordClass = null;
        try {
            recordClass = Class.forName(className);
            record = (Record) recordClass.newInstance();
        }
        catch (ClassNotFoundException exc) {
            throw new JCAXMLException("JCAXML_CLASS_EXCEPTION", new String[][]{{"type", "NOT FOUND"},
                    {"msg", exc.getMessage()}}, exc);
        }
        catch (InstantiationException exc) {
            throw new JCAXMLException("JCAXML_CLASS_EXCEPTION", new String[][]{{"type", "INSTANTIATION"},
                    {"msg", exc.getMessage()}}, exc);
        }
        catch (IllegalAccessException exc) {
            throw new JCAXMLException("JCAXML_CLASS_EXCEPTION", new String[][]{{"type", "ILLEGAL ACCESS"},
                    {"msg", exc.getMessage()}}, exc);
        }

        List<Element> properties = getChildElements(element, new String[]{"property"});
        for (Element property : properties) {
            String name = property.getAttribute("name");
            if (name.equals("")) {
                throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg", "attribute 'name' missing"}});
            }
            Object value = processValue(property);
            setProperty(record, recordClass, name, value);
        }

        return record;
    }

    /**
     * Set the value of a property of the given record.
     */
    private void setProperty(Record record, Class<?> recordClass, String name, Object value) throws JCAXMLException
    {
        try {
            String methodName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            PropertyDescriptor propertyDescr = new PropertyDescriptor(name, recordClass, null, "set" + methodName);
            Method writer = propertyDescr.getWriteMethod();
            Object params[] = new Object[]{value};
            writer.invoke(record, params);
        }
        catch (Exception exc) {
            throw new JCAXMLException("JCAXML_PROPERTY_EXCEPTION", new String[][]{{"exc", exc.getClass().getName()},
                    {"msg", exc.getMessage()}}, exc);
        }
    }

    /**
     * Return all elements that are children of the given node, are in the
     * required namespace, and the local name is one of the specified accepted
     * names.
     *
     * @exception JCAXMLException
     *            if a syntax error occurs.
     *
     * @see it.reply.jca.JCAXML#NAMESPACE_URI
     */
    private List<Element> getChildElements(Node node, String acceptedNames[]) throws JCAXMLException
    {
        Set<String> accepted = new HashSet<String>();
        for (int i = 0; i < acceptedNames.length; ++i) {
            accepted.add(acceptedNames[i]);
        }

        List<Element> ret = new ArrayList<Element>();

        Node child = node.getFirstChild();
        while (child != null) {
            if (child instanceof Element) {

                String localName = child.getLocalName();
                String uri = child.getNamespaceURI();

                if (accepted.contains(localName) && JCAXML.NAMESPACE_URI.equals(uri)) {
                    ret.add((Element) child);
                }
                else {

                    // Trovato un elemento non valido
                    //
                    throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg",
                            "invalid element: " + child.getNodeName()}});
                }
            }
            else if (child instanceof Text) {
                String val = child.getNodeValue().trim();
                if (!val.equals("")) {

                    // Trovato del testo
                    //
                    throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg", "text is not valid here"}});
                }
            }
            else if (child instanceof Comment) {

                // Do nothing.
            }
            else {

                // Tutto il resto non � valido
                //
                throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg", "invalid xml"}});
            }

            child = child.getNextSibling();
        }

        return ret;
    }

    /**
     * Return a record element ('map', 'list', 'custom') or null if it does not
     * exist.
     *
     * @param node
     *        parent for the record element
     *
     * @return Return a record element ('map', 'list', 'custom') or null if it
     *         does not exist.
     *
     * @exception JCAXMLException
     *            if a syntax error occurs.
     */
    private Element getRecordElement(Node node) throws JCAXMLException
    {
        Element toReturn = null;

        Node child = node.getFirstChild();
        while (child != null) {
            if (child instanceof Element) {

                if (toReturn != null) {

                    // Trovati due elementi figli del nodo
                    //
                    throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg",
                            "only one record elemen is admitted"}});
                }

                String localName = child.getLocalName();
                String uri = child.getNamespaceURI();

                if ((localName.equals("map") || localName.equals("list") || localName.equals("custom"))
                        && JCAXML.NAMESPACE_URI.equals(uri)) {
                    toReturn = (Element) child;
                }
                else {

                    // Trovato un elemento non valido
                    //
                    throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg",
                            "invalid element: " + child.getNodeName()}});
                }
            }
            else if (child instanceof Text) {
                String val = child.getNodeValue().trim();
                if (!val.equals("")) {

                    // Trovato del testo
                    //
                    throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg", "text is not valid here"}});
                }
            }
            else if (child instanceof Comment) {

                // Do nothing.
            }
            else {

                // Tutto il resto non � valido
                //
                throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg", "invalid xml"}});
            }

            child = child.getNextSibling();
        }

        return toReturn;
    }

    /**
     * Process the value of an entry, an item or a property.
     */
    private Object processValue(Element element) throws JCAXMLException
    {
        String type = element.getAttribute("type");
        if (type.equals("")) {
            return processValueWithImplicitType(element);
        }

        // L'ordine dei test � stato identificato secondo la probabilit� di
        // trovare
        // i tipi di dato corrispondenti.
        // E' possibile che qualcun altro abbia una diversa sensibilit�, ma ora
        // l'ordine
        // � questo!!!

        if (type.equals("string")) {
            return buildString(element);
        }
        else if (type.equals("int")) {
            return new Integer(buildString(element));
        }
        else if (type.equals("long")) {
            return new Long(buildString(element));
        }
        else if (type.equals("boolean")) {
            return new Boolean(buildString(element));
        }
        else if (type.equals("char")) {
            return new Character(buildString(element).charAt(0));
        }
        else if (type.equals("double")) {
            return new Double(buildString(element));
        }
        else if (type.equals("float")) {
            return new Float(buildString(element));
        }
        else if (type.equals("short")) {
            return new Short(buildString(element));
        }
        else if (type.equals("record")) {
            Element recordElement = getRecordElement(element);
            return buildRecord(recordElement);
        }
        else if (type.equals("byte")) {
            return new Byte(buildString(element));
        }
        else if (type.equals("null")) {
            return buildNull(element);
        }
        else if (type.equals("xml")) {
            return buildStringFromXML(element);
        }
        else if (type.equals("xml-bytes")) {
            return buildByteArrayFromXML(element);
        }
        else if (type.equals("node")) {
            return buildNode(element);
        }
        else if (type.equals("document")) {
            return buildDocument(element);
        }
        else {
            throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg", "invalid type: " + type}});
        }
    }

    /**
     * Process the value of an entry, an item or a property. The type of the
     * value is automatically detected according to the following algorithm:
     *
     * <ul>
     * <li>if the element has only text child nodes the values is a String
     * containing the text of the child nodes.
     * <li>if the element has a 1 child element and is in the JCAXML namespace,
     * then a record is created. This happen also if the element has also text
     * child nodes but they are empty.
     * <li>if the element has a 1 child element and it is not in the JCAXML
     * namespace, then the entire XML is transformed in a string. This happen
     * also if the element has also text child nodes but they are empty.
     * <li>otherwise an error is raised.
     * </ul>
     */
    private Object processValueWithImplicitType(Element element) throws JCAXMLException
    {
        NodeList children = element.getChildNodes();
        StringBuffer str = new StringBuffer();
        Element childElement = null;
        int N = children.getLength();
        for (int i = 0; i < N; ++i) {
            Node node = children.item(i);
            if (node instanceof Text) {
                str.append(node.getNodeValue());
            }
            else if (node instanceof Element) {
                if (childElement != null) {

                    // Trovati pi� di un elemento
                    //
                    throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg", "invalid content"}});
                }
                childElement = (Element) node;
            }
        }
        if (childElement == null) {
            return str.toString();
        }

        if (!str.toString().trim().equals("")) {

            // Trovato un elemento con del testo
            //
            throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg", "invalid content"}});
        }

        if (childElement.getNamespaceURI().equals(JCAXML.NAMESPACE_URI)) {
            return buildRecord(childElement);
        }

        return buildStringFromXML(element);
    }

    /**
     * Children of the parent node must be only text nodes.
     */
    private String buildString(Node parent) throws JCAXMLException
    {
        NodeList list = parent.getChildNodes();
        StringBuffer ret = new StringBuffer();
        int N = list.getLength();
        for (int i = 0; i < N; ++i) {
            Node node = list.item(i);
            if (!(node instanceof Text)) {
                throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg", "string not found"}});
            }
            ret.append(node.getNodeValue());
        }
        return ret.toString();
    }

    /**
     * Build the string representation of the children. The parent must have
     * only an Element child and can have many empty text nodes.
     */
    private String buildStringFromXML(Node parent) throws JCAXMLException
    {
        Element element = extractElement(parent);

        StringWriter writer = new StringWriter();
        try {
            transformer.transform(new DOMSource(element), new StreamResult(writer));
        }
        catch (TransformerException e) {
            throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg", "invalid XML: " + e}});
        }
        return writer.toString();
    }

    /**
     * Build a byte[] that represents the children. The parent must have only an
     * Element child and can have many empty text nodes.
     */
    private byte[] buildByteArrayFromXML(Node parent) throws JCAXMLException
    {
        Element element = extractElement(parent);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            transformer.transform(new DOMSource(element), new StreamResult(out));
        }
        catch (TransformerException e) {
            throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg", "invalid XML: " + e}});
        }
        return out.toByteArray();
    }

    /**
     * Extract the child that must be an Element. The parent must have only an
     * Element child and can have many empty text nodes.
     */
    private Element buildNode(Node parent) throws JCAXMLException
    {
        return extractElement(parent);
    }

    /**
     * Extract the child and creates a new Document with the root element set
     * with the extracted Element. The parent must have only an Element child
     * and can have many empty text nodes.
     */
    private Document buildDocument(Node parent) throws JCAXMLException
    {
        Element element = extractElement(parent);
        Document document = documentBuilder.newDocument();
        Node root = document.importNode(element, true);
        document.appendChild(root);

        return document;
    }

    /**
     * This method return everytime null. It check the form of the XML. Children
     * of the parent node must be only empty text nodes.
     *
     * @return <code>null</code>.
     */
    private Object buildNull(Node parent) throws JCAXMLException
    {
        NodeList list = parent.getChildNodes();
        int N = list.getLength();
        for (int i = 0; i < N; ++i) {
            Node node = list.item(i);
            if (!(node instanceof Text)) {
                throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg", "invalid 'null' element"}});
            }
            else {
                String txt = (String) (((Text) node).getNodeValue());
                if (!txt.trim().equals("")) {
                    throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg", "invalid 'null' element"}});
                }
            }
        }
        return null;
    }

    /**
     * Extract the child that must be an Element. The parent must have only an
     * Element child and can have many empty text nodes.
     *
     * @param parent
     * @return
     * @throws JCAXMLException
     */
    private Element extractElement(Node parent) throws JCAXMLException
    {
        Element element = null;
        NodeList list = parent.getChildNodes();
        int N = list.getLength();
        for (int i = 0; i < N; ++i) {
            Node node = list.item(i);
            if (node instanceof Text) {
                String txt = node.getNodeValue().trim();
                if (!txt.equals("")) {
                    throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg", "misplaced string"}});
                }
            }
            else if (node instanceof Element) {
                if (element != null) {
                    throw new JCAXMLException("JCAXML_SYNTAX", new String[][]{{"msg", "too many elements"}});
                }
                element = (Element) node;
            }
        }

        return element;
    }
}

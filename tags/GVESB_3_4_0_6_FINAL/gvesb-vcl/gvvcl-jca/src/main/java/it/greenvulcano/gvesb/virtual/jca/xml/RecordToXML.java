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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class build a JCA XML starting from a
 * <code>javax.resource.cci.Record</code>. The given record must be a
 * <code>MappedRecord<code>, an <code>IndexedRecord<code> or
 * a custom record following the JavaBean specification.<br/>
 * The produced XML will follow the specification as described in the
 * <b>"<i>to be defined</i>"</b> document.
 * <p>
 * This class is not thread safe. In a multi-thread environment, each thread should have
 * its own instance if the <code>RecordToXML</code> object.
 * <p>
 * The <code>RecordToXML</code> should not be directly used. Use
 * <code>JCAXML</code> instead that wrap both a <code>XMLToRecord</code> and a
 * <code>RecordToXML</code> objects.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class RecordToXML
{
    /**
     *
     */
    public static final Object[] EMPTY_PARAMETERS     = new Object[0];
    /**
     *
     */
    public static final int      BYTE_ARRAY_AS_STRING = 0;
    /**
     *
     */
    public static final int      BYTE_ARRAY_AS_ENC64  = 1;
    /**
     *
     */
    public static final int      BYTE_ARRAY_AS_XML    = 2;

    private DocumentBuilder      documentBuilder      = null;
    private String               namespacePrefix      = "jca";
    private String               nsPrefix             = "jca:";
    private int                  byteArrayAs          = BYTE_ARRAY_AS_ENC64;
    private String               encoding             = null;

    /**
     * Build a <code>RecordToXML</code> object.
     *
     * @param documentBuilder
     */
    public RecordToXML(DocumentBuilder documentBuilder)
    {
        this.documentBuilder = documentBuilder;
    }

    /**
     * Set the prefix for the namespace.
     *
     * @param prefix
     *        namespace prefix for the produced XML. If null or empty, then the
     *        namespace will have not a prefix.
     */
    public void setNSPrefix(String prefix)
    {
        namespacePrefix = prefix;
        if (namespacePrefix == null) {
            nsPrefix = "";
        }
        else {
            namespacePrefix = namespacePrefix.trim();
            if (namespacePrefix.equals("")) {
                namespacePrefix = null;
                nsPrefix = "";
            }
            else {
                nsPrefix = namespacePrefix + ":";
            }
        }

    }

    /**
     * Return the prefix for the namespace.
     *
     * @return the namespace prefix or null if the prefix is not set.
     */
    public String getNSPrefix()
    {
        return namespacePrefix;
    }

    /**
     * Return the settings for byte[].
     *
     * @return a BYTE_ARRAY_AS_xxx constant
     */
    public int getByteArrayAs()
    {
        return byteArrayAs;
    }

    /**
     * Set the byte[] interpretation as normal String. The encoding is the
     * default encoding of the platform.
     */
    public void byteArrayAsString()
    {
        byteArrayAs = BYTE_ARRAY_AS_STRING;
        encoding = null;
    }

    /**
     * Set the byte[] interpretation as normal String.
     *
     * @param encoding
     *        set the encoding to use for interpreting the array.
     */
    public void byteArrayAsString(String encoding)
    {
        byteArrayAs = BYTE_ARRAY_AS_STRING;
        this.encoding = encoding;
    }

    /**
     * Set the byte[] interpretation as XML.
     */
    public void byteArrayAsXML()
    {
        byteArrayAs = BYTE_ARRAY_AS_XML;
    }

    /**
     * Set the byte[] interpretation as a String containing the base64 encoding
     * of the array.
     */
    public void byteArrayAsEncode64()
    {
        byteArrayAs = BYTE_ARRAY_AS_ENC64;
    }

    /**
     * Build a XML following the JCA XML specifications, starting from a given
     * Record.
     *
     * @param record
     * @return a XML following the JCA XML specifications, starting from a given
     *         Record.
     * @throws JCAXMLException
     */
    public Document buildDocument(Record record) throws JCAXMLException
    {
        Document document = buildPreamble();
        Element root = document.getDocumentElement();
        produceXML(root, record);
        return document;
    }

    private void produceXML(Element parent, Object record) throws JCAXMLException
    {
        if (record instanceof MappedRecord) {
            produceMappedRecord(parent, (MappedRecord) record);
        }
        else if (record instanceof IndexedRecord) {
            produceIndexedRecord(parent, (IndexedRecord) record);
        }
        else { // JavaBean

            produceCustomRecord(parent, record);
        }
    }

    /**
     * Produce the JCA XML for a <code>MappedRecord</code>.
     */
    private void produceMappedRecord(Element parent, MappedRecord record) throws JCAXMLException
    {
        Element mapElement = parent.getOwnerDocument().createElementNS(JCAXML.NAMESPACE_URI, nsPrefix + "map");
        mapElement.setAttribute("name", record.getRecordName());
        parent.appendChild(mapElement);

        Iterator<?> i = record.keySet().iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            Object value = record.get(key);
            produceEntry(mapElement, key, value);
        }
    }

    private void produceEntry(Element mapElement, String key, Object value) throws JCAXMLException
    {
        Element entryElement = mapElement.getOwnerDocument().createElementNS(JCAXML.NAMESPACE_URI, nsPrefix + "entry");
        entryElement.setAttribute("key", key);
        mapElement.appendChild(entryElement);
        produceValue(entryElement, value);
    }

    /**
     * Produce the JCA XML for a <code>IndexedRecord</code>.
     */
    private void produceIndexedRecord(Element parent, IndexedRecord record) throws JCAXMLException
    {
        Element listElement = parent.getOwnerDocument().createElementNS(JCAXML.NAMESPACE_URI, nsPrefix + "list");
        listElement.setAttribute("name", record.getRecordName());
        parent.appendChild(listElement);

        Iterator<?> i = record.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            produceItem(listElement, value);
        }
    }

    private void produceItem(Element listElement, Object value) throws JCAXMLException
    {
        Element itemElement = listElement.getOwnerDocument().createElementNS(JCAXML.NAMESPACE_URI, nsPrefix + "item");
        listElement.appendChild(itemElement);
        produceValue(itemElement, value);
    }

    /**
     * Produce the JCA XML for a <code>MappedRecord</code>.
     */
    private void produceCustomRecord(Element parent, Object record) throws JCAXMLException
    {
        Element recordElement = parent.getOwnerDocument().createElementNS(JCAXML.NAMESPACE_URI, nsPrefix + "custom");
        Class<?> recordClass = record.getClass();
        recordElement.setAttribute("class", recordClass.getName());
        parent.appendChild(recordElement);

        BeanInfo info = null;
        try {
            info = Introspector.getBeanInfo(recordClass);
            PropertyDescriptor properties[] = info.getPropertyDescriptors();
            for (int i = 0; i < properties.length; ++i) {
                String property = properties[i].getName();
                Method method = properties[i].getReadMethod();
                if ((method != null) && (method.getDeclaringClass() != Object.class)) {
                    Class<?> types[] = method.getParameterTypes();
                    if (types.length != 0) {
                        throw new JCAXMLException("JCAXML_INTROSPECTION_EXCEPTION", new String[][]{{"msg",
                                "no indexed properties supported"}});
                    }
                    Object value = method.invoke(record, EMPTY_PARAMETERS);
                    produceProperty(recordElement, property, value);
                }
            }
        }
        catch (IntrospectionException exc) {
            throw new JCAXMLException("JCAXML_INTROSPECTION_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }
        catch (IllegalAccessException exc) {
            throw new JCAXMLException("JCAXML_INVOKE_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }
        catch (IllegalArgumentException exc) {
            throw new JCAXMLException("JCAXML_INVOKE_EXCEPTION", new String[][]{{"msg", exc.getMessage()}}, exc);
        }
        catch (InvocationTargetException exc) {
            throw new JCAXMLException("JCAXML_INVOKE_EXCEPTION", new String[][]{{"msg",
                    exc.getTargetException().getMessage()}}, exc.getTargetException());
        }
    }

    private void produceProperty(Element recordElement, String property, Object value) throws JCAXMLException
    {
        Element propertyElement = recordElement.getOwnerDocument().createElementNS(JCAXML.NAMESPACE_URI,
                nsPrefix + "property");
        propertyElement.setAttribute("name", property);
        recordElement.appendChild(propertyElement);
        produceValue(propertyElement, value);
    }

    /**
     * Build the root element of the JCAXML to produce.
     */
    private Document buildPreamble()
    {
        DOMImplementation implementation = documentBuilder.getDOMImplementation();
        Document document = implementation.createDocument(JCAXML.NAMESPACE_URI, nsPrefix + "record", null);

        Element root = document.getDocumentElement();
        if (namespacePrefix != null) {
            root.setAttribute("xmlns:" + namespacePrefix, JCAXML.NAMESPACE_URI);
        }
        else {
            root.setAttribute("xmlns", JCAXML.NAMESPACE_URI);
        }
        root.setAttribute("version", "1.0");

        return document;
    }

    private void produceValue(Element parent, Object value) throws JCAXMLException
    {
        String type = getType(value);
        parent.setAttribute("type", type);
        if (type.equals("record")) {
            produceXML(parent, value);
        }
        else if (!type.equals("null")) {
            Node valueNode = null;
            if (value instanceof byte[]) {
                valueNode = produceByteArrayValue(parent.getOwnerDocument(), (byte[]) value);
            }
            else {
                valueNode = parent.getOwnerDocument().createTextNode(value.toString());
            }
            parent.appendChild(valueNode);
        }
    }

    /**
     * Produces the Node for the JCAXML based on the value of the
     * <code>byteArrayAs</code> flag.
     *
     * @param document
     *        owner document
     * @param value
     * @return
     */
    private Node produceByteArrayValue(Document document, byte[] value) throws JCAXMLException
    {
        switch (byteArrayAs) {
            case BYTE_ARRAY_AS_ENC64 :
                return produceByteArrayValueAsBase64(document, value);

            case BYTE_ARRAY_AS_STRING :
                return produceByteArrayValueAsString(document, value);

            case BYTE_ARRAY_AS_XML :
                return produceByteArrayValueAsXML(document, value);

            default :
                return produceByteArrayValueAsBase64(document, value);
        }
    }

    /**
     * @param document
     * @param value
     * @return
     */
    private Node produceByteArrayValueAsBase64(Document document, byte[] value)
    {
        String encodedArray = new String(Base64.encodeBase64(value));
        return document.createTextNode(encodedArray);
    }

    /**
     * @param document
     * @param value
     * @return
     */
    private Node produceByteArrayValueAsString(Document document, byte[] value) throws JCAXMLException
    {
        String string = null;
        if (encoding == null) {
            string = new String(value);
        }
        else {
            try {
                string = new String(value, encoding);
            }
            catch (UnsupportedEncodingException exc) {
                throw new JCAXMLException("JCAXML_UNSUPPORTED_ENCODING", new String[][]{{"encoding", encoding}}, exc);
            }
        }
        return document.createTextNode(string);
    }

    /**
     * @param document
     * @param value
     * @return
     */
    private Node produceByteArrayValueAsXML(Document document, byte[] value) throws JCAXMLException
    {
        Document dom;
        try {
            dom = documentBuilder.parse(new ByteArrayInputStream(value));
        }
        catch (Exception exc) {
            throw new JCAXMLException("JCAXML_XML_EXCEPTION", new String[][]{{"exc", "" + exc}}, exc);
        }
        Element element = dom.getDocumentElement();
        return document.importNode(element, true);
    }

    private String getType(Object value)
    {
        if (value == null) {
            return "null";
        }

        Class<?> cls = value.getClass();

        // L'ordine dei test e' stato identificato secondo la probabilita' di
        // trovare i tipi di dato corrispondenti.
        // E' possibile che qualcun altro abbia una diversa sensibilita', ma ora
        // l'ordine e' questo!!!

        if (value instanceof String) {
            return "string";
        }
        else if ((value instanceof Integer) || (cls == Integer.TYPE)) {
            return "int";
        }
        else if ((value instanceof Long) || (cls == Long.TYPE)) {
            return "long";
        }
        else if ((value instanceof Boolean) || (cls == Boolean.TYPE)) {
            return "boolean";
        }
        else if ((value instanceof Character) || (cls == Character.TYPE)) {
            return "char";
        }
        else if ((value instanceof Double) || (cls == Double.TYPE)) {
            return "double";
        }
        else if ((value instanceof Float) || (cls == Float.TYPE)) {
            return "float";
        }
        else if ((value instanceof Short) || (cls == Short.TYPE)) {
            return "short";
        }
        else if (value instanceof Record) {
            return "record";
        }
        else if ((value instanceof Byte) || (cls == Byte.TYPE)) {
            return "byte";
        }
        else if (value instanceof BigDecimal) {
            return "double";
        }
        else if (value instanceof BigInteger) {
            return "long";
        }
        else if (value instanceof byte[]) {
            switch (byteArrayAs) {
                case BYTE_ARRAY_AS_STRING :
                case BYTE_ARRAY_AS_ENC64 :
                    return "string";
                case BYTE_ARRAY_AS_XML :
                    return "xml";
                default :
                    return "string";
            }
        }
        else {

            // Un generico oggetto Java proviamo a trattarlo come un custom
            // record.
            // D'altronde se abbiamo quest'oggetto vuol dire che un RA lo ha
            // ritornato!
            //
            return "record";
        }
    }
}

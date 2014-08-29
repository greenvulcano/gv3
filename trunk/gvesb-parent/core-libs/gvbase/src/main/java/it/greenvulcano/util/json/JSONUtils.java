/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.util.json;

import it.greenvulcano.util.xml.XMLUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.5.0 29/ago/2014
 * @author GreenVulcano Developer Team
 *
 */
public class JSONUtils
{
    /**
     * Convert the input XML to a JSONObject.
     * JSON does not distinguish between elements and attributes.
     * Sequences of similar elements are represented as JSONArrays.
     * If an element have attributes, content text/cdata may be placed in a "contentText" member.
     * Comments and namespaces are ignored.
     * 
     * @param xml
     *        the document to convert
     * @return
     * @throws JSONUtilsException
     */
    public static JSONObject xmlToJson(Object xml) throws JSONUtilsException {
        return xmlToJson(xml, new HashSet<String>());
    }

    /**
     * Convert the input XML to a JSONObject.
     * JSON does not distinguish between elements and attributes.
     * Sequences of similar elements (or elements which local-name are in forceElementsArray)
     * are represented as JSONArrays. 
     * If an element have attributes, content text/cdata may be placed in a "contentText" member.
     * Comments and namespaces are ignored.
     *
     * @param xml
     *        the document to convert
     * @param forceElementsArray
     *        a set containing element's local-name to be forced as JSONArray also if in single instance
     * @return
     * @throws JSONUtilsException
     */
    public static JSONObject xmlToJson(Object xml, Set<String> forceElementsArray) throws JSONUtilsException {
        XMLUtils parser = null;
        JSONObject result = new JSONObject();
        try {
            parser = XMLUtils.getParserInstance();
            Node node = parser.parseObject(xml, false, true, true);
            Element el = null;
            
            if (node instanceof Document) {
                el = ((Document) node).getDocumentElement();
            }
            else {
                el = (Element) node;
            }
            
            String name = el.getLocalName();
            
            if (forceElementsArray.contains(name)) {
                result.append(name, processElement(parser, el, forceElementsArray));
            }
            else {
                result.put(name, processElement(parser, el, forceElementsArray));
            }
            
            return result;
        }
        catch (JSONUtilsException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new JSONUtilsException("Error converting XML to JSON", exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    private static Object processElement(XMLUtils parser, Element el, Set<String> forceElementsArray) throws JSONUtilsException {
        try {
            if (el.hasAttributes() || el.hasChildNodes()) {
                JSONObject current = new JSONObject();
                boolean hasAttributes = el.hasAttributes();
                if (hasAttributes) {
                    NamedNodeMap attrs = el.getAttributes();
                    int len = attrs.getLength();
                    for (int i = 0; i < len; i++) {
                        Node att = attrs.item(i);
                        String name = att.getLocalName();
                        String value = parser.getNodeContent(att);
                        current.put(name, stringToValue(value));
                    }
                }
                if (el.hasChildNodes()) {
                    NodeList nl = el.getChildNodes();
                    boolean hasElementChild = false;
                    int len = nl.getLength();
                    for (int i = 0; i < len; i++) {
                        Node n = nl.item(i);
                        String name = n.getLocalName();
                        short nodeType = n.getNodeType();
                        switch (nodeType) {
                            case Node.ELEMENT_NODE :
                                hasElementChild = true;
                                if (forceElementsArray.contains(name)) {
                                    current.append(name, processElement(parser, (Element) n, forceElementsArray));
                                }
                                else {
                                    current.accumulate(name, processElement(parser, (Element) n, forceElementsArray));
                                }
                                break;
                            case Node.CDATA_SECTION_NODE :
                            case Node.TEXT_NODE :
                                if (hasElementChild) {
                                    break;
                                }
                                if (!"".equals(n.getTextContent().trim())) {
                                    Object value = stringToValue(parser.getNodeContent(el));
                                    if (hasAttributes) {
                                        current.put("contentText", value);
                                        return current;
                                    }
                                        
                                    return value;
                                }
                            default :
                        }
                    }
                }
                return current;
            }
            else {
                return "";
            }
        }
        catch (JSONUtilsException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new JSONUtilsException("Error converting Element[" + el.getTagName() + "] to JSON", exc);
        }
    }
    
    /**
     * Convert a JSONObject into an XML structure.
     * 
     * @param json 
     *        a JSONObject
     * @return 
     * @throws  JSONException
     */
    public static Node jsonToXml(Object json) throws JSONUtilsException {
        return jsonToXml(json, null, new HashSet<String>()); 
    }

    /**
     * Convert a JSONObject into an XML structure.
     * 
     * @param json 
     *        a JSONObject
     * @param forceAttribute
     *        a set containing keys name to be set as XML attributes
     * @return 
     * @throws  JSONException
     */
    public static Node jsonToXml(Object json, Set<String> forceAttributes) throws JSONUtilsException {
        return jsonToXml(json, null, forceAttributes); 
    }

    

    /**
     * Convert a JSONObject into Node structure.
     * @param json
     *        a JSONObject
     * @param rootName
     *        the optional name of the root element
     * @return
     * @throws JSONUtilsException
     */
    public static Node jsonToXml(Object json, String rootName) throws JSONUtilsException {
        return jsonToXml(json, rootName, new HashSet<String>());
    }

    /**
     * Convert a JSONObject into Node structure.
     * @param json
     *        a JSONObject
     * @param rootName
     *        the optional name of the root element
     * @param forceAttributes
     *        a set containing keys name to be set as XML attributes
     * @return
     * @throws JSONUtilsException
     */
    public static Node jsonToXml(Object json, String rootName, Set<String> forceAttributes) throws JSONUtilsException {
            XMLUtils parser = null;
        Document doc = null;
        try {
            parser = XMLUtils.getParserInstance();
            
            if (json instanceof String) {
                json = new JSONObject((String) json);
            }
            else if (json instanceof byte[]) {
                json = new JSONObject(new String((byte[]) json));
            }
            if (rootName != null) {
                doc = parser.newDocument(rootName);
                jsonToXml(parser, doc, json, null, doc.getDocumentElement(), forceAttributes);
            }
            else {
                doc = parser.newDocument();
                jsonToXml(parser, doc, json, null, null, forceAttributes);
            }
            
            return doc;
        }
        catch (JSONUtilsException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new JSONUtilsException("Error converting JSON to XML", exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }    
        
    private static Node jsonToXml(XMLUtils parser, Document doc, Object json, String tagName,
            Element context, Set<String> forceAttributes) throws JSONUtilsException {
        Element el = null;
        
        try {
            // create tagName element, if needed
            if (tagName != null) {
                el = parser.insertElement(context, tagName);
            }
            else {
                el = context;
            }

            if (json instanceof JSONObject) {
                // Loop thru the keys.
                JSONObject jo = (JSONObject) json;
                Iterator keys = jo.keys();
                while (keys.hasNext()) {
                    String key = keys.next().toString();
                    Object value = jo.opt(key);
                    if (value == null) {
                        value = "";
                    }

                    // Emit contentText in body
                    if ("contentText".equals(key)) {
                        if (value instanceof JSONArray) {
                            StringBuffer sb = new StringBuffer();
                            JSONArray ja = (JSONArray) value;
                            int length = ja.length();
                            for (int i = 0; i < length; i += 1) {
                                if (i > 0) {
                                    sb.append('\n');
                                }
                                sb.append(ja.get(i).toString());
                            }
                            parser.insertText(el, sb.toString());
                        } else {
                            parser.insertText(el, value.toString());
                        }
    
                    // Emit an array of similar keys
                    } else if (value instanceof JSONArray) {
                        JSONArray ja = (JSONArray) value;
                        int length = ja.length();
                        for (int i = 0; i < length; i += 1) {
                            value = ja.get(i);
                            jsonToXml(parser, doc, value, key, el, forceAttributes);
                        }
                    } else if ("".equals(value)) {
                        if (forceAttributes.contains(key)) {
                            parser.setAttribute(el, key, "");
                        }
                        else {
                            parser.insertElement(el, key);
                        }
    
                    // Emit a new tag <k>
                    } else {
                        if (doc.getDocumentElement() == null) {
                            jsonToXml(parser, doc, value, null, 
                                    (Element) doc.appendChild(parser.createElement(doc, key)), forceAttributes);
                        }
                        else {
                            if (forceAttributes.contains(key)) {
                                parser.setAttribute(el, key, value.toString());
                            }
                            else {
                                jsonToXml(parser, doc, value, key, el, forceAttributes);
                            }
                        }
                    }
                }
    
            // XML does not have good support for arrays. If an array appears in a place
            // where XML is lacking, synthesize an <array> element.
            } else {
                if (json.getClass().isArray()) {
                    json = new JSONArray(json);
                }
                if (json instanceof JSONArray) {
                    JSONArray ja = (JSONArray) json;
                    int length = ja.length();
                    for (int i = 0; i < length; i += 1) {
                        jsonToXml(parser, doc, ja.opt(i), tagName == null ? "array" : tagName, el, forceAttributes);
                    }
                } else {
                    String string = (json == null) ? "null" : json.toString();
                    //el = parser.insertElement(context, tagName);
                    parser.insertText(el, string);
                }
            }
            return el;
        }
        catch (Exception exc) {
            throw new JSONUtilsException("Error converting JSON[" + tagName + "] to XML[" + 
                                         (el != null ? el.getLocalName() : "null") + "]", exc);
        }
    }
    
    /**
     * Try to convert a string into a number, boolean, or null. If the string
     * can't be converted, return the string. This is much less ambitious than
     * JSONObject.stringToValue, especially because it does not attempt to
     * convert plus forms, octal forms, hex forms, or E forms lacking decimal
     * points.
     * @param string A String.
     * @return A simple JSON value.
     */
    public static Object stringToValue(String string) {
        if ("true".equalsIgnoreCase(string)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(string)) {
            return Boolean.FALSE;
        }
        if ("null".equalsIgnoreCase(string)) {
            return JSONObject.NULL;
        }

// If it might be a number, try converting it, first as a Long, and then as a
// Double. If that doesn't work, return the string.

        try {
            char initial = string.charAt(0);
            if (initial == '-' || (initial >= '0' && initial <= '9')) {
                Long value = new Long(string);
                if (value.toString().equals(string)) {
                    return value;
                }
            }
        }  catch (Exception ignore) {
            try {
                Double value = new Double(string);
                if (value.toString().equals(string)) {
                    return value;
                }
            }  catch (Exception ignoreAlso) {
            }
        }
        return string;
    }
}

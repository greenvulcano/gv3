/*
 * Copyright (c) 2004 Maxime Informatica s.n.c. - All right reserved
 *
 */
package max.documentation.dtd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import max.core.MaxException;
import max.xml.MaxXMLFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

public class DTDParser implements LexicalHandler, DeclHandler {
    //--------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------

    public static final String DUMMYELEMENT   = "__my_dummy__dummy___";

    protected StringBuffer     currentComment = new StringBuffer();
    protected XMLReader        parser         = null;
    protected Document         document;
    protected Element          root;

    /**
     * Maps element name to the Element node.
     *
     * Map[String, Element]
     */
    private Map                elements       = new HashMap();

    /**
     * Maps element name to parent element names.
     *
     * Map[String, TreeSet[String]]
     */
    private Map                usedIn         = new HashMap();

    /**
     * Relazione inversa di usedIn
     *
     * Map[String, TreeSet[String]]
     */
    private Map                children       = new HashMap();

    //--------------------------------------------------------------------------
    // CONSTRUCTORS
    //--------------------------------------------------------------------------

    public DTDParser() throws SAXException, MaxException {
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = parserFactory.newSAXParser();
            parser = saxParser.getXMLReader();
            parser.setProperty("http://xml.org/sax/properties/declaration-handler", this);
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", this);
            parser.setEntityResolver(MaxXMLFactory.instance().getEntityResolver());
        }
        catch (ParserConfigurationException exc) {
            throw new SAXException(exc);
        }
    }

    //--------------------------------------------------------------------------
    // HELPERS
    //--------------------------------------------------------------------------

    protected String getComment() {
        String ret = currentComment.toString();
        currentComment.delete(0, currentComment.length());
        return ret;
    }

    //--------------------------------------------------------------------------
    // IMPLEMENTAZIONE INTERFACCIA LexicalHandler
    //--------------------------------------------------------------------------

    public void comment(char[] ch, int start, int length) {
        String cmt = new String(ch, start, length);
        if (currentComment.length() != 0) {
            currentComment.append("\r\n");
        }
        currentComment.append(cmt);
    }

    public void endCDATA() {
    }

    public void endDTD() {
    }

    public void endEntity(String name) {
    }

    public void startCDATA() {
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver(parser.getEntityResolver());

            document = documentBuilder.newDocument();
            root = document.createElement("dtd");
            document.appendChild(root);

            currentComment = new StringBuffer();
        }
        catch (Exception exc) {
            throw new SAXException(exc);
        }
    }

    public void startEntity(String name) {
    }

    //--------------------------------------------------------------------------
    // IMPLEMENTAZIONE INTERFACCIA DeclHandler
    //--------------------------------------------------------------------------

    public void attributeDecl(String elementName, String attributeName, String type, String defaultType,
            String defaultValue) {
        String comment = getComment();

        Element attr = document.createElement("attribute");
        Element element = (Element) elements.get(elementName);
        if (element != null) {
            element.appendChild(attr);
            attr.setAttribute("name", attributeName);
            attr.setAttribute("type", type);
            attr.setAttribute("default-type", defaultType);
            attr.setAttribute("default-value", defaultValue);
            Node commentNode = manageComment(attr, comment);
            setComment(attr, commentNode);

            if (type.startsWith("(")) {
                StringTokenizer tokenizer = new StringTokenizer(type, "()|", false);
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken().trim();
                    Element value = document.createElement("value");
                    value.setAttribute("value", token);
                    attr.appendChild(value);
                }
            }
        }
        else {
            System.out.println("WARNING: skipping attribute '" + attributeName + "' for element '" + elementName
                    + "': probably the attribute was declared before the element");
        }
    }

    public void elementDecl(String name, String contentModel) {
        if (name.equals(DUMMYELEMENT)) {
            return;
        }
        String comment = getComment();

        Element element = document.createElement("element");
        elements.put(name, element);
        root.appendChild(element);
        element.setAttribute("name", name);
        processContentModel(element, contentModel);
        Node commentNode = manageComment(element, comment);
        setComment(element, commentNode);

        updateUsedIn(name, contentModel);
    }

    public void externalEntityDecl(String name, String publicId, String systemId) {
    }

    public void internalEntityDecl(String name, String value) {
    }

    //--------------------------------------------------------------------------
    // METHODS
    //--------------------------------------------------------------------------

    private void updateUsedIn(String name, String contentModel) {
        TreeSet childrenSet = (TreeSet) children.get(name);
        if (childrenSet == null) {
            childrenSet = new TreeSet();
            children.put(name, childrenSet);
        }

        if (contentModel.equals("ANY") || contentModel.equals("EMPTY")) {

            return;
        }

        StringTokenizer tokenizer = new StringTokenizer(contentModel, "|,?*+() ", false);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!token.equals("#PCDATA")) {
                TreeSet set = (TreeSet) usedIn.get(token);
                if (set == null) {
                    set = new TreeSet();
                    usedIn.put(token, set);
                }
                set.add(name);
                childrenSet.add(token);
            }
        }
    }

    private void processContentModel(Element el, String cm) {
        Document document = el.getOwnerDocument();
        Element model = document.createElement("model");
        el.appendChild(model);

        cm = cm.trim();
        StringTokenizer tokenizer = new StringTokenizer(cm, "|,?+*()", true);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("|")) {
                model.appendChild(document.createTextNode(" | "));
            }
            else if (token.equals(",")) {
                model.appendChild(document.createTextNode(", "));
            }
            else {
                if (!token.equals("#PCDATA") && !token.equals("?") && !token.equals("+") && !token.equals("*")
                        && !token.equals("(") && !token.equals(")")) {
                    Element child = document.createElement("child");
                    model.appendChild(child);
                    child.appendChild(document.createTextNode(token));
                }
                else {
                    model.appendChild(document.createTextNode(token));
                }
            }
        }
    }

    private Node manageComment(Element element, String comment) {
        StringBuffer newComment = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(comment, "\n", true);
        int countCR = 2;
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken().trim();

            if (line.equals("")) {
                if (countCR < 2) {
                    newComment.append("\n");
                }
                ++countCR;
            }
            else if (line.startsWith("#Template:")) {
                return format(element.getOwnerDocument(), newComment.toString());
            }
            else if (line.startsWith("#NotNull")) {
                Element notNull = document.createElement("not-null");
                element.appendChild(notNull);
            }
            else if (line.startsWith("#References")) {
                //...
            }
            else if (line.startsWith("#Choice")) {
                processChoice(element, line);
            }
            else if (line.startsWith("#Config")) {
                //...
            }
            else if (line.startsWith("#Document")) {
                //...
            }
            else if (line.startsWith("#Pattern")) {
                //...
            }
            else if (line.startsWith("#Label")) {
                //...
            }
            else if (line.startsWith("#Warn")) {
                //...
            }
            else if (line.startsWith("#Unique")) {
                //...
            }
            else if (line.startsWith("#Freezed")) {
                //...
            }
            else if (line.startsWith("#Counter")) {
                //...
            }
            else if (line.startsWith("#ExternalData")) {
                //...
            }
            else if (line.startsWith("#SelectOnInsert")) {
                //...
            }
            else if (line.startsWith("#Table")) {
                //...
            }
            else if (line.startsWith("#Graph")) {
                //...
            }
            else {
                newComment.append(line);
                countCR = 0;
            }
        }
        return format(element.getOwnerDocument(), newComment.toString());
    }

    private Node format(Document document, String str) {
        TextFormatter formatter = new TextFormatter(document, str);
        return formatter.format();
    }

    private void processChoice(Element element, String line) {
        Feature choice = Feature.getFeature(line);
        String param = choice.getParameter();
        StringTokenizer tokenizer = new StringTokenizer(param, "|", false);
        while (tokenizer.hasMoreTokens()) {
            String val = tokenizer.nextToken().trim();
            Element value = document.createElement("value");
            value.setAttribute("value", val);
            element.appendChild(value);
        }
    }

    //--------------------------------------------------------------------------
    // METHODS
    //--------------------------------------------------------------------------

    private void setComment(Element el, Node comment) {
        if (comment == null) {
            return;
        }

        Element descr = document.createElement("description");
        el.appendChild(descr);
        descr.appendChild(comment);
    }

    //--------------------------------------------------------------------------
    // SETTERS/GETTERS
    //--------------------------------------------------------------------------

    public Document getDocument() {
        return document;
    }

    public synchronized void setFeature(String feature, boolean val) throws SAXException {
        parser.setFeature(feature, val);
    }

    public synchronized void setEntityResolver(EntityResolver entityResolver) {
        parser.setEntityResolver(entityResolver);
    }

    //--------------------------------------------------------------------------
    // START PARSING
    //--------------------------------------------------------------------------

    public synchronized Document parseDTD(String rootElement, String publicId, String systemId) throws SAXException {
        try {
            File temp = File.createTempFile("temp_", ".xml");
            temp.deleteOnExit();
            FileOutputStream os = new FileOutputStream(temp);
            PrintStream out = new PrintStream(os);
            out.println("<?xml version=\"1.0\" standalone=\"no\"?>");

            String doctypeDeclaration = "";
            if ((publicId == null) || publicId.trim().equals("")) {
                doctypeDeclaration = "SYSTEM \"" + systemId + "\"";
            }
            else {
                doctypeDeclaration = "PUBLIC \"" + publicId + "\" \"" + systemId + "\"";
            }

            out.println("<!DOCTYPE " + DUMMYELEMENT + " " + doctypeDeclaration + " [");
            out.println("<!ELEMENT " + DUMMYELEMENT + " EMPTY>");
            out.println("]>");
            out.println("<" + DUMMYELEMENT + "/>");
            out.close();
            parser.parse(temp.getAbsolutePath());
            removeUnusedElements(rootElement);
            buildUsedInElements();
            temp.delete();
            return document;
        }
        catch (IOException exc) {
            throw new SAXException(exc);
        }
    }

    private void removeUnusedElements(String rootElement) {
        Set usedElements = new HashSet();
        LinkedList toVisitElements = new LinkedList();
        toVisitElements.add(rootElement);

        while (toVisitElements.size() != 0) {
            String elementName = (String) toVisitElements.removeFirst();
            if (!usedElements.contains(elementName)) {
                usedElements.add(elementName);
                Set elementChildren = (Set) children.get(elementName);
                if (elementChildren != null) {
                    toVisitElements.addAll((Set) children.get(elementName));
                }
                else {
                    System.out.println("WARNING: element '" + elementName + "' not defined");
                }
            }
        }

        Iterator it = elements.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String elementName = (String) entry.getKey();
            if (!usedElements.contains(elementName)) {
                Element element = (Element) entry.getValue();
                element.getParentNode().removeChild(element);
            }
        }

        elements.keySet().retainAll(usedElements);
        usedIn.keySet().retainAll(usedElements);
    }

    private void buildUsedInElements() {
        Iterator i = usedIn.keySet().iterator();
        while (i.hasNext()) {
            String elementName = (String) i.next();
            //System.out.println("elementName: " + elementName);
            Element element = (Element) elements.get(elementName);
            if (element != null) {
                Set set = (Set) usedIn.get(elementName);
                Iterator j = set.iterator();
                while (j.hasNext()) {
                    String usedInName = (String) j.next();
                    if (elements.containsKey(usedInName)) {
                        Element usedInElement = document.createElement("used-in");
                        usedInElement.setAttribute("element", usedInName);
                        element.appendChild(usedInElement);
                    }
                }
            }
            else {
                System.out.println("WARNING: element '" + elementName + "' not defined");
            }
        }
    }
}
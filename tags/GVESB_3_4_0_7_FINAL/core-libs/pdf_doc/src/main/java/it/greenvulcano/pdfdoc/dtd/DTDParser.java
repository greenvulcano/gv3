/*
 * Copyright (c) 2004 Maxime Informatica s.n.c. - All right reserved
 *
 */
package it.greenvulcano.pdfdoc.dtd;

import it.greenvulcano.catalog.GVCatalogResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2003 - All right reserved
 */
public class DTDParser implements LexicalHandler, DeclHandler
{

    public static final String DUMMYELEMENT = "__dummy__";

    protected StringBuffer currentComment = new StringBuffer();
    protected XMLReader parser = null;
    protected Document document;
    protected Element root;

    /**
     * Maps element name to the Element node.
     *
     */
    private Map<String, Element> elements = new HashMap<String, Element>();

    /**
     * Maps element name to parent element names.
     *
     */
    private Map<String, Set<String>> usedIn = new HashMap<String, Set<String>>();

    /**
     * Reverse of usedIn
     *
     */
    private Map<String, Set<String>> children = new HashMap<String, Set<String>>();

    public DTDParser() throws SAXException
    {
        try {
            parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            parser.setProperty("http://xml.org/sax/properties/declaration-handler", this);
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", this);
            parser.setEntityResolver(new GVCatalogResolver());
        }
        catch(ParserConfigurationException exc) {
            throw new SAXException(exc);
        }
    }

    protected String getComment()
    {
        String ret = currentComment.toString();
        currentComment.delete(0, currentComment.length());
        return ret;
    }

    public void comment(char[] ch, int start, int length)
    {
        String cmt = new String(ch, start, length);
        if(currentComment.length() != 0) {
            currentComment.append("\r\n");
        }
        currentComment.append(cmt);
    }

    public void endCDATA()
    {
        // do nothing
    }

    public void endDTD()
    {
        // do nothing
    }

    public void endEntity(String name)
    {
        // do nothing
    }

    public void startCDATA()
    {
        // do nothing
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException
    {
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
        catch(Exception exc) {
            throw new SAXException(exc);
        }
    }

    public void startEntity(String name)
    {
        // do nothing
    }

    public void attributeDecl(String elementName, String attributeName, String type,
        String defaultType, String defaultValue)
    {
        String comment = getComment();

        Element attr = document.createElement("attribute");
        Element element = elements.get(elementName);
        if(element != null) {
            element.appendChild(attr);
            attr.setAttribute("name", attributeName);
            attr.setAttribute("type", type);
            attr.setAttribute("default-type", defaultType);
            attr.setAttribute("default-value", defaultValue);
            Node commentNode = manageComment(attr, comment);
            setComment(attr, commentNode);

            if(type.startsWith("(")) {
                StringTokenizer tokenizer = new StringTokenizer(type, "()|", false);
                while(tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken().trim();
                    Element value = document.createElement("value");
                    value.setAttribute("value", token);
                    attr.appendChild(value);
                }
            }
        }
        else {
            System.out.println("WARNING: skipping attribute '" + attributeName + "' for element '"
                + elementName + "': probably the attribute was declared before the element");
        }
    }

    public void elementDecl(String name, String contentModel)
    {
        if(name.equals(DUMMYELEMENT)) return;
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

    public void externalEntityDecl(String name, String publicId, String systemId)
    {
        // do nothing
    }

    public void internalEntityDecl(String name, String value)
    {
        // do nothing
    }


    private void updateUsedIn(String name, String contentModel)
    {
        Set<String> childrenSet = children.get(name);
        if(childrenSet == null) {
            childrenSet = new TreeSet<String>();
            children.put(name, childrenSet);
        }

        if(contentModel.equals("ANY") || contentModel.equals("EMPTY")) {
            return;
        }

        StringTokenizer tokenizer = new StringTokenizer(contentModel, "|,?*+() ", false);

        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if(!token.equals("#PCDATA")) {
                Set<String> set = usedIn.get(token);
                if(set == null) {
                    set = new TreeSet<String>();
                    usedIn.put(token, set);
                }
                set.add(name);
                childrenSet.add(token);
            }
        }
    }

    private void processContentModel(Element el, String cm)
    {
        Document doc = el.getOwnerDocument();
        Element model = doc.createElement("model");
        el.appendChild(model);

        cm = cm.trim();
        StringTokenizer tokenizer = new StringTokenizer(cm, "|,?+*()", true);

        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if(token.equals("|")) {
                model.appendChild(doc.createTextNode(" | "));
            }
            else if(token.equals(",")) {
                model.appendChild(doc.createTextNode(", "));
            }
            else {
                if(!token.equals("#PCDATA") && !token.equals("?") && !token.equals("+")
                    && !token.equals("*") && !token.equals("(") && !token.equals(")")) {
                    Element child = doc.createElement("child");
                    model.appendChild(child);
                    child.appendChild(doc.createTextNode(token));
                }
                else {
                    model.appendChild(doc.createTextNode(token));
                }
            }
        }
    }

    private Node manageComment(Element element, String comment)
    {
        StringBuffer newComment = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(comment, "\n", true);
        int countCR = 2;
        while(tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken().trim();

            if(line.equals("")) {
                if(countCR < 2) {
                    newComment.append("\n");
                }
                ++countCR;
            }
            else if(line.startsWith("#Template:")) {
                return format(element.getOwnerDocument(), newComment.toString());
            }
            else if(line.startsWith("#NotNull")) {
                Element notNull = document.createElement("not-null");
                element.appendChild(notNull);
            }
            else if(line.startsWith("#References")) {
                //...
            }
            else if(line.startsWith("#Choice")) {
                processChoice(element, line);
            }
            else if(line.startsWith("#Config")) {
                //...
            }
            else if(line.startsWith("#Document")) {
                //...
            }
            else if(line.startsWith("#Pattern")) {
                //...
            }
            else if(line.startsWith("#Label")) {
                //...
            }
            else if(line.startsWith("#Warn")) {
                //...
            }
            else if(line.startsWith("#Unique")) {
                //...
            }
            else if(line.startsWith("#Freezed")) {
                //...
            }
            else if(line.startsWith("#Counter")) {
                //...
            }
            else if(line.startsWith("#ExternalData")) {
                //...
            }
            else if(line.startsWith("#SelectOnInsert")) {
                //...
            }
            else if(line.startsWith("#Table")) {
                //...
            } else if(line.startsWith("#Graph")) {
                //...
            }
            else {
                newComment.append(line);
                countCR = 0;
            }
        }
        return format(element.getOwnerDocument(), newComment.toString());
    }

    private Node format(Document doc, String str)
    {
        TextFormatter formatter = new TextFormatter(doc, str);
        return formatter.format();
    }

    private void processChoice(Element element, String line)
    {
        Feature choice = Feature.getFeature(line);
        String param = choice.getParameter();
        StringTokenizer tokenizer = new StringTokenizer(param, "|", false);
        while(tokenizer.hasMoreTokens()) {
            String val = tokenizer.nextToken().trim();
            Element value = document.createElement("value");
            value.setAttribute("value", val);
            element.appendChild(value);
        }
    }


    private void setComment(Element el, Node comment)
    {
        if(comment == null) {
            return;
        }

        Element descr = document.createElement("description");
        el.appendChild(descr);
        descr.appendChild(comment);
    }

    public Document getDocument()
    {
        return document;
    }

    public synchronized void setFeature(String feature, boolean val) throws SAXException
    {
        parser.setFeature(feature, val);
    }

    public synchronized void setEntityResolver(EntityResolver entityResolver)
    {
        parser.setEntityResolver(entityResolver);
    }

    public synchronized Document parseDTD(String rootElement, String publicId, String systemId)
        throws SAXException
    {
        try {
            File temp = File.createTempFile("temp_", ".xml");
            temp.deleteOnExit();
            FileOutputStream os = new FileOutputStream(temp);
            PrintStream out = new PrintStream(os);
            out.println("<?xml version=\"1.0\" standalone=\"no\"?>");

            String doctypeDeclaration = "";
            if(publicId == null || publicId.trim().equals("")) {
                doctypeDeclaration = "SYSTEM \"" + systemId + "\"";
            }
            else {
                doctypeDeclaration = "PUBLIC \"" + publicId + "\" \"" + systemId + "\"";
            }

            /*out.println("<!DOCTYPE " + DUMMYELEMENT + " " + doctypeDeclaration + " [");
            out.println("<!ELEMENT " + DUMMYELEMENT + " EMPTY>");
            out.println("]>");*/
            out.println("<!DOCTYPE " + DUMMYELEMENT + " " + doctypeDeclaration + ">");
            out.println("<" + DUMMYELEMENT + "/>");
            out.close();
            parser.parse(temp.getAbsolutePath());
            removeUnusedElements(rootElement);
            buildUsedInElements();
            temp.delete();
            return document;
        }
        catch(IOException exc) {
            throw new SAXException(exc);
        }
    }

    private void removeUnusedElements(String rootElement)
    {
        Set<String> usedElements = new HashSet<String>();
        LinkedList<String> toVisitElements = new LinkedList<String>();
        toVisitElements.add(rootElement);

        while(toVisitElements.size() != 0) {
            String elementName = toVisitElements.removeFirst();
            if(!usedElements.contains(elementName)) {
                usedElements.add(elementName);
                Set<String> elementChildren = children.get(elementName);
                if(elementChildren != null) {
                    toVisitElements.addAll(children.get(elementName));
                }
                else {
                    System.out.println("WARNING: element '" + elementName + "' not defined");
                }
            }
        }

        for(Entry<String, Element> entry : elements.entrySet()) {
            String elementName = entry.getKey();
            if(!usedElements.contains(elementName)) {
                Element element = entry.getValue();
                element.getParentNode().removeChild(element);
            }
        }

        elements.keySet().retainAll(usedElements);
        usedIn.keySet().retainAll(usedElements);
    }

    private void buildUsedInElements()
    {
        for(Entry<String, Set<String>> entry : usedIn.entrySet()) {
            String elementName = entry.getKey();
            Element element = elements.get(elementName);
            if(element != null) {
                Set<String> set = entry.getValue();
                for(String usedInName : set) {
                    if(elements.containsKey(usedInName)) {
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
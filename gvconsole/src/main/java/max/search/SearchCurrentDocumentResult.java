/*
 * Copyright (c) 2005 Maxime Informatica s.n.c. - All right reserved
 *
 * Created on 13-mag-2005
 *
 */
package max.search;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Ritorna un xml con la seguente struttura:
 * <pre>
 *  search-result, attributes: matches, nodes
 *    |
 *    +-- element, attributes: index
 *    |      |
 *    |      +-- match
 *    |      |     +- text()
 *    |      |
 *    |      +-- match
 *    |      |     +- text()
 *    |     ...
 *    |
 *    +-- element, attributes: index, node-name
 *    |      |
 *    |      +-- match
 *    |      |     +- text()
 *    |      |
 *    |      +-- match
 *    |      |     +- text()
 *    |     ...
 *   ...
 * </pre>
 * @author Maxime Informatica s.n.c. - Copyright (c) 2005 - All right reserved
 */
public class SearchCurrentDocumentResult {
    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    private Document document;
    private Element  root;
    private Map      elementToResult;
    private Map      idToElement = new HashMap();
    private int      nodeCount;

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    //----------------------------------------------------------------------------------------------

    public SearchCurrentDocumentResult() {
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    public Document createResultDocument(Collection nodes) throws Exception {
        return createResultDocument(nodes, nodes.size());
    }

    public Document createResultDocument(NodeList nodes) throws Exception {
        return createResultDocument(nodes, nodes.getLength());
    }

    public Element getElementFromId(int id) {
        return (Element) idToElement.get(new Integer(id));
    }

    //----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    //----------------------------------------------------------------------------------------------

    private Document createResultDocument(Object nodes, int size) throws Exception {
        prepareDocument();
        elementToResult = new LinkedHashMap();
        nodeCount = 0;

        if (nodes instanceof Collection) {
            fillList((Collection) nodes);
        }
        else if (nodes instanceof NodeList) {
            fillList((NodeList) nodes);
        }

        root.setAttribute("matches", "" + size);
        root.setAttribute("nodes", "" + nodeCount);

        elementToResult = null;

        return root.getOwnerDocument();
    }

    private void fillList(Collection nodes) {
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            fillList(node);
        }
    }

    private void fillList(NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); ++i) {
            fillList(nodes.item(i));
        }
    }

    /**
     * @param node
     */
    private void fillList(Node node) {
        switch (node.getNodeType()) {

        case Node.ATTRIBUTE_NODE:
            appendText(node, "Atribute: " + node.getNodeName() + "=" + node.getNodeValue());
            break;

        case Node.CDATA_SECTION_NODE:
        case Node.TEXT_NODE:
            appendText(node, "Text: " + node.getNodeValue());
            break;

        case Node.COMMENT_NODE:
            appendText(node, "Comment: " + node.getNodeValue());
            break;

        case Node.ELEMENT_NODE:
            appendText(node, "Element: " + node.getNodeName());
            break;
        }
    }

    private void appendText(Node node, String text) {
        Element element = findElement(node);
        Element resultElement = (Element) elementToResult.get(element);
        if (resultElement == null) {
            ++nodeCount;
            resultElement = document.createElement("element");
            elementToResult.put(element, resultElement);
            resultElement.setAttribute("index", "" + nodeCount);
            resultElement.setAttribute("node-name", element.getNodeName());
            root.appendChild(resultElement);
            idToElement.put(new Integer(nodeCount), element);
        }

        Element match = document.createElement("match");
        resultElement.appendChild(match);
        Text textNode = document.createTextNode(text);
        match.appendChild(textNode);
    }

    private Element findElement(Node node) {
        int nodeType = node.getNodeType();

        if (nodeType == Node.ATTRIBUTE_NODE) {
            Attr attr = (Attr) node;
            return attr.getOwnerElement();
        }
        if (nodeType == Node.ELEMENT_NODE) {
            return (Element) node;
        }
        return findElement(node.getParentNode());
    }

    private void prepareDocument() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
        root = document.createElement("search-result");
        document.appendChild(root);
    }
}
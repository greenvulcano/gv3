/*
 * Copyright (c) 2005 Maxime Informatica s.n.c. - All right reserved
 *
 * Created on 3-giu-2005
 *
 */
package max.search;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import max.config.Config;
import max.xml.XMLBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2005 - All right reserved
 */
public class SearchForm {
    //----------------------------------------------------------------------------------------------
    // CONSTANTS
    //----------------------------------------------------------------------------------------------

    public static final String    CHECKED                 = "checked";
    public static final String    TYPE_PLAIN_TEXT         = "plainText";
    public static final String    TYPE_REGULAR_EXPRESSION = "regularExpression";
    public static final String    TYPE_XPATH              = "xpath";
    public static final String    START_ROOT              = "root";
    public static final String    START_CURRENT           = "current";

    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    private String                text;

    /**
     * plainText, regularExpression, xpath
     */
    private String                searchType;

    /**
     * root, current
     */
    private String                startingNode;

    private boolean               matchCase;
    private boolean               searchText;
    private boolean               searchAttributes;
    private boolean               searchAttributeNames;
    private boolean               searchElementNames;
    private boolean               searchComments;

    private String                action;
    private String                error;

    private SearchDocuments       searchDocuments         = new SearchDocuments(this);
    private SearchCurrentDocument searchCurrentDocument   = new SearchCurrentDocument(this);

    private int                   nodeId;

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    //----------------------------------------------------------------------------------------------

    public SearchForm() {
        text = "";
        searchType = TYPE_PLAIN_TEXT;
        startingNode = START_ROOT;
        matchCase = true;
        searchText = true;
        searchAttributes = true;
        searchAttributeNames = false;
        searchElementNames = false;
        searchComments = false;
        action = "";
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    public void resetBooleans() {
        matchCase = false;
        searchText = false;
        searchAttributes = false;
        searchAttributeNames = false;
        searchElementNames = false;
        searchComments = false;
    }

    public String getError() {
        if (error != null) {
            return error;
        }
        return "";
    }

    public String getErrorHTML() {
        return toHTML(getError());
    }

    /**
     * @param error
     */
    public void setError(String error) {
        this.error = error;
    }

    public String getAction() {
        return action;
    }

    public String getActionHTML() {
        return toHTML(getAction());
    }

    public void setAction(String action) {
        String siteRoot = Config.get("", "max.site.root");
        this.action = siteRoot + action;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getStartingNode() {
        return startingNode;
    }

    public void setStartingNode(String startingNode) {
        this.startingNode = startingNode;
    }

    public String getText() {
        return text;
    }

    public String getTextHTML() {
        return toHTML(getText());
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isMatchCase() {
        return matchCase;
    }

    public void setMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
    }

    public boolean isSearchAttributeNames() {
        return searchAttributeNames;
    }

    public void setSearchAttributeNames(boolean searchAttributeNames) {
        this.searchAttributeNames = searchAttributeNames;
    }

    public boolean isSearchAttributes() {
        return searchAttributes;
    }

    public void setSearchAttributes(boolean searchAttributes) {
        this.searchAttributes = searchAttributes;
    }

    public boolean isSearchComments() {
        return searchComments;
    }

    public void setSearchComments(boolean searchComments) {
        this.searchComments = searchComments;
    }

    public boolean isSearchElementNames() {
        return searchElementNames;
    }

    public void setSearchElementNames(boolean searchElementNames) {
        this.searchElementNames = searchElementNames;
    }

    public boolean isSearchText() {
        return searchText;
    }

    public void setSearchText(boolean searchText) {
        this.searchText = searchText;
    }

    public String checkSearchType(String value) {
        if (value.equals(searchType)) {
            return CHECKED;
        }
        return "";
    }

    public String checkStartingNode(String value) {
        if (value.equals(startingNode)) {
            return CHECKED;
        }
        return "";
    }

    public String checkSearchText() {
        return searchText ? CHECKED : "";
    }

    public String checkSearchAttributes() {
        return searchAttributes ? CHECKED : "";
    }

    public String checkSearchAttributeNames() {
        return searchAttributeNames ? CHECKED : "";
    }

    public String checkSearchElementNames() {
        return searchElementNames ? CHECKED : "";
    }

    public String checkSearchComments() {
        return searchComments ? CHECKED : "";
    }

    public String checkMatchCase() {
        return matchCase ? CHECKED : "";
    }

    public void manageBooleans(HttpServletRequest servletRequest) {
        if (servletRequest.getParameter("resetBooleans") != null) {
            resetBooleans();
        }
    }

    public void selectCurrentNode(HttpServletRequest request) throws Exception {
        XMLBuilder builder = XMLBuilder.getFromSession(request.getSession());

        Element element = searchCurrentDocument.getElementFromId(getNodeId());
        if (findDocument(element) == builder.getDocument()) {
            builder.setCurrentElement(element);
        }
    }

    public Document findDocument(Node node) {
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            return (Document) node;
        }
        node = node.getParentNode();
        if (node == null) {
            return null;
        }
        return findDocument(node);
    }

    public void performDocumentsSearch(HttpServletRequest request) throws Exception {
        error = null;
        searchDocuments.performDocumentsSearch(request);
    }

    public void performCurrentDocumentSearch(HttpServletRequest request) throws Exception {
        error = null;
        XMLBuilder builder = XMLBuilder.getFromSession(request.getSession());

        Node node = builder.getCurrentElement();
        searchCurrentDocument.performDocumentSearch(node);
    }

    public String showFoundDocuments() throws Exception {
        Document searchResult = searchDocuments.getSearchResult();

        if (searchResult == null) {

            return "";
        }

        int foundDocumentsNumber = searchDocuments.getFoundDocumentsNumber();

        if (foundDocumentsNumber == 0) {
            return "<hr><b>No documents found</b><br/>";
        }

        StringBuffer ret = new StringBuffer();
        ret.append("<hr>");
        ret.append("<b>Found ").append(foundDocumentsNumber).append(" document");
        if (foundDocumentsNumber > 1) {
            ret.append("s");
        }
        ret.append("</b><br/><br/>");

        ClassLoader loader = getClass().getClassLoader();
        InputStream stream = loader.getResourceAsStream("max/documents/document_list.xsl");

        Transformer transformer = (TransformerFactory.newInstance()).newTransformer(new StreamSource(stream));

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        StringWriter html = new StringWriter();
        transformer.transform(new DOMSource(searchResult), new StreamResult(html));

        ret.append(html.toString());

        return ret.toString();
    }

    public String showFoundMatches(HttpServletRequest request) throws Exception {
        Document searchedDocument = searchCurrentDocument.getSearchedDocument();
        XMLBuilder builder = XMLBuilder.getFromSession(request.getSession());

        Document currentDocument = builder.getDocument();
        if (searchedDocument != currentDocument) {
            return "";
        }

        Document searchResult = searchCurrentDocument.getSearchResult();

        if (searchResult == null) {
            return "";
        }

        ClassLoader loader = getClass().getClassLoader();
        InputStream stream = loader.getResourceAsStream("max/search/searchResult.xsl");

        Transformer transformer = (TransformerFactory.newInstance()).newTransformer(new StreamSource(stream));

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        StringWriter html = new StringWriter();
        transformer.transform(new DOMSource(searchResult), new StreamResult(html));

        return html.toString();
    }

    public String toHTML(String str) {
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

    //----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    //----------------------------------------------------------------------------------------------
}
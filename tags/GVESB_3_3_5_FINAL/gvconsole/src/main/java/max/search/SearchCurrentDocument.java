/*
 * Copyright (c) 2005 Maxime Informatica s.n.c. - All right reserved
 *
 * Created on 3-giu-2005
 *
 */
package max.search;

import java.util.Set;
import java.util.regex.PatternSyntaxException;

import max.xpath.XPath;
import max.xpath.XPathAPI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2005 - All right reserved
 */
public class SearchCurrentDocument {
    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    private SearchForm                  searchForm;

    private Document                    searchResult;
    private SearchCurrentDocumentResult searchCurrentDocumentResult;
    private Document                    searchedDocument;

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    //----------------------------------------------------------------------------------------------

    public SearchCurrentDocument(SearchForm searchForm) {
        this.searchForm = searchForm;
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    public Document getSearchResult() {
        return searchResult;
    }

    public Element getElementFromId(int id) {
        return searchCurrentDocumentResult.getElementFromId(id);
    }

    public void performDocumentSearch(Node node) throws Exception {
        searchResult = null;
        searchForm.setError(null);
        searchedDocument = node.getOwnerDocument();

        if (searchForm.getStartingNode().equals(SearchForm.START_ROOT)) {
            node = node.getOwnerDocument().getDocumentElement();
        }

        if (searchForm.getSearchType().equals(SearchForm.TYPE_XPATH)) {
            performXPathSearch(node);
        }
        else {
            performTextSearch(node);
        }
    }

    public Document getSearchedDocument() {
        return searchedDocument;
    }

    //----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void performTextSearch(Node node) throws Exception {
        SearchText searchText = null;
        try {
            searchText = new SearchText(searchForm.getText(), searchForm.getSearchType().equals(
                    SearchForm.TYPE_REGULAR_EXPRESSION));
        }
        catch (PatternSyntaxException exc) {
            searchForm.setError(exc.getMessage());
            return;
        }
        searchText.setMatchCase(searchForm.isMatchCase());
        searchText.setSearchAttributeNames(searchForm.isSearchAttributeNames());
        searchText.setSearchAttributes(searchForm.isSearchAttributes());
        searchText.setSearchComments(searchForm.isSearchComments());
        searchText.setSearchElementNames(searchForm.isSearchElementNames());
        searchText.setSearchText(searchForm.isSearchText());

        Set nodes = searchText.search(node);
        searchCurrentDocumentResult = new SearchCurrentDocumentResult();
        searchResult = searchCurrentDocumentResult.createResultDocument(nodes);
    }

    /**
     *
     */
    private void performXPathSearch(Node node) throws Exception {
        XPathAPI xpathAPI = new XPathAPI();
        XPath xpath = null;
        try {
            xpath = new XPath(searchForm.getText());
        }
        catch (Exception exc) {
            searchForm.setError("" + exc);
            return;
        }

        NodeList list = xpathAPI.selectNodeList(node, xpath);
        searchCurrentDocumentResult = new SearchCurrentDocumentResult();
        searchResult = searchCurrentDocumentResult.createResultDocument(list);
    }
}
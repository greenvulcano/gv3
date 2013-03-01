/*
 * Copyright (c) 2005 Maxime Informatica s.n.c. - All right reserved
 *
 * Created on 3-giu-2005
 *
 */
package max.search;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;

import max.documents.DocumentDescriptor;
import max.documents.DocumentRepository;
import max.documents.HttpServletRequestRoleCheck;
import max.documents.RoleCheck;
import max.xpath.XPath;
import max.xpath.XPathAPI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2005 - All right reserved
 */
public class SearchDocuments {
    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    private SearchForm searchForm;

    private Document   searchResult;
    private int        foundDocumentsNumber;

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    //----------------------------------------------------------------------------------------------

    public SearchDocuments(SearchForm searchForm) {
        this.searchForm = searchForm;
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    public Document getSearchResult() {
        return searchResult;
    }

    public int getFoundDocumentsNumber() {
        return foundDocumentsNumber;
    }

    public void performDocumentsSearch(HttpServletRequest request) throws Exception {
        searchResult = null;
        foundDocumentsNumber = 0;
        searchForm.setError(null);

        if (searchForm.getSearchType().equals(SearchForm.TYPE_XPATH)) {
            performXPathSearchOnAllDocuments(request);
        }
        else {
            performTextSearchOnAllDocuments(request);
        }
    }

    //----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void performTextSearchOnAllDocuments(HttpServletRequest request) throws Exception {
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

        SearchCriteria searchCriteria = new TextSearch(searchText);
        performSearchOnAllDocuments(searchCriteria, request);
    }

    /**
     *
     */
    private void performXPathSearchOnAllDocuments(HttpServletRequest request) throws Exception {
        XPathAPI xpathAPI = new XPathAPI();
        XPath xpath = null;
        try {
            xpath = new XPath(searchForm.getText());
        }
        catch (Exception exc) {
            searchForm.setError("" + exc);
            return;
        }

        SearchCriteria searchCriteria = new XPathSearch(xpathAPI, xpath);
        performSearchOnAllDocuments(searchCriteria, request);
    }

    private void performSearchOnAllDocuments(SearchCriteria searchCriteria, HttpServletRequest request)
            throws Exception {
        Set foundDocuments = new LinkedHashSet();

        HttpServletRequestRoleCheck roleCheck = new HttpServletRequestRoleCheck(request);
        DocumentRepository documentRepository = DocumentRepository.instance();
        String[] documentNames = documentRepository.getDocumentNames();
        for (int i = 0; i < documentNames.length; ++i) {
            String documentName = documentNames[i];
            DocumentDescriptor documentDescriptor = documentRepository.getDocumentDescriptor(documentName);
            if (checkRoles(documentDescriptor, roleCheck)) {
                try {
                    Document document = documentRepository.getDocument(documentName);
                    if (mustApplyFilter(documentDescriptor, roleCheck)) {
                        document = documentRepository.applySecurityFilter(documentName, document, roleCheck);
                    }
                    if (searchCriteria.accept(document)) {
                        foundDocuments.add(documentDescriptor);
                    }
                }
                catch (Exception exc) {
                    throw new Exception("Error during searching document '" + documentName + "' in group '"
                            + documentDescriptor.getGroup() + "', label: " + documentDescriptor.getLabel(), exc);
                }
            }
        }

        foundDocumentsNumber = foundDocuments.size();
        searchResult = documentRepository.showDocuments(roleCheck, foundDocuments);
    }

    private boolean checkRoles(DocumentDescriptor documentDescriptor, RoleCheck roleCheck) {
        if (roleCheck.isUserInSomeRole(documentDescriptor.getReadWriteRoles())) {
            return true;
        }
        if (roleCheck.isUserInSomeRole(documentDescriptor.getReadOnlyRoles())) {
            return true;
        }
        if (roleCheck.isUserInSomeRole(documentDescriptor.getExternalSystemRoles())) {
            return true;
        }
        return false;
    }

    private boolean mustApplyFilter(DocumentDescriptor documentDescriptor, RoleCheck roleCheck) {
        if (roleCheck.isUserInSomeRole(documentDescriptor.getReadWriteRoles())) {
            return false;
        }
        if (roleCheck.isUserInSomeRole(documentDescriptor.getReadOnlyRoles())) {
            return false;
        }
        return true;
    }

    //----------------------------------------------------------------------------------------------
    // UTILITIES INNER CLASSES
    //----------------------------------------------------------------------------------------------

    interface SearchCriteria {
        boolean accept(Document document) throws Exception;
    }

    class TextSearch implements SearchCriteria {
        SearchText searchText;

        TextSearch(SearchText searchText) {
            this.searchText = searchText;
        }

        public boolean accept(Document document) {
            Element element = document.getDocumentElement();
            Set result = searchText.search(element);
            return (result != null) && (result.size() > 0);
        }
    }

    class XPathSearch implements SearchCriteria {
        XPathAPI xpathAPI;
        XPath    xpath;

        XPathSearch(XPathAPI xpathAPI, XPath xpath) {
            this.xpathAPI = xpathAPI;
            this.xpath = xpath;
        }

        public boolean accept(Document document) throws Exception {
            NodeList nodeList = xpathAPI.selectNodeList(document, xpath);
            xpathAPI.reset();
            return (nodeList != null) && (nodeList.getLength() > 0);
        }
    }
}
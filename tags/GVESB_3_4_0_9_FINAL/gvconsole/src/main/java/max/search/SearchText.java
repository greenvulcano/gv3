/*
 * Copyright (c) 2005 Maxime Informatica s.n.c. - All right reserved
 *
 * Created on 13-mag-2005
 *
 */
package max.search;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2005 - All right reserved
 */
public class SearchText {
    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    private String  text;
    private String  upperCaseText;
    private boolean searchText;
    private boolean searchAttributes;
    private boolean searchAttributeNames;
    private boolean searchElementNames;
    private boolean searchComments;
    private boolean matchCase = true;
    private Pattern pattern;

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    //----------------------------------------------------------------------------------------------

    public SearchText(String text, boolean regularExpression) {
        setText(text, regularExpression);
        setSearchText(true);
        setSearchAttributes(true);
        setSearchAttributeNames(false);
        setSearchElementNames(false);
        setSearchComments(false);
    }

    //----------------------------------------------------------------------------------------------
    // ACCESSOR METHODS
    //----------------------------------------------------------------------------------------------

    /**
     * @return Returns the searchAttributeNames.
     */
    public boolean isSearchAttributeNames() {
        return searchAttributeNames;
    }

    /**
     * @param searchAttributeNames The searchAttributeNames to set.
     */
    public void setSearchAttributeNames(boolean searchAttributeNames) {
        this.searchAttributeNames = searchAttributeNames;
    }

    /**
     * @return Returns the searchAttributes.
     */
    public boolean isSearchAttributes() {
        return searchAttributes;
    }

    /**
     * @param searchAttributes The searchAttributes to set.
     */
    public void setSearchAttributes(boolean searchAttributes) {
        this.searchAttributes = searchAttributes;
    }

    /**
     * @return Returns the searchComments.
     */
    public boolean isSearchComments() {
        return searchComments;
    }

    /**
     * @param searchComments The searchComments to set.
     */
    public void setSearchComments(boolean searchComments) {
        this.searchComments = searchComments;
    }

    /**
     * @return Returns the searchElementNames.
     */
    public boolean isSearchElementNames() {
        return searchElementNames;
    }

    /**
     * @param searchElementNames The searchElementNames to set.
     */
    public void setSearchElementNames(boolean searchElementNames) {
        this.searchElementNames = searchElementNames;
    }

    /**
     * @return Returns the searchText.
     */
    public boolean isSearchText() {
        return searchText;
    }

    /**
     * @param searchText The searchText to set.
     */
    public void setSearchText(boolean searchText) {
        this.searchText = searchText;
    }

    /**
     * @return Returns the matchCase.
     */
    public boolean isMatchCase() {
        return matchCase;
    }

    /**
     * @param matchCase The matchCase to set.
     */
    public void setMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
        if (pattern != null) {
            pattern = Pattern.compile(text, matchCase ? 0 : Pattern.CASE_INSENSITIVE);
        }
    }

    /**
     * @return Returns the text.
     */
    public String getText() {
        return text;
    }

    /**
     * @param text The text to set.
     */
    public void setText(String text, boolean regularExpression) {
        this.text = text;
        upperCaseText = text.toUpperCase();
        if (regularExpression) {
            pattern = Pattern.compile(text, matchCase ? 0 : Pattern.CASE_INSENSITIVE);
        }
        else {
            pattern = null;
        }
    }

    public boolean isRegularExpression() {
        return pattern != null;
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    public Set search(Node startingNode) {
        Set selectedNodes = new LinkedHashSet();
        search(startingNode, selectedNodes);
        return selectedNodes;
    }

    public void search(Node startingNode, Set returnNodes) {
        checkMatching(startingNode, returnNodes);
        Node child = startingNode.getFirstChild();
        while (child != null) {
            search(child, returnNodes);
            child = child.getNextSibling();
        }
    }

    public boolean matches(Node node) {
        switch (node.getNodeType()) {
        case Node.ATTRIBUTE_NODE:
            if (searchAttributeNames && matches(node.getNodeName())) {
                return true;
            }
            return searchAttributes && matches(node.getNodeValue());

        case Node.CDATA_SECTION_NODE:
        case Node.TEXT_NODE:
            return searchText && matches(node.getNodeValue());

        case Node.COMMENT_NODE:
            return searchComments && matches(node.getNodeValue());

        case Node.ELEMENT_NODE:
            return searchElementNames && matches(node.getNodeName());
        }

        return false;
    }

    public boolean matches(String str) {
        if (pattern != null) {
            Matcher matcher = pattern.matcher(str);
            return matcher.find();
        }

        if (matchCase) {
            return str.indexOf(text) != -1;
        }
        else {
            return str.toUpperCase().indexOf(upperCaseText) != -1;
        }
    }

    //----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    //----------------------------------------------------------------------------------------------

    private void checkMatching(Node node, Set selectedNodes) {
        if (matches(node)) {
            selectedNodes.add(node);
        }

        if (searchAttributes || searchAttributeNames) {
            NamedNodeMap map = node.getAttributes();
            if (map != null) {
                for (int i = 0; i < map.getLength(); ++i) {
                    if (matches(map.item(i))) {
                        selectedNodes.add(map.item(i));
                    }
                }
            }
        }
    }
}
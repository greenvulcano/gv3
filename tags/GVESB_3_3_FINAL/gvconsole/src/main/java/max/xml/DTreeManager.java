/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:51 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/DTreeManager.java,v 1.1 2010-04-03 15:28:51 nlariviera Exp $
 * $Id: DTreeManager.java,v 1.1 2010-04-03 15:28:51 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class maintains the association element&lt;--&gt;id in order to
 * manage dtree.js
 *
 * @author
 */
public class DTreeManager
{
    //------------------------------------------------------------------------------------
    // FIELDS
    //------------------------------------------------------------------------------------

    /**
     *
     */
    private Map idToElement;

    /**
     *
     */
    private Map elementToId;

    /**
     *
     */
    private Map elementToInterface;

    /**
     *
     */
    private int counter;

    /**
     *
     */
    private Document aDocument = null;

    /**
     *
     */
    private Element tree = null;

    /**
     *
     */
    private Element rootTreeNode = null;

    /**
     *
     */
    //private Element currentElement = null;

    /**
     *
     */
    private DocumentModel documentModel = null;

    /**
     * Activation status
     */
    private boolean activationStatus = false;

    //------------------------------------------------------------------------------------
    // CONSTRUCTORS
    //------------------------------------------------------------------------------------

    /**
     *
     */
    public DTreeManager()
    {
        reset();
    }

    //------------------------------------------------------------------------------------
    // METHODS - initializing
    //------------------------------------------------------------------------------------

    /**
     * @param document Document rappresentato da questo DTree
     * @param documentModel DocumentModel da usare per costruire le labels del tree.
     */
    public void init(Document document, DocumentModel documentModel)
    {
        this.documentModel = documentModel;

        reset();

        Element root = document.getDocumentElement();

        addAllElements(root);
    }

    /**
     *
     *
     */
    public void reset()
    {
        idToElement = new HashMap();
        elementToId = new HashMap();
        elementToInterface = new HashMap();
        counter = 0;
    }

    /**
     *
     * @param value
     */
    public void setActivationStatus(boolean value)
    {
        activationStatus = value;
        if(activationStatus) {
            tree.setAttribute("active", "yes");
        }
        else {
            tree.setAttribute("active", "no");
        }
    }

    /**
     *
     * @return
     */
    public boolean getActivationStatus()
    {
        return activationStatus;
    }

    //------------------------------------------------------------------------------------
    // METHODS - adding/removing elements
    //------------------------------------------------------------------------------------

    /**
     * @param nodes
     */
    public void addAllElements(Node[] nodes)
    {
        for(int i = 0; i < nodes.length; ++i) {
            addAllElements(nodes[i]);
        }
    }

    /**
     *
     * @param root
     */
    public void addAllElements(Node root)
    {
        if(root instanceof Element) {
            addElement((Element)root);

            Node child = root.getFirstChild();
            while(child != null) {
                if(child instanceof Element) {
                    addAllElements(child);
                }
                child = child.getNextSibling();
            }
        }
        else if(root instanceof NodeList) {
            NodeList list = (NodeList)root;
            for(int i = 0; i < list.getLength(); ++i) {
                addAllElements(list.item(i));
            }
        }
    }

    /**
     *
     * @param element elemento aggiunto al documento per cui DTree deve costruire
     *      un nodo nel tree.
     */
    public void addElement(Element element)
    {
        if(!elementToId.containsKey(element)) {

            if(aDocument == null) {
                initializeDocument(element.getOwnerDocument());
            }

            Integer id = new Integer(++counter);
            elementToId.put(element, id);
            idToElement.put(id, element);

            Node parentNode = element.getParentNode();
            Element parentInterface = (Element)elementToInterface.get(parentNode);

            Element treeNode = aDocument.createElement("tree-node");
            if(parentInterface != null) {
                parentInterface.appendChild(treeNode);
            }
            else {
                rootTreeNode = treeNode;
                tree.appendChild(treeNode);
            }
            treeNode.setAttribute("id", id.toString());
            treeNode.setAttribute("pid", "" + getId(parentNode));

            elementToInterface.put(element, treeNode);
            updateName(element);
            arrangeInterface(element);
        }
    }

    /**
     *
     * @param element
     */
    public void updateName(Element element)
    {
        String elementName = element.getNodeName();
        String name = elementName;
        if(documentModel != null) {
            ElementModel model = documentModel.getElementModel(elementName);
            if(model != null) {
                String label = model.getLabel(element);
                if((label != null) && !label.equals("")) {
                    name = elementName + " <b>" + label + "</b>";
                }
            }
        }
        Element treeNode = (Element)elementToInterface.get(element);
        treeNode.setAttribute("name", "<nobr>" + name + "</nobr>");
    }

    /**
     *
     * @param nodes
     */
    public void removeAllElements(Node[] nodes)
    {
        for(int i = 0; i < nodes.length; ++i) {
            removeAllElements(nodes[i]);
        }
    }

    /**
     *
     * @param root
     */
    public void removeAllElements(Node root)
    {
        if(root instanceof Element) {
            removeElement((Element)root);

            Node child = root.getFirstChild();
            while(child != null) {
                if(child instanceof Element) {
                    removeAllElements(child);
                }
                child = child.getNextSibling();
            }
        }
        else if(root instanceof NodeList) {
            NodeList list = (NodeList)root;
            for(int i = 0; i < list.getLength(); ++i) {
                removeAllElements(list.item(i));
            }
        }
    }

    /**
     *
     * @param element
     */
    public void removeElement(Element element)
    {
        Integer id = (Integer)elementToId.remove(element);
        if(id != null) {
            idToElement.remove(id);
            Element treeNode = (Element)elementToInterface.remove(element);
            Node parentNode = treeNode.getParentNode();
            parentNode.removeChild(treeNode);
            if(parentNode == rootTreeNode) {
                rootTreeNode = null;
            }
        }
    }

    //------------------------------------------------------------------------------------
    // METHODS - getting elements/ids
    //------------------------------------------------------------------------------------

    /**
     * @param element
     */
    public int getId(Object element)
    {
        Integer id = (Integer)elementToId.get(element);
        if(id != null) {
            return id.intValue();
        }
        else {
            return -1;
        }
    }

    /**
     *
     * @param id
     * @return the searched Element
     */
    public Element getElement(int id)
    {
        return (Element)idToElement.get(new Integer(id));
    }

    /**
     *
     * @param element
     */
    public void setCurrentElement(Element element)
    {
        //currentElement = element;
        Integer id = (Integer)elementToId.get(element);
        if(id == null) {
            tree.removeAttribute("openTo");
        }
        else {
            tree.setAttribute("openTo", "" + id);
        }
    }

    //------------------------------------------------------------------------------------
    // METHODS - getting interface
    //------------------------------------------------------------------------------------

    /**
     * @return the Element containing the interface for the DTree.
     */
    public Element getTreeFor(Document intfc)
    {
        if(activationStatus) {
            if((rootTreeNode != null) && (rootTreeNode.getParentNode() != tree)) {
                tree.appendChild(rootTreeNode);
            }
        }
        else {
            if((rootTreeNode != null) && (rootTreeNode.getParentNode() == tree)) {
                tree.removeChild(rootTreeNode);
            }
        }

        if(tree.getOwnerDocument() == intfc) {
            return tree;
        }
        Element ret = (Element)intfc.importNode(tree, true);
        return ret;
    }

    //------------------------------------------------------------------------------------
    // IMPLEMENTATION METHODS
    //------------------------------------------------------------------------------------

    /**
     * @param document inizializza le strutture interne.
     */
    private void initializeDocument(Document document)
    {
        aDocument = document;
        tree = aDocument.createElement("tree");
        tree.setAttribute("name", "tree");
        setActivationStatus(activationStatus);
    }

    /**
     *
     * @param element
     */
    private void arrangeInterface(Element element)
    {
        Node parentNode = element.getParentNode();
        if(!(parentNode instanceof Element)) {
            return;
        }

        NodeList children = parentNode.getChildNodes();
        for(int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if(child instanceof Element) {
                Element interfaceNode = (Element)elementToInterface.get(child);
                if(interfaceNode != null) {
                    Node parentInterfaceNode = interfaceNode.getParentNode();
                    parentInterfaceNode.removeChild(interfaceNode);
                    parentInterfaceNode.appendChild(interfaceNode);
                }
            }
        }
    }
}

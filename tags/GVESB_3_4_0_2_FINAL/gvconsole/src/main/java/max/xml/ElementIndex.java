/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:49 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/ElementIndex.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Id: ElementIndex.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utilizzata per reperire velocemente gli elementi di con un dato nome,
 * senza eseguire ogni volta la visita di tutto il documento.
 *
 * @author Maxime Informatica s.n.c. - Copyright (c) 2004 - All right reserved
 */
public class ElementIndex
{
    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    /**
     * Map[String elementName, Set[Element]]
     */
    private Map index = new HashMap();

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    //----------------------------------------------------------------------------------------------

    public void init(Document document)
    {
        clear();
        addElements(document, true);
    }

    public void clear()
    {
        index.clear();
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    /**
     */
    public void addElements(Node node, boolean recursive)
    {
        if(node instanceof Element) {
            Element element = (Element)node;
            addElement(element);
            if(recursive) {
                Node child = element.getFirstChild();
                while(child != null) {
                    addElements(child, recursive);
                    child = child.getNextSibling();
                }
            }
        }
        else if(node instanceof DocumentFragment) {
            Node child = node.getFirstChild();
            while(child != null) {
                addElements(child, recursive);
                child = child.getNextSibling();
            }
        }
        else if(node instanceof Document) {
            Document document = (Document)node;
            addElements(document.getDocumentElement(), recursive);
        }
    }

    public void addElements(Node nodeList[], boolean recursive)
    {
        if(nodeList == null) {
            return;
        }
        for(int i = 0; i < nodeList.length; ++i) {
            addElements(nodeList[i], recursive);
        }
    }

    /**
     */
    public void removeElements(Node node, boolean recursive)
    {
        if(node instanceof Element) {
            Element element = (Element)node;
            removeElement(element);
        }
        else if(node instanceof DocumentFragment) {
            Node child = node.getFirstChild();
            while(child != null) {
                removeElements(child, recursive);
                child = child.getNextSibling();
            }
        }
        else if(node instanceof Document) {
            Document document = (Document)node;
            removeElements(document.getDocumentElement(), recursive);
        }
    }

    public void removeElements(Node nodeList[], boolean recursive)
    {
        if(nodeList == null) {
            return;
        }
        for(int i = 0; i < nodeList.length; ++i) {
            removeElements(nodeList[i], recursive);
        }
    }

    /**
     *
     * @param elementName
     * @return insieme dei nodi con il nome dato. Se il nome non � mai stato utilizzato
     *      ritorna <code>null</code>
     */
    public Set getElements(String elementName)
    {
        Set elements = (Set)index.get(elementName);
        if(elements == null) {
            return null;
        }
        return Collections.unmodifiableSet(elements);
    }

    public String toString()
    {
        return index.toString();
    }

    //----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    //----------------------------------------------------------------------------------------------

    /**
     * Non � ricorsivo
     *
     * @param element
     */
    private void addElement(Element element)
    {
        String elementName = element.getNodeName();

        Set elements = (Set)index.get(elementName);
        if(elements == null) {
            elements = new HashSet();
            index.put(elementName, elements);
        }

        elements.add(element);
    }

    /**
     * Non � ricorsivo
     *
     * @param element
     */
    private void removeElement(Element element)
    {
        String elementName = element.getNodeName();

        Set elements = (Set)index.get(elementName);
        if(elements == null) {
            return;
        }

        elements.remove(element);
    }
}

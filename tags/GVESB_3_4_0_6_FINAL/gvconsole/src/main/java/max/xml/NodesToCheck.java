/*
 * Created on 31-ott-2005
 *
 * $Date: 2010-04-03 15:28:50 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/NodesToCheck.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Id: NodesToCheck.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2005 - All right reserved
 */
public class NodesToCheck
{
    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    /**
     * Nodi i cui sottoalberi devono essere controllati.
     *
     * LinkedList[Element]
     */
    private LinkedList subtreesToCheck    = new LinkedList();

    /**
     * Per velocizzare le ricerche.
     *
     * Set[Element]
     */
    private Set        subtreesToCheckSet = new HashSet();

    /**
     * Nodi gi� controllati e marcati esplicitamente.
     *
     * Set[Element]
     */
    private Set        checkedElements    = new HashSet();

    /**
     * Singoli nodi da controllare.
     *
     * Set[Element]
     */
    private Set        elementsToCheck    = new HashSet();

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    public void addElement(Element element)
    {
        elementsToCheck.add(element);
        checkedElements.remove(element);
    }

    public void addAllElements(Collection elements)
    {
        for (Iterator it = elements.iterator(); it.hasNext();) {
            addElement((Element) it.next());
        }
    }

    /**
     * Add an Element (ant thus all sub-tree) to the set of elements that requires checks.
     */
    public void addTree(Element root)
    {
        if (!subtreesToCheckSet.contains(root)) {
            subtreesToCheck.addFirst(root);
            subtreesToCheckSet.add(root);
        }
    }

    public void markAsChecked(Element element)
    {
        checkedElements.add(element);
        elementsToCheck.remove(element);
    }

    /**
     *
     * @return <code>null</code> se non ci sono piu' nodi da controllare.
     */
    public Element next()
    {
        Element element;

        if (elementsToCheck.size() > 0) {
            Iterator it = elementsToCheck.iterator();
            element = (Element) it.next();
            markAsChecked(element);
            return element;
        }

        do {
            if (subtreesToCheck.size() == 0) {
                clear();
                return null;
            }

            element = (Element) subtreesToCheck.removeFirst();
            subtreesToCheckSet.remove(element);

            Node child = element.getLastChild();
            while (child != null) {
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    addTree((Element) child);
                }
                child = child.getPreviousSibling();
            }
        }
        while (checkedElements.contains(element));

        return element;
    }

    public boolean mustBeChecked(Element element)
    {
        if (checkedElements.contains(element)) {
            return false;
        }
        if (elementsToCheck.contains(element)) {
            return true;
        }

        Node node = element;
        while (node != null) {
            if (subtreesToCheckSet.contains(node)) {
                return true;
            }
            node = node.getParentNode();
        }
        return false;
    }

    public boolean isEmpty()
    {
        return subtreesToCheck.isEmpty() && elementsToCheck.isEmpty();
    }

    //----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    //----------------------------------------------------------------------------------------------

    private void clear()
    {
        subtreesToCheck.clear();
        subtreesToCheckSet.clear();
        elementsToCheck.clear();
        checkedElements.clear();
    }
}

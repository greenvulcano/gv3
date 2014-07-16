/*
 * Copyright (c) 2005 Maxime Informatica s.n.c. - All right reserved
 *
 * Created on 8-apr-2005
 *
 * $Date: 2010-04-03 15:28:48 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xpath/jaxen/JaxenNodeIterator.java,v 1.1 2010-04-03 15:28:48 nlariviera Exp $
 * $Id: JaxenNodeIterator.java,v 1.1 2010-04-03 15:28:48 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xpath.jaxen;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2005 - All right reserved
 */
public class JaxenNodeIterator implements NodeIterator
{
    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    private LinkedList nodes;
    private Node root;
    private ListIterator iterator;

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    //----------------------------------------------------------------------------------------------

    public JaxenNodeIterator(Node root, List nodes)
    {
        this.root = root;
        this.nodes = new LinkedList(nodes);
        iterator = nodes.listIterator();
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.w3c.dom.traversal.NodeIterator#getWhatToShow()
     */
    public int getWhatToShow()
    {
        return NodeFilter.SHOW_ALL;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.traversal.NodeIterator#detach()
     */
    public void detach()
    {
        root = null;
        nodes = null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.traversal.NodeIterator#getExpandEntityReferences()
     */
    public boolean getExpandEntityReferences()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.traversal.NodeIterator#getRoot()
     */
    public Node getRoot()
    {
        return root;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.traversal.NodeIterator#nextNode()
     */
    public Node nextNode() throws DOMException
    {
        if(root == null && nodes == null) {
            throw new DOMException(DOMException.INVALID_STATE_ERR, "Invalid state error");
        }

        if(iterator.hasNext()) {
            return (Node)iterator.next();
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.traversal.NodeIterator#previousNode()
     */
    public Node previousNode() throws DOMException
    {
        if(root == null && nodes == null) {
            throw new DOMException(DOMException.INVALID_STATE_ERR, "Invalid state error");
        }

        if(iterator.hasPrevious()) {
            return (Node)iterator.previous();
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.traversal.NodeIterator#getFilter()
     */
    public NodeFilter getFilter()
    {
        return null;
    }
}

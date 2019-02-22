/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:48 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xpath/jaxen/JaxenNodeList.java,v 1.1 2010-04-03 15:28:48 nlariviera Exp $
 * $Id: JaxenNodeList.java,v 1.1 2010-04-03 15:28:48 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xpath.jaxen;

import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A simple implementation for org.w3c.dom.NodeList.
 *
 */
public class JaxenNodeList implements NodeList
{
    //--------------------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------------------

    /**
     * Nodes into the list.
     */
    private Node[] nodes = null;

    //--------------------------------------------------------------------------------------
    // CONSTRUCTOR
    //--------------------------------------------------------------------------------------

    /**
     * Creates a new NodeList starting from a List of Node.
     *
     * @param list List of Node
     */
    public JaxenNodeList(List list)
    {
        nodes = new Node[list.size()];
        list.toArray(nodes);
    }

    //--------------------------------------------------------------------------------------
    // METHODS
    //--------------------------------------------------------------------------------------

    /**
     * @return the number of Node into the list.
     */
    public int getLength()
    {
        return nodes.length;
    }

    /**
     * @param index index of the required Node
     * @return the Node of given index
     */
    public Node item(int index)
    {
        return nodes[index];
    }
}

/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:56 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xpath/XPathAPIImpl.java,v 1.1 2010-04-03 15:28:56 nlariviera Exp $
 * $Id: XPathAPIImpl.java,v 1.1 2010-04-03 15:28:56 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xpath;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Interface that the actual implementation must implement.
 *
 */
public interface XPathAPIImpl
{
    /**
     * Reset the XPath. Some implementations want to reset some data structures when the
     * underlying document changes.
     */
    void reset();

    /**
     * @param contextNode context node for the XPath evaluation
     * @param xpath implementation level XPath to evaluate
     * @return an iterator for the selected nodes
     * @throws TransformerException if an error occurs
     */
    NodeIterator selectNodeIterator(Node contextNode, Object xpath) throws TransformerException;

    /**
     * @param contextNode context node for the XPath evaluation
     * @param xpath implementation level XPath to evaluate
     * @param namespaceNode namespace node
     * @return an iterator for the selected nodes
     * @throws TransformerException if an error occurs
     */
    NodeIterator selectNodeIterator(Node contextNode, Object xpath, Node namespaceNode) throws TransformerException;

    /**
     * @param contextNode context node for the XPath evaluation
     * @param xpath implementation level XPath to evaluate
     * @return the selected nodes
     * @throws TransformerException if an error occurs
     */
    NodeList selectNodeList(Node contextNode, Object xpath) throws TransformerException;

    /**
     * @param contextNode context node for the XPath evaluation
     * @param xpath implementation level XPath to evaluate
     * @param namespaceNode namespace node
     * @return the selected nodes
     * @throws TransformerException if an error occurs
     */
    NodeList selectNodeList(Node contextNode, Object xpath, Node namespaceNode) throws TransformerException;

    /**
     * @param contextNode context node for the XPath evaluation
     * @param xpath implementation level XPath to evaluate
     * @return the selected Node
     * @throws TransformerException if an error occurs
     */
    Node selectSingleNode(Node contextNode, Object xpath) throws TransformerException;

    /**
     * @param contextNode context node for the XPath evaluation
     * @param xpath implementation level XPath to evaluate
     * @param namespaceNode namespace node
     * @return the selected Node
     * @throws TransformerException if an error occurs
     */
    Node selectSingleNode(Node contextNode, Object xpath, Node namespaceNode) throws TransformerException;
}

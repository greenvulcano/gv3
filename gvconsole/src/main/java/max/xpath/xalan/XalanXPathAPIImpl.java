/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:55 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xpath/xalan/XalanXPathAPIImpl.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Id: XalanXPathAPIImpl.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xpath.xalan;

import javax.xml.transform.TransformerException;

import max.xpath.XPathAPIImpl;

import org.apache.xpath.NodeSet;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Executes Xalan XPaths.
 *
 */
class XalanXPathAPIImpl implements XPathAPIImpl
{
    //--------------------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------------------

    /**
     * Low level support for the XPath.
     */
    private XPathContext xpathContext;

    //--------------------------------------------------------------------------------------
    // CONSTRUCTOR
    //--------------------------------------------------------------------------------------

    /**
     * Build a new XalanXPathAPIImpl
     */
    public XalanXPathAPIImpl()
    {
    }

    //--------------------------------------------------------------------------------------
    // METHODS - XPathAPIImpl interface
    //--------------------------------------------------------------------------------------

    /**
     *
     */
    public void reset()
    {
        xpathContext = new XPathContext(ExtensionsManager.instance());
    }

    /**
     * @param contextNode context node for the XPath evaluation
     * @param xpath XPath to evaluate
     * @return an iterator for the selected nodes
     * @throws TransformerException if an error occurs
     */
    public NodeIterator selectNodeIterator(Node contextNode, Object xpath) throws TransformerException
    {
        try {
            ExtensionsManager.instance().startEvaluation(contextNode);
            XObject xobj = eval(contextNode, xpath, contextNode);
            if (xobj.getType() == XObject.CLASS_NODESET) {
                return xobj.nodeset();
            }

            return new NodeSet(createTextNode(contextNode, xobj));
        }
        finally {
            ExtensionsManager.instance().endEvaluation();
        }
    }

    /**
     * @param contextNode context node for the XPath evaluation
     * @param xpath XPath to evaluate
     * @param namespaceNode namespace node
     * @return an iterator for the selected nodes
     * @throws TransformerException if an error occurs
     */
    public NodeIterator selectNodeIterator(Node contextNode, Object xpath, Node namespaceNode)
            throws TransformerException
    {
        try {
            ExtensionsManager.instance().startEvaluation(contextNode);
            XObject xobj = eval(contextNode, xpath, namespaceNode);
            if (xobj.getType() == XObject.CLASS_NODESET) {
                return xobj.nodeset();
            }

            return new NodeSet(createTextNode(contextNode, xobj));
        }
        finally {
            ExtensionsManager.instance().endEvaluation();
        }
    }

    /**
     * @param contextNode context node for the XPath evaluation
     * @param xpath XPath to evaluate
     * @return the selected nodes
     * @throws TransformerException if an error occurs
     */
    public NodeList selectNodeList(Node contextNode, Object xpath) throws TransformerException
    {
        try {
            ExtensionsManager.instance().startEvaluation(contextNode);
            XObject xobj = eval(contextNode, xpath, contextNode);
            if (xobj.getType() == XObject.CLASS_NODESET) {
                return xobj.nodelist();
            }
            NodeSet nodeSet = new NodeSet(createTextNode(contextNode, xobj));
            nodeSet.setShouldCacheNodes(true);
            return nodeSet;
        }
        finally {
            ExtensionsManager.instance().endEvaluation();
        }
    }

    /**
     * @param contextNode context node for the XPath evaluation
     * @param xpath XPath to evaluate
     * @param namespaceNode namespace node
     * @return the selected nodes
     * @throws TransformerException if an error occurs
     */
    public NodeList selectNodeList(Node contextNode, Object xpath, Node namespaceNode) throws TransformerException
    {
        try {
            ExtensionsManager.instance().startEvaluation(contextNode);
            XObject xobj = eval(contextNode, xpath, namespaceNode);
            if (xobj.getType() == XObject.CLASS_NODESET) {
                return xobj.nodelist();
            }
            NodeSet nodeSet = new NodeSet(createTextNode(contextNode, xobj));
            nodeSet.setShouldCacheNodes(true);
            return nodeSet;
        }
        finally {
            ExtensionsManager.instance().endEvaluation();
        }
    }

    /**
     * @param contextNode context node for the XPath evaluation
     * @param xpath XPath to evaluate
     * @return the selected Node
     * @throws TransformerException if an error occurs
     */
    public final Node selectSingleNode(Node contextNode, Object xpath) throws TransformerException
    {
        try {
            ExtensionsManager.instance().startEvaluation(contextNode);
            NodeIterator iterator = selectNodeIterator(contextNode, xpath);
            if (iterator != null) {
                return iterator.nextNode();
            }
            return null;
        }
        finally {
            ExtensionsManager.instance().endEvaluation();
        }
    }

    /**
     * @param contextNode context node for the XPath evaluation
     * @param xpath XPath to evaluate
     * @param namespaceNode namespace node
     * @return the selected Node
     * @throws TransformerException if an error occurs
     */
    public final Node selectSingleNode(Node contextNode, Object xpath, Node namespaceNode) throws TransformerException
    {
        try {
            ExtensionsManager.instance().startEvaluation(contextNode);
            NodeIterator iterator = selectNodeIterator(contextNode, xpath, namespaceNode);
            if (iterator != null) {
                return iterator.nextNode();
            }
            return null;
        }
        finally {
            ExtensionsManager.instance().endEvaluation();
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    /**
     * Evaluate a XPath.
     * <br/>
     *
     * @param contextNode context node for the XPath evaluation
     * @param xpath XPath to evaluate
     * @param namespaceNode namespace node
     * @return the low level result of the XPath
     * @throws TransformerException if an error occurs
     */
    private XObject eval(Node contextNode, Object xpathObject, Node namespaceNode) throws TransformerException
    {
        XPath xpath = (XPath) xpathObject;
        int ctxtNode = xpathContext.getDTMHandleFromNode(contextNode);
        return xpath.execute(xpathContext, ctxtNode, PrefixResolver.instance());
    }

    /**
     * Build a text node from an XObject.
     * The contained text is the XObject.toString() return value.
     *
     * @param contextNode a node of the Document
     * @param xobj XObject to convert in a String
     * @return the new created Node
     */
    private static Node createTextNode(Node contextNode, XObject xobj)
    {
        Document dom = contextNode.getOwnerDocument();
        return dom.createTextNode(xobj.toString());
    }
}

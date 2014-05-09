/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:48 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xpath/jaxen/JaxenXPathAPIImpl.java,v 1.1 2010-04-03 15:28:48 nlariviera Exp $
 * $Id: JaxenXPathAPIImpl.java,v 1.1 2010-04-03 15:28:48 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xpath.jaxen;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import max.xpath.XPathAPIImpl;

import org.jaxen.JaxenException;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Executes Jaxen XPaths.
 *
 */
public class JaxenXPathAPIImpl implements XPathAPIImpl
{
    private final boolean 	  STDOUT_LOGGING_ENABLED = false;

    // --------------------------------------------------------------------------------------
    // XPathAPIImpl interface
    // --------------------------------------------------------------------------------------

    public void reset()
    {
    }

    public NodeIterator selectNodeIterator(Node contextNode, Object xpath) throws TransformerException
    {
        return selectNodeIterator(contextNode, xpath, null);
    }

    public NodeIterator selectNodeIterator(Node contextNode, Object xpath, Node namespaceNode)
            throws TransformerException
    {
        DOMXPath lowlevelXPath = (DOMXPath) xpath;
        try {
            CurrentFunction.putCurrent(contextNode);
            List list = lowlevelXPath.selectNodes(contextNode);
            return new JaxenNodeIterator(contextNode, list);
        }
        catch (JaxenException e) {
            throw new TransformerException(e);
        }
        finally {
            CurrentFunction.removeCurrent();
        }
    }

    public NodeList selectNodeList(Node contextNode, Object xpath) throws TransformerException
    {
        return selectNodeList(contextNode, xpath, null);
    }

    public NodeList selectNodeList(Node contextNode, Object xpath, Node namespaceNode) throws TransformerException
    {
        DOMXPath lowlevelXPath = (DOMXPath) xpath;
        try {
            CurrentFunction.putCurrent(contextNode);
            List list = lowlevelXPath.selectNodes(contextNode);
            return new JaxenNodeList(list);
        }
        catch (JaxenException e) {
            throw new TransformerException(e);
        }
        catch (RuntimeException exc) {
            exc.printStackTrace();
            throw new RuntimeException("nodeName=" + contextNode.getNodeName() + ", xpath=" + lowlevelXPath.toString(),
                    exc);
        }
        finally {
            CurrentFunction.removeCurrent();
        }
    }

    public Node selectSingleNode(Node contextNode, Object xpath) throws TransformerException
    {
        return selectSingleNode(contextNode, xpath, null);
    }

    public Node selectSingleNode(Node contextNode, Object xpath, Node namespaceNode) throws TransformerException
    {
        DOMXPath lowlevelXPath = (DOMXPath) xpath;
        try {
            CurrentFunction.putCurrent(contextNode);
            List ret = lowlevelXPath.selectNodes(contextNode);
            if ((ret == null) || ret.size() == 0) {
                return null;
            }
            return extractNode(contextNode, ret);
        }
        catch (JaxenException e) {
            throw new TransformerException(e);
        }
        finally {
            CurrentFunction.removeCurrent();
        }
    }

    // ----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    // ----------------------------------------------------------------------------------------------

    private Node extractNode(Node contextNode, Object obj)
    {
        if (obj == null) {
            return null;
        }
        else if (obj instanceof Node) {
            return (Node) obj;
        }
        else if (obj instanceof NodeList) {
            NodeList nodeList = (NodeList) obj;
            if (nodeList.getLength() == 0) {
                return null;
            }
            return nodeList.item(0);
        }
        else if (obj instanceof Collection) {
            Iterator it = ((Collection) obj).iterator();
            if (it.hasNext()) {
                return extractNode(contextNode, it.next());
            }
            return null;
        }
        else {
        	log("WARN: Unespected object type: " + obj.getClass());
            Class[] interfaces = obj.getClass().getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                Class cls = interfaces[i];
                log("WARN:   interface: " + cls);
            }
            log("WARN:   Creating TextNode");
            return contextNode.getOwnerDocument().createTextNode(obj.toString());
        }
    }

    private void log(String message)
    {
        if(STDOUT_LOGGING_ENABLED) {
        	System.out.println(message);
        }
    }


}
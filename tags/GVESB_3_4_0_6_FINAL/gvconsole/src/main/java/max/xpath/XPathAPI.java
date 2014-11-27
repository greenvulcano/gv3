/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-14 15:31:42 $ $Header:
 * /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS
 * /core/webapps/gvconsole/src/main/java/max/xpath/XPathAPI.java,v 1.1
 * 2010-04-03 15:28:56 nlariviera Exp $ $Id: XPathAPI.java,v 1.1 2010-04-03
 * 15:28:56 nlariviera Exp $ $Name:  $ $Locker:  $ $Revision: 1.2 $ $State: Exp $
 */
package max.xpath;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

/**
 * This class executes an XPath over a Node. <br/>
 * Before execute the search, the XPath is first preprocessed in order to
 * substitute some <i>parameters</i> from the XPath.
 * <p/>
 * A parameter is a XPath itself contained into the <code><b>$[...]$</b></code>
 * syntax. <br/>
 * The XPath contained into a parameter is evaluated respect to the context
 * node, the result is substituted to the <code><b>$[...]$</b></code> syntax. <br/>
 * When all parameters are evaluated, the resulting XPath is evaluated and the
 * result is returned.
 * <p/>
 * For example consider the following XML node:
 *
 * <pre>
 *  ...
 *  <b>&lt;service server="MSP" name="RICARICA"&gt;
 *  &lt;/service&gt;</b>
 *  &lt;server server-name="MSP" description="Motore Servizi Prepagato"&gt;
 *  &lt;/server&gt;
 *  &lt;server server-name="OPSC" description="Mobile billing"&gt;
 *  &lt;/server&gt;
 *  ...
 * </pre>
 *
 * Suppose that the context node is in bold text. <br/>
 * The extended XPath <b><nobr>
 * <code>//server[@server-name='$[@server]$']/@description</code></nobr></b>
 * will produce the XPath <b><nobr>
 * <code>//server[@server-name='MSP']/@description</code></nobr></b>. Evaluating
 * the XPath we obtain <b><nobr><code>Motore Servizi Prepagato</code>
 * </nobr></b>.
 * <p/>
 * NOTE: parameters cannot be nested.
 * <p/>
 * NOTE: consider to use the <code>current()</code> XPath function instead of
 * <code>$[...]$</code> syntax. Consider deprecated the <code>$[...]$</code>
 * syntax and supported only for older code.
 * <p/>
 * NOTE: the <code>XPathAPI</code> return a node also if the result of the XPath
 * is a string or number. Such node is a TextNode.
 * <p/>
 * <b>ATTENTION:</b> because the XPathAPI contains a lower level object that
 * needs to be reset when the document changes, please call the
 * <code>reset()</code> method each time the document changes.
 *
 */
public class XPathAPI
{
    // --------------------------------------------------------------------------------------
    // STATIC METHODS
    // --------------------------------------------------------------------------------------

    /**
     * Registra una funzione per estendere l'XPath.
     *
     * @param namespace
     * @param name
     * @param function
     */
    public static void installFunction(String namespace, String name, XPathFunction function)
    {
        XPathAPIFactory.instance().installFunction(namespace, name, function);
    }


    /**
     * Registra un namespace utilizzabile negli XPath.
     *
     * @param prefix
     * @param namespace
     */
    public static void installNamespace(String prefix, String namespace)
    {
        XPathAPIFactory.instance().installNamespace(prefix, namespace);
    }

    // --------------------------------------------------------------------------------------
    // FIELDS
    // --------------------------------------------------------------------------------------

    private XPathAPIImpl implementation;

    // --------------------------------------------------------------------------------------
    // CONSTRUCTOR
    // --------------------------------------------------------------------------------------

    /**
     *
     */
    public XPathAPI()
    {
        implementation = XPathAPIFactory.instance().newXPathAPIImpl();
        reset();
    }

    // --------------------------------------------------------------------------------------
    // STATIC METHODS
    // --------------------------------------------------------------------------------------

    /**
     * Return the value for a node.
     *
     * @param node
     *        input Node.
     *
     * @return the node value. The value for an Element is the concatenation of
     *         children values. For other nodes the value is
     *         <code>node.getNodeValue()</code>.
     */
    public static synchronized String getNodeValue(Node node)
    {
        if (node instanceof Element) {
            StringBuilder buf = new StringBuilder();
            Node child = node.getFirstChild();
            while (child != null) {
                String val = getNodeValue(child);
                if (val != null) {
                    buf.append(val);
                }
                child = child.getNextSibling();
            }
            return buf.toString();
        }
        else {
            return node.getNodeValue();
        }
    }

    // --------------------------------------------------------------------------------------
    // METHODS
    // --------------------------------------------------------------------------------------

    /**
     * Reset the XPath. Some implementations want to reset some data structures
     * when the underlying document changes.
     */
    public final void reset()
    {
        implementation.reset();
    }

    /**
     * @param contextNode
     *        context node for the XPath evaluation
     * @param xpath
     *        XPath to evaluate
     * @return an iterator for the selected nodes
     * @throws TransformerException
     *         if an error occurs
     */
    public final NodeIterator selectNodeIterator(Node contextNode, XPath xpath) throws TransformerException
    {
        return implementation.selectNodeIterator(contextNode, getXPath(contextNode, xpath));
    }

    /**
     * @param contextNode
     *        context node for the XPath evaluation
     * @param xpath
     *        XPath to evaluate
     * @param namespaceNode
     *        namespace node
     * @return an iterator for the selected nodes
     * @throws TransformerException
     *         if an error occurs
     */
    public final NodeIterator selectNodeIterator(Node contextNode, XPath xpath, Node namespaceNode)
            throws TransformerException
    {
        return implementation.selectNodeIterator(contextNode, getXPath(contextNode, xpath), namespaceNode);
    }

    /**
     * @param contextNode
     *        context node for the XPath evaluation
     * @param xpath
     *        XPath to evaluate
     * @return the selected nodes
     * @throws TransformerException
     *         if an error occurs
     */
    public final NodeList selectNodeList(Node contextNode, XPath xpath) throws TransformerException
    {
        return implementation.selectNodeList(contextNode, getXPath(contextNode, xpath));
    }

    /**
     * @param contextNode
     *        context node for the XPath evaluation
     * @param xpath
     *        XPath to evaluate
     * @param namespaceNode
     *        namespace node
     * @return the selected nodes
     * @throws TransformerException
     *         if an error occurs
     */
    public final NodeList selectNodeList(Node contextNode, XPath xpath, Node namespaceNode) throws TransformerException
    {
        return implementation.selectNodeList(contextNode, getXPath(contextNode, xpath), namespaceNode);
    }

    /**
     * @param contextNode
     *        context node for the XPath evaluation
     * @param xpath
     *        XPath to evaluate
     * @return the selected Node
     * @throws TransformerException
     *         if an error occurs
     */
    public final Node selectSingleNode(Node contextNode, XPath xpath) throws TransformerException
    {
        return implementation.selectSingleNode(contextNode, getXPath(contextNode, xpath));
    }

    /**
     * @param contextNode
     *        context node for the XPath evaluation
     * @param xpath
     *        XPath to evaluate
     * @param namespaceNode
     *        namespace node
     * @return the selected Node
     * @throws TransformerException
     *         if an error occurs
     */
    public final Node selectSingleNode(Node contextNode, XPath xpath, Node namespaceNode) throws TransformerException
    {
        return implementation.selectSingleNode(contextNode, getXPath(contextNode, xpath), namespaceNode);
    }

    // --------------------------------------------------------------------------------------
    // METHODS
    // --------------------------------------------------------------------------------------

    /**
     * Translate a XPath substituting the <code>$[xpath]$</code> syntax with the
     * result of the valuation of the <code>xpath</code>.
     *
     * @param contextNode
     *        context node for the XPath
     * @param xpath
     *        XPath to translate
     * @return the translated XPath
     * @throws TransformerException
     *         if an error occurs
     * @deprecated the <code>$[...]$</code> is deprecated. Use the
     *             <code>current()</code> XPath function instead.
     */
    public final String translateXPath(Node contextNode, String xpath) throws TransformerException
    {
        StringBuffer ret = new StringBuffer();

        // Repeat to loop until there are parameters
        //
        int prevIdx = 0;
        while (true) {
            int startIdx = xpath.indexOf("$[", prevIdx);

            // No more parameters
            //
            if (startIdx == -1) {
                ret.append(xpath.substring(prevIdx));
                return ret.toString();
            }

            int endIdx = xpath.indexOf("]$", startIdx);

            // No more parameters (a parameter must be into a $[...]$ syntax)
            //
            if (endIdx == -1) {
                ret.append(xpath.substring(prevIdx));
                return ret.toString();
            }

            // Estracts the XPath from the $[...]$ syntax
            //
            String paramXpath = xpath.substring(startIdx + 2, endIdx);

            // Evaluate the XPath and obtains the node value
            //
            Node paramNode = selectSingleNode(contextNode, new XPath(paramXpath));
            String paramValue = getNodeValue(paramNode);

            // Append the xpath before the $[...]$ syntax and the parameter
            // value
            //
            ret.append(xpath.substring(prevIdx, startIdx));
            ret.append(paramValue);

            // Set the initial search point for the $[...]$ syntax
            //
            prevIdx = endIdx + 2;
        }
    }

    /**
     * Ritorna l'XPath di basso livello.
     *
     * @param contextNode
     *        nodo di contesto per eseguire l'eventuale translate()
     * @param xpath
     *        XPath
     * @return l'XPath di basso livello.
     * @throws TransformerException
     *         se l'XPath � errato
     */
    public final Object getXPath(Node contextNode, XPath xpath) throws TransformerException
    {
        if (xpath.isExtended()) {
            String xpathString = xpath.getXPathString();
            xpathString = translateXPath(contextNode, xpathString);
            return XPathAPIFactory.instance().newXPath(xpathString);
        }
        else {
            return xpath.getXPath();
        }
    }
}

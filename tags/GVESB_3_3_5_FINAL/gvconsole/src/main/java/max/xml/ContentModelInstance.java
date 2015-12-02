/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:50 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/ContentModelInstance.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Id: ContentModelInstance.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ContentModelInstance
{
    ContentModelInstance parent     = null;
    private ContentModel contentModel;
    private Vector       nodes      = new Vector();
    private int          toSubtract = 0;
    private Map          operations = new LinkedHashMap();

    private static class Flag
    {
        public boolean state;

        public Flag(boolean st)
        {
            state = st;
        }
    }

    public ContentModelInstance(ContentModel contentModel)
    {
        this.contentModel = contentModel;
    }

    public ContentModel getContentModel()
    {
        return contentModel;
    }

    public void addNode(Node node)
    {
        if (node == null)
            throw new NullPointerException();
        nodes.addElement(node);
    }

    public void addNode(Node node, boolean forced)
    {
        if (node == null)
            throw new NullPointerException();
        if (forced)
            ++toSubtract;
        nodes.addElement(node);
    }

    public void addNode(ContentModelInstance cmi)
    {
        if (cmi == null)
            throw new NullPointerException();
        cmi.parent = this;
        nodes.addElement(cmi);
    }

    public void addOperation(XMLBuilder.Operation op)
    {
        operations.put(op.getKey(), op);
    }

    public Element getElement()
    {
        Iterator i = nodes.iterator();
        while (i.hasNext()) {
            Object obj = i.next();
            if (obj instanceof Element) {
                return (Element) obj;
            }
            if (obj instanceof ContentModelInstance) {
                ContentModelInstance cmi = (ContentModelInstance) obj;
                Element element = cmi.getElement();
                if (element != null) {
                    return element;
                }
            }
        }
        return null;
    }

    public boolean isSimple()
    {
        return contentModel.type == ContentModel.T_SIMPLE_EMPTY || contentModel.type == ContentModel.T_SIMPLE_IDE
                || contentModel.type == ContentModel.T_SIMPLE_PCDATA;
    }

    public void concatNode(ContentModelInstance cmi)
    {
        if (cmi == null)
            throw new NullPointerException();
        toSubtract += cmi.toSubtract;
        for (Enumeration e = cmi.nodes.elements(); e.hasMoreElements();) {
            Object n = e.nextElement();
            if (n instanceof Node) {
                nodes.addElement((Node) n);
            }
            else {
                ContentModelInstance c = (ContentModelInstance) n;
                nodes.addElement(c);
                c.parent = this;
            }
        }
    }

    /**
     * Riempie il <code>Vector</code> in argomento con tutte le operazioni
     * del <code>ContentModelInstance</code> e dei suoi discendenti.
     */
    public void getAllOperations(Vector v, boolean readOnly)
    {
        getOperations(v, XMLBuilder.Operation.T_SELECT_P);
        if (!readOnly) {
            getOperations(v, XMLBuilder.Operation.T_INSERT_B);
            getOperations(v, XMLBuilder.Operation.T_PASTE_B);
            getOperations(v, XMLBuilder.Operation.T_DELETE);
            getOperations(v, XMLBuilder.Operation.T_CHANGE);
            getOperations(v, XMLBuilder.Operation.T_PASTE);
            getOperations(v, XMLBuilder.Operation.T_COPY);
            getOperations(v, XMLBuilder.Operation.T_CUT);
        }
        getOperations(v, XMLBuilder.Operation.T_EDIT);
        getOperations(v, XMLBuilder.Operation.T_EDIT_ANY);
        getOperations(v, XMLBuilder.Operation.T_SELECT);
        for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof ContentModelInstance) {
                ((ContentModelInstance) o).getAllOperations(v, readOnly);
            }
        }
        if (!readOnly) {
            getOperations(v, XMLBuilder.Operation.T_INSERT_A);
            getOperations(v, XMLBuilder.Operation.T_PASTE_A);
        }
    }

    private void getOperations(Vector v, int type)
    {
        Iterator i = operations.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            XMLBuilder.Operation op = (XMLBuilder.Operation) (e.getValue());
            if (op.getType() == type)
                v.addElement(op);
        }
    }

    public XMLBuilder.Operation getOperation(String opKey)
    {
        Object o = operations.get(opKey);
        if (o != null)
            return (XMLBuilder.Operation) o;
        for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
            Object n = e.nextElement();
            if (n instanceof ContentModelInstance) {
                o = ((ContentModelInstance) n).getOperation(opKey);
                if (o != null)
                    return (XMLBuilder.Operation) o;
            }
        }
        return null;
    }

    public Vector getNodes()
    {
        return nodes;
    }

    public Node getFirstDOMNodeBefore(ContentModelInstance cmi)
    {
        if (parent != null)
            return parent.getFirstDOMNodeBefore(cmi);
        else
            return getFirstDOMNodeBefore(cmi, new Flag(false));
    }

    private Node getFirstDOMNodeBefore(ContentModelInstance cmi, Flag found)
    {
        if (!found.state) {
            found.state = this == cmi;
        }
        for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof Node) {
                if (found.state)
                    return (Node) o;
            }
            else {
                Node n = ((ContentModelInstance) o).getFirstDOMNodeBefore(cmi, found);
                if (n != null)
                    return n;
            }
        }
        return null;
    }

    public Node getFirstDOMNodeAfter(ContentModelInstance cmi)
    {
        if (parent != null)
            return parent.getFirstDOMNodeAfter(cmi);
        else
            return getFirstDOMNodeAfter(cmi, new Flag(false));
    }

    private Node getFirstDOMNodeAfter(ContentModelInstance cmi, Flag passed)
    {
        for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof Node) {
                if (passed.state)
                    return (Node) o;
            }
            else {
                Node n = ((ContentModelInstance) o).getFirstDOMNodeAfter(cmi, passed);
                if (n != null)
                    return n;
            }
        }
        if (!passed.state) {
            passed.state = this == cmi;
        }
        return null;
    }

    public Node[] getDOMNodes()
    {
        Vector v = new Vector();
        getDOMNodes(v);
        Node ret[] = new Node[v.size()];
        v.copyInto(ret);
        return ret;
    }

    public void getDOMNodes(Vector v)
    {
        for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof Node) {
                v.addElement(o);
            }
            else {
                ((ContentModelInstance) o).getDOMNodes(v);
            }
        }
    }

    public int countNodes()
    {
        int n = 0;
        for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof ContentModelInstance) {
                n += ((ContentModelInstance) o).countNodes();
            }
            else
                n++;
        }
        return n - toSubtract;
    }

    public String getComments()
    {
        StringBuffer sb = new StringBuffer();
        for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof Comment) {
                sb.append(((Comment) o).getNodeValue()).append("\n");
            }
        }
        return sb.toString().trim();
    }

    public String toString()
    {
        return toString("");
    }

    public String toString(String indent)
    {
        StringBuffer bf = new StringBuffer();
        String ret = System.getProperty("line.separator");
        String chIndent = indent + "   ";

        bf.append(indent).append("{").append(contentModel).append(":").append(ret);
        for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof ContentModelInstance) {
                bf.append(((ContentModelInstance) o).toString(chIndent));
            }
            else {
                bf.append(chIndent).append(((Node) o).getNodeName()).append(ret);
            };
        }
        if (operations.size() > 0) {
            bf.append(chIndent).append("-->");
            Iterator i = operations.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
                bf.append(" ").append(e.getValue().toString());
            }
            bf.append(ret);
        }
        bf.append(indent).append("}").append(ret);
        return bf.toString();
    }
}

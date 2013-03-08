/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:48 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xpath/jaxen/JaxenXPathFunction.java,v 1.1 2010-04-03 15:28:48 nlariviera Exp $
 * $Id: JaxenXPathFunction.java,v 1.1 2010-04-03 15:28:48 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xpath.jaxen;

import java.util.Collection;
import java.util.List;

import javax.xml.transform.TransformerException;

import max.xpath.XPathFunction;

import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.w3c.dom.Node;

import java.util.ArrayList;

// TODO Rimuovere la dipendenza da org.apache.xpath.NodeSet

/**
 * Encapsulate a XPathFunction into a Jaxen Function.
 *
 */
public class JaxenXPathFunction implements Function
{
    //--------------------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------------------

    /**
     * Extension function.
     */
    private XPathFunction function;

    //--------------------------------------------------------------------------------------
    // CONSTRUCTOR
    //--------------------------------------------------------------------------------------

    /**
     * Creates a new Jaxen Function encapsulating a XPathFunction.
     *
     * @param function the encapsulated XPath function
     */
    public JaxenXPathFunction(XPathFunction function)
    {
        this.function = function;
    }

    //--------------------------------------------------------------------------------------
    // METHODS
    //--------------------------------------------------------------------------------------

    /**
     * Callback called by Jaxen when the XPathFunction must be executed
     */
    public Object call(Context context, List args) throws FunctionCallException
    {
        Object objects[] = args.toArray();

        // Invoca la funzione
        //
        Node contextNode = (Node)context.getNodeSet().get(0);
        Object object;
        try {
            object = function.evaluate(contextNode, objects);
        }
        catch (TransformerException e) {
            e.printStackTrace();
            throw new FunctionCallException(e);
        }

        if(object == null) {
            return null;
        }

        // Prepara i NodeSet nel caso di collezioni di Nodi
        //

            if (object instanceof Collection) {
            return new ArrayList((Collection) object);

        }
        else {
            Class cls = object.getClass();

            if (cls.isArray()) {
                Node[] nodes = (Node[]) object;
                ArrayList ret = new ArrayList(nodes.length);
                for (int i = 0; i < nodes.length; ++i) {
                    ret.add(nodes[i]);
                }
                return ret;
            }

        }

        return object;
    }
}

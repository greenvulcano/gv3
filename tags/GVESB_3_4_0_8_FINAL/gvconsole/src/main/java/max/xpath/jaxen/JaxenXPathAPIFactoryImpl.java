/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.xpath.jaxen;

import javax.xml.transform.TransformerException;

import max.xpath.XPathAPIFactoryImpl;
import max.xpath.XPathAPIImpl;
import max.xpath.XPathFunction;

import org.jaxen.JaxenException;
import org.jaxen.XPathFunctionContext;
import org.jaxen.dom.DOMXPath;

/**
 * Creates classes that encapsulate Jaxen XPath implementation.
 *
 */
public class JaxenXPathAPIFactoryImpl implements XPathAPIFactoryImpl
{
    //--------------------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------------------

    /**
     * FunctionContext for the build-in and extended XPath functions.
     */
    private XPathFunctionContext functionContext;

    //--------------------------------------------------------------------------------------
    // CONSTRUCTOR
    //--------------------------------------------------------------------------------------

    /**
     * Creates a max.xpath.jaxen.JaxenXPathAPIFactoryImpl.
     * Moreover registers the current() XPath function (not provided by Jaxen itself).
     */
    public JaxenXPathAPIFactoryImpl()
    {
        functionContext = (XPathFunctionContext) XPathFunctionContext.getInstance();

        // current() ce la dobbiamo implementare noi
        //
        functionContext.registerFunction(null, "current", new CurrentFunction());
    }

    //--------------------------------------------------------------------------------------
    // XPathAPIFactoryImpl interface
    //--------------------------------------------------------------------------------------

    /**
     * Build a new XPathAPIImpl.
     *
     * @return an instance of max.xpath.jaxen.JaxenXPathAPIImpl
     */
    public XPathAPIImpl newXPathAPIImpl()
    {
        return new JaxenXPathAPIImpl();
    }

    /**
     * @param xpath string representation of the XPath
     * @return an new instance of org.jaxen.dom.DOMXPath
     */
    public Object newXPath(String xpath) throws TransformerException
    {
        try {
            DOMXPath domXPath = new DOMXPath(xpath);
            domXPath.setFunctionContext(functionContext);
            domXPath.setNamespaceContext(NamespaceContext.instance());
            return domXPath;
        }
        catch (JaxenException exc) {
            exc.printStackTrace();
            System.out.println("XPATH that caused the exception: " + xpath);
            throw new TransformerException(exc);
        }
    }

    /**
     * Install an extension function.
     *
     * @param namespace namespace for the function
     * @param name Name of the funciton
     * @param function the implementation of the function.
     */
    public void installFunction(String namespace, String name, XPathFunction function)
    {
        functionContext.registerFunction(namespace, name, new JaxenXPathFunction(function));
    }

    /* (non-Javadoc)
     * @see max.xpath.XPathAPIFactoryImpl#installNamespace(java.lang.String, java.lang.String)
     */
    public void installNamespace(String prefix, String namespace)
    {
        NamespaceContext.instance().addNamespace(prefix, namespace);
    }
}

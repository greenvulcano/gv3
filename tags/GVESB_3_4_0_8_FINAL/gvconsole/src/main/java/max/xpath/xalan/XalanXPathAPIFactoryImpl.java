/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:55 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xpath/xalan/XalanXPathAPIFactoryImpl.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Id: XalanXPathAPIFactoryImpl.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xpath.xalan;

import javax.xml.transform.TransformerException;

import max.xpath.XPathAPIFactoryImpl;
import max.xpath.XPathAPIImpl;
import max.xpath.XPathFunction;

import org.apache.xpath.XPath;

/**
 * Creates classes that encapsulate Xalan XPath implementation.
 *
 */
public class XalanXPathAPIFactoryImpl implements XPathAPIFactoryImpl
{
    //--------------------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------------------

    /**
     * At the moment a DummyPrefixResolver is used (this means that the namespace
     * are not resolved).
     * <p/>
     * TO DO: build a mechanism for prefix resolver that support namespaces.
     */
    //static PrefixResolver prefixResolver = new DummyPrefixResolver();
    //--------------------------------------------------------------------------------------
    // XPathAPIFactoryImpl interface
    //--------------------------------------------------------------------------------------
    /**
     * Build a new max.xpath.XPathAPIImpl.
     *
     * @return an instance of max.xpath.xalan.XalanXPathAPIImpl
     */
    public XPathAPIImpl newXPathAPIImpl()
    {
        return new XalanXPathAPIImpl();
    }

    /**
     * @param xpath string representation of the XPath
     * @return an new instance of org.apache.xpath.XPath
     */
    public Object newXPath(String xpath) throws TransformerException
    {
        return new XPath(xpath, null, PrefixResolver.instance(), XPath.SELECT, null);
    }

    /**
     * Install an extension function.
     *
     * @param name. Name of the funciton.
     * @param function the implementation of the function.
     */
    public void installFunction(String namespace, String name, XPathFunction function)
    {
        ExtensionsManager.instance().installFunction(namespace, name, function);
    }

    /* (non-Javadoc)
     * @see max.xpath.XPathAPIFactoryImpl#installNamespace(java.lang.String, java.lang.String)
     */
    public void installNamespace(String prefix, String namespace)
    {
        PrefixResolver.instance().installNamespace(prefix, namespace);
    }
}

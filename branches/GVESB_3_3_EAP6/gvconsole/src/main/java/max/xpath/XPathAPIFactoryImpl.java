/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:56 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xpath/XPathAPIFactoryImpl.java,v 1.1 2010-04-03 15:28:56 nlariviera Exp $
 * $Id: XPathAPIFactoryImpl.java,v 1.1 2010-04-03 15:28:56 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xpath;

import javax.xml.transform.TransformerException;

/**
 * The actual implementation must implement this interface.
 * To use a particular implementation, you must configure a class implementing this interface.
 *
 */
public interface XPathAPIFactoryImpl
{
    /**
     * @return the XPathAPIImpl that executes the XPath using a particular implementation.
     */
    XPathAPIImpl newXPathAPIImpl();

    /**
     * Creates a new implementation XPath. The actual object type depends on the underying
     * implementation.
     *
     * @param xpath a string representation of the XPath
     * @return the loaw-level object used by the implementation.
     * @throws TransformerException if an error occurs.
     */
    Object newXPath(String xpath) throws TransformerException;

    /**
     * Install an extension function.
     *
     * @param name name of the function. The function is into the "max" namespace.
     * @param function function
     */
    void installFunction(String namespace, String name, XPathFunction function);

    /**
     * Installs a namespace that can be used in XPath expressions.
     *
     * @param prefix
     * @param namespace the empty string or null specify the default namespace
     */
    void installNamespace(String prefix, String namespace);
}

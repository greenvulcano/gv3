/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:56 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xpath/XPath.java,v 1.1 2010-04-03 15:28:56 nlariviera Exp $
 * $Id: XPath.java,v 1.1 2010-04-03 15:28:56 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xpath;

import javax.xml.transform.TransformerException;


/**
 * This class encapsulates a low level XPath (current version uses the Xalan
 * implementation). In order to enhance the performances, the low level XPath is build
 * only once.
 * <br/>
 * Note that the XPath in the Maxime framework supports an extended syntax
 * (see the {@link max.xpath.XPathAPI XPathAPI} class): the extended syntax is not
 * optimized because the xpath must be calculated at runtime. <i><b>The extended syntax
 * is deprecated.</b> Use the XPath <code>current()</code> function instead.</i>
 *
 */
public class XPath
{
    //--------------------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------------------

    /**
     * Low-level XPath.
     * The actual type depends on the implementation.
     */
    private Object xpath = null;

    /**
     * The string representation of the XPath
     */
    private String xpathString = null;

    //--------------------------------------------------------------------------------------
    // CONSTRUCTOR
    //--------------------------------------------------------------------------------------

    /**
     * @param xpathStr a correct XPath
     * @throws TransformerException if an invalid XPath is given
     */
    public XPath(String xpathStr) throws TransformerException
    {
        xpathString = xpathStr;

        // check if is an extended xpath (i.e. uses the $[...]$ syntax)
        //
        int idx1 = xpathString.indexOf("$[");
        int idx2 = xpathString.indexOf("]$", idx1);

        if((idx1 == -1) || (idx2 == -1)) {

            // The XPath is not extended, so we can calculate it at this time.
            //
            xpath = XPathAPIFactory.instance().newXPath(xpathStr);
        }
        else {

            // If the XPath is extended, then we set xpath = null because
            // it must be calculated at runtime.
            //
            xpath = null;
        }
    }


    //--------------------------------------------------------------------------------------
    // ACCESSOR METHODS
    //--------------------------------------------------------------------------------------

    /**
     * @return the low level XPath
     */
    public Object getXPath()
    {
        return xpath;
    }

    /**
     * @return the string representation of this XPath
     */
    public String getXPathString()
    {
        return xpathString;
    }

    /**
     * Check if this XPath is extended (i.e. uses the <code>$[...]$</code> syntax).
     *
     * @return true if this XPath is extended
     */
    public boolean isExtended()
    {
        // This implementation set xpath = null if is extended
        //
        return xpath == null;
    }

    /**
     * @return the string representation of this XPath
     */
    public String toString()
    {
        return xpathString;
    }
}

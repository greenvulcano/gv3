/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:49 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/ElementValueCheck.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Id: ElementValueCheck.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.Collection;

import max.xpath.XPathAPI;

import org.w3c.dom.Element;

/**
 * Check if the value of the element is in the set of values returned by the
 * given ValuesSelectorList.
 *
 */
public class ElementValueCheck implements Check
{
    private ValuesSelectorList valuesSelectorList = null;
    private String elementName;

    public ElementValueCheck(String elementName, ValuesSelectorList valuesSelectorList)
    {
        this.elementName = elementName;
        this.valuesSelectorList = valuesSelectorList;
    }

    /**
     * Return a list of warnings for the given element.
     *
     * @return <code>null</code> if there are not warnings.
     */
    public Warning[] getWarning(Element element)
    {
        // return if the check does not apply to the element
        //
        if(!elementName.equals(element.getNodeName())) {
            return null;
        }

        String val = XPathAPI.getNodeValue(element);

        // We append ## so the current value is present into the collection
        // only if it is a valid value
        //
        Collection coll = valuesSelectorList.getValues(element, val + "##");
        if(coll == null) return null;

        // If the value is in the collection, then OK
        //
        if(coll.contains(val)) return null;

        return new Warning[] {
            new Warning(
                "Invalid value for element '" + elementName + "'",
                element
            )
        };
    }

    /* (non-Javadoc)
     * @see max.xml.Check#getElementName()
     */
    public String getElementName()
    {
        return elementName;
    }

    /* (non-Javadoc)
     * @see max.xml.Check#getXPaths()
     */
    public String[] getXPaths()
    {
        return valuesSelectorList.getXPaths();
    }
}

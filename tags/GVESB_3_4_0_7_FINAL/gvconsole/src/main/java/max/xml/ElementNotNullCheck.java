/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-14 15:31:05 $ $Header:
 * /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS
 * /core/webapps/gvconsole/src/main/java/max/xml/ElementNotNullCheck.java,v 1.1
 * 2010-04-03 15:28:50 nlariviera Exp $ $Id: ElementNotNullCheck.java,v 1.1
 * 2010-04-03 15:28:50 nlariviera Exp $ $Name:  $ $Locker:  $ $Revision: 1.2 $
 * $State: Exp $
 */
package max.xml;

import max.xpath.XPath;
import max.xpath.XPathAPI;

import org.w3c.dom.Element;

/**
 * Checks that the attribute value is not an empty string. The value will be
 * trimmed before perform the test.
 *
 */
public class ElementNotNullCheck implements Check
{
    private String  elementName;
    private Feature feature;

    /**
     * @param feature
     * @param elementName
     */
    public ElementNotNullCheck(Feature feature, String elementName)
    {
        this.feature = feature;
        this.elementName = elementName;
    }

    /**
     * Return a list of warnings for the given element.
     *
     * @param element
     *
     * @return <code>null</code> if there are not warnings.
     */
    public Warning[] getWarning(Element element)
    {
        // return if the check does not apply to the element
        //
        if (!feature.appliesTo(element)) {
            return null;
        }

        String val = XPathAPI.getNodeValue(element);
        val = val.trim();

        // Se il valore � una stringa vuota, allora c'� un errore.
        //
        if (val.equals("")) {
            return new Warning[]{new Warning("Element '" + elementName + "' cannot be empty", element)};
        }

        return null;
    }

    /**
     * @see max.xml.Check#getElementName()
     */
    public String getElementName()
    {
        return elementName;
    }

    /**
     * @see max.xml.Check#getXPaths()
     */
    public String[] getXPaths()
    {
        XPath xpath = feature.getContext();
        if (xpath == null) {
            return null;
        }
        return new String[]{xpath.getXPathString()};
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ElementNotNullCheck[" + elementName + "]";
    }
}
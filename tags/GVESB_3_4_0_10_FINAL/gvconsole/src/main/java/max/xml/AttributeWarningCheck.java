/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:50 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/AttributeWarningCheck.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Id: AttributeWarningCheck.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import max.xpath.XPath;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Emits a warning. You can use this check in order to avoid the use of
 * not yet supported functionalities.
 *
 */
public class AttributeWarningCheck implements Check
{
    private String elementName;
    private String attributeName;
    private Feature feature;

    public AttributeWarningCheck(Feature feature, String elementName, String attributeName)
    {
        this.elementName = elementName;
        this.attributeName = attributeName;
        this.feature = feature;
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
        XPath xpath = feature.getContext();
        if(xpath == null) {
            return null;
        }
        return new String[] { xpath.getXPathString() };
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
        if(!feature.appliesTo(element)) {
            return null;
        }

        Attr attributeNode = element.getAttributeNode(attributeName);
        if(attributeNode == null) {
            return null;
        }

        return new Warning[] {
            new Warning(
                feature.getParameter(),
                element
            )
        };
    }

    public String toString()
    {
        return "AttributeWarningCheck[" + elementName + ", " + attributeName + "]";
    }
}

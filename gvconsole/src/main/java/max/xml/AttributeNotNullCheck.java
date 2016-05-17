/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-14 15:31:06 $ $Header:
 * /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS
 * /core/webapps/gvconsole/src/main/java/max/xml/AttributeNotNullCheck.java,v
 * 1.1 2010-04-03 15:28:51 nlariviera Exp $ $Id: AttributeNotNullCheck.java,v
 * 1.1 2010-04-03 15:28:51 nlariviera Exp $ $Name:  $ $Locker:  $ $Revision: 1.2 $
 * $State: Exp $
 */
package max.xml;

import max.xpath.XPath;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Checks that the attribute value is not an empty string. The value will be
 * trimmed before perform the test.
 *
 */
public class AttributeNotNullCheck implements Check
{
    private String  elementName;
    private String  attributeName;
    private Feature feature;

    /**
     * @param feature
     * @param elementName
     * @param attributeName
     */
    public AttributeNotNullCheck(Feature feature, String elementName, String attributeName)
    {
        this.feature = feature;
        this.elementName = elementName;
        this.attributeName = attributeName;
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

        Attr attr = element.getAttributeNode(attributeName);

        // Se l'attributo non � presente, allora non � necessario un check.
        //
        if (attr == null) {
            return null;
        }

        String val = attr.getValue();

        val = val.trim();

        // Se il valore � una stringa vuota, allora c'� un errore.
        //
        if (val.equals("")) {
            return new Warning[]{new Warning("Attribute '" + attributeName + "' in the element '" + elementName
                    + "' cannot be empty", element)};
        }

        return null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "AttributeNotNullCheck[" + elementName + ", " + attributeName + "]";
    }
}
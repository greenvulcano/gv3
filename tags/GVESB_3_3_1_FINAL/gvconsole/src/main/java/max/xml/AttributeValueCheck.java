/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:48 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/AttributeValueCheck.java,v 1.1 2010-04-03 15:28:48 nlariviera Exp $
 * $Id: AttributeValueCheck.java,v 1.1 2010-04-03 15:28:48 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.Collection;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

public class AttributeValueCheck implements Check
{
    private ValuesSelectorList valuesSelectorList = null;
    private String elementName;
    private String attributeName;

    public AttributeValueCheck(String elementName, String attributeName, ValuesSelectorList valuesSelectorList)
    {
        this.elementName = elementName;
        this.attributeName = attributeName;
        this.valuesSelectorList = valuesSelectorList;
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

        Attr attr = element.getAttributeNode(attributeName);

        // Se l'attributo non � presente, allora non � necessario un check.
        //
        if(attr == null) {
            return null;
        }

        String val = attr.getValue();

        // We append ## so the current value is present into the collection
        // only if it is a valid value
        //
        Collection coll = valuesSelectorList.getValues(element, val + "##");
        if(coll == null) {
            return null;
        }

        // If the value is in the collection, then OK
        //
        if(coll.contains(val)) {
            return null;
        }

        return new Warning[] {
            new Warning(
                "Invalid value for attribute '" + attributeName + "' in the element '" + elementName + "'",
                element
            )
        };
    }

    public String toString()
    {
        return "AttributeValueCheck[" + elementName + ", " + attributeName + "]";
    }
}

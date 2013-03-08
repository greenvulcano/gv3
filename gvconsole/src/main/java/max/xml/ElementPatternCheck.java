/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:50 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/ElementPatternCheck.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Id: ElementPatternCheck.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import max.xpath.XPath;
import max.xpath.XPathAPI;

import org.w3c.dom.Element;

/**
 * Controlla che il valore di un elemento rispetti un pattern definito da
 * un'espressione regolare.
 *
 */
public class ElementPatternCheck implements Check
{
    private String elementName;
    private Feature feature;
    private Pattern pattern;
    private String errorDescription;


    public ElementPatternCheck(Feature feature, String elementName)
    {
        this.feature = feature;
        this.elementName = elementName;
        String param = feature.getParameter().trim();
        char firstChar = param.charAt(0);
        int idx = param.indexOf(firstChar, 1);
        if(idx == -1) {
            this.pattern = Pattern.compile(param.substring(1).trim());
            errorDescription = "regular expression " + this.pattern + " not matched";
        }
        else {
            this.pattern = Pattern.compile(param.substring(1, idx).trim());
            errorDescription = param.substring(idx + 1).trim();
        }
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

        String val = XPathAPI.getNodeValue(element);

        Matcher matcher = pattern.matcher(val);
        if(!matcher.matches()) {
            return new Warning[] {
                new Warning(
                    "Element '" + elementName + "': " + errorDescription,
                    element
                )
            };
        }

        return null;
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

    public String toString()
    {
        return "ElementNotNullCheck[" + elementName + "]";
    }
}

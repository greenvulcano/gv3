/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:51 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/AttributePatternCheck.java,v 1.1 2010-04-03 15:28:51 nlariviera Exp $
 * $Id: AttributePatternCheck.java,v 1.1 2010-04-03 15:28:51 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import max.xpath.XPath;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Controlla che il valore di un attributo rispetti un pattern definito da
 * un'espressione regolare.
 *
 */
public class AttributePatternCheck implements Check
{
    private String elementName;
    private String attributeName;
    private Feature feature;
    private Pattern pattern;
    private String errorDescription;

    public AttributePatternCheck(Feature feature, String elementName, String attributeName)
    {
        this.feature = feature;
        this.elementName = elementName;
        this.attributeName = attributeName;
        String param = feature.getParameter().trim();
        char firstChar = param.charAt(0);
        int idx = param.indexOf(firstChar, 1);
        if(idx == -1) {
            this.pattern = Pattern.compile(param.substring(1).trim());
            errorDescription = "regular expression " + this.pattern.pattern() + " not matched";
        }
        else {
            this.pattern = Pattern.compile(param.substring(1, idx).trim());
            errorDescription = param.substring(idx + 1).trim();
        }
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
        return new String[]{xpath.getXPathString()};
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

        Attr attr = element.getAttributeNode(attributeName);

        // Se l'attributo non � presente, allora non � necessario un check.
        //
        if(attr == null) {
            return null;
        }

        String val = attr.getValue();

        Matcher matcher = pattern.matcher(val);
        if(!matcher.matches()) {
            return new Warning[]{new Warning("Attribute '" + attributeName + "' in the element '"
                + elementName + "': " + errorDescription, element)};
        }

        return null;
    }

    public String toString()
    {
        return "AttributePatternCheck[" + elementName + ", " + attributeName + ", "
            + pattern.pattern() + "]";
    }
}
/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *

 */
package max.xml;

import it.greenvulcano.configuration.XMLConfig;

import javax.xml.transform.TransformerException;

import max.xpath.XPath;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ElementUniqueCheck implements Check {
    private String  elementName;
    private Feature feature;
    private XPath   xpath = null;

    public ElementUniqueCheck(Feature feature, String elementName) {
        this.elementName = elementName;
        this.feature = feature;
        try {
            xpath = new XPath(feature.getParameter());
        }
        catch (TransformerException exc) {
            exc.printStackTrace();
            xpath = null;
        }

    }

    /**
     * Return a list of warnings for the given element.
     *
     * @return <code>null</code> if there are not warnings.
     */
    public Warning[] getWarning(Element element) {
        // return if the check does not apply to the element
        //
        if (!feature.appliesTo(element)) {
            return null;
        }

        String currentValue = XMLConfig.getNodeValue(element);

        NodeList nodeList;
        try {
            nodeList = feature.getXPathAPI().selectNodeList(element, xpath);
        }
        catch (TransformerException exc) {
            throw new RuntimeException(exc);
        }

        int len = nodeList.getLength();

        for (int i = 0; i < len; ++i) {
            Node node = nodeList.item(i);

            // We check only with other nodes values
            //
            if (node != element) {
                String val = XMLConfig.getNodeValue(node);

                // The value of the attribute match the value of another node
                //
                if (val.equals(currentValue)) {
                    return new Warning[] { new Warning("Element '" + elementName + "' is not unique", element) };
                }
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see max.xml.Check#getElementName()
     */
    public String getElementName() {
        return elementName;
    }

    /*
     * (non-Javadoc)
     *
     * @see max.xml.Check#getXPaths()
     */
    public String[] getXPaths() {
        XPath xpath = feature.getContext();
        if (xpath == null) {
            return new String[] { feature.getParameter() };
        }
        return new String[] { xpath.getXPathString(), feature.getParameter() };
    }

    @Override
    public String toString() {
        return "ElementUniqueCheck[" + elementName + "]";
    }
}

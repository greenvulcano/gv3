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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AttributeUniqueCheck implements Check {
    private String  elementName;
    private String  attributeName;
    private Feature feature;
    // Aggiunta Renato
    private XPath   xpath = null;

    public AttributeUniqueCheck(Feature feature, String elementName, String attributeName) {
        this.elementName = elementName;
        this.attributeName = attributeName;
        this.feature = feature;
        // Aggiunta Renato
        try {
            xpath = new XPath(feature.getParameter());
        }
        catch (TransformerException exc) {
            exc.printStackTrace();
            xpath = null;
        }

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

        Attr attr = element.getAttributeNode(attributeName);

        // Se l'attributo non � presente, allora non � necessario un check.
        //
        if (attr == null) {
            return null;
        }

        String currentValue = attr.getNodeValue();

        // Commentato Renato---NodeList nodeList =
        // XMLConfig.getNodeList(element, feature.getParameter());
        // Aggiunta Renato
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
            if (node != attr) {
                String val = XMLConfig.getNodeValue(node);

                // The value of the attribute match the value of another node
                //
                if (val.equals(currentValue)) {
                    return new Warning[] { new Warning("Attribute '" + attributeName + "' in the element '"
                            + elementName + "' is not unique", element) };
                }
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "AttributeUniqueCheck[" + elementName + ", " + attributeName + "]";
    }
}

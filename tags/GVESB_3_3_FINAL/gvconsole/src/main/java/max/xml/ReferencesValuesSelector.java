/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-11 14:32:08 $ $Header:
 * /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS
 * /core/webapps/gvconsole/src/main/java/max/xml/ReferencesValuesSelector.java,v
 * 1.1 2010-04-03 15:28:49 nlariviera Exp $ $Id: ReferencesValuesSelector.java,v
 * 1.1 2010-04-03 15:28:49 nlariviera Exp $ $Name:  $ $Locker:  $ $Revision: 1.2 $
 * $State: Exp $
 */
package max.xml;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerException;

import max.xpath.XPath;
import max.xpath.XPathAPI;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Selects a set of values selected by a given XPath.
 */
public class ReferencesValuesSelector extends ValuesSelector
{
    private XPath xpath = null;

    /**
     * The parameter must be an XPath relative to the element. An absolute XPath
     * is also accepted.
     */
    public ReferencesValuesSelector(Feature feature)
    {
        super(feature);
        try {
            xpath = new XPath(feature.getParameter());
        }
        catch (TransformerException exc) {
            exc.printStackTrace();
            xpath = null;
        }
    }

    public List getValues(Node node, String currentValue)
    {
        if (xpath == null) {
            return nullResult;
        }

        try {
            NodeList nodeList = feature.getXPathAPI().selectNodeList(node, xpath);
            if (nodeList == null) {
                return nullResult;
            }
            int len = nodeList.getLength();

            List list = new LinkedList();
            list.add(currentValue);

            for (int i = 0; i < len; ++i) {
                Node n = nodeList.item(i);
                String val = XPathAPI.getNodeValue(n);
                if (!val.equals(currentValue)) {
                    list.add(val);
                }
            }

            return list;
        }
        catch (Exception exc) {
            exc.printStackTrace();
            return nullResult;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see max.xml.ValuesSelector#fillXPaths(java.util.Set)
     */
    public void fillXPaths(Set xpaths)
    {
        if (xpath != null) {
            xpaths.add(xpath.getXPathString());
        }
    }
}
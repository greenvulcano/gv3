/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.xml;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfigValuesSelector extends ValuesSelector {
    private String xpath = null;
    private String file  = null;

    /**
     * The parameter of the feature must be of the form
     * <code>file : xpath</code>
     */
    public ConfigValuesSelector(Feature feature) {
        super(feature);
        String parameter = feature.getParameter();
        int idx = parameter.indexOf(':');
        file = parameter.substring(0, idx).trim();
        xpath = parameter.substring(idx + 1).trim();
    }

    @Override
    public List getValues(Node node, String currentValue) {
        List list = new LinkedList();
        list.add(currentValue);
        NodeList nodeList;
        try {
            nodeList = XMLConfig.getNodeList(file, xpath);
        }
        catch (XMLConfigException exc) {
            exc.printStackTrace();
            return Collections.EMPTY_LIST;
        }

        int len = nodeList.getLength();

        for (int i = 0; i < len; ++i) {
            String val = XMLConfig.getNodeValue(nodeList.item(i));
            if (!val.equals(currentValue)) {
                list.add(val);
            }
        }

        return list;
    }

    /*
     * (non-Javadoc)
     *
     * @see max.xml.ValuesSelector#fillXPaths(java.util.Set)
     */
    @Override
    public void fillXPaths(Set xpaths) {
        if (xpath != null) {
            xpaths.add(xpath);
        }
    }
}

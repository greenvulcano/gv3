package it.greenvulcano.birt.report.internal.field;

import it.greenvulcano.birt.report.Parameter;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FixedSource extends Source
{

    private List<LabelValueBean> entries = new ArrayList<LabelValueBean>();

    public FixedSource()
    {
        // do nothing
    }

    public void init(Node source)
    {
        try {
            type = Parameter.SOURCE_TYPE_FIXED;
            NodeList nl = XMLConfig.getNodeList(source, "ListItem");
            boolean sort = XMLConfig.getBoolean(source, "@sort", false);
            for (int j = 0; j < nl.getLength(); j++) {
                entries.add(new LabelValueBean(XMLConfig.get(nl.item(j), "@text"), XMLConfig.get(nl.item(j), "@value")));
            }
            if (sort) {
                Collections.sort(entries, new FieldValueComparator());
            }
        }
        catch (XMLConfigException exc) {
            exc.printStackTrace();
        }
    }

    public Object getData(Map<String, String> params)
    {
        return entries;
    }
}

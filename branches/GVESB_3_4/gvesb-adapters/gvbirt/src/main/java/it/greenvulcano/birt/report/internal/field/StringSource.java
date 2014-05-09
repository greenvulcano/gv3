package it.greenvulcano.birt.report.internal.field;

import it.greenvulcano.birt.report.Parameter;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.Map;

import org.w3c.dom.Node;

public class StringSource extends Source {

    private String value = "";

    public StringSource()
    {
        // do nothing
    }

    public void init(Node source)
    {
        try {
            type = Parameter.SOURCE_TYPE_STRING;
            value = XMLConfig.get(source, ".").trim();
        }
        catch (XMLConfigException exc) {
            exc.printStackTrace();
        }
    }

    public Object getData(Map<String, String> params)
    {
        try {
            return PropertiesHandler.expand(value, MapUtils.convertToHMStringObject(params));
        }
        catch (Exception exc) {
            // do nothing
        }
        return "";
    }
}

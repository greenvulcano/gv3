package it.greenvulcano.birt.report.internal.field;

import java.util.Map;

import org.w3c.dom.Node;

public abstract class Source {

    String type;

    abstract public void init(Node source);

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    abstract public Object getData(Map<String, String> params) throws Exception;
}

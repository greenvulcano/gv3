/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.transform.Transformer;

import max.core.MaxException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author MaximeInformatica snc - Sergio
 */
public abstract class StylesheetSource {
    protected HashMap parameters = new HashMap();

    public void init(Node node) throws MaxException, XMLConfigException {
        NodeList nl = XMLConfig.getNodeList(node, "XSLParameter");

        if ((nl != null) && (nl.getLength() != 0)) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                parameters.put(XMLConfig.get(n, "@name"), XMLConfig.get(n, "@value"));
            }
        }
    }

    public Transformer load() throws MaxException {
        Transformer transformer = loadInternal();
        setParameters(transformer);
        return transformer;
    }

    /**
     * @param transformer
     */
    private void setParameters(Transformer transformer) {
        Set p = parameters.keySet();
        Iterator i = p.iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            transformer.setParameter(name, parameters.get(name));
        }
    }

    protected abstract Transformer loadInternal() throws MaxException;
}

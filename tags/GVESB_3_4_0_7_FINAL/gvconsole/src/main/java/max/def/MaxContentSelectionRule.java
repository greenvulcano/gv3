/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.def;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import max.core.ContentProvider;
import max.core.ContentSelectionRule;
import max.core.Contents;
import max.core.MaxException;

import org.w3c.dom.Node;

/**
 * Seleziona tutti i contenuti disponibili per l'utente che sta facendo la
 * richiesta, dalla categoria configurata del provider configurato.
 * <p>
 *
 * Il provider � definito dalla property <code>max.content.provider.name</code>
 * e la categoria � definita dalla property <code>max.content.category</code>.
 */
public class MaxContentSelectionRule implements ContentSelectionRule {
    private String providerName;
    private String category;

    public void init(Node node) throws MaxException {
        try {
            String ruleName = XMLConfig.get(node, "../@name");

            providerName = XMLConfig.get(node, "@provider");
            if (providerName == null) {
                throw new MaxException("No 'provider' attribute for selector '" + ruleName + "'");
            }
            category = XMLConfig.get(node, "@category");
            if (category == null) {
                throw new MaxException("No 'category' attribute for selector '" + ruleName + "'");
            }
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }
    }

    public Map[] select(HttpServletRequest request, String param) throws MaxException {

        Contents contents = Contents.instance();
        ContentProvider provider;
        try {
            provider = contents.getProvider(providerName);
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }
        synchronized (provider) {
            String contentNames[] = provider.getContentNames(category);
            return provider.getContentsAttributes(category, contentNames);
        }
    }
}

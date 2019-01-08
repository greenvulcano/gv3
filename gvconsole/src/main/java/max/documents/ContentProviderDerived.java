/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 *
 */
package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.InputStream;

import max.core.ContentProvider;
import max.core.Contents;
import max.core.MaxException;

import org.w3c.dom.Node;

/**
 * @author Maxime Informatica s.n.c. -
 */
public class ContentProviderDerived implements DerivedDocumentProxy {
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    private String providerName;
    private String category;
    private String contentName;

    // ----------------------------------------------------------------------------------------------
    // DerivedDocumentProxy interface
    // ----------------------------------------------------------------------------------------------

    /**
     * @see max.documents.DerivedDocumentProxy#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws MaxException {
        try {
            providerName = XMLConfig.get(node, "@ContentProviderName");
            category = XMLConfig.get(node, "@Category");
            contentName = XMLConfig.get(node, "@ContentName");
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }
    }

    /**
     * @see max.documents.DerivedDocumentProxy#save(java.io.InputStream)
     */
    public void save(InputStream document) throws MaxException {
        ContentProvider contentProvider;
        try {
            contentProvider = Contents.instance().getProvider(providerName);
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }

        if (contentProvider.exists(category, contentName)) {
            contentProvider.update(category, contentName, document);
        }
        else {
            contentProvider.insert(category, contentName, document);
        }
    }

}

/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import max.core.ContentProvider;
import max.core.Contents;
import max.core.MaxException;

import org.w3c.dom.Node;

/**
 * @author Maxime Informatica s.n.c. -
 */
public class ContentProviderStylesheetSource extends StylesheetSource {
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    private String providerName;
    private String category;
    private String contentName;

    // ----------------------------------------------------------------------------------------------
    // StylesheetSource interface
    // ----------------------------------------------------------------------------------------------

    /**
     * @throws XMLConfigException
     * @see max.documents.StylesheetSource#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws MaxException, XMLConfigException {
        try {
            providerName = XMLConfig.get(node, "@ContentProviderName");
            category = XMLConfig.get(node, "@Category");
            contentName = XMLConfig.get(node, "@ContentName");
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }

        super.init(node);
    }

    /**
     * @see max.documents.StylesheetSource#loadInternal()
     */
    @Override
    protected Transformer loadInternal() throws MaxException {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();

            ContentProvider contentProvider;
            try {
                contentProvider = Contents.instance().getProvider(providerName);
            }
            catch (XMLConfigException exc) {
                throw new MaxException(exc);
            }

            StreamSource source = new StreamSource(contentProvider.get(category, contentName));
            Transformer transformer = factory.newTransformer(source);
            return transformer;
        }
        catch (TransformerConfigurationException e) {
            e.printStackTrace();
            throw new MaxException("Cannot load the transformer: provider=" + providerName + "category=" + category
                    + "content=" + contentName, e);
        }
        catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
            throw new MaxException("Cannot load the transformer: provider=" + providerName + "category=" + category
                    + "content=" + contentName + ". Cause: " + e.getMessage());
        }
    }
}

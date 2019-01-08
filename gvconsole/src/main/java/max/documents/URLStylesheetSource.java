/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import max.core.MaxException;

import org.w3c.dom.Node;

/**
 * @author Maxime Informatica s.n.c. -
 */
public class URLStylesheetSource extends StylesheetSource {
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    private URL url;

    // ----------------------------------------------------------------------------------------------
    // StylesheetSource interface
    // ----------------------------------------------------------------------------------------------

    /**
     * @throws XMLConfigException
     * @see max.documents.StylesheetSource#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws MaxException, XMLConfigException {
        String urlString = XMLConfig.get(node, "@url");
        try {
            url = new URL(urlString);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            throw new MaxException("Invalid url for URLStylesheetSource: " + urlString, e);
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
            Source source = new StreamSource(url.openStream());
            Transformer transformer = factory.newTransformer(source);
            return transformer;
        }
        catch (TransformerConfigurationException e) {
            e.printStackTrace();
            throw new MaxException("Cannot load the transformer: " + url, e);
        }
        catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
            throw new MaxException("Cannot load the transformer: " + url + ". Cause: " + e.getMessage());
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new MaxException("Cannot load the transformer: " + url, e);
        }
    }

}

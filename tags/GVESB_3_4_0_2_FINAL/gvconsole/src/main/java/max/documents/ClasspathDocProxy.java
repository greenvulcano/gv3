/*
 * Created on 10-nov-2003
 */
package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import max.core.MaxException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Maxime Informatica s.n.c. -
 */
public class ClasspathDocProxy implements DocumentProxy {
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    /**
     * Entity resolver used to ignore the Input file doctype
     */
    private static EntityResolver entityResolver = new DefaultEntityResolver();

    /**
     * The document name to read
     */
    private String                resource       = "";

    /**
     * Initialize proxy
     *
     * @param node
     *            the proxy node
     * @throws MaxException
     *             If an error occurred
     */
    public void init(Node node) throws MaxException {

        try {
            resource = XMLConfig.get(node, "@resource");
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }
    }

    /**
     * Read the document in the classpath
     *
     * @return document read.
     * @throws MaxException
     *             if an error occurred
     */
    public Document load() throws MaxException {
        ClassLoader loader = getClass().getClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        InputStream stream = loader.getResourceAsStream(resource);
        DocumentBuilderFactory factoryIn = DocumentBuilderFactory.newInstance();
        factoryIn.setNamespaceAware(true);

        try {
            DocumentBuilder doc = factoryIn.newDocumentBuilder();
            doc.setEntityResolver(entityResolver);
            return doc.parse(stream);
        }
        catch (ParserConfigurationException exc) {
            throw new MaxException(exc);
        }
        catch (SAXException exc) {
            throw new MaxException(exc);
        }
        catch (IOException exc) {
            throw new MaxException(exc);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see max.documents.DocumentProxy#getURL()
     */
    public URL getURL() {
        ClassLoader loader = getClass().getClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        return loader.getResource(resource);
    }

    /**
     * Generate e MaxException because this proxy is only to read documents.
     *
     * @see max.documents.DocumentProxy#save(org.w3c.dom.Document)
     */
    public void save(Document document) throws MaxException {
        throw new MaxException("Read only proxy: cannot save the document.");

    }

    /**
     * Default entity resolver used by XMLConfig. This entity resolver does not
     * resolve any entity. To use an actual entity resolver use the
     * <code>setEntityResolver()</code> method of <code>XMLConfig</code>.
     *
     * @see #setDefaultEntityResolver()
     * @see #setEntityResolver(org.xml.sax.EntityResolver)
     */
    static class DefaultEntityResolver implements EntityResolver {
        /**
         * @return an InputSource for an empty string.
         * @param publicId
         *            The public identification
         * @param systemId
         *            The system identification
         */
        public InputSource resolveEntity(String publicId, String systemId) {
            return new InputSource(new StringReader(""));
        }
    }
}
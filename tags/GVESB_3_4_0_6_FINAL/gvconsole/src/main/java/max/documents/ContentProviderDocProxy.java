package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import max.core.ContentProvider;
import max.core.Contents;
import max.core.MaxException;
import max.xml.DOMWriter;
import max.xml.MaxXMLFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * This class defines operations to load and save documents. It implements
 * DocumentProxy interface.
 *
 */
public class ContentProviderDocProxy implements DocumentProxy {

    private String providerName = null;
    private String category     = null;
    private String contentName  = null;

    /**
     * This load document
     *
     * @return Document The new document object of the DOM.
     */
    public Document load() throws MaxException {

        Document document = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            MaxXMLFactory xmlFactory = MaxXMLFactory.instance();
            EntityResolver entityResolver = xmlFactory.getEntityResolver();

            db.setEntityResolver(entityResolver);
            ContentProvider content = Contents.instance().getProvider(providerName);
            InputStream istream = content.get(category, contentName);
            document = db.parse(istream);
        }
        catch (ParserConfigurationException ex) {
            throw new MaxException(ex);
        }
        catch (SAXException ex) {
            throw new MaxException(ex);
        }
        catch (IOException ex) {
            throw new MaxException(ex);
        }
        catch (XMLConfigException ex) {
            throw new MaxException(ex);
        }
        return document;
    }

    /**
     * This method saves the document
     *
     * @param Document
     *            The document to save
     */
    public void save(Document document) throws MaxException {
        ContentProvider content;
        try {
            content = Contents.instance().getProvider(providerName);
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }

        InputStream inStream = toInputStream(document);
        if (content.exists(category, contentName)) {
            content.update(category, contentName, inStream);
        }
        else {
            content.insert(category, contentName, inStream);
        }
    }

    /**
     * This method initialize ContentProviderDocProxy with the values read from
     * the specified node
     *
     * @param Node
     *            The node starting from which values are read.
     */
    public void init(Node node) {

        try {
            providerName = XMLConfig.get(node, "@ContentProviderName");
            category = XMLConfig.get(node, "@Category");
            contentName = XMLConfig.get(node, "@ContentName");
        }
        catch (XMLConfigException exc) {
            throw new RuntimeException(exc);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see max.documents.DocumentProxy#getURL()
     */
    public URL getURL() {
        return null;
    }

    /**
     * This method transform a document in InputStream.
     *
     * @param dom
     *            The document to be transformed.
     *
     * @return The InputStream
     */
    private InputStream toInputStream(Document dom) throws MaxException {
        try {
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            DOMWriter domWriter = new DOMWriter();
            domWriter.setWriteDoctype(true);
            domWriter.write(dom, ostream);
            ostream.flush();
            return new ByteArrayInputStream(ostream.toByteArray());
        }
        catch (IOException exc) {
            throw new MaxException(exc);
        }
    }

}
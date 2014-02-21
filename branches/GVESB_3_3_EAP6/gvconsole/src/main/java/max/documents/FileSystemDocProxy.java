package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import max.core.MaxException;
import max.xml.DOMWriter;
import max.xml.MaxXMLFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * This class defines operations to load and save documents. It's work in
 * collaboration of the DocumentProxy interface.
 *
 */
public class FileSystemDocProxy implements DocumentProxy
{

    /**
     * File to load/save.
     */
    private String file = null;

    /**
     * @see max.documents.DocumentProxy#getURL()
     */
    public URL getURL()
    {
        try {
            return new URL("file", null, file);
        }
        catch (MalformedURLException exc) {
            exc.printStackTrace();
            return null;
        }
    }

    /**
     * This method parses the content of the given file as an XML document and
     * return a new DOM Document object
     *
     * @return Document The new document object of the DOM.
     * @throws MaxException
     */
    public Document load() throws MaxException
    {

        Document document = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            MaxXMLFactory xmlFactory = MaxXMLFactory.instance();
            EntityResolver entityResolver = xmlFactory.getEntityResolver();

            db.setEntityResolver(entityResolver);
            FileInputStream istream = new FileInputStream(file);

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
        return document;
    }

    /**
     * This method saves
     *
     * @param document
     * @throws MaxException
     */
    public void save(Document document) throws MaxException
    {
        try {
            DOMWriter domWriter = new DOMWriter();
            FileOutputStream ostream = new FileOutputStream(file);
            domWriter.write(document, ostream);
            ostream.flush();
            ostream.close();
        }
        catch (IOException ex) {
            throw new MaxException(ex);
        }
    }

    /**
     * This method draws the value of the "path" attribute and with it creates a
     * file object.
     *
     * @param node
     *        The node from which catch value.
     * @throws MaxException
     */
    public void init(Node node) throws MaxException
    {

        try {
            file = XMLConfig.get(node, "@path");

            if (!PropertiesHandler.isExpanded(file)) {
                try {
                    file = PropertiesHandler.expand(file, null);
                }
                catch (PropertiesHandlerException exc) {
                    exc.printStackTrace();
                }
            }

        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }
    }
}
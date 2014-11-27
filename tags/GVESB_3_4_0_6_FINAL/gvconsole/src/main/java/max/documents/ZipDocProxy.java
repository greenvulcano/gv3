package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import max.core.MaxException;
import max.util.ZipFileEditor;
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
public class ZipDocProxy implements DocumentProxy
{
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    static final int BUFFER = 2048;

    /**
     * zip file to load/save.
     */
    private File     file   = null;

    /**
     * File to load/save.
     */
    private String   entry  = null;

    // ----------------------------------------------------------------------------------------------
    // METHODS
    // ----------------------------------------------------------------------------------------------

    /**
     * @see max.documents.DocumentProxy#getURL()
     */
    public URL getURL()
    {
        try {
            return new URL("jar", null, "file:" + file.getAbsolutePath() + "!"
                    + (entry.startsWith("/") ? entry : "/" + entry));
        }
        catch (MalformedURLException exc) {
            exc.printStackTrace();
            return null;
        }
    }

    /**
     * This method parses the content of the given file, present in a file
     * '.zip', as an XML document and return a new DOM Document object
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

            JarFile jarFile = new JarFile(file);
            ZipEntry zipEntry = jarFile.getEntry(entry);

            InputStream istream = jarFile.getInputStream(zipEntry);

            document = db.parse(istream);
            istream.close();
            jarFile.close();
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
     * This method saves the modifications.
     *
     * @param document
     * @throws MaxException
     */
    public void save(Document document) throws MaxException
    {
        try {
            ZipFileEditor zipFileEditor = new ZipFileEditor(file);
            zipFileEditor.setEntry(entry, document);
            zipFileEditor.commit();
        }
        catch (Exception ex) {
            throw new MaxException("Zip: " + file.getAbsolutePath() + ", entry: " + entry, ex);
        }
    }

    /**
     * This method draws the value of the "PATH" tag and with it creates a file
     * object.
     *
     * @param node
     *        The node from which catch value.
     * @throws MaxException
     */
    public void init(Node node) throws MaxException
    {
        try {
            file = new File(XMLConfig.get(node, "@path"));
            entry = XMLConfig.get(node, "@entry");
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }
    }
}

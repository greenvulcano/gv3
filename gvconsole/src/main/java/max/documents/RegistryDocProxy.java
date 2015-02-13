package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.net.URL;

import max.core.MaxException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class get the document from registry.
 */
public class RegistryDocProxy implements DocumentProxy
{
    /**
     * The name of file to read
     */
    private String documentId = null;

    /*
     * (non-Javadoc)
     *
     * @see max.documents.DocumentProxy#getURL()
     */
    public URL getURL()
    {
        return null;
    }

    /**
     * This method parses the content of the given file, present in a file
     * '.zip', as an XML document and return a new DOM Document object
     *
     * @throws MaxException
     *         if an error occurred
     * @return Document The new document object of the DOM.
     */
    public Document load() throws MaxException
    {
        try {
            return DocumentRepository.instance().getDocument(documentId);
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }
    }

    /**
     * This proxy is only to read document. The save method generates a
     * MaxException.
     *
     * @param document
     *        The document to write
     * @throws MaxException
     *         An ICon exception
     */
    public void save(Document document) throws MaxException
    {
        throw new MaxException("Read only proxy: cannot save the document.");
    }

    /**
     * This method draws the value of the "PATH" tag and with it creates a file
     * object.
     *
     * @param node
     *        The node from which catch value.
     */
    public void init(Node node) throws MaxException
    {
        try {
            documentId = XMLConfig.get(node, "@resource");
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }
    }
}
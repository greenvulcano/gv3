/*
 * Created on 6-apr-2005
 */
package max.documentation;

import it.greenvulcano.configuration.XMLConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.w3c.dom.Node;

/**
 * @author
 *
 */

public class PDFProducer
{
    // ----------------------------------------------------------------------------------------------
    // CONSTANTS
    // ----------------------------------------------------------------------------------------------

    /**
     * The fop structor
     */
    public static final String RES_FOP    = "fop.xsl";

    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    private Node               fopDocument;
    private FopFactory         fopFactory = FopFactory.newInstance();
    private TransformerFactory tFactory   = TransformerFactory.newInstance();


    // ----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    // ----------------------------------------------------------------------------------------------

    public PDFProducer(String documentId) throws Exception
    {
        ConfigurationDocInterface configDocInterface = getDocumentInterface(documentId);
        fopDocument = configDocInterface.createFop();
    }

    public PDFProducer(Node fopDocument) throws Exception
    {
        this.fopDocument = fopDocument;
    }

    public PDFProducer(File fopDocument) throws Exception
    {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
        this.fopDocument = documentBuilder.parse(fopDocument);
    }

    // ----------------------------------------------------------------------------------------------
    // METHODS
    // ----------------------------------------------------------------------------------------------

    public static ConfigurationDocInterface getDocumentInterface(String documentId) throws Exception
    {
        Node configurationNode = XMLConfig.getNode(PDFRegistryBean.PDF_DOCUMENTS,
                "/documents/*[@type='document'][@id='" + documentId + "']");
        // Leggo il nome del plugin da istanziare e lo istanzio

        String className = XMLConfig.get(configurationNode, "@class", "");

        ConfigurationDocInterface configDocInterface = null;

        // Instance the plugin class requested.
        // For XML documentation or DTD documentation
        //
        Class pluginClass = Class.forName(className);
        configDocInterface = (ConfigurationDocInterface) pluginClass.newInstance();
        configDocInterface.init(configurationNode);

        return configDocInterface;
    }

    /**
     * Produce the documentation
     *
     * @param outputStream
     *        The OutputStream where the PDF document will be written
     * @return the size of the output
     * @throws Exception
     *         if an error occurred
     */
    public int produceDocumentation(OutputStream outputStream) throws Exception
    {

        // Preleva lo stylesheet per ottenere il file .fo dalle
        // risorse dell'applicazione
        //
        InputStream stream = getClass().getResourceAsStream(RES_FOP);
        if (stream == null) {
            throw new Exception("Resource " + RES_FOP + " not found");
        }

        StreamSource source = new StreamSource(stream);

        // Setup a buffer to obtain the content length
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Setup FOP
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

        // Setup Transformer
        Transformer transformer = tFactory.newTransformer(source);

        // Make sure the XSL transformation's result is piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        // Setup input
        Source src = new DOMSource(fopDocument);

        // Start the transformation and rendering process
        transformer.transform(src, res);

        out.close();
        // Send content to Browser
        outputStream.write(out.toByteArray());
        outputStream.flush();

        return out.size();
    }
}
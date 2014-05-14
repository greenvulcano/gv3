/*
 * Created on 21-feb-2005
 *
 */
package max.documentation.dtd;

import it.greenvulcano.configuration.XMLConfig;

import java.io.InputStream;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import max.documentation.ConfigurationDocInterface;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */

public class DtdDocumentationPlugin implements ConfigurationDocInterface {
    /**
     * The document result
     */
    Document                   resultDocument = null;

    /**
     * The roor element
     */
    Element                    rootElement    = null;

    /**
     * The dtd-ref elememt list
     */
    NodeList                   list           = null;

    /**
     * The xsl for the result
     */
    public static final String RES_STYLESHEET = "report.xsl";

    /**
     * @see max.documentation.ConfigurationDocInterface#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder;

        try {
            documentBuilder = dbFactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException exc) {
            throw new Exception("Cannot create the DocumentBuilder", exc);
        }

        resultDocument = documentBuilder.newDocument();
        rootElement = resultDocument.createElement("configuration-guide");
        resultDocument.appendChild(rootElement);

        // Creo la cover

        rootElement.setAttribute("title", XMLConfig.get(node, "cover/@title"));
        rootElement.setAttribute("version", XMLConfig.get(node, "cover/@version"));
        rootElement.setAttribute("date", XMLConfig.get(node, "cover/@date", "" + (new Date())));
        rootElement.setAttribute("author", XMLConfig.get(node, "cover/@author"));
        rootElement.setAttribute("company", XMLConfig.get(node, "cover/@owner"));

        list = XMLConfig.getNodeList(node, "dtd-ref");
    }

    public Node createFop() throws Exception {
        Document resultDocument = createDocument();
        Node result = getResult(resultDocument);
        return result;
    }

    /**
     * @param resultDocument2
     * @return
     */
    private Node getResult(Document resultDocument) throws Exception {
        Transformer transformer = null;
        Node result = null;

        InputStream stream = getClass().getResourceAsStream(RES_STYLESHEET);
        if (stream == null) {
            throw new Exception("Resource " + RES_STYLESHEET + " not found");
        }

        StreamSource source = new StreamSource(stream);

        // Prepara il source per la trasformazione
        //
        DOMSource documentSource = new DOMSource(resultDocument);

        // Prepara il risultato
        //
        DOMResult domResult = new DOMResult();

        // Costruisce il transformer
        //
        TransformerFactory tf = TransformerFactory.newInstance();

        try {
            transformer = tf.newTransformer(source);

            // Esegue la trasformazione
            //
            transformer.transform(documentSource, domResult);
            result = domResult.getNode();
        }
        catch (TransformerConfigurationException exc) {
            throw new Exception(exc);
        }
        catch (TransformerException exc) {
            throw new Exception(exc);
        }

        return result;
    }

    /**
     * @throws Exception
     *             If an error occurred processing DTD
     * @return resultDocument The resulting document
     */
    private Document createDocument() throws Exception {
        if (list != null) {
            for (int i = 0; i < list.getLength(); ++i) {
                Node dtdElement = list.item(i);
                String systemId = XMLConfig.get(dtdElement, "@systemId");
                String publicId = XMLConfig.get(dtdElement, "@publicId");
                String rootElementName = XMLConfig.get(dtdElement, "@rootElement");

                Document document;

                try {
                    document = processDTD(rootElementName, publicId, systemId);
                }
                catch (Exception exc) {
                    throw new Exception("Cannot process the DTD: " + publicId + " " + systemId, exc);
                }

                Element root = document.getDocumentElement();
                Element importedNode = (Element) resultDocument.importNode(root, true);

                String dtdRootElement = XMLConfig.get(dtdElement, "@rootElement");

                String title = XMLConfig.get(dtdElement, "@title");

                importedNode.setAttribute("root-element", dtdRootElement);
                importedNode.setAttribute("system-id", systemId);
                importedNode.setAttribute("public-id", publicId);
                importedNode.setAttribute("title", title);

                rootElement.appendChild(importedNode);
            }
        }

        return resultDocument;
    }

    // ----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    // ----------------------------------------------------------------------------------------------

    /**
     * Process the DTD documentation
     *
     * @param rootElementName
     *            The root element Name
     * @param publicId
     *            The publicId defined in the dtd-ref element
     * @param systemId
     *            The systemId defined in the dtd-ref element
     * @return document The processed DTD document
     * @throws Exception
     *             if an error occurred
     */
    private Document processDTD(String rootElementName, String publicId, String systemId) throws Exception {
        DTDParser parser = new DTDParser();
        Document document = parser.parseDTD(rootElementName, publicId, systemId);

        return document;
    }
}
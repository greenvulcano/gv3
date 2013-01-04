/*
 * Created on 21-feb-2005
 */
package max.documentation.xml;

import it.greenvulcano.configuration.XMLConfig;

import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import max.core.MaxException;
import max.documentation.ConfigurationDocInterface;
import max.documents.DocumentProxy;
import max.documents.StylesheetSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */

public class XmlDocumentationPlugin implements ConfigurationDocInterface
{

    /**
     * The xml-ref elememt list
     */
    private NodeList         list              = null;

    /**
     * The document title
     */
    private String           title             = "";

    /**
     * The document version
     */
    private String           version           = "";

    /**
     * The author of document
     */
    private String           author            = "";

    /**
     * The owner of document
     */
    private String           owner             = "";

    /**
     * The document date creation
     */
    private String           date              = "";

    /**
     * The root element to insert in the xml document created for multiple
     * xml-ref configured
     */
    private String           globalRootElement = "";

    /**
     * The stylesheet class name
     */
    private StylesheetSource stylesheetSrc     = null;

    /**
     * The init method read the configuration file and set the global fields.
     *
     * @see max.documentation.ConfigurationDocInterface#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws Exception
    {
        // The cover information
        //
        title = XMLConfig.get(node, "cover/@title");
        version = XMLConfig.get(node, "cover/@version");
        date = XMLConfig.get(node, "cover/@date", "" + (new Date()));
        author = XMLConfig.get(node, "cover/@author");
        owner = XMLConfig.get(node, "cover/@owner");

        // The root element is required when
        // multiple xml-ref are configured
        //
        globalRootElement = XMLConfig.get(node, "@rootElement");

        // Lo stylesheet da applicare
        //
        Node stylesheetNode = XMLConfig.getNode(node, "*[@type='stylesheet-src']");
        String className = XMLConfig.get(stylesheetNode, "@class");

        try {
            Class styleClass = Class.forName(className);
            stylesheetSrc = (StylesheetSource) styleClass.newInstance();
            stylesheetSrc.init(stylesheetNode);
        }
        catch (Exception exc) {
            throw new Exception(exc);
        }

        list = XMLConfig.getNodeList(node, "*[@type='proxy']");
    }

    /**
     * This method creates the document to process with fop mechanism.
     *
     * @throws Exception
     *         If an error occurred processing DTD
     * @return resultDocument The resulting document
     */
    public Node createFop() throws Exception
    {
        Node result = null;

        try {
            Document resultDocument = createDocument();
            result = applyStylesheet(resultDocument);
        }
        catch (Exception exc) {
            throw new Exception(exc);
        }

        return result;
    }

    /**
     * Creates the xml document to process with fop.
     *
     * @return the xml document
     * @throws Exception
     *         If an error occurred durinf document creation
     */
    public Document createDocument() throws Exception
    {
        Document resultDocument = null;
        Element rootElement = null;
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

        if (globalRootElement != null) {

            // Multiple xml-ref are configured
            // so the new xml document must have a global root.
            //
            rootElement = resultDocument.createElement(globalRootElement);
            resultDocument.appendChild(rootElement);
        }

        if (list != null) {
            for (int i = 0; i < list.getLength(); ++i) {
                Node proxy = list.item(i);

                try {
                    String className = XMLConfig.get(proxy, "@class");

                    DocumentProxy docProxy = createClass(className);
                    docProxy.init(proxy);
                    Document document = docProxy.load();

                    Element element = document.getDocumentElement();
                    Element importedNode = (Element) resultDocument.importNode(element, true);

                    if (i == 0) {
                        if (rootElement != null) {

                            // the cover in the root element configured
                            // (multiple xml-ref are configured)
                            //
                            rootElement.setAttribute("title", title);
                            rootElement.setAttribute("version", version);
                            rootElement.setAttribute("date", date);
                            rootElement.setAttribute("author", author);
                            rootElement.setAttribute("company", owner);
                            rootElement.appendChild(importedNode);
                        }
                        else {
                            // the cover in root element of document
                            // (only one xml-ref is configured)
                            //
                            importedNode.setAttribute("title", title);
                            importedNode.setAttribute("version", version);
                            importedNode.setAttribute("date", date);
                            importedNode.setAttribute("author", author);
                            importedNode.setAttribute("company", owner);
                            resultDocument.appendChild(importedNode);
                        }
                    }
                    else {
                        if (rootElement != null) {
                            rootElement.appendChild(importedNode);
                        }
                    }
                }
                catch (MaxException exc) {
                    exc.printStackTrace();
                }
                catch (TransformerException exc) {
                    exc.printStackTrace();
                }
            }
        }

        return resultDocument;
    }

    /**
     * @param className
     * @return
     */
    private DocumentProxy createClass(String className) throws Exception
    {
        try {
            // Instance the plugin proxy class requested.
            //
            Class pluginClass = Class.forName(className);
            return (DocumentProxy) pluginClass.newInstance();
        }
        catch (ClassNotFoundException exc) {
            throw new Exception("Class Not Found", exc);
        }
        catch (InstantiationException exc) {
            throw new Exception("Instantiation error for className " + className, exc);
        }
        catch (IllegalAccessException exc) {
            throw new Exception("Illegal access", exc);
        }
    }

    /**
     * Get the node result to process with fop
     *
     * @param resultDocument
     *        the xml document
     * @throws Exception
     *         if an error occurred
     * @return
     */
    private Node applyStylesheet(Document resultDocument) throws Exception
    {
        Transformer transformer = stylesheetSrc.load();

        // Prepara il source per la trasformazione
        //
        DOMSource documentSource = new DOMSource(resultDocument);

        // Prepara il risultato
        //
        DOMResult domResult = new DOMResult();

        // Esegue la trasformazione
        //
        transformer.transform(documentSource, domResult);

        return domResult.getNode();
    }
}
/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import max.core.MaxException;
import max.xml.DOMWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Derived document managed by the MasterDerivedDocProxy
 *
 * @see max.documents.MasterDerivedDocProxy
 *
 * @author Maxime Informatica s.n.c. -
 */
public class DerivedDocument
{
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    private StylesheetSource[]     stylesheetSources;
    private DerivedDocumentProxy[] derivedDocumentProxies;
    private String                 documentName;
    private String                 xpath;
    private URL                    masterURL;
    private boolean                replaceXMLProperties;

    // ----------------------------------------------------------------------------------------------
    // CONSTRUCTOR
    // ----------------------------------------------------------------------------------------------

    public DerivedDocument()
    {
    }

    // ----------------------------------------------------------------------------------------------
    // METHODS - initialization
    // ----------------------------------------------------------------------------------------------

    /**
     * @param node
     * @throws MaxException
     * @throws XMLConfigException
     */
    public void init(Node node) throws MaxException, XMLConfigException
    {
        documentName = XMLConfig.get(node, "@name", "");
        xpath = XMLConfig.get(node, "@xpath", "");
        replaceXMLProperties = XMLConfig.getBoolean(node, "@replace-xml-properties", false);

        // Stylesheets
        //
        NodeList list = XMLConfig.getNodeList(node, "*[@type='stylesheet-src']");
        stylesheetSources = new StylesheetSource[list.getLength()];
        for (int i = 0; i < stylesheetSources.length; ++i) {
            Node stylesheetNode = list.item(i);
            String className = XMLConfig.get(stylesheetNode, "@class");
            StylesheetSource stylesheetSrc = null;
            try {
                Class stylesheetClass = Class.forName(className);
                stylesheetSrc = (StylesheetSource) stylesheetClass.newInstance();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new MaxException("Cannot create the StylesheetSource '" + stylesheetNode.getNodeName()
                        + "' for the derived document '" + documentName + "'", e);
            }
            catch (InstantiationException e) {
                e.printStackTrace();
                throw new MaxException("Cannot create the StylesheetSource '" + stylesheetNode.getNodeName()
                        + "' for the derived document '" + documentName + "'", e);
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new MaxException("Cannot create the StylesheetSource '" + stylesheetNode.getNodeName()
                        + "' for the derived document '" + documentName + "'", e);
            }
            stylesheetSrc.init(stylesheetNode);
            stylesheetSources[i] = stylesheetSrc;
        }

        // Derived documents
        //
        list = XMLConfig.getNodeList(node, "*[@type='derived-doc']");
        derivedDocumentProxies = new DerivedDocumentProxy[list.getLength()];
        for (int i = 0; i < derivedDocumentProxies.length; ++i) {
            Node proxyNode = list.item(i);
            String className = XMLConfig.get(proxyNode, "@class");
            DerivedDocumentProxy proxy = null;
            try {
                Class proxyClass = Class.forName(className);
                proxy = (DerivedDocumentProxy) proxyClass.newInstance();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new MaxException("Cannot create the DerivedDocumentProxy '" + proxyNode.getNodeName()
                        + "' for the derived document '" + documentName + "'", e);
            }
            catch (InstantiationException e) {
                e.printStackTrace();
                throw new MaxException("Cannot create the DerivedDocumentProxy '" + proxyNode.getNodeName()
                        + "' for the derived document '" + documentName + "'", e);
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new MaxException("Cannot create the DerivedDocumentProxy '" + proxyNode.getNodeName()
                        + "' for the derived document '" + documentName + "'", e);
            }
            proxy.init(proxyNode);
            derivedDocumentProxies[i] = proxy;
        }
    }

    public void setMasterURL(URL masterURL)
    {
        this.masterURL = masterURL;
    }

    // ----------------------------------------------------------------------------------------------
    // METHODS
    // ----------------------------------------------------------------------------------------------

    /**
     * @param document
     * @throws MaxException
     */
    public void save(Document document) throws MaxException
    {
        if (xpath != null && xpath.length() > 0) {
            // Applyies XPath to get XML fragments to transform and save
            try {
                NodeList nodeList = XMLConfig.getNodeList(document, xpath);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    saveSingleDoc(nodeList.item(i));
                }
            }
            catch (XMLConfigException exc) {
                throw new MaxException("Cannot apply XPath '" + xpath + "'", exc);
            }
        }
        else {
            saveSingleDoc(document);
        }
    }

    private void saveSingleDoc(Node node) throws MaxException
    {
        byte[] result = applyTransformations(node);
        for (int i = 0; i < derivedDocumentProxies.length; ++i) {
            ByteArrayInputStream in = new ByteArrayInputStream(result);
            derivedDocumentProxies[i].save(in);
        }
    }

    /**
     * @return the derived document name
     */
    public String getDocumentName()
    {
        return documentName;
    }

    // ----------------------------------------------------------------------------------------------
    // IMPLEMENTATION METHODS
    // ----------------------------------------------------------------------------------------------

    /**
     * Applica le trasformazioni configurate. Le trasformazioni intermedie
     * avvengono in memoria utilizzando DOMSource e DOMResult. L'ultima
     * trasformazione utilizza un StreamResult con un ByteArrayOutputStream.
     *
     * @return il risultato delle trasformazioni sotto forma di byte[].
     */
    private byte[] applyTransformations(Node document) throws MaxException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Nessuna trasformazione: restituiamo il documento cos� come �
        //
        if (stylesheetSources.length == 0) {
            DOMWriter writer = new DOMWriter();
            try {
                writer.write(document, out);
            }
            catch (IOException e1) {
                e1.printStackTrace();
                throw new MaxException("Cannot write the document", e1);
            }
        }
        else {
            // Trasformazioni intermedie
            //
            for (int i = 0; i < stylesheetSources.length - 1; ++i) {
                Transformer transformer = stylesheetSources[i].load();
                DOMSource source = new DOMSource(document);
                DOMResult result = new DOMResult();
                try {
                    transformer.transform(source, result);
                }
                catch (TransformerException e) {
                    e.printStackTrace();
                    throw new MaxException("Cannot perform the transformation", e);
                }
                document = result.getNode();
            }

            // Ultima trasformazione
            //
            Transformer transformer = stylesheetSources[stylesheetSources.length - 1].load();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(out);
            try {
                transformer.transform(source, result);
            }
            catch (TransformerException e) {
                e.printStackTrace();
                throw new MaxException("Cannot perform the transformation", e);
            }
        }

        if (replaceXMLProperties) {
            try {
                return XMLConfig.replaceXMLProperties(masterURL, out.toByteArray());
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new MaxException("Cannot replace XML properties", e);
            }
        }

        return out.toByteArray();
    }

    private byte[] formatDocument(byte[] document) throws MaxException
    {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setExpandEntityReferences(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
                {
                    return new InputSource(new StringReader(""));
                }
            });
            Document formattedDocument = db.parse(new ByteArrayInputStream(document));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DOMWriter writer = new DOMWriter();
            writer.setWriteDoctype(true);
            writer.write(formattedDocument, out);

            return out.toByteArray();
        }
        catch (Exception exc) {
            // Se c'e' eccezione lasciamo perdere la formattazione (potrebbe non
            // essere un documento XML)
            // In fondo si tratta solo di un fattore estetico.
            return document;
        }
    }
}
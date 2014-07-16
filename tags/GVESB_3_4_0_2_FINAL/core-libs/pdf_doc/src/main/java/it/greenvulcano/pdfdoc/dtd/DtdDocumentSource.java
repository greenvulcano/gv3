/*
 * Copyright (c) 2004 Maxime Informatica s.n.c. - All right reserved
 *
 */
package it.greenvulcano.pdfdoc.dtd;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.pdfdoc.DocumentSource;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.InputStream;
import java.util.Date;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2003 - All right reserved
 */
public class DtdDocumentSource implements DocumentSource
{
    /**
     * The dtd-document elements
     */
    private NodeList     list              = null;

    /**
     * The document title
     */
    private String       title             = "";

    /**
     * The document version
     */
    private String       version           = "";

    /**
     * The document author
     */
    private String       author            = "";

    /**
     * The document owner
     */
    private String       owner             = "";

    /**
     * The document creation date
     */
    private String       date              = "";

    /**
     * The stylesheet
     */
    public static final String RES_STYLESHEET = "reference.xsl";

    /**
     * @see DocumentSource#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws Exception
    {
        title = XMLConfig.get(node, "cover/@title");
        version = XMLConfig.get(node, "cover/@version");
        date = XMLConfig.get(node, "cover/@date", "" + (new Date()));
        author = XMLConfig.get(node, "cover/@author");
        owner = XMLConfig.get(node, "cover/@owner");

        list = XMLConfig.getNodeList(node, "dtd-document");
    }

    /**
     * @see DocumentSource#getFop()
     */
    @Override
    public Node getFop() throws Exception
    {
        Document aggrDocument = readDocuments();

        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(RES_STYLESHEET);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(new StreamSource(is));
            DOMResult domResult = new DOMResult();
            transformer.transform(new DOMSource(aggrDocument), domResult);
            return domResult.getNode();
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }

    /**
     * @return
     * @throws Exception
     */
    public Document readDocuments() throws Exception
    {
        Document aggDocument = null;
        Element root = null;
        XMLUtils parser = null;

        try {
            parser = XMLUtils.getParserInstance();

            aggDocument = parser.newDocument("reference-guide");
            root = aggDocument.getDocumentElement();
            root.setAttribute("title", title);
            root.setAttribute("version", version);
            root.setAttribute("date", date);
            root.setAttribute("author", author);
            root.setAttribute("company", owner);

            for (int i = 0; i < list.getLength(); i++) {
                Node dd = list.item(i);

                String systemId = XMLConfig.get(dd, "@systemId");
                String publicId = XMLConfig.get(dd, "@publicId");
                String rootName = XMLConfig.get(dd, "@rootElement");
                String lTitle = XMLConfig.get(dd, "@title");

                Document document;

                try {
                    document = processDTD(rootName, publicId, systemId);
                }
                catch(Exception exc) {
                    throw new Exception("Cannot process the DTD: " + publicId + " " + systemId, exc);
                }

                Element element = document.getDocumentElement();
                Element importedNode = (Element)aggDocument.importNode(element, true);

                importedNode.setAttribute("root-element", rootName);
                importedNode.setAttribute("system-id", systemId);
                importedNode.setAttribute("public-id", publicId);
                importedNode.setAttribute("title", lTitle);

                root.appendChild(importedNode);
            }
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }

        return aggDocument;
    }

    /**
     * @param rootName
     * @param publicId
     * @param systemId
     * @return
     * @throws Exception
     */
    private Document processDTD(String rootName, String publicId, String systemId)
        throws Exception
    {
        DTDParser parser = new DTDParser();
        Document document = parser.parseDTD(rootName, publicId, systemId);

        return document;
    }
}
/*
 * Copyright (c) 2004 Maxime Informatica s.n.c. - All right reserved
 *
 */
package it.greenvulcano.pdfdoc.xml;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.pdfdoc.DocumentSource;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
public class XmlDocumentSource implements DocumentSource
{

    /**
     * The xml-document elements
     */
    private List<String> list              = null;

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
    private String       stylesheet        = null;

    /**
     * @see DocumentSource#init(Node)
     */
    @Override
    public void init(Node node) throws Exception
    {
        title = XMLConfig.get(node, "cover/@title");
        version = XMLConfig.get(node, "cover/@version");
        date = XMLConfig.get(node, "cover/@date", "" + (new Date()));
        author = XMLConfig.get(node, "cover/@author");
        owner = XMLConfig.get(node, "cover/@owner");

        stylesheet = XMLConfig.get(node, "stylesheet/@url");

        list = new ArrayList<String>();
        NodeList nlist = XMLConfig.getNodeList(node, "xml-document");
        for (int i = 0; i < nlist.getLength(); i++) {
            Node xd = nlist.item(i);
            list.add(XMLConfig.get(xd, "@url"));
        }
    }

    /**
     * @return
     * @throws Exception
     */
    @Override
    public Node getFop() throws Exception
    {
        Document aggrDocument = readDocuments();

        InputStream is = null;
        try {
            is = new URL(stylesheet).openStream();
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

            aggDocument = parser.newDocument("XmlDocument");
            root = aggDocument.getDocumentElement();
            root.setAttribute("title", title);
            root.setAttribute("version", version);
            root.setAttribute("date", date);
            root.setAttribute("author", author);
            root.setAttribute("company", owner);

            for (String xdUrl : list) {
                InputStream is = null;
                try {
                    System.out.println("XML Document: " + xdUrl);

                    is = new URL(xdUrl).openStream();
                    Document document = parser.parseDOM(is);

                    Element element = document.getDocumentElement();
                    Element importedNode = (Element) aggDocument.importNode(element, true);

                    root.appendChild(importedNode);
                }
                catch (Exception exc) {
                    exc.printStackTrace();
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
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }

        return aggDocument;
    }
}
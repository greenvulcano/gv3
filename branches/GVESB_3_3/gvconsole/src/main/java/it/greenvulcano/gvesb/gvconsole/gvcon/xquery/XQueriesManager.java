/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.gvconsole.gvcon.xquery;

import it.greenvulcano.configuration.XMLConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import max.xml.DOMWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class XQueriesManager
{
    String                          configFileName   = "XQueries.xml";

    private Node                    configNode       = null;

    private Vector<XQueriesElement> xQueriesElements = new Vector<XQueriesElement>();

    /**
     *
     */
    public XQueriesManager()
    {
        try {
            String xQueryesXPath = "/XQueries";
            configNode = XMLConfig.getNode(configFileName, xQueryesXPath);
            loadXQueries();
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * @throws Throwable
     */
    public void loadXQueries() throws Throwable
    {
        // load the XQuery elements
        String xQueryXPath = "./XQuery";
        NodeList xQueryNodes = XMLConfig.getNodeList(configNode, xQueryXPath);
        // get items
        for (int i = 0; i < xQueryNodes.getLength(); i++) {
            Element xQueryElement = (Element) xQueryNodes.item(i);
            String nome = xQueryElement.getAttribute("nome");
            String descrizione = XMLConfig.getNodeValue(XMLConfig.getNode(xQueryElement, "Descrizione"));
            String xQueryString = XMLConfig.getNodeValue(XMLConfig.getNode(xQueryElement, "XQueryString"));
            XQueriesElement xQueriesElement = new XQueriesElement();
            xQueriesElement.setFields(nome, descrizione, xQueryString);
            xQueriesElements.add(xQueriesElement);
        }
    }

    /**
     * @param element
     * @throws Throwable
     */
    public void updateConfig(XQueriesElement element) throws Throwable
    {

        URL url = XMLConfig.getURL(configFileName);
        String protocol = url.getProtocol();
        if (!(protocol.equals("file"))) {
            throw new Exception("The config cannot be load a remote file");
        }
        String nome = element.getNome();
        if ((nome == null) || nome.equals("")) {
            return;
        }
        // get the document
        Document doc = configNode.getOwnerDocument();
        // make XQuery Node
        makeXQueryNode(element, doc);
        setXQueryElement(element);
        // write document on the file
        saveFile(doc, url.getPath());
    }

    /**
     * @param element
     * @throws Throwable
     */
    public void deleteElement(XQueriesElement element) throws Throwable
    {
        xQueriesElements.remove(element);
        // remove from file
        String xpath = "//XQuery[@nome='" + element.getNome() + "']";
        Node node = XMLConfig.getNode(configNode, xpath);
        if (node != null) {
            configNode.removeChild(node);
            Document doc = configNode.getOwnerDocument();
            URL url = XMLConfig.getURL(configFileName);
            saveFile(doc, url.getPath());
        }
    }

    /**
     * @param element
     * @param doc
     * @throws Throwable
     */
    public void makeXQueryNode(XQueriesElement element, Document doc) throws Throwable
    {
        // make XQuery node
        Element eXQuery = doc.createElement("XQuery");
        eXQuery.setAttribute("nome", element.getNome());

        // make Descrizione node
        Element eDescrizione = doc.createElement("Descrizione");
        eDescrizione.appendChild(doc.createTextNode(element.getDescrizione()));

        // make XQueryString node
        Element eXQueryString = doc.createElement("XQueryString");
        eXQueryString.appendChild(doc.createTextNode(element.getXqueryString()));

        // append Descrizione and XQueryString nodes to XQuery node
        eXQuery.appendChild(eDescrizione);
        eXQuery.appendChild(eXQueryString);

        // if element exist then replace it
        String xpath = "//XQuery[@nome='" + element.getNome() + "']";
        Node node = XMLConfig.getNode(configNode, xpath);
        if (node != null) {
            configNode.replaceChild(eXQuery, node);
        }
        else {
            configNode.appendChild(eXQuery);
        }
    }

    /**
     * @param i
     * @return
     */
    public XQueriesElement getXQueriesElement(int i)
    {
        return xQueriesElements.get(i);
    }

    /**
     * @param element
     */
    public void setXQueryElement(XQueriesElement element)
    {
        int i = xQueriesElements.indexOf(element);
        if (i < 0) {
            xQueriesElements.add(element);
        }
        else {
            xQueriesElements.set(i, element);
        }
    }

    /**
     * @return
     */
    public int getNumberOfElements()
    {
        return xQueriesElements.size();
    }

    /**
     * @param doc
     * @param nameFile
     * @throws IOException
     */
    public void saveFile(Document doc, String nameFile) throws IOException
    {
        File fout = new File(nameFile);
        FileOutputStream out = new FileOutputStream(fout);
        DOMWriter domWriter = new DOMWriter();
        domWriter.write(doc, out);
        out.flush();
        out.close();
    }
}
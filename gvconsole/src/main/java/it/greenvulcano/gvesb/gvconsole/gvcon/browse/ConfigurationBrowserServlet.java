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
package it.greenvulcano.gvesb.gvconsole.gvcon.browse;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import max.documents.DocumentRepository;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class ConfigurationBrowserServlet extends HttpServlet
{

    private static final long serialVersionUID = 300L;

    /**
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse)
            throws ServletException, IOException
    {
        String s = httpservletrequest.getParameter("cmd");

        if (s == null) {
            throw new ServletException("No 'cmd' parameter found");
        }
        if (s.equals("tree")) {
            produceTree(httpservletrequest, httpservletresponse);
        }
        else if (s.equals("schemaGV")) {
            produceDescription(httpservletrequest, httpservletresponse, "schemaGV", "novalue", "schemaEB.xsl");
        }
        else if (s.equals("service")) {
            produceDescription(httpservletrequest, httpservletresponse, "service", "novalue", "svcDescription.xsl");
        }
        else if (s.equals("channel")) {
            produceDescription(httpservletrequest, httpservletresponse, "channel", "system", "channelDescription.xsl");
        }
        else if (s.equals("system")) {
            produceDescription(httpservletrequest, httpservletresponse, "system", "novalue", "systemDescription.xsl");
        }
        else {
            throw new ServletException("Invalid cmd: '" + s + "'");
        }
    }

    private void produceTree(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse)
            throws ServletException, IOException
    {
        Transformer transformer = getTransformer("tree.xsl");
        applyTransformer(transformer, httpservletresponse);
    }

    private void produceDescription(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse,
            String s, String ss, String stylesheetName) throws ServletException, IOException
    {

        String s1 = httpservletrequest.getParameter(s);
        String s2 = httpservletrequest.getParameter(ss);
        if (s1 == null) {
            throw new ServletException("No '" + s + "' parameter found in the request");
        }
        else {

            Transformer transformer = getTransformer(stylesheetName);
            transformer.setParameter(s, s1);
            if (s2 != null) {
                transformer.setParameter(ss, s2);
            }

            String serverpath = httpservletrequest.getScheme() + "://" + httpservletrequest.getServerName() + ":"
                    + httpservletrequest.getServerPort();
            transformer.setParameter("serverpath", serverpath);
            applyTransformer(transformer, httpservletresponse);
            return;
        }
    }

    private void applyTransformer(Transformer transformer, HttpServletResponse httpservletresponse)
            throws ServletException, IOException
    {
        DOMSource domsource = null;
        try {
            domsource = new DOMSource(getConfiguration(httpservletresponse));
        }
        catch (Exception exception) {
            exception.printStackTrace();
            throw new ServletException("Error while reading the configuration file", exception);
        }
        StreamResult streamresult = new StreamResult(httpservletresponse.getWriter());
        try {
            transformer.transform(domsource, streamresult);
        }
        catch (TransformerException transformerexception) {
            transformerexception.printStackTrace();
            throw new ServletException("Cannot apply the transformation", transformerexception);
        }
        catch (Exception e) {
            httpservletresponse.getWriter().print("Errore: le informazioni richieste non sono disponibili!!!");
        }

    }

    private Transformer getTransformer(String stylesheetName) throws ServletException, IOException
    {
        TransformerFactory transformerfactory = TransformerFactory.newInstance();
        InputStream istream = getClass().getResourceAsStream(stylesheetName);
        StreamSource streamsource = new StreamSource(istream);
        Transformer transformer = null;
        try {
            transformer = transformerfactory.newTransformer(streamsource);
        }
        catch (TransformerConfigurationException transformerconfigurationexception) {
            transformerconfigurationexception.printStackTrace();
            throw new ServletException("Cannot create the Transformer " + stylesheetName,
                    transformerconfigurationexception);
        }
        return transformer;
    }

    private Document getConfiguration(HttpServletResponse httpservletresponse) throws IOException
    {
        try {
            DocumentRepository registry = DocumentRepository.instance();
            Document servicesDocument = registry.getDocument("GreenVulcanoServices");
            Document systemsDocument = registry.getDocument("GreenVulcanoSystemsConfig");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document result = documentBuilder.newDocument();

            Element root = result.createElement("GreenVulcano");
            root.setAttribute("version", "1.0");
            result.appendChild(root);

            attachNode(systemsDocument, root, "Systems");
            attachNode(servicesDocument, root, "Groups");
            attachNode(servicesDocument, root, "Services");

            return result;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * @param sourceDocument
     * @param destinationDocument
     * @param destinationNode
     * @param nodeName
     */
    private void attachNode(Document sourceDocument, Element destinationNode, String nodeName)
    {
        Node templatesNode = sourceDocument.getElementsByTagName(nodeName).item(0);
        Document destinationDocument = destinationNode.getOwnerDocument();
        templatesNode = destinationDocument.importNode(templatesNode, true);
        destinationNode.appendChild(templatesNode);
    }
}
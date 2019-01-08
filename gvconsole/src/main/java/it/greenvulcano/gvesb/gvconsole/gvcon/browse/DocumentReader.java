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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class DocumentReader implements EntityResolver
{

    /**
     * @param url
     */
    public DocumentReader(URL url)
    {
        cacheDocument = false;
        entityResolver = this;
        document = null;
        documentURL = null;
        cacheDocument = false;
        document = null;
        entityResolver = this;
        documentURL = url;
    }

    /**
     * @param url
     * @param flag
     */
    public DocumentReader(URL url, boolean flag)
    {
        cacheDocument = false;
        entityResolver = this;
        document = null;
        documentURL = null;
        cacheDocument = flag;
        document = null;
        entityResolver = this;
        documentURL = url;
    }

    /**
     * @param url
     * @param flag
     * @param entityresolver
     */
    public DocumentReader(URL url, boolean flag, EntityResolver entityresolver)
    {
        cacheDocument = false;
        entityResolver = this;
        document = null;
        documentURL = null;
        cacheDocument = flag;
        entityResolver = entityresolver;
        document = null;
        documentURL = url;
    }

    /**
     * @return the document
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public Document getDocument() throws ParserConfigurationException, SAXException, IOException
    {
        if ((document != null) && cacheDocument) {
            return document;
        }
        else {
            DocumentBuilderFactory documentbuilderfactory = DocumentBuilderFactory.newInstance();
            documentbuilderfactory.setNamespaceAware(true);
            documentbuilderfactory.setValidating(false);
            DocumentBuilder documentbuilder = documentbuilderfactory.newDocumentBuilder();
            documentbuilder.setEntityResolver(entityResolver);
            document = documentbuilder.parse(documentURL.openStream());
            return document;
        }
    }

    /**
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
     *      java.lang.String)
     */
    public InputSource resolveEntity(String s, String s1)
    {
        return new InputSource(EMPTY_ENTITY);
    }

    private static final ByteArrayInputStream EMPTY_ENTITY = new ByteArrayInputStream(new byte[0]);
    private boolean                           cacheDocument;
    private EntityResolver                    entityResolver;
    private Document                          document;
    private URL                               documentURL;

}

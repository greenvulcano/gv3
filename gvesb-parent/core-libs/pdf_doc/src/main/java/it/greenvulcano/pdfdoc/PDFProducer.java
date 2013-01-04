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
package it.greenvulcano.pdfdoc;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Result;
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
 * @version 3.0.0 Apr 18, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class PDFProducer
{
    public static final String RES_FOP    = "fop.xsl";

    private FopFactory         fopFactory = FopFactory.newInstance();
    private Node               fopDocument;

    public PDFProducer(String documentId) throws Exception
    {
        DocumentSource docS = getDocumentSource(documentId);
        fopDocument = docS.getFop();
    }

    public PDFProducer(Node fopDocument) throws Exception
    {
        this.fopDocument = fopDocument;
    }

    public PDFProducer(File fopFile) throws Exception
    {
        this.fopDocument = XMLUtils.parseDOM_S(new FileInputStream(fopFile), false, true);
    }

    private static DocumentSource getDocumentSource(String documentId) throws Exception
    {
        Node configurationNode = XMLConfig.getNode("pdfDocuments.xml", "/documents/*[@type='document'][@id='"
                + documentId + "']");
        String className = XMLConfig.get(configurationNode, "@class", "");
        DocumentSource docS = (DocumentSource) Class.forName(className).newInstance();
        docS.init(configurationNode);

        return docS;
    }

    public void produceDocumentation(OutputStream out) throws Exception
    {
        InputStream xsl = getClass().getResourceAsStream(RES_FOP);
        if (xsl == null) {
            throw new Exception("Resource " + RES_FOP + " not found");
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer(new StreamSource(xsl));

        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(new DOMSource(fopDocument), res);
    }
}
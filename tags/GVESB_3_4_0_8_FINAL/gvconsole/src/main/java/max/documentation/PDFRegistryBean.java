/*
 * Copyright (c) 2004 Maxime Informatica s.n.c. - All right reserved
 *
 * Created on 18-nov-2004
 *
 */
package max.documentation;

import it.greenvulcano.configuration.XMLConfig;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import max.config.Config;

import org.w3c.dom.Node;

/**
 *
 */
public class PDFRegistryBean {
    // ----------------------------------------------------------------------------------------------
    // CONSTANTS
    // ----------------------------------------------------------------------------------------------

    public static final String SHOWDOCUMENTS_XSL = "showDocuments.xsl";
    public static final String PDF_DOCUMENTS     = "pdfDocuments.xml";

    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    private Transformer        transformer;

    // ----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    // ----------------------------------------------------------------------------------------------

    public PDFRegistryBean() throws Exception {
        TransformerFactory tfactory = TransformerFactory.newInstance();
        InputStream stream = getClass().getResourceAsStream(SHOWDOCUMENTS_XSL);
        Source source = new StreamSource(stream);
        transformer = tfactory.newTransformer(source);
        transformer.setParameter("generate", Config.get("", "max.site.root") + "/generatePDF");
    }

    // ----------------------------------------------------------------------------------------------
    // METHODS
    // ----------------------------------------------------------------------------------------------

    public String showDocuments() throws Exception {
        XMLConfig.reload(PDF_DOCUMENTS);
        Node documents = XMLConfig.getDocument(PDF_DOCUMENTS);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Source source = new DOMSource(documents);
        Result result = new StreamResult(out);
        transformer.transform(source, result);
        return new String(out.toByteArray());
    }

    // ----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    // ----------------------------------------------------------------------------------------------
}
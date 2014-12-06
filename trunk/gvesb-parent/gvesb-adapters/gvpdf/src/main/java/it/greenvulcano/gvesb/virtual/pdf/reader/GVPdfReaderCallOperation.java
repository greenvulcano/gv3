/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.virtual.pdf.reader;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.util.PDFTextStripper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @version 3.5.0 05/dec/2014
 * @author GreenVulcano Developer Team
 *
 */
public class GVPdfReaderCallOperation implements CallOperation
{
    private static final Logger logger      = GVLogger.getLogger(GVPdfReaderCallOperation.class);

    protected OperationKey      key         = null;

    /**
     * Source file name. Can contain placeholders that will be expanded at call
     * time.
     */
    private String              filename    = null;
   /**
    * First page to extract. Can contain placeholders that will be expanded at call
    * time.
    */
    private String              pageStart   = null;
   /**
    * Last page to extract. Can contain placeholders that will be expanded at call
    * time.
    */
    private String              pageEnd     = null;
   
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            filename = XMLConfig.get(node, "@fileName", "");
            pageStart = XMLConfig.get(node, "@pageStart", "-1");
            pageEnd = XMLConfig.get(node, "@pageEnd", "-1");
        }
        catch (Exception exc) {
            throw new InitializationException("GV_INIT_SERVICE_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
    }

    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException, 
            InterruptedException {
        InputStream in = null;
        PDDocument pdfDocument = null;
        XMLUtils parser = null;
        try {
            PropertiesHandler.enableExceptionOnErrors();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            if ("".equals(filename)) {
                in = new ByteArrayInputStream((byte[]) gvBuffer.getObject());                
            }
            else {
                String pdfFile = PropertiesHandler.expand(filename, params, gvBuffer);
                logger.debug("Reading Pdf file: " + pdfFile);
                in = new BufferedInputStream(new FileInputStream(pdfFile));
            }
            pdfDocument = PDDocument.load(in, true);
            parser = XMLUtils.getParserInstance();
            
            Document doc = parser.newDocument("pdf");
            Element root = doc.getDocumentElement();
            Element metadata = parser.insertElement(root, "metadata");
            
            setMetadata(pdfDocument, metadata, parser);
            
            int pStart = Integer.parseInt(PropertiesHandler.expand(pageStart, params, gvBuffer), 10);
            int pEnd = Integer.parseInt(PropertiesHandler.expand(pageEnd, params, gvBuffer), 10);

            if (pEnd < 0) {
                pEnd = pdfDocument.getNumberOfPages();
            }
            
            if ((pStart <= pEnd) && (pStart > 0)) {
                Element pages = parser.insertElement(root, "pages");
                parser.setAttribute(pages, "start", String.valueOf(pStart));
                parser.setAttribute(pages, "end", String.valueOf(pEnd));

                PDFTextStripper stripper = new PDFTextStripper("UTF-8");
                stripper.setForceParsing(true);
                stripper.setSortByPosition(false);
                stripper.setShouldSeparateByBeads(true);
    
                for (int pNum = pStart; pNum <= pEnd; pNum++) {
                    stripper.setStartPage(pNum);
                    stripper.setEndPage(pNum);
                    Element page = parser.insertElement(pages, "page");
                    parser.setAttribute(page, "num", String.valueOf(pNum));
                    String text = stripper.getText(pdfDocument);
                    parser.insertText(page, text);
                }
            }

            //gvBuffer.setObject(parser.serializeDOM(doc));
            gvBuffer.setObject(doc);
        }
        catch (Exception exc) {
            ThreadUtils.checkInterrupted(exc);
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);

        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
            XMLUtils.releaseParserInstance(parser);
            if(pdfDocument != null) {
                try {
                    pdfDocument.close();
                }
                catch (IOException exc) {
                 // do nothing
                }
            }
            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
        return gvBuffer;
    }


    @Override
    public void cleanUp()
    {
        // do nothing
    }

    @Override
    public void destroy()
    {
     // do nothing
    }

    /**
     * Return the alias for the given service
     *
     * @param data
     *        the input service data
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer data)
    {
        return data.getService();
    }


    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    public OperationKey getKey()
    {
        return key;
    }
    
    private void setMetadata(PDDocument document, Element metadata, XMLUtils parser) throws Exception
    {
        PDDocumentInformation info = document.getDocumentInformation();
        PDDocumentCatalog cat = document.getDocumentCatalog();
        PDMetadata md = cat.getMetadata();
        parser.insertText(parser.insertElement(metadata, "page-count"), String.valueOf(document.getNumberOfPages()));
        parser.insertText(parser.insertElement(metadata, "title"), info.getTitle());
        parser.insertText(parser.insertElement(metadata, "author"), info.getAuthor());
        parser.insertText(parser.insertElement(metadata, "subject"), info.getSubject());
        parser.insertText(parser.insertElement(metadata, "keywords"), info.getKeywords());
        parser.insertText(parser.insertElement(metadata, "creator"), info.getCreator());
        parser.insertText(parser.insertElement(metadata, "producer"), info.getProducer());
        String cDate = "";
        if (info.getCreationDate() != null) {
            cDate = DateUtils.dateToString(info.getCreationDate().getTime(), DateUtils.FORMAT_ISO8601_DATETIME);
        }
        parser.insertText(parser.insertElement(metadata, "creation-date"), cDate);
        String mDate = "";
        if (info.getModificationDate() != null) {
            mDate = DateUtils.dateToString(info.getModificationDate().getTime(), DateUtils.FORMAT_ISO8601_DATETIME);
        }
        parser.insertText(parser.insertElement(metadata, "modification-date"), mDate);
        String trap = "";
        if (info.getTrapped() != null) {
            trap = info.getTrapped().toLowerCase();
        }
        parser.insertText(parser.insertElement(metadata, "trapped"), trap);
        if(md != null) {
            String mdS = md.getInputStreamAsString();
            Node mdX = ((Document) parser.parseObject(mdS, false, true)).getDocumentElement();
            Node mdExtra = parser.insertElement(metadata, "extra");
            mdExtra.appendChild(mdExtra.getOwnerDocument().adoptNode(mdX));
        }
    }
}

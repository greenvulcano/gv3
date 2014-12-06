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
package tests.unit.vcl.pdf;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.pdf.reader.GVPdfReaderCallOperation;
import it.greenvulcano.util.bin.BinaryUtils;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Node;

/**
 * PdfReader Test class
 * 
 * 
 * @version 3.5.0  05/dec/2014
 * @author GreenVulcano Developer Team
 */
public class PdfReaderTestCase extends XMLTestCase
{

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testPdfFile_meta() throws Exception
    {
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/pdfreader-call[@name='read_pdf_file_meta']");
        GVPdfReaderCallOperation pdfr = new GVPdfReaderCallOperation();
        pdfr.init(node);

        GVBuffer gvBuffer = new GVBuffer("TEST", "PDFREADER-CALL");
        GVBuffer result = pdfr.perform(gvBuffer);
        
        String dom = XMLUtils.serializeDOM_S((Node) result.getObject());
        //System.out.println(dom);
        assertXMLEqual("read_pdf_file_meta failed", TextUtils.readFileFromCP("rtflib_meta.xml"), dom);
    }
    
    public void testPdfFile_full() throws Exception
    {
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/pdfreader-call[@name='read_pdf_file_full']");
        GVPdfReaderCallOperation pdfr = new GVPdfReaderCallOperation();
        pdfr.init(node);

        GVBuffer gvBuffer = new GVBuffer("TEST", "PDFREADER-CALL");
        GVBuffer result = pdfr.perform(gvBuffer);
        
        String dom = XMLUtils.serializeDOM_S((Node) result.getObject(), "UTF-8", false, true);
        //System.out.println(dom);
        assertXMLEqual("read_pdf_file_full failed", TextUtils.readFileFromCP("rtflib.xml"), dom);
    }
    
    public void testPdfBytes_full() throws Exception
    {
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/pdfreader-call[@name='read_pdf_bytes_full']");
        GVPdfReaderCallOperation pdfr = new GVPdfReaderCallOperation();
        pdfr.init(node);

        GVBuffer gvBuffer = new GVBuffer("TEST", "PDFREADER-CALL");
        gvBuffer.setObject(BinaryUtils.readFileAsBytesFromCP("rtflib.pdf"));
        GVBuffer result = pdfr.perform(gvBuffer);
        
        String dom = XMLUtils.serializeDOM_S((Node) result.getObject(), "UTF-8", false, true);
        //System.out.println(dom);
        assertXMLEqual("read_pdf_bytes_full failed", TextUtils.readFileFromCP("rtflib.xml"), dom);
    }
    
    public void testPdfFile_page() throws Exception
    {
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/pdfreader-call[@name='read_pdf_file_page']");
        GVPdfReaderCallOperation pdfr = new GVPdfReaderCallOperation();
        pdfr.init(node);

        GVBuffer gvBuffer = new GVBuffer("TEST", "PDFREADER-CALL");
        GVBuffer result = pdfr.perform(gvBuffer);
        
        String dom = XMLUtils.serializeDOM_S((Node) result.getObject(), "UTF-8", false, true);
        //System.out.println(dom);
        assertXMLEqual("read_pdf_file_page failed", TextUtils.readFileFromCP("ReadData.xml"), dom);
    }

}

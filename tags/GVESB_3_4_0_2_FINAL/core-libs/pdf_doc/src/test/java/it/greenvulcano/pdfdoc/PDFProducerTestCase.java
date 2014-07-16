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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @version 3.0.0 15/giu/2010
 * @author GreenVulcano Developer Team
 */
//@Ignore
public class PDFProducerTestCase extends TestCase
{

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        //FileUtils.deleteQuietly(new File("target/pdf"));
        FileUtils.forceMkdir(new File("target/pdf"));
        // windows system
        if (File.separator.equals("\\")) {
        	System.setProperty("gv.dtds.home", "/" + System.getProperty("gv.dtds.home"));
        }
        super.setUp();
    }

    /**
     * Test method for {@link it.greenvulcano.pdfdoc.PDFProducer#produceDocumentation(java.io.OutputStream)}.
     */
    @Test
    public void testGVCore() throws Exception
    {
        OutputStream out = new BufferedOutputStream(new FileOutputStream("target/pdf/GVCore.pdf"));
        try {
            PDFProducer pr = new PDFProducer("GVCoreReference");
            pr.produceDocumentation(out);
        }
        finally {
            out.close();
        }
        assertTrue("GVCore.pdf not generated", new File("target/pdf/GVCore.pdf").exists());
    }

    @Test
    public void testGVAdapters() throws Exception
    {
        OutputStream out = new BufferedOutputStream(new FileOutputStream("target/pdf/GVAdapters.pdf"));
        try {
            PDFProducer pr = new PDFProducer("GVAdaptersReference");
            pr.produceDocumentation(out);
        }
        finally {
            out.close();
        }
        assertTrue("GVAdapters.pdf not generated", new File("target/pdf/GVAdapters.pdf").exists());
    }

    @Test
    public void testGVSupport() throws Exception
    {
        OutputStream out = new BufferedOutputStream(new FileOutputStream("target/pdf/GVSupport.pdf"));
        try {
            PDFProducer pr = new PDFProducer("GVSupportReference");
            pr.produceDocumentation(out);
        }
        finally {
            out.close();
        }
        assertTrue("GVSupport.pdf not generated", new File("target/pdf/GVSupport.pdf").exists());
    }

    @Test
    public void testGVVariables() throws Exception
    {
        OutputStream out = new BufferedOutputStream(new FileOutputStream("target/pdf/GVVariables.pdf"));
        try {
            PDFProducer pr = new PDFProducer("GVVariablesReference");
            pr.produceDocumentation(out);
        }
        finally {
            out.close();
        }
        assertTrue("GVVariables.pdf not generated", new File("target/pdf/GVVariables.pdf").exists());
    }

}

package tests.vcl.mail;

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


import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.smtp.SMTPCallOperation;

import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.w3c.dom.Node;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

/**
 * @version 3.0.0 Apr 15, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class SMTPCallTestCase extends TestCase
{
    private static final String     TEST_SYSTEM  = "TEST_SYSTEM";
    private static final String     TEST_SERVICE = "TEST_SERVICE";
    private static final String     TEST_MESSAGE = "<ns1:headerNews xmlns:ns1=\"http://www.greenvulcano.it/gvesb\"><ns1:autsign>MAK</ns1:autsign><ns1:credate>2006-06-13T09:44:21.500Z</ns1:credate><ns1:takenr>1</ns1:takenr><ns1:version>1</ns1:version><ns1:title>Title</ns1:title><ns1:subtitle>Subtitle</ns1:subtitle><ns1:priority>1</ns1:priority><ns1:keytitle>Keytitle</ns1:keytitle><ns1:cresign>Cresign</ns1:cresign><ns1:categ>Cat</ns1:categ><ns1:subcateg>sub</ns1:subcateg><ns1:subjrefnr1>subj1</ns1:subjrefnr1><ns1:subjrefnr2>subj2</ns1:subjrefnr2><ns1:subjrefnr3>subj3</ns1:subjrefnr3><ns1:intaddr>INTADDR</ns1:intaddr><ns1:intqbx>QBX</ns1:intqbx><ns1:msg>MSG</ns1:msg><ns1:takeid>1</ns1:takeid><ns1:typesign>typeSign</ns1:typesign></ns1:headerNews>";

    private Context                 context;

    private static SimpleSmtpServer server       = SimpleSmtpServer.start(10025);


    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        context = new InitialContext();
    }

    /**
     * Tests email send
     *
     * @throws Exception
     *         if any error occurs
     */
    public final void testSendEmail() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//*[@name='SendEmail']");
        CallOperation op = new SMTPCallOperation();
        op.init(node);

        GVBuffer gvBuffer = new GVBuffer(TEST_SYSTEM, TEST_SERVICE);
        gvBuffer.setObject(TEST_MESSAGE);

        op.perform(gvBuffer);

        assertTrue(server.getReceivedEmailSize() == 1);
        Iterator<?> emailIter = server.getReceivedEmail();
        SmtpMessage email = (SmtpMessage) emailIter.next();
        emailIter.remove();
        System.out.println("---------MAIL DUMP: START");
        Iterator<?> hdrIter = email.getHeaderNames();
        while (hdrIter.hasNext()) {
            String hdr = (String) hdrIter.next();
            System.out.println("Header[" + hdr + "]: " + email.getHeaderValue(hdr));
        }
        System.out.println("Body: " + email.getBody());
        System.out.println("---------MAIL DUMP: END");
        assertTrue("Notifica SendEmail".equals(email.getHeaderValue("Subject")));
        assertTrue("1".equals(email.getHeaderValue("X-Priority")));
    }

    /**
     * Tests email send
     *
     * @throws Exception
     *         if any error occurs
     */
    public final void testSendEmailDynamicDest() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//*[@name='SendEmailDynamicDest']");
        CallOperation op = new SMTPCallOperation();
        op.init(node);

        GVBuffer gvBuffer = new GVBuffer(TEST_SYSTEM, TEST_SERVICE);
        gvBuffer.setObject(TEST_MESSAGE);

        gvBuffer.setProperty("GV_SMTP_TO", "g.dimaio@gv.com");
        gvBuffer.setProperty("GV_SMTP_CC", "g.dimaio2@gv.com");
        gvBuffer.setProperty("GV_SMTP_BCC", "g.dimaio3@gv.com");

        op.perform(gvBuffer);

        assertTrue(server.getReceivedEmailSize() == 1);
        Iterator<?> emailIter = server.getReceivedEmail();
        SmtpMessage email = (SmtpMessage) emailIter.next();
        emailIter.remove();
        System.out.println("---------MAIL DUMP: START");
        Iterator<?> hdrIter = email.getHeaderNames();
        while (hdrIter.hasNext()) {
            String hdr = (String) hdrIter.next();
            System.out.println("Header[" + hdr + "]: " + email.getHeaderValue(hdr));
        }
        System.out.println("Body: " + email.getBody());
        System.out.println("---------MAIL DUMP: END");
        assertTrue("Notifica SendEmailDynamicDest".equals(email.getHeaderValue("Subject")));
        assertTrue("g.dimaio@gv.com".equals(email.getHeaderValue("To")));
        assertTrue("g.dimaio2@gv.com".equals(email.getHeaderValue("Cc")));
    }

    /**
     * Tests email send
     *
     * @throws Exception
     *         if any error occurs
     */
    public final void testSendEmailBufferAttach() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//*[@name='SendEmailBufferAttach']");
        CallOperation op = new SMTPCallOperation();
        op.init(node);

        GVBuffer gvBuffer = new GVBuffer(TEST_SYSTEM, TEST_SERVICE);
        gvBuffer.setObject(TEST_MESSAGE);

        op.perform(gvBuffer);

        assertTrue(server.getReceivedEmailSize() == 1);
        Iterator<?> emailIter = server.getReceivedEmail();
        SmtpMessage email = (SmtpMessage) emailIter.next();
        emailIter.remove();
        System.out.println("---------MAIL DUMP: START");
        Iterator<?> hdrIter = email.getHeaderNames();
        while (hdrIter.hasNext()) {
            String hdr = (String) hdrIter.next();
            System.out.println("Header[" + hdr + "]: " + email.getHeaderValue(hdr));
        }
        System.out.println("Body: " + email.getBody());
        System.out.println("---------MAIL DUMP: END");
        assertTrue("Notifica SendEmailBufferAttach".equals(email.getHeaderValue("Subject")));
        // assertTrue(email.getBody().equals("Test Body"));
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        if (context != null) {
            try {
                context.close();
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}

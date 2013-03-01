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
package tests.unit.http;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.log.GVBufferDump;
import it.greenvulcano.gvesb.virtual.http.HTTPCallOperation;
import junit.framework.TestCase;

import org.w3c.dom.Node;

/**
 * @version 3.0.0 Jul 28, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class HttpCallTestCase extends TestCase
{
    static {
        new HttpServer(8888);
    }

    /**
     * @throws Exception
     *
     */
    public void testHEADMethod() throws Exception
    {
        Node node = XMLConfig.getNode("GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/http-call[@name='test_http_head']");
        HTTPCallOperation httpCall = new HTTPCallOperation();
        httpCall.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "HTTP-HEAD-CALL");
        GVBuffer result = httpCall.perform(gvBuffer);
        System.out.println(new GVBufferDump(result).toString());
        assertNotNull(result);
        assertEquals(200, Integer.parseInt(result.getProperty("GVHTTP_RESPONSE_STATUS")));
        assertNull(result.getObject());
    }

    /**
     * @throws Exception
     *
     */
    public void testGETMethod() throws Exception
    {
        Node node = XMLConfig.getNode("GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/http-call[@name='test_http_get']");
        HTTPCallOperation httpCall = new HTTPCallOperation();
        httpCall.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "HTTP-GET-CALL");
        GVBuffer result = httpCall.perform(gvBuffer);
        System.out.println(new GVBufferDump(result).toString());
        assertNotNull(result);
        assertEquals(200, Integer.parseInt(result.getProperty("GVHTTP_RESPONSE_STATUS")));
        assertNotNull(result.getObject());
    }

    /**
     * @throws Exception
     *
     */
    public void testPOSTMethod() throws Exception
    {
        Node node = XMLConfig.getNode("GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/http-call[@name='test_http_post']");
        HTTPCallOperation httpCall = new HTTPCallOperation();
        httpCall.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "HTTP-POST-CALL");
        gvBuffer.setObject("THIS MESSAGE IS SENT VIA MULTIPART".getBytes());
        GVBuffer result = httpCall.perform(gvBuffer);
        System.out.println(new GVBufferDump(result).toString());
        assertNotNull(result);
        assertEquals(200, Integer.parseInt(result.getProperty("GVHTTP_RESPONSE_STATUS")));
        assertNotNull(result.getObject());
    }

    /**
     * @throws Exception
     *
     */
    public void testSimplePOSTMethod() throws Exception
    {
        Node node = XMLConfig.getNode("GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/http-call[@name='test_http_simple_post']");
        HTTPCallOperation httpCall = new HTTPCallOperation();
        httpCall.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "HTTP-SIMPLE-POST-CALL");
        gvBuffer.setObject("THIS MESSAGE IS SENT VIA NORMAL POST".getBytes());
        GVBuffer result = httpCall.perform(gvBuffer);
        System.out.println(new GVBufferDump(result).toString());
        assertNotNull(result);
        assertEquals(200, Integer.parseInt(result.getProperty("GVHTTP_RESPONSE_STATUS")));
        assertNotNull(result.getObject());
        // SENDING A NEW SHORTER MESSAGE
        gvBuffer = new GVBuffer("TEST", "HTTP-SIMPLE-POST-CALL");
        gvBuffer.setObject("THIS MESSAGE IS SHORTER".getBytes());
        result = httpCall.perform(gvBuffer);
        System.out.println(new GVBufferDump(result).toString());
        assertNotNull(result);
        assertEquals(200, Integer.parseInt(result.getProperty("GVHTTP_RESPONSE_STATUS")));
        assertNotNull(result.getObject());
    }
}

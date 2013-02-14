/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package tests.unit.gvdebug;

import it.greenvulcano.core.debug.servlet.DebuggerServlet.DebugCommand;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.GreenVulcano;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import tests.unit.gvdebug.server.EmbeddedServer;

public class GVDebugTestCase extends TestCase
{

    private static final int    WEB_PORT     = 9091;

    private static final String CONTEXT_PATH = "/gvdebug";

    private static final String CONNECT_URL  = "http://localhost:" + WEB_PORT + CONTEXT_PATH + "/debugger";

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        EmbeddedServer.getInstance().start(WEB_PORT, CONTEXT_PATH);
        JMXEntryPoint.instance();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        EmbeddedServer.getInstance().getServer().stop();
    }

    private class EventPoller implements Runnable
    {
        private HttpClient hc;
        private boolean    fTerminated;

        public EventPoller(HttpClient hc)
        {
            this.hc = hc;
        }

        @Override
        public void run()
        {
            while (!isTerminated()) {
                try {

                    String eventResp = sendRequest(hc, DebugCommand.EVENT, null);
                    System.out.println("EVENT: " + eventResp);
                    Thread.sleep(1000);
                }
                catch (Exception e) {
                    terminated();
                }
            }
        }

        public boolean isTerminated()
        {
            return fTerminated;
        }

        private void terminated()
        {
            fTerminated = true;
        }

        public void terminate()
        {
            fTerminated = true;
            try {
                sendRequest(hc, DebugCommand.EXIT, null);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String sendRequest(final HttpClient httpClient, DebugCommand command, Map<String, String> params)
            throws Exception
    {
        PostMethod method = new PostMethod(CONNECT_URL);
        try {
            method.setParameter("debugOperation", command.toString());
            if (params != null && params.size() > 0) {
                for (Entry<String, String> e : params.entrySet()) {
                    method.setParameter(e.getKey(), e.getValue());
                }
            }
            int statusCode = httpClient.executeMethod(method);
            return parseResponse(statusCode, method);
        }
        finally {
            method.releaseConnection();
        }
    }

    private String parseResponse(int statusCode, HttpMethod method) throws Exception
    {
        assertEquals(HttpStatus.SC_OK, statusCode);
        XMLUtils xmlUtils = XMLUtils.getParserInstance();
        try {
            String responseBody = method.getResponseBodyAsString();
            Document document = xmlUtils.parseDOM(responseBody);
            assertEquals("OK", xmlUtils.get(document, "/GVDebugger/@result"));
            return responseBody;
        }
        finally {
            XMLUtils.releaseParserInstance(xmlUtils);
        }
    }

    /**
     * @throws Exception
     */
    public final void testDebugger() throws Exception
    {
        String serviceName = "TOUPPER";
        String opName = "RequestReply";
        /*
         * boolean run = true; Thread.sleep(30000);
         * System.out.println("Starting " + serviceName + " service.");
         * startService(serviceName, opName); while (run) { Thread.sleep(1000);
         * }
         */
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

        final HttpClient httpClient = new HttpClient(connectionManager);

        // Connect debugger
        Map<String, String> params = new HashMap<String, String>();
        params.put("service", serviceName);
        params.put("operation", opName);
        String responseBody = sendRequest(httpClient, DebugCommand.CONNECT, params);
        System.out.println(responseBody);

        final PostMethod method2 = new PostMethod(CONNECT_URL);
        Thread t = new Thread() {
            public void run()
            {
                method2.setParameter("debugOperation", "start");
                // method.setParameter("id", input.getId().toString());

                int statusCode = -1;
                try {
                    statusCode = httpClient.executeMethod(method2);
                }
                catch (HttpException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                assertEquals(HttpStatus.SC_OK, statusCode);
            };
        };
        t.start();
        System.out.println("SLEEP 2");
        Thread.sleep(2000);

        System.out.println("START SERVICE");
        GVBuffer input = startService(serviceName, opName);

        System.out.println("JOIN");
        t.join();

        responseBody = new String(method2.getResponseBody());
        method2.releaseConnection();
        System.out.println(responseBody);
        new Thread(new EventPoller(httpClient)).start();

        XMLUtils xmlUtils = XMLUtils.getParserInstance();
        try {
            Document document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            NodeList threads = xmlUtils.selectNodeList(document, "/GVDebugger/Service/Threads/Thread");
            assertNotNull(threads);
            assertFalse(threads.getLength() == 0);

            String threadName = threads.item(0).getAttributes().getNamedItem("name").getNodeValue();

            // step
            params.clear();
            params.put("threadName", threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STEP_OVER, params);
            System.out.println(responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
        }
        finally {
            XMLUtils.releaseParserInstance(xmlUtils);
        }
    }

    private GVBuffer startService(String serviceName, String opName) throws Exception
    {
        final GVBuffer gvBuffer = new GVBuffer("GVESB", serviceName);
        gvBuffer.setObject("test debugger");
        new Thread() {
            public void run()
            {
                try {
                    GreenVulcano greenVulcano = new GreenVulcano();
                    greenVulcano.requestReply(gvBuffer);
                }
                catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            };
        }.start();
        return gvBuffer;
    }

}

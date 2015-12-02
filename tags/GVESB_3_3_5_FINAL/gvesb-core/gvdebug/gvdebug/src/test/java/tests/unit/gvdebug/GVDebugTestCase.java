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
import it.greenvulcano.core.debug.servlet.DebuggerServlet.DebugKey;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.GreenVulcano;
import it.greenvulcano.gvesb.core.debug.DebuggerAdapter;
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
import org.w3c.dom.Node;
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
            method.setParameter(DebugKey.debugOperation.name(), command.toString());
            if (command == DebugCommand.CONNECT) {
                params.put(DebugKey.debuggerVersion.name(), DebuggerAdapter.DEBUGGER_VERSION.toString());
            }
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
        System.out.println("-------------------- BEGIN TOUPPER");
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
        params.put(DebugKey.service.name(), serviceName);
        params.put(DebugKey.operation.name(), opName);
        String responseBody = sendRequest(httpClient, DebugCommand.CONNECT, params);
        System.out.println("CONNECT response: " + responseBody);

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
        System.out.println("START response: " + responseBody);
        EventPoller poller = new EventPoller(httpClient);
        new Thread(poller).start();

        XMLUtils xmlUtils = XMLUtils.getParserInstance();
        try {
            Document document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            NodeList threads = xmlUtils.selectNodeList(document, "/GVDebugger/Service/Threads/Thread");
            assertNotNull(threads);
            assertFalse(threads.getLength() == 0);

            String threadName = threads.item(0).getAttributes().getNamedItem("name").getNodeValue();

            // data
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STACK, params);
            System.out.println("STACK response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            assertEquals(xmlUtils.get(document, "/GVDebugger/FrameStack/Frame/@flow_node"), "request");

            Node frame = xmlUtils.selectSingleNode(document, "/GVDebugger/FrameStack/Frame");
            Node env = xmlUtils.selectSingleNode(frame, "Variables/Variable[@name='input_test']");
            assertNotNull(env);
            // variable
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            params.put(DebugKey.varEnv.name(), xmlUtils.get(env, "@name"));
            params.put(DebugKey.varID.name(), xmlUtils.get(env, "@id"));
            params.put(DebugKey.stackFrame.name(), xmlUtils.get(frame, "@name"));
            responseBody = sendRequest(httpClient, DebugCommand.VAR, params);
            System.out.println("VAR(input_test) response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");

            // step
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STEP_OVER, params);
            System.out.println("STEP_OVER response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            // data
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STACK, params);
            System.out.println("STACK response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            assertEquals(xmlUtils.get(document, "/GVDebugger/FrameStack/Frame/@flow_node"), "check_status");

            frame = xmlUtils.selectSingleNode(document, "/GVDebugger/FrameStack/Frame");
            env = xmlUtils.selectSingleNode(frame, "Variables/Variable[@name='output_test']");
            assertNotNull(env);
            // variable
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            params.put(DebugKey.varEnv.name(), xmlUtils.get(env, "@name"));
            params.put(DebugKey.varID.name(), xmlUtils.get(env, "@id"));
            params.put(DebugKey.stackFrame.name(), xmlUtils.get(frame, "@name"));
            responseBody = sendRequest(httpClient, DebugCommand.VAR, params);
            System.out.println("VAR(output_test) response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");

            // step
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STEP_OVER, params);
            System.out.println("STEP_OVER response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            // data
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STACK, params);
            System.out.println("STACK response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            assertEquals(xmlUtils.get(document, "/GVDebugger/FrameStack/Frame/@flow_node"), "return_status");

            // step
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STEP_OVER, params);
            System.out.println("STEP_OVER response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            // data
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STACK, params);
            System.out.println("STACK response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
        }
        finally {
            if (poller != null) {
                poller.terminate();
            }
            XMLUtils.releaseParserInstance(xmlUtils);
        }
        System.out.println("-------------------- END TOUPPER");
    }


    /**
     * @throws Exception
     */
    public final void testDebuggerExc() throws Exception
    {
        System.out.println("\n\n-------------------- BEGIN TOUPPER_EXC");
        String serviceName = "TOUPPER_EXC";
        String opName = "RequestReply";
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

        final HttpClient httpClient = new HttpClient(connectionManager);

        // Connect debugger
        Map<String, String> params = new HashMap<String, String>();
        params.put(DebugKey.service.name(), serviceName);
        params.put(DebugKey.operation.name(), opName);
        String responseBody = sendRequest(httpClient, DebugCommand.CONNECT, params);
        System.out.println("CONNECT response: " + responseBody);

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
        System.out.println("START response: " + responseBody);
        EventPoller poller = new EventPoller(httpClient);
        new Thread(poller).start();

        XMLUtils xmlUtils = XMLUtils.getParserInstance();
        try {
            Document document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            NodeList threads = xmlUtils.selectNodeList(document, "/GVDebugger/Service/Threads/Thread");
            assertNotNull(threads);
            assertFalse(threads.getLength() == 0);

            String threadName = threads.item(0).getAttributes().getNamedItem("name").getNodeValue();

            // data
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STACK, params);
            System.out.println("STACK response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            assertEquals(xmlUtils.get(document, "/GVDebugger/FrameStack/Frame/@flow_node"), "request");

            Node frame = xmlUtils.selectSingleNode(document, "/GVDebugger/FrameStack/Frame");
            Node env = xmlUtils.selectSingleNode(frame, "Variables/Variable[@name='input_test']");
            assertNotNull(env);
            // variable
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            params.put(DebugKey.varEnv.name(), xmlUtils.get(env, "@name"));
            params.put(DebugKey.varID.name(), xmlUtils.get(env, "@id"));
            params.put(DebugKey.stackFrame.name(), xmlUtils.get(frame, "@name"));
            responseBody = sendRequest(httpClient, DebugCommand.VAR, params);
            System.out.println("VAR(input_test) response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");

            // step
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STEP_OVER, params);
            System.out.println("STEP_OVER response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            // data
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STACK, params);
            System.out.println("STACK response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            assertEquals(xmlUtils.get(document, "/GVDebugger/FrameStack/Frame/@flow_node"), "check_status");

            frame = xmlUtils.selectSingleNode(document, "/GVDebugger/FrameStack/Frame");
            env = xmlUtils.selectSingleNode(frame, "Variables/Variable[@name='output_test']");
            assertNotNull(env);
            // variable
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            params.put(DebugKey.varEnv.name(), xmlUtils.get(env, "@name"));
            params.put(DebugKey.varID.name(), xmlUtils.get(env, "@id"));
            params.put(DebugKey.stackFrame.name(), xmlUtils.get(frame, "@name"));
            responseBody = sendRequest(httpClient, DebugCommand.VAR, params);
            System.out.println("VAR(output_test) response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");

            // step
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STEP_OVER, params);
            System.out.println("STEP_OVER response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            // data
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STACK, params);
            System.out.println("STACK response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            assertEquals(xmlUtils.get(document, "/GVDebugger/FrameStack/Frame/@flow_node"), "return_error");

            // step
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STEP_OVER, params);
            System.out.println("STEP_OVER response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            // data
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STACK, params);
            System.out.println("STACK response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
        finally {
            if (poller != null) {
                poller.terminate();
            }
            XMLUtils.releaseParserInstance(xmlUtils);
        }
        System.out.println("-------------------- END TOUPPER_EXC");
    }

    /**
     * @throws Exception
     */
    public final void testDebuggerDom() throws Exception
    {
        System.out.println("\n\n-------------------- BEGIN TEST_DOM");
        String serviceName = "TEST_DATA";
        String opName = "RequestReply";
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

        final HttpClient httpClient = new HttpClient(connectionManager);

        // Connect debugger
        Map<String, String> params = new HashMap<String, String>();
        params.put(DebugKey.service.name(), serviceName);
        params.put(DebugKey.operation.name(), opName);
        String responseBody = sendRequest(httpClient, DebugCommand.CONNECT, params);
        System.out.println("CONNECT response: " + responseBody);

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
        GVBuffer input = startDomService(serviceName, opName);

        System.out.println("JOIN");
        t.join();

        responseBody = new String(method2.getResponseBody());
        method2.releaseConnection();
        System.out.println("START response: " + responseBody);
        EventPoller poller = new EventPoller(httpClient);
        new Thread(poller).start();

        XMLUtils xmlUtils = XMLUtils.getParserInstance();
        try {
            Document document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            NodeList threads = xmlUtils.selectNodeList(document, "/GVDebugger/Service/Threads/Thread");
            assertNotNull(threads);
            assertFalse(threads.getLength() == 0);

            String threadName = threads.item(0).getAttributes().getNamedItem("name").getNodeValue();

            // data
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STACK, params);
            System.out.println("STACK response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            assertEquals(xmlUtils.get(document, "/GVDebugger/FrameStack/Frame/@flow_node"), "check");

            Node frame = xmlUtils.selectSingleNode(document, "/GVDebugger/FrameStack/Frame");
            Node env = xmlUtils.selectSingleNode(frame, "Variables/Variable[@name='input_test']");
            assertNotNull(env);
            // variable
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            params.put(DebugKey.varEnv.name(), xmlUtils.get(env, "@name"));
            params.put(DebugKey.varID.name(), xmlUtils.get(env, "@id"));
            params.put(DebugKey.stackFrame.name(), xmlUtils.get(frame, "@name"));
            responseBody = sendRequest(httpClient, DebugCommand.VAR, params);
            System.out.println("VAR(input_test) response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");

            // step
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STEP_OVER, params);
            System.out.println("STEP_OVER response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
        }
        finally {
            if (poller != null) {
                poller.terminate();
            }
            XMLUtils.releaseParserInstance(xmlUtils);
        }
        System.out.println("-------------------- END TEST_DOM");
    }

    /**
     * @throws Exception
     */
    public final void testDebuggerByteArray() throws Exception
    {
        System.out.println("\n\n-------------------- BEGIN TEST_B[]");
        String serviceName = "TEST_DATA";
        String opName = "RequestReply";
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

        final HttpClient httpClient = new HttpClient(connectionManager);

        // Connect debugger
        Map<String, String> params = new HashMap<String, String>();
        params.put(DebugKey.service.name(), serviceName);
        params.put(DebugKey.operation.name(), opName);
        String responseBody = sendRequest(httpClient, DebugCommand.CONNECT, params);
        System.out.println("CONNECT response: " + responseBody);

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
        GVBuffer input = startBArrService(serviceName, opName);

        System.out.println("JOIN");
        t.join();

        responseBody = new String(method2.getResponseBody());
        method2.releaseConnection();
        System.out.println("START response: " + responseBody);
        EventPoller poller = new EventPoller(httpClient);
        new Thread(poller).start();

        XMLUtils xmlUtils = XMLUtils.getParserInstance();
        try {
            Document document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            NodeList threads = xmlUtils.selectNodeList(document, "/GVDebugger/Service/Threads/Thread");
            assertNotNull(threads);
            assertFalse(threads.getLength() == 0);

            String threadName = threads.item(0).getAttributes().getNamedItem("name").getNodeValue();

            // data
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STACK, params);
            System.out.println("STACK response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
            assertEquals(xmlUtils.get(document, "/GVDebugger/FrameStack/Frame/@flow_node"), "check");

            Node frame = xmlUtils.selectSingleNode(document, "/GVDebugger/FrameStack/Frame");
            Node env = xmlUtils.selectSingleNode(frame, "Variables/Variable[@name='input_test']");
            assertNotNull(env);
            // variable
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            params.put(DebugKey.varEnv.name(), xmlUtils.get(env, "@name"));
            params.put(DebugKey.varID.name(), xmlUtils.get(env, "@id"));
            params.put(DebugKey.stackFrame.name(), xmlUtils.get(frame, "@name"));
            responseBody = sendRequest(httpClient, DebugCommand.VAR, params);
            System.out.println("VAR(input_test) response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");

            // step
            params.clear();
            params.put(DebugKey.threadName.name(), threadName);
            responseBody = sendRequest(httpClient, DebugCommand.STEP_OVER, params);
            System.out.println("STEP_OVER response: " + responseBody);
            document = xmlUtils.parseDOM(responseBody);
            assertEquals(xmlUtils.get(document, "/GVDebugger/@result"), "OK");
        }
        finally {
            if (poller != null) {
                poller.terminate();
            }
            XMLUtils.releaseParserInstance(xmlUtils);
        }
        System.out.println("-------------------- END TEST_B[]");
    }

    private GVBuffer startService(final String serviceName, final String opName) throws Exception
    {
        final GVBuffer gvBuffer = new GVBuffer("GVESB", serviceName);
        gvBuffer.setObject("test debugger");
        gvBuffer.setProperty("PROP_A", "VALUE_A");
        gvBuffer.setProperty("PROP_B", "VALUE_B");
        new Thread() {
            public void run()
            {
                try {
                    GreenVulcano greenVulcano = new GreenVulcano();
                    greenVulcano.forward(gvBuffer, opName);
                }
                catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            };
        }.start();
        return gvBuffer;
    }

    private GVBuffer startDomService(final String serviceName, final String opName) throws Exception
    {
        final GVBuffer gvBuffer = new GVBuffer("GVESB", serviceName);
        gvBuffer.setObject(XMLUtils.parseObject_S(
                "<doc>\n\t<element>pippo</element>\n\t<element>pippo</element>\n</doc>", false, true));
        gvBuffer.setProperty("PROP_A", "VALUE_A");
        gvBuffer.setProperty("PROP_B", "VALUE_B");
        new Thread() {
            public void run()
            {
                try {
                    GreenVulcano greenVulcano = new GreenVulcano();
                    greenVulcano.forward(gvBuffer, opName);
                }
                catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            };
        }.start();
        return gvBuffer;
    }

    private GVBuffer startBArrService(final String serviceName, final String opName) throws Exception
    {
        final GVBuffer gvBuffer = new GVBuffer("GVESB", serviceName);
        gvBuffer.setObject("<doc>\n\t<element>pippo</element>\n\t<element>pippo</element>\n</doc>".getBytes());
        gvBuffer.setProperty("PROP_A", "VALUE_A");
        gvBuffer.setProperty("PROP_B", "VALUE_B");
        new Thread() {
            public void run()
            {
                try {
                    GreenVulcano greenVulcano = new GreenVulcano();
                    greenVulcano.forward(gvBuffer, opName);
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

/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package tests.unit.vcl.axis2.rest;

import java.io.File;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.io.FileUtils;
import org.jaxen.JaxenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.ws.rest.RestCallOperation;
import junit.framework.TestCase;

/**
 * @version 3.4.0 Jul 19, 2013
 * @author GreenVulcano Developer Team
 *
 */
public class Axis2RESTPluginTestCase extends TestCase
{
    private Context initialContext;

    /**
     *
     */
    @Override
	public void setUp() throws Exception
    {
        File repoSrc = new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
                + File.separator + "resources" + File.separator + "webservices");
        File repoDest = new File(System.getProperty("user.dir") + File.separator + "target" + File.separator
                + "webservices");

        File xmlSrc = new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
                + File.separator + "resources" + File.separator + "axis2.xml");
        File xmlDest = new File(System.getProperty("user.dir") + File.separator + "target" + File.separator
                + "xmlconfig" + File.separator + "axis2.xml");

        FileUtils.deleteQuietly(repoDest);
        FileUtils.deleteQuietly(xmlDest);
        FileUtils.forceMkdir(new File(System.getProperty("user.dir") + File.separator + "target/webservices"));
        FileUtils.forceMkdir(new File(System.getProperty("user.dir") + File.separator + "target/xmlconfig"));
        FileUtils.copyDirectory(repoSrc, repoDest);
        FileUtils.copyFile(xmlSrc, xmlDest);
        this.initialContext = new InitialContext();
    }

    /**
     *
     */
    @Override
	public void tearDown() throws Exception
    {
        this.initialContext.close();
    }

    /**
     * @throws Exception
     */
    public final void testRestCallSimple() throws Exception
    {
        RestCallOperation wsCall = new RestCallOperation();
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/rest-call[@name='test-customer' and @type='call']");
        wsCall.init(node);
        GVBuffer gvBuffer = getTestBuffer(null);
        gvBuffer.setProperty("customer_id", "1");
        GVBuffer output = wsCall.perform(gvBuffer);
        checkReturn("1", "Mario", "customer", output);
        wsCall.cleanUp();
    }

    /**
     * @throws Exception
     */
    public final void _testRestCRUD() throws Exception
    {
        RestCallOperation wsCall = new RestCallOperation();

        Node node = null;
        GVBuffer gvBuffer = null;
        GVBuffer output = null;

        node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/rest-call[@name='test-customer2' and @type='call']");
        wsCall.init(node);
        gvBuffer = getTestBuffer(createGetMessage());
        output = wsCall.perform(gvBuffer);
        checkReturn("1", "Mario", "customers/customer", output);
        wsCall.cleanUp();

        node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/rest-call[@name='test-add' and @type='call']");
        wsCall.init(node);
        gvBuffer = getTestBuffer(createAddMessage());
        output = wsCall.perform(gvBuffer);
        checkReturn("2", "Gino", "customer", output);
        wsCall.cleanUp();

        node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/rest-call[@name='test-update' and @type='call']");
        wsCall.init(node);
        gvBuffer = getTestBuffer(createUpdateMessage());
        gvBuffer.setProperty("customer_id", "2");
        output = wsCall.perform(gvBuffer);
        assertNotNull(output);
        wsCall.cleanUp();

        node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/rest-call[@name='test-del' and @type='call']");
        wsCall.init(node);
        gvBuffer = getTestBuffer(null);
        gvBuffer.setProperty("customer_id", "2");
        output = wsCall.perform(gvBuffer);
        assertNotNull(output);
        wsCall.cleanUp();

        node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/rest-call[@name='test-customers' and @type='call']");
        wsCall.init(node);
        gvBuffer = getTestBuffer(createGetMessage());
        output = wsCall.perform(gvBuffer);
        checkReturn("1", "Mario", "customers/customer", output);
        wsCall.cleanUp();

        wsCall.destroy();
    }

    /**
     * @throws Exception
     */
    public final void _testRestCRUDJSON() throws Exception
    {
        RestCallOperation wsCall = new RestCallOperation();

        Node node = null;
        GVBuffer gvBuffer = null;
        GVBuffer output = null;

        node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/rest-call[@name='test-json-get' and @type='call']");
        wsCall.init(node);
        gvBuffer = getTestBuffer(createGetMessage());
        output = wsCall.perform(gvBuffer);
        checkReturn("1", "Mario", "customers/customer", output);
        wsCall.cleanUp();

        node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/rest-call[@name='test-json-add' and @type='call']");
        wsCall.init(node);
        gvBuffer = getTestBuffer(createAddMessage());
        output = wsCall.perform(gvBuffer);
        checkReturn("2", "Gino", "customer", output);
        wsCall.cleanUp();

        node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/rest-call[@name='test-json-update' and @type='call']");
        wsCall.init(node);
        gvBuffer = getTestBuffer(createUpdateMessage());
        gvBuffer.setProperty("customer_id", "2");
        output = wsCall.perform(gvBuffer);
        assertNotNull(output);
        wsCall.cleanUp();

        node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/rest-call[@name='test-json-del' and @type='call']");
        wsCall.init(node);
        gvBuffer = getTestBuffer(null);
        gvBuffer.setProperty("customer_id", "2");
        output = wsCall.perform(gvBuffer);
        assertNotNull(output);
        wsCall.cleanUp();

        node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/rest-call[@name='test-json-customers' and @type='call']");
        wsCall.init(node);
        gvBuffer = getTestBuffer(createGetMessage());
        output = wsCall.perform(gvBuffer);
        checkReturn("1", "Mario", "customers/customer", output);
        wsCall.cleanUp();

        wsCall.destroy();
    }

    private void checkReturn(String expId, String expName, String context, GVBuffer output) throws JaxenException
    {
        assertNotNull(output);
        MessageContext mc = (MessageContext) output.getObject();
        String id = null;
        String name = null;
        SOAPBody body = mc.getEnvelope().getBody();
        AXIOMXPath xpath = new AXIOMXPath(context + "/id");
        OMElement idEl = (OMElement) xpath.selectSingleNode(body);
        if (idEl != null) {
            id = idEl.getText();
        }
        xpath = new AXIOMXPath(context + "/name");
        OMElement nameEl = (OMElement) xpath.selectSingleNode(body);
        if (nameEl != null) {
            name = nameEl.getText();
        }
        assertEquals(expId, id);
        assertEquals(expName, name);

    }

    private GVBuffer getTestBuffer(Object object) throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("TEST", "WS-TEST-CALL");
        gvBuffer.setObject(object);
        return gvBuffer;
    }

    private Element createGetMessage() throws Exception
    {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = documentFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element root = doc.createElement("customers");
        doc.appendChild(root);
        Element arg0 = doc.createElement("id");
        root.appendChild(arg0);
        arg0.appendChild(doc.createTextNode("1"));
        return root;
    }

    private Element createUpdateMessage() throws Exception
    {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = documentFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element customer = doc.createElement("customer");
        doc.appendChild(customer);
        Element id = doc.createElement("id");
        customer.appendChild(id);
        id.appendChild(doc.createTextNode("2"));
        Element name = doc.createElement("name");
        customer.appendChild(name);
        name.appendChild(doc.createTextNode("Paolo"));
        return customer;
    }

    private Element createAddMessage() throws Exception
    {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = documentFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element customer = doc.createElement("customer");
        doc.appendChild(customer);
        Element id = doc.createElement("id");
        customer.appendChild(id);
        id.appendChild(doc.createTextNode("0"));
        Element name = doc.createElement("name");
        customer.appendChild(name);
        name.appendChild(doc.createTextNode("Gino"));
        return customer;
    }

}

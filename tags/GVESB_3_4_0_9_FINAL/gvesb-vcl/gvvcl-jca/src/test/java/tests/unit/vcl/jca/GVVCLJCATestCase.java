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
package tests.unit.vcl.jca;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.jca.JCACallOperation;
import it.greenvulcano.gvesb.virtual.jca.xml.JCAXML;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Mar 23, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class GVVCLJCATestCase extends TestCase
{
    /**
     *
     */
    private static final String EXPECTED_RESULT = "test write on file using JCA";

    /**
     * @throws Exception
     */
    public final void testGVJCA() throws Exception
    {
        // Write to EIS
        JCACallOperation jcaCall = new JCACallOperation();
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/j2ee-jca-call[@name='test_jca_call_write' and @type='call']");
        jcaCall.init(node);
        GVBuffer gvBuffer = getTestBuffer();
        jcaCall.perform(gvBuffer);

        // Read from EIS
        jcaCall = new JCACallOperation();
        node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/j2ee-jca-call[@name='test_jca_call_read' and @type='call']");
        jcaCall.init(node);
        gvBuffer = getTestBuffer();
        GVBuffer result = jcaCall.perform(gvBuffer);
        Node response = (Node) result.getObject();

        assertEquals(EXPECTED_RESULT, XMLConfig.get(response, "/jca:record/jca:list/jca:item/text()"));
    }

    private GVBuffer getTestBuffer() throws Exception
    {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = documentFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Node root = doc.createElementNS(JCAXML.NAMESPACE_URI, JCAXML.ROOT_NAME);
        doc.appendChild(root);
        Element list = doc.createElementNS(JCAXML.NAMESPACE_URI, "list");
        list.setAttribute("name", "InputRecord");
        root.appendChild(list);
        Element item = doc.createElementNS(JCAXML.NAMESPACE_URI, "item");
        list.appendChild(item);
        item.setAttribute("type", "string");
        item.appendChild(doc.createTextNode(EXPECTED_RESULT));
        GVBuffer gvBuffer = new GVBuffer("TEST", "J2EE-JCA-TEST-CALL");
        gvBuffer.setObject(root);
        return gvBuffer;
    }

}

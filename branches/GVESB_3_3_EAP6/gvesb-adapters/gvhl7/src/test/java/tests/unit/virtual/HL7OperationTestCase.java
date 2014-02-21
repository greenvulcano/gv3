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
package tests.unit.virtual;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.hl7.HL7CallOperation;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Node;

import ca.uhn.hl7v2.app.SimpleServer;
import ca.uhn.hl7v2.llp.LowerLayerProtocol;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * @version 3.0.0 29/set/2010
 * @author GreenVulcano Developer Team
 */
public class HL7OperationTestCase extends TestCase
{
    private static final String TEST_FILE_RESOURCES       = System.getProperty("user.dir") + File.separatorChar
                                                            + "target" + File.separator + "test-classes";
    private static final String  HL72XML_IN_FILE          = "hl72xml_out.xml";
    private static final String  HL7_ID_FILE              = "hl7_id_file.cfg";
    /*private static final String  HL7_RESPONSE             = "MSH|^~\\&|||||20100929111354.166+0100||ACK|1|P|2.2\n"
                                                          + "MSA|AR|123456|No appropriate destination could be found to which this message could be routed.\n"
                                                          + "ERR|^^^207&Application Internal Error&HL70357\n";
    */
    private SimpleServer server = null;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        assertTrue("System property 'it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath' not set.",
                System.getProperty("it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath") != null);
        if (server == null) {
            server = new SimpleServer(8282, LowerLayerProtocol.makeLLP(), new PipeParser());
            server.loadApplicationsFromFile(new File(TEST_FILE_RESOURCES, HL7_ID_FILE));
            server.start();
        }
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    /**
     * @throws Exception
     */
    public void testCall() throws Exception
    {
        String xmlData = FileUtils.readFileToString(new File(TEST_FILE_RESOURCES, HL72XML_IN_FILE));
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/hl7-call[@name='test_hl7']");
        HL7CallOperation hl7c = new HL7CallOperation();
        hl7c.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "HL7-CALL");
        gvBuffer.setObject(xmlData);
        GVBuffer result = hl7c.perform(gvBuffer);
        System.out.println(result);
        //assertEquals(HL7_RESPONSE, result.getObject());
    }
}

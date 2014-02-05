/*
 * Copyright (c) 2009-2011 GreenVulcano ESB Open Source Project. All rights
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
package tests.unit.jmx;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.jmx.JMXCallOperation;
import it.greenvulcano.jmx.JMXEntryPoint;

import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.w3c.dom.Node;

/**
 * @version 3.1.0 May 02, 2011
 * @author GreenVulcano Developer Team
 */
public class JMXPluginTestCase extends TestCase
{
    private static JMXEntryPoint jmx                = null;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        if (jmx == null) {
            jmx = JMXEntryPoint.instance();
            MBeanServer server = jmx.getServer();

            Set<ObjectName> set = server.queryNames(new ObjectName("GreenVulcano:*"), null);
            for (ObjectName objectName : set) {
                System.out.println(objectName);
            }
            assertTrue("No JMX object returned in GreenVulcano domain", set != null && !set.isEmpty());
        }
    }

    /**
     *
     */
    public void testJMXInvokeMethod()
    {
        try {

            Node node = XMLConfig.getNode("GVSystems.xml",
                    "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/jmx-call[@name='TestJMXInvoke']");
            JMXCallOperation reader = new JMXCallOperation();
            reader.init(node);
            GVBuffer gvBuffer = new GVBuffer("TEST", "JMX-CALL");
            gvBuffer.setObject("GVAdapters.xml");
            GVBuffer result = reader.perform(gvBuffer);
            assertNull(result.getObject());

        }
        catch (Exception exc) {
            fail("Exception occurred while testing JMX configuration: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

    /**
     *
     */
    public void testJMXGetMethod()
    {
        try {
            Node node = XMLConfig.getNode(
                    "GVSystems.xml",
                    "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/jmx-call[@name='TestJMXGetAttribute']");
            JMXCallOperation reader = new JMXCallOperation();
            reader.init(node);
            GVBuffer gvBuffer = new GVBuffer("TEST", "JMX-CALL");
            gvBuffer.setObject("GVAdapters.xml");
            GVBuffer result = reader.perform(gvBuffer);
            assertNotNull(result.getObject());
            Object[] objs = (Object[]) result.getObject();
            for (Object obj : objs) {
                String[] files = (String[]) obj;
                for (String file : files) {
                    System.out.println(file);
                }

            }
        }
        catch (Exception exc) {
            fail("Exception occurred while testing JMX configuration: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

    /**
     *
     */
    public void testJMXSetMethod()
    {
        try {
            Node node = XMLConfig.getNode(
                    "GVSystems.xml",
                    "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/jmx-call[@name='TestJMXSetAttribute']");
            JMXCallOperation reader = new JMXCallOperation();
            reader.init(node);
            GVBuffer gvBuffer = new GVBuffer("TEST", "JMX-CALL");
            gvBuffer.setObject("GVAdapters.xml");
            try {
                reader.perform(gvBuffer);
                fail("Set on non writeable attribute should throw an exception.");
            }
            catch (CallException ce) {
                assertTrue(ce.getCause() instanceof AttributeNotFoundException);
            }
        }
        catch (Exception exc) {
            fail("Exception occurred while testing JMX configuration: " + exc.getMessage());
            exc.printStackTrace();
        }
    }
}

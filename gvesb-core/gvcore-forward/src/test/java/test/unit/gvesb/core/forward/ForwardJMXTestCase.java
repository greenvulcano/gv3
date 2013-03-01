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
package test.unit.gvesb.core.forward;

import it.greenvulcano.jmx.JMXEntryPoint;

import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQXAConnectionFactory;

/**
 * @version 3.2.0 18/gen/2012
 * @author GreenVulcano Developer Team
 */
public class ForwardJMXTestCase extends TestCase
{
    private Context context;

    @Override
    protected void setUp() throws Exception
    {
        context = new InitialContext();

        ConnectionFactory connFactory = new ActiveMQConnectionFactory("vm://localhost?async=true");
        context.rebind("Resource/queueConnectionFactory", connFactory);
        connFactory = new ActiveMQXAConnectionFactory("vm://localhost?async=true");
        context.rebind("Resource/xaQueueConnectionFactory", connFactory);

        JMXEntryPoint.instance();
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

    /**
     * @throws Exception
     */
    public void testForwardJMX() throws Exception
    {
        MBeanServer server = JMXEntryPoint.instance().getServer();
        Set<ObjectName> set = server.queryNames(
                new ObjectName(
                        "GreenVulcano:Type=JMSForwardListenerPoolInfo,Component=JMSForwardListenerPoolInfo,Forward=FWD_TOUPPER_A,*"),
                null);
        assertTrue(
                "No JMSForwardListenerPool info available in JMX object returned from GreenVulcano domain for Forward=FWD_TOUPPER_A",
                (set != null) && !set.isEmpty());
        ObjectName poolInfo = set.iterator().next();

        assertEquals(true, server.getAttribute(poolInfo, "active"));
        assertEquals(3, server.getAttribute(poolInfo, "initialSize"));
        assertEquals(5, server.getAttribute(poolInfo, "maximumSize"));
        assertTrue(((Integer) server.getAttribute(poolInfo, "maxCreated")).intValue() >= 1);
        assertTrue(((Integer) server.getAttribute(poolInfo, "pooledCount")).intValue() >= 1);
        assertTrue(((Integer) server.getAttribute(poolInfo, "workingCount")).intValue() >= 0);
        assertEquals(true, server.getAttribute(poolInfo, "dumpMessage"));
        assertEquals(true, server.getAttribute(poolInfo, "debug"));
        assertEquals("FWD_TOUPPER_A", server.getAttribute(poolInfo, "forwardName"));
        assertEquals("StandardServerName", server.getAttribute(poolInfo, "serverName"));
        assertEquals("openejb:Resource/xaQueueConnectionFactory", server.getAttribute(poolInfo, "connectionFactory"));
        assertEquals("openejb:Resource/requestTestQueue", server.getAttribute(poolInfo, "destinationName"));
        assertEquals("toupperTestJMSBytesMessageToStringDataProvider", server.getAttribute(poolInfo, "refDataProvider"));
        assertEquals("p$TEST_PROP is NULL", server.getAttribute(poolInfo, "messageSelector"));
        assertEquals("", server.getAttribute(poolInfo, "flowSystem"));
        assertEquals("", server.getAttribute(poolInfo, "flowService"));
        assertEquals(true, server.getAttribute(poolInfo, "transacted"));
        assertTrue(((Integer) server.getAttribute(poolInfo, "transactionTimeout")).intValue() == 30);

        set = server.queryNames(
                new ObjectName(
                        "GreenVulcano:Type=JMSForwardListenerPoolInfo,Component=JMSForwardListenerPoolInfo,Forward=FWD_TOUPPER_A_PROP,*"),
                null);
        assertTrue(
                "No JMSForwardListenerPool info available in JMX object returned from GreenVulcano domain for Forward=FWD_TOUPPER_A_PROP",
                (set != null) && !set.isEmpty());
        poolInfo = set.iterator().next();

        assertEquals(true, server.getAttribute(poolInfo, "active"));
        assertEquals(1, server.getAttribute(poolInfo, "initialSize"));
        assertEquals(10, server.getAttribute(poolInfo, "maximumSize"));
        assertTrue(((Integer) server.getAttribute(poolInfo, "maxCreated")).intValue() >= 1);
        assertTrue(((Integer) server.getAttribute(poolInfo, "pooledCount")).intValue() >= 1);
        assertTrue(((Integer) server.getAttribute(poolInfo, "workingCount")).intValue() >= 0);
        assertEquals(true, server.getAttribute(poolInfo, "dumpMessage"));
        assertEquals(true, server.getAttribute(poolInfo, "debug"));
        assertEquals("FWD_TOUPPER_A_PROP", server.getAttribute(poolInfo, "forwardName"));
        assertEquals("StandardServerName", server.getAttribute(poolInfo, "serverName"));
        assertEquals("openejb:Resource/xaQueueConnectionFactory", server.getAttribute(poolInfo, "connectionFactory"));
        assertEquals("openejb:Resource/requestTestQueue", server.getAttribute(poolInfo, "destinationName"));
        assertEquals("toupperTestJMSBytesMessageToStringDataProvider", server.getAttribute(poolInfo, "refDataProvider"));
        assertEquals("p$TEST_PROP = 'Test value'", server.getAttribute(poolInfo, "messageSelector"));
        assertEquals("", server.getAttribute(poolInfo, "flowSystem"));
        assertEquals("", server.getAttribute(poolInfo, "flowService"));
        assertEquals(true, server.getAttribute(poolInfo, "transacted"));
        assertTrue(((Integer) server.getAttribute(poolInfo, "transactionTimeout")).intValue() == 30);
    }

    /**
     * @throws Exception
     */
    public void testForwardJMXStartStop() throws Exception
    {
        MBeanServer server = JMXEntryPoint.instance().getServer();
        Set<ObjectName> set = server.queryNames(
                new ObjectName(
                        "GreenVulcano:Type=JMSForwardListenerPoolInfo,Component=JMSForwardListenerPoolInfo,Forward=FWD_TOUPPER_A,*"),
                null);
        assertTrue(
                "No JMSForwardListenerPool info available in JMX object returned from GreenVulcano domain for Forward=FWD_TOUPPER_A",
                (set != null) && !set.isEmpty());
        ObjectName poolInfo = set.iterator().next();

        assertEquals(true, server.getAttribute(poolInfo, "active"));
        assertTrue(((Integer) server.getAttribute(poolInfo, "pooledCount")).intValue() >= 1);

        server.invoke(poolInfo, "stop", new Object[0], new String[0]);

        try {
            Thread.sleep(5000);
        }
        catch (Exception exc) {
            // do nothing
        }
        assertEquals(false, server.getAttribute(poolInfo, "active"));
        assertTrue(((Integer) server.getAttribute(poolInfo, "pooledCount")).intValue() == 0);

        server.invoke(poolInfo, "start", new Object[0], new String[0]);

        try {
            Thread.sleep(5000);
        }
        catch (Exception exc) {
            // do nothing
        }
        assertEquals(true, server.getAttribute(poolInfo, "active"));
        assertTrue(((Integer) server.getAttribute(poolInfo, "pooledCount")).intValue() >= 1);
    }

}

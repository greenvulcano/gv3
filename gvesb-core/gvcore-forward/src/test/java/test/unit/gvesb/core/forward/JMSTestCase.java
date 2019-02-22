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

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQXAConnectionFactory;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.jmx.JMXEntryPoint;
import junit.framework.TestCase;

/**
 * @version 3.2.0 Gen 11, 2012
 * @author GreenVulcano Developer Team
 *
 */
public class JMSTestCase extends TestCase
{
    /**
     *
     */
    protected static final String SYSTEM          = "GVESB";

    /**
     *
     */
    protected static final String SERVICE_E       = "TEST_ENQUEUE";

    protected static final String SERVICE_D       = "TEST_DEQUEUE";

    protected static final String SERVICE_DS      = "TEST_DEQUEUE_SEL";

    protected static final String SERVICE_DDS     = "TEST_DEQUEUE_DYN_SEL";

    /**
     *
     */
    protected static final String TEST_MESSAGE    = "Test message!";

    /**
     *
     */
    protected static final String EXPECTED_RESULT = "TEST MESSAGE!";

    private Context               context;

    /**
     *
     */
    protected TransactionManager  transactionManager;

    @Override
    protected void setUp() throws Exception
    {
        this.context = new InitialContext();

        ConnectionFactory connFactory = new ActiveMQConnectionFactory("vm://localhost?async=true");
        this.context.rebind("Resource/queueConnectionFactory", connFactory);
        connFactory = new ActiveMQXAConnectionFactory("vm://localhost?async=true");
        this.context.rebind("Resource/xaQueueConnectionFactory", connFactory);

        this.transactionManager = (TransactionManager) this.context.lookup("java:comp/TransactionManager");

        JMXEntryPoint.instance();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        if (this.context != null) {
            try {
                this.context.close();
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }


    /**
     * @throws Exception
     */
    public void _testDUMMY() throws Exception
    {
        assertEquals(true, true);
    }

    /**
     * @throws Exception
     */
    public void testGVCoreAsync() throws Exception
    {
        String SYSTEM_NAME = SYSTEM;
        String SERVICE_NAME = SERVICE_DS;
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_MESSAGE);
        gvBuffer.setProperty("TEST_PROP", "1");

        GVBuffer gvBufferout = performEnq(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(TEST_MESSAGE, gvBufferout.getObject());

        try {
            Thread.sleep(1000);
        }
        catch (Exception exc) {
            // do nothing
        }

        try {
        	gvBuffer.setProperty("TEST_PROP", "2");
        	gvBuffer.setObject(null);
        	gvBufferout = performDeq(gvBuffer);
        	assertTrue("Dequeue must fail!!!", false);
        }
        catch (Exception exc) {
        	assertTrue(true);
        }

        try {
            Thread.sleep(1000);
        }
        catch (Exception exc) {
            // do nothing
        }

    	gvBuffer.setProperty("TEST_PROP", "1");
    	gvBuffer.setObject(null);
        gvBufferout = performDeq(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(TEST_MESSAGE, gvBufferout.getObject());
        assertEquals("1", gvBufferout.getProperty("TEST_PROP"));

        try {
            Thread.sleep(5000);
        }
        catch (Exception exc) {
            // do nothing
        }
    }


    private GVBuffer performEnq(GVBuffer gvBuffer) throws Exception
    {
        GreenVulcanoPool greenVulcano = GreenVulcanoPoolManager.instance().getGreenVulcanoPool("J2EEGreenVulcano");
        try {
            this.transactionManager.begin();
            greenVulcano.request(gvBuffer);
        }
        catch (Exception exc) {
            this.transactionManager.rollback();
            throw exc;
        }
        this.transactionManager.commit();

        return gvBuffer;
    }

    private GVBuffer performDeq(GVBuffer gvBuffer) throws Exception
    {
        GreenVulcanoPool greenVulcano = GreenVulcanoPoolManager.instance().getGreenVulcanoPool("J2EEGreenVulcano");
        GVBuffer gvBufferout = null;
        try {
            this.transactionManager.begin();
            gvBufferout = greenVulcano.getReply(gvBuffer);
        }
        catch (Exception exc) {
            this.transactionManager.rollback();
            throw exc;
        }
        this.transactionManager.commit();

        return gvBufferout;
    }
}

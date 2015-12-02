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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.jmx.JMXEntryPoint;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQXAConnectionFactory;

/**
 * @version 3.2.0 Gen 11, 2012
 * @author GreenVulcano Developer Team
 * 
 */
public class TOUPPERForwardTestCase extends TestCase
{
    /**
     *
     */
    protected static final String SYSTEM          = "GVESB";

    /**
     *
     */
    protected static final String SERVICE         = "TOUPPER_A";

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
        context = new InitialContext();

        ConnectionFactory connFactory = new ActiveMQConnectionFactory("vm://localhost?async=true");
        context.rebind("Resource/queueConnectionFactory", connFactory);
        connFactory = new ActiveMQXAConnectionFactory("vm://localhost?async=true");
        context.rebind("Resource/xaQueueConnectionFactory", connFactory);

        transactionManager = (TransactionManager) context.lookup("java:comp/TransactionManager");

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
    public void _testDUMMY() throws Exception
    {
        assertEquals(true, true);
    }

    /**
     * @throws Exception
     */
    public void testGVCoreAsync() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TOUPPER_A";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_MESSAGE);

        GVBuffer gvBufferout = perform(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(EXPECTED_RESULT, gvBufferout.getObject());

        try {
            Thread.sleep(10000);
        }
        catch (Exception exc) {
            // do nothing
        }

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = perform(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(EXPECTED_RESULT, gvBufferout.getObject());

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = perform(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(EXPECTED_RESULT, gvBufferout.getObject());

        try {
            Thread.sleep(5000);
        }
        catch (Exception exc) {
            // do nothing
        }
    }

    /**
     * @throws Exception
     */
    public void testGVCoreAsyncSelector() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TOUPPER_A";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_MESSAGE);
        gvBuffer.setProperty("TEST_PROP", "Test value");

        try {
            Thread.sleep(10000);
        }
        catch (Exception exc) {
            // do nothing
        }

        GVBuffer gvBufferout = perform(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(EXPECTED_RESULT, gvBufferout.getObject());

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = perform(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(EXPECTED_RESULT, gvBufferout.getObject());

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = perform(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(EXPECTED_RESULT, gvBufferout.getObject());

        try {
            Thread.sleep(5000);
        }
        catch (Exception exc) {
            // do nothing
        }
    }

    /**
     * @throws Exception
     */
    public void testGVCoreAsyncReload() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TOUPPER_A";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_MESSAGE);

        GVBuffer gvBufferout = perform(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(EXPECTED_RESULT, gvBufferout.getObject());

        XMLConfig.reload("GVCore.xml");
        try {
            Thread.sleep(10000);
        }
        catch (Exception exc) {
            // do nothing
        }

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = perform(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(EXPECTED_RESULT, gvBufferout.getObject());

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = perform(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(EXPECTED_RESULT, gvBufferout.getObject());

        try {
            Thread.sleep(5000);
        }
        catch (Exception exc) {
            // do nothing
        }
    }

    private GVBuffer perform(GVBuffer gvBuffer) throws Exception
    {
        GreenVulcanoPool greenVulcano = GreenVulcanoPoolManager.instance().getGreenVulcanoPool("J2EEGreenVulcano");
        try {
            transactionManager.begin();
            greenVulcano.request(gvBuffer);
        }
        catch (Exception exc) {
            transactionManager.rollback();
            throw exc;
        }
        transactionManager.commit();

        GVBuffer gvBufferout = null;
        try {
            transactionManager.begin();
            gvBufferout = greenVulcano.getReply(gvBuffer);
        }
        catch (Exception exc) {
            transactionManager.rollback();
            throw exc;
        }
        transactionManager.commit();

        return gvBufferout;
    }
}

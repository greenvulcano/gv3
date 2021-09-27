/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
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
package tests.unit.gvesb.gvcore;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.core.GreenVulcano;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

/**
 * @version 3.4.0 Jan 17, 2014
 * @author GreenVulcano Developer Team
 */
public class GVCoreSpawnTestCase extends XMLTestCase
{

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        XMLUnit.setIgnoreWhitespace(true);
    }


    /**
     * @throws Exception
     */
    public void testTestSpawnNormalEnd() throws Exception
    {
        GreenVulcanoPool gvpool = GreenVulcanoPoolManager.instance().getGreenVulcanoPool("J2EEGreenVulcano");

        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TestSpawnNormalEnd";
        String TEST_BUFFER = "CiRo,NuNzIo,GiAnFrAnCo,AnToNiO";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        gvBuffer.setProperty("P1", "V1");
        gvBuffer.setProperty("P2", "V2");

        GreenVulcano greenVulcano = gvpool.getGreenVulcano(gvBuffer);
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        System.out.println("TEST 1----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 1----PAR");
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(TEST_BUFFER, gvBufferout.getObject());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));
        gvpool.releaseGreenVulcano(greenVulcano);

        id = new Id();
        gvBuffer.setId(id);
        gvBuffer.setProperty("P1", "V1");
        gvBuffer.setProperty("P2", "X");
        greenVulcano = gvpool.getGreenVulcano(gvBuffer);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        System.out.println("TEST 2----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 2----PAR");
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(TEST_BUFFER, gvBufferout.getObject());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));
        gvpool.releaseGreenVulcano(greenVulcano);

        id = new Id();
        gvBuffer.setId(id);
        gvBuffer.setProperty("P1", "X");
        gvBuffer.setProperty("P2", "V2");
        greenVulcano = gvpool.getGreenVulcano(gvBuffer);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        System.out.println("TEST 3----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 3----PAR");
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(TEST_BUFFER, gvBufferout.getObject());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));
        gvpool.releaseGreenVulcano(greenVulcano);

        XMLConfig.reload("GVCore.xml");
        Thread.sleep(20000);

        id = new Id();
        gvBuffer.setId(id);
        gvBuffer.setProperty("P1", "X");
        gvBuffer.setProperty("P2", "X");
        greenVulcano = gvpool.getGreenVulcano(gvBuffer);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        System.out.println("TEST 4----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 4----PAR");
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(TEST_BUFFER, gvBufferout.getObject());
        assertEquals("SKIP", gvBufferout.getProperty("END"));
        gvpool.releaseGreenVulcano(greenVulcano);

        Thread.sleep(2000);
    }


    /**
     * @throws Exception
     */
    public void testTestSpawnTimeout() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TestSpawnTimeout";
        String TEST_BUFFER = "CiRo,NuNzIo,GiAnFrAnCo,AnToNiO";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        gvBuffer.setProperty("P1", "V1");
        gvBuffer.setProperty("P2", "V2");
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        System.out.println("TEST 1----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 1----PAR");
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(TEST_BUFFER, gvBufferout.getObject());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));
        Thread.sleep(2000);

        id = new Id();
        gvBuffer.setId(id);
        gvBuffer.setProperty("P1", "V1");
        gvBuffer.setProperty("P2", "X");
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        System.out.println("TEST 2----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 2----PAR");
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(TEST_BUFFER, gvBufferout.getObject());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));
        Thread.sleep(2000);

        id = new Id();
        gvBuffer.setId(id);
        gvBuffer.setProperty("P1", "X");
        gvBuffer.setProperty("P2", "V2");
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        System.out.println("TEST 3----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 3----PAR");
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(TEST_BUFFER, gvBufferout.getObject());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));
        Thread.sleep(2000);

        id = new Id();
        gvBuffer.setId(id);
        gvBuffer.setProperty("P1", "X");
        gvBuffer.setProperty("P2", "X");
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        System.out.println("TEST 4----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 4----PAR");
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(TEST_BUFFER, gvBufferout.getObject());
        assertEquals("SKIP", gvBufferout.getProperty("END"));
        Thread.sleep(2000);
    }

}

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
package tests.unit.virtual;

import static org.junit.Assert.assertTrue;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.core.GreenVulcano;
import it.greenvulcano.gvesb.gvzmq.GVZMQManager;

import org.junit.Test;
import org.zeromq.ZContext;

/**
 * @version 3.2.0 20/03/2012
 * @author GreenVulcano Developer Team
 */
public class ZMQPublisherTestCase
{
    private SimpleListener sl1 = null;
    private SimpleListener sl2 = null;

    protected void startSP(ZContext zctx) throws Exception
    {
        sl1 = new SimpleListener("SimpleListener_1", zctx);
        //sl1.setDaemon(true);
        sl1.start();

        sl2 = new SimpleListener("SimpleListener_2", zctx);
        //sl2.setDaemon(true);
        sl2.start();

        try {
            Thread.sleep(3000);
        }
        catch (Exception exc) {
            // TODO: handle exception
        }
    }


    protected void stopSP() throws Exception
    {
        if (sl1 != null) {
            sl1.stopListen();
            sl1 = null;
        }
        if (sl2 != null) {
            sl2.stopListen();
            sl2 = null;
        }
    }

    /**
     * @throws Exception
     */
    @Test
    public void testPublisher() throws Exception
    {
        GVZMQManager zm = GVZMQManager.instance();
        startSP(zm.getZMQContext());

        String TEST_BUFFER = "test input data";
        GVBuffer gvBuffer = new GVBuffer("GVESB", "TestZMQ_Publisher");
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        gvBuffer.setId(new Id());
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        gvBuffer.setId(new Id());
        gvBufferout = greenVulcano.requestReply(gvBuffer);

        try {
            Thread.sleep(3000);
        }
        catch (Exception exc) {
            // TODO: handle exception
        }
        stopSP();
        //zm.shutdownStarted(null);
        assertTrue(true);
    }
}

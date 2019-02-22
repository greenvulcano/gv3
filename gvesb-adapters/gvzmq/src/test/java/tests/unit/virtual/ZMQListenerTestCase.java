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
import it.greenvulcano.gvesb.gvzmq.GVZMQManager;

import org.junit.Test;
import org.zeromq.ZContext;

/**
 * @version 3.2.0 20/03/2012
 * @author GreenVulcano Developer Team
 */

public class ZMQListenerTestCase
{
    private SimplePublisher sp = null;
    private SimpleServer    ss = null;

    protected void startSP(ZContext zctx) throws Exception
    {
        ss = new SimpleServer("SimpleServer", zctx);
        //ss.setDaemon(true);
        ss.start();

        sp = new SimplePublisher("SimplePublisher", zctx, 3000);
        //sp.setDaemon(true);
        sp.start();
    }


    protected void stopSP() throws Exception
    {
        if (sp != null) {
            sp.stopPublish();
            sp = null;
        }
        if (ss != null) {
            ss.stopListen();
            ss = null;
        }
    }

    /**
     * @throws Exception
     */
    @Test
    public void testListener() throws Exception
    {
        GVZMQManager zm = GVZMQManager.instance();
        startSP(zm.getZMQContext());
        //System.out.println(result);
        try {
            Thread.sleep(15000);
        }
        catch (Exception exc) {
            // TODO: handle exception
        }
        stopSP();
        //zm.shutdownStarted(null);
        assertTrue(true);
    }
}

/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package tests.unit.virtual;

import it.greenvulcano.util.thread.BaseThread;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * @version 3.2.0 28/03/2012
 * @author GreenVulcano Developer Team
 */
public class SimpleListener extends BaseThread
{
    private ZContext   zctx       = null;
    private ZMQ.Socket subscriber = null;
    private boolean    isRunning  = true;

    public SimpleListener(String name, ZContext zctx)
    {
        super(name);
        this.zctx = ZContext.shadow(zctx);
        this.zctx.setMain(false);
    }

    @Override
    public void run()
    {
        try {
            // Prepare our subscriber
            subscriber = zctx.createSocket(ZMQ.SUB);
            subscriber.connect("tcp://127.0.0.1:5558");
            subscriber.subscribe("".getBytes());

            System.out.println(getName() + " - Started...");

            while (isRunning) {
                System.out.println(getName() + " - Listening...");
                // Use trim to remove the tailing '0' character
                String string = new String(subscriber.recv(0)).trim();

                System.out.println(getName() + "(IN): " + string);
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
        finally {
            try {
                zctx.destroy();
            }
            catch (Exception exc) {
                // do nothing
            }
            subscriber = null;
        }
    }

    public void stopListen()
    {
        isRunning = false;
        this.interrupt();
    }
}

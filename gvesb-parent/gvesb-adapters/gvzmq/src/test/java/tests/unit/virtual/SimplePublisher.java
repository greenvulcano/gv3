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

import java.util.Random;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * @version 3.2.0 21/03/2012
 * @author GreenVulcano Developer Team
 */
public class SimplePublisher extends BaseThread
{
    private ZContext   zctx      = null;
    private ZMQ.Socket publisher = null;
    private boolean    isRunning = true;
    private long       delay     = 1000;

    public SimplePublisher(String name, ZContext zctx, long delay)
    {
        super(name);
        this.delay = delay;
        this.zctx = ZContext.shadow(zctx);
        this.zctx.setMain(false);
        //this.zctx = new ZContext();
    }

    @Override
    public void run()
    {
        setRunning(true);
        try {
            // Prepare our publisher
            publisher = zctx.createSocket(ZMQ.PUB);
            publisher.bind("tcp://127.0.0.1:5556");
            System.out.println(getName() + " - Started...");

            // Initialize random number generator
            Random srandom = new Random(System.currentTimeMillis());
            while (isRunning) {
                // Get values that will fool the boss
                int zipcode, temperature, relhumidity;
                zipcode = srandom.nextInt(100000) + 1;
                temperature = (srandom.nextInt(215) - 80) + 1;
                relhumidity = srandom.nextInt(50) + 10 + 1;

                // Send message to all subscribers
                String update = String.format("%05d %d %d\u0000", zipcode, temperature, relhumidity);
                publisher.send(update.getBytes(), 0);

                try {
                    Thread.sleep(delay);
                }
                catch (Exception exc) {
                    // TODO: handle exception
                }
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
            publisher = null;
            setRunning(false);
        }
    }

    public void stopPublish()
    {
        isRunning = false;
        this.interrupt();
    }
}

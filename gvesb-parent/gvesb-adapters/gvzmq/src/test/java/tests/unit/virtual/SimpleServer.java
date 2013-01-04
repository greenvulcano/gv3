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

import java.util.StringTokenizer;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * @version 3.2.0 21/03/2012
 * @author GreenVulcano Developer Team
 */
public class SimpleServer extends BaseThread
{
    private ZContext   zctx      = null;
    private ZMQ.Socket rec       = null;
    private boolean    isRunning = true;

    public SimpleServer(String name, ZContext zctx)
    {
        super(name);
        this.zctx = ZContext.shadow(zctx);
        this.zctx.setMain(false);
        //this.zctx = new ZContext();
    }

    @Override
    public void run()
    {
        try {
            // Prepare our publisher
            rec = zctx.createSocket(ZMQ.REP);
            rec.bind("tcp://127.0.0.1:5557");
            System.out.println(getName() + " - Started...");

            while (isRunning) {
                // Use trim to remove the tailing '0' character
                String string = new String(rec.recv(0)).trim();

                System.out.println("SimpleServer(IN): " + string);
                int zipcode;
                float temperature, relhumidity;

                StringTokenizer sscanf = new StringTokenizer(string, " ");
                String t = sscanf.nextToken();
                zipcode = Integer.valueOf(t);

                t = sscanf.nextToken();
                temperature = Integer.valueOf(t) / 2;

                t = sscanf.nextToken();
                relhumidity = Integer.valueOf(t) / 2;

                // Send reply message
                String update = String.format("%05d %.3f %.3f\u0000", zipcode, temperature, relhumidity);

                System.out.println("SimpleServer(OUT): " + update);

                rec.send(update.getBytes(), 0);
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
            rec = null;
        }
    }

    public void stopListen()
    {
        isRunning = false;
        this.interrupt();
    }
}

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
package it.greenvulcano.gvesb.gvzmq.publisher;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvzmq.ZMQAdapterException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.BaseThread;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

/**
 * @version 3.2.0 26/03/2012
 * @author GreenVulcano Developer Team
 */
public class ZMQPublisher extends BaseThread
{
    private static Logger logger    = GVLogger.getLogger(ZMQPublisher.class);

    private boolean       isRunning = false;
    private String        address   = null;
    private ZContext      zctx      = null;
    private ZMQ.Socket    publisher = null;
    private Queue<ZMsg>   msgs      = new ConcurrentLinkedQueue<ZMsg>();


    public ZMQPublisher(String name)
    {
        super(name);
    }

    public void init(Node node, ZContext zctx) throws ZMQAdapterException
    {
        try {
            this.zctx = ZContext.shadow(zctx);
            this.zctx.setMain(false);

            address = XMLConfig.get(node, "@bind-address");
        }
        catch (Exception exc) {
            logger.error("Error initializing ZMQPublisher", exc);
            throw new ZMQAdapterException("GVZMQ_PUBLISHER_INIT_ERROR", exc);
        }
    }

    @Override
    public void run()
    {
        setRunning(true);
        try {
            publisher = zctx.createSocket(ZMQ.PUB);
            publisher.bind(address);
            isRunning = true;

            logger.debug("ZMQPublisher[" + getName() + "] running...");
            while (isRunning()) {
                try {
                    ZMsg msgIn = dequeue();
                    if ((msgIn != null) && !msgIn.isEmpty()) {
                        logger.debug("Sending published message on ZMQPublisher[" + getName() + "]...");
                        msgIn.send(publisher);
                    }
                }
                catch (InterruptedException exc) {
                    continue;
                }
                catch (Exception exc) {
                    logger.error("ZMQPublisher[" + getName() + "] error while publishing data", exc);
                }
            }
            logger.debug("ZMQPublisher[" + getName() + "] stop publishing");
        }
        catch (Exception exc) {
            logger.error("ZMQPublisher[" + getName() + "] publishing error", exc);
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


    public void publish(ZMsg message)
    {
        synchronized (msgs) {
            logger.debug("Request of publishing message on ZMQPublisher[" + getName() + "]...");
            msgs.add(message);
            msgs.notifyAll();
        }
    }

    private ZMsg dequeue() throws InterruptedException
    {
        synchronized (msgs) {
            if (msgs.isEmpty() && isRunning) {
                try {
                    msgs.wait(10000);
                }
                catch (InterruptedException exc) {
                    // do nothing
                }
            }
        }
        ZMsg message = null;
        synchronized (msgs) {
            if (!msgs.isEmpty()) {
                message = msgs.poll();
            }
        }
        return message;
    }

    /**
     * 
     * @return
     */
    public boolean isRunning()
    {
        return isRunning;
    }


    public void Destroy()
    {
        logger.debug("BEGIN - Destroing ZMQPublisher[" + getName() + "]");
        isRunning = false;
        this.interrupt();
        logger.debug("END - Destroing ZMQPublisher[" + getName() + "]");
    }
}

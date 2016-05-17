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
package it.greenvulcano.gvesb.gvzmq.listener;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvzmq.ZMQAdapterException;
import it.greenvulcano.gvesb.gvzmq.listener.invoker.ZMQInvoker;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.BaseThread;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

/**
 * 
 * @version 3.2.0 18/03/2012
 * @author GreenVulcano Developer Team
 */
public class ZMQListener extends BaseThread
{
    private static Logger logger     = GVLogger.getLogger(ZMQListener.class);

    private String        mode       = null;
    private boolean       isRunning  = false;
    private List<String>  addresses  = new ArrayList<String>();
    private List<String>  filters    = new ArrayList<String>();
    private ZMQInvoker    invoker    = null;
    private ZContext      zctx       = null;
    private ZMQ.Socket    subscriber = null;


    public ZMQListener(String name)
    {
        super(name);
    }

    public void init(Node node, ZContext zctx) throws ZMQAdapterException
    {
        try {
            this.zctx = ZContext.shadow(zctx);
            this.zctx.setMain(false);

            mode = XMLConfig.get(node, "@mode", "Subscribe");

            NodeList nl = XMLConfig.getNodeList(node, "zmqAddress");
            if (nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    addresses.add(XMLConfig.get(nl.item(i), "@address"));
                }
            }
            nl = XMLConfig.getNodeList(node, "zmqFilter");
            if (nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    filters.add(XMLConfig.get(nl.item(i), "@filter"));
                }
            }

            Node ni = XMLConfig.getNode(node, "*[@type='zmq-invoker']");
            invoker = (ZMQInvoker) Class.forName(XMLConfig.get(ni, "@class")).newInstance();
            invoker.init(ni);
        }
        catch (Exception exc) {
            logger.error("Error initializing ZMQListener", exc);
            throw new ZMQAdapterException("GVZMQ_LISTENER_INIT_ERROR", exc);
        }
    }


    @Override
    public void run()
    {
    	setRunning(true);
        try {
            if ("Subscribe".equals(mode)) {
                subscriber = zctx.createSocket(ZMQ.SUB);
            }
            else {
                subscriber = zctx.createSocket(ZMQ.REP);
            }

            for (String addr : addresses) {
                subscriber.connect(addr);
            }
            subscriber.subscribe("".getBytes());
            isRunning = true;
            logger.debug("ZMQListener[" + getName() + "] running...");
            while (isRunning()) {
                try {
                    ZMsg msgIn = ZMsg.recvMsg(subscriber, 0);
                    if (!msgIn.isEmpty()) {
                        ZMsg msgOut = invoker.processMessage(msgIn);
                    }
                }
                catch (Exception exc) {
                    logger.error("ZMQListener[" + getName() + "] error while processing data", exc);
                }
            }
            logger.debug("ZMQListener[" + getName() + "] stop listening");
        }
        catch (Exception exc) {
            logger.error("ZMQListener[" + getName() + "] listening error", exc);
        }
        finally {
            try {
                zctx.destroy();
            }
            catch (Exception exc) {
                // do nothing
            }
            subscriber = null;
            if (invoker != null) {
                invoker.destroy();
            }
            invoker = null;
            setRunning(false);
        }
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
        logger.debug("BEGIN - Destroing ZMQListener[" + getName() + "]");
        isRunning = false;
        this.interrupt();
        /*if (subscriber != null) {
            try {
                subscriber.setLinger(0);
                zctx.destroySocket(subscriber);
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        if (invoker != null) {
            invoker.destroy();
        }*/
        logger.debug("END - Destroing ZMQListener[" + getName() + "]");
    }
}

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
package it.greenvulcano.gvesb.gvzmq.listener.invoker;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.gvzmq.GVZMQManager;
import it.greenvulcano.gvesb.gvzmq.ZMQAdapterException;
import it.greenvulcano.gvesb.gvzmq.marshall.Decoder;
import it.greenvulcano.gvesb.gvzmq.marshall.Encoder;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.zeromq.ZMsg;

/**
 * 
 * @version 3.2.0 18/03/2012
 * @author GreenVulcano Developer Team
 */
public class GVCoreInvoker implements ZMQInvoker
{
    private static Logger logger    = GVLogger.getLogger(GVCoreInvoker.class);

    private String        name;
    private String        system;
    private String        service;
    private String        operation;
    private Decoder       decoder   = null;
    private Encoder       encoder   = null;
    private boolean       debug     = false;
    private boolean       sendReply = false;

    /**
     *
     */
    @Override
    public void init(Node node) throws ZMQAdapterException
    {
        try {
            name = XMLConfig.get(node, "@name");
            system = XMLConfig.get(node, "@gv-system", GVBuffer.DEFAULT_SYS);
            service = XMLConfig.get(node, "@gv-service");
            operation = XMLConfig.get(node, "@gv-operation");
            debug = XMLConfig.getBoolean(node, "@debug", false);
            sendReply = XMLConfig.getBoolean(node, "@send-reply", false);

            Node nd = XMLConfig.getNode(node, "*[@type='zmq-decoder']");
            if (nd != null) {
                decoder = (Decoder) Class.forName(XMLConfig.get(nd, "@class")).newInstance();
                decoder.init(nd);
            }
            Node ne = XMLConfig.getNode(node, "*[@type='zmq-encoder']");
            if (ne != null) {
                encoder = (Encoder) Class.forName(XMLConfig.get(ne, "@class")).newInstance();
                encoder.init(ne);
            }
        }
        catch (Exception exc) {
            throw new ZMQAdapterException("GVZMQ_APPLICATION_INIT_ERROR", exc);
        }
    }

    /**
     * @return the name
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * 
     */
    @Override
    public boolean isSendReply()
    {
        return sendReply;
    }

    @Override
    public ZMsg processMessage(ZMsg msgIn) throws ZMQAdapterException
    {
        ZMsg msgOut = null;

        try {
            logger.debug("BEGIN Operation");
            if (debug) {
                StringBuffer dmp = new StringBuffer();
                msgIn.dump(dmp);
                logger.debug("Input Message:\n" + dmp);
            }
            GVBuffer in = new GVBuffer(system, service);
            in.setObject(msgIn);

            if (decoder != null) {
                in = decoder.decode(msgIn, in);
            }

            GVBuffer out = getGreenVulcanoPool().forward(in, operation);

            if (sendReply) {
                if (out.getObject() instanceof ZMsg) {
                    msgOut = (ZMsg) out.getObject();
                }
                if (encoder != null) {
                    msgOut = encoder.encode(out, msgIn);
                }

                if (msgOut == null) {
                    throw new ZMQAdapterException("Invalid or missing reply message");
                }
                if (debug) {
                    StringBuffer dmp = new StringBuffer();
                    msgOut.dump(dmp);
                    logger.debug("Output Message:\n" + dmp);
                }
            }


            logger.debug("END Operation");
        }
        catch (Exception exc) {
            logger.error("Error processing ZMQ message", exc);
        }
        return msgOut;
    }

    @Override
    public void destroy()
    {
        encoder = null;
        decoder = null;
    }

    /**
     * @return
     * @throws ZMQAdapterException
     */
    private GreenVulcanoPool getGreenVulcanoPool() throws ZMQAdapterException
    {
        return GVZMQManager.instance().getGreenVulcanoPool();
    }
}

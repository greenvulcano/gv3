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
package it.greenvulcano.gvesb.gvnet.listener.invoker;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.gvnet.GVNetManager;
import it.greenvulcano.gvesb.gvnet.NetAdapterException;
import it.greenvulcano.gvesb.gvnet.marshall.Decoder;
import it.greenvulcano.gvesb.gvnet.marshall.Encoder;
import it.greenvulcano.gvesb.gvnet.marshall.NetMessage;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.ThreadMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.5.0 18/05/2014
 * @author GreenVulcano Developer Team
 */
public class GVCoreInvoker implements NetInvoker
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
    public void init(Node node) throws NetAdapterException
    {
        try {
            name = XMLConfig.get(node, "@name");
            system = XMLConfig.get(node, "@gv-system", GVBuffer.DEFAULT_SYS);
            service = XMLConfig.get(node, "@gv-service");
            operation = XMLConfig.get(node, "@gv-operation");
            debug = XMLConfig.getBoolean(node, "@debug", false);
            sendReply = XMLConfig.getBoolean(node, "@send-reply", false);

            Node nd = XMLConfig.getNode(node, "*[@type='net-decoder']");
            if (nd != null) {
                decoder = (Decoder) Class.forName(XMLConfig.get(nd, "@class")).newInstance();
                decoder.init(nd);
            }
            Node ne = XMLConfig.getNode(node, "*[@type='net-encoder']");
            if (ne != null) {
                encoder = (Encoder) Class.forName(XMLConfig.get(ne, "@class")).newInstance();
                encoder.init(ne);
            }
        }
        catch (Exception exc) {
            throw new NetAdapterException("GVNET_GVCOREINVOKER_INIT_ERROR", exc);
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
    public NetMessage processMessage(NetMessage msgIn) throws NetAdapterException, InterruptedException
    {
        NetMessage msgOut = null;

        try {
            NMDC.push();
            logger.debug("Processing message...");
            if (debug) {
                StringBuffer dmp = new StringBuffer();
                msgIn.dump(dmp);
                logger.debug("Input Message:\n" + dmp);
            }
            GVBuffer in = new GVBuffer(system, service);
            in.setObject(msgIn);
            in.setProperty("NET_LISTENER", (String) ThreadMap.get("NET_LISTENER"));
            in.setProperty("NET_REMOTE_ADDR", (String) ThreadMap.get("NET_REMOTE_ADDR"));
            in.setProperty("NET_REMOTE_PORT", (String) ThreadMap.get("NET_REMOTE_PORT"));

            if (decoder != null) {
                in = decoder.decode(msgIn, in);
            }
            GVBufferMDC.put(in);
            logger.debug("BEGIN Operation");

            GVBuffer out = getGreenVulcanoPool().forward(in, operation);

            if (sendReply && !"Y".equals(out.getProperty("IGNORE_RESPONSE"))) {
                if (out.getObject() instanceof NetMessage) {
                    msgOut = (NetMessage) out.getObject();
                }
                if (encoder != null) {
                    msgOut = encoder.encode(out, msgIn);
                }

                if (msgOut == null) {
                    throw new NetAdapterException("Invalid or missing reply message");
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
            logger.error("Error processing Net message", exc);
        }
        finally {
            NMDC.pop();
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
     * @throws NetAdapterException
     */
    private GreenVulcanoPool getGreenVulcanoPool() throws NetAdapterException
    {
        return GVNetManager.instance().getGreenVulcanoPool();
    }
}

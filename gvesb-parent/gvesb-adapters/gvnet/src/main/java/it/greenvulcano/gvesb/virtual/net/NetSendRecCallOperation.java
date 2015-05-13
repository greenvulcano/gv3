/*
 * Copyright (c) 2009-2015 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.virtual.net;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvnet.NetAdapterException;
import it.greenvulcano.gvesb.gvnet.marshall.Decoder;
import it.greenvulcano.gvesb.gvnet.marshall.Encoder;
import it.greenvulcano.gvesb.gvnet.marshall.NetMessage;
import it.greenvulcano.gvesb.gvnet.parser.Parser;
import it.greenvulcano.gvesb.gvnet.publisher.pool.Connection;
import it.greenvulcano.gvesb.gvnet.publisher.pool.ConnectionPool;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;


/**
 * 
 * @version 3.5.0 18/may/2014
 * @author GreenVulcano Developer Team
 */
public class NetSendRecCallOperation implements CallOperation
{
    private static Logger    logger   = GVLogger.getLogger(NetSendRecCallOperation.class);

    private OperationKey     key      = null;
    private String           host     = null;
    private int              port     = -1;
    private int              timeout  = 10000;
    private boolean          debug    = false;
    private Parser           parser   = null;
    private Encoder          encoder  = null;
    private Decoder          decoder  = null;



    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        logger.debug("Init start");
        try {
            host = XMLConfig.get(node, "@host");
            port = XMLConfig.getInteger(node, "@port");
            timeout = XMLConfig.getInteger(node, "@so-timeout", 10) * 1000;
            debug = XMLConfig.getBoolean(node, "@debug", false);

            logger.debug("init - loaded parameters: host = " + host + " - port: " + port + " - timeout: " + timeout);
            
            Node np = XMLConfig.getNode(node, "*[@type='net-parser']");
            parser = (Parser) Class.forName(XMLConfig.get(np, "@class")).newInstance();
            parser.init(np);

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
            logger.debug("Init stop");
        }
        catch (Exception exc) {
            throw new InitializationException("GV_INIT_SERVICE_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb
     * .buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        NetMessage msgIn = null;
        NetMessage msgOut = null;
        Connection conn = null;
        try {
            conn = ConnectionPool.instance().getConnection(host, port, timeout, true);
            parser.setInputStream(conn.getInputStream());

            if (gvBuffer.getObject() instanceof NetMessage) {
                msgOut = (NetMessage) gvBuffer.getObject();
            }
            if (encoder != null) {
                msgOut = encoder.encode(gvBuffer, null);
            }

            if (msgOut == null) {
                throw new NetAdapterException("Invalid or missing request message");
            }
            if (debug) {
                StringBuffer dmp = new StringBuffer();
                msgOut.dump(dmp);
                logger.debug("Output Message:\n" + dmp);
            }

            msgOut.writeTo(conn.getOutputStream());
            
            try {
                msgIn = parser.getMessage();
                if (msgIn != null) {
                    if (debug) {
                        StringBuffer dmp = new StringBuffer();
                        msgIn.dump(dmp);
                        logger.debug("Input Message:\n" + dmp);
                    }
                    gvBuffer.setObject(msgIn);

                    if (decoder != null) {
                        gvBuffer = decoder.decode(msgIn, gvBuffer);
                    }
                }
            }
            catch (Exception exc) {
                logger.error("NetSendRecCallOperation error while receiving data", exc);
                throw exc;
            }
        }
        catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
        finally {
            parser.releaseStream();
            try {
                ConnectionPool.instance().releaseConnection(conn);
            }
            catch (NetAdapterException exc) {
                // do nothing
            }            
        }
        return gvBuffer;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        ConnectionPool.instance().invalidatePool(host, port);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano
     * .gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }


    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    @Override
    public OperationKey getKey()
    {
        return key;
    }
}

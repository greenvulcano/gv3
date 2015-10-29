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
package it.greenvulcano.gvesb.virtual.zmq;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvzmq.GVZMQManager;
import it.greenvulcano.gvesb.gvzmq.ZMQAdapterException;
import it.greenvulcano.gvesb.gvzmq.marshall.Decoder;
import it.greenvulcano.gvesb.gvzmq.marshall.Encoder;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

/**
 * 
 * @version 3.2.0 18/03/2012
 * @author GreenVulcano Developer Team
 */
public class ZMQCallOperation implements CallOperation
{
    private static Logger logger  = GVLogger.getLogger(ZMQCallOperation.class);

    private OperationKey  key     = null;
    private String        address = null;
    private long          timeout = 0;
    private Decoder       decoder = null;
    private Encoder       encoder = null;
    private boolean       debug   = false;


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
            address = XMLConfig.get(node, "@address");
            timeout = XMLConfig.getLong(node, "@timeout", 10000) * 1000;
            debug = XMLConfig.getBoolean(node, "@debug", false);

            logger.debug("init - loaded parameters: address = " + address + " - timeout: " + timeout);
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
        ZMQ.Socket socket = null;
        try {
            ZMsg msgOut = null;

            if (gvBuffer.getObject() instanceof ZMsg) {
                msgOut = (ZMsg) gvBuffer.getObject();
            }
            if (encoder != null) {
                msgOut = encoder.encode(gvBuffer, null);
            }

            if (msgOut == null) {
                throw new ZMQAdapterException("Invalid or missing request message");
            }
            if (debug) {
                StringBuffer dmp = new StringBuffer();
                msgOut.dump(dmp);
                logger.debug("Request Message:\n" + dmp);
            }

            GVZMQManager zm = GVZMQManager.instance();
            ZContext zctx = ZContext.shadow(zm.getZMQContext());
            zctx.setMain(false);
            try {
                //socket = zm.getZMQContext().createSocket(ZMQ.REQ);
                socket = zctx.createSocket(ZMQ.REQ);
                socket.connect(address);

                System.out.println("ZMQCallOperation: create Socket");

                // send request
                msgOut.send(socket);
                System.out.println("ZMQCallOperation: sent request");

                //ZMQ.Poller items = zm.getZMQContext().getContext().poller(1);
                ZMQ.Poller items = zctx.getContext().poller(1);

                System.out.println("ZMQCallOperation: create Poller");

                items.register(socket, ZMQ.Poller.POLLIN);

                System.out.println("ZMQCallOperation: registered Socket");

                try {
                    // Poll socket for a reply, with timeout
                    long rc = items.poll(timeout);
                    if (items.pollin(0)) {
                        ZMsg msgIn = ZMsg.recvMsg(socket, 0);

                        if (debug) {
                            StringBuffer dmp = new StringBuffer();
                            msgIn.dump(dmp);
                            logger.debug("Response Message:\n" + dmp);
                        }

                        gvBuffer.setObject(msgIn);
                        if (decoder != null) {
                            gvBuffer = decoder.decode(msgIn, gvBuffer);
                        }

                        return gvBuffer;
                    }
                }
                finally {
                    items.unregister(socket);
                }
            }
            finally {
                if (socket != null) {
                    socket.setLinger(0);
                    //zm.getZMQContext().destroySocket(socket);
                    zctx.destroy();
                }
            }


            throw new Exception("Timeout receiving response");
        }
        catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
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
        // closeConnection();
    }

    /*
    private void initConnection() throws Exception
    {
        if ((connection == null) || !connection.isOpen()) {
            // The connection hub connects to listening servers
            ConnectionHub connectionHub = ConnectionHub.getInstance();
            // A connection object represents a socket attached to an ZMQ server
            connection = connectionHub.attach(host, port, new PipeParser(), MinLowerLayerProtocol.class);
        }
    }

    private void closeConnection()
    {
        try {
            if (connection != null) {
                // Close the connection
                ConnectionHub.getInstance().detach(connection);
            }
        }
        catch (Exception exc) {
            logger.error("Error closing ZMQ connection [" + getKey() + "]", exc);
        }
        connection = null;
    }*/

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

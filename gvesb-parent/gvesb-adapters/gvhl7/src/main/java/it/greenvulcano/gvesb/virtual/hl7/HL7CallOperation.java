/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.virtual.hl7;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;

import java.net.SocketException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionHub;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * 
 * @version 3.0.0 29/set/2010
 * @author GreenVulcano Developer Team
 */
public class HL7CallOperation implements CallOperation
{
    private static Logger    logger          = GVLogger.getLogger(HL7CallOperation.class);

    private OperationKey     key             = null;
    private String           host            = null;
    private int              port            = -1;
    private int              timeout         = 10000;
    private Parser           hl7StringParser = null;
    private DefaultXMLParser hl7XmlParser    = null;
    private Connection       connection      = null;


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
            timeout = XMLConfig.getInteger(node, "@timeout", 10) * 1000;

            logger.debug("init - loaded parameters: host = " + host + " - port: " + port + " - timeout: " + timeout);

            hl7StringParser = new GenericParser();
            hl7XmlParser = new DefaultXMLParser();

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
        try {
            String hl7MsgXmlIn = null;
            Object input = gvBuffer.getObject();
            if (input instanceof String) {
                logger.debug("Input object is a String");
                hl7MsgXmlIn = (String) input;
            }
            else if (input instanceof byte[]) {
                logger.debug("Input object is a byte array");
                hl7MsgXmlIn = new String((byte[]) input);
            }
            else if (input instanceof Node) {
                logger.debug("Input object is a Node");
                hl7MsgXmlIn = XMLUtils.serializeDOM_S((Node) input);
            }
            else {
                throw new Exception("Invalid input type: " + input.getClass());
            }
            logger.debug("Request XMLMessage:\n" + hl7MsgXmlIn);
            Message request = hl7XmlParser.parse(hl7MsgXmlIn);
            request.setParser(hl7StringParser);
            logger.debug("Request StringMessage:\n" + request.encode());

            initConnection();

            // The initiator is used to transmit unsolicited messages
            Initiator initiator = connection.getInitiator();
            initiator.setTimeoutMillis(timeout);

            Message response = null;

            try {
                response = initiator.sendAndReceive(request);
            }
            catch (SocketException exc) {
                // retry send;
                closeConnection();
                initConnection();
                response = initiator.sendAndReceive(request);
            }

            logger.debug("Response StringMessage:\n" + response.encode());
            String hl7MsgXmlOut = hl7XmlParser.encode(response);
            logger.debug("Response XMLMessage:\n" + hl7MsgXmlOut);

            gvBuffer.setObject(hl7MsgXmlOut);
        }
        catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
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
        closeConnection();
    }

    private void initConnection() throws Exception
    {
        if ((connection == null) || !connection.isOpen()) {
            // The connection hub connects to listening servers
            ConnectionHub connectionHub = ConnectionHub.getInstance();
            // A connection object represents a socket attached to an HL7 server
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
            logger.error("Error closing HL7 connection [" + getKey() + "]", exc);
        }
        connection = null;
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

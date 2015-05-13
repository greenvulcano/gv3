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
package it.greenvulcano.gvesb.gvnet.parser;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvnet.NetAdapterException;
import it.greenvulcano.gvesb.gvnet.marshall.NetMessage;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.bin.BinaryUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.5.0 18/mag/2014
 * @author GreenVulcano Developer Team
 * 
 */
public class SimpleStartStopParser implements Parser
{
    private static Logger logger      = GVLogger.getLogger(SimpleStartStopParser.class);

    private byte[]        beginMarker = null;
    private byte[]        endMarker   = null;
    private boolean       keepMarkers = false;
    private boolean       skipUntilBeginMarker = true;
    private InputStream   is          = null;


    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.gvnet.parser.Parser#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws NetAdapterException {
        try {
            String begin = XMLConfig.get(node, "@begin-marker", "");
            String end = XMLConfig.get(node, "@end-marker", "");
            keepMarkers = XMLConfig.getBoolean(node, "@keep-markers", false);
            skipUntilBeginMarker = XMLConfig.getBoolean(node, "@skip-until-begin-marker", true);

            beginMarker = BinaryUtils.dumpHexIntsAsByteArray(begin);
            endMarker = BinaryUtils.dumpHexIntsAsByteArray(end);

            logger.debug("beginMarker : " + begin);
            logger.debug("endMarker   : " + end);
            logger.debug("keepMarkers : " + keepMarkers);
            logger.debug("skipUntilBeginMarker : " + skipUntilBeginMarker);

            if ((beginMarker.length == 0) || (endMarker.length == 0)) {
                logger.error("Invalid value for Begin[" + begin + "]/End[" + end + "] marker");
                throw new NetAdapterException("Invalid value for Begin[" + begin + "]/End[" + end + "] marker");
            }
        }
        catch (NetAdapterException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("A generic error occurred while initializing", exc);
            throw new NetAdapterException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.greenvulcano.gvesb.gvnet.parser.Parser#setInputStream(java.io.InputStream
     * )
     */
    @Override
    public synchronized void setInputStream(InputStream is) throws NetAdapterException {
        this.is = is;
    }
    
    @Override
    public void releaseStream() {
        this.is = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.gvnet.parser.Parser#getMessage()
     */
    @Override
    public synchronized NetMessage getMessage() throws IOException, NetAdapterException, InterruptedException {
        boolean isError = false;
        NetMessage msg = new NetMessage();
        try {
            boolean endOfMessage = false;
    
            int c = 0;
            try {
                if (skipUntilBeginMarker) {
                    while (c != -1) {
                        c = is.read();
                        if (((byte) c) == beginMarker[0]) {
                            if (keepMarkers) {
                                msg.write(beginMarker);
                            }
                            break;
                        }
                    }
                }
                else {
                    c = is.read();
                }
            }
            catch (SocketException exc) {
                logger.info("SocketException on read() attempt.  Socket appears to have been closed: " + exc.getMessage());
                return null;
            }
    
            // trying to read when there is no data (stream may have been closed at other end)
            if (c == -1) {
                logger.info("End of input stream reached.");
                return null;
            }
    
            if (((byte) c) != beginMarker[0]) {
                throw new NetAdapterException("Message violates the minimal protocol: no start of message indicator received. Received: " + c);
            }
    
            while(!endOfMessage) {
                c = is.read();
    
                if (c == -1) {
                    throw new NetAdapterException("Message violates the minimal protocol: message terminated without a terminating character.");
                }
    
                if (((byte) c) == endMarker[0]) {
                    if (keepMarkers) {
                        msg.write(endMarker);
                    }
                    endOfMessage = true;
                }
                else {
                    // the character wasn't the end of message, append it to the message
                    msg.write(c);
                }
            }
    
            return msg;
        }
        catch (NetAdapterException exc) {
            isError = true;
            logger.error("Error reading Message", exc);
            throw exc;
        }
        catch (IOException exc) {
            isError = true;
            logger.error("Error reading Message", exc);
            //throw new NetAdapterException("Error reading Message", exc);
            throw exc;
        }
        finally {
            if (isError) {
                StringBuffer dmp = new StringBuffer();
                msg.dump(dmp);
                logger.error("Received Message:\n" + dmp);
            }
        }
    }

}

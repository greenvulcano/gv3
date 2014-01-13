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
package it.greenvulcano.gvesb.core.forward.preprocess.hl7;

import it.greenvulcano.gvesb.core.forward.JMSForwardException;
import it.greenvulcano.log.GVLogger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionHub;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * 
 * @version 3.4.0 07/gen/2014
 * @author GreenVulcano Developer Team
 * 
 */
public class HL7ConnectionValidatorHolder
{
    private static Logger                       logger      = GVLogger.getLogger(HL7ConnectionValidatorHolder.class);

    private Map<String, Connection>             connections = null;
    private Parser                              parser      = null;
    private static HL7ConnectionValidatorHolder instance    = null;

    private HL7ConnectionValidatorHolder() {
        connections = new TreeMap<String, Connection>();
        parser = new PipeParser();
    }

    public synchronized static final HL7ConnectionValidatorHolder instance() {
        if (instance == null) {
            instance = new HL7ConnectionValidatorHolder();
        }
        return instance;
    }


    public synchronized boolean isValid(String name, List<String> urls) throws JMSForwardException {
        if (urls.isEmpty()) {
            logger.debug("Empty HL7ConnectionValidator[" + name + "]: false");
            return false;
        }
        String url = "";
        try {
            Iterator<String> is = urls.iterator();
            while (is.hasNext()) {
                url = is.next();
                Connection conn = connections.get(url);
                if (conn == null) {
                    conn = getConnection(url);
                    if (conn.isOpen()) {
                        connections.put(url, conn);
                        continue;
                    }
                }
                if (!conn.isOpen()) {
                    connections.put(url, null);
                    ConnectionHub.getInstance().detach(conn);
                    conn = getConnection(url);
                    if (conn.isOpen()) {
                        connections.put(url, conn);
                    }
                }
            }
            //logger.debug("Validated HL7ConnectionValidator[" + name + "]: true");
            return true;
        }
        catch (Exception exc) {
            logger.error("Error validating HL7ConnectionValidator[" + name + "]: " + url, exc);
            return false;
        }
    }

    public synchronized void reset() {
        try {
            Iterator<String> is = connections.keySet().iterator();
            while (is.hasNext()) {
                String url = is.next();
                Connection conn = connections.get(url);
                if (conn != null) {
                    ConnectionHub.getInstance().detach(conn);
                    connections.put(url, null);
                }
            }
        }
        catch (Exception exc) {
            // do nothing
        }
    }

    public synchronized void reset(String name, List<String> urls) {
        try {
            Iterator<String> is = urls.iterator();
            while (is.hasNext()) {
                String url = is.next();
                Connection conn = connections.remove(url);
                if (conn != null) {
                    ConnectionHub.getInstance().detach(conn);
                }
            }
        }
        catch (Exception exc) {
            // do nothing
        }
    }

    public synchronized void destroy() {
        try {
            String url;
            Iterator<String> is = connections.keySet().iterator();
            while (is.hasNext()) {
                url = is.next();
                Connection conn = connections.get(url);
                if (conn != null) {
                    ConnectionHub.getInstance().detach(conn);
                }
            }
            connections.clear();
        }
        catch (Exception exc) {
            // do nothing
        }
    }

    private Connection getConnection(String url) throws JMSForwardException {
        String host = url.split(":")[0];
        int port = Integer.parseInt(url.split(":")[1]);

        try {
            Connection conn = ConnectionHub.getInstance().attach(host, port, parser, MinLowerLayerProtocol.class);
            return conn;
        }
        catch (Exception exc) {
            throw new JMSForwardException("Error initializing Connection[" + url + "]", exc);
        }
    }
}

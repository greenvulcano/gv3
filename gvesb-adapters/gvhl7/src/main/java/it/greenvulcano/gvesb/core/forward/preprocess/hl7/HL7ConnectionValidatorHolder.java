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
import it.greenvulcano.gvesb.gvhl7.utils.HL7Connection;
import it.greenvulcano.log.GVLogger;

import java.util.HashMap;
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
    private static Logger                       logger          = GVLogger.getLogger(HL7ConnectionValidatorHolder.class);

    private Map<HL7Connection, Connection>      connections     = null;
    private Map<String, Boolean>                previousStatus  = null;
    private Parser                              parser          = null;
    private static HL7ConnectionValidatorHolder instance        = null;

    private HL7ConnectionValidatorHolder() {
        connections = new TreeMap<HL7Connection, Connection>();
        previousStatus = new HashMap<String, Boolean>();
        parser = new PipeParser();
    }

    public synchronized static final HL7ConnectionValidatorHolder instance() {
        if (instance == null) {
            instance = new HL7ConnectionValidatorHolder();
        }
        return instance;
    }


    public synchronized boolean isValid(String name, List<HL7Connection> urls) throws JMSForwardException {
    	boolean isFirst = !previousStatus.containsKey(name);
    	if (isFirst) {
    		previousStatus.put(name, false);
    	}
    	boolean prevStatus = previousStatus.get(name);
        if (urls.isEmpty()) {
            if (isFirst || prevStatus) {
                logger.debug("Empty HL7ConnectionValidator[" + name + "]: false");
                previousStatus.put(name, false);
            }
            return false;
        }
        HL7Connection url = null;
        try {
            Iterator<HL7Connection> is = urls.iterator();
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
            if (isFirst || !prevStatus) {
                logger.debug("Validated HL7ConnectionValidator[" + name + "]: true");
                previousStatus.put(name, true);
            }
            return true;
        }
        catch (Exception exc) {
            if (isFirst || prevStatus) {
                logger.error("Error validating HL7ConnectionValidator[" + name + "]: " + url, exc);
                previousStatus.put(name, false);
            }
            return false;
        }
    }

    public synchronized void reset() {
        previousStatus.clear();
        try {
            Iterator<HL7Connection> is = connections.keySet().iterator();
            while (is.hasNext()) {
                HL7Connection url = is.next();
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

    public synchronized void reset(String name, List<HL7Connection> urls) {
        previousStatus.remove(name);
        try {
            Iterator<HL7Connection> is = urls.iterator();
            while (is.hasNext()) {
                HL7Connection url = is.next();
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
        previousStatus.clear();
        try {
            HL7Connection url;
            Iterator<HL7Connection> is = connections.keySet().iterator();
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

    private Connection getConnection(HL7Connection url) throws JMSForwardException {
        try {
            Connection conn = ConnectionHub.getInstance().attach(url.getHost(), url.getPort(), parser, MinLowerLayerProtocol.class);
            return conn;
        }
        catch (Exception exc) {
            throw new JMSForwardException("Error initializing Connection[" + url + "]", exc);
        }
    }
}

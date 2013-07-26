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
package it.greenvulcano.gvesb.gvhl7.listener;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvhl7.listener.handler.GVCoreApplication;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.ThreadMap;

import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.app.ThreadUtils;
import ca.uhn.hl7v2.llp.LowerLayerProtocol;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * 
 * @version 3.0.0 28/set/2010
 * @author GreenVulcano Developer Team
 */
public class HL7Listener extends HL7Service
{
    private static Logger                  logger       = GVLogger.getLogger(HL7Listener.class);
    /**
     * Socket timeout
     */
    private static final int               SO_TIMEOUT   = 3000;

    private int                            port;
    private String                         name;
    private ServerSocket                   ss           = null;
    private Map<String, GVCoreApplication> applications = new HashMap<String, GVCoreApplication>();
    private boolean						   autoStart    = false;
    private String                         serverName;


    public HL7Listener()
    {
        super(new PipeParser(), LowerLayerProtocol.makeLLP());
    }

    public void init(Node node) throws HL7AdapterException
    {
        try {
        	serverName = JMXEntryPoint.getServerName();
            name = XMLConfig.get(node, "@name");
            port = XMLConfig.getInteger(node, "@port");
            autoStart = XMLConfig.getBoolean(node, "@autoStart", true);

            NodeList nl = XMLConfig.getNodeList(node, "HL7Applications/*[@type='hl7application']");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                String clazz = XMLConfig.get(n, "@class");
                GVCoreApplication app = (GVCoreApplication) Class.forName(clazz).newInstance();
                app.init(n);
                applications.put(app.getName(), app);
                logger.debug("Registering HL7Application[" + app.getName() + "] to:");
                List<String[]> activations = app.getActivations();
                for (String[] entry : activations) {
                    logger.debug("MessageType[" + entry[0] + "] TriggerEvent[" + entry[1] + "]");
                    registerApplication(entry[0], entry[1], app);
                }
                logger.debug("Configured HL7Application[" + app.getName() + "]");
            }
        }
        catch (Exception exc) {
            logger.error("Error initializing HL7Listener", exc);
            throw new HL7AdapterException("GVHL7_LISTENER_INIT_ERROR", exc);
        }
    }

    /**
     * Loop that waits for a connection and starts a ConnectionManager when it
     * gets one.
     */
    @Override
    public void run()
    {
    	NMDC.push();
        NMDC.clear();
        NMDC.setServer(serverName);
        NMDC.setSubSystem(GVHL7ListenerManager.SUBSYSTEM);
        NMDC.put("LISTENER", name);
         
        try {
        	ThreadUtils.setListenerName(name);
            ss = new ServerSocket(port);
            ss.setSoTimeout(SO_TIMEOUT);
            logger.info("HL7Listener[" + name + "] running on port " + ss.getLocalPort());
            while (isRunning()) {
                try {
                    Socket newSocket = ss.accept();
                    logger.debug("HL7Listener[" + name + "] accepted connection from "
                            + newSocket.getInetAddress().getHostAddress());
                    Connection conn = new Connection(parser, this.llp, newSocket);
                    newConnection(conn);
                }
                catch (InterruptedIOException exc) {
                    // ignore - just timed out waiting for connection
                }
                catch (Exception exc) {
                    logger.error("HL7Listener[" + name + "] error while accepting connections: ", exc);
                }
            }
            logger.info("HL7Listener[" + name + "] stop listening");
        }
        catch (Exception exc) {
            logger.error("HL7Listener[" + name + "] listening error", exc);
        }
        finally {
            this.stop();
            if (ss != null) {
                try {
                    ss.close();
                }
                catch (Exception exc) {
                    //exc.printStackTrace();
                }
            }
            NMDC.pop();
            ThreadMap.clean();
            ThreadUtils.removeListenerName();
        }
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return
     */
    public boolean isAutoStart() {
		return autoStart;
	}
    
    /**
     * 
     * @return
     */
    public int getPort() {
		return port;
	}
    
    public void destroy()
    {
        logger.debug("BEGIN - Destroing HL7Listener[" + name + "]");
        
        try {
            this.stop();
        }
        catch (Exception exc) {
            // do nothing
        }
        if (ss != null) {
            try {
                ss.close();
            }
            catch (Exception exc) {
                //exc.printStackTrace();
            }
        }
        applications.clear();
        logger.debug("END - Destroing HL7Listener[" + name + "]");
    }
}

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
package it.greenvulcano.gvesb.gvmqtt.server;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.gvmqtt.MQTTAdapterException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.moquette.server.Server;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @version 3.5.0 13/mar/2015
 * @author GreenVulcano Developer Team
 * 
 */
public class Moquette implements MQTTServer, ShutdownEventListener
{
    private static Logger logger = GVLogger.getLogger(Moquette.class);
    private static final boolean IS_MANAGED = true;

    private String  name          = null;
    private String  brokerUrl     = null;
    private String  moquetteHome  = null;
    private Properties properties = new Properties();
    private Server  server        = null;

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.gvmqtt.server.MQTTServer#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws MQTTAdapterException {
        try {
            name = XMLConfig.get(node, "@name");
            brokerUrl = PropertiesHandler.expand(XMLConfig.get(node, "@brokerUrl"));
            moquetteHome = PropertiesHandler.expand(XMLConfig.get(node, "@moquetteHome", "sp{{gv.app.home}}" + File.separator + "moquette"));
            NodeList nl = XMLConfig.getNodeList(node, "moq-property");
            for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				properties.setProperty(XMLConfig.get(n, "@name"), PropertiesHandler.expand(XMLConfig.get(n, "@value", "")));
			}
            
        	ShutdownEventLauncher.addEventListener(this);
        	
        	logger.info("Inititalized Moquette MQTT Managed Server: name[" + name + "] moquetteHome[" + moquetteHome + "] brokerUrl[" + brokerUrl + "] properties:" + properties);
        }
        catch (Exception exc) {
            throw new MQTTAdapterException("Error initializing Moquette MQTT server interface", exc);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.gvmqtt.server.MQTTServer#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.gvmqtt.server.MQTTServer#getBrokerUrl()
     */
    @Override
    public String getBrokerUrl() {
        return brokerUrl;
    }

    @Override
    public boolean isManaged() {
        return IS_MANAGED;
    }
    
    @Override
    public boolean isRunning() throws MQTTAdapterException {
        return server != null;
    }
    
    @Override
    public void start() throws MQTTAdapterException {
    	System.setProperty("moquette.path", moquetteHome);
    	if (server == null) {
    		server = new Server();
    		try {
				server.startServer(properties);
			} catch (Exception exc) {
				server = null;
				throw new MQTTAdapterException("Error starting Moquette Server", exc);
			}
    	}
    	logger.info("Moquette Server started...");
    }
    
    @Override
    public void stop() throws MQTTAdapterException {
        if (server != null) {
        	server.stopServer();
        }
        server = null;
    	logger.info("Moquette Server stopped...");
    }
    
    public void destroy() {
    	ShutdownEventLauncher.removeEventListener(this);
    	try {
			stop();
		} catch (MQTTAdapterException exc) {
			// do nothing
		}
    }
    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.event.util.shutdown.ShutdownEventListener#shutdownStarted
     * (it.greenvulcano.event.util.shutdown.ShutdownEvent)
     */
    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
        try {
			stop();
		} catch (MQTTAdapterException e) {
			// do nothing
		}
    }
}

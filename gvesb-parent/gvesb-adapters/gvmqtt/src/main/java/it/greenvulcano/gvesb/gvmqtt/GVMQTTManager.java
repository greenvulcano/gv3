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
package it.greenvulcano.gvesb.gvmqtt;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.gvesb.gvmqtt.publisher.MQTTPublisher;
import it.greenvulcano.gvesb.gvmqtt.publisher.jmx.MQTTPublisherInfo;
import it.greenvulcano.gvesb.gvmqtt.server.MQTTServer;
import it.greenvulcano.gvesb.gvmqtt.subscriber.MQTTSubscriber;
import it.greenvulcano.gvesb.gvmqtt.subscriber.jmx.MQTTSubscriberInfo;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.GVLogger;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.5.0 13/mar/2014
 * @author GreenVulcano Developer Team
 */
public class GVMQTTManager implements ConfigurationListener, ShutdownEventListener
{
    private static Logger                    logger                 = GVLogger.getLogger(GVMQTTManager.class);

    private static final String              DEFAULT_CONF_FILE_NAME = "GVMQTTAdapter-Configuration.xml";

    private static GVMQTTManager             instance               = null;
    
    private GreenVulcanoPool                 greenVulcanoPool       = null;

    private HashMap<String, MQTTSubscriber>  subscribers            = new HashMap<String, MQTTSubscriber>();
    private HashMap<String, MQTTPublisher>   publishers             = new HashMap<String, MQTTPublisher>();

    private MQTTServer                       server                 = null;
    
    private MqttClientPersistence            dataStore              = null;

    public static final String               SUBSYSTEM              = "GVMQTTSubscriber";

    public static synchronized GVMQTTManager instance() throws MQTTAdapterException
    {
        if (instance == null) {
            instance = new GVMQTTManager();
        }

        return instance;
    }

    /**
     *
     */
    private GVMQTTManager() throws MQTTAdapterException
    {
        XMLConfig.addConfigurationListener(this, DEFAULT_CONF_FILE_NAME);
        ShutdownEventLauncher.addEventListener(this);
        init();
    }

    /**
     *
     */
    private void init() throws MQTTAdapterException
    {
        try {
            logger.debug("Initializing GVMQTTManager");

            Document globalConfig = null;
            try {
                globalConfig = XMLConfig.getDocument(DEFAULT_CONF_FILE_NAME);
            }
            catch (XMLConfigException exc) {
                logger.warn("Error reading MQTT configuration from file: " + DEFAULT_CONF_FILE_NAME , exc);
            }

            if (globalConfig == null) {
                return;
            }

            greenVulcanoPool = GreenVulcanoPoolManager.instance().getGreenVulcanoPool(SUBSYSTEM);
            if (greenVulcanoPool == null) {
                throw new MQTTAdapterException("GVMQTT_GREENVULCANOPOOL_NOT_CONFIGURED");
            }
            
            Node sn = XMLConfig.getNode(DEFAULT_CONF_FILE_NAME, "/GVMQTTConfiguration/*[@type='mqtt-server']");
            server = (MQTTServer) Class.forName(XMLConfig.get(sn, "@class")).newInstance();
            server.init(sn);
            
            if (server.isManaged()) {
            	try {
    				server.start();
    			} catch (MQTTAdapterException exc) {
    				logger.error("Error starting MQTT managed Server", exc);
    				throw exc;
    			}
            }

            NodeList nl = XMLConfig.getNodeList(DEFAULT_CONF_FILE_NAME,
                    "/GVMQTTConfiguration/MQTTSubscribers/*[@type='mqtt-subscriber' and @enabled='true']");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                MQTTSubscriber s = (MQTTSubscriber) Class.forName(XMLConfig.get(n, "@class")).newInstance();
                s.setManager(this);
                s.init(n);
                register(s);
                subscribers.put(s.getName(), s);
                logger.debug("Configured MQTTSubscriber[" + s.getName() + "]");
                if (s.isAutoStart()) {
                    s.start();
                }
            }

            nl = XMLConfig.getNodeList(DEFAULT_CONF_FILE_NAME,
                    "/GVMQTTConfiguration/MQTTPublishers/*[@type='mqtt-publisher' and @enabled='true']");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                MQTTPublisher p = (MQTTPublisher) Class.forName(XMLConfig.get(n, "@class")).newInstance();
                p.setManager(this);
                p.init(n);
                register(p);
                publishers.put(p.getName(), p);
                logger.debug("Configured MQTTPublisher[" + p.getName() + "]");
                if (p.isAutoStart()) {
                    p.start();
                }
            }

        }
        catch (MQTTAdapterException exc) {
            logger.error("Error initializing GVMQTTManager", exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error initializing GVMQTTManager", exc);
            throw new MQTTAdapterException("GVMQTT_APPLICATION_INIT_ERROR", exc);
        }
    }


    public MQTTPublisher getPublisher(String name) throws MQTTAdapterException {
        MQTTPublisher p = this.publishers.get(name);
        if ((p == null) || !p.isActive()) {
            throw new MQTTAdapterException("MQTTPublisher[" + name + "] not found or not active");
        }
        return p;
    }

    /**
     * @return the greenVulcanoPool
     */
    public GreenVulcanoPool getGreenVulcanoPool()
    {
        return this.greenVulcanoPool;
    }
    
    public void destroy()
    {
        logger.debug("BEGIN - Destroing GVMQTTManager");
        for (Map.Entry<String, MQTTSubscriber> sEntry : subscribers.entrySet()) {
            try {
                deregister(sEntry.getValue(), true);
                sEntry.getValue().destroy();
            }
            catch (Exception exc) {
                logger.error("Error destroing MQTTSubscriber[" + sEntry.getKey() + "]", exc);
            }
        }
        subscribers.clear();

        for (Map.Entry<String, MQTTPublisher> sEntry : publishers.entrySet()) {
            try {
                deregister(sEntry.getValue(), true);
                sEntry.getValue().destroy();
            }
            catch (Exception exc) {
                logger.error("Error destroing MQTTPublisher[" + sEntry.getKey() + "]", exc);
            }
        }
        publishers.clear();
        
        if ((server != null) && server.isManaged()) {
        	try {
				server.stop();
			} catch (MQTTAdapterException exc) {
				logger.error("Error shutting down MQTT managed Server", exc);
			}
        }
        logger.debug("END - Destroing GVMQTTManager");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.configuration.ConfigurationListener#configurationChanged
     * (it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        if ((evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && evt.getFile().equals(DEFAULT_CONF_FILE_NAME)) {
            logger.debug("BEGIN - Operation(reload Configuration)");
            destroy();
            try {
                init();
            }
            catch (Exception exc) {
                logger.error("Error initializing GVMQTTManager", exc);
            }
            logger.debug("END - Operation(reload Configuration)");
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
        destroy();
    }

    /**
     * Register the receiver as MBean.
     * 
     * @param receiver
     *        the receiver to register.
     */
    private void register(MQTTSubscriber receiver)
    {
        logger.debug("Registering MBean for MQTTSubscriber(" + receiver.getName() + ")");
        Hashtable<String, String> properties = getMBeanProperties(receiver);
        try {
            deregister(receiver, false);
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            jmx.registerObject(new MQTTSubscriberInfo(receiver), MQTTSubscriberInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            logger.warn("Error registering MBean for MQTTSubscriber(" + receiver.getName() + ")", exc);
        }
    }

    /**
     * Deregister the receiver as MBean.
     * 
     * @param receiver
     *        the receiver to deregister.
     */
    private void deregister(MQTTSubscriber receiver, boolean showError)
    {
        logger.debug("Deregistering MBean for MQTTSubscriber(" + receiver.getName() + ")");
        Hashtable<String, String> properties = getMBeanProperties(receiver);
        try {
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            jmx.unregisterObject(new MQTTSubscriberInfo(receiver), MQTTSubscriberInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            if (showError) {
                logger.warn("Cannot de-register MQTTSubscriber(" + receiver.getName() + ")", exc);
            }
        }
    }

    private Hashtable<String, String> getMBeanProperties(MQTTSubscriber receiver)
    {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("Name", receiver.getName());
        return properties;
    }

    /**
     * Register the sender as MBean.
     * 
     * @param sender
     *        the sender to register.
     */
    private void register(MQTTPublisher sender)
    {
        logger.debug("Registering MBean for MQTTPublisher(" + sender.getName() + ")");
        Hashtable<String, String> properties = getMBeanProperties(sender);
        try {
            deregister(sender, false);
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            jmx.registerObject(new MQTTPublisherInfo(sender), MQTTPublisherInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            logger.warn("Error registering MBean for MQTTPublisher(" + sender.getName() + ")", exc);
        }
    }

    /**
     * Deregister the sender as MBean.
     * 
     * @param sender
     *        the sender to deregister.
     */
    private void deregister(MQTTPublisher sender, boolean showError)
    {
        logger.debug("Deregistering MBean for MQTTPublisher(" + sender.getName() + ")");
        Hashtable<String, String> properties = getMBeanProperties(sender);
        try {
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            jmx.unregisterObject(new MQTTPublisherInfo(sender), MQTTPublisherInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            if (showError) {
                logger.warn("Cannot de-register MQTTPublisher(" + sender.getName() + ")", exc);
            }
        }
    }

    private Hashtable<String, String> getMBeanProperties(MQTTPublisher sender)
    {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("Name", sender.getName());
        return properties;
    }

    public String getDefaultBrokerUrl() {
        return server.getBrokerUrl();
    }

    public MqttClientPersistence getDataStore() {
        return dataStore;
    }
}

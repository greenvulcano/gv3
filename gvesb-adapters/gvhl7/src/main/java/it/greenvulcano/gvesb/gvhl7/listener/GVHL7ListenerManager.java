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

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.gvesb.gvhl7.listener.jmx.HL7ListenerInfo;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.BaseThread;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.uhn.hl7v2.app.ThreadUtils;

/**
 *
 * @version 3.0.0 28/set/2010
 * @author GreenVulcano Developer Team
 */
public class GVHL7ListenerManager implements ConfigurationListener, ShutdownEventListener
{
    private static Logger                logger                 = GVLogger.getLogger(GVHL7ListenerManager.class);

    private static final String          DEFAULT_CONF_FILE_NAME = "GVHL7ListenerManager.xml";

    private static GVHL7ListenerManager  instance               = null;

    private GreenVulcanoPool             greenVulcanoPool       = null;
    private HashMap<String, HL7Listener> listeners              = new HashMap<String, HL7Listener>();

    public static final String           SUBSYSTEM              = "GVHL7Listener";

    public static synchronized GVHL7ListenerManager instance() throws HL7AdapterException
    {
        if (instance == null) {
            instance = new GVHL7ListenerManager();
        }

        return instance;
    }

    /**
     *
     */
    private GVHL7ListenerManager() throws HL7AdapterException
    {
        XMLConfig.addConfigurationListener(this, DEFAULT_CONF_FILE_NAME);
        ShutdownEventLauncher.addEventListener(this);
        init();
    }

    /**
     *
     */
    private void init() throws HL7AdapterException
    {
        try {
        	ThreadUtils.setCtxClassLoader(this.getClass().getClassLoader());

            Document globalConfig = null;
            try {
                globalConfig = XMLConfig.getDocument(DEFAULT_CONF_FILE_NAME);
            }
            catch (XMLConfigException exc) {
                logger.warn("Error reading HL7 configuration from file: " + DEFAULT_CONF_FILE_NAME , exc);
            }

            if (globalConfig == null) {
                return;
            }

            greenVulcanoPool = GreenVulcanoPoolManager.instance().getGreenVulcanoPool(SUBSYSTEM);
            if (greenVulcanoPool == null) {
                throw new HL7AdapterException("GVHL7_GREENVULCANOPOOL_NOT_CONFIGURED");
            }

            NodeList nl = XMLConfig.getNodeList(DEFAULT_CONF_FILE_NAME,
                    "/GVHL7ListenerManager/HL7Listeners/*[@type='hl7listener']");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                String clazz = XMLConfig.get(n, "@class");
                HL7Listener list = (HL7Listener) Class.forName(clazz).newInstance();
                list.init(n);
                register(list);
                listeners.put(list.getName(), list);
                logger.debug("Configured HL7Listener[" + list.getName() + "]");
                if (list.isAutoStart()) {
                	list.start();
                }
            }
        }
        catch (HL7AdapterException exc) {
            logger.error("Error initializing GVHL7ListenerManager", exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error initializing GVHL7ListenerManager", exc);
            throw new HL7AdapterException("GVHL7_APPLICATION_INIT_ERROR", exc);
        }
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
        logger.debug("BEGIN - Destroing GVHL7ListenerManager");
        try {
            for (Entry<String, HL7Listener> entry : listeners.entrySet()) {
                try {
                	deregister(entry.getValue(), true);
                    entry.getValue().destroy();
                }
                catch (Exception exc) {
                    logger.error("Error destroing HL7Listener[" + entry.getKey() + "]", exc);
                }
            }
            listeners.clear();
        }
        catch (Exception exc) {
            // TODO: handle exception
        }
        logger.debug("END - Destroing GVHL7ListenerManager");
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
        logger.debug("BEGIN - Operation(reload Configuration)");
        if ((evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && evt.getFile().equals(DEFAULT_CONF_FILE_NAME)) {
            destroy();
            // initialize after a delay
            Runnable rr = new Runnable() {
                @Override
                public void run()
                {
                    try {
                        Thread.sleep(10000);
                    }
                    catch (InterruptedException exc) {
                        // do nothing
                    }
                    try {
                        init();
                    }
                    catch (Exception exc) {
                        logger.error("Error initializing GVHL7ListenerManager", exc);
                    }
                }
            };

            BaseThread bt = new BaseThread(rr, "Config reloader for GVHL7ListenerManager");
            bt.setDaemon(true);
            bt.start();
        }
        logger.debug("END - Operation(reload Configuration)");

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
     * Register the listener as MBean.
     * 
     * @param listener
     *        the instance to register.
     */
    private void register(HL7Listener listener)
    {
        logger.debug("Registering MBean for HL7Listener(" + listener.getName() + ")");
        Hashtable<String, String> properties = getMBeanProperties(listener);
        try {
            deregister(listener, false);
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            jmx.registerObject(new HL7ListenerInfo(listener), HL7ListenerInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            logger.warn("Error registering MBean for HL7Listener(" + listener.getName() + ")", exc);
        }
    }

    /**
     * Deregister the listener as MBean.
     * 
     * @param listener
     *        the instance to deregister.
     */
    private void deregister(HL7Listener listener, boolean showError)
    {
        logger.debug("Deregistering MBean for HL7Listener(" + listener.getName() + ")");
        Hashtable<String, String> properties = getMBeanProperties(listener);
        try {
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            jmx.unregisterObject(new HL7ListenerInfo(listener), HL7ListenerInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            if (showError) {
                logger.warn("Cannot de-register HL7Listener(" + listener.getName() + ")", exc);
            }
        }
    }

    private Hashtable<String, String> getMBeanProperties(HL7Listener listener)
    {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("Name", listener.getName());
        return properties;
    }
}

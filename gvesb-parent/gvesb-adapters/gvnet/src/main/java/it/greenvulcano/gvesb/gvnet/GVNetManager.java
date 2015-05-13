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
package it.greenvulcano.gvesb.gvnet;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.gvesb.gvnet.listener.NetReceiver;
import it.greenvulcano.gvesb.gvnet.listener.jmx.NetReceiverInfo;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.GVLogger;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.5.0 18/may/2014
 * @author GreenVulcano Developer Team
 */
public class GVNetManager implements ConfigurationListener, ShutdownEventListener
{
    private static Logger                 logger                 = GVLogger.getLogger(GVNetManager.class);

    private static final String           DEFAULT_CONF_FILE_NAME = "GVNetAdapter-Configuration.xml";

    private static GVNetManager           instance               = null;
    
    private GreenVulcanoPool              greenVulcanoPool       = null;

    private HashMap<String, NetReceiver>  receivers              = new HashMap<String, NetReceiver>();
    //private HashMap<String, NetListener>  listeners              = new HashMap<String, NetListener>();
    //private HashMap<String, NetPublisher> publishers             = new HashMap<String, NetPublisher>();

    public static final String            SUBSYSTEM              = "GVNetListener";

    public static synchronized GVNetManager instance() throws NetAdapterException
    {
        if (instance == null) {
            instance = new GVNetManager();
        }

        return instance;
    }

    /**
     *
     */
    private GVNetManager() throws NetAdapterException
    {
        XMLConfig.addConfigurationListener(this, DEFAULT_CONF_FILE_NAME);
        ShutdownEventLauncher.addEventListener(this);
        init();
    }

    /**
     *
     */
    private void init() throws NetAdapterException
    {
        try {
            logger.debug("Initializing GVNetManager");

            Document globalConfig = null;
            try {
                globalConfig = XMLConfig.getDocument(DEFAULT_CONF_FILE_NAME);
            }
            catch (XMLConfigException exc) {
                logger.warn("Error reading Network configuration from file: " + DEFAULT_CONF_FILE_NAME , exc);
            }

            if (globalConfig == null) {
                return;
            }

            greenVulcanoPool = GreenVulcanoPoolManager.instance().getGreenVulcanoPool(SUBSYSTEM);
            if (greenVulcanoPool == null) {
                throw new NetAdapterException("GVNET_GREENVULCANOPOOL_NOT_CONFIGURED");
            }

            NodeList nl = XMLConfig.getNodeList(DEFAULT_CONF_FILE_NAME,
                    "/GVNetConfiguration/NetListeners/*[@type='net-receiver' and @enabled='true']");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                NetReceiver rec = (NetReceiver) Class.forName(XMLConfig.get(n, "@class")).newInstance();
                rec.init(n);
                register(rec);
                receivers.put(rec.getName(), rec);
                logger.debug("Configured NetReceiver[" + rec.getName() + "]");
                if (rec.isAutoStart()) {
                    rec.start();
                }
            }
        }
        catch (NetAdapterException exc) {
            logger.error("Error initializing GVNetManager", exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error initializing GVNetManager", exc);
            throw new NetAdapterException("GVNET_APPLICATION_INIT_ERROR", exc);
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
        logger.debug("BEGIN - Destroing GVNetManager");
        for (Map.Entry<String, NetReceiver> recEntry : receivers.entrySet()) {
            try {
                deregister(recEntry.getValue(), true);
                recEntry.getValue().destroy();
            }
            catch (Exception exc) {
                logger.error("Error destroing NetReceiver[" + recEntry.getKey() + "]", exc);
            }
        }
        receivers.clear();
        logger.debug("END - Destroing GVNetManager");
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
                logger.error("Error initializing GVNetManager", exc);
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
    private void register(NetReceiver receiver)
    {
        logger.debug("Registering MBean for NetReceiver(" + receiver.getName() + ")");
        Hashtable<String, String> properties = getMBeanProperties(receiver);
        try {
            deregister(receiver, false);
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            jmx.registerObject(new NetReceiverInfo(receiver), NetReceiverInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            logger.warn("Error registering MBean for NetReceiver(" + receiver.getName() + ")", exc);
        }
    }

    /**
     * Deregister the receiver as MBean.
     * 
     * @param receiver
     *        the receiver to deregister.
     */
    private void deregister(NetReceiver receiver, boolean showError)
    {
        logger.debug("Deregistering MBean for NetReceiver(" + receiver.getName() + ")");
        Hashtable<String, String> properties = getMBeanProperties(receiver);
        try {
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            jmx.unregisterObject(new NetReceiverInfo(receiver), NetReceiverInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            if (showError) {
                logger.warn("Cannot de-register NetReceiver(" + receiver.getName() + ")", exc);
            }
        }
    }

    private Hashtable<String, String> getMBeanProperties(NetReceiver receiver)
    {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("Name", receiver.getName());
        return properties;
    }
}

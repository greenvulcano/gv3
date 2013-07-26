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
package it.greenvulcano.gvesb.gvzmq;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.gvesb.gvzmq.listener.ZMQListener;
import it.greenvulcano.gvesb.gvzmq.publisher.ZMQPublisher;
import it.greenvulcano.log.GVLogger;

import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.zeromq.ZContext;
import org.zeromq.ZMsg;

/**
 * 
 * @version 3.2.0 18/03/2012
 * @author GreenVulcano Developer Team
 */
public class GVZMQManager implements ConfigurationListener, ShutdownEventListener
{
    private static Logger                 logger                 = GVLogger.getLogger(GVZMQManager.class);

    private static final String           DEFAULT_CONF_FILE_NAME = "GVZMQ-Configuration.xml";

    private static GVZMQManager           instance               = null;
    private static ZContext               zctx                   = null;

    private GreenVulcanoPool              greenVulcanoPool       = null;
    private HashMap<String, ZMQListener>  listeners              = new HashMap<String, ZMQListener>();
    private HashMap<String, ZMQPublisher> publishers             = new HashMap<String, ZMQPublisher>();

    public static final String            SUBSYSTEM              = "GVZMQListener";

    public static synchronized GVZMQManager instance() throws ZMQAdapterException
    {
        if (instance == null) {
            instance = new GVZMQManager();
        }

        return instance;
    }

    /**
     *
     */
    private GVZMQManager() throws ZMQAdapterException
    {
        XMLConfig.addConfigurationListener(this, DEFAULT_CONF_FILE_NAME);
        ShutdownEventLauncher.addEventListener(this);
        init();
    }

    /**
     *
     */
    private void init() throws ZMQAdapterException
    {
        try {
            if (zctx == null) {
                zctx = new ZContext();
            }

            Document globalConfig = null;
            try {
                globalConfig = XMLConfig.getDocument(DEFAULT_CONF_FILE_NAME);
            }
            catch (XMLConfigException exc) {
                logger.warn("Error reading ZMQ configuration from file: " + DEFAULT_CONF_FILE_NAME, exc);
            }

            if (globalConfig == null) {
                return;
            }

            greenVulcanoPool = GreenVulcanoPoolManager.instance().getGreenVulcanoPool(SUBSYSTEM);
            if (greenVulcanoPool == null) {
                throw new ZMQAdapterException("GVZMQ_GREENVULCANOPOOL_NOT_CONFIGURED");
            }

            NodeList nl = XMLConfig.getNodeList(DEFAULT_CONF_FILE_NAME,
                    "/GVZMQConfiguration/ZMQPublishers/*[@type='zmq-publisher' and @enabled='true']");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                String name = XMLConfig.get(n, "@name");
                ZMQPublisher publ = new ZMQPublisher(name);
                publ.init(n, getZMQContext());
                publishers.put(publ.getName(), publ);
                logger.debug("Configured ZMQPublisher[" + publ.getName() + "]");
                //publ.setDaemon(true);
                publ.start();
            }

            nl = XMLConfig.getNodeList(DEFAULT_CONF_FILE_NAME,
                    "/GVZMQConfiguration/ZMQListeners/*[@type='zmq-listener' and @enabled='true']");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                String name = XMLConfig.get(n, "@name");
                ZMQListener list = new ZMQListener(name);
                list.init(n, getZMQContext());
                listeners.put(list.getName(), list);
                logger.debug("Configured ZMQListener[" + list.getName() + "]");
                //list.setDaemon(true);
                list.start();
            }
        }
        catch (ZMQAdapterException exc) {
            logger.error("Error initializing GVZMQManager", exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error initializing GVZMQManager", exc);
            throw new ZMQAdapterException("GVZMQ_APPLICATION_INIT_ERROR", exc);
        }
    }

    /**
     * @return the greenVulcanoPool
     */
    public GreenVulcanoPool getGreenVulcanoPool()
    {
        return this.greenVulcanoPool;
    }

    /**
     * 
     * @return
     */
    public ZContext getZMQContext()
    {
        return zctx;
    }

    public void publish(String publName, ZMsg message) throws ZMQAdapterException
    {
        ZMQPublisher publ = publishers.get(publName);
        if (publ == null) {
            throw new ZMQAdapterException("GVZMQ_INVALID_PUBLISHER: " + publName);
        }
        publ.publish(message);
    }


    /**
     * 
     */
    public void destroy()
    {
        logger.debug("BEGIN - Destroing GVZMQManager");
        try {
            for (Entry<String, ZMQListener> entry : listeners.entrySet()) {
                try {
                    entry.getValue().Destroy();
                }
                catch (Exception exc) {
                    logger.error("Error destroing ZMQListener[" + entry.getKey() + "]", exc);
                }
            }
            listeners.clear();
        }
        catch (Exception exc) {
            // TODO: handle exception
        }
        try {
            for (Entry<String, ZMQPublisher> entry : publishers.entrySet()) {
                try {
                    entry.getValue().Destroy();
                }
                catch (Exception exc) {
                    logger.error("Error destroing ZMQPublisher[" + entry.getKey() + "]", exc);
                }
            }
            publishers.clear();
        }
        catch (Exception exc) {
            // TODO: handle exception
        }
        logger.debug("END - Destroing GVZMQManager");
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
            try {
                init();
            }
            catch (Exception exc) {
                logger.error("Error initializing GVZMQManager", exc);
            }
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
        try {
            if (zctx != null) {
                logger.debug("Closing ZMQ Context...");
                //zctx.destroy();
                logger.debug("Closed ZMQ Context");
            }
        }
        catch (Exception exc) {
            // do nothing
        }
    }

}

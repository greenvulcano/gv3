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
package it.greenvulcano.gvesb.core.pool;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.core.jmx.GreenVulcanoPoolInfo;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.GVLogger;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 * 
 */
public final class GreenVulcanoPoolManager implements ConfigurationListener
{
    private static final Logger            logger            = GVLogger.getLogger(GreenVulcanoPoolManager.class);
    public static final String             CONF_FILE_NAME    = "GVPoolManager.xml";

    /**
     * The subsystem -> GreenVulcanoPool map.
     */
    private Map<String, GreenVulcanoPool>  greenVulcanoPools = new HashMap<String, GreenVulcanoPool>();
    /**
     * If true the configuration is valid.
     */
    private boolean                        initialized       = false;
    /**
     * The singleton instance.
     */
    private static GreenVulcanoPoolManager _instance         = null;

    /**
     * The singleton entry point.
     * 
     * @return the singleton instance
     */
    public static synchronized GreenVulcanoPoolManager instance()
    {
        if (_instance == null) {
            _instance = new GreenVulcanoPoolManager();
            XMLConfig.addConfigurationListener(_instance, CONF_FILE_NAME);
        }
        return _instance;
    }

    /**
     * Constructor.
     */
    private GreenVulcanoPoolManager()
    {
        // do nothing
    }

    /**
     * Configuration changed. When the configuration changes, the internal
     * chache is removed.
     * 
     * @param event
     *        The configuration event received
     */
    @Override
    public synchronized void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && event.getFile().equals(CONF_FILE_NAME)) {
            initialized = false;
        }
    }

    /**
     * Obtain a GreenVulcanoPool instance.
     * 
     * @param subsystem
     * 
     * @return the required instance
     * @throws Exception
     *         if error occurs
     */
    public GreenVulcanoPool getGreenVulcanoPool(String subsystem) throws Exception
    {
        initGreenVulcanoPool();

        logger.debug("Requested GreenVulcanoPool(" + subsystem + ")");
        return greenVulcanoPools.get(subsystem);
    }

    /**
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        XMLConfig.removeConfigurationListener(this);
        destroyGreenVulcanoPool();
    }

    /**
     * Initialize the pool.
     * 
     * @throws Exception
     *         if error occurs
     */
    private void initGreenVulcanoPool() throws Exception
    {
        if (initialized) {
            return;
        }

        synchronized (_instance) {
            if (initialized) {
                return;
            }

            logger.debug("Initializing GreenVulcanoPoolManager");
            if (greenVulcanoPools.isEmpty()) {
                NodeList nl = XMLConfig.getNodeList(CONF_FILE_NAME, "//GreenVulcanoPool");
                for (int i = 0; i < nl.getLength(); i++) {
                    GreenVulcanoPool GreenVulcanoPool = new GreenVulcanoPool(nl.item(i));
                    logger.debug("Initialized GreenVulcanoPool(" + GreenVulcanoPool.getSubsystem() + ")");
                    register(GreenVulcanoPool);
                    greenVulcanoPools.put(GreenVulcanoPool.getSubsystem(), GreenVulcanoPool);
                }
                initialized = true;
                return;
            }

            Map<String, GreenVulcanoPool> tmp = new HashMap<String, GreenVulcanoPool>();
            NodeList nl = XMLConfig.getNodeList(CONF_FILE_NAME, "//GreenVulcanoPool");
            for (int p = 0; p < nl.getLength(); p++) {
                GreenVulcanoPool GreenVulcanoPool = greenVulcanoPools.remove(XMLConfig.get(nl.item(p), "@subsystem"));
                if (GreenVulcanoPool == null) {
                    GreenVulcanoPool = new GreenVulcanoPool(nl.item(p));
                }
                else {
                    GreenVulcanoPool.init(nl.item(p));
                }
                logger.debug("Initialized GreenVulcanoPool(" + GreenVulcanoPool.getSubsystem() + ")");
                register(GreenVulcanoPool);
                tmp.put(GreenVulcanoPool.getSubsystem(), GreenVulcanoPool);
            }

            for (GreenVulcanoPool GreenVulcanoPool : greenVulcanoPools.values()) {
                logger.debug("Destroying GreenVulcanoPool(" + GreenVulcanoPool.getSubsystem() + ")");
                deregister(GreenVulcanoPool, true);
                try {
                    GreenVulcanoPool.destroy();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }

            greenVulcanoPools.putAll(tmp);

            initialized = true;
        }
    }

    /**
     * Destroy the pools.
     * 
     */
    private void destroyGreenVulcanoPool()
    {
        for (GreenVulcanoPool GreenVulcanoPool : greenVulcanoPools.values()) {
            try {
                logger.debug("Destroying GreenVulcanoPool(" + GreenVulcanoPool.getSubsystem() + ")");
                deregister(GreenVulcanoPool, true);
                GreenVulcanoPool.destroy();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        greenVulcanoPools.clear();
    }

    /**
     * Register the pool as MBean.
     * 
     * @param pool
     *        the instance to register.
     */
    private void register(GreenVulcanoPool pool)
    {
        logger.debug("Registering MBean for GreenVulcanoPool(" + pool.getSubsystem() + ")");
        Hashtable<String, String> properties = getMBeanProperties(pool);
        try {
            deregister(pool, false);
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            jmx.registerObject(new GreenVulcanoPoolInfo(pool), GreenVulcanoPoolInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            logger.warn("Error registering MBean for GreenVulcanoPool(" + pool.getSubsystem() + ")", exc);
        }
    }

    /**
     * Deregister the pool as MBean.
     * 
     * @param pool
     *        the instance to deregister.
     */
    private void deregister(GreenVulcanoPool pool, boolean showError)
    {
        logger.debug("Deregistering MBean for GreenVulcanoPool(" + pool.getSubsystem() + ")");
        Hashtable<String, String> properties = getMBeanProperties(pool);
        try {
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            jmx.unregisterObject(new GreenVulcanoPoolInfo(pool), GreenVulcanoPoolInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            if (showError) {
                logger.warn("Cannot de-register GreenVulcano ESB Pool Manager.", exc);
            }
        }
    }

    private Hashtable<String, String> getMBeanProperties(GreenVulcanoPool pool)
    {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("Subsystem", pool.getSubsystem());
        return properties;
    }
}
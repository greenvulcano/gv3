/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.core.forward;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.core.forward.jms.JMSForwardData;
import it.greenvulcano.gvesb.core.forward.jms.JMSForwardListenerPool;
import it.greenvulcano.gvesb.core.forward.jmx.JMSForwardListenerPoolInfo;
import it.greenvulcano.gvesb.core.forward.preprocess.ValidatorManager;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.BaseThread;
import it.greenvulcano.log.NMDC;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.2.0 11/gen/2012
 * @author GreenVulcano Developer Team
 */
public class JMSForwardManager implements ConfigurationListener, ShutdownEventListener
{
    private static final Logger                     logger                = GVLogger.getLogger(JMSForwardManager.class);
    /**
     * JMS Forward configuration file name.
     */
    private static String                           JMS_FORWARD_FILE_NAME = "GVJMSForward.xml";
    private static JMSForwardManager                instance              = null;

    private List<JMSForwardListenerPool> jmsListeners          = new ArrayList<JMSForwardListenerPool>();
    private String 									serverName;

    private JMSForwardManager()
    {
        // do nothing
    }

    /**
    *
    */
    public static synchronized JMSForwardManager instance() throws JMSForwardException
    {
        if (instance == null) {
            instance = new JMSForwardManager();
            XMLConfig.addConfigurationListener(instance, JMS_FORWARD_FILE_NAME);
            ShutdownEventLauncher.addEventListener(instance);
            instance.init();
        }
        return instance;
    }

    /**
    *
    */
    private void init() throws JMSForwardException
    {
    	serverName = JMXEntryPoint.getServerName();
    	
        logger.debug("Initializing JMSForwardManager");
        NMDC.push();
        NMDC.clear();
        NMDC.setServer(serverName);
        NMDC.setSubSystem(JMSForwardData.SUBSYSTEM);
        try {
            NodeList nl = XMLConfig.getNodeList(JMS_FORWARD_FILE_NAME,
                    "/GVForwards/ForwardConfiguration[@enabled='true']");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                JMSForwardListenerPool jmsLP = new JMSForwardListenerPool();
                jmsLP.init(n);
                jmsListeners.add(jmsLP);
                logger.debug("Configured JMSForwardListenerPool[" + jmsLP.getName() + "/" + jmsLP.getForwardName() + "]");
                register(jmsLP);
            }
        }
        catch (JMSForwardException exc) {
            logger.error("Error initializing JMSForwardManager", exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error initializing JMSForwardManager", exc);
            throw new JMSForwardException("GVJMS_APPLICATION_INIT_ERROR", exc);
        }
        finally {
            NMDC.pop();
        }
    }

    public void destroy()
    {
        NMDC.push();
        NMDC.clear();
        NMDC.setServer(serverName);
        NMDC.setSubSystem(JMSForwardData.SUBSYSTEM);
        try {
            logger.debug("BEGIN - Destroing JMSForwardManager");
            ValidatorManager.instance().reset();
            for (JMSForwardListenerPool pool : jmsListeners) {
                String forwardName = pool.getName() + "/" + pool.getForwardName();
                try {
                    deregister(pool, true);
                    pool.destroy();
                }
                catch (Exception exc) {
                    logger.error("Error destroing JMSForwardListenerPool[" + forwardName + "]", exc);
                }
            }
            jmsListeners.clear();
            logger.debug("END - Destroing JMSForwardManager");
    	}
        finally {
            NMDC.pop();
        }
    }

    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        logger.debug("BEGIN - JMSForwardManager Operation(reload Configuration)");
        if ((evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && evt.getFile().equals(JMS_FORWARD_FILE_NAME)) {
             // destroy now
            destroy();
            // initialize after a delay
            Runnable rr = new Runnable() {
                @Override
                public void run()
                {
                    try {
                        Thread.sleep(20000);
                    }
                    catch (InterruptedException exc) {
                        // do nothing
                    }
                    try {
                        init();
                    }
                    catch (Exception exc) {
                        logger.error("Error initializing JMSForwardManager", exc);
                    }
                }
            };

            BaseThread bt = new BaseThread(rr, "Config reloader for: JMSForwardManager");
            bt.setDaemon(true);
            bt.start();
        }
        logger.debug("END - JMSForwardManager Operation(reload Configuration)");
    }

    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
        destroy();
    }

    /**
     * Register the pool as MBean.
     * 
     * @param pool
     *        the instance to register.
     */
    private void register(JMSForwardListenerPool pool)
    {
        logger.debug("Registering MBean for JMSForwardListenerPool(" + pool.getName() + "/" + pool.getForwardName() + ")");
        Hashtable<String, String> properties = getMBeanProperties(pool);
        try {
            deregister(pool, false);
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            jmx.registerObject(new JMSForwardListenerPoolInfo(pool.getData()),
                    JMSForwardListenerPoolInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            logger.warn("Error registering MBean for JMSForwardListenerPool(" + pool.getName() + "/" + pool.getForwardName() + ")", exc);
        }
    }

    /**
     * Deregister the pool as MBean.
     * 
     * @param pool
     *        the instance to deregister.
     */
    private void deregister(JMSForwardListenerPool pool, boolean showError)
    {
        if (showError) {
            logger.debug("Deregistering MBean for JMSForwardListenerPool(" + pool.getName() + "/" + pool.getForwardName() + ")");
        }
        Hashtable<String, String> properties = getMBeanProperties(pool);
        try {
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            jmx.unregisterObject(new JMSForwardListenerPoolInfo(pool.getData()),
                    JMSForwardListenerPoolInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            if (showError) {
                logger.warn("Cannot de-register JMSForwardListenerPool.", exc);
            }
        }
    }

    private Hashtable<String, String> getMBeanProperties(JMSForwardListenerPool pool)
    {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("Forward", pool.getName());
        return properties;
    }
}

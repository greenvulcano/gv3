
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
package it.greenvulcano.gvesb.jbpmhumantask.listener;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.log.GVLogger;

import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.0.0 28/set/2010
 * @author GreenVulcano Developer Team
 */
public class GVJbpmTaskListenerManager implements ConfigurationListener, ShutdownEventListener
{
    private static Logger                logger                 = GVLogger.getLogger(GVJbpmTaskListenerManager.class);

    private static final String          DEFAULT_CONF_FILE_NAME = "GVJbpmTaskListenerManager.xml";
    public static final String           SUBSYSTEM              = "GVJBPMListener";

    private static GVJbpmTaskListenerManager  instance               = null;

    private GreenVulcanoPool             greenVulcanoPool       = null;
    private HashMap<String, JbpmListnerTask> listeners              = new HashMap<String, JbpmListnerTask>();

    public static synchronized GVJbpmTaskListenerManager instance() throws GVJbpmHumanTaskAdapterException
    {
        if (instance == null) {
            instance = new GVJbpmTaskListenerManager();
        }

        return instance;
    }

    /**
     *
     */
    private GVJbpmTaskListenerManager() throws GVJbpmHumanTaskAdapterException
    {
        XMLConfig.addConfigurationListener(this, DEFAULT_CONF_FILE_NAME);
        ShutdownEventLauncher.addEventListener(this);
        init();
    }

    /**
     *
     */
    private void init() throws GVJbpmHumanTaskAdapterException
    {
        try {
            greenVulcanoPool = GreenVulcanoPoolManager.instance().getGreenVulcanoPool(SUBSYSTEM);
            if (greenVulcanoPool == null) {
                throw new GVJbpmHumanTaskAdapterException("GVJBPMTASK_GREENVULCANOPOOL_NOT_CONFIGURED");
            }

            NodeList nl = XMLConfig.getNodeList(DEFAULT_CONF_FILE_NAME,
                    "/GVJbpmTaskListenerManager/GVJbpmListeners/*[@type='gvjbpmlistener']");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                String clazz = XMLConfig.get(n, "@class");
                System.out.println("class="+clazz);
                JbpmListnerTask list = (JbpmListnerTask) Class.forName(clazz).newInstance();
                list.init(n);
                listeners.put(list.getName(), list);
                list.start();
                logger.debug("Configured jbpmTasklistener[" + list.getName() + "]");
            }
        }
        catch (Exception exc) {
        	exc.printStackTrace();
            logger.error("Error initializing JbpmTaskListenerManager", exc);
            throw new GVJbpmHumanTaskAdapterException("JBPMTASK_APPLICATION_INIT_ERROR", exc);
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
        logger.debug("BEGIN - Destroing JBPMTASKListenerManager");
        try {
            for (Entry<String, JbpmListnerTask> entry : listeners.entrySet()) {
                try {
                    entry.getValue().destroy();
                }
                catch (Exception exc) {
                    logger.error("Error destroing JBPMTASKListener[" + entry.getKey() + "]", exc);
                }
            }
            listeners.clear();
        }
        catch (Exception exc) {
            // TODO: handle exception
        	exc.printStackTrace();
        }
        logger.debug("END - Destroing JBPMTASKListenerManager");
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
            	exc.printStackTrace();
                logger.error("Error initializing JBPMTASKListenerManager", exc);
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
    }

}

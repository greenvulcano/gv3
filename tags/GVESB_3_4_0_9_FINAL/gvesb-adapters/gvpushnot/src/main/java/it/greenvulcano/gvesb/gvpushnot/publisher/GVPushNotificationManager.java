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
package it.greenvulcano.gvesb.gvpushnot.publisher;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.gvpushnot.publisher.jmx.NotificationEngineInfo;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.BaseThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @version 3.4.0 16/feb/2015
 * @author GreenVulcano Developer Team
 */
public class GVPushNotificationManager implements ConfigurationListener, ShutdownEventListener
{
    private static Logger                logger                 = GVLogger.getLogger(GVPushNotificationManager.class);

    private static final String          DEFAULT_CONF_FILE_NAME = "GVPushNotificationManager.xml";

    private static GVPushNotificationManager    instance             = null;

    private HashMap<String, NotificationEngine> engines              = new HashMap<String, NotificationEngine>();

    public static final String                  SUBSYSTEM            = "GVPushNotification";

    public static synchronized GVPushNotificationManager instance() throws PushNotificationException
    {
        if (instance == null) {
            instance = new GVPushNotificationManager();
        }

        return instance;
    }

    /**
     *
     */
    private GVPushNotificationManager() throws PushNotificationException
    {
        XMLConfig.addConfigurationListener(this, DEFAULT_CONF_FILE_NAME);
        ShutdownEventLauncher.addEventListener(this);
        init();
    }

    /**
     *
     */
    private void init() throws PushNotificationException
    {
        try {
            Document globalConfig = null;
            try {
                globalConfig = XMLConfig.getDocument(DEFAULT_CONF_FILE_NAME);
            }
            catch (XMLConfigException exc) {
                logger.warn("Error reading PushNotification configuration from file: " + DEFAULT_CONF_FILE_NAME , exc);
            }

            if (globalConfig == null) {
                return;
            }

            NodeList nl = XMLConfig.getNodeList(DEFAULT_CONF_FILE_NAME,
                    "/GVPushNotificationManager/NotificationEngines/*[@type='pushnotif']");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                String clazz = XMLConfig.get(n, "@class");
                NotificationEngine eng = (NotificationEngine) Class.forName(clazz).newInstance();
                eng.init(n);
                register(eng);
                engines.put(eng.getName(), eng);
                logger.debug("Configured NotificationEngine[" + eng.getName() + "]");
                if (eng.isAutoStart()) {
                	eng.start();
                }
            }
        }
        catch (PushNotificationException exc) {
            logger.error("Error initializing GVPushNotificationManager", exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error initializing GVPushNotificationManager", exc);
            throw new PushNotificationException("GVPUSHNOT_APPLICATION_INIT_ERROR", exc);
        }
    }
    
	public NotificationResult push(Notification notification) throws PushNotificationException {
		try {
			NotificationEngine engine = checkActiveEngine(notification.getEngineName());
			NotificationResult nr = engine.push(notification);

			return nr;
		}
		catch (PushNotificationException exc) {
			logger.error("Error sending notification", exc);
			throw exc;
		}
		catch (Exception exc) {
			logger.error("Error sending notification", exc);
			throw new PushNotificationException("Error sending notification", exc);
		}
	}

	public NotificationResult push(List<Notification> notifications) throws PushNotificationException {
		Map<String, List<Notification>> splittedNotifications = splittByEngine(notifications);
		NotificationResult nr = new NotificationResult();
		for (Map.Entry<String, List<Notification>> splittedList : splittedNotifications.entrySet()) {
			NotificationEngine engine = checkActiveEngine(splittedList.getKey());
			nr.addAllResults(engine.push(splittedList.getValue()));
		}

		return nr; 
	}

	public FeedbackResult getFeedback(String engineName) throws PushNotificationException {
		try {
			NotificationEngine engine = checkActiveEngine(engineName);
			FeedbackResult fr = engine.getFeedback();

			return fr;
		}
		catch (PushNotificationException exc) {
			logger.error("Error reading feedback", exc);
			throw exc;
		}
		catch (Exception exc) {
			logger.error("Error reading feedback", exc);
			throw new PushNotificationException("Error reading feedback", exc);
		}
	}


    public void destroy()
    {
        logger.debug("BEGIN - Destroing GVPushNotificationManager");
        try {
            for (Entry<String, NotificationEngine> entry : engines.entrySet()) {
                try {
                	deregister(entry.getValue(), true);
                    entry.getValue().destroy();
                }
                catch (Exception exc) {
                    logger.error("Error destroing NotificationEngine[" + entry.getKey() + "]", exc);
                }
            }
            engines.clear();
        }
        catch (Exception exc) {
            // TODO: handle exception
        }
        logger.debug("END - Destroing GVPushNotificationManager");
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
                        logger.error("Error initializing GVPushNotificationManager", exc);
                    }
                }
            };

            BaseThread bt = new BaseThread(rr, "Config reloader for GVPushNotificationManager");
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

    private Map<String, List<Notification>> splittByEngine(List<Notification> notifications) throws PushNotificationException {
    	Map<String, List<Notification>> splittedNotifications = new HashMap<String, List<Notification>>();
    	for (Notification notification : notifications) {
    		List<Notification> nl = splittedNotifications.get(notification.getEngineName());
    		if (nl == null) {
    			checkActiveEngine(notification.getEngineName());
    			nl = new ArrayList<Notification>();
    			splittedNotifications.put(notification.getEngineName(), nl);
    		}
			nl.add(notification);
		}
    	return splittedNotifications;
    }
    
    private NotificationEngine checkActiveEngine(String engineName) throws PushNotificationException {
    	NotificationEngine engine = engines.get(engineName);
		if (engine == null) {
			throw new PushNotificationException("Invalid engine name: " + engineName);
		}
		if (!engine.isActive()) {
			throw new PushNotificationException("Inactive engine: " + engineName);
		}
		return engine;
	}

	/**
     * Register the engine as MBean.
     * 
     * @param engine
     *        the instance to register.
     */
    private void register(NotificationEngine engine)
    {
        logger.debug("Registering MBean for NotificationEngine(" + engine.getName() + ")");
        Hashtable<String, String> properties = getMBeanProperties(engine);
        try {
            deregister(engine, false);
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            jmx.registerObject(new NotificationEngineInfo(engine), NotificationEngineInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            logger.warn("Error registering MBean for NotificationEngine(" + engine.getName() + ")", exc);
        }
    }

    /**
     * Deregister the engine as MBean.
     * 
     * @param engine
     *        the instance to deregister.
     */
    private void deregister(NotificationEngine engine, boolean showError)
    {
        logger.debug("Deregistering MBean for NotificationEngine(" + engine.getName() + ")");
        Hashtable<String, String> properties = getMBeanProperties(engine);
        try {
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            jmx.unregisterObject(new NotificationEngineInfo(engine), NotificationEngineInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            if (showError) {
                logger.warn("Cannot de-register NotificationEngine(" + engine.getName() + ")", exc);
            }
        }
    }

    private Hashtable<String, String> getMBeanProperties(NotificationEngine engine)
    {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("Name", engine.getName());
        return properties;
    }
}

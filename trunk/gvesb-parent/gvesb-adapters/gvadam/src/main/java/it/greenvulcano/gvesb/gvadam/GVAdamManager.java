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
package it.greenvulcano.gvesb.gvadam;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.log.GVLogger;

import java.util.HashMap;
import java.util.Map;

import net.sf.adam.an.ApplicationSession;
import net.sf.adam.core.AdamException;
import net.sf.adam.core.Engine;
import net.sf.adam.core.sec.Security;
import net.sf.adam.core.sec.auth.DefaultLoginHandler;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.4.0 30/apr/2014
 * @author GreenVulcano Developer Team
 */
public class GVAdamManager implements ConfigurationListener, ShutdownEventListener
{
    private static Logger        logger                 = GVLogger.getLogger(GVAdamManager.class);

    private static final String  DEFAULT_CONF_FILE_NAME = "GVAdamAdapter-Configuration.xml";

    private static GVAdamManager instance               = null;
	private Engine               engine                 = null; 
			
    private Map<String, DefaultLoginHandler> sessions = new HashMap<String, DefaultLoginHandler>();

    public static synchronized GVAdamManager instance() throws AdamAdapterException
    {
        if (instance == null) {
            instance = new GVAdamManager();
        }

        return instance;
    }

    /**
     *
     */
    private GVAdamManager() throws AdamAdapterException
    {
        XMLConfig.addConfigurationListener(this, DEFAULT_CONF_FILE_NAME);
        ShutdownEventLauncher.addEventListener(this);
        init();
    }

    /**
     *
     */
    private void init() throws AdamAdapterException
    {
        try {
            logger.debug("Initializing GVAdamManager");

            Document globalConfig = null;
            try {
                globalConfig = XMLConfig.getDocument(DEFAULT_CONF_FILE_NAME);
            }
            catch (XMLConfigException exc) {
                logger.warn("Error reading Adam configuration from file: " + DEFAULT_CONF_FILE_NAME , exc);
            }

            if (globalConfig == null) {
                return;
            }
            
            NodeList sNodes = XMLConfig.getNodeList(globalConfig, "GVAdamAdapterManager/SessionConfiguration/Session");
            //DefaultLoginHandler session = null;
            for (int i = 0; i < sNodes.getLength(); i++) {
                Node fn = sNodes.item(i);
                String uSession = XMLConfig.get(fn, "@name");
                String user = XMLConfig.get(fn, "@user");
                String password = XMLConfig.getDecrypted(fn, "@password");
                String realm = XMLConfig.get(fn, "@realm");
                sessions.put(uSession, new DefaultLoginHandler(user, password, realm));
                logger.debug("Added a Session configuration [name=" + uSession + ", user=" + user + ", realm=" + realm + "].");
            }

        }
        catch (Exception exc) {
            logger.error("Error initializing GVAdamManager", exc);
            throw new AdamAdapterException("GVADAM_APPLICATION_INIT_ERROR", exc);
        }
    }


    public void destroy()
    {
        logger.debug("BEGIN - Destroing GVAdamManager");
        sessions.clear();
        try {
			Engine.instance().shutdown();
        }
        catch (Exception exc) {
            logger.warn("Error while shutting down ADAM engine", exc);
        }
        engine = null;
        logger.debug("END - Destroing GVAdamManager");
    }

    private synchronized Engine getAdamEngine(){
    	if (engine == null) {
    		engine = Engine.instance();
    		engine.startup(GVAdamManager.class.getClassLoader());
    	}
    	return engine;
    }
    
    /**
	 * @return
	 * @throws AdamAdapterException
	 * @throws AdamException
	 */
	public ApplicationSession getSession(String uSession, String archive) throws AdamAdapterException, AdamException {
		//Instantiating ADAM engine
		Engine engine = instance().getAdamEngine();
		
		DefaultLoginHandler callback = sessions.get(uSession);
		if (callback == null) {
			throw new AdamAdapterException("Unknown ADAM session. Session[name=" + uSession + "].");
		}
		ApplicationSession session = engine.createSession(Security.DEFAULT_LOGIN_MODULE, callback, archive, null);
		return session;
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
                logger.error("Error initializing GVAdamManager", exc);
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

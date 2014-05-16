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

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 *
 * @version 3.4.0 30/apr/2014
 * @author GreenVulcano Developer Team
 */
public class GVAdamManager implements ConfigurationListener, ShutdownEventListener
{
    private static Logger                logger                 = GVLogger.getLogger(GVAdamManager.class);

    private static final String          DEFAULT_CONF_FILE_NAME = "GVAdamAdapter-Configuration.xml";

    private static GVAdamManager  instance               = null;

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

            if (false) {
                throw new AdamAdapterException("BOOOOM!!!");
            }
        }
        catch (AdamAdapterException exc) {
            logger.error("Error initializing GVAdamManager", exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error initializing GVAdamManager", exc);
            throw new AdamAdapterException("GVADAM_APPLICATION_INIT_ERROR", exc);
        }
    }


    public void destroy()
    {
        logger.debug("BEGIN - Destroing GVAdamManager");
        try {

        }
        catch (Exception exc) {
            // TODO: handle exception
        }
        logger.debug("END - Destroing GVAdamManager");
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

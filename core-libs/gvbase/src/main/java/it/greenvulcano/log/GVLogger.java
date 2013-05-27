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
package it.greenvulcano.log;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;

/**
 * This class is an central point for access log4j functionalities.
 * <p>
 * The GreenVulcano code MUST NOT call <code>Logger.getLogger()</code> directly,
 * but MUST call <code>GVLogger.getLogger()</code> method. This ensure a correct
 * initialization mechanism. The returned Logger is a classic log4j logger.
 * <p>
 * This class is a <code>ConfigurationListener</code> that reconfigures log4j if
 * the configuration file is reloaded.<br>
 * The <code>reload()</code> mehod can be used in order to reconfigure log4j.<br>
 * <code>getConfigurationFile()</code> return the configuration file name used by
 * <code>GVLogger</code>.
 * <p>
 *
 * Note that the reloading mechanism is a little bit complicated, but this
 * uniforms the configuration reloading mechanism for all GreenVulcano classes.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 **/
public final class GVLogger
{
    /**
     * Flag for initialization status.
     */
    private static boolean               initialized           = false;
    /**
     * Default configuration file name.
     */
    private static String                configurationFile     = "gv-log4j.xml";
    /**
     * XMLConfig event listener instance.
     */
    private static ConfigurationListener configurationListener = null;

    /**
     * Constructor.
     */
    private GVLogger()
    {
        // do nothing
    }

    /**
     * Initialize log4j.
     */
    private static synchronized void init()
    {
        if (initialized) {
            return;
        }

        initialized = true;

        if (configurationListener == null) {
            configurationListener = new ConfigurationListener() {
                public void configurationChanged(ConfigurationEvent event)
                {
                    GVLogger.configurationChanged(event);
                }
            };

            XMLConfig.addConfigurationListener(configurationListener, configurationFile);

            try {
                XMLConfig.load(configurationFile);
            }
            catch (XMLConfigException exc) {
                exc.printStackTrace();
            }
        }
    }

    /**
     * @return the configuration file name.
     */
    public static String getConfigurationFile()
    {
        return configurationFile;
    }

    /**
     * Set the configuration file name. This method must be called before use
     * getLogger() method.
     *
     * @param _configurationFile
     *        the configuration file name
     * @exception IllegalStateException
     *            if the GVLogger is alredy initialized (getLogger() was already
     *            called).
     */
    public static void setConfigurationFile(String _configurationFile) throws IllegalStateException
    {
        if (initialized) {
            throw new IllegalStateException("Cannot set the configuration file: Log4J already initialized.");
        }
        configurationFile = _configurationFile;
    }

    /**
     * Return the Logger for the given class.
     *
     * @param cls
     *        the logger class
     * @return the requested logger
     */
    public static Logger getLogger(Class<?> cls)
    {
        if (!initialized) {
            init();
        }
        return Logger.getLogger(cls);
    }

    /**
     * Return the Logger for the given logger name.
     *
     * @param logger
     *        the logger name
     * @return the requested logger
     */
    public static Logger getLogger(String logger)
    {
        if (!initialized) {
            init();
        }
        return Logger.getLogger(logger);
    }

    /**
     * Reload the log4j configuration.
     */
    public static void reload()
    {
        try {
            XMLConfig.reload(configurationFile);
        }
        catch (XMLConfigException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * XMLConfig changed. Reconfigure Log4J if the configurationFile is
     * reloaded.
     *
     * @param event
     *        the configuration event
     */
    private static void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_LOADED) && (event.getFile().equals(configurationFile))) {
            System.out.println("Initializing Log4J with URL: " + event.getURL());
            Document document;
            try {
                document = XMLConfig.getDocument(configurationFile);
                DOMConfigurator.configure(document.getDocumentElement());
            }
            catch (XMLConfigException exc) {
                exc.printStackTrace();
            }
        }
    }
}

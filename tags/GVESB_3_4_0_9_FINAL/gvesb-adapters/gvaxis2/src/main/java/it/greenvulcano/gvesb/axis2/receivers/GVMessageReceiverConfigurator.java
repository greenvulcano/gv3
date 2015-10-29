/*
 * Copyright (c) 2009-2011 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.axis2.receivers;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.axis2.config.WebServiceConf;
import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.log.GVLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

/**
 * @version 3.1.0 Aug 23, 2011
 * @author GreenVulcano Developer Team
 *
 */
public class GVMessageReceiverConfigurator implements ConfigurationListener
{
    private static final Logger         logger               = GVLogger.getLogger(GVMessageReceiverConfigurator.class);

    private String                      configFileName       = "GVAdapters.xml";
    private Map<String, WebServiceConf> webServicesConfig    = new HashMap<String, WebServiceConf>();
    private GreenVulcanoPool            greenVulcanoPool;
    private boolean                     configurationChanged = false;

    private static final String         SUBSYSTEM            = "GreenVulcano-WebServices";

    /**
     * @throws Exception
     *
     */
    public GVMessageReceiverConfigurator() throws Exception
    {
        try {
            initWebServicesConfig();
        }
        catch (Exception exc) {
            logger.error("Cannot initalize WebServices", exc);
            throw exc;
        }
        XMLConfig.addConfigurationListener(this, configFileName);
    }

    /**
     * @throws XMLConfigException
     * @throws GVCoreException
     * @throws GVPublicException
     * @throws IOException
     */
    public void initWebServicesConfig() throws XMLConfigException, GVCoreException, GVPublicException, IOException
    {
        String baseWSPath = "/GVAdapters/GVWebServices/BusinessWebServices";
        try {
            greenVulcanoPool = GreenVulcanoPoolManager.instance().getGreenVulcanoPool(SUBSYSTEM);
            if (greenVulcanoPool == null) {
                throw new GVPublicException("GVWS_GREENVULCANOPOOL_NOT_CONFIGURED");
            }
        }
        catch (Exception exc) {
            throw new GVCoreException("GVWS_APPLICATION_INIT_ERROR", exc);
        }

        NodeList webServices = XMLConfig.getNodeList(configFileName, baseWSPath + "/WebService");

        if (webServices != null && webServices.getLength() > 0) {
            for (int i = 0; i < webServices.getLength(); i++) {
                WebServiceConf webService = new WebServiceConf();
                webService.init(webServices.item(i));
                webServicesConfig.put(webService.getWebService(), webService);
            }
        }
        configurationChanged = false;
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        configurationChanged = true;
    }

    /**
     * @throws Exception
     *
     */
    public void checkConfig() throws Exception
    {
        if (configurationChanged) {
            initWebServicesConfig();
        }
    }

    /**
     * @return the web services configuration map
     */
    public Map<String, WebServiceConf> getWebServicesConfig()
    {
        return webServicesConfig;
    }

    /**
     * @return the GV pool configuration
     */
    public GreenVulcanoPool getGreenVulcanoPool()
    {
        return greenVulcanoPool;
    }

}

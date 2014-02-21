/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.adapter.http;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.adapter.http.formatters.FormatterManager;
import it.greenvulcano.gvesb.adapter.http.formatters.handlers.AdapterHttpConfigurationException;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpConstants;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpInitializationException;
import it.greenvulcano.log.GVLogger;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * HttpServletMappingManager class
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class HttpServletMappingManager implements ConfigurationListener
{
    private static Logger                   logger               = GVLogger.getLogger(HttpServletMappingManager.class);

    private Map<String, HttpServletMapping> mappings             = new HashMap<String, HttpServletMapping>();

    private HttpServletTransactionManager   transactionManager   = null;

    private FormatterManager                formatterMgr         = null;

    private boolean                         configurationChanged = false;

    public HttpServletMappingManager() throws AdapterHttpConfigurationException
    {
        init();
    }

    /**
     * @throws AdapterHttpConfigurationException
     */
    public void init() throws AdapterHttpConfigurationException
    {
        try {
            formatterMgr = new FormatterManager();
            transactionManager = new HttpServletTransactionManager();
            initMappings();
            configurationChanged = false;
            XMLConfig.addConfigurationListener(this, AdapterHttpConstants.CFG_FILE);
        }
        catch (Exception exc) {
            logger.error("HttpServletMappingManager - Initialization error: ", exc);
            throw new AdapterHttpConfigurationException("HttpServletMappingManager - Initialization error", exc);
        }
    }

    /**
     * @param action
     * @return the HTTP servlet mapping
     * @throws AdapterHttpConfigurationException
     */
    public HttpServletMapping getMapping(String action) throws AdapterHttpConfigurationException
    {
        if (configurationChanged) {
            init();
        }
        HttpServletMapping smapping = mappings.get(action);

        if (smapping == null) {
            throw new AdapterHttpConfigurationException("HttpServletMappingManager - Mapping '" + action
                    + "' not found");
        }

        return smapping;
    }

    /**
     *
     */
    public void destroy()
    {
        XMLConfig.removeConfigurationListener(this);
        for (HttpServletMapping mapping : mappings.values()) {
            mapping.destroy();
        }
        mappings.clear();
        if (transactionManager != null) {
            transactionManager.destroy();
            transactionManager = null;
        }
        if (formatterMgr != null) {
            formatterMgr.destroy();
            formatterMgr = null;
        }
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        if ((evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED)
                && evt.getFile().equals(AdapterHttpConstants.CFG_FILE)) {
            configurationChanged = true;
            destroy();
            logger.info("HttpServletMappingManager: Configuration Event received for file: "
                    + AdapterHttpConstants.CFG_FILE);
        }
    }

    private void initMappings() throws AdapterHttpConfigurationException, AdapterHttpInitializationException
    {
        try {
            NodeList mappingNodes = XMLConfig.getNodeList(AdapterHttpConstants.CFG_FILE,
                    "/GVAdapterHttpConfiguration/InboundConfiguration/ActionMappings/ActionMapping[@enabled='true']");

            if (mappingNodes != null) {
                for (int i = 0; i < mappingNodes.getLength(); i++) {
                    Node confNode = mappingNodes.item(i);
                    HttpServletMapping smapping = new HttpServletMapping();
                    smapping.init(transactionManager, formatterMgr, confNode);
                    mappings.put(smapping.getAction(), smapping);
                    if (!smapping.getAction().startsWith("/")) {
                        mappings.put("/" + smapping.getAction(), smapping);
                    }
                }
            }
        }
        catch (XMLConfigException exc) {
            logger.error("HttpServletMappingManager - Error while accessing configuration informations via XMLConfig: "
                    + exc);
            throw new AdapterHttpConfigurationException("GVHA_XML_CONFIG_ERROR", exc);
        }
    }
}
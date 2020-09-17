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
package it.greenvulcano.gvesb.gvrules.drools.config;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.gvrules.drools.RulesException;
import it.greenvulcano.log.GVLogger;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 10/feb/2012
 * @author GreenVulcano Developer Team
 */
public class GVRulesConfigManager implements ShutdownEventListener, ConfigurationListener
{
    private static final Logger              logger    = GVLogger.getLogger(GVRulesConfigManager.class);
    public static String                     CFG_FILE  = "GVRules-Configuration.xml";
    private static GVRulesConfigManager      instance  = null;

    private Map<String, KnowledgeBaseConfig> knbCfgMap = new HashMap<String, KnowledgeBaseConfig>();

    private GVRulesConfigManager() throws RulesException
    {
        init();
    }

    public static synchronized GVRulesConfigManager instance() throws RulesException
    {
        if (instance == null) {
            instance = new GVRulesConfigManager();
            ShutdownEventLauncher.addEventListener(instance);
            XMLConfig.addConfigurationListener(instance, CFG_FILE);
        }
        return instance;
    }

    private void init() throws RulesException
    {
        // TODO Auto-generated method stub
    }

    public StatelessKnowledgeSession getStatelessKnowledgeSession(String name) throws RulesException
    {
        KnowledgeBaseConfig knbCfg = getKwBConfig(name);
        return knbCfg.getStatelessKnowledgeSession();
    }

    public StatefulKnowledgeSession getStatefulKnowledgeSession(String name) throws RulesException
    {
        KnowledgeBaseConfig knbCfg = getKwBConfig(name);
        return knbCfg.getStatefulKnowledgeSession();
    }

    /**
     * @param id
     * @return
     * @throws RulesException
     */
    private synchronized KnowledgeBaseConfig getKwBConfig(String name) throws RulesException
    {
        KnowledgeBaseConfig knbCfg = knbCfgMap.get(name);
        if (knbCfg == null) {
            try {
                Node node = XMLConfig.getNode(CFG_FILE, "/GVRulesConfigManager/*[@type='knwl-config' and @name='"
                        + name + "']");
                if (node == null) {
                    throw new RulesException("KnowledgeBaseConfig[" + name + "] not configured");
                }
                knbCfg = (KnowledgeBaseConfig) Class.forName(XMLConfig.get(node, "@class")).newInstance();
                knbCfg.init(node);
                knbCfgMap.put(name, knbCfg);
            }
            catch (RulesException exc) {
                throw exc;
            }
            catch (Exception exc) {
                throw new RulesException("Error initializing KnowledgeBaseConfig[" + name + "]", exc);
            }
        }
        return knbCfg;
    }

    private synchronized void destroy()
    {
        ShutdownEventLauncher.removeEventListener(instance);
        XMLConfig.removeConfigurationListener(instance);
        instance = null;

        for (Map.Entry<String, KnowledgeBaseConfig> entry : knbCfgMap.entrySet()) {
            entry.getValue().destroy();
        }
        knbCfgMap.clear();
    }


    /*
     * (non-Javadoc)
     * @see it.greenvulcano.event.util.shutdown.ShutdownEventListener#shutdownStarted(it.greenvulcano.event.util.shutdown.ShutdownEvent)
     */
    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
        destroy();
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && event.getFile().equals(CFG_FILE)) {
            destroy();
        }
    }


}

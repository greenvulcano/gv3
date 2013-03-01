/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.greenvulcano.js.initializer;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.js.util.ScriptCache;

import java.util.HashMap;
import java.util.Set;

import javax.management.InstanceNotFoundException;

import org.w3c.dom.Node;

/**
 * JSInitManager class
 *
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
**/
public final class JSInitManager implements ConfigurationListener {
    /**
     * XMLConfig file name.
     */
    public static final String      CFG_FILE        = "GVJavaScriptConfig.xml";
    /**
     * Singleton reference.
     */
    private static JSInitManager    _instance       = null;
    /**
     * If true the configuration is changed.
     */
    private boolean                 confChangedFlag = false;
    /**
     * The scope generator map.
     */
    private HashMap<String, JSInit> jsinitMap       = new HashMap<String, JSInit>();

    /**
     * Constructor.
     */
    private JSInitManager() {
        // do nothing
    }

    /**
     * Singleton entry-point.
     *
     * @return the instance reference
     */
    public static synchronized JSInitManager instance() {
        if (_instance == null) {
            _instance = new JSInitManager();
            _instance.jsinitMap.clear();
            XMLConfig.addConfigurationListener(_instance, CFG_FILE);
        }
        return _instance;
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    public void configurationChanged(ConfigurationEvent event) {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && event.getFile().equals(CFG_FILE)) {
            confChangedFlag = true;
        }
    }

    /**
     * Create a scope generator instance, from the given name.
     *
     * @param name
     *            the generator name
     * @return the requested generator
     * @throws Exception
     *             if error occurs
     */
    public synchronized JSInit getJSInit(String name) throws Exception {
        if (confChangedFlag) {
            destroy();
        }
        JSInit jsinit = jsinitMap.get(name);
        if (jsinit == null) {
            Node node = XMLConfig.getNode(CFG_FILE, "//*[@type='jsinit' and @name='" + name + "']");
            if (node == null) {
                throw new InstanceNotFoundException("Invalid JSInit name: " + name);
            }
            String className = XMLConfig.get(node, "@class");
            jsinit = (JSInit) Class.forName(className).newInstance();
            jsinit.init(node);
            jsinitMap.put(name, jsinit);
        }
        return jsinit;
    }

    /**
     * Perform cleanup operation.
     */
    private void destroy() {
        Set<String> keySet = jsinitMap.keySet();
        for (String id : keySet) {
            JSInit jsinit = jsinitMap.get(id);
            jsinit.destroy();
        }
        jsinitMap.clear();
        ScriptCache.instance().clearMap();
        confChangedFlag = false;
    }
}

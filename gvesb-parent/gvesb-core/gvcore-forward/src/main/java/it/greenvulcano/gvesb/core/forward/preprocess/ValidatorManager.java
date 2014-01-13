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
package it.greenvulcano.gvesb.core.forward.preprocess;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.core.forward.JMSForwardException;
import it.greenvulcano.log.GVLogger;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.4.0 08/gen/2014
 * @author GreenVulcano Developer Team
 * 
 */
public class ValidatorManager
{
    private static final Logger     logger     = GVLogger.getLogger(ValidatorManager.class);

    private Map<String, Validator>  validators = null;
    private static ValidatorManager instance   = null;

    private ValidatorManager() {
        validators = new TreeMap<String, Validator>();
    }

    public synchronized static final ValidatorManager instance() {
        if (instance == null) {
            instance = new ValidatorManager();
        }
        return instance;
    }
    
    public synchronized Validator getValidator(Node node) throws JMSForwardException {
        String name = "";
        try {
            name = XMLConfig.get(node, "@name");
            Validator val = validators.get(name);
            if (val == null) {
                String clazz = XMLConfig.get(node, "@class");
                val = (Validator) Class.forName(clazz).newInstance();
                val.init(node);
                validators.put(name, val);
                logger.debug("Created Validator[" + name + "]");
            }

            return val;
        }
        catch (Exception exc) {
            logger.error("Error initializing Validator[" + name + "]", exc);
            throw new JMSForwardException("Error initializing Validator[" + name + "]", exc);
        }
    }
    
    public synchronized void reset() {
        String name = "";
        try {
            Iterator<String> is = validators.keySet().iterator();
            while (is.hasNext()) {
                name = is.next();
                Validator val = validators.get(name);
                if (val != null) {
                    logger.debug("Destroying Validator[" + name + "]");
                    val.destroy();
                }
            }
        }
        catch (Exception exc) {
            logger.error("Error destroying Validator[" + name + "]", exc);
        }
        validators.clear();
    }
}

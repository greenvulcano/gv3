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
package it.greenvulcano.gvesb.gvmqtt.server;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvmqtt.MQTTAdapterException;
import it.greenvulcano.util.metadata.PropertiesHandler;

import org.w3c.dom.Node;

/**
 * 
 * @version 3.5.0 13/mar/2015
 * @author GreenVulcano Developer Team
 * 
 */
public class ExternalServer implements MQTTServer
{
    private static final boolean IS_MANAGED = false;

    private String  name         = null;
    private String  brokerUrl    = null;

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.gvmqtt.server.MQTTServer#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws MQTTAdapterException {
        try {
            name = XMLConfig.get(node, "@name");
            brokerUrl = PropertiesHandler.expand(XMLConfig.get(node, "@brokerUrl"));
        }
        catch (Exception exc) {
            throw new MQTTAdapterException("Error initializing External MQTT server interface", exc);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.gvmqtt.server.MQTTServer#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.gvmqtt.server.MQTTServer#getBrokerUrl()
     */
    @Override
    public String getBrokerUrl() {
        return brokerUrl;
    }

    @Override
    public boolean isManaged() {
        return IS_MANAGED;
    }

    @Override
    public boolean isRunning() throws MQTTAdapterException {
        return true;
    }

    @Override
    public void start() throws MQTTAdapterException {
        // do nothing
    }

    @Override
    public void stop() throws MQTTAdapterException {
        // do nothing
    }
}

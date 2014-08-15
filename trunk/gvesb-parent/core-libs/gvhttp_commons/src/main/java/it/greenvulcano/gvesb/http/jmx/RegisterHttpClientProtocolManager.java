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
package it.greenvulcano.gvesb.http.jmx;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.MBeanServerInitializer;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Jul 26, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class RegisterHttpClientProtocolManager implements MBeanServerInitializer
{

    /**
     * The ObjectName properties.
     */
    private Map<String, String>     properties = new HashMap<String, String>();
    /**
     * The MBean descriptor.
     */
    private String                  descriptorName;
    private HttpClientProtocolProxy proxy      = null;

    /**
     * Initialize the <code>MBeanServerInitializer</code>.
     * 
     * @param conf
     *        the configuration node
     * @throws Exception
     */
    @Override
    public void init(Node conf) throws Exception
    {
        try {
            NodeList list = XMLConfig.getNodeList(conf, "property");
            for (int i = 0; i < list.getLength(); ++i) {
                Node node = list.item(i);
                String key = XMLConfig.get(node, "@name", "undef");
                String value = XMLConfig.get(node, "@value", "undef");
                properties.put(key, PropertiesHandler.expand(value));
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }

        properties.put(HttpClientProtocolProxy.JMX_KEY_NAME, HttpClientProtocolProxy.JMX_KEY_VALUE);

        descriptorName = XMLConfig.get(conf, "descriptor/@name", "HttpClientProtocolManager");
    }

    /**
     * Initialize the given <code>MBeanServer</code>.
     * 
     * @param server
     *        the MBean Server instance
     * @throws Exception
     *         if error occurs
     */
    @Override
    public final void initializeMBeanServer(MBeanServer server) throws Exception
    {
        JMXEntryPoint jmx = JMXEntryPoint.instance();
        NMDC.push();
        try {
            NMDC.setServer(JMXEntryPoint.getServerName());

            try {
                if (proxy != null) {
                    HttpClientProtocolProxy.destroy();
                    jmx.unregisterObject(proxy, descriptorName, properties);
                }
            }
            catch (Exception exc) {
                // do nothing
            }

            proxy = HttpClientProtocolProxy.instance();

            jmx.registerObject(proxy, descriptorName, properties);
        }
        finally {
            NMDC.pop();
        }
    }


}

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
package it.greenvulcano.gvesb.virtual.ws.monitoring;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.MBeanServerInitializer;
import it.greenvulcano.log.NMDC;

import java.util.Hashtable;

import javax.management.MBeanServer;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * RegisterRESTManager class
 * 
 * @version 3.4.0 Jul 17, 2013
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class RegisterRESTManager implements MBeanServerInitializer
{
    /**
     * The ObjectName properties.
     */
    private Hashtable<String, String> properties = new Hashtable<String, String>();
    /**
     * The MBean descriptor.
     */
    private String                    descriptorName;

    /**
     * Initialize the <code>MBeanServerInitializer</code>.
     * 
     * @param conf
     *        the configuration node
     * @throws Exception
     * @see it.greenvulcano.jmx.MBeanServerInitializer#init(org.w3c.dom.Node)
     */
    @Override
    public final void init(Node conf) throws Exception
    {
        try {
            NodeList list = XMLConfig.getNodeList(conf, "property");
            for (int i = 0; i < list.getLength(); ++i) {
                Node config = list.item(i);
                String key = XMLConfig.get(config, "@name", "undef");
                String value = XMLConfig.get(config, "@value", "undef");
                properties.put(key, expand(value));
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }

        properties.put(RESTManagerProxy.JMX_KEY_NAME, RESTManagerProxy.JMX_KEY_VALUE);

        descriptorName = XMLConfig.get(conf, "descriptor/@name", "Axis2RESTManager");
    }

    /**
     * Expand the macro.
     * 
     * @param str
     *        the string containing macro
     * @return the expanded string
     */
    private String expand(String str)
    {
        int idx = 0;
        while (true) {
            int beginIndex = str.indexOf("${", idx);
            if (beginIndex != -1) {
                int endIndex = str.indexOf("}", beginIndex);
                if (endIndex != -1) {
                    String propName = str.substring(beginIndex + 2, endIndex);
                    String paramValue = System.getProperty(propName, propName);
                    str = str.substring(0, beginIndex) + paramValue + str.substring(endIndex + 1);
                    idx = beginIndex + paramValue.length();
                }
                else {
                    return str;
                }
            }
            else {
                return str;
            }
        }
    }

    /**
     * Initialize the given <code>MBeanServer</code>.
     * 
     * @param server
     *        the MBean Server instance
     * @throws Exception
     *         if error occurs
     * @see it.greenvulcano.jmx.MBeanServerInitializer#initializeMBeanServer(javax.management.MBeanServer)
     */
    @Override
    public final void initializeMBeanServer(MBeanServer server) throws Exception
    {
        JMXEntryPoint jmx = JMXEntryPoint.instance();
        NMDC.push();
        try {
            NMDC.setServer(JMXEntryPoint.getServerName());
            RESTManagerProxy proxy = new RESTManagerProxy();

            try {
                jmx.unregisterObject(proxy, descriptorName, properties);
            }
            catch (Exception exc) {
                // do nothing
            }

            jmx.registerObject(proxy, descriptorName, properties);
        }
        finally {
            NMDC.pop();
        }
    }
}

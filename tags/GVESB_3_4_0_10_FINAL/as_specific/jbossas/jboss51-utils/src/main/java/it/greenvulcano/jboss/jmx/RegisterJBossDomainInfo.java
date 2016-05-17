/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.jboss.jmx;

// ------------------------------------------------------------------------------

// ------------------------------------------------------------------------------
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.MBeanServerInitializer;
import it.greenvulcano.log.NMDC;

import javax.management.MBeanServer;

import org.w3c.dom.Node;

/**
 * @version 3.2.0 Feb 17, 2012
 * @author GreenVulcano Developer Team
 * 
 */
public class RegisterJBossDomainInfo implements MBeanServerInitializer
{

    /**
     * component descriptor name
     */
    private String descriptorName = "";

    private Node   conf           = null;

    /**
     * Initialize the <code>MBeanServerInitializer</code>.
     * 
     * @param conf
     *        the node from which read configuration data
     * @exception Exception
     *            if errors occurs
     */
    @Override
    public void init(Node conf) throws Exception
    {
        this.conf = conf;
        descriptorName = XMLConfig.get(conf, "descriptor/@name", "DomainInfo");
    }

    /**
     * Initialize the given <code>MBeanServer</code>.
     * 
     * @param server
     *        the mbean server instance
     * @exception Exception
     *            if errors occurs
     */
    @Override
    public void initializeMBeanServer(MBeanServer server) throws Exception
    {
        JMXEntryPoint jmx = JMXEntryPoint.instance();
        NMDC.push();
        try {
            NMDC.setServer(JMXEntryPoint.getServerName());
            JBossDomainInfo dinfo = new JBossDomainInfo(conf);

            try {
                jmx.unregisterObject(dinfo, descriptorName + "_Internal", null);
            }
            catch (Exception exc) {
                // do nothing
            }

            jmx.registerObject(dinfo, descriptorName + "_Internal");
        }
        finally {
            NMDC.pop();
        }
    }
}
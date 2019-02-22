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
package it.greenvulcano.gvesb.j2ee.jndi.jmx;

import it.greenvulcano.gvesb.j2ee.jndi.JNDIBuilderManager;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.MBeanServerInitializer;
import it.greenvulcano.log.NMDC;

import javax.management.MBeanServer;

import org.w3c.dom.Node;

/**
 * 
 * RegisterJNDIBuilderManager class
 * 
 * @version 3.0.0 05/lug/2010
 * @author GreenVulcano Developer Team
 * 
 * 
 **/
public class RegisterJNDIBuilderManager implements MBeanServerInitializer
{

    /**
     * Initialize the <code>MBeanServerInitializer</code>.
     * 
     * @param conf
     *        the configuration node
     * @throws Exception
     */
    @Override
    public final void init(Node conf) throws Exception
    {
        // do nothing
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
        NMDC.push();
        try {
            NMDC.setServer(JMXEntryPoint.getServerName());
            JNDIBuilderManager bm = new JNDIBuilderManager();
        }
        finally {
            NMDC.pop();
        }
    }

}

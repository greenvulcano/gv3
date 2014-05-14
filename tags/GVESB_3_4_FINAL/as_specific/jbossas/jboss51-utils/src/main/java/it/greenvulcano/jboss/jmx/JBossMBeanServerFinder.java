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
package it.greenvulcano.jboss.jmx;

import it.greenvulcano.jmx.MBeanServerFinder;

import javax.management.MBeanServer;

import org.jboss.mx.util.MBeanServerLocator;
import org.w3c.dom.Node;

/**
 * Finds the MBean Server used by JBoss
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class JBossMBeanServerFinder implements MBeanServerFinder
{

    /**
     * Initializes the <code>JBossMBeanServerFinder</code>.
     *
     * @param conf
     *        the configuration.
     *
     * @throws Exception
     *         if an error occurs.
     */
    public void init(Node conf) throws Exception
    {
    }

    /**
     * Finds the <code>MBeanServer</code>.
     *
     * @return the <code>MBeanServer</code>
     * @throws Exception
     *         if an error occurs.
     */
    public MBeanServer findMBeanServer() throws Exception
    {
        return MBeanServerLocator.locateJBoss();
    }

    /**
     * Returns the name of the Server where the MBeanServer is running.
     *
     * @return the name of the Server where the MBeanServer is running.
     */
    public String getServerName()
    {
        return System.getProperty("jboss.server.name");
    }
}
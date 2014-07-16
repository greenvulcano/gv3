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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.jmx.MBeanServerFinder;

import javax.management.MBeanServer;
import javax.naming.Context;

import org.w3c.dom.Node;

/**
 * Finds the DistributedMBeanServer.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class DistributedMBeanServerFinder implements MBeanServerFinder
{
    private String     hajndiMBeanName;
    private String     rmiAdaptorName;
    private JNDIHelper jndiContext = null;
    private String     userName;
    private String     password;
    private String     loginContext;

    /**
     * Initialize the <code>MBeanServerFinder</code>.
     * 
     * @see it.greenvulcano.jmx.MBeanServerFinder#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node conf) throws Exception
    {
        userName = XMLConfig.get(conf, "@user", "admin");
        password = XMLConfig.get(conf, "@password", "admin");
        loginContext = XMLConfig.get(conf, "@login-context", "jmx-console");
        hajndiMBeanName = XMLConfig.get(conf, "@hajndi-mbean-name", "jboss:service=HAJNDI");
        rmiAdaptorName = XMLConfig.get(conf, "@RMIAdaptor-jndi-name", "jmx/rmi/RMIAdaptor");

        Node initialContextNode = XMLConfig.getNode(conf, "JNDIHelper");
        if (initialContextNode != null) {
            jndiContext = new JNDIHelper(initialContextNode);
        }
        else {
            jndiContext = new JNDIHelper();
            jndiContext.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.jboss.security.jndi.JndiLoginInitialContextFactory");
            jndiContext.setProperty(Context.SECURITY_PRINCIPAL, userName);
            jndiContext.setProperty(Context.SECURITY_CREDENTIALS, password);
        }
    }

    /**
     * @see it.greenvulcano.jmx.MBeanServerFinder#findMBeanServer()
     */
    @Override
    public MBeanServer findMBeanServer() throws Exception
    {
        return new DistributedMBeanServer(jndiContext, hajndiMBeanName, rmiAdaptorName, userName, password,
                loginContext);
    }

    /**
     * @see it.greenvulcano.jmx.MBeanServerFinder#getServerName()
     */
    @Override
    public String getServerName()
    {
        return System.getProperty("jboss.server.name");
    }
}
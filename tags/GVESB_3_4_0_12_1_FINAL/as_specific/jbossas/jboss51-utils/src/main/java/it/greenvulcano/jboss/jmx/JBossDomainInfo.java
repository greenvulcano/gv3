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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.jmx.DomainInfo;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;

import org.jboss.mx.util.MBeanServerLocator;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 Feb 17, 2012
 * @author GreenVulcano Developer Team
 */
public class JBossDomainInfo implements DomainInfo
{
    /**
     * The JBOSS domain name.
     */
    private String     domainName      = "";
    /**
     * The domain admin server name
     */
    private String     adminServerName = null;
    /**
     * The domain servers names
     */
    private String[]   serversNames    = null;

    private ObjectName hajndiMBeanName;
    private String     rmiAdaptorName;
    private JNDIHelper jndiContext     = null;
    private String     userName;
    private String     password;

    /**
     * Constructor
     */
    public JBossDomainInfo(Node conf)
    {
        try {
            userName = XMLConfig.get(conf, "param[@name='user']/@value", "admin");
            password = XMLConfig.get(conf, "param[@name='password']/@value", "admin");
            hajndiMBeanName = new ObjectName(XMLConfig.get(conf, "param[@name='hajndi-mbean-name']/@value",
                    "jboss:service=HAJNDI"));
            rmiAdaptorName = XMLConfig.get(conf, "param[@name='RMIAdaptor-jndi-name']/@value", "jmx/rmi/RMIAdaptor");

            jndiContext = new JNDIHelper();
            jndiContext.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.jboss.security.jndi.JndiLoginInitialContextFactory");
            jndiContext.setProperty(Context.SECURITY_PRINCIPAL, userName);
            jndiContext.setProperty(Context.SECURITY_CREDENTIALS, password);

            domainName = PropertiesHandler.expand("${{jboss.partition.name}}");
            adminServerName = PropertiesHandler.expand("${{jboss.server.name}}");

            serversNames = new String[1];
            serversNames[0] = adminServerName;
        }
        catch (Exception exc) {
            System.err.println("Error retrieving domain servers names");
            exc.printStackTrace();
            serversNames = new String[0];
        }
    }

    /**
     * @see it.greenvulcano.jmx.DomainInfo#getServersNames()
     */
    @Override
    public String[] getServersNames()
    {
        findMBeanServers();
        return serversNames;
    }

    /**
     * @see it.greenvulcano.jmx.DomainInfo#getAdminServerName()
     */
    @Override
    public String getAdminServerName()
    {
        return adminServerName;
    }

    /**
     * @see it.greenvulcano.jmx.DomainInfo#getDomainName()
     */
    @Override
    public String getDomainName()
    {
        return domainName;
    }

    /**
     * Finds the MBeanServers for all JBoss servers and initializes the internal
     * structures.
     */
    private void findMBeanServers()
    {
        Set<String> jbossMBeanServers = new HashSet<String>();
        jbossMBeanServers.add(adminServerName);
        try {
            MBeanServer jbossLocalMBeanServer = MBeanServerLocator.locateJBoss();

            String partitionName = (String) jbossLocalMBeanServer.getAttribute(hajndiMBeanName, "PartitionName");
            ObjectName haPartition = new ObjectName("jboss:service=HAPartition,partition=" + partitionName);

            Vector<?> currentView = (Vector<?>) jbossLocalMBeanServer.getAttribute(haPartition, "CurrentView");
            System.out.println("JBossDomainInfo - list: " + currentView);

            ObjectName serverConfig = new ObjectName("jboss.system:type=ServerConfig");
            for (Iterator<?> it = currentView.iterator(); it.hasNext();) {
                try {
                    String clusterNode = (String) it.next();
                    jndiContext.setProperty(Context.PROVIDER_URL, "jnp://" + clusterNode);

                    MBeanServerConnection rmiAdaptor = null;
                    try {
                        rmiAdaptor = (MBeanServerConnection) jndiContext.lookup(rmiAdaptorName);
                    }
                    finally {
                        jndiContext.close();
                    }

                    String serverName = (String) rmiAdaptor.getAttribute(serverConfig, "ServerName");
                    System.out.println("JBossDomainInfo - jbossMBeanServers add: " + serverName);
                    jbossMBeanServers.add(serverName);
                }
                catch (Exception exc) {
                    System.err.println("JBossDomainInfo - Error processing server info");
                    exc.printStackTrace();
                }
            }
        }
        catch (Exception exc) {
            System.err.println("JBossDomainInfo - Error preparing processing server info");
            exc.printStackTrace();
        }
        serversNames = jbossMBeanServers.toArray(new String[jbossMBeanServers.size()]);
    }
}

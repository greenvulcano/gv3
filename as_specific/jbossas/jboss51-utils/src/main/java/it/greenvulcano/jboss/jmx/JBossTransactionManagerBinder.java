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
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.MBeanServerInitializer;
import it.greenvulcano.log.NMDC;

import javax.management.MBeanServer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.w3c.dom.Node;

/**
 * La configurazione si compone di elementi <code>&lt;param&gt;</code> con gli
 * attributi <code>name</code> e <code>value</code>. I parametri ammessi (tutti
 * opzionali) sono:
 * <ul>
 * <li>objectName
 * <li>attributeName
 * <li>jndiName
 * </ul>
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class JBossTransactionManagerBinder implements MBeanServerInitializer
{

    private String transactionManagerObjectName;
    private String attributeName;
    private String jndiName;

    /**
     * @see it.greenvulcano.jmx.MBeanServerInitializer#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node conf) throws Exception
    {
        transactionManagerObjectName = XMLConfig.get(conf, "param[@name='objectName']/@value",
                "jboss:service=TransactionManager");
        attributeName = XMLConfig.get(conf, "param[@name='attributeName']/@value", "TransactionManager");
        jndiName = XMLConfig.get(conf, "param[@name='jndiName']/@value", "javax.transaction.TransactionManager");
    }

    /**
     * @see it.greenvulcano.jmx.MBeanServerInitializer#initializeMBeanServer(javax.management.MBeanServer)
     */
    @Override
    public void initializeMBeanServer(MBeanServer server) throws Exception
    {
        Context ctx = null;
        NMDC.push();
        try {
            NMDC.setServer(JMXEntryPoint.getServerName());

            ctx = new InitialContext();
            initializeTransactionManager(ctx);
            initializeUserTransaction(ctx);
        }
        finally {
            if (ctx != null) {
                try {
                    ctx.close();

                }
                catch (NamingException exc) {
                    // Do nothing
                }
            }
            NMDC.pop();
        }
    }

    private void initializeTransactionManager(Context ctx) throws Exception
    {
        try {
            Object obj = ctx.lookup(jndiName);
            if (!(obj instanceof TransactionManagerProxy) && (obj instanceof TransactionManager)) {
                return;
            }
        }
        catch (NameNotFoundException exc) {
            // do nothing
        }

        TransactionManagerProxy tm = new TransactionManagerProxy(transactionManagerObjectName, attributeName);
        ctx.rebind(jndiName, tm);
    }

    private void initializeUserTransaction(Context ctx) throws Exception
    {
        ctx.rebind("javax.transaction.UserTransaction", new UserTransactionProxy());
    }
}
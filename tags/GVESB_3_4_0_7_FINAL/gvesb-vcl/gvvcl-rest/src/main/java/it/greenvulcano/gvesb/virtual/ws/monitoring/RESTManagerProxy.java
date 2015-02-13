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

import it.greenvulcano.gvesb.virtual.ws.invoker.RestDynamicInvoker;
import it.greenvulcano.gvesb.virtual.ws.rest.WSCallException;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.JMXUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * @version 3.4.0 Jul 17, 2013
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class RESTManagerProxy
{
    /**
     *
     */
    public static final String JMX_KEY_NAME  = "Component";
    /**
     *
     */
    public static final String JMX_KEY_VALUE = "RESTManager";
    /**
     *
     */
    public static final String JMX_KEY       = JMX_KEY_NAME + "=" + JMX_KEY_VALUE;
    /**
     *
     */
    public static final String JMX_FILTER    = "GreenVulcano:*," + JMX_KEY;

    /**
     * @return the loaded RESTs
     * @throws Exception
     */
    public String[] getLoadedRESTEPR() throws Exception
    {
        JMXEntryPoint jmx = JMXEntryPoint.instance();
        MBeanServer server = jmx.getServer();

        Set<String> ret = new HashSet<String>();
        Set<ObjectName> names = server.queryNames(new ObjectName(JMX_FILTER), null);
        for (ObjectName objectName : names) {
            Object val = server.getAttribute(objectName, "loadedRESTEPRLocal");
            String eprs[] = (String[]) val;
            if (eprs != null) {
                for (String epr : eprs) {
                    ret.add(epr);
                }
            }
        }

        String loadedEPRs[] = new String[ret.size()];
        ret.toArray(loadedEPRs);
        return loadedEPRs;
    }


    /**
     * @return the number of invokers actually in use
     * @throws Exception
     * @see #getInUseInvokersLocal()
     */
    @SuppressWarnings("unchecked")
    public Map<String, Integer> getInUseInvokers() throws Exception
    {
        JMXEntryPoint jmx = JMXEntryPoint.instance();
        MBeanServer server = jmx.getServer();

        Map<String, Integer> ret = new HashMap<String, Integer>();
        Set<ObjectName> names = server.queryNames(new ObjectName(JMX_FILTER), null);
        for (ObjectName objectName : names) {
            Map<String, Integer> val = (Map<String, Integer>) server.getAttribute(objectName, "inUseInvokersLocal");
            for (Entry<String, Integer> entry : val.entrySet()) {
                String epr = entry.getKey();
                int newValue = 0;
                if (ret.containsKey(epr)) {
                    newValue = ret.get(epr);
                }
                newValue += entry.getValue();
                ret.put(epr, newValue);
            }
        }
        return ret;
    }

    /**
     * @return the number of invokers actually in cache
     * @throws Exception
     * @see #getInCacheInvokersLocal()
     */
    @SuppressWarnings("unchecked")
    public Map<String, Integer> getInCacheInvokers() throws Exception
    {
        JMXEntryPoint jmx = JMXEntryPoint.instance();
        MBeanServer server = jmx.getServer();

        Map<String, Integer> ret = new HashMap<String, Integer>();
        Set<ObjectName> names = server.queryNames(new ObjectName(JMX_FILTER), null);
        for (ObjectName objectName : names) {
            Map<String, Integer> val = (Map<String, Integer>) server.getAttribute(objectName, "inCacheInvokersLocal");
            for (Entry<String, Integer> entry : val.entrySet()) {
                String epr = entry.getKey();
                int newValue = 0;
                if (ret.containsKey(epr)) {
                    newValue = ret.get(epr);
                }
                newValue += entry.getValue();
                ret.put(epr, newValue);
            }
        }
        return ret;
    }

    /**
     * @param rest
     * @throws Exception
     * @see #reloadLocal(String)
     */
    public void reload(String rest) throws Exception
    {
        Object params[] = new Object[]{rest};
        String signature[] = new String[]{"java.lang.String"};
        JMXUtils.invoke(JMX_FILTER, "reloadLocal", params, signature, null);
    }

    /**
     * @throws Exception
     * @see #reloadAllLocal()
     */
    public void reloadAll() throws Exception
    {
        Object params[] = new Object[0];
        String signature[] = new String[0];
        JMXUtils.invoke(JMX_FILTER, "reloadAllLocal", params, signature, null);
    }

    /**
     * @return the loaded REST End Point references
     * @see RestDynamicInvoker#getLoadedRESTEPR()
     */
    public String[] getLoadedRESTEPRLocal()
    {
        return RestDynamicInvoker.getLoadedRESTEPR();
    }

    /**
     * @return the number of invokers actually in use.
     * @see RestDynamicInvoker#getInUseInvokers()
     */
    public Map<String, Integer> getInUseInvokersLocal()
    {
        return RestDynamicInvoker.getInUseInvokers();
    }

    /**
     * @return the number of invokers actually in cache.
     * @see RestDynamicInvoker#getInCacheInvokers()
     */
    public Map<String, Integer> getInCacheInvokersLocal()
    {
        return RestDynamicInvoker.getInCacheInvokers();
    }

    /**
     * @param rest
     * @throws WSCallException
     * @see RestDynamicInvoker#reload(String)
     */
    public void reloadLocal(String rest) throws WSCallException
    {
        RestDynamicInvoker.reload(rest);
    }

    /**
     * @throws WSCallException
     * @see RestDynamicInvoker#reloadAll()
     */
    public void reloadAllLocal() throws WSCallException
    {
        RestDynamicInvoker.reloadAll();
    }
}

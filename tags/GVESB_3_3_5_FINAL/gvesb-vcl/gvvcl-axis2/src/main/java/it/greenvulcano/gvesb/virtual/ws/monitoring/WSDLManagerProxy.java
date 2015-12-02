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
package it.greenvulcano.gvesb.virtual.ws.monitoring;

import it.greenvulcano.gvesb.virtual.ws.WSCallException;
import it.greenvulcano.gvesb.virtual.ws.dynamic.invoker.DynamicInvoker;
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
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class WSDLManagerProxy
{
    /**
     *
     */
    public static final String JMX_KEY_NAME  = "Component";
    /**
     *
     */
    public static final String JMX_KEY_VALUE = "WSDLManager";
    /**
     *
     */
    public static final String JMX_KEY       = JMX_KEY_NAME + "=" + JMX_KEY_VALUE;
    /**
     *
     */
    public static final String JMX_FILTER    = "GreenVulcano:*," + JMX_KEY;

    /**
     * @return the loaded WSDLs
     * @throws Exception
     */
    public String[] getLoadedWSDL() throws Exception
    {
        JMXEntryPoint jmx = JMXEntryPoint.instance();
        MBeanServer server = jmx.getServer();

        Set<String> ret = new HashSet<String>();
        Set<ObjectName> names = server.queryNames(new ObjectName(JMX_FILTER), null);
        for (ObjectName objectName : names) {
            Object val = server.getAttribute(objectName, "loadedWSDLLocal");
            String wsdls[] = (String[]) val;
            if (wsdls != null) {
                for (String wsdl : wsdls) {
                    ret.add(wsdl);
                }
            }
        }

        String loadedWSDL[] = new String[ret.size()];
        ret.toArray(loadedWSDL);
        return loadedWSDL;
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
                String wsdl = entry.getKey();
                int newValue = 0;
                if (ret.containsKey(wsdl)) {
                    newValue = ret.get(wsdl);
                }
                newValue += entry.getValue();
                ret.put(wsdl, newValue);
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
                String wsdl = entry.getKey();
                int newValue = 0;
                if (ret.containsKey(wsdl)) {
                    newValue = ret.get(wsdl);
                }
                newValue += entry.getValue();
                ret.put(wsdl, newValue);
            }
        }
        return ret;
    }

    /**
     * @param wsdl
     * @throws Exception
     * @see #reloadLocal(String)
     */
    public void reload(String wsdl) throws Exception
    {
        Object params[] = new Object[]{wsdl};
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
     * @return the loaded WSDLs
     * @see DynamicInvoker#getLoadedWSDL()
     */
    public String[] getLoadedWSDLLocal()
    {
        return DynamicInvoker.getLoadedWSDL();
    }

    /**
     * @return the number of invokers actually in use.
     * @see DynamicInvoker#getInUseInvokers()
     */
    public Map<String, Integer> getInUseInvokersLocal()
    {
        return DynamicInvoker.getInUseInvokers();
    }

    /**
     * @return the number of invokers actually in cache.
     * @see DynamicInvoker#getInCacheInvokers()
     */
    public Map<String, Integer> getInCacheInvokersLocal()
    {
        return DynamicInvoker.getInCacheInvokers();
    }

    /**
     * @param wsdl
     * @throws WSCallException
     * @see DynamicInvoker#reload(String)
     */
    public void reloadLocal(String wsdl) throws WSCallException
    {
        DynamicInvoker.reload(wsdl);
    }

    /**
     * @throws WSCallException
     * @see DynamicInvoker#reloadAll()
     */
    public void reloadAllLocal() throws WSCallException
    {
        DynamicInvoker.reloadAll();
    }
}

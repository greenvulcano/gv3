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
package it.greenvulcano.gvesb.gvconsole.webservice.test;

import it.greenvulcano.configuration.XMLConfig;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class InvocationBeanManager
{
    /**
     * InvocationBean's Map
     */
    private Map<String, InvocationBean> beans;
    /**
     * Current invocation bean
     */
    private InvocationBean              currentInvocationBean;
    /**
     *
     */
    private String                      currentName;

    /**
     * Configuration file name
     */
    public static final String          FILE_NAME = "InvocationBeanConfig.xml";


    /**
     * The constructor
     *
     * @throws Exception
     */
    public InvocationBeanManager() throws Exception
    {
        beans = new HashMap<String, InvocationBean>();
        init();
    }


    /**
     * Init without parameter
     *
     * @throws Exception
     */
    private void init() throws Exception
    {
        Node invocationBeansConfiguration = XMLConfig.getNode(FILE_NAME, "InvocationBeans");
        NodeList classesConfiguration = XMLConfig.getNodeList(invocationBeansConfiguration, "./*");
        if (classesConfiguration.getLength() > 0) {
            // Factory factory = new Factory(getClass().getClassLoader(), null);
            for (int i = 0; i < classesConfiguration.getLength(); i++) {
                Node current = classesConfiguration.item(i);
                String name = XMLConfig.get(current, "@name");
                String beanClassName = XMLConfig.get(current, "@class");
                Class<?> beanImpl = Class.forName(beanClassName);
                InvocationBean bean = (InvocationBean) beanImpl.newInstance();
                // InvocationBean bean = (InvocationBean)
                // factory.createObject(current);
                bean.init(current);
                beans.put(name, bean);
                if (i == 0) {
                    currentInvocationBean = bean;
                    currentName = name;
                }
            }
        }
    }

    /**
     * Return the specific InvocationBean
     *
     * @param name
     * @return the specific <code>InvocationBean</code>
     * @throws Exception
     */
    public InvocationBean getInvocationBean(String name) throws Exception
    {
        InvocationBean bean = beans.get(name);
        if (bean == null) {
            throw new Exception("The InvocationBean " + name + " isn't present");
        }
        currentInvocationBean = bean;
        currentName = name;
        return bean;
    }

    /**
     * Return all InvocationBean
     *
     * @return all InvocationBean beans
     * @throws Exception
     */
    public Map<String, InvocationBean> getAllInvocationBean() throws Exception
    {
        return new HashMap<String, InvocationBean>(beans);
    }

    /**
     * Set a InvocationBean into map
     *
     * @param name
     * @param bean
     * @throws Exception
     */
    public void setInvocationBean(String name, InvocationBean bean) throws Exception
    {
        if (name == null || name.length() == 0 || bean == null) {
            throw new Exception("Invalid key or InvocationBean's value");
        }
        beans.put(name, bean);
        currentInvocationBean = bean;
        currentName = name;
    }

    /**
     * @return the current InvocationBean
     */
    public InvocationBean getCurrentInvocationBean()
    {
        return currentInvocationBean;
    }

    /**
     * @return the current InvocationBean name
     */
    public String getCurrentName()
    {
        return currentName;
    }
}
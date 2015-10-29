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
package it.greenvulcano.gvesb.gvconsole.workbench.plugin;

import it.greenvulcano.gvesb.buffer.GVException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * JMSAdditionalProperties class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class JMSAdditionalProperties
{
    /**
     * Contains optional/extended header fields that will transit into
     * GreenVulcano transparently. <br>
     * This field is allocated only if necessary.
     */
    private Map<String, String> jmsProperties      = null;
    private Map<String, String> jmsPropertiesTypes = null;

    /**
     * The GVBuffer code must not access directly property fields, but it
     * MUST use the getProperties() method. This ensure that properties
     * are allocated only if necessary.
     */
    private Map<String, String> getJMSPropertiesFields()
    {
        if (jmsProperties == null) {
            jmsProperties = new HashMap<String, String>();
        }
        return jmsProperties;
    }

    private Map<String, String> getJMSPropertiesTypes()
    {
        if (jmsPropertiesTypes == null) {
            jmsPropertiesTypes = new HashMap<String, String>();
        }
        return jmsPropertiesTypes;
    }

    /**
     * @param name
     * @return
     */
    public String getField(String name)
    {
        return getJMSPropertiesFields().get(name);
    }

    /**
     * @param name
     * @return
     */
    public String getType(String name)
    {
        return getJMSPropertiesTypes().get(name);
    }

    /**
     * @return
     */
    public Iterator<String> getFieldNamesIterator()
    {
        return getJMSPropertiesFields().keySet().iterator();
    }

    /**
     * @return
     */
    public Iterator<String> getTypesNamesIterator()
    {
        return getJMSPropertiesTypes().keySet().iterator();
    }

    /**
     * @return
     */
    public Set<String> getFieldNamesSet()
    {
        return getJMSPropertiesFields().keySet();
    }

    /**
     * @return
     */
    public Set<String> getTypeNamesSet()
    {
        return getJMSPropertiesTypes().keySet();
    }

    /**
     * @return
     */
    public String[] getFieldNames()
    {
        Set<String> namesSet = getJMSPropertiesFields().keySet();
        String[] names = new String[namesSet.size()];
        namesSet.toArray(names);
        return names;
    }

    /**
     * @return
     */
    public String[] getTypeNames()
    {
        Set<String> namesSet = getJMSPropertiesTypes().keySet();
        String[] names = new String[namesSet.size()];
        namesSet.toArray(names);
        return names;
    }

    /**
     *
     * @param field
     * @param value
     * @param type
     * @throws GVException
     */
    public void setProperty(String field, String value, String type) throws GVException
    {
        getJMSPropertiesFields().put(field, value);
        getJMSPropertiesTypes().put(field, type);
    }
}

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
package it.greenvulcano.util.metadata;

import java.util.Map;

import org.mozilla.javascript.Scriptable;

/**
 * Helper interface for metadata substitution in strings.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface PropertyHandler
{
    /**
     *
     */
    public static final String PROP_START       = "{{";
    /**
     *
     */
    public static final String PROP_END         = "}}";

    public static final String[] PROPS_START    = new String[]{"{{", "§#", "?#"};
    public static final String[] PROPS_END      = new String[]{"}}", "#§", "#?"};

    /**
     * ThreadMap key to enable the exception throwing on handler errors.
     */
    public static final String THROWS_EXCEPTION = "PropertyHandler.THROWS_EXCEPTION";

    /**
     * ThreadMap key to enable the external resource local storage.
     */
    public static final String RESOURCE_STORAGE = "PropertyHandler.RESOURCE_STORAGE";

    /**
     * This method insert the correct values for the dynamic parameter found in
     * the input string.
     *
     * @param type
     *        the type
     * @param trigger
     *        the trigger type {{ or §# or ##
     * @param str
     *        the string to value
     * @param inProperties
     *        the hashTable containing the properties
     * @param object
     *        the object to work with
     * @param scope
     *        the JavaScript scope
     * @param extra
     *        a extra object
     * @return the expanded string
     *
     * @throws PropertiesHandlerException
     *         if error occurs and the flag THROWS_EXCEPTION is set for the
     *         current thread
     */
    public String expand(String type, int trigger, String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra) throws PropertiesHandlerException;

    public void cleanupResources();
}

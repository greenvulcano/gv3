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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.mozilla.javascript.Scriptable;

import it.greenvulcano.util.thread.ThreadMap;
import it.greenvulcano.util.txt.TextUtils;

/**
 * Helper class for metadata substitution in strings.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 **/
public final class PropertiesHandler
{
    /**
     * @version 3.0.0 Feb 27, 2010
     * @author nunzio
     *
     */
    public final class MetaDataTokenizer
    {
        private final Vector<String> tokens  = new Vector<String>();
        private int            lastPos = 0;
        private Stack<Integer> lastTrigger = new Stack<Integer>();

        /**
         * @param string
         */
        public MetaDataTokenizer(String string)
        {
            parse(string, 0);
        }

        /**
         * @return it there is another token
         */
        public boolean hasNext()
        {
            return (this.lastPos < this.tokens.size());
        }

        /**
         * @return the next token
         */
        public String next()
        {
            return this.tokens.get(this.lastPos++);
        }

        /**
         *
         */
        public void pushBack()
        {
            this.lastPos--;
            if (this.lastPos < -1) {
                this.lastPos = -1;
            }
        }

        private void parse(String string, int index)
        {
            if (string == null) {
                return;
            }
            if (index == string.length()) {
                return;
            }
            int begin = string.indexOf(PropertyHandler.PROPS_START[0], index);
            int begin1 = string.indexOf(PropertyHandler.PROPS_START[1], index);
            int begin2 = string.indexOf(PropertyHandler.PROPS_START[2], index);
            int end = string.indexOf(PropertyHandler.PROPS_END[0], index);
            int end1 = string.indexOf(PropertyHandler.PROPS_END[1], index);
            int end2 = string.indexOf(PropertyHandler.PROPS_END[2], index);

            int trigger = 0;
            if (begin != -1) {
            	if (begin1 != -1) {
            		if (begin1 < begin) { // meta #§ before {{
            			trigger = 1;
            			begin = begin1;
            			end = end1;
            		}
            		// meta {{ before #§ or ?#
            	}
            	else if (begin2 != -1) {
            		if (begin2 < begin) { // meta ?# before {{
            			trigger = 2;
            			begin = begin2;
            			end = end2;
            		}
            		// meta {{ before #§ or ?#
            	}
            	// meta {{
            }
            else {
            	if (begin1 != -1) { // meta #§
            		trigger = 1;
           			begin = begin1;
           		}
            	else if (begin2 != -1) { // meta ?#
            		trigger = 2;
           			begin = begin2;
           		}
           		// no meta
    			//begin = begin1;

            	if (end == -1) {
            		if (end1 != -1) { // meta #§
            			end = end1;
            		}
            		else if (end2 != -1) { // meta ?#
            			end = end2;
            		}
           		}
            }

            String terminator = "";
            int pos = -1;
            if (begin == -1) {
                if (end == -1) {
                    this.tokens.add(string.substring(index));
                    return;
                }
                terminator = PropertyHandler.PROPS_END[this.lastTrigger.pop()];
                pos = end;
            }
            else {
                if (end == -1) {
                    terminator = PropertyHandler.PROPS_START[trigger];
                    this.lastTrigger.push(trigger);
                    pos = begin;
                }
                else {
                	if (begin < end) {
                        pos = begin;
                        terminator = PropertyHandler.PROPS_START[trigger];
                        this.lastTrigger.push(trigger);
                	}
                	else {
                		pos = end;
                		terminator = PropertyHandler.PROPS_END[this.lastTrigger.pop()];
                	}
                }
            }
            this.tokens.add(string.substring(index, pos));
            this.tokens.add(terminator);
            parse(string, pos + PropertyHandler.PROPS_START[trigger].length());
        }
    }

    private static HashMap<String, PropertyHandler> propHandlers = new HashMap<String, PropertyHandler>();
    private static Set<PropertyHandler> propHandlersSet = new HashSet<PropertyHandler>();
    private static HashSet<String>                  propSet      = new HashSet<String>();

    static {
    	String clazz = null;
        try {
            String classes = "";

            try {
                classes = TextUtils.readFileFromCP("PropertiesHandler.properties");
            }
            catch (Exception exc) {
                System.out.println("PropertiesHandler: unable to load file PropertiesHandler.properties");
                classes = "";
            }
            String[] cl = classes.split("(\\n\\r|\\n)");
            for (String c : cl) {
            	clazz = c;
            	if (!"".equals(clazz)) {
            		Class.forName(clazz);
            	}
            }
        }
        catch (Exception exc) {
            System.out.println("Error registering xxxPropertyHandler[" + clazz + "]: " + exc);
            exc.printStackTrace();

            try {
                Class.forName("it.greenvulcano.util.metadata.BasicPropertyHandler");
            }
            catch (Exception exc2) {
                System.out.println("Error registering BasicPropertyHandler[" + clazz + "]: " + exc2);
                exc2.printStackTrace();
            }
        }
    }

    /**
     * A private empty constructor. Is not possible to instantiate this class.
     */
    public PropertiesHandler()
    {
        // do nothing
    }

    /**
     * @param type
     * @param handler
     */
    public static void registerHandler(String type, PropertyHandler handler)
    {
        System.out.println("PropertiesHandler.registerHandler: " + type + " -> " + handler);
        propHandlers.put(type, handler);
        propHandlersSet.add(handler);
        propSet.add(type);
    }

    /**
     * This method insert the correct values for the dynamic parameter found in
     * the input string. The property value can be a combination of: - fixed : a
     * text string; - ${{propname}} : a System property name.
     *
     * @param str
     *        the string to value
     * @return the expanded string
     *
     * @throws PropertiesHandlerException
     *         if error occurs and the flag THROWS_EXCEPTION is set
     */
    public static String expand(String str) throws PropertiesHandlerException
    {
        return expand(str, null, null, null, null);
    }

    /**
     * This method insert the correct values for the dynamic parameter found in
     * the input string. The property value can be a combination of: - fixed : a
     * text string; - ${{propname}} : a System property name; - @{{propname}} :
     * a inProperties property name;
     *
     * @param str
     *        the string to value
     * @param inProperties
     *        the hashTable containing the properties
     * @return the expanded string
     *
     * @throws PropertiesHandlerException
     *         if error occurs and the flag THROWS_EXCEPTION is set
     */
    public static String expand(String str, Map<String, Object> inProperties) throws PropertiesHandlerException
    {
        return expand(str, inProperties, null, null, null);
    }

    /**
     * This method insert the correct values for the dynamic parameter found in
     * the input string. The property value can be a combination of: - fixed : a
     * text string; - %{{class}} : the object class name; - %{{fqclass}} : the
     * object fully qualified class name; - %{{package}} : the object package
     * name; - ${{propname}} : a System property name; - @{{propname}} : a
     * inProperties property name;
     *
     * @param str
     *        the string to value
     * @param inProperties
     *        the hashTable containing the properties
     * @param obj
     *        the object to work with
     * @return the expanded string
     *
     * @throws PropertiesHandlerException
     *         if error occurs and the flag THROWS_EXCEPTION is set
     */
    public static String expand(String str, Map<String, Object> inProperties, Object obj)
            throws PropertiesHandlerException
    {
        return expand(str, inProperties, obj, null, null);
    }

    /**
     * This method insert the correct values for the dynamic parameter found in
     * the input string. The property value can be a combination of: - fixed : a
     * text string; - %{{class}} : the object class name; - %{{fqclass}} : the
     * object fully qualified class name; - %{{package}} : the obj package name;
     * - ${{propname}} : a System property name; - @{{propname}} : a
     * inProperties property name; - &{{script}} : a JavaScript script;
     *
     * @param str
     *        the string to value
     * @param inProperties
     *        the hashTable containing the properties
     * @param obj
     *        the object to work with
     * @param scope
     *        the JavaScript scope
     * @return the expanded string
     *
     * @throws PropertiesHandlerException
     *         if error occurs and the flag THROWS_EXCEPTION is set
     */
    public static String expand(String str, Map<String, Object> inProperties, Object obj, Scriptable scope)
            throws PropertiesHandlerException
    {
        return expand(str, inProperties, obj, scope, null);
    }

    /**
     * This method insert the correct values for the dynamic parameter found in
     * the input string. The property value can be a combination of: - fixed : a
     * text string; - %{{class}} : the object class name; - %{{fqclass}} : the
     * object fully qualified class name; - %{{package}} : the object package
     * name; - ${{propname}} : a System property name; - @{{propname}} : a
     * inProperties property name; - &{{script}} : a JavaScript script;
     *
     * @param str
     *        the string to value
     * @param inProperties
     *        the hashTable containing the properties
     * @param obj
     *        the object to work with
     * @param scope
     *        the JavaScript scope
     * @param extra
     *        a extra object passed to property handlers
     * @return the expanded string
     *
     * @throws PropertiesHandlerException
     *         if error occurs and the flag THROWS_EXCEPTION is set
     */
    public static String expand(String str, Map<String, Object> inProperties, Object obj, Scriptable scope, Object extra)
            throws PropertiesHandlerException
    {
        if (str == null) {
            return null;
        }
        if (inProperties == null) {
            inProperties = new HashMap<String, Object>();
        }
        PropertyToken token = null;
        try {
            token = PropertiesHandler.parse(str);
            return token.getValue(inProperties, obj, scope, extra);
        }
        catch (PropertiesHandlerException exc) {
            if (isExceptionOnErrors()) {
                throw exc;
            }
        }
        catch (Exception exc) {
            if (isExceptionOnErrors()) {
                throw new PropertiesHandlerException(exc);
            }
        }
        return null;
    }

    /**
     * @param type
     * @param value
     * @param inProperties
     * @param obj
     * @param scope
     * @param extra
     *        a extra object passed to property handlers
     * @return the expanded string
     * @throws PropertiesHandlerException
     */
    public static String expandInternal(String type, int trigger, String value, Map<String, Object> inProperties, Object obj,
            Scriptable scope, Object extra) throws PropertiesHandlerException
    {
        PropertyHandler handler = propHandlers.get(type);
        if (handler == null) {
            return value;
        }
        return handler.expand(type, trigger, value, inProperties, obj, scope, extra);
    }

    private static PropertyToken parse(String str)
    {
        PropertyToken token = new PropertyToken(0, 0, "", "", 0);
        MetaDataTokenizer mdt = new PropertiesHandler().new MetaDataTokenizer(str);
        parse(token, mdt);
        return token;
    }

    /**
     * @param token
     * @param mdt
     */
    private static void parse(PropertyToken token, MetaDataTokenizer mdt)
    {
        String pToken = null;
        while (mdt.hasNext()) {
            String sToken = mdt.next();
            if (sToken.equals(PropertyHandler.PROPS_START[0])
             || sToken.equals(PropertyHandler.PROPS_START[1])
             || sToken.equals(PropertyHandler.PROPS_START[2])) {
            	int trigger = sToken.equals(PropertyHandler.PROPS_START[0]) ? 0 : (sToken.equals(PropertyHandler.PROPS_START[1]) ? 1 : 2);
                String type = extractType(pToken);
                String staticToken = pToken.substring(0, pToken.lastIndexOf(type));
                PropertyToken subToken = null;
                if (staticToken.length() > 0) {
                    subToken = new PropertyToken(0, 0, staticToken, "", trigger);
                    token.addSubToken(subToken);
                }
                subToken = new PropertyToken(0, 0, "", type, trigger);
                token.addSubToken(subToken);
                parse(subToken, mdt);
            }
            else if (sToken.equals(PropertyHandler.PROPS_END[0])
            	  || sToken.equals(PropertyHandler.PROPS_END[1])
            	  || sToken.equals(PropertyHandler.PROPS_END[2])) {
                break;
            }
            else {
                if (mdt.hasNext()) {
                    String nToken = mdt.next();
                    if (nToken.equals(PropertyHandler.PROPS_START[0])
                     || nToken.equals(PropertyHandler.PROPS_START[1])
                     || nToken.equals(PropertyHandler.PROPS_START[2])) {
                        pToken = sToken;
                    }
                    else {
                        PropertyToken subToken = new PropertyToken(0, 0, sToken, "", 0);
                        token.addSubToken(subToken);
                    }
                    mdt.pushBack();
                }
                else {
                    PropertyToken subToken = new PropertyToken(0, 0, sToken, "", 0);
                    token.addSubToken(subToken);
                }
            }
        }
    }

    /**
     * Enable the exception throwing on errors, for the current thread.
     *
     * Example:
     *
     * <pre>
     *
     *     ...
     *     PropertiesHandler.enableExceptionOnErrors();
     *     try {
     *        ...
     *        String value = PropertiesHandler.expand(...);
     *        ...
     *     }
     *     catch (PropertiesHandlerException exc) {
     *        ...
     *     }
     *     finally {
     *        PropertiesHandler.disableExceptionOnErrors();
     *     }
     *
     * </pre>
     */
    public static void enableExceptionOnErrors()
    {
        ThreadMap.put(PropertyHandler.THROWS_EXCEPTION, "true");
    }

    /**
     * Disable the exception throwing on errors, for the current thread.
     *
     */
    public static void disableExceptionOnErrors()
    {
        ThreadMap.remove(PropertyHandler.THROWS_EXCEPTION);
    }

    /**
     * Check if the exception throwing on errors is enabled for the current
     * thread.
     *
     * @return if the exception throwing on errors is enabled for the current
     *         thread.
     *
     */
    public static boolean isExceptionOnErrors()
    {
        return "true".equals(ThreadMap.get(PropertyHandler.THROWS_EXCEPTION));
    }

    /**
     * Enable the external resource (like DB connection) local storage, for the current thread.
     *
     * Example:
     *
     * <pre>
     *
     *     ...
     *     PropertiesHandler.enableResourceLocalStorage();
     *     try {
     *        ...
     *        String value = PropertiesHandler.expand(...);
     *        ...
     *     }
     *     catch (PropertiesHandlerException exc) {
     *        ...
     *     }
     *     finally {
     *        PropertiesHandler.disableResourceLocalStorage();
     *     }
     *
     * </pre>
     */
    public static void enableResourceLocalStorage()
    {
        ThreadMap.put(PropertyHandler.RESOURCE_STORAGE, "true");
    }

    /**
     * Disable the external resource local storage, for the current thread.
     *
     */
    public static void disableResourceLocalStorage()
    {
    	for (PropertyHandler ph : propHandlersSet) {
    		ph.cleanupResources();
		}
        ThreadMap.remove(PropertyHandler.RESOURCE_STORAGE);
    }

    /**
     * Check if the exception throwing on errors is enabled for the current
     * thread.
     *
     * @return if the exception throwing on errors is enabled for the current
     *         thread.
     *
     */
    public static boolean isResourceLocalStorage()
    {
        return "true".equals(ThreadMap.get(PropertyHandler.RESOURCE_STORAGE));
    }

    /**
     * @param str
     * @return
     */
    private static String extractType(String str)
    {
        String type = "";
        Iterator<String> i = propSet.iterator();
        while (i.hasNext()) {
            String currType = i.next();
            if (endsWith(str, currType)) {
                type = currType;
                break;
            }
        }
        return type;
    }

    private static boolean endsWith(String str, String value)
    {
        if (value.length() == 0) {
            return false;
        }
        int begin = str.length() - 1;
        int end = begin - value.length();
        int index = value.length() - 1;
        if (begin < index) {
            return false;
        }
        while (begin > end) {
            if (str.charAt(begin) != value.charAt(index)) {
                return false;
            }
            index--;
            begin--;
        }
        return true;
    }

    /**
     * Check if the input string need to be processed.
     *
     * @param str
     *        the string to check
     * @return true if the string is processed
     */
    public static boolean isExpanded(String str)
    {
        if ((str == null) || (str.length() == 0)) {
            return true;
        }
        Iterator<String> i = propSet.iterator();
        while (i.hasNext()) {
        	String p = i.next();
            if ((str.indexOf((p + PropertyHandler.PROPS_START[0])) != -1)
             || (str.indexOf((p + PropertyHandler.PROPS_START[1])) != -1)
             || (str.indexOf((p + PropertyHandler.PROPS_START[2])) != -1)) {
                return false;
            }
        }
        return true;
    }
}

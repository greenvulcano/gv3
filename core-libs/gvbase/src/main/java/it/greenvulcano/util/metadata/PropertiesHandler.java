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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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
    public static class TypeDef {
        public String type;
        public int start;
        public int end = -1;
        public int trigger;
        public List<TypeDef> children = new ArrayList<TypeDef>();

        public TypeDef(String type, int trigger, int start) {
            this.type = type;
            this.trigger = trigger;
            this.start = start;
        }

        public TypeDef(String type, int trigger, int start, int end) {
            this.type = type;
            this.trigger = trigger;
            this.start = start;
            this.end = end;
        }

        public boolean addChild(TypeDef td) {
            if ((this.start <= td.start) && (td.end <= this.end)) {
                for (TypeDef tdi : this.children) {
                    if (tdi.addChild(td)) {
                        return true;
                    }
                }
                this.children.add(td);
                Collections.sort(this.children, (t1, t2) -> Integer.compare(t1.start, t2.start));
                return true;
            }
            return false;
        }

        public void addToken(String str, PropertyToken parent) {
            int idx = this.start;
            PropertyToken token = null;
            if (this.trigger == -2) {
                token = new PropertyToken(this.start, this.end, "", this.type, -1);
            }
            else if (this.trigger == -1) {
                token = new PropertyToken(this.start, this.end, str.substring(this.start, this.end), this.type, this.trigger);
            }
            else {
                if (this.children.isEmpty()) {
                    token = new PropertyToken(this.start, this.end, str.substring(this.start + this.type.length() + 2, this.end -2), this.type, this.trigger);
                }
                else {
                    token = new PropertyToken(this.start, this.end, "", this.type, this.trigger);
                    idx += this.type.length() + 2;
                }
            }
            parent.addSubToken(token);

            for (TypeDef tdi : this.children) {
                if (tdi.start > idx) {
                    PropertyToken sToken = new PropertyToken(idx, tdi.start, str.substring(idx, tdi.start), "", -1);
                    token.addSubToken(sToken);
                }
                idx = tdi.end;
                tdi.addToken(str, token);
            }
            if (this.trigger >= 0) {
                if (!this.children.isEmpty()) {
                    if (idx < (this.end -2)) {
                        PropertyToken sToken = new PropertyToken(idx, this.end -2, str.substring(idx, this.end -2), "", -1);
                        token.addSubToken(sToken);
                    }
                }
            }
            if (this.trigger == -2) {
                if (idx < (this.end)) {
                    PropertyToken sToken = new PropertyToken(idx, this.end, str.substring(idx, this.end), "", -1);
                    token.addSubToken(sToken);
                }
            }
        }

        @Override
        public String toString() {
            return "TypeDef [type=" + this.type + ", trigger=" + this.trigger + ", start=" + this.start  + ", end=" + this.end + "]";
        }

        public String toString(int offset) {
            String tmp = "";
            for (int i = 0; i < offset; i++) {
                tmp += "\t";
            }
            tmp += toString();
            String tmpC = "";
            for (TypeDef tdi : this.children) {
                tmpC += "\n" + tdi.toString(offset + 1);
            }
            return tmp + tmpC;
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
        TypeDef root = PropertiesHandler.extractTypesTree(str);
        PropertyToken token = new PropertyToken(0, 0, "", "", 0);
        root.addToken(str, token);
        return token;
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
    public static List<TypeDef> extractTypes(String str)
    {
        List<TypeDef> types = new ArrayList<TypeDef>();

        for (String type : propSet) {
            int start = 0;

            while ((start != -1) && (start < str.length())) {
                start = str.indexOf(type, start);
                if (start != -1) {
                    if ((start + type.length() + 2) < str.length()) {
                        String trigger = str.substring(start + type.length(), start + type.length() + 2);
                        if (trigger.equals(PropertyHandler.PROPS_START[0])) {
                            types.add(new TypeDef(type, 0, start));
                            start += type.length() + 2;
                        }
                        else if (trigger.equals(PropertyHandler.PROPS_START[1])) {
                            types.add(new TypeDef(type, 1, start));
                            start += type.length() + 2;
                        }
                        else if (trigger.equals(PropertyHandler.PROPS_START[2])) {
                           types.add(new TypeDef(type, 2, start));
                           start += type.length() + 2;
                        }
                        else {
                            start++;
                        }
                    }
                    else {
                        start++;
                    }
                }
            }
        }

        Collections.sort(types, (t1, t2) -> Integer.compare(t1.start, t2.start));

        Stack<TypeDef> typeStack = new Stack<TypeDef>();

        for (int i = 0; i < types.size(); i++) {
            TypeDef td = types.get(i);
            boolean canClose = true;
            int end = str.indexOf(PropertyHandler.PROPS_END[td.trigger], td.start);
            for (int j = i +1; j < types.size(); j++) {
                TypeDef td2 = types.get(j);
                if (td.trigger == td2.trigger) {
                    canClose = false;
                    if (end > td2.start) {
                        typeStack.push(td);
                    }
                    else {
                        td.end = end + 2;
                    }
                    break;
                }
            }
            if (canClose) {
                td.end = end + 2;
            }
        }

        while (!typeStack.isEmpty()) {
            TypeDef td = typeStack.pop();

            boolean canClose = true;
            int i = types.indexOf(td);
            int end = str.indexOf(PropertyHandler.PROPS_END[td.trigger], td.start);
            for (int j = i+1; j < types.size(); j++) {
                TypeDef td2 = types.get(j);
                if (td.trigger == td2.trigger) {
                    if ((td2.start < end) && (end < td2.end)) {
                        end = str.indexOf(PropertyHandler.PROPS_END[td.trigger], td2.end);
                        continue;
                    }
                }
            }
            if (canClose) {
                td.end = end + 2;
            }
        }

        return types;
    }

    /**
     * @param str
     * @return
     */
    public static TypeDef extractTypesTree(String str)
    {
        TypeDef root = new TypeDef("", -2, 0, str.length());
        List<TypeDef> types = PropertiesHandler.extractTypes(str);

        for (TypeDef td : types) {
            root.addChild(td);
        }
        return root;
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

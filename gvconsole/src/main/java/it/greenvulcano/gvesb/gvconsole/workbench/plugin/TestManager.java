/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Node;

/**
 * <code>TestManager</code> utility class to get the readMethods and the
 * writeMethods requested by user. <br>
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class TestManager
{

    /**
     * The configuration file
     */
    public static final String CONFIGURATION_FILE = "GVWorkbenchConfig.xml";

    /**
     * The HttpServletRequest
     */
    private HttpServletRequest request;

    /**
     * Constructor to value the request object
     *
     * @param request
     *        The HttpServletRequest object
     */
    public TestManager(HttpServletRequest request)
    {
        this.request = request;
    }

    /**
     * Get the wrapper for this test and set it in the session
     *
     * @return wrapper <code>TestPluginWrapper</code>
     * @throws Throwable
     *         if an error occurred
     */
    public TestPluginWrapper getWrapper() throws Throwable
    {
        HttpSession session = request.getSession();
        String currentTest = (String) session.getAttribute("currentTest");
        if(currentTest == null || currentTest.equals("")) {
            currentTest = "Core";
        }
        TestPluginWrapper wrapper = (TestPluginWrapper) session.getAttribute(currentTest);

        if (wrapper != null) {
            return wrapper;
        }
        // If the wrapper object is not present the class manager create it
        //
        wrapper = createWrapper(currentTest);
        session.setAttribute(currentTest, wrapper);
        return wrapper;
    }

    /**
     * Get the plug-in class <br>
     *
     * @return testPlugin TestPlugin class for the current test
     * @throws Throwable
     *         if an error occurred
     */
    public TestPlugin getPlugin() throws Throwable
    {
        TestPluginWrapper wrapper = getWrapper();
        return wrapper.getTestPlugin();
    }

    /**
     * Creates the wrapper class for the required test <br>
     *
     * @param currentTest
     *        Name of the current test
     * @return wrapper <code>TestPluginWrapper</code> with the plugin object.
     * @throws Throwable
     *         if an error occurred
     */
    protected TestPluginWrapper createWrapper(String currentTest) throws Throwable
    {

        String xPath = "/GVWorkbenchConfig/*[@id='" + currentTest + "']";

        Node configNode = XMLConfig.getNode(CONFIGURATION_FILE, xPath);

        String className = XMLConfig.get(configNode, "@class");
        Class<?> pluginTest = Class.forName(className);

        TestPlugin testPlugin = (TestPlugin) pluginTest.newInstance();

        // Initialize the object variables
        //
        testPlugin.init(configNode);

        return new TestPluginWrapper(CONFIGURATION_FILE, xPath, testPlugin);
    }

    /**
     * The parameter type to execute introspection
     */
    private static final Class<?>[] paramTypes = new Class[]{HttpServletRequest.class};

    /**
     * Invoke the method request by user
     *
     * @param methodName
     *        name of the method to invoke
     * @throws Throwable
     *         if an error occurred
     */
    public void invoke(String methodName) throws Throwable
    {

        TestPluginWrapper wrapper = getWrapper();
        try {
            TestPlugin plugin = wrapper.getTestPlugin();
            Class<? extends TestPlugin> pluginClass = plugin.getClass();
            Method method = pluginClass.getMethod(methodName, paramTypes);

            try {
                Object result = method.invoke(plugin, new Object[]{request});
                if (result != null) {
                    if (result.getClass() == Boolean.class) {
                        wrapper.setThrowable(null);

                        // if the method has a boolean value return then
                        // showsResult else do nothing.
                        //
                        wrapper.setShowsResult(((Boolean) result).booleanValue());

                    }
                }
            }
            catch (InvocationTargetException exc) {
                throw exc.getTargetException();
            }
        }
        catch (Throwable throwable) {
            wrapper.setThrowable(throwable);
        }
    }

    /**
     * Invoke the get method with two input parameters to encode or not the
     * result. <br>
     *
     * @param fieldName
     *        name of the input parameter to pass at the ReadMethod to invoke
     * @return result the object result
     * @throws Throwable
     *         if an error occurred
     */
    public Object get(String fieldName) throws Throwable
    {
        return get(fieldName, true);
    }

    /**
     * Get the ReadMethod(get) to invoke, get his parameter and encode the
     * result if the boolean is <code>true</code><br>
     * .
     *
     * @param fieldName
     *        name of the property as the javaBeans specifications
     * @param mustEncode
     *        boolean value to encode the result
     * @return result the object result <br>
     *         encodeResult result object without HTML code <br>
     *         <code>spaces</code> if the ReadMethod is null. <br>
     *         exception if an exception occurred
     * @throws Throwable
     *         if an error occurred
     */
    public Object get(String fieldName, boolean mustEncode) throws Throwable
    {
        TestPlugin plugin = getPlugin();
        Class<? extends TestPlugin> pluginClass = plugin.getClass();

        PropertyDescriptor descriptor = new PropertyDescriptor(fieldName, pluginClass);
        Method method = descriptor.getReadMethod();

        if (method == null) {
            return "";
        }
        try {
            Object result = method.invoke(plugin, (Object[]) null);
            if (mustEncode) {
                return encode(result);
            }
            return result;
        }
        catch (InvocationTargetException exc) {
            exc.printStackTrace();
            return exc.getTargetException().toString();
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
            return throwable.toString();
        }
    }

    /**
     * Get the WriteMethod(set) to invoke.
     *
     * @param fieldName
     *        name of the property as the javaBeans specifications
     * @param fieldValue
     *        name of method parameter
     * @return result the object result <br>
     *         encodeResult result object without HTML code <br>
     *         Throwable if an exception occurred.
     * @throws Throwable
     *         if an error occurred
     */
    public Object set(String fieldName, Object fieldValue) throws Throwable
    {
        TestPlugin plugin = getPlugin();
        Class<? extends TestPlugin> pluginClass = plugin.getClass();

        PropertyDescriptor descriptor = new PropertyDescriptor(fieldName, pluginClass);
        Method method = descriptor.getWriteMethod();
        if (method == null) {
            return "";
        }

        try {
            return method.invoke(plugin, new Object[]{fieldValue});
        }
        catch (InvocationTargetException exc) {
            return exc.getTargetException().toString();
        }
    }

    /**
     * @param fieldName
     * @param gvBuffer
     * @return the result of invoked set method
     * @throws Throwable
     */
    public Object set(String fieldName, GVBuffer gvBuffer) throws Throwable
    {
        TestPlugin plugin = getPlugin();
        Class<? extends TestPlugin> pluginClass = plugin.getClass();

        PropertyDescriptor descriptor = new PropertyDescriptor(fieldName, pluginClass);
        Method method = descriptor.getWriteMethod();
        if (method == null) {
            return "";
        }

        try {
            return method.invoke(plugin, new Object[]{gvBuffer});
        }
        catch (InvocationTargetException exc) {
            return exc.getTargetException().toString();
        }
    }

    /**
     * Get the WriteMethod(set) to invoke.
     *
     * @param fieldName
     *        name of the property as the javaBeans specifications
     * @param fieldValue
     *        name of method parameter
     * @return the object result <br>
     *         encodeResult result object without HTML code <br>
     *         Throwable if an exception occurred.
     * @throws Throwable
     *         if an error occurred
     */
    public Object set(String fieldName, Integer[] fieldValue) throws Throwable
    {
        TestPlugin plugin = getPlugin();
        Class<? extends TestPlugin> pluginClass = plugin.getClass();

        PropertyDescriptor descriptor = new PropertyDescriptor(fieldName, pluginClass);
        Method method = descriptor.getWriteMethod();
        if (method == null) {
            return "";
        }

        try {
            return method.invoke(plugin, new Object[]{fieldValue});
        }
        catch (InvocationTargetException exc) {
            return exc.getTargetException().toString();
        }
    }

    /**
     * Get the result object and encode it.
     *
     * @param obj
     *        a result object
     * @return objectEncode result object encode <br>
     *         <code>spaces</code> if the object is null.
     */

    private String encode(Object obj)
    {
        if (obj == null) {
            return "";
        }

        return TestPluginWrapper.encode(obj.toString());
    }

    /**
     * Reset action to remove from session the current test Object
     *
     * @throws Throwable
     *         if an error occurred
     */
    public void reset() throws Throwable
    {
        HttpSession session = request.getSession();
        String currentTest = (String) session.getAttribute("currentTest");
        session.removeAttribute(currentTest);
    }

    /**
     * No-cache
     *
     * @param response
     *        HttpServletResponse
     * @throws Throwable
     *         if an error occurred
     */
    public void cleanCache(HttpServletResponse response) throws Throwable
    {
        response.setHeader("Expires", "-1");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
    }
}

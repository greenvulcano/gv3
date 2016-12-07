/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.js.util;

import java.io.InputStreamReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

/**
 * Helper class for script execution.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 **/
public final class JavaScriptHelper
{
    /**
     * Constructor.
     */
    private JavaScriptHelper()
    {
        // do nothing
    }

    /**
     * Execute a script string in the given scope.
     *
     * @param script
     *        a string containing a script
     * @param name
     *        a name for the script, used in exception message
     * @param scope
     *        the execution scope
     * @param cx
     *        the execution context, can be null
     * @return the script execution result
     * @throws Exception
     *         if error occurs
     */
    public static Object executeScript(String script, String name, Scriptable scope, Context cx) throws Exception
    {
        boolean cxcreated = false;

        if (cx == null) {
            cx = ContextFactory.getGlobal().enterContext();
            cxcreated = true;
        }
        try {
            return cx.evaluateString(scope, script, name, 1, null);
        }
        finally {
            if (cxcreated) {
                Context.exit();
            }
        }
    }

    /**
     * Execute a script file in the given scope.
     *
     * @param name
     *        the script name
     * @param scope
     *        the execution scope
     * @param cx
     *        the execution context, can be null
     * @param useCache
     *        if true the script is cached
     * @return the script execution result
     * @throws Exception
     *         if error occurs
     */
    public static Object executeFile(String name, Scriptable scope, Context cx, boolean useCache) throws Exception
    {
        boolean cxcreated = false;

        if (cx == null) {
            cx = ContextFactory.getGlobal().enterContext();
            cxcreated = true;
        }
        try {
            if (useCache) {
                String script = ScriptCache.instance().getScript(name);
                return executeScript(script, name, scope, cx);
            }
            InputStreamReader isr = new InputStreamReader(ClassLoader.getSystemResourceAsStream(name));
            return cx.evaluateReader(scope, isr, name, 1, null);
        }
        finally {
            if (cxcreated) {
                Context.exit();
            }
        }
    }

    /**
     * Convert a script execution result in a string format.
     *
     * @param result
     *        the result to convert to string
     * @return the string representation of result
     */
    public static String resultToString(Object result)
    {
    	if (result == null) return "";
        return Context.toString(result);
    }

    /**
     * Convert a script execution result in a Java object.
     *
     * @param result
     *        the result to convert to Java object
     * @return the Java object representation of result
     */
    public static Object resultToObject(Object result, Class<?> desiredType)
    {
        return Context.jsToJava(result, desiredType);
    }

}

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.greenvulcano.js.initializer;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Node;

/**
 * JSInit class
 *
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
**/
public abstract class JSInit {
    /**
     * Initialize the scope generator.
     *
     * @param script
     *            the initialization script
     * @throws Exception
     *             if error occurs
     */
    public abstract void init(String script) throws Exception;

    /**
     * Initialize the scope generator.
     *
     * @param node
     *            the initialization node
     * @throws Exception
     *             if error occurs
     */
    public abstract void init(Node node) throws Exception;

    /**
     * Create a scope to be used for script execution.
     *
     * @return The scope to be used
     * @throws Exception
     *             if error occurs
     */
    public abstract Scriptable getScope() throws Exception;

    /**
     * Perform cleanup operation.
     */
    public abstract void destroy();

    /**
     * Create a scope to be used for script execution.
     *
     * @param prototype
     *            the scope to be used as prototype
     * @return The scope to be used
     * @throws Exception
     *             if error occurs
     */
    public static Scriptable getScope(Scriptable prototype) throws Exception {
        Context cx = ContextFactory.getGlobal().enterContext();
        try {
            if (prototype == null) {
                throw new IllegalArgumentException("The 'prototype' field can't be null");
            }
            Scriptable newScope = cx.newObject(prototype);
            newScope.setPrototype(prototype);
            newScope.setParentScope(null);
            return newScope;
        }
        finally {
            Context.exit();
        }
    }

    /**
     * Insert a function in the scope.
     *
     * @param scope
     *            the scope to be use
     * @param functionName
     *            the function name
     * @param function
     *            the function definition
     * @return the enriched scope
     * @throws Exception
     *             if error occurs
     */
    public static Scriptable compileFunction(Scriptable scope, String functionName, String function) throws Exception {
        Context cx = ContextFactory.getGlobal().enterContext();
        try {
            if (scope == null) {
                throw new IllegalArgumentException("The 'scope' field can't be null");
            }
            Object fnct = cx.compileFunction(scope, function, functionName, 1, null);
            ScriptableObject.putProperty(scope, functionName, fnct);
            return scope;
        }
        finally {
            Context.exit();
        }
    }

    /**
     * Insert a property in the scope.
     *
     * @param scope
     *            the scope to be use
     * @param name
     *            the property name
     * @param property
     *            the objecto to insert
     * @return the enriched scope
     * @throws Exception
     *             if error occurs
     */
    public static Scriptable setProperty(Scriptable scope, String name, Object property) throws Exception {
        if (scope == null) {
            throw new IllegalArgumentException("The 'scope' field can't be null");
        }
        Object obj = Context.javaToJS(property, scope);
        ScriptableObject.putProperty(scope, name, obj);
        return scope;
    }
}

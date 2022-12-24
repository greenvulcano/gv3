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
package it.greenvulcano.js.initializer;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.js.function.IncludeFunction;
import it.greenvulcano.js.function.InstanceOfFunction;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Node;

/**
 * BasicJSInit class
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class BasicJSInit extends JSInit
{
    /**
     * Default scope.
     */
    private Scriptable basicScope = null;
    /**
     * Instance name.
     */
    private String     name       = "";

    /**
     * Constructor.
     */
    public BasicJSInit()
    {
        // do nothing
    }

    /**
     * Constructor.
     *
     * @param script
     *        initialization script
     * @throws Exception
     *         if error occurs
     */
    public BasicJSInit(String script) throws Exception
    {
        this.name = script;
        init(script);
    }

    /**
     * Create a scope to be used for script execution.
     *
     * @return The scope to be used
     */
    @Override
    public final Scriptable getScope()
    {
        Context cx = Context.enter();
        try {
            getBasicScope(cx);
            Scriptable newScope = cx.newObject(this.basicScope);
            newScope.setPrototype(this.basicScope);
            //newScope.setParentScope(null);
            return newScope;
        }
        finally {
            Context.exit();
        }
    }

    /**
     * Initialize the default scope.
     *
     * @param script
     *        the initialization script
     * @throws Exception
     *         if error occurs
     */
    @Override
    public final void init(String script) throws Exception
    {
        if ((script == null) || (script.equals(""))) {
            throw new IllegalArgumentException("The 'script' argument can't be null or empty");
        }
        this.name = script;
        Context cx = Context.enter();
        this.basicScope = null;
        try {
            getBasicScope(cx);
            InputStream scriptStream = ClassLoader.getSystemResourceAsStream(script);
            if (scriptStream == null) {
                scriptStream = getClass().getClassLoader().getResourceAsStream(script);
            }
            InputStreamReader isr = new InputStreamReader(scriptStream);
            Script initscript = cx.compileReader(isr, script, 1, null);
            initscript.exec(cx, this.basicScope);
        }
        finally {
            Context.exit();
        }
    }

    /**
     * Initialize the default scope.
     *
     * @param node
     *        the initialization node
     * @throws Exception
     *         if error occurs
     */
    @Override
    public final void init(Node node) throws Exception
    {
        Context cx = Context.enter();
        this.basicScope = null;
        try {
            String scriptfile = XMLConfig.get(node, "@script-file", "");
            if (!scriptfile.equals("")) {
                init(scriptfile);
            }
            this.name = XMLConfig.get(node, "@name", node.getLocalName());
            getBasicScope(cx);
            String script = XMLConfig.get(node, "Script", "");
            if (!script.equals("")) {
                cx.evaluateString(this.basicScope, script, this.name, 1, null);
            }
        }
        finally {
            Context.exit();
        }
    }

    /**
     * Perform cleanup operation.
     */
    @Override
    public final void destroy()
    {
        this.basicScope = null;
    }

    /**
     * Create/return the default scope.
     *
     * @param cx
     *        the context to be used for scope creation
     * @return the default scope
     */
    protected final Scriptable getBasicScope(Context cx)
    {
        if (this.basicScope == null) {
            cx.initStandardObjects();
            cx.setLanguageVersion(Context.VERSION_1_8);
            this.basicScope = new ImporterTopLevel(cx);
            //ImporterTopLevel.init(cx, this.basicScope, false);
            //this.basicScope = cx.newObject(null);
            setBasicProperties();
        }
        return this.basicScope;
    }

    /**
     * Set some basic properties in the default scope.
     */
    protected final void setBasicProperties()
    {
        Object jsOut = Context.javaToJS(System.out, this.basicScope);
        ScriptableObject.putProperty(this.basicScope, "out", jsOut);
        Object jsErr = Context.javaToJS(System.err, this.basicScope);
        ScriptableObject.putProperty(this.basicScope, "err", jsErr);

        ScriptableObject.putProperty(this.basicScope, "include", new IncludeFunction());
        ScriptableObject.putProperty(this.basicScope, "instanceOf", new InstanceOfFunction());
    }
}

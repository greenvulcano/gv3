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
package it.greenvulcano.gvesb.internal.condition;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.js.initializer.JSInit;
import it.greenvulcano.js.initializer.JSInitManager;
import it.greenvulcano.js.util.JavaScriptHelper;
import it.greenvulcano.js.util.ScriptCache;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.Map;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class JavaScriptCondition implements GVCondition
{
    private static final Logger logger          = GVLogger.getLogger(JavaScriptCondition.class);

    /**
     * The default script name.
     */
    private static final String INTERNAL_SCRIPT = "internal";

    /**
     * The Condition name.
     */
    private String              condition       = "";
    /**
     * The execution scope for the script.
     */
    private String              scopeName       = "";
    /**
     * The script file name.
     */
    private String              scriptName      = "";
    /**
     * The script.
     */
    private String              script          = "";
    /**
     * The execution context.
     */
    private Context             cx              = null;
    /**
     * If true an execution exception must be propagate to the caller.
     */
    private boolean             throwException  = false;

    /**
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#init(org.w3c.dom.Node)
     */
    @Override
    public final void init(Node node) throws XMLConfigException
    {
        condition = XMLConfig.get(node, "@condition", "");
        logger.debug("Initializing JavaScriptCondition: " + condition);
        throwException = XMLConfig.getBoolean(node, "@throw-exception", false);
        scopeName = XMLConfig.get(node, "@scope-name");
        scriptName = XMLConfig.get(node, "@script-file", INTERNAL_SCRIPT);
        if (scriptName.equals(INTERNAL_SCRIPT)) {
            script = XMLConfig.get(node, "Script");
            if (script.length() == 0) {
                throw new XMLConfigException(
                        "Must be defined at least the @script-file attribute or the Script element. Node: "
                                + XPathFinder.buildXPath(node));
            }
        }
    }

    /**
     * @throws GVConditionException
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#check(java.lang.String,
     *      java.util.Map)
     */
    @Override
    public final boolean check(String dataName, Map<String, Object> environment) throws GVConditionException
    {
        boolean result = false;

        logger.debug("BEGIN - Cheking JavaScriptCondition[" + condition + "]");
        try {
            cx = ContextFactory.getGlobal().enterContext();
            Scriptable scope = null;
            scope = JSInitManager.instance().getJSInit(scopeName).getScope();
            scope = JSInit.setProperty(scope, "environment", environment);
            scope = JSInit.setProperty(scope, "dataName", dataName);
            scope = JSInit.setProperty(scope, "logger", logger);
            if (!scriptName.equals(INTERNAL_SCRIPT)) {
                script = ScriptCache.instance().getScript(scriptName);
            }
            String jsResult = JavaScriptHelper.resultToString(JavaScriptHelper.executeScript(script, scriptName, scope,
                    cx));
            logger.debug("JavaScriptCondition::check() -- RESULT: [" + jsResult + "]");
            result = (jsResult.equalsIgnoreCase("true") || jsResult.equalsIgnoreCase("yes") || jsResult.equalsIgnoreCase("on"));

            return result;
        }
        catch (Exception exc) {
            logger.error("Error occurred on JavaScriptCondition.check()", exc);
            if (throwException) {
                throw new GVConditionException("JAVASCRIPT_CONDITION_EXEC_ERROR", new String[][]{
                        {"condition", condition}, {"scopeName", scopeName}, {"scriptName", scriptName},
                        {"exception", "" + exc}}, exc);
            }
        }
        finally {
            logger.debug("END - Cheking JavaScriptCondition[" + condition + "]: " + result);
        }

        return result;
    }

    /**
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#cleanUp()
     */
    @Override
    public final void cleanUp()
    {
        if (cx != null) {
            cx = null;
            Context.exit();
        }
    }
}

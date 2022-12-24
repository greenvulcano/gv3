/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
package it.greenvulcano.expression.js;

import it.greenvulcano.expression.ExpressionEvaluator;
import it.greenvulcano.expression.ExpressionEvaluatorException;
import it.greenvulcano.js.initializer.JSInit;
import it.greenvulcano.js.initializer.JSInitManager;
import it.greenvulcano.js.util.JavaScriptHelper;
import it.greenvulcano.log.GVLogger;

import java.util.Map;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

/**
 * Uses JSCRIPT_EXPRESSION_LANGUAGE to evaluate expressions on POJO.
 * 
 * @version 3.2.0 17/dic/2012
 * @author GreenVulcano Developer Team
 */
public class JavaScriptExpressionEvaluator implements ExpressionEvaluator
{
    private static final Logger logger    = GVLogger.getLogger(JavaScriptExpressionEvaluator.class);
    private Context             context   = null;
    private Scriptable          scope     = null;
    private String              scopeName = "gvesb";

    /* (non-Javadoc)
     * @see it.greenvulcano.expression.ExpressionEvaluator#addToContext(java.lang.String, java.lang.Object)
     */
    @Override
    public void addToContext(String key, Object value)
    {
        try {
            initContext();
            this.scope = JSInit.setProperty(this.scope, key, value);
        }
        catch (Exception exc) {
            logger.error("JavaScriptExpressionEvaluator - Error setting key[" + key + "] in context", exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.expression.ExpressionEvaluator#addAllToContext(java.util.Map)
     */
    @Override
    public void addAllToContext(Map<String, Object> context)
    {
        try {
            initContext();
            for (Map.Entry<String, Object> element : context.entrySet()) {
                try {
                    this.scope = JSInit.setProperty(this.scope, element.getKey(), element.getValue());
                }
                catch (Exception exc) {
                    logger.error("JavaScriptExpressionEvaluator - Error setting key[" + element.getKey() + "] in context", exc);
                }
            }
        }
        catch (Exception exc) {
            logger.error("JavaScriptExpressionEvaluator - Error initializing context", exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.expression.ExpressionEvaluator#getValue(java.lang.String, java.lang.Object)
     */
    @Override
    public Object getValue(String expression, Object object) throws ExpressionEvaluatorException
    {
        try {
            initContext();
            this.scope = JSInit.setProperty(this.scope, "root", object);
            Object result = JavaScriptHelper.executeScript(expression, "JavaScriptExpressionEvaluator", this.scope, this.context);
            if (result instanceof NativeJavaObject) {
                result = ((NativeJavaObject) result).unwrap();
            }
            return result;
        }
        catch (Exception exc) {
            logger.error("Error evaluating the expression [\n" + expression + "\n].", exc);
            throw new ExpressionEvaluatorException("Error evaluating the expression " + expression, exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.expression.ExpressionEvaluator#setValue(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void setValue(String expression, Object value, Object object) throws ExpressionEvaluatorException
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.expression.ExpressionEvaluator#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        if (this.context != null) {
            this.context = null;
            Context.exit();
        }
    }

    private void initContext() throws Exception
    {
        if (this.context == null) {
            this.context = Context.enter();
            this.scope = JSInitManager.instance().getJSInit(this.scopeName).getScope();
            this.scope = JSInit.setProperty(this.scope, "logger", logger);
        }
    }
}

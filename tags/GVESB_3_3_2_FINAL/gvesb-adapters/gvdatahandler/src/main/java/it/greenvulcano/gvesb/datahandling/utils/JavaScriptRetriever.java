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
package it.greenvulcano.gvesb.datahandling.utils;

import it.greenvulcano.js.initializer.JSInit;
import it.greenvulcano.js.initializer.JSInitManager;
import it.greenvulcano.js.util.JavaScriptHelper;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.txt.TextUtils;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;


/**
 * JavaScriptRetriever class
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JavaScriptRetriever extends AbstractRetriever
{
    private static final Logger logger  = GVLogger.getLogger(JavaScriptRetriever.class);

    private static String       jsScope = "JavaScriptRetriever";

    /**
     * @see it.greenvulcano.gvesb.datahandling.utils.AbstractRetriever#getDataRetriever()
     */
    @Override
    protected String getDataRetriever()
    {
        return null;
    }

    /**
     * @param method
     * @param paramList
     * @return the retrieved data
     * @throws Exception
     */
    public static String getData(String method, String paramList) throws Exception
    {
        JavaScriptRetriever retr = AbstractRetriever.javaScriptRetrieverInstance();
        Map<String, String> resultsCache = retr.getMethodCache(method);
        boolean cacheable = false;
        if (resultsCache != null){
            cacheable = true;
            if (resultsCache.containsKey(paramList)){
                String result = resultsCache.get(paramList);
                logger.debug("Result Function [" + method + "] from cache: " + result);
                return result;
            }
        }

        List<String> paramL = TextUtils.splitByStringSeparator(paramList, ",");
        String jsFunction = retr.getDataRetriever(method, paramL);
        String result = null;
        if (jsFunction != null) {
            Context ctx = null;
            try {
                ctx = Context.enter();
                Scriptable scope = JSInitManager.instance().getJSInit(jsScope).getScope();
                Map<String, Object> params = retr.getMethodParamMap(method, paramL);
                handleArguments(scope, jsFunction, params);
                result = JavaScriptHelper.resultToString(JavaScriptHelper.executeScript(jsFunction, method, scope, ctx));
            }
            catch (Exception exc) {
                throw new Exception("Error executing the javascript function in JavaScriptRetriever '" + method
                        + "' method.", exc);
            }
            finally {
                if (ctx != null) {
                    Context.exit();
                }
            }
        }

        if (cacheable){
            resultsCache.put(paramList, result);
        }

        logger.debug("Result Function [" + method + "] calculated: " + result);

        return result;
    }

    private static void handleArguments(Scriptable scope, String jsFunction, Map<String, Object> params)
            throws Exception
    {
        StringTokenizer st = new StringTokenizer(jsFunction, "\"'=!<>()+*-%\\/,; ");
        while (st.hasMoreTokens()) {
            String varName = st.nextToken();
            if (params.containsKey(varName)) {
                Object varValue = params.get(varName);
                JSInit.setProperty(scope, varName, varValue);
            }
        }
        JSInit.setProperty(scope, "props", params);
    }
}
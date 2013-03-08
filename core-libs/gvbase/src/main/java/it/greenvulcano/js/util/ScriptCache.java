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
package it.greenvulcano.js.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Perform a script file cache.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 **/
public final class ScriptCache
{
    /**
     * #include directive size.
     */
    private static final int        INCLUDE_DIR_SIZE = 9;
    /**
     * Singleton reference.
     */
    private static ScriptCache      _instance        = null;
    /**
     * Script file map.
     */
    private HashMap<String, String> scriptMap        = new HashMap<String, String>();

    /**
     * Constructor.
     */
    private ScriptCache()
    {
        // do nothing
    }

    /**
     * Singleton entry point.
     *
     * @return the instance reference
     */
    public static synchronized ScriptCache instance()
    {
        if (_instance == null) {
            _instance = new ScriptCache();
        }
        return _instance;
    }

    /**
     * Get the requested script from cache.
     *
     * @param name
     *        the script file name
     * @return the requested script
     * @throws Exception
     *         if error occurs
     */
    public synchronized String getScript(String name) throws Exception
    {
        String script = scriptMap.get(name);

        if (script == null) {
            script = readScript(name);
            scriptMap.put(name, script);
        }

        return script;
    }

    /**
     * Read the requested script from class-path.
     *
     * @param name
     *        the script file name
     * @return the requested script
     * @throws Exception
     *         if error occurs
     */
    private String readScript(String name) throws Exception
    {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(ScriptCache.class.getClassLoader().getResourceAsStream(name)));
            StringBuilder sb = new StringBuilder();
            String line = in.readLine();
            while (line != null) {
                sb.append(line);
                line = in.readLine();
            }
            handleImport(sb);
            String script = sb.toString();
            if (script.length() == 0) {
                throw new IllegalArgumentException("The script named '" + name + "' is invalid.");
            }
            return script;
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Viene invocato da JSInitManager a fronte di un reload della
     * configurazione.
     */
    public synchronized void clearMap()
    {
        scriptMap.clear();
    }

    /**
     * Risolve le direttive 'include'.
     *
     * @param sb
     *        Lo script corrente.
     * @throws Exception
     *         Se ci sono errori.
     */
    private void handleImport(StringBuilder sb) throws Exception
    {
        int idx = sb.indexOf("include(");
        while (idx != -1) {
            int start = idx + INCLUDE_DIR_SIZE;
            int stop = sb.indexOf(");", start) - 1;
            String name = sb.substring(start, stop);
            String include = readScript(name);
            sb.replace(idx, stop + 2, include);
            idx = sb.indexOf("include(");
        }
    }
}

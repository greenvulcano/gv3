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
package tests.unit.js;

import it.greenvulcano.js.initializer.JSInit;
import it.greenvulcano.js.initializer.JSInitManager;
import it.greenvulcano.js.util.JavaScriptHelper;
import junit.framework.TestCase;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

/**
 * @version 3.0.0 Apr 3, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class JSTestCase extends TestCase
{
    /**
     * @throws Exception
     */
    public final void testJS() throws Exception
    {
        Context cx = ContextFactory.getGlobal().enterContext();
        Scriptable scope = null;
        try {
            String scopeName = "gvesb";
            scope = JSInitManager.instance().getJSInit(scopeName).getScope();
            scope = JSInit.setProperty(scope, "data", "test");
            String script = "data = data + ' successful';";
            assertEquals("test successful", JavaScriptHelper.executeScript(script, "internal", scope, cx));
        }
        finally {
            Context.exit();
        }
    }
}

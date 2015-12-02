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
package it.greenvulcano.js.function;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

/**
 * Perform the Java instanceof check.
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
*/
public class InstanceOfFunction extends BaseFunction {
    /**
     *
     */
    private static final long serialVersionUID = -1024901631417295477L;

    /**
     * Constructor.
     */
    public InstanceOfFunction() {
        // do nothing
    }

    /**
     * Perform the #include job.
     *
     * @param cx
     *            the execution context
     * @param scope
     *            the execution scope
     * @param thisObj
     *            the this reference
     * @param args
     *            the first and second argumento of instanceof call
     * @return a Boolean representing the instanceof result
     * @throws JavaScriptException
     *             if error occurs
     */
    @Override
    public final Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args)
            throws JavaScriptException {
        if (args.length == 0) {
            return Boolean.FALSE;
        }
        try {
            Object obj = ((Wrapper) args[0]).unwrap();
            Class<?> cls = Class.forName((String) args[1]);
            return Boolean.valueOf(cls.isInstance(obj));
        }
        catch (Exception exc) {
            return Boolean.FALSE;
        }
    }
}

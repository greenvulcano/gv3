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
package tests.unit.gvdebug;

import it.greenvulcano.gvesb.core.debug.ejb3.GVDebugger;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

/**
 * @version 3.0.0 Mar 5, 2010
 * @author GreenVulcano Developer Team
 * 
 */
public class EJBGVDebugTestCase extends TestCase
{

    private Context initialContext;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        initialContext = new InitialContext();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        if (initialContext != null) {
            initialContext.close();
        }
    }

    /**
     * @throws Exception
     * 
     */
    public void testEJBGVDebugRemote() throws Exception
    {
        GVDebugger bean = (GVDebugger) initialContext.lookup("gvesb/core/GVDebugger");
        assertNotNull(bean);
    }
}

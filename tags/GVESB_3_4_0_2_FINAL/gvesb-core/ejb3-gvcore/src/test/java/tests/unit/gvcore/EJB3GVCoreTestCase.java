/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package tests.unit.gvcore;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.ejb3.J2EEGreenVulcano;
import it.greenvulcano.gvesb.core.ejb3.J2EEGreenVulcanoLocal;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

/**
 * @version 3.3.0 Feb 5, 2013
 * @author GreenVulcano Developer Team
 *
 */
public class EJB3GVCoreTestCase extends TestCase
{
    /**
     *
     */
    private static final String EXPECTED_RESULT = "HELLO WORLD";

    private Context             initialContext;

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

    public void testDummy()
    {
        assertTrue(true);
    }

    /**
     * @throws Exception
     *
     */
    /*public void testEJBGVCore() throws Exception
    {
        J2EEGreenVulcano greenVulcano = (J2EEGreenVulcano) initialContext.lookup("gvesb/core/GreenVulcano_ejb3/remote");
        GVBuffer gvBuffer = new GVBuffer("GVESB", "TOUPPER");
        gvBuffer.setObject("hello world");
        GVBuffer response = greenVulcano.requestReply(gvBuffer);
        assertEquals(EXPECTED_RESULT, response.getObject());
    }*/
    
    /**
     * @throws Exception
     *
     */
    /*public void testEJBGVCoreLocal() throws Exception
    {
        J2EEGreenVulcano greenVulcano = (J2EEGreenVulcano) initialContext.lookup("gvesb/core/GreenVulcano_ejb3/local");
        GVBuffer gvBuffer = new GVBuffer("GVESB", "TOUPPER");
        gvBuffer.setObject("hello world");
        GVBuffer response = greenVulcano.requestReply(gvBuffer);
        assertEquals(EXPECTED_RESULT, response.getObject());
    }*/
}

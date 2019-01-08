/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package test.unit.ejb;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.util.txt.DateUtils;

import java.util.Calendar;
import java.util.Date;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

/**
 * @version 3.2.0 Gen 31, 2012
 * @author GreenVulcano Developer Team
 * 
 */
public class EJBTestCase extends TestCase
{
    /**
     *
     */
    private static final String   EXPECTED_RESULT   = "HELLO WORLD";
    private static final String[] EXPECTED_RESULT_A = new String[]{"HELLO WORLD", "HAVE FUN", "WE ARE THE BEST"};
    private static final int      EXPECTED_RESULT_I = 10;
    private static final String   EXPECTED_RESULT_T = "31/01/2012 12:45:00";

    private Context               initialContext;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
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
    public void testEJBtoupper() throws Exception
    {
        //J2EETestHome home = (J2EETestHome) initialContext.lookup("gvesb/test/Test");
        J2EETestHome home = (J2EETestHome) initialContext.lookup("J2EETest_mainRemoteHome");
        J2EETest test = home.create();
        GVBuffer gvBuffer = new GVBuffer("GVESB", "TOUPPER");
        gvBuffer.setObject("hello world");
        GVBuffer response = test.toupper(gvBuffer);
        assertEquals(EXPECTED_RESULT, response.getObject());
    }

    /**
     * @throws Exception
     * 
     */
    public void testEJBtoupper2() throws Exception
    {
        //J2EETestHome home = (J2EETestHome) initialContext.lookup("gvesb/test/Test");
        J2EETestHome home = (J2EETestHome) initialContext.lookup("J2EETest_mainRemoteHome");
        J2EETest test = home.create();
        String[] input = new String[]{"hello world", "have fun", "we are the best"};
        String[] response = test.toupper(input);
        for (int i = 0; i < response.length; i++) {
            assertEquals(EXPECTED_RESULT_A[i], response[i]);
        }
    }

    /**
     * @throws Exception
     * 
     */
    public void testEJBsum() throws Exception
    {
        //J2EETestHome home = (J2EETestHome) initialContext.lookup("gvesb/test/Test");
        J2EETestHome home = (J2EETestHome) initialContext.lookup("J2EETest_mainRemoteHome");
        J2EETest test = home.create();
        int response = test.sum(4, 6);
        assertEquals(EXPECTED_RESULT_I, response);
    }

    /**
     * @throws Exception
     * 
     */
    public void testEJBaddTime() throws Exception
    {
        //J2EETestHome home = (J2EETestHome) initialContext.lookup("gvesb/test/Test");
        J2EETestHome home = (J2EETestHome) initialContext.lookup("J2EETest_mainRemoteHome");
        J2EETest test = home.create();
        Date response = test.addTime(DateUtils.stringToDate("31/01/2012 12:15:00", "dd/MM/yyyy HH:mm:ss"),
                Calendar.MINUTE, 30);
        assertEquals(EXPECTED_RESULT_T, DateUtils.dateToString(response, "dd/MM/yyyy HH:mm:ss"));
    }
}

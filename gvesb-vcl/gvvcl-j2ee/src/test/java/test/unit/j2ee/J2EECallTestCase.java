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
package test.unit.j2ee;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.j2ee.J2EECallOperation;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.w3c.dom.Node;

/**
 * @version 3.2.0 Gen 31, 2012
 * @author GreenVulcano Developer Team
 * 
 */
public class J2EECallTestCase extends TestCase
{
    /**
     *
     */
    private static final String EXPECTED_RESULT   = "HELLO WORLD";
    private static final String EXPECTED_RESULT_A = "[HELLO WORLD, HAVE FUN, WE ARE THE BEST]";
    private static final int    EXPECTED_RESULT_I = 10;
    private static final String EXPECTED_RESULT_T = "31/01/2012 12:45:00";

    private Context             initialContext;

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
    public void testEJB2toupper() throws Exception
    {
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/j2ee-ejb-call[@name='ejb2ToupperGVBuffer']");
        J2EECallOperation j2ee = new J2EECallOperation();
        j2ee.init(node);
        GVBuffer gvBuffer = new GVBuffer("GVESB", "TOUPPER");
        gvBuffer.setObject("hello world");
        GVBuffer result = j2ee.perform(gvBuffer);
        assertEquals(EXPECTED_RESULT, result.getObject());
    }

    /**
     * @throws Exception
     * 
     */
    public void testEJB2toupper2() throws Exception
    {
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/j2ee-ejb-call[@name='ejb2ToupperStringArr']");
        J2EECallOperation j2ee = new J2EECallOperation();
        j2ee.init(node);
        GVBuffer gvBuffer = new GVBuffer("GVESB", "TOUPPER");
        gvBuffer.setObject("hello world,have fun,we are the best");
        GVBuffer result = j2ee.perform(gvBuffer);
        assertEquals(EXPECTED_RESULT_A, result.getObject());
    }

    /**
     * @throws Exception
     * 
     */
    public void testEJB2sum() throws Exception
    {
        Node node = XMLConfig.getNode("GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/j2ee-ejb-call[@name='ejb2SumInt']");
        J2EECallOperation j2ee = new J2EECallOperation();
        j2ee.init(node);
        GVBuffer gvBuffer = new GVBuffer("GVESB", "TOUPPER");
        gvBuffer.setProperty("I1", "4");
        gvBuffer.setProperty("I2", "6");
        GVBuffer result = j2ee.perform(gvBuffer);
        assertEquals(EXPECTED_RESULT_I, Integer.parseInt(result.getProperty("SUM")));
    }

    /**
     * @throws Exception
     * 
     */
    public void testEJB2addTime() throws Exception
    {
        Node node = XMLConfig.getNode("GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/j2ee-ejb-call[@name='ejb2AddTime']");
        J2EECallOperation j2ee = new J2EECallOperation();
        j2ee.init(node);
        GVBuffer gvBuffer = new GVBuffer("GVESB", "TOUPPER");
        gvBuffer.setProperty("DATE", "31/01/2012 12:15:00");
        gvBuffer.setProperty("DELTA", "30");
        GVBuffer result = j2ee.perform(gvBuffer);
        assertEquals(EXPECTED_RESULT_T, result.getProperty("DATE_OUT"));
    }

    /**
     * @throws Exception
     * 
     */
    public void testEJB3toupper() throws Exception
    {
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/j2ee-ejb-call[@name='ejb3ToupperGVBuffer']");
        J2EECallOperation j2ee = new J2EECallOperation();
        j2ee.init(node);
        GVBuffer gvBuffer = new GVBuffer("GVESB", "TOUPPER");
        gvBuffer.setObject("hello world");
        GVBuffer result = j2ee.perform(gvBuffer);
        assertEquals(EXPECTED_RESULT, result.getObject());
    }

    /**
     * @throws Exception
     * 
     */
    public void testEJB3toupper2() throws Exception
    {
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/j2ee-ejb-call[@name='ejb3ToupperStringArr']");
        J2EECallOperation j2ee = new J2EECallOperation();
        j2ee.init(node);
        GVBuffer gvBuffer = new GVBuffer("GVESB", "TOUPPER");
        gvBuffer.setObject("hello world,have fun,we are the best");
        GVBuffer result = j2ee.perform(gvBuffer);
        assertEquals(EXPECTED_RESULT_A, result.getObject());
    }

    /**
     * @throws Exception
     * 
     */
    public void testEJB3sum() throws Exception
    {
        Node node = XMLConfig.getNode("GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/j2ee-ejb-call[@name='ejb3SumInt']");
        J2EECallOperation j2ee = new J2EECallOperation();
        j2ee.init(node);
        GVBuffer gvBuffer = new GVBuffer("GVESB", "TOUPPER");
        gvBuffer.setProperty("I1", "4");
        gvBuffer.setProperty("I2", "6");
        GVBuffer result = j2ee.perform(gvBuffer);
        assertEquals(EXPECTED_RESULT_I, Integer.parseInt(result.getProperty("SUM")));
    }

    /**
     * @throws Exception
     * 
     */
    public void testEJB3addTime() throws Exception
    {
        Node node = XMLConfig.getNode("GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/j2ee-ejb-call[@name='ejb3AddTime']");
        J2EECallOperation j2ee = new J2EECallOperation();
        j2ee.init(node);
        GVBuffer gvBuffer = new GVBuffer("GVESB", "TOUPPER");
        gvBuffer.setProperty("DATE", "31/01/2012 12:15:00");
        gvBuffer.setProperty("DELTA", "30");
        GVBuffer result = j2ee.perform(gvBuffer);
        assertEquals(EXPECTED_RESULT_T, result.getProperty("DATE_OUT"));
    }

}

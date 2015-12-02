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
package tests.unit.gvrules;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvrules.virtual.RulesCall;
import junit.framework.TestCase;

import org.w3c.dom.Node;

import tests.unit.gvrules.bean.figure.Circle;
import tests.unit.gvrules.bean.figure.FigureBag;
import tests.unit.gvrules.bean.figure.Square;
import tests.unit.gvrules.bean.figure.Triangle;

/**
 * GV VCL Rules Test class
 * 
 * @version 3.2.0 Feb 10, 2012
 * @author GreenVulcano Developer Team
 */
public class GVVCLRulesTestCase extends TestCase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Test the figures VCL.
     * 
     * @throws Exception
     */
    public static void testFiguresVCL() throws Exception
    {
        FigureBag fb = new FigureBag();
        fb.add(new Triangle("red"));
        fb.add(new Triangle("green"));
        fb.add(new Square("red"));
        fb.add(new Circle("yellow"));
        fb.add(new Circle("green"));

        Node node = XMLConfig.getNode("GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/rules-call[@name='figureFilter']");
        RulesCall rules = new RulesCall();
        rules.init(node);

        GVBuffer gvBuffer = new GVBuffer("GVESB", "RULES");
        gvBuffer.setProperty("COLOR", "red");
        gvBuffer.setObject(fb);
        GVBuffer result = rules.perform(gvBuffer);

        FigureBag out = (FigureBag) result.getObject();

        System.out.println("VCL - " + out);
        assertEquals(out, fb);
        assertEquals(2, out.getFigures().size());
    }


    /**
     * Test the figures VCL.
     * 
     * @throws Exception
     */
    public static void testFiguresVCL2() throws Exception
    {
        FigureBag fb = new FigureBag();
        fb.add(new Triangle("red"));
        fb.add(new Triangle("green"));
        fb.add(new Square("red"));
        fb.add(new Circle("yellow"));
        fb.add(new Circle("green"));

        Node node = XMLConfig.getNode("GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/rules-call[@name='figureFilterOut']");
        RulesCall rules = new RulesCall();
        rules.init(node);

        GVBuffer gvBuffer = new GVBuffer("GVESB", "RULES");
        gvBuffer.setProperty("COLOR", "red");
        gvBuffer.setObject(fb);
        GVBuffer result = rules.perform(gvBuffer);

        FigureBag out = (FigureBag) result.getObject();

        System.out.println("VCL - " + out);
        assertNotSame(out, fb);
        assertEquals(2, out.getFigures().size());
    }

    /**
     * Test the figures VCL.
     * 
     * @throws Exception
     */
    public static void testFiguresVCL3() throws Exception
    {
        FigureBag fb = new FigureBag();
        fb.add(new Triangle("red"));
        fb.add(new Triangle("green"));
        fb.add(new Square("red"));
        fb.add(new Circle("yellow"));
        fb.add(new Circle("green"));

        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/rules-call[@name='figureFilterOutColl']");
        RulesCall rules = new RulesCall();
        rules.init(node);

        GVBuffer gvBuffer = new GVBuffer("GVESB", "RULES");
        gvBuffer.setProperty("COLOR", "red");
        gvBuffer.setObject(fb);
        GVBuffer result = rules.perform(gvBuffer);

        FigureBag out = (FigureBag) result.getObject();

        System.out.println("VCL - " + out);
        assertNotSame(out, fb);
        assertEquals(2, out.getFigures().size());
    }
}

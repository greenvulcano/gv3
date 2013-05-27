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

import it.greenvulcano.gvesb.gvrules.drools.config.GVRulesConfigManager;
import it.greenvulcano.util.txt.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatelessKnowledgeSession;

import tests.unit.gvrules.bean.figure.Circle;
import tests.unit.gvrules.bean.figure.FigureBag;
import tests.unit.gvrules.bean.figure.Square;
import tests.unit.gvrules.bean.figure.Triangle;
import tests.unit.gvrules.bean.license.Applicant;
import tests.unit.gvrules.bean.license.Application;

/**
 * GV Rules Test class
 * 
 * @version 3.2.0 Feb 10, 2012
 * @author GreenVulcano Developer Team
 */
public class GVRulesTestCase extends TestCase
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
     * Test the license.
     * 
     * @throws Exception
     */
    public static void testLicense() throws Exception
    {
        Applicant applicant = new Applicant("Mr John Smith", 16);
        Application application = new Application(DateUtils.addTime(new Date(), Calendar.YEAR, -1));
        List lst = new ArrayList();
        lst.add(applicant);
        lst.add(application);
        StatelessKnowledgeSession ksession = GVRulesConfigManager.instance().getStatelessKnowledgeSession("license");
        assertTrue(application.isValid());
        ksession.execute(lst);
        assertFalse(application.isValid());

        applicant = new Applicant("Mr John Smith", 16);
        application = new Application(new Date());
        lst.clear();
        lst.add(applicant);
        lst.add(application);
        ksession = GVRulesConfigManager.instance().getStatelessKnowledgeSession("license");
        assertTrue(application.isValid());
        ksession.execute(lst);
        assertFalse(application.isValid());

        applicant = new Applicant("Mr John Doe", 21);
        application = new Application(DateUtils.addTime(new Date(), Calendar.YEAR, -1));
        lst.clear();
        lst.add(applicant);
        lst.add(application);
        ksession = GVRulesConfigManager.instance().getStatelessKnowledgeSession("license");
        assertTrue(application.isValid());
        ksession.execute(lst);
        assertTrue(application.isValid());

        applicant = new Applicant("Mr John Doe", 21);
        application = new Application(new Date());
        lst.clear();
        lst.add(applicant);
        lst.add(application);
        ksession = GVRulesConfigManager.instance().getStatelessKnowledgeSession("license");
        assertTrue(application.isValid());
        ksession.execute(lst);
        assertFalse(application.isValid());
    }

    /**
     * Test the license.
     * 
     * @throws Exception
     */
    public static void testLicenseCmd() throws Exception
    {
        List<Command> cmds = new ArrayList<Command>();
        cmds.add(CommandFactory.newInsert(new Applicant("Mr John Smith", 18), "mrSmith"));
        cmds.add(CommandFactory.newInsert(new Applicant("Mr John Doe", 21), "mrDoe"));
        StatelessKnowledgeSession ksession = GVRulesConfigManager.instance().getStatelessKnowledgeSession("license");
        ExecutionResults results = ksession.execute(CommandFactory.newBatchExecution(cmds));
        assertEquals(new Applicant("Mr John Smith", 18), results.getValue("mrSmith"));
        assertEquals(new Applicant("Mr John Doe", 21), results.getValue("mrDoe"));
    }


    /**
     * Test the figures.
     * 
     * @throws Exception
     */
    public static void testFiguresCmd() throws Exception
    {
        FigureBag fb = new FigureBag();
        fb.add(new Triangle("red"));
        fb.add(new Triangle("green"));
        fb.add(new Square("red"));
        fb.add(new Circle("yellow"));
        fb.add(new Circle("green"));

        List<Command> cmds = new ArrayList<Command>();
        cmds.add(CommandFactory.newSetGlobal("filterColor", "red"));
        cmds.add(CommandFactory.newInsert(fb, "figset"));
        StatelessKnowledgeSession ksession = GVRulesConfigManager.instance().getStatelessKnowledgeSession(
                "figureFilter");
        ExecutionResults results = ksession.execute(CommandFactory.newBatchExecution(cmds));

        FigureBag out = (FigureBag) results.getValue("figset");

        System.out.println(out);
        assertEquals(out, fb);
        assertEquals(2, out.getFigures().size());
    }

    /**
     * Test the figures.
     * 
     * @throws Exception
     */
    public static void testFiguresCmd2() throws Exception
    {
        FigureBag fb = new FigureBag();
        fb.add(new Triangle("red"));
        fb.add(new Triangle("green"));
        fb.add(new Square("red"));
        fb.add(new Circle("yellow"));
        fb.add(new Circle("green"));

        FigureBag out = new FigureBag();

        List<Command> cmds = new ArrayList<Command>();
        cmds.add(CommandFactory.newSetGlobal("filterColor", "red"));
        cmds.add(CommandFactory.newSetGlobal("figsetOut", out));
        cmds.add(CommandFactory.newInsert(fb, "figset"));
        StatelessKnowledgeSession ksession = GVRulesConfigManager.instance().getStatelessKnowledgeSession(
                "figureFilterOut");
        ExecutionResults results = ksession.execute(CommandFactory.newBatchExecution(cmds));

        System.out.println(out);
        assertNotSame(out, fb);
        assertEquals(2, out.getFigures().size());
    }

    /**
     * Test the figures.
     * 
     * @throws Exception
     */
    public static void testFiguresCmd3() throws Exception
    {
        FigureBag fb = new FigureBag();
        fb.add(new Triangle("red"));
        fb.add(new Triangle("green"));
        fb.add(new Square("red"));
        fb.add(new Circle("yellow"));
        fb.add(new Circle("green"));

        FigureBag out = new FigureBag();

        List<Command> cmds = new ArrayList<Command>();
        cmds.add(CommandFactory.newSetGlobal("filterColor", "red"));
        cmds.add(CommandFactory.newSetGlobal("figsetOut", out));
        cmds.add(CommandFactory.newInsertElements(fb.getFigures()));
        StatelessKnowledgeSession ksession = GVRulesConfigManager.instance().getStatelessKnowledgeSession(
                "figureFilterOutColl");
        ExecutionResults results = ksession.execute(CommandFactory.newBatchExecution(cmds));

        System.out.println(out);
        assertNotSame(out, fb);
        assertEquals(2, out.getFigures().size());
    }

}

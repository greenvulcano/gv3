/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
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
package tests.unit.script;

import java.io.File;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.script.ScriptExecutor;
import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Node;

/**
 *
 * @version 3.5.0 06/ago/2014
 * @author GreenVulcano Developer Team
 *
 */
public class ScriptExecutorTestCase extends TestCase
{
    private static String dest = System.getProperty("user.dir") + File.separator
                                + "target"+ File.separator + "scripts";
    private static String src = System.getProperty("user.dir") + File.separator
                                + "target" + File.separator + "test-classes"
                                + File.separator + "scripts";
    private static String[] svc = new String[]{"LIST_PDF", "LIST_PIPPO"};
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("org.jruby.embed.localcontext.scope", "threadsafe");
        FileUtils.copyDirectory(new File(src), new File(dest), true);
    }

    /**
     * @throws Exception
     */
    public final void testSimpleJS() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJS']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleJS[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJS: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    /**
     * @throws Exception
     */
    public final void testSimpleOGNL() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testOGNL']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleOGNL[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleOGNL: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    /**
     * @throws Exception
     */
    public final void testSimpleGroovy() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testGroovy']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleGroovy[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleGroovy: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    /**
     * @throws Exception
     */
    public final void testSimpleJRuby() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJRuby']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleJRuby[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJRuby: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    /**
     * @throws Exception
     */
    public final void testSimpleJS_props() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJS_props']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleJS_props[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJS_props: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    /**
     * @throws Exception
     */
    public final void testSimpleOGNL_props() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testOGNL_props']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleOGNL_props[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleOGNL_props: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    /**
     * @throws Exception
     */
    public final void testSimpleGroovy_props() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testGroovy_props']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleGroovy_props[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleGroovy_props: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    /**
     * @throws Exception
     */
    public final void testSimpleJRuby_props() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJRuby_props']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleJRuby_props[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJRuby_props: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    /**
     * @throws Exception
     */
    public final void testSimpleJS_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJS_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleJS_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJS_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    /**
     * @throws Exception
     */
    public final void testSimpleOGNL_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testOGNL_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleOGNL_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleOGNL_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    /**
     * @throws Exception
     */
    public final void testSimpleGroovy_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testGroovy_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleGroovy_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleGroovy_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    /**
     * @throws Exception
     */
    public final void testSimpleJRuby_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJRuby_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleJRuby_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJRuby_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    /**
     * @throws Exception
     */
    public final void testSimpleJS_inc_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJS_inc_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleJS_inc_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJS_inc_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    /**
     * @throws Exception
     */
    public final void testSimpleOGNL_inc_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testOGNL_inc_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleOGNL_inc_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleOGNL_inc_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    /**
     * @throws Exception
     */
    public final void testSimpleGroovy_inc_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testGroovy_inc_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleGroovy_inc_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleGroovy_inc_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    /**
     * @throws Exception
     */
    public final void testSimpleJRuby_inc_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJRuby_inc_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = new ScriptExecutor();
        se.init(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(gvb);
            se.cleanup();
            System.out.println("testSimpleJRuby_inc_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJRuby_inc_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

}

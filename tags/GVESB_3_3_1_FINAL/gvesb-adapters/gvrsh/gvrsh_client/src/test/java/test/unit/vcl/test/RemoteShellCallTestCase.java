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
package test.unit.vcl.test;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.rsh.server.RSHServer;
import it.greenvulcano.gvesb.virtual.rsh.RemoteShellCallOperation;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 06/10/2011
 * @author GreenVulcano Developer Team
 */
public class RemoteShellCallTestCase extends TestCase
{
    private static final String TEST_FILE_RESOURCES      = System.getProperty("user.dir") + File.separator
                                                                 + "target" + File.separator + "test-classes";
    private static final String TEST_FILE_DIR            = "TestFileManager";
    private static final String TEST_FILE_DEST_RESOURCES = System.getProperty("java.io.tmpdir") + File.separator
                                                                 + TEST_FILE_DIR;
    private static final String TEST_FILE                = "test_shell.txt";
	private static final String TEST_FILE_CONTENT        = "Test file for Virtual Communication Layer Shell plugin test cases.";

	private static String       channel                  = File.separator.equals("/") ? "TEST_CHANNEL" : "TEST_CHANNEL_WIN";
    private static RSHServer    instance                 = null;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        if (instance == null) {
            instance = new RSHServer(3099);
            instance.startUp();
        }
        FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
        FileUtils.forceMkdir(new File(TEST_FILE_DEST_RESOURCES));
        FileUtils.copyFileToDirectory(new File(TEST_FILE_RESOURCES, TEST_FILE), new File(TEST_FILE_DEST_RESOURCES));
        assertTrue("System property 'it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath' not set.",
                System.getProperty("it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath") != null);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        /*if (instance != null) {
            instance.shutDown();
        }*/
        FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
        super.tearDown();
    }

    /**
     * @throws Exception
     */
    public void testLocalSingle() throws Exception
    {
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='" + channel + "']/rsh-call[@name='rsh_loc_single_cmd']");
        RemoteShellCallOperation sh = new RemoteShellCallOperation();
        sh.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTE_SHELL-CALL");
        gvBuffer.setProperty("FILE_NAME", TEST_FILE);
        gvBuffer.setProperty("WORK_DIR", TEST_FILE_DEST_RESOURCES);
        GVBuffer result = sh.perform(gvBuffer);
		System.out.println("Output testLocalSingle [" + result.getObject() + "]");
        assertTrue(((String) result.getObject()).trim().contains(TEST_FILE_CONTENT));
    }

    /**
     * @throws Exception
     */
    public void testLocalMulti() throws Exception
    {
        Node node = XMLConfig.getNode("GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='" + channel + "']/rsh-call[@name='rsh_loc_multi_cmd']");
        RemoteShellCallOperation sh = new RemoteShellCallOperation();
        sh.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTE_SHELL-CALL");
        gvBuffer.setProperty("FILE_NAME", TEST_FILE);
        gvBuffer.setProperty("WORK_DIR", TEST_FILE_DEST_RESOURCES);
        GVBuffer result = sh.perform(gvBuffer);
		System.out.println("Output testLocalMulti [" + result.getObject() + "]");
        assertTrue(((String) result.getObject()).trim().contains(TEST_FILE_CONTENT));
    }

    /**
     * @throws Exception
     */
    public void testRemoteSingle() throws Exception
    {
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='" + channel + "']/rsh-call[@name='rsh_rem_single_cmd']");
        RemoteShellCallOperation sh = new RemoteShellCallOperation();
        sh.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTE_SHELL-CALL");
        gvBuffer.setProperty("FILE_NAME", TEST_FILE);
        gvBuffer.setProperty("WORK_DIR", TEST_FILE_DEST_RESOURCES);
        GVBuffer result = sh.perform(gvBuffer);
		System.out.println("Output testRemoteSingle [" + result.getObject() + "]");
        assertTrue(((String) result.getObject()).trim().contains(TEST_FILE_CONTENT));
    }

    /**
     * @throws Exception
     */
    public void testRemoteMulti() throws Exception
    {
        Node node = XMLConfig.getNode("GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='" + channel + "']/rsh-call[@name='rsh_rem_multi_cmd']");
        RemoteShellCallOperation sh = new RemoteShellCallOperation();
        sh.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTE_SHELL-CALL");
        gvBuffer.setProperty("FILE_NAME", TEST_FILE);
        gvBuffer.setProperty("WORK_DIR", TEST_FILE_DEST_RESOURCES);
        GVBuffer result = sh.perform(gvBuffer);
		System.out.println("Output testRemoteMulti[" + result.getObject() + "]");
        assertTrue(((String) result.getObject()).trim().contains(TEST_FILE_CONTENT));
    }
}

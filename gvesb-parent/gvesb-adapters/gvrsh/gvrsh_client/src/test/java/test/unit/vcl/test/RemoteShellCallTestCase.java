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
import it.greenvulcano.gvesb.virtual.rsh.RemoteShellCallOperation;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 06/10/2011
 * @author GreenVulcano Developer Team
 */
public class RemoteShellCallTestCase extends BaseTest
{

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        FileUtils.copyFileToDirectory(new File(TEST_FILE_RESOURCES, TEST_FILE), new File(TEST_FILE_DEST_RESOURCES));
    }

    @Override
    protected void tearDown() throws Exception
    {
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

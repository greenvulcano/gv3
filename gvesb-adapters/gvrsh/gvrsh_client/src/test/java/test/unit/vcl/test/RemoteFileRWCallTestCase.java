/*
 * Copyright (c) 2009-2015 GreenVulcano ESB Open Source Project. All rights
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
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.rsh.RemoteFileReaderCallOperation;
import it.greenvulcano.gvesb.virtual.rsh.RemoteFileWriterCallOperation;
import it.greenvulcano.util.txt.TextUtils;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Node;

/**
 * @version 3.5.0 15/05/2015
 * @author GreenVulcano Developer Team
 */
public class RemoteFileRWCallTestCase extends BaseTest
{
    private static final String TEST_FILE_R              = "test_shell_read.txt";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        FileUtils.copyFile(new File(TEST_FILE_RESOURCES, TEST_FILE), new File(TEST_FILE_DEST_RESOURCES, TEST_FILE_R));
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * @throws Exception
     */
    public void testLocalWrite() throws Exception
    {
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='" + channel + "']/rsh-filewriter-call[@name='rsh_loc_write']");
        CallOperation sh = new RemoteFileWriterCallOperation();
        sh.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "LOCAL_SHELL-WRITE-CALL");
        gvBuffer.setObject(TEST_FILE_CONTENT);
        gvBuffer.setProperty("FILE_NAME", TEST_FILE);
        gvBuffer.setProperty("WORK_DIR", TEST_FILE_DEST_RESOURCES);
        GVBuffer result = sh.perform(gvBuffer);
        assertTrue("Resource " + TEST_FILE + " not found in " + TEST_FILE_DEST_RESOURCES, new File(TEST_FILE_DEST_RESOURCES, TEST_FILE).exists());
        assertTrue("Resource " + TEST_FILE + " content invalid", TextUtils.readFile(new File(TEST_FILE_DEST_RESOURCES, TEST_FILE)).equals(TEST_FILE_CONTENT));
    }

    /**
     * @throws Exception
     */
    public void testRemoteWrite() throws Exception
    {
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='" + channel + "']/rsh-filewriter-call[@name='rsh_rem_write']");
        CallOperation sh = new RemoteFileWriterCallOperation();
        sh.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTE_SHELL-WRITE-CALL");
        gvBuffer.setObject(TEST_FILE_CONTENT);
        gvBuffer.setProperty("FILE_NAME", TEST_FILE);
        gvBuffer.setProperty("WORK_DIR", TEST_FILE_DEST_RESOURCES);
        GVBuffer result = sh.perform(gvBuffer);
        assertTrue("Resource " + TEST_FILE + " not found in " + TEST_FILE_DEST_RESOURCES, new File(TEST_FILE_DEST_RESOURCES, TEST_FILE).exists());
        assertTrue("Resource " + TEST_FILE + " content invalid", TextUtils.readFile(new File(TEST_FILE_DEST_RESOURCES, TEST_FILE)).equals(TEST_FILE_CONTENT));
    }

    /**
     * @throws Exception
     */
    public void testLocalRead() throws Exception
    {
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='" + channel + "']/rsh-filereader-call[@name='rsh_loc_read']");
        CallOperation sh = new RemoteFileReaderCallOperation();
        sh.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "LOCAL_SHELL-READ-CALL");
        gvBuffer.setProperty("FILE_NAME", TEST_FILE_R);
        gvBuffer.setProperty("WORK_DIR", TEST_FILE_DEST_RESOURCES);
        GVBuffer result = sh.perform(gvBuffer);
        assertTrue("Resource " + TEST_FILE_R + " content invalid", new String((byte[]) result.getObject()).equals(TEST_FILE_CONTENT));
    }

    /**
     * @throws Exception
     */
    public void testRemoteRead() throws Exception
    {
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='" + channel + "']/rsh-filereader-call[@name='rsh_rem_read']");
        CallOperation sh = new RemoteFileReaderCallOperation();
        sh.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "LOCAL_SHELL-READ-CALL");
        gvBuffer.setProperty("FILE_NAME", TEST_FILE_R);
        gvBuffer.setProperty("WORK_DIR", TEST_FILE_DEST_RESOURCES);
        GVBuffer result = sh.perform(gvBuffer);
        assertTrue("Resource " + TEST_FILE_R + " content invalid", new String((byte[]) result.getObject()).equals(TEST_FILE_CONTENT));
    }

}

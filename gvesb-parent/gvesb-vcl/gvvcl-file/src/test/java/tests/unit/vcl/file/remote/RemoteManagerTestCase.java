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
package tests.unit.vcl.file.remote;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.file.remote.RemoteManagerCall;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.mockftpserver.fake.FakeFtpServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Feb 26, 2010
 * @author GreenVulcano Developer Team
 */
public class RemoteManagerTestCase extends TestCase
{
    private static final String TEST_FILE_RESOURCES       = System.getProperty("user.dir") + File.separator
                                                                  + "target" + File.separator + "test-classes";
    private static final String TEST_FILE_DIR             = "TestFileManager";
    private static final String TEST_FILE_DIR_RENAMED     = "TestFileManager_Renamed";
    private static final String TEST_FILE_DEST_RESOURCES  = System.getProperty("java.io.tmpdir") + File.separator
                                                                  + "TestFTP";
    private static final String TEST_FILE_MANAGER         = "fileManager_test.txt";
    private static final String TEST_FILE_MANAGER_XML     = "fileManager_test.xml";
    private static final String TEST_FILE_MANAGER_RENAMED = "fileManager_test_renamed.txt";
    private static final String TEST_FILE_MANAGER_ZIP     = "fileManager_test.zip";

    private FakeFtpServer       fakeFtpServer;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
        FileUtils.forceMkdir(new File(TEST_FILE_DEST_RESOURCES));
        assertTrue("System property 'it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath' not set.",
                System.getProperty("it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath") != null);

        ApplicationContext context = new ClassPathXmlApplicationContext("fakeFTP.xml");
        fakeFtpServer = (FakeFtpServer) context.getBean("FakeFtpServer");
        fakeFtpServer.start();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
        if (fakeFtpServer != null) {
            fakeFtpServer.stop();
        }
        super.tearDown();
    }

    /**
     * @throws Exception
     */
    public void testExistFile() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_check_file_remote']");
        RemoteManagerCall rm = new RemoteManagerCall();
        rm.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        gvBuffer.setProperty("fileMask", ".*\\.txt");
        GVBuffer result = rm.perform(gvBuffer);
        System.out.println("testExistFile: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
        assertEquals(2, Integer.parseInt(result.getProperty("GVRM_FOUND_FILES_NUM")));
        assertEquals("Test1.txt;Test0.txt", result.getProperty("GVRM_FOUND_FILES_LIST"));
    }

    /**
     * @throws Exception
     */
    public void testDownloadFile() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_download_file_remote']");
        RemoteManagerCall rm = new RemoteManagerCall();
        rm.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        gvBuffer.setProperty("fileMask", ".*\\.txt");
        rm.perform(gvBuffer);
        assertTrue("Resource Test0.txt not in " + TEST_FILE_DEST_RESOURCES, new File(TEST_FILE_DEST_RESOURCES
                + File.separator + "Test0.txt").exists());
        assertTrue("Resource Test1.txt not in " + TEST_FILE_DEST_RESOURCES, new File(TEST_FILE_DEST_RESOURCES
                + File.separator + "Test1.txt").exists());
    }

    /**
     * @throws Exception
     */
    public void testDownloadDir() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_download_dir_remote']");
        RemoteManagerCall rm = new RemoteManagerCall();
        rm.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        rm.perform(gvBuffer);
        assertTrue("Resource Test0.txt not in " + TEST_FILE_DEST_RESOURCES + File.separator + "dir0", new File(
                TEST_FILE_DEST_RESOURCES + File.separator + "dir0" + File.separator + "Test1.txt").exists());
    }

    /**
     * @throws Exception
     */
    public void testDownloadFileInGVBuffer() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_download_file_gvbuffer_remote']");
        RemoteManagerCall rm = new RemoteManagerCall();
        rm.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        gvBuffer.setProperty("fileMask", "Test0.txt");
        gvBuffer = rm.perform(gvBuffer);
        System.out.println("testDownloadFileInGVBuffer: " + gvBuffer.getObject());
        assertEquals("Content of resource Test0.txt not in GVBuffer", "1234567890", gvBuffer.getObject());
    }

    /**
     * @throws Exception
     */
    public void testUploadCheckFile() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_upload_file_remote']");
        RemoteManagerCall rm = new RemoteManagerCall();
        rm.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        gvBuffer.setProperty("fileMask", ".*_test\\.txt");
        GVBuffer result = rm.perform(gvBuffer);
        System.out.println("testUploadCheckFile: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
        assertEquals(1, Integer.parseInt(result.getProperty("GVRM_FOUND_FILES_NUM")));
        assertEquals("fileManager_test.txt", result.getProperty("GVRM_FOUND_FILES_LIST"));
    }

    /**
     * @throws Exception
     */
    public void testUploadGVBufferCheckFile() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_upload_file_gvbuffer_remote']");
        RemoteManagerCall rm = new RemoteManagerCall();
        rm.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        gvBuffer.setObject("1234567890");
        gvBuffer.setProperty("fileMask", "TestGVBuffer.txt");
        GVBuffer result = rm.perform(gvBuffer);
        System.out.println("testUploadGVBufferCheckFile: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
        assertEquals(1, Integer.parseInt(result.getProperty("GVRM_FOUND_FILES_NUM")));
        assertEquals("TestGVBuffer.txt", result.getProperty("GVRM_FOUND_FILES_LIST"));
    }

}

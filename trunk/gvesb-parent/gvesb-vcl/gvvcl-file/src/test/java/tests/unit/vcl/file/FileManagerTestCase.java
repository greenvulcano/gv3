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
package tests.unit.vcl.file;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.file.FileManagerCall;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Feb 26, 2010
 * @author GreenVulcano Developer Team
 */
public class FileManagerTestCase extends TestCase
{
    private static final String TEST_FILE_RESOURCES       = System.getProperty("user.dir") + File.separator
                                                            + "target/test-classes";
    private static final String TEST_FILE_DIR             = "TestFileManager";
    private static final String TEST_FILE_DIR_RENAMED     = "TestFileManager_Renamed";
    private static final String TEST_FILE_DEST_RESOURCES  = System.getProperty("java.io.tmpdir") + File.separator
                                                            + TEST_FILE_DIR;
    private static final String TEST_FILE_MANAGER         = "fileManager_test.txt";
    private static final String TEST_FILE_MANAGER_XML     = "fileManager_test.xml";
    private static final String TEST_FILE_MANAGER_RENAMED = "fileManager_test_renamed.txt";
    private static final String TEST_FILE_MANAGER_ZIP     = "fileManager_test.zip";

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
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
        super.tearDown();
    }

    /**
     * @throws Exception
     */
    public void testExistFile() throws Exception
    {
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/filemanager-call[@name='test_check_file']");
        FileManagerCall fm = new FileManagerCall();
        fm.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "FILEMANAGER-CALL");
        gvBuffer.setProperty("fileMask", ".*_test\\.txt");
        GVBuffer result = fm.perform(gvBuffer);
        assertEquals(1, Integer.parseInt(result.getProperty("GVFM_FOUND_FILES_NUM")));
        assertEquals(TEST_FILE_MANAGER, result.getProperty("GVFM_FOUND_FILES_LIST"));
    }

    /**
     * @throws Exception
     */
    public void testCopyFile() throws Exception
    {
        FileManagerCall fm = new FileManagerCall();
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/filemanager-call[@name='test_copy_file']");
        fm.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "FILEMANAGER-CALL");
        gvBuffer.setProperty("filename", TEST_FILE_MANAGER);
        gvBuffer.setProperty("renamedFilename", TEST_FILE_MANAGER_RENAMED);
        fm.perform(gvBuffer);
        assertTrue("Resource " + TEST_FILE_MANAGER_RENAMED + " yet in " + TEST_FILE_DEST_RESOURCES, !new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER_RENAMED).exists());
    }

    /**
     * @throws Exception
     */
    public void testZip() throws Exception
    {
        FileManagerCall fm = new FileManagerCall();
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/filemanager-call[@name='test_zip_file']");
        fm.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "FILEMANAGER-CALL");
        gvBuffer.setProperty("filename", TEST_FILE_MANAGER);
        gvBuffer.setProperty("zippedFilename", TEST_FILE_MANAGER_ZIP);
        fm.perform(gvBuffer);
        assertTrue("Resource " + TEST_FILE_MANAGER + " not found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER).exists());
    }

}

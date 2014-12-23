package it.greenvulcano.util.remotefs.hdfs;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.file.remote.RemoteManagerCall;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Node;

public class HDFSTestCase extends TestCase
{
    private static final String TEST_FILE_RESOURCES       = System.getProperty("user.dir") + File.separator
                                                                  + "target" + File.separator + "test-classes";
    private static final String TEST_FILE_DIR             = "TestFileManager";
    private static final String TEST_FILE_DEST_RESOURCES  = System.getProperty("java.io.tmpdir") + File.separator
                                                                  + "TestHDFS";


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

//  TEST PROVA -- OK
//    /**
//     * @throws Exception
//     */
//    public void testMoveFile() throws Exception
//    {
//        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_move_file_remote']");
//        RemoteManagerCall hdfsOperation = new RemoteManagerCall();
//        hdfsOperation.init(node);
//
//        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
//        //TODO gvBuffer.setProperty("fileMask", ".*_test\\.txt");
//        gvBuffer.setProperty("fileMask", "FileUploadxxxxx.txt");
//        gvBuffer.setProperty("fileMask_NEW", "FileUploadxxxxx_MOVED.txt");
//        GVBuffer result = hdfsOperation.perform(gvBuffer);
//        System.out.println("testMoveFile: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
//
//        //TROVARE
//        assertEquals(1, Integer.parseInt(result.getProperty("GVRM_FOUND_FILES_NUM")));
//        assertEquals("FileUploadxxxxx_MOVED.txt", result.getProperty("GVRM_FOUND_FILES_LIST"));
//    }


    /**
     * Ricerca di uno o piu' files nel path HDFS indicato con "sourcePath" e file specificato dalla property fileMask.
     *
     * @throws Exception
     */
    public void testExistFile() throws Exception
    {
        System.out.println("####testExistFile...");
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_check_file_remote']");
        RemoteManagerCall hdfsOperation = new RemoteManagerCall();
        hdfsOperation.init(node);

        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        gvBuffer.setProperty("HDFS_URL", "hdfs://hadoop-hdfs:54310");
        gvBuffer.setProperty("PASSWORD", "{3DES}PDZ8kUexPv0=");
        gvBuffer.setProperty("USER", "hduser");

        //TEST 1: 1 file con regex.
        gvBuffer.setProperty("fileMask", "*.java");
        GVBuffer result = hdfsOperation.perform(gvBuffer);
        System.out.println("testExistFile: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
        assertEquals(1, Integer.parseInt(result.getProperty("GVRM_FOUND_FILES_NUM")));
        assertEquals("WordCount.java", result.getProperty("GVRM_FOUND_FILES_LIST"));

//        //TEST 2: 2 files con regex.
//        gvBuffer.setProperty("fileMask", "*.txt");
//        result = hdfsOperation.perform(gvBuffer);
//        System.out.println("files Found: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
//        assertEquals(2, Integer.parseInt(result.getProperty("GVRM_FOUND_FILES_NUM")));
//
//        //TEST 3: Nome file piu' regex unix.
//        gvBuffer.setProperty("fileMask", "README.*");
//        result = hdfsOperation.perform(gvBuffer);
//        System.out.println("files Found: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
//        assertEquals("README.txt", result.getProperty("GVRM_FOUND_FILES_LIST"));
//
//        //TEST 4: Nome esatto del file.
//        gvBuffer.setProperty("fileMask", "README.txt");
//        result = hdfsOperation.perform(gvBuffer);
//        System.out.println("files Found: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
//        assertEquals("README.txt", result.getProperty("GVRM_FOUND_FILES_LIST"));
    }

// TODO: Per testare il download del file in locale su windows c'e' bisogno di installare le utilities per windows winutils.exe dai sorgenti hadoop e cygwin.
    /**
     * @throws Exception
     */
    public void testDownloadFile() throws Exception
    {
        System.out.println("####testDownloadFile...");
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_download_file_remote']");
        RemoteManagerCall hdfsOperation = new RemoteManagerCall();
        hdfsOperation.init(node);

        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        //gvBuffer.setProperty("fileMask", ".*\\.txt");
        gvBuffer.setProperty("fileMask", "README.txt");
        hdfsOperation.perform(gvBuffer);

        assertTrue("Resource README.txt in " + TEST_FILE_DEST_RESOURCES, new File(TEST_FILE_DEST_RESOURCES
                + File.separator + "README.txt").exists());
    }

//    /**
//     * @throws Exception
//     */
//    public void testDownloadDir() throws Exception
//    {
//        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_download_dir_remote']");
//        //RemoteManagerCall rm = new RemoteManagerCall();
//        RemoteManagerCall hdfsOperation = new RemoteManagerCall();
//        hdfsOperation.init(node);
//        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
//        hdfsOperation.perform(gvBuffer);
//        assertTrue("Resource Test0.txt not in " + TEST_FILE_DEST_RESOURCES + File.separator + "dir0", new File(
//                TEST_FILE_DEST_RESOURCES + File.separator + "dir0" + File.separator + "Test1.txt").exists());
//    }
//
    /**
     * @throws Exception
     */
    public void testDownloadFileInGVBuffer() throws Exception
    {
        System.out.println("####testDownloadFileInGVBuffer...");
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_download_file_gvbuffer_remote']");
        RemoteManagerCall hdfsOperation = new RemoteManagerCall();
        hdfsOperation.init(node);

        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        gvBuffer.setProperty("fileMask", "README.txt");
        gvBuffer = hdfsOperation.perform(gvBuffer);

        System.out.println("testDownloadFileInGVBufferOutput => OBJECT OUTPUT: "+ gvBuffer.getObject());
        assertNotNull("Content of resource WordCount.java not in GVBuffer", gvBuffer.getObject());
    }

    /**
     * @throws Exception
     */
    public void testUploadCheckFile() throws Exception
    {
        System.out.println("####testUploadCheckFile...");
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_upload_file_remote']");
        RemoteManagerCall hdfsOperation = new RemoteManagerCall();
        hdfsOperation.init(node);

        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        //TODO gvBuffer.setProperty("fileMask", ".*_test\\.txt");
        gvBuffer.setProperty("fileMask", "fileManager_test.txt");
        GVBuffer result = hdfsOperation.perform(gvBuffer);

        System.out.println("testUploadCheckFile: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
        assertEquals(1, Integer.parseInt(result.getProperty("GVRM_FOUND_FILES_NUM")));
        assertEquals("fileManager_test.txt", result.getProperty("GVRM_FOUND_FILES_LIST"));
    }

    /** File creato sulla destinazione utilizzando i dati inseriti nel GVBuffer
     * @throws Exception
     */
    public void testUploadGVBufferCheckFile() throws Exception
    {
        System.out.println("####testUploadGVBufferCheckFile...");
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_upload_file_gvbuffer_remote']");
        RemoteManagerCall hdfsOperation = new RemoteManagerCall();
        hdfsOperation.init(node);

        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        gvBuffer.setObject("1234567890Ã¨+Ã²Ã Ã¹");
        String fileNameToUpload = "TestGVBuffer_HDFSxxxx.txt";
        gvBuffer.setProperty("fileMask", fileNameToUpload);
        GVBuffer result = hdfsOperation.perform(gvBuffer);

        System.out.println("testUploadGVBufferCheckFile: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
        assertEquals(1, Integer.parseInt(result.getProperty("GVRM_FOUND_FILES_NUM")));
        assertEquals(fileNameToUpload, result.getProperty("GVRM_FOUND_FILES_LIST"));
    }


    /**
     * @throws Exception
     */
    public void testMoveCheckFile() throws Exception
    {
        System.out.println("####testMoveCheckFile...");
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_move_check_file_remote']");
        RemoteManagerCall hdfsOperation = new RemoteManagerCall();
        hdfsOperation.init(node);

        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        //TODO gvBuffer.setProperty("fileMask", ".*_test\\.txt");
        gvBuffer.setProperty("fileMask", "fileManager_test.txt");
        gvBuffer.setProperty("fileMask_NEW", "fileManager_test_MOVED.txt");
        GVBuffer result = hdfsOperation.perform(gvBuffer);

        System.out.println("testMoveCheckFile: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
        assertEquals(1, Integer.parseInt(result.getProperty("GVRM_FOUND_FILES_NUM")));
        assertEquals("fileManager_test_MOVED.txt", result.getProperty("GVRM_FOUND_FILES_LIST"));
    }

    /**
     * @throws Exception
     */
    public void testDeleteCheckFileRemote() throws Exception
    {
        System.out.println("####testDeleteCheckFileRemote...");
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_delete_check_file_remote']");
        RemoteManagerCall hdfsOperation = new RemoteManagerCall();
        hdfsOperation.init(node);

        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        //TODO gvBuffer.setProperty("fileMask", ".*_test\\.txt");
        gvBuffer.setProperty("fileMask", "fileManager_test_MOVED.txt");
        GVBuffer result = hdfsOperation.perform(gvBuffer);

        System.out.println("testDeleteCheckFile: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
        assertEquals(0, Integer.parseInt(result.getProperty("GVRM_FOUND_FILES_NUM")));
        assertEquals(null, result.getProperty("GVRM_FOUND_FILES_LIST"));
    }


    /**
     * @throws Exception
     */
    public void testUploadDirectory() throws Exception
    {
        System.out.println("####testUploadDirectory...");
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_upload_directory_remote']");
        RemoteManagerCall hdfsOperation = new RemoteManagerCall();
        hdfsOperation.init(node);

        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
//        gvBuffer.setProperty("fileMask", "");
//        gvBuffer.setProperty("dirMask", "test_delete_folder");
        GVBuffer result = hdfsOperation.perform(gvBuffer);

        System.out.println("testUploadDirectory: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
        assertEquals(1, Integer.parseInt(result.getProperty("GVRM_FOUND_FILES_NUM")));
        //assertEquals("fileManager_test.txt", result.getProperty("GVRM_FOUND_FILES_LIST"));
    }


    /**
     * @throws Exception
     */
    public void testDeleteCheckDirectoryRemote() throws Exception
    {
        System.out.println("####testDeleteCheckDirectoryRemote...");
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_delete_folder_remote']");
        RemoteManagerCall hdfsOperation = new RemoteManagerCall();
        hdfsOperation.init(node);

        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        gvBuffer.setProperty("fileMask", "");
        GVBuffer result = hdfsOperation.perform(gvBuffer);

        System.out.println("testDeleteCheckDirectoryRemote: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
        assertEquals(0, Integer.parseInt(result.getProperty("GVRM_FOUND_FILES_NUM")));
        assertEquals(null, result.getProperty("GVRM_FOUND_FILES_LIST"));
    }

}

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
package it.greenvulcano.util.remotefs.hdfs;

import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.file.RegExFileFilter;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.remotefs.RemoteManager;
import it.greenvulcano.util.remotefs.RemoteManagerException;
import it.greenvulcano.util.remotefs.hdfs.utility.HDFSParamsKey;
import it.greenvulcano.util.remotefs.hdfs.utility.RegexHdfsPathFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * This class provides some utility methods for accessing a HDFS
 * filesystem.
 * It wraps the FileSystem class of hadoop library.
 * The 'autoconnect' functionality manage connection to and from
 * server at every method invocation.
 *
 * @version 3.5.0 Sep 01, 2014
 * @author GreenVulcano Developer Team
 *
 *
 */
public class HDFSRemoteManager extends RemoteManager
{
    private static final Logger logger        = GVLogger.getLogger(HDFSRemoteManager.class);

    private String defaultFileSystemName;

    private List<String> configurationResources; //entries caricate da xml Opzionali
    private Map<String, String> configurationEntries; //entries caricate da xml Opzionali

    /**
     * access informations
     */
    protected String            connectionURL; //hostname:port

    /**
     *
     */
    protected String            username;
    /**
     *
     */
    protected String            password;

    /**
     * connect timeout settings
     */
    protected int               connectTimeout;
    /**
     * data timeout settings
     */
    protected int               dataTimeout;

    /**
     * Enable the autoconnect functionality.
     */
    private boolean             isAutoconnect = false;

    /**
     * A flag indicating if the RemoteManager object is connected on
     * HDFS fileSystem.
     */
    private boolean      isConnected = false;


    /**
     * A private instance of <code>FileSystem</code> class to perform hadoop client
     * operations.
     */
    //private RemoteManager         manager  = null;
    private FileSystem fileSystem = null;

    /**
     * Loads data from XML configuration section.
     *
     * @param configNode
     * @throws RemoteManagerException
     */
    public void init(Node configNode) throws RemoteManagerException
    {
        try {
            connectionURL = XMLConfig.get(configNode, "@connectionURL");
            username = XMLConfig.get(configNode, "@username");
            password = XMLConfig.getDecrypted(configNode, "@password");
            isAutoconnect = XMLConfig.getBoolean(configNode, "@autoConnect", false);

            connectTimeout = XMLConfig.getInteger(configNode, "@connectTimeout", 0);
            dataTimeout = XMLConfig.getInteger(configNode, "@dataTimeout", 0);

            logger.debug("connectionURL          : " + connectionURL);
            logger.debug("username          : " + username);
            logger.debug("password          : ******");
            logger.debug("connectTimeout    : " + connectTimeout);
            logger.debug("dataTimeout       : " + dataTimeout);
        }
        catch (Exception exc)
        {
            throw new RemoteManagerException("Initialization error", exc);
        }
    }


    /**
     * @return the hadoop FileSystem
     */
    public FileSystem getFileSystem() {
        return fileSystem;
    }


    /**
     * @param fileSystem - File System hadoop
     */
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }


    /**
     * @return the connectionURL
     */
    public String getConnectionURL() {
        return connectionURL;
    }


    /**
     *
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     *
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param isAutoconnect
     *        the isAutoconnect to set
     */
    public void setAutoconnect(boolean isAutoconnect)
    {
        this.isAutoconnect = isAutoconnect;
    }

    /**
     * @return the isAutoconnect
     */
    public boolean isAutoconnect()
    {
        return this.isAutoconnect;
    }

    /**
     * Returns a <code>String</code> representation of this HDFSRemoteManager object
     * in the format:<br>
     *
     * <pre>
     * user/password@connectionURL
     * </pre>
     */
    @Override
    public String toString()
    {
        return username + "/******@" + connectionURL;
    }

    /**
     * Connect to the HDFS server and performs login. Does nothing if
     * already connected and logged in.
     *
     * @param optProperties
     * @throws RemoteManagerException
     */
    public void connect(Map<String, String> optProperties) throws RemoteManagerException
    {
        //Senza settare defaultFileSystem nell'xml il client si connettera' con le creadenziali
        //della macchina. Esempio: Freedomind...
        //

        //Connessione al fileSystem hdfs con parametri da configurazione GVCore.xml o file configurazione hadoop xml.
        final Configuration configuration = new Configuration();
        if (isNotBlank(defaultFileSystemName)) {
            configuration.set(FileSystem.FS_DEFAULT_NAME_KEY, defaultFileSystemName);
        }

        final boolean hasConfigurationResources = CollectionUtils.isNotEmpty(configurationResources);
        if (hasConfigurationResources) {
            for (final String configurationResource : configurationResources) {
                configuration.addResource(new Path(configurationResource));
            }
        }

        if (isNotEmpty(configurationEntries)) {
            for (final Entry<String, String> configurationEntry : configurationEntries.entrySet()) {
                configuration.set(configurationEntry.getKey(), configurationEntry.getValue());
            }
        }

        String localConnectionURL = connectionURL;
        try
        {
            Map<String, Object> localProps = MapUtils.convertToHMStringObject(optProperties);
            String localUsername = PropertiesHandler.expand(username, localProps);
            //String localPassword = XMLConfig.getDecrypted(PropertiesHandler.expand(password, localProps));
            localConnectionURL = PropertiesHandler.expand(connectionURL, localProps);

            fileSystem = FileSystem.get(URI.create(localConnectionURL), configuration, localUsername);
            isConnected = true;
        }
        catch (Exception exc)
        {
            logger.error(exc.getMessage(), exc);
            throw new RemoteManagerException(exc.getMessage());
        }
//        catch (InterruptedException e)
//        {
//            logger.error(e.getMessage(), e);
//            throw new RemoteManagerException(e.getMessage());
//        }

        logger.info("Connected to: " + getFileSystemUri());

    }

    public String getFileSystemUri() {
        return fileSystem == null ? null : fileSystem.getUri().toString();
    }

    /**
     * Disconnect from the HDFS server after logging out. Does nothing if
     * already disconnected.
     *
     * @param optProperties
     */
    public void disconnect(Map<String, String> optProperties)
    {
        if (fileSystem != null) {
            try {
                fileSystem.close();
            } catch (IOException exc) {
                logger.warn("I/O error while logging out", exc);
            } catch (Exception exc) {
                logger.warn("Generic error while logging out", exc);
            } finally {
                fileSystem = null;
            }
        }
    }


    /**
     * @throws RemoteManagerException
     *         if not connected
     */
    protected void checkConnected() throws RemoteManagerException
    {
        if (isAutoconnect()) {
            connect();
        }
        if (!isConnected) {
            throw new RemoteManagerException("NOT connected to HDFS file system.");
        }
    }

    /**
     * Return a key identifying the Monitor instance.
     *
     * @return
     */
    public String getManagerKey()
    {
        return username + "@" + hostname + ":" + port;
    }

    /**
     * Return a key identifying the Monitor instance.
     *
     * @return
     */
    public String getManagerKey(Map<String, String> optProperties)  throws RemoteManagerException
    {
        try {
            Map<String, Object> localProps = MapUtils.convertToHMStringObject(optProperties);
            return PropertiesHandler.expand(username, localProps) + "@"
            + PropertiesHandler.expand(connectionURL, localProps);
        }
        catch (Exception exc) {
            throw new RemoteManagerException(exc);
        }
    }

    @Override
    public Set<FileProperties> ls(String remoteDirectory, String fileNamePattern, Date modifiedSince, int fileTypeFilter, Map<String, String> optProperties) throws RemoteManagerException {
        Set<FileProperties> resultsSet = new HashSet<FileProperties>();
        //SEARCH COMMAND
        try
        {
            final Path hdfsPathSrc = new Path(remoteDirectory);

            //TODO: Effettuare MORE TESTS.
            fileNamePattern = ".*/".concat(fileNamePattern);
            RegexHdfsPathFilter regexFilter = new RegexHdfsPathFilter(fileSystem, fileNamePattern, fileTypeFilter,  (modifiedSince != null) ? modifiedSince.getTime() : -1);

            FileStatus[] fileStatusList = new FileStatus[0];
            try
            {
                fileStatusList = fileSystem.listStatus(hdfsPathSrc, regexFilter);
            }
            catch(Exception ex)
            {
                logger.error("Error searching files in the HDFS path: "+remoteDirectory, ex);
            }

            logger.debug(fileStatusList.length + " file entries DETECTED into current remote working directory");
            for (FileStatus fileStatusItem : fileStatusList) {
                if (fileStatusItem != null) {
                    FsPermission fsPermission = fileStatusItem.getPermission();
                    FsAction userAction = fsPermission.getUserAction();

                    logger.debug("fsPermission.getUserAction(): "+fsPermission.getUserAction());

                    FileProperties currFile = new FileProperties(fileStatusItem.getPath().getName(),
                            fileStatusItem.getModificationTime(), fileStatusItem.getLen(),
                            fileStatusItem.isDirectory(), hasReadPermission(userAction), hasWritePermission(userAction), hasExecutePermission(userAction));
                    resultsSet.add(currFile);
                }
                else {
                    logger.debug("Remote HDFS file entry NULL");
                }
            }

            return resultsSet;
        }
        catch (Exception exc) {
            throw new RemoteManagerException("HDFS directory scan error", exc);
        }
        finally {
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    @Override
    public boolean get(String remoteDirectory, String remoteFile,
            OutputStream outputStream, Map<String, String> optProperties) throws RemoteManagerException {
        //In questo caso non è possibile copiare il contenuto del file nel GVBuffer ???

        checkConnected();
        //READ FROM PROPERTIES propertyDef
        int bufferSize = 4096;
        if( optProperties != null && optProperties.get(HDFSParamsKey.bufferSize) != null) {
            bufferSize = Integer.parseInt(optProperties.get(HDFSParamsKey.bufferSize));
        }

        boolean result = false;
        try {
            logger.debug("HDFS Downloading remote file "
                    + remoteFile
                    + " from "
                    + (remoteDirectory != null
                    ? " remote directory " + remoteDirectory
                            : " current remote working directory") + "...");

            //Copy file in the OutputStream (?GVBuffer).
            InputStream in = null;
            try
            {
                logger.debug("REMOTE HDFS PATH URL: >"+remoteDirectory+remoteFile+"<");

                final Path hdfsPathSrc = new Path(remoteDirectory+remoteFile);
                in = fileSystem.open(hdfsPathSrc);
                IOUtils.copyBytes(in, outputStream, bufferSize, false);

                logger.debug("File " + remoteFile + " successfully downloaded from remote directory " + remoteDirectory + " into GVBuffer.");
                logger.debug("Remote file " + remoteFile + " saved to OutputStream");

                result = true;
            }
            finally
            {
                IOUtils.closeStream(in);
            }
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            if (isAutoconnect()) {
                disconnect();
            }
        }

        return result;
    }

    @Override
    public boolean get(String remoteDirectory, String remoteFilePattern,
            String localDirectory, Map<String, String> optProperties) throws RemoteManagerException {
        // TODO Auto-generated method stub
        // copy To Local file system

        checkConnected();
        boolean oldAutoConnect = isAutoconnect();
        setAutoconnect(false);


        boolean result = false;
        try {
            //changeWorkingDirectory(remoteDirectory);
            //String currentlyDownloading = ftpClient.printWorkingDirectory();

            //create local directory
            File localDirectoryObj = new File(localDirectory);
            if (!localDirectoryObj.exists()) {
                if (!localDirectoryObj.mkdir()) {
                    throw new RemoteManagerException("Cannot create local directory "
                            + localDirectoryObj.getAbsolutePath());
                }
                logger.debug("Local directory " + localDirectoryObj.getAbsolutePath() + " created");
            }

            Set<FileProperties> files = this.ls(remoteDirectory, remoteFilePattern, null, RegexHdfsPathFilter.ALL, optProperties);
            logger.debug("N. FILES FOUND: "+files.size());

            String currentRemoteDirectory = remoteDirectory;
            System.out.println("N. FILES FOUND: "+files.size());
            for (FileProperties currHDFSFile : files) {
                if (currHDFSFile != null) {
                    boolean partialResult = true;
                    if (currHDFSFile.isDirectory()) {
                        partialResult = getDir(currHDFSFile.getName(), localDirectoryObj.getAbsolutePath(), null, optProperties);
                        String esito = (partialResult == true) ? "" : "NOT";
                        logger.warn("Directory "+currHDFSFile.getName()+" "+esito+" correctly downloaded in the local path: "+ localDirectoryObj.getAbsolutePath());

                        currentRemoteDirectory = currHDFSFile.getName();
                    }
                    else {
                        System.out.println("CALL GET...");
                        partialResult = get(currentRemoteDirectory, currHDFSFile.getName(), localDirectoryObj.getAbsolutePath(), null, optProperties);
                        //partialResult = get(null, currHDFSFile.getName(), localDirectoryObj.getAbsolutePath(), null, optProperties);
                        String esito = (partialResult == true) ? "" : "NOT";
                        logger.warn("File "+currHDFSFile.getName()+" "+esito+" correctly downloaded in the local path: "+ localDirectoryObj.getAbsolutePath());
                    }

                    result = result && partialResult;

                    if (!partialResult) {
                        break;
                    }
                }
                else {
                    logger.debug("Remote HDFS file entry NULL");
                }
            }
        }
        catch (RemoteManagerException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            setAutoconnect(oldAutoConnect);
            if (isAutoconnect()) {
                disconnect();
            }
        }

        return result; //return true IF ALL files and directory are downloaded correctly.
    }

    @Override
    public boolean get(String remoteDirectory, String remoteFile,
            String localDirectory, String localFile, Map<String, String> optProperties)
                    throws RemoteManagerException {
        checkConnected();

        boolean result = false;
        OutputStream output = null;
        try {
            logger.debug("Downloading remote file "
                    + remoteFile
                    + " from "
                    + (remoteDirectory != null
                    ? " remote directory " + remoteDirectory
                            : " current remote working directory") + "...");
            File localPathname = new File(localDirectory, (localFile != null ? localFile : remoteFile));
            if (!localPathname.isAbsolute()) {
                throw new RemoteManagerException("Local pathname (" + localPathname + ") is NOT absolute.");
            }

            System.out.println("localPathName: "+localPathname.getAbsolutePath());
            if(!localPathname.exists()){
                System.out.println("CREATING the newFile: "+localPathname.getAbsolutePath());
                localPathname.createNewFile();
            }

            System.out.println("Saving to " + localPathname);
            output = new FileOutputStream(localPathname);
            get(remoteDirectory, remoteFile, output, optProperties);

            /*          final Path hdfsPathSrc = new Path(remoteDirectory+remoteFile);
            //Path hdfsPathSrc = fileSystem.resolvePath(new Path(remoteDirectory+remoteFile));

            //                         //In Windows OS: installare winutils.exe partendo dai sorgenti di hadoop e scaricando Cygwin.
            //                         //copyToLocalFileSystem
            System.out.println("CALL fileSystem.copyToLocalFile with: "+ "hdfsPathSrc: >"+hdfsPathSrc+"< localDirectory: >"+localDirectory+"< useRawLocalFileSystem: >"+true+"<");
            //fileSystem.copyToLocalFile(false, new Path(remoteDirectory+remoteFile), new Path(localDirectory));

            fileSystem.copyToLocalFile(deleteSource, hdfsPathSrc, new Path(localDirectory), false);
             */
            System.out.println("File " + localDirectory + " successfully downloaded from remote directory " + remoteDirectory + " into local directory.");
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            if (output != null) {
                try {
                    output.close();
                }
                catch (IOException exc) {
                    logger.warn("Error while closing local file output stream");
                }
            }
            if (isAutoconnect()) {
                disconnect();
            }
        }

        return result;
    }

    @Override
    public boolean getDir(String remoteDirectory, String localParentDirectory,
            String localDirectory, Map<String, String> optProperties) throws RemoteManagerException {
        // TODO Auto-generated method stub

        boolean result = false;
        try {
            logger.debug("HDFS Downloading remote directory "
                    + remoteDirectory);

            final Path hdfsPathSrc = new Path(remoteDirectory);

            //                         //In Windows OS: installare winutils.exe partendo dai sorgenti di hadoop e scaricando Cygwin.
            //                         //copyToLocalFileSystem
            logger.debug("CALL fileSystem.copyToLocalFile with: "+ "hdfsPathSrc: >"+hdfsPathSrc+"< localParentDirectory: >"+localParentDirectory+"< useRawLocalFileSystem: >"+true+"<");
            fileSystem.copyToLocalFile(false, new Path(remoteDirectory), new Path(localParentDirectory+localDirectory));
            logger.debug("Directory " + localParentDirectory+localDirectory + " successfully downloaded from remote directory " + remoteDirectory + " into local directory.");

            result = true;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            if (isAutoconnect()) {
                disconnect();
            }
        }

        return result;
    }

    @Override
    public boolean put(InputStream inputDataStream, String remoteDirectory,
            String remoteFile, Map<String, String> optProperties) throws RemoteManagerException {

        checkConnected();

        boolean     overwrite         = false;
        String         permission         = "700";
        int         bufferSize        = 4096;
        int         replication        = 1;
        long         blockSize        = 4194304; //4096;
        String         ownerUserName    = this.username;
        String         ownerGroupName    = null;

        //READ FROM PROPERTIES propertyDef
        if( optProperties != null ) {
            if( optProperties.get(HDFSParamsKey.overwrite) != null) {
                overwrite = Boolean.parseBoolean(optProperties.get(HDFSParamsKey.overwrite));
            }

            logger.debug("OVERWRTIE: "+overwrite);

            if( optProperties.get(HDFSParamsKey.permission) != null) {
                permission = optProperties.get(HDFSParamsKey.permission);
            }

            if( optProperties.get(HDFSParamsKey.bufferSize) != null) {
                bufferSize = Integer.parseInt(optProperties.get(HDFSParamsKey.bufferSize));
            }

            if( optProperties.get(HDFSParamsKey.replication) != null) {
                replication = Integer.parseInt(optProperties.get(HDFSParamsKey.replication));
            }

            if( optProperties.get(HDFSParamsKey.blockSize) != null) {
                blockSize = Long.parseLong(optProperties.get(HDFSParamsKey.blockSize));
            }

            if( optProperties.get(HDFSParamsKey.ownerUserName) != null) {
                ownerUserName = optProperties.get(HDFSParamsKey.ownerUserName);
            }

            if( optProperties.get(HDFSParamsKey.ownerGroupName) != null) {
                ownerGroupName = optProperties.get(HDFSParamsKey.ownerGroupName);
            }

        }

        boolean result = false;
        try {
            logger.debug("Uploading stream "
                    + (remoteDirectory != null
                    ? " to remote directory " + remoteDirectory
                            : " to current remote working directory") + "...");

            //COPY SOURCE FILE FROM GV BUFFER - NOME FILE INDICATO DA "currSourceFile".
            //            OGNLExpressionEvaluator ognl = new OGNLExpressionEvaluator();
            //            ognl.addToContext("gvbuffer", gvBuffer);
            //            Object obj = ognl.getValue(fromGVBufferExpression, gvBuffer);

            //InputStream fileInputSrc = new ByteArrayInputStream((byte[]) obj);
            final Path hdfsPathDest  = new Path(remoteDirectory+remoteFile);
            try
            {
                final FSDataOutputStream fsDataOutputStream = fileSystem.create(hdfsPathDest, getFileSystemPermission(permission), overwrite, bufferSize, (short) replication, blockSize, null);
                IOUtils.copyBytes(inputDataStream, fsDataOutputStream, bufferSize, true);
                //fsDataOutputStream.hsync();

                logger.debug("Stream uploaded in HDFS path: "+hdfsPathDest.getName());

                result = true;
            }
            finally
            {
                if(inputDataStream != null)
                {
                    IOUtils.closeStream(inputDataStream);
                }
            }

            if ((isNotBlank(ownerUserName)) || (isNotBlank(ownerGroupName))) {
                fileSystem.setOwner(hdfsPathDest, ownerUserName, ownerGroupName);
            }
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            if (isAutoconnect()) {
                disconnect();
            }
        }

        return result;
    }

    @Override
    public boolean put(String localDirectory, String localFile,    String remoteDirectory, String remoteFile
            , Map<String, String> optProperties) throws RemoteManagerException {
        checkConnected();

        boolean          deleteSource    = false;
        boolean     overwrite         = false;
        int         replication        = 1;
        String         ownerUserName    = "hduser";
        String         ownerGroupName    = "hadoop";

        //READ FROM PROPERTIES propertyDef
        if( optProperties != null ) {
            if( optProperties.get(HDFSParamsKey.overwrite) != null) {
                overwrite = Boolean.parseBoolean(optProperties.get(HDFSParamsKey.overwrite));
            }

            logger.debug("***overwrite put: "+overwrite);

            if( optProperties.get(HDFSParamsKey.replication) != null) {
                replication = Integer.parseInt(optProperties.get(HDFSParamsKey.replication));
            }

            if( optProperties.get(HDFSParamsKey.ownerUserName) != null) {
                ownerUserName = optProperties.get(HDFSParamsKey.ownerUserName);
            }

            if( optProperties.get(HDFSParamsKey.ownerGroupName) != null) {
                ownerGroupName = optProperties.get(HDFSParamsKey.ownerGroupName);
            }
        }

        boolean result = false;
        FileInputStream input = null;
        try {
            File localPathname = new File(localDirectory, localFile);
            if (!localPathname.isAbsolute()) {
                throw new RemoteManagerException("Local pathname (" + localPathname + ") is NOT absolute.");
            }

            logger.debug("Uploading local file "
                    + localPathname
                    + (remoteDirectory != null
                    ? " to remote directory " + remoteDirectory
                            : " to current remote working directory") + "...");

            if (remoteFile != null) {
                logger.debug("Renaming remote file to " + remoteFile);
            }
            else
            {
                remoteFile = localFile;
            }


            //COPY SOURCE FILE FROM SOURCE PATH
            logger.debug("srcPath: "+localPathname.getAbsolutePath());
            final Path srcPath  = new Path(localPathname.getAbsolutePath());
            String remotePath = remoteDirectory+(remoteFile != null ? remoteFile : localFile);
            logger.debug("remotePath: "+remotePath);
            final Path hdfsDestPath  = new Path(remotePath);

            fileSystem.copyFromLocalFile(deleteSource, overwrite, srcPath, hdfsDestPath);

            if ((isNotBlank(ownerUserName)) || (isNotBlank(ownerGroupName))) {
                fileSystem.setOwner(hdfsDestPath, ownerUserName, ownerGroupName);
            }

            fileSystem.setReplication(hdfsDestPath, (short) replication);

            logger.debug("File " + localFile + " successfully uploaded from local directory "
                    + srcPath + " to remote directory " + remoteDirectory);

            logger.debug("srcPath: " + srcPath + " - remotePath: "+remotePath);

            result = true;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException exc) {
                    logger.warn("Error while closing local file input stream", exc);
                }
            }
            if (isAutoconnect()) {
                disconnect();
            }
        }

        return result;
    }

    @Override
    public boolean put(String localDirectory, String localFilePattern, String remoteDirectory
            , Map<String, String> optProperties) throws RemoteManagerException {

        logger.debug("localDirectory: "+localDirectory);
        logger.debug("localFilePattern: "+localFilePattern);
        logger.debug("remoteDirectory: "+remoteDirectory);

        boolean          deleteSource    = false;
        boolean     overwrite         = false;
        String         permission         = "700";
        int         bufferSize        = 4098;
        int         replication        = 1;
        long         blockSize        = 4098;
        String         ownerUserName    = "hduser";
        String         ownerGroupName    = "hadoop";

        //READ FROM PROPERTIES propertyDef
        if( optProperties != null ) {
            if( optProperties.get(HDFSParamsKey.overwrite) != null) {
                overwrite = Boolean.parseBoolean(optProperties.get(HDFSParamsKey.overwrite));
            }

            logger.debug("***overwrite put: "+overwrite);

            if( optProperties.get(HDFSParamsKey.replication) != null) {
                replication = Integer.parseInt(optProperties.get(HDFSParamsKey.replication));
            }

            if( optProperties.get(HDFSParamsKey.ownerUserName) != null) {
                ownerUserName = optProperties.get(HDFSParamsKey.ownerUserName);
            }

            if( optProperties.get(HDFSParamsKey.ownerGroupName) != null) {
                ownerGroupName = optProperties.get(HDFSParamsKey.ownerGroupName);
            }
        }

        checkConnected();

        boolean oldAutoConnect = isAutoconnect();
        setAutoconnect(false);

        boolean result = true;
        try {
            File localDirectoryObj = new File(localDirectory);

            //changeWorkingDirectory(remoteDirectory);
            fileSystem.setWorkingDirectory(new Path(remoteDirectory));
            logger.debug("localFilePattern: "+localFilePattern.toString());
            File[] localFiles = localDirectoryObj.listFiles(new RegExFileFilter(localFilePattern, RegExFileFilter.ALL));
            logger.debug("localFiles FOUND: "+localFiles);
            for (File currLocalFile : localFiles) {
                logger.debug("currLocalFile: "+currLocalFile);
            }

            for (File currLocalFile : localFiles) {
                boolean partialResult = true;
                if (currLocalFile.isDirectory()) {
                    String currentTargetDirectory = fileSystem.getWorkingDirectory().getName()+"/"+currLocalFile.getName();
                    partialResult = putDir(currLocalFile.getAbsolutePath(), "", currentTargetDirectory, optProperties);
                }
                else {
                    partialResult = put(localDirectoryObj.getAbsolutePath(), currLocalFile.getName(), remoteDirectory, null,
                            optProperties);
                }

                result = result && partialResult;
                if (!partialResult) {
                    break;
                }
            }

            if (result) {
                logger.debug("Local directory " + localDirectory + " uploaded");
            }
            else {
                logger.warn("Could not upload local directory " + localDirectory);
            }

            return result;
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            setAutoconnect(oldAutoConnect);
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }


    @Override
    public boolean putDir(String localDirectory, String remoteParentDirectory,
            String remoteDirectory, Map<String, String> optProperties) throws RemoteManagerException {
        boolean          deleteSource    = false;
        boolean     overwrite         = false;
        int         replication        = 1;
        String         ownerUserName    = "hduser";
        String         ownerGroupName    = "hadoop";

        //READ FROM PROPERTIES propertyDef
        if( optProperties != null ) {
            if( optProperties.get(HDFSParamsKey.overwrite) != null) {
                overwrite = Boolean.parseBoolean(optProperties.get(HDFSParamsKey.overwrite));
            }

            if( optProperties.get(HDFSParamsKey.replication) != null) {
                replication = Integer.parseInt(optProperties.get(HDFSParamsKey.replication));
            }

            if( optProperties.get(HDFSParamsKey.ownerUserName) != null) {
                ownerUserName = optProperties.get(HDFSParamsKey.ownerUserName);
            }

            if( optProperties.get(HDFSParamsKey.ownerGroupName) != null) {
                ownerGroupName = optProperties.get(HDFSParamsKey.ownerGroupName);
            }
        }

        boolean oldAutoConnect = isAutoconnect();
        setAutoconnect(false);

        boolean result = false;
        try {
            File localDirectoryObj = new File(localDirectory);
            if (!localDirectoryObj.isAbsolute()) {
                throw new RemoteManagerException("Local pathname (" + localDirectoryObj + ") is NOT absolute.");
            }
            logger.debug("localDirectory To Upload: "+localDirectoryObj.getAbsolutePath());

            //File localDirectoryObj = new File(localDirectory);

            //changeWorkingDirectory(remoteDirectory);

            //            if (remoteParentDirectory != null) {
            //                fileSystem.setWorkingDirectory(new Path(remoteParentDirectory));
            //            }
            //
            //            if (remoteDirectory == null) {
            //                remoteDirectory = fileSystem.getWorkingDirectory()+localDirectoryObj.getName();
            //            }


            //COPY SOURCE FILE FROM SOURCE PATH
            logger.debug("srcPath: "+localDirectory);
            final Path srcPath  = new Path(localDirectory);
            String remotePath = remoteParentDirectory+remoteDirectory;
            final Path hdfsDestPath  = new Path(remotePath);

            fileSystem.copyFromLocalFile(deleteSource, overwrite, srcPath, hdfsDestPath);

            if ((isNotBlank(ownerUserName)) || (isNotBlank(ownerGroupName))) {
                fileSystem.setOwner(hdfsDestPath, ownerUserName, ownerGroupName);
            }

            fileSystem.setReplication(hdfsDestPath, (short) replication);

            logger.debug("Directory " + localDirectory + " successfully uploaded from local directory "
                    + localDirectory + " to remote directory " + remotePath);

            logger.debug("srcPath: " + localDirectory + "remotePath: "+remotePath);

            result = true;
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            setAutoconnect(oldAutoConnect);
            if (isAutoconnect()) {
                disconnect();
            }
        }

        return result;
    }

    @Override
    public boolean rm(String remoteDirectory, String entryNamePattern, Map<String, String> optProperties)
            throws RemoteManagerException {

        checkConnected();

        boolean oldAutoConnect = isAutoconnect();
        setAutoconnect(false);

        boolean resultAll = false;
        try {

            logger.debug("Path to delete on HDFS-> remoteDirectory:" + remoteDirectory+"-entryNamePattern: "+entryNamePattern +"<");

            Path hdfsPath = new Path(remoteDirectory);

            boolean isDirectory = fileSystem.isDirectory(hdfsPath);
            if(entryNamePattern != null && !"".equals(entryNamePattern))
            {
                Set<FileProperties> fileList = this.ls( remoteDirectory, entryNamePattern, null, RegExFileFilter.FILES_ONLY, optProperties);

                for(FileProperties file : fileList)
                {
                    boolean result = false;
                    hdfsPath = new Path(remoteDirectory+file.getName());
                    logger.debug("File To Delete: "+hdfsPath.toString());
                    result = fileSystem.delete(hdfsPath, false);

                    if(result) logger.debug("File Deleted: "+hdfsPath.toString()+" - successfully");
                    else logger.debug("File NOT Deleted: "+hdfsPath.toString()+" - successfully");

                    resultAll = (result && resultAll);
                }
            }
            else if (isDirectory)
            {
                logger.debug("Directory To Delete: "+hdfsPath.toString());
                resultAll = fileSystem.delete(hdfsPath, true);

                if(resultAll) logger.debug("Directory Deleted: "+hdfsPath.toString()+" - successfully");
                else logger.debug("Directory NOT Deleted: "+hdfsPath.toString()+" - successfully");
            }

            return resultAll;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            setAutoconnect(oldAutoConnect);
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    @Override
    public boolean mv(String remoteDirectory, String oldEntryName,
            String newEntryName, Map<String, String> optProperties) throws RemoteManagerException {

        boolean overwrite         = false;

        //READ FROM PROPERTIES propertyDef
        if( optProperties != null ) {
            if( optProperties.get(HDFSParamsKey.overwrite) != null) {
                overwrite = Boolean.parseBoolean(optProperties.get(HDFSParamsKey.overwrite));
            }
        }

        checkConnected();

        boolean result = false;

        final Path hdfsPathSrc  = new Path(remoteDirectory+oldEntryName);
        final Path hdfsPathDest = new Path(remoteDirectory+newEntryName);

        try {
            //FORCE MV IF FILE OR DIRECTORY NEW EXIST.
            if(overwrite){
                if(fileSystem.isDirectory(hdfsPathDest)){
                    fileSystem.delete(hdfsPathDest, true);
                }else{
                    fileSystem.delete(hdfsPathDest, false);
                }
            }

            result = fileSystem.rename(hdfsPathSrc, hdfsPathDest);

            if (result) {
                logger.debug("File '" + oldEntryName + "' successfully moved to  '" + newEntryName
                        + "'  in remote directory " + remoteDirectory);
            }
            else
            {
                logger.debug("File '" + oldEntryName + "' NOT successfully moved to  '" + newEntryName
                        + "'  in remote directory " + remoteDirectory);
            }
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            if (isAutoconnect()) {
                disconnect();
            }
        }

        return result;
    }

    @Override
    public boolean mkdir(String remoteParentDirectory, String remoteDirectory, Map<String, String> optProperties)
            throws RemoteManagerException {
        checkConnected();

        String         permission         = "700";

        //READ FROM PROPERTIES propertyDef
        if( optProperties != null ) {
            if( optProperties.get(HDFSParamsKey.permission) != null) {
                permission = optProperties.get(HDFSParamsKey.permission);
            }
        }
        boolean result = false;
        try {
            Path path = new Path(remoteParentDirectory+remoteDirectory);
            result = fileSystem.mkdirs(path, getFileSystemPermission(permission));

            if(result){
                logger.debug("Remote directory " + remoteDirectory + " created into " + remoteParentDirectory);
            }
            else{
                logger.warn("Could not create remote directory " + remoteDirectory + " into " + remoteParentDirectory);
            }

            return result;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    private FsPermission getFileSystemPermission(final String permission) {
        return isBlank(permission) ? FsPermission.getDefault() : new FsPermission(permission);
    }

    public static boolean hasReadPermission(FsAction userAction) {
        boolean hasPermission = ((FsAction.ALL == userAction)
                || (FsAction.READ == userAction)
                || (FsAction.READ_EXECUTE == userAction)
                || (FsAction.READ_WRITE == userAction));

        return hasPermission;
    }

    public static boolean hasWritePermission(FsAction userAction) {
        boolean hasPermission = ((FsAction.ALL == userAction)
                || (FsAction.WRITE == userAction)
                || (FsAction.WRITE_EXECUTE == userAction));

        return hasPermission;
    }

    public static boolean hasExecutePermission(FsAction userAction) {
        boolean hasPermission = ((FsAction.ALL == userAction)
                || (FsAction.EXECUTE == userAction)
                || (FsAction.READ_EXECUTE == userAction)
                || (FsAction.READ_WRITE == userAction));

        return hasPermission;
    }
}

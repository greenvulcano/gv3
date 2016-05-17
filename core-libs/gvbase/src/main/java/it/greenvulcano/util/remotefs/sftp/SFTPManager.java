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
package it.greenvulcano.util.remotefs.sftp;

import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.file.RegExFileFilter;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.remotefs.RemoteManagerException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

/**
 * @version 3.0.0 Apr 24, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 * 
 */
public class SFTPManager extends SSHManager
{
    private static final Logger logger = GVLogger.getLogger(SFTPManager.class);

    private ChannelSftp         sftpClient;

    /**
     *
     */
    public SFTPManager()
    {
        // do nothing
    }


    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#connect(Map<String,
     *      String>)
     */
    @Override
    public void connect(Map<String, String> optProperties) throws RemoteManagerException
    {
        if (!isConnected()) {
            super.connect(optProperties);
            try {
                sftpClient = (ChannelSftp) getSSHClient().openChannel("sftp");
                sftpClient.connect();
            }
            catch (JSchException exc) {
                throw new RemoteManagerException("Cannot instantiate the SFTP client", exc);
            }
        }
    }

    @Override
    public void disconnect(Map<String, String> optProperties)
    {
        if (isConnected()) {
            if ((sftpClient != null) && sftpClient.isConnected()) {
                sftpClient.disconnect();
            }
            super.disconnect(optProperties);
        }
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#mkdir(java.lang.String,
     *      java.lang.String, java.util.Map)
     */
    @Override
    public boolean mkdir(String remoteParentDirectory, String remoteDirectory, Map<String, String> optProperties) 
            throws RemoteManagerException
    {
        checkConnected();

        try {
            if (remoteParentDirectory != null) {
                changeWorkingDirectory(remoteParentDirectory);
            }

            sftpClient.mkdir(remoteDirectory);
            logger.debug("Remote directory " + remoteDirectory + " created into " + sftpClient.pwd());
            return true;
        }
        catch (SftpException exc) {
            throw new RemoteManagerException("Could not create remote directory " + remoteDirectory, exc);
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

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#getDir(java.lang.String,
     *      java.lang.String, java.lang.String, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean getDir(String remoteDirectory, String localParentDirectory, String localDirectory,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        try {
            changeWorkingDirectory(remoteDirectory);

            if (localDirectory == null) {
                localDirectory = new File(remoteDirectory).getName();
            }

            File localDirectoryObj = new File(localParentDirectory, localDirectory);
            if (!localDirectoryObj.exists()) {
                if (!localDirectoryObj.mkdir()) {
                    throw new RemoteManagerException("Cannot create local directory "
                            + localDirectoryObj.getAbsolutePath());
                }
                logger.debug("Local directory " + localDirectoryObj.getAbsolutePath() + " created");
            }

            Vector<LsEntry> filenames = sftpClient.ls(".");
            int detectedFiles = (filenames != null ? filenames.size() : 0);
            logger.debug(detectedFiles + " file entries DETECTED into current remote working directory");

            sftpClient.get(remoteDirectory, localDirectoryObj.getCanonicalPath());

            return true;
        }
        catch (RemoteManagerException exc) {
            throw exc;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("Could not download remote directory " + remoteDirectory, exc);
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

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#get(java.lang.String,
     *      java.lang.String, java.io.OutputStream, java.util.Map)
     */
    @Override
    public boolean get(String remoteDirectory, String remoteFile, OutputStream outputStream,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        try {
            logger.debug("Downloading remote file "
                    + remoteFile
                    + " from "
                    + (remoteDirectory != null
                            ? " remote directory " + remoteDirectory
                            : " current remote working directory") + "...");

            if (remoteDirectory != null) {
                changeWorkingDirectory(remoteDirectory);
            }

            sftpClient.get(remoteFile, outputStream);

            return true;
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

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#get(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    public boolean get(String remoteDirectory, String remoteFile, String localDirectory, String localFile,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

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

            logger.debug("Saving to " + localPathname);

            if (remoteDirectory != null) {
                changeWorkingDirectory(remoteDirectory);
            }

            sftpClient.get(remoteFile, localPathname.getCanonicalPath());

            return true;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("Could not download file " + remoteFile, exc);
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

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#get(String, String,
     *      String, java.util.Map)
     */
    @Override
    public boolean get(String remoteDirectory, String remoteFilePattern, String localDirectory,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        boolean oldAutoConnect = isAutoconnect();
        setAutoconnect(false);

        boolean result = false;
        try {
            changeWorkingDirectory(remoteDirectory);

            File localDirectoryObj = new File(localDirectory);
            if (!localDirectoryObj.exists()) {
                if (!localDirectoryObj.mkdir()) {
                    throw new RemoteManagerException("Cannot create local directory "
                            + localDirectoryObj.getAbsolutePath());
                }
                logger.debug("Local directory " + localDirectoryObj.getAbsolutePath() + " created");
            }

            Vector<LsEntry> filenames = sftpClient.ls(".");

            if (filenames != null) {
                RegExFileFilter fileFilter = new RegExFileFilter(remoteFilePattern, RegExFileFilter.ALL, -1);
                for (LsEntry currFTPFile : filenames) {
                    if (currFTPFile != null) {
                        if (fileFilter.accept(currFTPFile)) {
                            boolean partialResult = true;
                            if (currFTPFile.getAttrs().isDir()) {
                                partialResult = getDir(currFTPFile.getFilename(), localDirectoryObj.getAbsolutePath(),
                                        null, optProperties);
                            }
                            else {
                                partialResult = get(null, currFTPFile.getFilename(),
                                        localDirectoryObj.getAbsolutePath(), null, optProperties);
                            }
                            if (!partialResult) {
                                break;
                            }
                        }
                    }
                    else {
                        logger.debug("Remote file entry NULL");
                    }
                }
            }

            return result;
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
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#listMatchingFiles(java.lang.String,
     *      java.lang.String, java.util.Date, int, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<FileProperties> ls(String remoteDirectory, String fileNamePattern, Date modifiedSince, int fileTypeFilter,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        Set<FileProperties> resultsSet = new HashSet<FileProperties>();
        try {
            changeWorkingDirectory(remoteDirectory);

            Vector<LsEntry> filenames = sftpClient.ls(".");
            int detectedFiles = (filenames != null ? filenames.size() : 0);
            logger.debug(detectedFiles + " file entries DETECTED into current remote working directory");

            if (filenames != null) {
                RegExFileFilter fileFilter = new RegExFileFilter(fileNamePattern, fileTypeFilter,
                        (modifiedSince != null) ? modifiedSince.getTime() : -1);
                for (LsEntry currFTPFile : filenames) {
                    if (currFTPFile != null) {
                        if (fileFilter.accept(currFTPFile)) {
                            SftpATTRS attrs = currFTPFile.getAttrs();
                            int permissions = attrs.getPermissions();
                            FileProperties currFile = new FileProperties(currFTPFile.getFilename(),
                                    ((long) attrs.getMTime()) * 1000, attrs.getSize(), attrs.isDir(),
                                    ((permissions & 256) != 0), ((permissions & 128) != 0), ((permissions & 64) != 0));
                            resultsSet.add(currFile);
                        }
                    }
                }
            }
            else {
                logger.debug("Remote file entry NULL");
            }
            return resultsSet;
        }
        catch (Exception exc) {
            throw new RemoteManagerException("SSH directory scan error", exc);
        }
        finally {
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#rm(String, String, java.util.Map)
     */
    @Override
    public boolean rm(String remoteDirectory, String entryNamePattern,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        try {
            if (remoteDirectory != null) {
                changeWorkingDirectory(remoteDirectory);
            }
            Vector<LsEntry> filenames = sftpClient.ls(".");

            if (filenames != null) {
                RegExFileFilter fileFilter = new RegExFileFilter(entryNamePattern, RegExFileFilter.ALL, -1);
                for (LsEntry currFTPFile : filenames) {
                    if (currFTPFile != null) {
                        if (fileFilter.accept(currFTPFile)) {
                            if (currFTPFile.getAttrs().isDir()) {
                                sftpClient.rmdir(currFTPFile.getFilename());
                                logger.debug("Remote directory " + currFTPFile.getFilename() + " deleted.");
                            }
                            else {
                                sftpClient.rm(currFTPFile.getFilename());
                                logger.debug("Remote file " + currFTPFile.getFilename() + " deleted.");
                            }
                        }
                    }
                }
            }
            else {
                logger.debug("Remote file entry NULL");
            }

            return true;
        }
        catch (SftpException exc) {
            throw new RemoteManagerException("Cannot remove entry: " + remoteDirectory + "/" + entryNamePattern, exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error ", exc);
        }
        finally {
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }


    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#mv(java.lang.String,
     *      java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    public boolean mv(String remoteParentDirectory, String oldEntryName, String newEntryName,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        boolean result = false;
        try {
            if (remoteParentDirectory != null) {
                changeWorkingDirectory(remoteParentDirectory);
            }
            sftpClient.rename(oldEntryName, newEntryName);

            logger.debug("Remote entry " + oldEntryName + " renamed to " + newEntryName + ".");
            return result;
        }
        catch (SftpException exc) {
            throw new RemoteManagerException("Cannot rename remote entry " + oldEntryName + " to " + newEntryName, exc);
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

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#putDir(java.lang.String,
     *      java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    public boolean putDir(String localDirectory, String remoteParentDirectory, String remoteDirectory,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        try {
            File localDirectoryObj = new File(localDirectory);

            if (remoteParentDirectory != null) {
                changeWorkingDirectory(remoteParentDirectory);
            }

            if (remoteDirectory == null) {
                remoteDirectory = localDirectoryObj.getName();
            }
            sftpClient.put(localDirectory, remoteDirectory);
            logger.debug("Local directory " + localDirectory + " uploaded");
            return true;
        }
        catch (SftpException exc) {
            throw new RemoteManagerException("Error uploading local directory " + localDirectory, exc);
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

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#put(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    public boolean put(String localDirectory, String localFile, String remoteDirectory, String remoteFile,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

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

            if (remoteDirectory != null) {
                changeWorkingDirectory(remoteDirectory);
            }

            sftpClient.put(localPathname.getAbsolutePath(), (remoteFile != null ? remoteFile : localFile));
            logger.debug("Local file " + localPathname + " uploaded.");
            return true;
        }
        catch (SftpException exc) {
            throw new RemoteManagerException("Error uploading file " + localFile, exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error ", exc);
        }
        finally {
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#put(String, String,
     *      String, java.util.Map)
     */
    @Override
    public boolean put(String localDirectory, String localFilePattern, String remoteDirectory,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        boolean oldAutoConnect = isAutoconnect();
        setAutoconnect(false);

        boolean result = false;
        try {
            File localDirectoryObj = new File(localDirectory);

            changeWorkingDirectory(remoteDirectory);

            File[] localFiles = localDirectoryObj.listFiles(new RegExFileFilter(localFilePattern, RegExFileFilter.ALL));
            for (File currLocalFile : localFiles) {
                boolean partialResult = true;
                if (currLocalFile.isDirectory()) {
                    partialResult = putDir(currLocalFile.getAbsolutePath(), null, null, optProperties);
                }
                else {
                    partialResult = put(localDirectoryObj.getAbsolutePath(), currLocalFile.getName(), null, null,
                            optProperties);
                }

                if (!partialResult) {
                    break;
                }
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

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#put(java.io.InputStream,
     *      java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    public boolean put(InputStream inputDataStream, String remoteDirectory, String remoteFile,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        try {
            logger.debug("Uploading local file stream"
                    + (remoteDirectory != null
                            ? " to remote directory " + remoteDirectory
                            : " to current remote working directory") + "...");

            if (remoteFile != null) {
                logger.debug("Renaming remote file to " + remoteFile);
            }

            if (remoteDirectory != null) {
                changeWorkingDirectory(remoteDirectory);
            }

            sftpClient.put(inputDataStream, remoteFile);
            logger.debug("Local file stream uploaded.");
            return true;
        }
        catch (SftpException exc) {
            throw new RemoteManagerException("Error uploading file stream", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error ", exc);

        }
        finally {
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see it.greenvulcano.util.remotefs.RemoteManager#getManagerKey()
     */
    @Override
    public String getManagerKey()
    {
        return "sftp://" + username + "@" + hostname + ":" + port;
    }

    @Override
    public String getManagerKey(Map<String, String> optProperties) throws RemoteManagerException
    {
        try {
            Map<String, Object> localProps = MapUtils.convertToHMStringObject(optProperties);
            return "sftp://" + PropertiesHandler.expand(username, localProps) + "@"
                    + PropertiesHandler.expand(hostname, localProps) + ":" + port;
        }
        catch (Exception exc) {
            throw new RemoteManagerException(exc);
        }
    }

    /**
     * Changes current working directory on the remote SSH server.
     * 
     * @param remoteDirectory
     *        the new working directory
     * @throws RemoteManagerException
     *         if any error occurs
     */
    private void changeWorkingDirectory(String remoteDirectory) throws RemoteManagerException
    {
        logger.debug("Changing remote working directory to " + remoteDirectory + "...");
        try {
            sftpClient.cd(remoteDirectory);
            logger.debug("Current working directory is: " + sftpClient.pwd());
        }
        catch (SftpException exc) {
            throw new RemoteManagerException("SSH server refused remote working directory change", exc);
        }
    }

}

/*
 * Copyright (c) 2005 E@I Software - All right reserved
 *
 * Created on dd-mmm-yyyy
 *
 */
package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import max.config.Config;

/**
 * This class manages the locks over the files.
 *
 *
 *
 */
public class LocksManager {
    /**
     * The directory containing the locks.
     */
    public static File          lockDir   = new File(Config.getDef("", "max.tmp.lockdir", System
                                                  .getProperty("java.io.tmpdir")));

    private static final String CRLF      = System.getProperty("line.separator");

    private static DateFormat   formatter = new SimpleDateFormat(Config.getDef("", "max.date.format",
                                                  "dd/MM/yyyy HH:mm:ss"));

    private LocksManager() {
        // The class cannot be instanciated.
    }

    /**
     * Sets a lock file related to a document.
     *
     * @param name
     *            the name of the document edited.
     * @param sessionId
     *            the session identifier.
     * @param remoteUser
     *            the user editing the document.
     */
    public static synchronized void lockDocument(String name, HttpServletRequest req) {
        try {
            File lockFile = new File(lockDir, name + ".lck");
            lockFile.deleteOnExit();
            BufferedWriter writer = new BufferedWriter(new FileWriter(lockFile));
            StringBuffer buf = new StringBuffer();
            String date = formatter.format(new Date());
            buf.append(req.getSession().getId()).append(CRLF).append(req.getRemoteUser()).append(CRLF).append(
                    req.getRemoteAddr()).append(CRLF).append(req.getRemoteHost()).append(CRLF).append(date);
            writer.write(buf.toString());
            writer.flush();
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove the lock of a document.
     *
     * @param sessionId
     *            the session identifier.
     * @throws XMLConfigException
     */
    public static synchronized void unlockDocument(String sessionId) throws XMLConfigException {
        LockInfo[] locks = getLocksInfo();
        String lockFileName = null;
        for (LockInfo info : locks) {
            if (info.getSessionId().equals(sessionId)) {
                lockFileName = info.getName();
            }
        }

        if (lockFileName != null) {
            File lockFile = new File(lockDir, lockFileName + ".lck");
            if (lockFile != null) {
                lockFile.delete();
            }
        }
    }

    /**
     * Checks if the document is locked.
     *
     * @param name
     *            the document name.
     * @return true if the document is locked.
     */
    public static synchronized boolean isLocked(String name) {
        File f = new File(lockDir, name + ".lck");
        return f.exists();
    }

    /**
     * Gets the list of lock files.
     *
     * @return the list of lock files.
     */
    public static synchronized File[] getLocks() {

        return lockDir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".lck");
            }
        });

    }

    /**
     * Gets the info of all locks.
     *
     * @return the info of all locks.
     */
    public static synchronized LockInfo[] getLocksInfo() throws XMLConfigException {
        File[] locks = getLocks();

        LockInfo[] l = new LockInfo[] {};
        Vector v = new Vector();
        if(locks!=null)
        for (File lock : locks) {
            v.add(getLockInfo(lock));
        }

        Collections.sort(v);
        return (LockInfo[]) v.toArray(l);
    }

    /**
     * Gets the info of a lock.
     *
     * @param lock
     *            the lock.
     *
     * @return the info of a lock.
     */
    public static synchronized LockInfo getLockInfo(File lock) throws XMLConfigException {

        LockInfo lockInfo = new LockInfo();
        String fileName = lock.getName();
        fileName = lock.getName().substring(0, fileName.length() - 4);
        lockInfo.setName(fileName);
        String label = XMLConfig.get("documentRepository.xml", "//document[@name='" + fileName + "']/@label");
        lockInfo.setLabel(label);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(lock));
            // The session ID
            lockInfo.setSessionId(reader.readLine());

            // The user
            lockInfo.setUser(reader.readLine());

            // The ip address
            lockInfo.setIpAddress(reader.readLine());

            // The host name
            lockInfo.setHostName(reader.readLine());

            // The date
            lockInfo.setDateString(reader.readLine());

            reader.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return lockInfo;
    }

    /**
     * Gets the info of a lock.
     *
     * @param name
     *            the name of the lock.
     * @return the info of a lock.
     * @throws XMLConfigException
     */
    public static synchronized LockInfo getLockInfo(String name) throws XMLConfigException {
        return getLockInfo(new File(lockDir, name + ".lck"));
    }

    /**
     * Forces the unlock of a document.
     *
     * @param name
     *            the name of the document.
     * @throws XMLConfigException
     */
    public static synchronized void forceUnlockDocument(String name) throws XMLConfigException {

        if (isLocked(name)) {
            String sessionId = getLockInfo(name).getSessionId();
            unlockDocument(sessionId);
        }
    }
}

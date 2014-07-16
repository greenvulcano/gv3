/*
 * Created on 10-ago-2005
 *
 */
package max.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import max.xml.DOMWriter;

import org.w3c.dom.Document;

/**
 * @author
 */
public class ZipFileEditor
{
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    private File    zipFile;
    private Map     newEntries;
    private Set     deletedEntries;
    private boolean verbose = false;

    // ----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    // ----------------------------------------------------------------------------------------------

    public ZipFileEditor(File zipFile)
    {
        this.zipFile = zipFile;
        newEntries = new HashMap();
        deletedEntries = new HashSet();
    }

    // ----------------------------------------------------------------------------------------------
    // METHODS
    // ----------------------------------------------------------------------------------------------

    /**
     * @param verbose
     *        The verbose to set.
     */
    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }

    /**
     * @return Returns the verbose.
     */
    public boolean isVerbose()
    {
        return verbose;
    }

    /**
     * Add or replace an entry.
     *
     * Normalizes the entry name using {@linkplain #normalizeEntryName(String)}.
     * If the entry is already marked as deleted, the mark is removed.
     *
     * @param entryName
     * @param content
     *
     * @see #deleteEntry(String)
     * @see #normalizeEntryName(String)
     */
    public void setEntry(String entryName, InputStream content)
    {
        checkNull("content", content);
        checkNull("entry", entryName);
        entryName = normalizeEntryName(entryName);
        newEntries.put(entryName, content);
        deletedEntries.remove(entryName);
    }

    /**
     * Add or replace an entry.
     *
     * Normalizes the entry name using {@linkplain #normalizeEntryName(String)}.
     * If the entry is already marked as deleted, the mark is removed.
     *
     * @param entryName
     * @param content
     *
     * @see #deleteEntry(String)
     * @see #normalizeEntryName(String)
     */
    public void setEntry(String entryName, File content)
    {
        checkNull("content", content);
        checkNull("entry", entryName);
        entryName = normalizeEntryName(entryName);
        newEntries.put(entryName, content);
        deletedEntries.remove(entryName);
    }

    /**
     * @param entryName
     * @param content
     *
     * @see #setEntry(String, InputStream)
     */
    public void setEntry(String entryName, byte[] content)
    {
        checkNull("content", content);
        setEntry(entryName, new ByteArrayInputStream(content));
    }

    /**
     * @param entryName
     * @param content
     *
     * @see #setEntry(String, InputStream)
     */
    public void setEntry(String entryName, String content)
    {
        checkNull("content", content);
        setEntry(entryName, content.getBytes());
    }

    /**
     * @param entryName
     * @param content
     * @param encoding
     *
     * @see #setEntry(String, InputStream)
     */
    public void setEntry(String entryName, String content, String encoding) throws UnsupportedEncodingException
    {
        checkNull("content", content);
        checkNull("encoding", encoding);
        setEntry(entryName, content.getBytes(encoding));
    }

    /**
     * @param entryName
     * @param content
     *
     * @see #setEntry(String, InputStream)
     */
    public void setEntry(String entryName, Document content) throws IOException
    {
        checkNull("content", content);

        DOMWriter writer = new DOMWriter();
        writer.setPreferredWidth(100);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        writer.write(content, outputStream);

        setEntry(entryName, outputStream.toByteArray());
    }

    /**
     * Removes an entry.
     *
     * Normalizes the entry name using {@linkplain #normalizeEntryName(String)}.
     *
     * @param entryName
     *
     * @see #setEntry(String, InputStream)
     * @see #normalizeEntryName(String)
     */
    public void deleteEntry(String entryName)
    {
        checkNull("entry", entryName);
        entryName = normalizeEntryName(entryName);
        newEntries.remove(entryName);
        deletedEntries.add(entryName);
    }

    /**
     * Normalizes the entry name.
     *
     * Backslashes (\) will be substituted by forward slashes (/), then double
     * slashes will be substituted with single slashes, then trailing slashes
     * will be removed.
     *
     * This method is automatically invoked by {@linkplain #deleteEntry(String)}
     * and {@linkplain #setEntry(String, InputStream)} and related methods.
     *
     * @param entryName
     *
     * @return the normalized entry name
     *
     * @see #setEntry(String, InputStream)
     * @see #deleteEntry(String)
     */
    public String normalizeEntryName(String entryName)
    {
        entryName = entryName.replace('\\', '/');
        int idx;
        while ((idx = entryName.indexOf("//")) != -1) {
            String pre = entryName.substring(0, idx);
            String post = entryName.substring(idx + 1);
            entryName = pre + post;
        }
        if (entryName.startsWith("/")) {
            return entryName.substring(1);
        }
        return entryName;
    }

    /**
     * Discard all setting performed with {@linkplain #deleteEntry(String)} and
     * {@linkplain #setEntry(String, InputStream)} and related methods.
     */
    public void rollback()
    {
        newEntries.clear();
        deletedEntries.clear();
    }

    /**
     * Modify the zip file according the settings performed by
     * {@linkplain #deleteEntry(String)} and
     * {@linkplain #setEntry(String, InputStream)} and related methods.
     *
     * @exception Exception
     *            in case of exception can occurs two situations:
     *            <ol>
     *            <li>A does not <code>*.new.zip</code> file exists<br/> The
     *            original file is valid and the commit is not performed.</br>
     *            The editor can be used to continue work.
     *
     * <li>A <code>*.new.zip</code> file exists<br/> The original file may
     * be corrupted, the <code>*.new.zip</code> file is valid and contains the
     * committed version.
     * </ol>
     */
    public void commit() throws Exception
    {
        log("----------------------------------------------------------------------");
        log("committing: " + zipFile.getAbsolutePath());

        File tempFile = createTempFile();

        log("writing...: " + tempFile.getAbsolutePath());

        try {
            writeToTempFile(tempFile);
        }
        catch (Exception exc) {
            log("ERROR: " + exc);
            tempFile.delete();
            throw exc;
        }

        log("updating..: " + zipFile.getAbsolutePath());

        // In case of exception, the original file may be corrupted,
        // the temporary file contains the valid committed version.
        //
        try {
            tempFileToOriginalFile(tempFile);
        }
        catch (Exception exc) {
            log("ERROR: " + exc);
            throw exc;
        }

        tempFile.delete();
        newEntries.clear();
        deletedEntries.clear();

        log("done.");
        log("----------------------------------------------------------------------");
    }

    // ----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    // ----------------------------------------------------------------------------------------------

    private void checkNull(String parameterName, Object obj)
    {
        if (obj != null) {
            return;
        }

        throw new NullPointerException(parameterName + " is null");
    }

    private File createTempFile()
    {
        File directory = zipFile.getParentFile();
        String fileName = zipFile.getName();
        int counter = 0;
        while (true) {
            File tempFile = new File(directory, fileName + "." + counter + ".new.zip");
            if (!tempFile.exists()) {
                return tempFile;
            }
            ++counter;
        }
    }

    private void writeToTempFile(File tempFile) throws IOException
    {
        FileOutputStream dest = new FileOutputStream(tempFile);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

        Map toWrite = new HashMap();
        toWrite.putAll(newEntries);

        transferToTempFile(out, toWrite);

        Iterator it = toWrite.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String entryName = (String) entry.getKey();
            InputStream istream = toInputStream(entry.getValue());
            ZipEntry newEntry = new ZipEntry(entryName);
            out.putNextEntry(newEntry);
            log("  new: " + entryName);
            transferContents(istream, out);
        }

        out.flush();
        out.close();
    }

    /**
     * @param zipOutputStream
     * @param toTransfer
     *        as collateral effect, from this map will be removed transfered
     *        entries
     * @throws IOException
     */
    private void transferToTempFile(ZipOutputStream zipOutputStream, Map toTransfer) throws IOException
    {
        if (!zipFile.exists()) {
            return;
        }

        ZipFile sourceZipFile = new ZipFile(zipFile);

        Enumeration e = sourceZipFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry zipEn = (ZipEntry) e.nextElement();
            String entryName = normalizeEntryName(zipEn.getName());

            if (!deletedEntries.contains(entryName)) {
                InputStream istream = toInputStream(toTransfer.remove(entryName));

                if (istream == null) {
                    istream = sourceZipFile.getInputStream(zipEn);
                    zipOutputStream.putNextEntry(zipEn);
                    log("  cpy: " + entryName);
                }
                else {
                    ZipEntry newEntry = new ZipEntry(entryName);
                    zipOutputStream.putNextEntry(newEntry);
                    log("  upd: " + entryName);
                }

                transferContents(istream, zipOutputStream);
            }
            else {
                log("  del: " + entryName);
            }
        }

        sourceZipFile.close();
    }

    private void tempFileToOriginalFile(File tempFile) throws IOException
    {
        transferContents(tempFile, zipFile);
    }

    private void transferContents(File sourceFile, File destFile) throws IOException
    {
        InputStream istream = new FileInputStream(sourceFile);
        OutputStream ostream = new FileOutputStream(destFile);

        transferContents(istream, ostream);

        ostream.close();
    }

    /**
     * Does not close the destination stream.
     *
     * @param sourceStream
     * @param destStream
     * @throws IOException
     */
    private void transferContents(InputStream sourceStream, OutputStream destStream) throws IOException
    {
        if (!(sourceStream instanceof BufferedInputStream)) {
            sourceStream = new BufferedInputStream(sourceStream);
        }
        if (!(destStream instanceof BufferedOutputStream)) {
            destStream = new BufferedOutputStream(destStream);
        }

        byte[] buff = new byte[4096];
        int len = 0;
        while ((len = sourceStream.read(buff)) != -1) {
            destStream.write(buff, 0, len);
        }

        sourceStream.close();
        destStream.flush();
    }

    private InputStream toInputStream(Object obj) throws FileNotFoundException
    {
        if (obj == null) {
            return null;
        }
        else if (obj instanceof InputStream) {
            return (InputStream) obj;
        }
        else if (obj instanceof File) {
            return new FileInputStream((File) obj);
        }
        else {
            throw new IllegalArgumentException(obj.getClass().getName());
        }
    }

    private void log(String logString)
    {
        if (verbose) {
            System.out.println("ZipFileEditor: " + logString);
        }
    }
}

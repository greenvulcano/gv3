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
package it.greenvulcano.gvesb.gvconsole.workbench.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;

/**
 * This utility class reads the client data with an HTML form
 * <code>method="POST"</code> and <code>enctype="multipart/form-data"</code>.
 * <p>
 * This form can have as input <code>type="file"</code>.
 * <p>
 *
 * In the previous example the form executes POST to <code>/some/url</code> of a
 * file or text:
 *
 * <pre>
 *
 *
 *  &lt;form action=&quot;/some/url&quot; &lt;b&gt;method=&quot;post&quot;&lt;/b&gt;
 *     &lt;b&gt;enctype=&quot;multipart/form-data&quot;&lt;/b&gt;&gt;
 *     &lt;input &lt;b&gt;type=&quot;file&quot;&lt;/b&gt; name=&quot;photo&quot;&gt;&lt;br&gt;
 *     &lt;input type=&quot;text&quot; name=&quot;who&quot;&gt;&lt;br&gt;
 *     &lt;input type=&quot;submit&quot; value=&quot; Submit &quot;&gt;
 *  &lt;/form&gt;
 *
 *
 * </pre>
 *
 * <p>
 * To receive data the <code>MultipartFormDataParser</code> object must be
 * instantiated with <code>ServletRequest</code> parameter. This object must be
 * used to access data as following example:
 *
 * <pre>
 *
 *
 *     ...
 *     MultipartFormDataParser mp = new MultipartFormDataParser(request);
 *     ...
 *     InputStream is = mp.getInputStream(&quot;photo&quot;);
 *     String text = mp.getString(&quot;who&quot;);
 *     ...
 *
 *
 * </pre>
 *
 * <p>
 * The client can send different occurrence for the same field. <br>
 * <code>MultipartFormDataParser</code> put data sent by client in a local
 * server files. <br>
 * And gives different methods to access at the client data sent.
 * <p>
 *
 * It's appropriate to delete temporary files with the <code>close()</code>
 * method when these files are not yet necessary. <br>
 * These files can be deleted with the <code>finalize()</code> method invoked by
 * the garbage collector if the object is not yet referentiated. <br>
 * In every case files will be deleted on JVM exit.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class MultipartFormDataParser
{
    /**
     * The input stream
     */
    private ServletInputStream in;

    /**
     * The initial step
     */
    private String             boundary;

    /**
     * The final step
     */
    private String             endBoundary;

    /**
     * The final data
     */
    private boolean            endData;

    /**
     * For every name memorizes a Vector of files. These files have the objects
     * in upload.
     */
    private Hashtable          bodies = new Hashtable();

    /**
     * The current file
     */
    private File               currentFile;

    /**
     * Read the input stream and put the request in a temporary local files on
     * server.
     * <p>
     *
     * @param request
     *        The client request
     * @exception IOException
     *            If an error occurred putting data in temporary files.
     * @exception MessagingException
     *            If data sent by client have an invalid mime format
     */
    public MultipartFormDataParser(ServletRequest request) throws IOException, MessagingException
    {
        in = request.getInputStream();
        endData = false;

        boundary = readString().trim();
        endBoundary = boundary + "--";

        while (!endData) {
            MimeBodyPart bp = readBodyPart();
            put(getName(bp), currentFile);
        }

        in = null;
        boundary = null;
        endBoundary = null;
        currentFile = null;
    }

    /**
     * Verifies that client has sent a parameter with name.
     *
     * @param name
     *        parameter name
     * @return <code>true</code> if the client sent the parameter name
     */
    public boolean exists(String name)
    {
        Object o = bodies.get(name);
        return o != null;
    }

    /**
     * Gives the occurrence number of the parameter.
     *
     * @param name
     *        parameter name.
     * @return parameter name occurrence number.
     */
    public int getCount(String name)
    {
        Vector v = (Vector) bodies.get(name);
        if (v == null) {
            return 0;
        }
        return v.size();
    }

    /**
     * Gives names of all parameters.
     *
     * @return parameters names.
     */
    public String[] getParameterNames()
    {
        int n = bodies.size();
        String[] ret = new String[n];
        int i = 0;
        for (Enumeration e = bodies.keys(); e.hasMoreElements(); ++i) {
            ret[i] = e.nextElement().toString();
        }
        return ret;
    }

    /**
     * Gives the <code>MimeBodyPart</code> object. This object describes the
     * parameter.
     *
     * @param name
     *        parameter name
     * @param occ
     *        occurrence parameter's number
     * @return oggetto <code>MimeBodyPart</code> describes parameters. <br>
     *         Gives <code>null</code> if the parameter not exist or the
     *         occurrence not exist.
     * @exception FileNotFoundException
     *            If file not found
     * @exception MessagingException
     *            If the temporary file is corrupted and have data in not valid
     *            mime format
     */
    public MimeBodyPart getBodyPart(String name, int occ) throws FileNotFoundException, MessagingException
    {
        Vector v = (Vector) bodies.get(name);
        if (v == null) {
            return null;
        }
        if (v.size() <= occ) {
            return null;
        }
        File f = (File) v.elementAt(occ);
        FileInputStream inputStream = new FileInputStream(f);
        return new MimeBodyPart(inputStream);
    }

    /**
     * Gives the object <code>MimeBodyPart</code>. This object describes the
     * first parameter occurrence.
     *
     * @param name
     *        parameter name
     * @return <code>MimeBodyPart</code> object describing the first parameter
     *         occurrence. <br>
     *         Gives <code>null</code> if the parameter not exist.
     * @exception FileNotFoundException
     *            Temporary file not found
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    public MimeBodyPart getBodyPart(String name) throws FileNotFoundException, MessagingException
    {
        return getBodyPart(name, 0);
    }

    /**
     * Gives <code>MimeBodyPart</code> objects. These objects describe all the
     * parameter occurrences.
     *
     * @param name
     *        parameter name
     * @return <code>MimeBodyPart</code> objects describing parameter
     *         occurrences.
     * @exception FileNotFoundException
     *            If file not found
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    public MimeBodyPart[] getBodyParts(String name) throws FileNotFoundException, MessagingException
    {
        MimeBodyPart[] ret = new MimeBodyPart[getCount(name)];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = getBodyPart(name, i);
        }
        return ret;
    }

    /**
     * Gives the local file name to client for the parameter.
     *
     * @param name
     *        parameter name
     * @param occ
     *        occurrence numgvr of paramater
     * @return the local file name for the parameter. <br>
     *         Gives <code>null</code> if parameter not exist or the occurrence
     *         not exist or the parameter isn't <code>type="file"</code>.
     * @exception FileNotFoundException
     *            If temporary file not found
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    public String getFileName(String name, int occ) throws MessagingException, FileNotFoundException
    {
        MimeBodyPart bp = getBodyPart(name, occ);
        if (bp == null) {
            return null;
        }
        return getFileName(bp);
    }

    /**
     * Gives the local file name to client for the first parametr occurrence.
     *
     * @param name
     *        parameter name
     * @return locale file name to client for the parameter. <br>
     *         Gives <code>null</code> if the parameter not exist or the
     *         parameter isn't <code>type="file"</code>.
     * @exception FileNotFoundException
     *            If temporary file not found
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    public String getFileName(String name) throws MessagingException, FileNotFoundException
    {
        return getFileName(name, 0);
    }

    /**
     * Gives local files names to client for every parameter's occurrences.
     *
     * @param name
     *        parameter name
     * @return local files names to client for parameter. <br>
     *         The array element corresponding at occurrences not equal to
     *         <code>type="file"</code> is <code>null</code>.
     * @exception FileNotFoundException
     *            If temporary file not found
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    public String[] getFileNames(String name) throws MessagingException, FileNotFoundException
    {
        String[] ret = new String[getCount(name)];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = getFileName(name, i);
        }
        return ret;
    }

    /**
     * Gives the <code>Object</code> containing parameter value.
     *
     * @param name
     *        parameter name
     * @param occ
     *        occurrence number parameter
     * @return <code>Object</code> containing parameter value <br>
     *         Gives <code>null</code> if parameter not exist or the occurrence
     *         not exist.
     * @exception IOException
     *            If an error occurred reading file
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    public Object getContent(String name, int occ) throws MessagingException, IOException
    {
        MimeBodyPart bp = getBodyPart(name, occ);
        if (bp == null) {
            return null;
        }
        return bp.getContent();
    }

    /**
     * Gives <code>Object</code> containig the first parameter occurrence value.
     *
     * @param name
     *        Parameter name
     * @return <code>Object</code> containing the first occurrence parameter
     *         value. <br>
     *         Gives <code>null</code> if parameter not exist.
     * @exception IOException
     *            If an error occurred reading file.
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    public Object getContent(String name) throws MessagingException, IOException
    {
        return getContent(name, 0);
    }

    /**
     * Gives <code>Object</code> containing all the occurrence parameter value.
     *
     * @param name
     *        parameter name
     * @return array <code>Object</code> containing all occurrences parameter
     *         values.
     * @exception IOException
     *            If an error occurred reading file
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    public Object[] getContents(String name) throws MessagingException, IOException
    {
        Object[] ret = new Object[getCount(name)];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = getContent(name, i);
        }
        return ret;
    }

    /**
     * Gives parameter value.
     *
     * @param name
     *        parameter name
     * @param occ
     *        parameter occurrence number
     * @return parameter value. <br>
     *         Gives <code>null</code> if parameter not exist or occurrence not
     *         exist.
     * @exception IOException
     *            If an error occurred reading file.
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     *
     */
    public String getString(String name, int occ) throws MessagingException, IOException
    {
        Object ret = getContent(name, occ);
        if (ret == null) {
            return null;
        }
        return ret.toString();
    }

    /**
     * Gives the first parameter occurrence value.
     *
     * @param name
     *        parameter name
     * @return the first paramater occurence value. <br>
     *         Gives <code>null</code> if parameter not exist.
     * @exception IOException
     *            If an error occurred reading file
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    public String getString(String name) throws MessagingException, IOException
    {
        return getString(name, 0);
    }

    /**
     * Gives oll occurrences parameter values
     *
     * @param name
     *        parameter name
     * @return all occurrences parameter values.
     * @exception IOException
     *            If an error occurred reading file
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    public String[] getStrings(String name) throws MessagingException, IOException
    {
        String[] ret = new String[getCount(name)];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = getString(name, i);
        }
        return ret;
    }

    /**
     * Gives <code>InputStream</code> to read the occurrence parameter value
     *
     * @param name
     *        parameter name
     * @param occ
     *        occurrence number parameter
     * @return <code>InputStream</code> to read value. <br>
     *         Gives <code>null</code> if the parameter not exist or parameter
     *         not exist or occurrence not exist.
     * @exception IOException
     *            If an error occurred reading file
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    public InputStream getInputStream(String name, int occ) throws MessagingException, IOException
    {
        MimeBodyPart bp = getBodyPart(name, occ);
        if (bp == null) {
            return null;
        }
        return bp.getInputStream();
    }

    /**
     *
     * Gives <code>InputStream</code> to read the first occurrence values
     *
     * @param name
     *        parameter name
     * @return <code>InputStream</code> to read value. <br>
     *         Gives <code>null</code> if parameter not exist
     * @exception IOException
     *            If an error occurred reading file
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    public InputStream getInputStream(String name) throws MessagingException, IOException
    {
        return getInputStream(name, 0);
    }

    /**
     * Gives <code>InputStream</code> to read parameter occurence value.
     *
     * @param name
     *        parametr name
     * @return <code>InputStream</code> object to read values.
     * @exception IOException
     *            If an error occurred reading file
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    public InputStream[] getInputStreams(String name) throws MessagingException, IOException
    {
        InputStream[] inputStream = new InputStream[getCount(name)];
        for (int i = 0; i < inputStream.length; ++i) {
            inputStream[i] = getInputStream(name, i);
        }
        return inputStream;
    }

    /**
     * Put temporary file in vector
     *
     * @param name
     *        name
     * @param file
     *        file
     */
    private void put(String name, File file)
    {
        Vector v = (Vector) bodies.get(name);
        if (v == null) {
            v = new Vector();
            bodies.put(name, v);
        }
        v.addElement(file);
    }

    /**
     * Get the parameter name
     *
     * @param bp
     *        The Mime body part
     * @return The name
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    private String getName(MimeBodyPart bp) throws MessagingException
    {
        String disposition = bp.getDisposition();
        if (disposition.toLowerCase().equals("form-data")) {
            return getAttribute(bp.getHeader("Content-disposition", null), "name");
        }

        return null;
    }

    /**
     * Get the file name
     *
     * @param bp
     *        The mime body part
     * @return The file name
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    private String getFileName(MimeBodyPart bp) throws MessagingException
    {
        String disposition = bp.getDisposition();
        if (disposition.toLowerCase().equals("form-data")) {
            return getAttribute(bp.getHeader("Content-disposition", null), "filename");
        }

        return null;
    }

    /**
     * Get the attribute
     *
     * @param str
     *        The string value
     * @param attr
     *        the attribute to get
     * @return The attribute
     */
    private String getAttribute(String str, String attr)
    {
        int idx = str.indexOf(attr + "=\"");
        if (idx == -1) {
            return null;
        }
        idx = idx + attr.length() + 2;
        int idx2 = str.indexOf("\"", idx);
        if (idx2 == -1) {
            return null;
        }
        return str.substring(idx, idx2);
    }

    /**
     * Read the mime body part
     *
     * @return the <code>MimeBodyPart</code> object
     * @exception IOException
     *            If an error occurred reading file
     * @exception MessagingException
     *            If the temporary file is corrupted or contains data with not
     *            valid mime format.
     */
    private MimeBodyPart readBodyPart() throws IOException, MessagingException
    {
        currentFile = File.createTempFile("MPFormDataParser", ".tmp");
        currentFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(currentFile);
        byte[] b = null;
        byte[] prev = null;
        byte[] n = {'\n'};
        byte[] rn = {'\r', '\n'};
        while ((b = readBytes()) != null) {
            try {
                String s = new String(b);
                if (s.startsWith(boundary)) {
                    endData = s.startsWith(endBoundary);
                    break;
                }
            }
            catch (Exception exc) {
                // Eccezione di encoding: non � un boundary!!!
            }
            if (prev != null) {
                out.write(prev);
            }
            int l = b.length;
            if ((l >= 2) && (b[l - 2] == '\r') && (b[l - 1] == '\n')) {
                prev = rn;
                out.write(b, 0, l - 2);
            }
            else if ((l >= 1) && (b[l - 1] == '\n')) {
                prev = n;
                out.write(b, 0, l - 1);
            }
            else {
                prev = null;
                out.write(b);
            }
        }
        out.flush();
        out.close();
        FileInputStream inputStream = new FileInputStream(currentFile);
        MimeBodyPart bp = new MimeBodyPart(inputStream);
        inputStream.close();
        return bp;
    }

    /**
     * Read string
     *
     * @return The string
     * @throws IOException
     *         If an error occurred
     */
    private String readString() throws IOException
    {
        byte[] buf = readBytes();
        if (buf == null) {
            return null;
        }
        return new String(buf);
    }

    /**
     * Read bytes
     *
     * @return the bytes
     * @throws IOException
     *         If an error occurred
     */
    private byte[] readBytes() throws IOException
    {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(2048);
        byte[] b = new byte[1024];
        int l;
        while ((l = in.readLine(b, 0, 1024)) != -1) {
            buf.write(b, 0, l);
            if (b[l - 1] == '\n') {
                return buf.toByteArray();
            }
        }
        return null;
    }

    /**
     * Removes the temporary files containing data sent by client.
     */
    public void close()
    {
        removeFiles();
        bodies = new Hashtable();
    }

    /**
     * Delets temporary files.
     */
    private void removeFiles()
    {
        for (Enumeration b = bodies.elements(); b.hasMoreElements();) {
            Vector v = (Vector) b.nextElement();
            for (Enumeration e = v.elements(); e.hasMoreElements();) {
                File f = (File) e.nextElement();
                f.delete();
            }
        }
    }

    /**
     * Delets temporary files containing parameters value if files are not
     * already deleted. <br>
     * This method is not invoked esplicity, use <code>close()</code>.
     *
     * @see #close()
     */
    @Override
    public void finalize()
    {
        removeFiles();
    }
}
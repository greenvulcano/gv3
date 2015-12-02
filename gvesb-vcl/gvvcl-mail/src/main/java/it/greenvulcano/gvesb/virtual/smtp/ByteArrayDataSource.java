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
package it.greenvulcano.gvesb.virtual.smtp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.activation.DataSource;

/**
 * A simple DataSource for demonstration purposes. This class implements a
 * DataSource from: an InputStream a byte array a String
 *
 * @version 3.0.0 Feb 17, 2010
 * @author Ciro Romano
 */
public class ByteArrayDataSource implements DataSource
{

    /**
     * The data.
     */
    private byte[] data;

    /**
     * The content type.
     */
    private String type;

    /**
     * Creates a DataSource from an input stream.
     *
     * @param is
     *        the input stream.
     * @param type
     *        the content type.
     */
    public ByteArrayDataSource(InputStream is, String type)
    {
        this.type = type;

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int ch;

            while ((ch = is.read()) != -1) {
                os.write(ch);
            }
            data = os.toByteArray();
        }
        catch (IOException ioex) {
            // do nothing
        }
    }

    /**
     * Creates a DataSource from a byte array.
     *
     * @param data
     *        the byte array.
     * @param type
     *        the content type.
     */
    public ByteArrayDataSource(byte[] data, String type)
    {
        this.data = data;
        this.type = type;
    }

    /**
     * Create a DataSource from a String.
     *
     * @param data
     *        the string.
     * @param type
     *        the content type.
     */
    public ByteArrayDataSource(String data, String type)
    {
        try {
            // Assumption that the string contains only ASCII
            // characters! Otherwise just pass a charset into this
            // constructor and use it in getBytes()
            this.data = data.getBytes("iso-8859-1");
        }
        catch (UnsupportedEncodingException uex) {
            // do nothing
        }
        this.type = type;
    }

    /**
     * Returns an InputStream representing the the data. and will throw an
     * IOException if it can not do so.
     *
     * @return an InputStream representing the the data.
     *
     * @exception IOException
     *            if it can not do so.
     */
    public InputStream getInputStream() throws IOException
    {
        if (data == null) {
            throw new IOException("no data");
        }
        return new ByteArrayInputStream(data);
    }

    /**
     * Returns an OutputStream representing the the data and will throw an
     * IOException if it can not do so. This method will return a new instance
     * of OutputStream with each invocation.
     *
     * @return an OutputStream representing the the data.
     *
     * @exception IOException
     *            if it can not do so.
     */
    public OutputStream getOutputStream() throws IOException
    {
        throw new IOException("cannot do this");
    }

    /**
     * This method returns the MIME type of the data in the form of a string.
     *
     * @return the MIME type of the data in the form of a string.
     */
    public String getContentType()
    {
        return type;
    }

    /**
     * This method is not significant for this class.
     *
     * @return dummy
     */
    public String getName()
    {
        return "dummy";
    }
}
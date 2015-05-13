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
package it.greenvulcano.gvesb.gvnet.marshall;

import it.greenvulcano.gvesb.gvnet.NetAdapterException;
import it.greenvulcano.util.bin.Dump;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;

/**
 *
 * @version 3.5.0 18/mag/2014
 * @author GreenVulcano Developer Team
 *
 */
public class NetMessage
{
    private ByteArrayOutputStream buffer   = null;

    public NetMessage() {
        buffer = new ByteArrayOutputStream();
    }

    public NetMessage(byte[] data) {
        this(data, 0, data.length);
    }

    public NetMessage(byte[] data, int offset, int length) {
        buffer = new ByteArrayOutputStream(length);
        buffer.write(data, offset, length);
    }

    public void write(int data) throws NetAdapterException {
        buffer.write(data);
    }

    public void write(String data, String charsetName) throws NetAdapterException {
        try {
            buffer.write(data.getBytes(charsetName));
        }
        catch (Exception exc) {
            throw new NetAdapterException("Error converting data", exc);
        }
    }

    public void write(byte[] data) throws NetAdapterException {
        write(data, 0, data.length);
    }

    public void write(byte[] data, int offset, int length) throws NetAdapterException {
        buffer.write(data, offset, length);
    }

    public int getLength() {
        return buffer.size();
    }
    
    public byte[] getAsBytes() {
        return buffer.toByteArray();
    }
    
    public String getAsString(String charsetName) throws NetAdapterException {
        try {
            return buffer.toString(charsetName);
        }
        catch (UnsupportedEncodingException exc) {
            throw new NetAdapterException("Error decoding buffer data", exc);
        }
    }
    
    public void writeTo(OutputStream out) throws NetAdapterException {
        try {
            IOUtils.write(buffer.toByteArray(), out);
            //out.flush();
        }
        catch (Exception exc) {
            throw new NetAdapterException("Error writing data on stream", exc);
        }
    }
    
    public void dump(Appendable app) throws NetAdapterException {
        try {
            app.append((new Dump(buffer.toByteArray(), -1)).toString());
        }
        catch (IOException exc) {
            throw new NetAdapterException("Error making data dump", exc);
        }
    }
}

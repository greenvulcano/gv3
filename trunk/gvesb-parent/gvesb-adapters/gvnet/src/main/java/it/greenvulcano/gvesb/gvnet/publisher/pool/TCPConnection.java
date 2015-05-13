/*
 * Copyright (c) 2009-2015 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.gvnet.publisher.pool;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvnet.NetAdapterException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.w3c.dom.Node;

/**
 * 
 * @version 3.5.0 09/mar/2015
 * @author GreenVulcano Developer Team
 * 
 */
public class TCPConnection implements Connection
{
    private String  host;
    private int     port;
    private boolean pooled    = false;

    private Socket  socket    = null;
    private int     soTimeout = 0;

    /**
     * 
     */
    public TCPConnection(String host, int port, int soTimeout, boolean pooled) throws NetAdapterException {
        this.host = host;
        this.port = port;
        this.soTimeout = soTimeout;
        this.pooled = pooled;
        try {
            socket = new Socket(this.host, this.port);
            socket.setSoTimeout(this.soTimeout);
        }
        catch (Exception exc) {
            throw new NetAdapterException("Error initializing connection to[" + host + ":" + port + "]", exc);
        }
    }
    
    public TCPConnection() {
        // do nothing
    }
    
    public void init(Node node) throws NetAdapterException {
        try {
            this.host = XMLConfig.get(node, "@host");
            this.port = XMLConfig.getInteger(node, "@port");
            this.soTimeout = XMLConfig.getInteger(node, "@soTimeout");

            socket = new Socket(this.host, this.port);
            socket.setSoTimeout(this.soTimeout);
        }
        catch (Exception exc) {
            throw new NetAdapterException("Error initializing connection to[" + host + ":" + port + "]", exc);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.greenvulcano.gvesb.gvnet.publisher.pool.Connection#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws NetAdapterException {
        try {
            return socket.getInputStream();
        }
        catch (IOException exc) {
            throw new NetAdapterException("Error obtaining InputStream", exc);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.greenvulcano.gvesb.gvnet.publisher.pool.Connection#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream() throws NetAdapterException {
        try {
            return socket.getOutputStream();
        }
        catch (IOException exc) {
            throw new NetAdapterException("Error obtaining OurputStream", exc);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.gvnet.publisher.pool.Connection#isValid()
     */
    @Override
    public boolean isValid() {
        return isConnected();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.gvnet.publisher.pool.Connection#isConnected()
     */
    @Override
    public boolean isConnected() {
        return (socket != null) && !socket.isClosed() && socket.isConnected();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.gvnet.publisher.pool.Connection#isClosed()
     */
    @Override
    public boolean isClosed() {
        return (socket == null) || socket.isClosed() || !socket.isConnected();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.gvnet.publisher.pool.Connection#isPooled()
     */
    @Override
    public boolean isPooled() {
        return pooled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.gvnet.publisher.pool.Connection#close()
     */
    @Override
    public void close() {
        if (socket != null) {
            try {
                socket.close();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.gvnet.publisher.pool.Connection#getHost()
     */
    @Override
    public String getHost() {
        return host;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.gvnet.publisher.pool.Connection#getPort()
     */
    @Override
    public int getPort() {
        return port;
    }

}

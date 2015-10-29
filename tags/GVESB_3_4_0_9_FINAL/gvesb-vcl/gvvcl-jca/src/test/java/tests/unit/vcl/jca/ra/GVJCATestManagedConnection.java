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
package tests.unit.vcl.jca.ra;

import java.io.File;
import java.io.PrintWriter;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import tests.unit.vcl.jca.ra.cci.GVJCATestCciConnection;

/**
 * @version 3.0.0 Mar 23, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class GVJCATestManagedConnection implements ManagedConnection
{
    private File                   file = null;
    private GVJCATestCciConnection conn = null;

    /**
     * @param file
     */
    public GVJCATestManagedConnection(File file)
    {
        this.file = file;
    }

    /**
     * @return the EIS
     * @throws ResourceException
     */
    public File getFile() throws ResourceException
    {
        if (file == null) {
            throw new ResourceException("File is NULL");
        }
        else {
            return file;
        }
    }

    /**
     * @see javax.resource.spi.ManagedConnection#addConnectionEventListener(javax.resource.spi.ConnectionEventListener)
     */
    @Override
    public void addConnectionEventListener(ConnectionEventListener connectioneventlistener)
    {
    }

    /**
     * @see javax.resource.spi.ManagedConnection#associateConnection(java.lang.Object)
     */
    @Override
    public void associateConnection(Object obj) throws ResourceException
    {
    }

    /**
     * @see javax.resource.spi.ManagedConnection#cleanup()
     */
    @Override
    public void cleanup() throws ResourceException
    {
    }

    /**
     * @see javax.resource.spi.ManagedConnection#destroy()
     */
    @Override
    public void destroy() throws ResourceException
    {
        file = null;
    }

    /**
     * @see javax.resource.spi.ManagedConnection#getConnection(javax.security.auth.Subject,
     *      javax.resource.spi.ConnectionRequestInfo)
     */
    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo connectionrequestinfo) throws ResourceException
    {
        if (conn == null) {
            conn = new GVJCATestCciConnection(this);
        }
        return conn;
    }

    /**
     * @see javax.resource.spi.ManagedConnection#getLocalTransaction()
     */
    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException
    {
        throw new ResourceException("Local transactions are NOT SUPPORTED");
    }

    /**
     * @see javax.resource.spi.ManagedConnection#getLogWriter()
     */
    @Override
    public PrintWriter getLogWriter() throws ResourceException
    {
        return null;
    }

    /**
     * @see javax.resource.spi.ManagedConnection#getMetaData()
     */
    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException
    {
        return new GVJCATestManagedConnectionMetaData();
    }

    /**
     * @see javax.resource.spi.ManagedConnection#getXAResource()
     */
    @Override
    public XAResource getXAResource() throws ResourceException
    {
        throw new ResourceException("XA transactions are NOT SUPPORTED");
    }

    /**
     * @see javax.resource.spi.ManagedConnection#removeConnectionEventListener(javax.resource.spi.ConnectionEventListener)
     */
    @Override
    public void removeConnectionEventListener(ConnectionEventListener connectioneventlistener)
    {
    }

    /**
     * @see javax.resource.spi.ManagedConnection#setLogWriter(java.io.PrintWriter)
     */
    @Override
    public void setLogWriter(PrintWriter printwriter) throws ResourceException
    {
    }

}

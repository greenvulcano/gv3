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
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import tests.unit.vcl.jca.ra.cci.GVJCATestCciConnectionFactory;

/**
 * @version 3.0.0 Mar 23, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class GVJCATestManagedConnectionFactory implements ManagedConnectionFactory
{

    private static final long serialVersionUID = 210L;

    private String            fileName;
    private File              file;

    /**
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory()
     */
    @Override
    public Object createConnectionFactory() throws ResourceException
    {
        throw new UnsupportedOperationException("ManagedConnectionFactory.createConnectionFactory");
    }

    /**
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory(javax.resource.spi.ConnectionManager)
     */
    @Override
    public Object createConnectionFactory(ConnectionManager connectionmanager) throws ResourceException
    {
        file = new File(fileName);
        return new GVJCATestCciConnectionFactory(this, connectionmanager);
    }

    /**
     * @see javax.resource.spi.ManagedConnectionFactory#createManagedConnection(javax.security.auth.Subject,
     *      javax.resource.spi.ConnectionRequestInfo)
     */
    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionrequestinfo)
            throws ResourceException
    {
        this.file = new File(this.fileName);
        return new GVJCATestManagedConnection(file);
    }

    /**
     * @see javax.resource.spi.ManagedConnectionFactory#getLogWriter()
     */
    @Override
    public PrintWriter getLogWriter() throws ResourceException
    {
        return null;
    }

    /**
     * @see javax.resource.spi.ManagedConnectionFactory#matchManagedConnections(java.util.Set,
     *      javax.security.auth.Subject,
     *      javax.resource.spi.ConnectionRequestInfo)
     */
    @SuppressWarnings("unchecked")
    @Override
    public ManagedConnection matchManagedConnections(Set set, Subject subject,
            ConnectionRequestInfo connectionrequestinfo) throws ResourceException
    {
        throw new UnsupportedOperationException("ManagedConnectionFactory.matchManagedConnections");
    }

    /**
     * @see javax.resource.spi.ManagedConnectionFactory#setLogWriter(java.io.PrintWriter)
     */
    @Override
    public void setLogWriter(PrintWriter printwriter) throws ResourceException
    {
    }

    /**
     * @param filename
     */
    public void setFileName(String filename)
    {
        this.fileName = filename;
    }

}

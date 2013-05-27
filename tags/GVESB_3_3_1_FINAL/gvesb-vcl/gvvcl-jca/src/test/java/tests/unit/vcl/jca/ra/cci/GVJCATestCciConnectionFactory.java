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
package tests.unit.vcl.jca.ra.cci;

import java.io.Serializable;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

/**
 * @version 3.0.0 Mar 23, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class GVJCATestCciConnectionFactory implements ConnectionFactory, ResourceAdapter, Serializable, Referenceable
{
    private static final long        serialVersionUID = 210L;
    private ConnectionManager        connectionManager;
    private ManagedConnectionFactory managedConnectionFactory;
    private Reference                reference;

    /**
     *
     */
    public GVJCATestCciConnectionFactory()
    {
    }

    /**
     * Constructor
     *
     * @param mcf
     * @param cm
     */
    public GVJCATestCciConnectionFactory(ManagedConnectionFactory mcf, ConnectionManager cm)
    {
        managedConnectionFactory = mcf;
        connectionManager = cm;
    }

    /**
     * @see javax.resource.cci.ConnectionFactory#getConnection()
     */
    @Override
    public Connection getConnection() throws ResourceException
    {
        if (connectionManager == null) {
            throw new ResourceException("Connection Manager IS null");
        }

        Connection con = (Connection) connectionManager.allocateConnection(managedConnectionFactory, null);

        if (con == null) {
            throw new ResourceException("The Application Server cannot allocate a new connection");
        }

        return con;
    }

    /**
     * @see javax.resource.cci.ConnectionFactory#getConnection(javax.resource.cci.ConnectionSpec)
     */
    @Override
    public Connection getConnection(ConnectionSpec connectionspec) throws ResourceException
    {
        if (connectionManager == null) {
            throw new ResourceException("Connection Manager IS null");
        }

        Connection con = (Connection) connectionManager.allocateConnection(managedConnectionFactory, null);

        if (con == null) {
            throw new ResourceException("The Application Server cannot allocate a new connection");
        }

        return con;
    }

    /**
     * @see javax.resource.cci.ConnectionFactory#getMetaData()
     */
    @Override
    public ResourceAdapterMetaData getMetaData() throws ResourceException
    {
        return new GVJCATestCciResourceAdapterMetaData();
    }

    /**
     * @see javax.resource.cci.ConnectionFactory#getRecordFactory()
     */
    @Override
    public RecordFactory getRecordFactory() throws ResourceException
    {
        return new GVJCATestCciRecordFactory();
    }

    /**
     * @see javax.resource.Referenceable#setReference(javax.naming.Reference)
     */
    @Override
    public void setReference(Reference reference)
    {
        this.reference = reference;
    }

    /**
     * @see javax.naming.Referenceable#getReference()
     */
    @Override
    public Reference getReference() throws NamingException
    {
        return reference;
    }

    /**
     * @see javax.resource.spi.ResourceAdapter#endpointActivation(javax.resource.spi.endpoint.MessageEndpointFactory,
     *      javax.resource.spi.ActivationSpec)
     */
    @Override
    public void endpointActivation(MessageEndpointFactory arg0, ActivationSpec arg1) throws ResourceException
    {
    }

    /**
     * @see javax.resource.spi.ResourceAdapter#endpointDeactivation(javax.resource.spi.endpoint.MessageEndpointFactory,
     *      javax.resource.spi.ActivationSpec)
     */
    @Override
    public void endpointDeactivation(MessageEndpointFactory arg0, ActivationSpec arg1)
    {
    }

    /**
     * @see javax.resource.spi.ResourceAdapter#getXAResources(javax.resource.spi.ActivationSpec[])
     */
    @Override
    public XAResource[] getXAResources(ActivationSpec[] arg0) throws ResourceException
    {
        return new XAResource[0];
    }

    /**
     * @see javax.resource.spi.ResourceAdapter#start(javax.resource.spi.BootstrapContext)
     */
    @Override
    public void start(BootstrapContext arg0) throws ResourceAdapterInternalException
    {
    }

    /**
     * @see javax.resource.spi.ResourceAdapter#stop()
     */
    @Override
    public void stop()
    {
    }

}

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

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.Interaction;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.ResultSetInfo;

import tests.unit.vcl.jca.ra.GVJCATestManagedConnection;

/**
 * @version 3.0.0 Mar 23, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class GVJCATestCciConnection implements Connection
{
    private GVJCATestManagedConnection managedConnection;

    /**
     * @param mc
     */
    public GVJCATestCciConnection(GVJCATestManagedConnection mc)
    {
        managedConnection = mc;
    }

    /**
     * @see javax.resource.cci.Connection#close()
     */
    @Override
    public void close() throws ResourceException
    {
        if (managedConnection != null) {
            managedConnection.destroy();
        }
    }

    /**
     * @see javax.resource.cci.Connection#createInteraction()
     */
    @Override
    public Interaction createInteraction() throws ResourceException
    {
        return new GVJCATestCciInteraction(this);
    }

    /**
     * @see javax.resource.cci.Connection#getLocalTransaction()
     */
    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException
    {
        throw new ResourceException("Local transactions are NOT SUPPORTED");
    }

    /**
     * @see javax.resource.cci.Connection#getMetaData()
     */
    @Override
    public ConnectionMetaData getMetaData() throws ResourceException
    {
        return new GVJCATestCciConnectionMetaData();
    }

    /**
     * @see javax.resource.cci.Connection#getResultSetInfo()
     */
    @Override
    public ResultSetInfo getResultSetInfo() throws ResourceException
    {
        throw new NotSupportedException("ResultSet is not supported.");
    }

    /**
     * @return the managed connection
     */
    public GVJCATestManagedConnection getManagedConnection()
    {
        return managedConnection;
    }

}

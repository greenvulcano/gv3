/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.scheduler.util.quartz.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.quartz.utils.ConnectionProvider;

import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;

/**
 * @version 3.2.0 20/nov/2011
 * @author GreenVulcano Developer Team
 */
public class GVQuartzConnectionProvider implements ConnectionProvider
{
    private String jdbcConnectionName = null;
    private List<Connection> connections = new ArrayList<Connection>();

    public GVQuartzConnectionProvider(String jdbcConnectionName)
    {
        this.jdbcConnectionName = jdbcConnectionName;
    }

    /* (non-Javadoc)
     * @see org.quartz.utils.ConnectionProvider#getConnection()
     */
    @Override
    public Connection getConnection() throws SQLException
    {
        try {
            Connection conn = JDBCConnectionBuilder.getConnection(this.jdbcConnectionName);
            this.connections.add(conn);
            return conn;
        }
        catch (Exception exc) {
            throw new SQLException(exc);
        }
    }

    /* (non-Javadoc)
     * @see org.quartz.utils.ConnectionProvider#shutdown()
     */
    @Override
    public void shutdown() throws SQLException
    {
        try {
            while (!this.connections.isEmpty()) {
                Connection conn = this.connections.remove(0);
                JDBCConnectionBuilder.releaseConnection(this.jdbcConnectionName, conn);
            }
        }
        catch (Exception exc) {
            throw new SQLException(exc);
        }
    }

}

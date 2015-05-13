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
package it.greenvulcano.gvesb.gvnet.publisher.pool;

import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.gvnet.NetAdapterException;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manage connection to external servers. A connection can be:
 * - pooled : a connection used only by a client at a time, the using client 
 *            must release the connection when not needed 
 * - shared : a connection shared by many client, the using client must 
 *            synchronize connection access
 *
 * @version 3.5.0 26/mag/2014
 * @author GreenVulcano Developer Team
 *
 */
public class ConnectionPool implements ShutdownEventListener
{
    private static ConnectionPool          instance = null;
    private Map<String, Queue<Connection>> pools    = new ConcurrentHashMap<String, Queue<Connection>>();
    private Map<String, Connection>        shares   = new ConcurrentHashMap<String, Connection>();

    public static ConnectionPool instance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    /**
     * Return a pooled connection to the given host:port.
     *
     * @param host
     * @param port
     * @return
     * @throws NetAdapterException
     */
    public Connection getConnection(String host, int port) throws NetAdapterException {
        return getConnection(host, port, true);
    }

    /**
     * Return a Connection to the given host:port, pooled or shared.
     *
     * @param host
     * @param port
     * @param pooled
     * @return
     * @throws NetAdapterException
     */
    public Connection getConnection(String host, int port, boolean pooled) throws NetAdapterException {
        return getConnection(host, port, 30000, pooled);
    }

    /**
     * Return a Connection to the given host:port, pooled or shared.
     *
     * @param host
     * @param port
     * @param soTimeout
     * @param pooled
     * @return
     * @throws NetAdapterException
     */
    public Connection getConnection(String host, int port, int soTimeout, boolean pooled) throws NetAdapterException {
        Connection conn = null;
        String key = getKey(host, port);
        if (pooled) {
            Queue<Connection> pool = pools.get(key);
            if (pool == null) {
                pool = new ConcurrentLinkedQueue<Connection>();
                pools.put(key, pool);
            }
            conn = pool.poll();
            if (conn == null) {
                conn = new TCPConnection(host, port, soTimeout, true);
            }
        }
        else {
            conn = shares.get(key);
            if (conn == null) {
                conn = new TCPConnection(host, port, soTimeout, false);
                shares.put(key, conn);
            }
        }
        return conn;
    }

    public void releaseConnection(Connection conn) throws NetAdapterException {
        if (conn == null) return;

        if (conn.isPooled()) {
            if (conn.isValid()) {
                String key = getKey(conn.getHost(), conn.getPort());
                Queue<Connection> pool = pools.get(key);
                if (pool != null) {
                    pool.add(conn);
                    return;
                }
            }
            conn.close();
        }
        else {
            if (!conn.isValid()) {
                String key = getKey(conn.getHost(), conn.getPort());
                conn.close();
                shares.remove(key);
            }
        }
    }
    
    public void invalidatePool(String host, int port) {
        String key = getKey(host, port);
        Queue<Connection> pool = pools.remove(key);
        if (pool != null) {
            for (Connection conn : pool) {
                conn.close();
            }
            pool.clear();
        }
    }

    @Override
    public void shutdownStarted(ShutdownEvent event) {
        for (Map.Entry<String, Queue<Connection>> el : pools.entrySet()) {
            for (Connection conn : el.getValue()) {
                conn.close();
            }
            el.getValue().clear();
        }
        pools.clear();

        for (Map.Entry<String, Connection> el : shares.entrySet()) {
            el.getValue().close();
        }
        shares.clear();
    }

    private String getKey(String host, int port) {
        return host + ":" + port;
    }
}

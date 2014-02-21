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
package it.greenvulcano.gvesb.virtual.pop.uidcache;

import it.greenvulcano.log.GVLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Keep track of read message's UID in memory.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class MemoryUIDCacheManager implements UIDCacheManager
{
    private static final Logger          logger                = GVLogger.getLogger(MemoryUIDCacheManager.class);

    private static MemoryUIDCacheManager    instance              = null;

    private HashMap<String, Set<String>> popCacheMap           = new HashMap<String, Set<String>>();


    public static synchronized MemoryUIDCacheManager instance() throws Exception
    {
        if (instance == null) {
            instance = new MemoryUIDCacheManager();
        }
        return instance;
    }

    public synchronized UIDCache getUIDCache(String key) throws Exception
    {
        return new MemoryUIDCache(key, getUIDCacheInt(key));
    }

    public MemoryUIDCacheManager()
    {
        // do nothing
    }

    @SuppressWarnings("unchecked")
    private Set<String> getUIDCacheInt(String key) throws Exception
    {
        Set<String> uidCache = popCacheMap.get(key);

        if (uidCache == null) {
            logger.debug("Init POP Cache [" + key + "]");
            uidCache = new HashSet<String>();
            popCacheMap.put(key, uidCache);
        }

        return uidCache;
    }
}

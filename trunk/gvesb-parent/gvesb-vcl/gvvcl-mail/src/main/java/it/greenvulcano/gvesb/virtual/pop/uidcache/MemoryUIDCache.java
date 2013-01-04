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

import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Fachade for UID Cache.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class MemoryUIDCache implements UIDCache
{
    private static final Logger   logger  = GVLogger.getLogger(MemoryUIDCache.class);

    private String                key     = null;
    private Set<String>           cache   = null;

    public MemoryUIDCache(String key, Set<String> cache)
    {
        this.key = key;
        this.cache = cache;
    }

    public boolean contains(String uid)
    {
        boolean found = cache.contains(uid);
        if (found) {
            logger.debug("Found UID [" + uid + "] in Cache [" + key + "]");
        }
        return found;
    }

    public void add(String uid)
    {
        logger.debug("Writing UID [" + uid + "] in Cache [" + key + "]");
        cache.add(uid);
    }
}

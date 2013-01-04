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

import org.apache.log4j.Logger;

/**
 * Instantiate the singleton Cache Manager.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public final class UIDCacheManagerFactory
{
    private static final Logger      logger                  = GVLogger.getLogger(UIDCacheManagerFactory.class);

    private static final String      POP_CACHE_CLASS_NAME    = "gv.pop.uidcache.manager";
    private static final String      POP_CACHE_CLASS_DEFAULT = "it.greenvulcano.gvesb.virtual.pop.uidcache.FileUIDCacheManager";
    private static UIDCacheManager   instance                = null;

    private static String            popCacheClass           = System.getProperty(POP_CACHE_CLASS_NAME, POP_CACHE_CLASS_DEFAULT);


    public static synchronized UIDCacheManager getInstance() throws Exception
    {
        if (instance == null) {
            try {
                logger.debug("Initializing UIDCacheManager [" + popCacheClass + "]");
                instance = (UIDCacheManager) Class.forName(popCacheClass).newInstance();
            }
            catch (Exception exc) {
                logger.error("Error Initializing UIDCacheManager [" + popCacheClass + "]", exc);
                throw exc;
            }
        }
        return instance;
    }

}

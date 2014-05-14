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
package it.greenvulcano.gvesb.gvconsole.monitoring.action;

import it.greenvulcano.gvesb.gvconsole.monitoring.domain.ThreadInfo;
import it.greenvulcano.gvesb.gvconsole.monitoring.service.ThreadUsageService;
import it.greenvulcano.gvesb.gvconsole.monitoring.service.ThreadsUsageServiceImpl;
import it.greenvulcano.log.GVLogger;

import javax.management.MBeanServerConnection;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;

/**
 * @version 3.0.0 Apr 12, 2010
 * @author Angelo
 *
 */
public class ThreadAction extends GeneralAction
{

    private static final long     serialVersionUID = 300L;
    private static final Logger   logger           = GVLogger.getLogger(ThreadAction.class);

    private MBeanServerConnection mBeanServerConnection;

    private ThreadUsageService    threadUsageService;

    public JSONArray execute(String server)
    {
        ThreadInfo threadInfo = null;
        try {
            mBeanServerConnection = getmBeanServerConnection(server);
            threadUsageService = new ThreadsUsageServiceImpl(mBeanServerConnection);

            threadInfo = threadUsageService.getThreadInfo();

            return JSONArray.fromObject(threadInfo);
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

}

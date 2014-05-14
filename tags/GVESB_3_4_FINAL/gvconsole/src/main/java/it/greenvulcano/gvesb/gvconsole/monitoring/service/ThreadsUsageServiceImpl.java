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
package it.greenvulcano.gvesb.gvconsole.monitoring.service;

import static java.lang.management.ManagementFactory.THREAD_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.getThreadMXBean;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;
import it.greenvulcano.gvesb.gvconsole.monitoring.domain.ThreadInfo;

import java.lang.management.ThreadMXBean;

import javax.management.MBeanServerConnection;

/**
 * @version 3.0.0 Apr 12, 2010
 * @author Angelo
 *
 */
public class ThreadsUsageServiceImpl implements ThreadUsageService
{
    private ThreadMXBean threadMXBean = null;

    /**
     *
     */
    public ThreadsUsageServiceImpl()
    {
        threadMXBean = getThreadMXBean();
    }

    /**
     * @param mBeanServerConnection
     * @throws Exception
     */
    public ThreadsUsageServiceImpl(MBeanServerConnection mBeanServerConnection) throws Exception
    {
        this.threadMXBean = (ThreadMXBean) newPlatformMXBeanProxy(mBeanServerConnection, THREAD_MXBEAN_NAME,
                ThreadMXBean.class);
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.monitoring.service.ThreadUsageService#getThreadInfo()
     */
    @Override
    public ThreadInfo getThreadInfo()
    {
        ThreadInfo threadInfo = new ThreadInfo();

        // Returns the current number of live threads including both daemon and
        // non-daemon threads.
        threadInfo.setTotalThread(threadMXBean.getThreadCount());

        // Returns the current number of live daemon threads.
        threadInfo.setLiveThread(threadMXBean.getDaemonThreadCount());

        // Returns the peak live thread count since the Java virtual machine
        // started or peak was reset.
        threadInfo.setPeakThread(threadMXBean.getPeakThreadCount());

        return threadInfo;
    }

}

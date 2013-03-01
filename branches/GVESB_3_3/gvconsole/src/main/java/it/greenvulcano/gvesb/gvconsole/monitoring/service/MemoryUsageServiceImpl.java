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

import static java.lang.management.ManagementFactory.MEMORY_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.getMemoryMXBean;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;
import it.greenvulcano.gvesb.gvconsole.monitoring.domain.HeapMemory;
import it.greenvulcano.gvesb.gvconsole.monitoring.domain.NotHeapMemory;
import it.greenvulcano.gvesb.gvconsole.monitoring.domain.ObjectPendingFinalizationCount;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import javax.management.MBeanServerConnection;

import org.apache.log4j.Logger;

/**
 * @version 3.0.0 Apr 12, 2010
 * @author Angelo
 *
 */
public class MemoryUsageServiceImpl implements MemoryUsageService
{
    private static final Logger logger = Logger.getLogger(MemoryUsageServiceImpl.class);
    private MemoryMXBean        mbean  = null;

    /**
     *
     */
    public MemoryUsageServiceImpl()
    {
        this.mbean = getMemoryMXBean();
    }

    /**
     * @param mBeanServerConnection
     * @throws Exception
     */
    public MemoryUsageServiceImpl(MBeanServerConnection mBeanServerConnection) throws Exception
    {
        this.mbean = (MemoryMXBean) newPlatformMXBeanProxy(mBeanServerConnection, MEMORY_MXBEAN_NAME,
                MemoryMXBean.class);
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.monitoring.service.MemoryUsageService#getHeapInfoMemoryUsage()
     */
    @Override
    public HeapMemory getHeapInfoMemoryUsage()
    {
        logger.debug("method: getHeapInfoMemoryUsage");
        HeapMemory heapMemory = new HeapMemory();
        MemoryUsage memoryHeapUsage = mbean.getHeapMemoryUsage();

        heapMemory.setHeapMemoryInit(memoryHeapUsage.getInit());
        heapMemory.setHeapMemoryCommitted(memoryHeapUsage.getCommitted());
        heapMemory.setHeapMemoryUsed(memoryHeapUsage.getUsed());
        heapMemory.setHeapMemoryMax(memoryHeapUsage.getMax());

        return heapMemory;
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.monitoring.service.MemoryUsageService#getNotHeapInfoMemoryUsage()
     */
    public NotHeapMemory getNotHeapInfoMemoryUsage()
    {
        logger.debug("method: getNotHeapInfoMemoryUsage");
        NotHeapMemory notHeapMemory = new NotHeapMemory();
        MemoryUsage memoryHeapUsage = mbean.getNonHeapMemoryUsage();

        notHeapMemory.setHeapMemoryInit(memoryHeapUsage.getInit());
        notHeapMemory.setHeapMemoryCommitted(memoryHeapUsage.getCommitted());
        notHeapMemory.setHeapMemoryUsed(memoryHeapUsage.getUsed());
        notHeapMemory.setHeapMemoryMax(memoryHeapUsage.getMax());

        return notHeapMemory;
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.monitoring.service.MemoryUsageService#getObjectPendingFinalizationCount()
     */
    public ObjectPendingFinalizationCount getObjectPendingFinalizationCount()
    {
        logger.debug("method: getObjectPendingFinalizationCount");
        ObjectPendingFinalizationCount objectPendingFinalizationCount = new ObjectPendingFinalizationCount();

        objectPendingFinalizationCount.setNumberObjectPendingFinalizationCount(mbean.getObjectPendingFinalizationCount());
        return objectPendingFinalizationCount;
    }

}

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

import static java.lang.management.ManagementFactory.RUNTIME_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;
import it.greenvulcano.gvesb.gvconsole.monitoring.domain.CPUInfo;

import java.io.IOException;
import java.lang.management.RuntimeMXBean;

import javax.management.MBeanServerConnection;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author Angelo
 *
 */
public class RuntimeServiceImpl implements RuntimeService
{

    private RuntimeMXBean bean;

    /**
     *
     */
    public RuntimeServiceImpl()
    {
        bean = getRuntimeMXBean();
    }

    /**
     * @param mBeanServerConnection
     * @throws IOException
     */
    public RuntimeServiceImpl(MBeanServerConnection mBeanServerConnection) throws IOException
    {
        this.bean = (RuntimeMXBean) newPlatformMXBeanProxy(mBeanServerConnection, RUNTIME_MXBEAN_NAME,
                RuntimeMXBean.class);
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.monitoring.service.RuntimeService#getCpuInfo(long, int)
     */
    @Override
    public CPUInfo getCpuInfo(long processCpuTime, int availableProcessors)
    {
        CPUInfo cpuInfo = new CPUInfo();
        cpuInfo.setUpTime(bean.getUptime());
        cpuInfo.setProcessCpuTime(processCpuTime);
        if (CPUInfo.prevUpTime > 0L && cpuInfo.getUpTime() > CPUInfo.prevUpTime) {
            // elapsedCpu is in ns and elapsedTime is in ms.
            long elapsedCpu = cpuInfo.getProcessCpuTime() - CPUInfo.prevProcessCpuTime;
            long elapsedTime = cpuInfo.getUpTime() - CPUInfo.prevUpTime;
            // cpuUsage could go higher than 100% because elapsedTime and
            // elapsedCpu are not fetched simultaneously. Limit to 99% to avoid
            // Plotter showing a scale from 0% to 200%.
            float cpuUsage = Math.min(99F, elapsedCpu / (elapsedTime * 10000F * availableProcessors));
            cpuInfo.setCpuUsage(Math.round(cpuUsage * Math.pow(10.0, 1)));
        }
        else {
            cpuInfo.setCpuUsage(0);
        }
        CPUInfo.prevUpTime = cpuInfo.getUpTime();
        CPUInfo.prevProcessCpuTime = cpuInfo.getProcessCpuTime();

        return cpuInfo;
    }

}

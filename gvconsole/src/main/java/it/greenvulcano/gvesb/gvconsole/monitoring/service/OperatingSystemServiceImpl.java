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

import static java.lang.management.ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;
import it.greenvulcano.gvesb.gvconsole.monitoring.domain.OperatingSystemInfo;

import java.io.IOException;
import java.lang.management.OperatingSystemMXBean;

import javax.management.MBeanServerConnection;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author Angelo
 *
 */
public class OperatingSystemServiceImpl implements OperatingSystemService
{

    private OperatingSystemMXBean mBean;

    /**
     *
     */
    public OperatingSystemServiceImpl()
    {
        mBean = getOperatingSystemMXBean();
    }

    /**
     * @param mBeanServerConnection
     * @throws IOException
     */
    public OperatingSystemServiceImpl(MBeanServerConnection mBeanServerConnection) throws IOException
    {
        mBean = newPlatformMXBeanProxy(mBeanServerConnection, OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.monitoring.service.OperatingSystemService#getOperatingSystemInfo()
     */
    @Override
    public OperatingSystemInfo getOperatingSystemInfo()
    {
        OperatingSystemInfo operatingSystemInfo = new OperatingSystemInfo();
        operatingSystemInfo.setAvailableProcessors(mBean.getAvailableProcessors());
        return operatingSystemInfo;
    }

}

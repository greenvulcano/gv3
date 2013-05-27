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

import it.greenvulcano.gvesb.gvconsole.monitoring.domain.CPUInfo;
import it.greenvulcano.gvesb.gvconsole.monitoring.domain.OperatingSystemInfo;
import it.greenvulcano.gvesb.gvconsole.monitoring.domain.SunOperatingSystemInfo;
import it.greenvulcano.gvesb.gvconsole.monitoring.service.OperatingSystemService;
import it.greenvulcano.gvesb.gvconsole.monitoring.service.OperatingSystemServiceImpl;
import it.greenvulcano.gvesb.gvconsole.monitoring.service.RuntimeService;
import it.greenvulcano.gvesb.gvconsole.monitoring.service.RuntimeServiceImpl;
import it.greenvulcano.gvesb.gvconsole.monitoring.service.SunOperatingSystemService;
import it.greenvulcano.gvesb.gvconsole.monitoring.service.SunOperatingSystemServiceImpl;

import javax.management.MBeanServerConnection;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;

/**
 * @version 3.0.0 Apr 12, 2010
 * @author Angelo
 *
 */
public class CpuAction extends GeneralAction
{

    private static final long         serialVersionUID = 300L;
    private static final Logger       logger           = Logger.getLogger(CpuAction.class);

    private MBeanServerConnection     mBeanServerConnection;

    private OperatingSystemService    operatingSystemService;
    private SunOperatingSystemService sunOperatingSystemService;
    private RuntimeService            runtimeService;

    public JSONArray execute(String server)
    {
        OperatingSystemInfo operatingSystemInfo = null;
        SunOperatingSystemInfo sunOperatingSystemInfo = null;
        CPUInfo cpuInfo = null;

        try {
            mBeanServerConnection = getmBeanServerConnection(server);
            operatingSystemService = new OperatingSystemServiceImpl(mBeanServerConnection);
            sunOperatingSystemService = new SunOperatingSystemServiceImpl(mBeanServerConnection);
            runtimeService = new RuntimeServiceImpl(mBeanServerConnection);

            operatingSystemInfo = operatingSystemService.getOperatingSystemInfo();
            sunOperatingSystemInfo = sunOperatingSystemService.getSunOperatingSystem();
            cpuInfo = runtimeService.getCpuInfo(sunOperatingSystemInfo.getProcessCpuTime(),
                    operatingSystemInfo.getAvailableProcessors());

            return JSONArray.fromObject(cpuInfo);
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

}

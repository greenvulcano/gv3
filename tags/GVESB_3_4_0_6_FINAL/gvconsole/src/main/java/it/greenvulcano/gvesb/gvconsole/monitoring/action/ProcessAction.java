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

import it.greenvulcano.gvesb.gvconsole.monitoring.domain.MonitoredProcess;
import it.greenvulcano.gvesb.gvconsole.monitoring.service.MonitoredProcessServiceImpl;
import it.greenvulcano.log.GVLogger;

import java.util.List;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;

/**
 * @version 3.0.0 Apr 12, 2010
 * @author Angelo
 *
 */
public class ProcessAction extends GeneralAction
{

    private static final long           serialVersionUID = 300L;
    private static final Logger         logger           = GVLogger.getLogger(ProcessAction.class);

    private MonitoredProcessServiceImpl monitoredProcessServiceImpl;
    private List<MonitoredProcess>      monitoredProcesses;

    public JSONArray execute(String server)
    {
        monitoredProcessServiceImpl = new MonitoredProcessServiceImpl();
        try {
            monitoredProcesses = monitoredProcessServiceImpl.getMonitoredProcesses();
            return JSONArray.fromObject(monitoredProcesses);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}

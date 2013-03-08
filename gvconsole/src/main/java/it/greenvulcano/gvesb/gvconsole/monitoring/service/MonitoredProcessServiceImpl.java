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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvconsole.monitoring.domain.MonitoredProcess;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Apr 12, 2010
 * @author Angelo
 *
 */
public class MonitoredProcessServiceImpl implements MonitoredProcessService
{

    private static final Logger logger = Logger.getLogger(MonitoredProcessServiceImpl.class);

    /**
     * @see it.greenvulcano.gvesb.gvconsole.monitoring.service.MonitoredProcessService#getMonitoredProcesses()
     */
    @Override
    public List<MonitoredProcess> getMonitoredProcesses() throws Exception
    {
        logger.debug("method: getMonitoredProcesses");
        List<MonitoredProcess> mpList = new ArrayList<MonitoredProcess>();
        NodeList nodeList = XMLConfig.getNodeList("GVMonitoringConfig.xml",
                "/GVMonitoringConfig/MonitoredProcesses/MonitoredProcess");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            MonitoredProcess mp = new MonitoredProcess();
            mp.setName(element.getAttribute("name"));
            mp.setUrl(element.getAttribute("url"));
            mp.setUser(element.getAttribute("user"));
            mp.setPassword(element.getAttribute("password"));
            logger.debug(mp);
            mpList.add(mp);
        }

        return mpList;
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.monitoring.service.MonitoredProcessService#getMonitoredProcess(java.lang.String)
     */
    @Override
    public MonitoredProcess getMonitoredProcess(String name) throws Exception
    {
        logger.debug("method: getMonitoredProcess(" + name + ")");
        NodeList nodeList = XMLConfig.getNodeList("GVMonitoringConfig.xml",
                "/GVMonitoringConfig/MonitoredProcesses/MonitoredProcess[@name='" + name + "']");
        Element element = (Element) nodeList.item(0);
        MonitoredProcess mp = new MonitoredProcess();
        mp.setName(element.getAttribute("name"));
        mp.setUrl(element.getAttribute("url"));
        mp.setUser(element.getAttribute("user"));
        mp.setPassword(element.getAttribute("password"));
        logger.debug(mp);
        return mp;
    }

}

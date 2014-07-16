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
import it.greenvulcano.gvesb.gvconsole.monitoring.service.MonitoredProcessService;
import it.greenvulcano.gvesb.gvconsole.monitoring.service.MonitoredProcessServiceImpl;
import it.greenvulcano.log.GVLogger;

import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * @version 3.0.0 Apr 12, 2010
 * @author Angelo
 *
 */
public abstract class GeneralAction extends Action
{

    private static final long   serialVersionUID = 300L;
    private static final Logger logger           = GVLogger.getLogger(GeneralAction.class);

    /**
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        logger.debug("method: execute");
        String server = request.getParameter("server");

        JSONArray jsonArray = execute(server);
        logger.debug("jsonArray:\n" + jsonArray);
        if (jsonArray != null) {
            String jsonString = jsonArray.toString();

            PrintWriter out = response.getWriter();
            out.println(jsonString);
            out.flush();
        }
        return null;
    }

    public abstract JSONArray execute(String request);

    /**
     * @param server
     * @return the <code>MBeanServerConnection</code>
     * @throws Exception
     */
    public MBeanServerConnection getmBeanServerConnection(String server) throws Exception
    {
        MBeanServerConnection mBeanServerConnection = null;
        MonitoredProcessService monitoredProcessService;
        monitoredProcessService = new MonitoredProcessServiceImpl();
        MonitoredProcess monitoredProcess = monitoredProcessService.getMonitoredProcess(server);
        String monitoredURL = monitoredProcess.getUrl();
        if (monitoredURL == null || monitoredURL.isEmpty()) {
            return ManagementFactory.getPlatformMBeanServer();
        }
        JMXServiceURL jmxserviceurl = new JMXServiceURL(monitoredURL);
        JMXConnector connect = null;
        Map<String, String[]> env = null;

        if (monitoredProcess.getUser().isEmpty() || monitoredProcess.getPassword().isEmpty()
                || monitoredProcess.getUser() == null || monitoredProcess.getPassword() == null) {
            connect = JMXConnectorFactory.connect(jmxserviceurl);
        }
        else {
            env = new HashMap<String, String[]>();
            env.put(JMXConnector.CREDENTIALS, new String[]{monitoredProcess.getUser(), monitoredProcess.getPassword()});
            connect = JMXConnectorFactory.connect(jmxserviceurl, env);
        }
        mBeanServerConnection = connect.getMBeanServerConnection();
        return mBeanServerConnection;
    }

}

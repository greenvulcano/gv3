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
package it.greenvulcano.gvesb.gvconsole.workbench.action;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.log.GVLogger;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Apr 12, 2010
 * @author Angelo
 * 
 */
public class TestingAction extends Action
{
    private static final Logger logger = GVLogger.getLogger(TestingAction.class);

    /**
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception
            {
        String actionName = mapping.getName();
        PrintWriter out = response.getWriter();
        if ("loadServices".equals(actionName)) {
            out.println(loadServices(request));
        }
        else if ("loadSystems".equals(actionName)) {
            out.println(loadSystems(request));
        }
        out.flush();
        return null;
            }

    /**
     * @return list of systems in "json" array
     */
    public String loadSystems(HttpServletRequest request)
    {
        logger.debug("Start loadSystems");

        List<String> systems = new ArrayList<String>();
        systems.add(GVBuffer.DEFAULT_SYS);

        try {
            NodeList systemList = XMLConfig.getNodeList("GVSystems.xml", "/GVSystems/Systems/System");
            if ((systemList != null) && (systemList.getLength() > 0)) {
                for (int index = 0; index < systemList.getLength(); index++) {
                    systems.add(XMLConfig.get(systemList.item(index), "@id-system"));
                }
            }
            else {
                systems.add("ERROR: No system found");
            }
        }
        catch (XMLConfigException e) {
            systems.add("ERROR: " + e.getMessage());
        }

        logger.debug("End loadSystems");
        return sendJsonResponse(systems);
    }

    /**
     * @return list of services in "json" array
     */
    public String loadServices(HttpServletRequest request)
    {
        logger.debug("Start loadServices");

        List<String> services = new ArrayList<String>();

        try {
            NodeList servicesList = XMLConfig.getNodeList("GVServices.xml", "/GVServices/Services/Service");
            if ((servicesList != null) && (servicesList.getLength() > 0)) {
                for (int index = 0; index < servicesList.getLength(); index++) {
                    services.add(XMLConfig.get(servicesList.item(index), "@id-service"));
                }
            }
            else {
                services.add("ERROR: No service found");
            }
        }
        catch (XMLConfigException e) {
            services.add("ERROR: " + e.getMessage());
        }

        logger.debug("End loadServices");
        return sendJsonResponse(services);
    }

    private String sendJsonResponse(Object obj)
    {
        logger.debug("Start sendJsonResponse");
        JSONArray jsonArray = JSONArray.fromObject(obj);
        logger.debug("jsonArray:\n" + jsonArray);
        logger.debug("End sendJsonResponse");
        return jsonArray.toString();
    }
}

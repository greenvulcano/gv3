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
package it.greenvulcano.gvesb.gvconsole.webservice.action;

import it.greenvulcano.gvesb.gvconsole.webservice.utils.ActionResult;
import it.greenvulcano.gvesb.gvconsole.webservice.wsdl.WSDLInfoBean;
import it.greenvulcano.gvesb.virtual.ws.monitoring.WSDLManagerProxy;
import it.greenvulcano.jmx.JMXEntryPoint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class SubmitWSDLAction extends Action
{
    /**
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public ActionForward execute(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {

        WSDLInfoBean myForm = (WSDLInfoBean) actionForm;
        if (myForm.getReload().equals("reload")) {

            if (request.getParameterValues("selectedItems") != null
                    && request.getParameterValues("selectedNodes") != null) {
                String selectedFiles[] = request.getParameterValues("selectedItems");
                String selectedNodes[] = request.getParameterValues("selectedNodes");
                ActionResult result = new ActionResult();

                JMXEntryPoint jmx = JMXEntryPoint.instance();
                MBeanServer server = jmx.getServer();
                Object params[] = new Object[1];
                String signature[] = new String[]{"java.lang.String"};

                // Mappa dei parametri
                // dell'operazione da inserire nel security log
                Map<String, String> selected = new HashMap<String, String>();

                for (int j = 0; j < selectedNodes.length; j++) {
                    // Parametro per il Security Log
                    // contenente il server
                    selected.put("selectedNode", selectedNodes[j]);
                    try {
                        Set<?> set = server.queryNames(new ObjectName(WSDLManagerProxy.JMX_FILTER + ",Location="
                                + selectedNodes[j]), null);
                        Iterator<?> iterator = set.iterator();
                        ObjectName objectName = (ObjectName) iterator.next();
                        for (int i = 0; i < selectedFiles.length; i++) {
                            // Parametro per il Security Log
                            // contenente il file
                            selected.put("selectedFile", selectedFiles[i]);
                            try {
                                params[0] = selectedFiles[i];
                                server.invoke(objectName, "reloadLocal", params, signature);
                                result.addDetails(selectedNodes[j], true, "Reload " + selectedFiles[i], null);
                            }
                            catch (Exception e) {
                                result.addDetails(selectedNodes[j], false, "Reload " + selectedFiles[i], e);
                            }
                        }
                    }
                    catch (Exception e) {
                        result.addDetails(selectedNodes[j], false, "Reload server non attivo", e);
                    }
                }

                request.setAttribute("operationResult", result);
            }
        }
        else if (myForm.getReload().equals("reset")) {

        }


        return actionMapping.getInputForward();
    }
}
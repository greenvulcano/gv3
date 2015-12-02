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
package it.greenvulcano.gvesb.gvconsole.savepoint;

import formdef.plugin.util.FormUtils;
import it.greenvulcano.birt.report.ReportManager;
import it.greenvulcano.gvesb.datahandling.utils.dao.DataAccessObject;
import it.greenvulcano.gvesb.gvconsole.deploy.action.StrutsExportAction;
import it.greenvulcano.log.GVLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.util.LabelValueBean;

/**
 *
 * @version 3.1.0 27 Feb 2011
 * @author GreenVulcano Developer Team
 */
public class InitAction extends Action
{
    private static final Logger logger = GVLogger.getLogger(InitAction.class);

    /*
     * (non-Javadoc)
     *
     * @seeorg.apache.struts.action.Action#execute(org.apache.struts.action.
     * ActionMapping, org.apache.struts.action.ActionForm,
     * javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        HttpSession session = request.getSession();

        Map<String, String> params = new HashMap<String, String>();
        params.put("SERVICE", "NULL");
        params.put("ID", "NULL");
        params.put("DATE", "NULL");

        List<LabelValueBean> lstSVC = new ArrayList<LabelValueBean>();
        try {
            String[] svcs = DataAccessObject.getStringArray("ListRecoveryPointServices", params);
            lstSVC.add(new LabelValueBean("", ""));
            for (String svc : svcs) {
                lstSVC.add(new LabelValueBean(svc, svc));
            }
        }
        catch (Exception exc) {
            logger.error("Error reading services list", exc);
        }
        session.setAttribute("listSVC", lstSVC);

        List<LabelValueBean> lstID = new ArrayList<LabelValueBean>();
        try {
            String[] ids = DataAccessObject.getStringArray("ListRecoveryPointIDs", params);
            lstID.add(new LabelValueBean("", ""));
            for (String id : ids) {
                lstID.add(new LabelValueBean(id, id));
            }
        }
        catch (Exception exc) {
            logger.error("Error reading IDs list", exc);
        }
        session.setAttribute("listID", lstID);

        DynaActionForm frm = (DynaActionForm) FormUtils.getInstance().createActionForm("SavePointForm", this, mapping,
                request);
        frm.set("rec_id", "");
        frm.set("id", "");
        frm.set("service", "");
        frm.set("date", "");
        session.setAttribute("SavePointForm", frm);

        return mapping.findForward("home");
    }
}

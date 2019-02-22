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
package it.greenvulcano.gvesb.gvconsole.report;

import it.greenvulcano.birt.report.Parameter;
import it.greenvulcano.birt.report.ReportManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.actions.LookupDispatchAction;
import org.apache.struts.util.LabelValueBean;

/**
 *
 * @version 3.1.0 02 Feb 2011
 * @author GreenVulcano Developer Team
 */
public class ReportAction extends LookupDispatchAction
{
    private HashMap keyMethodMap = new HashMap();

    /**
     *
     */
    public ReportAction()
    {
        keyMethodMap.put("birtreport.setreports", "setReports");
        keyMethodMap.put("birtreport.setparams", "setParams");
    }

    /* (non-Javadoc)
     * @see org.apache.struts.actions.LookupDispatchAction#getKeyMethodMap()
     */
    @Override
    protected Map getKeyMethodMap()
    {
        return keyMethodMap;
    }


    /* (non-Javadoc)
     * @see org.apache.struts.actions.LookupDispatchAction#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public ActionForward setReports(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        DynaActionForm frm = (DynaActionForm) form;
        String grp = frm.getString("group");

        ReportManager rm = ReportManager.instance();
        HttpSession session = request.getSession();

        List<String> lstRep = rm.getReportsName(grp);
        List<LabelValueBean> lstRepLVB = new ArrayList<LabelValueBean>();
        for (String rep : lstRep) {
            lstRepLVB.add(new LabelValueBean(rep, rep));
        }
        String rep = lstRep.get(0);
        String repCfg = rm.getReport(grp, rep).getReportConfig();

        List<Parameter> lstPar = rm.getParams(grp, rep);
        Map<String, String> params = new HashMap<String, String>();
        for (Parameter par : lstPar) {
            par.setData(session, params);
        }

        session.setAttribute("listReport", lstRepLVB);
        session.setAttribute("listParams", lstPar);

        frm.set("group", grp);
        frm.set("report", rep);
        frm.set("reportConfig", repCfg);
        session.setAttribute("BirtReportForm", frm);

        return mapping.findForward("home");
    }

    /* (non-Javadoc)
     * @see org.apache.struts.actions.LookupDispatchAction#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public ActionForward setParams(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        DynaActionForm frm = (DynaActionForm) form;
        String grp = frm.getString("group");
        String rep = frm.getString("report");

        ReportManager rm = ReportManager.instance();
        HttpSession session = request.getSession();

        String repCfg = rm.getReport(grp, rep).getReportConfig();

        List<Parameter> lstPar = rm.getParams(grp, rep);
        Map<String, String> params = new HashMap<String, String>();
        for (Parameter par : lstPar) {
            par.setData(session, params);
        }

        session.setAttribute("listParams", lstPar);

        frm.set("group", grp);
        frm.set("report", rep);
        frm.set("reportConfig", repCfg);
        session.setAttribute("BirtReportForm", frm);

        return mapping.findForward("home");
    }

}

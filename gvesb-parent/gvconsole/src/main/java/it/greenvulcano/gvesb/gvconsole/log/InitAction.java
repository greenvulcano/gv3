/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved. This file is part of GreenVulcano ESB. GreenVulcano ESB is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with GreenVulcano ESB. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.gvconsole.log;

import formdef.plugin.util.FormUtils;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.txt.DateUtils;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 *
 * @version 3.1.0 01/feb/2011
 * @author GreenVulcano Developer Team
 */
public class InitAction extends Action {
    private static final Logger logger = GVLogger.getLogger(InitAction.class);

    /*
     * (non-Javadoc)
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.
     * ActionMapping, org.apache.struts.action.ActionForm,
     * javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {

        HttpSession session = request.getSession();
        session.removeAttribute("iteratorListLog");

        DynaActionForm frm = (DynaActionForm) FormUtils.getInstance().createActionForm("LogViewerForm", this, mapping,
                request);
        Date now = new Date();
        //Date dateFrom = DateUtils.addTime(now, Calendar.HOUR, -1);
        //Date dateTo = DateUtils.addTime(now, Calendar.MINUTE, 5);
        Date dateFrom = DateUtils.addTime(now, Calendar.MINUTE, -10);
        Date dateTo = DateUtils.addTime(now, Calendar.MINUTE, 5);

        frm.set("dateFrom", DateUtils.dateToString(dateFrom, "dd/MM/yyyy HH:mm"));
        frm.set("dateTo", DateUtils.dateToString(dateTo,"dd/MM/yyyy HH:mm"));
        frm.set("date", DateUtils.dateToString(now,"dd/MM/yyyy"));
        session.setAttribute("LogViewerForm", frm);

        return mapping.findForward("home");
    }
}

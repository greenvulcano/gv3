/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.gvconsole.gvcon.property;


import java.util.ArrayList;

import formdef.plugin.util.FormUtils;
import it.greenvulcano.log.GVLogger;

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
 *
 * @version 3.4.0 Dec 17, 2013
 * @author GreenVulcano Developer Team
 */
public class InitAction extends Action
{
    private static final Logger logger = GVLogger.getLogger(InitAction.class);

    /**
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response)
    {
        logger.debug("Begin InitAction");
        HttpSession session = request.getSession(false);
        session.removeAttribute("error");
        session.removeAttribute("warning");
        try {
        	String mode = (String) request.getParameter("mode");
        	logger.debug("mode: " + mode);
        	if((mode!=null) && (mode.equalsIgnoreCase("view"))){
        		session.setAttribute("MODE", mode);
        	}
        	else{
        		session.setAttribute("MODE", "edit");
        	}
        	PropertiesEditor pe = new PropertiesEditor();
        	session.setAttribute("PROPERTIES_EDITOR", pe);
        	
        	PropertiesEditorForm pef = new PropertiesEditorForm();
        	pef.setProperties(pe.getProperties());
        
			DynaActionForm dynaForm = (DynaActionForm) FormUtils.setFormValues("PropertiesEditorForm", pef,
                            this, mapping, request);
        	session.setAttribute("PropertiesEditorForm", dynaForm);
        	logger.debug("Properties form: " + dynaForm);
            logger.debug("End InitAction");
            return mapping.findForward("home");
        }
        catch (Exception exc) {
        	session.setAttribute("error", exc.getMessage());
            logger.error("Exception loading global properties", exc);
            return mapping.findForward("home");
        }

    }

}
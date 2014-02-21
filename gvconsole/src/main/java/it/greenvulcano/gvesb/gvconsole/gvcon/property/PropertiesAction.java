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
package it.greenvulcano.gvesb.gvconsole.gvcon.property;

import formdef.plugin.util.FormUtils;
import it.greenvulcano.log.GVLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.actions.LookupDispatchAction;

/**
 * 
 * @version 3.4.0 Dec 17, 2013
 * @author GreenVulcano Developer Team
 */
public class PropertiesAction extends LookupDispatchAction {
	private static final Logger logger = GVLogger
			.getLogger(PropertiesAction.class);
	private HashMap keyMethodMap = new HashMap();

	/**
	 *
	 */
	public PropertiesAction() {
		keyMethodMap.put("globalprops.revert", "revert");
		keyMethodMap.put("globalprops.save", "save");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.struts.actions.LookupDispatchAction#getKeyMethodMap()
	 */
	@Override
	protected Map getKeyMethodMap() {
		return keyMethodMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.struts.actions.LookupDispatchAction#execute(org.apache.struts
	 * .action.ActionMapping, org.apache.struts.action.ActionForm,
	 * javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward revert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		HttpSession session = request.getSession();
		try {
			logger.debug("ActionForward revert");
			PropertiesEditor pe = new PropertiesEditor();
			session.setAttribute("PROPERTIES_EDITOR", pe);

			PropertiesEditorForm pef = new PropertiesEditorForm();
			pef.setProperties(pe.getProperties());

			DynaActionForm dynaForm = (DynaActionForm) FormUtils.setFormValues(
					"PropertiesEditorForm", pef, this, mapping, request);
			session.setAttribute("PropertiesEditorForm", dynaForm);
			logger.debug("Properties form: " + dynaForm);
			logger.debug("End ActionForward revert");
			return mapping.findForward("home");

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("iteratorListLog",
					new ArrayList<List<String>>());
		}

		return mapping.findForward("home");
	}

	public ActionForward save(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		HttpSession session = request.getSession();
		try {
			logger.debug("ActionForward save");
			logger.debug("dynaForm:   " + form);
			PropertiesEditorForm pef = (PropertiesEditorForm) FormUtils
					.getFormValues(form, this, mapping, request);
			logger.debug("editorForm:   " + pef.toString());
			List<GlobalProperty> props = pef.getProperties();
			PropertiesEditor pe = (PropertiesEditor) session.getAttribute("PROPERTIES_EDITOR");

			pe.setProperties(props);
			pe.saveGlobalProperties();
			
			//Form reloading...
        	pe = new PropertiesEditor();
        	//session.setAttribute("PROPERTIES_EDITOR", pe);
        	
        	pef = new PropertiesEditorForm();
        	pef.setProperties(pe.getProperties());
        
			DynaActionForm dynaForm = (DynaActionForm) FormUtils.setFormValues("PropertiesEditorForm", pef,
                            this, mapping, request);
        	session.setAttribute("PropertiesEditorForm", dynaForm);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return mapping.findForward("home");
	}
}

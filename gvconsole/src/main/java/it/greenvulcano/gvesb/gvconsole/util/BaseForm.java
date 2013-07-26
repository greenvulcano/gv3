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
package it.greenvulcano.gvesb.gvconsole.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;

/**
 * @version 3.1.0 02 Feb 2011
 * @author GreenVulcano Developer Team
 */
public class BaseForm extends DynaValidatorForm {

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		System.out.println("BaseForm - " + request.getParameter("skipValidation"));
		boolean skipValidation = Boolean.valueOf(request.getParameter("skipValidation")).booleanValue();
		if (skipValidation) {
			System.out.println("BaseForm - validation skipped");
			return null;
		}
		return super.validate(mapping, request);
	}

}

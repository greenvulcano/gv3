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
package it.greenvulcano.gvesb.gvconsole.deploy.form;

import org.apache.struts.action.*;


/**
 * Form bean for Struts File Upload.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class StrutsGestFileForm extends ActionForm
{

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;
    private String            servizio         = null;
    private String            adapter          = null;
    private String            support          = null;

    public String getServizio()
    {
        return servizio;
    }

    public void setServizio(String servizio)
    {
        this.servizio = servizio;
    }

    public String getAdapter()
    {
        return adapter;
    }

    public void setAdapter(String adapter)
    {
        this.adapter = adapter;
    }

    public String getSupport()
    {
        return support;
    }

    public void setSupport(String support)
    {
        this.support = support;
    }
}
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
package it.greenvulcano.birt.report.internal.param;

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.w3c.dom.Node;

/**
 *
 * @version 3.1.0 03/feb/2011
 * @author GreenVulcano Developer Team
 */
public class BooleanParameter extends BaseParameter {

    private String        type   = TYPE_BOOLEAN;

    @Override
    public void init(Node node, IGetParameterDefinitionTask task, IScalarParameterDefn scalar, IReportRunnable report)
            throws Exception {
        super.init(node, task, scalar, report);
    }

    @Override
    public String toString() {
        return "param[type: " + type + " - " + getName() + "]";
    }

    public String getType() {
        return type;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.birtreport.config.Parameter#getFormat()
     */
    @Override
    public String getFormat()
    {
        return "";
    }

   public String convertFromValue(Object val) throws Exception {
        return ((Boolean)val).toString();
    }

	public Object convertToValue(String val) throws Exception {

		/*
		 * da aggiustare una volta decisi i formati che usiamo
		 */
		Boolean True = new Boolean(true);
		if(val.equals("1")) return True;
		return Boolean.parseBoolean(val);
	}
}
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

import java.text.DecimalFormat;

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;

/**
 * @version 3.1.0 03/feb/2011
 * @author GreenVulcano Developer Team
 */
public class FloatParameter extends BaseParameter
{
    private String        format = null;
    private String        type   = TYPE_FLOAT;
    private DecimalFormat df     = null;


    @Override
    public void init(Node node, IGetParameterDefinitionTask task, IScalarParameterDefn scalar, IReportRunnable report)
            throws Exception
    {
        super.init(node, task, scalar, report);
        this.format = scalar.getDisplayFormat();
        if (node != null) {
            if (XMLConfig.exists(node, "@format")) {
                format = XMLConfig.get(node, "@format");
            }
        }
        if (format != null) {
            df = new DecimalFormat(format);
        }
        else {
            df = new DecimalFormat();
        }
    }

    @Override
    public String toString()
    {
        return "param[type: " + type + " - " + getName() + " - " + format + "]";
    }

    public String getType()
    {
        return type;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.birtreport.config.Parameter#getFormat()
     */
    @Override
    public String getFormat()
    {
        return format;
    }

    @Override
    public synchronized Object convertToValue(String val) throws Exception
    {
        return df.parse(val);
    }

    public synchronized String convertFromValue(Object val) throws Exception
    {
        return df.format(val);
    }
}
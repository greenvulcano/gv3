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

/**
 *
 * @version 3.1.0 03/feb/2011
 * @author GreenVulcano Developer Team
 */
public class DateParameter extends BaseDateParameter
{
    private static final String type = TYPE_DATE;

    /**
     *
     */
    public DateParameter()
    {
        super();
        defaultFormat = "dd/MM/yyyy";
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.birtreport.config.Parameter#getType()
     */
    @Override
    public String getType()
    {
        return type;
    }

}

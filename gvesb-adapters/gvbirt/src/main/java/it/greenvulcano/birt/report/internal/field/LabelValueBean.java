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
package it.greenvulcano.birt.report.internal.field;

/**
 *
 * @version 3.1.0 20 Feb 2011
 * @author GreenVulcano Developer Team
 */
public class LabelValueBean
{
    private String label = null;
    private String value = null;

    /**
     * @param label
     * @param value
     */
    public LabelValueBean(String label, String value)
    {
        this.label = label;
        this.value = value;
    }

    /**
     * @return the label
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * @return the value
     */
    public String getValue()
    {
        return this.value;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value)
    {
        this.value = value;
    }
}

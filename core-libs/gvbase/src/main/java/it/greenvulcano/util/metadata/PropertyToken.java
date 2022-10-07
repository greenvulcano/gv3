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
package it.greenvulcano.util.metadata;

import java.util.Map;
import java.util.Vector;

import org.mozilla.javascript.Scriptable;

/**
 * PropertyToken class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class PropertyToken
{
    int                           begin     = -1;
    int                           end       = -1;
    String                        value     = "";
    String                        type      = "";
    int                           trigger   = 0;
    private Vector<PropertyToken> subTokens = null;

    /**
     * @param begin
     * @param end
     * @param value
     * @param type
     * @param trigger
     */
    public PropertyToken(int begin, int end, String value, String type, int trigger)
    {
        this.begin = begin;
        this.end = end;
        this.value = value;
        this.type = type;
        this.trigger = trigger;
    }

    /**
     * @return Returns the begin.
     */
    public int getBegin()
    {
        return this.begin;
    }

    /**
     * @return Returns the end.
     */
    public int getEnd()
    {
        if (this.subTokens != null) {
            PropertyToken subToken = this.subTokens.lastElement();
            return subToken.getEnd();
        }
        return this.end;
    }

    /**
     * @return Returns the type.
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * @return Returns the trigger.
     */
    public int getTrigger()
    {
        return this.trigger;
    }

    /**
     * @param inProperties
     * @param obj
     * @param scope
     * @param extra
     * @return Returns the value.
     * @throws PropertiesHandlerException
     */
    public String getValue(Map<String, Object> inProperties, Object obj, Scriptable scope, Object extra)
            throws PropertiesHandlerException
    {
        String retVal = this.value;
        if (this.subTokens != null) {
            for (int i = 0; i < this.subTokens.size(); i++) {
                PropertyToken subToken = this.subTokens.elementAt(i);
                retVal += subToken.getValue(inProperties, obj, scope, extra);
            }
        }
        if (!this.type.equals("")) {
            retVal = PropertiesHandler.expandInternal(this.type, this.trigger, retVal, inProperties, obj, scope, extra);
        }
        return retVal;
    }

    /**
     *
     * @param subToken
     */
    public void addSubToken(PropertyToken subToken)
    {
        if (this.subTokens == null) {
            this.subTokens = new Vector<PropertyToken>();
        }
        this.subTokens.add(subToken);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String result = "PropertyToken: type='" + this.type + "' - value= '" + this.value + "' - begin=" + this.begin + " - end=" + this.end + " - trigger=" + this.trigger;
        if (this.subTokens != null) {
            result += "\nBEGIN SUB\n";
            for (int i = 0; i < this.subTokens.size(); i++) {
                PropertyToken subToken = this.subTokens.elementAt(i);
                result += subToken + "\n";
            }
            result += "END SUB";
        }
        return result;
    }
}

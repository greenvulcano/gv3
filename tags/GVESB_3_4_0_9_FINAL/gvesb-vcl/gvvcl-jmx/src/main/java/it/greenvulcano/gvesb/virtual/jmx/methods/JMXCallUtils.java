/*
 * Copyright (c) 2009-2011 GreenVulcano ESB Open Source Project. All rights
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

package it.greenvulcano.gvesb.virtual.jmx.methods;

/**
 * @version 3.1.0 May 2, 2011
 * @author GreenVulcano Developer Team
 *
 *         REVISION OK
 */
public class JMXCallUtils
{

    /**
     *
     * @param value
     * @param cls
     * @return
     * @throws Exception
     */
    public static Object cast(String value, String cls) throws Exception
    {
        if (cls.equals("byte")) {
            return new Byte(value);
        }
        else if (cls.equals("boolean")) {
            return new Boolean(value);
        }
        else if (cls.equals("char")) {
            return new Character(value.charAt(0));
        }
        else if (cls.equals("double")) {
            return new Double(value);
        }
        else if (cls.equals("float")) {
            return new Float(value);
        }
        else if (cls.equals("int")) {
            return new Integer(value);
        }
        else if (cls.equals("long")) {
            return new Long(value);
        }
        else if (cls.equals("short")) {
            return new Short(value);
        }
        return value;
    }

}

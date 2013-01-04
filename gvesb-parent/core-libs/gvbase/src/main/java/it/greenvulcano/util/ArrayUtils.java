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
package it.greenvulcano.util;

import java.lang.reflect.Array;

/**
 *
 * ArrayUtils class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ArrayUtils
{

    /**
     * @param a
     * @param b
     * @param type
     * @return the arrays concatenated
     */
    public static final Object[] concat(Object[] a, Object[] b, Class<?> type)
    {

        if (a == null) {
            a = (Object[]) Array.newInstance(type, 0);
        }

        if (b == null) {
            b = (Object[]) Array.newInstance(type, 0);
        }

        Object[] result = (Object[]) Array.newInstance(type, a.length + b.length);

        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);

        return result;
    }
}

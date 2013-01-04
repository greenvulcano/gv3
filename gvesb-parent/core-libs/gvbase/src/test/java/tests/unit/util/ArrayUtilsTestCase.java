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
package tests.unit.util;

import it.greenvulcano.util.ArrayUtils;
import junit.framework.TestCase;

/**
 * @version 3.0.0 Feb 27, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class ArrayUtilsTestCase extends TestCase
{

    /**
     *
     */
    public void testArrayUtils()
    {
        String[] a = {"aaa", "bbb"};
        String[] b = {"ccc", "ddd", "eee"};

        Object[] oresult = ArrayUtils.concat(a, b, String.class);
        assertTrue(oresult != null && oresult.length == 5);
        assertEquals(oresult[0], "aaa");
        assertEquals(oresult[1], "bbb");
        assertEquals(oresult[2], "ccc");
        assertEquals(oresult[3], "ddd");
        assertEquals(oresult[4], "eee");
    }
}

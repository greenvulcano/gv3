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
package tests.unit.util.bin;

import it.greenvulcano.util.bin.Dump;

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class DumpTestCase extends TestCase
{
    private static final String EXPECTED_DUMP = "00000000; 74 65 73 74 20 64 61 74 61 20 74 6F 20 64 75 6D ; test data to dum\n"
                                                      + "00000010; 70                                              ; p\n";

    /**
     * @throws Exception
     */
    public void testDump() throws Exception
    {
        byte buffer[] = "test data to dump".getBytes();
        Dump dump = new Dump(buffer);
        dump.setMaxBufferLength(Dump.UNBOUNDED);
        ByteArrayOutputStream actualDump = new ByteArrayOutputStream();
        dump.dump(actualDump);
        actualDump.close();
        assertEquals(EXPECTED_DUMP, actualDump.toString());
    }
}

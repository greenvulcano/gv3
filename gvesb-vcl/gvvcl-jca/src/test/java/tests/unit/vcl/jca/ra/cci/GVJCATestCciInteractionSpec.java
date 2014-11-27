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
package tests.unit.vcl.jca.ra.cci;

import javax.resource.cci.InteractionSpec;

/**
 * @version 3.0.0 Mar 23, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class GVJCATestCciInteractionSpec implements InteractionSpec
{
    private static final long  serialVersionUID = 210L;

    /**
     *
     */
    public static final String WRITE            = "WRITE";
    /**
     *
     */
    public static final String READ             = "READ";

    /**
     *
     */
    public static final int    READ_ALL_FILE    = -1;

    private String             operation;
    private int                numOfLines;

    /**
     *
     */
    public GVJCATestCciInteractionSpec()
    {
    }

    /**
     * @return the operation type
     */
    public String getOperation()
    {
        return operation;
    }

    /**
     * @param op
     */
    public void setOperation(String op)
    {
        operation = op;
    }

    /**
     * @return the number of lines to read
     */
    public int getNumOfLines()
    {
        return numOfLines;
    }

    /**
     * @param lines
     */
    public void setNumOfLines(int lines)
    {
        numOfLines = lines;
    }

}

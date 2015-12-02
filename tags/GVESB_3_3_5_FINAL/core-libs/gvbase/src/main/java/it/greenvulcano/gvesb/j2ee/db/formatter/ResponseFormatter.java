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
package it.greenvulcano.gvesb.j2ee.db.formatter;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;

import java.sql.Statement;

import org.w3c.dom.Node;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface ResponseFormatter
{
    /**
     * Initializes ResponseFormatter
     *
     * @param node
     *        the configuration node
     * @throws GVDBException
     *         if an error occurred
     */
    void init(Node node) throws GVDBException;

    /**
     * Executes the SQL statement
     *
     * @param stmt
     *        the statement
     * @param inputGVBuffer
     *        the input GVBuffer object
     * @throws GVDBException
     *         if an error occurred
     */
    void execute(Statement stmt, GVBuffer inputGVBuffer) throws GVDBException;
}

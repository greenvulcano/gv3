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
package it.greenvulcano.log.db;

/**
 *
 * @version 3.1.0 24/gen/2011
 * @author GreenVulcano Developer Team
 */
public enum ColumnType {
    /**
     * The column will get the log-message..
     */
    MSG,
    /**
     * The column will get the log-message length..
     */
    MSG_SIZE,
    /**
     *  The column gets the priority of the log-message.
     */
    PRIO,
    /**
     * The column gets the integer value of the priority of the log-message.
     */
    IPRIO,
    /**
     *  The column gets the category name.
     */
    CAT,
    /**
     *  The column gets the thread name.
     */
    THREAD,
    /**
     *  The column always gets this value.
     */
    STATIC,
    /**
     *  The column gets an log timestamp.
     */
    TIMESTAMP,
    /**
     *  The column will be ignored.
     */
    EMPTY,
    /**
     *  The column is of type AUTO_INCREMENT.
     */
    AUTO_INC,
    /**
     * The column gets the result of a SEQUENCE call.
     */
    SEQUENCE,
    /**
     *  The column gets the throwable information, if available.
     */
    THROWABLE,
    /**
     * The column will get the NDC.
     */
    NDC,
    /**
     * The column will get the MDC for the given key
     */
    MDC
}

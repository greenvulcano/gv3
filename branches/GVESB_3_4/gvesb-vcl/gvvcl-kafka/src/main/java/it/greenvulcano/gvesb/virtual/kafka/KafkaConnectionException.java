/*
 * Copyright (c) 2009-2021 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.virtual.kafka;

import it.greenvulcano.gvesb.virtual.ConnectionException;

/**
 * <code>KafkaConnectionException</code> is the exception raised by the Kafka GVVL
 * implementation if connection related problems occurs.
 *
 * @version 3.4.0 Oct 12, 2021
 * @author GreenVulcano Developer Team
 *
 */
public class KafkaConnectionException extends ConnectionException
{
    private static final long serialVersionUID = 210L;

    /**
     * Creates a new KafkaConnectionException.
     *
     * @param id
     *        error associated to the exception
     */
    public KafkaConnectionException(String id)
    {
        super(id);
    }

    /**
     * Creates a new KafkaConnectionException. Uses given parameters in order to
     * format the error code.
     *
     * @param id
     *        error associated to the exception
     * @param parameter
     *        parameters for the error message
     */
    public KafkaConnectionException(String id, String[][] parameter)
    {
        super(id, parameter);
    }

    /**
     * Creates a new KafkaConnectionException with a nested exception.
     *
     * @param id
     *        error associated to the exception
     * @param exc
     *        nested exception
     */
    public KafkaConnectionException(String id, Throwable exc)
    {
        super(id, exc);
    }

    /**
     * Creates a new KafkaConnectionException from with a nested exception. Uses
     * given parameters in order to format the error code.
     *
     * @param id
     *        error associated to the exception
     * @param parameter
     *        parameters for the error message
     * @param exc
     *        nested exception
     */
    public KafkaConnectionException(String id, String[][] parameter, Throwable exc)
    {
        super(id, parameter, exc);
    }
}

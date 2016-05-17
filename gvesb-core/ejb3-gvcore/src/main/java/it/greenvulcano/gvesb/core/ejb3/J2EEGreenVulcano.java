/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.ejb3;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;

import javax.ejb.Remote;

/**
 * Local interface of the J2EEGreenVulcano EJB.
 *
 *
 * @version 3.3.0 Feb 5, 2013
 * @author GreenVulcano Developer Team
 */
@Remote
public interface J2EEGreenVulcano
{
    /**
     * request
     *
     * @param gvBuffer
     * @return the result of the operation
     * @throws GVException
     *
     * @see it.greenvulcano.gvesb.core.ejb3.J2EEGreenVulcanoBean#request(GVBuffer)
     */
    public GVBuffer request(GVBuffer gvBuffer) throws GVException;

    /**
     * requestReply
     *
     * @param gvBuffer
     * @return the result of the operation
     * @throws GVException
     *
     * @see it.greenvulcano.gvesb.core.ejb3.J2EEGreenVulcanoBean#requestReply(GVBuffer)
     */
    public GVBuffer requestReply(GVBuffer gvBuffer) throws GVException;

    /**
     * getReply
     *
     * @param gvBuffer
     * @return the result of the operation
     * @throws GVException
     *
     * @see it.greenvulcano.gvesb.core.ejb3.J2EEGreenVulcanoBean#getReply(GVBuffer)
     */
    public GVBuffer getReply(GVBuffer gvBuffer) throws GVException;

    /**
     * sendReply
     *
     * @param gvBuffer
     * @return the result of the operation
     * @throws GVException
     *
     * @see it.greenvulcano.gvesb.core.ejb3.J2EEGreenVulcanoBean#sendReply(GVBuffer)
     */
    public GVBuffer sendReply(GVBuffer gvBuffer) throws GVException;

    /**
     * getRequest
     *
     * @param gvBuffer
     * @return the result of the operation
     * @throws GVException
     *
     * @see it.greenvulcano.gvesb.core.ejb3.J2EEGreenVulcanoBean#getRequest(GVBuffer)
     */
    public GVBuffer getRequest(GVBuffer gvBuffer) throws GVException;

    /**
     * forward
     *
     * @param gvBuffer
     * @param name
     * @return the result of the operation
     * @throws GVException
     *
     * @see it.greenvulcano.gvesb.core.ejb3.J2EEGreenVulcanoBean#forward(GVBuffer,
     *      String)
     */
    public GVBuffer forward(GVBuffer gvBuffer, String name) throws GVException;
}

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
package it.greenvulcano.gvesb.core.ejb;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;

import javax.ejb.EJBLocalObject;

/**
 * Local Interface for J2EEGreenVulcano bean
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public interface J2EEGreenVulcanoLocal extends EJBLocalObject
{
    /**
     * @param gvBuffer
     * @return the result of the operation
     * @throws GVException
     */
    public GVBuffer request(GVBuffer gvBuffer) throws GVException;

    /**
     * @param gvBuffer
     * @return the result of the operation
     * @throws GVException
     */
    public GVBuffer requestReply(GVBuffer gvBuffer) throws GVException;

    /**
     * @param gvBuffer
     * @return the result of the operation
     * @throws GVException
     */
    public GVBuffer getReply(GVBuffer gvBuffer) throws GVException;

    /**
     * @param gvBuffer
     * @return the result of the operation
     * @throws GVException
     */
    public GVBuffer sendReply(GVBuffer gvBuffer) throws GVException;

    /**
     * @param gvBuffer
     * @return the result of the operation
     * @throws GVException
     */
    public GVBuffer getRequest(GVBuffer gvBuffer) throws GVException;

    /**
     * @param gvBuffer
     * @param name
     * @return the result of the operation
     * @throws GVException
     */
    public GVBuffer forward(GVBuffer gvBuffer, String name) throws GVException;
}
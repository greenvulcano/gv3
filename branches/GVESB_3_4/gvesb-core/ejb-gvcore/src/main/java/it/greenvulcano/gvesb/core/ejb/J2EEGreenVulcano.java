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

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

/**
 * Local interface of the J2EEGreenVulcano EJB.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public interface J2EEGreenVulcano extends EJBObject
{
    /**
     * request
     *
     * @param gvBuffer
     * @return the result of the operation
     * @throws RemoteException
     * @throws GVException
     *
     * @see it.greenvulcano.gvesb.core.ejb.J2EEGreenVulcanoBean#request(GVBuffer)
     */
    public GVBuffer request(GVBuffer gvBuffer) throws RemoteException, GVException;

    /**
     * requestReply
     *
     * @param gvBuffer
     * @return the result of the operation
     * @throws RemoteException
     * @throws GVException
     *
     * @see it.greenvulcano.gvesb.core.ejb.J2EEGreenVulcanoBean#requestReply(GVBuffer)
     */
    public GVBuffer requestReply(GVBuffer gvBuffer) throws RemoteException, GVException;

    /**
     * getReply
     *
     * @param gvBuffer
     * @return the result of the operation
     * @throws RemoteException
     * @throws GVException
     *
     * @see it.greenvulcano.gvesb.core.ejb.J2EEGreenVulcanoBean#getReply(GVBuffer)
     */
    public GVBuffer getReply(GVBuffer gvBuffer) throws RemoteException, GVException;

    /**
     * sendReply
     *
     * @param gvBuffer
     * @return the result of the operation
     * @throws RemoteException
     * @throws GVException
     *
     * @see it.greenvulcano.gvesb.core.ejb.J2EEGreenVulcanoBean#sendReply(GVBuffer)
     */
    public GVBuffer sendReply(GVBuffer gvBuffer) throws RemoteException, GVException;

    /**
     * getRequest
     *
     * @param gvBuffer
     * @return the result of the operation
     * @throws RemoteException
     * @throws GVException
     *
     * @see it.greenvulcano.gvesb.core.ejb.J2EEGreenVulcanoBean#getRequest(GVBuffer)
     */
    public GVBuffer getRequest(GVBuffer gvBuffer) throws RemoteException, GVException;

    /**
     * forward
     *
     * @param gvBuffer
     * @param name
     * @return the result of the operation
     * @throws RemoteException
     * @throws GVException
     *
     * @see it.greenvulcano.gvesb.core.ejb.J2EEGreenVulcanoBean#forward(GVBuffer,
     *      String)
     */
    public GVBuffer forward(GVBuffer gvBuffer, String name) throws RemoteException, GVException;
}

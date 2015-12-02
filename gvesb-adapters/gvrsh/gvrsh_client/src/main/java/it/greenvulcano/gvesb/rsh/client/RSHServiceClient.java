/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.rsh.client;

import it.greenvulcano.gvesb.rsh.RSHException;
import it.greenvulcano.gvesb.rsh.server.rmi.RSHService;

import java.rmi.RemoteException;

import org.w3c.dom.Node;

/**
 * @version 3.2.0 16/10/2011
 * @author GreenVulcano Developer Team
 */
public interface RSHServiceClient extends RSHService
{

    public void init(Node node) throws RSHException;

    public byte[] getFileB(String fileName) throws RemoteException, RSHException;

    public void sendFileB(String fileName, byte[] content) throws RemoteException, RSHException;

    public String getName();

    public void invalidate();

    public boolean isValid();

    public void cleanup();
}

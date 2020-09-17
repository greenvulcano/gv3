/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.rsh.server.rmi;

import it.greenvulcano.gvesb.rsh.RSHException;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandDef;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandResult;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.healthmarketscience.rmiio.RemoteInputStream;

/**
 * @version 3.2.0 06/10/2011
 * @author GreenVulcano Developer Team
 */
public interface RSHService extends Remote
{
    /**
     *
     * @param commandDef
     * @return
     * @throws RemoteException
     * @throws RSHException
     */
    public ShellCommandResult shellExec(ShellCommandDef commandDef) throws RemoteException, RSHException;

    /**
     *
     * @param fileName
     * @param ristream
     * @throws RemoteException
     * @throws RSHException
     */
    public void sendFile(String fileName, RemoteInputStream ristream) throws RemoteException, RSHException;

    /**
     *
     * @param fileName
     * @return
     * @throws RemoteException
     * @throws RSHException
     */
    public RemoteInputStream getFile(String fileName) throws RemoteException, RSHException;
}

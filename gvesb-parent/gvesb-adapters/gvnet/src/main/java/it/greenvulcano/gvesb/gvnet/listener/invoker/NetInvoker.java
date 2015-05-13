/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project.
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
package it.greenvulcano.gvesb.gvnet.listener.invoker;

import it.greenvulcano.gvesb.gvnet.NetAdapterException;
import it.greenvulcano.gvesb.gvnet.marshall.NetMessage;

import org.w3c.dom.Node;

/**
 * @version 3.5.0 18/mag/2014
 * @author GreenVulcano Developer Team
 */
public interface NetInvoker
{

    /**
     * @param node
     */
    public void init(Node node) throws NetAdapterException;

    /**
     * @return the name
     */
    public String getName();

    /**
     * 
     * @return
     */
    public boolean isSendReply();

    /**
     * 
     * @param msgIn
     * @return
     * @throws NetAdapterException
     */
    public NetMessage processMessage(NetMessage msgIn) throws NetAdapterException, InterruptedException;

    /**
     * 
     */
    public void destroy();
}

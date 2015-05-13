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
package it.greenvulcano.gvesb.gvnet.marshall;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvnet.NetAdapterException;

import org.w3c.dom.Node;

/**
 * @version 3.5.0 18/05/2014
 * @author GreenVulcano Developer Team
 */
public interface Encoder
{
    /**
     * @param node
     */
    public void init(Node node) throws NetAdapterException;

    /**
     * 
     * @param gvbIn
     * @param msgIn
     * @return
     * @throws NetAdapterException
     */
    public NetMessage encode(GVBuffer gvbIn, NetMessage msgIn) throws NetAdapterException;
}

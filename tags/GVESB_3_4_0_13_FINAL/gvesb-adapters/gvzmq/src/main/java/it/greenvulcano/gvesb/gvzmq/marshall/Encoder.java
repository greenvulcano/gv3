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
package it.greenvulcano.gvesb.gvzmq.marshall;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvzmq.ZMQAdapterException;

import org.w3c.dom.Node;
import org.zeromq.ZMsg;

/**
 * @version 3.2.0 18/03/2012
 * @author GreenVulcano Developer Team
 */
public interface Encoder
{
    /**
     * @param node
     */
    public void init(Node node) throws ZMQAdapterException;

    /**
     * 
     * @param gvbIn
     * @param msgIn
     * @return
     * @throws ZMQAdapterException
     */
    public ZMsg encode(GVBuffer gvbIn, ZMsg msgIn) throws ZMQAdapterException;
}

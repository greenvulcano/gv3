/*
 * Copyright (c) 2009-2011 GreenVulcano ESB Open Source Project. All rights
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

package it.greenvulcano.gvesb.virtual.jmx;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.InitializationException;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * @version 3.1.0 May 2, 2011
 * @author GreenVulcano Developer Team
 * 
 *         REVISION OK
 */
public interface JMXMethodOperation
{

    /**
     * Invoked from <code>JMXCallOperation</code> when a JMX Operation is
     * initialized.<br>
     * 
     * @param node
     *        configuration node. The operation should use this node with
     *        <code>XMLConfig</code> in order to read its configuration
     *        parameters.
     * 
     * @exception InitializationException
     *            if an error occurs during initialization
     * 
     */
    void init(Node node) throws InitializationException;

    /**
     * @param objectName
     * @param props
     * @param gvBuffer
     * @return the resulting operation output
     * @throws Exception
     */
    public Object perform(String objectName, Map<String, Object> props, GVBuffer gvBuffer) throws Exception;

}

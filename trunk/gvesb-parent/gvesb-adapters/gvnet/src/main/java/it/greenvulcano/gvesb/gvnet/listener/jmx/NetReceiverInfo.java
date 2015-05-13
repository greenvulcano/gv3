/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.gvnet.listener.jmx;

import it.greenvulcano.gvesb.gvnet.listener.NetReceiver;

/**
 * NetReceiverInfo class.
 *
 * @version 3.5.0 18/05/2014
 * @author GreenVulcano Developer Team
 *
 */
public class NetReceiverInfo
{
    /**
     * the object JMX descriptor.
     */
    public static final String DESCRIPTOR_NAME  = "NetReceiverInfo";

    private NetReceiver   netReceiver = null;

    /**
     * @param netReceiver
     */
    public NetReceiverInfo(NetReceiver netReceiver)
    {
        this.netReceiver = netReceiver;
    }

    /**
     * @return the receiver name
     */
    public String getName()
    {
        return netReceiver.getName();
    }

    /**
     * @return the receiver status
     */
    public boolean getActive()
    {
        return netReceiver.isActive();
    }

    /**
     * @return the receiver host
     */
    public String getHost()
    {
        return netReceiver.getHost();
    }

    /**
     * @return the receiver port
     */
    public int getPort()
    {
        return netReceiver.getPort();
    }

    /**
     * Start the receiver
     */
    public void start()
    {
    	netReceiver.start();
    }
    
    /**
     * Stop the receiver
     */
    public void stop()
    {
    	netReceiver.stop();
    }
}

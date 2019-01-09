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
package it.greenvulcano.gvesb.gvhl7.listener.jmx;

import it.greenvulcano.gvesb.gvhl7.listener.HL7Listener;

/**
 * HL7ListenerInfo class.
 *
 * @version 3.3.1 Apr 09, 2013
 * @author GreenVulcano Developer Team
 *
 */
public class HL7ListenerInfo
{
    /**
     * the object JMX descriptor.
     */
    public static final String DESCRIPTOR_NAME  = "HL7ListenerInfo";

    private HL7Listener   hl7Listener = null;

    /**
     * @param hl7Listener
     */
    public HL7ListenerInfo(HL7Listener hl7Listener)
    {
        this.hl7Listener = hl7Listener;
    }

    /**
     * @return the listener name
     */
    public String getName()
    {
        return hl7Listener.getName();
    }

    /**
     * @return the listener status
     */
    public boolean getRunning()
    {
        return hl7Listener.isRunning();
    }

    /**
     * @return the listening port
     */
    public int getPort()
    {
        return hl7Listener.getPort();
    }

    /**
     * Start the listener
     */
    public void start()
    {
    	hl7Listener.start();
    }
    
    /**
     * Stop the listener
     */
    public void stop()
    {
    	hl7Listener.stop();
    }
}

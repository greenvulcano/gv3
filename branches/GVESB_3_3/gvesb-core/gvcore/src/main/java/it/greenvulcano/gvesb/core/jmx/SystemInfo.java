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
package it.greenvulcano.gvesb.core.jmx;

import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;

/**
 * SystemInfo class.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class SystemInfo
{
    private static final Logger logger          = GVLogger.getLogger(SystemInfo.class);

    /**
     * the object JMX descriptor
     */
    public static final String  DESCRIPTOR_NAME = "SystemInfo";
    /**
     * the system name
     */
    private String              name            = "";
    /**
     * the JMX filter for inter-instances communication
     */
    private String              jmxFilter       = "";
    /**
     * The status of the system activation.
     */
    private boolean             isActive      = true;

    /**
     * Constructor
     *
     * @param sName
     *        the service name
     * @param act
     *        the isActive flag
     */
    public SystemInfo(String sName, boolean act)
    {
        name = sName;
        isActive = act;
        jmxFilter = "GreenVulcano:*,Component=" + ServiceOperationInfo.DESCRIPTOR_NAME
        + ",Group=management,Internal=Yes,IDSystem=" + name;
    }

    /**
     * Get the status of the system isActive. <br/>
     *
     * @return The System isActive flag
     */
    public boolean getActivation()
    {
        return isActive;
    }

    /**
     * @param act
     *        the isActive flag
     * @exception Exception
     *            if errors occurs
     */
    public void setActivation(boolean act) throws Exception
    {
        isActive = act;
        //JMXUtils.set(jmxFilter, "systemActivation", new Boolean(isActive), false, logger);
    }

    /**
     * @return the system name
     */
    public String getName()
    {
        return name;
    }
}
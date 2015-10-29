/*
 * Copyright (c) 2009-2015 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.gvpushnot.publisher.jmx;

import it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine;
import it.greenvulcano.gvesb.gvpushnot.publisher.PushNotificationException;

/**
 * NotificatioEngineInfo class.
 *
 * @version 3.4.0 16/feb/2015
 * @author GreenVulcano Developer Team
 *
 */
public class NotificationEngineInfo
{
    /**
     * the object JMX descriptor.
     */
    public static final String DESCRIPTOR_NAME  = "NotificationEngineInfo";

    private NotificationEngine   engine = null;

    /**
     * @param NotificationEngine
     */
    public NotificationEngineInfo(NotificationEngine engine)
    {
        this.engine = engine;
    }

    /**
     * @return the engine name
     */
    public String getName()
    {
        return engine.getName();
    }

    /**
     * @return the engine status
     */
    public boolean getActive()
    {
        return engine.isActive();
    }

    /**
     * @return the listening port
     */
    public String getType()
    {
        return engine.getType().toString();
    }

    /**
     * Start the engine
     */
    public void start() throws PushNotificationException
    {
    	engine.start();
    }
    
    /**
     * Stop the engine
     */
    public void stop() throws PushNotificationException
    {
    	engine.stop();
    }
}

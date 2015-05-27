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
package it.greenvulcano.gvesb.gvmqtt.subscriber.jmx;

import it.greenvulcano.gvesb.gvmqtt.subscriber.MQTTSubscriber;

/**
 * MQTTSubscriberInfo class.
 *
 * @version 3.5.0 13/03/2015
 * @author GreenVulcano Developer Team
 *
 */
public class MQTTSubscriberInfo
{
    /**
     * the object JMX descriptor.
     */
    public static final String DESCRIPTOR_NAME  = "MQTTSubscriberInfo";

    private MQTTSubscriber   mqttSubscriber = null;

    /**
     * @param netReceiver
     */
    public MQTTSubscriberInfo(MQTTSubscriber mqttSubscriber)
    {
        this.mqttSubscriber = mqttSubscriber;
    }

    /**
     * @return the subscriber name
     */
    public String getName()
    {
        return mqttSubscriber.getName();
    }

    /**
     * @return the subscriber status
     */
    public boolean getActive()
    {
        return mqttSubscriber.isActive();
    }

    /**
     * @return the subscriber brokerUrl
     */
    public String getBrokerUrl()
    {
        return mqttSubscriber.getBrokerUrl();
    }

    /**
     * Start the subscriber
     */
    public void start()
    {
        mqttSubscriber.start();
    }
    
    /**
     * Stop the subscriber
     */
    public void stop()
    {
        mqttSubscriber.stop();
    }
}

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
package it.greenvulcano.gvesb.gvmqtt.publisher.jmx;

import it.greenvulcano.gvesb.gvmqtt.publisher.MQTTPublisher;

/**
 * MQTTPublisherInfo class.
 *
 * @version 3.5.0 13/03/2015
 * @author GreenVulcano Developer Team
 *
 */
public class MQTTPublisherInfo
{
    /**
     * the object JMX descriptor.
     */
    public static final String DESCRIPTOR_NAME  = "MQTTPublisherInfo";

    private MQTTPublisher   mqttPublisher = null;

    /**
     * @param mqttPublisher
     */
    public MQTTPublisherInfo(MQTTPublisher mqttPublisher)
    {
        this.mqttPublisher = mqttPublisher;
    }

    /**
     * @return the publisher name
     */
    public String getName()
    {
        return mqttPublisher.getName();
    }

    /**
     * @return the publisher status
     */
    public boolean getActive()
    {
        return mqttPublisher.isActive();
    }

    /**
     * @return the publisher brokerUrl
     */
    public String getBrokerUrl()
    {
        return mqttPublisher.getBrokerUrl();
    }

    /**
     * @return the publisher topic
     */
    public String getTopic()
    {
        return mqttPublisher.getTopic();
    }

    /**
     * @return the publisher QoS
     */
    public int getQos()
    {
        return mqttPublisher.getQos();
    }

    /**
     * @return the publisher retained
     */
    public boolean getRetained()
    {
        return mqttPublisher.isRetained();
    }

    /**
     * Start the publisher
     */
    public void start()
    {
        mqttPublisher.start();
    }
    
    /**
     * Stop the publisher
     */
    public void stop()
    {
        mqttPublisher.stop();
    }
}

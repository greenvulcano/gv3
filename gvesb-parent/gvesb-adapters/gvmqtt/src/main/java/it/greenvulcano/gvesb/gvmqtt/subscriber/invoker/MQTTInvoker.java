/*
 * Copyright (c) 2009-2015 GreenVulcano ESB Open Source Project.
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
package it.greenvulcano.gvesb.gvmqtt.subscriber.invoker;

import it.greenvulcano.gvesb.gvmqtt.MQTTAdapterException;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.w3c.dom.Node;

/**
 * @version 3.5.0 13/mar/2015
 * @author GreenVulcano Developer Team
 */
public interface MQTTInvoker
{

    /**
     * @param node
     */
    public void init(Node node) throws MQTTAdapterException;

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
     * @param topic
     * @param msgIn
     * @return
     * @throws MQTTAdapterException
     */
    public MqttMessage processMessage(String subscriber, String topic, MqttMessage msgIn) throws MQTTAdapterException, InterruptedException;

    /**
     * 
     */
    public void destroy();
}

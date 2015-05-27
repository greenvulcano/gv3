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
package it.greenvulcano.gvesb.gvmqtt.publisher;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvmqtt.GVMQTTManager;
import it.greenvulcano.gvesb.gvmqtt.MQTTAdapterException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.BaseThread;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.5.0 13/03/2015
 * @author GreenVulcano Developer Team
 */
public class MQTTPublisher implements MqttCallback
{
    private static Logger       logger       = GVLogger.getLogger(MQTTPublisher.class);

    private String              name;
    private boolean             autoStart    = false;
    private boolean             isActive     = false;
    private long                reconTime    = 30000;

    private String              clientID     = null;
    private MqttClient          client       = null;
    private String              brokerUrl    = null;
    private MqttConnectOptions  conOpt       = null;
    private boolean             cleanSession = false;
    private String              password     = null;
    private String              userName     = null;

    private String              topic        = null;
    private int                 qos          = 0;
    private boolean             retained     = false;

    private GVMQTTManager       manager      = null;


    public void init(Node node) throws MQTTAdapterException {
        try {
            name = XMLConfig.get(node, "@name");
            brokerUrl = XMLConfig.get(node, "@brokerUrl", null);
            autoStart = XMLConfig.getBoolean(node, "@autoStart", true);
            reconTime = XMLConfig.getInteger(node, "@reconnectTime", 30) * 1000;

            clientID = XMLConfig.get(node, "@clientID");
            brokerUrl = XMLConfig.get(node, "@brokerUrl");
            cleanSession = XMLConfig.getBoolean(node, "@cleanSession", false);
            userName = XMLConfig.get(node, "@userName");
            password = XMLConfig.get(node, "@password");

            topic = XMLConfig.get(node, "@topic");
            qos = XMLConfig.getInteger(node, "@qos", 0);
        }
        catch (Exception exc) {
            logger.error("Error initializing MQTTPublisher", exc);
            throw new MQTTAdapterException("GVMQTT_PUBLISHER_INIT_ERROR", exc);
        }
    }

    public void setManager(GVMQTTManager manager) {
        this.manager = manager;
    }

    public void publish(byte[] payload) throws MQTTAdapterException {
        publish(topic, payload, qos, retained);
    }

    public void publish(String topic, byte[] payload) throws MQTTAdapterException {
        if (topic == null) {
            topic = this.topic;
        }
        publish(topic, payload, qos, retained);
    }

    public void publish(String topic, byte[] payload, int qos) throws MQTTAdapterException {
        if (topic == null) {
            topic = this.topic;
        }
        if (qos < 0) {
            qos = this.qos;
        }
        publish(topic, payload, qos, retained);
    }

    public void publish(String topic, byte[] payload, int qos, boolean retained) throws MQTTAdapterException {
        if (topic == null) {
            topic = this.topic;
        }
        if (qos < 0) {
            qos = this.qos;
        }
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        message.setRetained(retained);
        publish(topic, message);
    }

    public void publish(String topic, MqttMessage message) throws MQTTAdapterException {
        if (topic == null) {
            topic = this.topic;
        }
        if (!isActive) {
            throw new MQTTAdapterException("MQTTPublisher[" + name + "] NOT active");
        }
        connect();
        try {
        	logger.debug("Publishing message on Topic[" + topic + "] from MQTTPublisher[" + name + "]");
            client.publish(topic, message);
        }
        catch (Exception exc) {
            disconnect();
            throw new MQTTAdapterException("Error publishing message on MQTTPublisher[" + name + "]", exc);
        }
    }
    
    public void publish(MqttMessage message) throws MQTTAdapterException {
        if (!isActive) {
            throw new MQTTAdapterException("MQTTPublisher[" + name + "] NOT active");
        }
        connect();
        try {
            client.publish(topic, message);
        }
        catch (Exception exc) {
            disconnect();
            throw new MQTTAdapterException("Error publishing message on MQTTPublisher[" + name + "]", exc);
        }
    }

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
    public void connectionLost(Throwable cause) {
        logger.error("MQTTPublisher[" + name + "] connection error", cause);
        try {
            connect();
        }
        catch (Exception exc) {
            logger.error("Error reconnecting MQTTPublisher[" + name + "]", exc);
        }
    }

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Called when a message has been delivered to the
        // server. The token passed in here is the same one
        // that was passed to or returned from the original call to publish.
        // This allows applications to perform asynchronous
        // delivery without blocking until delivery completes.
        //
        // This sample demonstrates asynchronous deliver and
        // uses the token.waitForCompletion() call in the main thread which
        // blocks until the delivery has completed.
        // Additionally the deliveryComplete method will be called if
        // the callback is set on the client
        //
        // If the connection to the server breaks before delivery has completed
        // delivery of a message will complete after the client has re-connected.
        // The getPendingTokens method will provide tokens for any messages
        // that are still to be delivered.
    }

    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
    public void messageArrived(String topic, MqttMessage msgIn) throws MqttException {
        // Called when a message arrives from the server that matches any
        // subscription made by the client
    }

    /**
     * 
     */
    private void connect() {
        while (isActive && (client == null)) {
            try {
                // Construct the connection options object that contains connection parameters
                // such as cleanSession and LWT
                conOpt = new MqttConnectOptions();
                conOpt.setCleanSession(cleanSession);
                if(userName != null) {
                    conOpt.setUserName(this.userName);
                }
                if(password != null ) {
                    conOpt.setPassword(this.password.toCharArray());
                }

                String localBrokerUrl = brokerUrl;
                if (brokerUrl == null) {
                    localBrokerUrl = manager.getDefaultBrokerUrl();
                }

                // Construct an MQTT blocking mode client
                client = new MqttClient(localBrokerUrl, clientID, manager.getDataStore());

                // Set this wrapper as the callback handler
                client.setCallback(this);

                // Connect to the MQTT server
                client.connect(conOpt);
                logger.debug("Connected to " + localBrokerUrl + " with client ID " + client.getClientId());

                logger.info("MQTTPublisher[" + name + "] started");
            }
            catch (Exception exc) {
                if (client != null) {
                    try {
                        client.disconnectForcibly();
                    }
                    catch (Exception exc2) {
                        // do nothing
                    }
                }
                client = null;
                logger.error("Error connecting to [" + brokerUrl + "]... retring", exc);
                try {
                    Thread.sleep(reconTime);
                }
                catch (InterruptedException exc1) {
                    // do nothing
                }
            }
        }
    }
    
    /**
     * 
     */
    private void disconnect() {
        if (client != null) {
            try {
                client.disconnect();
            }
            catch (Exception exc) {
                //exc.printStackTrace();
            }
        }
        client = null;
    }

    /**
     * @return
     */
    public boolean isAutoStart() {
        return autoStart;
    }
    
    /**
     * 
     * @return
     */
    public String getBrokerUrl() {
        return this.brokerUrl;
    }
    
    public String getTopic() {
        return this.topic;
    }
    
    public int getQos() {
        return this.qos;
    }
    
    public boolean isRetained() {
        return this.retained;
    }

    public void start() {
        if ((client != null) && client.isConnected()) {
            return;
        }
        isActive = true;
        // start subscriber
        Runnable rr = new Runnable() {
            @Override
            public void run()
            {
                try {
                    connect();
                }
                catch (Exception exc) {
                    logger.error("Error starting MQTTPublisher[" + name + "]", exc);
                }
            }
        };

        BaseThread bt = new BaseThread(rr, "MQTTPublisher[" + name + "] starter");
        bt.setDaemon(true);
        bt.start();
        logger.info("MQTTPublisher[" + name + "] starting...");
    }
    
    public void stop() {
        isActive = false;
        disconnect();
        logger.info("MQTTPublisher[" + name + "] stopped");
    }

    /**
     * 
     * @return
     */
    public boolean isActive() {
        return isActive;
    }


    public String getName() {
        return this.name;
    }

    public void destroy() {
        logger.debug("BEGIN - Destroing MQTTPublisher[" + name + "]");
        stop();
        logger.debug("END - Destroing MQTTPublisher[" + name + "]");
    }
}

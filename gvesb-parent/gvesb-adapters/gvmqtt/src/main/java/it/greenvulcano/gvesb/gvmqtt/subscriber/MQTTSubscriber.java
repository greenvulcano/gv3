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
package it.greenvulcano.gvesb.gvmqtt.subscriber;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvmqtt.GVMQTTManager;
import it.greenvulcano.gvesb.gvmqtt.MQTTAdapterException;
import it.greenvulcano.gvesb.gvmqtt.subscriber.invoker.MQTTInvoker;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.BaseThread;
import it.greenvulcano.util.thread.ThreadMap;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @version 3.5.0 13/03/2015
 * @author GreenVulcano Developer Team
 */
public class MQTTSubscriber implements MqttCallback
{
    private static Logger     logger     = GVLogger.getLogger(MQTTSubscriber.class);

    private String            name;
    private MQTTInvoker       invoker     = null;
    private boolean           autoStart   = false;
    private String            serverName;
    private boolean           isActive    = false;
    private long              reconTime   = 30000;

    private String              clientID     = null;
    private MqttClient          client       = null;
    private String              brokerUrl    = null;
    private MqttConnectOptions  conOpt       = null;
    private boolean             cleanSession = false;
    private String              password     = null;
    private String              userName     = null;

    private String[]            topicFilters = null;
    private int[]               qos          = null;

    private GVMQTTManager       manager      = null;


    public void init(Node node) throws MQTTAdapterException {
        try {
            serverName = JMXEntryPoint.getServerName();
            name = XMLConfig.get(node, "@name");
            brokerUrl = XMLConfig.get(node, "@brokerUrl", null);
            autoStart = XMLConfig.getBoolean(node, "@autoStart", true);
            reconTime = XMLConfig.getInteger(node, "@reconnectTime", 30) * 1000;

            clientID = XMLConfig.get(node, "@clientID");
            brokerUrl = XMLConfig.get(node, "@brokerUrl");
            cleanSession = XMLConfig.getBoolean(node, "@cleanSession", false);
            userName = XMLConfig.get(node, "@userName");
            password = XMLConfig.get(node, "@password");

            NodeList nl = XMLConfig.getNodeList(node, "TopicFilters/Filter");
            if (nl.getLength() == 0) {
                throw new MQTTAdapterException("Empty topic filter list for MQTTSubscriber[" + name + "]");
            }
            topicFilters = new String[nl.getLength()];
            qos = new int[nl.getLength()];
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                topicFilters[i] = XMLConfig.get(n, "@topic");
                qos[i] = XMLConfig.getInteger(n, "@qos", 0);
            }

            Node ni = XMLConfig.getNode(node, "*[@type='mqtt-invoker']");
            invoker = (MQTTInvoker) Class.forName(XMLConfig.get(ni, "@class")).newInstance();
            invoker.init(ni);
        }
        catch (Exception exc) {
            logger.error("Error initializing MQTTSubscriber", exc);
            throw new MQTTAdapterException("GVMQTT_SUBSCRIBER_INIT_ERROR", exc);
        }
    }

    
    public void setManager(GVMQTTManager manager) {
        this.manager = manager;
    }

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
    public void connectionLost(Throwable cause) {
        logger.error("MQTTSubscriber[" + name + "] listening error", cause);
        try {
            connect();
        }
        catch (Exception exc) {
            logger.error("Error reconnecting MQTTSubscriber[" + name + "]", exc);
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
        NMDC.push();
        NMDC.clear();
        NMDC.setServer(serverName);
        NMDC.setSubSystem(GVMQTTManager.SUBSYSTEM);
        NMDC.put("SUBSCRIBER", name);
         
        try {
            MqttMessage msgOut = invoker.processMessage(name, topic, msgIn);
            if (msgOut != null) {
                //msgOut.writeTo(os);
            }
        }
        catch (Exception exc) {
            logger.error("MQTTSubscriber[" + name + "] processing error", exc);
        }
        finally {
            NMDC.pop();
            ThreadMap.clean();
        }
    }

    /**
     * 
     */
    private void connect() {
        if (client != null) {
            try {
                client.disconnectForcibly();
            }
            catch (Exception exc2) {
                // do nothing
            }
        }
        client = null;

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

                // Subscribe to the requested topic(s)
                for (int i = 0; i < topicFilters.length; i++) {
                    logger.debug("Subscribing to topic \"" + topicFilters[i] + "\" qos " + qos[i]);                    
                }
                client.subscribe(topicFilters, qos);

                logger.info("MQTTSubscriber[" + name + "] started");
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
                    logger.error("Error starting MQTTSubscriber[" + name + "]", exc);
                }
            }
        };

        BaseThread bt = new BaseThread(rr, "MQTTSubscriber[" + name + "] starter");
        bt.setDaemon(true);
        bt.start();
        logger.info("MQTTSubscriber[" + name + "] starting...");
    }
    
    public void stop() {
        isActive = false;
        disconnect();
        logger.info("MQTTSubscriber[" + name + "] stopped");
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
        logger.debug("BEGIN - Destroing MQTTSubscriber[" + name + "]");
        stop();
        if (invoker != null) {
            invoker.destroy();
        }
        invoker = null;
        logger.debug("END - Destroing MQTTSubscriber[" + name + "]");
    }
}

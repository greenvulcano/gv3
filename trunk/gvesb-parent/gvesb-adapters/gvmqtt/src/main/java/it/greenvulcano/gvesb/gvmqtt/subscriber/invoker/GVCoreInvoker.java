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
package it.greenvulcano.gvesb.gvmqtt.subscriber.invoker;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.gvmqtt.GVMQTTManager;
import it.greenvulcano.gvesb.gvmqtt.MQTTAdapterException;
import it.greenvulcano.gvesb.gvmqtt.marshall.Decoder;
import it.greenvulcano.gvesb.gvmqtt.marshall.Encoder;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.5.0 13/03/2015
 * @author GreenVulcano Developer Team
 */
public class GVCoreInvoker implements MQTTInvoker
{
    private static Logger logger    = GVLogger.getLogger(GVCoreInvoker.class);

    private String        name;
    private String        system;
    private String        service;
    private String        operation;
    private Decoder       decoder   = null;
    private Encoder       encoder   = null;
    private boolean       debug     = false;
    private boolean       sendReply = false;

    /**
     *
     */
    @Override
    public void init(Node node) throws MQTTAdapterException
    {
        try {
            name = XMLConfig.get(node, "@name");
            system = XMLConfig.get(node, "@gv-system", GVBuffer.DEFAULT_SYS);
            service = XMLConfig.get(node, "@gv-service");
            operation = XMLConfig.get(node, "@gv-operation");
            debug = XMLConfig.getBoolean(node, "@debug", false);
            sendReply = XMLConfig.getBoolean(node, "@send-reply", false);

            Node nd = XMLConfig.getNode(node, "*[@type='mqtt-decoder']");
            if (nd != null) {
                decoder = (Decoder) Class.forName(XMLConfig.get(nd, "@class")).newInstance();
                decoder.init(nd);
            }
            Node ne = XMLConfig.getNode(node, "*[@type='mqtt-encoder']");
            if (ne != null) {
                encoder = (Encoder) Class.forName(XMLConfig.get(ne, "@class")).newInstance();
                encoder.init(ne);
            }
        }
        catch (Exception exc) {
            throw new MQTTAdapterException("GVNET_GVCOREINVOKER_INIT_ERROR", exc);
        }
    }

    /**
     * @return the name
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * 
     */
    @Override
    public boolean isSendReply()
    {
        return sendReply;
    }

    @Override
    public MqttMessage processMessage(String subscriber, String topic, MqttMessage msgIn) throws MQTTAdapterException, InterruptedException
    {
        MqttMessage msgOut = null;

        try {
            NMDC.push();
            logger.debug("Processing message from topic[" + topic + "]...");
            if (debug) {
                logger.debug("Input Message:\n" + msgIn);
            }
            GVBuffer in = new GVBuffer(system, service);
            in.setObject(msgIn);
            in.setProperty("MQTT_SUBSCRIBER", subscriber);
            in.setProperty("MQTT_TOPIC", topic);
            in.setProperty("MQTT_QOS", String.valueOf(msgIn.getQos()));
            in.setProperty("MQTT_IS_DUPLICATE", msgIn.isDuplicate() ? "Y" : "N");
            in.setProperty("MQTT_IS_RETAINED", msgIn.isRetained() ? "Y" : "N");

            if (decoder != null) {
                in = decoder.decode(msgIn, in);
            }
            else {
                in.setObject(msgIn.getPayload());
            }
            GVBufferMDC.put(in);
            logger.debug("BEGIN Operation");

            GVBuffer out = getGreenVulcanoPool().forward(in, operation);

            if (sendReply && !"Y".equals(out.getProperty("IGNORE_RESPONSE"))) {
                if (out.getObject() instanceof MqttMessage) {
                    msgOut = (MqttMessage) out.getObject();
                }
                if (encoder != null) {
                    msgOut = encoder.encode(out, msgIn);
                }

                if (msgOut == null) {
                    throw new MQTTAdapterException("Invalid or missing reply message");
                }
                if (debug) {
                    logger.debug("Output Message:\n" + msgOut);
                }
            }

            logger.debug("END Operation");
        }
        catch (Exception exc) {
            logger.error("Error processing Net message", exc);
        }
        finally {
            NMDC.pop();
        }
        return msgOut;
    }

    @Override
    public void destroy()
    {
        encoder = null;
        decoder = null;
    }

    /**
     * @return
     * @throws MQTTAdapterException
     */
    private GreenVulcanoPool getGreenVulcanoPool() throws MQTTAdapterException
    {
        return GVMQTTManager.instance().getGreenVulcanoPool();
    }
}

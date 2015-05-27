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
package it.greenvulcano.gvesb.virtual.mqtt;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvmqtt.GVMQTTManager;
import it.greenvulcano.gvesb.gvmqtt.publisher.MQTTPublisher;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.json.JSONUtils;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import org.w3c.dom.Node;


/**
 * 
 * @version 3.5.0 18/may/2014
 * @author GreenVulcano Developer Team
 */
public class MQTTPublisherCallOperation implements CallOperation
{
    private static Logger    logger   = GVLogger.getLogger(MQTTPublisherCallOperation.class);

    private OperationKey     key       = null;
    private boolean          dynamic   = false;
    private String           publisher = null;
    private String           topic     = null;
    private String           qos       = null;
    private boolean          debug     = false;


    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        logger.debug("Init start");
        try {
            publisher = XMLConfig.get(node, "@publisher");
            topic = XMLConfig.get(node, "@topic", null);
            qos = XMLConfig.get(node, "@qos", null);
            debug = XMLConfig.getBoolean(node, "@debug", false);
            dynamic = XMLConfig.getBoolean(node, "@dynamic", false);

            logger.debug("init - loaded parameters: publisher[" + dynamic + "]= " + publisher + " - topic= " 
                       + (topic != null ? topic : "") + " - qos= " + (qos != null ? qos : ""));
            logger.debug("Init stop");
        }
        catch (Exception exc) {
            throw new InitializationException("GV_INIT_SERVICE_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb
     * .buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        MqttMessage msgOut = null;
        try {
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            String locPublisher = publisher;
            if (dynamic) {
                locPublisher = PropertiesHandler.expand(publisher, params, gvBuffer);
            }
            String locTopic = PropertiesHandler.expand(topic, params, gvBuffer);
            String locQoS = PropertiesHandler.expand(qos, params, gvBuffer);

            byte[] payload = null;
            Object obj = gvBuffer.getObject();
            if (obj instanceof byte[]) {
                payload = (byte[]) obj;
            }
            else if (obj instanceof String) {
                payload = ((String) obj).getBytes();
            }
            else if (obj instanceof Node) {
                payload = XMLUtils.serializeDOMToByteArray_S((Node) obj);
            }
            else if (obj instanceof JSONObject) {
                payload = JSONUtils.serializeJSON((JSONObject) obj).getBytes();
            }
            else {
                throw new Exception("Invalid input type: " + (obj != null ? obj.getClass() : "null"));
            }

            MQTTPublisher publ = GVMQTTManager.instance().getPublisher(locPublisher);

            if ((locQoS == null) || "".equals(locQoS)) {
                publ.publish(locTopic, payload);
            }
            else {
                publ.publish(locTopic, payload, Integer.parseInt(locQoS, 10));
            }
        }
        catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
        return gvBuffer;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano
     * .gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }


    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    @Override
    public OperationKey getKey()
    {
        return key;
    }
}

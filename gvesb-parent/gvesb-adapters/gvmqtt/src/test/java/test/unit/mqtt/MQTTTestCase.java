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
package test.unit.mqtt;

import it.greenvulcano.gvesb.gvmqtt.GVMQTTManager;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.TextUtils;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


/**
 * @version 3.0.0 29/set/2010
 * @author GreenVulcano Developer Team
 */
public class MQTTTestCase extends TestCase
{
	private MqttClient listenerClient = null;
	private static int NUM_MSG_TO_SEND = 5;
	private static int numMsgRec = 0;
	private static String marker = TextUtils.generateRandomString(20);
			
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        assertTrue("System property 'it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath' not set.",
                System.getProperty("it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath") != null);
        GVLogger.getLogger("dummy");
        File mqHome = new File(PropertiesHandler.expand("sp{{gv.app.home}}"), "moquette");
        FileUtils.deleteQuietly(mqHome);
        FileUtils.forceMkdir(mqHome); 
        numMsgRec = 0;
        //GVMQTTManager.instance();
        regListener();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        try {
        	listenerClient.disconnect();
		} catch (Exception e) {
			// do nothing
		}
        listenerClient = null;
    }

    /**
     * @throws Exception
     */
    public void testSendRec() throws Exception
    {
    	String topic        = "gvesb/test";
        String content      = "Message[%d] from MqttPublishSample - " + marker;
        int qos             = 2;
        String broker       = "tcp://localhost:1883";
        String clientId     = "JavaSample";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            sampleClient.connect(connOpts);
            for (int i = 0; i < NUM_MSG_TO_SEND; i++) {
                String msg = String.format(content, i);
                System.out.println("[MQTT Send] message sent: " + msg);
                MqttMessage message = new MqttMessage(msg.getBytes());
                message.setQos(qos);
                sampleClient.publish(topic, message);
            }
            sampleClient.disconnect();
            Thread.sleep(2000);
            if (numMsgRec != NUM_MSG_TO_SEND) {
            	fail("Bad number of message received [" + numMsgRec + "/" + NUM_MSG_TO_SEND + "]");
            }
        } catch(MqttException me) {
            fail(me.toString());
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
    
    private void regListener() {
    	String topic        = "gvesb/test_resp";
        String broker       = "tcp://localhost:1883";
        String clientId     = "JavaSample2";
        
        if (listenerClient != null) {
        	return;
        }
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            listenerClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            listenerClient.connect(connOpts);
            listenerClient.setCallback(new MqttCallback() {
                
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("[MQTT Rec] message received from topic[" + topic+"]: " + message);
                    String msg = new String(message.getPayload());
                    if (msg.contains(marker)) {
                    	numMsgRec++;
                    }
                    else {
                    	fail("Bad message received!!!");
                    }
                }
                
                @Override
                public void deliveryComplete(IMqttDeliveryToken arg0) {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void connectionLost(Throwable arg0) {
                    // TODO Auto-generated method stub
                    
                }
            });
            listenerClient.subscribe(topic);
            
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
}

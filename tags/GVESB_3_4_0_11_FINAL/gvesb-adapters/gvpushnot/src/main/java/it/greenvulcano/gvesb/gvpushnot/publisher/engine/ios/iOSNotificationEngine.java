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
package it.greenvulcano.gvesb.gvpushnot.publisher.engine.ios;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvpushnot.publisher.EngineType;
import it.greenvulcano.gvesb.gvpushnot.publisher.FeedbackResult;
import it.greenvulcano.gvesb.gvpushnot.publisher.Notification;
import it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine;
import it.greenvulcano.gvesb.gvpushnot.publisher.NotificationResult;
import it.greenvulcano.gvesb.gvpushnot.publisher.PushNotificationException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.crypto.CryptoHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javapns.devices.Device;
import javapns.devices.Devices;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.feedback.AppleFeedbackServer;
import javapns.feedback.AppleFeedbackServerBasicImpl;
import javapns.feedback.FeedbackServiceManager;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;
import javapns.notification.ResponsePacket;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.4.0 16/feb/2015
 * @author GreenVulcano Developer Team
 */
public class iOSNotificationEngine implements NotificationEngine {
	private static Logger    logger          = GVLogger.getLogger(iOSNotificationEngine.class);

	private static EngineType type = EngineType.iOS;

	private String        name;
	private String        keystoreID;
	private String        keystorePwd;
	private boolean       useProduction;
	private boolean       isActive = false;
	private boolean       isAutoStart = false;
	private boolean       heavyDebug  = false;
	private AppleNotificationServer pushServer      = null;
	private PushNotificationManager pushManager     = new PushNotificationManager();
	private AppleFeedbackServer     feedbackServer  = null;
	private FeedbackServiceManager  feedbackManager = new FeedbackServiceManager();

	/* (non-Javadoc)
	 * @see it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine#init(org.w3c.dom.Node)
	 */
	@Override
	public void init(Node node) throws PushNotificationException {
		try {
			name = XMLConfig.get(node, "@name");
			keystoreID = XMLConfig.get(node, "@keystoreID");
			useProduction = XMLConfig.getBoolean(node, "@useProduction", false);
			heavyDebug = XMLConfig.getBoolean(node, "@heavyDebug", false);
			isAutoStart = XMLConfig.getBoolean(node, "@autoStart", true);
			keystorePwd = CryptoHelper.getKeyStoreID(keystoreID).getKeyStorePwd();

			logger.info("Initialized iOSNotificationEngine[" + name + "]: keystoreID = " +  keystoreID 
			+ " - useProduction = " + useProduction + " - isAutoStart = " + isAutoStart + ")");
		}
		catch (Exception exc) {
			throw new PushNotificationException("Error initializing iOSNotificationEngine", exc);
		}

	}

	/* (non-Javadoc)
	 * @see it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine#getType()
	 */
	@Override
	public EngineType getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine#push(it.greenvulcano.gvesb.gvpushnot.publisher.Notification)
	 */
	@Override
	public NotificationResult push(Notification notification) throws PushNotificationException {
    	if (!isActive()) {
    		throw new PushNotificationException("Invalid state on iOSNotificationEngine[" + name + "]: Not Active");
    	}

		logger.debug("BEGIN - Sending Notification on iOSNotificationEngine[" + name + "]: " + notification.getNotificationID());
		try {
			PushNotificationPayload complexPayload = PushNotificationPayload.complex();
			if (notification.getTitle() != null) {
				complexPayload.addCustomAlertTitle(notification.getTitle());
	        }
			if (notification.getMessage() != null) {
				complexPayload.addCustomAlertBody(notification.getMessage());
	        }
	        Map<String, String> extra = notification.getExtraData();
	        if (!extra.isEmpty()) {
	        	for (Map.Entry<String, String> element : extra.entrySet()) {
	        		complexPayload.addCustomDictionary(element.getKey(), element.getValue());					
				}
	        }
			
			logger.debug("Native Message: " + complexPayload.getPayload().toString());

			//List<PushedNotification> pNotifications = Push.payload(complexPayload, CryptoHelper.getKeyStore(keystoreID), keystorePwd, useProduction, notification.getRegistrationIds());
			PushedNotifications pNotifications = new PushedNotifications();
			List<Device> deviceList = new ArrayList<Device>();
			List<String> regIDs = notification.getRegistrationIds();
			if (regIDs.size() > 1) {
				deviceList = Devices.asDevices(regIDs);
			}
			else {
				Map<String, String> regIDKeys = notification.getRegistrationIdKeys();
            	String regId = regIDs.get(0);
            	String regKey = regIDKeys.get(regId);
            	Device dev = new BasicDevice();
            	dev.setToken(regId);
            	dev.setDeviceId(regKey);
            	deviceList.add(dev);
			}
			pNotifications.setMaxRetained(deviceList.size());
			for (Device device : deviceList) {
				try {
					BasicDevice.validateTokenFormat(device.getToken());
					PushedNotification pNotification = pushManager.sendNotification(device, complexPayload, false);
					pNotifications.add(pNotification);
				} catch (InvalidDeviceTokenFormatException exc) {
					pNotifications.add(new PushedNotification(device, complexPayload, exc));
				}
			}
			
			NotificationResult nr = new NotificationResult();
            for (int i = 0; i < pNotifications.size(); i++) {
            	PushedNotification pN = pNotifications.get(i);
            	String msg = null;
            	String canonicalID = null; 
            	if (!pN.isSuccessful()) {
            		Exception exc = pN.getException();
            		ResponsePacket rp = pN.getResponse();
            		if (exc != null) {
            			msg = exc.getMessage();
            		}
            		else if (rp != null) {
            			msg = rp.getMessage();
            		}
            		if (msg != null) {
            			if (msg.contains("required 64 bytes")) {
            				canonicalID = "-1";
            			}
            		}
            	}
            	nr.addResult(name, type, notification.getNotificationID(), String.valueOf(pN.getIdentifier()),
            			msg, pN.getDevice().getToken(), pN.getDevice().getDeviceId(), canonicalID);
            }
            logger.debug("Result: " + nr);
            //System.out.println("Result XML: " + XMLUtils.serializeDOM_S(nr.toXML()));
            
            return nr;
    	}
    	catch (Exception exc) {
    		throw new PushNotificationException("Error processing iOS Notification on Engine[" + name + "]", exc);
		}
		finally {
			logger.debug("END - Sending Notification on iOSNotificationEngine[" + name + "]: " + notification.getNotificationID());
		}
	}

	/* (non-Javadoc)
	 * @see it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine#push(java.util.List)
	 */
	@Override
	public NotificationResult push(List<Notification> notifications) throws PushNotificationException {
		NotificationResult nr = new NotificationResult();
		for (Notification notification : notifications) {
			nr.addAllResults(push(notification));
		}
		return nr;
	}
	
	/* (non-Javadoc)
	 * @see it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine#getFeedback()
	 */
	@Override
	public FeedbackResult getFeedback() throws PushNotificationException {
    	logger.debug("Requested feedback on iOSNotificationEngine[" + name + "]");
		try {
			LinkedList<Device> devices = feedbackManager.getDevices(feedbackServer);
			
			FeedbackResult fr = new FeedbackResult();

			for (Device device : devices) {
            	fr.addResult(name, type, device.getDeviceId(), device.getToken(), device.getLastRegister());
            }
            logger.debug("Feedback: " + fr);
            //System.out.println("Feedback XML: " + XMLUtils.serializeDOM_S(fr.toXML()));

            return fr;
		} catch (Exception exc) {
    		throw new PushNotificationException("Error processing iOS Feedback on Engine[" + name + "]", exc);
		}
	}

	/* (non-Javadoc)
	 * @see it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine#start()
	 */
	@Override
	public void start() throws PushNotificationException {
		isActive = false;
		if (pushServer == null) {
			try {
				PushNotificationManager.setHeavyDebugMode(heavyDebug);
				pushServer = new AppleNotificationServerBasicImpl(CryptoHelper.getKeyStore(keystoreID), keystorePwd, useProduction);
				feedbackServer = new AppleFeedbackServerBasicImpl(CryptoHelper.getKeyStore(keystoreID), keystorePwd, useProduction);
				pushManager.restartConnection(pushServer);
			}
			catch (Exception exc) {
	    		throw new PushNotificationException("Error starting iSONotificationEngine[" + name + "]", exc);
			}
		}
		isActive = true;
	}

	/* (non-Javadoc)
	 * @see it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine#stop()
	 */
	@Override
	public void stop() throws PushNotificationException {
		try {
			pushManager.stopConnection();
		} catch (Exception exc) {
			throw new PushNotificationException("Error stopping iOSNotificationEngine[" + name + "]", exc);
		}
		isActive = false;
		pushServer = null;
		feedbackServer = null;
	}

	public boolean isAutoStart() {
		return isAutoStart;
	}

	/* (non-Javadoc)
	 * @see it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine#isActive()
	 */
	@Override
	public boolean isActive() {
		return isActive;
	}

	/* (non-Javadoc)
	 * @see it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine#destroy()
	 */
	@Override
	public void destroy() {
		try {
			stop();
		}
		catch (Exception exc) {
			// do nothing
		}
	}
	
	@Override
	public String toString() {
		return "iOSNotificationEngine[" + name + "]";
	}

}

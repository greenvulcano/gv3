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
package it.greenvulcano.gvesb.gvpushnot.publisher.engine.android;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvpushnot.publisher.EngineType;
import it.greenvulcano.gvesb.gvpushnot.publisher.FeedbackResult;
import it.greenvulcano.gvesb.gvpushnot.publisher.Notification;
import it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine;
import it.greenvulcano.gvesb.gvpushnot.publisher.NotificationResult;
import it.greenvulcano.gvesb.gvpushnot.publisher.PushNotificationException;
import it.greenvulcano.log.GVLogger;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

/**
 * 
 * @version 3.4.0 17/feb/2015
 * @author GreenVulcano Developer Team
 */
public class AndroidNotificationEngine implements NotificationEngine {
	private static Logger    logger          = GVLogger.getLogger(AndroidNotificationEngine.class);
	
    private static EngineType type = EngineType.Android;

    private String        name;
    private String        apiKey;
    private boolean       isDryRun    = false;
    private boolean       isActive    = false;
    private boolean       isAutoStart = false;
    private Sender        sender      = null; 

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws PushNotificationException {
        try {
            name = XMLConfig.get(node, "@name");
            apiKey = XMLConfig.getDecrypted(node, "@apiKey");
            isDryRun = XMLConfig.getBoolean(node, "@dryRun", false);
            isAutoStart = XMLConfig.getBoolean(node, "@autoStart", true);
            
            logger.info("Initialized AndroidNotificationEngine[" + name + "]: apiKey = *" 
        			+ " - isDryRun = " + isDryRun + " - isAutoStart = " + isAutoStart + ")");
        }
        catch (Exception exc) {
            throw new PushNotificationException("Error initializing AndroidNotificationEngine", exc);
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
    		throw new PushNotificationException("Invalid state on AndroidNotificationEngine[" + name + "]: Not Active");
    	}

    	logger.debug("BEGIN - Sending Notification on AndroidNotificationEngine[" + name + "]: " + notification.getNotificationID());
    	try {
	    	Message.Builder builder = new Message.Builder();
	    	if (notification.getTitle() != null) {
	    		builder.addData("title", notification.getTitle());
	        }
	        if (notification.getMessage() != null) {
	        	builder.addData("message", notification.getMessage());
	        }
	        Map<String, String> extra = notification.getExtraData();
	        if (!extra.isEmpty()) {
	        	for (Map.Entry<String, String> element : extra.entrySet()) {
		        	builder.addData(element.getKey(), element.getValue());					
				}
	        }
	
	        Message gcmMessage = builder.build();
	        logger.debug("Native Message: " + gcmMessage.toString());
	        
	        List<String> regIDs = notification.getRegistrationIds();
	        Map<String, String> regIDKeys = notification.getRegistrationIdKeys();
	        MulticastResult results = sender.send(gcmMessage, regIDs, 5);
            //System.out.println(results);
            
            NotificationResult nr = new NotificationResult();
            List<Result> lr = results.getResults();
            for (int i = 0; i < lr.size(); i++) {
            	Result r = lr.get(i);
            	String crid = r.getCanonicalRegistrationId();
            	if ("NotRegistered".equals(r.getErrorCodeName()) || "InvalidRegistration".equals(r.getErrorCodeName())) {
            		crid = "-1";
            	}
            	String regId = regIDs.get(i);
            	String regKey = regIDKeys.get(regId);
            	nr.addResult(name, type, notification.getNotificationID(),
            			r.getMessageId(), r.getErrorCodeName(),
            			regId, regKey, crid);
            }
            logger.debug("Result: " + nr);
            //System.out.println("Result XML: " + XMLUtils.serializeDOM_S(nr.toXML()));

            return nr;
    	}
    	catch (Exception exc) {
    		throw new PushNotificationException("Error processing Android Notification on Engine[" + name + "]", exc);
		}
		finally {
			logger.debug("END - Sending Notification on AndroidNotificationEngine[" + name + "]: " + notification.getNotificationID());
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
		return new FeedbackResult();
	}

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine#start()
     */
    @Override
    public void start() throws PushNotificationException {
    	isActive = false;
    	if (sender == null) {
    		sender = new Sender(apiKey);
    	}
    	isActive = true;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.gvpushnot.publisher.NotificationEngine#stop()
     */
    @Override
    public void stop() throws PushNotificationException {
        sender = null;
        isActive = false;
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
		} catch (Exception exc) {
			// do nothing
		}
    }
	
	@Override
	public String toString() {
		return "AndroidNotificationEngine[" + name + "]";
	}
}

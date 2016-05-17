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
package it.greenvulcano.gvesb.gvpushnot.publisher;

import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.4.0 16/feb/2015
 * @author GreenVulcano Developer Team
 */
public class NotificationResult {
	public static class MessageStatus {
		private String     engine         = "";
		private EngineType type;
		private String     notificationID = "";
		private String     messageID      = "";
		private String     error          = "";
		private String     destinationID  = "";
		private String     destinationKey = "";
		private String     canonicalID    = "";

		public MessageStatus(String engine, EngineType type, String notificationID, 
				String messageID, String error, String destinationID, String destinationKey, String canonicalID) {
			this.engine = engine;
			this.type = type;
			this.notificationID = (notificationID != null) ? notificationID : "";
			this.messageID = (messageID != null) ? messageID : "";
			this.error = (error != null) ? error : "";
			this.destinationID = (destinationID != null) ? destinationID : "";
			this.destinationKey = (destinationKey != null) ? destinationKey : "";
			this.canonicalID = (canonicalID != null) ? canonicalID : "";
		}
		
		public String getEngine() {
			return this.engine;
		}

		public EngineType getType() {
			return this.type;
		}

		public String getNotificationID() {
			return this.notificationID;
		}
		
		public String getMessageID() {
			return this.messageID;
		}

		public String getError() {
			return this.error;
		}

		public String getDestinationID() {
			return this.destinationID;
		}

		public String getDestinationKey() {
			return this.destinationKey;
		}

		public String getCanonicalID() {
			return this.canonicalID;
		}

		@Override
		public String toString() {
			return "{ engine = " + engine
			+ " - engineType = " +  type
			+ " - notificationID = " + notificationID
			+ " - messageID = " + messageID
			+ " - error = " + error
			+ " - destinationID = " + destinationID
			+ " - destinationKey = " + destinationKey
			+ " - canonicalID = " + canonicalID + " }";
		}

		public void appendTo(Element root, XMLUtils parser) throws PushNotificationException {
			try {
				Element nr = parser.insertElement(root, "Message");
				nr.setAttribute("engine", engine);
				nr.setAttribute("engineType", type.toString());
				nr.setAttribute("notificationID", notificationID);
				nr.setAttribute("messageID", messageID);
				nr.setAttribute("error", error);
				nr.setAttribute("destinationID", destinationID);
				nr.setAttribute("destinationKey", destinationKey);
				nr.setAttribute("canonicalID", canonicalID);
			} catch (XMLUtilsException exc) {
				throw new PushNotificationException("Error processing MessageStatus", exc);
			}
		}
	}

	private List<MessageStatus> results = new ArrayList<MessageStatus>();
	private int success = 0;
	private int failure = 0;
	private int total   = 0;
	private int canonicalIDs = 0;

	public void addResult(String engine, EngineType type, String notificationID, String messageID, 
	        String error, String destinationID, String destinationKey, String canonicalID) {
		MessageStatus status = new MessageStatus(engine, type, notificationID, messageID, 
		        error, destinationID, destinationKey, canonicalID);
		total++;
		if (error == null) {
			success++;
		}
		else {
			failure++;
		}
		if ((canonicalID != null) && !"-1".equals(canonicalID)) {
			canonicalIDs++;
		}
		results.add(status);
	}

	public void addAllResults(NotificationResult nr) {
		this.results.addAll(nr.results);
		this.total += nr.total;
		this.success += nr.success;
		this.failure += nr.failure;
		this.canonicalIDs += nr.canonicalIDs;
	}

	@Override
	public String toString() {
		return "NotificationResult: {total: " + total + " - success: " + success + " - failure: " + failure
				+ " - canonicalIDs: " + canonicalIDs + "} -> " + results;
	}

	public Node toXML() throws PushNotificationException {
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			Element root = parser.newDocument("NotificationResult").getDocumentElement();
			root.setAttribute("total", String.valueOf(total));
			root.setAttribute("success", String.valueOf(success));
			root.setAttribute("failure", String.valueOf(failure));
			root.setAttribute("canonicalIDs", String.valueOf(canonicalIDs)); 

			for (MessageStatus status : results) {
				status.appendTo(root, parser);
			}
			return root;
		} catch (Exception exc) {
			throw new PushNotificationException("Error processing NotificationResult", exc);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
	}
}

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

import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.4.0 16/feb/2015
 * @author GreenVulcano Developer Team
 */
public class FeedbackResult {
	public static class DeviceStatus {
		private String     engine        = "";
		private EngineType type;
		private String     deviceID      = "";
		private String     destinationID = "";
		private Timestamp  lastRegister  = null;

		public DeviceStatus(String engine, EngineType type, String deviceID, String destinationID, Timestamp lastRegister) {
			this.engine = engine;
			this.type = type;
			this.deviceID = (deviceID != null) ? deviceID : "";
			this.destinationID = (destinationID != null) ? destinationID : "";
			this.lastRegister= lastRegister;
		}
		
		public String getEngine() {
			return this.engine;
		}

		public EngineType getType() {
			return this.type;
		}
		
		public String getDeviceID() {
			return this.deviceID;
		}
		
		public String getDestinationID() {
			return this.destinationID;
		}

		public Timestamp lastRegister() {
			return this.lastRegister;
		}

		@Override
		public String toString() {
			return "{ deviceID = " + deviceID + " - destinationID = " + destinationID + " - lastRegister = " + lastRegister + " }";
		}
		
		public void appendTo(Element root, XMLUtils parser) throws PushNotificationException {
			try {
				Element nr = parser.insertElement(root, "Device");
				nr.setAttribute("engine", engine);
				nr.setAttribute("type", type.toString());
				nr.setAttribute("deviceID", deviceID);
				nr.setAttribute("destinationID", destinationID);
				nr.setAttribute("lastRegister", DateUtils.dateToString(lastRegister, DateUtils.FORMAT_ISO8601_DATETIME));
			} catch (XMLUtilsException exc) {
				throw new PushNotificationException("Error processing DeviceStatus", exc);
			}
		}
	}

	private List<DeviceStatus> results = new ArrayList<DeviceStatus>();
	private int total = 0;

	public void addResult(String engine, EngineType type, String deviceID, String destinationID, Timestamp lastRegister) {
		DeviceStatus status = new DeviceStatus(engine, type, deviceID, destinationID, lastRegister);
		total++;
		results.add(status);
	}
	
	public void addAllResults(FeedbackResult fr) {
		this.results.addAll(fr.results);
		this.total += fr.total;
	}


	@Override
	public String toString() {
		return "FeedbackResult: {total: " + total + "} -> " + results;
	}
	
	public Node toXML() throws PushNotificationException {
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			Element root = parser.newDocument("FeedbackResult").getDocumentElement();
			root.setAttribute("total", String.valueOf(total));

			for (DeviceStatus status : results) {
				status.appendTo(root, parser);
			}
			return root;
		} catch (Exception exc) {
			throw new PushNotificationException("Error processing FeedbackResult", exc);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
	}
}

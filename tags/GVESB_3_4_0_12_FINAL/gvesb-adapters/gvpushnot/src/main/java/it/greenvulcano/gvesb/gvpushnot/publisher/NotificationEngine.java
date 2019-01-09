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

import java.util.List;

import org.w3c.dom.Node;

/**
 * 
 * @version 3.4.0 16/feb/2015
 * @author GreenVulcano Developer Team
 */
public interface NotificationEngine
{
	void init(Node node) throws PushNotificationException;

	EngineType getType();
	String getName();
	
	NotificationResult push(Notification notification) throws PushNotificationException;
	NotificationResult push(List<Notification> notifications) throws PushNotificationException;
	
	FeedbackResult getFeedback() throws PushNotificationException;
	
	void start() throws PushNotificationException;
	void stop() throws PushNotificationException;
	boolean isAutoStart();
	boolean isActive();
	
	void destroy();
}

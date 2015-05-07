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
package it.greenvulcano.gvesb.gvdicom.support;

import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.BaseThreadFactory;

import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.dicom4j.dicom.network.dimse.DimseStatus;
import org.dicom4j.dicom.uniqueidentifiers.SOPClass;
import org.dicom4j.network.association.Association;
import org.dicom4j.network.association.associate.AssociateSession;
import org.dicom4j.network.dimse.DimseMessageFactory;
import org.dicom4j.network.dimse.messages.CEchoRequestMessage;
import org.dicom4j.network.dimse.messages.CEchoResponseMessage;
import org.dicom4j.network.dimse.messages.DimseMessage;


/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class EchoSupport {
	private static final Logger 				logger 				= GVLogger.getLogger(EchoSupport.class);
	
	private static final DimseMessageFactory 	MessageFactory 		= new DimseMessageFactory();
	
	private static final ThreadFactory 			thFactory;
	
	
	static {
		thFactory = new BaseThreadFactory("DICOM_EchoSupport", true);	
	}
	
	public EchoSupport() {
		
	}
	
	/*
	 *  Method that throws an association request for a C-Echo
	 */
	public void associationEcho(Association association, AssociateSession associateSession) throws Exception {
		CEchoRequestMessage message = association.getMessageFactory()
				.newCEchoRequest(SOPClass.Verification.getUID());
		
		logger.info("Open connection for DicomEchoCallOperation");
		byte lPres = associateSession.getSuitablePresentationContextID(SOPClass.Verification.getUID());
				
		Thread lT = thFactory.newThread(new SendRunnable(association, lPres, message));
		lT.start();		
	}
	
	/*
	 *  Method that handles a C-Echo replay
	 */
	public void messageEcho(Association association, DimseMessage message) throws Exception {
		
		logger.info("C-Echo association established");
		logger.info("Response: "
				+ ((CEchoResponseMessage) message).statusToString());
		association.sendReleaseRequest();
	}
	
	/*
	 * Method that handles a C-Echo request
	 */
	public void messageEchoReq(Association association, byte presentationContextID, 
			DimseMessage message) throws Exception {
		
		logger.info("C-Echo Request received");									  
		CEchoRequestMessage lRequest = (CEchoRequestMessage) message;		
		int lMessageID = lRequest.getMessageID();
		String lSOPClassUID = lRequest.getAffectedSOPClassUID();
		
		CEchoResponseMessage lreponse = MessageFactory.newCEchoResponse(
				lMessageID, lSOPClassUID, DimseStatus.Success);
		association.sendMessage(presentationContextID, lreponse);
	}

}

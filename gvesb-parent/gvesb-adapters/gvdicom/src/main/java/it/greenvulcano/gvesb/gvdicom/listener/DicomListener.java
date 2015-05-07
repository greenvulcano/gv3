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
package it.greenvulcano.gvesb.gvdicom.listener;

import it.greenvulcano.gvesb.gvdicom.DicomAdapterException;
import it.greenvulcano.gvesb.gvdicom.support.EchoSupport;
import it.greenvulcano.gvesb.gvdicom.support.FindSupport;
import it.greenvulcano.gvesb.gvdicom.support.MoveSupport;
import it.greenvulcano.gvesb.gvdicom.support.StoreSupport;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;
import org.dicom4j.network.association.Association;
import org.dicom4j.network.association.associate.AssociateAbort;
import org.dicom4j.network.association.associate.AssociateReject;
import org.dicom4j.network.association.associate.AssociateRelease;
import org.dicom4j.network.association.associate.AssociateSession;
import org.dicom4j.network.association.listeners.AssociationListener;
import org.dicom4j.network.dimse.messages.CEchoRequestMessage;
import org.dicom4j.network.dimse.messages.CMoveRequestMessage;
import org.dicom4j.network.dimse.messages.CStoreRequestMessage;
import org.dicom4j.network.dimse.messages.DimseMessage;


/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class DicomListener implements AssociationListener {
	
	protected static Logger 			logger;
	
	protected OperationKey 				key;
	
	protected String 					host;
	
	protected int 						port;

	/*
	 * 
	 */
	public DicomListener() {
		logger = GVLogger.getLogger(DicomListener.class);
		host = null;
		port = -1;
	}
	
	@Override
	public void associateRelease(Association association, AssociateRelease arg1)
			throws Exception, DicomAdapterException {
		logger.info("Received the request for release");
		try {
			association.sendReleaseResponse();
		} catch (Exception exc) {
			throw new DicomAdapterException("GVDICOM_ASSOCIATE-RELEASE_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
		}

	}

	@Override
	public void associationAborted(Association association, AssociateAbort arg1)
			throws Exception {
		logger.info("Association has been aborted");

	}

	@Override
	public void associationCreated(Association association) throws Exception {
	}

	@Override  
	public void associationOpened(Association association, 
			AssociateSession associateSession) throws Exception {
	}

	
	@Override
	public void associationRejected(Association association, AssociateReject arg1)
			throws Exception {
		logger.info("Association has been rejected");

	}

	@Override
	public void associationReleased(Association association) throws Exception {
		logger.info("Association has been released with success");

	}

	@Override
	public void exceptionCaught(Association association, Throwable cause) {
		logger.debug(cause.getMessage());
		try {
			association.sendAbort(AssociateAbort.ServiceUserAbort);
			logger.error("SEND-ABORT INVOCATED CAUSED BY: " + cause.getMessage());
		} catch (Exception exc) {
			logger.error("SEND-ABORT_ERROR" + exc.getMessage());
		}

	}

	@Override
	public void messageReceived(Association association,
			byte presentationContextID, DimseMessage message) throws Exception {
		
		logger.debug("messageReceived: " + message);
		
		
		if (message instanceof CEchoRequestMessage) {                        
			
			// processing the C-Echo request
			EchoSupport echo = new EchoSupport();
			echo.messageEchoReq(association, presentationContextID, message);
			
		} else if (message.isCFindRequest()) {
			
			// processing the C-Find request
			FindSupport find = new FindSupport();
			find.messageFindReq(association,presentationContextID, message);
		
		} else if (message instanceof CStoreRequestMessage) {
			
			// processing the C-Store request 
			StoreSupport store = new StoreSupport();
			store.messageStoreReq(association, presentationContextID, message);
			
		} else if (message instanceof CMoveRequestMessage) {
			
			// processing the C-Move request
			MoveSupport move = new MoveSupport();
			move.messageMoveReq(association, presentationContextID, message);
			
		} else
			association.sendAbort( AssociateAbort.ServiceUserAbort );
		
	}

}

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


import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.gvdicom.Constants;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.BaseThreadFactory;

import java.util.Random;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.dicom4j.data.CommandSet;
import org.dicom4j.data.CommandSetType;
import org.dicom4j.data.DataElements;
import org.dicom4j.data.DataSet;
import org.dicom4j.data.elements.UnsignedLong;
import org.dicom4j.data.elements.UnsignedShort;
import org.dicom4j.dicom.DicomTags;
import org.dicom4j.dicom.network.dimse.DimsePriority;
import org.dicom4j.dicom.uniqueidentifiers.SOPClass;
import org.dicom4j.network.association.Association;
import org.dicom4j.network.association.associate.AssociateSession;
import org.dicom4j.network.dimse.DimseConst;
import org.dicom4j.network.dimse.messages.CMoveRequestMessage;
import org.dicom4j.network.dimse.messages.CMoveResponseMessage;
import org.dicom4j.network.dimse.messages.DimseMessage;


/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class MoveSupport {
	private static final Logger 			logger 				= GVLogger.getLogger(MoveSupport.class);
	
	private static Random 					random				= new Random();
	
	private static final ThreadFactory 		thFactory;
	
	private String 							applicationEntity;
	
	
	static {
		thFactory = new BaseThreadFactory("DICOM_MoveSupport", true);	
	}
	
	public MoveSupport() {
		try {
			applicationEntity = XMLConfig.get(Constants.CONF_FILE_NAME, "/GVDicomAdapterConfiguration/Local/@applicationEntity");
		}
		catch (Exception exc) {
			logger.error("GVDICOM_APPLICATION-ENTITY_PROPERTIES_ERROR: " + exc.getMessage());
		}
	}
	
	/*
	 *  Method that throws an association request for a C-Move
	 */
	@SuppressWarnings("deprecation")
	public void associationMove(Association association, AssociateSession associateSession, String sopClass,
			String queryLevel, String patID, String patName, String sopClassUID, 
			String studyUID, String seriesUID) throws Exception {
		
		DataSet data = new DataSet();
		CommandSet cm = new CommandSet();
		logger.info("Open connection for DicomMoveCallOperation");
		
		cm.addElement(new UnsignedLong(DicomTags.CommandGroupLength));
		cm.addElement(new UnsignedShort(DicomTags.CommandField));
		cm.addElement(new UnsignedShort(DicomTags.DataSetType));
		cm.addElement(DataElements.newMessageID()); 
		cm.addElement(DataElements.newPriority()); 
		
		if (!sopClass.equals("")) {
			if (sopClass
					.equals(SOPClass.PatientRootQueryRetrieveInformationModelMOVE.getUID())) {
						cm.addUniqueIdentifier(DataElements.newAffectedSOPClassUID(),
							SOPClass.PatientRootQueryRetrieveInformationModelMOVE.getUID());
			} else if (sopClass
					.equals(SOPClass.StudyRootQueryRetrieveInformationModelMOVE.getUID())) {
						cm.addUniqueIdentifier(DataElements.newAffectedSOPClassUID(),
							SOPClass.StudyRootQueryRetrieveInformationModelMOVE.getUID());
			} else if (sopClass
					.equals(SOPClass.PatientStudyOnlyQueryRetrieveInformationModelMOVE)) {
						cm.addUniqueIdentifier(DataElements.newAffectedSOPClassUID(),
							SOPClass.PatientStudyOnlyQueryRetrieveInformationModelMOVE.getUID());
			} 
		} else {
			logger.error("SOPClass is required field");
			return;
		}
		
		data.addPersonName(DataElements.newPatientName(), patName); 
		data.addLongString(DataElements.newPatientID(), patID); 
		data.addUniqueIdentifier(DataElements.newSOPClassUID(), sopClassUID); 
		data.addUniqueIdentifier(DataElements.newStudyInstanceUID(), studyUID);
		data.addUniqueIdentifier(DataElements.newSeriesInstanceUID(), seriesUID);
		
		if (!queryLevel.equals("")) {
			if (queryLevel
			.equals(DimseConst.QueryRetrieveLevel.STUDY_LEVEL)) {
				data.addCodeString(DataElements.newQueryRetrieveLevel(),
						DimseConst.QueryRetrieveLevel.STUDY_LEVEL);
			} else if (queryLevel
			.equals(DimseConst.QueryRetrieveLevel.PATIENT_LEVEL)) {
				data.addCodeString(DataElements.newQueryRetrieveLevel(),
						DimseConst.QueryRetrieveLevel.PATIENT_LEVEL);
			} else if (queryLevel
			.equals(DimseConst.QueryRetrieveLevel.SERIES_LEVEL)) {
				data.addCodeString(DataElements.newQueryRetrieveLevel(),
						DimseConst.QueryRetrieveLevel.SERIES_LEVEL);
			} else if (queryLevel
			.equals(DimseConst.QueryRetrieveLevel.IMAGES_LEVEL)) {
				data.addCodeString(DataElements.newQueryRetrieveLevel(),
						DimseConst.QueryRetrieveLevel.IMAGES_LEVEL);
			}
		} else {
			logger.error("query level is required field");
			return;
		}
		
		data.addApplicationEntity(DataElements.newMoveDestination(), applicationEntity);
		cm.addApplicationEntity(DataElements.newMoveDestination(), applicationEntity);
		
		cm.addDataSet(data);
		
		cm.setCommandField(CommandSetType.C_MOVE_REQUEST.value()); 
		
		CMoveRequestMessage message = association.getMessageFactory()
				.newCMoveRequest(cm);
		
		message.setCommandField(CommandSetType.C_MOVE_REQUEST.value()); 
		message.setDataSet(data);
		
		int messId = random.nextInt(100);
		message.setMessageID(messId);
		message.setPriority(DimsePriority.MEDIUM.value());
		logger.info("Message ID: " + message.getMessageID());
		
		Id id = new Id();
		String messID = id.toString();
		logger.info("Id generated in String format: " + messID);
		
		byte lPres = associateSession
				.getSuitablePresentationContextID(sopClass);

		Thread lT = thFactory.newThread(new SendRunnable(association, lPres, message));
		lT.start();														
	}
	
	/*
	 *  Method that handles a C-Move reply
	 */
	public void messageMove(Association association, DimseMessage message) throws Exception {
		
		logger.info("------------------------------------------------");
		logger.info("CMoveResponseMessage\n");
		logger.info("CommandSet: " + message.getCommandSet());
		logger.info("Message status: " + message.toString());
		
		logger.info("C-Move association established");
		logger.info("Response: "
				+ ((CMoveResponseMessage) message).statusToString());
		
		// Check on the status of the message
		if (((CMoveResponseMessage) message).hasSucces() && !((CMoveResponseMessage) message).isPending()) {
			
			logger.info("Status: Success!!"); 
			association.sendReleaseRequest();	
		} else {
			logger.info("Status: Pending...");
		}
	}
	
	/*
	 * Method that handles a C-Move request
	 * 
	 * --- This method still does not work [server features] ---
	 */
	public void messageMoveReq(Association association, byte presentationContextID, 
			DimseMessage message) throws Exception {
	}

}

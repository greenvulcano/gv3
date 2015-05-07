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


import java.util.concurrent.ThreadFactory;

import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.BaseThreadFactory;

import org.apache.log4j.Logger;
import org.dicom4j.data.DataElements;
import org.dicom4j.data.DataSet;
import org.dicom4j.dicom.uniqueidentifiers.SOPClass;
import org.dicom4j.network.association.Association;
import org.dicom4j.network.association.associate.AssociateSession;
import org.dicom4j.network.dimse.DimseConst;
import org.dicom4j.network.dimse.messages.CFindRequestMessage;
import org.dicom4j.network.dimse.messages.CFindResponseMessage;
import org.dicom4j.network.dimse.messages.DimseMessage;



/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class FindSupport {
	private static final Logger 			logger 			= GVLogger.getLogger(FindSupport.class);
	
	private static final ThreadFactory 		thFactory;
	
	
	static {
		thFactory = new BaseThreadFactory("DICOM_FindSupport", true);	
	}
	
	public FindSupport() {
		
	}
	
	/*
	 *  Method that throws an association request for a C-Find
	 */
	public void associationFind(Association association, AssociateSession associateSession, String sopClass,
			String queryLevel, String patientId, String patientName, String patientSex, 
			String sopInstanceUID, String seriesInstanceUID, String affectedSopClassUID, 
			String affectedSopInstanceUID, String studyInstanceUID, 
			String modalityStudy) throws Exception {
		
		CFindRequestMessage message = null;
		DataSet data = new DataSet();

		logger.info("Open connection for DicomFindCallOperation");
		if (sopClass
				.equals(SOPClass.PatientRootQueryRetrieveInformationModelFIND
						.getUID())) {
			message = association.getMessageFactory().newCFindRequest(
					SOPClass.PatientRootQueryRetrieveInformationModelFIND);
			data.addUniqueIdentifier(DataElements.newSOPClassUID(),
					SOPClass.PatientRootQueryRetrieveInformationModelFIND
					.getUID());
		} else if (sopClass
				.equals(SOPClass.PatientStudyOnlyQueryRetrieveInformationModelFIND
						.getUID())) {
			message = association.getMessageFactory().newCFindRequest(
					SOPClass.PatientStudyOnlyQueryRetrieveInformationModelFIND);
			data.addUniqueIdentifier(DataElements.newSOPClassUID(),
					SOPClass.PatientStudyOnlyQueryRetrieveInformationModelFIND
					.getUID());
		} else if (sopClass.equals(SOPClass.StudyRootQueryRetrieveInformationModelFIND
					.getUID())) {
			message = association.getMessageFactory().newCFindRequest(
					SOPClass.StudyRootQueryRetrieveInformationModelFIND);
			data.addUniqueIdentifier(DataElements.newSOPClassUID(),
					SOPClass.StudyRootQueryRetrieveInformationModelFIND
					.getUID());
		} else if (sopClass.equals(SOPClass.ModalityWorklistInformationModelFIND
				.getUID())){ 
			message = association.getMessageFactory().newCFindRequest(
					SOPClass.ModalityWorklistInformationModelFIND);
			data.addUniqueIdentifier(DataElements.newSOPClassUID(),
					SOPClass.ModalityWorklistInformationModelFIND.getUID());
		}else {
			logger.error("SOPClass is not supported");
		}
		if (!queryLevel.equals("")) {
			if (queryLevel
					.equals(DimseConst.QueryRetrieveLevel.PATIENT_LEVEL))
				data.addCodeString(DataElements.newQueryRetrieveLevel(),
						DimseConst.QueryRetrieveLevel.PATIENT_LEVEL);
			else if (queryLevel
					.equals(DimseConst.QueryRetrieveLevel.SERIES_LEVEL))
				data.addCodeString(DataElements.newQueryRetrieveLevel(),
						DimseConst.QueryRetrieveLevel.SERIES_LEVEL);
			else if (queryLevel
					.equals(DimseConst.QueryRetrieveLevel.STUDY_LEVEL))
				data.addCodeString(DataElements.newQueryRetrieveLevel(),
						DimseConst.QueryRetrieveLevel.STUDY_LEVEL);
			else if (queryLevel
					.equals(DimseConst.QueryRetrieveLevel.IMAGES_LEVEL))
				data.addCodeString(DataElements.newQueryRetrieveLevel(),
						DimseConst.QueryRetrieveLevel.IMAGES_LEVEL);

		} else {
			logger.error("query level is required field");
			return;
		}
		
		data.addLongString(DataElements.newPatientID(), patientId);
		data.addPersonName(DataElements.newPatientName(), patientName);
		data.addCodeString(DataElements.newPatientSex(), patientSex);
		data.addElement(DataElements.newPatientBirthDate());
		data.addElement(DataElements.newPatientAddress());
		data.addElement(DataElements.newPatientMotherBirthName());
		data.addElement(DataElements.newPatientPhoneNumbers());
		data.addElement(DataElements.newPatientReligiousPreference());
		data.addElement(DataElements.newPixelData());
		data.addUniqueIdentifier(DataElements.newSOPInstanceUID(), sopInstanceUID);
		data.addUniqueIdentifier(DataElements.newSeriesInstanceUID(), seriesInstanceUID);
		data.addUniqueIdentifier(DataElements.newAffectedSOPClassUID(), affectedSopClassUID);
		data.addUniqueIdentifier(DataElements.newAffectedSOPInstanceUID(), affectedSopInstanceUID);
		data.addUniqueIdentifier(DataElements.newStudyInstanceUID(), studyInstanceUID);
		data.addCodeString(DataElements.newModalitiesInStudy(), modalityStudy);
		data.addElement(DataElements.newStudyDescription());

		message.setDataSet(data);
		byte lPres = associateSession
				.getSuitablePresentationContextID(sopClass);

		Thread lT = thFactory.newThread(new SendRunnable(association, lPres, message));
		lT.start();
	}
	
	/*
	 *  Method that handles a C-Find reply
	 */
	public void messageFind(Association association, DimseMessage message) throws Exception {
		
		CFindResponseMessage response = (CFindResponseMessage) message;
		logger.info("Response Status" + response.statusToString());
		if (response.isFailure() || response.isSuccess()) {
			association.sendReleaseRequest();
		}

		DataSet data = response.getDataSet();
		logger.info("Dati: \n" + data);
	}
	
	/*
	 *  Method that handles a C-Find request
	 *  
	 *  --- This method still does not work [server features] ---
	 */
	public void messageFindReq(Association association, byte presentationContextID, 
			DimseMessage message) throws Exception {
	}
	
	

}

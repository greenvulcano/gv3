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
import it.greenvulcano.gvesb.gvdicom.Constants;
import it.greenvulcano.gvesb.gvdicom.DicomAdapterException;
import it.greenvulcano.gvesb.gvdte.transformers.dicom.Dicom2XML;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.file.FileManager;
import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.BaseThreadFactory;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.dicom4j.data.DataElements;
import org.dicom4j.data.DataSet;
import org.dicom4j.dicom.DicomTags;
import org.dicom4j.dicom.network.dimse.DimseStatus;
import org.dicom4j.dicom.uniqueidentifiers.SOPClass;
import org.dicom4j.dicom.uniqueidentifiers.TransferSyntax;
import org.dicom4j.io.file.DicomFileWriter;
import org.dicom4j.io.media.DicomFile;
import org.dicom4j.network.association.Association;
import org.dicom4j.network.association.associate.AssociateSession;
import org.dicom4j.network.dimse.messages.CStoreRequestMessage;
import org.dicom4j.network.dimse.messages.CStoreResponseMessage;
import org.dicom4j.network.dimse.messages.DimseMessage;


/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class StoreSupport {
	private static final Logger 			logger 			= GVLogger.getLogger(StoreSupport.class);
	
	private static final ThreadFactory 		thFactory;
	
	private String 							storeDir;
	
	static {
		thFactory = new BaseThreadFactory("DICOM_StoreSupport", true);	
	}
	
	public StoreSupport() {
		try {
			storeDir = PropertiesHandler.expand(XMLConfig.get(Constants.CONF_FILE_NAME, "/GVDicomAdapterConfiguration/Local/@dicomStoreDirectory"));
			// check dir esistente
		}
		catch (Exception exc) {
			logger.error("GVDICOM_STORE-DIR_PROPERTIES_ERROR: " + exc.getMessage());
		}
	}
	
	/*
	 *  Method that throws an association request for a C-Store
	 */
	public void associationStore(Association association, AssociateSession associateSession,
			String sopClass, String SopInstance, String dicomFile) throws Exception {
		
		logger.info("Open connection for DicomStoreCallOperation");
		CStoreRequestMessage message = null;
		
		DicomFile df = new DicomFile(dicomFile); 
		df.open();

		DataSet data = new DataSet();
		data = df.getDataset();
		
		if (sopClass.equals(SOPClass.SecondaryCaptureImageStorage.getUID())) {
				message = association.getMessageFactory().newCStoreRequest(
						SOPClass.SecondaryCaptureImageStorage);
				data.addUniqueIdentifier(DataElements.newSOPClassUID(),
						SOPClass.SecondaryCaptureImageStorage.getUID());
		} else if (sopClass.equals(SOPClass.EnhancedCTImageStorage.getUID())) {
				message = association.getMessageFactory().newCStoreRequest(
						SOPClass.EnhancedCTImageStorage);
				data.addUniqueIdentifier(DataElements.newSOPClassUID(),
						SOPClass.EnhancedCTImageStorage.getUID());
		} else if (sopClass.equals(SOPClass.CTImageStorage.getUID())) {
				message = association.getMessageFactory().newCStoreRequest(
						SOPClass.CTImageStorage);
				data.addUniqueIdentifier(DataElements.newSOPClassUID(),
						SOPClass.CTImageStorage.getUID());
		} else if (sopClass.equals(SOPClass.MRImageStorage.getUID())) {
				message = association.getMessageFactory().newCStoreRequest(
						SOPClass.MRImageStorage);
				data.addUniqueIdentifier(DataElements.newSOPClassUID(),
						SOPClass.MRImageStorage.getUID());
		} else if (sopClass.equals(SOPClass.EnhancedMRImageStorage.getUID())) {
			message = association.getMessageFactory().newCStoreRequest(
					SOPClass.EnhancedMRImageStorage);
			data.addUniqueIdentifier(DataElements.newSOPClassUID(),
					SOPClass.EnhancedMRImageStorage.getUID());
		} else {
				logger.error("SOPClass is not supported");
		}
		if (SopInstance.equals("")) {
				logger.error("required field");
				return;
		}
		
		data.addUniqueIdentifier(DataElements.newSOPInstanceUID(), SopInstance);
		message.setAffectedSOPInstanceUID(SopInstance);
		message.setDataSet(data);
		
		if (associateSession.isAccepted(sopClass)) {
			byte lPres = associateSession
					.getSuitablePresentationContextID(sopClass);
			Thread lT = thFactory.newThread(new SendRunnable(association, lPres, message));
			lT.start();
		}
		else {
			logger.error("SopClass: " + sopClass + " not accepted");
		}
	}
	
	/*
	 *  Method that handles a C-Store reply
	 */
	public void messageStore(Association association, DimseMessage message) throws Exception {
		
		logger.debug("C-Store association established");
		logger.info("Response: " + ((CStoreResponseMessage)message).statusToString());
		
		association.sendReleaseRequest();
	}
	
	/*
	 *  Method that handles a C-Store request
	 */
	public void messageStoreReq(Association association, byte presentationContextID, 
			DimseMessage message) throws Exception, DicomAdapterException {
		
		logger.info("C-Store Request Message received");
		CStoreRequestMessage lRequest = (CStoreRequestMessage) message;
		String lSOPClassUID = lRequest.getAffectedSOPClassUID();
		try {
			DataSet lData = lRequest.getDataSet();
	
			String idPat = lData.getElement(DicomTags.PatientID).getSingleStringValue();
			String idImage = lData.getElement(DicomTags.SOPInstanceUID).getSingleStringValue();
			
			File base = new File(storeDir, idPat);
			Set<FileProperties> files = null;
			
			try {
				FileUtils.forceMkdir(base);
				files = FileManager.ls(base.getAbsolutePath(), idImage + ".dcm");
				if (!files.isEmpty()) {
					logger.debug("File " + idImage + ".dcm already exist on path: " + base.getAbsolutePath());
				} else {
					DicomFileWriter lWriter = new DicomFileWriter(new File(base, idImage + ".dcm"));
					lWriter.write(lData, TransferSyntax.Default);
					files = FileManager.ls(base.getAbsolutePath(), idImage + ".dcm");
					
					if (!files.isEmpty()) {
						logger.info("File " + idImage + ".dcm write correctly on path: " + base.getAbsolutePath());
					} else {
						logger.error("File " + idImage + ".dcm doesn't write correctly on path: "+ base.getAbsolutePath());
					}
				}
			} catch (Exception exc) {
				throw new DicomAdapterException("ERROR_IN_FILE_WRITING", new String[][]{{"message", exc.getMessage()}}, exc);
			}
			
			// the received image is converted to xml
			Dicom2XML d2x = new Dicom2XML();
			d2x.convertDicom2XML(lData, base.getAbsolutePath());
			
			CStoreResponseMessage lRsp = association.getMessageFactory().newCStoreResponseMessage(
					message.getMessageID(), lSOPClassUID, DimseStatus.Success);
			association.sendMessage(presentationContextID, lRsp);
		} catch (Exception exc){
			logger.error("DICOM_SEND-MESSAGE_STORE_RESPONSE-SUCCESS_ERROR: " + exc.getMessage());
			CStoreResponseMessage lRsp = association.getMessageFactory().newCStoreResponseMessage(
					message.getMessageID(), lSOPClassUID, DimseStatus.RefusedOutOfResources);
			try {
				association.sendMessage(presentationContextID, lRsp);
			} catch (Exception e) {
				throw new DicomAdapterException("GVDICOM_SEND-MESSAGE_STORE_RESPONSE-REFUSED_ERROR", 
						new String[][]{{"message", e.getMessage()}}, exc);
			}
		}
	}
}

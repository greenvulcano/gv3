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

import java.util.Iterator;

import org.dicom4j.dicom.uniqueidentifiers.SOPClass;
import org.dicom4j.network.NetworkStaticProperties.PresentationContextReasons;
import org.dicom4j.network.association.Association;
import org.dicom4j.network.association.associate.AssociateRequest;
import org.dicom4j.network.association.associate.AssociateResponse;
import org.dicom4j.network.association.listeners.DefaultAssociateRequestHandler;
import org.dicom4j.network.protocoldataunit.items.PresentationContexRequestItem;
import org.dicom4j.network.protocoldataunit.support.AbstractPresentationContextItem;

/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
@SuppressWarnings("deprecation")
public class DicomAssociateRequestHandler extends  DefaultAssociateRequestHandler {

	
	@Override
	public AssociateResponse requestReceived(Association aAssociation,
			AssociateRequest aAssociateRequest) {
		
		// create a default response
		AssociateResponse lResponse = this.createDefaultResponse(aAssociateRequest);
		
		// uses an iterator to analyze all presentation context
		Iterator<AbstractPresentationContextItem> lPres = aAssociateRequest.getPresentationIterator();
		while (lPres.hasNext()) {
			PresentationContexRequestItem lPresRQ = (PresentationContexRequestItem) lPres.next();
			String sopClass = lPresRQ.getAbstractSyntax();
						
				if (sopClass.equals(SOPClass.Verification.getUID())) {
					lResponse.addPresentationContext(lPresRQ.getID(),
							PresentationContextReasons.ACCEPTANCE,
							lPresRQ.getTransferSyntax(0));
				} else if (sopClass.equals(SOPClass.PatientRootQueryRetrieveInformationModelFIND.getUID())) {
					lResponse.addPresentationContext(lPresRQ.getID(),
							PresentationContextReasons.ACCEPTANCE,
							lPresRQ.getTransferSyntax(0));
				} else if (sopClass.equals(SOPClass.PatientStudyOnlyQueryRetrieveInformationModelFIND.getUID())) {
					lResponse.addPresentationContext(lPresRQ.getID(),
							PresentationContextReasons.ACCEPTANCE,
							lPresRQ.getTransferSyntax(0));
				} else if (sopClass.equals(SOPClass.ModalityWorklistInformationModelFIND.getUID())) {
					lResponse.addPresentationContext(lPresRQ.getID(),
							PresentationContextReasons.ACCEPTANCE,
							lPresRQ.getTransferSyntax(0));
				} else if (sopClass.equals(SOPClass.PatientRootQueryRetrieveInformationModelMOVE.getUID())) {
					lResponse.addPresentationContext(lPresRQ.getID(),
							PresentationContextReasons.ACCEPTANCE,
							lPresRQ.getTransferSyntax(0));
				} else if (sopClass.equals(SOPClass.PatientStudyOnlyQueryRetrieveInformationModelMOVE.getUID())) {
					lResponse.addPresentationContext(lPresRQ.getID(),
							PresentationContextReasons.ACCEPTANCE,
							lPresRQ.getTransferSyntax(0));
				} else if (sopClass.equals(SOPClass.CTImageStorage.getUID())) {
					lResponse.addPresentationContext(lPresRQ.getID(),
							PresentationContextReasons.ACCEPTANCE,
							lPresRQ.getTransferSyntax(0));
				} else if (sopClass.equals(SOPClass.EnhancedCTImageStorage.getUID())) {
					lResponse.addPresentationContext(lPresRQ.getID(),
							PresentationContextReasons.ACCEPTANCE,
							lPresRQ.getTransferSyntax(0));
				} else if (sopClass.equals(SOPClass.MRImageStorage.getUID())) {
					lResponse.addPresentationContext(lPresRQ.getID(),
							PresentationContextReasons.ACCEPTANCE,
							lPresRQ.getTransferSyntax(0));
				} else if (sopClass.equals(SOPClass.EnhancedMRImageStorage.getUID())) {
					lResponse.addPresentationContext(lPresRQ.getID(),
							PresentationContextReasons.ACCEPTANCE,
							lPresRQ.getTransferSyntax(0));
				} else if (sopClass.equals(SOPClass.XRayRadiofluoroscopicImageStorage.getUID())) {
					lResponse.addPresentationContext(lPresRQ.getID(),
							PresentationContextReasons.ACCEPTANCE,
							lPresRQ.getTransferSyntax(0));
				} else if (sopClass.equals(SOPClass.XRayAngiographicImageStorage.getUID())) {
					lResponse.addPresentationContext(lPresRQ.getID(),
							PresentationContextReasons.ACCEPTANCE,
							lPresRQ.getTransferSyntax(0));
				} else if (sopClass.equals(SOPClass.SecondaryCaptureImageStorage.getUID())) {
					lResponse.addPresentationContext(lPresRQ.getID(),
							PresentationContextReasons.ACCEPTANCE,
							lPresRQ.getTransferSyntax(0));
				} else {
					lResponse.addPresentationContext(lPresRQ.getID(),
							PresentationContextReasons.USER_REJECTION,
							lPresRQ.getTransferSyntax(0));
				}
		}
		
		return lResponse;
	}
	

}

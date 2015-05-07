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

import org.dicom4j.dicom.uniqueidentifiers.SOPClass;


/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class SOPClassConverter {

	public static String converter(String sop, String type){
		String result = null;
		
		if(type.equals("MOVE")){
			if(sop.equals("PATIENT_ROOT_QUERY_RIM")){
				result = SOPClass.PatientRootQueryRetrieveInformationModelMOVE.getUID();
			}else if(sop.equals("STUDY_ROOT_QUERY_RIM")){
				result = SOPClass.StudyRootQueryRetrieveInformationModelMOVE.getUID();
			}else if(sop.equals("PATIENT_SOQ_RIM")){
				result = SOPClass.PatientStudyOnlyQueryRetrieveInformationModelMOVE.getUID();
			}
		}else if(type.equals("FIND")){
			if(sop.equals("PATIENT_ROOT_QUERY_RIM")){
				result = SOPClass.PatientRootQueryRetrieveInformationModelFIND.getUID();
			}else if(sop.equals("STUDY_ROOT_QUERY_RIM")){
				result = SOPClass.StudyRootQueryRetrieveInformationModelFIND.getUID();
			}else if(sop.equals("PATIENT_SOQ_RIM")){
				result = SOPClass.PatientStudyOnlyQueryRetrieveInformationModelFIND.getUID();
			}else if(sop.equals("MODALITY_WORKLIST_INFORMATION_MODEL")){
				result = SOPClass.ModalityWorklistInformationModelFIND.getUID();
			}
		}else if(type.equals("STORE")){
			if(sop.equals("CT_IS")){
				result = SOPClass.CTImageStorage.getUID();
			}else if(sop.equals("MR_IS")){
				result = SOPClass.MRImageStorage.getUID();
			}else if(sop.equals("ENHANCED_MR_IS")){
				result = SOPClass.EnhancedMRImageStorage.getUID();
			}else if(sop.equals("ENHANCED_CT_IS")){
				result = SOPClass.EnhancedCTImageStorage.getUID();
			}else if(sop.equals("SECONDARY_CAPTURE_IS")){
				result = SOPClass.SecondaryCaptureImageStorage.getUID();
			}
		}
		return result;
	}
}

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
package it.greenvulcano.gvesb.virtual.dicom;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvdicom.GVDicomAssociationManager;
import it.greenvulcano.gvesb.gvdicom.listener.DicomAssociationListener;
import it.greenvulcano.gvesb.gvdicom.support.MoveSupport;
import it.greenvulcano.gvesb.gvdicom.support.SOPClassConverter;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dicom4j.network.association.Association;
import org.dicom4j.network.association.associate.AssociateSession;
import org.dicom4j.network.dimse.messages.CMoveResponseMessage;
import org.dicom4j.network.dimse.messages.DimseMessage;
import org.w3c.dom.Node;


/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class DicomMoveCallOperation extends DicomAssociationListener implements CallOperation
{
    private static Logger    			logger          	= GVLogger.getLogger(DicomMoveCallOperation.class);
    
    private String 						nameAssociation 	= "";
    
    private String 						sopClass 			= "";
    
    private String 						queryLevel 			= "";
    
    private String 						patientID 			= "";
    
    private String 						patientName 		= "";
    
    private String 						sopClassUID 		= "";
    
    private String 						studyInstanceUID 	= "";
    
    private String 						seriesInstanceUID 	= "";
    
    private GVBuffer 					currBuffer 			= null;
    
    private Map<String, Object> 		props 				= null;
    
    private List<CMoveResponseMessage> 	files 				= new ArrayList<CMoveResponseMessage>();
    
    private MoveSupport 				move 				= new MoveSupport();


    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        logger.debug("Init start");
       try {
    	   
    	   	this.nameAssociation = XMLConfig.get(node,  "@nameAssociation");
			this.sopClass = XMLConfig.get(node, "@sopClass");
			this.queryLevel = XMLConfig.get(node, "@queryLevel");
			this.patientID = XMLConfig.get(node, "@patientID", "");
			this.patientName = XMLConfig.get(node, "@patientName", "");
			this.sopClassUID = XMLConfig.get(node, "@sopClassUID", "");
			this.studyInstanceUID = XMLConfig.get(node, "@studyInstanceUID", "");
			this.seriesInstanceUID = XMLConfig.get(node, "@seriesInstanceUID", "");
    	   
        }
        catch (Exception exc) {
            throw new InitializationException("GV_INIT_SERVICE_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb
     * .buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException {
    	
    	try {
    		
    		files.clear();
    		currBuffer = gvBuffer;
    		
    		this.props = GVBufferPropertiesHelper.getPropertiesMapSO(currBuffer, true);
    		
    		String locNameAssociation = currBuffer.getProperty("DICOM_ASSOCIATION");
        	if ((locNameAssociation == null) || "".equals(locNameAssociation)) {
        		locNameAssociation = this.nameAssociation;
        	}
    		locNameAssociation = PropertiesHandler.expand(locNameAssociation, props, currBuffer);
    		
    		String locSopClass = currBuffer.getProperty("DICOM_SOP_CLASS");
        	if ((locSopClass == null) || "".equals(locSopClass)) {
        		locSopClass = this.sopClass;
        	}
        	locSopClass = PropertiesHandler.expand(locSopClass, props, currBuffer);
        	locSopClass = SOPClassConverter.converter(locSopClass, "MOVE");
    		
    		GVDicomAssociationManager.instance().createAssociation(locNameAssociation, locSopClass, this);
    		logger.debug("C-Move called on server: " + locNameAssociation);
    		while (isActive()) {
    			synchronized (this) {
					wait();
				}
    		}
    		gvBuffer.setObject(new ArrayList<CMoveResponseMessage>(files));
    		gvBuffer.setProperty("GVDICOM_FILES_COUNT", String.valueOf(files.size()));
    	
    	} catch (Exception exc) {
    		throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
    	}
    	
        return gvBuffer;
    }

	/*
	 * 
	 */
    @Override  
	public void associationOpened(Association association, 
			AssociateSession associateSession) throws Exception {
    	
    	String locSopClass = currBuffer.getProperty("DICOM_SOP_CLASS");
    	if ((locSopClass == null) || "".equals(locSopClass)) {
    		locSopClass = this.sopClass;
    	}
    	locSopClass = PropertiesHandler.expand(locSopClass, props, currBuffer);
    	locSopClass = SOPClassConverter.converter(locSopClass, "MOVE");
    	
    	String locQueryLevel = currBuffer.getProperty("DICOM_QUERY_LEVEL");
    	if ((locQueryLevel == null) || "".equals(locQueryLevel)) {
    		locQueryLevel = this.queryLevel;
    	}
    	locQueryLevel = PropertiesHandler.expand(locQueryLevel, props, currBuffer);
    	
    	String locPatientID = currBuffer.getProperty("DICOM_PATIENT_ID");
    	if ((locPatientID == null) || "".equals(locPatientID)) {
    		locPatientID = this.patientID;
    	}
    	locPatientID = PropertiesHandler.expand(locPatientID, props, currBuffer);
    	
    	String locPatientName = currBuffer.getProperty("DICOM_PATIENT_NAME");
    	if ((locPatientName == null) || "".equals(locPatientName)) {
    		locPatientName = this.patientName;
    	}
    	locPatientName = PropertiesHandler.expand(locPatientName, props, currBuffer);
    	
    	String locSopClassUID = currBuffer.getProperty("DICOM_SOP_CLASS_UID");
    	if ((locSopClassUID == null) || "".equals(locSopClassUID)) {
    		locSopClassUID = this.sopClassUID;
    	}
    	locSopClassUID = PropertiesHandler.expand(locSopClassUID, props, currBuffer);
    	
    	String locStudyInstanceUID = currBuffer.getProperty("DICOM_STUDY_INSTANCE_UID");
    	if ((locStudyInstanceUID == null) || "".equals(locStudyInstanceUID)) {
    		locStudyInstanceUID = this.studyInstanceUID;
    	}
    	locStudyInstanceUID = PropertiesHandler.expand(locStudyInstanceUID, props, currBuffer);
    	
    	String locSeriesInstanceUID = currBuffer.getProperty("DICOM_SERIES_INSTANCE_UID");
    	if ((locSeriesInstanceUID == null) || "".equals(locSeriesInstanceUID)) {
    		locSeriesInstanceUID = this.seriesInstanceUID;
    	}
    	locSeriesInstanceUID = PropertiesHandler.expand(locSeriesInstanceUID, props, currBuffer);
    	
    	logger.debug("Move: querylevel [" + locQueryLevel + "] patientID[" +  locPatientID + "] patientName[" + 
    			locPatientName + "] sopClassUID [" + locSopClassUID + "] seriesUID [" + locSeriesInstanceUID + 
    			"] studyUID [" + locStudyInstanceUID +  "] sopClass [" + locSopClass + "]");
    	
    	super.associationOpened(association, associateSession);
    	
    	/* --- what to do if it is an association for a Move --- */
    	move.associationMove(association, associateSession, locSopClass, locQueryLevel,
						locPatientID, locPatientName, locSopClassUID, locStudyInstanceUID,
						locSeriesInstanceUID);
    	
    }
    
    /*
     * 
     */
    @Override
	public void messageReceived(Association association,
			byte presentationContextID, DimseMessage message) throws Exception {
    	
    	if (message instanceof CMoveResponseMessage) {
    		
			/* --- what to do when it receives a Move reply --- */
			move.messageMove(association, message);
			
			if (message != null) {
				files.add((CMoveResponseMessage)message);
			}
		}
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp() {
    	
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy() {
    	files.clear();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano
     * .gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }


    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    @Override
    public OperationKey getKey()
    {
        return key;
    }
}

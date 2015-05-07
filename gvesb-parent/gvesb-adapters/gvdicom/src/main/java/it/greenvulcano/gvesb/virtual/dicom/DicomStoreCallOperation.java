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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvdicom.GVDicomAssociationManager;
import it.greenvulcano.gvesb.gvdicom.listener.DicomAssociationListener;
import it.greenvulcano.gvesb.gvdicom.support.SOPClassConverter;
import it.greenvulcano.gvesb.gvdicom.support.StoreSupport;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;

import org.apache.log4j.Logger;
import org.dicom4j.network.association.Association;
import org.dicom4j.network.association.associate.AssociateSession;
import org.dicom4j.network.dimse.messages.CStoreResponseMessage;
import org.dicom4j.network.dimse.messages.DimseMessage;
import org.w3c.dom.Node;


/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class DicomStoreCallOperation extends DicomAssociationListener implements CallOperation
{
    private static Logger    				logger     			= GVLogger.getLogger(DicomStoreCallOperation.class);
    
    private String 							nameAssociation 	= "";
    
    private String 							sopClass 			= "";
    
    private String 							sopInstanceUID 		= "";
    
    private String 							dicomFile 			= "";
    
    private GVBuffer 						currBuffer 			= null;
    
    private Map<String, Object> 			props 				= null;
    
    private List<CStoreResponseMessage> 	files 				= new ArrayList<CStoreResponseMessage>();
    
    private StoreSupport 					store 				= new StoreSupport();


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
			this.sopInstanceUID = XMLConfig.get(node, "@sopInstanceUID", "");
			this.dicomFile = XMLConfig.get(node, "@dicomFile", "");
    	   
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
        	locSopClass = SOPClassConverter.converter(locSopClass, "STORE");
    		
    		GVDicomAssociationManager.instance().createAssociation(locNameAssociation, locSopClass, this);
    		logger.debug("C-Store called on server: " + locNameAssociation);
    		while (isActive()) {
    			synchronized (this) {
					wait();
				}
    		}
    		gvBuffer.setObject(new ArrayList<CStoreResponseMessage>(files));
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
    	locSopClass = SOPClassConverter.converter(locSopClass, "STORE");
    	
    	String locSopInstanceUID = currBuffer.getProperty("DICOM_SOP_INSTANCE_UID");
    	if ((locSopInstanceUID == null) || "".equals(locSopInstanceUID)) {
    		locSopInstanceUID = this.sopInstanceUID;
    	}
    	locSopInstanceUID = PropertiesHandler.expand(locSopInstanceUID, props, currBuffer);
    	
    	String locDicomFile = currBuffer.getProperty("DICOM_FILE_PATH");
    	if ((locDicomFile == null) || "".equals(locDicomFile)) {
    		locDicomFile = this.dicomFile;
    	}
    	locDicomFile = PropertiesHandler.expand(locDicomFile, props, currBuffer);
    	
    	logger.debug("Store: sopClass [" + locSopClass + "] sopInstance [" + locSopInstanceUID +
    			"] dicomFile [" + locDicomFile + "]");
    	
    	super.associationOpened(association, associateSession);
    	
    	/* --- what to do if it is an association for a Store --- */
		store.associationStore(association, associateSession, locSopClass,
				locSopInstanceUID, locDicomFile);
    }
    
    /*
     * 
     */
    @Override
	public void messageReceived(Association association,
			byte presentationContextID, DimseMessage message) throws Exception {
    	
    	if (message instanceof CStoreResponseMessage) {
    		
			/* --- what to do when it receives a Store reply --- */
			store.messageStore(association, message);
			if (message != null) {
				files.add((CStoreResponseMessage)message);
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

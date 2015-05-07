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
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;
import org.dicom4j.network.association.Association;
import org.dicom4j.network.association.associate.AssociateAbort;
import org.dicom4j.network.association.associate.AssociateReject;
import org.dicom4j.network.association.associate.AssociateRelease;
import org.dicom4j.network.association.associate.AssociateSession;
import org.dicom4j.network.association.listeners.AssociationListener;


/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public abstract class DicomAssociationListener implements AssociationListener {
	
	protected static Logger 			logger;
	
	protected OperationKey 				key;
	
	protected String 					host;
	
	protected int 						port;
	
	protected String 					portStr;
	
	private boolean 					isActive 		= false;

	/*
	 * 
	 */
	public DicomAssociationListener() {
		logger = GVLogger.getLogger(DicomAssociationListener.class);
		host = null;
		portStr = null;
		port = -1;
	}
	
	@Override
	public void associateRelease(Association association, AssociateRelease arg1)
			throws Exception, DicomAdapterException {
		setActive(false);
		logger.debug("--- associateRelease ---");
		try {
			association.sendReleaseResponse();
		} catch (Exception exc) {
			throw new DicomAdapterException("GVDICOM_ASSOCIATE-RELEASE_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
		}
		
		synchronized (this) {
			this.notifyAll();
		}
	}

	@Override
	public void associationAborted(Association association, AssociateAbort arg1)
			throws Exception {
		setActive(false);
		logger.debug("--- associationAborted ---");
		synchronized (this) {
			this.notifyAll();
		}
	}

	@Override
	public void associationCreated(Association association) throws Exception {
		setActive(true);
		logger.debug("--- associationCreated ---");
	}

	@Override  
	public void associationOpened(Association association, 
			AssociateSession associateSession) throws Exception {
		setActive(true);
		logger.debug("--- associationOpened ---");
	}

	
	@Override
	public void associationRejected(Association association, AssociateReject arg1)
			throws Exception {
		setActive(false);
		logger.debug("--- associateRejected ---");
		synchronized (this) {
			this.notifyAll();
		}
	}

	@Override
	public void associationReleased(Association association) throws Exception {
		setActive(false);
		logger.debug("--- associationReleased ---");
		synchronized (this) {
			this.notifyAll();
		}
	}

	@Override
	public void exceptionCaught(Association association, Throwable exc) {
		setActive(false);
		logger.debug("--- exceptionCaught ---");
		synchronized (this) {
			this.notifyAll();
		}
	}


	protected void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isActive() {
		return isActive;
	}

}

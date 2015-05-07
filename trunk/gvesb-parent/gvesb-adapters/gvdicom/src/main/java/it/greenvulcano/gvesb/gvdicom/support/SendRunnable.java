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

import org.apache.log4j.Logger;
import org.dicom4j.network.association.Association;
import org.dicom4j.network.association.associate.AssociateAbort;
import org.dicom4j.network.dimse.messages.support.AbstractDimseMessage;


/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class SendRunnable implements Runnable {
	private static Logger 				logger 			= GVLogger.getLogger(SendRunnable.class);
	
	private Association 				fAssoc;
	
	private AbstractDimseMessage 		fMessage;
	
	private byte 						fPres;
	
	private boolean 					stopThread 		= true;
	
	
	public SendRunnable(Association aAssoc, byte aPres, AbstractDimseMessage aMessage) {
		this.fAssoc = aAssoc;
		this.fPres = aPres;
		this.fMessage = aMessage;
	}
	
	@Override
	public void run() {
		long inizio = System.currentTimeMillis();

		try {
			this.fAssoc.sendMessage(this.fPres, this.fMessage);
		} catch (Exception exc) {
			logger.error("DICOM_SEND-MESSAGE_ERROR_IN_THREAD: " + exc.getMessage());
		}
		
		while (stopThread){
			long endtime = System.currentTimeMillis();
			long tempoTrascorso = (endtime - inizio)/1000;
			if (tempoTrascorso >= 60){
			stopThread = false;
				try {
					this.fAssoc.sendAbort(AssociateAbort.ServiceUserAbort);
				} catch (Exception exc) {
					logger.error("DICOM_SEND-ABORT_ERROR_IN_THREAD: " + exc.getMessage());
				}
			}
		}
	}

}

package it.greenvulcano.gvesb.jbpm;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;
/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
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
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
/**
 * Call GVESB Service from JBPM.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 *         REVISION OK
 */

public class CallGreenVulcano {
	private static Logger       logger   = GVLogger.getLogger(CallGreenVulcano.class);
	private static String JBPM_SUBSYSTEM ="JBPM_SUBSYSTEM";
	
	public static final Object JbpmCallGreenVulcano(String system,
			                                        String service,
			                                        Id id,
			                                        String operation,
			                                        Object input) {
		
		GVBuffer gvBuffer = null;
		try {
		    logger.debug("JBPM init Call service "+service);
			gvBuffer = new GVBuffer();
			gvBuffer.setSystem(system);
			gvBuffer.setService(service);
			gvBuffer.setObject(input);
			if(id == null)
				id = new Id();
			gvBuffer.setId(id);
			GreenVulcanoPool greenVulcanoPool = GreenVulcanoPoolManager.instance().getGreenVulcanoPool(JBPM_SUBSYSTEM);
			if(greenVulcanoPool==null)
				throw new Exception("Erron cannot istantiate GreenVulcano Pool for JBPM_SUBSYSTEM");
			if(operation.equals("RequestReply"))
			  gvBuffer=greenVulcanoPool.requestReply(gvBuffer);
			else if (operation.equals("Request"))
				gvBuffer=greenVulcanoPool.request(gvBuffer);
			else if (operation.equals("getReply"))
				gvBuffer=greenVulcanoPool.getReply(gvBuffer);
			else if (operation.equals("getRequest"))
				gvBuffer=greenVulcanoPool.getRequest(gvBuffer);
			else if (operation.equals("sendReply"))
				gvBuffer=greenVulcanoPool.sendReply(gvBuffer);
			else
				throw new Exception("Erron Operation Name");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return gvBuffer.getObject();
	}
}

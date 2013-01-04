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
package it.greenvulcano.gvesb.axis2.handler;

import java.util.Iterator;

import it.greenvulcano.log.GVLogger;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.log4j.Logger;

public class SkipHeadersHandler extends AbstractHandler{
	    private static final Logger           log         = GVLogger.getLogger(SkipHeadersHandler.class);

	    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
	        log.info("Init InvocationResponse");	        
	        if (msgContext == null) {
	        	return InvocationResponse.CONTINUE;
	        }
	        
	        SOAPEnvelope envelope = msgContext.getEnvelope();
	        if (envelope.getHeader() == null) {
	        	return InvocationResponse.CONTINUE;
	        }
	        
	        
	        // Passing in null will get headers targeted for NEXT and ULTIMATE RECEIVER
	        Iterator<?> headerBlocks = envelope.getHeader().getHeadersToProcess(null);
	        while (headerBlocks.hasNext()) {
	            SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) headerBlocks.next();
	            headerBlock.setProcessed();
	        }
	        return InvocationResponse.CONTINUE;
	    }

	}


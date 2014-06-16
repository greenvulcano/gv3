/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.virtual.adam.filter;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvadam.AdamAdapterException;
import it.greenvulcano.util.xml.XMLUtils;
import net.sf.adam.an.ApplicationSession;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.5.0 27/may/2014
 * @author GreenVulcano Developer Team
 */
public interface Filter {
	public void init(Node node) throws AdamAdapterException;
	
	public void filter(XMLUtils parser, Element root, ApplicationSession session, GVBuffer gvBuffer) 
			throws AdamAdapterException, InterruptedException;
	
	public void cleanUp();
	
	public void destroy();
}

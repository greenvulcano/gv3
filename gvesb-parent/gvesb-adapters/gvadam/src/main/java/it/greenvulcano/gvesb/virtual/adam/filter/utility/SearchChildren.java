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
package it.greenvulcano.gvesb.virtual.adam.filter.utility;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvadam.AdamAdapterException;
import it.greenvulcano.gvesb.virtual.adam.filter.BaseFilter;
import it.greenvulcano.util.xml.XMLUtils;
import net.sf.adam.an.ApplicationSession;
import net.sf.adam.an.op.SearchOperation;
import net.sf.adam.core.entity.ElementCollection;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @version 3.5.0 06/giu/2014
 * @author GreenVulcano Developer Team
 *
 */
public class SearchChildren {

	private String returnType;
	private String elemType;
	
	public void init(Node node, String fatherRetType) throws AdamAdapterException {
		try {
			returnType = XMLConfig.get(node, "@return-type", fatherRetType);
			elemType = XMLConfig.get(node, "@elem-type", null);
		} catch (Exception exc) {
			throw new AdamAdapterException("Error in Filter initialization", exc);
		}
	}

	public void setReturnType(String returnType){
		this.returnType = returnType;
	}

	
	public void filter(XMLUtils parser, Element root, ApplicationSession session, String uuid, String id, Logger logger) 
			throws AdamAdapterException, InterruptedException { 
		SearchOperation searchOp = new SearchOperation(session, false, true);
		try {
			//if elemType is null return ALL types of children
			ElementCollection res = searchOp.children(uuid, elemType);
			BaseFilter.toXML(parser, root, session, res, returnType, null, null, getClass().getSimpleName(), id, logger);
		} finally {
			searchOp.close();
		}	
	}

}

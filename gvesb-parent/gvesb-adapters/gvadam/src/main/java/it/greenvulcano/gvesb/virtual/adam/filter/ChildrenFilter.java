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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvadam.AdamAdapterException;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.Map;

import net.sf.adam.an.ApplicationSession;
import net.sf.adam.an.op.SearchOperation;
import net.sf.adam.core.entity.ElementCollection;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.5.0 27/may/2014
 * @author GreenVulcano Developer Team
 */
public class ChildrenFilter extends BaseFilter {

	private static Logger logger = GVLogger.getLogger(ChildrenFilter.class);

	private String id;
	private String uuid;
	private String elemType;

	@Override
	public void init(Node node) throws AdamAdapterException {
		super.init(node);
		try {
			id = XMLConfig.get(node, "@id");
			uuid = XMLConfig.get(node, "@uniqueId");
			elemType = XMLConfig.get(node, "@elem-type", null);
		} catch (XMLConfigException exc) {
			throw new AdamAdapterException("Error in ChildrenFilter initialization", exc);
		}

	}

	@Override
	public void filter(XMLUtils parser, Element root, ApplicationSession session, GVBuffer gvBuffer)
			throws AdamAdapterException, InterruptedException {
		try {
			Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);

			String expandeduuid = PropertiesHandler.expand(uuid, params, gvBuffer);

			SearchOperation searchOp = new SearchOperation(session, false, true);
			try {
				ElementCollection res = searchOp.children(expandeduuid, elemType);
				toXML(parser, root, session, res, returnType, null, null, getClass().getSimpleName(), id, logger);
			} finally {
				searchOp.close();
			}
		} catch (Exception exc) {
			throw new AdamAdapterException("Error in ChildrenFilter filter operation", exc);
		}

	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void destroy() {

	}

}

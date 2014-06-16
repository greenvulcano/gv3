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
import it.greenvulcano.gvesb.gvadam.AdamAdapterException;
import it.greenvulcano.gvesb.virtual.adam.filter.utility.SearchChildren;
import it.greenvulcano.gvesb.virtual.adam.filter.utility.SearchLinksTo;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayOutputStream;

import net.sf.adam.an.ApplicationSession;
import net.sf.adam.an.op.ElementOperation;
import net.sf.adam.core.entity.ElementCollection;
import net.sf.adam.core.entity.ElementContentOutput;
import net.sf.adam.core.entity.ElementEntity;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @version 3.5.0 06/giu/2014
 * @author GreenVulcano Developer Team
 *
 */
public abstract class BaseFilter implements Filter {

	protected String returnType;
	protected String sortBy;
	protected SearchLinksTo linksTo = null;
	protected SearchChildren children = null;
	
	/* (non-Javadoc)
	 * @see it.greenvulcano.gvesb.virtual.adam.filter.Filter#init(org.w3c.dom.Node)
	 */
	@Override
	public void init(Node node) throws AdamAdapterException {
		try {
			returnType = XMLConfig.get(node, "@return-type", "metadata-only");
			sortBy = XMLConfig.get(node, "@sort-by");
			Node nLinksTo = XMLConfig.getNode(node, "linksTo");
			if (nLinksTo != null){
				String innerRetType = XMLConfig.get(nLinksTo, "@return-type");
				linksTo = new SearchLinksTo(); 
				linksTo.init(nLinksTo, returnType);
				if (innerRetType != null){
					linksTo.setReturnType(innerRetType);
				}
			}
			Node nChildren = XMLConfig.getNode(node, "children");
			if (nChildren != null){
				String innerRetType = XMLConfig.get(nChildren, "@return-type");
				children = new SearchChildren(); 
				children.init(nChildren, returnType);
				if (innerRetType != null){
					children.setReturnType(innerRetType);
				}				
			}
		} catch (Exception exc) {
			throw new AdamAdapterException("Error in Filter initialization", exc);
		}
		

	}

	public static void toXML(XMLUtils parser, Element root, ApplicationSession session, ElementCollection res, String returnType, SearchLinksTo linksTo, SearchChildren children, String className, String id,
			Logger logger) throws AdamAdapterException, InterruptedException {
		try {

			if (res != null) {
				ElementOperation elOp = session.getElementOperation();
				
				Element row = null;
				for (int i = 0; i < res.getSize(); i++) {

					if (i % 10 == 0) {
						ThreadUtils.checkInterrupted(className, id, logger);
					}

					ElementEntity ee = res.getElement(i);

					row = parser.insertElement(root, "record");

					parser.setAttribute(row, "id", id);
					parser.setAttribute(row, "name", ee.getName());
					parser.setAttribute(row, "uuid", ee.getUuid());
					parser.setAttribute(row, "archive-id", ee.getArcid());
					parser.setAttribute(row, "type", ee.getType());
					
					//USELESS?
					parser.setAttribute(row, "couid", ee.getCheckedOutUserid());
					parser.setAttribute(row, "version", "" + ee.getLastVersionNum());
					parser.setAttribute(row, "version-creator", ee.getVersionCreator());

					String metadata = ee.getMetadata();
					if (metadata != null){
						Document meta = parser.parseDOM(metadata);
						parser.insertElement(row, "metadata").appendChild(
								row.getOwnerDocument().adoptNode(meta.getDocumentElement()));
					}

					String mimetype = ee.getMimetype();
					if ((mimetype != null) && returnType.equals("all")) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ElementContentOutput elOut = new ElementContentOutput(baos);

						elOp.readContent(ee.getUuid(), ee.getArcid(), ee.getLastVersionNum(), elOut);
						Element data = parser.insertElement(row, "data");
						parser.setAttribute(data, "mime-type", mimetype);
						parser.insertText(data, Base64.encodeBase64String(baos.toByteArray()));
					}

					if (linksTo != null){
						Element linksToN = parser.insertElement(row, "linksTo");
						linksTo.filter(parser, linksToN, session, ee.getUuid(), id, logger);
					}
					if (children != null){
						Element childrenN = parser.insertElement(row, "children");
						children.filter(parser, childrenN, session, ee.getUuid(), id, logger);
					}
					
				}

			}
		} catch (Exception exc) {
			throw new AdamAdapterException("Error XML document creation", exc);
		}

	}
	
	@Override
	public void destroy() {
		children = null;
		linksTo = null;
	}
	
	@Override
	public void cleanUp() {
		//Do nothing
	}

}

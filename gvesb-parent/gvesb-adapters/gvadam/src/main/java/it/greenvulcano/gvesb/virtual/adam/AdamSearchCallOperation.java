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
package it.greenvulcano.gvesb.virtual.adam;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvadam.GVAdamManager;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.gvesb.virtual.adam.filter.Filter;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.ArrayList;
import java.util.List;

import net.sf.adam.an.ApplicationSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * 
 * @version 3.4.0 30/apr/2014
 * @author GreenVulcano Developer Team
 */
public class AdamSearchCallOperation implements CallOperation
{
    private static Logger    logger          = GVLogger.getLogger(AdamSearchCallOperation.class);

    private OperationKey     key             = null;
    private String           name            = null;
    private String           uSession        = null;
    private String           archive         = null;
    private List<Filter>     filters         = new ArrayList<Filter>();



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
            name = XMLConfig.get(node, "@name");
            uSession = XMLConfig.get(node, "@user-session");
            archive = XMLConfig.get(node, "@arch-name");

            logger.debug("init - loaded parameters: name = " + name + " - user-session: " + uSession + " - archive: " + archive);

            logger.debug("Listing for Filters.");
            NodeList fNodes = XMLConfig.getNodeList(node, "*[@type='filter']");
            Filter filter = null;
            for (int i = 0; i < fNodes.getLength(); i++) {
                Node fn = fNodes.item(i);
                String clazz = XMLConfig.get(fn, "@class");

                filter = (Filter) Class.forName(clazz).newInstance();
                filter.init(fn);
                filters.add(filter);
                logger.debug("Added a Filter class [" + clazz + "].");
            }
            logger.debug("Init stop");
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
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
    	XMLUtils parser = null;
    	
        try {
        	ApplicationSession session = GVAdamManager.instance().getSession(uSession, archive);
        	
	        parser = XMLUtils.getParserInstance();
        	Document doc = parser.newDocument("AdamResult");
        	
        	for (Filter filter : filters) {
    			filter.filter(parser, doc.getDocumentElement(), session, gvBuffer);
    		}

            gvBuffer.setObject(doc);
        }
        catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
        finally {
        	XMLUtils.releaseParserInstance(parser);
        }
        return gvBuffer;
    }

	

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
    	for (Filter filter : filters) {
			filter.cleanUp();
		}
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
    	for (Filter filter : filters) {
			filter.destroy();
		}
    	filters.clear();
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

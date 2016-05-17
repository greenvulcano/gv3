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
package it.greenvulcano.gvesb.virtual.pushnot;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvpushnot.publisher.GVPushNotificationManager;
import it.greenvulcano.gvesb.gvpushnot.publisher.Notification;
import it.greenvulcano.gvesb.gvpushnot.publisher.NotificationResult;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @version 3.4.0 16/feb/2015
 * @author GreenVulcano Developer Team
 */
public class PushNotificationCallOperation implements CallOperation
{
    private static Logger    logger          = GVLogger.getLogger(PushNotificationCallOperation.class);

    private OperationKey     key             = null;
    private String           defEngine       = null;

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
            defEngine = XMLConfig.get(node, "@defaultEngine");

            logger.debug("init - loaded parameters: defEngine = " + defEngine);

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
        	parser = XMLUtils.getParserInstance();
        	
            Object input = gvBuffer.getObject();
            Node pnXmlIn = parser.parseObject(input, false, true);

            List<Notification> nList = new ArrayList<Notification>();

            NodeList nl = parser.selectNodeList(pnXmlIn, "/NotificationList/Notification");
            for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				String eng = parser.get(n, "@engine", defEngine);
				if ("".equals(eng)) {
					eng = defEngine;
				}
				String id  = parser.get(n, "@id");
				String dest_key  = parser.get(n, "@destination_key");
				String dest_id  = parser.get(n, "@destination_id");
				String data = parser.get(n, ".");
				Notification not = new Notification(eng, id, data);
				/*if ((dest_id != null) && !"".equals(dest_id)) {
					not.addRegistrationId(dest_id);
				}*/
				if ((dest_id != null) && !"".equals(dest_id)) {
					not.addRegistrationId(dest_key, dest_id);
				}
	            nList.add(not);				
			}

            NotificationResult nr = GVPushNotificationManager.instance().push(nList);
            
            gvBuffer.setObject(nr.toXML());
        }
        catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
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
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
    	// do nothing
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

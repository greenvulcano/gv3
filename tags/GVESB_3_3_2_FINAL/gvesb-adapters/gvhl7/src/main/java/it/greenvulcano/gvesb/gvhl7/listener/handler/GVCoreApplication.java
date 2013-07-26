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
package it.greenvulcano.gvesb.gvhl7.listener.handler;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.gvhl7.listener.GVHL7ListenerManager;
import it.greenvulcano.gvesb.gvhl7.listener.HL7AdapterException;
import it.greenvulcano.gvesb.identity.GVIdentityHelper;
import it.greenvulcano.gvesb.identity.gvhl7.listener.HL7IdentityInfo;
import it.greenvulcano.gvesb.j2ee.XAHelper;
import it.greenvulcano.gvesb.j2ee.XAHelperException;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.ThreadMap;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.app.ThreadUtils;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.GenericParser;

/**
 * 
 * @version 3.0.0 28/set/2010
 * @author GreenVulcano Developer Team
 */
public class GVCoreApplication implements Application
{
    private static Logger  logger      = GVLogger.getLogger(GVCoreApplication.class);

    private String         name;
    private String         system;
    private String         service;
    private String         operation;
    private List<String[]> activations = new ArrayList<String[]>();
    private String         serverName;
    private boolean        transacted  = false;
    private int	           txTimeout   = 30; 
    private XAHelper       xaHelper    = null;

    /**
     *
     */
    public void init(Node node) throws HL7AdapterException
    {
        try {
        	serverName = JMXEntryPoint.getServerName();
            name = XMLConfig.get(node, "@name");
            system = XMLConfig.get(node, "@gv-system", GVBuffer.DEFAULT_SYS);
            service = XMLConfig.get(node, "@gv-service");
            operation = XMLConfig.get(node, "@gv-operation");
            transacted = XMLConfig.getBoolean(node, "@transacted", false);
            txTimeout = XMLConfig.getInteger(node, "@tx-timeout", 30);
            
            if (transacted) {
            	xaHelper = new XAHelper();
            }

            NodeList nl = XMLConfig.getNodeList(node, "HL7Activations/HL7Activation");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                activations.add(new String[]{XMLConfig.get(n, "@messageType"), XMLConfig.get(n, "@triggerEvent")});
            }
            if (activations.size() == 0) {
                throw new HL7AdapterException("Activation list empty");
            }
        }
        catch (Exception exc) {
            throw new HL7AdapterException("GVHL7_APPLICATION_INIT_ERROR", exc);
        }
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the activation
     */
    public List<String[]> getActivations()
    {
        return Collections.unmodifiableList(this.activations);
    }

    /*
     * (non-Javadoc)
     *
     * @see ca.uhn.hl7v2.app.Application#canProcess(ca.uhn.hl7v2.model.Message)
     */
    @Override
    public boolean canProcess(Message msgIn)
    {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * ca.uhn.hl7v2.app.Application#processMessage(ca.uhn.hl7v2.model.Message)
     */
    @Override
    public Message processMessage(Message msgIn) throws ApplicationException, HL7Exception
    {
        DefaultXMLParser hl7XmlParser = new DefaultXMLParser();
        GenericParser hl7StringParser = new GenericParser();
        Message msgOut = null;
        boolean forceTxRollBack = false;
        boolean inError = true;

        NMDC.push();
        NMDC.clear();
        NMDC.setServer(serverName);
        NMDC.setSubSystem(GVHL7ListenerManager.SUBSYSTEM);
        
        GVIdentityHelper.push(new HL7IdentityInfo(this.name));
        try {
            logger.debug("BEGIN Operation");
            logger.debug("Input StringMessage:\n" + msgIn.encode());
            String hl7MsgXmlIn = hl7XmlParser.encode(msgIn);
            logger.debug("Input XMLMessage:\n" + hl7MsgXmlIn);

            GVBuffer in = new GVBuffer(system, service);
            in.setObject(hl7MsgXmlIn);
            in.setProperty("HL7_REMOTE_ADDR", ThreadUtils.getIPRef());
            
            if (transacted) {
                xaHelper.begin();
                if (txTimeout > 0) {
                    xaHelper.setTransactionTimeout(txTimeout);
                }
                logger.debug("Begin transaction: " + xaHelper.getTransaction());
            }

            GVBuffer out = getGreenVulcanoPool().forward(in, operation);

            String hl7MsgXmlOut = "";
            if (out.getObject() instanceof String) {
                hl7MsgXmlOut = (String) out.getObject();
            }
            else if (out.getObject() instanceof byte[]) {
                hl7MsgXmlOut = new String((byte[]) out.getObject());
            }
            else if (out.getObject() instanceof Node) {
                hl7MsgXmlOut = XMLUtils.serializeDOM_S((Node) out.getObject());
            }

            logger.debug("Output XMLMessage:\n" + hl7MsgXmlOut);
            msgOut = hl7XmlParser.parse(hl7MsgXmlOut);
            msgOut.setParser(hl7StringParser);
            logger.debug("Output StringMessage:\n" + msgOut.encode());
            
            if ("Y".equalsIgnoreCase(out.getProperty("HL7_FORCE_TX_ROLLBACK"))) {
                logger.warn("Output contains HL7_FORCE_TX_ROLLBACK=Y : prepare to roll back transaction");
                forceTxRollBack = true;
            }
            inError = false;
        }
        catch (Exception exc) {
            logger.error("Error processing HL7 message", exc);
        }
        finally {
            if (transacted) {
                try {
                    if (inError || forceTxRollBack) {
                        logger.warn("Rolling back transaction: " + xaHelper.getTransaction());
                        xaHelper.rollback();
                    }
                    else {
                        logger.debug("Commiting transaction: " + xaHelper.getTransaction());
                        xaHelper.commit();
                    }
                }
                catch (Exception exc) {
                    logger.error("Error handling tansaction", exc);
                }
            }
            logger.debug("END Operation");

            GVIdentityHelper.pop();
            NMDC.pop();
            ThreadMap.clean();
        }
        return msgOut;
    }

    /**
     * @return
     * @throws HL7AdapterException
     */
    private GreenVulcanoPool getGreenVulcanoPool() throws HL7AdapterException
    {
        return GVHL7ListenerManager.instance().getGreenVulcanoPool();
    }

}

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
package it.greenvulcano.gvesb.axis2.config;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.ChangeGVBuffer;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 May 29, 2010
 * @author GreenVulcano Developer Team
 * 
 */
public class Binding
{
	private static Logger  logger     = GVLogger.getLogger(Binding.class);
	
    private String         gvService;
    private String         gvSystem;
    private String         inputType;
    private String         gvOperation;
    private ChangeGVBuffer cGVBuffer  = null;
    private boolean        transacted = false;
    private int            txTimeout  = -1;

    /**
     * @param bindingConf
     * @throws XMLConfigException
     */
    public void init(Node bindingConf) throws XMLConfigException
    {
        gvSystem = XMLConfig.get(bindingConf, "@gv-system", GVBuffer.DEFAULT_SYS);
        gvService = XMLConfig.get(bindingConf, "@gv-service");
        gvOperation = XMLConfig.get(bindingConf, "@gv-operation");
        inputType = XMLConfig.get(bindingConf, "@inputType", "context");
        transacted = XMLConfig.getBoolean(bindingConf, "@transacted", false);
        txTimeout = XMLConfig.getInteger(bindingConf, "@tx-timeout", 30);

        Node cGVBufferNode = XMLConfig.getNode(bindingConf, "ChangeGVBuffer");
        if (cGVBufferNode != null) {
            cGVBuffer = new ChangeGVBuffer();
            cGVBuffer.setLogger(logger);
            try {
                cGVBuffer.init(cGVBufferNode);
            }
            catch (XMLConfigException exc) {
                logger.error("Error initializing ChangeGVBuffer", exc);
                throw exc;
            }
        }
    }

    /**
     * @return the GV System
     */
    public String getGvSystem()
    {
        return gvSystem;
    }

    /**
     * @return the GV Service
     */
    public String getGvService()
    {
        return gvService;
    }

    /**
     * @return the inputType
     */
    public String getInputType()
    {
        return inputType;
    }

    /**
     * @return the GV operation
     */
    public String getGvOperation()
    {
        return gvOperation;
    }

    /**
     * @return the cGVBuffer
     */
    public ChangeGVBuffer getcGVBuffer()
    {
        return this.cGVBuffer;
    }


    /**
     * @return the transacted
     */
    public boolean isTransacted()
    {
        return this.transacted;
    }


    /**
     * @return the txTimeout
     */
    public int getTxTimeout()
    {
        return this.txTimeout;
    }

    @Override
    protected void finalize() throws Throwable {
        if (cGVBuffer != null) {
            cGVBuffer.destroy();
        }
        super.finalize();
    }
}

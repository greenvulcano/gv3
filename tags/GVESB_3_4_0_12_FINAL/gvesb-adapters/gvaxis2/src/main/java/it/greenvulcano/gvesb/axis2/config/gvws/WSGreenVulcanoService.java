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
package it.greenvulcano.gvesb.axis2.config.gvws;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import org.w3c.dom.Node;

/**
 * @version 3.3.0 Feb 2, 2013
 * @author GreenVulcano Developer Team
 * 
 */
public class WSGreenVulcanoService
{
    private String  serviceName;
    private String  operation;
    private String  inputOdp;
    private String  outputOdp;
    private Boolean  forceHttps;
    private Boolean  transacted;
    private int  txTimeout;

    /**
     * Initializes reading the web service operation configuration.
     *
     * @param config
     *        configuration node
     * @throws XMLConfigException
     */
    public void init(Node config) throws XMLConfigException
    {
        serviceName = XMLConfig.get(config, "@gv-service");
        setOperation(XMLConfig.get(config, "@gv-operation"));
        setInputOdp(XMLConfig.get(config, "@input-dp"));
        setOutputOdp(XMLConfig.get(config, "@output-dp"));

        setForceHttps(XMLConfig.getBoolean(config, "@force-https",false));
        setTransacted(XMLConfig.getBoolean(config, "@transacted",false));
        setTxTimeout(XMLConfig.getInteger(config, "@tx-timeout", 30));
    }

    /**
     * @return the serviceName
     */
    public Object getServiceName()
    {
        return serviceName;
    }

    /**
     * @return the inputOdp
     */
    public String getInputOdp() {
        return inputOdp;
    }

    public void setInputOdp(String inputOdp) {
        this.inputOdp = inputOdp;
    }

    /**
     * @return the outputOdp
     */
    public String getOutputOdp() {
        return outputOdp;
    }

    public void setOutputOdp(String outputOdp) {
        this.outputOdp = outputOdp;
    }

    /**
     * @return the forceHttps
     */
    public Boolean getForceHttps() {
        return forceHttps;
    }

    public void setForceHttps(Boolean forceHttps) {
        this.forceHttps = forceHttps;
    }

    /**
    * @return the transacted
    */
    public Boolean getTransacted() {
        return transacted;
    }

    public void setTransacted(Boolean transacted) {
        this.transacted = transacted;
    }

    /**
     * @return the txTimeout
    */
    public int getTxTimeout() {
        return txTimeout;
    }

    public void setTxTimeout(int txTimeout) {
        this.txTimeout = txTimeout;
    }

    /**
     * @return the operation
    */
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}

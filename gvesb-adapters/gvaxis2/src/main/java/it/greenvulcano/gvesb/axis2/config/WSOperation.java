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

import org.w3c.dom.Node;

/**
 * @version 3.0.0 May 29, 2010
 * @author nunzio
 *
 */
public class WSOperation
{

    private String  operationQname;
    private Binding binding;
    private String  refDp;

    /**
     * Initializes reading the web service operation configuration.
     *
     * @param config
     *        configuration node
     * @throws XMLConfigException
     */
    public void init(Node config) throws XMLConfigException
    {
        operationQname = XMLConfig.get(config, "@operation-qname");
        refDp = XMLConfig.get(config, "@ref-dp");
        Node bindingConf = XMLConfig.getNode(config, "Binding");
        binding = new Binding();
        binding.init(bindingConf);
    }

    /**
     * @return the operationQname
     */
    public Object getOperationQname()
    {
        return operationQname;
    }

    /**
     * @return the binding
     */
    public Binding getBinding()
    {
        return binding;
    }

    /**
     * @return the referenced data provider
     */
    public String getRefDp()
    {
        return refDp;
    }

}

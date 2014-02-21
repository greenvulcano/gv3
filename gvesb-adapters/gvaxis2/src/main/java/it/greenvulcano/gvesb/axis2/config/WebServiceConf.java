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
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 May 29, 2010
 * @author nunzio
 *
 */
public class WebServiceConf
{
    private String        webService;

    private WSOperation[] wsOperations;

    /**
     * Initializes reading the web service configuration
     *
     * @param config
     *        configuration node
     * @throws XMLConfigException
     */
    public void init(Node config) throws XMLConfigException
    {
        webService = XMLConfig.get(config, "@web-service");
        NodeList wsOperationList = XMLConfig.getNodeList(config, "WSOperation");
        if (wsOperationList != null && wsOperationList.getLength() > 0) {
            wsOperations = new WSOperation[wsOperationList.getLength()];
            for (int i = 0; i < wsOperationList.getLength(); i++) {
                wsOperations[i] = new WSOperation();
                wsOperations[i].init(wsOperationList.item(i));
            }
        }
    }

    /**
     * @return the webService
     */
    public String getWebService()
    {
        return webService;
    }

    /**
     * @return the wsOperations
     */
    public WSOperation[] getWSOperations()
    {
        return wsOperations;
    }
}

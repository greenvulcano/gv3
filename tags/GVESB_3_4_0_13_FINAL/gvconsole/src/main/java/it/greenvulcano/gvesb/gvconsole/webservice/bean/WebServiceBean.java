/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.greenvulcano.gvesb.gvconsole.webservice.bean;

import it.greenvulcano.configuration.XMLConfig;

import org.w3c.dom.Node;

/**
 *  WebServiceBean class
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
*/
public class WebServiceBean
{
    /**
     * The webService name
     */
    private String webService;

    /**
     * The webService config node
     */
    private Node   nodeConfig;

    /**
     * Default constructor.
     */
    public WebServiceBean()
    {
        webService = "";
    }

    /**
     * Constructor with param.
     *
     * @param nodeConfig
     */
    public WebServiceBean(Node nodeConfig)
    {
        this.nodeConfig = nodeConfig;
        webService = XMLConfig.get(nodeConfig,"@web-service", "");
    }

    /**
     * @return Returns the webService.
     */
    public String getWebService()
    {
        return webService;
    }

    /**
     * @param webService
     *        The webService to set.
     */
    public void setWebService(String webService)
    {
        this.webService = webService;
    }

    /**
     * @return Returns the nodeConfig.
     */
    public Node getNodeConfig()
    {
        return nodeConfig;
    }

    /**
     * @param nodeConfig
     *        The nodeConfig to set.
     */
    public void setNodeConfig(Node nodeConfig)
    {
        this.nodeConfig = nodeConfig;
    }
}

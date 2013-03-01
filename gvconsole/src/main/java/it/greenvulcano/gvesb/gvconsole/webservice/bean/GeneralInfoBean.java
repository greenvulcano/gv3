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
package it.greenvulcano.gvesb.gvconsole.webservice.bean;

import it.greenvulcano.configuration.XMLConfig;

import org.w3c.dom.Node;

/**
 * GeneralInfoBean class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GeneralInfoBean
{

    private String id;
    private String urlp;
    private String urli;
    private String organization;

    /**
     * Constructor.
     *
     */
    public GeneralInfoBean()
    {
        id = "";
        urlp = "";
        urli = "";
        organization = "";
    }

    /**
     * Constructor with param.
     *
     * @param nodeConfig
     */
    public GeneralInfoBean(Node nodeConfig)
    {
        this();
        if (nodeConfig != null) {
            id = XMLConfig.get(nodeConfig, "@id-registry", "");
            urlp = XMLConfig.get(nodeConfig, "@publish-url", "");
            urli = XMLConfig.get(nodeConfig, "@query-url", "");
            organization = XMLConfig.get(nodeConfig, "@organization-name", "");
        }
    }

    /**
     * @param organization
     */
    public void setOrganization(String organization)
    {
        this.organization = organization;
    }

    /**
     * @param id
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @param urlp
     */
    public void setUrlp(String urlp)
    {
        this.urlp = urlp;
    }

    /**
     * @param urli
     */
    public void setUrli(String urli)
    {
        this.urli = urli;
    }

    /**
     * @return
     */
    public String getOrganization()
    {
        return organization;
    }

    /**
     * @return
     */
    public String getId()
    {
        return id;
    }

    /**
     * @return
     */
    public String getUrlp()
    {
        return urlp;
    }

    /**
     * @return
     */
    public String getUrli()
    {
        return urli;
    }

}

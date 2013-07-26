/*
 * Created on 31-mag-2005
 *
 */
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

/**
 *
 * ServiceBean class
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
*/
public class ServiceBean
{
    /**
     * description.
     */
    private String  description = "";
    /**
     * serviceName.
     */
    private String  serviceName = "";
    /**
     * serviceKey.
     */
    private String  serviceKey  = "";
    /**
     * accessURL.
     */
    private String  accessURL   = "";
    /**
     * accessType.
     */
    private String  accessType  = "";
    /**
     * bindingKey.
     */
    private String  bindingKey  = "";
    /**
     * overviewURL.
     */
    private String  overviewURL = "";
    /**
     * flag.
     */
    private boolean flag       = false;


    public void setFlag(boolean flag)
    {
        this.flag = flag;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setServiceKey(String serviceKey)
    {
        this.serviceKey = serviceKey;
    }

    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }

    public void setAccessURL(String accessURL)
    {
        this.accessURL = accessURL;
    }

    public void setAccessType(String accessType)
    {
        this.accessType = accessType;
    }

    public void setBindingKey(String bindingKey)
    {
        this.bindingKey = bindingKey;
    }

    public void setOverviewURL(String overviewURL)
    {
        this.overviewURL = overviewURL;
    }

    public String getDescription()
    {
        return description;
    }

    public String getServiceKey()
    {
        return serviceKey;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public String getAccessURL()
    {
        return accessURL;
    }

    public String getAccessType()
    {
        return accessType;
    }

    public String getBindingKey()
    {
        return bindingKey;
    }

    public String getOverviewURL()
    {
        return overviewURL;
    }

    public boolean getFlag()
    {
        return flag;
    }

}

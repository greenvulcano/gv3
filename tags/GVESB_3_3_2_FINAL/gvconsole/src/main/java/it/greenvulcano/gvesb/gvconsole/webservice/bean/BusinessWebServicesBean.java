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
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class BusinessWebServicesBean
{
    /**
     * The authenticated HTTP soap address
     */
    private String                      authenticatedHttpSoapAddress;

    /**
     * The authenticated HTTPS soap address
     */
    private String                      authenticatedHttpsSoapAddress;

    /**
     * The HTTP soap address
     */
    private String                      httpSoapAddress;

    /**
     * The HTTPS soap address
     */
    private String                      httpsSoapAddress;

    /**
     * The WSDL directory
     */
    private String                      wsdlDirectory;

    /**
     * The services directory
     */
    private String                      servicesDirectory;

    /**
     * map web services.
     */
    private Map<String, WebServiceBean> webServicesBeanMap = null;

    /**
     * Default constructor.
     */
    public BusinessWebServicesBean()
    {
    }

    /**
     * Constructor with parameters.
     *
     * @param nodeConfig
     * @throws XMLConfigException
     */
    public BusinessWebServicesBean(Node nodeConfig) throws XMLConfigException
    {
        authenticatedHttpSoapAddress = XMLConfig.get(nodeConfig, "@authenticated-http-soap-address", "");
        authenticatedHttpsSoapAddress = XMLConfig.get(nodeConfig, "@authenticated-https-soap-address", "");
        httpSoapAddress = XMLConfig.get(nodeConfig, "@http-soap-address", "");
        httpsSoapAddress = XMLConfig.get(nodeConfig, "@https-soap-address", "");
        wsdlDirectory = XMLConfig.get(nodeConfig, "@wsdl-directory", "");
        if (!PropertiesHandler.isExpanded(wsdlDirectory)) {
            try {
                wsdlDirectory = PropertiesHandler.expand(wsdlDirectory, null);
            }
            catch (PropertiesHandlerException exc) {
                exc.printStackTrace();
            }
        }
        servicesDirectory = XMLConfig.get(nodeConfig, "@services-directory", "");
        if (!PropertiesHandler.isExpanded(servicesDirectory)) {
            try {
                servicesDirectory = PropertiesHandler.expand(servicesDirectory, null);
            }
            catch (PropertiesHandlerException exc) {
                exc.printStackTrace();
            }
        }
        NodeList webServicesList = XMLConfig.getNodeList(nodeConfig, "WebService");

        webServicesBeanMap = new HashMap<String, WebServiceBean>();
        for (int i = 0; i < webServicesList.getLength(); i++) {
            Node serviceConfig = webServicesList.item(i);
            webServicesBeanMap.put(XMLConfig.get(serviceConfig, "@web-service"), new WebServiceBean(serviceConfig));
        }
    }

    /**
     * @return Returns the authenticatedHttpSoapAddress.
     */
    public String getAuthenticatedHttpSoapAddress()
    {
        return authenticatedHttpSoapAddress;
    }

    /**
     * @param authenticatedHttpSoapAddress
     *        The authenticatedHttpSoapAddress to set.
     */
    public void setAuthenticatedHttpSoapAddress(String authenticatedHttpSoapAddress)
    {
        this.authenticatedHttpSoapAddress = authenticatedHttpSoapAddress;
    }

    /**
     * @return Returns the authenticatedHttpsSoapAddress.
     */
    public String getAuthenticatedHttpsSoapAddress()
    {
        return authenticatedHttpsSoapAddress;
    }

    /**
     * @param authenticatedHttpsSoapAddress
     *        The authenticatedHttpsSoapAddress to set.
     */
    public void setAuthenticatedHttpsSoapAddress(String authenticatedHttpsSoapAddress)
    {
        this.authenticatedHttpsSoapAddress = authenticatedHttpsSoapAddress;
    }

    /**
     * @return Returns the httpSoapAddress.
     */
    public String getHttpSoapAddress()
    {
        return httpSoapAddress;
    }

    /**
     * @param httpSoapAddress
     *        The httpSoapAddress to set.
     */
    public void setHttpSoapAddress(String httpSoapAddress)
    {
        this.httpSoapAddress = httpSoapAddress;
    }

    /**
     * @return Returns the httpsSoapAddress.
     */
    public String getHttpsSoapAddress()
    {
        return httpsSoapAddress;
    }

    /**
     * @param httpsSoapAddress
     *        The httpsSoapAddress to set.
     */
    public void setHttpsSoapAddress(String httpsSoapAddress)
    {
        this.httpsSoapAddress = httpsSoapAddress;
    }

    /**
     * @return Returns the wsdlDirectory.
     */
    public String getWsdlDirectory()
    {
        return wsdlDirectory;
    }

    /**
     * @param wsdlDirectory
     *        The wsdlDirectory to set.
     */
    public void setWsdlDirectory(String wsdlDirectory)
    {
        this.wsdlDirectory = wsdlDirectory;
    }

    /**
     * @return Returns the servicesDirectory.
     */
    public String getServicesDirectory()
    {
        return servicesDirectory;
    }

    /**
     * @param servicesDirectory
     *        The servicesDirectory to set.
     */
    public void setServicesDirectory(String servicesDirectory)
    {
        this.servicesDirectory = servicesDirectory;
    }

    /**
     * @return the WebServices beans map.
     */
    public Map<String, WebServiceBean> getWebServicesBeanMap()
    {
        return webServicesBeanMap;
    }

    /**
     * @param webServicesBeanMap
     */
    public void setWebServicesBeanMap(Map<String, WebServiceBean> webServicesBeanMap)
    {
        this.webServicesBeanMap = webServicesBeanMap;
    }
}
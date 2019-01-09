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
package it.greenvulcano.gvesb.gvconsole.webservice.utils;

import it.greenvulcano.gvesb.gvconsole.webservice.bean.BusinessWebServicesBean;
import it.greenvulcano.gvesb.gvconsole.webservice.bean.WebServiceBean;
import it.greenvulcano.gvesb.ws.wsdl.WSDLGenerator;
import it.greenvulcano.log.GVLogger;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.wsdl.WSDLException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class BusinessWSDLGenerator extends WSDLGenerator
{
    private static final Logger     logger = GVLogger.getLogger(BusinessWSDLGenerator.class);

    private BusinessWebServicesBean bean;

    /**
     * @param bean
     * @throws WSDLException
     */
    public BusinessWSDLGenerator(BusinessWebServicesBean bean) throws WSDLException
    {
        this.bean = bean;
        //setAuthenticatedHttpSoapAddress(bean.getAuthenticatedHttpSoapAddress());
        //setAuthenticatedHttpsSoapAddress(bean.getAuthenticatedHttpsSoapAddress());
        setHttpSoapAddress(bean.getHttpSoapAddress());
        setHttpsSoapAddress(bean.getHttpsSoapAddress());
        setWsdlDirectory(new File((bean.getWsdlDirectory())));
        logger.debug("Path WSDL Directory: " + bean.getWsdlDirectory());
    }

    /**
     * @see it.greenvulcano.gvesb.ws.wsdl.WSDLGenerator#getWebServiceConfig()
     */
    protected List<Node> getWebServiceConfig()
    {
        List<Node> ll = new LinkedList<Node>();
        for (WebServiceBean webServiceBean : bean.getWebServicesBeanMap().values()) {
            ll.add(webServiceBean.getNodeConfig());
        }
        return ll;
    }
}

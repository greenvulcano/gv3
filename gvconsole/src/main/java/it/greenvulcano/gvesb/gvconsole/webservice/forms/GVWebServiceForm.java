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
package it.greenvulcano.gvesb.gvconsole.webservice.forms;

import it.greenvulcano.gvesb.gvconsole.webservice.bean.BusinessWebServicesBean;
import it.greenvulcano.gvesb.gvconsole.webservice.bean.FileBean;
import it.greenvulcano.gvesb.gvconsole.webservice.bean.GeneralInfoBean;
import it.greenvulcano.gvesb.gvconsole.webservice.bean.ServiceBean;
import it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry;
import it.greenvulcano.gvesb.utils.StringUtility;

import java.util.Hashtable;
import java.util.List;

import org.apache.struts.validator.ValidatorForm;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class GVWebServiceForm extends ValidatorForm
{

    private static final long serialVersionUID = 300L;

    /**
     * boolean Flag for uddiServices length.
     */
    private String            flag             = "true";
    /**
     * action.
     */
    private String            action           = "";
    /**
     * exception.
     */
    private String            exception        = "";
    /**
     * wsdl selected.
     */
    private String[]          src              = null;
    /**
     * wsdl selected.
     */
    private String[]          dest             = null;
    /**
     * wsdl selected.
     */
    private String[]          srcAxis2         = null;
    /**
     * wsdl selected.
     */
    private String[]          destAxis2        = null;
    /**
     * url.
     */
    private String            url              = null;

    private String            elem_select      = "";

    /**
     * Constructor GVWebServiceForm.
     */
    public GVWebServiceForm()
    {
    }

    /**
     * @param exception
     */
    public void setException(String exception)
    {
        this.exception = exception;
    }

    /**
     * @return businessWebServicesBean
     */
    public BusinessWebServicesBean getBusinessWebServicesBean()
    {
        return GVWebServiceConfig.getInstance().getBusinessWebServicesBean();
    }

    /**
     * @return wsdlMapBean
     */
    public List<FileBean> getWsdlListBean()
    {
        return GVWebServiceConfig.getInstance().getWsdlListBean();
    }

    /**
     * @return uddiInfoBean
     */
    public GeneralInfoBean getUddiInfoBean()
    {
        return GVWebServiceConfig.getInstance().getUddiInfoBean();
    }

    /**
     * @return uddiServices
     */
    public Hashtable<String, ServiceBean> getUddiServices()
    {
        return GVWebServiceConfig.getInstance().getUddiServices();
    }

    /**
     * @return organizationName
     */
    public String getOrganizationName()
    {
        return GVWebServiceConfig.getInstance().getOrganizationName();
    }

    /**
     * @return exception
     */
    public String getException()
    {
        return exception;
    }

    /**
     *
     * @return action
     */
    public String getAction()
    {
        return action;
    }

    /**
     *
     * @param action
     */
    public void setAction(String action)
    {
        this.action = action;
    }

    /**
     *
     * @return dest
     */
    public String[] getDest()
    {
        return dest;
    }

    /**
     *
     * @param dest
     *        String[]
     */
    public void setDest(String[] dest)
    {
        this.dest = dest;
    }

    /**
     *
     * @return String[]
     */
    public String[] getSrc()
    {
        return src;
    }

    /**
     *
     * @param src
     */
    public void setSrc(String[] src)
    {
        this.src = src;
    }

    /**
     *
     * @return url
     */
    public String getUrl()
    {
        return url;
    }

    /**
     *
     * @return url
     */
    public String getBusinessValue()
    {
        return (getBusinessWebServicesBean().getAuthenticatedHttpSoapAddress() + "/" + getUrl() + "?wsdl");
    }

    /**
     *
     * @param url
     *        String
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * getRegistry.
     *
     * @return registry
     */
    public Registry getRegistry()
    {
        return GVWebServiceConfig.getInstance().getRegistry();
    }

    /**
     * getOrganizationKey.
     *
     * @return organizationKey
     */
    public String getOrganizationKey()
    {
        return GVWebServiceConfig.getInstance().getOrganizationKey();
    }

    /**
     * @return
     */
    public String getFlag()
    {
        if (getOrganizationKey().equals("")) {
            flag = "true";
        }
        else {
            flag = "false";
        }
        return flag;
    }

    /**
     * @param flag
     */
    public void setFlag(String flag)
    {
        this.flag = flag;
    }


    /**
     * @return
     */
    public String[] getDestAxis2()
    {
        return destAxis2;
    }

    /**
     * @param destAxis2
     */
    public void setDestAxis2(String[] destAxis2)
    {
        this.destAxis2 = destAxis2;
    }

    /**
     * @return
     */
    public String[] getSrcAxis2()
    {
        return srcAxis2;
    }

    /**
     * @param srcAxis2
     */
    public void setSrcAxis2(String[] srcAxis2)
    {
        this.srcAxis2 = srcAxis2;
    }

    /**
     * @return
     */
    public String getElem_select()
    {
        return elem_select;
    }

    /**
     * @param elem_select
     */
    public void setElem_select(String elem_select)
    {
        this.elem_select = elem_select;
    }

    /**
     * Metodo per la creazione di una stringa contenente alcuni dati del form di
     * interesse per la sicurezza
     *
     * @return the string
     *
     */
    public String toString_Security()
    {
        String result = "";

        // Se si sta eseguendo un deploy
        if (action.equals("Deploy") && elem_select != null && !elem_select.equals("")) {
            result += elem_select + " ";
        }

        if (src != null)
            for (int i = 0; i < src.length; i++)
                result += StringUtility.addIfNotEmpty(src[i], " - BUSINESS ");
        return result;


    }

    /**
     * @param listFilesBean
     */
    public void setBusinessWsdlFilesBean(List<FileBean> listFilesBean)
    {
        GVWebServiceConfig.getInstance().setWsdlFilesBean(listFilesBean);
    }

    /**
     * @param registry
     * @param serviceKey
     * @param organizationKey
     * @return
     */
    public ServiceBean createServiceBean(Registry registry, String serviceKey, String organizationKey)
    {
        return GVWebServiceConfig.getInstance().createServiceBean(registry, serviceKey, organizationKey);
    }

    /**
     * @param uddiServices
     */
    public void setUddiServices(Hashtable<String, ServiceBean> uddiServices)
    {
        GVWebServiceConfig.getInstance().setUddiServices(uddiServices);
    }

}
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

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvconsole.webservice.bean.BusinessWebServicesBean;
import it.greenvulcano.gvesb.gvconsole.webservice.bean.FileBean;
import it.greenvulcano.gvesb.gvconsole.webservice.bean.GeneralInfoBean;
import it.greenvulcano.gvesb.gvconsole.webservice.bean.ServiceBean;
import it.greenvulcano.gvesb.gvconsole.webservice.utils.FileUtility;
import it.greenvulcano.gvesb.gvconsole.webservice.utils.WSDLFilter;
import it.greenvulcano.gvesb.j2ee.xmlRegistry.Proxy;
import it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry;
import it.greenvulcano.log.GVLogger;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Dec 28, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class GVWebServiceConfig implements ConfigurationListener
{

    private static GVWebServiceConfig      _instance               = new GVWebServiceConfig();

    /**
     * Logger definition.
     */
    private static final Logger            logger                  = GVLogger.getLogger(GVWebServiceConfig.class);

    /**
     * CONFIGURATION FILE.
     */
    private static final String            configurationFile       = "GVAdapters.xml";

    /**
     * BusinessWebServicesBean object.
     */
    private BusinessWebServicesBean        businessWebServicesBean = null;

    private List<FileBean>                 wsdlListBean            = null;

    /**
     * GeneralInfoBean contenente le informazioni sull'UDDI registry.
     */
    private GeneralInfoBean                uddiInfoBean            = null;

    /**
     * Id UDDI Registry.
     */
    private String                         id_registry             = "";

    /**
     * Hashtable containing GreenVulcano's ESB services, published on UDDI
     * registry.
     */
    private Hashtable<String, ServiceBean> uddiServices            = null;

    /**
     * UDDI organizationName.
     */
    private String                         organizationName        = null;

    /**
     * UDDI organizationKey.
     */
    private String                         organizationKey         = "";

    /**
     * REGISTRY UDDI.
     */
    private Registry                       registry                = null;

    /**
     * Boolean value that mean if the configuration changed.
     */
    private static boolean                 configurationChanged    = true;

    /**
     * @return the singleton instance
     */
    public static GVWebServiceConfig getInstance()
    {
        if (configurationChanged) {
            _instance.loadConfiguration();
        }
        return _instance;
    }

    /**
     *
     */
    private synchronized void loadConfiguration()
    {
        if (configurationChanged) {
            logger.debug("init configuration");
            try {
                logger.debug("Create the WebServices...");
                businessWebServicesBean = new BusinessWebServicesBean(XMLConfig.getNode(configurationFile,
                        "GVAdapters/GVWebServices/BusinessWebServices"));

                createWSDLList();

                // BEGIN UDDI
                // preparo il registry uddi e creo il service bean
                Node uddiNode = XMLConfig.getNode(configurationFile, "GVAdapters/GVWebServices/UDDI");
                logger.debug("uddiNode " + uddiNode);

                if (uddiNode != null) {
                    Node registryNode = XMLConfig.getNode(uddiNode, "*[@type='xmlregistry']");
                    logger.debug("registryNode " + registryNode);
                    uddiInfoBean = new GeneralInfoBean(registryNode);
                    uddiServices = new Hashtable<String, ServiceBean>();
                    Node proxyNode = XMLConfig.getNode(uddiNode, "Proxy[@type='proxy']");
                    logger.debug("proxyNode " + proxyNode);

                    logger.debug("Proxy creato.. ");

                    String className = XMLConfig.get(registryNode, "@class");
                    Class<?> regImpl = Class.forName(className);
                    registry = (Registry) regImpl.newInstance();
                    logger.debug("Registry creato.. ");

                    Proxy proxy = null;
                    if (proxyNode != null) {
                        proxy = new Proxy(proxyNode);
                    }
                    registry.init(registryNode, proxy);

                    id_registry = registry.getRegistryID();
                    organizationName = registry.getOrganization();

                    if (!id_registry.equals("") && !organizationName.equals("")) {
                        logger.debug("Load organization info...");
                        try {
                            Map<String, String> ibOrganization = registry.findBusinessByName(organizationName);
                            Set<String> lo = ibOrganization.keySet();
                            Iterator<String> loIt = lo.iterator();
                            while (loIt.hasNext()) {
                                organizationKey = loIt.next();
                            }
                            if (!organizationKey.equals("")) {
                                Map<String, String> listServices = registry.findServiceByName(organizationKey);
                                logger.debug("Create list of services...");
                                String serviceKey = "";
                                Set<String> ls = listServices.keySet();
                                Iterator<String> lsIt = ls.iterator();
                                while (lsIt.hasNext()) {
                                    serviceKey = lsIt.next();
                                    ServiceBean serviceBean = createServiceBean(registry, serviceKey, organizationKey);
                                    uddiServices.put(serviceKey, serviceBean);
                                }
                            }
                        }
                        catch (Exception exc) {
                            logger.warn("Impossible to connect to UDDI Server with id: '" + id_registry + "'", exc);
                        }
                    }
                    else {
                        logger.debug("Configuration Error: non ci sono registry e/o organizzazioni configurate");
                    }
                }
            }
            catch (Throwable exc) {
                logger.error("Exception initializing WS config: ", exc);
            }
            configurationChanged = false;
        }
    }

    private void createWSDLList()
    {
        // The wsdl files in the wsdl directory
        logger.debug("Create list of File wsdl...");
        wsdlListBean = FileUtility.listFilesBean(businessWebServicesBean, new WSDLFilter());
    }

    /**
     * Creo il service bean da mettere in sessione.
     * 
     * @param registry
     * @param serviceKey
     * @param organizationKey
     * @return the created service bean
     */
    ServiceBean createServiceBean(Registry registry, String serviceKey, String organizationKey)
    {

        String serviceName = "";
        String description = "";
        String accessURL = "";
        String accessType = "";
        String bindingKey = "";
        String overviewURL = "";

        Map<String, String> serviceDetail = registry.getServiceDetail(serviceKey);

        if (serviceDetail.containsKey("ServiceName")) {
            serviceName = serviceDetail.get("ServiceName");
        }
        if (serviceDetail.containsKey("Description")) {
            description = serviceDetail.get("Description");
        }
        if (serviceDetail.containsKey("AccessURL")) {
            accessURL = serviceDetail.get("AccessURL");
        }
        if (serviceDetail.containsKey("AccessType")) {
            accessType = serviceDetail.get("AccessType");
        }
        if (serviceDetail.containsKey("BindingKey")) {
            bindingKey = serviceDetail.get("BindingKey");
        }
        if (serviceDetail.containsKey("OverviewURL")) {
            overviewURL = serviceDetail.get("OverviewURL");
        }

        ServiceBean serviceBean = new ServiceBean();
        serviceBean.setServiceKey(serviceKey);
        serviceBean.setServiceName(serviceName);
        serviceBean.setBindingKey(bindingKey);
        serviceBean.setAccessType(accessType);
        serviceBean.setAccessURL(accessURL);
        serviceBean.setDescription(description);
        serviceBean.setOverviewURL(overviewURL);

        for (FileBean fileBean : wsdlListBean) {
            if (fileBean.getName().equals(serviceName) && fileBean.getFlag()) {
                serviceBean.setFlag(true);
            }
        }

        return serviceBean;
    }

    private GVWebServiceConfig()
    {
        XMLConfig.addConfigurationListener(this);
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        configurationChanged = true;
    }

    /**
     * @return
     */
    public BusinessWebServicesBean getBusinessWebServicesBean()
    {
        return businessWebServicesBean;
    }

    /**
     * @return
     */
    public Hashtable<String, ServiceBean> getUddiServices()
    {
        return uddiServices;
    }

    /**
     * @return
     */
    public GeneralInfoBean getUddiInfoBean()
    {
        return uddiInfoBean;
    }

    /**
     * @return
     */
    public List<FileBean> getWsdlListBean()
    {
        return wsdlListBean;
    }

    /**
     * @return
     */
    public String getOrganizationName()
    {
        return organizationName;
    }

    /**
     * @return
     */
    public Registry getRegistry()
    {
        return registry;
    }

    /**
     * @return
     */
    public String getOrganizationKey()
    {
        return organizationKey;
    }

    /**
     * @param listFilesBean
     */
    public void setWsdlFilesBean(List<FileBean> listFilesBean)
    {
        this.wsdlListBean = listFilesBean;
    }

    /**
     * @param uddiServices
     */
    public void setUddiServices(Hashtable<String, ServiceBean> uddiServices)
    {
        this.uddiServices = uddiServices;
    }

}

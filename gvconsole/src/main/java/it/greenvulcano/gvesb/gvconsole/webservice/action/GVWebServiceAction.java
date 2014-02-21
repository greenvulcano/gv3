/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.gvconsole.webservice.action;


import it.greenvulcano.gvesb.gvconsole.webservice.bean.BusinessWebServicesBean;
import it.greenvulcano.gvesb.gvconsole.webservice.bean.ServiceBean;
import it.greenvulcano.gvesb.gvconsole.webservice.bean.WebServiceBean;
import it.greenvulcano.gvesb.gvconsole.webservice.forms.GVWebServiceForm;
import it.greenvulcano.gvesb.gvconsole.webservice.utils.BusinessWSDLGenerator;
import it.greenvulcano.gvesb.gvconsole.webservice.utils.FileUtility;
import it.greenvulcano.gvesb.gvconsole.webservice.utils.WSDLFilter;
import it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry;
import it.greenvulcano.gvesb.ws.wsdl.GVWebService;
import it.greenvulcano.gvesb.ws.wsdl.Module;
import it.greenvulcano.gvesb.ws.wsdl.ServiceOperation;
import it.greenvulcano.gvesb.ws.wsdl.WSDLGenerator;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 * 
 */
public class GVWebServiceAction extends Action
{

    /**
     * Logger definition.
     */
    private static final Logger            logger             = GVLogger.getLogger(GVWebServiceAction.class);

    private WSDLGenerator                  businessGenerator  = null;

    private Registry                       registry           = null;
    private String                         organizationKey    = "";
    private Hashtable<String, ServiceBean> uddiServices       = null;

    private static Transformer             serviceTransformer = null;

    /**
     * Execute method.
     * 
     * @param actionMapping
     *        ActionMapping
     * @param actionForm
     *        ActionForm
     * @param request
     *        HttpServletRequest
     * @param response
     *        HttpServletResponse
     * @throws Exception
     *         if an error occurs.
     * @return ActionForward
     * 
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        logger.debug("starting execute()");
        String forward = "success";
        GVWebServiceForm form = (GVWebServiceForm) actionForm;

        // Mappa dei parametri
        // dell'operazione da inserire nel security log
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("GVWebServiceForm", form.toString_Security());

        String action = null;
        try {
            action = form.getAction();
            if ((action != null) && !action.equals("")) {
                if (action.equals("Create WSDL")) {
                    invokeWSDL(form);
                    form.setAction("");
                    form.setSrc(null);
                }
                else if (action.equals("Delete WSDL")) {
                    elimina(form);
                    form.setAction("");
                    form.setSrc(null);
                }
                else if (action.equals("Deploy")) {
                    deployWSDLBusiness(form);
                    form.setAction("");
                    form.setSrc(null);
                }
                else if (action.equals("Publish WSDL") || action.equals("Unpublish WSDL")) {
                    invokeUDDI(form);
                    form.setAction("");
                    form.setSrc(null);
                    forward = "successUDDI";
                }
                else if (action.equals("Reload UDDI")) {
                    reloadUDDI(form);
                    form.setAction("");
                    form.setSrc(null);
                    forward = "successUDDI";
                }
            }
            ActionForward actionForward = actionMapping.findForward(forward);
            return actionForward;
        }
        catch (Exception exc) {
            logger.error("Exception executing WebService Action ", exc);
            form.setException("Exception :" + exc);
            return actionMapping.findForward(forward);
        }
    }

    /**
     * Method for generate and delete wsdl.
     * 
     * @param form
     *        GVWebServiceForm
     * @throws Exception
     *         if an error occurs.
     */
    public void invokeWSDL(GVWebServiceForm form) throws Exception
    {
        logger.debug("starting execute()");

        try {
            String action = form.getAction();
            logger.debug("The command is '" + action + "'");
            String srcWS[] = form.getSrc();
            BusinessWebServicesBean businessBean = form.getBusinessWebServicesBean();
            if (action.equals("Create WSDL")) {
                if (srcWS != null) {
                    String wsName = "";
                    logger.debug("Generate the WSDL for following Web Services:");
                    StringBuffer print = new StringBuffer();
                    for (int i = 0; i < srcWS.length; i++) {
                        if (businessGenerator == null) {
                            businessGenerator = new BusinessWSDLGenerator(businessBean);
                        }
                        WSDLGenerator generator = businessGenerator;
                        wsName = srcWS[i];
                        String wsdlDir = businessBean.getWsdlDirectory();
                        GVWebService gvWebService = getWebService(businessBean, wsName);
                        if (gvWebService == null) {
                            gvWebService = getWebService(businessBean, wsName);
                            wsdlDir = businessBean.getWsdlDirectory();
                            if (businessGenerator == null) {
                                businessGenerator = new BusinessWSDLGenerator(businessBean);
                            }
                            generator = businessGenerator;
                        }
                        print.append(wsName + " - ");
                        // Generate the WSDL
                        logger.info("Generating web service: " + gvWebService.getServiceName());

                        generator.generateWSDL(gvWebService, wsdlDir);
                    }
                    logger.debug(print.toString());
                }
            }
            else {
                if (srcWS != null) {
                    String fileName = "";
                    logger.debug("Delete the following Files:");
                    StringBuffer print = new StringBuffer();
                    boolean delete = false;
                    String businessWsdlDirectory = businessBean.getWsdlDirectory();
                    for (int i = 0; i < srcWS.length; i++) {
                        fileName = srcWS[i];
                        print.append(fileName).append(", path = ");
                        File delFile = new File(businessWsdlDirectory, fileName + ".wsdl");
                        if (!delFile.exists()) {
                            delFile = new File(businessWsdlDirectory, fileName + ".wsdl");
                        }
                        delete = delFile.delete();
                        print.append(delFile.getAbsolutePath()).append(", delete = ");
                        print.append(delete).append('\n');
                    }
                    logger.debug(print);
                }
            }
            // Reload the WSDL file and put wsdlFilesBean in the session
            form.setBusinessWsdlFilesBean(FileUtility.listFilesBean(businessBean, new WSDLFilter()));
            logger.debug("Put the wsdlFilesBean in the session");
            logger.debug("END doPost");
        }
        catch (Exception exc) {
            logger.error("Exception ", exc);
            form.setException("Exception :" + exc);
        }
    }

    /**
     * Method to delete wsdl.
     * 
     * @param form
     *        GVWebServiceForm
     * @throws Exception
     *         if an error occurs.
     */
    public void elimina(GVWebServiceForm form) throws Exception
    {
        logger.debug("starting execute()");

        try {
            String action = form.getAction();
            logger.debug("The command is '" + action + "'");
            if (action.equals("Delete WSDL")) {
                deleteWSDL(form.getSrc(), form.getBusinessWebServicesBean());
                form.setBusinessWsdlFilesBean(FileUtility.listFilesBean(form.getBusinessWebServicesBean(),
                        new WSDLFilter()));
                logger.debug("Put the wsdlFilesBean in the session");
                logger.debug("END doPost");
            }
        }
        catch (Exception exc) {
            logger.error("Exception ", exc);
            form.setException("Exception :" + exc);
        }
    }

    /**
     * Method to deploy wsdlBusiness.
     * 
     * @param form
     *        GVWebServiceForm
     * @throws Exception
     *         if an error occurs.
     */
    public void deployWSDLBusiness(GVWebServiceForm form) throws Exception
    {
        logger.debug("starting deployWSDLBusiness()");
        BusinessWebServicesBean axis2Bean = form.getBusinessWebServicesBean();
        String servicesDirectory = axis2Bean.getServicesDirectory();

        List<String> servicesFiles = new ArrayList<String>();
        // read from services.list file
        File servicesListFile = new File(servicesDirectory, "services.list");
        if (servicesListFile.exists()) {
            BufferedInputStream bi = new BufferedInputStream(new FileInputStream(servicesListFile));
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(bi));
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().startsWith("#")) {
                        servicesFiles.add(line);
                    }
                }
            }
            finally {
                bi.close();
            }
        }

        if (serviceTransformer == null) {
            ClassLoader loader = getClass().getClassLoader();
            InputStream serviceTemplate = loader.getResourceAsStream("it/greenvulcano/gvesb/gvconsole/webservice/action/service_template.xsl");
            serviceTransformer = TransformerFactory.newInstance().newTransformer(new StreamSource(serviceTemplate));
        }
        String[] srcWS = form.getSrc();
        for (String serviceName : srcWS) {
            GVWebService webService = getWebService(axis2Bean, serviceName);

            Iterator<ServiceOperation> serviceOperations = webService.getServiceOperations();

            XMLUtils xmlUtils = XMLUtils.getParserInstance();
            Document serviceDoc = null;
            try {
                serviceDoc = xmlUtils.parseDOM("<service/>");
                Element serviceElem = serviceDoc.getDocumentElement();
                serviceElem.setAttribute("name", serviceName);
                if (webService.isUseOriginalwsdl()) {
                    System.out.println("Console=" + webService.isUseOriginalwsdl());
                    serviceElem.setAttribute("useOriginalwsdl", "true");
                }
                logger.debug("isHttpTransport console="
                        + (webService.isSoapTransport() || webService.isSoap12Transport() || webService.isRestTransport()));
                if (webService.isSoapTransport() || webService.isSoap12Transport() || webService.isRestTransport()) {
                    Element bindinghttp = serviceDoc.createElement("bindinghttp");
                    serviceElem.appendChild(bindinghttp);
                }
                logger.debug("isJmsTransport console=" + webService.isJmsTransport());
                logger.debug("serviceElem=" + serviceElem.toString());
                if (webService.isJmsTransport()) {
                    Element bindingjms = serviceDoc.createElement("bindingjms");
                    bindingjms.setAttribute("connectionFactory", webService.getJndiConnectionFactoryName());
                    bindingjms.setAttribute("queue", webService.getJmsDestination());
                    bindingjms.setAttribute("destinationType", webService.getJmsDestinationType());
                    if (webService.getJmsReplyDestination() != null) {
                        bindingjms.setAttribute("replyDestination", webService.getJmsReplyDestination());
                    }
                    if (webService.getJmsContentType() != null) {
                        bindingjms.setAttribute("contentType", webService.getJmsContentType());
                    }
                    if (webService.getJmsBytesMessage() != null) {
                        bindingjms.setAttribute("bytesMessage", webService.getJmsBytesMessage());
                    }
                    if (webService.getJmsTextMessage() != null) {
                        bindingjms.setAttribute("textMessage", webService.getJmsTextMessage());
                    }
                    logger.debug("bindingjms=" + bindingjms.toString());
                    serviceElem.appendChild(bindingjms);
                }

                Iterator<Module> serviceModules = webService.getServiceModules();
                if (serviceModules.hasNext()) {
                    Element modules = serviceDoc.createElement("modules");
                    serviceElem.appendChild(modules);
                    while (serviceModules.hasNext()) {
                        Module module = serviceModules.next();

                        Element modElem = serviceDoc.createElement("module");
                        modElem.setAttribute("name", module.getName());
                        modules.appendChild(modElem);

                        String policy = module.getPolicyData();
                        if (policy != null) {
                            Element polElem = serviceDoc.createElement("policy");
                            modElem.appendChild(polElem);

                            Document polDoc = xmlUtils.parseDOM(policy, false, true);
                            polElem.appendChild(serviceDoc.importNode(polDoc.getDocumentElement(), true));
                        }
                    }
                }

                Element operations = serviceDoc.createElement("operations");
                serviceElem.appendChild(operations);
                if (serviceOperations != null) {
                    while (serviceOperations.hasNext()) {
                        ServiceOperation serviceOperation = serviceOperations.next();
                        Element op = serviceDoc.createElement("operation");
                        op.setAttribute("name", serviceOperation.getOperationName());
                        op.setAttribute("namespace", serviceOperation.getTargetNameSpace());
                        operations.appendChild(op);

                        String policy = serviceOperation.getPolicyData();
                        if (policy != null) {
                            Element polElem = serviceDoc.createElement("policy");
                            op.appendChild(polElem);

                            Document polDoc = xmlUtils.parseDOM(policy, false, true);
                            polElem.appendChild(serviceDoc.importNode(polDoc.getDocumentElement(), true));
                        }
                    }
                }
            }
            finally {
                XMLUtils.releaseParserInstance(xmlUtils);
            }
            String jarName = serviceName + ".aar";
            BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(servicesDirectory
                    + File.separatorChar + jarName));
            JarOutputStream serviceJar = new JarOutputStream(bo);

            try {
                JarEntry servicesEntry = new JarEntry("META-INF/services.xml");
                serviceJar.putNextEntry(servicesEntry);

                synchronized (serviceTransformer) {
                    serviceTransformer.transform(new DOMSource(serviceDoc), new StreamResult(serviceJar));
                }
                serviceJar.closeEntry();

                String wsdlName = webService.getServiceName() + ".wsdl";
                String wsdlDirectory = axis2Bean.getWsdlDirectory();
                if ((wsdlDirectory != null) && (wsdlDirectory.length() > 0)) {
                    File wsdlDir = new File(wsdlDirectory);
                    if (wsdlDir.exists() && wsdlDir.isDirectory()) {
                        File wsdlFile = new File(wsdlDir, wsdlName);
                        if (wsdlFile.exists() && wsdlFile.isFile()) {
                            BufferedInputStream wsdlStream = new BufferedInputStream(new FileInputStream(wsdlFile));

                            JarEntry wsdlEntry = new JarEntry("META-INF/" + wsdlName);
                            serviceJar.putNextEntry(wsdlEntry);
                            byte[] tempBuffer = new byte[1024];
                            int read = -1;
                            while ((read = wsdlStream.read(tempBuffer)) > -1) {
                                serviceJar.write(tempBuffer, 0, read);
                            }
                            serviceJar.closeEntry();
                        }
                    }
                }
            }
            finally {
                serviceJar.close();
                bo.close();
            }

            // add to services.list
            if (!servicesFiles.contains(jarName)) {
                FileOutputStream outputStream = new FileOutputStream(servicesListFile, true);
                try {
                    outputStream.write('\n');
                    outputStream.write(jarName.getBytes());
                }
                finally {
                    outputStream.close();
                }
            }
        }
    }

    private void deleteWSDL(String[] srcWS, BusinessWebServicesBean bean)
    {
        if (srcWS != null) {
            String fileName = "";
            logger.debug("Delete the following Files:");
            StringBuffer print = new StringBuffer();
            boolean delete = false;
            String wsdlDirectory = bean.getWsdlDirectory();
            for (int i = 0; i < srcWS.length; i++) {
                fileName = srcWS[i];
                print.append(fileName).append(", path = ");
                File delFile = new File(wsdlDirectory, fileName + ".wsdl");
                delete = delFile.delete();
                print.append(delFile.getAbsolutePath()).append(", delete = ");
                print.append(delete).append('\n');
            }
            logger.debug(print);
        }
    }

    private GVWebService getWebService(BusinessWebServicesBean bean, String wsName) throws Exception
    {
        Map<?, ?> webServicesBeanMap = bean.getWebServicesBeanMap();
        GVWebService toReturn = null;
        if (webServicesBeanMap.containsKey(wsName)) {
            WebServiceBean webServiceBean = (WebServiceBean) webServicesBeanMap.get(wsName);
            // Create the GVWebService
            toReturn = new GVWebService(webServiceBean.getNodeConfig());
        }
        return toReturn;
    }

    /**
     * invokeUDDI.
     * 
     * @param form
     *        GVWebServiceForm
     */
    public void invokeUDDI(GVWebServiceForm form)
    {
        try {
            String action = form.getAction();
            if (action.equals("Publish WSDL") || action.equals("Unpublish WSDL")) {
                registry = form.getRegistry();
                organizationKey = form.getOrganizationKey();
                if (action.equals("Publish WSDL")) {
                    String srcWS[] = form.getSrc();
                    String serviceName = "";
                    String wsdlUrl = "";

                    for (int i = 0; i < srcWS.length; i++) {
                        serviceName = srcWS[i];

                        wsdlUrl = form.getBusinessWebServicesBean().getAuthenticatedHttpSoapAddress() + "/"
                                + serviceName + "?wsdl";
                        // prendere descrizione *********
                        registry.saveService(organizationKey, serviceName, "descrizione", wsdlUrl);
                    }
                }
                else {
                    String srcWS[] = form.getSrc();
                    String serviceKey = "";
                    for (int i = 0; i < srcWS.length; i++) {
                        serviceKey = srcWS[i];
                        // delete service on UDDI Registry
                        registry.delService(serviceKey);
                    }
                }
                // Reload the UDDI WSDL list and put list in the session
                uddiServices = new Hashtable<String, ServiceBean>();
                Map<String, String> listServices = registry.findServiceByName(organizationKey);

                if (listServices != null) {
                    logger.debug("Create list of services...");
                    String serviceKey = "";
                    Set<String> ls = listServices.keySet();
                    Iterator<String> lsIt = ls.iterator();
                    while (lsIt.hasNext()) {
                        serviceKey = lsIt.next();
                        ServiceBean serviceBean = form.createServiceBean(registry, serviceKey, organizationKey);
                        uddiServices.put(serviceKey, serviceBean);
                    }
                }
                logger.debug("Put the uddiServices in the session");
                form.setUddiServices(uddiServices);
                logger.debug("END doPost");
            }
        }
        catch (Exception exc) {
            logger.error("Exception: " + exc);
            form.setException("Exception :" + exc);
        }
    }

    /**
     * reloadUDDI.
     * 
     * @param form
     *        GVWebServiceForm
     */
    public void reloadUDDI(GVWebServiceForm form)
    {
        try {
            String action = form.getAction();
            if (action.equals("Reload UDDI")) {
                registry = form.getRegistry();
                organizationKey = form.getOrganizationKey();
                // Reload the UDDI WSDL list and put list in the session
                uddiServices = new Hashtable<String, ServiceBean>();
                Map<String, String> listServices = registry.findServiceByName(organizationKey);

                if (listServices != null) {
                    logger.debug("Create list of services...");
                    String serviceKey = "";
                    Set<String> ls = listServices.keySet();
                    Iterator<String> lsIt = ls.iterator();
                    while (lsIt.hasNext()) {
                        serviceKey = lsIt.next();
                        ServiceBean serviceBean = null;
                        try {
                            serviceBean = form.createServiceBean(registry, serviceKey, organizationKey);
                        }
                        catch (Exception exc) {
                            serviceBean = new ServiceBean();
                            serviceBean.setServiceKey(serviceKey);
                            serviceBean.setDescription("Exception loading this service: " + exc);
                        }
                        uddiServices.put(serviceKey, serviceBean);
                    }
                }
                logger.debug("Put the uddiServices in the session");
                form.setUddiServices(uddiServices);
                logger.debug("END doPost");
            }
        }
        catch (Exception exc) {
            logger.error("Exception: ", exc);
            form.setException("Exception :" + exc);
        }
    }

    private String getNameSpace(String qname)
    {
        if (qname == null) {
            return "";
        }
        if (qname.startsWith("{") && (qname.indexOf('}') > 0)) { //$NON-NLS-1$
            return qname.substring(1, qname.indexOf('}'));
        }
        return "";
    }

    private String getLocalName(String qname)
    {
        if (qname == null) {
            return "";
        }
        if (qname.startsWith("{") && (qname.indexOf('}') > 0)) { //$NON-NLS-1$
            qname = qname.substring(qname.indexOf('}') + 1);
        }
        return qname;
    }

}
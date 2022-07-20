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
package it.greenvulcano.gvesb.gvconsole.deploy;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;
import max.xml.DOMWriter;

/**
 *
 * GVCoreParser class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GVConfig
{

	/**
	 * @param
	 */
	private Document coreXml = null;
	private Document adapterXml = null;
	private String type = null;
	private static Logger  logger    = Logger.getLogger(GVConfig.class);

	public Document getCoreXml(){
		return this.coreXml;
	}

	public Document getAdapterXml(){
		return this.adapterXml;
	}

	public GVConfig(String type, Document coreXml, Document adapterXml)
	{
		this.type = type;
		this.coreXml =coreXml;
		this.adapterXml = adapterXml;
	}

	public GVConfig(String type, URL urlCore,URL urlAdapter)
	{
		this.type = type;
		try {
			this.coreXml = parseXml(urlCore);
			this.adapterXml = parseXml(urlAdapter);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public GVConfig(String type, String fileCore,String fileAdapter)
	{
		this.type = type;
		try {
			this.adapterXml = parseXml(fileAdapter);
			this.coreXml = parseXml(fileCore);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public List<String> getListaServizi()
	{
		List<String> retListaServizi = new ArrayList<String>();
		for(String servizio : getListaNodeServizi().keySet()){
			retListaServizi.add(servizio);
		}
		Collections.sort(retListaServizi);
		return retListaServizi;
	}

	public Map<String, Node> getListaNodeServizi()
	{
		logger.debug("init getListaServizi[" + this.type + "]");
		Map<String, Node> retListaServizi = new HashMap<String, Node>();;
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			NodeList results = parser.selectNodeList(this.coreXml, "/GVCore/GVServices/Services/Service");
			for (int i = 0; i < results.getLength(); i++) {
				String serviceName = parser.get(results.item(i), "@id-service");
				retListaServizi.put(serviceName, results.item(i));
				logger.debug("Found[" + this.type + "] Service " + serviceName);
			}
			return retListaServizi;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return retListaServizi;
	}

	public Map<String, Node> getListaNodeServizi(List<String> listaServizi)
	{
		logger.debug("init getListaServizi[" + this.type + "]");
		Map<String, Node> retListaServizi = new HashMap<String, Node>();;
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			for(String servizio:listaServizi) {
				Node result = parser.selectSingleNode(this.coreXml, "/GVCore/GVServices/Services/Service[@id-service='"+servizio+"']");
				if(result != null) {
					logger.debug("Found Service[" + this.type + "] " + servizio);
					retListaServizi.put(servizio, result);
				}
			}
			return retListaServizi;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return retListaServizi;
	}

	public String getDteDir(String dataSource,String fileType)
	{
		String xpath="/GVCore/GVDataTransformation/DataSourceSets/DataSourceSet[@name='"+dataSource+"']/LocalFSDataSource[@formatHandled='"+fileType+"']/@repositoryHome";
		String dirDTE=null;
		try {
			dirDTE = XMLUtils.get_S(this.coreXml, xpath);
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		dirDTE = dirDTE.replace("${{gv.app.home}}", "");
		return dirDTE;
	}

	public String geWsdlDir()
	{
		String wsdlDir = null;
		try {
			wsdlDir = XMLUtils.get_S(this.adapterXml, "/GVAdapters/GVWebServices/BusinessWebServices/@wsdl-directory").replace("${{gv.app.home}}", "");
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		return wsdlDir;
	}

	public String getWsServiceDir()
	{
		String wsdlDir = null;
		try {
			wsdlDir = XMLUtils.get_S(this.adapterXml, "/GVAdapters/GVWebServices/BusinessWebServices/@services-directory").replace("${{gv.app.home}}", "");
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		return wsdlDir;
	}

	public String getGvAdapters(Boolean forExport)
	{
		List<String> listaServizi = getListaServizi();
		return getGvAdapters(listaServizi,forExport);
	}

	public String getGvAdapters(List<String> listaServizi,Boolean forExport)
	{
		Document localXmlGVCore = getDocGvAdapters(listaServizi,forExport);
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		DOMWriter domWriter = new DOMWriter();
		try {
			domWriter.write(localXmlGVCore, out);
		} catch (IOException e) {
			logger.error(e);
		}
		String localXml = out.toString();
		return localXml;
	}

	private Document getDocGvAdapters(List<String> listaServizi,Boolean forExport)
	{
		logger.debug("init getDocGvAdapters[" + this.type + "]");
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();

			Document localXmlGVAdapter = parser.newDocument("GVAdapters");
			parser.setAttribute(localXmlGVAdapter.getDocumentElement(), "version", "1.0");
			if(forExport){
			  DOMImplementation domImpl = localXmlGVAdapter.getImplementation();
			  DocumentType doctype = domImpl.createDocumentType("web-app",
					"SYSTEM",
					"http://www.greenvulcano.com/gvesb/dtds/GVAdapters.dtd");
			  localXmlGVAdapter.appendChild(doctype);
			}
			Map<String, Node> mapListaGvWs = getListaGvWebServices(listaServizi);
			Map<String, Node> mapListaBsWs = getListaBusinessWebServices(listaServizi);
			if(((mapListaGvWs.size()+mapListaBsWs.size())>0) || forExport){
				Node locGVWebServices = localXmlGVAdapter.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapter, "GVWebServices"));
				parser.setAttribute((Element) locGVWebServices, "name", "WEB_SERVICES");
				parser.setAttribute((Element) locGVWebServices, "type", "Module");
				parser.setAttribute((Element) locGVWebServices, "version", "1.0");
				Node locGreenVulcanoWebServices = locGVWebServices.appendChild(parser.createElement(localXmlGVAdapter, "GreenVulcanoWebServices"));
				if(mapListaGvWs.size()>0){
					for (String nome:mapListaGvWs.keySet()) {
						Node ws = mapListaGvWs.get(nome);
						locGreenVulcanoWebServices.appendChild(localXmlGVAdapter.importNode(ws, true));
					}
				}
                Node locBusinessWebServices = locGVWebServices.appendChild(parser.createElement(localXmlGVAdapter, "BusinessWebServices"));
				if(mapListaBsWs.size()>0){
				  Node businessWebServices = getNodeBusinessWebServices();
				  for(int i=0;i<businessWebServices.getAttributes().getLength();i++){
					  parser.setAttribute((Element) locBusinessWebServices, businessWebServices.getAttributes().item(i).getNodeName(), businessWebServices.getAttributes().item(i).getNodeValue());
				  }
				  for (String nome:mapListaBsWs.keySet()) {
					Node ws = mapListaBsWs.get(nome);
					locBusinessWebServices.appendChild(localXmlGVAdapter.importNode(ws, true));
				  }
				}
			}

			Map<String, Node> mapListaDataProviders = getListDataProvider(listaServizi);
			if((mapListaDataProviders.size()>0) || forExport){
				Node locGVDataProviders = localXmlGVAdapter.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapter, "GVDataProviderManager"));
				parser.setAttribute((Element) locGVDataProviders, "name", "GVDP");
				parser.setAttribute((Element) locGVDataProviders, "type", "Module");

				Node locDataProviders = locGVDataProviders.appendChild(parser.createElement(localXmlGVAdapter, "DataProviders"));
				for (String nomeDp:mapListaDataProviders.keySet()) {
					Node dp = mapListaDataProviders.get(nomeDp);
					locDataProviders.appendChild(localXmlGVAdapter.importNode(dp, true));
				}
			}

			Map<String, Node> mapListaExcelWorkBook = getListExcelWorkBook(listaServizi);
			if((mapListaExcelWorkBook.size()>0) || forExport){
				Node locGVExcelWorkbook = localXmlGVAdapter.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapter, "GVExcelWorkbookConfiguration"));
				parser.setAttribute((Element) locGVExcelWorkbook, "name", "EXCEL_WORK");
				parser.setAttribute((Element) locGVExcelWorkbook, "type", "Module");
				parser.setAttribute((Element) locGVExcelWorkbook, "version", "1");

				for (String nomeWb : mapListaExcelWorkBook.keySet()) {
					Node wb = mapListaExcelWorkBook.get(nomeWb);
					locGVExcelWorkbook.appendChild(localXmlGVAdapter.importNode(wb, true));
				}
			}

			Map<String, Node> mapListaExcelReport = getListExcelReport(listaServizi);
			if((mapListaExcelReport.size()>0) || forExport){
				Node locGVExcelCreator = localXmlGVAdapter.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapter, "GVExcelCreatorConfiguration"));
				parser.setAttribute((Element) locGVExcelCreator, "name", "EXCEL_REPO");
				parser.setAttribute((Element) locGVExcelCreator, "type", "Module");
				parser.setAttribute((Element) locGVExcelCreator, "version", "1");

				for (String nomeRp : mapListaExcelReport.keySet()) {
					Node rp = mapListaExcelReport.get(nomeRp);
					locGVExcelCreator.appendChild(localXmlGVAdapter.importNode(rp, true));
				}
			}

			Map<String, Node> mapListaBIRTReport = getListBIRTReport(listaServizi);
			if((mapListaBIRTReport.size()>0) || forExport){
				Node locGVBIRTReport = localXmlGVAdapter.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapter, "GVBIRTReportConfiguration"));
				parser.setAttribute((Element) locGVBIRTReport, "name", "BIRT_REPO");
				parser.setAttribute((Element) locGVBIRTReport, "type", "Module");
				parser.setAttribute((Element) locGVBIRTReport, "version", "1");

				Node locReportGroups = locGVBIRTReport.appendChild(parser.createElement(localXmlGVAdapter, "ReportGroups"));
				for (String nomeRp : mapListaBIRTReport.keySet()) {
					Node rp = mapListaBIRTReport.get(nomeRp);
					String group  = parser.get(rp, "parent::ReportGroup/@name");
					Node grp = parser.selectSingleNode(locReportGroups, "ReportGroup[@name='" + group + "']");
					if (grp == null) {
						grp = locReportGroups.appendChild(parser.createElement(localXmlGVAdapter, "ReportGroup"));
						((Element) grp).setAttribute("name", group);
					}
					grp.appendChild(localXmlGVAdapter.importNode(rp, true));
				}
			}

			Map<String, Node> mapListaRSHClient = getListRSHClient(listaServizi);
			if((mapListaRSHClient.size()>0) || forExport){
				Node locRSHService = localXmlGVAdapter.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapter, "RSHServiceClientConfiguration"));
				parser.setAttribute((Element) locRSHService, "name", "GVDP");
				parser.setAttribute((Element) locRSHService, "type", "Module");
				parser.setAttribute((Element) locRSHService, "version", "1.0");

				for (String nomeRc : mapListaRSHClient.keySet()) {
					Node rc = mapListaRSHClient.get(nomeRc);
					locRSHService.appendChild(localXmlGVAdapter.importNode(rc, true));
				}
			}

			Map<String,Node> listActionMapping = getListActionMapping(listaServizi);
			if((listActionMapping.size()>0) || forExport){
				Node base = localXmlGVAdapter.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapter, "GVAdapterHttpConfiguration"));
				parser.setAttribute((Element) base, "version", "1.0");
				parser.setAttribute((Element) base, "type", "module");
				parser.setAttribute((Element) base, "name", "HTTP_ADAPTER");
				Node inbCfg = base.appendChild(parser.createElement(localXmlGVAdapter, "InboundConfiguration"));
				Node mappings = inbCfg.appendChild(parser.createElement(localXmlGVAdapter, "ActionMappings"));
				if (forExport){
					inbCfg.appendChild(parser.createElement(localXmlGVAdapter, "InboundTransactions"));
					base.appendChild(parser.createElement(localXmlGVAdapter, "Formatters"));
				}

				for(String action:listActionMapping.keySet()){
					Node restMappings = mappings.appendChild(parser.createElement(localXmlGVAdapter, "RESTActionMapping"));
					Node nodeAtion = listActionMapping.get(action);
					parser.setAttribute((Element) restMappings, "Action", action);
					if(((Element) nodeAtion).hasAttribute("RespCharacterEncoding")) {
						((Element) restMappings).setAttribute("RespCharacterEncoding", ((Element) nodeAtion).getAttribute("RespCharacterEncoding"));
					}
					if(((Element) nodeAtion).hasAttribute("RespContentType")) {
						((Element) restMappings).setAttribute("RespContentType", ((Element) nodeAtion).getAttribute("RespContentType"));
					}
					if(((Element) nodeAtion).hasAttribute("class")) {
						((Element) restMappings).setAttribute("class", ((Element) nodeAtion).getAttribute("class"));
					}
					if(((Element) nodeAtion).hasAttribute("dump-in-out")) {
						((Element) restMappings).setAttribute("dump-in-out", ((Element) nodeAtion).getAttribute("dump-in-out"));
					}
					if(((Element) nodeAtion).hasAttribute("enabled")) {
						((Element) restMappings).setAttribute("enabled", ((Element) nodeAtion).getAttribute("enabled"));
					}
					if(((Element) nodeAtion).hasAttribute("type")) {
						((Element) restMappings).setAttribute("type", ((Element) nodeAtion).getAttribute("type"));
					}
					if(((Element) nodeAtion).hasAttribute("master-id-filter")) {
						((Element) restMappings).setAttribute("master-id-filter", ((Element) nodeAtion).getAttribute("master-id-filter"));
					}
					Node opMappings = restMappings.appendChild(parser.createElement(localXmlGVAdapter, "OperationMappings"));
					Map<String,Node> listRestActionMapping = getListaRestActionMapping(listaServizi,action);
					for(String rest:listRestActionMapping.keySet()){
						opMappings.appendChild(localXmlGVAdapter.importNode(listRestActionMapping.get(rest), true));
					}
				}
			}

			Map<String, Node> mapListaHL7Listener = getListHL7Listener(listaServizi);
			if((mapListaHL7Listener.size()>0) || forExport){
				Node locGVHL7ListenerManager = localXmlGVAdapter.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapter, "GVHL7ListenerManager"));
				parser.setAttribute((Element) locGVHL7ListenerManager, "name", "HL7_LISTENERS");
				parser.setAttribute((Element) locGVHL7ListenerManager, "type", "Module");
				parser.setAttribute((Element) locGVHL7ListenerManager, "version", "1.0");

				Node locHL7Listeners = locGVHL7ListenerManager.appendChild(parser.createElement(localXmlGVAdapter, "HL7Listeners"));
				for (String nomeCa : mapListaHL7Listener.keySet()) {
					Node ca = mapListaHL7Listener.get(nomeCa);
					String hl7l  = parser.get(ca, "ancestor::HL7Listener/@name");
					Node hl = parser.selectSingleNode(locHL7Listeners, "HL7Listener[@name='" + hl7l + "']/HL7Applications");
					if (hl == null) {
						Node hlp = parser.selectSingleNode(ca, "ancestor::HL7Listener");
						hl = locHL7Listeners.appendChild(parser.createElement(localXmlGVAdapter, "HL7Listener"));
						((Element) hl).setAttribute("name", hl7l);
						if(((Element) hlp).hasAttribute("type")) {
							((Element) hl).setAttribute("type", ((Element) hlp).getAttribute("type"));
						}
						if(((Element) hlp).hasAttribute("class")) {
							((Element) hl).setAttribute("class", ((Element) hlp).getAttribute("class"));
						}
						if(((Element) hlp).hasAttribute("port")) {
							((Element) hl).setAttribute("port", ((Element) hlp).getAttribute("port"));
						}
						if(((Element) hlp).hasAttribute("autoStart")) {
							((Element) hl).setAttribute("autoStart", ((Element) hlp).getAttribute("autoStart"));
						}
						if(((Element) hlp).hasAttribute("receivingApplication")) {
							((Element) hl).setAttribute("receivingApplication", ((Element) hlp).getAttribute("receivingApplication"));
						}
						if(((Element) hlp).hasAttribute("receivingFacility")) {
							((Element) hl).setAttribute("receivingFacility", ((Element) hlp).getAttribute("receivingFacility"));
						}
						hl = hl.appendChild(parser.createElement(localXmlGVAdapter, "HL7Applications"));
					}
					hl.appendChild(localXmlGVAdapter.importNode(ca, true));
				}
			}

			Map<String, Node> mapListaKnowledgeBase = getListKnowledgeBase(listaServizi);
			if((mapListaKnowledgeBase.size()>0) || forExport){
				Node locGVRulesConfigManager = localXmlGVAdapter.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapter, "GVRulesConfigManager"));
				parser.setAttribute((Element) locGVRulesConfigManager, "name", "RULES_CFG");
				parser.setAttribute((Element) locGVRulesConfigManager, "type", "Module");
				parser.setAttribute((Element) locGVRulesConfigManager, "version", "1.0");

				for (String nomeKb : mapListaKnowledgeBase.keySet()) {
					Node kb = mapListaKnowledgeBase.get(nomeKb);
					locGVRulesConfigManager.appendChild(localXmlGVAdapter.importNode(kb, true));
				}
			}

			Map<String, Node> mapListaSocialAdp = getListSocialAdp(listaServizi);
			if((mapListaSocialAdp.size()>0) || forExport){
				Node locGVSocialAdp = localXmlGVAdapter.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapter, "GVSocialAdapterManager"));
				parser.setAttribute((Element) locGVSocialAdp, "name", "GV_SOCIAL");
				parser.setAttribute((Element) locGVSocialAdp, "type", "Module");
				parser.setAttribute((Element) locGVSocialAdp, "version", "1.0");

				Node locSocAdapters = locGVSocialAdp.appendChild(parser.createElement(localXmlGVAdapter, "SocialAdapters"));
				if (!mapListaSocialAdp.isEmpty()) {
					Node sAdpAcc = null;
					for (String nomeSa : mapListaSocialAdp.keySet()) {
						Node sAcc = mapListaSocialAdp.get(nomeSa);
						if (sAdpAcc == null) {
							sAdpAcc = parser.selectSingleNode(locSocAdapters, "TwitterSocialAdapter/Accounts");
							if (sAdpAcc == null) {
								Node tsa = locSocAdapters.appendChild(parser.createElement(localXmlGVAdapter, "TwitterSocialAdapter"));
								parser.setAttribute((Element) tsa, "class", "it.greenvulcano.gvesb.social.twitter.TwitterSocialAdapter");
								parser.setAttribute((Element) tsa, "type", "social-adapter");
								parser.setAttribute((Element) tsa, "social", "twitter");
								sAdpAcc = tsa.appendChild(parser.createElement(localXmlGVAdapter, "Accounts"));
							}
						}
						Node sa = parser.selectSingleNode(sAdpAcc, "Account[@name='" + nomeSa + "']");
						if (sa == null) {
							sAdpAcc.appendChild(localXmlGVAdapter.importNode(sAcc, true));
						}
						else {
							sAdpAcc.replaceChild(sa, localXmlGVAdapter.importNode(sAcc, true));
						}
					}
				}
			}

			Map<String, Node> mapListaPushNotification = getListPushNotification(listaServizi);
			if((mapListaPushNotification.size()>0) || forExport){
				Node locGVPushMng = localXmlGVAdapter.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapter, "GVPushNotificationManager"));
				parser.setAttribute((Element) locGVPushMng, "name", "PUSH_NOTIFICATION");
				parser.setAttribute((Element) locGVPushMng, "type", "Module");
				parser.setAttribute((Element) locGVPushMng, "version", "1.0");

				Node locPushNots = locGVPushMng.appendChild(parser.createElement(localXmlGVAdapter, "NotificationEngines"));
				for (String nomePn : mapListaPushNotification.keySet()) {
					Node pne = mapListaPushNotification.get(nomePn);
					locPushNots.appendChild(localXmlGVAdapter.importNode(pne, true));
				}
			}
			return localXmlGVAdapter;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return null;
	}

	public Map<String, Node> getListaGvWebServices()
	{
		Map<String, Node> mapService = getListaNodeServizi();
		return getListaGvWebServices(mapService);
	}

	private Map<String, Node> getListaGvWebServices(List<String> listService)
	{
		Map<String, Node> mapService = getListaNodeServizi(listService);
		return getListaGvWebServices(mapService);
	}

	private Map<String, Node> getListaGvWebServices(Map<String, Node> mapService)
	{
		XMLUtils parser = null;
		Map<String, Node> mapGvWS = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			for (String service : mapService.keySet()) {
				Node serviceNode = mapService.get(service);
				NodeList operations = parser.selectNodeList(serviceNode, "Operation");
				for (int i = 0; i < operations.getLength(); i++){
					String operation = parser.get(operations.item(i), "@name");
					String xPath = "/GVAdapters/GVWebServices/GreenVulcanoWebServices/GreenVulcanoWebService[@gv-service='" + service + "' and @gv-operation='" + operation + "']";
					Node wsNode = parser.selectSingleNode(this.adapterXml, xPath);
					if (wsNode != null) {
						mapGvWS.put(service+operation, wsNode);
					}
				}
			}
			return mapGvWS;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return mapGvWS;
	}

	public Map<String, Node> getListaBusinessWebServices()
	{
		Map<String, Node> mapService = getListaNodeServizi();
		return getListaBusinessWebServices(mapService);
	}

	public Map<String, Node> getListaBusinessWebServices(List<String> listService)
	{
		Map<String, Node> mapService = getListaNodeServizi(listService);
		return getListaBusinessWebServices(mapService);
	}

	private Map<String, Node> getListaBusinessWebServices(Map<String, Node> mapService)
	{
		XMLUtils parser = null;
		Map<String, Node> mapGvWS = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			for (String service : mapService.keySet()) {
				Node serviceNode = mapService.get(service);
				NodeList operations = parser.selectNodeList(serviceNode, "Operation");
				for (int i = 0; i < operations.getLength(); i++){
					String operation = parser.get(operations.item(i), "@name");
					String xPath = "/GVAdapters/GVWebServices/BusinessWebServices/WebService[WSOperation/Binding/@gv-service='" + service + "' and WSOperation/Binding/@gv-operation='" + operation + "']";
					Node wsNode = parser.selectSingleNode(this.adapterXml, xPath);
					if (wsNode != null) {
						mapGvWS.put(service+operation, wsNode);
					}
				}
			}
			return mapGvWS;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return mapGvWS;
	}

	private Node getNodeBusinessWebServices()
	{
		XMLUtils parser = null;
		Node nodeBusinessWebServices = null;
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVAdapters/GVWebServices/BusinessWebServices";
			nodeBusinessWebServices = parser.selectSingleNode(this.adapterXml, xPath);
			return nodeBusinessWebServices;
		} catch (XMLUtilsException e) {

			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return nodeBusinessWebServices;
	}

	public Map<String,Node> getListConnBuilder()
	{
		XMLUtils parser = null;
		Map<String, Node> listaCB = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVAdapters/GVJDBCConnectionBuilder/*[@type='jdbc-connection-builder']";
			NodeList lsts = parser.selectNodeList(this.adapterXml, xPath);
			for (int i = 0; i < lsts.getLength(); i++) {
				Node lst = lsts.item(i);
				String name = parser.get(lst, "@name");
				listaCB.put(name, lst);
			}
			return listaCB;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaCB;
	}

	public Map<String, Node> getListaAclGV()
	{
		List<String> listaServizi = getListaServizi();
		return getListaAclGV(listaServizi);
	}

	public Map<String, Node> getListaAclGV(List<String> listaServizi)
	{
		XMLUtils parser = null;
		Map<String, Node> listaAclGV = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			for (String servizio : listaServizi) {
				Map<String, Node> listaOperazioni = getListaOperazioni(servizio);
				for (String operazione : listaOperazioni.keySet()) {
					String xPath = "/GVCore/GVPolicy/ACLGreenVulcano/*[@service='"+servizio+"' and (@operazione='"+operazione+"' or not(@operation))]";
					Node localXml = parser.selectSingleNode(this.coreXml, xPath);
					if(localXml != null){
						logger.info("Found[" + this.type + "] ACL for Service " + servizio + "/" + operazione);
						String oper = parser.get(localXml, "@operazione","");
						String name = servizio+oper;
						listaAclGV.put(name, localXml);
					}

				}

			}
			return listaAclGV;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaAclGV;
	}

	public Map<String, Node> getListaAddressSet()
	{
		List<String> listaServizi = getListaServizi();
		return getListaAddressSet(listaServizi);
	}

	public Map<String, Node> getListaAddressSet(List<String> listaServizi)
	{
		XMLUtils parser = null;
		Map<String, Node> listaAclGV = getListaAclGV(listaServizi);
		Map<String, Node> listaAddressSet = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			for(String key:listaAclGV.keySet()){
				Node nodeAclGV = listaAclGV.get(key);
				String addressesDef = parser.get(nodeAclGV, "ACL/AddressSetRef/@name");
				if(addressesDef != null){
					if (listaAddressSet.containsKey(addressesDef)) {
						continue;
					}
					Node nodeAddressSet = parser.selectSingleNode(this.coreXml, "/GVCore/GVPolicy/Addresses/AddressSet[@name='"+addressesDef+"']");
					if(nodeAddressSet != null) {
						logger.info("Found[" + this.type + "] AddressSetRef " + addressesDef);
						listaAddressSet.put(addressesDef, nodeAddressSet);
					}
				}
			}
			return listaAddressSet;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaAddressSet;
	}

	public Node getAddressSet(String addressesDef)
	{
		XMLUtils parser = null;
		Node nodeAddressSet = null;
		try {
			parser = XMLUtils.getParserInstance();
		    nodeAddressSet = parser.selectSingleNode(this.coreXml, "/GVCore/GVPolicy/Addresses/AddressSet[@name='"+addressesDef+"']");
			return nodeAddressSet;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return nodeAddressSet;
	}

	public Map<String, Node> getListaRoleRef()
	{
		List<String> listaServizi = getListaServizi();
		return getListaRoleRef(listaServizi);
	}

	public Map<String, Node> getListaRoleRef(List<String> listaServizi)
	{
		XMLUtils parser = null;
		Map<String, Node> listaAclGV = getListaAclGV(listaServizi);
		Map<String, Node> listaRoleDef = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			for(String key : listaAclGV.keySet()){
				Node nodeAclGV = listaAclGV.get(key);
				String roleDef = parser.get(nodeAclGV, "ACL/RoleRef/@name");
				if (listaRoleDef.containsKey(roleDef)) {
					continue;
				}
				Node nodeRoleDef = parser.selectSingleNode(this.coreXml, "/GVCore/GVPolicy/Roles/Role[@name='"+roleDef+"']");
				if(nodeRoleDef != null) {
					logger.info("Found[" + this.type + "] RoleRef " + roleDef);
					listaRoleDef.put(roleDef, nodeRoleDef);
				}
			}
			return listaRoleDef;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaRoleDef;
	}

	public Node getRoleRef(String roleDef)
	{
		XMLUtils parser = null;
		Node nodeRoleDef = null;
		try {
			parser = XMLUtils.getParserInstance();
			nodeRoleDef = parser.selectSingleNode(this.coreXml, "/GVCore/GVPolicy/Roles/Role[@name='"+roleDef+"']");
			return nodeRoleDef;
		} catch (XMLUtilsException e) {

			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return nodeRoleDef;
	}

	public Map<String, Node> getListaRestActionMapping(String action)
	{
		List<String> listaServizi = getListaServizi();
		return getListaRestActionMapping(listaServizi,action);
	}

	private Map<String, Node> getListaRestActionMapping(List<String> listaServizi,String action)
	{
		XMLUtils parser = null;
		Map<String, Node> listaRest = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			for (String servizio : listaServizi) {
				Map<String, Node> listaOperazioni = getListaOperazioni(servizio);
				for (String operazione : listaOperazioni.keySet()) {
					String xPath = "/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/ActionMappings/RESTActionMapping[@Action='"+action+"']/OperationMappings/Mapping[@service='"+servizio+"' and @operation='"+operazione+"']";
					Node localXml = parser.selectSingleNode(this.adapterXml, xPath);
					if(localXml!=null){
						if(parser.get(localXml, "@pattern")!=null){
							String name = parser.get(localXml, "@pattern");
							listaRest.put(name, localXml);
						}
					}
				}
			}
			return listaRest;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaRest;
	}

	public Map<String, Node> getListActionMapping()
	{
		List<String> listaServizi = getListaServizi();
		return getListActionMapping(listaServizi);
	}

	private Map<String, Node> getListActionMapping(List<String> listaServizi)
	{
		XMLUtils parser = null;
		Map<String, Node> listaRest = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			for (String servizio : listaServizi) {
				Map<String, Node> listaOperazioni = getListaOperazioni(servizio);
				for (String operazione : listaOperazioni.keySet()) {
					String xPath = "/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/ActionMappings/RESTActionMapping[OperationMappings/Mapping/@service='"+servizio+"' and OperationMappings/Mapping/@operation='"+operazione+"']";
					Node localXml = parser.selectSingleNode(this.adapterXml, xPath);
					if(localXml!=null){
						if(parser.get(localXml, "@Action")!=null){
							String name = parser.get(localXml, "@Action");
							listaRest.put(name, localXml);
						}
					}
				}
			}
			return listaRest;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaRest;
	}

    public Map<String, Node> getListHL7Listener()
	{
		List<String> listaServizi= getListaServizi();
		return getListHL7ListenerOnly(listaServizi);
	}

    // returns listener with applications
    public Map<String, Node> getListHL7Application(String listener)
	{
		XMLUtils parser = null;
		Map<String, Node> listaHL7 = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVAdapters/GVHL7ListenerManager/HL7Listeners/HL7Listener[@name='" + listener + "']/HL7Applications/*";
			NodeList apps = parser.selectNodeList(this.adapterXml, xPath);
			for (int i = 0; i < apps.getLength(); i++) {
				Node app = apps.item(i);
				String name = parser.get(app, "@name");
				listaHL7.put(name, app);
			}
			return listaHL7;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaHL7;
	}

    // returns listener with applications
    private Map<String, Node> getListHL7ListenerOnly(List<String> listaServizi)
	{
    	XMLUtils parser = null;
		Map<String, Node> listaHL7 = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVAdapters/GVHL7ListenerManager/HL7Listeners/HL7Listener";
			NodeList lsts = parser.selectNodeList(this.adapterXml, xPath);
			for (int i = 0; i < lsts.getLength(); i++) {
				Node lst = lsts.item(i);
				String name = parser.get(lst, "@name");
				listaHL7.put(name, lst);
			}
			return listaHL7;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaHL7;
	}

    // Returns applications
	private Map<String, Node> getListHL7Listener(List<String> listaServizi)
	{
		XMLUtils parser = null;
		Map<String, Node> listaHL7 = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			for (String servizio : listaServizi) {
				Map<String, Node> listaOperazioni = getListaOperazioni(servizio);
				for (String operazione : listaOperazioni.keySet()) {
					String xPath = "/GVAdapters/GVHL7ListenerManager/HL7Listeners/HL7Listener/HL7Applications/GVCoreApplication[@gv-service='" + servizio + "' and @gv-operation='" + operazione +"']";
					Node localXml = parser.selectSingleNode(this.adapterXml, xPath);
					if(localXml!=null){
						String name = parser.get(localXml, "@name");
						listaHL7.put(name, localXml);
					}
				}
			}
			return listaHL7;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaHL7;
	}

	public List<String> getListaFileKB()
	{
		List<String> listaServizi = getListaServizi();
		return getListaFileKB(listaServizi);
	}

	private List<String> getListaFileKB(List<String> listaServizi)
	{
		List<String> listaKb = new ArrayList<String>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			Map<String, Node> listaSistemi = getListaSistemi(listaServizi);
			for (String sistema:listaSistemi.keySet()){
				Map<String, Node> mapListaChannel = getListaChannel(listaServizi,sistema);
				for(String canale:mapListaChannel.keySet()){
					Map<String, Node> mapListaOperation = getListaVCLOp(listaServizi,sistema,canale);
					for(String operation:mapListaOperation.keySet()){
						Node oper = mapListaOperation.get(operation);
						if ("rules-call".equals(oper.getNodeName())) {
							String ruleSet  = parser.get(oper, "@ruleSet");
							String url = parser.get(this.adapterXml, "/GVAdapters/GVRulesConfigManager/GVKnowledgeBaseConfig[@name='" + ruleSet + "']/RuleResource[@resourceType='DRL']/@url", "");
							if (!"".equals(url)) {
								listaKb.add(url.substring(url.indexOf("/Rules") + 6));
							}
						}
					}
				}
			}
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaKb;
	}

    public Map<String, Node> getListKnowledgeBase()
	{
    	XMLUtils parser = null;
		Map<String, Node> listaKb = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVAdapters/GVRulesConfigManager/GVKnowledgeBaseConfig";
			NodeList lsts = parser.selectNodeList(this.adapterXml, xPath);
			for (int i = 0; i < lsts.getLength(); i++) {
				Node lst = lsts.item(i);
				String name = parser.get(lst, "@name");
				listaKb.put(name, lst);
			}
			return listaKb;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaKb;
	}

	private Map<String, Node> getListKnowledgeBase(List<String> listaServizi)
	{
		Map<String, Node> listaKb = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			Map<String, Node> listaSistemi = getListaSistemi(listaServizi);
			for (String sistema:listaSistemi.keySet()){
				Map<String, Node> mapListaChannel = getListaChannel(listaServizi,sistema);
				for(String canale:mapListaChannel.keySet()){
					Map<String, Node> mapListaOperation = getListaVCLOp(listaServizi,sistema,canale);
					for(String operation:mapListaOperation.keySet()){
						Node oper = mapListaOperation.get(operation);
						if ("rules-call".equals(oper.getNodeName())) {
							String ruleSet  = parser.get(oper, "@ruleSet");
							Node br = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVRulesConfigManager/GVKnowledgeBaseConfig[@name='" + ruleSet + "']");
							if (br != null) {
								listaKb.put(ruleSet, br);
							}
						}
					}
				}
			}
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaKb;
	}

    public Map<String, Node> getListSocialAdp()
	{
		List<String> listaServizi= getListaServizi();
		return getListSocialAdpOnly(listaServizi);
	}

    public Map<String, Node> getListSocialAdpAccounts(String social)
	{
		XMLUtils parser = null;
		Map<String, Node> listaSa = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVAdapters/GVSocialAdapterManager/SocialAdapters/*[@social='" + social + "']/Accounts/Account";
			NodeList accs = parser.selectNodeList(this.adapterXml, xPath);
			for (int i = 0; i < accs.getLength(); i++) {
				Node acc = accs.item(i);
				String name = parser.get(acc, "@name");
				listaSa.put(name, acc);
			}
			return listaSa;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaSa;
	}

	private Map<String, Node> getListSocialAdpOnly(List<String> listaServizi)
	{
		XMLUtils parser = null;
		Map<String, Node> listaSa = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVAdapters/GVSocialAdapterManager/SocialAdapters/*[@type='social-adapter']";
			NodeList lsts = parser.selectNodeList(this.adapterXml, xPath);
			for (int i = 0; i < lsts.getLength(); i++) {
				Node lst = lsts.item(i);
				String name = parser.get(lst, "@social");
				listaSa.put(name, lst);
			}
			return listaSa;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaSa;
	}

	private Map<String, Node> getListSocialAdp(List<String> listaServizi)
	{
		Map<String, Node> listaSa = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			Map<String, Node> listaSistemi = getListaSistemi(listaServizi);
			for (String sistema:listaSistemi.keySet()){
				Map<String, Node> mapListaChannel = getListaChannel(listaServizi,sistema);
				for(String canale:mapListaChannel.keySet()){
					Map<String, Node> mapListaOperation = getListaVCLOp(listaServizi,sistema,canale);
					for(String operation:mapListaOperation.keySet()){
						Node oper = mapListaOperation.get(operation);
						if (oper.getNodeName().startsWith("twitter-")) {
							String account  = parser.get(oper, "@account");
							Node sa = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVSocialAdapterManager/SocialAdapters/*[@social='twitter']/Accounts/Account[@name='" + account + "']");
							if (sa != null) {
								listaSa.put(account, sa);
							}
						}
					}
				}
			}
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaSa;
	}


    public Map<String, Node> getListPushNotification()
	{
    	XMLUtils parser = null;
		Map<String, Node> listaNe = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVAdapters/GVPushNotificationManager/NotificationEngines/*[@type='pushnotif']";
			NodeList lsts = parser.selectNodeList(this.adapterXml, xPath);
			for (int i = 0; i < lsts.getLength(); i++) {
				Node lst = lsts.item(i);
				String name = parser.get(lst, "@name");
				listaNe.put(name, lst);
			}
			return listaNe;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaNe;
	}

	private Map<String, Node> getListPushNotification(List<String> listaServizi)
	{
		Map<String, Node> listaNe = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			Map<String, Node> listaSistemi = getListaSistemi(listaServizi);
			for (String sistema : listaSistemi.keySet()){
				Map<String, Node> mapListaChannel = getListaChannel(listaServizi,sistema);
				for(String canale : mapListaChannel.keySet()){
					Map<String, Node> mapListaOperation = getListaVCLOp(listaServizi,sistema,canale);
					for(String operation : mapListaOperation.keySet()){
						Node oper = mapListaOperation.get(operation);
						if (oper.getNodeName().startsWith("push-")) {
							String eng  = parser.get(oper, "@defaultEngine");
							Node ne = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVPushNotificationManager/NotificationEngines/*[@type='pushnotif' and @name='" + eng + "']");
							if (ne != null) {
								listaNe.put(eng, ne);
							}
						}
					}
				}
			}
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaNe;
	}

	private Map<String, Node> getListaOperazioni(String servizio)
	{
		XMLUtils parser = null;
		Map<String, Node> listaOperazioni = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xpath= "/GVCore/GVServices/Services/Service[@id-service='"+servizio+"']/Operation";
			NodeList operations = parser.selectNodeList(this.coreXml, xpath);
			for(int i=0; i<operations.getLength(); i++){
				String name = parser.get(operations.item(i), "@name");
				if (name.equals("Forward")) {
					name = parser.get(operations.item(i), "@forward-name");
				}
				listaOperazioni.put(name, operations.item(i));
			}
			return listaOperazioni;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaOperazioni;
	}

	public Map<String,Node> getListaGVBufferDump(List<String> listaServizi)
	{
		Map<String,Node> mapGvDataDump = new HashMap<String,Node>();
		for (String nomeServizio : listaServizi) {
			Node gvDataDump = null;
			try {
				gvDataDump = XMLUtils.selectSingleNode_S(this.coreXml, "/GVCore/GVBufferDump/ServiceDump[@id-service='" + nomeServizio + "']");
			} catch (XMLUtilsException e) {
				logger.error(e);
			}
			if(gvDataDump != null) {
				logger.debug("Found[" + this.type + "] ServiceDump for Service " + nomeServizio);
				mapGvDataDump.put(nomeServizio, gvDataDump);
			}
		}
		return mapGvDataDump;
	}

	public Map<String,Node> getListaGVBufferDump()
	{
		List<String> listaServizi = getListaServizi();
		return getListaGVBufferDump(listaServizi);
	}

	public String getGvCore(Boolean forExport)
	{
		List<String> listaServizi = getListaServizi();
		Document localXmlGVCore = getDocGvCore(listaServizi,forExport);
		DOMWriter writer = new DOMWriter();
		OutputStream out = new ByteArrayOutputStream();
		String ret = "";
		try {
			writer.write(localXmlGVCore, out);
			ret = out.toString();
			out.close();
		} catch (IOException e) {
			logger.error(e);
		}
		return ret;
	}

	public String getGvCore(List<String> listaServizi,Boolean foExport)
	{
		Document localXmlGVCore;
		String ret = null;
		try {
			localXmlGVCore = getDocGvCore(listaServizi,foExport);

			DOMWriter writer = new DOMWriter();
			OutputStream out = new ByteArrayOutputStream();
			writer.write(localXmlGVCore, out);
			ret = out.toString();
			out.close();
		} catch (IOException e) {
			logger.error(e);
		}
		return ret;
	}

	public void writeGvCore(URL configURL)
	{
		Document localXmlGVCore = this.getCoreXml();
		DOMWriter writer = new DOMWriter();
		OutputStream out;
		try {
			out = new FileOutputStream(URLDecoder.decode(configURL.getPath(), "UTF-8"));
			writer.write(localXmlGVCore, out);
			out.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void writeGvAdapters(URL configURL)
	{
		Document localXmlGVAdapters = getAdapterXml();
		DOMWriter writer = new DOMWriter();
		OutputStream out;
		try {
			out = new FileOutputStream(URLDecoder.decode(configURL.getPath(), "UTF-8"));
			writer.write(localXmlGVAdapters, out);
			out.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}


	private Document getDocGvCore(List<String> listaServizi,Boolean forExport)
	{
		logger.debug("init getGvCore[" + this.type + "]");
		XMLUtils parser = null;
		String key = null;
		try {
			parser = XMLUtils.getParserInstance();

			Document localXmlGVCore = parser.newDocument("GVCore");
			DOMImplementation domImpl = localXmlGVCore.getImplementation();
			if(forExport){
			  DocumentType doctype = domImpl.createDocumentType("web-app",
				    	"SYSTEM",
					   "http://www.greenvulcano.com/gvesb/dtds/GVCore.dtd");
			  localXmlGVCore.appendChild(doctype);
			}

			Map<String, Node> mapLCryptoHelper = getListaIDKeyStore(listaServizi);
			if((mapLCryptoHelper.size()>0) || forExport){
			  Node locCripoHelper = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVCryptoHelper"));
			  parser.setAttribute((Element) locCripoHelper, "type", "module");
			  parser.setAttribute((Element) locCripoHelper, "name", "CRYPTO_HELPER");

			  for (String id:mapLCryptoHelper.keySet()) {
				locCripoHelper.appendChild(localXmlGVCore.importNode(mapLCryptoHelper.get(id), true));
			  }
			}

			Map<String, Node> mapListaForward = getListaForward(listaServizi);
			if((mapListaForward.size()>0) || forExport){
				Node locForwards = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVForwards"));
				parser.setAttribute((Element) locForwards, "type", "module");
				parser.setAttribute((Element) locForwards, "name", "JMS_FORWARD");

				for (String nomeForward:mapListaForward.keySet()) {
					locForwards.appendChild(localXmlGVCore.importNode(mapListaForward.get(nomeForward), true));
				}
			}

			Node locServices = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVServices"));
			parser.setAttribute((Element) locServices, "type", "module");
			parser.setAttribute((Element) locServices, "name", "SERVICES");
			Map<String, Node> mapListaGruppi = getGroups(listaServizi);
			if((mapListaGruppi.size()>0) || forExport){
				Node locGroups = locServices.appendChild(parser.createElement(localXmlGVCore, "Groups"));

				for (String nomeGruppo:mapListaGruppi.keySet()) {
					key = nomeGruppo;
					locGroups.appendChild(localXmlGVCore.importNode(mapListaGruppi.get(nomeGruppo), true));
				}
			}

			Node locService = locServices.appendChild(parser.createElement(localXmlGVCore, "Services"));
			for (String servizio : listaServizi) {
				Node nodeService = getNodeService(servizio);
				if((nodeService!=null) && (locService!=null)) {
					locService.appendChild(localXmlGVCore.importNode(nodeService, true));
				}
			}

			Map<String, Node> mapListaSistemi = getListaSistemi(listaServizi);
			if((mapListaSistemi.size() > 0) || forExport){
				Node locSystems = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVSystems"));
				parser.setAttribute((Element) locSystems, "type", "module");
				parser.setAttribute((Element) locSystems, "name", "SYSTEMS");
				locSystems = locSystems.appendChild(parser.createElement(localXmlGVCore, "Systems"));

				for (String sistema:mapListaSistemi.keySet()){
					Node locSystem = locSystems.appendChild(parser.createElement(localXmlGVCore, "System"));
					parser.setAttribute((Element) locSystem, "id-system", sistema);
					parser.setAttribute((Element) locSystem, "system-activation", "on");
					Map<String, Node> mapListaChannel = getListaChannel(listaServizi,sistema);
					for(String canale : mapListaChannel.keySet()){
						Map<String, Node> mapListaOperation = getListaVCLOp(listaServizi,sistema,canale);
						Node channel = locSystem.appendChild(parser.createElement(localXmlGVCore, "Channel"));
						parser.setAttribute((Element) channel, "id-channel", canale);
						for(String operation : mapListaOperation.keySet()){
							Node oper = mapListaOperation.get(operation);
							if(oper.getLocalName().equals("dh-call")){
								String operazione = parser.get(oper,"@name");
								Map<String, Node> mapListaServiziDhCall = getListaDboBuilder(listaServizi,sistema,canale,operazione);
								if(mapListaServiziDhCall.size() > 0){
									Node dhCall = channel.appendChild(parser.createElement(localXmlGVCore, "dh-call"));
									parser.setAttribute((Element) dhCall, "class", "it.greenvulcano.gvesb.virtual.datahandler.DataHandlerCallOperation");
									parser.setAttribute((Element) dhCall, "name", operazione);
									parser.setAttribute((Element) dhCall, "type", "call");
									for(String builder : mapListaServiziDhCall.keySet()){
										Node nodeDH = mapListaServiziDhCall.get(builder);
										dhCall.appendChild(localXmlGVCore.importNode(nodeDH, true));
									}
								}
							}else{
								channel.appendChild(localXmlGVCore.importNode(oper, true));
							}
						}
					}
				}
			}

			Map<String, Node> mapGVBufferDump = getListaGVBufferDump(listaServizi);
			if((mapGVBufferDump.size() > 0) || forExport){
				Node locGVBufferDump = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVBufferDump"));
				parser.setAttribute((Element) locGVBufferDump, "type", "module");
				parser.setAttribute((Element) locGVBufferDump, "name", "BUFFER_DUMP");
				parser.setAttribute((Element) locGVBufferDump, "log-dump-size", "-1");
				for (String servizio : mapGVBufferDump.keySet()) {
					locGVBufferDump.appendChild(localXmlGVCore.importNode(mapGVBufferDump.get(servizio), true));
				}
			}

			Map<String, Node> mapAddressSet = getListaAddressSet(listaServizi);
			Map<String, Node> mapRoleDef = getListaRoleRef(listaServizi);
			Map<String, Node> mapACLGreenVulcano = getListaAclGV(listaServizi);
			if(((mapAddressSet.size() + mapAddressSet.size() + mapACLGreenVulcano.size()) > 0) || forExport){
				Node locGVPolicy = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVPolicy"));
				parser.setAttribute((Element) locGVPolicy, "type", "module");
				parser.setAttribute((Element) locGVPolicy, "name", "POLICY_MANAGER");

				if(mapRoleDef.size() > 0){
					Node locGVRoles = locGVPolicy.appendChild(parser.createElement(localXmlGVCore, "Roles"));
					for (String servizio : mapRoleDef.keySet()) {
						locGVRoles.appendChild(localXmlGVCore.importNode(mapRoleDef.get(servizio), true));
					}
				}

				if(mapAddressSet.size() > 0){
					Node locGVAddress = locGVPolicy.appendChild(parser.createElement(localXmlGVCore, "Addresses"));
					for (String servizio : mapAddressSet.keySet()) {
						locGVAddress.appendChild(localXmlGVCore.importNode(mapAddressSet.get(servizio), true));
					}
				}

				if(mapACLGreenVulcano.size() > 0){
					Node nodeACLGreenVulcano = locGVPolicy.appendChild(parser.createElement(localXmlGVCore, "ACLGreenVulcano"));
					parser.setAttribute((Element) nodeACLGreenVulcano, "class", "it.greenvulcano.gvesb.policy.impl.ACLGreenVulcano");
					parser.setAttribute((Element) nodeACLGreenVulcano, "type", "acl-manager");
					for (String servizio : mapACLGreenVulcano.keySet()) {
						nodeACLGreenVulcano.appendChild(localXmlGVCore.importNode(mapACLGreenVulcano.get(servizio), true));
					}
				}
			}

			Map<String, Node> mapListaTrasformazioni = getListaTrasformazioni(listaServizi);
			if((mapListaTrasformazioni.size() > 0) || forExport){
				Node localTransformation = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVDataTransformation"));
				parser.setAttribute((Element) localTransformation, "type", "module");
				parser.setAttribute((Element) localTransformation, "name", "GVDT");

				Map<String, Node> mapListaDataSource = getListaDataSource(mapListaTrasformazioni);
				Node localNodeDataSource = localTransformation.appendChild(parser.createElement(localXmlGVCore, "DataSourceSets"));
				for (String nomeDataSource : mapListaDataSource.keySet()) {
					localNodeDataSource.appendChild(localXmlGVCore.importNode(mapListaDataSource.get(nomeDataSource), true));
				}
				Node localNodeTrasf = localTransformation.appendChild(parser.createElement(localXmlGVCore, "Transformations"));
				for (String nomeTrasformazione : mapListaTrasformazioni.keySet()) {
					localNodeTrasf.appendChild(localXmlGVCore.importNode(mapListaTrasformazioni.get(nomeTrasformazione), true));
				}
			}

			Map<String, Node> mapGVTask = getListaTask(listaServizi);
			if((mapGVTask.size() > 0) || forExport){
				Node localTask = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVTaskManagerConfiguration"));
				parser.setAttribute((Element) localTask, "type", "module");
				parser.setAttribute((Element) localTask, "name", "GVTASKS");
				Node localNodeTask = localTask.appendChild(parser.createElement(localXmlGVCore, "TaskGroups"));
				for (String nomeTask : mapGVTask.keySet()) {
					localNodeTask.appendChild(localXmlGVCore.importNode(mapGVTask.get(nomeTask), true));
				}
			}
			return localXmlGVCore;
		} catch (Exception e) {
			logger.error("Error importing '" + key + "'", e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return null;
	}

	public Map<String, Node> getListaForward(){
		List<String> listaServizi = getListaServizi();
		return getListaForward(listaServizi);
	}

	public Map<String, Node> getListaForward(List<String> listaServizi) {
		Map<String, Node> listaForward = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			for (String nomeServizio : listaServizi) {
				Map<String, Node> listaOperazioni  = getListaOperazioni(nomeServizio);
				for (String operazione : listaOperazioni.keySet()) {
					String  forwardName = operazione;
					String xpath = "/GVCore/GVForwards/ForwardConfiguration[@forwardName='"+forwardName+"']";
					//System.out.println("xpath="+xpath);
					NodeList forwards = parser.selectNodeList(this.coreXml, xpath);
					if (forwards.getLength() > 0) {
						for (int i = 0; i < forwards.getLength(); i++) {
							String name = parser.get(forwards.item(i),"@name");
							logger.debug("Found[" + this.type + "] Forward " + name + " for Service " + nomeServizio + "/" + operazione);
							listaForward.put(name, forwards.item(i));
						}
					}
				}
			}
			return listaForward;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaForward;
	}

	public Map<String, Node> getListaForward(Map<String, Node> mapService) {
		Map<String, Node> mapForward = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			for (String service : mapService.keySet()) {
				Node serviceNode = mapService.get(service);
				NodeList operations = parser.selectNodeList(serviceNode, "Operation");
				for (int i = 0; i < operations.getLength(); i++){
					String operation = parser.get(operations.item(i), "@name");
					if (operation.equals("Forward")) {
						String xpath = "/GVCore/GVForwards/ForwardConfiguration[@forwardName='" + operation + "']";
						NodeList forwards = parser.selectNodeList(this.coreXml, xpath);
						if (forwards.getLength() > 0) {
							for (int f = 0; f < forwards.getLength(); f++) {
								String fwName = parser.get(forwards.item(f),"@name");
								logger.debug("Found[" + this.type + "] Forward " + fwName + " for Service " + service + "/" + operation);
								mapForward.put(fwName, forwards.item(i));
							}
						}
					}
				}
			}
			return mapForward;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return mapForward;
	}

	public Map<String, Node> getListaDboBuilder(String sistema, String canale, String operazione) {
		List<String> listaServizi = getListaServizi();
		return getListaDboBuilder(listaServizi, sistema, canale, operazione);
	}

	public Map<String, Node> getListaDboBuilder(List<String> listaServizi, String sistema, String canale, String operazione){
		Map<String, Node> listaOperation = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			for (String nomeServizio : listaServizi) {
				String xpath= "/GVCore/GVServices/Services/Service[@id-service='"+nomeServizio+"']/Operation/Flow/GVOperationNode[@id-system='"+sistema+"' and @operation-name='"+operazione+"']";
				//System.out.println("xpath="+xpath);
				NodeList operations = parser.selectNodeList(this.coreXml, xpath);
				if (operations.getLength() > 0) {
					for (int i = 0; i < operations.getLength(); i++) {
						Node gvOperNode = operations.item(i);
						Node nodeOpDH = parser.selectSingleNode(gvOperNode, "InputServices/dh-selector-service/dh-selector-call/@DH_SERVICE_NAME");
						String nomeOpDH =null;
						if(nodeOpDH != null){
							nomeOpDH = nodeOpDH.getNodeValue();
						}else{
							nomeOpDH = nomeServizio;
						}
						xpath="/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/dh-call/DBOBuilder[@name='"+nomeOpDH+"']";
						logger.debug("Found[" + this.type + "] Flow DBOBuilder " + nomeServizio + "/" + sistema + "/" + canale + "/" + nomeOpDH);
						Node operation = parser.selectSingleNode(this.coreXml, xpath);
						   if(operation!=null) {
							listaOperation.put(nomeOpDH, operation);
						}
					}
				}
				xpath= "/GVCore/GVServices/Services/Service[@id-service='"+nomeServizio+"']/Operation/SubFlow/GVOperationNode[@id-system='"+sistema+"' and @operation-name='"+operazione+"']";
				//System.out.println("xpath="+xpath);
				operations = parser.selectNodeList(this.coreXml, xpath);
				if (operations.getLength() > 0) {
					for (int i = 0; i < operations.getLength(); i++) {
						Node gvOperNode = operations.item(i);
						Node nodeOpDH = parser.selectSingleNode(gvOperNode, "InputServices/dh-selector-service/dh-selector-call/@DH_SERVICE_NAME");
						String nomeOpDH =null;
						if(nodeOpDH != null){
							nomeOpDH = nodeOpDH.getNodeValue();
						}else{
							nomeOpDH = nomeServizio;
						}
						xpath="/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/dh-call/DBOBuilder[@name='"+nomeOpDH+"']";
						logger.debug("Found[" + this.type + "] SubFlow DBOBuilder " + nomeServizio + "/" + sistema + "/" + canale + "/" + nomeOpDH);
						Node operation = parser.selectSingleNode(this.coreXml, xpath);
						   if(operation!=null) {
							listaOperation.put(nomeOpDH, operation);
						}
					}
				}
			}
			return listaOperation;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaOperation;
	}

	public Map<String, Node> getListaDboBuilder(Node opNode, Node vclNode){
		Map<String, Node> dboBuilderMap = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();

			String service = parser.get(opNode.getParentNode(), "@id-service");
			String system  = parser.get(vclNode.getParentNode().getParentNode(), "@id-system");
			String channel = parser.get(vclNode.getParentNode(), "@id-channel");
			String  vclOp  = parser.get(vclNode, "@name");

			NodeList operNodes = parser.selectNodeList(opNode, ".//GVOperationNode[@id-system='" + system + "' and @operation-name='" + vclOp + "']");
			if (operNodes.getLength() > 0) {
				for (int i = 0; i < operNodes.getLength(); i++) {
					Node gvOperNode = operNodes.item(i);
					String dhName = parser.get(gvOperNode, "InputServices/dh-selector-service/dh-selector-call/@DH_SERVICE_NAME", "");
					if ("".equals(dhName)){
						dhName = service;
					}
					logger.debug("Found[" + this.type + "] DBOBuilder " + service + "/" + system + "/" + channel + "/" + vclOp + "/" + dhName);
					Node dboNode = parser.selectSingleNode(vclNode, "DBOBuilder[@name='" + dhName + "']");
					if (dboNode != null) {
						dboBuilderMap.put(dhName, dboNode);
					}
				}
			}
			return dboBuilderMap;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return dboBuilderMap;
	}

	public Node getDboBuilder(String dboBuilder, String sistema, String canale, String operazione){
		Node nodeDboBuilder = null;
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
		    String xpath="/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/dh-call[@name='"+operazione+"']/DBOBuilder[@name='"+dboBuilder+"']";
			nodeDboBuilder = parser.selectSingleNode(this.coreXml, xpath);
			return nodeDboBuilder;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return nodeDboBuilder;
	}

	public Map<String, Node> getListaServiziDhCall(String servizio, String sistema, String canale, String operazione){
		Map<String, Node> listaOperation = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
				String xpath= "/GVCore/GVServices/Services/Service[@id-service='"+servizio+"']/Operation/Flow/GVOperationNode[@id-system='"+sistema+"' and @operation-name='"+operazione+"']";
				//System.out.println("xpath="+xpath);
				NodeList operations = parser.selectNodeList(this.coreXml, xpath);
				if (operations.getLength() > 0) {
					for (int i = 0; i < operations.getLength(); i++) {
						Node gvOperNode = operations.item(i);
						Node nameOpDH = parser.selectSingleNode(gvOperNode, "InputServices/dh-selector-service/dh-selector-call/@DH_SERVICE_NAME");
						if(nameOpDH!=null){
							xpath="/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/dh-call/DBOBuilder[@name='"+nameOpDH.getNodeValue()+"']";
							Node operation = parser.selectSingleNode(this.coreXml, xpath);
							if(operation != null) {
								logger.debug("Found[" + this.type + "] Service " + servizio + " uses DBOBuilder=" + sistema + "/" + canale + "/" + nameOpDH.getLocalName());
								listaOperation.put(nameOpDH.getLocalName(), operation);
							}
						}
					}
				}
			return listaOperation;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaOperation;
	}

	public Map<String,Node> getListaGVCryptoHelper(List<String> listaServizi)
	{
		Map<String,Node> listTrasformazioni = getListaTrasformazioni(listaServizi);
		return getListaCryptoHelper(listTrasformazioni);
	}

	public Map<String,Node> getListaGVCryptoHelper()
	{
		XMLUtils parser = null;
		Map<String, Node> listaCe = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVCore/GVCryptoHelper/*";
			NodeList lsts = parser.selectNodeList(this.coreXml, xPath);
			for (int i = 0; i < lsts.getLength(); i++) {
				Node lst = lsts.item(i);
				String name = parser.get(lst, "@id");
				listaCe.put(name, lst);
			}
			return listaCe;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaCe;
	}

	private Map<String,Node> getListaCryptoHelper(Map <String,Node>listaTrasformazioni)
	{
		logger.debug("init getGVCryptoHelper[" + this.type + "]");
		String[] listaKeyId = getListaKeyIdTrasformazioni(listaTrasformazioni);
		Map<String,Node> gvCryptoHelper = new HashMap<String,Node>();;
		String strXpath = "";
		if (listaKeyId != null) {
			for (int i = 0; i < listaKeyId.length; i++) {
				if (i == 0) {
					strXpath = "/GVCore/GVCryptoHelper/KeyID[@id='";
				}
				strXpath = strXpath + listaKeyId[i] + "'";
				if (i == (listaKeyId.length - 1)) {
					strXpath = strXpath + "]";
				}
				else {
					strXpath = strXpath + " or @id='";
				}
			}
			logger.debug("strXpath = " + strXpath);

			Node nodeGvCryptoHelper;
			try {
				nodeGvCryptoHelper = XMLUtils.selectSingleNode_S(this.coreXml, strXpath);
				String name = XMLUtils.get_S(nodeGvCryptoHelper, "@id");
				gvCryptoHelper.put(name, nodeGvCryptoHelper);
			} catch (XMLUtilsException e) {
				logger.error(e);
			}
		}
		return gvCryptoHelper;
	}

	public Node getGVCryptoHelper(String name)
	{
		Node nodeGvCryptoHelper = null;
		String strXpath = "/GVCore/GVCryptoHelper[KeyID/@id='"+name+"']";
		try {
			nodeGvCryptoHelper = XMLUtils.selectSingleNode_S(this.coreXml, strXpath);
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		return nodeGvCryptoHelper;
	}

	private Node getGVKeyStoreCryptoHelper(String id)
	{
		logger.debug("init getGVCryptoHelper[" + this.type + "]");

		String strXpath = "/GVCore/GVCryptoHelper/KeyStoreID[@id='"+id+"']";
		Node nodeGvCryptoHelper = null;
		try {
			nodeGvCryptoHelper = XMLUtils.selectSingleNode_S(this.coreXml, strXpath);
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		return nodeGvCryptoHelper;
	}

	public Map<String,Node> getListaGVXPath()
	{
		XMLUtils parser = null;
		Map<String, Node> listaNs = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVCore/GVXPath/XPath/XPathNamespace";
			NodeList lsts = parser.selectNodeList(this.coreXml, xPath);
			for (int i = 0; i < lsts.getLength(); i++) {
				Node lst = lsts.item(i);
				String name = parser.get(lst, "@prefix");
				listaNs.put(name, lst);
			}
			return listaNs;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaNs;
	}

	private Node getNodeService(String nomeServizio)
	{
		Node service = null;
		try {
			service = XMLUtils.selectSingleNode_S(this.coreXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio + "']");
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		return service;
	}

	public Map<String, Node> getListaTask()
	{
		List<String> listaServizi = getListaServizi();
		return getListaTask(listaServizi);
	}

	public Map<String, Node> getGroups()
	{
		List<String> listaServizi = getListaServizi();
		return getGroups(listaServizi);
	}

	public Map<String, Node> getListaTask(List<String> listaServizi)
	{
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			Map<String, Node> listTask = new HashMap<String,Node>();
			for (String servizio : listaServizi) {
				String xPathTask = "/GVCore/GVTaskManagerConfiguration/TaskGroups/TaskGroup[ServiceCallerTask/@id-service='" + servizio+ "']";
				NodeList tasks = parser.selectNodeList(this.coreXml, xPathTask);
				if (tasks.getLength() > 0) {
					for (int i = 0; i < tasks.getLength(); i++) {
						String name = parser.get(tasks.item(i), "@name");
						logger.debug("Found[" + this.type + "] TimerTask " + name + " for Service " + servizio);
						listTask.put(name, tasks.item(i));
					}
				}
			}
			return listTask;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return null;
	}

	public Map<String, Node> getGroups(List<String> listService)
	{
		Map<String, Node> mapGroup = new HashMap<String,Node>();
		XMLUtils parser = null;
		String key = null;
		try {
			parser = XMLUtils.getParserInstance();
			for (String service : listService) {
				key = service;
				String group = parser.get(this.coreXml, "/GVCore/GVServices/Services/Service[@id-service='" + service + "']/@group-name");
				if((group != null) && (group.length() > 0) && !mapGroup.containsKey(group)){
					key += ":" + group;
					Node groupNode = parser.selectSingleNode(this.coreXml, "/GVCore/GVServices/Groups/Group[@id-group='" + group + "']");
					if (groupNode != null) {
						logger.debug("Found[" + this.type + "] Group " + group + " for Service " + service);
						mapGroup.put(group, groupNode);
					}
				}
			}
			return mapGroup;
		} catch (Exception e) {
			logger.error("Error processing " + key, e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return mapGroup;
	}

	public Map<String, Node> getGroups(Map<String, Node> mapService)
	{
		Map<String, Node> mapGroup = new HashMap<String,Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			for (String service : mapService.keySet()) {
				String group = parser.get(mapService.get(service), "@group-name");
				if ((group != null) && !mapGroup.containsKey(group)){
					Node groupNode = parser.selectSingleNode(this.coreXml, "/GVCore/GVServices/Groups/Group[@id-group='" + group + "']");
					if(group != null) {
						logger.debug("Found[" + this.type + "] Group " + group + " for Service " + service);
						mapGroup.put(group, groupNode);
					}
				}
			}
			return mapGroup;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return mapGroup;
	}

	public Map<String, Node> getListDataProvider()
	{
		Map<String, Node> mapService = getListaNodeServizi();
		return getListDataProvider(mapService);
	}

	private Map<String, Node> getListDataProvider(List<String> listService)
	{
		Map<String, Node> mapService = getListaNodeServizi(listService);
		return getListDataProvider(mapService);
	}

	private Map<String, Node> getListDataProvider(Map<String, Node> mapService)
	{
		Map<String, Node> mapDP = new Hashtable<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			for (String service : mapService.keySet()) {
				getListDataProviderCore(mapService.get(service), mapDP, parser);
			}
			//j2ee-ejb-call: input-ref-dp,output-ref-dp
			//jms-enqueue: ref-dp
			//jms-dequeue: ref-dp
			//RestServiceInvoker: ref-dp
			//http-call: ref-dp

			getListDataProviderForward(mapService, mapDP, parser);
			getListDataProviderWs(mapService, mapDP, parser);
			getListDataProviderVCLOperation(mapService, mapDP, parser);
			return mapDP;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return mapDP;
	}

	private void getListDataProviderForward(Map<String, Node> mapService, Map<String, Node> mapDP, XMLUtils parser)
	{
		Map<String, Node> mapForward = getListaForward(mapService);
		for (Node forward : mapForward.values()) {
			try {
				String dp = parser.get(forward, "@ref-dp", "");
				if (!("".equals(dp) || mapDP.containsKey(dp))) {
					Node nodeDp = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='" + dp + "']");
					if ( nodeDp != null) {
						mapDP.put(dp, nodeDp);
					}
				}
			} catch (XMLUtilsException e) {
				logger.error(e);
			}
		}
	}

	private void getListDataProviderWs(Map<String, Node> mapService, Map<String, Node> mapDP, XMLUtils parser)
	{
		try {
			Map<String, Node> mapWs = this.getListaGvWebServices(mapService);
			for (String wsName : mapWs.keySet()) {
				Node wsNode = mapWs.get(wsName);
				String dp = parser.get(wsNode, "@input-dp", "");
				if (!("".equals(dp) || mapDP.containsKey(dp))) {
					Node nodeDp = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
					if (nodeDp != null) {
						mapDP.put(dp, nodeDp);
					}
				}
				dp = parser.get(wsNode, "@output-dp", "");
				if (!("".equals(dp) || mapDP.containsKey(dp))) {
					Node nodeDp = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
					if (nodeDp != null) {
						mapDP.put(dp, nodeDp);
					}
				}
			}

			mapWs = this.getListaBusinessWebServices(mapService);
			for (String wsName : mapWs.keySet()) {
				Node wsNode = mapWs.get(wsName);
				NodeList ops = parser.selectNodeList(wsNode, "WSOperation");
				for (int i = 0; i < ops.getLength(); i++) {
					String dp = parser.get(ops.item(i), "@ref-dp", "");
					if (!("".equals(dp) || mapDP.containsKey(dp))) {
						Node nodeDp = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
						if (nodeDp != null) {
							mapDP.put(dp, nodeDp);
						}
					}
				}
			}
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
	}

	private void getListDataProviderCore(Node service, Map<String, Node> mapDP, XMLUtils parser)
	{
		try {
			NodeList dps = parser.selectNodeList(service, ".//*[@collection-dp]");
			for (int i = 0; i < dps.getLength(); i++) {
				String dp = parser.get(dps.item(i), "@collection-dp");
				if (!mapDP.containsKey(dp)) {
					Node nodeDp = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='" + dp + "']");
					if( nodeDp != null) {
						mapDP.put(dp, nodeDp);
					}
				}
			}

			dps = parser.selectNodeList(service, ".//*[@ref-dp]");
			for (int i = 0; i < dps.getLength(); i++) {
				String dp = parser.get(dps.item(i), "@ref-dp");
				if (!mapDP.containsKey(dp)) {
					Node nodeDp = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='" + dp + "']");
					if( nodeDp != null) {
						mapDP.put(dp, nodeDp);
					}
				}
			}

			dps = parser.selectNodeList(service, ".//*[@partition-dp]");
			for (int i = 0; i < dps.getLength(); i++) {
				String dp = parser.get(dps.item(i), "@partition-dp");
				if (!mapDP.containsKey(dp)) {
					Node nodeDp = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='" + dp + "']");
					if( nodeDp != null) {
						mapDP.put(dp, nodeDp);
					}
				}
			}
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
	}

	private void getListDataProviderVCLOperation(Map<String, Node> mapService, Map<String, Node> mapDP, XMLUtils parser)
	{
		try {
			for (String servizio : mapService.keySet()){
				Node servNode = mapService.get(servizio);

				NodeList operations = parser.selectNodeList(servNode, ".//Operation");
				for (int i = 0; i < operations.getLength(); i++){
					Node opNode = operations.item(i);

					Map<String, Node> mapVCLOp = getListaVCLOp(opNode);
					for (String vclOp : mapVCLOp.keySet()){
						Node vclNode = mapVCLOp.get(vclOp);

						String dp = parser.get(vclNode, "@ref-dp");
						if((dp != null) && !mapDP.containsKey(dp)) {
							Node nodeDp = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
							if(nodeDp != null) {
								mapDP.put(dp, nodeDp);
							}
						}
						dp = parser.get(vclNode, "*/@ref-dp");
						if((dp != null) && !mapDP.containsKey(dp)) {
							Node nodeDp = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
							if(nodeDp!=null) {
								mapDP.put(dp, nodeDp);
							}
						}
						dp = parser.get(vclNode, "@input-ref-dp");
						if((dp != null) && !mapDP.containsKey(dp)) {
							Node nodeDp = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
							if(nodeDp!=null) {
								mapDP.put(dp, nodeDp);
							}
						}
						dp = parser.get(vclNode, "@output-ref-dp");
						if((dp != null) && !mapDP.containsKey(dp)) {
							Node nodeDp = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
							if(nodeDp!=null) {
								mapDP.put(dp, nodeDp);
							}
						}
						dp = parser.get(vclNode, "@globals-ref-dp");
						if((dp != null) && !mapDP.containsKey(dp)) {
							Node nodeDp = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
							if(nodeDp!=null) {
								mapDP.put(dp, nodeDp);
							}
						}
					}
				}
			}
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
	}


	public Map<String, Node> getListExcelWorkBook()
	{
		XMLUtils parser = null;
		Map<String, Node> listaWb = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVAdapters/GVExcelWorkbookConfiguration/GVExcelWorkbook";
			NodeList lsts = parser.selectNodeList(this.adapterXml, xPath);
			for (int i = 0; i < lsts.getLength(); i++) {
				Node lst = lsts.item(i);
				String name = parser.get(lst, "@configName");
				listaWb.put(name, lst);
			}
			return listaWb;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaWb;
	}

	private Map<String, Node> getListExcelWorkBook(List<String> listaServizi)
	{
		Map<String, Node> listaWb = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			Map<String, Node> listaSistemi = getListaSistemi(listaServizi);
			for (String sistema : listaSistemi.keySet()){
				Map<String, Node> mapListaChannel = getListaChannel(listaServizi,sistema);
				for(String canale : mapListaChannel.keySet()){
					Map<String, Node> mapListaOperation = getListaVCLOp(listaServizi,sistema,canale);
					for(String operation : mapListaOperation.keySet()){
						Node oper = mapListaOperation.get(operation);
						if ("excel-call".equals(oper.getNodeName())) {
							String wbName = null;
							Node rep = parser.selectSingleNode(oper, "GVExcelReport']");
							if (rep == null) {
								String group  = parser.get(oper, "@group", "Generic");
								String report = parser.get(oper, "@report");
								wbName = parser.get(this.adapterXml, "/GVAdapters/GVExcelCreatorConfiguration/GVExcelReport[@name='" + report + "' and @group='" + group + "']/@format", "default");
							}
							else {
								wbName = parser.get(rep, "@format", "default");
							}
							Node wb = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVExcelWorkbookConfiguration/GVExcelWorkbook[@configName='"+wbName+"']");
							if(wb != null) {
								listaWb.put(wbName, wb);
							}
						}
					}
				}
			}
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaWb;
	}

    public Map<String, Node> getListExcelReport()
	{
    	XMLUtils parser = null;
		Map<String, Node> listaEr = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVAdapters/GVExcelCreatorConfiguration/GVExcelReport";
			NodeList lsts = parser.selectNodeList(this.adapterXml, xPath);
			for (int i = 0; i < lsts.getLength(); i++) {
				Node lst = lsts.item(i);
				String name = parser.get(lst, "@name");
				listaEr.put(name, lst);
			}
			return listaEr;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaEr;
	}

	private Map<String, Node> getListExcelReport(List<String> listaServizi)
	{
		Map<String, Node> listaEr = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			Map<String, Node> listaSistemi = getListaSistemi(listaServizi);
			for (String sistema : listaSistemi.keySet()){
				Map<String, Node> mapListaChannel = getListaChannel(listaServizi,sistema);
				for(String canale : mapListaChannel.keySet()){
					Map<String, Node> mapListaOperation = getListaVCLOp(listaServizi,sistema,canale);
					for(String operation : mapListaOperation.keySet()){
						Node oper = mapListaOperation.get(operation);
						if ("excel-call".equals(oper.getNodeName())) {
							String erName = null;
							Node rep = parser.selectSingleNode(oper, "GVExcelReport']");
							if (rep == null) {
								String group  = parser.get(oper, "@group", "Generic");
								String report = parser.get(oper, "@report");
								Node er = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVExcelCreatorConfiguration/GVExcelReport[@name='" + report + "' and @group='" + group + "']");
								if(er != null) {
									listaEr.put(erName, er);
								}
							}
						}
					}
				}
			}
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaEr;
	}

    public Map<String, Node> getListBIRTReport()
	{
		List<String> listaServizi= getListaServizi();
		return getListBIRTReport(listaServizi);
	}

	public Map<String, Node> getListBIRTReport(String group)
	{
		XMLUtils parser = null;
		Map<String, Node> listaBr = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVAdapters/GVBIRTReportConfiguration/ReportGroups/ReportGroup[@name='" + group + "']/Report";
			NodeList reports = parser.selectNodeList(this.adapterXml, xPath);
			for (int i = 0; i < reports.getLength(); i++) {
				Node report = reports.item(i);
				String name = parser.get(report, "@name");
				listaBr.put(name, report);
			}
			return listaBr;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaBr;
	}

	public Map<String, Node> getListBIRTReportGroup()
	{
    	XMLUtils parser = null;
		Map<String, Node> listaBrg = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVAdapters/GVBIRTReportConfiguration/ReportGroups/ReportGroup";
			NodeList lsts = parser.selectNodeList(this.adapterXml, xPath);
			for (int i = 0; i < lsts.getLength(); i++) {
				Node lst = lsts.item(i);
				String name = parser.get(lst, "@name");
				listaBrg.put(name, lst);
			}
			return listaBrg;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaBrg;
	}

	public Node getListBIRTReportGroup(String group)
	{
		Node brg = null;
		try {
			brg = XMLUtils.selectSingleNode_S(this.adapterXml, "/GVAdapters/GVBIRTReportConfiguration/ReportGroups/ReportGroup[@name='" + group + "']");
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		return brg;
	}

	private Map<String, Node> getListBIRTReport(List<String> listaServizi)
	{
		Map<String, Node> listaBr = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			Map<String, Node> listaSistemi = getListaSistemi(listaServizi);
			for (String sistema : listaSistemi.keySet()){
				Map<String, Node> mapListaChannel = getListaChannel(listaServizi,sistema);
				for(String canale : mapListaChannel.keySet()){
					Map<String, Node> mapListaOperation = getListaVCLOp(listaServizi,sistema,canale);
					for(String operation : mapListaOperation.keySet()){
						Node oper = mapListaOperation.get(operation);
						if ("birt-report-call".equals(oper.getNodeName())) {
							String group  = parser.get(oper, "@groupName");
							String report = parser.get(oper, "@reportName");
							Node br = parser.selectSingleNode(this.adapterXml, "/GVAdapters/GVBIRTReportConfiguration/ReportGroups/ReportGroup[@name='" + group + "']/Report[@name='" + report + "']");
							if (br != null) {
								listaBr.put(report, br);
							}
						}
					}
				}
			}
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaBr;
	}

    public Map<String, Node> getListRSHClient()
	{
    	XMLUtils parser = null;
		Map<String, Node> listaRc = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVAdapters/RSHServiceClientConfiguration/*[@type='rshClient']";
			NodeList lsts = parser.selectNodeList(this.adapterXml, xPath);
			for (int i = 0; i < lsts.getLength(); i++) {
				Node lst = lsts.item(i);
				String name = parser.get(lst, "@name");
				listaRc.put(name, lst);
			}
			return listaRc;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaRc;
	}

	private Map<String, Node> getListRSHClient(List<String> listaServizi)
	{
		Map<String, Node> listaRc = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			Map<String, Node> listaSistemi = getListaSistemi(listaServizi);
			for (String sistema : listaSistemi.keySet()){
				Map<String, Node> mapListaChannel = getListaChannel(listaServizi,sistema);
				for(String canale : mapListaChannel.keySet()){
					Map<String, Node> mapListaOperation = getListaVCLOp(listaServizi,sistema,canale);
					for(String operation : mapListaOperation.keySet()){
						Node oper = mapListaOperation.get(operation);
						if (("rsh-call".equals(oper.getNodeName())) || ("rsh-filereader-call".equals(oper.getNodeName())) || ("rsh-filewriter-call".equals(oper.getNodeName()))) {
							String rshC  = parser.get(oper, "@rsh-client-name");
							Node rc = parser.selectSingleNode(this.adapterXml, "/GVAdapters/RSHServiceClientConfiguration/*[@name='" + rshC + "']");
							if (rc != null) {
								listaRc.put(rshC, rc);
							}
						}
					}
				}
			}
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaRc;
	}

	public Map<String, Node> getListaSistemi()
	{
		List<String> listaServizi= getListaServizi();
		return getListaSistemi(listaServizi);
	}

	public Map<String, Node> getListaSistemi(List<String> listaServizi)
	{
		Map<String, Node> listaSistemi = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			for (String nomeServizio : listaServizi) {
				NodeList partecipants = parser.selectNodeList(this.coreXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
						+ "']/Operation/Participant");
				if (partecipants.getLength() > 0) {
					for (int i = 0; i < partecipants.getLength(); i++) {
						String name = parser.get(partecipants.item(i), "@id-system");
						if (listaSistemi.containsKey(name)) {
							continue;
						}
						Node sytem = parser.selectSingleNode(this.coreXml, "/GVCore/GVSystems/Systems/System[@id-system='"+name+"']");
						logger.debug("Found[" + this.type + "] Partecipant " + name + " for Service " + nomeServizio);
						listaSistemi.put(name, sytem);
					}
				}
			}
			return listaSistemi;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaSistemi;
	}

	public Node getSistema(String name)
	{
		Node nodeSistema = null;
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			nodeSistema = parser.selectSingleNode(this.coreXml, "/GVCore/GVSystems/Systems/System[@id-system='"+name+"']");
			return nodeSistema;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return nodeSistema;
	}

	public Map<String, Node> getListaChannel(List<String> listaServizi,String sistema)
	{
		Map<String, Node> listaChannel = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			for (String nomeServizio : listaServizi) {
				NodeList partecipants = parser.selectNodeList(this.coreXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
						+ "']/Operation/Participant[@id-system='"+sistema+"']");
				if (partecipants.getLength() > 0) {
					for (int i = 0; i < partecipants.getLength(); i++) {
						String name = parser.get(partecipants.item(i), "@id-channel");
						Node channel = parser.selectSingleNode(this.coreXml, "/GVCore/GVSystems/Systems/System/Channel[@id-channel='"+name+"']");
						logger.debug("Found[" + this.type + "] Partecipant " + name + " for Service " + nomeServizio);
						listaChannel.put(name, channel);
					}
				}
			}
			return listaChannel;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaChannel;
	}

	public Node getChannel(String canale,String sistema)
	{

		Node nodeChannel = null;
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			nodeChannel = parser.selectSingleNode(this.coreXml, "/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']");
			return nodeChannel;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return nodeChannel;
	}

	public Map<String, Node> getListaChannel(String sistema)
	{
		List<String> listaServizi = getListaServizi();
		return getListaChannel(listaServizi,sistema);
	}

	public Map<String, Node> getListaVCLOp(String sistema,String canale)
	{
		List<String> listaServizi = getListaServizi();
		return getListaVCLOp(listaServizi,sistema,canale);
	}

	public Map<String, Node> getListaVCLOp(List<String> listaServizi,String sistema,String canale)
	{
		Map<String, Node> listaOperation = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			for (String nomeServizio : listaServizi) {
				String xPath = "/GVCore/GVServices/Services/Service[@id-service='"+nomeServizio+"']/Operation/Flow/*[@id-system='"+sistema+"']";
				NodeList operations = parser.selectNodeList(this.coreXml, xPath);
				if (operations.getLength() > 0) {
					for (int i = 0; i < operations.getLength(); i++) {
						String name = parser.get(operations.item(i), "@operation-name");
						Node operation = parser.selectSingleNode(this.coreXml, "/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/*[@name='"+name+"']");
						if(operation != null) {
							logger.debug("Found[" + this.type + "] VCLOperation " + name + " for Service " + nomeServizio + " Operations");
							listaOperation.put(name, operation);
						}
					}
				}
				xPath = "/GVCore/GVServices/Services/Service[@id-service='"+nomeServizio+"']/Operation/SubFlow/*[@id-system='"+sistema+"']";
				operations = parser.selectNodeList(this.coreXml, xPath);
				if (operations.getLength() > 0) {
					for (int i = 0; i < operations.getLength(); i++) {
						String name = parser.get(operations.item(i), "@operation-name");
						Node operation = parser.selectSingleNode(this.coreXml, "/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/*[@name='"+name+"']");
						if(operation != null) {
							logger.debug("Found[" + this.type + "] VCLOperation " + name + " for Service " + nomeServizio + " SubFlows");
							listaOperation.put(name, operation);
						}
					}
				}
			}
			return listaOperation;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaOperation;
	}


	public Map<String, Node> getListaVCLOp(Node opNode)
	{
		Map<String, Node> listaOperation = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			String service = parser.get(opNode.getParentNode(), "@id-service");
			String operation = parser.get(opNode, "@name");
			if ("Forward".equals(operation)) {
				operation = parser.get(opNode, "@forward-name");
			}

			NodeList partecipants = parser.selectNodeList(opNode, "Partecipant");
			for (int i = 0; i < partecipants.getLength(); i++) {
				Node partecipant = partecipants.item(i);
				String system = parser.get(partecipant, "@id-system");
				String channel = parser.get(partecipant, "@id-channel");

				String xPath = ".//*[@id-system='" + system + "']";
				NodeList operNodes = parser.selectNodeList(opNode, xPath);
				if (operNodes.getLength() > 0) {
					for (int o = 0; o < operNodes.getLength(); o++) {
						String name = parser.get(operNodes.item(i), "@operation-name");
						Node vclNode = parser.selectSingleNode(this.coreXml, "/GVCore/GVSystems/Systems/System[@id-system='" + system + "']/Channel[@id-channel='" + channel + "']/*[@name='" + name + "']");
						if(vclNode != null) {
							logger.debug("Found[" + this.type + "] VCLOperation " + name + " for Service " + service + "/" + operation);
							listaOperation.put(name, vclNode);
						}
					}
				}
			}
			return listaOperation;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaOperation;
	}

	public Node getVCLOp(String vclOp, String sistema, String canale)
	{
		Node nodeOperation = null;
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			nodeOperation = parser.selectSingleNode(this.coreXml, "/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/*[@name='"+vclOp+"']");
			return nodeOperation;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return nodeOperation;
	}


	public Map<String, Node> getListaDataSource()
	{
		Map<String, Node> listaTrasf = getListaTrasformazioni();
		return getListaDataSource(listaTrasf);
	}

	public Map<String, Node> getListaDataSource(List<String> listaServizi)
	{
		Map<String, Node> listaTrasf = getListaTrasformazioni(listaServizi);
		return getListaDataSource(listaTrasf);
	}

	public Map<String, Node> getListaDataSource(Map<String, Node> listaTrasf)
	{
		XMLUtils parser = null;
		Map<String, Node> listaDataSource = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			for (String nomeTrasformazione : listaTrasf.keySet()) {
				Node trasf = listaTrasf.get(nomeTrasformazione);
				String dataSourceName = "Default";
				if(trasf !=null ) {
					dataSourceName = parser.get(trasf, "@DataSourceSet", "Default");
				}
				if (listaDataSource.containsKey(dataSourceName)) {
					continue;
				}
				String xPathDataSpurce = "/GVCore/GVDataTransformation/DataSourceSets/DataSourceSet[@name='"
						+ dataSourceName+"']";
				Node dataSource = parser.selectSingleNode(this.coreXml, xPathDataSpurce);
				logger.debug("Found[" + this.type + "] DataSourceSet " + dataSourceName);
				listaDataSource.put(dataSourceName, dataSource);
			}
			return listaDataSource;
		} catch (XMLUtilsException e) {
			logger.error(e);
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaDataSource;
	}

	protected Node getDataSourceFromTrasf(String nomeTrasf)
	{
		XMLUtils parser = null;
		Node nodeDataSource = null;
		try {
			parser = XMLUtils.getParserInstance();
			Node trasf = this.getTrasformazione(nomeTrasf);
			String dataSourceName = "Default";
			if(trasf !=null ) {
				dataSourceName = parser.get(trasf, "@DataSourceSet", "Default");
			}
			String xPathDataSpurce = "/GVCore/GVDataTransformation/DataSourceSets/DataSourceSet[@name='"
					+ dataSourceName+"']";
			nodeDataSource = parser.selectSingleNode(this.coreXml, xPathDataSpurce);
			return nodeDataSource;
		} catch (XMLUtilsException e) {
			logger.error(e);
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return nodeDataSource;
	}

	public String getDataSourceValueFromTrasf(String nomeTrasf)
	{
		XMLUtils parser = null;
		String dataSourceName = "Default";
		try {
			parser = XMLUtils.getParserInstance();
			Node trasf = this.getTrasformazione(nomeTrasf);

			if(trasf !=null ) {
				dataSourceName = parser.get(trasf, "@DataSourceSet", "Default");
			}
			return dataSourceName;
		} catch (XMLUtilsException e) {
			logger.error(e);
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return dataSourceName;
	}

	public Node getDataSource(String dataSourceName)
	{
		XMLUtils parser = null;
		Node nodeDataSource = null;
		try {
			parser = XMLUtils.getParserInstance();
			String xPathDataSpurce = "/GVCore/GVDataTransformation/DataSourceSets/DataSourceSet[@name='"
					+ dataSourceName+"']";
			nodeDataSource = parser.selectSingleNode(this.coreXml, xPathDataSpurce);
			return nodeDataSource;
		} catch (XMLUtilsException e) {
			logger.error(e);
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return nodeDataSource;
	}

	private List<String> getListaTrasformazioniDH(List<String> listaServizi) {
		Map<String, Node> mapListaSistemi = getListaSistemi(listaServizi);
		List<String> listaTrasformazioniDH = new ArrayList<String>();
		for (String sistema : mapListaSistemi.keySet()){
			Map<String, Node> mapListaChannel = getListaChannel(listaServizi, sistema);
			for(String canale : mapListaChannel.keySet()){
				Map<String, Node> mapListaOperation = getListaVCLOp(listaServizi, sistema, canale);
				for(String operation : mapListaOperation.keySet()){
					Node oper = mapListaOperation.get(operation);
					if(oper.getLocalName().equals("dh-call")){
						String operazione = oper.getAttributes().getNamedItem("name").getNodeValue();
						Map<String, Node> mapListaServiziDhCall = getListaDboBuilder(listaServizi, sistema, canale, operazione);
						if(mapListaServiziDhCall.size()>0){
							for(String builder : mapListaServiziDhCall.keySet()){
								Node nodeDH = mapListaServiziDhCall.get(builder);
								NodeList trasfsDH = nodeDH.getChildNodes();
								for(int i = 0; i < trasfsDH.getLength(); i++){
									Node dboBuilder = trasfsDH.item(i);
									String trasfNAme = null;
									try {
										trasfNAme = XMLUtils.get_S(dboBuilder, "@transformation");
									} catch (XMLUtilsException e) {
										logger.error(e);
									}
									if((trasfNAme != null) && !trasfNAme.equals("")){
										if(!listaTrasformazioniDH.contains(trasfNAme)) {
											listaTrasformazioniDH.add(trasfNAme);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return listaTrasformazioniDH;
	}

	private List<String> getListaTrasformazioniDH(Map<String, Node> serviceMap) {
		List<String> dhTrasfList = new ArrayList<String>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			for (String servizio : serviceMap.keySet()){
				Node servNode = serviceMap.get(servizio);

				NodeList operations = parser.selectNodeList(servNode, ".//Operation");
				for (int i = 0; i < operations.getLength(); i++){
					Node opNode = operations.item(i);

					Map<String, Node> mapVCLOp = getListaVCLOp(opNode);
					for (String vclOp : mapVCLOp.keySet()){
						Node vclNode = mapVCLOp.get(vclOp);
						if (vclNode.getLocalName().equals("dh-call")){
							Map<String, Node> dboBuilderMap = getListaDboBuilder(opNode, vclNode);
							if (dboBuilderMap.size()>0){
								for (String builder : dboBuilderMap.keySet()){
									Node nodeDH = dboBuilderMap.get(builder);
									NodeList trasfsDH = parser.selectNodeList(nodeDH, "*[@transformation]");
									for (int t = 0; t < trasfsDH.getLength(); t++){
										Node dbo = trasfsDH.item(i);
										String trasfNAme = parser.get(dbo, "@transformation", "");
										if (!"".equals(trasfNAme)){
											if (!dhTrasfList.contains(trasfNAme)) {
												dhTrasfList.add(trasfNAme);
											}
										}
									}
								}
							}
						}
					}
				}
			}
			return dhTrasfList;
		} catch (XMLUtilsException e) {
			logger.error(e);
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return dhTrasfList;
	}

	public List<String> getListaFileXsd()
	{
		List<String> listaServizi = getListaServizi();
		return getListaFileXsd(listaServizi);
	}

	protected List<String> getListaFileXsd(List<String> listaServizi)
	{
		List<String> listaFileXsd = new ArrayList<String>();
		Map<String, Node> listaTrasformazioni = getListaTrasformazioni(listaServizi);
		for (String trasformazione : listaTrasformazioni.keySet()) {
			Node trasf = listaTrasformazioni.get(trasformazione);
			if(trasf.getAttributes().getNamedItem("SchemaInput")!=null){
				String xsdFileName = trasf.getAttributes().getNamedItem("SchemaInput").getNodeValue();
				listaFileXsd.add(xsdFileName);
			}
			if(trasf.getAttributes().getNamedItem("SchemaOutput")!=null){
				String xsdFileName = trasf.getAttributes().getNamedItem("SchemaOutput").getNodeValue();
				listaFileXsd.add(xsdFileName);
			}

		}
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();

			for(String nomeServizio:listaServizi){
				String xPathInputServices = "/GVCore/GVServices/Services/Service[@id-service='"
						+ nomeServizio
						+ "']/Operation/*/*[@type='flow-node']/InputServices/xml-validation-service/xml-validation-call/@default-xsd";
				String xPathOutputServices = "/GVCore/GVServices/Services/Service[@id-service='"
						+ nomeServizio
						+ "']/Operation/*/*[@type='flow-node']/OutputServices/xml-validation-service/xml-validation-call/@default-xsd";
				NodeList inputServices = parser.selectNodeList(this.coreXml, xPathInputServices);
				NodeList outputServices = parser.selectNodeList(this.coreXml, xPathOutputServices);

				int i = 0;
				for (i = 0; i < inputServices.getLength(); i++) {
					String xsdFileName = inputServices.item(i).getNodeValue();
					listaFileXsd.add(xsdFileName);
				}
				for (int j = 0; j < outputServices.getLength(); j++) {
					String xsdFileName = outputServices.item(j).getNodeValue();
					listaFileXsd.add(xsdFileName);
				}
			}
			return listaFileXsd;
		} catch (XMLUtilsException e) {
			logger.error(e);
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaFileXsd;
	}

	public List<String> getListaFileJsd()
	{
		List<String> listaServizi = getListaServizi();
		return getListaFileJsd(listaServizi);
	}

	private List<String> getListaFileJsd(List<String> listaServizi)
	{
		List<String> listaFileJsd = new ArrayList<String>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();

			for(String nomeServizio:listaServizi){
				String xPathInputServices = "/GVCore/GVServices/Services/Service[@id-service='"
						+ nomeServizio
						+ "']/Operation/*/*[@type='flow-node']/InputServices/json-validation-service/json-validation-call/@jsd-name";
				String xPathOutputServices = "/GVCore/GVServices/Services/Service[@id-service='"
						+ nomeServizio
						+ "']/Operation/*/*[@type='flow-node']/OutputServices/json-validation-service/json-validation-call/@jsd-name";
				NodeList inputServices = parser.selectNodeList(this.coreXml, xPathInputServices);
				NodeList outputServices = parser.selectNodeList(this.coreXml, xPathOutputServices);

				int i = 0;
				for (i = 0; i < inputServices.getLength(); i++) {
					String xsdFileName = inputServices.item(i).getNodeValue();
					listaFileJsd.add(xsdFileName);
				}
				for (int j = 0; j < outputServices.getLength(); j++) {
					String xsdFileName = outputServices.item(j).getNodeValue();
					listaFileJsd.add(xsdFileName);
				}
			}
			return listaFileJsd;
		} catch (XMLUtilsException e) {
			logger.error(e);
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaFileJsd;
	}

	public Map<String,String> getListaFileXsl()
	{
		List<String> listaServizi = getListaServizi();
		return getListaFileXsl(listaServizi);
	}

	public Map<String,String> getListaFileXsl(List<String> listaServizi)
	{
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			Map<String,String> listaFileXsl = new HashMap<String,String>();
			Map<String, Node> listaTrasformazioni = getListaTrasformazioni(listaServizi);
			for (String trasformazione : listaTrasformazioni.keySet()) {
				Node trasf = listaTrasformazioni.get(trasformazione);
				String dtType = trasf.getLocalName();
				String name = parser.get(trasf, "@name");
				String dataSource = parser.get(trasf, "@DataSourceSet", "Default");

				if ("XSLTransformation".equals(dtType) ||
						"XSLFOPTransformation".equals(dtType) ||
						"HL72XMLTransformation".equals(dtType) ||
						"XML2HL7Transformation".equals(dtType) ||
						"JSON2XMLTransformation".equals(dtType) ||
						"XML2JSONTransformation".equals(dtType)) {
					String nomeFile = parser.get(trasf, "@XSLMapName", parser.get(trasf, "@InputXSLMapName",
							parser.get(trasf, "@OutputXSLMapName", "")));
					if ((nomeFile != null) && !nomeFile.equals("")) {
						listaFileXsl.put(name + "#" + dataSource, nomeFile);
					}
				}
			}
			return listaFileXsl;
		} catch (XMLUtilsException e) {
			logger.error(e);
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return null;
	}

	public Map<String,String> getListaFileBin()
	{
		List<String> listaServizi = getListaServizi();
		return getListaFileBin(listaServizi);
	}

	protected Map<String,String> getListaFileBin(List<String> listaServizi)
	{
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			Map<String,String> listaFileXsl = new HashMap<String,String>();
			Map<String, Node> listaTrasformazioni = getListaTrasformazioni(listaServizi);
			for (String trasformazione : listaTrasformazioni.keySet()) {
				Node trasf = listaTrasformazioni.get(trasformazione);
				String dtType = trasf.getLocalName();
				String name = parser.get(trasf, "@name");
				String dataSource = parser.get(trasf, "@DataSourceSet", "Default");

				if ("Bin2XMLTransformation".equals(dtType) ||
						"XML2BinTransformation".equals(dtType)) {
					String nomeFile = parser.get(trasf, "@ConversionMapName");
					listaFileXsl.put(name + "#" + dataSource, nomeFile);
				}
			}
			return listaFileXsl;
		} catch (XMLUtilsException e) {
			logger.error(e);
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return null;
	}

	public Map<String,String> getListaFileXq()
	{
		List<String> listaServizi = getListaServizi();
		return getListaFileXq(listaServizi);
	}

	protected Map<String,String> getListaFileXq(List<String> listaServizi)
	{
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			Map<String,String> listaFileXsl = new HashMap<String,String>();
			Map<String, Node> listaTrasformazioni = getListaTrasformazioni(listaServizi);
			for (String trasformazione : listaTrasformazioni.keySet()) {
				Node trasf = listaTrasformazioni.get(trasformazione);
				String dtType = trasf.getLocalName();
				String name = parser.get(trasf, "@name");
				String dataSource = parser.get(trasf, "@DataSourceSet", "Default");

				if ("XQTransformation".equals(dtType)) {
					String nomeFile = parser.get(trasf, "@XQMapName");
					listaFileXsl.put(nomeFile,dataSource);
				}else if ("Bin2XMLTransformation".equals(dtType) ||
						"XML2BinTransformation".equals(dtType)) {
					String nomeFile = parser.get(trasf, "@ConversionMapName");
					listaFileXsl.put(name + "#" + dataSource, nomeFile);
				}
			}
			return listaFileXsl;
		} catch (XMLUtilsException e) {
			logger.error(e);
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return null;
	}

	public List<String> getListaFileKeyStore()
	{
		List<String> listaServizi = getListaServizi();
		return getListaFileKeyStore(listaServizi);
	}

	protected List<String> getListaFileKeyStore(List<String> listaServizi)
	{
		List<String> listaFileKeyStore = new ArrayList<String>();
		Map<String, Node> listaTrasformazioni = getListaTrasformazioni(listaServizi);
		Map<String, Node> listaCrypoHelper = getListaCryptoHelper(listaTrasformazioni);
		for(String cryptoHelper:listaCrypoHelper.keySet()){
			Node nodeCryptoHelper = listaCrypoHelper.get(cryptoHelper);
			String ksn;
			try {
				ksn = XMLUtils.get_S(nodeCryptoHelper, "@key-store-name");
				listaFileKeyStore.add(ksn);
			} catch (XMLUtilsException e) {
				logger.error(e);
			}
		}
		Map<String, Node> listaPushNotif = getListPushNotification(listaServizi);
		for(String pne:listaPushNotif.keySet()){
			Node nodePNE = listaPushNotif.get(pne);
			String ksn;
			try {
				ksn = XMLUtils.get_S(nodePNE, "@keystoreID", "");
				if (!"".equals(ksn)) {
					listaFileKeyStore.add(XMLUtils.get_S(getGVKeyStoreCryptoHelper(ksn), "@key-store-name"));
				}
			} catch (XMLUtilsException e) {
				logger.error(e);
			}
		}
		return listaFileKeyStore;
	}

	protected Map<String, Node> getListaIDKeyStore(List<String> listaServizi)
	{
		Map<String, Node> listaIDKeyStore = new HashMap<String, Node>();
		Map<String, Node> listaTrasformazioni = getListaTrasformazioni(listaServizi);
		Map<String, Node> listaCrypoHelper = getListaCryptoHelper(listaTrasformazioni);
		for(String cryptoHelper:listaCrypoHelper.keySet()){
			Node nodeCryptoHelper = listaCrypoHelper.get(cryptoHelper);
			String type;
			String ksid;
			try {
				type = nodeCryptoHelper.getLocalName();
				if (type.equals("KeyStoreID")) {
					listaIDKeyStore.put(cryptoHelper, nodeCryptoHelper);
				}
				else {
					ksid = XMLUtils.get_S(nodeCryptoHelper, "@key-store-id");
					listaIDKeyStore.put(ksid, getGVKeyStoreCryptoHelper(ksid));
				}
			} catch (XMLUtilsException e) {
				logger.error(e);
			}
		}
		Map<String, Node> listaPushNotif = getListPushNotification(listaServizi);
		for(String pne:listaPushNotif.keySet()){
			Node nodePNE = listaPushNotif.get(pne);
			String ksid;
			try {
				ksid = XMLUtils.get_S(nodePNE, "@keystoreID", "");
				if (!"".equals(ksid)) {
					listaIDKeyStore.put(ksid, getGVKeyStoreCryptoHelper(ksid));
				}
			} catch (XMLUtilsException e) {
				logger.error(e);
			}
		}
		return listaIDKeyStore;
	}

	public Map<String, Node> getListaTrasformazioni(List<String> listaServizi)
	{
		return getListaTrasformazioni(getListaNodeServizi(listaServizi));
	}

	public Map<String, Node> getListaTrasformazioni()
	{
		Map<String, Node> serviceMap = getListaNodeServizi();
		return getListaTrasformazioni(serviceMap);
	}

	public Map<String, Node> getListaTrasformazioni(Map<String, Node> serviceMap)
	{
		List<String> dhTrasfList = getListaTrasformazioniDH(serviceMap);
		return getListaTrasformazioni(serviceMap, dhTrasfList);
	}

	/**
	 * @param xml
	 * @param nomeServizio
	 * @return
	 * @
	 */
	private Map<String, Node> _getListaTrasformazioni(List<String> listaServizi, List<String> listaTrasformazioniDH)
	{
		XMLUtils parser = null;
		Map<String, Node> listaTrasformazioni = new HashMap<String, Node>();
		try {
			for (String nomeServizio : listaServizi) {
				logger.debug("Begin[" + this.type + "] - Transformations for service["	+ nomeServizio + "]");
				String xPathInputServices = "/GVCore/GVServices/Services/Service[@id-service='"
						+ nomeServizio
						+ "']/Operation/*/*[@type='flow-node']/InputServices/gvdte-service/map-name-param/@value";
				String xPathOutputServices = "/GVCore/GVServices/Services/Service[@id-service='"
						+ nomeServizio
						+ "']/Operation/*/*[@type='flow-node']/OutputServices/gvdte-service/map-name-param/@value";

				parser = XMLUtils.getParserInstance();
				NodeList inputServices = parser.selectNodeList(this.coreXml,
						xPathInputServices);
				NodeList outputServices = parser.selectNodeList(this.coreXml,
						xPathOutputServices);

				int i = 0;
				for (i = 0; i < inputServices.getLength(); i++) {
					String name = inputServices.item(i).getNodeValue();
					Node trasf = parser.selectSingleNode(this.coreXml, "/GVCore/GVDataTransformation/Transformations/*[@name='"	+ name + "']");
					listaTrasformazioni.put(name, trasf);
					if(trasf.getLocalName().equals("SequenceTransformation")){
						getListaTrasfSequence(trasf, parser, listaTrasformazioni);
					}
				}
				for (int j = 0; j < outputServices.getLength(); j++) {
					String name = outputServices.item(j).getNodeValue();
					Node trasf = parser.selectSingleNode(this.coreXml, "/GVCore/GVDataTransformation/Transformations/*[@name='"	+ name + "']");
					listaTrasformazioni.put(name, trasf);
					if((trasf!=null) && trasf.getLocalName().equals("SequenceTransformation")){
						getListaTrasfSequence(trasf, parser, listaTrasformazioni);
					}
				}
				for (String nameTrafDH:listaTrasformazioniDH) {
					Node trasf = parser.selectSingleNode(this.coreXml,
							"/GVCore/GVDataTransformation/Transformations/*[@name='" + nameTrafDH + "']");
					listaTrasformazioni.put(nameTrafDH, trasf);
					if((trasf!=null) && trasf.getLocalName().equals("SequenceTransformation")){
						getListaTrasfSequence(trasf, parser, listaTrasformazioni);
					}
				}
				logger.debug("End[" + this.type + "] - Transformations for service["	+ nomeServizio + "]");
			}
			return listaTrasformazioni;
		} catch (XMLUtilsException e) {
			logger.error(e);
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaTrasformazioni;

	}

	private Map<String, Node> getListaTrasformazioni(Map<String, Node> serviceMap, List<String> dhTrasfList)
	{
		XMLUtils parser = null;
		Map<String, Node> trasfList = new HashMap<String, Node>();
		try {
			for (String service : serviceMap.keySet()) {
				Node serviceNode = serviceMap.get(service);
				logger.debug("Begin[" + this.type + "] - Transformations for service["	+ service + "]");

				String xPathDTEServices = ".//*[@type='flow-node']/*/gvdte-service/map-name-param/@value";
				parser = XMLUtils.getParserInstance();
				NodeList dteServices = parser.selectNodeList(serviceNode, xPathDTEServices);

				int i = 0;
				for (i = 0; i < dteServices.getLength(); i++) {
					String name = dteServices.item(i).getNodeValue();
					Node trasf = parser.selectSingleNode(this.coreXml, "/GVCore/GVDataTransformation/Transformations/*[@name='"	+ name + "']");
					trasfList.put(name, trasf);
					if(trasf.getLocalName().equals("SequenceTransformation")){
						getListaTrasfSequence(trasf, parser, trasfList);
					}
				}
				for (String nameTrafDH : dhTrasfList) {
					Node trasf = parser.selectSingleNode(this.coreXml,	"/GVCore/GVDataTransformation/Transformations/*[@name='" + nameTrafDH + "']");
					trasfList.put(nameTrafDH, trasf);
					if(trasf.getLocalName().equals("SequenceTransformation")){
						getListaTrasfSequence(trasf, parser, trasfList);
					}
				}
				logger.debug("End[" + this.type + "] - Transformations for service[" + service + "]");
			}
			return trasfList;
		} catch (XMLUtilsException e) {
			logger.error(e);
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return trasfList;
	}

	public List<String> getListaServiziGVCall(String servizio)
	{
			String xPathInputServices = "/GVCore/GVServices/Services/Service[Operation/*/GVCoreCallNode/@id-service='"+servizio+"']";
			return getListaServiziFiltered(xPathInputServices);
	}

	public List<String> getListaServiziTrasf(String trasf)
	{
			String xPathInputServices = "/GVCore/GVServices/Services/Service[Operation/*/*/*/gvdte-service/map-name-param/@value='"+trasf+"']";
			return getListaServiziFiltered(xPathInputServices);
	}

	public List<String> getListaServiziVclOP(String system,String channel,String operation)
	{
		String xPathInputServices = "/GVCore/GVServices/Services/Service[Operation/Participant/@id-system='"+system+"' and Operation/Participant/@id-channel='"+channel+"' and Operation/*/*/@id-system='"+system+"' and Operation/*/*/@operation-name='"+operation+"']";
		return getListaServiziFiltered(xPathInputServices);
	}

	public List<String> getListaServiziDboBuilder(String dboBuilder)
	{
		String xPathInputServices = "/GVCore/GVServices/Services/Service[Operation/*/*/InputServices/dh-selector-service/dh-selector-call/@DH_SERVICE_NAME='"+dboBuilder+"']";
	    return getListaServiziFiltered(xPathInputServices);
	}

	private List<String> getListaServiziFiltered(String xPath)
	{
		XMLUtils parser = null;
		List<String> retListaServizi = new ArrayList<String>();
		try {
			parser = XMLUtils.getParserInstance();
			NodeList results = parser.selectNodeList(this.coreXml, xPath);
			for (int i = 0; i < results.getLength(); i++) {
				String serviceName = parser.get(results.item(i), "@id-service");
				retListaServizi.add(serviceName);
				logger.debug("Id[" + this.type + "] Service " + serviceName);
			}
			return retListaServizi;
		} catch (XMLUtilsException e) {
			logger.error(e);
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return retListaServizi;

	}

	public Node getTrasformazione(String name)
	{
		XMLUtils parser = null;
		Node nodeTrasf = null;
		try {
			parser = XMLUtils.getParserInstance();
			nodeTrasf = parser.selectSingleNode(this.coreXml,"/GVCore/GVDataTransformation/Transformations/*[@name='"+ name + "']");

			return nodeTrasf;
		} catch (XMLUtilsException e) {
			logger.error(e);
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return nodeTrasf;

	}

	private void getListaTrasfSequence(Node trasf, XMLUtils parser, Map<String, Node> listaTrasformazioni)
	{
		NodeList sequenceTraf;
		try {
			sequenceTraf = parser.selectNodeList(trasf, "./SequenceElement");

			for (int z = 0; z < sequenceTraf.getLength(); z++) {
				String name = parser.get(sequenceTraf.item(z), "@Transformer");
				trasf = parser.selectSingleNode(this.coreXml,
						"/GVCore/GVDataTransformation/Transformations/*[@name='" + name + "']");
				listaTrasformazioni.put(name, trasf);
				if(trasf.getLocalName().equals("SequenceTransformation")){
					getListaTrasfSequence(trasf, parser, listaTrasformazioni);
				}
			}
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
	}

	/**
	 * @param xml
	 * @param nomeServizio
	 * @return
	 * @
	 */
	private String[] getListaKeyIdTrasformazioni(Map <String,Node>listaTrasformazioni)
	{
		String[] listaKeyIdTrasformazioni = null;
		int i=0;
		String strXpath = "";
		for(String trasfName:listaTrasformazioni.keySet()){
			if (i == 0) {
				strXpath = strXpath + "/GVCore/GVDataTransformation/Transformations/CryptoTransformation[@name='";
			}
			strXpath = strXpath + trasfName + "'";
			if (i == (listaTrasformazioni.keySet().size()-1)) {
				strXpath = strXpath + "]";
			}
			else {
				strXpath = strXpath + " or @name='";
			}
			i++;
		}
		strXpath = strXpath + "/@KeyID";
		logger.debug("strXpath=" + strXpath);

		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			NodeList kid = parser.selectNodeList(this.coreXml, strXpath);
			if (kid.getLength() > 0) {
				listaKeyIdTrasformazioni = new String[kid.getLength()];
				for (int j = 0; j < kid.getLength(); j++) {
					listaKeyIdTrasformazioni[i] = parser.get(kid.item(j), ".");
					logger.debug("value=" + listaKeyIdTrasformazioni[j]);
				}
			}
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaKeyIdTrasformazioni;
	}

	/**
	 * @param xml
	 * @param nomeServizio
	 * @return
	 * @
	 */
	public  Map<String, Node> getListaForward(String nomeServizio)
	{
		String xpath = "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
				+ "']/Operation[@name='Forward']/@forward-name";
		return getListaObject(this.coreXml, "forward-name" , xpath);
	}

	/**
	 * @param xml
	 * @return
	 * @
	 * @throws IOException
	 */
	private Map<String, Node> getListaObject(Document xml, String keyAttribute, String xpath)
	{
		XMLUtils parser = null;
		Map<String, Node> listaObject = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			NodeList results = parser.selectNodeList(xml, xpath);
			for (int i = 0; i < results.getLength(); i++) {
				if((results.item(i).getAttributes()!=null) && (results.item(i).getAttributes().getNamedItem(keyAttribute)!=null)){
					String name = results.item(i).getAttributes().getNamedItem(keyAttribute).getNodeValue();
					listaObject.put(name,results.item(i));
				}
			}
			return listaObject;
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaObject;
	}
	public Node getNodeCore(String xPath)  {
		Node node = null;
		try {
			node = XMLUtils.selectSingleNode_S(this.coreXml, xPath);
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		return node;
	}

	public Node getNodeAdapter(String xPath)  {
		Node node = null;
		try {
			node = XMLUtils.selectSingleNode_S(this.adapterXml, xPath);
		} catch (XMLUtilsException e) {
			logger.error(e);
		}
		return node;
	}

	private Document parseXml(String xmlFilePath) throws Exception
	{
		FileInputStream poFile = null;
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			poFile = new FileInputStream(xmlFilePath);
			Document doc = parser.parseDOM(poFile, false, true);
			return doc;
		}
		finally {
			if (poFile != null) {
				poFile.close();
			}
			XMLUtils.releaseParserInstance(parser);
		}
	}

	private Document parseXml(URL url) throws Exception
	{
		InputStream poFile = null;
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			poFile = url.openStream();
			Document doc = parser.parseDOM(poFile, false, true);
			return doc;
		}
		finally {
			if (poFile != null) {
				poFile.close();
			}
			XMLUtils.releaseParserInstance(parser);
		}
	}

	public String getXmlFile(String fileName) throws Exception
	{
		Document localXmlFile = parseXml(fileName);
		DOMWriter writer = new DOMWriter();
		OutputStream out = new ByteArrayOutputStream();
		String ret = "";
		try {
			writer.write(localXmlFile, out);
			ret = out.toString();
			out.close();
		} catch (IOException e) {
			logger.error(e);
		}

		return ret;
	}

	public static void main(String[] args)
	{
		try {
			GVConfig gvConfigServer = new GVConfig("server", "/home/gianluca/applicazioni/GvServer-3.4.0.12.Final/GreenV/xmlconfig/GVCore.xml","/home/gianluca/applicazioni/GvServer-3.4.0.12.Final/GreenV/xmlconfig/GVAdapters.xml");
			//System.out.println(gvConfigServer.getGvCore(true));
			//System.out.println(gvConfigServer.getGvAdapters(true));
			List <String> listaServizi = new ArrayList<String>();
			//listaServizi.add("ProcessSingleSVCEmail");
			//listaServizi.add("TestRSH");
			//listaServizi.add("TestHL7_Listener");listaServizi.add("TestHL7_ListenerPippo");
			listaServizi.add("TWEET");
			//listaServizi.add("PushNotification");


			System.out.println(gvConfigServer.getGvCore(listaServizi, true));
			System.out.println(gvConfigServer.getGvAdapters(listaServizi, true));

			/*List<String> listaServizi = gvConfigServer.getListaServiziTrasf("TAD_TripTrendsResponseToJson");
			System.out.println(listaServizi);
			listaServizi = gvConfigServer.getListaServiziGVCall("CheckAuth");
			System.out.println(listaServizi);
			listaServizi = gvConfigServer.getListaServiziDboBuilder("readServicePlansDetail");
			System.out.println(listaServizi);
			Map<String,Node> listaNodeServizio = gvConfigServer.getListaNodeServizi();
			for(String servizio:listaNodeServizio.keySet()){
				Node nodeServizio = listaNodeServizio.get(servizio);
				NodeList operations = XMLUtils.selectNodeList_S(nodeServizio,"Operation/*-/-*[@op-type='call']");
				for(int i=0;i<operations.getLength();i++){
					String system = XMLUtils.get_S(operations.item(i), "@id-system");
					String operation = XMLUtils.get_S(operations.item(i), "@operation-name");
					String channel = XMLUtils.selectSingleNode_S(nodeServizio,"Operation/Participant[@id-system='"+system+"']/@id-channel").getNodeValue();
					listaServizi = gvConfigServer.getListaServiziVclOP(system, channel,operation);
					System.out.println(listaServizi);
				}
			}*/
		}
		catch (Exception e) {
			logger.error(e);
		}
	}


}

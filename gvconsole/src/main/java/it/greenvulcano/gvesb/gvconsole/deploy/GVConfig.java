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

import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import max.xml.DOMWriter;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	private static Logger  logger    = Logger.getLogger(GVConfig.class);
	public Document getCoreXml(){
		return coreXml;
	}
	public Document getAdapterXml(){
		return adapterXml;
	}
	public GVConfig(Document coreXml,Document adapterXml)
	{
			this.coreXml =coreXml;
			this.adapterXml = adapterXml;
	}
	public GVConfig(URL urlCore,URL urlAdapter)
	{
		try {
			coreXml = parseXml(urlCore);
			adapterXml = parseXml(urlAdapter);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
	public GVConfig(String fileCore,String fileAdapter)
	{

		try {
			adapterXml = parseXml(fileAdapter);
			coreXml = parseXml(fileCore);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
	public List<String> getListaServizi()
	{
		List<String> retListaServizi = new ArrayList<String>();
		for(String servizio:getListaNodeServizi().keySet()){
			retListaServizi.add(servizio);
		}
		Collections.sort(retListaServizi);
		return retListaServizi;
	}
	public Map<String, Node> getListaNodeServizi()
	{
		logger.debug("init getListaServizi");
		Map<String, Node> retListaServizi = new HashMap<String, Node>();;
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			NodeList results = parser.selectNodeList(coreXml, "/GVCore/GVServices/Services/Service");
			for (int i = 0; i < results.getLength(); i++) {
				String serviceName = parser.get(results.item(i), "@id-service");
				retListaServizi.put(serviceName, results.item(i));
				logger.debug("Id Servizio Nuovo=" + serviceName);
			}
			return retListaServizi;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return retListaServizi;
	}
	public Map<String, Node> getListaNodeServizi(List<String> listaServizi)
	{
		logger.debug("init getListaServizi");
		Map<String, Node> retListaServizi = new HashMap<String, Node>();;
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			for(String servizio:listaServizi) {
				Node result = parser.selectSingleNode(coreXml, "/GVCore/GVServices/Services/Service[@id-service='"+servizio+"']");
				if(result!=null)
					retListaServizi.put(servizio, result);
			}
			return retListaServizi;
		} catch (XMLUtilsException e) {
			e.printStackTrace();
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
			dirDTE = XMLUtils.get_S(coreXml, xpath);
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		dirDTE = dirDTE.replace("${{gv.app.home}}", "");
		return dirDTE;
	}
	public String geWsdlDir()
	{
		String wsdlDir = null;
		try {
			wsdlDir = XMLUtils.get_S(adapterXml, "/GVAdapters/GVWebServices/BusinessWebServices/@wsdl-directory").replace("${{gv.app.home}}", "");
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		return wsdlDir;
	}
	public String getWsServiceDir()
	{
		String wsdlDir = null;
		try {
			wsdlDir = XMLUtils.get_S(adapterXml, "/GVAdapters/GVWebServices/BusinessWebServices/@services-directory").replace("${{gv.app.home}}", "");
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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

			e.printStackTrace();
		}
		String localXml = out.toString();
		return localXml;
	}
	private Document getDocGvAdapters(List<String> listaServizi,Boolean forExport) 
	{
		logger.debug("init getDocGvAdapters");
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
			if(mapListaGvWs.size()+mapListaBsWs.size()>0 || forExport){
				Node locGVWebServices = localXmlGVAdapter.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapter, "GVWebServices"));
				parser.setAttribute((Element) locGVWebServices, "name", "WEB_SERVICES");
				parser.setAttribute((Element) locGVWebServices, "type", "Module");
				parser.setAttribute((Element) locGVWebServices, "version", "1.0");
				if(mapListaGvWs.size()>0){
					Node locGreenVulcanoWebServices = locGVWebServices.appendChild(parser.createElement(localXmlGVAdapter, "GreenVulcanoWebServices"));
					for (String nome:mapListaGvWs.keySet()) {
						Node ws = mapListaGvWs.get(nome);
						locGreenVulcanoWebServices.appendChild(localXmlGVAdapter.importNode(ws, true));
					}
				}
				if(mapListaBsWs.size()>0){
				  Node locBusinessWebServices = locGVWebServices.appendChild(parser.createElement(localXmlGVAdapter, "BusinessWebServices"));
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
			if(mapListaDataProviders.size()>0 || forExport){
				Node locGVDataProviders = localXmlGVAdapter.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapter, "GVDataProviderManager"));
				parser.setAttribute((Element) locGVDataProviders, "name", "GVDP");
				parser.setAttribute((Element) locGVDataProviders, "type", "Module");

				Node locDataProviders = locGVDataProviders.appendChild(parser.createElement(localXmlGVAdapter, "DataProviders"));
				for (String nomeDp:mapListaDataProviders.keySet()) {
					Node dp = mapListaDataProviders.get(nomeDp);
					locDataProviders.appendChild(localXmlGVAdapter.importNode(dp, true));
				}
			}
			Map<String,Node> listActionMapping = getListActionMapping(listaServizi);
			if(listActionMapping.size()>0 || forExport){
				Node base = localXmlGVAdapter.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapter, "GVAdapterHttpConfiguration"));
				Node mappings = base.appendChild(parser.createElement(localXmlGVAdapter, "InboundConfiguration"));
				mappings = mappings.appendChild(parser.createElement(localXmlGVAdapter, "ActionMappings"));
				parser.setAttribute((Element) base, "version", "1.0");
				parser.setAttribute((Element) base, "type", "module");
				parser.setAttribute((Element) base, "name", "HTTP_ADAPTER");

				for(String action:listActionMapping.keySet()){
					Node restMappings = mappings.appendChild(parser.createElement(localXmlGVAdapter, "RESTActionMapping"));
					Node nodeAtion = listActionMapping.get(action);
					parser.setAttribute((Element) restMappings, "Action", action);
					if(parser.get(nodeAtion, "@RespCharacterEncoding")!=null)
						parser.setAttribute((Element) restMappings, "RespCharacterEncoding", parser.get(nodeAtion, "@RespCharacterEncoding"));
					if(parser.get(nodeAtion, "@RespContentType")!=null)
						parser.setAttribute((Element) restMappings, "RespContentType", parser.get(nodeAtion, "@RespContentType"));
					if(parser.get(nodeAtion, "@class")!=null)
						parser.setAttribute((Element) restMappings, "class", parser.get(nodeAtion, "@class"));
					if(parser.get(nodeAtion, "@dump-in-out")!=null)
						parser.setAttribute((Element) restMappings, "dump-in-out", parser.get(nodeAtion, "@dump-in-out"));
					if(parser.get(nodeAtion, "@enabled")!=null)
						parser.setAttribute((Element) restMappings, "enabled", parser.get(nodeAtion, "@enabled"));
					if(parser.get(nodeAtion, "@type")!=null)
						parser.setAttribute((Element) restMappings, "type", parser.get(nodeAtion, "@type"));
					Node opMappings = restMappings.appendChild(parser.createElement(localXmlGVAdapter, "OperationMappings"));
					Map<String,Node> listRestActionMapping = getListaRestActionMapping(listaServizi,action);
					for(String rest:listRestActionMapping.keySet()){
						opMappings.appendChild(localXmlGVAdapter.importNode(listRestActionMapping.get(rest), true));
					}
				}
			}
			return localXmlGVAdapter;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return null;
	}
	public Map<String, Node> getListaGvWebServices() 
	{
		List<String> listaServizi =  getListaServizi();
		return getListaGvWebServices(listaServizi);
		
	}
	private Map<String, Node> getListaGvWebServices(List<String> listaServizi) 
	{
		XMLUtils parser = null;
		Map<String, Node> listaGvHttp = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			for (String servizio : listaServizi) {
				Map<String, Node> listaOperazioni = getListaOperazioni(servizio);
				for (String operazione : listaOperazioni.keySet()) {
					String xPath = "/GVAdapters/GVWebServices/GreenVulcanoWebServices/GreenVulcanoWebService[@gv-service='"+servizio+"' and @gv-operation='"+operazione+"']";
					Node localXml = parser.selectSingleNode(adapterXml, xPath);
					if(localXml!=null)
						listaGvHttp.put(servizio+operazione, localXml);
				}  

			}
			return listaGvHttp;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaGvHttp;
	}	
	public Map<String, Node> getListaBusinessWebServices() 
	{
		List<String> listaServizi = getListaServizi();
		return getListaBusinessWebServices(listaServizi);
	}
	protected Map<String, Node> getListaBusinessWebServices(List<String> listaServizi) 
	{
		XMLUtils parser = null;
		Map<String, Node> listaGvHttp = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			for (String servizio : listaServizi) {
				Map<String, Node> listaOperazioni = getListaOperazioni(servizio);
				for (String operazione : listaOperazioni.keySet()) {
					if(operazione.equals("Forward")){
						operazione = parser.get(listaOperazioni.get(operazione), "@forward-name");
					}

					String xPath = "/GVAdapters/GVWebServices/BusinessWebServices/WebService[WSOperation/Binding/@gv-service='"+servizio+"' and WSOperation/Binding/@gv-operation='"+operazione+"']";
					Node localXml = parser.selectSingleNode(adapterXml, xPath);					
					if(localXml!=null){
						String name = parser.get(localXml, "@web-service");
						listaGvHttp.put(name, localXml);
					}

				}  

			}
			return listaGvHttp;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaGvHttp;
	}
	private Node getNodeBusinessWebServices() 
	{
		XMLUtils parser = null;
		Node nodeBusinessWebServices = null;
		try {
			parser = XMLUtils.getParserInstance();
			String xPath = "/GVAdapters/GVWebServices/BusinessWebServices";
			nodeBusinessWebServices = parser.selectSingleNode(adapterXml, xPath);					
			return nodeBusinessWebServices;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return nodeBusinessWebServices;
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
					if(operazione.equals("Forward")){
						operazione = parser.get(listaOperazioni.get(operazione), "@forward-name");
					}

					String xPath = "/GVCore/GVPolicy/ACLGreenVulcano/*[@service='"+servizio+"' and (@operazione='"+operazione+"' or not(@operation))]";
					Node localXml = parser.selectSingleNode(coreXml, xPath);					
					if(localXml!=null){
						String oper = parser.get(localXml, "@operazione","");
						String name = servizio+oper;
						listaAclGV.put(name, localXml);
					}

				}  

			}
			return listaAclGV;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
				if(addressesDef!=null){
					Node nodeAddressSet = parser.selectSingleNode(coreXml, "/GVCore/GVPolicy/Addresses/AddressSet[@name='"+addressesDef+"']");
					if(nodeAddressSet!=null)
						listaAddressSet.put(addressesDef, nodeAddressSet);
				}
			}
			return listaAddressSet;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
		    nodeAddressSet = parser.selectSingleNode(coreXml, "/GVCore/GVPolicy/Addresses/AddressSet[@name='"+addressesDef+"']");
			return nodeAddressSet;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
			parser = XMLUtils.getParserInstance();for(String key:listaAclGV.keySet()){
				Node nodeAclGV = listaAclGV.get(key);
				String roleDef = parser.get(nodeAclGV, "ACL/RoleRef/@name");
				Node nodeRoleDef = parser.selectSingleNode(coreXml, "/GVCore/GVPolicy/Roles/Role[@name='"+roleDef+"']");
				if(nodeRoleDef!=null)
					listaRoleDef.put(roleDef, nodeRoleDef);
			}
			return listaRoleDef;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
			nodeRoleDef = parser.selectSingleNode(coreXml, "/GVCore/GVPolicy/Roles/Role[@name='"+roleDef+"']");
			return nodeRoleDef;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
		Map<String, Node> listaGvHttp = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			for (String servizio : listaServizi) {
				Map<String, Node> listaOperazioni = getListaOperazioni(servizio);
				for (String operazione : listaOperazioni.keySet()) {
					if(operazione.equals("Forward"))
					{
						Node forward = listaOperazioni.get(operazione);
						operazione =parser.get(forward, "@forward-name");

					}
					String xPath = "/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/ActionMappings/RESTActionMapping[@Action='"+action+"']/OperationMappings/Mapping[@service='"+servizio+"' and @operation='"+operazione+"']";
					Node localXml = parser.selectSingleNode(adapterXml, xPath);
					if(localXml!=null){
						if(parser.get(localXml, "@pattern")!=null){
							String name = parser.get(localXml, "@pattern");
							listaGvHttp.put(name, localXml);
						}

					}

				}  

			}
			return listaGvHttp;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaGvHttp;
	}
	public Map<String, Node> getListActionMapping() 
	{
		List<String> listaServizi = getListaServizi();
		return getListActionMapping(listaServizi);

	}
	private Map<String, Node> getListActionMapping(List<String> listaServizi) 
	{
		XMLUtils parser = null;
		Map<String, Node> listaGvHttp = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			for (String servizio : listaServizi) {
				Map<String, Node> listaOperazioni = getListaOperazioni(servizio);
				for (String operazione : listaOperazioni.keySet()) {
					if(operazione.equals("Forward"))
					{
						Node forward = listaOperazioni.get(operazione);
						operazione =parser.get(forward, "@forward-name");

					}
					String xPath = "/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/ActionMappings/RESTActionMapping[OperationMappings/Mapping/@service='"+servizio+"' and OperationMappings/Mapping/@operation='"+operazione+"']";
					Node localXml = parser.selectSingleNode(adapterXml, xPath);
					if(localXml!=null){
						if(parser.get(localXml, "@Action")!=null){
							String name = parser.get(localXml, "@Action");
							listaGvHttp.put(name, localXml);
						}

					}

				}  

			}
			return listaGvHttp;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaGvHttp;
	}
	private Map<String, Node> getListaOperazioni(String servizio) 
	{
		XMLUtils parser = null;
		Map<String, Node> listaOperazioni = new HashMap<String, Node>();
		try {
			parser = XMLUtils.getParserInstance();
			String xpath= "/GVCore/GVServices/Services/Service[@id-service='"+servizio+"']/Operation";
			NodeList operations = parser.selectNodeList(coreXml, xpath);		
			for(int i=0;i<operations.getLength();i++){
				String name = parser.get(operations.item(i), "@name");
				listaOperazioni.put(name, operations.item(i));
			}
			return listaOperazioni;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
				gvDataDump = XMLUtils.selectSingleNode_S(coreXml, "/GVCore/GVBufferDump/ServiceDump[@id-service='" + nomeServizio + "']");
			} catch (XMLUtilsException e) {

				e.printStackTrace();
			}
			if(gvDataDump!=null)
				mapGvDataDump.put(nomeServizio, gvDataDump);
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

			e.printStackTrace();
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

			e.printStackTrace();
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
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
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
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}


	}


	private Document getDocGvCore(List<String> listaServizi,Boolean forExport)
	{
		logger.debug("init getGvCore");
		XMLUtils parser = null;
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
			Map<String, Node> mapListaTrasformazioni = getListaTrasformazioni(listaServizi);
			if(mapListaTrasformazioni.size()>0){
				Map<String, Node> mapLCryptoHelper = getListaCryptoHelper(mapListaTrasformazioni);
				if(mapLCryptoHelper.size()>0 || forExport){
				  Node locCripoHelper = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVCryptoHelper"));
				  parser.setAttribute((Element) locCripoHelper, "type", "module");
				  parser.setAttribute((Element) locCripoHelper, "name", "CRYPTO_HELPER");

				  for (String id:mapLCryptoHelper.keySet()) {
					locCripoHelper.appendChild(localXmlGVCore.importNode(this.getGVKeyStoreCryptoHelper(id), true));
					locCripoHelper.appendChild(localXmlGVCore.importNode(mapLCryptoHelper.get(id), true));
				  }
				}
			}
			Map<String, Node> mapListaForward = getListaForward(listaServizi);
			if(mapListaForward.size()>0 || forExport){
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
			if(mapListaGruppi.size()>0 || forExport){
				Node locGroups = locServices.appendChild(parser.createElement(localXmlGVCore, "Groups"));

				for (String nomeGruppo:mapListaGruppi.keySet()) {
					locGroups.appendChild(localXmlGVCore.importNode(mapListaGruppi.get(nomeGruppo), true));
				}
			}

			Node locService = locServices.appendChild(parser.createElement(localXmlGVCore, "Services"));
			for (String servizio : listaServizi) {
				Node nodeService = getNodeService(servizio);
				if(nodeService!=null && locService!=null)
					locService.appendChild(localXmlGVCore.importNode(nodeService, true));
			}
			Map<String, Node> mapListaSistemi = getListaSistemi(listaServizi);
			if(mapListaSistemi.size()>0 || forExport){
				Node locSystems = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVSystems"));
				parser.setAttribute((Element) locSystems, "type", "module");
				parser.setAttribute((Element) locSystems, "name", "SYSTEMS");
				locSystems = locSystems.appendChild(parser.createElement(localXmlGVCore, "Systems"));

				for (String sistema:mapListaSistemi.keySet()){
					Node locSystem = locSystems.appendChild(parser.createElement(localXmlGVCore, "System"));
					parser.setAttribute((Element) locSystem, "id-system", sistema);
					parser.setAttribute((Element) locSystem, "system-activation", "on");
					Map<String, Node> mapListaChannel = getListaChannel(listaServizi,sistema);
					for(String canale:mapListaChannel.keySet()){
						Map<String, Node> mapListaOperation = getListaVCLOp(listaServizi,sistema,canale);
						Node channel = locSystem.appendChild(parser.createElement(localXmlGVCore, "Channel"));
						parser.setAttribute((Element) channel, "id-channel", canale);
						for(String operation:mapListaOperation.keySet()){
							Node oper = mapListaOperation.get(operation);
							if(oper.getLocalName().equals("dh-call")){
								String operazione = parser.get(oper,"@name");
								Map<String, Node> mapListaServiziDhCall = getListaDboBuilder(listaServizi,sistema,canale,operazione);
								if(mapListaServiziDhCall.size()>0){
									Node dhCall = channel.appendChild(parser.createElement(localXmlGVCore, "dh-call"));
									parser.setAttribute((Element) dhCall, "class", "it.greenvulcano.gvesb.virtual.datahandler.DataHandlerCallOperation");
									parser.setAttribute((Element) dhCall, "name", operazione);
									parser.setAttribute((Element) dhCall, "type", "call");
									for(String builder:mapListaServiziDhCall.keySet()){
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

			if(mapGVBufferDump.size()>0 || forExport){
				Node locGVBufferDump = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVBufferDump"));
				parser.setAttribute((Element) locGVBufferDump, "type", "module");
				parser.setAttribute((Element) locGVBufferDump, "name", "BUFFER_DUMP");
				parser.setAttribute((Element) locGVBufferDump, "log-dump-size", "-1");
				for (String servizio:mapGVBufferDump.keySet()) {
					locGVBufferDump.appendChild(localXmlGVCore.importNode(mapGVBufferDump.get(servizio), true));
				}
			}
			Map<String, Node> mapAddressSet = getListaAddressSet(listaServizi);
			Map<String, Node> mapRoleDef = getListaRoleRef(listaServizi);
			Map<String, Node> mapACLGreenVulcano = getListaAclGV(listaServizi);
			if(mapAddressSet.size()+mapAddressSet.size()+mapACLGreenVulcano.size()>0 || forExport){
				Node locGVPolicy = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVPolicy"));
				parser.setAttribute((Element) locGVPolicy, "type", "module");
				parser.setAttribute((Element) locGVPolicy, "name", "POLICY_MANAGER");

				if(mapRoleDef.size()>0){
					Node locGVRoles = locGVPolicy.appendChild(parser.createElement(localXmlGVCore, "Roles"));
					for (String servizio:mapRoleDef.keySet()) {
						locGVRoles.appendChild(localXmlGVCore.importNode(mapRoleDef.get(servizio), true));
					}
				}

				if(mapAddressSet.size()>0){
					Node locGVAddress = locGVPolicy.appendChild(parser.createElement(localXmlGVCore, "Addresses"));
					for (String servizio:mapAddressSet.keySet()) {
						locGVAddress.appendChild(localXmlGVCore.importNode(mapAddressSet.get(servizio), true));
					}
				}

				if(mapACLGreenVulcano.size()>0){
					Node nodeACLGreenVulcano = locGVPolicy.appendChild(parser.createElement(localXmlGVCore, "ACLGreenVulcano"));
					parser.setAttribute((Element) nodeACLGreenVulcano, "class", "it.greenvulcano.gvesb.policy.impl.ACLGreenVulcano");
					parser.setAttribute((Element) nodeACLGreenVulcano, "type", "acl-manager");
					for (String servizio:mapACLGreenVulcano.keySet()) {
						nodeACLGreenVulcano.appendChild(localXmlGVCore.importNode(mapACLGreenVulcano.get(servizio), true));
					}
				}
			}
			if(mapListaTrasformazioni.size()>0 || forExport){
				Node localTransformation = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVDataTransformation"));
				parser.setAttribute((Element) localTransformation, "type", "module");
				parser.setAttribute((Element) localTransformation, "name", "GVDT");

				Map<String, Node> mapListaDataSource = getListaDataSource(mapListaTrasformazioni);		
				Node localNodeDataSource = localTransformation.appendChild(parser.createElement(localXmlGVCore, "DataSourceSets"));
				for (String nomeDataSource:mapListaDataSource.keySet()) {
					localNodeDataSource.appendChild(localXmlGVCore.importNode(mapListaDataSource.get(nomeDataSource), true));
				}
				Node localNodeTrasf = localTransformation.appendChild(parser.createElement(localXmlGVCore, "Transformations"));
				for (String nomeTrasformazione:mapListaTrasformazioni.keySet()) {
					localNodeTrasf.appendChild(localXmlGVCore.importNode(mapListaTrasformazioni.get(nomeTrasformazione), true));
				}
			}
			Map<String, Node> mapGVTask = getListaTask(listaServizi);
			if(mapGVTask.size()>0 || forExport){
				Node localTask = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVTaskManagerConfiguration"));
				parser.setAttribute((Element) localTask, "type", "module");
				parser.setAttribute((Element) localTask, "name", "GVTASKS");
				Node localNodeTask = localTask.appendChild(parser.createElement(localXmlGVCore, "TaskGroups"));
				for (String nomeTask:mapGVTask.keySet()) {
					localNodeTask.appendChild(localXmlGVCore.importNode(mapGVTask.get(nomeTask), true));
				}
			}
			return localXmlGVCore;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
					if(operazione.equals("Forward"))
					{
						Node forward = listaOperazioni.get(operazione);
						String  forwardName= parser.get(forward,"@forward-name");
						String xpath= "/GVCore/GVForwards/ForwardConfiguration[@forwardName='"+forwardName+"']";
						//System.out.println("xpath="+xpath);
						NodeList forwards = parser.selectNodeList(coreXml, xpath);
						if (forwards.getLength() > 0) {
							for (int i = 0; i < forwards.getLength(); i++) {
								String name = parser.get(forwards.item(i),"@name");
								listaForward.put(name, forwards.item(i));
							}
						}
					} 	
				}
			}
			return listaForward;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaForward;
	}
	public Map<String, Node> getListaDboBuilder(String sistema, String canale,String operazione) {
		List<String> listaServizi = getListaServizi();
		return getListaDboBuilder(listaServizi,sistema,canale,operazione);
	}
	public Map<String, Node> getListaDboBuilder(
			List<String> listaServizi, String sistema, String canale,String operazione){
		Map<String, Node> listaOperation = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			for (String nomeServizio : listaServizi) {
				String xpath= "/GVCore/GVServices/Services/Service[@id-service='"+nomeServizio+"']/Operation/Flow/GVOperationNode[@id-system='"+sistema+"' and @operation-name='"+operazione+"']";
				//System.out.println("xpath="+xpath);
				NodeList operations = parser.selectNodeList(coreXml, xpath);
				if (operations.getLength() > 0) {
					for (int i = 0; i < operations.getLength(); i++) {
						Node gvOperNode = operations.item(i);
						Node nodeOpDH = parser.selectSingleNode(gvOperNode, "InputServices/dh-selector-service/dh-selector-call/@DH_SERVICE_NAME");
						String nomeOpDH =null;
						if(nodeOpDH!=null){
							nomeOpDH = nodeOpDH.getNodeValue();
						}else{
							nomeOpDH =nomeServizio;
						}
						xpath="/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/dh-call/DBOBuilder[@name='"+nomeOpDH+"']";
						logger.debug("Operation=" + nomeOpDH);
						Node operation = parser.selectSingleNode(coreXml, xpath);
						   if(operation!=null)
								listaOperation.put(nomeOpDH, operation);
					}
				}
				xpath= "/GVCore/GVServices/Services/Service[@id-service='"+nomeServizio+"']/Operation/SubFlow/GVOperationNode[@id-system='"+sistema+"' and @operation-name='"+operazione+"']";
				//System.out.println("xpath="+xpath);
				operations = parser.selectNodeList(coreXml, xpath);
				if (operations.getLength() > 0) {
					for (int i = 0; i < operations.getLength(); i++) {
						Node gvOperNode = operations.item(i);
						Node nodeOpDH = parser.selectSingleNode(gvOperNode, "InputServices/dh-selector-service/dh-selector-call/@DH_SERVICE_NAME");
						String nomeOpDH =null;
						if(nodeOpDH!=null){
							nomeOpDH = nodeOpDH.getNodeValue();
						}else{
							nomeOpDH =nomeServizio;
						}
						xpath="/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/dh-call/DBOBuilder[@name='"+nomeOpDH+"']";
						logger.debug("Operation=" + nomeOpDH);
						Node operation = parser.selectSingleNode(coreXml, xpath);
						   if(operation!=null)
								listaOperation.put(nomeOpDH, operation);
					}
				}
			}
			return listaOperation;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaOperation;
	}
	public Node getDboBuilder(String dboBuilder, String sistema, String canale,String operazione){
		Node nodeDboBuilder = null;
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
		    String xpath="/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/dh-call[@name='"+operazione+"']/DBOBuilder[@name='"+dboBuilder+"']";
			nodeDboBuilder = parser.selectSingleNode(coreXml, xpath);		   
			return nodeDboBuilder;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return nodeDboBuilder;
	}
	public Map<String, Node> getListaServiziDhCall(
			String servizio, String sistema, String canale,String operazione){
		Map<String, Node> listaOperation = new HashMap<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
				String xpath= "/GVCore/GVServices/Services/Service[@id-service='"+servizio+"']/Operation/Flow/GVOperationNode[@id-system='"+sistema+"' and @operation-name='"+operazione+"']";
				//System.out.println("xpath="+xpath);
				NodeList operations = parser.selectNodeList(coreXml, xpath);
				if (operations.getLength() > 0) {
					for (int i = 0; i < operations.getLength(); i++) {
						Node gvOperNode = operations.item(i);
						Node nameOpDH = parser.selectSingleNode(gvOperNode, "InputServices/dh-selector-service/dh-selector-call/@DH_SERVICE_NAME");
						if(nameOpDH!=null){
							xpath="/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/dh-call/DBOBuilder[@name='"+nameOpDH.getNodeValue()+"']";
							logger.debug("Operation=" + nameOpDH.getLocalName());
							Node operation = parser.selectSingleNode(coreXml, xpath);
							if(operation!=null)
								listaOperation.put(nameOpDH.getLocalName(), operation);
						}
					}
				}
			return listaOperation;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
		List<String> listaServizi = getListaServizi();
		Map<String,Node> listTrasformazioni = getListaTrasformazioni(listaServizi);
		return getListaCryptoHelper(listTrasformazioni);

	}
	private Map<String,Node> getListaCryptoHelper(Map <String,Node>listaTrasformazioni) 
	{
		logger.debug("init getGVCryptoHelper");
		String[] listaKeyId = getListaKeyIdTrasformazioni(listaTrasformazioni);
		Map<String,Node> gvCryptoHelper = new HashMap<String,Node>();;
		String strXpath = "";
		if (listaKeyId != null) {
			for (int i = 0; i < listaKeyId.length; i++) {
				if (i == 0) {
					strXpath = "/GVCore/GVCryptoHelper[KeyID/@id='";
				}
				strXpath = strXpath + listaKeyId[i] + "'";
				if (i == (listaKeyId.length - 1)) {
					strXpath = strXpath + "]";
				}
				else {
					strXpath = strXpath + " or KeyID/@id='";
				}
			}
			logger.debug("strXpath = " + strXpath);

			Node nodeGvCryptoHelper;
			try {
				nodeGvCryptoHelper = XMLUtils.selectSingleNode_S(coreXml, strXpath);
				String name = nodeGvCryptoHelper.getAttributes().getNamedItem("id").getNodeValue();
				gvCryptoHelper.put(name, nodeGvCryptoHelper);
			} catch (XMLUtilsException e) {

				e.printStackTrace();
			}

		}
		return gvCryptoHelper;
	}
	public Node getGVCryptoHelper(String name) 
	{
		Node nodeGvCryptoHelper = null;
		String strXpath = "/GVCore/GVCryptoHelper[KeyID/@id='"+name+"']";
		try {
			nodeGvCryptoHelper = XMLUtils.selectSingleNode_S(coreXml, strXpath);
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}

		return nodeGvCryptoHelper;
	}

	private Node getGVKeyStoreCryptoHelper(String id) 
	{
		logger.debug("init getGVCryptoHelper");

		String strXpath = "/GVCore/GVCryptoHelper/KeyStoreID[id0'"+id+"']";
		Node nodeGvCryptoHelper = null;
		try {
			nodeGvCryptoHelper = XMLUtils.selectSingleNode_S(coreXml, strXpath);
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		return nodeGvCryptoHelper;
	}

	private Node getNodeService(String nomeServizio) 
	{
		Node service = null;
		try {
			service = XMLUtils.selectSingleNode_S(coreXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio + "']");
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
				NodeList tasks = parser.selectNodeList(coreXml, xPathTask);
				if (tasks.getLength() > 0) {
					for (int i = 0; i < tasks.getLength(); i++) {
						String name = parser.get(tasks.item(i), "@name");
						listTask.put(name, tasks.item(i));
					}
				}
			}
			return listTask;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return null;
	}
	public Map<String, Node> getGroups(List<String> listaServizi) 
	{
		Map<String, Node> listGroup = new HashMap<String,Node>();
		for (String servizio : listaServizi) {
			String xPathGroup = "/GVCore/GVServices/Services/Service[@id-service='" + servizio+ "']/@group-name";
			
			String groupName;
			try {
				Node groupa = XMLUtils.selectSingleNode_S(coreXml, xPathGroup);
				groupName = XMLUtils.get_S(coreXml, xPathGroup);
				System.out.println(groupName);
				if(groupName!=null){
					Node group = XMLUtils.selectSingleNode_S(coreXml, "/GVCore/GVServices/Groups/Group[@id-group='" + groupName + "']");
					if(group!=null)
						listGroup.put(groupName, group);			
				}
			} catch (XMLUtilsException e) {

				e.printStackTrace();
			}	
		}
		return listGroup;
	}
	public Map<String, Node> getListDataProvider() 
	{
		List<String> listaServizi = getListaServizi();
		return getListDataProvider(listaServizi);
	}

	private Map<String, Node> getListDataProvider(List<String> listaServizi) 
	{
		Map<String, Node> hlistaDp = new Hashtable<String, Node>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			for (String nomeServizio : listaServizi) {
				//getListDataProviderSystem(nomeServizio, hlistaDp, parser);
				getListDataProviderCoreIterator(nomeServizio, hlistaDp, parser);
				getListDataProviderCoreIteratorCall(nomeServizio, hlistaDp, parser);
				getListDataProviderCoreIteratorSubFlowCall(nomeServizio, hlistaDp, parser);
				getListDataProviderCoreSubFlowSplitted(nomeServizio, hlistaDp, parser);
				getListDataProviderCoreFlowDefs(nomeServizio, hlistaDp, parser);

			}
			//j2ee-ejb-call: input-ref-dp,output-ref-dp
			//jms-enqueue: ref-dp
			//jms-dequeue: ref-dp
			//RestServiceInvoker: ref-dp
			//http-call: ref-dp
			
			getListDataProviderForward(listaServizi, hlistaDp, parser);
			getListDataProviderWs(listaServizi, hlistaDp, parser);
			getListDataProviderOperation(listaServizi, hlistaDp, parser);
			return hlistaDp;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return hlistaDp;
	}

	private void getListDataProviderForward(List<String> listaServizi,Map<String, Node> vlistaDp, XMLUtils parser) 
	{
		Map<String, Node> listaForward = getListaForward(listaServizi);
		for (String forwardName:listaForward.keySet()) {
			String dp = listaForward.get(forwardName).getAttributes().getNamedItem("ref-dp").getNodeValue();
			Node nodeDp;
			try {
				nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
				if(nodeDp!=null)
				  vlistaDp.put(dp, nodeDp);
			} catch (XMLUtilsException e) {

				e.printStackTrace();
			}

		}
	}
	private void getListDataProviderWs(List<String> listaServizi,Map<String, Node> vlistaDp, XMLUtils parser) 
	{
		try {
			Map<String, Node> listaWs = this.getListaGvWebServices(listaServizi);
			for (String wsName:listaWs.keySet()) {
				if(listaWs.get(wsName).getAttributes().getNamedItem("input-dp")!=null){
					String dp = listaWs.get(wsName).getAttributes().getNamedItem("input-dp").getNodeValue();
					Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
					vlistaDp.put(dp, nodeDp);
					dp = listaWs.get(wsName).getAttributes().getNamedItem("output-dp").getNodeValue();
					nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
					vlistaDp.put(dp, nodeDp);
				}
			}
			listaWs = this.getListaGvWebServices(listaServizi);
			for (String wsName:listaWs.keySet()) {
				if(listaWs.get(wsName).getAttributes().getNamedItem("input-dp")!=null){
					String dp = listaWs.get(wsName).getAttributes().getNamedItem("input-dp").getNodeValue();
					Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
					vlistaDp.put(dp, nodeDp);
					dp = XMLUtils.get_S(listaWs.get(wsName),"//BusinessWebServices/WebService/WSOperation/@ref-dp");
					nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
					vlistaDp.put(dp, nodeDp);
				}
			}
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
	}


	private void getListDataProviderSystem(String nomeServizio, Map<String, Node> vlistaDp, XMLUtils parser) 
	{
		NodeList partecipants;
		try {
			partecipants = parser.selectNodeList(coreXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
					+ "']/Operation/Participant");

			logger.debug("xpath=" + "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
					+ "']/Operation/Participant");
			if (partecipants.getLength() > 0) {
				for (int i = 0; i < partecipants.getLength(); i++) {
					String sistema = parser.get(partecipants.item(i), "@id-system");
					String canale = parser.get(partecipants.item(i), "@id-channel");
					logger.debug("Partecipant=" + sistema);
					logger.debug("canale=" + canale);

					NodeList operations = parser.selectNodeList(coreXml, "/GVCore/GVServices/Services/Service[@id-service='"+nomeServizio+"']/Operation/Flow/GVOperationNode[@id-system='"+sistema+"']");
					if (operations.getLength() > 0) {
						for (int j = 0; j < operations.getLength(); j++) {
							String name = parser.get(operations.item(j), "@operation-name");
							logger.debug("Operation=" + name);

							NodeList dataProviders = parser.selectNodeList(coreXml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema
									+ "']/Channel[@id-channel='" + canale + "']/*[@name='"+name+"']/@ref-dp");
							for (int d = 0; d < dataProviders.getLength(); d++) {
								String dp = parser.get(dataProviders.item(d), ".");
								Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
								vlistaDp.put(dp, nodeDp);
							}
							dataProviders = parser.selectNodeList(coreXml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema
									+ "']/Channel[@id-channel='" + canale + "']/*[@name='"+name+"']/*/@ref-dp");
							for (int d = 0; d < dataProviders.getLength(); d++) {
								String dp = parser.get(dataProviders.item(d), ".");
								Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
								if(nodeDp!=null)
								  vlistaDp.put(dp, nodeDp);
							}
							dataProviders = parser.selectNodeList(coreXml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema
									+ "']/Channel[@id-channel='" + canale + "']/*[@name='"+name+"']/@input-ref-dp");
							for (int d = 0; d < dataProviders.getLength(); d++) {
								String dp = parser.get(dataProviders.item(d), ".");
								Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
								vlistaDp.put(dp, nodeDp);
							}
							dataProviders = parser.selectNodeList(coreXml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema
									+ "']/Channel[@id-channel='" + canale + "']/*[@name='"+name+"']/@output-ref-dp");
							for (int d = 0; d < dataProviders.getLength(); d++) {
								String dp = parser.get(dataProviders.item(d), ".");
								Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
								vlistaDp.put(dp, nodeDp);
							}
							dataProviders = parser.selectNodeList(coreXml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema
									+ "']/Channel[@id-channel='" + canale + "']/*[@name='"+name+"']/@globals-ref-dp");
							for (int d = 0; d < dataProviders.getLength(); d++) {
								String dp = parser.get(dataProviders.item(d), ".");
								Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
								vlistaDp.put(dp, nodeDp);
							}
						}
					}
				}
			}
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		//logger.debug("getListDataProviderSystem - LISTA DP=" + vlistaDp);
	}

	private void getListDataProviderCoreIterator(String nomeServizio, Map<String, Node> vlistaDp, 
			XMLUtils parser) 
	{
		NodeList iterator;
		try {
			iterator = parser.selectNodeList(coreXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
					+ "']/Operation/*/GVIteratorOperationNode/@collection-dp");
			logger.debug("getListDataProviderCoreIterator NUM ITERATOR=" + iterator.getLength());
			for (int i = 0; i < iterator.getLength(); i++) {
				String dp = parser.get(iterator.item(i), ".");
				Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
				if(nodeDp!=null)
				  vlistaDp.put(dp, nodeDp);
			}
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}

	}

	private void getListDataProviderCoreIteratorCall(String nomeServizio,Map<String, Node> vlistaDp, XMLUtils parser) 
	{
		NodeList refdp;
		try {
			refdp = parser.selectNodeList(coreXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
					+ "']/Operation/*/GVIteratorOperationNode/CoreCall/@ref-dp");
			logger.debug("getListDataProviderCoreIteratorCall NUM REFDP=" + refdp.getLength());
			for (int i = 0; i < refdp.getLength(); i++) {
				String dp = parser.get(refdp.item(i), ".");
				Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
				vlistaDp.put(dp, nodeDp);
			}
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}

	}

	private void getListDataProviderCoreIteratorSubFlowCall(String nomeServizio,
			Map<String, Node> vlistaDp, XMLUtils parser) 
	{
		NodeList refdp;
		try {
			refdp = parser.selectNodeList(coreXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
					+ "']/Operation/*/GVIteratorOperationNode/SubFlowCall/@ref-dp");
			logger.debug("getListDataProviderCoreIteratorSubFlowCall NUM REFDP=" + refdp.getLength());
			for (int i = 0; i < refdp.getLength(); i++) {
				String dp = parser.get(refdp.item(i), ".");
				Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
				if(nodeDp!=null)
				  vlistaDp.put(dp, nodeDp);
			}
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}

	}
	private void getListDataProviderOperation(List<String> listaServizi,
			Map<String, Node> vlistaDp, XMLUtils parser) 
	{
		try {
			Map<String, Node> listaSistemi = getListaSistemi(listaServizi);			
			for (String sistema:listaSistemi.keySet()){
				Map<String, Node> mapListaChannel = getListaChannel(listaServizi,sistema);
				for(String canale:mapListaChannel.keySet()){
					Map<String, Node> mapListaOperation = getListaVCLOp(listaServizi,sistema,canale);
					for(String operation:mapListaOperation.keySet()){
						Node oper = mapListaOperation.get(operation);
						
						//http-call: ref-dp
						if(oper.getLocalName().equals("http-call")){
							String dp = parser.get(oper, "method/@ref-dp");
							if(dp!=null){
								Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
								if(nodeDp!=null)
								  vlistaDp.put(dp, nodeDp);					
							}
						}
						//j2ee-ejb-call: input-ref-dp,output-ref-dp
						if(oper.getLocalName().equals("j2ee-ejb-call")){
							String dp = parser.get(oper, "@input-ref-dp");
							if(dp!=null){
								Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
								if(nodeDp!=null)
								  vlistaDp.put(dp, nodeDp);					
							}
							dp = parser.get(oper, "@output-ref-dp");
							if(dp!=null){
								Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
								if(nodeDp!=null)
								  vlistaDp.put(dp, nodeDp);					
							}
						}
						//jms-enqueue: ref-dp
						if(oper.getLocalName().equals("jms-enqueue")){
							String dp = parser.get(oper, "@ref-dp");
							if(dp!=null){
								Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
								if(nodeDp!=null)
								   vlistaDp.put(dp, nodeDp);					
							}
						}
						//jms-dequeue: ref-dp
						if(oper.getLocalName().equals("jms-dequeue")){
							String dp = parser.get(oper, "@ref-dp");
							if(dp!=null){
								Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
								if(nodeDp!=null)
								   vlistaDp.put(dp, nodeDp);					
							}
						}
						//RestServiceInvoker: ref-dp
						if(oper.getLocalName().equals("rest-call")){
							String dp = parser.get(oper, "RestServiceInvoker/@ref-dp");
							if(dp!=null){
								Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
								if(nodeDp!=null)
								  vlistaDp.put(dp, nodeDp);					
							}
						}
					}
				}
			}
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
	}

	private void getListDataProviderCoreSubFlowSplitted(String nomeServizio,
			Map<String, Node> vlistaDp, XMLUtils parser) 
	{
		try {
			NodeList refdp = parser.selectNodeList(coreXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
					+ "']/Operation/*/GVSubFlowSplittedNode/@ref-dp");
			logger.debug("getListDataProviderCoreSubFlowSplitted NUM REFDP=" + refdp.getLength());
			for (int i = 0; i < refdp.getLength(); i++) {
				String dp = parser.get(refdp.item(i), ".");
				Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
				vlistaDp.put(dp, nodeDp);
			}
			refdp = parser.selectNodeList(coreXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
					+ "']/Operation/*/GVSubFlowSplittedNode/@partition-dp");
			logger.debug("getListDataProviderCoreSubFlowSplitted NUM PARTITIONDP=" + refdp.getLength());
			for (int i = 0; i < refdp.getLength(); i++) {
				String dp = parser.get(refdp.item(i), ".");
				Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
				vlistaDp.put(dp, nodeDp);
			}
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
	}

	private void getListDataProviderCoreFlowDefs(String nomeServizio,
			Map<String, Node> vlistaDp, XMLUtils parser) 
	{
		try {
			NodeList refdp = parser.selectNodeList(coreXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
					+ "']/Operation/*/*/FlowDefs/FlowDef/@ref-dp");
			logger.debug("getListDataProviderCoreFlowDefs NUM REFDP=" + refdp.getLength());
			for (int i = 0; i < refdp.getLength(); i++) {
				String dp = parser.get(refdp.item(i), ".");
				Node nodeDp = parser.selectSingleNode(adapterXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"+dp+"']");
				vlistaDp.put(dp, nodeDp);
			}
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
	}

	private String[] getListKnowledgeBaseConfig(String nomeServizio) 
	{
		Map<String, String> hlistaKwB = new Hashtable<String, String>();
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			NodeList partecipants = parser.selectNodeList(coreXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
					+ "']/Operation/Participant");
			if (partecipants.getLength() > 0) {
				for (int i = 0; i < partecipants.getLength(); i++) {
					String sistema = parser.get(partecipants.item(i), "@id-system");
					String canale = parser.get(partecipants.item(i), "@id-channel");
					logger.debug("Partecipant=" + sistema);
					logger.debug("canale=" + canale);
					NodeList kwBase = parser.selectNodeList(coreXml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema
							+ "']/Channel[@id-channel='" + canale + "']/*/@ruleSet");
					logger.debug("xpath=" + "/GVCore/GVSystems/Systems/System[@id-system='" + sistema
							+ "']/Channel[@id-channel='" + canale + "']/*/@ruleSet");
					for (int c = 0; c < kwBase.getLength(); c++) {
						String kwb = parser.get(kwBase.item(i), ".");
						hlistaKwB.put(kwb, kwb);
					}
				}
			}
			Set<String> set = hlistaKwB.keySet(); // get set-view of keys
			logger.debug("getListKnowledgeBaseConfig - LISTA KwB=" + set);
			// get iterator
			String[] listKnowledgeBaseConfig = new String[hlistaKwB.size()];
			Iterator<String> itr = set.iterator();
			int i = 0;
			while (itr.hasNext()) {
				listKnowledgeBaseConfig[i] = itr.next();
				i++;
			}
			return listKnowledgeBaseConfig;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return null;
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
				NodeList partecipants = parser.selectNodeList(coreXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
						+ "']/Operation/Participant");
				if (partecipants.getLength() > 0) {
					for (int i = 0; i < partecipants.getLength(); i++) {
						String name = parser.get(partecipants.item(i), "@id-system");
						Node sytem = parser.selectSingleNode(coreXml, "/GVCore/GVSystems/Systems/System[@id-system='"+name+"']");
						logger.debug("Partecipant=" + name);
						listaSistemi.put(name, sytem);
					}
				}
			}
			return listaSistemi;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
			nodeSistema = parser.selectSingleNode(coreXml, "/GVCore/GVSystems/Systems/System[@id-system='"+name+"']");
			return nodeSistema;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
				NodeList partecipants = parser.selectNodeList(coreXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
						+ "']/Operation/Participant[@id-system='"+sistema+"']");
				if (partecipants.getLength() > 0) {
					for (int i = 0; i < partecipants.getLength(); i++) {
						String name = parser.get(partecipants.item(i), "@id-channel");
						Node channel = parser.selectSingleNode(coreXml, "/GVCore/GVSystems/Systems/System/Channel[@id-channel='"+name+"']");
						logger.debug("CHANNEL=" + name);
						listaChannel.put(name, channel);
					}
				}
			}
			return listaChannel;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
			nodeChannel = parser.selectSingleNode(coreXml, "/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']");
			return nodeChannel;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
				NodeList operations = parser.selectNodeList(coreXml, xPath);
				if (operations.getLength() > 0) {
					for (int i = 0; i < operations.getLength(); i++) {
						String name = parser.get(operations.item(i), "@operation-name");
						logger.debug("Operation=" + name);
						Node operation = parser.selectSingleNode(coreXml, "/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/*[@name='"+name+"']");
						if(operation!=null)
							listaOperation.put(name, operation);
					}
				}
				xPath = "/GVCore/GVServices/Services/Service[@id-service='"+nomeServizio+"']/Operation/SubFlow/*[@id-system='"+sistema+"']";
				operations = parser.selectNodeList(coreXml, xPath);
				if (operations.getLength() > 0) {
					for (int i = 0; i < operations.getLength(); i++) {
						String name = parser.get(operations.item(i), "@operation-name");
						logger.debug("Operation=" + name);
						Node operation = parser.selectSingleNode(coreXml, "/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/*[@name='"+name+"']");
						if(operation!=null)
							listaOperation.put(name, operation);
					}
				}
			}
			return listaOperation;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaOperation;
	}
	public Node getVCLOp(String vclOp,String sistema,String canale) 
	{
		Node nodeOperation = null;
		XMLUtils parser = null;
		try {
			parser = XMLUtils.getParserInstance();
			nodeOperation = parser.selectSingleNode(coreXml, "/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/*[@name='"+vclOp+"']");
			return nodeOperation;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
	private Map<String, Node> getListaDataSource(Map<String, Node> listaTrasf)
	{
		XMLUtils parser = null;
		Map<String, Node> listaDataSource = new HashMap<String, Node>();
		try {	
			parser = XMLUtils.getParserInstance();
			for (String nomeTrasformazione:listaTrasf.keySet()) {	
				Node trasf = listaTrasf.get(nomeTrasformazione);
				String dataSourceName = "Default";
				if(trasf!=null && trasf.getAttributes()!=null && trasf.getAttributes().getNamedItem("DataSourceSet")!=null){
					dataSourceName = listaTrasf.get(nomeTrasformazione).getAttributes().getNamedItem("DataSourceSet").getNodeValue();
				}
				String xPathDataSpurce = "/GVCore/GVDataTransformation/DataSourceSets/DataSourceSet[@name='"
						+ dataSourceName+"']";
				Node dataSource = parser.selectSingleNode(coreXml, xPathDataSpurce);
				listaDataSource.put(dataSourceName, dataSource);
			}
			return listaDataSource;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
			Node nodeTrasf = this.getTrasformazione(nomeTrasf);
			String dataSourceName = "Default";
			if(nodeTrasf!=null && nodeTrasf.getAttributes()!=null && nodeTrasf.getAttributes().getNamedItem("DataSourceSet")!=null){
				dataSourceName = parser.get(nodeTrasf, "@DataSourceSet");
			}
			String xPathDataSpurce = "/GVCore/GVDataTransformation/DataSourceSets/DataSourceSet[@name='"
					+ dataSourceName+"']";
			nodeDataSource = parser.selectSingleNode(coreXml, xPathDataSpurce);
			return nodeDataSource;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
			Node nodeTrasf = this.getTrasformazione(nomeTrasf);
			
			if(nodeTrasf!=null && nodeTrasf.getAttributes()!=null && nodeTrasf.getAttributes().getNamedItem("DataSourceSet")!=null){
				dataSourceName = parser.get(nodeTrasf, "@DataSourceSet");
			}
			return dataSourceName;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
			nodeDataSource = parser.selectSingleNode(coreXml, xPathDataSpurce);
			return nodeDataSource;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return nodeDataSource;
	}
	public Map<String, Node> getListaDataSource(List<String> listaServizi)
	{
		Map<String, Node> listaTrasformazioni = getListaTrasformazioni(listaServizi);
		return getListaDataSource(listaTrasformazioni);
	}
	private List<String> getListaTrasformazioniDH(List<String> listaServizi) {
		Map<String, Node> mapListaSistemi = getListaSistemi(listaServizi);
		List<String> listaTrasformazioniDH = new ArrayList<String>();
		for (String sistema:mapListaSistemi.keySet()){
			Map<String, Node> mapListaChannel = getListaChannel(listaServizi,sistema);
			for(String canale:mapListaChannel.keySet()){
				Map<String, Node> mapListaOperation = getListaVCLOp(listaServizi,sistema,canale);
				for(String operation:mapListaOperation.keySet()){
					Node oper = mapListaOperation.get(operation);
					if(oper.getLocalName().equals("dh-call")){
						String operazione = oper.getAttributes().getNamedItem("name").getNodeValue();
						Map<String, Node> mapListaServiziDhCall = getListaDboBuilder(listaServizi,sistema,canale,operazione);
						if(mapListaServiziDhCall.size()>0){
							for(String builder:mapListaServiziDhCall.keySet()){
								Node nodeDH = mapListaServiziDhCall.get(builder);
								NodeList trasfsDH =nodeDH.getChildNodes();
								for(int i=0;i<trasfsDH.getLength();i++){
									Node dboBuilder =trasfsDH.item(i);
									String trasfNAme = null;
									try {
										trasfNAme = XMLUtils.get_S(dboBuilder, "@transformation");
									} catch (XMLUtilsException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									if(trasfNAme!=null && !trasfNAme.equals("")){
										if(!listaTrasformazioniDH.contains(trasfNAme))
										  listaTrasformazioniDH.add(trasfNAme);
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
				NodeList inputServices = parser.selectNodeList(coreXml,
						xPathInputServices);
				NodeList outputServices = parser.selectNodeList(coreXml,
						xPathOutputServices);

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

			e.printStackTrace();
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
				NodeList inputServices = parser.selectNodeList(coreXml,
						xPathInputServices);
				NodeList outputServices = parser.selectNodeList(coreXml,
						xPathOutputServices);

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

			e.printStackTrace();
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

				if ("XSLTransformation".equals(dtType) ||
						"XSLFOPTransformation".equals(dtType) ||
						"HL72XMLTransformation".equals(dtType) ||
						"XML2HL7Transformation".equals(dtType) ||
						"JSON2XMLTransformation".equals(dtType) ||
						"XML2JSONTransformation".equals(dtType)) {
					String nomeFile = parser.get(trasf, "@XSLMapName", parser.get(trasf, "@InputXSLMapName",
							parser.get(trasf, "@OutputXSLMapName", "")));
					if(nomeFile!=null && !nomeFile.equals("")){	
						listaFileXsl.put(name,nomeFile);;		
					}
				}
			}
			return listaFileXsl;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
				String dataSource = "Default";
				if(trasf.getAttributes().getNamedItem("DataSourceSet")!=null)
					dataSource = parser.get(trasf, "@DataSourceSet"); 

				if ("Bin2XMLTransformation".equals(dtType) ||
						"XML2BinTransformation".equals(dtType)) {
					String nomeFile = parser.get(trasf, "@ConversionMapName");
					listaFileXsl.put(nomeFile,dataSource);
				}
			}
			return listaFileXsl;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
				String dataSource = "Default";
				if(trasf.getAttributes().getNamedItem("DataSourceSet")!=null)
					dataSource = parser.get(trasf, "@DataSourceSet"); 	
				if ("XQTransformation".equals(dtType)) {
					String nomeFile = parser.get(trasf, "@XQMapName");
					listaFileXsl.put(nomeFile,dataSource);
				}else if ("Bin2XMLTransformation".equals(dtType) ||
						"XML2BinTransformation".equals(dtType)) {
					String nomeFile = parser.get(trasf, "@ConversionMapName");
					listaFileXsl.put(nomeFile,dataSource);
				}
			}
			return listaFileXsl;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
		List<String> listaTrasformazioniDH = this.getListaTrasformazioniDH(listaServizi);
		Map<String, Node> listaTrasformazioni = getListaTrasformazioni(listaServizi,listaTrasformazioniDH);	
		Map<String, Node> listaCrypoHelper = getListaCryptoHelper(listaTrasformazioni);
		for(String cryptoHelper:listaCrypoHelper.keySet()){
			Node nodeCryptoHelper = listaCrypoHelper.get(cryptoHelper);
			String ksn;
			try {
				ksn = XMLUtils.get_S(nodeCryptoHelper, "@key-store-name");

				listaFileKeyStore.add(ksn);
			} catch (XMLUtilsException e) {

				e.printStackTrace();
			}
		}
		return listaFileKeyStore;
	}
	public Map<String, Node> getListaTrasformazioni(List<String> listaServizi)
	{
		List<String> listaTrasformazioniDH = getListaTrasformazioniDH(listaServizi);
		return getListaTrasformazioni(listaServizi,listaTrasformazioniDH);
	}

	public Map<String, Node> getListaTrasformazioni()
	{
		List<String> listaServizi = getListaServizi();
		return getListaTrasformazioni(listaServizi);
	}
	/**
	 * @param xml
	 * @param nomeServizio
	 * @return
	 * @ 
	 */
	private Map<String, Node> getListaTrasformazioni(List<String> listaServizi,List<String> listaTrasformazioniDH)
	{
		XMLUtils parser = null;
		Map<String, Node> listaTrasformazioni = new HashMap<String, Node>();
		try {
			for (String nomeServizio : listaServizi) {
				String xPathInputServices = "/GVCore/GVServices/Services/Service[@id-service='"
						+ nomeServizio
						+ "']/Operation/*/*[@type='flow-node']/InputServices/gvdte-service/map-name-param/@value";
				String xPathOutputServices = "/GVCore/GVServices/Services/Service[@id-service='"
						+ nomeServizio
						+ "']/Operation/*/*[@type='flow-node']/OutputServices/gvdte-service/map-name-param/@value";

				parser = XMLUtils.getParserInstance();
				NodeList inputServices = parser.selectNodeList(coreXml,
						xPathInputServices);
				NodeList outputServices = parser.selectNodeList(coreXml,
						xPathOutputServices);

				int i = 0;
				for (i = 0; i < inputServices.getLength(); i++) {
					String name = inputServices.item(i).getNodeValue();
					Node trasf = parser.selectSingleNode(coreXml,
							"/GVCore/GVDataTransformation/Transformations/*[@name='"
									+ name + "']");
					listaTrasformazioni.put(name, trasf);
					if(trasf.getLocalName().equals("SequenceTransformation")){
						getListaTrasfSequence(trasf, parser, listaTrasformazioni);
					}
				}
				for (int j = 0; j < outputServices.getLength(); j++) {
					String name = outputServices.item(j).getNodeValue();
					Node trasf = parser.selectSingleNode(coreXml,
							"/GVCore/GVDataTransformation/Transformations/*[@name='"
									+ name + "']");
					listaTrasformazioni.put(name, trasf);
					if(trasf!=null && trasf.getLocalName().equals("SequenceTransformation")){
						getListaTrasfSequence(trasf, parser, listaTrasformazioni);
					}
				}
				for (String nameTrafDH:listaTrasformazioniDH) {
					Node trasf = parser.selectSingleNode(coreXml,
							"/GVCore/GVDataTransformation/Transformations/*[@name='"
									+ nameTrafDH + "']");
					listaTrasformazioni.put(nameTrafDH, trasf);
					if(trasf!=null && trasf.getLocalName().equals("SequenceTransformation")){
						getListaTrasfSequence(trasf, parser, listaTrasformazioni);
					}
				}
				logger.debug("END - Transformations for service["
						+ nomeServizio + "]");

			}
			return listaTrasformazioni;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		} finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaTrasformazioni;

	}
	public List<String> getListaServiziGVCall(String servizio)
	{
			String xPathInputServices = "/GVCore/GVServices/Services/Service[Operation/*/GVCoreCallNode/@id-service='"+servizio+"']";
			return getListaServiziFiltered(xPathInputServices);
	}
	public List<String> getListaServiziTrasf(String trasf)
	{
			String xPathInputServices = "/GVCore/GVServices/Services/Service[Operation/*/*/OutputServices/gvdte-service/map-name-param/@value='"+trasf+"']";
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
			NodeList results = parser.selectNodeList(coreXml, xPath);
			for (int i = 0; i < results.getLength(); i++) {
				String serviceName = parser.get(results.item(i), "@id-service");
				retListaServizi.add(serviceName);
				logger.debug("Id Servizio Nuovo=" + serviceName);
			}
			return retListaServizi;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
			nodeTrasf = parser.selectSingleNode(coreXml,"/GVCore/GVDataTransformation/Transformations/*[@name='"+ name + "']");
			
			return nodeTrasf;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
				trasf = parser.selectSingleNode(coreXml,
						"/GVCore/GVDataTransformation/Transformations/*[@name='"
								+ name + "']");
				listaTrasformazioni.put(name, trasf);
				if(trasf.getLocalName().equals("SequenceTransformation")){
					getListaTrasfSequence(trasf, parser, listaTrasformazioni);
				}

			}
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
			NodeList kid = parser.selectNodeList(coreXml, strXpath);
			if (kid.getLength() > 0) {
				listaKeyIdTrasformazioni = new String[kid.getLength()];
				for (int j = 0; j < kid.getLength(); j++) {
					listaKeyIdTrasformazioni[i] = parser.get(kid.item(j), ".");
					logger.debug("value=" + listaKeyIdTrasformazioni[j]);
				}
			}
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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
		return getListaObject(coreXml, "forward-name" , xpath);
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
				if(results.item(i).getAttributes()!=null && results.item(i).getAttributes().getNamedItem(keyAttribute)!=null){
					String name = results.item(i).getAttributes().getNamedItem(keyAttribute).getNodeValue();
					listaObject.put(name,results.item(i));
				}
			}
			return listaObject;
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
		return listaObject;
	}
	public Node getNodeCore(String xPath)  {
		Node node = null;
		try {
			node = XMLUtils.selectSingleNode_S(coreXml, xPath);
		} catch (XMLUtilsException e) {

			e.printStackTrace();
		}
		return node;
	}
	public Node getNodeAdapter(String xPath)  {
		Node node = null;
		try {
			node = XMLUtils.selectSingleNode_S(adapterXml, xPath);
		} catch (XMLUtilsException e) {

			e.printStackTrace();
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

			e.printStackTrace();
		}

		return ret;
	}

	public static void main(String[] args)
	{
		try {
			//GVConfig gvConfigServer = new GVConfig("/Users/macbook/Desktop/DATI/GvServer-3.4.0.10/GreenV/xmlconfig/GVcore.xml","/Users/macbook/Desktop/DATI/GvServer-3.4.0.10/GreenV/xmlconfig/GVAdapters.xml");
			//GVConfig gvConfigServer = new GVConfig("/Users/macbook/Desktop/GV/appo/conf/GVcore.xml","/Users/macbook/Desktop/GV/appo/conf/GVAdapters.xml");
			GVConfig gvConfigServer = new GVConfig("/Users/macbook/Desktop/GV/PROVA/GV/conf/GVcore.xml","/Users/macbook/Desktop/GV/PROVA/GV/conf/GVAdapters.xml");
			System.out.println(gvConfigServer.getGvCore(true));
			System.out.println(gvConfigServer.getGvAdapters(true));
			//List <String> listaServizi = new ArrayList<String>();
			/*listaServizi.add("ELD_Diagnostic");
			listaServizi.add("ELD_DiagnosticList");
			listaServizi.add("ELD_DutyDailyHeader");
			listaServizi.add("ELD_DutyStatus");
			listaServizi.add("ELD_DutyStatusDayTurn");
			listaServizi.add("ELD_DutyStatusList");
			listaServizi.add("ELD_EmailOutputFile");
			listaServizi.add("ELD_EventEnhancement");
			listaServizi.add("ELD_Events");
			listaServizi.add("ELD_EventsList");
			listaServizi.add("ELD_GenerateOutputFile");
			listaServizi.add("ELD_GetHeader");
			listaServizi.add("ELD_GetInfo");
			listaServizi.add("ELD_GetLogo");
			listaServizi.add("ELD_GetOnBoard");
			listaServizi.add("ELD_GetRecordData");
			listaServizi.add("ELD_GetStateCode");
			listaServizi.add("ELD_HeaderInsert");
			listaServizi.add("ELD_InfoInsert");
			listaServizi.add("ELD_InvioNotifiche");
			listaServizi.add("ELD_LastDriverDutyStatus");
			listaServizi.add("ELD_Login");
			listaServizi.add("ELD_ReportCertification");
			listaServizi.add("ELD_ReportOperation");
			listaServizi.add("ELD_SendSMS");
			listaServizi.add("ELD_SetDutyStatusApp");
			listaServizi.add("ELD_SetOnBoard");
			listaServizi.add("ELD_logout");*/
			
			
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
			e.printStackTrace();
		}
	}


}

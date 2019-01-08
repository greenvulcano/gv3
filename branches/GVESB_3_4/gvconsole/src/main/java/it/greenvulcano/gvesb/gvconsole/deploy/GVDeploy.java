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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.file.FileManager;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.zip.ZipHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import max.xml.DOMWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;


/**
 * 
 * GVCoreParser class
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GVDeploy
{

	/**
	 * @param
	 */
	private String          nomeZipFile     = null;
	private GVConfig gvConfigZip = null;
	private GVConfig gvConfigServer = null;

	private static Logger  logger    = GVLogger.getLogger(GVDeploy.class);

	public GVConfig getZipGVConfig(){
		return gvConfigZip;
	}
	public GVConfig getServerGVConfig(){
		return gvConfigServer;
	}
	public GVDeploy(GVConfig gvConfigZip,GVConfig gvConfigServer)
	{
		this.gvConfigServer=gvConfigServer;
		this.gvConfigZip=gvConfigZip;
	}
	public GVDeploy() throws Exception
	{
		loadParser();
	}
	/**
	 * Legge i file xml di configurazione dal file .zip e dal server
	 * @throws Exception 
	 * 
	 * @throws 
	 * @throws IOException
	 * @throws XMLConfigException
	 */
	public void loadParser() throws XMLConfigException
	{
		String path = java.lang.System.getProperty("java.io.tmpdir");
		gvConfigZip = new GVConfig(path + File.separator + "conf" + File.separator + "GVCore.xml",
				path + File.separator + "conf" + File.separator + "GVAdapters.xml");
		gvConfigServer = new GVConfig(XMLConfig.getURL("GVCore.xml"),XMLConfig.getURL("GVAdapters.xml"));
	}

	/**
	 * 
	 * Restituice la lista dei nomi dei servizi presenti nel file di
	 * configurazione presenti sul server
	 * 
	 * 
	 * @return String[] lista dei nomi dei servizi presenti nel file di
	 *         configurazione in input
	 * @throws 
	 * @throws IOException
	 */
	public List<String> getListaServiziServer() throws Exception
	{
		return gvConfigServer.getListaServizi();
	}

	private void aggiornaGVCore()
	{
		List<String>  listaServizi = gvConfigZip.getListaServizi();
		Map<String, Node> mapListaCryptoHelperZip = gvConfigZip.getListaGVCryptoHelper();
		for(String crytoHelper:mapListaCryptoHelperZip.keySet()){
			Node cryptoHelperNodeZip = mapListaCryptoHelperZip.get(crytoHelper);
			Node cryptoHelperNodeServer = gvConfigServer.getGVCryptoHelper(crytoHelper);
			aggiornaNode(cryptoHelperNodeZip,cryptoHelperNodeServer,"/GVCore/GVCryptoHelper");
		}

		Map<String, Node> listaForwardZip = gvConfigZip.getListaForward();
		Map<String, Node> listaForwardServer = gvConfigServer.getListaForward();
		for(String servizio:listaForwardZip.keySet()){
			Node nodeForwaredZip=listaForwardZip.get(servizio);
			Node nodeForwardServer=listaForwardServer.get(servizio);
			aggiornaNode(nodeForwaredZip,nodeForwardServer,"/GVCore/GVForwards");
		}
		
		Map<String, Node> listaGruppiZip = gvConfigZip.getGroups();
		Map<String, Node> listaGruppiServer = gvConfigServer.getGroups();
		for(String servizio:listaGruppiZip.keySet()){
			Node nodeGruppoZip=listaGruppiZip.get(servizio);
			Node nodeGruppoServer=listaGruppiServer.get(servizio);
			aggiornaNode(nodeGruppoZip,nodeGruppoServer,"/GVCore/GVServices/Groups");
		}
		Map<String, Node> listaServiziZip = gvConfigZip.getListaNodeServizi();
		Map<String, Node> listaServiziServer = gvConfigServer.getListaNodeServizi(listaServizi);
		for(String servizio:listaServiziZip.keySet()){
			Node nodeServizioZip=listaServiziZip.get(servizio);
			Node nodeServizioServer=listaServiziServer.get(servizio);
			aggiornaNode(nodeServizioZip,nodeServizioServer,"/GVCore/GVServices/Services");
		}

		Map<String, Node> mapListaSistemiZip = gvConfigZip.getListaSistemi();
		for (String sistema:mapListaSistemiZip.keySet()){
			Node nodeSistemaZip    = mapListaSistemiZip.get(sistema);
			Node nodeSistemaServer = gvConfigServer.getSistema(sistema);
			if(nodeSistemaServer==null){
				aggiornaNode(nodeSistemaZip,nodeSistemaServer,"/GVCore/GVSystems/Systems");
			}else{
				Map<String, Node> mapListaChannelZip = gvConfigZip.getListaChannel(sistema);
				for (String canale:mapListaChannelZip.keySet()){
					Node nodeCanaleZip    = mapListaChannelZip.get(canale);
					Node nodeCanaleServer = gvConfigServer.getChannel(canale, sistema);
					if(nodeCanaleServer==null){
						aggiornaNode(nodeCanaleZip,nodeCanaleServer,"/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']");
					}else{
						Map<String, Node> mapListaVCLOpZip = gvConfigZip.getListaVCLOp(sistema,canale);
						for (String vclOp:mapListaVCLOpZip.keySet()){
							Node nodeVclOPZip    = mapListaVCLOpZip.get(vclOp);
							Node nodeVclOpServer =gvConfigServer.getVCLOp(vclOp, sistema, canale);
							if(nodeVclOpServer==null){
								aggiornaNode(nodeVclOPZip,nodeVclOpServer,"/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']");
							}else{
								if(nodeVclOPZip.getLocalName().equals("dh-call")){
									Map<String, Node> mapListaDboBuilder = gvConfigZip.getListaDboBuilder(sistema,canale,vclOp);
									for (String dboBuilder:mapListaDboBuilder.keySet()){
										Node nodeDataHandlerZip = mapListaDboBuilder.get(dboBuilder);
										Node nodeDataHandlerServer = gvConfigServer.getDboBuilder(dboBuilder, sistema, canale, vclOp);
										aggiornaNode(nodeDataHandlerZip,nodeDataHandlerServer,"/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/dh-call[@name='"+vclOp+"']");
									}
								}else{
									aggiornaNode(nodeVclOPZip,nodeVclOpServer,"/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']");
								}

							}

						}

					}
				}
			}
		}

		Map<String, Node> listaAclGvZip = gvConfigZip.getListaAclGV();
		Map<String, Node> listaAclGvServer = gvConfigServer.getListaAclGV(listaServizi);
		for(String key:listaAclGvZip.keySet()){
			Node nodeAclGvZip=listaAclGvZip.get(key);
			Node nodeAclGvServer=listaAclGvServer.get(key);
			aggiornaNode(nodeAclGvZip,nodeAclGvServer,"/GVCore/GVPolicy/ACLGreenVulcano");
		}
		Map<String, Node> listaRoleRefZip = gvConfigZip.getListaRoleRef();
		for(String key:listaRoleRefZip.keySet()){
			Node nodeRoleRefZip=listaRoleRefZip.get(key);
			Node nodeRoleRefServer=gvConfigServer.getRoleRef(key);
			aggiornaNode(nodeRoleRefZip,nodeRoleRefServer,"/GVCore/GVPolicy/Roles");
		}
		Map<String, Node> listaAddressSetZip = gvConfigZip.getListaAddressSet();
		for(String key:listaAddressSetZip.keySet()){
			Node nodeAddressSetZip=listaAddressSetZip.get(key);
			Node nodeAddressSetServer=gvConfigServer.getAddressSet(key);
			aggiornaNode(nodeAddressSetZip,nodeAddressSetServer,"/GVCore/GVPolicy/Addresses");
		}

		Map<String, Node> listaGVBufferDumpZip = gvConfigZip.getListaGVBufferDump();
		Map<String, Node> listaGVBufferDumpServer = gvConfigServer.getListaGVBufferDump(listaServizi);
		for(String gvBufferDump:listaGVBufferDumpZip.keySet()){
			Node nodeGVBufferDumpZip=listaGVBufferDumpZip.get(gvBufferDump);
			Node nodeGVBufferDumpServer=listaGVBufferDumpServer.get(gvBufferDump);
			aggiornaNode(nodeGVBufferDumpZip,nodeGVBufferDumpServer,"/GVCore/GVBufferDump");
		}

		Map<String, Node> listaDataSourceZip = gvConfigZip.getListaDataSource();
		for(String dataSource:listaDataSourceZip.keySet()){
			Node nodeDataSourceZip=listaDataSourceZip.get(dataSource);
			Node nodeDataSourceServer=gvConfigServer.getDataSource(dataSource);
			aggiornaNode(nodeDataSourceZip,nodeDataSourceServer,"/GVCore/GVDataTransformation/DataSourceSets");
		}
		Map<String, Node> listaTrasformazioniZip = gvConfigZip.getListaTrasformazioni();
		for(String trasformazione:listaTrasformazioniZip.keySet()){
			Node nodeTrasformazioneZip=listaTrasformazioniZip.get(trasformazione);
			Node nodeTrasformszionServer=gvConfigServer.getTrasformazione(trasformazione);
			aggiornaNode(nodeTrasformazioneZip,nodeTrasformszionServer,"/GVCore/GVDataTransformation/Transformations");
		}
		Map<String, Node> listaTaskZip = gvConfigZip.getListaTask();
		Map<String, Node> listaTaskServer = gvConfigServer.getListaTask(listaServizi);
		for(String task:listaTaskZip.keySet()){
			Node nodeTaskZip=listaTaskZip.get(task);
			Node nodeTaskServer=listaTaskServer.get(task);
			aggiornaNode(nodeTaskZip,nodeTaskServer,"/GVCore/GVTaskManagerConfiguration/TaskGroups");
		}
	}
	private void aggiornaGVAdapters()
	{
		
		Map<String, Node> mapListaBusinessWsZip = gvConfigZip.getListaBusinessWebServices();
		Map<String, Node> mapListaBusinessWsServer = gvConfigZip.getListaBusinessWebServices();
		for(String businessWs:mapListaBusinessWsZip.keySet()){
			Node nodeBusinessWsZip = mapListaBusinessWsZip.get(businessWs);
			Node nodeBusinessWsServer = mapListaBusinessWsServer.get(businessWs);
			aggiornaNodeAdapter(nodeBusinessWsZip,nodeBusinessWsServer,"/GVAdapters/GVWebServices/BusinessWebServices/WebService");
		}
		Map<String, Node> mapListaGvWsZip = gvConfigZip.getListaGvWebServices();
		Map<String, Node> mapListaGvWsServer = gvConfigZip.getListaGvWebServices();
		for(String gvWs:mapListaGvWsZip.keySet()){
			Node nodeGvWsZip = mapListaGvWsZip.get(gvWs);
			Node nodeGvWsServer = mapListaGvWsServer.get(gvWs);
			aggiornaNodeAdapter(nodeGvWsZip,nodeGvWsServer,"/GVAdapters/GVWebServices/GreenVulcanoWebServices");
		}
		Map<String, Node> mapListaDataProviderZip = gvConfigZip.getListDataProvider();
		Map<String, Node> mapListaDataProviderServer = gvConfigZip.getListDataProvider();
		for(String dataProvider:mapListaDataProviderZip.keySet()){
			Node dataProviderNodeZip = mapListaDataProviderZip.get(dataProvider);
			Node dataProviderNodeServer = mapListaDataProviderServer.get(dataProvider);
			aggiornaNodeAdapter(dataProviderNodeZip,dataProviderNodeServer,"/GVAdapters/GVDataProviderManager/DataProviders");
		}
		Map<String,Node> listActionMapping = gvConfigZip.getListActionMapping();
		for(String action:listActionMapping.keySet()){
			Node nodeActionMappingZip    = listActionMapping.get(action);
			Node nodeActionMappingServer = listActionMapping.get(action);
			if(nodeActionMappingServer==null){
				aggiornaNodeAdapter(nodeActionMappingZip,nodeActionMappingServer,"/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/");
			}else{
				Map<String, Node> listaRestActionMappingZip = gvConfigZip.getListaRestActionMapping(action);
				Map<String, Node> listaGVBufferDumpServer = gvConfigServer.getListaRestActionMapping(action);
				for(String rest:listaRestActionMappingZip.keySet()){
					Node nodeRestActionMappingZip=listaRestActionMappingZip.get(rest);
					Node nodeRestActionMappingServer=listaGVBufferDumpServer.get(rest);
					aggiornaNodeAdapter(nodeRestActionMappingZip,nodeRestActionMappingServer,"/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/ActionMappings/RESTActionMapping[@Action='"+action+"']/OperationMappings");
				}

			}
		}
	}
	private void aggiornaNode(Node nodeZip,Node nodeServer,String baseXpath)
	{
		XmlDiff xmlDiff = new XmlDiff();
		boolean ifDiff = true;
		try {
			if(nodeServer!=null){
				List<String> listDiff = xmlDiff.compareXML(nodeZip, nodeServer);
				ifDiff = listDiff.size()>0;
			}
			if(ifDiff){
				Node base = gvConfigServer.getNodeCore(baseXpath);
				if(base!=null){
				  if (nodeServer != null) {
					Node importedNode = base.getOwnerDocument().importNode(nodeZip, true);
					base.replaceChild(importedNode, nodeServer);
					logger.debug("Nodo Service gia esistente aggiornamento");
				  }
				  else {
					Node importedNode = base.getOwnerDocument().importNode(nodeZip, true);
					base.appendChild(importedNode);
					logger.debug("Nodo Service non esistente inserimento");
				  }
				}
			}
		} catch (Exception e) {
			// 
			e.printStackTrace();
		}
	}
	private void aggiornaNodeAdapter(Node nodeZip,Node nodeServer,String baseXpath)
	{
		XmlDiff xmlDiff = new XmlDiff();
		boolean ifDiff = true;
		try {
			if(nodeServer!=null){
				List<String> listDiff = xmlDiff.compareXML(nodeZip, nodeServer);
				ifDiff = listDiff.size()>0;
			}
			if(ifDiff){
				Node base = gvConfigServer.getNodeAdapter(baseXpath);
				if(base!=null){
				  if (nodeServer != null) {
					Node importedNode = base.getOwnerDocument().importNode(nodeZip, true);
					base.replaceChild(importedNode, nodeServer);
					logger.debug("Nodo Service gia esistente aggiornamento");
				  }
				  else {
					Node importedNode = base.getOwnerDocument().importNode(nodeZip, true);
					base.appendChild(importedNode);
					logger.debug("Nodo Service non esistente inserimento");
				  }
				}
			}
		} catch (Exception e) {
			// 
			e.printStackTrace();
		}
	}
	
	
	public void save()
	{
		System.out.println("Init Save");
		aggiornaGVCore();
		aggiornaGVAdapters();
		copiaFileGvCore();
		copiaFileGvAdapter();
		copiaFileXsl();
		copiaFileBin();
		copiaFileXsd();
		copiaFileJsd();
		copiaFileWsdl();
		copiaFileKeyStore();
		copiaFileXq();
		System.out.println("End Save");
	}
	private void copiaFileGvCore() {
		try {
			gvConfigServer.writeGvCore(XMLConfig.getURL("GVCore.xml"));
		} catch (XMLConfigException e) {
			// 
			e.printStackTrace();
		}	
	}
	private void copiaFileGvAdapter(){
		try {
			gvConfigServer.writeGvAdapters(XMLConfig.getURL("GVAdapters.xml"));
		} catch (XMLConfigException e) {
			// 
			e.printStackTrace();
		}		
	}

	private void copiaFileXsl() {

		String appoDir =getAppoDir();
		String gvDir = null;
		try {
			gvDir = PropertiesHandler.expand("${{gv.app.home}}", null);

			Map<String, String> listFileXslZip = gvConfigZip.getListaFileXsl();
			for(String key:listFileXslZip.keySet()){
				String nomeFile = listFileXslZip.get(key);
				String dataSourceName = gvConfigZip.getDataSourceValueFromTrasf(key);
				String dirDteZip = gvConfigZip.getDteDir(dataSourceName,"xsl");
				String inputFile = appoDir + File.separator + dirDteZip + File.separator +nomeFile;
				File fin = new File(inputFile);
				dataSourceName = gvConfigServer.getDataSourceValueFromTrasf(key);
				String dirDteServer =gvConfigServer.getDteDir(dataSourceName,"xsl");
				String outputFile = gvDir + File.separator + dirDteServer+ File.separator +nomeFile;
				if (fin.exists()) {
					copiaFile(inputFile,outputFile);
				}
			}
		} catch (PropertiesHandlerException e) {
			e.printStackTrace();
		}

	}
	private void copiaFileXq() {
		String appoDir =getAppoDir();
		String gvDir = getGvDir();
		Map<String, String> listFileXqZip = gvConfigZip.getListaFileXq();
		for(String key:listFileXqZip.keySet()){
			String nomeFile = listFileXqZip.get(key);
			String dataSourceName = gvConfigZip.getDataSourceValueFromTrasf(key);
			String dirDteZip = this.gvConfigZip.getDteDir(dataSourceName,"xq");
			String inputFile = appoDir + File.separator + dirDteZip + File.separator +nomeFile;
			File fin = new File(inputFile);
			dataSourceName = gvConfigServer.getDataSourceValueFromTrasf(key);
			String dirDteServer =gvConfigServer.getDteDir(dataSourceName,"xq");
			String outputFile = gvDir + File.separator + dirDteServer+ File.separator +nomeFile;
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}
		}
	}
	private void copiaFileBin(){
		String appoDir =getAppoDir();
		String gvDir = getGvDir();
		Map<String, String> listFileBinZip = gvConfigZip.getListaFileBin();
		for(String key:listFileBinZip.keySet()){
			String nomeFile = listFileBinZip.get(key);
			String dataSourceName = gvConfigZip.getDataSourceValueFromTrasf(key);
			String dirDteZip = this.gvConfigZip.getDteDir(dataSourceName,"bin");
			String inputFile = appoDir + File.separator + dirDteZip + File.separator +nomeFile;
			File fin = new File(inputFile);
			dataSourceName = gvConfigServer.getDataSourceValueFromTrasf(key);
			String dirDteServer =gvConfigServer.getDteDir(dataSourceName,"bin");
			String outputFile = gvDir + File.separator + dirDteServer+ File.separator +nomeFile;
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}
		}
	}
	private void copiaFileXsd() {
		String appoDir =getAppoDir();
		String gvDir = getGvDir();
		List<String> listFileXsdZip = gvConfigZip.getListaFileXsd();
		for(String nomeFile:listFileXsdZip){
			String inputFile = appoDir + File.separator + "xsds" + File.separator +nomeFile;
			File fin = new File(inputFile);
			String outputFile = gvDir + File.separator + "xsds"+ File.separator +nomeFile;
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}

		}
	}
	private void copiaFileJsd() {
		String appoDir =getAppoDir();
		String gvDir = getGvDir();
		List<String> listFileJsdZip = gvConfigZip.getListaFileJsd();
		for(String nomeFile:listFileJsdZip){
			String inputFile = appoDir + File.separator + "jsds" + File.separator +nomeFile;
			File fin = new File(inputFile);
			String outputFile = gvDir + File.separator + "jsds"+ File.separator +nomeFile;
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}

		}
	}
	public String getAppoDir() {
		String appoDir = null;
		try {
			appoDir = PropertiesHandler.expand("${{java.io.tmpdir}}", null) + File.separator + "conf";
		} catch (PropertiesHandlerException e) {
			// 
			e.printStackTrace();
		}
		return appoDir;
	}
	public String getGvDir() {
		String gvDir = null;
		try {
			gvDir = PropertiesHandler.expand("${{gv.app.home}}", null)+ File.separator + "xmlconfig";
		} catch (PropertiesHandlerException e) {
			// 
			e.printStackTrace();
		}
		return gvDir;
	}
	private void copiaFileWsdl() {
		String appoDir =getAppoDir();
		String gvDir = getGvDir();

		Map<String, Node> listaWs = gvConfigZip.getListaBusinessWebServices(); 
		String dirWsdlZip = gvConfigZip.geWsdlDir();
		String dirServiceZip = gvConfigZip.getWsServiceDir();
		String dirWsdlServer = gvConfigServer.geWsdlDir();
		String dirServiceServer = gvConfigServer.getWsServiceDir();

		for(String wsService:listaWs.keySet()){
			String inputFile = appoDir + File.separator + dirWsdlZip + File.separator +wsService+".wsdl";
			File fin = new File(inputFile);
			String outputFile = gvDir + File.separator + dirWsdlServer+ File.separator +wsService+".wsdl";
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}
			inputFile = appoDir + File.separator + dirServiceZip + File.separator +wsService+".aar";
			fin = new File(inputFile);
			outputFile = gvDir + File.separator + dirServiceServer+ File.separator +wsService+".aar";
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}

		}
	}
	private void copiaFileKeyStore() {
		String appoDir =getAppoDir();
		String gvDir = getGvDir();

		List<String> listaKeyStore= gvConfigZip.getListaFileKeyStore(); 

		for(String keyStore:listaKeyStore){
			String inputFile = appoDir + File.separator + "keystores" + File.separator +keyStore;
			File fin = new File(inputFile);
			String outputFile = gvDir + File.separator + "keystores"+ File.separator +keyStore;
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}
		}
	}
	public void scriviFile()
	{
		DOMWriter domWriter = new DOMWriter();
		FileOutputStream ostream;
		try {
			ostream = new FileOutputStream(XMLConfig.getURL("GVCore.xml").getFile());

			domWriter.write(gvConfigServer.getCoreXml(), ostream);
			ostream.flush();
			ostream.close();
			ostream = new FileOutputStream(XMLConfig.getURL("GVAdapters.xml").getFile());
			domWriter.write(gvConfigServer.getAdapterXml(), ostream);
			ostream.flush();
			ostream.close();
		} catch (FileNotFoundException e) {
			// 
			e.printStackTrace();
		} catch (XMLConfigException e) {
			// 
			e.printStackTrace();
		} catch (IOException e) {
			// 
			e.printStackTrace();
		}
	}
	public ByteArrayInputStream copyFileForBackupZip()
	{
		String tmpDir = null;
		String appoDir = null;
		String gvDir = null;
		String nomeZipDir="GVDeploy_"+ DateUtils.nowToString("yyyyMMddHHmmss");
		nomeZipFile= nomeZipDir + ".zip";
		try {
			tmpDir = PropertiesHandler.expand("${{java.io.tmpdir}}", null);        
			appoDir = tmpDir + File.separator + nomeZipDir;
			gvDir = PropertiesHandler.expand("${{gv.app.home}}", null);
			String xmlDir = File.separator + "xmlconfig";
			String xsdsDir = xmlDir + File.separator + "xsds";
			String wsdlDir = xmlDir + File.separator + "wsdl";
			String defDTEDir = File.separator + "gvdte";//+File.separator+"datasource";
			String dtdsDir = File.separator + "dtds";
			String birtDir = File.separator + "BIRTReportEngine";
			String bipelDir = File.separator + "BpelProcess";
			String rulesDir = File.separator + "Rules";
			String servicesDir = File.separator + "webservices" + File.separator + "services";
			String reportDir = birtDir + File.separator + "reports";
			String keyStoreDir = File.separator + "keystores";
			String bpelProcessDir = bipelDir;// + File.separator + "BpelProcess";

			FileUtils.forceMkdir(new File(appoDir));

			FileManager.cp(gvDir + xmlDir, appoDir + xmlDir, "^((GVCore)|(GVSupport)|(GVAdapters)|(GVVariables)|(gvesb-catalog))\\.xml$");
			FileManager.cp(gvDir + xmlDir, appoDir + xmlDir, "^GVEsb\\.jks$");

			FileManager.cp(gvDir + dtdsDir, appoDir + dtdsDir, ".*");
			FileManager.cp(gvDir + wsdlDir, appoDir + wsdlDir, ".*");
			FileManager.cp(gvDir + xsdsDir, appoDir + xsdsDir, ".*");
			FileManager.cp(gvDir + defDTEDir, appoDir + defDTEDir, ".*");

			FileManager.cp(gvDir + servicesDir, appoDir + servicesDir, ".*\\.aar");
			FileManager.cp(gvDir + reportDir, appoDir + reportDir, ".*");
			FileManager.cp(gvDir + keyStoreDir, appoDir + keyStoreDir, ".*");
			if ((new File(gvDir + bipelDir)).exists()) {
				FileManager.cp(gvDir+ bpelProcessDir, appoDir + bpelProcessDir, ".*");
			}
			if ((new File(gvDir + rulesDir)).exists()) {
				FileManager.cp(gvDir+ rulesDir, appoDir + rulesDir, ".*");
			}
			ZipHelper zh = new ZipHelper();
			zh.zipFile(appoDir, ".*", tmpDir, nomeZipFile);

			ByteArrayInputStream is = new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(tmpDir, nomeZipFile)));
			FileManager.rm(appoDir, null);
			return is;
		}
		catch (Exception exc) {
			logger.error("Failed configuration backup", exc);
			exc.printStackTrace();
		}
		return null;
	}

	private void copiaFile(String fileOrigine, String fileDestinazione)
	{
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			try {
				in = new FileInputStream(fileOrigine);
			}
			catch (Exception exc) {
				logger.error("File [" + fileOrigine + "] not found");
				return;
			}
			File fop = new File(PropertiesHandler.expand(fileDestinazione, null));
			FileUtils.forceMkdir(fop.getParentFile());
			out = new FileOutputStream(fop);
			IOUtils.copy(in, out);
		} catch (PropertiesHandlerException e) {
			// 
			e.printStackTrace();
		} catch (IOException e) {
			// 
			e.printStackTrace();
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// 
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// 
					e.printStackTrace();
				}
			}
		}
	}

	public void deleteFileZip() throws Exception
	{
		String tmpDir = PropertiesHandler.expand("${{java.io.tmpdir}}", null);
		FileManager.rm(tmpDir, nomeZipFile);
	}
	public InputStream readFileZip() throws Exception
	{
		String tmpDir = PropertiesHandler.expand("${{java.io.tmpdir}}", null);
		InputStream is = new FileInputStream(new File(tmpDir, nomeZipFile));
		return is;
	}
	public static void main(String[] args)
	{
		try {

			GVConfig gvConfigServer = new GVConfig("/Users/macbook/Desktop/DATI/GvServer-3.4.0.10/GreenV/xmlconfig/GVcore.xml","/Users/macbook/Desktop/DATI/GvServer-3.4.0.10/GreenV/xmlconfig/GVAdapters.xml");
			GVConfig gvConfigZip = new GVConfig("/Users/macbook/Desktop/GV/appo/conf/GVcore.xml","/Users/macbook/Desktop/GV/appo/conf/GVAdapters.xml"); 	
			//List<String> listaServizi = new ArrayList<String>();
			//listaServizi.add("PIPPO");

			GVDeploy deploy = new GVDeploy(gvConfigZip,gvConfigServer);
			deploy.save();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}

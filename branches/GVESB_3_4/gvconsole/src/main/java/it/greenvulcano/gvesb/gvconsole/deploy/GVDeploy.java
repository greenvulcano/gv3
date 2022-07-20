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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.file.FileManager;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.zip.ZipHelper;
import max.xml.DOMWriter;


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
	private String   nomeZipFile     = null;
	private GVConfig gvConfigZip     = null;
	private GVConfig gvConfigServer  = null;

	private static Logger  logger    = GVLogger.getLogger(GVDeploy.class);

	public GVConfig getZipGVConfig(){
		return this.gvConfigZip;
	}
	public GVConfig getServerGVConfig(){
		return this.gvConfigServer;
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
		this.gvConfigZip = new GVConfig("deploy", path + File.separator + "conf" + File.separator + "GVCore.xml",
				path + File.separator + "conf" + File.separator + "GVAdapters.xml");
		this.gvConfigServer = new GVConfig("server", XMLConfig.getURL("GVCore.xml"),XMLConfig.getURL("GVAdapters.xml"));
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
		return this.gvConfigServer.getListaServizi();
	}

	private void aggiornaGVCore()
	{
		List<String>  listaServizi = this.gvConfigZip.getListaServizi();

		long start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVCryptoHelper");
		Map<String, Node> mapListaCryptoHelperZip = this.gvConfigZip.getListaGVCryptoHelper();
		Map<String, Node> mapListaCryptoHelperServer = this.gvConfigServer.getListaGVCryptoHelper();
		for(String crytoHelper : mapListaCryptoHelperZip.keySet()){
			Node cryptoHelperNodeZip = mapListaCryptoHelperZip.get(crytoHelper);
			Node cryptoHelperNodeServer = mapListaCryptoHelperServer.get(crytoHelper);
			aggiornaNode(cryptoHelperNodeZip,cryptoHelperNodeServer,"/GVCore/GVCryptoHelper");
		}
		logger.info("END - Deploy GVCryptoHelper (" + (System.currentTimeMillis() - start) + ")");

		logger.info("BEGIN - Deploy GVXPath");
		Map<String, Node> mapListaNamespaceZip = this.gvConfigZip.getListaGVXPath();
		Map<String, Node> mapListaNamespaceServer = this.gvConfigServer.getListaGVXPath();
		for(String namespace : mapListaNamespaceZip.keySet()){
			Node namespaceNodeZip = mapListaNamespaceZip.get(namespace);
			Node namespaceNodeServer = mapListaNamespaceServer.get(namespace);
			aggiornaNode(namespaceNodeZip,namespaceNodeServer,"/GVCore/GVXPath/XPath");
		}
		logger.info("END - Deploy GVXPath (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVForwards");
		Map<String, Node> listaForwardZip = this.gvConfigZip.getListaForward(listaServizi);
		Map<String, Node> listaForwardServer = this.gvConfigServer.getListaForward(listaServizi);
		for(String servizio : listaForwardZip.keySet()){
			Node nodeForwaredZip=listaForwardZip.get(servizio);
			Node nodeForwardServer=listaForwardServer.get(servizio);
			aggiornaNode(nodeForwaredZip,nodeForwardServer,"/GVCore/GVForwards");
		}
		logger.info("END - Deploy GVForwards (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy Services");
		Map<String, Node> listaServiziZip = this.gvConfigZip.getListaNodeServizi(listaServizi);
		Map<String, Node> listaServiziServer = this.gvConfigServer.getListaNodeServizi(listaServizi);
		for(String servizio : listaServiziZip.keySet()){
			Node nodeServizioZip=listaServiziZip.get(servizio);
			Node nodeServizioServer=listaServiziServer.get(servizio);
			aggiornaNode(nodeServizioZip,nodeServizioServer,"/GVCore/GVServices/Services");
		}
		logger.info("END - Deploy Services (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy Groups");
		Map<String, Node> listaGruppiZip = this.gvConfigZip.getGroups(listaServiziZip);
		Map<String, Node> listaGruppiServer = this.gvConfigServer.getGroups(listaServiziServer);
		for(String servizio : listaGruppiZip.keySet()){
			Node nodeGruppoZip=listaGruppiZip.get(servizio);
			Node nodeGruppoServer=listaGruppiServer.get(servizio);
			aggiornaNode(nodeGruppoZip,nodeGruppoServer,"/GVCore/GVServices/Groups");
		}
		logger.info("END - Deploy Groups (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy Systems");
		Map<String, Node> mapListaSistemiZip = this.gvConfigZip.getListaSistemi(listaServizi);
		for (String sistema : mapListaSistemiZip.keySet()){
			Node nodeSistemaZip    = mapListaSistemiZip.get(sistema);
			Node nodeSistemaServer = this.gvConfigServer.getSistema(sistema);
			if(nodeSistemaServer == null){
				logger.debug("Deployed System " + sistema);
				aggiornaNode(nodeSistemaZip,nodeSistemaServer,"/GVCore/GVSystems/Systems");
			}else{
				Map<String, Node> mapListaChannelZip = this.gvConfigZip.getListaChannel(sistema);
				for (String canale : mapListaChannelZip.keySet()){
					Node nodeCanaleZip    = mapListaChannelZip.get(canale);
					Node nodeCanaleServer = this.gvConfigServer.getChannel(canale, sistema);
					if(nodeCanaleServer == null){
						logger.debug("Deployed System " + sistema + " Channel " + canale);
						aggiornaNode(nodeCanaleZip,nodeCanaleServer,"/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']");
					}else{
						Map<String, Node> mapListaVCLOpZip = this.gvConfigZip.getListaVCLOp(sistema,canale);
						for (String vclOp:mapListaVCLOpZip.keySet()){
							Node nodeVclOPZip    = mapListaVCLOpZip.get(vclOp);
							Node nodeVclOpServer = this.gvConfigServer.getVCLOp(vclOp, sistema, canale);
							if(nodeVclOpServer == null){
								logger.debug("Deployed System " + sistema + " Channel " + canale + " Operation " + nodeVclOPZip.getLocalName() + "[" + vclOp + "]");
								aggiornaNode(nodeVclOPZip,nodeVclOpServer,"/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']");
							}else{
								if(nodeVclOPZip.getLocalName().equals("dh-call")){
									Map<String, Node> mapListaDboBuilder = this.gvConfigZip.getListaDboBuilder(sistema,canale,vclOp);
									for (String dboBuilder:mapListaDboBuilder.keySet()){
										Node nodeDataHandlerZip = mapListaDboBuilder.get(dboBuilder);
										Node nodeDataHandlerServer = this.gvConfigServer.getDboBuilder(dboBuilder, sistema, canale, vclOp);
										logger.debug("Deployed System " + sistema + " Channel " + canale + " VCLOperation " + nodeVclOPZip.getLocalName() + "[" + vclOp + "] DBOBuilder " + dboBuilder);
										aggiornaNode(nodeDataHandlerZip,nodeDataHandlerServer,"/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']/dh-call[@name='"+vclOp+"']");
									}
								}else{
									logger.debug("Updated System " + sistema + " Channel " + canale + " VCLOperation " + nodeVclOPZip.getLocalName() + "[" + vclOp + "]");
									aggiornaNode(nodeVclOPZip,nodeVclOpServer,"/GVCore/GVSystems/Systems/System[@id-system='"+sistema+"']/Channel[@id-channel='"+canale+"']");
								}
							}
						}
					}
				}
			}
		}
		logger.info("END - Deploy Systems (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVPolicy/ACLGreenVulcano");
		Map<String, Node> listaAclGvZip = this.gvConfigZip.getListaAclGV(listaServizi);
		Map<String, Node> listaAclGvServer = this.gvConfigServer.getListaAclGV(listaServizi);
		for(String key:listaAclGvZip.keySet()){
			Node nodeAclGvZip=listaAclGvZip.get(key);
			Node nodeAclGvServer=listaAclGvServer.get(key);
			aggiornaNode(nodeAclGvZip,nodeAclGvServer,"/GVCore/GVPolicy/ACLGreenVulcano");
		}
		logger.info("END - Deploy GVPolicy/ACLGreenVulcano (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVPolicy/Roles");
		Map<String, Node> listaRoleRefZip = this.gvConfigZip.getListaRoleRef(listaServizi);
		for(String key:listaRoleRefZip.keySet()){
			Node nodeRoleRefZip=listaRoleRefZip.get(key);
			Node nodeRoleRefServer=this.gvConfigServer.getRoleRef(key);
			aggiornaNode(nodeRoleRefZip,nodeRoleRefServer,"/GVCore/GVPolicy/Roles");
		}
		logger.info("END - Deploy GVPolicy/Roles (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVPolicy/Addresses");
		Map<String, Node> listaAddressSetZip = this.gvConfigZip.getListaAddressSet(listaServizi);
		for(String key:listaAddressSetZip.keySet()){
			Node nodeAddressSetZip=listaAddressSetZip.get(key);
			Node nodeAddressSetServer=this.gvConfigServer.getAddressSet(key);
			aggiornaNode(nodeAddressSetZip,nodeAddressSetServer,"/GVCore/GVPolicy/Addresses");
		}
		logger.info("END - Deploy GVPolicy/Addresses (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVBufferDump");
		Map<String, Node> listaGVBufferDumpZip = this.gvConfigZip.getListaGVBufferDump(listaServizi);
		Map<String, Node> listaGVBufferDumpServer = this.gvConfigServer.getListaGVBufferDump(listaServizi);
		for(String gvBufferDump:listaGVBufferDumpZip.keySet()){
			Node nodeGVBufferDumpZip=listaGVBufferDumpZip.get(gvBufferDump);
			Node nodeGVBufferDumpServer=listaGVBufferDumpServer.get(gvBufferDump);
			aggiornaNode(nodeGVBufferDumpZip,nodeGVBufferDumpServer,"/GVCore/GVBufferDump");
		}
		logger.info("END - Deploy GVBufferDump (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy Transformations");
		Map<String, Node> listaTrasformazioniZip = this.gvConfigZip.getListaTrasformazioni(listaServiziZip);
		for(String trasformazione:listaTrasformazioniZip.keySet()){
			Node nodeTrasformazioneZip=listaTrasformazioniZip.get(trasformazione);
			Node nodeTrasformszionServer=this.gvConfigServer.getTrasformazione(trasformazione);
			aggiornaNode(nodeTrasformazioneZip,nodeTrasformszionServer,"/GVCore/GVDataTransformation/Transformations");
		}
		logger.info("END - Deploy Transformations (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy DataSourceSets");
		Map<String, Node> listaDataSourceZip = this.gvConfigZip.getListaDataSource(listaTrasformazioniZip);
		for(String dataSource:listaDataSourceZip.keySet()){
			Node nodeDataSourceZip=listaDataSourceZip.get(dataSource);
			Node nodeDataSourceServer=this.gvConfigServer.getDataSource(dataSource);
			aggiornaNode(nodeDataSourceZip,nodeDataSourceServer,"/GVCore/GVDataTransformation/DataSourceSets");
		}
		logger.info("END - Deploy DataSourceSets (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVTaskManagerConfiguration");
		Map<String, Node> listaTaskZip = this.gvConfigZip.getListaTask(listaServizi);
		Map<String, Node> listaTaskServer = this.gvConfigServer.getListaTask(listaServizi);
		for(String task:listaTaskZip.keySet()){
			Node nodeTaskZip=listaTaskZip.get(task);
			Node nodeTaskServer=listaTaskServer.get(task);
			aggiornaNode(nodeTaskZip,nodeTaskServer,"/GVCore/GVTaskManagerConfiguration/TaskGroups");
		}
		logger.info("END - Deploy GVTaskManagerConfiguration (" + (System.currentTimeMillis() - start) + ")");
	}

	private void aggiornaGVAdapters()
	{
		long start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GreenVulcanoWebServices");
		Map<String, Node> mapListaGvWsZip = this.gvConfigZip.getListaGvWebServices();
		Map<String, Node> mapListaGvWsServer = this.gvConfigServer.getListaGvWebServices();
		for(String gvWs:mapListaGvWsZip.keySet()){
			Node nodeGvWsZip = mapListaGvWsZip.get(gvWs);
			Node nodeGvWsServer = mapListaGvWsServer.get(gvWs);
			aggiornaNodeAdapter(nodeGvWsZip,nodeGvWsServer,"/GVAdapters/GVWebServices/GreenVulcanoWebServices");
		}
		logger.info("END - Deploy GreenVulcanoWebServices (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy BusinessWebServices");
		Map<String, Node> mapListaBusinessWsZip = this.gvConfigZip.getListaBusinessWebServices();
		Map<String, Node> mapListaBusinessWsServer = this.gvConfigServer.getListaBusinessWebServices();
		for(String businessWs:mapListaBusinessWsZip.keySet()){
			Node nodeBusinessWsZip = mapListaBusinessWsZip.get(businessWs);
			Node nodeBusinessWsServer = mapListaBusinessWsServer.get(businessWs);
			aggiornaNodeAdapter(nodeBusinessWsZip,nodeBusinessWsServer,"/GVAdapters/GVWebServices/BusinessWebServices");
		}
		logger.info("END - Deploy BusinessWebServices (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVJDBCConnectionBuilder");
		Map<String, Node> mapListaConnBuilderZip = this.gvConfigZip.getListConnBuilder();
		Map<String, Node> mapListaConnBuilderServer = this.gvConfigServer.getListConnBuilder();
		for(String connBuilder:mapListaConnBuilderZip.keySet()){
			Node connBuilderNodeZip = mapListaConnBuilderZip.get(connBuilder);
			Node connBuilderNodeServer = mapListaConnBuilderServer.get(connBuilder);
			aggiornaNodeAdapter(connBuilderNodeZip,connBuilderNodeServer,"/GVAdapters/GVJDBCConnectionBuilder");
		}
		logger.info("END - Deploy GVJDBCConnectionBuilder (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVDataProviderManager");
		Map<String, Node> mapListaDataProviderZip = this.gvConfigZip.getListDataProvider();
		Map<String, Node> mapListaDataProviderServer = this.gvConfigServer.getListDataProvider();
		for(String dataProvider:mapListaDataProviderZip.keySet()){
			Node dataProviderNodeZip = mapListaDataProviderZip.get(dataProvider);
			Node dataProviderNodeServer = mapListaDataProviderServer.get(dataProvider);
			aggiornaNodeAdapter(dataProviderNodeZip,dataProviderNodeServer,"/GVAdapters/GVDataProviderManager/DataProviders");
		}
		logger.info("END - Deploy GVDataProviderManager (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVAdapterHttpConfiguration");
		Map<String,Node> listActionMappingZip = this.gvConfigZip.getListActionMapping();
		Map<String,Node> listActionMappingServer = this.gvConfigServer.getListActionMapping();
		for(String action : listActionMappingZip.keySet()){
			Node nodeActionMappingZip    = listActionMappingZip.get(action);
			Node nodeActionMappingServer = listActionMappingServer.get(action);
			if (nodeActionMappingServer == null) {
				aggiornaNodeAdapter(nodeActionMappingZip, nodeActionMappingServer, "/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/ActionMappings");
			}
			else {
				Map<String, Node> listaRestActionMappingZip = this.gvConfigZip.getListaRestActionMapping(action);
				Map<String, Node> listaRestActionMappingServer = this.gvConfigServer.getListaRestActionMapping(action);
				for(String rest:listaRestActionMappingZip.keySet()){
					Node nodeRestActionMappingZip=listaRestActionMappingZip.get(rest);
					Node nodeRestActionMappingServer=listaRestActionMappingServer.get(rest);
					aggiornaNodeAdapter(nodeRestActionMappingZip,nodeRestActionMappingServer,"/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/ActionMappings/RESTActionMapping[@Action='"+action+"']/OperationMappings");
				}

			}
		}
		logger.info("END - Deploy GVAdapterHttpConfiguration (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVExcelWorkbookConfiguration");
		Map<String, Node> mapListaExcelWorkbookZip = this.gvConfigZip.getListExcelWorkBook();
		Map<String, Node> mapListaExcelWorkbookServer = this.gvConfigServer.getListExcelWorkBook();
		for(String excelWorkbook:mapListaExcelWorkbookZip.keySet()){
			Node excelWorkbookNodeZip = mapListaExcelWorkbookZip.get(excelWorkbook);
			Node excelWorkbookNodeServer = mapListaExcelWorkbookServer.get(excelWorkbook);
			aggiornaNodeAdapter(excelWorkbookNodeZip,excelWorkbookNodeServer,"/GVAdapters/GVExcelWorkbookConfiguration");
		}
		logger.info("END - Deploy GVExcelWorkbookConfiguration (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVExcelCreatorConfiguration");
		Map<String, Node> mapListaExcelReportZip = this.gvConfigZip.getListExcelReport();
		Map<String, Node> mapListaExcelReportServer = this.gvConfigServer.getListExcelReport();
		for(String excelReport:mapListaExcelReportZip.keySet()){
			Node excelReportNodeZip = mapListaExcelReportZip.get(excelReport);
			Node excelReportNodeServer = mapListaExcelReportServer.get(excelReport);
			// HANDLE GROUP DEPLOY
			aggiornaNodeAdapter(excelReportNodeZip,excelReportNodeServer,"/GVAdapters/GVExcelCreatorConfiguration");
		}
		logger.info("END - Deploy GVExcelCreatorConfiguration (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVBIRTReportConfiguration");
		Map<String, Node> mapListaBIRTReportGrpZip = this.gvConfigZip.getListBIRTReportGroup();
		Map<String, Node> mapListaBIRTReportGrpServer = this.gvConfigServer.getListBIRTReportGroup();
		for(String birtReportGrp:mapListaBIRTReportGrpZip.keySet()){
			Node birtReportGrpNodeZip = mapListaBIRTReportGrpZip.get(birtReportGrp);
			Node birtReportGrpNodeServer = mapListaBIRTReportGrpServer.get(birtReportGrp);
			if (birtReportGrpNodeServer == null) {
				aggiornaNodeAdapter(birtReportGrpNodeZip,birtReportGrpNodeServer,"/GVAdapters/GVBIRTReportConfiguration/ReportGroups");
			}
			else {
				Map<String, Node> mapListaBIRTReportZip = this.gvConfigZip.getListBIRTReport(birtReportGrp);
				Map<String, Node> mapListaBIRTReportServer = this.gvConfigServer.getListBIRTReport(birtReportGrp);
				for(String birtReport:mapListaBIRTReportZip.keySet()){
					Node birtReportNodeZip = mapListaBIRTReportZip.get(birtReport);
					Node birtReportNodeServer = mapListaBIRTReportServer.get(birtReport);
					aggiornaNodeAdapter(birtReportNodeZip,birtReportNodeServer,"/GVAdapters/GVBIRTReportConfiguration/ReportGroups/ReportGroup[@name='" + birtReportGrp + "']");
				}
			}
		}
		logger.info("END - Deploy GVBIRTReportConfiguration (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy RSHServiceClientConfiguration");
		Map<String, Node> mapListaRSHClientZip = this.gvConfigZip.getListRSHClient();
		Map<String, Node> mapListaRSHClientServer = this.gvConfigServer.getListRSHClient();
		for(String rshClient:mapListaRSHClientZip.keySet()){
			Node rshClientNodeZip = mapListaRSHClientZip.get(rshClient);
			Node rshClientNodeServer = mapListaRSHClientServer.get(rshClient);
			aggiornaNodeAdapter(rshClientNodeZip,rshClientNodeServer,"/GVAdapters/RSHServiceClientConfiguration");
		}
		logger.info("END - Deploy RSHServiceClientConfiguration (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVRulesConfigManager");
		Map<String, Node> mapListaKnowledgeBaseZip = this.gvConfigZip.getListKnowledgeBase();
		Map<String, Node> mapListaKnowledgeBaseServer = this.gvConfigServer.getListKnowledgeBase();
		for(String knowledgeBase:mapListaKnowledgeBaseZip.keySet()){
			Node knowledgeBaseNodeZip = mapListaKnowledgeBaseZip.get(knowledgeBase);
			Node knowledgeBaseNodeServer = mapListaKnowledgeBaseServer.get(knowledgeBase);
			aggiornaNodeAdapter(knowledgeBaseNodeZip,knowledgeBaseNodeServer,"/GVAdapters/GVRulesConfigManager");
		}
		logger.info("END - Deploy GVRulesConfigManager (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVHL7ListenerManager");
		Map<String, Node> mapListaHL7ListenerZip = this.gvConfigZip.getListHL7Listener();
		Map<String, Node> mapListaHL7ListenerServer = this.gvConfigServer.getListHL7Listener();
		for(String hl7Listener:mapListaHL7ListenerZip.keySet()){
			Node hl7ListenerNodeZip = mapListaHL7ListenerZip.get(hl7Listener);
			Node hl7ListenerNodeServer = mapListaHL7ListenerServer.get(hl7Listener);
			if (hl7ListenerNodeServer == null) {
				aggiornaNodeAdapter(hl7ListenerNodeZip,hl7ListenerNodeServer,"/GVAdapters/GVHL7ListenerManager/HL7Listeners");
			}
			else {
				Map<String, Node> mapListaHL7ApplicationZip = this.gvConfigZip.getListHL7Application(hl7Listener);
				Map<String, Node> mapListaHL7ApplicationServer = this.gvConfigServer.getListHL7Application(hl7Listener);
				for(String hl7Application:mapListaHL7ApplicationZip.keySet()){
					Node hl7ApplicationNodeZip = mapListaHL7ApplicationZip.get(hl7Application);
					Node hl7ApplicationNodeServer = mapListaHL7ApplicationServer.get(hl7Application);
					aggiornaNodeAdapter(hl7ApplicationNodeZip,hl7ApplicationNodeServer,"/GVAdapters/GVHL7ListenerManager/HL7Listeners/HL7Listener[@name='" + hl7Listener + "']/HL7Applications");
				}
			}
		}
		logger.info("END - Deploy GVHL7ListenerManager (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVSocialAdapterManager");
		Map<String, Node> mapListaSocialAdpZip = this.gvConfigZip.getListSocialAdp();
		Map<String, Node> mapListaSocialAdpServer = this.gvConfigServer.getListSocialAdp();
		for(String socialAdp:mapListaSocialAdpZip.keySet()){
			Node socialAdpNodeZip = mapListaSocialAdpZip.get(socialAdp);
			Node socialAdpNodeServer = mapListaSocialAdpServer.get(socialAdp);
			if (socialAdpNodeServer == null) {
				aggiornaNodeAdapter(socialAdpNodeZip,socialAdpNodeServer,"/GVAdapters/GVSocialAdapterManager/SocialAdapters");
			}
			else {
				Map<String, Node> mapListaSocialAdpAccountsZip = this.gvConfigZip.getListSocialAdpAccounts(socialAdp);
				Map<String, Node> mapListaSocialAdpAccountsServer = this.gvConfigServer.getListSocialAdpAccounts(socialAdp);
				for(String saAccount:mapListaSocialAdpAccountsZip.keySet()){
					Node saAccountNodeZip = mapListaSocialAdpAccountsZip.get(saAccount);
					Node saAccountNodeServer = mapListaSocialAdpAccountsServer.get(saAccount);
					aggiornaNodeAdapter(saAccountNodeZip,saAccountNodeServer,"/GVAdapters/GVSocialAdapterManager/SocialAdapters/*[@social='" + socialAdp + "']/Accounts");
				}
			}
		}
		logger.info("END - Deploy GVSocialAdapterManager (" + (System.currentTimeMillis() - start) + ")");

		start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy GVPushNotificationManager");
		Map<String, Node> mapListaPushNotificationZip = this.gvConfigZip.getListPushNotification();
		Map<String, Node> mapListaPushNotificationServer = this.gvConfigServer.getListPushNotification();
		for(String rshClient:mapListaPushNotificationZip.keySet()){
			Node pushNotificationNodeZip = mapListaPushNotificationZip.get(rshClient);
			Node pushNotificationNodeServer = mapListaPushNotificationServer.get(rshClient);
			aggiornaNodeAdapter(pushNotificationNodeZip,pushNotificationNodeServer,"/GVAdapters/GVPushNotificationManager/NotificationEngines");
		}
		logger.info("END - Deploy GVPushNotificationManager (" + (System.currentTimeMillis() - start) + ")");
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
				Node base = this.gvConfigServer.getNodeCore(baseXpath);
				if(base!=null){
				  if (nodeServer != null) {
					Node importedNode = base.getOwnerDocument().importNode(nodeZip, true);
					base.replaceChild(importedNode, nodeServer);
					logger.debug("Node Core " + nodeInfo(importedNode) + " already exists on " + baseXpath + ", updating...");
				  }
				  else {
					Node importedNode = base.getOwnerDocument().importNode(nodeZip, true);
					base.appendChild(importedNode);
					logger.debug("Node Core " + nodeInfo(importedNode) + " doesn't exist on " + baseXpath + ", inserting...");
				  }
				}
			}
		} catch (Exception e) {
			//
			logger.error(e);
		}
	}

	private void aggiornaNodeAdapter(Node nodeZip,Node nodeServer,String baseXpath)
	{
		XmlDiff xmlDiff = new XmlDiff();
		boolean isDiff = true;
		try {
			if (nodeServer != null){
				List<String> listDiff = xmlDiff.compareXML(nodeZip, nodeServer);
				isDiff = listDiff.size() > 0;
			}
			if (isDiff){
				Node base = this.gvConfigServer.getNodeAdapter(baseXpath);
				if (base != null) {
				  if (nodeServer != null) {
					Node importedNode = base.getOwnerDocument().importNode(nodeZip, true);
					base.replaceChild(importedNode, nodeServer);
					logger.debug("Node Adapter " + nodeInfo(importedNode) + " already exists on " + baseXpath + ", updating...");
				  }
				  else {
					Node importedNode = base.getOwnerDocument().importNode(nodeZip, true);
					base.appendChild(importedNode);
					logger.debug("Node Adapter " + nodeInfo(importedNode) + " doesn't exist on " + baseXpath + ", inserting...");
				  }
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private String nodeInfo(Node node) {
		StringBuilder sb = new StringBuilder(node.getLocalName());
		NamedNodeMap nnm = node.getAttributes();
		if ((nnm != null) && (nnm.getLength() > 0)) {
			sb.append("{");
			for (int i = 0; i < nnm.getLength(); i++) {
				if (i > 0) {
					sb.append("; ");
				}
				Node n = nnm.item(i);
				sb.append(n.getLocalName()).append("=").append(n.getNodeValue());
			}
			sb.append("}");
		}
		return sb.toString();
	}

	public void save()
	{
		long start = System.currentTimeMillis();
		logger.info("BEGIN - Saving Deployed Configuration");
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
		copiaFileKB();
		copiaFileBIRT();
		copiaFileBipel();
		logger.info("END - Saving Deployed Configuration (" + (System.currentTimeMillis() - start) + ")");
	}

	private void copiaFileGvCore() {
		try {
			this.gvConfigServer.writeGvCore(XMLConfig.getURL("GVCore.xml"));
		} catch (XMLConfigException e) {
			logger.error(e);
		}
	}

	private void copiaFileGvAdapter(){
		try {
			this.gvConfigServer.writeGvAdapters(XMLConfig.getURL("GVAdapters.xml"));
		} catch (XMLConfigException e) {
			logger.error(e);
		}
	}

	private void copiaFileXsl() {
		long start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy File Xsl");

		Map<String, String> dirsZip = new HashMap<String, String>();
		Map<String, String> dirsGV = new HashMap<String, String>();
		String appoDir = getAppoDir();
		String gvDir = getGvDir();
		Map<String, String> listFileXslZip = this.gvConfigZip.getListaFileXsl();
		for(String key : listFileXslZip.keySet()){
			String nomeFile = listFileXslZip.get(key);
			String dataSourceName = key.split("#")[1]; //this.gvConfigZip.getDataSourceValueFromTrasf(key);
			String dirDteZip = dirsZip.computeIfAbsent(dataSourceName, k1 -> this.gvConfigZip.getDteDir(k1,"xsl"));
			String inputFile = appoDir + File.separator + dirDteZip + File.separator +nomeFile;
			File fin = new File(inputFile);
			dataSourceName = this.gvConfigServer.getDataSourceValueFromTrasf(key);
			String dirDteServer = dirsGV.computeIfAbsent(dataSourceName, k1 -> this.gvConfigServer.getDteDir(k1,"xsl"));
			String outputFile = gvDir + File.separator + dirDteServer+ File.separator +nomeFile;
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}
		}
		logger.info("END - Deploy File Xsl (" + (System.currentTimeMillis() - start) + ")");
	}

	private void copiaFileXq() {
		long start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy File Xq");

		Map<String, String> dirsZip = new HashMap<String, String>();
		Map<String, String> dirsGV = new HashMap<String, String>();
		String appoDir = getAppoDir();
		String gvDir = getGvDir();
		Map<String, String> listFileXqZip = this.gvConfigZip.getListaFileXq();
		for(String key:listFileXqZip.keySet()){
			String nomeFile = listFileXqZip.get(key);
			String dataSourceName = key.split("#")[1]; //this.gvConfigZip.getDataSourceValueFromTrasf(key);
			String dirDteZip = dirsZip.computeIfAbsent(dataSourceName, k1 -> this.gvConfigZip.getDteDir(k1,"xq"));
			String inputFile = appoDir + File.separator + dirDteZip + File.separator +nomeFile;
			File fin = new File(inputFile);
			dataSourceName = this.gvConfigServer.getDataSourceValueFromTrasf(key);
			String dirDteServer = dirsGV.computeIfAbsent(dataSourceName, k1 -> this.gvConfigServer.getDteDir(k1,"xq"));
			String outputFile = gvDir + File.separator + dirDteServer+ File.separator +nomeFile;
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}
		}
		logger.info("END - Deploy File Xq (" + (System.currentTimeMillis() - start) + ")");
	}

	private void copiaFileBin(){
		long start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy File Bin");

		Map<String, String> dirsZip = new HashMap<String, String>();
		Map<String, String> dirsGV = new HashMap<String, String>();
		String appoDir = getAppoDir();
		String gvDir = getGvDir();
		Map<String, String> listFileBinZip = this.gvConfigZip.getListaFileBin();
		for(String key:listFileBinZip.keySet()){
			String nomeFile = listFileBinZip.get(key);
			String dataSourceName = key.split("#")[1]; //this.gvConfigZip.getDataSourceValueFromTrasf(key);
			String dirDteZip = dirsZip.computeIfAbsent(dataSourceName, k1 -> this.gvConfigZip.getDteDir(k1,"bin"));
			String inputFile = appoDir + File.separator + dirDteZip + File.separator +nomeFile;
			File fin = new File(inputFile);
			dataSourceName = this.gvConfigServer.getDataSourceValueFromTrasf(key);
			String dirDteServer = dirsGV.computeIfAbsent(dataSourceName, k1 -> this.gvConfigServer.getDteDir(k1,"bin"));
			String outputFile = gvDir + File.separator + dirDteServer+ File.separator +nomeFile;
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}
		}
		logger.info("END - Deploy File Bin (" + (System.currentTimeMillis() - start) + ")");
	}

	private void copiaFileXsd() {
		long start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy File Xsd");

		String appoDir = getAppoDir();
		String gvDir = getGvDir()+ File.separator + "xmlconfig";
		List<String> listFileXsdZip = this.gvConfigZip.getListaFileXsd();
		for(String nomeFile:listFileXsdZip){
			String inputFile = appoDir + File.separator + "xsds" + File.separator +nomeFile;
			File fin = new File(inputFile);
			String outputFile = gvDir + File.separator + "xsds"+ File.separator +nomeFile;
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}
		}
		logger.info("END - Deploy File Xsd (" + (System.currentTimeMillis() - start) + ")");
	}

	private void copiaFileJsd() {
		long start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy File Jsd");

		String appoDir = getAppoDir();
		String gvDir = getGvDir()+ File.separator + "xmlconfig";
		List<String> listFileJsdZip = this.gvConfigZip.getListaFileJsd();
		for(String nomeFile:listFileJsdZip){
			String inputFile = appoDir + File.separator + "jsds" + File.separator +nomeFile;
			File fin = new File(inputFile);
			String outputFile = gvDir + File.separator + "jsds"+ File.separator +nomeFile;
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}
		}
		logger.info("END - Deploy File Jsd (" + (System.currentTimeMillis() - start) + ")");
	}

	private void copiaFileKB() {
		long start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy File KB");

		String appoDir =getAppoDir() + File.separator + "Rules";
		String gvDir = getGvDir() + File.separator + "Rules";
		if ((new File(appoDir)).exists()) {
			try {
				FileManager.cp(appoDir, gvDir, ".*");
			}
			catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		/*List<String> listFileDSLZip = this.gvConfigZip.getListaFileKB();
		for(String nomeFile:listFileDSLZip){
			String inputFile = appoDir + File.separator + "Rules" + File.separator +nomeFile;
			File fin = new File(inputFile);
			String outputFile = gvDir + File.separator + "Rules"+ File.separator +nomeFile;
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}
		}*/
		logger.info("END - Deploy File KB (" + (System.currentTimeMillis() - start) + ")");
	}

	private void copiaFileBIRT() {
		long start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy File BIRT");

		String appoDir =getAppoDir() + File.separator + "reports";
		String gvDir = getGvDir() + File.separator + "BIRTReportEngine" + File.separator + "reports";
		if ((new File(appoDir)).exists()) {
			try {
				FileManager.cp(appoDir, gvDir, ".*");
			}
			catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		/*List<String> listFileDSLZip = this.gvConfigZip.getListaFileKB();
		for(String nomeFile:listFileDSLZip){
			String inputFile = appoDir + File.separator + "Rules" + File.separator +nomeFile;
			File fin = new File(inputFile);
			String outputFile = gvDir + File.separator + "Rules"+ File.separator +nomeFile;
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}
		}*/
		logger.info("END - Deploy File BIRT (" + (System.currentTimeMillis() - start) + ")");
	}

	private void copiaFileBipel() {
		long start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy File Bipel");

		String appoDir =getAppoDir() + File.separator + "BpelProcess";
		String gvDir = getGvDir() + File.separator + "BpelProcess";
		if ((new File(appoDir)).exists()) {
			try {
				FileManager.cp(appoDir, gvDir, ".*");
			}
			catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		/*List<String> listFileDSLZip = this.gvConfigZip.getListaFileKB();
		for(String nomeFile:listFileDSLZip){
			String inputFile = appoDir + File.separator + "Rules" + File.separator +nomeFile;
			File fin = new File(inputFile);
			String outputFile = gvDir + File.separator + "Rules"+ File.separator +nomeFile;
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}
		}*/
		logger.info("END - Deploy File Bipel (" + (System.currentTimeMillis() - start) + ")");
	}

	public String getAppoDir() {
		String appoDir = null;
		try {
			appoDir = PropertiesHandler.expand("${{java.io.tmpdir}}", null) + File.separator + "conf";
		} catch (PropertiesHandlerException e) {
			logger.error(e);
		}
		return appoDir;
	}

	public String getGvDir() {
		String gvDir = null;
		try {
			gvDir = PropertiesHandler.expand("${{gv.app.home}}", null); //+ File.separator + "xmlconfig";
		} catch (PropertiesHandlerException e) {
			logger.error(e);
		}
		return gvDir;
	}

	private void copiaFileWsdl() {
		long start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy File Wsdl");

		String appoDir = getAppoDir();
		String gvDir = getGvDir()+ File.separator + "xmlconfig";

		Map<String, Node> listaWs = this.gvConfigZip.getListaBusinessWebServices();
		String dirWsdlZip = "wsdl"; //this.gvConfigZip.geWsdlDir();
		String dirServiceZip = "service"; //this.gvConfigZip.getWsServiceDir();
		String dirWsdlServer = this.gvConfigServer.geWsdlDir();
		String dirServiceServer = this.gvConfigServer.getWsServiceDir();

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
		logger.info("END - Deploy File Wsdl (" + (System.currentTimeMillis() - start) + ")");
	}

	private void copiaFileKeyStore() {
		long start = System.currentTimeMillis();
		logger.info("BEGIN - Deploy File Keystore");

		String appoDir = getAppoDir() + File.separator + "keystores";
		String gvDir = getGvDir() + File.separator + "keystores";
		if ((new File(appoDir)).exists()) {
			try {
				FileManager.cp(appoDir, gvDir, ".*");
			}
			catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		/*String appoDir =getAppoDir();
		String gvDir = getGvDir();

		List<String> listaKeyStore= this.gvConfigZip.getListaFileKeyStore();

		for(String keyStore:listaKeyStore){
			String inputFile = appoDir + File.separator + "keystores" + File.separator +keyStore;
			File fin = new File(inputFile);
			String outputFile = gvDir + File.separator + "keystores"+ File.separator +keyStore;
			if (fin.exists()) {
				copiaFile(inputFile,outputFile);
			}
		}*/
		logger.info("END - Deploy File Keystore (" + (System.currentTimeMillis() - start) + ")");
	}

	public void scriviFile()
	{
		DOMWriter domWriter = new DOMWriter();
		FileOutputStream ostream;
		try {
			ostream = new FileOutputStream(XMLConfig.getURL("GVCore.xml").getFile());

			domWriter.write(this.gvConfigServer.getCoreXml(), ostream);
			ostream.flush();
			ostream.close();
			ostream = new FileOutputStream(XMLConfig.getURL("GVAdapters.xml").getFile());
			domWriter.write(this.gvConfigServer.getAdapterXml(), ostream);
			ostream.flush();
			ostream.close();
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (XMLConfigException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public ByteArrayInputStream copyFileForBackupZip()
	{
		String tmpDir = null;
		String appoDir = null;
		String gvDir = null;
		String nomeZipDir="GVDeploy_"+ DateUtils.nowToString("yyyyMMddHHmmss");
		this.nomeZipFile= nomeZipDir + ".zip";
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
			zh.zipFile(appoDir, ".*", tmpDir, this.nomeZipFile);

			ByteArrayInputStream is = new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(tmpDir, this.nomeZipFile)));
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
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}

	public void deleteFileZip() throws Exception
	{
		String tmpDir = PropertiesHandler.expand("${{java.io.tmpdir}}", null);
		FileManager.rm(tmpDir, this.nomeZipFile);
	}

	public InputStream readFileZip() throws Exception
	{
		String tmpDir = PropertiesHandler.expand("${{java.io.tmpdir}}", null);
		InputStream is = new FileInputStream(new File(tmpDir, this.nomeZipFile));
		return is;
	}

}

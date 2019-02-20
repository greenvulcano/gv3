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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.file.FileManager;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;
import it.greenvulcano.util.zip.ZipHelper;

/**
 *
 * GVCoreParser class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GVConfigZipFile
{

	private static Logger  logger    = GVLogger.getLogger(GVConfigZipFile.class);
	GVConfig gvConfig = null;

	public GVConfigZipFile(GVConfig gvConfig)
	{
		this.gvConfig=gvConfig;
	}

	public ByteArrayInputStream creaFileZip(List<String> listaServizi)
	{
		ByteArrayInputStream is = new ByteArrayInputStream(new byte[0]);
		String tmpDir = null;
		String appoDir = null;
		String gvDir = null;
		try {
			tmpDir = PropertiesHandler.expand("${{java.io.tmpdir}}", null);
			appoDir = tmpDir + File.separator + "conf";
			gvDir = PropertiesHandler.expand("${{gv.app.home}}", null);
			String xmlDir = gvDir + File.separator + "xmlconfig";
			String dtdsDir = gvDir + File.separator + "dtds";
			String birtDir = gvDir + File.separator + "BIRTReportEngine";
			String bipelDir = gvDir + File.separator + "BpelProcess";
			String rulesDir = gvDir + File.separator + "Rules";

			try {
				FileUtils.deleteDirectory(new File(appoDir));
				FileUtils.forceMkdir(new File(appoDir));
			} catch (IOException e) {

				e.printStackTrace();
			}
			//FileManager.cp(xmlDir, appoDir, "^GVEsb\\.jks$");
			try {
			  FileManager.cp(xmlDir, appoDir, "^((GVSupport)|(GVVariables)|(gvesb-catalog))\\.xml$");
			  FileManager.cp(xmlDir, appoDir, "^GVEsb\\.jks$");
			  FileManager.cp(dtdsDir, appoDir + File.separator + "dtds", ".*");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String filenameCore = appoDir+File.separator+"GVCore.xml";
			File fileCore = new File(filenameCore);
			String gvcore = this.gvConfig.getGvCore(listaServizi,true);
			Node gvCoreDoc = XMLUtils.parseObject_S(gvcore, false, false);
			try {
				FileUtils.writeStringToFile(fileCore, gvcore);
			} catch (IOException e1) {

				e1.printStackTrace();
			}

			String filenameAdapter = appoDir+File.separator+"GVAdapters.xml";
			File fileAdapter = new File(filenameAdapter);
			String gvadapter = this.gvConfig.getGvAdapters(listaServizi,true);
			Node gvAdaptersDoc = XMLUtils.parseObject_S(gvadapter, false, false);
			try {
				FileUtils.writeStringToFile(fileAdapter, gvadapter);
			} catch (IOException e1) {

				e1.printStackTrace();
			}

			try {
				FileUtils.forceMkdir(new File(appoDir+File.separator+"keystores"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			List<String> listaKeyStore = this.gvConfig.getListaFileKeyStore(listaServizi);
			for(String keyStore:listaKeyStore){
				try {
					FileManager.cp(gvDir + File.separator + "keystores", appoDir + File.separator + "keystores", keyStore);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			String dirXsd = "xsds";
			try {
				FileUtils.forceMkdir(new File(appoDir+File.separator+dirXsd));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			List<String> listaFileXsd = this.gvConfig.getListaFileXsd(listaServizi);
			for(String fileXsd:listaFileXsd){
				dirXsd = "xsds";
				String[] listDir = fileXsd.split("/");
				for(int i=0;i<(listDir.length-1);i++){
					dirXsd=dirXsd+File.separator+listDir[i];
					try {
						FileUtils.forceMkdir(new File(appoDir+File.separator+dirXsd));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(listDir.length>0) {
					fileXsd = listDir[listDir.length-1];
				}
				try {
					FileManager.cp(xmlDir + File.separator + dirXsd, appoDir + File.separator + dirXsd, fileXsd);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			//FileManager.cp(xmlDir + File.separator + "xsds", appoDir + File.separator + "xsds", ".*");

			try {
				FileManager.cp(xmlDir + File.separator + "jsds", appoDir + File.separator + "jsds", ".*");
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			Map<String,String> listFile = this.gvConfig.getListaFileXsl(listaServizi);
			for(String key:listFile.keySet()){
				String nomeFile = listFile.get(key);
				Node dataSource = this.gvConfig.getDataSourceFromTrasf(key);
				String xpath="LocalFSDataSource[@formatHandled='xsl']/@repositoryHome";
				String dirDTE = XMLUtils.get_S(dataSource, xpath);
				dirDTE = dirDTE.replace("${{gv.app.home}}", "");
				try {
					FileUtils.forceMkdir(new File(appoDir+File.separator+dirDTE));
				} catch (IOException e) {
					e.printStackTrace();
				}
				String[] listDir = nomeFile.split("/");
				for(int i=0;i<(listDir.length-1);i++){
					dirDTE=dirDTE+File.separator+listDir[i];
					try {
						FileUtils.forceMkdir(new File(appoDir+File.separator+dirDTE));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(listDir.length>0) {
					nomeFile = listDir[listDir.length-1];
				}
			    String model = nomeFile.substring(0, nomeFile.indexOf(".xsl")) + ".gvxdt";
				try {
					FileManager.cp(gvDir+ dirDTE, appoDir + dirDTE, nomeFile);
					FileManager.cp(gvDir+ dirDTE, appoDir + dirDTE, model);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			listFile = this.gvConfig.getListaFileXq(listaServizi);
			for(String key:listFile.keySet()){
				String nomeFile = listFile.get(key);
				Node dataSource = this.gvConfig.getDataSourceFromTrasf(key);
				String xpath="LocalFSDataSource[@formatHandled='xq']/@repositoryHome";
				String dirDTE = XMLUtils.get_S(dataSource, xpath);
				dirDTE = dirDTE.replace("${{gv.app.home}}", "");
				try {
					FileUtils.forceMkdir(new File(appoDir+File.separator+dirDTE));
				} catch (IOException e) {
					e.printStackTrace();
				}
				String[] listDir = nomeFile.split("/");
				for(int i=0;i<(listDir.length-1);i++){
					dirDTE=dirDTE+File.separator+listDir[i];
					try {
						FileUtils.forceMkdir(new File(appoDir+File.separator+dirDTE));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(listDir.length>0) {
					nomeFile = listDir[listDir.length-1];
				}
				try {
					FileManager.cp(gvDir+ dirDTE, appoDir + dirDTE, nomeFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			listFile = this.gvConfig.getListaFileBin(listaServizi);
			for(String key:listFile.keySet()){
				String nomeFile = listFile.get(key);
				Node dataSource = this.gvConfig.getDataSourceFromTrasf(key);
				String xpath="LocalFSDataSource[@formatHandled='bin']/@repositoryHome";
				String dirDTE = XMLUtils.get_S(dataSource, xpath);
				dirDTE = dirDTE.replace("${{gv.app.home}}", "");
				try {
					FileUtils.forceMkdir(new File(appoDir+File.separator+dirDTE));
				} catch (IOException e) {
					e.printStackTrace();
				}
				String[] listDir = nomeFile.split("/");
				for(int i=0;i<(listDir.length-1);i++){
					dirDTE=dirDTE+File.separator+listDir[i];
					try {
						FileUtils.forceMkdir(new File(appoDir+File.separator+dirDTE));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(listDir.length>0) {
					nomeFile = listDir[listDir.length-1];
				}
				try {
					FileManager.cp(gvDir+ dirDTE, appoDir + dirDTE, nomeFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			String wsdlDir = appoDir + File.separator + "wsdl";
			String aarDir = appoDir + File.separator + "service";
			Map<String, Node> listaWs = this.gvConfig.getListaBusinessWebServices(listaServizi);
			String dirWsdl = this.gvConfig.geWsdlDir();
			String dirService = this.gvConfig.getWsServiceDir();
			boolean usesRampart = false;

			for(String wsService:listaWs.keySet()){
				try {
					if (!usesRampart) {
						Node ws = listaWs.get(wsService);
						usesRampart = XMLUtils.selectSingleNode_S(ws, "EngageModule[@name='rampart']") != null;
					}
					FileManager.cp(gvDir+ dirWsdl, wsdlDir, wsService+".wsdl");
					FileManager.cp(gvDir+ dirService, aarDir, wsService+".aar");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (listaWs.isEmpty()) {
				try {
					FileManager.mkdir(wsdlDir);
					FileManager.mkdir(aarDir);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (XMLUtils.existNode_S(gvAdaptersDoc, "/GVAdapters/GVBIRTReportConfiguration/ReportGroups/ReportGroup/Report")) {
				try {
					FileManager.cp(birtDir + File.separator + "reports", appoDir + File.separator + "reports", ".*");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					FileManager.mkdir(appoDir + File.separator + "reports" + File.separator + "images");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (usesRampart) {
				try {
					FileManager.cp(gvDir + File.separator + "keystores", appoDir + File.separator + "keystores", ".*");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					FileManager.mkdir(appoDir + File.separator + "keystores");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}


			if (XMLUtils.existNode_S(gvCoreDoc, "//BpelOperation")) {
				if ((new File(bipelDir)).exists()) {
					try {
						FileManager.cp(bipelDir, appoDir + File.separator + "BpelProcess", ".*");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else {
				try {
					FileManager.mkdir(appoDir + File.separator + "BpelProcess");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (XMLUtils.existNode_S(gvAdaptersDoc, "//RuleResource[@resourceType='DRL']")) {
				if ((new File(rulesDir)).exists()) {
					try {
						FileManager.cp(rulesDir, appoDir + File.separator + "Rules", ".*");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else {
				try {
					FileManager.mkdir(appoDir + File.separator + "Rules");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			ZipHelper zh = new ZipHelper();
			try {
				zh.zipFile(tmpDir, "conf", tmpDir, "GVExport.zip");
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				is = new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(tmpDir, "GVExport.zip")));
			} catch (IOException e) {
				e.printStackTrace();
			}

			logger.debug("File zippato con successo");
		} catch (PropertiesHandlerException e) {
			e.printStackTrace();
		} catch (XMLUtilsException e) {
			e.printStackTrace();
		}

		finally {
			if (tmpDir != null) {
				try {
					FileManager.rm(tmpDir, "GVExport.zip");
				}
				catch (Exception exc) {
					// do nothing
				}
			}
			if (appoDir != null) {
				try {
					FileManager.rm(appoDir, null);
				}
				catch (Exception exc) {
					// do nothing
				}
			}
		}
		return is;
	}

	public ByteArrayInputStream creaFileZip()
	{
		ByteArrayInputStream is = new ByteArrayInputStream(new byte[0]);
		String tmpDir = null;
		String appoDir = null;
		String gvDir = null;
		try {
			tmpDir = PropertiesHandler.expand("${{java.io.tmpdir}}", null);
			appoDir = tmpDir + File.separator + "conf";
			gvDir = PropertiesHandler.expand("${{gv.app.home}}", null);
			String xmlDir = gvDir + File.separator + "xmlconfig";
			String defDTEDir = gvDir + File.separator + "gvdte";//+File.separator+"datasource";
			String dtdsDir = gvDir + File.separator + "dtds";
			String birtDir = gvDir + File.separator + "BIRTReportEngine";
			String bipelDir = gvDir + File.separator + "BpelProcess";
			String rulesDir = gvDir + File.separator + "Rules";

			FileUtils.deleteDirectory(new File(appoDir));
			FileUtils.forceMkdir(new File(appoDir));

			FileManager.cp(xmlDir, appoDir, "^((GVCore)|(GVSupport)|(GVAdapters)|(GVVariables)|(gvesb-catalog))\\.xml$");
			FileManager.cp(xmlDir, appoDir, "^GVEsb\\.jks$");

			FileManager.cp(dtdsDir, appoDir + File.separator + "dtds", ".*");
			FileManager.cp(xmlDir + File.separator + "xsds", appoDir + File.separator + "xsds", ".*");
			FileManager.cp(xmlDir + File.separator + "jsds", appoDir + File.separator + "jsds", ".*");
			FileManager.cp(defDTEDir, appoDir + File.separator + "gvdte", ".*");
			//FileManager.cp(PropertiesHandler.expand(XMLConfig.get("GVCore.xml", "//DataSourceSet[@name='Default']/*[@formatHandled='xsl']/@repositoryHome", defDTEDir+File.separator+"xsl"), null), appoDir+File.separator+"gvdte"+File.separator+"xsl", ".*");
			//FileManager.cp(PropertiesHandler.expand(XMLConfig.get("GVCore.xml", "//DataSourceSet[@name='Default']/*[@formatHandled='xsd']/@repositoryHome", defDTEDir+File.separator+"xsd"), null), appoDir+File.separator+"gvdte"+File.separator+"xsd", ".*");
			//FileManager.cp(PropertiesHandler.expand(XMLConfig.get("GVCore.xml", "//DataSourceSet[@name='Default']/*[@formatHandled='bin']/@repositoryHome", defDTEDir+File.separator+"bin"), null), appoDir+File.separator+"gvdte"+File.separator+"bin", ".*");

			FileManager.cp(xmlDir + File.separator + "wsdl", appoDir + File.separator + "wsdl", ".*");
			FileManager.cp(gvDir + File.separator + "webservices" + File.separator + "services", appoDir
					+ File.separator + "services", ".*\\.aar");
			FileManager.cp(birtDir + File.separator + "reports", appoDir + File.separator + "reports", ".*");
			FileManager.cp(gvDir + File.separator + "keystores", appoDir + File.separator + "keystores", ".*");
			if ((new File(bipelDir)).exists()) {
				FileManager.cp(bipelDir, appoDir + File.separator + "BpelProcess", ".*");
			}
			if ((new File(rulesDir)).exists()) {
				FileManager.cp(rulesDir, appoDir + File.separator + "Rules", ".*");
			}

			ZipHelper zh = new ZipHelper();
			zh.zipFile(tmpDir, "conf", tmpDir, "GVExport.zip");

			is = new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(tmpDir, "GVExport.zip")));

			logger.debug("File zippato con successo");
		}
		catch (Exception exc) {
			logger.error("Error zipping exported configuration", exc);
		}
		finally {
			if (tmpDir != null) {
				try {
					FileManager.rm(tmpDir, "GVExport.zip");
				}
				catch (Exception exc) {
					// do nothing
				}
			}
			if (appoDir != null) {
				try {
					FileManager.rm(appoDir, null);
				}
				catch (Exception exc) {
					// do nothing
				}
			}
		}
		return is;
	}

	public static ByteArrayInputStream copyFileForBackupZip() throws Exception
	{
		String tmpDir = null;
		String appoDir = null;
		String gvDir = null;
		String nomeZipDir="GVDeploy_"+ DateUtils.nowToString("yyyyMMddHHmmss");
		String nomeZipFile= nomeZipDir + ".zip";
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
			throw exc;
		}
	}

	public static void main(String[] args) {
		try {
			GVConfig gvConfigServer = new GVConfig("/home/gianluca/applicazioni/GvServer-3.4.0.12.Final/GreenV/xmlconfig/GVCore.xml","/home/gianluca/applicazioni/GvServer-3.4.0.12.Final/GreenV/xmlconfig/GVAdapters.xml");
			//System.out.println(gvConfigServer.getGvCore(true));
			//System.out.println(gvConfigServer.getGvAdapters(true));
			List <String> listaServizi = new ArrayList<String>();
			listaServizi.add("ProcessSingleSVCEmail");
			//listaServizi.add("TestRSH");
			//listaServizi.add("TestHL7_Listener");listaServizi.add("TestHL7_ListenerPippo");
			//listaServizi.add("TWEET");
			//listaServizi.add("PushNotification");


			System.out.println(gvConfigServer.getGvCore(listaServizi, true));
			System.out.println(gvConfigServer.getGvAdapters(listaServizi, true));

			GVConfigZipFile gvzip = new GVConfigZipFile(gvConfigServer);
			IOUtils.copy(gvzip.creaFileZip(listaServizi), new FileOutputStream("/home/gianluca/applicazioni/GvServer-3.4.0.12.Final/GVExport.zip"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

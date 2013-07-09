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
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import max.xml.DOMWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
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
public class GVCoreParser
{

    /**
     * @param
     */
    private URL            url       = null;
    private Document serverXml = null;
    private Document newXml    = null;
    private GVAdapterParser adapterParser = null;
    private static Logger  logger    = GVLogger.getLogger(GVCoreParser.class);

    /**
     * Legge i file xml di configurazione dal file .zip e dal server
     * 
     * @throws 
     * @throws IOException
     * @throws XMLConfigException
     */
    public void loadParser() throws Exception
    {
        logger.debug("init load Parser GVCoreParser");
        newXml = getXmlZip();
        serverXml = getXmlServer();
    }

    public void setAdapterParser(GVAdapterParser adapterParser) {
		this.adapterParser = adapterParser;
	}

    /**
     * Restituice un valore booleano che indica se il servizio gi� esiste sul
     * server
     * 
     * @param nomeServizio
     *        nome del servizio
     * @return boolean value
     * @throws XMLUtilsException 
     */
    public boolean getExist(String nomeServizio) throws XMLUtilsException
    {
        boolean existServizio = XMLUtils.existNode_S(serverXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
                + "']");
        return existServizio;
    }

    /**
     * 
     * Restituice la lista dei nomi dei servizi presenti nel file di
     * configurazione in input
     * 
     * @param GVCoreDocument
     *        xml file xml di configurazione del Core
     * 
     * @return String[] lista dei nomi dei servizi presenti nel file di
     *         configurazione in input
     */
    private String[] getListaServizi(Document xml) throws Exception
    {
        logger.debug("init getListaServizi");
        String[] retListaServizi = null;
    	XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();
    		NodeList results = parser.selectNodeList(xml, "/GVCore/GVServices/Services/Service");
    		retListaServizi = new String[results.getLength()];
			for (int i = 0; i < results.getLength(); i++) {
				retListaServizi[i] = parser.get(results.item(i), "@id-service");
				logger.debug("Id Servizio Nuovo=" + retListaServizi[i]);
			}
    		return retListaServizi;
    	}
    	finally {
    		XMLUtils.releaseParserInstance(parser);
    	}
    }

    private boolean getExistXpathZip() throws Exception
    {
        return getExistXpath(newXml);
    }

    /**
     * @return
     * @throws 
     * @throws IOException
     */
    public boolean getExistXpathServer() throws Exception
    {
        return getExistXpath(serverXml);
    }

    private boolean getExistXpath(Document xml) throws Exception
    {
        return XMLUtils.existNode_S(xml, "/GVCore/GVXPath");
    }

    private boolean getExistPoolManager(Document xml) throws Exception
    {
        return XMLUtils.existNode_S(xml, "/GVCore/GVPoolManager");
    }

    private boolean getExistPoolManagerZip() throws Exception
    {
        return getExistPoolManager(newXml);
    }

    /**
     * @return
     * @throws 
     * @throws IOException
     */
    public boolean getExistPoolManagerServer() throws Exception
    {
        return getExistPoolManager(serverXml);
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public boolean getEqualPoolManager() throws XMLUtilsException
    {
        boolean equalPoolManager = getEqualObject(getGVPoolManager(serverXml), getGVPoolManager(newXml));
        logger.debug("equalPoolManager = " + equalPoolManager);
        return equalPoolManager;
    }

    private boolean getExistTask(Document xml) throws Exception
    {
        return XMLUtils.existNode_S(xml, "/GVCore/GVTaskManagerConfiguration");
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public boolean getEqualXpath() throws XMLUtilsException
    {
        boolean equalXPath = getEqualObject(getGVXPath(serverXml), getGVXPath(newXml));
        logger.debug("getEqualXpath = " + equalXPath);
        return equalXPath;
    }

    /**
     * @return
     * @throws 
     * @throws IOException
     */
    public boolean getExistTaskZip() throws Exception
    {
        return getExistTask(newXml);
    }

    /**
     * @return
     * @throws 
     * @throws IOException
     */
    public boolean getExistTaskServer() throws Exception
    {
        return getExistTask(serverXml);
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public boolean getEqualTask() throws XMLUtilsException
    {
        boolean equalTask = getEqualObject(getGVTask(serverXml), getGVTask(newXml));
        logger.debug("equalTask = " + equalTask);
        return equalTask;
    }

    private boolean getExistConcurrencyHandler(Document xml) throws Exception
    {
        return XMLUtils.existNode_S(xml, "/GVCore/GVConcurrencyHandler");
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public boolean getEqualConcurrencyHandler() throws XMLUtilsException
    {
        boolean equalConcurrencyHandler = getEqualObject(getGVConcurrencyHandler(serverXml), getGVConcurrencyHandler(newXml));
        logger.debug("equalConcurrencyHandler = " + equalConcurrencyHandler);
        return equalConcurrencyHandler;
    }

    private boolean getExistConcurrencyHandlerZip() throws Exception
    {
        return getExistConcurrencyHandler(newXml);
    }

    /**
     * @return
     * @throws 
     * @throws IOException
     */
    public boolean getExistConcurrencyHandlerServer() throws Exception
    {
        return getExistConcurrencyHandler(serverXml);
    }


    private boolean getExistCryptoHelper(Document xml) throws Exception
    {
        return XMLUtils.existNode_S(xml, "/GVCore/GVCryptoHelper");
    }


    private boolean getExistGVPolicy(Document xml) throws Exception
    {
        return XMLUtils.existNode_S(xml, "/GVCore/GVPolicy");
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public boolean getEqualCryptoHelper() throws XMLUtilsException
    {
        boolean equalCryptoHelper = getEqualObject(getGVCryptoHelper(serverXml), getGVCryptoHelper(newXml));
        logger.debug("equalCryptoHelper = " + equalCryptoHelper);
        return equalCryptoHelper;
    }

    private boolean getExistCryptoHelperZip() throws Exception
    {
        return getExistCryptoHelper(newXml);
    }

    /**
     * @return
     * @throws 
     * @throws IOException
     */
    public boolean getExistCryptoHelperServer() throws Exception
    {
        return getExistCryptoHelper(serverXml);
    }


    /**
     * @return
     * @throws XMLUtilsException 
     */
    public boolean getEqualPolicy() throws XMLUtilsException
    {
        boolean equalPolicy = getEqualObject(getGVPolicy(serverXml), getGVPolicy(newXml));
        logger.debug("equalPolicy = " + equalPolicy);
        return equalPolicy;
    }

    private boolean getExistGVPolicyZip() throws Exception
    {
        return getExistGVPolicy(newXml);
    }

    /**
     * @return
     * @throws 
     * @throws IOException
     */
    public boolean getExistGVPolicyServer() throws Exception
    {
        return getExistGVPolicy(serverXml);
    }

    /**
     * @param nomeAdapter
     * @return
     * @throws XMLUtilsException 
     */
    private boolean getEqualObject(Node resultsServer, Node resultsZip) throws XMLUtilsException
    {
        boolean equalObject = false;
        XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();
	        if ((resultsServer != null) && (resultsZip != null)) {
	            if (parser.serializeDOM(resultsServer).equals(parser.serializeDOM(resultsZip))) {
	                equalObject = true;
	            }
	        }
	        logger.debug("getEqualObject=" + equalObject);
	        return equalObject;
    	}
    	finally {
    		XMLUtils.releaseParserInstance(parser);
    	}
    }
    
    /**
     * @return
     * @throws 
     * @throws IOException
     */
    public String[] getListParameterZip() throws Exception
    {
        Vector<String> listaParametri = new Vector<String>();
        if (getExistPoolManagerZip()) {
            listaParametri.add("PoolManager");
        }
        if (getExistXpathZip()) {
            listaParametri.add("Xpath");
        }
        if (getExistTaskZip()) {
            listaParametri.add("Task");
        }
        if (getExistConcurrencyHandlerZip()) {
            listaParametri.add("ConcurrencyHandler");
        }
        if (getExistCryptoHelperZip()) {
            listaParametri.add("CryptoHelper");
        }
        if (getExistGVPolicyZip()) {
            listaParametri.add("Policy");
        }
        String[] listaServizi = new String[listaParametri.size()];
        for (int i = 0; i < listaParametri.size(); i++) {
            listaServizi[i] = listaParametri.get(i);
            logger.debug("parametro = " + listaServizi[i]);
        }
        return listaServizi;
    }

    /**
     * 
     * Restituice la lista dei nomi dei servizi presenti nel file di
     * configurazione da deploiare
     * 
     * 
     * @return String[] lista dei nomi dei servizi presenti nel file di
     *         configurazione in input
     * @throws 
     * @throws IOException
     */
    public String[] getListaServiziZip() throws Exception
    {
        return getListaServizi(newXml);
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
    public String[] getListaServiziServer() throws Exception
    {
        return getListaServizi(serverXml);
    }

    /**
     * @param nomeServizio
     * @return
     * @throws XMLUtilsException 
     */
    public boolean getEqualService(String nomeServizio) throws XMLUtilsException
    {
        boolean equalServizio = getEqualObject(getService(serverXml, nomeServizio), getService(newXml, nomeServizio));
        logger.debug("getEqual = " + equalServizio);
        return equalServizio;
    }

    /**
     * @param nomeServizio
     * @return
     * @throws XMLUtilsException 
     */
    public String getGvCoreZip(String nomeServizio) throws XMLUtilsException
    {
        return getGvCore(newXml, nomeServizio);
    }

    /**
     * @param nomeServizio
     * @return
     * @throws XMLUtilsException 
     */
    public String getGvCoreServer(String nomeServizio) throws XMLUtilsException
    {
        return getGvCore(serverXml, nomeServizio);
    }

    private String getGvCore(Document xml, String nomeServizio) throws XMLUtilsException
    {
        logger.debug("init getGvCore");
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();

	        Document localXmlGVCore = parser.newDocument("GVCore");
	        
	        Node locServices = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVServices"));
            parser.setAttribute((Element) locServices, "type", "module");
            parser.setAttribute((Element) locServices, "name", "SERVICES");
            
            Node locSystems = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVSystems"));
            parser.setAttribute((Element) locServices, "type", "module");
            parser.setAttribute((Element) locServices, "name", "SYSTEMS");
            locSystems = locSystems.appendChild(parser.createElement(localXmlGVCore, "Systems"));
            
	        Node service = getService(xml, nomeServizio);
	        if (service != null) {
	            Node cryptoHelper = getGVCryptoHelper(xml, nomeServizio);
	            if (cryptoHelper != null) {
	            	Node importedNode = localXmlGVCore.importNode(cryptoHelper, true);
	            	localXmlGVCore.getDocumentElement().appendChild(importedNode);
	            }
	            List<Node> gvForwards = getGVForwards(xml, nomeServizio);
	            if (gvForwards.size() > 0) {
	                Node locGVForwards = localXmlGVCore.getDocumentElement().appendChild(parser.createElement(localXmlGVCore, "GVForwards"));
	                for (Node forwardConfiguration : gvForwards) {
	                	Node importedNode = localXmlGVCore.importNode(forwardConfiguration, true);
	                	locGVForwards.appendChild(importedNode);
	                }
	            }
	            Node group = getGroup(xml, nomeServizio);
	            if (group != null) {
	                Node locGroups = locServices.appendChild(parser.createElement(localXmlGVCore, "Groups"));
                	Node importedNode = localXmlGVCore.importNode(group, true);
                	locGroups.appendChild(importedNode);
	            }
	            
	            locServices = locServices.appendChild(parser.createElement(localXmlGVCore, "Services"));
	            locServices.appendChild(localXmlGVCore.importNode(service, true));

	            Map<String, Node> mapListaSistemi = getListaSistemi(nomeServizio);
	            String[] listaSistemi = new String[mapListaSistemi.keySet().size()];
	            mapListaSistemi.keySet().toArray(listaSistemi);
	            for (int i = 0; i < listaSistemi.length; i++) {
	                String sistema = listaSistemi[i];
	                Node participant = mapListaSistemi.get(sistema);
	                Node resultsSystem = parser.selectSingleNode(xml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema + "']");
	                if (resultsSystem != null) {
	                	Node importedNode = localXmlGVCore.importNode(resultsSystem, false);
	                	Node locSystem = locSystems.appendChild(importedNode);

	                	Node resultsChannel = parser.selectSingleNode(resultsSystem, "Channel[@id-channel='" + parser.get(participant, "@id-channel") + "']");
		                if (resultsChannel != null) {
		                	locSystem.appendChild(localXmlGVCore.importNode(resultsChannel, true));
		                }
	                }
	            }

	            Node gvBufferDump = getGVBufferDump(xml, nomeServizio);
	            if (gvBufferDump != null) {
	            	Node importedNode = localXmlGVCore.importNode(gvBufferDump, true);
	            	localXmlGVCore.getDocumentElement().appendChild(importedNode);
	            }

	            String[] listaTrasformazioni = getListaTrasformazioni(xml, nomeServizio);
            	if ((listaTrasformazioni != null) && (listaTrasformazioni.length > 0)) {
                    Node gvDataTransformation = parser.selectSingleNode(xml, "/GVCore/GVDataTransformation");
	            	Node localDTE = localXmlGVCore.getDocumentElement().appendChild(localXmlGVCore.importNode(gvDataTransformation, false));
                    if (gvDataTransformation != null) {
                        NodeList dataSourceSet = parser.selectNodeList(gvDataTransformation, "DataSourceSets/DataSourceSet");
                        Node localDSS = localDTE.appendChild(parser.createElement(localXmlGVCore, "DataSourceSets"));
                        for (int i = 0; i < dataSourceSet.getLength(); i++) {
                            localDSS.appendChild(localXmlGVCore.importNode(dataSourceSet.item(i), true));
                        }

                        Node localDT = localDTE.appendChild(parser.createElement(localXmlGVCore, "Transformations"));
                        for (int i = 0; i < listaTrasformazioni.length; i++) {
                            String name = listaTrasformazioni[i];
                            Node gvDT = parser.selectSingleNode(gvDataTransformation, "Transformations/*[@type='transformation' and @name='" + name + "']");
                            if (gvDT != null) {
            	            	Node importedNode = localXmlGVCore.importNode(gvDT, true);
            	            	localDT.appendChild(importedNode);
            	            }
                        }
                    }
	            }
	        }
	        String localXml = parser.serializeDOM(localXmlGVCore, false, true);
	        logger.debug("----" + localXml + "-------");
	        getListDataProvider(xml, nomeServizio);
	        return localXml;
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private String getGVTaskLocal(Document xml) throws XMLUtilsException
    {
        logger.debug("init getGVTaskLocal");
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
        	Document localXmlGVCore = parser.newDocument("GVCore");
        	Node localXml = getGVTask(xml);
        	if (localXml != null) {
        		Node importedNode = localXmlGVCore.importNode(localXml, true);
        		localXmlGVCore.getDocumentElement().appendChild(importedNode);
        	}
	        return parser.serializeDOM(localXmlGVCore, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private String getGvPoolManager(Document xml) throws XMLUtilsException
    {
        logger.debug("init getGvPoolManager");
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
        	Document localXmlGVCore = parser.newDocument("GVCore");
        	Node localXml = getGVPoolManager(xml);
        	if (localXml != null) {
        		Node importedNode = localXmlGVCore.importNode(localXml, true);
        		localXmlGVCore.getDocumentElement().appendChild(importedNode);
        	}
	        return parser.serializeDOM(localXmlGVCore, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private String getGvXpath(Document xml) throws XMLUtilsException
    {
        logger.debug("init getGvXpath");
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
        	Document localXmlGVCore = parser.newDocument("GVCore");
        	Node localXml = getGVXPath(xml);
        	if (localXml != null) {
        		Node importedNode = localXmlGVCore.importNode(localXml, true);
        		localXmlGVCore.getDocumentElement().appendChild(importedNode);
        	}
	        return parser.serializeDOM(localXmlGVCore, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public String getGVTaskZip() throws XMLUtilsException
    {
        return getGVTaskLocal(newXml);
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public String getGvPoolManagerZip() throws XMLUtilsException
    {
        return getGvPoolManager(newXml);
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public String getGvXpathZip() throws XMLUtilsException
    {
        return getGvXpath(newXml);
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public String getGVTaskServer() throws XMLUtilsException
    {
        return getGVTaskLocal(serverXml);
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public String getGvPoolManagerServer() throws XMLUtilsException
    {
        return getGvPoolManager(serverXml);
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public String getGvXpathServer() throws XMLUtilsException
    {
        return getGvXpath(serverXml);
    }

    private String getGVConcurrencyHandlerLocal(Document xml) throws XMLUtilsException
    {
        logger.debug("init getGVConcurrencyHandler");
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
        	Document localXmlGVCore = parser.newDocument("GVCore");
        	Node localXml = getGVConcurrencyHandler(xml);
        	if (localXml != null) {
        		Node importedNode = localXmlGVCore.importNode(localXml, true);
        		localXmlGVCore.getDocumentElement().appendChild(importedNode);
        	}
	        return parser.serializeDOM(localXmlGVCore, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public String getGVConcurrencyHandlerZip() throws XMLUtilsException
    {
        return getGVConcurrencyHandlerLocal(newXml);
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public String getGVConcurrencyHandlerServer() throws XMLUtilsException
    {
        return getGVConcurrencyHandlerLocal(serverXml);
    }

    private String getGVCryptoHelperLocal(Document xml) throws XMLUtilsException
    {
        logger.debug("init getGVCryptoHelperLocal");
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
        	Document localXmlGVCore = parser.newDocument("GVCore");
        	Node localXml = getGVCryptoHelper(xml);
        	if (localXml != null) {
        		Node importedNode = localXmlGVCore.importNode(localXml, true);
        		localXmlGVCore.getDocumentElement().appendChild(importedNode);
        	}
	        return parser.serializeDOM(localXmlGVCore, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public String getGVCryptoHelperZip() throws XMLUtilsException
    {
        return getGVCryptoHelperLocal(newXml);
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public String getGVCryptoHelperServer() throws XMLUtilsException
    {
        return getGVCryptoHelperLocal(serverXml);
    }


    private Node getGVCryptoHelper(Document xml, String nomeServizio) throws XMLUtilsException
    {
        logger.debug("init getGVCryptoHelper");
        String[] listaKeyId = getListaKeyIdTrasformazioni(xml, nomeServizio);
        Node gvCryptoHelper = null;
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

            gvCryptoHelper = XMLUtils.selectSingleNode_S(xml, strXpath);
        }
        return gvCryptoHelper;
    }

    private String getGVPolicyLocal(Document xml) throws XMLUtilsException
    {
        logger.debug("init getGVPolicy");
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
        	Document localXmlGVCore = parser.newDocument("GVCore");
        	Node localXml = getGVPolicy(xml);
        	if (localXml != null) {
        		Node importedNode = localXmlGVCore.importNode(localXml, true);
        		localXmlGVCore.getDocumentElement().appendChild(importedNode);
        	}
	        return parser.serializeDOM(localXmlGVCore, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public String getGVPolicyZip() throws XMLUtilsException
    {
        return getGVPolicyLocal(newXml);
    }

    /**
     * @return
     * @throws XMLUtilsException 
     */
    public String getGVPolicyServer() throws XMLUtilsException
    {
        return getGVPolicyLocal(serverXml);
    }


    private Node getService(Document xml, String nomeServizio) throws XMLUtilsException
    {
    	Node service = XMLUtils.selectSingleNode_S(xml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio + "']");
        return service;
    }

    private Node getGroup(Document xml, String nomeServizio) throws XMLUtilsException
    {
        String xPathGroup = "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio + "']/@group-name";
        String groupName = XMLUtils.get_S(xml, xPathGroup);
        Node group = XMLUtils.selectSingleNode_S(xml, "/GVCore/GVServices/Groups/Group[@id-group='" + groupName + "']");
        return group;
    }

    private Node getGVXPath(Document xml) throws XMLUtilsException
    {
    	Node gvXPath = XMLUtils.selectSingleNode_S(xml, "/GVCore/GVXPath");
        return gvXPath;
    }

    private List<Node> getGVForwards(Document xml, String nomeServizio) throws XMLUtilsException 
    {
        List<Node> gvForwards = new ArrayList<Node>();
        String[] listaForward = getListaForward(xml, nomeServizio);
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
        	
	        for (int i = 0; i < listaForward.length; i++) {
	        	String strXpath = "/GVCore/GVForwards/ForwardConfiguration[@forwardName='" + listaForward[i] + "']";
	            NodeList nodeList = parser.selectNodeList(xml, strXpath);
	            if (nodeList != null) {
	                for (int n = 0; n < nodeList.getLength(); n++) {
	                    gvForwards.add(nodeList.item(n));
	                }
	            }
	        }
	        return gvForwards;
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private Node getGVPoolManager(Document xml) throws XMLUtilsException
    {
        Node gvPoolManager = XMLUtils.selectSingleNode_S(xml, "/GVCore/GVPoolManager");
        return gvPoolManager;
    }

    private Node getGVConcurrencyHandler(Document xml) throws XMLUtilsException
    {
    	Node gvConcurrencyHandler = XMLUtils.selectSingleNode_S(xml, "/GVCore/GVConcurrencyHandler");
        return gvConcurrencyHandler;
    }

    private Node getGVCryptoHelper(Document xml) throws XMLUtilsException
    {
    	Node gvCryptoHelper = XMLUtils.selectSingleNode_S(xml, "/GVCore/GVCryptoHelper");
        return gvCryptoHelper;
    }


    private Node getGVPolicy(Document xml) throws XMLUtilsException
    {
    	Node gvPolicy = XMLUtils.selectSingleNode_S(xml, "/GVCore/GVPolicy");
        return gvPolicy;
    }

    private Node getGVBufferDump(Document xml, String nomeServizio) throws XMLUtilsException
    {
    	Node gvDataDump = XMLUtils.selectSingleNode_S(xml, "/GVCore/GVBufferDump[ServiceDump/@id-service='" + nomeServizio + "']");
        return gvDataDump;
    }

    private Node getGVDataTransformation(Document xml, String nomeServizio) throws XMLUtilsException
    {
        String[] listaTrasformazioni = getListaTrasformazioni(xml, nomeServizio);
        Node gvDataTransformation = null;
        String strXpath = "";
        if (listaTrasformazioni.length > 0) {
	        for (int i = 0; i < listaTrasformazioni.length; i++) {
	            if (i == 0) {
	                strXpath = strXpath + "/GVCore/GVDataTransformation[Transformations/*/@name='";
	            }
	            strXpath = strXpath + listaTrasformazioni[i] + "'";
	            if (i == (listaTrasformazioni.length - 1)) {
	                strXpath = strXpath + "]";
	            }
	            else {
	                strXpath = strXpath + " or Transformations/*/@name='";
	            }
	        }
            logger.debug("strXpath = " + strXpath);
            gvDataTransformation = XMLUtils.selectSingleNode_S(xml, strXpath);
        }
        return gvDataTransformation;
    }

    private Node getGVTask(Document xml) throws XMLUtilsException
    {
        Node gvTaskConfiguration = XMLUtils.selectSingleNode_S(xml, "/GVCore/GVTaskManagerConfiguration");;
        return gvTaskConfiguration;
    }

    private String[] getListDataProvider(Document xml, String nomeServizio) throws XMLUtilsException
    {
        Map<String, String> hlistaDp = new Hashtable<String, String>();
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
       
	        getListDataProviderSystem(xml, nomeServizio, hlistaDp, parser);
	        getListDataProviderCoreIterator(xml, nomeServizio, hlistaDp, parser);
	        getListDataProviderCoreIteratorCall(xml, nomeServizio, hlistaDp, parser);
	        getListDataProviderCoreIteratorSubFlowCall(xml, nomeServizio, hlistaDp, parser);
	        Set<String> set = hlistaDp.keySet(); // get set-view of keys
	        logger.debug("getListDataProvider - LISTA DP=" + set);
	        // get iterator
	        String[] listDataProvider = new String[hlistaDp.size()];
	        Iterator<String> itr = set.iterator();
	        int i = 0;
	        while (itr.hasNext()) {
	            listDataProvider[i] = itr.next();
	            i++;
	        }
	        return listDataProvider;
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private String[] getListDataProviderForward(Document xml, String forwardName) throws XMLUtilsException
    {
        NodeList nl = XMLUtils.selectNodeList_S(xml, "/GVCore/GVForwards/ForwardConfiguration[@forwardName='"
                + forwardName + "']");
        String[] dps = null; 
        if (nl != null) {
            dps = new String[nl.getLength()];
            for (int i = 0; i < nl.getLength(); i++) {
                dps[i] = XMLUtils.get_S(nl.item(i), "@ref-dp");
                logger.debug("getListDataProviderForward - [" + forwardName + "] DP=" + dps[i]);
            }
        }
        
        return dps;
    }

    private void getListDataProviderSystem(Document xml, String nomeServizio, Map<String, String> vlistaDp, XMLUtils parser) throws XMLUtilsException
    {
        NodeList partecipants = parser.selectNodeList(xml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
                + "']/Operation/Participant");
        logger.debug("xpath=" + "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
                + "']/Operation/Participant");
        if (partecipants.getLength() > 0) {
            for (int i = 0; i < partecipants.getLength(); i++) {
            	String sistema = parser.get(partecipants.item(i), "@id-system");
                String canale = parser.get(partecipants.item(i), "@id-channel");
                logger.debug("Partecipant=" + sistema);
                logger.debug("canale=" + canale);
                NodeList dataProviders = parser.selectNodeList(xml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema
                        + "']/Channel[@id-channel='" + canale + "']/*/@ref-dp");
                for (int d = 0; d < dataProviders.getLength(); d++) {
                    String dp = parser.get(dataProviders.item(d), ".");
                    vlistaDp.put(dp, dp);
                }
                dataProviders = parser.selectNodeList(xml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema
                        + "']/Channel[@id-channel='" + canale + "']/*/*/@ref-dp");
                for (int d = 0; d < dataProviders.getLength(); d++) {
                    String dp = parser.get(dataProviders.item(d), ".");
                    vlistaDp.put(dp, dp);
                }
                dataProviders = parser.selectNodeList(xml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema
                        + "']/Channel[@id-channel='" + canale + "']/*/@input-ref-dp");
                for (int d = 0; d < dataProviders.getLength(); d++) {
                    String dp = parser.get(dataProviders.item(d), ".");
                    vlistaDp.put(dp, dp);
                }
                dataProviders = parser.selectNodeList(xml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema
                        + "']/Channel[@id-channel='" + canale + "']/*/@output-ref-dp");
                for (int d = 0; d < dataProviders.getLength(); d++) {
                    String dp = parser.get(dataProviders.item(d), ".");
                    vlistaDp.put(dp, dp);
                }
                dataProviders = parser.selectNodeList(xml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema
                        + "']/Channel[@id-channel='" + canale + "']/*/@globals-ref-dp");
                for (int d = 0; d < dataProviders.getLength(); d++) {
                    String dp = parser.get(dataProviders.item(d), ".");
                    vlistaDp.put(dp, dp);
                }
            }
        }
        //logger.debug("getListDataProviderSystem - LISTA DP=" + vlistaDp);
    }

    private void getListDataProviderCoreIterator(Document xml, String nomeServizio, Map<String, String> vlistaDp, 
    		XMLUtils parser) throws XMLUtilsException
    {
        NodeList iterator = parser.selectNodeList(xml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
                + "']/Operation/*/GVIteratorOperationNode/@collection-dp");
        logger.debug("getListDataProviderCoreIterator NUM ITERATOR=" + iterator.getLength());
        for (int i = 0; i < iterator.getLength(); i++) {
            String dp = parser.get(iterator.item(i), ".");
            vlistaDp.put(dp, dp);
        }
    }

    private void getListDataProviderCoreIteratorCall(Document xml, String nomeServizio,
            Map<String, String> vlistaDp, XMLUtils parser) throws XMLUtilsException
    {
        NodeList refdp = parser.selectNodeList(xml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
                + "']/Operation/*/GVIteratorOperationNode/CoreCall/@ref-dp");
        logger.debug("getListDataProviderCoreIteratorCall NUM REFDP=" + refdp.getLength());
        for (int i = 0; i < refdp.getLength(); i++) {
            String dp = parser.get(refdp.item(i), ".");
            vlistaDp.put(dp, dp);
        }
    }

    private void getListDataProviderCoreIteratorSubFlowCall(Document xml, String nomeServizio,
            Map<String, String> vlistaDp, XMLUtils parser) throws XMLUtilsException
    {
        NodeList refdp = parser.selectNodeList(xml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
                + "']/Operation/*/GVIteratorOperationNode/SubFlowCall/@ref-dp");
        logger.debug("getListDataProviderCoreIteratorSubFlowCall NUM REFDP=" + refdp.getLength());
        for (int i = 0; i < refdp.getLength(); i++) {
            String dp = parser.get(refdp.item(i), ".");
            vlistaDp.put(dp, dp);
        }
    }

    private String[] getListKnowledgeBaseConfig(Document xml, String nomeServizio) throws XMLUtilsException
    {
        Map<String, String> hlistaKwB = new Hashtable<String, String>();
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
        	NodeList partecipants = parser.selectNodeList(newXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
        			+ "']/Operation/Participant");
        	if (partecipants.getLength() > 0) {
        		for (int i = 0; i < partecipants.getLength(); i++) {
	                String sistema = parser.get(partecipants.item(i), "@id-system");
	                String canale = parser.get(partecipants.item(i), "@id-channel");
	                logger.debug("Partecipant=" + sistema);
	                logger.debug("canale=" + canale);
	                NodeList kwBase = parser.selectNodeList(newXml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema
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
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private Map<String, Node> getListaSistemi(String nomeServizio) throws XMLUtilsException
    {
        Map<String, Node> listaSistemi = new HashMap<String, Node>();
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
        	NodeList partecipants = parser.selectNodeList(newXml, "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
        			+ "']/Operation/Participant");
        	if (partecipants.getLength() > 0) {
        		for (int i = 0; i < partecipants.getLength(); i++) {
        			String name = parser.get(partecipants.item(i), "@id-system");
        			logger.debug("Partecipant=" + name);
        			listaSistemi.put(name, partecipants.item(i));
        		}
        	}
        	return listaSistemi;
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    /**
     * @param xml
     * @param nomeServizio
     * @return
     * @throws XMLUtilsException 
     */
    public String[] getListaTrasformazioni(Document xml, String nomeServizio) throws XMLUtilsException
    {
        String xPathInputServices = "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
                + "']/Operation/*/*[@type='flow-node']/InputServices/gvdte-service/map-name-param/@value";
        String xPathOutputServices = "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
                + "']/Operation/*/*[@type='flow-node']/OutputServices/gvdte-service/map-name-param/@value";
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
	        NodeList inputServices = parser.selectNodeList(xml, xPathInputServices);
	        NodeList outputServices = parser.selectNodeList(xml, xPathOutputServices);
	        String[] listaTrasformazioni = new String[inputServices.getLength() + outputServices.getLength()];
	        int i = 0;
	        for (i = 0; i < inputServices.getLength(); i++) {
	            listaTrasformazioni[i] = parser.get(inputServices.item(i), ".");
	        }
	        for (int j = 0; j < outputServices.getLength(); j++) {
	            listaTrasformazioni[i] = parser.get(outputServices.item(j), ".");
	            i++;
	        }
	        logger.debug("BEGIN- Transformations for service[" + nomeServizio + "]");
	        for (int z = 0; z < listaTrasformazioni.length; z++) {
	            logger.debug(listaTrasformazioni[z]);
	        }
	        logger.debug("END - Transformations for service[" + nomeServizio + "]");
	        return listaTrasformazioni;
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    /**
     * @param xml
     * @param nomeServizio
     * @return
     * @throws XMLUtilsException 
     */
    public String[] getListaKeyIdTrasformazioni(Document xml, String nomeServizio) throws XMLUtilsException
    {
        String[] listaTrasformazioni = getListaTrasformazioni(xml, nomeServizio);
        String[] listaKeyIdTrasformazioni = null;
        if (listaTrasformazioni.length > 0) {
            String strXpath = "";
            for (int i = 0; i < listaTrasformazioni.length; i++) {
	            if (i == 0) {
	                strXpath = strXpath + "/GVCore/GVDataTransformation/Transformations/CryptoTransformation[@name='";
	            }
	            strXpath = strXpath + listaTrasformazioni[i] + "'";
	            if (i == (listaTrasformazioni.length - 1)) {
	                strXpath = strXpath + "]";
	            }
	            else {
	                strXpath = strXpath + " or @name='";
	            }
            }
            strXpath = strXpath + "/@KeyID";
            logger.debug("strXpath=" + strXpath);
            
            XMLUtils parser = null;
        	try {
        		parser = XMLUtils.getParserInstance();
	            NodeList kid = parser.selectNodeList(xml, strXpath);
	            if (kid.getLength() > 0) {
	                listaKeyIdTrasformazioni = new String[kid.getLength()];
	                for (int i = 0; i < kid.getLength(); i++) {
	                    listaKeyIdTrasformazioni[i] = parser.get(kid.item(i), ".");
	                    logger.debug("value=" + listaKeyIdTrasformazioni[i]);
	                }
	            }
        	}
        	finally {
        		XMLUtils.releaseParserInstance(parser);
        	}
        }
        return listaKeyIdTrasformazioni;
    }

    /**
     * @param xml
     * @param nomeServizio
     * @return
     * @throws XMLUtilsException 
     */
    public String[] getListaForward(Document xml, String nomeServizio) throws XMLUtilsException
    {
        String xpath = "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
                + "']/Operation[@name='Forward']/@forward-name";
    	return getListaObject(xml, xpath);
    }

    /**
     * @param xml
     * @return
     * @throws XMLUtilsException 
     * @throws IOException
     */
    private String[] getListaObject(Document xml, String xpath) throws XMLUtilsException
    {
    	XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();
    		NodeList results = parser.selectNodeList(xml, xpath);
    		String[] retListaObject = new String[results.getLength()];
    		for (int i = 0; i < results.getLength(); i++) {
    			retListaObject[i] = parser.get(results.item(i), ".");
    			logger.debug(retListaObject[i]);
    		}
    		return retListaObject;
    	}
    	finally {
    		XMLUtils.releaseParserInstance(parser);
    	}
    }
    
    public void aggiorna(String tipoOggetto, String nomeServizio) throws Exception
    {
        if (tipoOggetto.equals("Servizio")) {
        	XMLUtils parser = null;
        	try {
        		parser = XMLUtils.getParserInstance();
	            aggiornaService(nomeServizio, parser);
	            aggiornaGroup(nomeServizio, parser);
	            aggiornaSystem(nomeServizio, parser);
	            aggiornaGVWebServices(nomeServizio, parser);
	            aggiornaOdeProcess(nomeServizio, parser);
	            aggiornaGVCryptoHelper(nomeServizio, parser);
	            aggiornaGVForwards(nomeServizio, parser);
	            aggiornaGVBufferDump(nomeServizio, parser);
	            aggiornaGVDataTransformationForService(nomeServizio, parser);
	            aggiornaGVDataProviderService(nomeServizio, parser);
	            aggiornaGVKnowledgeBaseConfigService(nomeServizio, parser);
        	}
        	finally {
        		XMLUtils.releaseParserInstance(parser);
        	}
        }
        else if (tipoOggetto.equals("Xpath")) {
            aggiornaGVXPath();
        }
        else if (tipoOggetto.equals("PoolManager")) {
            aggiornaGVPoolManager();
        }
        else if (tipoOggetto.equals("Task")) {
            aggiornaGVTask();
        }
        else if (tipoOggetto.equals("ConcurrencyHandler")) {
            aggiornaGVConcurrencyManager();
        }
        else if (tipoOggetto.equals("CryptoHelper")) {
            aggiornaGVCryptoHelper();
        }
        else if (tipoOggetto.equals("Policy")) {
            aggiornaGVPolicy();
        }
    }

    private void aggiornaService(String nomeServizio, XMLUtils parser) throws Exception
    {
    	Node base = parser.selectSingleNode(serverXml, "/GVCore/GVServices/Services");
        Node serverService = getService(serverXml, nomeServizio);
        Node zipService = getService(newXml, nomeServizio);
        if (serverService != null) {
        	Node importedNode = base.getOwnerDocument().importNode(zipService, true);
        	base.replaceChild(importedNode, serverService);
            logger.debug("Nodo Service gia esistente aggiornamento");
        }
        else {
        	Node importedNode = base.getOwnerDocument().importNode(zipService, true);
        	base.appendChild(importedNode);
            logger.debug("Nodo Service non esistente inserimento");
        }

        Node resultsValidation = parser.selectSingleNode(zipService, ".//xml-validation-service");
        if (resultsValidation != null) {
            copiaFileXSD(parser);
        }
    }

    private void aggiornaGroup(String nomeServizio, XMLUtils parser) throws XMLUtilsException
    {
    	Node base = parser.selectSingleNode(serverXml, "/GVCore/GVServices/Groups");
        Node serverGroup = getGroup(serverXml, nomeServizio);
        Node zipGroup = getGroup(newXml, nomeServizio);
        if (serverGroup != null) {
        	Node importedNode = base.getOwnerDocument().importNode(zipGroup, true);
        	base.replaceChild(importedNode, serverGroup);
            logger.debug("Nodo Group gia esistente aggiornamento");
        }
        else {
        	Node importedNode = base.getOwnerDocument().importNode(zipGroup, true);
        	base.appendChild(importedNode);
            logger.debug("Nodo Group non esistente inserimento");
        }
    }

    private void aggiornaGVCryptoHelper(String nomeServizio, XMLUtils parser) throws Exception
    {
    	Node gvCryptoHelperZip = getGVCryptoHelper(newXml, nomeServizio);
        Node gvCryptoHelperServer = getGVCryptoHelper(serverXml, nomeServizio);
        if (gvCryptoHelperZip != null) {
            NodeList keyIDZip = parser.selectNodeList(gvCryptoHelperZip, "KeyID");
            for (int i = 0; i < keyIDZip.getLength(); i++) {
                String id = parser.get(keyIDZip.item(i), "@id");
                Node resultsKeyServer = parser.selectSingleNode(serverXml, "/GVCore/GVCryptoHelper/KeyID[@id='" + id + "']");
                if (resultsKeyServer == null) {
                	Node importedNode = gvCryptoHelperServer.getOwnerDocument().importNode(keyIDZip.item(i), true);
                	gvCryptoHelperServer.appendChild(importedNode);
                	logger.debug("Nodo KeyID[" + id + "] non esistente inserimento");
                }
                else {
                	Node importedNode = gvCryptoHelperServer.getOwnerDocument().importNode(keyIDZip.item(i), true);
                	gvCryptoHelperServer.replaceChild(importedNode, resultsKeyServer);
                	logger.debug("Nodo KeyID[" + id + "] gia esistente aggiornamento");
                }
                
                String ksid = parser.get(keyIDZip.item(i), "@key-store-id");
                NodeList gvKeyStoreIDZip = parser.selectNodeList(gvCryptoHelperZip, "KeyStoreID[@id='" + ksid + "']");
                Node resultsKeyStoServer = parser.selectSingleNode(gvCryptoHelperServer, "KeyStoreID[@id='" + ksid + "']");
                if (resultsKeyStoServer == null) {
                    Node importedNode = gvCryptoHelperServer.getOwnerDocument().importNode(gvKeyStoreIDZip.item(i), true);
                    gvCryptoHelperServer.appendChild(importedNode);
                    logger.debug("Nodo KeyStoreID[" + ksid + "] non esistente inserimento");
                }
                else {
                    Node importedNode = gvCryptoHelperServer.getOwnerDocument().importNode(gvKeyStoreIDZip.item(i), true);
                    gvCryptoHelperServer.replaceChild(importedNode, resultsKeyStoServer);
                    logger.debug("Nodo KeyStoreID[" + ksid + "] gia esistente aggiornamento");
                }
                copiaFileKeyStore(gvKeyStoreIDZip.item(i));
            }
        }
    }

    private void aggiornaGVPoolManager() throws XMLUtilsException
    {
        Node gvPoolManagerZip = getGVPoolManager(newXml);
        if (gvPoolManagerZip != null) {
        	XMLUtils parser = null;
        	try {
        		parser = XMLUtils.getParserInstance();

        		Node base = parser.selectSingleNode(serverXml, "/GVCore/GVPoolManager");
	        	NodeList greenVulcanoPool = parser.selectNodeList(gvPoolManagerZip, "GreenVulcanoPool");
	            for (int i = 0; i < greenVulcanoPool.getLength(); i++) {
	                String subsystem = parser.get(greenVulcanoPool.item(i), "@subsystem");
	                logger.debug("GreenVulcanoPool=" + subsystem);
	                Node resultsPoolServer = parser.selectSingleNode(serverXml, "/GVCore/GVPoolManager/GreenVulcanoPool[@subsystem='"
	                        + subsystem + "']");
	                if (resultsPoolServer == null) {
	                    Node importedNode = base.getOwnerDocument().importNode(greenVulcanoPool.item(i), true);
	                	base.appendChild(importedNode);
	                    logger.debug("Nodo GreenVulcanoPool non esistente inserimento");
	                }
	                else {
	                    Node importedNode = base.getOwnerDocument().importNode(greenVulcanoPool.item(i), true);
	                	base.replaceChild(importedNode, resultsPoolServer);
	                    logger.debug("Nodo GreenVulcanoPool gia esistente aggiornamento");
	                }
	            }
        	}
        	finally {
        		XMLUtils.releaseParserInstance(parser);
        	}
        }
    }

    private void aggiornaGVPolicy() throws XMLUtilsException
    {
        Node gvPolicyZip = getGVPolicy(newXml);
        if (gvPolicyZip != null) {
        	XMLUtils parser = null;
        	try {
        		parser = XMLUtils.getParserInstance();

        		Node base = parser.selectSingleNode(serverXml, "/GVCore");
	            Node resultsPolicyServer = parser.selectSingleNode(serverXml, "/GVCore/GVPolicy");
	            if (resultsPolicyServer == null) {
	                Node importedNode = base.getOwnerDocument().importNode(gvPolicyZip, true);
                	base.appendChild(importedNode);
                    logger.debug("Nodo GVPolicy non esistente inserimento");
	            }
	            else {
	                Node importedNode = base.getOwnerDocument().importNode(gvPolicyZip, true);
                	base.replaceChild(importedNode, resultsPolicyServer);
                    logger.debug("Nodo GVPolicy gia esistente aggiornamento");
	            }
        	}
        	finally {
        		XMLUtils.releaseParserInstance(parser);
        	}
        }
    }

    public void aggiornaGVXPath() throws XMLUtilsException
    {
    	XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();
    		
	        Node gvXPathZip = getGVXPath(newXml);
	        Node xPathZip = null;
	        if (gvXPathZip != null) {
	            xPathZip = parser.selectSingleNode(gvXPathZip, "XPath");
	        }
	        Node gvXPathServer = getGVXPath(serverXml);
	        Node xPathServer = null;
	        if (gvXPathServer != null) {
	            xPathServer = parser.selectSingleNode(gvXPathServer, "XPath");
	        }
	        if (xPathZip != null) {
	            NodeList gvXPathExtensionZip = parser.selectNodeList(xPathZip, "XPathExtension");
	            for (int i = 0; i < gvXPathExtensionZip.getLength(); i++) {
	                String function = parser.get(gvXPathExtensionZip.item(i), "@function-name");
	                Node resultsXPEServer = parser.selectSingleNode(xPathServer, "XPathExtension[@function-name='" + function + "']");
	                if (resultsXPEServer == null) {
	                    Node importedNode = xPathServer.getOwnerDocument().importNode(gvXPathExtensionZip.item(i), true);
	                    xPathServer.appendChild(importedNode);
	                    logger.debug("Nodo XPathExtension[" + function + "] non esistente inserimento");
	                }
	                else {
	                    Node importedNode = xPathServer.getOwnerDocument().importNode(gvXPathExtensionZip.item(i), true);
	                    xPathServer.replaceChild(importedNode, resultsXPEServer);
	                    logger.debug("Nodo XPathExtension[" + function + "] gia esistente aggiornamento");
	                }
	            }

	            NodeList gvXPathNamespaceZip = parser.selectNodeList(xPathZip, "XPathNamespace");
	            for (int i = 0; i < gvXPathNamespaceZip.getLength(); i++) {
	                String namespace = parser.get(gvXPathNamespaceZip.item(i), "@namespace");
	                Node resultsXPNServer = parser.selectSingleNode(xPathServer, "XPathNamespace[@namespace='" + namespace + "']");
	                if (resultsXPNServer == null) {
	                    Node importedNode = xPathServer.getOwnerDocument().importNode(gvXPathNamespaceZip.item(i), true);
	                    xPathServer.appendChild(importedNode);
	                    logger.debug("Nodo XPathNamespace[" + namespace + "] non esistente inserimento");
	                }
	                else {
	                    Node importedNode = xPathServer.getOwnerDocument().importNode(gvXPathNamespaceZip.item(i), true);
	                    xPathServer.replaceChild(importedNode, resultsXPNServer);
	                    logger.debug("Nodo XPathNamespace[" + namespace + "] gia esistente aggiornamento");
	                }
	            }
	        }
    	}
    	finally {
    		XMLUtils.releaseParserInstance(parser);
    	}
    }

    private void aggiornaGVForwards(String nomeServizio, XMLUtils parser) throws Exception
    {
    	Node base = parser.selectSingleNode(serverXml, "/GVCore/GVForwards");
        List<Node> forwardConfigurations = getGVForwards(newXml, nomeServizio);
        Map<String, String> hlistaDp = new Hashtable<String, String>();
        for (Node forwardConfiguration : forwardConfigurations) {
            String name = parser.get(forwardConfiguration, "@name");
            String forward = parser.get(forwardConfiguration, "@forwardName");
            Node resultsFwdServer = parser.selectSingleNode(serverXml, "/GVCore/GVForwards/ForwardConfiguration[@name='"
                    + name + "']");
            if (resultsFwdServer == null) {
                Node importedNode = base.getOwnerDocument().importNode(forwardConfiguration, true);
                base.appendChild(importedNode);
                logger.debug("Nodo ForwardConfiguration[" + name + "/" + forward + "] non esistente inserimento");
            }
            else {
                Node importedNode = base.getOwnerDocument().importNode(forwardConfiguration, true);
                base.replaceChild(importedNode, resultsFwdServer);
                logger.debug("Nodo ForwardConfiguration[" + name + "/" + forward + "] gia esistente aggiornamento");
            }
            String[] dps = getListDataProviderForward(newXml, forward);
            if (dps != null) {
                for (int i = 0; i < dps.length; i++) {
                    if (dps[i] != null) {
                        hlistaDp.put(dps[i], dps[i]);
                    }
                }
            }
        }
        Set<String> set = hlistaDp.keySet(); // get set-view of keys
        // get iterator
        String[] listDataProvider = new String[hlistaDp.size()];
        Iterator<String> itr = set.iterator();
        int i = 0;
        while (itr.hasNext()) {
            listDataProvider[i] = itr.next();
            logger.debug("ForwardConfiguration LISTA DP=" + listDataProvider[i]);
            i++;
        }
        aggiornaListaGVDataProvider(listDataProvider, nomeServizio);

    }

    public void aggiornaGVConcurrencyManager() throws XMLUtilsException
    {
        Node gvConcurrencyManagerZip = getGVConcurrencyHandler(newXml);
        Node gvConcurrencyManagerServer = getGVConcurrencyHandler(serverXml);
        if (gvConcurrencyManagerZip != null) {
        	XMLUtils parser = null;
        	try {
        		parser = XMLUtils.getParserInstance();
        		Node base = parser.selectSingleNode(serverXml, "/GVCore/GVConcurrencyHandler/SubSystems");

	            NodeList subSystems = parser.selectNodeList(gvConcurrencyManagerZip, "SubSystems/SubSystem");
	            for (int i = 0; i < subSystems.getLength(); i++) {
	                String subsystem = parser.get(subSystems.item(i), "@name");
	                Node resultsSubSysServer = parser.selectSingleNode(gvConcurrencyManagerServer, "SubSystems/SubSystem[@name='"
	                        + subsystem + "']");
	                if (resultsSubSysServer == null) {
	                    Node importedNode = base.getOwnerDocument().importNode(subSystems.item(i), true);
	                    base.appendChild(importedNode);
	                    logger.debug("Nodo SubSystem[" + subsystem + "] non esistente inserimento");
	                }
	                else {
	                	logger.debug("Nodo SubSystem[" + subsystem + "] gia esistente aggiornamento");
		                NodeList concurrentServices = parser.selectNodeList(subSystems.item(i), "ConcurrentService");
	                    for (int j = 0; j < concurrentServices.getLength(); j++) {
	                        String service = parser.get(concurrentServices.item(j), "@service");
	                        String system = parser.get(concurrentServices.item(j), "@system");
	                        Node resultsCSServer = null;
	                        if (system == null) {
	                        	resultsCSServer = parser.selectSingleNode(gvConcurrencyManagerServer, "SubSystems/SubSystem[@name="
	                                  + subsystem + "]/ConcurrentService[@service='" + service + "']");
	                        }
	                        else {
	                        	resultsCSServer = parser.selectSingleNode(gvConcurrencyManagerServer, "SubSystems/SubSystem[@name="
		                                  + subsystem + "]/ConcurrentService[@system='" + system + "' and @service='" + service + "']");
	                        }
	                        if (resultsCSServer == null) {
	                            Node importedNode = resultsSubSysServer.getOwnerDocument().importNode(concurrentServices.item(j), true);
	                            resultsSubSysServer.appendChild(importedNode);
	                            logger.debug("Nodo ConcurrentService[" + system + "/" + service + "] non esistente inserimento");
	                        }
	                        else {
	                            Node importedNode = resultsSubSysServer.getOwnerDocument().importNode(concurrentServices.item(j), true);
	                            resultsSubSysServer.replaceChild(importedNode, resultsCSServer);
	                            logger.debug("Nodo ConcurrentService[" +  system + "/" + service + "] gia esistente aggiornamento");
	                        }
	
	                    }
	                }
	            }
        	}
        	finally {
        		XMLUtils.releaseParserInstance(parser);
        	}
        }
    }

    public void aggiornaGVCryptoHelper() throws Exception
    {
        Node gvCryptoHelperZip = getGVCryptoHelper(newXml);
        Node gvCryptoHelperServer = getGVCryptoHelper(serverXml);
        if (gvCryptoHelperZip != null) {
        	XMLUtils parser = null;
        	try {
        		parser = XMLUtils.getParserInstance();
        		
	            NodeList gvKeyStoreIDZip = parser.selectNodeList(gvCryptoHelperZip, "KeyStoreID");
	            NodeList gvKeyIDZip = parser.selectNodeList(gvCryptoHelperZip, "KeyID");
	            for (int i = 0; i < gvKeyStoreIDZip.getLength(); i++) {
	                String ksid = parser.get(gvKeyStoreIDZip.item(i), "@id");
	                Node resultsKeyStoServer = parser.selectSingleNode(gvCryptoHelperServer, "KeyStoreID[@id='" + ksid + "']");
	                if (resultsKeyStoServer == null) {
                        Node importedNode = gvCryptoHelperServer.getOwnerDocument().importNode(gvKeyStoreIDZip.item(i), true);
                        gvCryptoHelperServer.appendChild(importedNode);
                        logger.debug("Nodo KeyStoreID[" + ksid + "] non esistente inserimento");
                    }
                    else {
                        Node importedNode = gvCryptoHelperServer.getOwnerDocument().importNode(gvKeyStoreIDZip.item(i), true);
                        gvCryptoHelperServer.replaceChild(importedNode, resultsKeyStoServer);
                        logger.debug("Nodo KeyStoreID[" +  ksid + "] gia esistente aggiornamento");
                    }
	                copiaFileKeyStore(gvKeyStoreIDZip.item(i));
	            }
	            for (int i = 0; i < gvKeyIDZip.getLength(); i++) {
	                String kid = parser.get(gvKeyIDZip.item(i), "@id");
	                Node resultsKeyServer = parser.selectSingleNode(gvCryptoHelperServer, "KeyID[@id='" + kid + "']");
	                if (resultsKeyServer == null) {
                        Node importedNode = gvCryptoHelperServer.getOwnerDocument().importNode(gvKeyIDZip.item(i), true);
                        gvCryptoHelperServer.appendChild(importedNode);
                        logger.debug("Nodo KeyID[" + kid + "] non esistente inserimento");
                    }
                    else {
                        Node importedNode = gvCryptoHelperServer.getOwnerDocument().importNode(gvKeyIDZip.item(i), true);
                        gvCryptoHelperServer.replaceChild(importedNode, resultsKeyServer);
                        logger.debug("Nodo KeyID[" +  kid + "] gia esistente aggiornamento");
                    }
	            }
        	}
        	finally {
        		XMLUtils.releaseParserInstance(parser);
        	}
        }
    }

    private void copiaFileKeyStore(Node ksid) throws Exception
    {
        String ksn = XMLUtils.get_S(ksid, "@key-store-name");
        logger.debug("Prepare copy keystore file: " + ksn);
        String path = java.lang.System.getProperty("java.io.tmpdir");
        String input = path + File.separator + "conf" + File.separator + "keystores" + File.separator + ksn;
        String output = java.lang.System.getProperty("gv.app.home") + File.separator + "keystores" + File.separator
                + ksn;

        copiaFile(input, output);
    }

    private void aggiornaGVBufferDump(String nomeServizio, XMLUtils parser) throws XMLUtilsException
    {
        Node gvDataDumpZip = getGVBufferDump(newXml, nomeServizio);
        Node gvDataDumpServer = getGVBufferDump(serverXml, nomeServizio);
        Node base = parser.selectSingleNode(serverXml, "/GVCore/GVBufferDump");
        if (gvDataDumpZip != null) {
        	if (gvDataDumpServer == null) {
                Node importedNode = base.getOwnerDocument().importNode(gvDataDumpZip, true);
                base.appendChild(importedNode);
                logger.debug("Nodo ServiceDump[" + nomeServizio + "] non esistente inserimento");
            }
            else {
                Node importedNode = base.getOwnerDocument().importNode(gvDataDumpZip, true);
                base.replaceChild(importedNode, gvDataDumpServer);
                logger.debug("Nodo ServiceDump[" +  nomeServizio + "] gia esistente aggiornamento");
            }
        }
    }

    private void aggiornaListaGVDataProvider(String[] listaDp, String nomeServizio) throws Exception
    {
        if (listaDp.length > 0) {
            for (int i = 0; i < listaDp.length; i++) {
                if (!adapterParser.getExist("GVDP", listaDp[i])) {
                    adapterParser.aggiorna("GVDP", listaDp[i]);
                }
            }
            adapterParser.scriviFile();
        }
    }

    private void aggiornaGVWebServices(String nomeServizio,XMLUtils parser) throws Exception
    {
        String xPathOperation = "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
                + "']/Operation";
        NodeList listOperazioni = parser.selectNodeList(newXml, xPathOperation);

        if (listOperazioni.getLength() > 0) {
            String[] listOp = new String[listOperazioni.getLength()];
            for (int i = 0; i < listOperazioni.getLength(); i++) {
                Node operation = listOperazioni.item(i);
                String operazione = parser.get(operation, "@name");
                if ("Forward".equals(operazione)) {
                    operazione = parser.get(operation, "@forward-name");
                }
                listOp[i] = operazione;
            }
            adapterParser.aggiornaGreenVulcanoWebServices(nomeServizio, listOp);
            adapterParser.scriviFile();
        }
    }

    private void aggiornaListaGVKnowledgeBaseConfig(String[] listaKwB, String nomeServizio) throws Exception
    {
        if (listaKwB.length > 0) {
            for (int i = 0; i < listaKwB.length; i++) {
                if (!adapterParser.getExist("RULES_CFG", listaKwB[i])) {
                    adapterParser.aggiorna("RULES_CFG", listaKwB[i]);
                }
            }
            adapterParser.scriviFile();
        }
    }


    private void aggiornaOdeProcess(String nomeServizio, XMLUtils parser) throws Exception
    {
        String xPathOdeProcess = "/GVCore/GVServices/Services/Service[@id-service='" + nomeServizio
                + "']/BpelOperation";
        NodeList odeProcess = parser.selectNodeList(newXml, xPathOdeProcess);
        String xPathDirProcess = "/GVCore/GVServices/BpelEngineConfiguration/@deployMentUnitProcess";
        if (odeProcess.getLength() > 0) {
            String dirProcPath = parser.get(serverXml, xPathDirProcess, "${{gv.app.home}}" + File.separator + "BpelProcess"); 
            for (int i = 0; i < odeProcess.getLength(); i++) {
            	Node np = odeProcess.item(i);
                String opName = parser.get(np, "@name");
                if (opName.equals("Forward")) {
                	opName = parser.get(np, "@forward-name");
                }
                String procName = parser.get(np, "BpelFlow/@processname"); 
                String dirName = nomeServizio + "_" + opName + "_" + procName;
                copiaFileProcess(dirProcPath,dirName);
            }
        }
    }

    private void aggiornaGVDataProviderService(String nomeServizio, XMLUtils parser) throws Exception
    {
        String[] listaDp = getListDataProvider(newXml, nomeServizio);
        aggiornaListaGVDataProvider(listaDp, nomeServizio);
    }

    private void aggiornaGVKnowledgeBaseConfigService(String nomeServizio, XMLUtils parser) throws Exception
    {
        String[] listaKwB = getListKnowledgeBaseConfig(newXml, nomeServizio);
        aggiornaListaGVKnowledgeBaseConfig(listaKwB, nomeServizio);
    }

    private void aggiornaGVDataTransformationForService(String nomeServizio, XMLUtils parser) throws Exception
    {
        String[] listaTrasformazioni = getListaTrasformazioni(newXml, nomeServizio);

        if ((listaTrasformazioni != null) && (listaTrasformazioni.length > 0)) {
            Node gvDataTransformationZip = parser.selectSingleNode(newXml, "/GVCore/GVDataTransformation");
            if (gvDataTransformationZip != null) {
            	Node baseDSS = parser.selectSingleNode(serverXml, "/GVCore/GVDataTransformation/DataSourceSets");
                NodeList dataSourceSets = parser.selectNodeList(gvDataTransformationZip, "DataSourceSets/DataSourceSet");
                for (int i = 0; i < dataSourceSets.getLength(); i++) {
                    String name = parser.get(dataSourceSets.item(i), "@name");
                    Node resultsDSSServer = parser.selectSingleNode(baseDSS, "DataSourceSet[@name='" + name + "']");
                    if (resultsDSSServer == null) {
                        Node importedNode = baseDSS.getOwnerDocument().importNode(dataSourceSets.item(i), true);
                        baseDSS.appendChild(importedNode);
                        logger.debug("Nodo DataSourceSet[" + name + "] non esistente inserimento");
                    }
                    else {
                        Node importedNode = baseDSS.getOwnerDocument().importNode(dataSourceSets.item(i), true);
                        baseDSS.replaceChild(importedNode, resultsDSSServer);
                        logger.debug("Nodo DataSourceSet[" +  name + "] gia esistente aggiornamento");
                    }
                }
                
                Node baseDT = parser.selectSingleNode(serverXml, "/GVCore/GVDataTransformation/Transformations");
                for (int i = 0; i < listaTrasformazioni.length; i++) {
                    String name = listaTrasformazioni[i];
                    aggiornaSingleTransformation(name, gvDataTransformationZip, baseDT, parser);
                }
            }
        }
    }

    public void aggiornaGVDataTransformationForDH(String nomeTrasformazione, XMLUtils parser) throws Exception
    {
        if ((nomeTrasformazione != null) && !"".equals(nomeTrasformazione)) {
        	Node gvDataTransformationZip = parser.selectSingleNode(newXml, "/GVCore/GVDataTransformation");
            if (gvDataTransformationZip != null) {
            	Node baseDSS = parser.selectSingleNode(serverXml, "/GVCore/GVDataTransformation/DataSourceSets");
                NodeList dataSourceSets = parser.selectNodeList(gvDataTransformationZip, "DataSourceSets/DataSourceSet");
                for (int i = 0; i < dataSourceSets.getLength(); i++) {
                    String name = parser.get(dataSourceSets.item(i), "@name");
                    Node resultsDSSServer = parser.selectSingleNode(baseDSS, "DataSourceSet[@name='" + name + "']");
                    if (resultsDSSServer == null) {
                        Node importedNode = baseDSS.getOwnerDocument().importNode(dataSourceSets.item(i), true);
                        baseDSS.appendChild(importedNode);
                        logger.debug("Nodo DataSourceSet[" + name + "] non esistente inserimento");
                    }
                    else {
                        Node importedNode = baseDSS.getOwnerDocument().importNode(dataSourceSets.item(i), true);
                        baseDSS.replaceChild(importedNode, resultsDSSServer);
                        logger.debug("Nodo DataSourceSet[" +  name + "] gia esistente aggiornamento");
                    }
                }
                
                Node baseDT = parser.selectSingleNode(serverXml, "/GVCore/GVDataTransformation/Transformations");
                aggiornaSingleTransformation(nomeTrasformazione, gvDataTransformationZip, baseDT, parser);
            }
        }
    }

    private void aggiornaSingleTransformation(String name, Node gvDataTransformationZip, Node baseDT, XMLUtils parser)
            throws Exception
    {
        Node gvDTZip = parser.selectSingleNode(gvDataTransformationZip, "Transformations/*[@type='transformation' and @name='" + name + "']");
        if (gvDTZip != null) {
        	Node gvDTServer = parser.selectSingleNode(baseDT, "*[@type='transformation' and @name='" + name + "']");
            String dtType = gvDTZip.getLocalName();
        	if (gvDTServer == null) {
                Node importedNode = baseDT.getOwnerDocument().importNode(gvDTZip, true);
                baseDT.appendChild(importedNode);
                logger.debug("Nodo " + dtType + "[" + name + "] non esistente inserimento");
            }
            else {
                Node importedNode = baseDT.getOwnerDocument().importNode(gvDTZip, true);
                baseDT.replaceChild(importedNode, gvDTServer);
                logger.debug("Nodo " + dtType + "[" +  name + "] gia esistente aggiornamento");
            }
            if ("XSLTransformation".equals(dtType) ||
            	"XSLFOPTransformation".equals(dtType) ||
        		"HL72XMLTransformation".equals(dtType) ||
        		"XML2HL7Transformation".equals(dtType)) {
            	copiaFileXsl(gvDTZip, parser);
            }
            else if ("SequenceTransformation".equals(dtType)) {
            	NodeList seqElements = parser.selectNodeList(gvDTZip, "SequenceElement");
                for (int i = 0; i < seqElements.getLength(); i++) {
                    String dtName = parser.get(seqElements.item(i), "@Transformer");
                    aggiornaSingleTransformation(dtName, gvDataTransformationZip, baseDT, parser);
                }
            }
            else if ("AddTagTransformation".equals(dtType) || 
                    "CSV2XMLTransformation".equals(dtType) ||
                    "Base64Transformation".equals(dtType) ||
                    "OverWriteBytesTransformation".equals(dtType) ||
                    "ChangeCharTransformation".equals(dtType)) {
              	// do nothing
            }
            else if ("CryptoTransformation".equals(dtType)) {
               	// do nothing
            }
            else if ("XQTransformation".equals(dtType)) {
            	copiaFileXQ(gvDTZip, parser);
            }
            else if ("Bin2XMLTransformation".equals(dtType) ||
            		 "XML2BinTransformation".equals(dtType)) {
            	copiaFileBin(gvDTZip, parser);
            }
            else {
            	// do nothing
            }
            //aggiornaXSDTransformations(gvDataTransformationZip, nomeTrasformazione);
        }
    }

    private void aggiornaXSDTransformations(Node gvDataTransformationZip, XMLUtils parser)
            throws Exception
    {
        copiaFileXSD(parser);
    }

    private void aggiornaGVTask() throws Exception
    {
        logger.debug("aggiornaGVTask");
        XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();
   	
	        Node gvTaskConfigurationZip = getGVTask(newXml);
	        Node gvTaskConfigurationServer = getGVTask(serverXml);
	        if (gvTaskConfigurationZip != null) {
	            if (gvTaskConfigurationServer == null) {
	                Node base = parser.selectSingleNode(serverXml, "GVCore");
		            Node importedNode = base.getOwnerDocument().importNode(gvTaskConfigurationZip, true);
		            gvTaskConfigurationServer = base.appendChild(importedNode);
	                logger.debug("aggiornaGVTask - Copied all configuration data");
	                return;
	            }
	            Node schedulerZip = parser.selectSingleNode(gvTaskConfigurationZip, "GVSchedulerBuilder");
	            Node schedulerServer = parser.selectSingleNode(gvTaskConfigurationServer, "GVSchedulerBuilder");
	            if (schedulerServer == null) {
	                Node importedNode = gvTaskConfigurationServer.getOwnerDocument().importNode(schedulerZip, true);
	                gvTaskConfigurationServer.appendChild(importedNode);
	                logger.debug("Nodo GVSchedulerBuilder non esistente inserimento");
	            }
	            else {
	                Node importedNode = gvTaskConfigurationServer.getOwnerDocument().importNode(schedulerZip, true);
	                gvTaskConfigurationServer.replaceChild(importedNode, schedulerServer);
	                logger.debug("Nodo GVSchedulerBuilder gia esistente aggiornamento");
	            }
	            Node groupsZip = parser.selectSingleNode(gvTaskConfigurationZip, "TaskGroups");
	            Node groupsServer = parser.selectSingleNode(gvTaskConfigurationServer, "TaskGroups");
	            if (groupsServer == null) {
	            	Node importedNode = gvTaskConfigurationServer.getOwnerDocument().importNode(groupsZip, true);
	                gvTaskConfigurationServer.appendChild(importedNode);
	                logger.debug("aggiornaGVTask - Copied all TaskGroups data");
	                return;
	            }
	            NodeList groupListZip = parser.selectNodeList(groupsZip, "TaskGroup");
	            for (int i = 0; i < groupListZip.getLength(); i++) {
	                String name = parser.get(groupListZip.item(i), "@name");
	                Node groupServer = parser.selectSingleNode(groupsServer, "TaskGroup[@name='" + name + "']");
	                logger.debug("Processing TaskGroup[" + name + "]");
	                if (groupServer == null) {
	                	Node importedNode = groupsServer.getOwnerDocument().importNode(groupListZip.item(i), true);
	                	groupsServer.appendChild(importedNode);
	                    logger.debug("aggiornaGVTask - Copied all TaskGroup[" + name + "] data");
	                    continue;
	                }
	                NodeList taskZip = parser.selectNodeList(groupListZip.item(i), "*[@type='task']");
	                for (int k = 0; k < taskZip.getLength(); k++) {
	                    Node taskZipK = taskZip.item(k);
	                    String nameTaskZip = parser.get(taskZipK, "@name");
	                    logger.debug("taskZip[" + k + "]=" + nameTaskZip + "\n" + parser.serializeDOM(taskZipK));
	                    Node taskServer = parser.selectSingleNode(groupServer, "*[@name='" + nameTaskZip + "']");
	                    if (taskServer != null) {
	                    	Node importedNode = groupServer.getOwnerDocument().importNode(taskZipK, true);
		                	groupServer.replaceChild(importedNode, taskServer);
	                        logger.debug("Nodo Task[" + name + "/" + nameTaskZip + "] esistente, aggiornamento");
	                    }
	                    else {
	                    	Node importedNode = groupServer.getOwnerDocument().importNode(taskZipK, true);
		                	groupServer.appendChild(importedNode);
	                        logger.debug("Nodo Task[" + name + "/" + nameTaskZip + "] non esistente, inserimento");
	                    }
	                }
	            }
	        }    	
        }
    	finally {
    		XMLUtils.releaseParserInstance(parser);
    	}

    }

    private void aggiornaSystem(String nomeServizio, XMLUtils parser) throws Exception
    {
        Map<String, Node> mapListaSistemi = getListaSistemi(nomeServizio);
        String[] listaSistemi = new String[mapListaSistemi.keySet().size()];
        mapListaSistemi.keySet().toArray(listaSistemi);
        logger.debug("size listaSistemi = " + listaSistemi.length);
        
        Node base = parser.selectSingleNode(serverXml, "/GVCore/GVSystems/Systems");
        for (int i = 0; i < listaSistemi.length; i++) {
            String sistema = listaSistemi[i];
            logger.debug("SISTEMA = " + sistema);
            Node resultsSysServer = parser.selectSingleNode(serverXml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema + "']");
            Node resultsSysZip = parser.selectSingleNode(newXml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema + "']");
            if (resultsSysServer == null) {
            	Node importedNode = base.getOwnerDocument().importNode(resultsSysZip, true);
            	base.appendChild(importedNode);
                logger.debug("Nodo System[" + sistema + "] non esistente, inserimento");
                handleVCLOperationDetails(importedNode, parser);
            }
            else {
                logger.debug("Nodo System[" + sistema + "] esistente, aggiornamento");
                String channelName = parser.get(mapListaSistemi.get(sistema), "@id-channel");
                Node resChZip = parser.selectSingleNode(newXml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema
                        + "']/Channel[@id-channel='" + channelName + "']");
                if (resChZip != null) {
                    Node resChServer = parser.selectSingleNode(serverXml, "/GVCore/GVSystems/Systems/System[@id-system='" + sistema
                            + "']/Channel[@id-channel='" + channelName + "']");
                	if (resChServer == null) {
                    	Node importedNode = resultsSysServer.getOwnerDocument().importNode(resChZip, true);
                    	resultsSysServer.appendChild(importedNode);
                        logger.debug("Nodo Channel[" + channelName + "] non esistente, inserimento");
                        handleVCLOperationDetails(importedNode, parser);
                    }
                	else {
                		logger.debug("Nodo Channel[" + channelName + "] esistente, aggiornamento");
                        NodeList operZip = parser.selectNodeList(resChZip, "*[@type='call' or @type='enqueue' or @type='dequeue']");
                        for (int k = 0; k < operZip.getLength(); k++) {
                            Node operZipK = operZip.item(k);
                            String nameOperZip = parser.get(operZipK, "@name");
                            logger.debug("operZip[" + k + "]=" + operZipK + " - " + nameOperZip);
                            Node operServer = parser.selectSingleNode(resChServer, "*[@name='" + nameOperZip + "']");
                            if (operServer != null) {
                            	Node importedNode = resChServer.getOwnerDocument().importNode(operZipK, true);
                            	resChServer.replaceChild(importedNode, operServer);
                                logger.debug("Nodo VCLOperation[" + nameOperZip + "] esistente, aggiornamento");
                            }
                            else {
                            	Node importedNode = resChServer.getOwnerDocument().importNode(operZipK, true);
                            	resChServer.appendChild(importedNode);
                                logger.debug("Nodo VCLOperation[" + nameOperZip + "] non esistente, inserimento");
                            }
                        }
                        handleVCLOperationDetails(resChServer, parser);
                    }
                }
            }
        }
    }

    private void handleVCLOperationDetails(Node base, XMLUtils parser) throws Exception
    {
        // handle DH service's transformations
        NodeList dbos = parser.selectNodeList(base, ".//*[@type='dbo']");

        if (dbos.getLength() > 0) {
            for (int j = 0; j < dbos.getLength(); j++) {
                String nameDBO = parser.get(dbos.item(j), "@name");
                logger.debug("DBO[" + j + "]=" + nameDBO);
                String dt = parser.get(dbos.item(j), "@transformation");
                if (dt != null) {
                    aggiornaGVDataTransformationForDH(dt, parser);
                }
            }
        }

        // handle WS service's WSDL
        NodeList wsdl = parser.selectNodeList(base, ".//*[@type='wsdlinfo']");

        if (wsdl != null) {
            for (int j = 0; j < wsdl.getLength(); j++) {
                String nameWSDL = parser.get(wsdl.item(j), "@wsdl");
                nameWSDL = nameWSDL.substring(nameWSDL.lastIndexOf("/")+1);
                logger.debug("WSDL[" + j + "]=" + nameWSDL);
                copiaFileWSDL(nameWSDL);
            }
        }
    }

    private Document getXmlZip() throws Exception
    {
        String path = java.lang.System.getProperty("java.io.tmpdir");
        return parseXmlFile(path + File.separator + "conf" + File.separator + "GVCore.xml");
    }

    private Document getXmlServer() throws Exception, XMLConfigException
    {
        URL url = getUrl();
        //return parseXmlURL(url);
        return parseXmlFile(url.getFile());
    }

    public void scriviFile() throws IOException
    {
    	DOMWriter domWriter = new DOMWriter();
        FileOutputStream ostream = new FileOutputStream(url.getFile());
        domWriter.write(serverXml, ostream);
        ostream.flush();
        ostream.close();
        logger.debug("Scrittura File" + url.getFile());
    }

    private URL getUrl() throws XMLConfigException
    {
        url = XMLConfig.getURL("GVCore.xml");
        logger.debug("URL=" + url.getPath());
        return url;
    }

    private Document parseXmlFile(String xmlFilePath) throws Exception
    {
    	FileInputStream poFile = null;
    	try {
    		poFile = new FileInputStream(xmlFilePath);
    		Document doc = XMLUtils.parseDOM_S(poFile, false, true);
    		return doc;
    	}
    	finally {
    		if (poFile != null) {
    			poFile.close();
    		}
    	}
    }

    private Document parseXmlURL(URL url) throws Exception
    {
    	InputStream poFile = null;
    	try {
    		poFile = url.openStream();
    		Document doc = XMLUtils.parseDOM_S(poFile, false, true);
    		return doc;
    	}
    	finally {
    		if (poFile != null) {
    			poFile.close();
    		}
    	}
    }

    private Document parseXmlString(String xmlNew) throws XMLUtilsException
    {
    	Document doc = XMLUtils.parseDOM_S(xmlNew, false, true);
        return doc;
    }

    public void sostituisciVariabili(VariabiliGlobali[] variabiliGlobali) throws XMLUtilsException
    {
        String strNewXml = newXml.toString();
        for (int i = 0; i < variabiliGlobali.length; i++) {
            logger.debug(variabiliGlobali[i].getNome() + "=" + variabiliGlobali[i].getValore());
            strNewXml = strNewXml.replaceAll("%%" + variabiliGlobali[i].getNome() + "%%",
                    variabiliGlobali[i].getValore());
        }
        newXml = parseXmlString(strNewXml);
    }

    private void copiaFile(String fileOrigine, String fileDestinazione) throws Exception
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
        }
        finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    private void copiaFileXsl(Node xslTransformation, XMLUtils parser) throws Exception
    {
        String path = java.lang.System.getProperty("java.io.tmpdir");
        String nomeFile = parser.get(xslTransformation, "@XSLMapName", parser.get(xslTransformation, "@InputXSLMapName", parser.get(xslTransformation, "@OutputXSLMapName")));
        String nomeDataSourceSet = parser.get(xslTransformation, "@DataSourceSet", "Default");
        logger.debug("Prepare XSL file for DT= " + parser.get(xslTransformation, "@name") + " - "
                + nomeFile + " - " + nomeDataSourceSet);
        
        String pathZip = parser.get(newXml, "/GVCore/GVDataTransformation/DataSourceSets/DataSourceSet[@name='"
                + nomeDataSourceSet + "']/LocalFSDataSource[@formatHandled='xsl']/@repositoryHome");
        pathZip = pathZip.substring(pathZip.indexOf("/"));
        logger.debug("pathZip  = " + pathZip);
        String inputFile = path + File.separator + "conf" + File.separator + pathZip + File.separator + nomeFile;
        logger.debug("inputFile  = " + inputFile);
        File fin = new File(inputFile);
        if (fin.exists()) {
            String pathServer = parser.get(serverXml, "/GVCore/GVDataTransformation/DataSourceSets/DataSourceSet[@name='"
                    + nomeDataSourceSet + "']/LocalFSDataSource[@formatHandled='xsl']/@repositoryHome");
            String outputFile = pathServer + File.separator + nomeFile;
            logger.debug("outputFile = " + outputFile);
            copiaFile(inputFile, outputFile);

            // copy gvxdt model
            String inputModel = inputFile.substring(0, inputFile.indexOf(".xsl")) + ".gvxdt";
            String outputModel = outputFile.substring(0, outputFile.indexOf(".xsl")) + ".gvxdt";
            File fm = new File(inputModel);
            if (fm.exists()) {
                copiaFile(inputModel, outputModel);
            }
        }
    }

    private void copiaFileXQ(Node xqTransformation, XMLUtils parser) throws Exception
    {
        String path = java.lang.System.getProperty("java.io.tmpdir");
        String nomeFile = parser.get(xqTransformation, "@XQMapName");
        String nomeDataSourceSet = parser.get(xqTransformation, "@DataSourceSet", "Default");
        logger.debug("Prepare XQ file for DT= " + parser.get(xqTransformation, "@name") + " - "
                + nomeFile + " - " + nomeDataSourceSet);
        
        String pathZip = parser.get(newXml, "/GVCore/GVDataTransformation/DataSourceSets/DataSourceSet[@name='"
                + nomeDataSourceSet + "']/LocalFSDataSource[@formatHandled='xq']/@repositoryHome");
        pathZip = pathZip.substring(pathZip.indexOf("/"));
        logger.debug("pathZip  = " + pathZip);
        String inputFile = path + File.separator + "conf" + File.separator + pathZip + File.separator + nomeFile;
        logger.debug("inputFile  = " + inputFile);
        File fin = new File(inputFile);
        if (fin.exists()) {
            String pathServer = parser.get(serverXml, "/GVCore/GVDataTransformation/DataSourceSets/DataSourceSet[@name='"
                    + nomeDataSourceSet + "']/LocalFSDataSource[@formatHandled='xq']/@repositoryHome");
            String outputFile = pathServer + File.separator + nomeFile;
            logger.debug("outputFile = " + outputFile);
            copiaFile(inputFile, outputFile);
        }
    }

    private void copiaFileBin(Node b2xTransformation, XMLUtils parser) throws Exception
    {
    	String path = java.lang.System.getProperty("java.io.tmpdir");
        String nomeFile = parser.get(b2xTransformation, "@ConversionMapName");
        String nomeDataSourceSet = parser.get(b2xTransformation, "@DataSourceSet", "Default");
        logger.debug("Prepare B2X/X2B file for DT= " + parser.get(b2xTransformation, "@name") + " - "
                + nomeFile + " - " + nomeDataSourceSet);
        
        String pathZip = parser.get(newXml, "/GVCore/GVDataTransformation/DataSourceSets/DataSourceSet[@name='"
                + nomeDataSourceSet + "']/LocalFSDataSource[@formatHandled='bin']/@repositoryHome");
        pathZip = pathZip.substring(pathZip.indexOf("/"));
        logger.debug("pathZip  = " + pathZip);
        String inputFile = path + File.separator + "conf" + File.separator + pathZip + File.separator + nomeFile;
        logger.debug("inputFile  = " + inputFile);
        File fin = new File(inputFile);
        if (fin.exists()) {
            String pathServer = parser.get(serverXml, "/GVCore/GVDataTransformation/DataSourceSets/DataSourceSet[@name='"
                    + nomeDataSourceSet + "']/LocalFSDataSource[@formatHandled='bin']/@repositoryHome");
            String outputFile = pathServer + File.separator + nomeFile;
            logger.debug("outputFile = " + outputFile);
            copiaFile(inputFile, outputFile);
        }
    }

    private void copiaFileXSD(XMLUtils parser) throws Exception
    {
        logger.debug("Prepare XSD for transformation validation");
        String path = java.lang.System.getProperty("java.io.tmpdir");
        String input = path + File.separator + "conf" + File.separator + "gvdte" + File.separator + "xsd";
        String output = null;
        File fin = new File(input);
        if (fin.exists()) {
            String nomeDataSourceSet = "Default";
            String pathServer = parser.get(serverXml, "/GVCore/GVDataTransformation/DataSourceSets/DataSourceSet[@name='"
                    + nomeDataSourceSet + "']/LocalFSDataSource[@formatHandled='bin']/@repositoryHome");
            output = PropertiesHandler.expand(pathServer, null);
            FileManager.cp(input, output, ".*");
        }

        logger.debug("Prepare XSD for validation only");
        input = path + File.separator + "conf" + File.separator + "xsds";
        fin = new File(input);
        if (fin.exists()) {
            output = PropertiesHandler.expand("${{gv.app.home}}" + File.separator + "xmlconfig" + File.separator
                    + "xsds", null);
            FileManager.cp(input, output, ".*");
        }
    }

    private void copiaFileProcess(String dirName) throws Exception
    {
        logger.debug("dirName  = " + dirName);
        String outputDir = PropertiesHandler.expand("${{gv.app.home}}" + File.separator + "xmlconfig" + File.separator
                + "BpelProcess", null);
        File nomeProcessDir = new File(outputDir, dirName);
        nomeProcessDir.mkdirs();
        String strPath = java.lang.System.getProperty("java.io.tmpdir");
        String inputFile = strPath + File.separator + "conf" + File.separator + "BpelProcess" + File.separator
                + dirName;
        logger.debug("inputFile  = " + inputFile);
        File pathBpel = new File(inputFile);
        File[] files = pathBpel.listFiles();
        logger.debug("file  = " + files.toString());
        logger.debug("num file  = " + files.length);
        for (File file : files) {
            copiaFile(file.getAbsolutePath(), outputDir + File.separator + dirName + File.separator + file.getName());
        }
    }

    private void copiaFileProcess(String pathProcess, String dirName) throws Exception
    {
        logger.debug("pathProcess  = " + pathProcess);
        logger.debug("dirName  = " + dirName);
        String outputDir = PropertiesHandler.expand(pathProcess, null);
        File nomeProcessDir = new File(outputDir, dirName);
        nomeProcessDir.mkdirs();
        String strPath = java.lang.System.getProperty("java.io.tmpdir");
        String inputFile = strPath + File.separator + "conf" + File.separator + "BpelProcess" + File.separator
                + dirName;
        logger.debug("inputFile  = " + inputFile);
        File pathBpel = new File(inputFile);
        File[] files = pathBpel.listFiles();
        logger.debug("files  = " + Arrays.toString(files));
        logger.debug("num file  = " + files.length);
        for (File file : files) {
            copiaFile(file.getAbsolutePath(), outputDir + File.separator + dirName + File.separator + file.getName());
        }
    }

    
    private void copiaFileWSDL(String nomeFile) throws Exception
    {
        logger.debug("inputFile  = " + nomeFile);
        String path = java.lang.System.getProperty("java.io.tmpdir");
        String inputFile = path + File.separator + "conf" + File.separator + "wsdl" + File.separator + nomeFile;
        File fin = new File(inputFile);
        if (fin.exists()) {
            String outputFile = PropertiesHandler.expand("${{gv.app.home}}" + File.separator + "xmlconfig"
                    + File.separator + "wsdl" + File.separator + nomeFile, null);
            logger.debug("inputFile  = " + nomeFile);
            logger.debug("outputFile = " + outputFile);
            copiaFile(inputFile, outputFile);
        }
    }

    public static void main(String[] args)
    {
        try {
            GVCoreParser parser = new GVCoreParser();
            parser.loadParser();

            // parser.aggiornaGVDataTransformation("provaXslt");
            // GVCoreParser.copiaFile("C:\\Documents and Settings\\cromano\\Desktop\\conf/Accounts_All_Insert.xsl",
            // "C:\\GreenV\\gvdte\\datasource/Accounts_All_Insert.xsl");
            // parser.loadParser();
            // parser.aggiorna("TOUPPER");
            // parser.getGvCoreZip("TOUPPER");
            // logger.debug(parser.getListaServiziZip()");
            /*
             * logger.debug(parser.getEqual("TOUPPER"));
             * logger.debug(parser.getExist("TOUPPER"));
             * parser.getGvCoreZip("TOUPPER"); parser.aggiorna("TOUPPER");
             * parser.scriviFile();
             */
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}

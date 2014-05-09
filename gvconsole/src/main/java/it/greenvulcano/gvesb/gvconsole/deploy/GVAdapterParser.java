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
import java.util.Arrays;

import max.xml.DOMWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * GVAdapterParser class
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GVAdapterParser
{

    /**
     * @param
     */
    private URL           url        = null;
    private Document      serverXml  = null;
    private Document      newXml     = null;
    private GVCoreParser  coreParser = null;

    private static Logger logger     = GVLogger.getLogger(GVAdapterParser.class);

    /**
     * @throws IOException
     * @throws XMLConfigException
     */
    public void loadParser() throws Exception
    {
        logger.debug("init load Parser GVAdapterParser");
        newXml = getXmlZip();
        serverXml = getXmlServer();
    }

    public void setCoreParser(GVCoreParser coreParser) {
		this.coreParser = coreParser;
	}

    /**
     * @param nomeAdapter
     * @return
     * @throws XMLUtilsException 
     */
    public boolean getExist(String nomeAdapter, String nomeServizio) throws XMLUtilsException
    {
        boolean ret = false;
        if (nomeAdapter.equals("WEB_SERVICES")) {
            ret = getExistWebServices(nomeServizio);
        }
        else if (nomeAdapter.equals("GVDP")) {
            ret = getExistDataProvider(nomeServizio);
        }
        else if (nomeAdapter.equals("RULES_CFG")) {
            ret = getExistKnowledgeBaseConfig(nomeServizio);
        }
        else if (nomeAdapter.equals("EXCEL_WORK")) {
            ret = getExistGVExcelWorkbook(nomeServizio);
        }
        else if (nomeAdapter.equals("EXCEL_REPO")) {
            ret = getExistGVExcelRepo(nomeServizio);
        }
        else if (nomeAdapter.equals("BIRT_REPO")) {
            ret = getExistGVBirtRepo(nomeServizio);
        }
        else if (nomeAdapter.equals("DH_ENGINE")) {
            ret = getExistGVDataHandler(nomeServizio);
        }
        else if (nomeAdapter.equals("HL7_LISTENERS")) {
            ret = getExistGVHL7(nomeServizio);
        }
        else if (nomeAdapter.equals("HTTP_ADAPTER")) {
            ret = getExistGVHTTP(nomeServizio);
        }
        else {
            ret = getExistObject("/GVAdapters/*[@name='" + nomeAdapter + "']");
        }
        return ret;
    }

    /**
     * @param nomeAdapter
     * @return
     * @throws XMLUtilsException 
     */
    public boolean getEqual(String nomeAdapter, String nomeServizio) throws XMLUtilsException
    {
        boolean ret = false;
        if (nomeAdapter.equals("WEB_SERVICES")) {
            ret = getEqualWebServices(nomeServizio);
        }
        else if (nomeAdapter.equals("GVDP")) {
            ret = getEqualDataProvider(nomeServizio);
        }
        else if (nomeAdapter.equals("RULES_CFG")) {
            ret = getEqualKnowledgeBaseConfig(nomeServizio);
        }
        else if (nomeAdapter.equals("EXCEL_WORK")) {
            ret = getEqualGVExcelWorkbook(nomeServizio);
        }
        else if (nomeAdapter.equals("EXCEL_REPO")) {
            ret = getEqualGVExcelRepo(nomeServizio);
        }
        else if (nomeAdapter.equals("BIRT_REPO")) {
            ret = getEqualGVBirtRepo(nomeServizio);
        }
        else if (nomeAdapter.equals("DH_ENGINE")) {
            ret = getEqualGVDataHandler(nomeServizio);
        }
        else if (nomeAdapter.equals("HL7_LISTENERS")) {
            ret = getEqualGVHL7(nomeServizio);
        }
        else if (nomeAdapter.equals("HTTP_ADAPTER")) {
            ret = getEqualGVHTTP(nomeServizio);
        }
        else {
            ret = getEqualObject("/GVAdapters/*[@name='" + nomeAdapter + "']");
        }
        return ret;
    }

    private boolean getExistDataProvider(String dataProvider) throws XMLUtilsException
    {
        return getExistObject("/GVAdapters/GVDataProviderManager/DataProviders/*[@name='" + dataProvider + "']");
    }

    private boolean getExistKnowledgeBaseConfig(String kwBase) throws XMLUtilsException
    {
        return getExistObject("/GVAdapters/GVRulesConfigManager/*[@type='knwl-config' and @name='" + kwBase + "']");
    }

    private boolean getExistGVExcelWorkbook(String excelWorkbook) throws XMLUtilsException
    {
        return getExistObject("/GVAdapters/GVExcelWorkbookConfiguration/GVExcelWorkbook[@configName='" + excelWorkbook
                + "']");
    }

    private boolean getExistGVExcelRepo(String excelRepo) throws XMLUtilsException
    {
        return getExistObject("/GVAdapters/GVExcelCreatorConfiguration/GVExcelReport[@name='" + excelRepo + "']");
    }

    private boolean getExistGVBirtRepo(String birtRepo) throws XMLUtilsException
    {
        return getExistObject("/GVAdapters/GVBIRTReportConfiguration/ReportGroups/ReportGroup/Report[@name='"
                + birtRepo + "']");
    }

    private boolean getExistGVDataHandler(String dboBuilder) throws XMLUtilsException
    {
        return getExistObject("/GVAdapters/GVDataHandlerConfiguration/*[@type='dbobuilder' and @name='" + dboBuilder
                + "']");
    }

    private boolean getExistGVHL7(String listener) throws XMLUtilsException
    {
        return getExistObject("/GVAdapters/GVHL7ListenerManager/HL7Listeners/*[@type='hl7listener' and @name='"
                + listener + "']");
    }

    private boolean getExistGVHTTP(String action) throws XMLUtilsException
    {
        return getExistObject("/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/ActionMappings/*[@type='action-mapping' and @Action='"
                + action + "']");
    }

    private boolean getEqualGVExcelWorkbook(String excelWorkbook) throws XMLUtilsException
    {
        return getEqualObject("/GVAdapters/GVExcelWorkbookConfiguration/GVExcelWorkbook[@configName='" + excelWorkbook
                + "']");
    }

    private boolean getEqualGVExcelRepo(String excelRepo) throws XMLUtilsException
    {
        return getEqualObject("/GVAdapters/GVExcelCreatorConfiguration/GVExcelReport[@name='" + excelRepo + "']");
    }

    private boolean getEqualGVBirtRepo(String birtRepo) throws XMLUtilsException
    {
        return getEqualObject("/GVAdapters/GVBIRTReportConfiguration/ReportGroups/ReportGroup/Report[@name='"
                + birtRepo + "']");
    }

    private boolean getEqualGVDataHandler(String dboBuilder) throws XMLUtilsException
    {
        return getEqualObject("/GVAdapters/GVDataHandlerConfiguration/*[@type='dbobuilder' and @name='" + dboBuilder
                + "']");
    }

    private boolean getEqualGVHL7(String listener) throws XMLUtilsException
    {
        return getEqualObject("/GVAdapters/GVHL7ListenerManager/HL7Listeners/*[@type='hl7listener' and @name='"
                + listener + "']");
    }

    private boolean getEqualGVHTTP(String action) throws XMLUtilsException
    {
        return getEqualObject("/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/ActionMappings/*[@type='action-mapping' "
        		+ "and @Action='" + action + "']");
    }

    /**
     * @param nomeAdapter
     * @return
     * @throws XMLUtilsException 
     */
    private boolean getEqualDataProvider(String dataProvider) throws XMLUtilsException
    {
        return getEqualObject("/GVAdapters/GVDataProviderManager/DataProviders/*[@name='" + dataProvider + "']");
    }

    /**
     * @param nomeAdapter
     * @return
     * @throws XMLUtilsException 
     */
    private boolean getEqualKnowledgeBaseConfig(String kwBase) throws XMLUtilsException
    {
        return getEqualObject("/GVAdapters/GVRulesConfigManager/*[@type='knwl-config' and @name='" + kwBase + "']");
    }

    /**
     * @param nomeAdapter
     * @return
     * @throws XMLUtilsException 
     */
    private boolean getExistWebServices(String servizio) throws XMLUtilsException
    {
        return getExistObject("/GVAdapters/GVWebServices/BusinessWebServices/WebService[@web-service='" + servizio
                + "']");
    }

    /**
     * @param nomeAdapter
     * @return
     * @throws XMLUtilsException 
     */
    private boolean getEqualWebServices(String servizio) throws XMLUtilsException
    {
        return getEqualObject("/GVAdapters/GVWebServices/BusinessWebServices/WebService[@web-service='" + servizio
                + "']");
    }

    /**
     * @param nomeAdapter
     * @return
     * @throws XMLUtilsException 
     */
    public boolean getExistGVWebServices(String servizio,String operation) throws XMLUtilsException
    {
        return getExistObject("/GVAdapters/GVWebServices/GreenVulcanoWebServices/GreenVulcanoWebService[@gv-service='" + servizio
                + "' and @gv-operation='"+operation+"']");
    }

    /**
     * @param nomeAdapter
     * @return
     * @throws XMLUtilsException 
     */
    public boolean getEqualGVWebServices(String servizio,String operation) throws XMLUtilsException
    {
        return getEqualObject("/GVAdapters/GVWebServices/GreenVulcanoWebServices/GreenVulcanoWebService[@gv-service='" + servizio
                + "' and @gv-operation='"+operation+"']");
    }

    /**
     * @param xml
     * @return
     * @throws IOException
     */
    private String[] getListaObject(Document xml, String xpath) throws Exception
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

    /**
     * @param xpath
     * @return
     * @throws XMLUtilsException 
     */
    private boolean getExistObject(String xpath) throws XMLUtilsException
    {
        boolean existServizio = XMLUtils.existNode_S(serverXml, xpath);
        logger.debug("getExistObject=" + existServizio);
        return existServizio;
    }

    /**
     * @param nomeAdapter
     * @return
     * @throws XMLUtilsException 
     */
    private boolean getEqualObject(String xpath) throws XMLUtilsException
    {
        boolean equalObject = false;
        XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();
	        Node resultsServer =  parser.selectSingleNode(serverXml, xpath);
	        Node resultsZip =  parser.selectSingleNode(newXml, xpath);
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
     * @throws IOException
     */
    public String[] getListaAdapterZip() throws Exception
    {
        return getListaObject(newXml, "/GVAdapters/*/@name");
    }

    /**
     * @return
     * @throws IOException
     */
    public String[] getListaAdapterServer() throws Exception
    {
        return getListaObject(serverXml, "/GVAdapters/*/@name");
    }

    /**
     * @return
     * @throws IOException
     */
    private String[] getListaWebServicesServer() throws Exception
    {
        return getListaWebServices(serverXml);
    }

    private String[] getListaWebServices(Document Xml) throws Exception
    {
        String[] listaBusinessWebServices = getListaBusinessWebServices(Xml);
        return listaBusinessWebServices;
    }

    /**
     * @return
     * @throws IOException
     */
    public String[] getListaWebServicesZip() throws Exception
    {
        return getListaWebServices(newXml);
    }

    public String[] getListaGVExcelWorkbookZip() throws Exception
    {
        return getListaGVExcelWorkbook(newXml);
    }

    public String[] getListaGVExcelRepoZip() throws Exception
    {
        return getListaGVExcelRepo(newXml);
    }

    /**
     * @return
     * @throws IOException
     */
    public String[] getListaKnowledgeBaseConfigZip() throws Exception
    {
        return getListaKnowledgeBaseConfig(newXml);
    }

    /**
     * @return
     * @throws IOException
     */
    public String[] getListaDataProviderZip() throws Exception
    {
        return getListaDataProvider(newXml);
    }

    public String[] getKnowledgeBaseConfigZip() throws Exception
    {
        return getListaKnowledgeBaseConfig(newXml);
    }

    public String[] getListaGVDataHandlerZip() throws Exception
    {
        return getListaGVDataHandler(newXml);
    }

    public String[] getListaGVHL7Zip() throws Exception
    {
        return getListaGVHL7(newXml);
    }

    public String[] getListaGVHTTPZip() throws Exception
    {
        return getListaGVHTTP(newXml);
    }

    public String[] getListaGVBirtRepoZip() throws Exception
    {
        return getListaGVBirtRepo(newXml);
    }


    /**
     * @param nomeAdapter
     * @return
     * @throws XMLUtilsException 
     */
    public String getGvAdapterZip(String nomeAdapter, String nomeServizio) throws XMLUtilsException
    {
        String ret = null;
        if (nomeAdapter.equals("WEB_SERVICES")) {
            ret = getBusinessWebServicesZip(nomeServizio);
        }
        else if (nomeAdapter.equals("GVDP")) {
            ret = getGvDataProviderZip(nomeServizio);
        }
        else if (nomeAdapter.equals("EXCEL_WORK")) {
            ret = getGVExcelWorkbookZip(nomeServizio);
        }
        else if (nomeAdapter.equals("EXCEL_REPO")) {
            ret = getGVExcelRepoZip(nomeServizio);
        }
        else if (nomeAdapter.equals("BIRT_REPO")) {
            ret = getGVBirtRepoZip(nomeServizio);
        }
        else if (nomeAdapter.equals("DH_ENGINE")) {
            ret = getGVDataHandlerZip(nomeServizio);
        }
        else if (nomeAdapter.equals("HL7_LISTENERS")) {
            ret = getGVHL7Zip(nomeServizio);
        }
        else if (nomeAdapter.equals("HTTP_ADAPTER")) {
            ret = getGVHTTPZip(nomeServizio);
        }
        else if (nomeAdapter.equals("RULES_CFG")) {
            ret = getGvKnowledgeBaseConfigZip(nomeServizio);
        }
        else {
            ret = getGvAdapter(newXml, nomeAdapter);
        }
        return ret;
    }

    /**
     * @param nomeAdapter
     * @return
     * @throws XMLUtilsException 
     */
    public String getGvAdapterServer(String nomeAdapter, String nomeServizio) throws XMLUtilsException
    {
        String ret = null;
        if (nomeAdapter.equals("WEB_SERVICES")) {
            ret = getBusinessWebServicesServer(nomeServizio);
        }
        else if (nomeAdapter.equals("GVDP")) {
            ret = getGvDataProviderServer(nomeServizio);
        }
        else if (nomeAdapter.equals("EXCEL_WORK")) {
            ret = getGVExcelWorkbookServer(nomeServizio);
        }
        else if (nomeAdapter.equals("EXCEL_REPO")) {
            ret = getGVExcelRepoServer(nomeServizio);
        }
        else if (nomeAdapter.equals("BIRT_REPO")) {
            ret = getGVBirtRepoServer(nomeServizio);
        }
        else if (nomeAdapter.equals("DH_ENGINE")) {
            ret = getGVDataHandlerServer(nomeServizio);
        }
        else if (nomeAdapter.equals("HL7_LISTENERS")) {
            ret = getGVHL7Server(nomeServizio);
        }
        else if (nomeAdapter.equals("HTTP_ADAPTER")) {
            ret = getGVHTTPServer(nomeServizio);
        }
        else if (nomeAdapter.equals("RULES_CFG")) {
            ret = getGvKnowledgeBaseConfigServer(nomeServizio);
        }
        else {
            ret = getGvAdapter(serverXml, nomeAdapter);
        }
        return ret;
    }

    /**
     * @param nomeAdapter
     * @return
     * @throws XMLUtilsException 
     */
    private String getBusinessWebServicesZip(String servizio) throws XMLUtilsException
    {
        return getBusinessWebServices(newXml, servizio);
    }

    /**
     * @param nomeAdapter
     * @return
     * @throws XMLUtilsException 
     */
    private String getBusinessWebServicesServer(String servizio) throws XMLUtilsException
    {
        return getBusinessWebServices(serverXml, servizio);
    }

    private String getBusinessWebServices(Document xml, String servizio) throws XMLUtilsException
    {
    	logger.debug("getGvWebServices servizio =" + servizio);
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
	        Node localXml = parser.selectSingleNode(xml, "/GVAdapters/GVWebServices/BusinessWebServices/WebService[@web-service='"
	                + servizio + "']");
	        Document localXmlGVAdapters = parser.newDocument("GVAdapters");
	        if (localXml != null) {
	            Node base = localXmlGVAdapters.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapters, "GVWebServices"));
	            parser.setAttribute((Element) base, "version", "1.0");
	            parser.setAttribute((Element) base, "type", "module");
	            parser.setAttribute((Element) base, "name", "WEB_SERVICES");
	            Node bws = base.appendChild(parser.createElement(localXmlGVAdapters, "BusinessWebServices"));
	            Node importedNode = localXmlGVAdapters.importNode(localXml, true);
	            bws.appendChild(importedNode);
	        }
	        return parser.serializeDOM(localXmlGVAdapters, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private String getGVExcelWorkbookZip(String servizio) throws XMLUtilsException
    {
        return getGVExcelWorkbook(newXml, servizio);
    }

    private String getGVExcelWorkbookServer(String servizio) throws XMLUtilsException
    {
        return getGVExcelWorkbook(serverXml, servizio);
    }

    private String getGVExcelWorkbook(Document xml, String servizio) throws XMLUtilsException
    {
        logger.debug("getGVExcelWorkbook servizio =" + servizio);
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
	        Node localXml = parser.selectSingleNode(xml, "/GVAdapters/GVExcelWorkbookConfiguration/GVExcelWorkbook[@configName='"
	                + servizio + "']");
	        Document localXmlGVAdapters = parser.newDocument("GVAdapters");
	        if (localXml != null) {
	            Node base = localXmlGVAdapters.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapters, "GVExcelWorkbookConfiguration"));
	            parser.setAttribute((Element) base, "version", "1.0");
	            parser.setAttribute((Element) base, "type", "module");
	            parser.setAttribute((Element) base, "name", "EXCEL_WORK");
	            Node importedNode = localXmlGVAdapters.importNode(localXml, true);
	            base.appendChild(importedNode);
	        }
	        return parser.serializeDOM(localXmlGVAdapters, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private String getGVExcelRepoZip(String servizio) throws XMLUtilsException
    {
        return getGVExcelRepo(newXml, servizio);
    }

    private String getGVExcelRepoServer(String servizio) throws XMLUtilsException
    {
        return getGVExcelRepo(serverXml, servizio);
    }

    private String getGVExcelRepo(Document xml, String servizio) throws XMLUtilsException
    {
        logger.debug("getGVExcelRepo servizio =" + servizio);
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
	        Node localXml = parser.selectSingleNode(xml, "/GVAdapters/GVExcelCreatorConfiguration/GVExcelReport[@name='"
	                + servizio + "']");
	        Document localXmlGVAdapters = parser.newDocument("GVAdapters");
	        if (localXml != null) {
	            Node base = localXmlGVAdapters.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapters, "GVExcelCreatorConfiguration"));
	            parser.setAttribute((Element) base, "version", "1.0");
	            parser.setAttribute((Element) base, "type", "module");
	            parser.setAttribute((Element) base, "name", "EXCEL_REPO");
	            Node importedNode = localXmlGVAdapters.importNode(localXml, true);
	            base.appendChild(importedNode);
	        }
	        return parser.serializeDOM(localXmlGVAdapters, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private String getGvDataProviderZip(String servizio) throws XMLUtilsException
    {
        return getGvDataProvider(newXml, servizio);
    }

    private String getGvDataProviderServer(String servizio) throws XMLUtilsException
    {
        return getGvDataProvider(serverXml, servizio);
    }

    private String getGvDataProvider(Document xml, String servizio) throws XMLUtilsException
    {
        logger.debug("getGvDataProvider servizio =" + servizio);
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
	        Node localXml = parser.selectSingleNode(xml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='" + servizio
	                + "']");
	        Document localXmlGVAdapters = parser.newDocument("GVAdapters");
	        if (localXml != null) {
	            Node base = localXmlGVAdapters.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapters, "GVDataProviderManager"));
	            parser.setAttribute((Element) base, "version", "1.0");
	            parser.setAttribute((Element) base, "type", "module");
	            parser.setAttribute((Element) base, "name", "GVDP");
	            Node dps = base.appendChild(parser.createElement(localXmlGVAdapters, "DataProviders"));
	            Node importedNode = localXmlGVAdapters.importNode(localXml, true);
	            dps.appendChild(importedNode);
	        }
	        return parser.serializeDOM(localXmlGVAdapters, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private String getGvKnowledgeBaseConfigZip(String servizio) throws XMLUtilsException
    {
        return getGvKnowledgeBaseConfig(newXml, servizio);
    }

    private String getGvKnowledgeBaseConfigServer(String servizio) throws XMLUtilsException
    {
        return getGvKnowledgeBaseConfig(serverXml, servizio);
    }

    private String getGvKnowledgeBaseConfig(Document xml, String servizio) throws XMLUtilsException
    {
        logger.debug("getGvKnowledgeBaseConfig servizio =" + servizio);
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
	        Node localXml = parser.selectSingleNode(xml, "/GVAdapters/GVRulesConfigManager/*[@name='" + servizio + "']");
	        Document localXmlGVAdapters = parser.newDocument("GVAdapters");
	        if (localXml != null) {
	            Node base = localXmlGVAdapters.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapters, "GVRulesConfigManager"));
	            parser.setAttribute((Element) base, "version", "1.0");
	            parser.setAttribute((Element) base, "type", "module");
	            parser.setAttribute((Element) base, "name", "RULES_CFG");
	            Node importedNode = localXmlGVAdapters.importNode(localXml, true);
	            base.appendChild(importedNode);
	        }
	        return parser.serializeDOM(localXmlGVAdapters, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private String getGVBirtRepoZip(String servizio) throws XMLUtilsException
    {
        return getGVBirtRepo(newXml, servizio);
    }

    private String getGVBirtRepoServer(String servizio) throws XMLUtilsException
    {
        return getGVBirtRepo(serverXml, servizio);
    }

    private String getGVBirtRepo(Document xml, String servizio) throws XMLUtilsException
    {
        logger.debug("getGVBirtRepo servizio =" + servizio);
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
	        Node localXml = parser.selectSingleNode(xml, "/GVAdapters/GVBIRTReportConfiguration/ReportGroups/ReportGroup/Report[@name='"
	                + servizio + "']");
	        Document localXmlGVAdapters = parser.newDocument("GVAdapters");
	        if (localXml != null) {
	            Node base = localXmlGVAdapters.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapters, "GVBIRTReportConfiguration"));
	            parser.setAttribute((Element) base, "version", "1.0");
	            parser.setAttribute((Element) base, "type", "module");
	            parser.setAttribute((Element) base, "name", "BIRT_REPO");
	            Node rgs = base.appendChild(parser.createElement(localXmlGVAdapters, "ReportGroups"));
	            Node rg = rgs.appendChild(parser.createElement(localXmlGVAdapters, "ReportGroup"));
	            parser.setAttribute((Element) rg, "name", parser.get(localXml, "../@name"));
	            Node importedNode = localXmlGVAdapters.importNode(localXml, true);
                rg.appendChild(importedNode);
	        }
	        return parser.serializeDOM(localXmlGVAdapters, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private String getGVDataHandlerZip(String servizio) throws XMLUtilsException
    {
        return getGVDataHandler(newXml, servizio);
    }

    private String getGVDataHandlerServer(String servizio) throws XMLUtilsException
    {
        return getGVDataHandler(serverXml, servizio);
    }

    private String getGVDataHandler(Document xml, String servizio) throws XMLUtilsException
    {
        logger.debug("getGVDataHandler servizio =" + servizio);
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
	        Node localXml = parser.selectSingleNode(xml, "/GVAdapters/GVDataHandlerConfiguration/*[@type='dbobuilder' and @name='"
	                + servizio + "']");
	        Document localXmlGVAdapters = parser.newDocument("GVAdapters");
	        if (localXml != null) {
	            Node base = localXmlGVAdapters.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapters, "GVDataHandlerConfiguration"));
	            parser.setAttribute((Element) base, "version", "1.0");
	            parser.setAttribute((Element) base, "type", "module");
	            parser.setAttribute((Element) base, "name", "DH_ENGINE");
	            Node importedNode = localXmlGVAdapters.importNode(localXml, true);
	            base.appendChild(importedNode);
	        }
	        return parser.serializeDOM(localXmlGVAdapters, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }


    private String getGVHL7Zip(String servizio) throws XMLUtilsException
    {
        return getGVHL7(newXml, servizio);
    }

    private String getGVHL7Server(String servizio) throws XMLUtilsException
    {
        return getGVHL7(serverXml, servizio);
    }

    private String getGVHL7(Document xml, String servizio) throws XMLUtilsException
    {
        logger.debug("getGVHL7 servizio =" + servizio);
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
	        Node localXml = parser.selectSingleNode(xml, "/GVAdapters/GVHL7ListenerManager/HL7Listeners/*[@type='hl7listener' and @name='"
	                + servizio + "']");
	        Document localXmlGVAdapters = parser.newDocument("GVAdapters");
	        if (localXml != null) {
	            Node base = localXmlGVAdapters.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapters, "GVHL7ListenerManager"));
	            parser.setAttribute((Element) base, "version", "1.0");
	            parser.setAttribute((Element) base, "type", "module");
	            parser.setAttribute((Element) base, "name", "HL7_LISTENERS");
	            Node ls = base.appendChild(parser.createElement(localXmlGVAdapters, "HL7Listeners"));
	            Node importedNode = localXmlGVAdapters.importNode(localXml, true);
                ls.appendChild(importedNode);
	        }
	        return parser.serializeDOM(localXmlGVAdapters, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private String getGVHTTPZip(String servizio) throws XMLUtilsException
    {
        return getGVHTTP(newXml, servizio);
    }

    private String getGVHTTPServer(String servizio) throws XMLUtilsException
    {
        return getGVHTTP(serverXml, servizio);
    }

    private String getGVHTTP(Document xml, String servizio) throws XMLUtilsException
    {
        logger.debug("getGVHTTP servizio =" + servizio);
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            Node localXml = parser.selectSingleNode(xml, "/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/ActionMappings/*[@type='action-mapping' "
                    + "and @Action='" + servizio + "']");
            Document localXmlGVAdapters = parser.newDocument("GVAdapters");
            if (localXml != null) {
                Node base = localXmlGVAdapters.getDocumentElement().appendChild(parser.createElement(localXmlGVAdapters, "GVAdapterHttpConfiguration"));
                Node mappings = base.appendChild(parser.createElement(localXmlGVAdapters, "InboundConfiguration"));
                mappings = mappings.appendChild(parser.createElement(localXmlGVAdapters, "ActionMappings"));
                parser.setAttribute((Element) base, "version", "1.0");
                parser.setAttribute((Element) base, "type", "module");
                parser.setAttribute((Element) base, "name", "HTTP_ADAPTER");

                Node importedNode = localXmlGVAdapters.importNode(localXml, true);
                mappings.appendChild(importedNode);
                
                localXml = parser.selectSingleNode(xml, "/GVAdapters/GVAdapterHttpConfiguration/Formatters/*[@Type='FormatterPlugin' and @ID='"
                        + parser.get(localXml, "@FormatterID") + "']");
            
                if (localXml != null) {
                    Node formatters = base.appendChild(parser.createElement(localXmlGVAdapters, "Formatters"));
                    importedNode = localXmlGVAdapters.importNode(localXml, true);
                    formatters.appendChild(importedNode);
                }
            }
            return parser.serializeDOM(localXmlGVAdapters, false, true);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    private String getGvAdapter(Document xml, String nomeAdapter) throws XMLUtilsException
    {
    	logger.debug("Adapter =" + nomeAdapter);
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
	        Node localXml = parser.selectSingleNode(xml, "/GVAdapters/*[@name='" + nomeAdapter + "']");
	        Document localXmlGVAdapters = parser.newDocument("GVAdapters");
	        if (localXml != null) {
	            Node importedNode = localXmlGVAdapters.importNode(localXml, true);
	            localXmlGVAdapters.getDocumentElement().appendChild(importedNode);
	        }
	        return parser.serializeDOM(localXmlGVAdapters, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }


    /**
     * @param nomeAdapter
     */
    public void aggiorna(String nomeAdapter, String nomeServizio) throws Exception
    {
        if (nomeAdapter.equals("WEB_SERVICES")) {
            aggiornaWebServices(nomeServizio);
        }
        else if (nomeAdapter.equals("GVDP")) {
            aggiornaDp(nomeServizio);
        }
        else if (nomeAdapter.equals("RULES_CFG")) {
            aggiornaKwB(nomeServizio);
        }
        else if (nomeAdapter.equals("EXCEL_WORK")) {
            aggiornaGVExcelWorkbook(nomeServizio);
        }
        else if (nomeAdapter.equals("EXCEL_REPO")) {
            aggiornaGVExcelRepo(nomeServizio);
        }
        else if (nomeAdapter.equals("BIRT_REPO")) {
            aggiornaGVBirtRepo(nomeServizio);
        }
        else if (nomeAdapter.equals("DH_ENGINE")) {
            aggiornaGVDataHandler(nomeServizio);
        }
        else if (nomeAdapter.equals("HL7_LISTENERS")) {
            aggiornaGVHL7(nomeServizio);
        }
        else if (nomeAdapter.equals("HTTP_ADAPTER")) {
            aggiornaGVHTTP(nomeServizio);
        }
        else {
            XMLUtils parser = null;
            try {
            	parser = XMLUtils.getParserInstance();
	            Node resultsServer =  parser.selectSingleNode(serverXml, "/GVAdapters/*[@name='" + nomeAdapter + "']");
	            Node resultsZip = parser.selectSingleNode(newXml, "/GVAdapters/*[@name='" + nomeAdapter + "']");
	            Node parentServer =  parser.selectSingleNode(serverXml, "/GVAdapters");
		        if (resultsZip != null) {
			        if (resultsServer == null) {
		                Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
		                parentServer.appendChild(importedNode);
		                logger.debug("Adapter[" + nomeServizio + "] non esistente, inserimento");
		            }
			        else {
			            Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
			            parentServer.replaceChild(importedNode, resultsServer);
			            logger.debug("Adapter[" + nomeServizio + "] esistente, aggiornamento");
			        }
		        }
        	}
    		finally {
    			XMLUtils.releaseParserInstance(parser);
    		}
        }
    }

    /**
     *
     */
    public void aggiornaWebServices(String nomeServizio) throws Exception
    {
        aggiornaBusinessWebServices(nomeServizio);
        //aggiornaGreenVulcanoWebServices(nomeServizio);
        aggiornaAxisExtra(nomeServizio);
        String[] dataProvider = getWSDataProvider(nomeServizio);
        for (int i=0; i < dataProvider.length; i++) {
        	aggiornaDp(dataProvider[i]);
        }
    }

    public void aggiornaGVExcelWorkbook(String nomeServizio) throws Exception
    {
        logger.debug("init aggiornaGVExcelWorkbook");
        logger.debug("workbook=" + nomeServizio);
        XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();

	        Node resultsServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVExcelWorkbookConfiguration/GVExcelWorkbook[@configName='"
	                + nomeServizio + "']");
	        Node resultsZip =  parser.selectSingleNode(newXml, "/GVAdapters/GVExcelWorkbookConfiguration/GVExcelWorkbook[@configName='"
	                + nomeServizio + "']");
	        Node parentServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVExcelWorkbookConfiguration");
	        if (resultsZip != null) {
		        if (resultsServer == null) {
	                Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
	                parentServer.appendChild(importedNode);
	                logger.debug("ExcelWorkbook[" + nomeServizio + "] non esistente, inserimento");
	            }
		        else {
		            Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
		            parentServer.replaceChild(importedNode, resultsServer);
		            logger.debug("ExcelWorkbook[" + nomeServizio + "] esistente, aggiornamento");
		        }
	        }
	        logger.debug("end aggiornaGVExcelWorkbook");
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    public void aggiornaGVExcelRepo(String nomeServizio) throws Exception
    {
        logger.debug("init aggiornaGVExcelRepo");
        logger.debug("excel=" + nomeServizio);
        XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();

	        Node resultsServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVExcelCreatorConfiguration/GVExcelReport[@name='"
	                + nomeServizio + "']");
	        Node resultsZip =  parser.selectSingleNode(newXml, "/GVAdapters/GVExcelCreatorConfiguration/GVExcelReport[@name='"
	                + nomeServizio + "']");
	        Node parentServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVExcelCreatorConfiguration");
	        if (resultsZip != null) {
		        if (resultsServer == null) {
	                Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
	                parentServer.appendChild(importedNode);
	                logger.debug("ExcelReport[" + nomeServizio + "] non esistente, inserimento");
	            }
		        else {
		            Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
		            parentServer.replaceChild(importedNode, resultsServer);
		            logger.debug("ExcelReport[" + nomeServizio + "] esistente, aggiornamento");
		        }
	        }
	        logger.debug("end aggiornaGVExcelRepo");
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    public void aggiornaGVBirtRepo(String nomeServizio) throws Exception
    {
        logger.debug("init aggiornaGVBirtRepo");
        logger.debug("report=" + nomeServizio);
        XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();
    		
	        Node resultsServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVBIRTReportConfiguration/ReportGroups/ReportGroup/Report[@name='"
	                + nomeServizio + "']");
	        Node resultsZip =  parser.selectSingleNode(newXml, "/GVAdapters/GVBIRTReportConfiguration/ReportGroups/ReportGroup/Report[@name='"
	                + nomeServizio + "']");
	        
	        if (resultsZip != null) {
	            Node repo = null;
                String grp = parser.get(resultsZip, "../@name");
                Node rg = parser.selectSingleNode(serverXml, "/GVAdapters/GVBIRTReportConfiguration/ReportGroups/ReportGroup[@name='"
                        + grp + "']");
	            if (resultsServer == null) {
	                if (rg != null) {
	                	repo = rg.getOwnerDocument().importNode(resultsZip, true);
		                rg.appendChild(repo);
		                logger.debug("BirtReport[" + nomeServizio + "] non esistente, inserimento");
	                }
	                else {
	                	Node parentServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVBIRTReportConfiguration/ReportGroups");
	                	rg = parentServer.getOwnerDocument().importNode(resultsZip.getParentNode(), true);
	                	parentServer.appendChild(rg);
	                	repo = rg.getOwnerDocument().importNode(resultsZip, true);
	                	rg.appendChild(repo);
	                }
	                logger.debug("BirtReport[" + nomeServizio + "] e gruppo non esistente, inserimento");
	            }
	            else {
	            	repo = rg.getOwnerDocument().importNode(resultsZip, true);
                	rg.replaceChild(repo, resultsServer);
	                logger.debug("BirtReport[" + nomeServizio + "] esistente, aggiornamento");
	            }
	            copiaFileRtpDesign(repo, parser);
	        }
	        logger.debug("end aggiornaGVBirtRepo");
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private void copiaFileRtpDesign(Node report, XMLUtils parser) throws Exception
    {
        String path = java.lang.System.getProperty("java.io.tmpdir");
        String nomeReport = parser.get(report, "@name");
        String nomeFile = parser.get(report, "@config");
        logger.debug("Prepare BirtReport= " + nomeReport + " - " + nomeFile);
        String inputFile = path + File.separator + "conf" + File.separator + "reports" + File.separator + nomeFile;
        logger.debug("inputFile  = " + inputFile);
        File fin = new File(inputFile);
        if (fin.exists()) {
            String pathServer = parser.get(serverXml, "/GVAdapters/GVBIRTReportConfiguration/Engine/@reportEngineHome");
            if (pathServer == null) {
                pathServer = "${{gv.app.home}}" + File.separator + "BIRTReportEngine";
            }
            if (inputFile.indexOf(File.separator) != -1) {
                String outputFile = pathServer + File.separator + "reports" + File.separator + nomeFile;
                inputFile = inputFile.substring(0, inputFile.lastIndexOf(File.separator));
                outputFile = outputFile.substring(0, outputFile.lastIndexOf(File.separator));
                logger.debug("outputDir = " + outputFile);
                copiaDir(inputFile, outputFile);
            }
            else {
                String outputFile = pathServer + File.separator + "reports" + File.separator + nomeFile;
                logger.debug("outputFile = " + outputFile);
                copiaFile(inputFile, outputFile);
            }
        }
    }

    public void aggiornaGVDataHandler(String nomeServizio) throws Exception
    {
        logger.debug("init aggiornaGVDataHandler");
        logger.debug("dbobuilder=" + nomeServizio);
        XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();
    		
	        Node resultsServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVDataHandlerConfiguration/*[@type='dbobuilder' and @name='"
	                + nomeServizio + "']");
	        Node resultsZip =  parser.selectSingleNode(newXml, "/GVAdapters/GVDataHandlerConfiguration/*[@type='dbobuilder' and @name='"
	                + nomeServizio + "']");
	        Node parentServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVDataHandlerConfiguration");
	        if (resultsZip != null) {
		        if (resultsServer == null) {
	                Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
	                parentServer.appendChild(importedNode);
	                logger.debug("DBOBuilder[" + nomeServizio + "] non esistente, inserimento");
	                handleDHServicesDetails(importedNode, parser);
	            }
		        else {
		            Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
		            parentServer.replaceChild(importedNode, resultsServer);
	                logger.debug("DBOBuilder[" + nomeServizio + "] esistente, aggiornamento");
	                handleDHServicesDetails(importedNode, parser);
		        }
	            coreParser.scriviFile();
	        }
	        logger.debug("end aggiornaGVDataHandler");
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private void handleDHServicesDetails(Node base, XMLUtils parser) throws Exception
    {
        // handle DH service's transformations
        NodeList dbos = parser.selectNodeList(base, ".//*[@type='dbo']");

        if (dbos.getLength() > 0) {
            for (int j = 0; j < dbos.getLength(); j++) {
            	Node dbo = dbos.item(j);
                String nameDBO = parser.get(dbo, "@name");
                logger.debug("DBO[" + j + "]=" + nameDBO);
                String dtDBO = parser.get(dbo, "@transformation");
                if (dtDBO != null) {
                    coreParser.aggiornaGVDataTransformationForDH(dtDBO, parser);
                }
            }
        }
    }

    public void aggiornaGVHL7(String nomeServizio) throws Exception
    {
        logger.debug("init aggiornaGVHL7");
        logger.debug("listener=" + nomeServizio);
        XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();
    		
	        Node resultsServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVHL7ListenerManager/HL7Listeners/*[@type='hl7listener' and @name='"
	                + nomeServizio + "']");
	        Node resultsZip =  parser.selectSingleNode(newXml, "/GVAdapters/GVHL7ListenerManager/HL7Listeners/*[@type='hl7listener' and @name='"
	                + nomeServizio + "']");
	        Node parentServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVHL7ListenerManager/HL7Listeners");
	        if (resultsZip != null) {
		        if (resultsServer == null) {
	                Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
	                parentServer.appendChild(importedNode);
	                logger.debug("Listener[" + nomeServizio + "] non esistente, inserimento");
	            }
		        else {
		            Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
		            parentServer.replaceChild(importedNode, resultsServer);
		            logger.debug("Listener[" + nomeServizio + "] esistente, aggiornamento");
		        }
	        }
	        logger.debug("end aggiornaGVHL7");
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    
    public void aggiornaGVHTTP(String nomeServizio) throws Exception
    {
        logger.debug("init aggiornaGVHTTP");
        logger.debug("action=" + nomeServizio);
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            
            // handle ActionMapping deployment
            Node resultsServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/ActionMappings/*[@type='action-mapping' "
                    + "and @Action='" + nomeServizio + "']");
            Node resultsZip =  parser.selectSingleNode(newXml, "/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/ActionMappings/*[@type='action-mapping' "
                    + "and @Action='" + nomeServizio + "']");
            Node parentServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/ActionMappings");
            if (resultsZip != null) {
                if (resultsServer == null) {
                    Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
                    parentServer.appendChild(importedNode);
                    logger.debug("ActionMapping[" + nomeServizio + "] non esistente, inserimento");
                }
                else {
                    Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
                    parentServer.replaceChild(importedNode, resultsServer);
                    logger.debug("ActionMapping[" + nomeServizio + "] esistente, aggiornamento");
                }

                // handle Formatter deployment
                String formatter = parser.get(resultsZip, "@FormatterID");
                if (formatter != null) {
                    resultsServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVAdapterHttpConfiguration/Formatters/*[@Type='FormatterPlugin' and @ID='"
                            + formatter + "']");
                    resultsZip =  parser.selectSingleNode(newXml, "/GVAdapters/GVAdapterHttpConfiguration/Formatters/*[@Type='FormatterPlugin' and @ID='"
                            + formatter + "']");
                    parentServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVAdapterHttpConfiguration/Formatters");
                    if (resultsZip != null) {
                        if (resultsServer == null) {
                            Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
                            parentServer.appendChild(importedNode);
                            logger.debug("Formatter[" + formatter + "] non esistente, inserimento");
                        }
                        else {
                            Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
                            parentServer.replaceChild(importedNode, resultsServer);
                            logger.debug("Formatter[" + formatter + "] esistente, aggiornamento");
                        }
                    }
                }
                
                // handle Transaction deployment
                NodeList txNewList = parser.selectNodeList(newXml, "/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/InboundTransactions/Transaction");
                
                if (txNewList != null) {
                    parentServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/InboundTransactions");
                    
                    for (int i = 0; i < txNewList.getLength(); i++) {
                        resultsZip = txNewList.item(i);
                        String service = parser.get(resultsZip, "@service");
                        String system = parser.get(resultsZip, "@system");
                        if ((system != null) && !"".equals(system)) {
                            resultsServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/InboundTransactions/Transaction[@service='" + service + "' and @system='"
                                + system + "']");
                        }
                        else {
                            resultsServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/InboundTransactions/Transaction[@service='" + service + "' and not(@system)]");
                        }

                        if (resultsServer == null) {
                            Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
                            parentServer.appendChild(importedNode);
                            logger.debug("Transaction[" + service + "::" + (system != null ? system : "ALL") + "] non esistente, inserimento");
                        }
                        else {
                            Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
                            parentServer.replaceChild(importedNode, resultsServer);
                            logger.debug("Transaction[" + service + "::" + (system != null ? system : "ALL") + "] esistente, aggiornamento");
                        }
                    }
                }
            }
            
            logger.debug("end aggiornaGVHTTP");
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }
    
    public void aggiornaGreenVulcanoWebServices(String servizio,String[] operazioni) throws Exception
    {
        logger.debug("init aggiornaGreenVulcanoWebServices");
    	logger.debug("webServices = " + servizio + "/" + Arrays.toString(operazioni));
    	XMLUtils parser = null;
    	try {
    	    parser = XMLUtils.getParserInstance();

    		Node parentServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVWebServices/GreenVulcanoWebServices");
    		NodeList listService =  parser.selectNodeList(parentServer, "GreenVulcanoWebService[@gv-service='" + servizio+"']");
    		for (int i = 0; i < listService.getLength(); i++){
    		    logger.debug("Elimino " + servizio + "/" + parser.get(listService.item(i), "@gv-operation"));
    		    parentServer.removeChild(listService.item(i));
    		}
    		for(String operazione: operazioni){
    		    Node resultsZip =  parser.selectSingleNode(newXml, "/GVAdapters/GVWebServices/GreenVulcanoWebServices/GreenVulcanoWebService[@gv-service='" + servizio
    			                    + "' and @gv-operation='" + operazione + "']");
    			if (resultsZip != null) {
    			    Node importedNode = serverXml.importNode(resultsZip, true);
   					parentServer.appendChild(importedNode);
   					logger.debug("GreenVulcanoWebService[" + servizio + "/" + operazione + "] non esistente, inserimento");
    			}
    		}
    		logger.debug("end aggiornaGreenVulcanoWebServices");
    	}
    	finally {
    	    XMLUtils.releaseParserInstance(parser);
    	}
    }

    private void aggiornaBusinessWebServices(String webServices) throws Exception
    {
        logger.debug("init aggiornaBusinessWebServices");
        XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();
    		
	        Node resultsServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVWebServices/BusinessWebServices/WebService[@web-service='"
	                + webServices + "']");
	        Node resultsZip =  parser.selectSingleNode(newXml, "/GVAdapters/GVWebServices/BusinessWebServices/WebService[@web-service='"
	                + webServices + "']");
	        Node parentServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVWebServices/BusinessWebServices");
	        if (resultsZip != null) {
		        if (resultsServer == null) {
	                Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
	                parentServer.appendChild(importedNode);
	                logger.debug("BusinessWebService[" + webServices + "] non esistente, inserimento");
	            }
		        else {
		            Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
		            parentServer.replaceChild(importedNode, resultsServer);
		            logger.debug("BusinessWebService[" + webServices + "] esistente, aggiornamento");
		        }
	        }
	
	        copiaFileAAR(resultsZip);
	        copiaFileXsd_WS(resultsZip);
	        copiaFileWSDL(resultsZip);
	
	        logger.debug("end aggiornaBusinessWebServices");
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }


    private void aggiornaAxisExtra(String webServices) throws Exception
    {
        logger.debug("init aggiornaAxisExtra");
        XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();
    		
	        Node resultsServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVWebServices/AxisExtra");
	        Node resultsZip =  parser.selectSingleNode(newXml, "/GVAdapters/GVWebServices/AxisExtra");
	        Node parentServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVWebServices");
	        if (resultsZip != null) {
		        if (resultsServer == null) {
	                Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
	                parentServer.appendChild(importedNode);
	                logger.debug("AxisExtra non esistente, inserimento");
	            }
		        else {
		            Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
		            parentServer.replaceChild(importedNode, resultsServer);
		            logger.debug("AxisExtra esistente, aggiornamento");
		        }
	        }
	        logger.debug("end aggiornaAxisExtra");
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    public void aggiornaDp(String dataProvider) throws Exception
    {
        logger.debug("init aggiornaDp");
        logger.debug("dataProvider=" + dataProvider);
        XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();
    		
    		Node resultsServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"
	                + dataProvider + "']");
	        Node resultsZip =  parser.selectSingleNode(newXml, "/GVAdapters/GVDataProviderManager/DataProviders/*[@name='"
	                + dataProvider + "']");
            Node parentServer = parser.selectSingleNode(serverXml, "/GVAdapters/GVDataProviderManager/DataProviders");
            if (resultsZip != null) {
		        if (resultsServer == null) {
	                Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
	                parentServer.appendChild(importedNode);
	                logger.debug("DataProvider[" + dataProvider + "] non esistente, inserimento");
		        }
		        else {
	        		Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
	        		parentServer.replaceChild(importedNode, resultsServer);
	        		logger.debug("DataProvider[" + dataProvider + "] esistente, aggiornamento");
		        }
	        }
	        logger.debug("end aggiornaDp");
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    public void aggiornaKwB(String kwBase) throws Exception
    {
        logger.debug("init aggiornaKwB");
        logger.debug("kwBase=" + kwBase);
    	XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();

	        Node resultsServer =  parser.selectSingleNode(serverXml, "/GVAdapters/GVRulesConfigManager/*[@type='knwl-config' and @name='"
	                + kwBase + "']");
	        Node resultsZip =  parser.selectSingleNode(newXml, "/GVAdapters/GVRulesConfigManager/*[@type='knwl-config' and @name='"
	                + kwBase + "']");
            Node parentServer = parser.selectSingleNode(serverXml, "/GVAdapters/GVRulesConfigManager");
            if (resultsZip != null) {
		        if (resultsServer == null) {
	                Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
	                parentServer.appendChild(importedNode);
	                logger.debug("KwBase[" + kwBase + "] non esistente, inserimento");
	            }
		        else {
		            Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
		            parentServer.replaceChild(importedNode, resultsServer);
		            logger.debug("KwBase[" + kwBase + "] esistente, aggiornamento");
		        }
	        }
	        copiaFilesRules();
	        logger.debug("end aggiornaKwB");
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private String[] getWSDataProvider(String servizio) throws Exception
    {
    	XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();
	        logger.debug("init getWSDataProvider");
	        NodeList wsOp = parser.selectNodeList(serverXml, "/GVAdapters/GVWebServices/BusinessWebServices/WebService[@web-service='"
	                + servizio + "']/WSOperation");
	        String[] resDataProvider = new String[wsOp.getLength()];
	        for (int i=0; i < wsOp.getLength(); i++) {
	        	resDataProvider[i] = parser.get(wsOp.item(i), "@ref-dp");
	        	logger.debug("resDataProvider= " + resDataProvider[i]);
	        }
	        logger.debug("end getWSDataProvider");
	        return resDataProvider;
		}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private String[] getListaBusinessWebServices(Document xml) throws Exception
    {
        return getListaObject(xml, "/GVAdapters/GVWebServices/BusinessWebServices/WebService/@web-service");
    }

    private String[] getListaDataProvider(Document xml) throws Exception
    {
        return getListaObject(xml, "/GVAdapters/GVDataProviderManager/DataProviders/*/@name");
    }

    private String[] getListaKnowledgeBaseConfig(Document xml) throws Exception
    {
        return getListaObject(xml, "/GVAdapters/GVRulesConfigManager/*/@name");
    }

    private String[] getListaGVExcelWorkbook(Document xml) throws Exception
    {
        return getListaObject(xml, "/GVAdapters/GVExcelWorkbookConfiguration/GVExcelWorkbook/@configName");
    }

    private String[] getListaGVExcelRepo(Document xml) throws Exception
    {
        return getListaObject(xml, "/GVAdapters/GVExcelCreatorConfiguration/GVExcelReport/@name");
    }

    private String[] getListaGVDataHandler(Document xml) throws Exception
    {
        return getListaObject(xml, "/GVAdapters/GVDataHandlerConfiguration/*[@type='dbobuilder']/@name");
    }

    private String[] getListaGVHL7(Document xml) throws Exception
    {
        return getListaObject(xml, "/GVAdapters/GVHL7ListenerManager/HL7Listeners/*[@type='hl7listener']/@name");
    }

    private String[] getListaGVHTTP(Document xml) throws Exception
    {
        return getListaObject(xml, "/GVAdapters/GVAdapterHttpConfiguration/InboundConfiguration/ActionMappings/*[@type='action-mapping']/@Action");
    }

    private String[] getListaGVBirtRepo(Document xml) throws Exception
    {
        return getListaObject(xml, "/GVAdapters/GVBIRTReportConfiguration/ReportGroups/ReportGroup/Report/@name");
    }

    private Document getXmlZip() throws Exception
    {
        String path = java.lang.System.getProperty("java.io.tmpdir");
        return parseXmlFile(path + File.separator + "conf" + File.separator + "GVAdapters.xml");
    }

    private Document getXmlServer() throws IOException, XMLConfigException, XMLUtilsException
    {
        URL url = getUrl();
        //return parseXmlURL(url);
        return parseXmlFile(url.getFile());
    }

    /**
     * @throws IOException
     */
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
        url = XMLConfig.getURL("GVAdapters.xml");
        logger.debug("URL=" + url.getPath());
        return url;
    }

    private Document parseXmlFile(String xmlFilePath) throws IOException, XMLUtilsException
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

    private Document parseXmlURL(URL url) throws IOException, XMLUtilsException
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

    private void copiaFileAAR(Node businessWS) throws Exception
    {
        String path = java.lang.System.getProperty("java.io.tmpdir");
        String wsName = XMLUtils.get_S(businessWS, "@web-service");
        logger.debug("Prepare copiaFileAAR= " + wsName);
        String nomeFile = wsName + ".aar";
        String inputFile = path + File.separator + "conf" + File.separator + "services" + File.separator + nomeFile;
        File fin = new File(inputFile);
        if (fin.exists()) {
            String outputFile = PropertiesHandler.expand("${{gv.app.home}}" + File.separator + "webservices"
                    + File.separator + "services" + File.separator + nomeFile, null);
            logger.debug("inputFile  = " + inputFile);
            logger.debug("outputFile = " + outputFile);
            copiaFile(inputFile, outputFile);
        }
    }

    private void copiaFileXsd_WS(Node businessWS) throws Exception
    {
    	String wsName = XMLUtils.get_S(businessWS, "@web-service");
        logger.debug("Prepare copiaFileXsd_WS= " + wsName);
        String nomeFile = XMLUtils.get_S(businessWS, "@input-xsd");
        if (nomeFile != null) {
            copiaFileXsd(nomeFile);
        }
        nomeFile = XMLUtils.get_S(businessWS, "@output-xsd");
        if (nomeFile != null) {
            copiaFileXsd(nomeFile);
        }
    }

    private void copiaFileXsd(String nomeFile) throws Exception
    {
        logger.debug("inputFile  = " + nomeFile);
        String path = java.lang.System.getProperty("java.io.tmpdir");
        String inputFile = path + File.separator + "conf" + File.separator + "xsds" + File.separator + nomeFile;
        File fin = new File(inputFile);
        if (fin.exists()) {
            String outputFile = PropertiesHandler.expand("${{gv.app.home}}" + File.separator + "xmlconfig"
                    + File.separator + "xsds" + File.separator + nomeFile, null);
            logger.debug("inputFile  = " + nomeFile);
            logger.debug("outputFile = " + outputFile);
            copiaFile(inputFile, outputFile);
        }
    }

    private void copiaFileWSDL(Node businessWS) throws Exception
    {
    	String wsName = XMLUtils.get_S(businessWS, "@web-service");
        logger.debug("Prepare copiaFileWSDL= " + wsName);
        String nomeFile = wsName + ".wsdl";
        copiaFileWSDL(nomeFile);
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

    private void copiaFilesRules() throws Exception
    {
        String path = java.lang.System.getProperty("java.io.tmpdir");
        String inputDir = path + File.separator + "conf" + File.separator + "Rules";
        File fin = new File(inputDir);
        if (fin.exists()) {
            String outputDir = PropertiesHandler.expand("${{gv.app.home}}" + File.separator + "Rules");
            copiaDir(inputDir, outputDir);
        }
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
            out = new FileOutputStream(PropertiesHandler.expand(fileDestinazione, null));
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

    private void copiaDir(String dirOrigine, String dirDestinazione) throws Exception
    {
        File in = null;
        try {
            in = new File(dirOrigine);
        }
        catch (Exception exc) {
            logger.error("Directory [" + dirOrigine + "] not found");
            return;
        }
        dirDestinazione = PropertiesHandler.expand(dirDestinazione, null);
        FileUtils.forceMkdir(new File(dirDestinazione));
        FileManager.cp(dirOrigine, dirDestinazione, null);
    }


    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {

        GVAdapterParser parser = new GVAdapterParser();
        parser.loadParser();
        System.out.println(parser.getEqual("OBJECT_CONVERTER", null));
        System.out.println(parser.getExist("OBJECT_CONVERTER", null));
        parser.getListaAdapterZip();
        // parser.aggiorna();
        parser.scriviFile();
    }
}
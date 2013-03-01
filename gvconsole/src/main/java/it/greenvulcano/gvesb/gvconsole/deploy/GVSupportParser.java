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
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import max.xml.DOMWriter;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * GVSupportParser class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GVSupportParser
{
    private URL               url       = null;
    private Document serverXml = null;
    private Document newXml    = null;
    private static Logger     logger    = GVLogger.getLogger(GVSupportParser.class);

    /**
     * @throws XMLUtilsException
     * @throws IOException
     * @throws XMLConfigException
     */
    public void loadParser() throws XMLUtilsException, IOException, XMLConfigException
    {
        logger.debug("init load Parser GVSupportParser");
        newXml = getXmlZip();
        serverXml = getXmlServer();
    }

    public boolean getExist(String servizio) throws XMLUtilsException
    {
        boolean existServizio = XMLUtils.existNode_S(serverXml, "/GVSupport/*[@name='" + servizio + "']");
        logger.debug("getExist=" + existServizio);
        return existServizio;
    }

    public boolean getEqual(String servizio) throws XMLUtilsException
    {
        boolean equalServizio = false;
        XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();
    		Node resultsServer = parser.selectSingleNode(serverXml, "/GVSupport/*[@name='" + servizio + "']");
    		Node resultsZip = parser.selectSingleNode(newXml, "/GVSupport/*[@name='" + servizio + "']");
    		if ((resultsServer != null) && (resultsZip != null)) {
	            if (parser.serializeDOM(resultsServer).equals(parser.serializeDOM(resultsZip))) {
	            	equalServizio = true;
	            }
	        }
    		logger.debug("getEqual=" + equalServizio);
    		return equalServizio;
    	}
    	finally {
    		XMLUtils.releaseParserInstance(parser);
    	}
    }

    public String getGvSupportZip(String servizio) throws XMLUtilsException
    {
        return getGvSupport(newXml, servizio);
    }

    public String getGvSupportServer(String servizio) throws XMLUtilsException
    {
        return getGvSupport(serverXml, servizio);
    }

    private String getGvSupport(Document xml, String servizio) throws XMLUtilsException
    {
    	logger.debug("getGvSupport servizio =" + servizio);
        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
	        Node localXml = parser.selectSingleNode(xml, "/GVSupport/*[@name='" + servizio + "']");
	        Document localXmlGVSupport = parser.newDocument("GVSupport");
	        if (localXml != null) {
	            Node base = localXmlGVSupport.getDocumentElement();
	            Node importedNode = localXmlGVSupport.importNode(localXml, true);
	            base.appendChild(importedNode);
	        }
	        return parser.serializeDOM(localXmlGVSupport, false, true);
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    public String[] getListaSupportZip() throws XMLUtilsException, IOException
    {
        return getListaSupport(newXml);
    }

    public String[] getListaSupportServer() throws XMLUtilsException, IOException
    {
        return getListaSupport(serverXml);
    }

    public String[] getListaSupport(Document xml) throws XMLUtilsException, IOException
    {
    	XMLUtils parser = null;
    	try {
    		parser = XMLUtils.getParserInstance();
    		NodeList results = parser.selectNodeList(xml, "/GVSupport/*/@name");
    		String[] retListaServizi = new String[results.getLength()];
    		for (int i = 0; i < results.getLength(); i++) {
    			retListaServizi[i] = parser.get(results.item(i), ".");
    			logger.debug(retListaServizi[i]);
    		}
    		return retListaServizi;
    	}
    	finally {
    		XMLUtils.releaseParserInstance(parser);
    	}
    }

    public void aggiorna(String nomeServizio) throws XMLUtilsException
    {
    	XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
            Node resultsServer =  parser.selectSingleNode(serverXml, "/GVSupport/*[@name='" + nomeServizio + "']");
            Node resultsZip = parser.selectSingleNode(newXml, "/GVSupport/*[@name='" + nomeServizio + "']");
            Node parentServer =  parser.selectSingleNode(serverXml, "/GVSupport");
	        if (resultsZip != null) {
		        if (resultsServer == null) {
	                Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
	                parentServer.appendChild(importedNode);
	                logger.debug("GVSupport[" + nomeServizio + "] non esistente, inserimento");
	            }
		        else {
		            Node importedNode = parentServer.getOwnerDocument().importNode(resultsZip, true);
		            parentServer.replaceChild(importedNode, resultsServer);
		            logger.debug("GVSupport[" + nomeServizio + "] esistente, aggiornamento");
		        }
	        }
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private Document getXmlZip() throws XMLUtilsException, IOException
    {
        String path = java.lang.System.getProperty("java.io.tmpdir");
        return parseXmlFile(path + File.separator + "conf" + File.separator + "GVSupport.xml");
    }

    private Document getXmlServer() throws IOException, XMLConfigException, XMLUtilsException
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
        url = XMLConfig.getURL("GVSupport.xml");
        logger.debug("URL=" + url.getPath());
        return url;
    }

    private Document parseXmlFile(String xmlFilePath) throws XMLUtilsException, IOException
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

    private Document parseXmlURL(URL url) throws XMLUtilsException, IOException
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

    public static void main(String[] args) throws Exception
    {
        // TODO Auto-generated method stub
        GVSupportParser parser = new GVSupportParser();
        parser.loadParser();
        // System.out.println(parser.getGvSupportZip());
        parser.getListaSupportZip();
        // parser.aggiorna();
        // parser.scriviFile();
    }
}

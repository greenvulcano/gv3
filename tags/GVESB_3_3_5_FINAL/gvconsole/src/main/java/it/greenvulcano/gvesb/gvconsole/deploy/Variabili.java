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
import org.w3c.dom.NodeList;

/**
 * Variabili class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class Variabili
{
    private static Logger       logger           = GVLogger.getLogger(Variabili.class);
    private VariabiliGlobali[]  variabiliGlobali = null;
    private Document            serverXml        = null;
    private Document            newXml           = null;
    private URL                 url              = null;

    /**
     * @throws IOException
     * @throws XMLConfigException
     * @throws XMLUtilsException 
     */
    public Variabili() throws IOException, XMLConfigException, XMLUtilsException
    {
        loadParser();
    }

    private void loadParser() throws IOException, XMLUtilsException, XMLConfigException
    {
    	XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
        	
        	newXml = getXmlZip();
            serverXml = getXmlServer();
            
	        NodeList variabili = parser.selectNodeList(newXml, "/GVVariables/Variable");
	        variabiliGlobali = new VariabiliGlobali[variabili.getLength()];
	        for (int i = 0; i < variabili.getLength(); i++) {
	            variabiliGlobali[i] = new VariabiliGlobali();
	            String nome = parser.get(variabili.item(i), "@name");
	            String valore = parser.get(variabili.item(i), "@value");
	            variabiliGlobali[i].setNome(nome);
	            variabiliGlobali[i].setValore(valore);
	            String valoreServer = parser.get(serverXml, "/GVVariables/variable[@name='" + nome + "']/@value");
	            if (valoreServer != null) {
	                variabiliGlobali[i].setValoreServer(valoreServer);
	            }
	            variabiliGlobali[i].setDescrizione(parser.get(variabili.item(i), "Description"));
	            variabiliGlobali[i].setTipo(parser.get(variabili.item(i), "@type"));
	            logger.debug("Nome=" + nome + " Valore=" + valore + " valore Server="
	                    + variabiliGlobali[i].getValoreServer());
	        }
    	}
		finally {
			XMLUtils.releaseParserInstance(parser);
		}
    }

    private Document getXmlZip() throws XMLUtilsException, IOException
    {
        String path = java.lang.System.getProperty("java.io.tmpdir");
        return parseXmlFile(path + File.separator + "conf" + File.separator + "GVVariables.xml");
    }

    private Document getXmlServer() throws IOException, XMLConfigException, XMLUtilsException
    {
        URL url = getUrl();
        return parseXmlURL(url);
    }
    
    private URL getUrl() throws XMLConfigException
    {
        url = XMLConfig.getURL("GVVariables.xml");
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
    
    public void scriviFile() throws IOException
    {
    	DOMWriter domWriter = new DOMWriter();
        FileOutputStream ostream = new FileOutputStream(url.getFile());
        domWriter.write(serverXml, ostream);
        ostream.flush();
        ostream.close();
        logger.debug("Scrittura File" + url.getFile());
    }

    /**
     * @return
     */
    public VariabiliGlobali[] getVariabiliGlobali()
    {
        return variabiliGlobali;
    }

    /**
     * @param input
     * @return
     */
    public VariabiliGlobali[] getVariabiliGlobaliPresenti(String input)
    {
        int numeroVariabiliPresenti = 0;
        VariabiliGlobali[] variabiliGlobaliPresenti = null;
        for (int i = 0; i < variabiliGlobali.length; i++) {
            if (input.contains("%%" + variabiliGlobali[i].getNome() + "%%")) {
                numeroVariabiliPresenti++;
            }
        }
        logger.debug("numeroVariabiliPresenti=" + numeroVariabiliPresenti);
        if (numeroVariabiliPresenti > 0) {
            variabiliGlobaliPresenti = new VariabiliGlobali[numeroVariabiliPresenti];
            int j = 0;
            for (int i = 0; i < variabiliGlobali.length; i++) {

                if (input.contains("%%" + variabiliGlobali[i].getNome() + "%%")) {
                    variabiliGlobaliPresenti[j] = variabiliGlobali[i];
                    j++;
                }
            }
        }

        return variabiliGlobaliPresenti;
    }

    
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        Variabili variabili = new Variabili();
        variabili.loadParser();
        variabili.getVariabiliGlobaliPresenti("ss%%system-activation%%dd");
    }

}

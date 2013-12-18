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

import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.file.FileManager;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.zip.ZipHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;


/**
 *
 * GVParser class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GVParser
{
    private GVCoreParser    gvCoreParser    = null;
    private GVAdapterParser gvAdapterParser = null;
    private GVSupportParser gvSupportParser = null;
    private String          nomeZipFile     = null;
    private static Logger   logger          = GVLogger.getLogger(GVParser.class);

    /**
     * @throws IOException
     * @throws XmlException
     * @throws XMLConfigException
     */
    public GVParser() throws Exception
    {
        logger.debug("Init GVParser");
        gvCoreParser = new GVCoreParser();
        gvAdapterParser = new GVAdapterParser();
        gvCoreParser.setAdapterParser(gvAdapterParser);
        gvAdapterParser.setCoreParser(gvCoreParser);
        gvSupportParser = new GVSupportParser();
        gvCoreParser.loadParser();
        gvAdapterParser.loadParser();
        gvSupportParser.loadParser();
        logger.debug("End GVParser");
    }

    public GVParser(boolean noParse) throws Exception
    {
    }

    public ByteArrayInputStream copyFileForBackupZip() throws Exception
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
            throw exc;
        }
    }
    
    /**
     * The caller MUST close the stream!!!
     * 
     * @return
     * @throws Exception
     */
    public InputStream readFileZip() throws Exception
    {
        String tmpDir = PropertiesHandler.expand("${{java.io.tmpdir}}", null);
        InputStream is = new FileInputStream(new File(tmpDir, nomeZipFile));
        return is;
    }

    public void deleteFileZip() throws Exception
    {
        String tmpDir = PropertiesHandler.expand("${{java.io.tmpdir}}", null);
        FileManager.rm(tmpDir, nomeZipFile);
    }

    /**
     * @return the GVCore parser
     */
    public GVCoreParser getGVCoreParser()
    {
        return gvCoreParser;
    }

    /**
     * @return the GVAdapter parser
     */
    public GVAdapterParser getGVAdapterParser()
    {
        return gvAdapterParser;
    }

    /**
     * @return the GVSupport parser
     */
    public GVSupportParser getGVSupportParser()
    {
        return gvSupportParser;
    }

    public String getNomeFileBackup()
    {
        return nomeZipFile;
    }
    
    public static void main(String[] args) throws Exception
    {
            GVParser parser = new GVParser();
            //parser.creaFileZip();
    }
            

}

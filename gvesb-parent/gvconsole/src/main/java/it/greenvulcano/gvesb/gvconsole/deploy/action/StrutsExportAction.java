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
package it.greenvulcano.gvesb.gvconsole.deploy.action;

import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.file.FileManager;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.zip.ZipHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Struts File Upload Action Form.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class StrutsExportAction extends Action
{

    private static final Logger logger = GVLogger.getLogger(StrutsExportAction.class);

    /**
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response)
    {
        logger.debug("Init StrutsExportAction");
        HttpSession sessione = request.getSession(false);
        try {
            ByteArrayInputStream is = creaFileZip();
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition",
                    "attachment; filename=GVExport_" + DateUtils.nowToString("yyyyMMddHHmmss") + ".zip");
            response.setHeader("Connection", "close");
            response.setContentLength(is.available());
            OutputStream resstream = response.getOutputStream();
            IOUtils.copy(is, resstream);
            resstream.flush();
            resstream.close();
            logger.debug("End StrutsExportAction");
            return mapping.findForward("success");
        }
        catch (Exception exc) {
            logger.error("Error exporting services", exc);
            sessione.setAttribute("message", exc.getMessage());
            return mapping.findForward("unsuccess");
        }
    }

    private ByteArrayInputStream creaFileZip()
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
}

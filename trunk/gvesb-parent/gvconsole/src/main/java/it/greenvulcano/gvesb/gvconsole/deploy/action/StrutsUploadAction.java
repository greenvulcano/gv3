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

import it.greenvulcano.gvesb.gvconsole.deploy.FileZip;
import it.greenvulcano.gvesb.gvconsole.deploy.GVAdapterParser;
import it.greenvulcano.gvesb.gvconsole.deploy.GVCoreParser;
import it.greenvulcano.gvesb.gvconsole.deploy.GVParser;
import it.greenvulcano.gvesb.gvconsole.deploy.GVSupportParser;
import it.greenvulcano.gvesb.gvconsole.deploy.form.StrutsUploadForm;
import it.greenvulcano.log.GVLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

/**
 * Struts File Upload Action Form.
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class StrutsUploadAction extends Action
{

    private static final Logger logger = GVLogger.getLogger(StrutsUploadAction.class);

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
        logger.debug("Init StrutsUploadAction");
        HttpSession sessione = request.getSession(false);
        try {
            StrutsUploadForm myForm = (StrutsUploadForm) form;
            FormFile myFile = myForm.getTheFile();
            String contentType = myFile.getContentType();
            String fileName = myFile.getFileName();

            if (fileName.endsWith("zip")) {
                request.setAttribute("fileName", fileName);
                logger.debug("File zip=" + fileName);
            }
            else {
                request.setAttribute("contentType", contentType);
                return mapping.findForward("unsuccess");
            }
            FileZip fileZip = new FileZip(fileName, myFile.getFileData());
            GVParser parser = new GVParser();
            GVCoreParser parserService = parser.getGVCoreParser();
            GVAdapterParser parserAdapter = parser.getGVAdapterParser();
            GVSupportParser parserSupport = parser.getGVSupportParser();

            sessione.setAttribute("fileZip", fileZip);
            sessione.setAttribute("listaServizi", parserService.getListaServiziZip());
            sessione.setAttribute("listaAdapter", parserAdapter.getListaAdapterZip());
            sessione.setAttribute("listaWebServices", parserAdapter.getListaWebServicesZip());
            sessione.setAttribute("listaDataProvider", parserAdapter.getListaDataProviderZip());
            sessione.setAttribute("listaKnowledgeBaseConfig", parserAdapter.getKnowledgeBaseConfigZip());
            sessione.setAttribute("listaGVExcelWorkbook", parserAdapter.getListaGVExcelWorkbookZip());
            sessione.setAttribute("listaGVExcelRepo", parserAdapter.getListaGVExcelRepoZip());
            sessione.setAttribute("listaGVDataHandler", parserAdapter.getListaGVDataHandlerZip());
            sessione.setAttribute("listaGVHL7", parserAdapter.getListaGVHL7Zip());
            sessione.setAttribute("listaGVHTTP", parserAdapter.getListaGVHTTPZip());
            sessione.setAttribute("listaGVBirtRepo", parserAdapter.getListaGVBirtRepoZip());
            sessione.setAttribute("listaSupport", parserSupport.getListaSupportZip());
            sessione.setAttribute("listaCoreParametri", parserService.getListParameterZip());
            sessione.setAttribute("parser", parser);
            logger.debug("End StrutsUploadAction");
            return mapping.findForward("success");
        }
        catch (Exception exc) {
        	sessione.setAttribute("message", exc.getMessage());
            logger.error("Exception uploading new configuration", exc);
            return mapping.findForward("unsuccess");
        }
    }
}

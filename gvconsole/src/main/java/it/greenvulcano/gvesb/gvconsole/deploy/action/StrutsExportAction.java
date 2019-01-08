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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvconsole.deploy.GVConfig;
import it.greenvulcano.gvesb.gvconsole.deploy.GVConfigZipFile;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.file.FileManager;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.zip.ZipHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.w3c.dom.Node;

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
        	Map <String,String[]> listParameter = request.getParameterMap();
        	List<String> listaServizi = new ArrayList<String>();
        	for(String param:listParameter.keySet()){
        		listaServizi.add(param);
        	}
        	ByteArrayInputStream is = null;
        	GVConfig gvConfig = new GVConfig(XMLConfig.getURL("GVCore.xml"),XMLConfig.getURL("GVAdapters.xml")); 
        	GVConfigZipFile gvConfigZipFile = new GVConfigZipFile(gvConfig);
        	if(listaServizi.size()==0)	
        	    is = gvConfigZipFile.creaFileZip();
        	else
        		is = gvConfigZipFile.creaFileZip(listaServizi);
        		
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition",
                    "attachment; filename=GVExport_" + JMXEntryPoint.getServerName() + "_" + DateUtils.nowToString("yyyyMMddHHmmss") + ".zip");
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
    
    
       
}

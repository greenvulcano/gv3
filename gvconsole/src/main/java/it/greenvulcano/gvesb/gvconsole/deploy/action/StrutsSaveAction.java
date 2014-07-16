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

import it.greenvulcano.gvesb.gvconsole.deploy.DatiServizio;
import it.greenvulcano.gvesb.gvconsole.deploy.GVAdapterParser;
import it.greenvulcano.gvesb.gvconsole.deploy.GVCoreParser;
import it.greenvulcano.gvesb.gvconsole.deploy.GVParser;
import it.greenvulcano.gvesb.gvconsole.deploy.GVSupportParser;
import it.greenvulcano.log.GVLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Struts Parse XML Action Form.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class StrutsSaveAction extends Action
{
    private static final Logger logger = GVLogger.getLogger(StrutsSaveAction.class);

    /**
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        logger.debug("Init StrutsSaveAction");
        HttpSession sessione = request.getSession(false);
        try {
            GVParser parser = (GVParser) sessione.getAttribute("parser");
            String file = request.getParameter("file");
            if (file.equals("GVCore")) {
                String nomeServizio = (String) sessione.getAttribute("servizio");
                GVCoreParser coreParser = parser.getGVCoreParser();
                DatiServizio datiServizio = new DatiServizio();
                datiServizio.setEquals(coreParser.getEqualService(nomeServizio));
                datiServizio.setExist(coreParser.getExist(nomeServizio));
                String cZip = coreParser.getGvCoreZip(nomeServizio);
                if (cZip != null) {
                    datiServizio.setNodoNew(cZip.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
                String cServer = coreParser.getGvCoreServer(nomeServizio);
                if (cServer != null) {
                    datiServizio.setNodoServer(cServer.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
            }
            else if (file.equals("GVAdapters")) {
                String nomeAdapter = (String) sessione.getAttribute("adapter");
                String nomeServizio = (String) sessione.getAttribute("servizio");
                logger.debug("nomeServizio="+nomeServizio);
                logger.debug("nomeAdapter="+nomeAdapter);
                GVAdapterParser adapterParser = parser.getGVAdapterParser();
                adapterParser.loadParser();
                DatiServizio datiServizio = new DatiServizio();
                datiServizio.setEquals(adapterParser.getEqual(nomeAdapter,nomeServizio));
                datiServizio.setExist(adapterParser.getExist(nomeAdapter,nomeServizio));
                String aZip = adapterParser.getGvAdapterZip(nomeAdapter, nomeServizio);
                if (aZip != null) {
                    datiServizio.setNodoNew(aZip.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
                String aServer = adapterParser.getGvAdapterServer(nomeAdapter, nomeServizio);
                if (aServer != null) {
                    datiServizio.setNodoServer(aServer.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
            }
            else if (file.equals("GVSupport")) {
                String support = (String) sessione.getAttribute("support");
                GVSupportParser supportParser = parser.getGVSupportParser();
                supportParser.loadParser();
                DatiServizio datiServizio = new DatiServizio();
                datiServizio.setEquals(supportParser.getEqual(support));
                datiServizio.setExist(supportParser.getExist(support));
                String sZip = supportParser.getGvSupportZip(support);
                if (sZip != null) {
                    datiServizio.setNodoNew(sZip.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
                String sServer = supportParser.getGvSupportServer(support);
                if (sServer != null) {
                    datiServizio.setNodoServer(sServer.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
            }
            logger.debug("End StrutsSaveAction");
            return mapping.findForward("success");
        }
        catch (Exception exc) {
        	sessione.setAttribute("message", exc.getMessage());
            logger.error("Exception saving configuration", exc);
            return mapping.findForward("unsuccess");
        }

    }

}
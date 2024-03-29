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
import it.greenvulcano.gvesb.gvconsole.deploy.GVParser;
import it.greenvulcano.gvesb.gvconsole.deploy.GVSupportParser;
import it.greenvulcano.gvesb.gvconsole.deploy.form.StrutsGestFileForm;
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
 * Struts File Upload Action Form.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class StrutsGestSupportAction extends Action
{

    private static final Logger logger = GVLogger.getLogger(StrutsGestSupportAction.class);

    /**
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response)
    {
        logger.debug("Init StrutsGestSupportAction");
        HttpSession sessione = request.getSession(false);
        try {
            GVParser parser = (GVParser) sessione.getAttribute("parser");
            StrutsGestFileForm formServizio = (StrutsGestFileForm) form;
            String nomeServizio = formServizio.getSupport();
            GVSupportParser gvSupportParser = parser.getGVSupportParser();
            DatiServizio datiServizio = new DatiServizio();
            String tipoOggetto = request.getParameter("tipoOggetto");
            if (tipoOggetto.equals("SCRIPT")) {
                datiServizio.setEquals(gvSupportParser.getEqual(tipoOggetto));
                datiServizio.setExist(gvSupportParser.getExist(tipoOggetto));
                String sZip = gvSupportParser.getGvSupportZip(tipoOggetto);
                if (sZip != null) {
                    datiServizio.setNodoNew(sZip.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
                String sServer = gvSupportParser.getGvSupportServer(tipoOggetto);
                if (sServer != null) {
                    datiServizio.setNodoServer(sServer.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
            }
            else {
                datiServizio.setEquals(gvSupportParser.getEqual(tipoOggetto));
                datiServizio.setExist(gvSupportParser.getExist(tipoOggetto));
                String sZip = gvSupportParser.getGvSupportZip(tipoOggetto);
                if (sZip != null) {
                    datiServizio.setNodoNew(sZip.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
                String sServer = gvSupportParser.getGvSupportServer(tipoOggetto);
                if (sServer != null) {
                    datiServizio.setNodoServer(sServer.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
            }
            sessione.setAttribute("datiServizio", datiServizio);
            sessione.setAttribute("servizio", nomeServizio);
            sessione.setAttribute("tipoOggetto", tipoOggetto);
            sessione.setAttribute("file", "GVSupport");
            logger.debug("End StrutsGestSupportAction");
            return mapping.findForward("success");
        }
        catch (Exception exc) {
        	sessione.setAttribute("message", exc.getMessage());
            logger.error("Exception deploying new support services", exc);
            return mapping.findForward("unsuccess");
        }
    }
}

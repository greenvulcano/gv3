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
import it.greenvulcano.gvesb.gvconsole.deploy.GVCoreParser;
import it.greenvulcano.gvesb.gvconsole.deploy.GVParser;
import it.greenvulcano.gvesb.gvconsole.deploy.Variabili;
import it.greenvulcano.gvesb.gvconsole.deploy.VariabiliGlobali;
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
public class StrutsGestCoreAction extends Action
{

    private static final Logger logger = GVLogger.getLogger(StrutsDeployAction.class);

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
        logger.debug("Init StrutsGestCoreAction");
        HttpSession sessione = request.getSession(false);
        try {
            StrutsGestFileForm formServizio = (StrutsGestFileForm) form;
            String nomeServizio = formServizio.getServizio();
            GVParser parser = (GVParser) sessione.getAttribute("parser");
            GVCoreParser gvCoreParser = parser.getGVCoreParser();
            DatiServizio datiServizio = new DatiServizio();
            String tipoOggetto = request.getParameter("tipoOggetto");
            if (tipoOggetto.equals("Servizio")) {
                datiServizio.setEquals(gvCoreParser.getEqualService(nomeServizio));
                datiServizio.setExist(gvCoreParser.getExist(nomeServizio));
                if (gvCoreParser.getGvCoreZip(nomeServizio) != null) {
                    datiServizio.setNodoNew(gvCoreParser.getGvCoreZip(nomeServizio).replaceAll("\n", "").replaceAll(
                            "\r", "").replaceAll("'", "&apos;"));
                }

                if (gvCoreParser.getGvCoreServer(nomeServizio) != null) {
                    datiServizio.setNodoServer(gvCoreParser.getGvCoreServer(nomeServizio).replaceAll("\n", "").replaceAll(
                            "\r", "").replaceAll("'", "&apos;"));
                }
            }
            else if (request.getParameter("tipoOggetto").equals("Xpath")) {
                datiServizio.setEquals(gvCoreParser.getEqualXpath());
                datiServizio.setExist(gvCoreParser.getExistXpathServer());
                if (gvCoreParser.getGvXpathZip() != null) {
                    datiServizio.setNodoNew(gvCoreParser.getGvXpathZip().replaceAll("\n", "").replaceAll("\r", "").replaceAll(
                            "'", "&apos;"));
                }

                if (gvCoreParser.getGvXpathServer() != null) {
                    datiServizio.setNodoServer(gvCoreParser.getGvXpathServer().replaceAll("\n", "").replaceAll("\r", "").replaceAll(
                            "'", "&apos;"));
                }
            }
            else if (request.getParameter("tipoOggetto").equals("PoolManager")) {
                datiServizio.setEquals(gvCoreParser.getEqualPoolManager());
                datiServizio.setExist(gvCoreParser.getExistPoolManagerServer());
                if (gvCoreParser.getGvPoolManagerZip() != null) {
                    datiServizio.setNodoNew(gvCoreParser.getGvPoolManagerZip().replaceAll("\n", "").replaceAll("\r", "").replaceAll(
                            "'", "&apos;"));
                }

                if (gvCoreParser.getGvPoolManagerServer() != null) {
                    datiServizio.setNodoServer(gvCoreParser.getGvPoolManagerServer().replaceAll("\n", "").replaceAll(
                            "\r", "").replaceAll("'", "&apos;"));
                }
            }
            else if (request.getParameter("tipoOggetto").equals("Task")) {
                datiServizio.setEquals(gvCoreParser.getEqualTask());
                datiServizio.setExist(gvCoreParser.getExistTaskServer());
                if (gvCoreParser.getGVTaskZip() != null) {
                    datiServizio.setNodoNew(gvCoreParser.getGVTaskZip().replaceAll("\n", "").replaceAll("\r", "").replaceAll(
                            "'", "&apos;"));
                }

                if (gvCoreParser.getGVTaskServer() != null) {
                    datiServizio.setNodoServer(gvCoreParser.getGVTaskServer().replaceAll("\n", "").replaceAll("\r", "").replaceAll(
                            "'", "&apos;"));
                }
            }
            else if (request.getParameter("tipoOggetto").equals("ConcurrencyHandler")) {
                datiServizio.setEquals(gvCoreParser.getEqualConcurrencyHandler());
                datiServizio.setExist(gvCoreParser.getExistConcurrencyHandlerServer());
                if (gvCoreParser.getGVConcurrencyHandlerZip() != null) {
                    datiServizio.setNodoNew(gvCoreParser.getGVConcurrencyHandlerZip().replaceAll("\n", "").replaceAll(
                            "\r", "").replaceAll("'", "&apos;"));
                }

                if (gvCoreParser.getGVConcurrencyHandlerServer() != null) {
                    datiServizio.setNodoServer(gvCoreParser.getGVConcurrencyHandlerServer().replaceAll("\n", "").replaceAll(
                            "\r", "").replaceAll("'", "&apos;"));
                }
            }
            else if (request.getParameter("tipoOggetto").equals("CryptoHelper")) {
                datiServizio.setEquals(gvCoreParser.getEqualCryptoHelper());
                datiServizio.setExist(gvCoreParser.getExistCryptoHelperServer());
                if (gvCoreParser.getGVCryptoHelperZip() != null) {
                    datiServizio.setNodoNew(gvCoreParser.getGVCryptoHelperZip().replaceAll("\n", "").replaceAll("\r",
                            "").replaceAll("'", "&apos;"));
                }

                if (gvCoreParser.getGVCryptoHelperServer() != null) {
                    datiServizio.setNodoServer(gvCoreParser.getGVCryptoHelperServer().replaceAll("\n", "").replaceAll(
                            "\r", "").replaceAll("'", "&apos;"));
                }
            }
            else if (request.getParameter("tipoOggetto").equals("Policy")) {
                datiServizio.setEquals(gvCoreParser.getEqualPolicy());
                datiServizio.setExist(gvCoreParser.getExistGVPolicyServer());
                if (gvCoreParser.getGVPolicyZip() != null) {
                    datiServizio.setNodoNew(gvCoreParser.getGVPolicyZip().replaceAll("\n", "").replaceAll("\r", "").replaceAll(
                            "'", "&apos;"));
                }

                if (gvCoreParser.getGVPolicyServer() != null) {
                    datiServizio.setNodoServer(gvCoreParser.getGVPolicyServer().replaceAll("\n", "").replaceAll("\r",
                            "").replaceAll("'", "&apos;"));
                }
            }
            Variabili var = new Variabili();
            VariabiliGlobali[] variabiliGlobali = var.getVariabiliGlobaliPresenti(gvCoreParser.getGvCoreZip(nomeServizio));
            sessione.setAttribute("variabili", variabiliGlobali);
            sessione.setAttribute("datiServizio", datiServizio);
            sessione.setAttribute("servizio", nomeServizio);
            sessione.setAttribute("tipoOggetto", tipoOggetto);
            sessione.setAttribute("file", "GVCore");
            logger.debug("End StrutsGestCoreAction");
            return mapping.findForward("success");
        }
        catch (Exception exc) {
        	sessione.setAttribute("message", exc.getMessage());
            logger.error("Exception deploying new core services", exc);
            return mapping.findForward("unsuccess");
        }
    }
}

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

    private static final Logger logger = GVLogger.getLogger(StrutsGestCoreAction.class);

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
                String sZip = gvCoreParser.getGvCoreZip(nomeServizio);
                if (sZip != null) {
                    datiServizio.setNodoNew(sZip.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
                String sServer = gvCoreParser.getGvCoreServer(nomeServizio);
                if (sServer != null) {
                    datiServizio.setNodoServer(sServer.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
            }
            else if (tipoOggetto.equals("Transformation")) {
                datiServizio.setEquals(gvCoreParser.getEqualTransformation(nomeServizio));
                datiServizio.setExist(gvCoreParser.getExistTransformationServer(nomeServizio));
                String tZip = gvCoreParser.getGvTransformationZip(nomeServizio);
                if (tZip != null) {
                    datiServizio.setNodoNew(tZip.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
                String tServer = gvCoreParser.getGvTransformationServer(nomeServizio);
                if (tServer != null) {
                    datiServizio.setNodoServer(tServer.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
            }
            else if (tipoOggetto.equals("XPath")) {
                datiServizio.setEquals(gvCoreParser.getEqualXpath());
                datiServizio.setExist(gvCoreParser.getExistXpathServer());
                String xpZip = gvCoreParser.getGvXpathZip();
                if (xpZip != null) {
                    datiServizio.setNodoNew(xpZip.replaceAll("\n", "").replaceAll("\r", "").replaceAll(
                            "'", "&apos;"));
                }
                String xpServer = gvCoreParser.getGvXpathServer();
                if (xpServer != null) {
                    datiServizio.setNodoServer(xpServer.replaceAll("\n", "").replaceAll("\r", "").replaceAll(
                            "'", "&apos;"));
                }
            }
            else if (tipoOggetto.equals("PoolManager")) {
                datiServizio.setEquals(gvCoreParser.getEqualPoolManager());
                datiServizio.setExist(gvCoreParser.getExistPoolManagerServer());
                String pmZip = gvCoreParser.getGvPoolManagerZip();
                if (pmZip != null) {
                    datiServizio.setNodoNew(pmZip.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
                String pmServer = gvCoreParser.getGvPoolManagerServer();
                if (pmServer != null) {
                    datiServizio.setNodoServer(pmServer.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
            }
            else if (tipoOggetto.equals("TimerTask")) {
                datiServizio.setEquals(gvCoreParser.getEqualTask());
                datiServizio.setExist(gvCoreParser.getExistTaskServer());
                String tZip = gvCoreParser.getGVTaskZip();
                if (tZip != null) {
                    datiServizio.setNodoNew(tZip.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
                String tServer = gvCoreParser.getGVTaskServer();
                if (tServer != null) {
                    datiServizio.setNodoServer(tServer.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
            }
            else if (tipoOggetto.equals("ConcurrencyHandler")) {
                datiServizio.setEquals(gvCoreParser.getEqualConcurrencyHandler());
                datiServizio.setExist(gvCoreParser.getExistConcurrencyHandlerServer());
                String chZip = gvCoreParser.getGVConcurrencyHandlerZip();
                if (chZip != null) {
                    datiServizio.setNodoNew(chZip.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
                String chServer = gvCoreParser.getGVConcurrencyHandlerServer();
                if (chServer != null) {
                    datiServizio.setNodoServer(chServer.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
            }
            else if (tipoOggetto.equals("CryptoHelper")) {
                datiServizio.setEquals(gvCoreParser.getEqualCryptoHelper());
                datiServizio.setExist(gvCoreParser.getExistCryptoHelperServer());
                String chZip = gvCoreParser.getGVCryptoHelperZip();
                if (chZip != null) {
                    datiServizio.setNodoNew(chZip.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
                String chServer = gvCoreParser.getGVCryptoHelperServer();
                if (chServer != null) {
                    datiServizio.setNodoServer(chServer.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
            }
            else if (tipoOggetto.equals("ACLPolicy")) {
                datiServizio.setEquals(gvCoreParser.getEqualPolicy());
                datiServizio.setExist(gvCoreParser.getExistGVPolicyServer());
                String pZip = gvCoreParser.getGVPolicyZip();
                if (pZip != null) {
                    datiServizio.setNodoNew(pZip.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
                String pServer = gvCoreParser.getGVPolicyServer();
                if (pServer != null) {
                    datiServizio.setNodoServer(pServer.replaceAll("\n", "").replaceAll("\r", "").replaceAll("'", "&apos;"));
                }
            }
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

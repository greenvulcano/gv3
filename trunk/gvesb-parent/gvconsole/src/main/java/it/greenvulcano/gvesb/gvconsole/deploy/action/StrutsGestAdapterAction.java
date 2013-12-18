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
public class StrutsGestAdapterAction extends Action
{

    private static final Logger logger = GVLogger.getLogger(StrutsGestAdapterAction.class);

    /**
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response)
    {
        logger.debug("Init StrutsGestAdapterAction");
        HttpSession sessione = request.getSession(false);
        String nomeAdapter = (String) request.getParameter("adapter");
        String nomeServizio = (String) request.getParameter("servizio");
        logger.debug("nomeServizio="+nomeServizio);
        logger.debug("tipoOggetto="+nomeAdapter);
        try {
            GVParser parser = (GVParser) sessione.getAttribute("parser");
            GVAdapterParser adapterParser = parser.getGVAdapterParser();
            DatiServizio datiServizio = new DatiServizio();
            datiServizio.setEquals(adapterParser.getEqual(nomeAdapter,nomeServizio));
            datiServizio.setExist(adapterParser.getExist(nomeAdapter,nomeServizio));
            if (adapterParser.getGvAdapterZip(nomeAdapter, nomeServizio) != null) {
                datiServizio.setNodoNew(adapterParser.getGvAdapterZip(nomeAdapter, nomeServizio).replaceAll("\n", "").replaceAll(
                        "\r", "").replaceAll("'", "&apos;"));
            }

            if (adapterParser.getGvAdapterServer(nomeAdapter, nomeServizio) != null) {
                datiServizio.setNodoServer(adapterParser.getGvAdapterServer(nomeAdapter, nomeServizio).replaceAll("\n", "").replaceAll(
                        "\r", "").replaceAll("'", "&apos;"));
            }
            Variabili var = new Variabili();
            VariabiliGlobali[] variabiliGlobali = var.getVariabiliGlobaliPresenti(adapterParser.getGvAdapterZip(nomeAdapter,nomeServizio));
            sessione.setAttribute("variabili", variabiliGlobali);
            sessione.setAttribute("datiServizio", datiServizio);
            sessione.setAttribute("file", "GVAdapters");
            sessione.setAttribute("adapter", nomeAdapter);
            sessione.setAttribute("servizio", nomeServizio);
            logger.debug("End StrutsGestAdapterAction");
            return mapping.findForward("success");
        }
        catch (Exception exc) {
        	sessione.setAttribute("message", exc.getMessage());
            logger.error("Exception deploying new adapter services", exc);
            return mapping.findForward("unsuccess");
        }
    }
}

/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.core;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Node;

public interface ContentSelectionRule {
    /**
     * Inizializza il content provider.
     */
    public void init(Node node) throws MaxException;

    /**
     * Seleziona una collezione di contenuti per l'utente dato.
     *
     * @param user utente che sta facendo la richiesta.
     * @param request richiesta dell'utente.
     * @param param parametro ulteriore passato alla regola.
     *
     * @return un array di Map contenente gli attributi dei contenuti
     *         selezionati.
     */
    public Map[] select(HttpServletRequest request, String param) throws MaxException;
}

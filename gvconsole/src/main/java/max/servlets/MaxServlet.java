/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.servlets;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import max.util.MultipartFormDataParser;
import max.util.Parameter;

/**
 * Classe astratta per la costruzione di servlets in grado di fare
 * l'upload di files spediti dall'utente.<p>
 *
 * <code>MaxServlet</code> ridefinisce i metodi <code>doPost</code>
 * e <code>doGet</code>. I metodi prelevano i parametri, li
 * depositano in un oggetto <code>Parameter</code> ed invocano il
 * metodo <code>service</code> astratto.</p>
 *
 * Il metodo <code>doPost</code>, in funzione del content-type della
 * richiesta, pu� instanziare un oggetto <code>MultipartFormDataParser</code>.
 * Fare riferimento alla documentazione di <code>MultipartFormDataParser</code>
 * per ulteriori dettagli.<p>
 *
 * Il metodo <code>service</code> definito da <code>MaxServlet</code>
 * dovrebbe utilizzare l'oggetto <code>Parameter</code> anzich�
 * <code>HttpServletRequest</code> per ottenere i parametri, tuttavia
 * l'oggetto <code>HttpServletRequest</code> deve essere utilizzato
 * per ottenere la sessione o gli attributi della richiesta.<p>
 *
 * L'oggetto <code>Parameter</code> pu� essere utilizzato sia per
 * ottenere i parametri della richiesta, sia dati di cui il client
 * esegue un upload.<p>
 *
 * @see max.util.Parameter
 * @see max.util.MultipartFormDataParser
 */
public abstract class MaxServlet extends HttpServlet {
    /**
     * It is necessary that the request is sent as specified in the
     * <code>max.util.MultipartFormDataParser</code>.
     *
     * @see max.util.MultipartFormDataParser
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean multipart = true;
        String contentType = request.getContentType();
        if (contentType != null) {
            multipart = contentType.indexOf("multipart/form-data") != -1;
        }
        MultipartFormDataParser mp = null;
        Parameter params = null;
        try {
            if (multipart) {
                mp = new MultipartFormDataParser(request);
                params = new Parameter(request, mp);
            }
            else {
                params = new Parameter(request);
            }
        }
        catch (MessagingException exc) {
            throw new ServletException(exc);
        }
        service(request, response, params);
    }

    /**
     * Manage GET requests.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Parameter params = new Parameter(request);
        service(request, response, params);
    }

    /**
     * Gestisce la richiesta.
     *
     * @param request
     * @param response
     * @param params utilizzare questo oggetto per leggere i parametri della richiesta.
     *      Questo oggetto permette inoltre di ottenere gli stream necessari per eseguire
     *      l'upload di files dal client.
     */
    public abstract void service(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, IOException;
}

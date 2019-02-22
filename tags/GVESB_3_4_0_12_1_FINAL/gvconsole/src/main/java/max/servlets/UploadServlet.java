/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.servlets;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import max.util.MultipartFormDataParser;

/**
 * Costruisce un oggetto <code>max.util.MultipartFormDataParser</code>
 * per raccogliere i dati inviati dal client, e lo deposita come attributo
 * della richiesta con il nome indicato dalla costante
 * <code>UploadServlet.MULTIPART</code>.<p>
 *
 * Inoltra il tutto al servlet dal nome specificato dal parametro
 * di configurazione <code>forwardTo</code> sul file <code>web.xml</code>.<br>
 * Il servlet destinatario pu� ottenere i dati inviati dall'utente tramite
 * l'oggetto <code>max.util.MultipartFormDataParser</code> come nell'esempio
 * seguente:
 * <pre>
 *    ...
 *    MultipartFormDataParser mp;
 *    mp = (MultipartFormDataParser)req.getAttribute(UploadServlet.MULTIPART);
 *    ...
 *    InputStream photo = mp.getInputStream("photo");
 *    String name = mp.getString("name");
 *    ...
 * </pre>
 * <p>
 *
 * E' possibile utilizzare anche la classe <code>java.util.Parameter</code> per leggere
 * coerentemente i parametri passati sia con <code>HttpServletRequest</code> che con
 * <code>MultipartFormDataParser</code>, come mostrato nell'esempio seguente:
 * <pre>
 *    ...
 *    MultipartFormDataParser mp;
 *    mp = (MultipartFormDataParser)req.getAttribute(UploadServlet.MULTIPART);
 *    Parameter params = new Parameter(request, mp);
 *    ...
 *    InputStream photo = params.getInputStream("photo");
 *    String name = params.getString("name");
 *    ...
 * </pre>
 * <p>
 *
 * @see max.util.MultipartFormDataParser
 * @see max.util.Parameter
 */
public class UploadServlet extends HttpServlet {
    /**
     *
     */
    private static final long  serialVersionUID = -7375899572771385057L;
    public static final String MULTIPART        = "__max__MultipartFormDataParser__";
    private String             forwardTo        = null;

    @Override
    public void init() throws ServletException {
        ServletConfig sc = getServletConfig();
        forwardTo = getInitParameter("forwardTo");
        if (forwardTo == null) {
            throw new ServletException("'forwardTo' parameter must be defined for servlet '" + sc.getServletName()
                    + "'");
        }
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            MultipartFormDataParser mp = new MultipartFormDataParser(request);
            request.setAttribute(MULTIPART, mp);
            ServletContext ctx = getServletConfig().getServletContext();
            RequestDispatcher rd = ctx.getNamedDispatcher(forwardTo);
            rd.forward(request, response);
        }
        catch (IOException exc) {
            throw new ServletException("Uploading", exc);
        }
        catch (MessagingException exc) {
            throw new ServletException("Uploading", exc);
        }
    }
}

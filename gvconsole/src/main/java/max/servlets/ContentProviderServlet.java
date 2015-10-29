/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.servlets;

import java.io.InputStream;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import max.core.ContentProvider;
import max.core.Contents;

/**
 * Fornisce un contenuto di un <code>ContentProvider</code>.<p>
 */
public class ContentProviderServlet extends HttpServlet {
    /**
     *
     */
    private static final long serialVersionUID = 3480544243404496783L;
    private ContentProvider   provider;

    @Override
    public void init(ServletConfig config) throws ServletException {
        String providerName = config.getInitParameter("provider");
        if (providerName == null) {
            throw new ServletException("No 'provider' parameter specified");
        }

        try {
            provider = Contents.instance().getProvider(providerName);
        }
        catch (Exception exc) {
            throw new ServletException(exc);
        }
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            ServletOutputStream out = response.getOutputStream();

            String pathInfo = request.getPathInfo();

            StringTokenizer tk = new StringTokenizer(pathInfo, "/", false);
            String category = tk.nextToken();
            String content = request.getRequestURL().toString();

            InputStream istream = provider.get(category, content);
            if (istream == null) {
                throw new ServletException("Content '" + content + "' in category '" + category + "' not found");
            }

            byte buf[] = new byte[2048];
            int l;
            while ((l = istream.read(buf)) != -1) {
                out.write(buf, 0, l);
            }
            istream.close();
        }
        catch (Exception exc) {
            throw new ServletException(exc);
        }
    }
}

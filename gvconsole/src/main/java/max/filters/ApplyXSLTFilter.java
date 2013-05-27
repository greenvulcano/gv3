package max.filters;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;


/**
 * Interpreta la risposta della risorsa come XML ed applica lo
 * stylesheet XSLT dichiarato nell'XML.<p>
 *
 * Se l'XML non specifica alcuno stylesheet XSLT, allora ci si basa
 * sull'estensione del file. Se l'estensione e' .xyz allora si accede
 * al parametro xslt_xyz per ottenere lo stylesheet da applicare.
 * Lo stylesheet da applicare e' relativo alla root della web application.<br>
 * Se il parametro xslt_xyz non esiste, non si applica alcuno stylesheet.
 */
public class ApplyXSLTFilter implements Filter
{
    private FilterConfig     filterConfig   = null;
    private ServletContext   servletContext = null;
    private static Hashtable cache          = new Hashtable();

    public void init(FilterConfig filterConfig) throws ServletException
    {
        this.filterConfig = filterConfig;
        servletContext = filterConfig.getServletContext();
    }

    public void destroy()
    {
        filterConfig = null;
    }

    public FilterConfig getFilterConfig()
    {
        return filterConfig;
    }

    public void setFilterConfig(FilterConfig filterConfig)
    {
        this.filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException
    {
        try {
            doFilterPriv((HttpServletRequest) request, (HttpServletResponse) response, chain);
        }
        catch (Exception exc) {
            exc.printStackTrace();
            throw new ServletException(exc);
        }
    }

    private void doFilterPriv(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws Exception
    {
        // Calcola il nome assoluto del file richiesto.
        //
        String fileName = request.getServletPath();
        File absoluteFileName = new File(servletContext.getRealPath(fileName));

        // Prepara un wrapper per contenere la risposta.
        //
        CharResponseWrapper responseWrapper = new CharResponseWrapper(response);

        // Invoca la catena con il wrapper ed ottiene il risultato
        //
        chain.doFilter(request, responseWrapper);
        String result = responseWrapper.toString();

        // Prepara un DOM con il risultato
        //
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(result.getBytes()));
        DOMSource source = new DOMSource(doc);

        // Cerca il nodo <?xml:stylesheet ... ?> per ottenere il nome dello
        // stylesheet XSLT
        //
        File stylesheet = null;
        Node n = doc.getFirstChild();
        while (n != null) {
            if (n instanceof ProcessingInstruction) {
                ProcessingInstruction pi = (ProcessingInstruction) n;
                if (pi.getTarget().equals("xml:stylesheet")) {
                    String data = pi.getData();
                    int idx = data.indexOf("href=\"");
                    if (idx != -1) {
                        int idx2 = data.indexOf("\"", idx + 6);
                        if (idx2 != -1) {
                            String str = data.substring(idx + 6, idx2);
                            stylesheet = new File(absoluteFileName.getParent(), str);
                            break;
                        }
                    }
                }
            }
            n = n.getNextSibling();
        }

        // Se non si e' definito alcuno stylesheet ci calcoliamo
        // l'estensione
        //
        String ext = null;
        if (stylesheet == null) {
            String str = absoluteFileName.getName();
            int idx = str.lastIndexOf('.');
            if (idx != -1)
                ext = str.substring(idx + 1);
        }

        // Ottiene il Transformer da applicare.
        //
        Transformer trans = getTransformer(stylesheet, ext);

        // Poiche' i Transformer sono in cache puo' accadere che
        // piu' threads ottengano lo stesso, quindi sincronizziamo.
        // Il risultato dello stylesheet e' dirottato verso il Writer
        // della risposta.
        //
        if (trans == null) {
            response.getWriter().print(result);
        }
        else {
            synchronized (trans) {
                StreamResult streamResult = new StreamResult(response.getWriter());
                trans.transform(source, streamResult);
            }
        }
    }

    /**
     * Ottiene uno stylesheet cotenuto nella cache.<br>
     * L'accesso alla cache e' ottenuto direttamente dal nome del file.<br>
     * Gli stylesheet modificati sono ricaricati.
     */
    private Transformer getTransformer(File stylesheet, String ext) throws Exception
    {
        Transformer trans = null;

        // Se non e' stato specificato alcun stylesheet XSLT
        //
        if (stylesheet == null) {
            if (ext == null)
                return null;

            String par = "xslt_" + ext;
            String val = filterConfig.getInitParameter(par);
            if (val == null) {
                System.out.println("No parameter " + par + " found for filter " + filterConfig.getFilterName());
                return null;
            }

            stylesheet = new File(servletContext.getRealPath(val));
        }

        // Qui stylesheet e' valorizzato o perche' era specificato nell'XML
        // o perche' e' stato prelevato dal parametro del filtro.

        TransformerDatePair pair = (TransformerDatePair) cache.get(stylesheet);
        if (pair != null) {
            long time = stylesheet.lastModified();
            if (time > pair.time) {
                synchronized (cache) {
                    cache.remove(stylesheet);
                }
                pair = null;
            }
        }
        if (pair != null)
            trans = pair.trans;
        if (trans == null) {
            TransformerFactory transFact = TransformerFactory.newInstance();
            trans = transFact.newTransformer(new StreamSource(stylesheet));
            synchronized (cache) {
                if (!cache.contains(stylesheet)) {
                    cache.put(stylesheet, new TransformerDatePair(trans, stylesheet.lastModified()));
                }
            }
        }

        return trans;
    }

    private class TransformerDatePair
    {
        public Transformer trans;
        public long        time;

        public TransformerDatePair(Transformer trans, long time)
        {
            this.trans = trans;
            this.time = time;
        }
    }
}

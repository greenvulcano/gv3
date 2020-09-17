/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.xml;

import it.greenvulcano.configuration.XMLConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.mail.MessagingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import max.core.MaxException;
import max.servlets.MaxServlet;
import max.util.Parameter;
import max.util.ParameterException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Gestisce le operazioni per lavorare con i files XML.
 * <p>
 *
 * Le operazioni implementate sono:
 * <li><code>registerDTD</code> - registrazione di un DTD.
 * <li><code>updateDTD</code> - aggiorna un DTD.
 * <li><code>ui</code> - azioni per l'<code>XMLBuilder</code>.
 * <li><code>downloadXML</code> - ottiene l'xml correntemente in editing
 * <li><code>editClientDoc</code> - fa l'upload di un file XML dell'utente e lo
 * edita
 * <li><code>newDoc</code> - costruisce un nuovo documento
 * <li><code>editDTD</code> - edita gli attributi di un DTD
 * <li><code>downloadDTD</code> - ottiene un DTD
 * <li><code>downloadXSLT</code> - ottiene un DTD
 * <li><code>deleteDTD</code> - elimina un DTD
 * <li><code>deleteXSLT</code> - elimina un XSLT associato ad un DTD
 * <li><code>registerXSLT</code> - registra un XSLT per i dettagli
 * dell'interfaccia
 * <li><code>action</code> - esegue un'operazione del menu'
 *
 * <br>
 * Le operazioni sono determinate dal parametro <code>operation</code>.
 */
public class MaxXMLServlet extends MaxServlet {
    // ----------------------------------------------------------------------------
    // INNESCO DELLE OPERAZIONI
    // ----------------------------------------------------------------------------

    /**
     *
     */
    private static final long serialVersionUID = -5456739588747740621L;

    /**
     * Invoca l'operazione richiesta.
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, IOException {
        try {
            String operation = params.getString("operation", true);
            String forwardTo = getServletConfig().getInitParameter("forwardTo_" + operation);
            if (forwardTo == null) {
                throw new ServletException("No servlet parameter 'forwardTo_" + operation
                        + "' defined in web.xml for servlet '" + getServletName() + "'");
            }

            if (operation.equals("ui")) {
                userInterface(request, response, params);
            }
            else if (operation.equals("editClientDoc")) {
                editClientDoc(request, response, params);
            }
            else if (operation.equals("downloadXML")) {
                downloadXML(request, response, params);
            }
            else if (operation.equals("newDoc")) {
                newDocument(request, response, params);
            }
            else if (operation.equals("editDTD")) {
                editDTD(request, response, params);
            }
            else if (operation.equals("downloadDTD")) {
                downloadDTD(request, response, params);
            }
            else if (operation.equals("downloadXSLT")) {
                downloadXSLT(request, response, params);
            }
            else if (operation.equals("deleteDTD")) {
                deleteDTD(request, response, params);
            }
            else if (operation.equals("deleteXSLT")) {
                deleteXSLT(request, response, params);
            }
            else if (operation.equals("registerDTD")) {
                registerDTD(request, response, params);
            }
            else if (operation.equals("updateDTD")) {
                updateDTD(request, response, params);
            }
            else if (operation.equals("registerXSLT")) {
                registerXSLT(request, response, params);
            }
            else if (operation.equals("action")) {
                action(request, response, params);
            }
            else if (operation.equals("graphic")) {
                doVisualEditor(request, response, params);
            }
            else if (operation.equals("graphic-save")) {
                saveVisualEditor(request, response, params);
            }
            else {
                throw new ServletException("Invalid 'operation' parameter: '" + operation + "'");
            }

            if (!forwardTo.equals("-")) {
                ServletContext ctx = getServletConfig().getServletContext();
                RequestDispatcher rd = ctx.getRequestDispatcher(forwardTo);
                rd.forward(request, response);
            }
        }
        catch (MaxException exc) {
            exc.printStackTrace();
            Exception cause = exc.getNestedException();
            if (cause != null) {
                throw new ServletException(cause);
            }
            else {
                throw new ServletException(exc);
            }
        }
        catch (Exception exc) {
            throw new ServletException(exc);
        }
    }

    // ----------------------------------------------------------------------------
    // IMPLEMENTAZIONE DELLE OPERAZIONI
    // ----------------------------------------------------------------------------

    /**
     * @param request
     * @param response
     * @param params
     * @throws ParameterException
     * @throws ServletException
     * @throws TransformerException
     */
    public void saveVisualEditor(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ParameterException, ServletException, TransformerException {

        HttpSession session = request.getSession();
        XMLBuilder builder = (XMLBuilder) session.getAttribute("XMLBuilder");
        if (builder == null) {
            throw new ServletException("XMLBuilder is null");
        }

        if (builder.isReadOnly()) {
            throw new ServletException("You cannot save a read only document");
        }

        InputStream xslOut = MaxXMLServlet.class.getClassLoader().getResourceAsStream(
                params.getString("xsl-out", true, false));

        DOMResult result = new DOMResult();
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(new StreamSource(xslOut));
        transformer.transform(new StreamSource(new StringReader(params.getString("xml_data", true, false))), result);

        // System.out.println("**** Node to be transformed:\n");
        // System.out.println(params.getString("xml_data", true, false));

        /*
         * try { DOMWriter writer = new DOMWriter();
         * System.out.println("*******\nReturned Node: ");
         * writer.write(result.getNode(), System.out); } catch(Exception e) {
         * e.printStackTrace(); }
         */

        builder.doSaveGraph(result.getNode());

    }

    public void doVisualEditor(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ParameterException, ServletException, TransformerException {

        HttpSession session = request.getSession();
        XMLBuilder builder = (XMLBuilder) session.getAttribute("XMLBuilder");
        if (builder == null) {
            throw new ServletException("XMLBuilder is null");
        }

        String xslOut = params.getString("xsl-out", true, false);
        String unique = params.getString("unique", true, false);

        InputStream xslIn = MaxXMLServlet.class.getClassLoader().getResourceAsStream(
                params.getString("xsl-in", true, false));
        Element element = builder.getCurrentElement();

        DOMResult result = new DOMResult();
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(new StreamSource(xslIn));
        transformer.setParameter("outputURL", request.getContextPath() + request.getServletPath()
                + "?operation=graphic-save&unique=" + unique + "&xsl-out=" + xslOut);
        transformer.transform(new DOMSource(element), result);

        request.setAttribute("visual-node", result.getNode());
        request.setAttribute("readOnly", "" + builder.isReadOnly());

        // DEBUGGING

        /*
         * try { DOMWriter writer = new DOMWriter();
         * System.out.println("*******\nVisual Node: ");
         * writer.write(result.getNode(), System.out); } catch(Exception e) {
         * e.printStackTrace(); }
         */
    }

    /**
     * Register a DTD.
     *
     * Request parameters: <li><code>doctype</code> - document type specified by
     * the DTD. <li><code>fpi</code> - Formal Public Identifier for the DTD. Is
     * empty for SYSTEM DTDs. <li><code>uri</code> - URI for the DTD. <li>
     * <code>file</code> - uploading file for the DTD. <li>
     * <code>description</code> - description for the DTD. <li>
     * <code>namespace</code> - namespace URI.
     */
    public void registerDTD(HttpServletRequest req, HttpServletResponse resp, Parameter params)
            throws ServletException, IOException, MessagingException, ParameterException, MaxException, SAXException,
            XMLConfigException {
        String doctype = params.getString("doctype", false).trim();
        String fpi = params.getString("fpi", true).trim();
        String uri = params.getString("uri", false).trim();
        String description = params.getString("description", true);
        String namespace = params.getString("namespace", true);
        InputStream file = params.getInputStream("file");
        if (file == null) {
            throw new ServletException("Invalid 'file' parameter");
        }

        MaxDTDParser dtdParser = new MaxDTDParser();
        try {
            DocumentModel dm = dtdParser.parseDTD(file);

        }
        catch (SAXException exc) {
            throw new ServletException(exc);
        }

        file = params.getInputStream("file");

        MaxEntityResolver mer = (MaxEntityResolver) getEntityResolver(true);
        if (fpi.equals("")) {
            fpi = null;
        }
        if (uri.equals("")) {
            uri = null;
        }
        mer.insert(doctype, fpi, uri, file, description, namespace);
    }

    /**
     * Update a DTD.
     *
     * Request parameters: <li><code>key</code> - URI for the DTD. <li>
     * <code>file</code> - uploading file for the DTD.
     *
     * @throws XMLConfigException
     */
    public void updateDTD(HttpServletRequest req, HttpServletResponse resp, Parameter params) throws ServletException,
            IOException, MessagingException, ParameterException, MaxException, SAXException, XMLConfigException {
        String uri = params.getString("key", false).trim();
        InputStream file = params.getInputStream("file");
        if (file == null) {
            throw new ServletException("Invalid 'file' parameter");
        }

        MaxDTDParser dtdParser = new MaxDTDParser();
        try {
            DocumentModel dm = dtdParser.parseDTD(file);

        }
        catch (SAXException exc) {
            throw new ServletException(exc);
        }

        file = params.getInputStream("file");

        MaxEntityResolver mer = (MaxEntityResolver) getEntityResolver(true);
        mer.update(uri, file);
    }

    /**
     * Edit un documento XML del client.
     *
     * Request parameters: <li><code>file</code> - uploading XML file to edit.
     */
    public void editClientDoc(HttpServletRequest req, HttpServletResponse resp, Parameter params)
            throws ServletException, IOException, MessagingException, ParameterException, MaxException {
        try {
            InputStream file = params.getInputStream("file");
            if (file == null) {
                throw new ServletException("Invalid 'file' parameter");
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(true);
            dbf.setNamespaceAware(true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setEntityResolver(getEntityResolver(false));
            Document document = db.parse(file);
            XMLBuilder builder = new XMLBuilder(document);
            builder.addDefaultMenuActions();
            builder.storeInSession(req.getSession());

        }
        catch (MaxException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new MaxException(exc);
        }
    }

    /**
     * Register a XSLT.
     *
     * Request parameters: <li><code>key</code> - identify the DTD which will
     * have the XSLT associated. <li><code>file</code> - uploading file for the
     * XSLT.
     *
     * @throws XMLConfigException
     */
    public void registerXSLT(HttpServletRequest req, HttpServletResponse resp, Parameter params)
            throws ServletException, IOException, MessagingException, ParameterException, MaxException,
            XMLConfigException {
        String key = params.getString("key", true, false);
        InputStream file = params.getInputStream("file");

        try {
            // Check if the XSLT has a correct syntax
            TransformerFactory tFactory = TransformerFactory.newInstance();
            // Transformer transformer = tFactory.newTransformer(new
            // StreamSource(file));

            Transformer transformer = tFactory.newTransformer(new StreamSource(file));
        }
        catch (TransformerConfigurationException exc) {
            throw new MaxException(exc);
        }

        file = params.getInputStream("file");

        MaxXMLFactory xmlFactory = MaxXMLFactory.instance();
        xmlFactory.registerXSLT(key, file);
    }

    /**
     * Modify the DTD attributes.
     *
     * Request parameters: <li><code>doctype</code> - document type specified by
     * the DTD. <li><code>fpi</code> - Formal Public Identifier for the DTD. Is
     * empty for SYSTEM DTDs. <li><code>uri</code> - URI for the DTD. <li>
     * <code>description</code> - description for the DTD. <li><code>key</code>
     * - identify the DTD to modify. <li><code>namespace</code> - namespace URI.
     *
     * @throws XMLConfigException
     */
    public void editDTD(HttpServletRequest req, HttpServletResponse resp, Parameter params) throws ServletException,
            IOException, MessagingException, ParameterException, MaxException, XMLConfigException {
        String doctype = params.getString("doctype", false).trim();
        String fpi = params.getString("fpi", true).trim();
        String uri = params.getString("uri", false).trim();
        String description = params.getString("description", true);
        String key = params.getString("key", true, false);
        String namespace = params.getString("namespace", true, true);

        MaxEntityResolver mer = (MaxEntityResolver) getEntityResolver(true);
        if (fpi.equals("")) {
            fpi = null;
        }
        if (uri.equals("")) {
            uri = null;
        }
        mer.updateAttributes(key, doctype, fpi, uri, description, namespace);
    }

    /**
     * Delete a DTD.
     *
     * Request parameters: <li><code>key</code> - identify the DTD to modify.
     *
     * @throws XMLConfigException
     */
    public void deleteDTD(HttpServletRequest req, HttpServletResponse resp, Parameter params) throws ServletException,
            IOException, MessagingException, ParameterException, MaxException, XMLConfigException {
        String key = params.getString("key", true, false);

        MaxEntityResolver mer = (MaxEntityResolver) getEntityResolver(true);
        mer.delete(key);
    }

    /**
     * Delete a XSLT.
     *
     * Request parameters: <li><code>key</code> - identify the XSLT to delete.
     *
     * @throws XMLConfigException
     */
    public void deleteXSLT(HttpServletRequest req, HttpServletResponse resp, Parameter params) throws ServletException,
            IOException, MessagingException, ParameterException, MaxException, XMLConfigException {
        String uri = params.getString("uri", true, true);
        if (uri.equals("")) {
            uri = null;
        }

        MaxXMLFactory xmlFactory = MaxXMLFactory.instance();
        xmlFactory.removeXSLT(uri);
    }

    /**
     * Download a DTD.
     *
     * Request parameters: <li><code>fpi</code> <li><code>uri</code>
     */
    public void downloadDTD(HttpServletRequest req, HttpServletResponse resp, Parameter params)
            throws ServletException, IOException, MessagingException, ParameterException, MaxException, SAXException {
        String fpi = params.getString("fpi", true, true);
        String uri = params.getString("uri", true, true);

        EntityResolver er = getEntityResolver(false);
        if (fpi.equals("")) {
            fpi = null;
        }
        if (uri.equals("")) {
            uri = null;
        }

        resp.setContentType("text/plain");

        InputSource isource = er.resolveEntity(fpi, uri);
        InputStream is = isource.getByteStream();
        ServletOutputStream out = resp.getOutputStream();
        byte buf[] = new byte[2048];
        int l;
        while ((l = is.read(buf)) != -1) {
            out.write(buf, 0, l);
        }
        is.close();
    }

    /**
     * Download a XSLT.
     *
     * Request parameters: <li><code>fpi</code> <li><code>uri</code>
     *
     * @throws XMLConfigException
     */
    public void downloadXSLT(HttpServletRequest req, HttpServletResponse resp, Parameter params)
            throws ServletException, IOException, MessagingException, ParameterException, MaxException, SAXException,
            XMLConfigException {
        String fpi = params.getString("fpi", true, true);
        String uri = params.getString("uri", true, true);

        EntityResolver er = getEntityResolver(false);

        if (fpi.equals("")) {
            fpi = null;
        }
        if (uri.equals("")) {
            uri = null;
        }

        resp.setContentType("text/plain");

        MaxXMLFactory xmlFactory = MaxXMLFactory.instance();
        InputStream is = xmlFactory.getXSLT(uri);

        if (is != null) {
            ServletOutputStream out = resp.getOutputStream();
            byte buf[] = new byte[2048];
            int l;
            while ((l = is.read(buf)) != -1) {
                out.write(buf, 0, l);
            }
            is.close();
        }
        else {
            PrintWriter pw = resp.getWriter();
            pw.println("No detail XSLT defined for DTD. " + (fpi != null ? "FPI: " + fpi : "") + " URI: " + uri);
            pw.flush();
            pw.close();
        }
    }

    /**
     * Download the current XML.
     */
    public void downloadXML(HttpServletRequest req, HttpServletResponse resp, Parameter params)
            throws ServletException, IOException, MessagingException, ParameterException, MaxException, SAXException,
            TransformerConfigurationException, TransformerException {
        resp.setContentType("text/xml");

        XMLBuilder builder = XMLBuilder.getFromSession(req.getSession());

        if (builder == null) {
            return;
        }

        DocumentModel documentModel = builder.getDocumentModel();

        ServletOutputStream out = resp.getOutputStream();
        DOMWriter writer = new DOMWriter();
        writer.write(builder.getDocument(), new PrintStream(out));
    }

    /**
     * Manage an action performed in the user interface.
     *
     * Request parameters: <li><code>opkey</code> - chiave dell'operazione da
     * eseguire cos� come specificata da <code>XMLBuilder</code>. <li>
     * <code>cmd</code> - per le operazioni di edit di elementi deve valere '
     * <code>edit</code>'.<br>'<code>setAttributes</code>' imposta tutti gli
     * attributi dell'elemento corrente; in tal caso i parametri di nome
     * 'attr_XXX' indicano i valori degli attributi 'XXX'. <li>
     * <code>value</code> - per le operazioni di edit specifica il valore da
     * impostare.
     *
     * @see max.xml.XMLBuilder
     */
    public void userInterface(HttpServletRequest request, HttpServletResponse resp, Parameter params)
            throws ServletException, IOException, MessagingException, ParameterException, MaxException {
        XMLBuilder builder = XMLBuilder.getFromSession(request.getSession());

        if (builder != null) {

            String op = params.getString("opkey", true);
            String cmd = params.getString("cmd", false, true);

            if (cmd.equals("edit")) {
                String value = params.getString("value", true);
                builder.doOperation(op, value);
            }
            else if (cmd.equals("treeSelect")) {
                builder.doTreeSelect(new Integer(op).intValue());
            }
            else if (cmd.equals("setAttributes")) {
                Hashtable values = new Hashtable();
                for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
                    String param = (String) e.nextElement();
                    if (param.startsWith("attr_")) {
                        String val = request.getParameter(param);
                        values.put(param.substring(5), val);
                    }
                }
                builder.setAttributeValues(values);
            }
            else if (cmd.equals("undo")) {
                builder.undo();
            }
            else if (cmd.equals("redo")) {
                builder.redo();
            }
            else if (cmd.equals("tree-on")) {
                builder.activateTree();
            }
            else if (cmd.equals("tree-off")) {
                builder.deactivateTree();
            }
            else if (cmd.equals("autocheck-off")) {
                builder.setAutoCheck(false);
            }
            else if (cmd.equals("autocheck-on")) {
                builder.setAutoCheck(true);
            }
            else if (cmd.equals("check")) {
                builder.checkDocument(true);
            }
            else if (cmd.equals("graphic-mode-on")) {
                builder.setGraphicMode(true);
            }
            else if (cmd.equals("graphic-mode-off")) {
                builder.setGraphicMode(false);
                builder.setNewNode(null);
                builder.checkDocument(true);
            }
            else {
                builder.doOperation(op, null);
            }
        }
    }

    /**
     * Create a new document and put it into the session.
     *
     * Request parameters: <li><code>doctype</code> - document type specified by
     * the DTD. <li><code>fpi</code> - Formal Public Identifier for the DTD. Is
     * empty for SYSTEM DTDs. <li><code>uri</code> - URI for the DTD.
     */
    public void newDocument(HttpServletRequest req, HttpServletResponse resp, Parameter params)
            throws ServletException, IOException, MessagingException, ParameterException, MaxException, SAXException {
        String publicId = params.getString("fpi", true, true);
        if (publicId.equals("")) {
            publicId = null;
        }
        String systemId = params.getString("uri", true);
        String doctype = params.getString("doctype", true);
        String namespace = params.getString("namespace", true, true);
        EntityResolver er = getEntityResolver(false);
        InputSource is = er.resolveEntity(publicId, systemId);
        XMLBuilder builder = null;
        if (is != null) {
            builder = new XMLBuilder(doctype, is, publicId, systemId, namespace);
            builder.addDefaultMenuActions();

            builder.storeInSession(req.getSession());

        }
        else {
            throw new ServletException("Invalid fpi, uri and doctype");
        }
    }

    /**
     * Execute a menu action.
     *
     * Request parameters: <li><code>key</code> - operation key.
     */
    public void action(HttpServletRequest req, HttpServletResponse resp, Parameter params) throws ParameterException,
            ServletException {
        XMLBuilder builder = XMLBuilder.getFromSession(req.getSession());

        if (builder != null) {
            String key = params.getString("key", false);
            MenuAction action = builder.getMenuAction(key);
            try {
                if (action != null) {
                    action.doAction(builder, req, resp, params);
                }
            }
            catch (Exception exc) {
                throw new ServletException(exc);
            }
        }
    }

    // ----------------------------------------------------------------------------
    // UTILITIES
    // ----------------------------------------------------------------------------

    private EntityResolver getEntityResolver(boolean mustBeMax) throws MaxException {
        MaxXMLFactory xmlFact = MaxXMLFactory.instance();
        EntityResolver er = xmlFact.getEntityResolver();
        if (mustBeMax) {
            if (!(er instanceof MaxEntityResolver)) {
                throw new MaxException("No MaxEntityResolver. Found " + er.getClass());
            }
        }
        return er;
    }
}

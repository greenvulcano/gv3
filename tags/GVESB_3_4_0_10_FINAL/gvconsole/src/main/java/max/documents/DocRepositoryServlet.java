/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
package max.documents;

import it.greenvulcano.configuration.XMLConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import max.core.ContentProvider;
import max.core.Contents;
import max.core.MaxException;
import max.servlets.MaxServlet;
import max.util.Parameter;
import max.util.ParameterException;
import max.xml.DOMWriter;
import max.xml.XMLBuilder;

import org.w3c.dom.Document;

/**
 * This class is the web interface to the <code>DocumentRepository</code>.
 * <p>
 *
 * The <code>DocRepositoryServlet</code> uses a content provider in order to
 * obtains stylesheet for views. The content provider name is specified by the
 * init parameter <code>view-content-provider</code>.
 * <p>
 *
 * The <code>DocRepositoryServlet</code> uses some pages in case of error or
 * warning. These pages are specified by following initialization parameters:
 *
 * <ul>
 * <li><code>edit-page</code>
 * <li><code>warning-page</code>
 * <li><code>document-page </code>
 * <li><code>administrator-page </code>
 * </ul>
 *
 * The <code>DocRepositoryServlet</code> exposes following methods.
 *
 * <ul>
 * <li><code>showRepository</code>
 * <li><code>selectDocument(name: Sttring)</code>
 * <li><code>forceSelectDocument(name: String)</code>
 * <li><code>forceRollback(name: String, version: int, notes: String)</code>
 * <li><code>viewDocument(name: String)</code>
 * <li><code>viewDocumentVersion(name: String, version: int)</code>
 * <li><code>insertView(name: String, view: InputStream)</code>
 * <li><code>insertFilter(name: String, filter: InputStream)</code>
 * </ul>
 *
 */
public class DocRepositoryServlet extends MaxServlet
{
    /**
     *
     */
    private static final long   serialVersionUID        = 4946975230555587985L;

    private static final String DOC_GROUP_SESSION_PARAM = "max.documents.DocRepositoryServlet.DOC_GROUP";

    private String              VIEW_PROVIDER_NAME;

    private String              EDIT_PAGE;
    private String              WARNING_PAGE;
    private String              WARNING_LOCK_PAGE;
    private String              DOCUMENT_PAGE;
    private String              ADMINISTRATOR_PAGE;
    private static final String CHECK_WARNINGS          = "CHECK_WARNINGS";

    /**
     * This method is the variable initialization method or object
     */
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        VIEW_PROVIDER_NAME = getParameter(config, "view-content-provider");

        EDIT_PAGE = getParameter(config, "edit-page");
        WARNING_PAGE = getParameter(config, "warning-page");
        WARNING_LOCK_PAGE = getParameter(config, "warning-lock-page");
        DOCUMENT_PAGE = getParameter(config, "document-page");
        ADMINISTRATOR_PAGE = getParameter(config, "administrator-page");
    }

    /**
     * Manage the request.
     *
     * @param request
     * @param response
     * @param params
     *        using this object to read the parameter of the request.
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, IOException
    {
        try {
            String operation = request.getPathInfo();
            if (operation == null) {
                operation = params.getString("operation", true, false);
            }

            if (operation.equals("/showRepository")) {
                showRepository(request, response, params);
            }
            else if (operation.equals("/showHistory")) {
                showHistory(request, response, params);
            }
            else if (operation.equals("/selectDocument")) {
                selectDocument(request, response, params);
            }
            else if (operation.equals("/selectGroup")) {
                selectGroup(request, response, params);
            }
            else if (operation.equals("/forceSelectDocument")) {
                forceSelectDocument(request, response, params);
            }
            else if (operation.equals("/forceRollback")) {
                forceRollback(request, response, params);
            }
            else if (operation.equals("/viewDocument")) {
                viewDocument(request, response, params);
            }
            else if (operation.equals("/viewDocumentVersion")) {
                viewDocumentVersion(request, response, params);
            }
            else if (operation.equals("/insertView")) {
                insertView(request, response, params);
            }
            else if (operation.equals("/insertFilter")) {
                insertFilter(request, response, params);
            }
            else if (operation.equals("/removeView")) {
                removeView(request, response, params);
            }
            else if (operation.equals("/removeFilter")) {
                removeFilter(request, response, params);
            }
            else if (operation.equals("/checkWarnings")) {
                checkWarnings(request, response, params);
            }
            else {
                throw new ServletException("Invalid operation: '" + operation + "'");
            }
        }
        catch (Exception exc) {
            System.err.println("*****************************************************");
            exc.printStackTrace();
            throw new ServletException(exc);
        }
    }

    /**
     * Seleziona un gruppo di documenti.
     *
     * @param request
     * @param response
     * @param params
     * @throws ServletException
     * @throws MaxException
     * @throws IOException
     */
    public void checkWarnings(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, MaxException, IOException, ParameterException
    {
        String group = (String) request.getSession().getAttribute(DOC_GROUP_SESSION_PARAM);

        request.getSession().setAttribute(CHECK_WARNINGS, CHECK_WARNINGS);
        forward(request, response, DOCUMENT_PAGE);
    }

    /**
     * Keeps the visualization request for-Repository.
     */
    public void showRepository(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, MaxException, IOException, XMLConfigException
    {
        HttpServletRequestRoleCheck roleCheck = new HttpServletRequestRoleCheck(request);
        HttpSession session = request.getSession();
        String group = (String) session.getAttribute(DOC_GROUP_SESSION_PARAM);

        String checkWarnings = (String) session.getAttribute(CHECK_WARNINGS);
        session.removeAttribute(CHECK_WARNINGS);
        Document document = DocumentRepository.instance().showRepository(roleCheck, group, checkWarnings != null);

        ClassLoader loader = getClass().getClassLoader();
        InputStream stream = loader.getResourceAsStream("max/documents/document_list.xsl");
        applyView(stream, document, response);
    }

    /**
     * Seleziona un gruppo di documenti.
     *
     * @param request
     * @param response
     * @param params
     * @throws ServletException
     * @throws MaxException
     * @throws IOException
     */
    public void selectGroup(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, MaxException, IOException, ParameterException
    {
        String group = params.getString("group", true, true);
        request.getSession().setAttribute(DOC_GROUP_SESSION_PARAM, group);
        forward(request, response, DOCUMENT_PAGE);
    }

    /**
     * Keeps the visualization request for-History.
     */
    public void showHistory(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, MaxException, ParameterException, IOException, XMLConfigException
    {

        String name = params.getString("name", false);
        HttpServletRequestRoleCheck roleCheck = new HttpServletRequestRoleCheck(request);

        Document document = DocumentRepository.instance().showHistory(name, roleCheck);

        ClassLoader loader = getClass().getClassLoader();
        InputStream stream = loader.getResourceAsStream("max/documents/document_history.xsl");
        applyView(stream, document, response);
    }

    /**
     * Edits a document if it doesn't result already open-end and if the user is
     * of R/W.
     */
    public void selectDocument(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, MaxException, ParameterException, IOException, XMLConfigException
    {

        String name = params.getString("name", false);

        if (!LocksManager.isLocked(name)) {
            if (checkForActiveEditing(request)) {
                DocumentRepository dr = DocumentRepository.instance();
                DocumentDescriptor dd = dr.getDocumentDescriptor(name);
                if (checkRoles(request, dd.getReadWriteRoles())) {

                    // Per le specifiche servlet 2.3
                    // XMLBuilder builder = dr.editDocument(name);

                    // Per le specifiche servlet 2.2
                    XMLBuilder builder = dr.editDocument(name, getServletContext(), false);
                    storeBuilderInSession(name, request, builder, false);
                    forward(request, response, EDIT_PAGE);
                }
                else {
                    throw new ServletException("You cannot edit the document '" + name + "'");
                }
            }
            else {
                request.getSession().setAttribute("readOnly", "false");
                forward(request, response, WARNING_PAGE);
            }
        }
        else {
            forward(request, response, WARNING_LOCK_PAGE);
        }
    }

    /**
     * Force the edit of a document also if there was an other document in edit.
     */
    public void forceSelectDocument(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, MaxException, ParameterException, IOException, XMLConfigException
    {
        HttpSession session = request.getSession();
        LocksManager.unlockDocument(session.getId());

        XMLBuilder.removeFromSession(session);

        String readOnlyStr = (String) request.getSession().getAttribute("readOnly");
        if ((readOnlyStr != null) && readOnlyStr.equals("false")) {
            selectDocument(request, response, params);
        }
        else {
            viewDocument(request, response, params);
        }
    }

    /**
     * Force the rollback of a document on the Version Manager Class.
     */
    public void forceRollback(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, MaxException, ParameterException, IOException, XMLConfigException
    {

        String name = params.getString("name", false);
        int version = params.getInt("version", false);
        String notes = params.getString("notes", false);

        DocumentRepository dr = DocumentRepository.instance();
        DocumentDescriptor dd = dr.getDocumentDescriptor(name);
        if (checkRoles(request, dd.getReadWriteRoles())) {
            try {
                dr.rollback(name, version, notes, request.getRemoteUser());
            } catch (Exception e) {
                throw new ServletException("You cannot rollback the document '" + name + "'", e);
            }
        }
        else {
            throw new ServletException("You cannot rollback the document '" + name + "'");
        }

        forward(request, response, DOCUMENT_PAGE);
    }

    /**
     * Visualize a normal/filtrate document, if it was an user of the external
     * system.
     */
    public void viewDocument(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, ParameterException, IOException, MaxException, XMLConfigException
    {
        String name = params.getString("name", false);
        DocumentRepository dr = DocumentRepository.instance();

        ContentProvider provider = Contents.instance().getProvider(VIEW_PROVIDER_NAME);
        if (provider.exists("stylesheet", name) || dr.hasFilter(name)) {
            Document doc = dr.getDocument(name);
            HttpServletRequestRoleCheck roleCheck = new HttpServletRequestRoleCheck(request);
            doc = dr.applySecurityFilter(name, doc, roleCheck);
            applyView(name, doc, response);
        }
        else {
            DocumentDescriptor dd = dr.getDocumentDescriptor(name);
            if (checkForActiveEditing(request)) {
                if (checkRoles(request, dd.getReadOnlyRoles())) {

                    // Per le specifiche servlet 2.3
                    // XMLBuilder builder = dr.editDocument(name);

                    // Per le specifiche servlet 2.2
                    XMLBuilder builder = dr.editDocument(name, getServletContext(), true);
                    storeBuilderInSession(name, request, builder, true);
                    forward(request, response, EDIT_PAGE);
                }
                else {
                    throw new ServletException("You cannot view the document '" + name + "'");
                }
            }
            else {
                request.getSession().setAttribute("readOnly", "true");
                forward(request, response, WARNING_PAGE);
            }
        }
    }

    /**
     * Visualize the nth version of a normal/filtrate document, if it was an
     * user of the external system.
     */
    public void viewDocumentVersion(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, ParameterException, IOException, MaxException, XMLConfigException
    {

        String name = params.getString("name", false);
        int version = params.getInt("version", false);

        DocumentRepository dr = DocumentRepository.instance();
        Document doc = dr.getDocumentVersion(name, version);
        HttpServletRequestRoleCheck roleCheck = new HttpServletRequestRoleCheck(request);
        doc = dr.applySecurityFilter(name, doc, roleCheck);
        applyView(name, doc, response);
    }

    /**
     * Inserts in the View provider the InputStream View.
     */
    public void insertView(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, ParameterException, IOException, MaxException, XMLConfigException
    {

        String name = params.getString("name", false);
        InputStream view = params.getInputStream("view");
        ContentProvider provider = Contents.instance().getProvider(VIEW_PROVIDER_NAME);
        if (provider.exists("stylesheet", name)) {
            provider.update("stylesheet", name, view);
        }
        else {
            provider.insert("stylesheet", name, view);
        }

        forward(request, response, ADMINISTRATOR_PAGE);
    }

    /**
     * Inserts in the Filter provider the InputStream Filter.
     *
     */
    public void insertFilter(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, ParameterException, IOException, MaxException, XMLConfigException
    {

        String name = params.getString("name", false);
        InputStream filter = params.getInputStream("filter");
        DocumentRepository.instance().setSecurityFilter(name, filter);
        forward(request, response, ADMINISTRATOR_PAGE);
    }

    /**
     * Removes the view associated to a document
     */
    public void removeView(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, ParameterException, IOException, MaxException, XMLConfigException
    {

        String name = params.getString("name", false);
        ContentProvider provider = Contents.instance().getProvider(VIEW_PROVIDER_NAME);
        if (provider.exists("stylesheet", name)) {
            provider.remove("stylesheet", name);
        }

        forward(request, response, ADMINISTRATOR_PAGE);
    }

    /**
     * Removes the filter associated to a document
     *
     */
    public void removeFilter(HttpServletRequest request, HttpServletResponse response, Parameter params)
            throws ServletException, ParameterException, IOException, MaxException, XMLConfigException
    {

        String name = params.getString("name", false);
        DocumentRepository.instance().removeSecurityFilter(name);
        forward(request, response, ADMINISTRATOR_PAGE);
    }

    /**
     * Checks if there is some document in edit.
     */
    public boolean checkForActiveEditing(HttpServletRequest request) throws ServletException, IOException
    {
        XMLBuilder builder = XMLBuilder.getFromSession(request.getSession());

        if (builder == null) {
            // non c'� nulla in editing
            return true;
        }
        else {
            return builder.isReadOnly();
        }
    }

    /**
     * Checks if the user is enabled to edit a document.
     *
     */
    public boolean checkRoles(HttpServletRequest request, String[] rwRoles) throws ServletException, IOException
    {

        for (String rwRole : rwRoles) {

            if (request.isUserInRole(rwRole)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Inserts the XMLBuilder object in the current session.
     */
    public void storeBuilderInSession(String name, HttpServletRequest request, XMLBuilder builder, boolean readOnly)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession();

        builder.storeInSession(session);

        if (!readOnly) {
            LocksManager.lockDocument(name, request);
        }
    }

    public void applyView(String name, Document doc, HttpServletResponse response) throws ServletException,
            IOException, MaxException, XMLConfigException
    {
        ContentProvider provider = Contents.instance().getProvider(VIEW_PROVIDER_NAME);
        InputStream inStream = provider.get("stylesheet", name);
        if (inStream == null) {
            response.setContentType("text/xml");
            DOMWriter domWriter = new DOMWriter();
            domWriter.setWriteDoctype(false);
            try {
                domWriter.write(doc, response.getOutputStream());
            }
            catch (IllegalStateException exc) {
                // Se non pu� ottenere l'output stream scrive sul writer senza
                // utilizzare
                // il DOM writer.
                // Usa il transformer con una codifica che funziona sempre!!

                try {
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
                    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                    transformer.setOutputProperty(OutputKeys.ENCODING, "US-ASCII");
                    StreamResult sResult = new StreamResult(response.getWriter());
                    transformer.transform(new DOMSource(doc), sResult);
                }
                catch (TransformerException exc2) {
                    throw new MaxException(exc2);
                }
            }
        }
        else {
            applyView(inStream, doc, response);
        }
    }

    /**
     * It converts the document in a HTML page.
     *
     */
    public void applyView(InputStream inStream, Document doc, HttpServletResponse response) throws IOException,
            MaxException
    {

        try {
            Transformer transformer = null;
            if (inStream == null) {
                transformer = TransformerFactory.newInstance().newTransformer();
            }
            else {
                transformer = (TransformerFactory.newInstance()).newTransformer(new StreamSource(inStream));
            }

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            // transformer.setOutputProperty(OutputKeys.ENCODING, "US-ASCII");

            StringWriter outxml = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(outxml));

            try {
                ServletOutputStream out = response.getOutputStream();
                out.println(outxml.toString());
            }
            catch (IllegalStateException exc) {
                // Se non pu� ottenere l'output stream prova con il writer
                PrintWriter out = response.getWriter();
                out.println(outxml.toString());
            }
        }
        catch (TransformerException exc) {

            throw new MaxException("ApplyView failed", exc);
        }
    }

    /**
     * Read an init parameter from servlet configuration, throws an exception if
     * the parameter is not specified.
     *
     * @return the value of the parameter
     */
    private String getParameter(ServletConfig config, String param) throws ServletException
    {

        String val = config.getInitParameter(param);
        if (val == null) {
            throw new ServletException("Parameter '" + param + "' not specified");
        }
        return val;
    }

    /**
     * Forward the response.
     *
     * @param request
     * @param response
     * @param page
     *        the page on which go round the response.
     */
    private void forward(HttpServletRequest request, HttpServletResponse response, String page)
            throws ServletException, IOException
    {
        ServletConfig config = getServletConfig();
        ServletContext context = config.getServletContext();
        RequestDispatcher dispatcher = context.getRequestDispatcher(page);
        dispatcher.forward(request, response);
    }

}
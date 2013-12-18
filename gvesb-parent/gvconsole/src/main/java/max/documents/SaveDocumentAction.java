/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.documents;

import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.gvconsole.deploy.GVParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import max.core.MaxException;
import max.util.Parameter;
import max.xml.DOMWriter;
import max.xml.MenuAction;
import max.xml.XMLBuilder;

import org.w3c.dom.Document;

/**
 * This class checks document inconsistencies, save the document with the
 * DocumentProxy, save the document with the VersionManager.
 *
 *
 */
public class SaveDocumentAction extends MenuAction {

    /* DocumentProxy Object */
    private DocumentProxy      documentProxy = null;

    /* DocumentDescriptor Object */
    private DocumentDescriptor desc          = null;

    /* ServletContext Object */
    private ServletContext     context       = null;

    /**
     * SaveDocumentAction Constructor
     */
    public SaveDocumentAction(String key, String label, String description, String target) {
        super(key, label, description, target);
        documentProxy = null;
        desc = null;
    }

    /**
     * This method save the document with the DocumentProxy and save the
     * document with the VersionManager.
     *
     * @param builder
     *            the object to be saved.
     * @param req
     *            : the HttpServletRequest object.
     * @param resp
     *            : the HttpServletResponse object.
     * @throws XMLConfigException
     */
    public void doSave(XMLBuilder builder, HttpServletRequest req, HttpServletResponse resp) throws Exception,
            Exception {
        HttpSession session = req.getSession();
        VersionManager vm = VersionManager.instance();
        String descr = req.getParameter("notes"); 
        InputStream in = null;
        GVParser gvParser = null;
        try {
            if (!vm.exists(desc.getName())) {
                descr = descr + " -> First version";
            }
            if(!req.getRequestURI().equals("/gvconsole/deploy/save.jsp")){
                gvParser = new GVParser(false);
                in = gvParser.copyFileForBackupZip();
                builder.encryptDocument();
                Document document = builder.getDocument();
                documentProxy.save(document);     
                XMLBuilder.removeFromSession(session);
            }
            else{
                gvParser = (GVParser) session.getAttribute("parser");
                in = gvParser.readFileZip();
            }
       
            vm.newDocumentVersion(desc.getName(), in, descr, req.getRemoteUser(), new Date());

            LocksManager.unlockDocument(session.getId());
            gvParser.deleteFileZip();
            // forward to document page.
            if(req.getRequestURI().equals("/gvconsole/deploy/save.jsp"))
                forward(req, resp, "/deploy/listaServiziCore.jsp");
            else
                forward(req, resp, "/def/xmleditor/index.jsp");
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }

    /**
     * This method is the share of the Save class.
     *
     * @param request
     *            : the HttpServletRequest object.
     * @param response
     *            : the HttpServletResponse object.
     * @param params
     *            : Object used to read the parameter of the request.
     */
    @Override
    public void doAction(XMLBuilder builder, HttpServletRequest req, HttpServletResponse resp, Parameter params)
            throws MaxException {

        if (builder.isDocumentInError(true)) {
            forward(req, resp, "/def/xmleditor/xmlwarnings.jsp");
        }
        else {
            forward(req, resp, "/def/documents/save.jsp");
        }
    }

    /**
     * Setter method to increase the value of the DocumentProxy object.
     */
    public void setDocumentProxy(DocumentProxy documentProxy) {
        this.documentProxy = documentProxy;
    }

    /**
     * Setter method to increase the value of the DocumentDescriptor object.
     */
    public void setDocumentDescriptor(DocumentDescriptor description) {
        desc = description;
    }

    /**
     * Set the servlet context.
     *
     * @param context
     *            the servlet context.
     */
    public void setServletContext(ServletContext context) {
        this.context = context;
    }

    /**
     * This method transform a document in InputStream.
     *
     * @param dom
     *            The document to be transformed.
     *
     * @return The InputStream
     */
    private InputStream toInputStream(Document dom) throws MaxException {
        try {
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();

            DOMWriter domWriter = new DOMWriter();
            domWriter.setWriteDoctype(true);
            domWriter.write(dom, ostream);
            ostream.flush();
            return new ByteArrayInputStream(ostream.toByteArray());
        }
        catch (IOException exc) {
            throw new MaxException(exc);
        }
    }

    /**
     * Forward the response.
     *
     * @param request
     * @param response
     * @param page
     *            the page on which go round the response.
     */
    private void forward(HttpServletRequest request, HttpServletResponse response, String page) throws MaxException {
        try {
            // Per le specifiche servlet 2.3
            // HttpSession session = request.getSession();
            // ServletContext context = session.getServletContext();

            RequestDispatcher dispatcher = context.getRequestDispatcher(page);
            dispatcher.forward(request, response);
        }
        catch (Exception exc) {
            throw new MaxException(exc);
        }
    }
}

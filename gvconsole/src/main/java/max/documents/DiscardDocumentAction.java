/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *

 */
package max.documents;

import it.greenvulcano.configuration.XMLConfigException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import max.core.MaxException;
import max.util.Parameter;
import max.xml.MenuAction;
import max.xml.XMLBuilder;

/**
 * This class has been created to allow the user to discard the editing.
 *
 */

public class DiscardDocumentAction extends MenuAction {

    /* ServletContext Object */
    private ServletContext context = null;

    /*
     * DiscardDocumentAction Constructors
     */
    public DiscardDocumentAction(String key, String label, String description, String target) {

        super(key, label, description, target);
    }

    /**
     * This method has the active task of discard the operation. In specifies
     * removed from the session the XMLBuilder object that's in editing.
     *
     * @throws XMLConfigException
     */
    public void doDiscard(HttpServletRequest req, HttpServletResponse resp) throws MaxException, XMLConfigException {

        XMLBuilder.removeFromSession(req.getSession());

        LocksManager.unlockDocument(req.getSession().getId());
        // forward to document page
        forward(req, resp, "/def/xmleditor/index.jsp");
    }

    /**
     *
     */
    @Override
    public void doAction(XMLBuilder builder, HttpServletRequest req, HttpServletResponse resp, Parameter params)
            throws MaxException {
        // forward to discard page
        forward(req, resp, "/def/documents/discard.jsp");
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
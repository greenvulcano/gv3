/*
 * Copyright (c) 2005 Maxime Informatica s.n.c. - All right reserved
 *
 * Created on 3-giu-2005
 *
 */
package max.search;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import max.util.Parameter;
import max.xml.MenuAction;
import max.xml.XMLBuilder;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2005 - All right reserved
 */
public class SearchCurrentDocumentAction extends MenuAction {
    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    //----------------------------------------------------------------------------------------------

    /**
     * @param key
     * @param label
     * @param description
     * @param target
     */
    public SearchCurrentDocumentAction(String key, String label, String description, String target) {
        super(key, label, description, target);
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see max.xml.MenuAction#doAction(max.xml.XMLBuilder, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, max.util.Parameter)
     */
    @Override
    public void doAction(XMLBuilder builder, HttpServletRequest req, HttpServletResponse resp, Parameter params)
            throws Exception {
        forward(req, resp, "/def/search/searchCurrentDocument.jsp");
    }

    //----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    //----------------------------------------------------------------------------------------------

    /**
     * @param request
     * @param response
     * @param page
     */
    private void forward(HttpServletRequest request, HttpServletResponse response, String page) throws Exception {
        HttpSession session = request.getSession();
        ServletContext context = session.getServletContext();
        RequestDispatcher dispatcher = context.getRequestDispatcher(page);
        dispatcher.forward(request, response);
    }
}
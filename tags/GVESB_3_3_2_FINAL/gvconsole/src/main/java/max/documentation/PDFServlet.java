/*
 * Copyright (c) 2004 Maxime Informatica s.n.c. - All right reserved
 *
 * Created on 18-nov-2004
 */
package max.documentation;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2004 - All right reserved
 */
public class PDFServlet extends HttpServlet
{
    // ----------------------------------------------------------------------------------------------
    // SERVLET ENTRY POINT
    // ----------------------------------------------------------------------------------------------

    /**
     *
     */
    private static final long serialVersionUID = 8162128748797234022L;

    /**
     * Servlet method.
     *
     * @param request
     *        The HttpServletRequest object
     * @param response
     *        The HttpServletResponse response
     * @throws ServletException
     *         If an error occurred
     * @throws IOException
     *         If an error occurred
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String documentId = request.getParameter("id");

        try {
            PDFProducer producer = new PDFProducer(documentId);
            int size = producer.produceDocumentation(response.getOutputStream());
            // Prepare response
            response.setContentType("application/pdf");
            response.setContentLength(size);
        }
        catch (Exception exc) {
            throw new ServletException("Cannot produce the PDF document", exc);
        }
    }
}

/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.taglib;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class NoCacheTag extends TagSupport {
    /**
     *
     */
    private static final long serialVersionUID = 7145555951517845986L;

    @Override
    public int doStartTag() throws JspException {
        ServletResponse resp = pageContext.getResponse();
        if (resp instanceof HttpServletResponse) {
            HttpServletResponse response = (HttpServletResponse) resp;
            response.setHeader("Expires", "-1");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
        }
        return SKIP_BODY;
    }
}

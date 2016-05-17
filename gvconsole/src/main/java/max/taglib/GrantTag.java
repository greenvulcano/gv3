/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.taglib;

import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

public class GrantTag extends MaxBodyTagSupport {
    /**
     *
     */
    private static final long serialVersionUID = 1688004119954884497L;
    private String            roles            = null;

    public void setRoles(String val) {
        if (val != null) {
            val = val.trim();
        }
        roles = val;
    }

    @Override
    public boolean startTag() throws JspException {
        if (roles == null) {
            return false;
        }
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        StringTokenizer stRoles = new StringTokenizer(roles, ",");

        while (stRoles.hasMoreTokens()) {
            if (request.isUserInRole(stRoles.nextToken())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean endTag() throws JspException {
        return true;
    }

    /* (non-Javadoc)
     * @see max.taglib.MaxBodyTagSupport#afterBody()
     */
    @Override
    protected boolean afterBody() throws JspException {
        return false;
    }

    /* (non-Javadoc)
     * @see max.taglib.MaxBodyTagSupport#initBody()
     */
    @Override
    protected void initBody() throws JspException {
    }

    /* (non-Javadoc)
     * @see max.taglib.MaxBodyTagSupport#reset()
     */
    @Override
    protected void reset() {
        roles = null;
    }
}
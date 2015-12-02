/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.taglib;

import javax.servlet.jsp.JspException;

public class FirstLoopTag extends MaxBodyTagSupport {
    /**
     *
     */
    private static final long serialVersionUID = 3659506522625859417L;
    private String            id               = null;

    @Override
    public void setId(String val) {
        id = val;
    }

    @Override
    public boolean startTag() throws JspException {
        LoopTag loop = LoopTag.findLoopTag(this, id);
        if (loop == null) {
            return false;
        }

        return loop.isFirstLoop() && !(loop.isExtraLoop()) ? true : false;
    }

    @Override
    public boolean afterBody() throws JspException {
        return false;
    }

    /* (non-Javadoc)
     * @see max.taglib.MaxBodyTagSupport#endTag()
     */
    @Override
    protected boolean endTag() throws JspException {
        return true;
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
        id = null;
    }
}
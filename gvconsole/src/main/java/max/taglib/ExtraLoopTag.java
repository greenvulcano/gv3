/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.taglib;

import javax.servlet.jsp.JspException;

public class ExtraLoopTag extends MaxBodyTagSupport {
    /**
     *
     */
    private static final long serialVersionUID = -6582541720493880876L;
    private String            id               = null;
    private boolean           emptySpecified   = false;
    private boolean           empty;

    @Override
    public void setId(String val) {
        id = val;
    }

    public void setEmpty(String val) throws JspException {
        if (val.equals("no")) {
            emptySpecified = true;
            empty = false;
        }
        else if (val.equals("yes")) {
            emptySpecified = true;
            empty = true;
        }
        else {
            throw new JspException("Tag 'extraLoop', attribute 'empty' can be 'yes' or 'no': '" + val + "'");
        }
    }

    @Override
    public boolean startTag() throws JspException {
        LoopTag loop = LoopTag.findLoopTag(this, id);
        if (loop == null) {
            return false;
        }

        if (!loop.isExtraLoop()) {
            return false;
        }

        if (emptySpecified) {
            if (empty) {
                return loop.getWasEmpty() ? true : false;
            }
            else {
                return !loop.getWasEmpty() ? true : false;
            }
        }
        else {
            return true;
        }
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
        emptySpecified = false;
        empty = false;
    }
}
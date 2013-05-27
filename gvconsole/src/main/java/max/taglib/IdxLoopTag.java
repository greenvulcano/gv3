/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class IdxLoopTag extends TagSupport {
    /**
     *
     */
    private static final long serialVersionUID = -8923873711987719477L;
    private static final int  ADD              = 0;
    private static final int  SET              = 1;

    private String            id               = null;
    private int               op               = ADD;
    private int               value            = 1;

    //private String values[] = null;
    //private String fmt = null;

    @Override
    public void setId(String val) {
        id = val;
    }

    public void setOp(String val) throws JspException {
        if (val.equals("add")) {
            op = ADD;
        }
        else if (val.equals("set")) {
            op = SET;
        }
        else {
            throw new JspException("tag 'idxLoop': attribute 'op' can be 'add' or 'set': you specified '" + val + "'");
        }
    }

    public void setValue(String val) throws JspException {
        try {
            value = Integer.parseInt(val);
        }
        catch (NumberFormatException exc) {
            throw new JspException(exc);
        }
    }

    @Override
    public int doStartTag() {
        LoopTag loop = LoopTag.findLoopTag(this, id);
        if (loop == null) {
            return SKIP_BODY;
        }

        if (op == ADD) {
            int idx = loop.getUserIndex();
            loop.setUserIndex(idx + value);
        }
        else if (op == SET) {
            loop.setUserIndex(value);
        }

        return SKIP_BODY;
    }
}

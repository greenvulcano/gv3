/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import max.config.Config;

public class PropertyTag extends TagSupport {
    /**
     *
     */
    private static final long serialVersionUID = 1007307363330841940L;
    private String            sect             = "";
    private String            prop             = "";

    public void setSect(String val) {
        sect = val;
    }

    public void setProp(String val) {
        prop = val;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            String val = Config.getDef(sect, prop, "");
            JspWriter out = pageContext.getOut();
            out.print(val);
            return SKIP_BODY;
        }
        catch (IOException exc) {
            throw new JspException(exc);
        }
    }
}

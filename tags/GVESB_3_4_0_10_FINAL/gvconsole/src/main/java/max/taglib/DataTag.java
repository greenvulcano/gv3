/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.taglib;

import java.util.StringTokenizer;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import max.util.FormatManager;
import max.util.Formatter;

public class DataTag extends TagSupport {
    /**
     *
     */
    private static final long serialVersionUID = -6406026169728901170L;
    private String            id               = null;
    private String            fld              = null;
    private String            fields[]         = null;
    private Object            values[]         = null;
    private Formatter         fmt              = null;

    @Override
    public void setId(String val) {
        id = val;
    }

    public void setFld(String val) {
        fld = val;
        StringTokenizer st = new StringTokenizer(fld, ",", false);
        fields = new String[st.countTokens()];
        values = new Object[fields.length];
        for (int i = 0; i < fields.length; ++i) {
            fields[i] = st.nextToken().trim();
        }
    }

    public void setFmt(String val) throws JspException {
        try {
            fmt = FormatManager.instance().get(val);
        }
        catch (Exception exc) {
            throw new JspException(exc);
        }
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            LoopTag loop = LoopTag.findLoopTag(this, id);
            if (loop == null) {
                return SKIP_BODY;
            }

            for (int i = 0; i < fields.length; ++i) {
                values[i] = loop.getField(fields[i]);
            }

            String val;
            if (fmt == null) {
                StringBuffer sb = new StringBuffer();
                sb.append(values[0]);
                for (int i = 1; i < values.length; ++i) {
                    sb.append(" ").append(values[i]);
                }
                val = sb.toString();
            }
            else {
                val = fmt.format(values);
            }

            JspWriter out = pageContext.getOut();
            out.print(val);
            return SKIP_BODY;
        }
        catch (Exception exc) {
            throw new JspException(exc);
        }
    }
}

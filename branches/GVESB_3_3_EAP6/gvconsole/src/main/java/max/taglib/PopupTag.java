/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.taglib;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import max.config.Config;

public class PopupTag extends MaxBodyTagSupport {
    /**
     *
     */
    private static final long  serialVersionUID = 6179448055736264297L;

    public static final String DEFAULT_STYLE    = "<nobr><b>[?]</b></nobr>";

    private String             target           = "__max_popup";
    private String             style            = Config.getDef("", "max.popup.help.button", DEFAULT_STYLE);
    private String             section          = null;
    private String             property         = null;

    public void setTarget(String val) {
        target = val;
    }

    public void setStyle(String val) throws JspException {
        int idx = val.indexOf('!');
        if (idx == -1) {
            throw new JspException("PopupTag: 'style' mu be in the form 'section!property' (" + val + ")");
        }

        section = val.substring(0, idx).trim();
        property = val.substring(idx + 1).trim();
        style = Config.get(section, property);
        if (style == null) {
            throw new JspException("PopupTag: no property '" + property + "' in section '" + section + "'");
        }
    }

    @Override
    protected void reset() {
        target = "__max_popup";
        style = Config.getDef("", "max.popup.help.button", DEFAULT_STYLE);
        section = null;
        property = null;
    }

    @Override
    public boolean startTag() throws JspException {
        String str = (String) pageContext.getAttribute("__max__popup_support__", PageContext.PAGE_SCOPE);
        if (str == null) {
            str = Config.get("", "max.popup.support");
            if (str == null) {
                throw new JspException("No 'max.popup.support' defined in default section");
            }
            pageContext.setAttribute("__max__popup_support__", "*", PageContext.PAGE_SCOPE);
            JspWriter out = pageContext.getOut();
            try {
                out.println(str);
            }
            catch (IOException exc) {
                throw new JspException(exc);
            }
        }
        return true;
    }

    private static int id = 0;

    private static synchronized int getVarId() {
        return ++id;
    }

    @Override
    public boolean endTag() throws JspException {
        if (bodyContent == null) {
            return true;
        }
        String content = bodyContent.getString();
        bodyContent.clearBody();

        if (content == null) {
            return true;
        }

        StringBuffer jscontent = new StringBuffer();
        StringTokenizer tk = new StringTokenizer(content, "\r\n\t'\\", true);
        while (tk.hasMoreTokens()) {
            String s = tk.nextToken();
            if (s.length() == 1) {
                if (s.equals("\r")) {
                    jscontent.append("\\r");
                }
                else if (s.equals("\n")) {
                    jscontent.append("\\n");
                }
                else if (s.equals("\t")) {
                    jscontent.append("\\t");
                }
                else if (s.equals("'")) {
                    jscontent.append("\\'");
                }
                else if (s.equals("\\")) {
                    jscontent.append("\\\\");
                }
                else {
                    jscontent.append(s);
                }
            }
            else {
                jscontent.append(s);
            }
        }

        try {
            //JspWriter out = pageContext.getOut();
            String varName = "__max_popup_var" + getVarId();
            bodyContent.println("<script>");
            bodyContent.println("var " + varName + " = '" + jscontent.toString() + "';");
            bodyContent.println("</script>");
            bodyContent.print("<a href=\"javascript:popup('" + target + "'," + varName
                    + ")\" style=\"color: #191970\">");
            bodyContent.print(style.trim());
            bodyContent.print("</a>");
        }
        catch (IOException exc) {
            throw new JspException(exc);
        }

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
}
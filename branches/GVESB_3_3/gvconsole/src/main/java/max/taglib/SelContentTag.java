/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:46 $ $Header:
 * /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS
 * /core/webapps/gvconsole/src/main/java/max/taglib/SelContentTag.java,v 1.1
 * 2010-04-03 15:28:46 nlariviera Exp $ $Id: SelContentTag.java,v 1.1 2010-04-03
 * 15:28:46 nlariviera Exp $ $Name: $ $Locker: $ $Revision: 1.1 $ $State: Exp $
 */
package max.taglib;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import max.core.ContentSelectionRule;
import max.core.Contents;
import max.util.MapComparator;


public class SelContentTag extends TagSupport
{
    /**
     *
     */
    private static final long   serialVersionUID = -6765953243443976222L;

    private static final String PROCESSED_ATTR   = "__max_selContent_tag_processed__";

    private String              group            = null;
    private String              rule             = null;
    private String              param            = null;
    private String              attribute        = null;
    private String              scopeStr         = "session";
    private int                 scope            = PageContext.SESSION_SCOPE;
    private String              orderBy          = null;

    private MapComparator       comparator       = null;

    public void setGroup(String val)
    {
        group = val;
    }

    public void setRule(String val)
    {
        rule = val;
    }

    public void setParam(String val)
    {
        param = val;
    }

    public void setAttribute(String val)
    {
        attribute = val;
    }

    public void setOrderBy(String val) throws JspException
    {
        orderBy = val;
        StringTokenizer tk = new StringTokenizer(orderBy, ",", false);
        int N = tk.countTokens();
        String keys[] = new String[N];
        int mode[] = new int[N];
        for (int i = 0; i < N; ++i) {
            String t = tk.nextToken();
            int idx = t.indexOf(' ');
            if (idx == -1) {
                keys[i] = t.trim();
                mode[i] = 1;
            }
            else {
                keys[i] = t.substring(0, idx).trim();
                String md = t.substring(idx + 1).trim().toUpperCase();
                if (md.equals("ASC"))
                    mode[i] = 1;
                else if (md.equals("DESC"))
                    mode[i] = -1;
                else {
                    throw new JspException("Tag 'selContent': attribute 'orderBy': mode can be only 'ASC' or 'DESC' ("
                            + md + ")");
                }
            }
        }
        comparator = new MapComparator(keys, mode);
    }

    public void setScope(String val) throws JspException
    {
        if (val.equals("page")) {
            scope = PageContext.PAGE_SCOPE;
        }
        else if (val.equals("request")) {
            scope = PageContext.REQUEST_SCOPE;
        }
        else if (val.equals("session")) {
            scope = PageContext.SESSION_SCOPE;
        }
        else if (val.equals("application")) {
            scope = PageContext.APPLICATION_SCOPE;
        }
        else {
            throw new JspException(
                    "'selContent' tag: attribute 'scope' can be one of 'page', 'request', 'session' or 'application' ("
                            + val + ")");
        }
        scopeStr = val;
    }


    public int doStartTag() throws JspException
    {
        if (group != null) {
            Hashtable processed = (Hashtable) pageContext.getAttribute(PROCESSED_ATTR);
            if (processed != null) {
                if (processed.get(group) != null) {
                    return SKIP_BODY;
                }
            }
        }

        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        try {
            Contents contents = Contents.instance();
            ContentSelectionRule selectionRule = contents.getSelectionRule(rule);
            Map attribs[] = selectionRule.select(request, param);

            if (comparator != null) {
                Arrays.sort(attribs, comparator);
            }

            pageContext.setAttribute(attribute, attribs, scope);
        }
        catch (Exception exc) {
            exc.printStackTrace();
            try {
                JspWriter out = pageContext.getOut();
                out.println("" + exc);
            }
            catch (IOException exc2) {
            }
            throw new JspException(exc);
        }
        /*
         * catch(JspException exc) { throw exc; } //
         */

        if (group != null) {
            Hashtable processed = (Hashtable) pageContext.getAttribute(PROCESSED_ATTR);
            if (processed == null) {
                processed = new Hashtable();
                pageContext.setAttribute(PROCESSED_ATTR, processed);
            }
            processed.put(group, processed);
        }

        return SKIP_BODY;
    }
}

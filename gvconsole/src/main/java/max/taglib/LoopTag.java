/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.taglib;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import max.db.ResultSetEx;

/**
 * @author
 */
public class LoopTag extends MaxBodyTagSupport {
    /**
     *
     */
    private static final long serialVersionUID = 5819080189240609568L;

    /**
     *
     */
    private String            attribute        = null;

    /**
     *
     */
    private String            id               = null;

    /**
     *
     */
    private boolean           doextraloop      = false;

    /**
     *
     */
    private boolean           extraloop        = false;

    /**
     *
     */
    private int               scope            = PageContext.SESSION_SCOPE;

    /**
     *
     */
    private int               storeScope       = PageContext.PAGE_SCOPE;

    /**
     *
     */
    private String            storeAttr        = null;

    /**
     *
     */
    private int               offset           = 0;

    public LoopTag() {
        super();
        reset();
    }

    /**
     * Imposta i valori di default
     */
    @Override
    protected void reset() {
        attribute = null;
        id = null;
        doextraloop = false;
        extraloop = false;
        scope = PageContext.SESSION_SCOPE;
        storeScope = PageContext.PAGE_SCOPE;
        storeAttr = null;
        offset = 0;
        collectionType = 0;
        mrs = null;
        jrs = null;
        iter = null;
        currObject = null;
        array = null;
        index = 0;
        usrindex = 0;
        wasEmpty = false;
        firstLoop = false;
    }

    /**
     *
     */
    public void setExtraloop(String val) throws JspException {
        if (val.equals("no")) {
            doextraloop = false;
        }
        else if (val.equals("yes")) {
            doextraloop = true;
        }
        else {
            throw new JspException("Tag 'loop', attribute 'extraloop' can be 'yes' or 'no': '" + val + "'");
        }
    }

    /**
     *
     */
    public void setAttribute(String val) {
        attribute = val;
    }

    /**
     *
     */
    public void setStoreAttr(String val) {
        storeAttr = val;
    }

    /**
     *
     */
    @Override
    public void setId(String val) {
        id = val;
    }

    /**
     *
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     *
     */
    public void setScope(String val) throws JspException {
        scope = determinaScope(val);
    }

    /**
     *
     */
    public void setStoreScope(String val) throws JspException {
        storeScope = determinaScope(val);
    }

    /**
     *
     */
    private int determinaScope(String val) throws JspException {
        if (val == null) {
            val = "";
        }
        val = val.toLowerCase();
        if (val.equals("application")) {
            return PageContext.APPLICATION_SCOPE;
        }
        else if (val.equals("session")) {
            return PageContext.SESSION_SCOPE;
        }
        else if (val.equals("request")) {
            return PageContext.REQUEST_SCOPE;
        }
        else if (val.equals("page")) {
            return PageContext.PAGE_SCOPE;
        }
        else {
            throw new JspException("LoopTag: 'scope' =  '" + val + "' != 'application', 'session', 'request', 'page'");
        }
    }

    /**
     *
     */
    public void setStartidx(String val) {
        offset = Integer.parseInt(val);
    }

    /**
     *
     */
    private static final int MAX_RESULT_SET  = 1;

    /**
     *
     */
    private static final int JAVA_RESULT_SET = 2;

    /**
     *
     */
    private static final int JAVA_COLLECTION = 3;

    /**
     *
     */
    private static final int JAVA_ARRAY      = 4;

    /**
     *
     */
    private int              collectionType;

    /**
     *
     */
    private ResultSetEx      mrs             = null;

    /**
     *
     */
    private ResultSet        jrs             = null;

    /**
     *
     */
    private Iterator         iter            = null;

    /**
     *
     */
    private Object           currObject      = null;

    /**
     *
     */
    private Object[]         array           = null;

    /**
     *
     */
    private int              index           = 0;

    /**
     *
     */
    private int              usrindex        = 0;

    /**
     *
     */
    private boolean          wasEmpty;

    /**
     *
     */
    private boolean          firstLoop;

    /**
     *
     */
    @Override
    protected boolean startTag() throws JspException {
        if (storeAttr != null) {
            pageContext.setAttribute(storeAttr, this, storeScope);
        }

        wasEmpty = true;
        firstLoop = true;
        boolean procedi;
        Object obj = pageContext.getAttribute(attribute, scope);
        if (obj == null) {
            return false;
        }

        if (obj instanceof ResultSetEx) {
            collectionType = MAX_RESULT_SET;
            mrs = (ResultSetEx) obj;
            procedi = mrs.next();
        }
        else if (obj instanceof ResultSet) {
            collectionType = JAVA_RESULT_SET;
            jrs = (ResultSet) obj;
            try {
                procedi = jrs.next();
            }
            catch (SQLException exc) {
                throw new JspException(exc);
            }
        }
        else if (obj instanceof Collection) {
            collectionType = JAVA_COLLECTION;
            iter = ((Collection) obj).iterator();
            if (iter.hasNext()) {
                currObject = iter.next();
                procedi = true;
            }
            else {
                procedi = false;
            }
        }
        else if (Object[].class.isInstance(obj)) {
            collectionType = JAVA_ARRAY;
            array = (Object[]) obj;
            if (array.length > 0) {
                currObject = array[index];
                procedi = true;
            }
            else {
                procedi = false;
            }
        }
        else {
            collectionType = JAVA_ARRAY;
            array = new Object[] { obj };
            procedi = true;
        }
        if (procedi) {
            wasEmpty = false;
            return true;
        }
        if (doextraloop) {
            extraloop = true;
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see max.taglib.MaxBodyTagSupport#initBody()
     */
    @Override
    protected void initBody() throws JspException {
    }

    @Override
    protected boolean afterBody() throws JspException {
        // Se stiamo effettuando l'extra-loop, allora la precedente
        // era l'ultima valutazione del body.
        if (extraloop) {
            return false;
        }

        ++index;
        firstLoop = false;

        boolean procedi = false;

        switch (collectionType) {

        case MAX_RESULT_SET:
            procedi = mrs.next();
            if (!procedi) {
                mrs.close();
            }
            break;

        case JAVA_RESULT_SET:
            try {
                procedi = jrs.next();
                if (!procedi) {
                    jrs.close();
                }
            }
            catch (SQLException exc) {
                throw new JspException(exc);
            }
            break;

        case JAVA_COLLECTION:
            procedi = iter.hasNext();
            if (procedi) {
                currObject = iter.next();
            }
            break;

        case JAVA_ARRAY:
            procedi = index < array.length;
            if (procedi) {
                currObject = array[index];
            }
            break;

        default:
            procedi = false;
            break;
        }

        if (procedi) {
            return true;
        }
        if (doextraloop) {
            extraloop = true;
            return true;
        }
        return false;
    }

    @Override
    protected boolean endTag() {
        return true;
    }

    public int getIndex() {
        return index + offset;
    }

    public int getUserIndex() {
        return usrindex;
    }

    public void setUserIndex(int idx) {
        usrindex = idx;
    }

    public boolean getWasEmpty() {
        return wasEmpty;
    }

    public boolean isFirstLoop() {
        return firstLoop;
    }

    public boolean isExtraLoop() {
        return extraloop;
    }

    public Object getField(String fld) throws SQLException {
        if (fld.equals("#")) {
            return "" + getIndex();
        }
        else if (fld.equals("*")) {
            return "" + getUserIndex();
        }

        if (extraloop) {
            return "";
        }

        switch (collectionType) {

        case MAX_RESULT_SET:
            return mrs.getObject(fld);

        case JAVA_RESULT_SET:
            return jrs.getObject(fld);

        case JAVA_COLLECTION:
        case JAVA_ARRAY:
            return getField(currObject, fld);

        default:
            return null;
        }
    }

    private static final Class  parmTypes[] = new Class[0];
    private static final Object parms[]     = new Object[0];

    private Object getField(Object o, String fld) {
        if (o instanceof Map) {
            return ((Map) o).get(fld);
        }
        else {
            try {
                Field field = o.getClass().getField(fld);
                return field.get(o);
            }
            catch (NoSuchFieldException exc) {
                try {
                    Method meth = o.getClass().getMethod(fld, parmTypes);
                    return meth.invoke(o, parms);
                }
                catch (Exception exc2) {
                    exc.printStackTrace();
                    return null;
                }
            }
            catch (IllegalAccessException exc) {
                exc.printStackTrace();
                return null;
            }
        }
    }

    public static LoopTag findLoopTag(Tag from, String id) {
        while (true) {
            Tag tag = findAncestorWithClass(from, LoopTag.class);
            if (tag == null) {
                return null;
            }
            LoopTag loopTag = (LoopTag) tag;
            if (id == null) {
                return loopTag;
            }
            if (id.equals(loopTag.getId())) {
                return loopTag;
            }
            from = tag;
        }
    }

    public static String data(ServletRequest req, String loopAttr, String field) throws Exception {
        LoopTag loop = (LoopTag) req.getAttribute(loopAttr);
        if (loop == null) {
            return "";
        }
        return loop.getField(field).toString();
    }

    public static String data(HttpSession session, String loopAttr, String field) throws Exception {
        LoopTag loop = (LoopTag) session.getAttribute(loopAttr);
        if (loop == null) {
            return "";
        }
        return loop.getField(field).toString();
    }

    public static String data(ServletContext context, String loopAttr, String field) throws Exception {
        LoopTag loop = (LoopTag) context.getAttribute(loopAttr);
        if (loop == null) {
            return "";
        }
        return loop.getField(field).toString();
    }

    public static String data(PageContext page, String loopAttr, String field) throws Exception {
        LoopTag loop = (LoopTag) page.getAttribute(loopAttr);
        if (loop == null) {
            return "";
        }
        return loop.getField(field).toString();
    }
}
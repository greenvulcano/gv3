/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:46 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/taglib/RepeatLoopTag.java,v 1.1 2010-04-03 15:28:46 nlariviera Exp $
 * $Id: RepeatLoopTag.java,v 1.1 2010-04-03 15:28:46 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.taglib;

import java.sql.SQLException;

import javax.servlet.jsp.JspException;

public class RepeatLoopTag extends MaxBodyTagSupport
{
    /**
     *
     */
    private static final long serialVersionUID = -5730997428345207865L;
    private String id = null;
    private String fld = null;
    private String type = "number";
    private String op = "=";
    private String value = null;
    private boolean negate = false;

    public void setId(String val)
    {
        id = val;
    }

    public void setFld(String val)
    {
        fld = val;
    }

    public void setType(String val) throws JspException
    {
        if(val.equals("string") || val.equals("number")) {
            type = val;
        }
        else {
            throw new JspException(
                "tag 'repeatLoop': attribute 'type' can be 'number' or 'string': you specified '"
                    + val + "'");
        }
    }

    public void setNegate(String val) throws JspException
    {
        if(val.equals("yes") || val.equals("no")) {
            negate = val.equals("yes");
        }
        else {
            throw new JspException(
                "tag 'repeatLoop': attribute 'negate' can be 'yes' or 'no': you specified '" + val
                    + "'");
        }
    }

    public void setOp(String val) throws JspException
    {
        boolean ok = val.equals("<") || val.equals("<=") || val.equals("=") || val.equals("<>")
            || val.equals(">=") || val.equals(">") || val.startsWith("%");

        if(!ok) throw new JspException("condLoop tag: invalid 'op' attribute: " + val);

        op = val;
    }

    public void setValue(String val)
    {
        value = val.trim();
    }

    public boolean startTag() throws JspException
    {
        LoopTag loop = LoopTag.findLoopTag(this, id);
        if(loop == null) return false;

        if(loop.isExtraLoop()) {
            return false;
        }

        if(fld != null) {
            if(value == null) {
                throw new JspException(
                    "tag 'repeatLoop': attribute 'fld' not empty and 'value' empty");
            }

            boolean condition = evalCondition(loop);

            if(negate)
                return !condition;
            else return condition;
        }
        else {
            if(value != null) {
                throw new JspException(
                    "tag 'repeatLoop': attribute 'fld' empty and 'value' not empty");
            }
            return true;
        }
    }

    public boolean afterBody() throws JspException
    {
        return false;
    }

    private boolean evalCondition(LoopTag loop) throws JspException
    {
        String v;
        try {
            Object obj = loop.getField(fld);
            if(obj == null)
                v = "";
            else v = obj.toString();
        }
        catch(SQLException exc) {
            throw new JspException(exc);
        }

        if(type.equals("string")) {
            int comp = -value.compareTo(v.trim());
            if(op.equals("<")) {
                return comp < 0;
            }
            else if(op.equals("<=")) {
                return comp <= 0;
            }
            else if(op.equals("=")) {
                return comp == 0;
            }
            else if(op.equals("<>")) {
                return comp != 0;
            }
            else if(op.equals(">")) {
                return comp > 0;
            }
            else if(op.equals(">=")) {
                return comp >= 0;
            }
        }
        else if(type.equals("number")) {
            double val = Double.parseDouble(value);
            double valFld = Double.parseDouble(v.trim());
            if(op.equals("<")) {
                return valFld < val;
            }
            else if(op.equals("<=")) {
                return valFld <= val;
            }
            else if(op.equals("=")) {
                return valFld == val;
            }
            else if(op.equals("<>")) {
                return valFld != val;
            }
            else if(op.equals(">")) {
                return valFld > val;
            }
            else if(op.equals(">=")) {
                return valFld >= val;
            }
            else if(op.startsWith("%")) {
                int n = Integer.parseInt(op.substring(1));
                return (((int)valFld) % n) == ((int)val);
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see max.taglib.MaxBodyTagSupport#endTag()
     */
    protected boolean endTag() throws JspException
    {
        return true;
    }

    /* (non-Javadoc)
     * @see max.taglib.MaxBodyTagSupport#initBody()
     */
    protected void initBody() throws JspException
    {
    }

    /* (non-Javadoc)
     * @see max.taglib.MaxBodyTagSupport#reset()
     */
    protected void reset()
    {
        id = null;
        fld = null;
        type = "number";
        op = "=";
        value = null;
        negate = false;
    }
}
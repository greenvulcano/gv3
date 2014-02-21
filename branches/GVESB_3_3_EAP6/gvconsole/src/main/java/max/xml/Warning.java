/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:50 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/Warning.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Id: Warning.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import org.w3c.dom.Element;

public class Warning
{
    //---------------------------------------------------------------------------
    // FIELDS
    //---------------------------------------------------------------------------

    private String warning;
    private Element element;
    private String key;

    //---------------------------------------------------------------------------
    // STATIC METHODS
    //---------------------------------------------------------------------------

    private static int count = 0;

    private static synchronized String makeKey()
    {
        return "warn" + (++count);
    }

    //---------------------------------------------------------------------------
    // CONSTRUCTORS
    //---------------------------------------------------------------------------

    public Warning(String warning, Element element)
    {
        this.warning = warning;
        this.element = element;
        key = makeKey();
    }

    //---------------------------------------------------------------------------
    // GETTERS
    //---------------------------------------------------------------------------

    public String getWarning()
    {
        return warning;
    }

    public Element getElement()
    {
        return element;
    }

    public String getKey()
    {
        return key;
    }

    //---------------------------------------------------------------------------
    // TO STRING
    //---------------------------------------------------------------------------

    public String toString()
    {
        return "Warning[" + key + ", " + element.getNodeName() + ", " + warning + ", " + element + "]";
    }
}


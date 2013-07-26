/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:51 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/ExternalData.java,v 1.1 2010-04-03 15:28:51 nlariviera Exp $
 * $Id: ExternalData.java,v 1.1 2010-04-03 15:28:51 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import org.w3c.dom.*;

/**
 * This class is an hook that provides the ability to insert in the user
 * interface information contained outside of the currently XML document
 * in editing.
 *
 */
public abstract class ExternalData
{
    public static final int ELEMENT = 1;
    public static final int CHILD = 2;
    public static final int ALL = 4;

    public static final String ELEMENT_STR = "element";
    public static final String CHILD_STR = "child";
    public static final String ALL_STR = "all";

    private int when = ALL;

    public void setWhen(String whenStr)
    {
        if(whenStr.equals(ELEMENT_STR)) {
            when = ELEMENT;
        }
        else if(whenStr.equals(CHILD_STR)) {
            when = CHILD;
        }
        else if(whenStr.equals(ALL_STR)) {
            when = ALL;
        }
        else {
            throw new IllegalArgumentException(
                "no " + ELEMENT_STR + ", "
                + CHILD_STR + " or " + ALL_STR + " provided"
            );
        }
    }

    public int getWhen()
    {
        return when;
    }



    public static ExternalData createExternalData(String def)
    {
        try {
            return createExternalDataPriv(def);
        }
        catch(Exception exc) {
            exc.printStackTrace();
            return null;
        }
    }

    private static ExternalData createExternalDataPriv(String def) throws Exception
    {
        int idx1 = def.indexOf(":");
        if(idx1 == -1) {
            throw new IllegalArgumentException("definition of external data is not valid: " + def);
        }

        int idx2 = def.indexOf(":", idx1 + 1);
        if(idx2 == -1) {
            throw new IllegalArgumentException("definition of external data is not valid: " + def);
        }

        String whenStr = def.substring(0, idx1).trim();
        String className = def.substring(idx1 + 1, idx2).trim();
        String param = def.substring(idx2 + 1).trim();

        ExternalData ed = (ExternalData)Class.forName(className).newInstance();
        ed.init(param);
        ed.setWhen(whenStr);

        return ed;
    }


    public abstract void init(String param);
    public abstract Node getData(Document intfc, XMLBuilder builder, Element element);
}

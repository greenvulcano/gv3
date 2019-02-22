/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:50 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/MenuAction.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Id: MenuAction.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import max.util.Parameter;


public abstract class MenuAction
{
    private String key;
    private String label;
    private String description;
    private String target;

    public MenuAction(String key, String label, String description, String target)
    {
        this.key = key;
        this.label = label;
        this.description = description;
        this.target = target;
    }

    public String getKey()
    {
        return key;
    }

    public String getLabel()
    {
        return label;
    }

    public String getDescription()
    {
        return description;
    }

    public String getTarget()
    {
        return target;
    }

    public abstract void doAction(XMLBuilder builder, HttpServletRequest req,
                         HttpServletResponse resp, Parameter params)
                         throws Exception;
}

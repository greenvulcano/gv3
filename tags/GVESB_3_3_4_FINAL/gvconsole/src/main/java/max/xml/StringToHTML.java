/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:49 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/StringToHTML.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Id: StringToHTML.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.*;

public class StringToHTML
{
    public static String toHTML(String str)
    {
        StringTokenizer st = new StringTokenizer(str, "\n", true);
        StringBuffer ret = new StringBuffer();
        while(st.hasMoreTokens()) {
            String tk = st.nextToken();
            if(tk.equals("\n")) ret.append("<br>\n");
            else ret.append(quote(tk));
        }
        return ret.toString();
    }

    public static String quote(String str)
    {
        StringTokenizer st = new StringTokenizer(str, "<>&\"", true);
        StringBuffer ret = new StringBuffer();
        while(st.hasMoreTokens()) {
            String tk = st.nextToken();
            if(tk.equals("<")) ret.append("&lt;");
            else if(tk.equals(">")) ret.append("&gt;");
            else if(tk.equals("&")) ret.append("&amp;");
            else if(tk.equals("\"")) ret.append("&quot;");
            else ret.append(tk);
        }
        return ret.toString();
    }
}

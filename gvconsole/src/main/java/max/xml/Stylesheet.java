/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:49 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/Stylesheet.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Id: Stylesheet.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.StringTokenizer;

/**
 * Provides some functions usefull in the XSLT stylesheets.
 *
 * @author Maxime Informatica s.n.c. -
 */
public class Stylesheet
{
    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    /**
     * Substitute ' and \ characters with \' and \\.
     */
    public static String jsString(String str)
    {
        StringTokenizer tokenizer = new StringTokenizer(str, "'\\", true);
        StringBuffer ret = new StringBuffer();

        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if(token.equals("'")) {
                ret.append("\\'");
            }
            else if(token.equals("\\")) {
                ret.append("\\\\");
            }
            else {
                ret.append(token);
            }
        }

        return ret.toString();
    }
}

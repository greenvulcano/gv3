/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:49 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/DownloadMenuAction.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Id: DownloadMenuAction.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import max.util.*;
import java.io.*;
import javax.servlet.http.*;


public class DownloadMenuAction extends MenuAction
{
    private boolean withDoctype;
    private boolean wholeDocument;
    private String contentType;
    private String charset;

    public DownloadMenuAction(String key, String label, String description, String target)
    {
        super(key, label, description, target);
        withDoctype = true;
        wholeDocument = true;
        contentType = "text/plain";
        charset = "UTF-8";
    }

    public void setWithDoctype(boolean withDoctype)
    {
        this.withDoctype = withDoctype;
    }

    public void setWholeDocument(boolean wholeDocument)
    {
        this.wholeDocument = wholeDocument;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public void setCharset(String charset)
    {
        this.charset = charset;
    }

    public void doAction(XMLBuilder builder, HttpServletRequest req,
                         HttpServletResponse resp, Parameter params)
                         throws Exception
    {
        resp.setContentType(contentType + "; charset=" + charset);
        PrintWriter writer = new PrintWriter(resp.getWriter());
        DOMWriter domWriter = new DOMWriter();
        domWriter.setWriteDoctype(withDoctype);
        if(wholeDocument) {
            domWriter.write(builder.getDocument(), writer);
        }
        else {
            domWriter.write(builder.getCurrentElement(), writer);
        }
        writer.flush();
    }
}

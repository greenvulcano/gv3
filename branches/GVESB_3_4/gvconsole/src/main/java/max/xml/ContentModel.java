/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:50 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/ContentModel.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Id: ContentModel.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

public class ContentModel
{
    public static final int Q_ONE = 0;
    public static final int Q_ONE_OR_MANY = 1;
    public static final int Q_ZERO_OR_ONE = 2;
    public static final int Q_ZERO_OR_MANY = 3;

    public static final int T_SIMPLE_PCDATA = 0;
    public static final int T_SIMPLE_ANY = 1;
    public static final int T_SIMPLE_IDE = 2;
    public static final int T_SIMPLE_EMPTY = 3;
    public static final int T_LIST = 4;
    public static final int T_ALTERNATIVE = 5;

    public int qualifier = Q_ONE;
    public int type = T_SIMPLE_PCDATA;
    public String ide = null;
    public ContentModel children[] = null;


    public ContentModel()
    {
    }

    public String toString()
    {
        StringBuffer bf = new StringBuffer();

        switch(type) {
            case T_SIMPLE_PCDATA:
                bf.append("#PCDATA");
                break;

            case T_SIMPLE_ANY:
                bf.append("ANY");
                break;

            case T_SIMPLE_IDE:
                bf.append(ide);
                break;

            case T_SIMPLE_EMPTY:
                bf.append("EMPTY");
                break;

            case T_LIST:
                bf.append("(");
                for(int i = 0; i < children.length; ++i) {
                    bf.append(children[i].toString());
                    if(i < children.length - 1) bf.append(",");
                }
                bf.append(")");
                break;

            case T_ALTERNATIVE:
                bf.append("(");
                for(int i = 0; i < children.length; ++i) {
                    bf.append(children[i].toString());
                    if(i < children.length - 1) bf.append("|");
                }
                bf.append(")");
                break;
        }

        switch(qualifier) {
            case Q_ONE_OR_MANY:
                bf.append("+");
                break;

            case Q_ZERO_OR_ONE:
                bf.append("?");
                break;

            case Q_ZERO_OR_MANY:
                bf.append("*");
                break;
        }

        return bf.toString();
    }
}

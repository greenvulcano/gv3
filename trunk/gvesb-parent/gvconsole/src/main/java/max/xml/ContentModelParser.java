/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:49 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/ContentModelParser.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Id: ContentModelParser.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.*;
import max.core.MaxException;


/**
 * ContentModelParser costruisce un ContentModel descritto da una stringa
 * generata dalla seguente grammatica a partire dal simbolo
 * <code>CONTENT_MODEL</code>:
 * <pre>
 * CONTENT_MODEL ::= COMPLEX QUALIFIER | "EMPTY" | "ANY"
 * SIMPLE ::= "#PCDATA" | XML_IDE
 * QUALIFIED ::= COMPLEX QUALIFIER | SIMPLE QUALIFIER
 * QUALIFIER ::= "?" | "*" | "+" | ""
 * COMPLEX ::= "(" ALTERNATIVE ")"
 * ALTERNATIVE ::= LIST | LIST "|" ALTERNATIVE
 * LIST ::= QUALIFIED | QUALIFIED "," LIST
 * </pre>
 * <p>
 * ContentModelParser non vuole essere un parser che verifica la bont� della
 * stringa di descrizione del content model, piuttosto il suo scopo �
 * costruire un oggetto ContentModel a partire da una stringa valida.
 * Verificare che il DTD sia un DTD corretto!!.<p>
 * ContentModelParser gestisce alcune sequenze degeneri trasformandole.
 * Per sequenze degeneri si intende una lista o una alternativa composte da
 * un solo elemento, come gli esempi seguenti:
 * <pre>
 * (element+)*
 * (element+)+
 * (element+)?
 * </pre>
 * Verranno cambiati in:
 * <pre>
 * element*
 * element+
 * element*
 * </pre>
 */
public class ContentModelParser
{
    public String contentModel;
    public int pos;

    public ContentModelParser(String contentModel)
    {
        this.contentModel = contentModel == null ? null : contentModel + '\001';
        pos = 0;
    }

    //--------------------------------------------------------------------------

    public ContentModel parseContentModel() throws MaxException
    {
        if(contentModel == null || check("EMPTY")) {
            ContentModel cm = new ContentModel();
            cm.type = ContentModel.T_SIMPLE_EMPTY;
            return cm;
        }
        else if(check("ANY")) {
            ContentModel cm = new ContentModel();
            cm.type = ContentModel.T_SIMPLE_ANY;
            return cm;
        }
        else {
            ContentModel cm = parseComplex();
            return simplify(parseQualifier(cm));
        }
    }

    public ContentModel parseComplex() throws MaxException
    {
        match("(");
        ContentModel cm = parseAlternative();
        match(")");
        return cm;
    }

    public ContentModel parseAlternative() throws MaxException
    {
        Vector v = new Vector();
        while(true) {
            v.addElement(parseList());
            if(check("|")) {
                match("|");
            }
            else {
                ContentModel cm = new ContentModel();
                cm.type = ContentModel.T_ALTERNATIVE;
                cm.children = new ContentModel[v.size()];
                v.copyInto(cm.children);
                return cm;
            }
        }
    }

    public ContentModel parseList() throws MaxException
    {
        Vector v = new Vector();
        while(true) {
            v.addElement(parseQualified());
            if(check(",")) {
                match(",");
            }
            else {
                ContentModel cm = new ContentModel();
                cm.type = ContentModel.T_LIST;
                cm.children = new ContentModel[v.size()];
                v.copyInto(cm.children);
                return cm;
            }
        }
    }

    public ContentModel parseQualified() throws MaxException
    {
        ContentModel cm = null;
        if(check("(")) {
            cm = parseComplex();
        }
        else cm = parseSimple();
        return parseQualifier(cm);
    }

    public ContentModel parseSimple() throws MaxException
    {
        ContentModel cm = null;
        if(check("#PCDATA")) {
            match("#PCDATA");
            cm = new ContentModel();
            cm.type = ContentModel.T_SIMPLE_PCDATA;
        }
        else {
            cm = new ContentModel();
            cm.type = ContentModel.T_SIMPLE_IDE;
            cm.ide = parseIde();
        }
        return cm;
    }

    public ContentModel parseQualifier(ContentModel cm) throws MaxException
    {
        if(check("*")) {
            match("*");
            cm.qualifier = ContentModel.Q_ZERO_OR_MANY;
        }
        else if(check("?")) {
            match("?");
            cm.qualifier = ContentModel.Q_ZERO_OR_ONE;
        }
        else if(check("+")) {
            match("+");
            cm.qualifier = ContentModel.Q_ONE_OR_MANY;
        }
        else {
            cm.qualifier = ContentModel.Q_ONE;
        }
        return cm;
    }

    public static final String validChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-:0123456789";

    public String parseIde()
    {
        String ide = "";
        while(true) {
            char c = contentModel.charAt(pos);
            if(validChars.indexOf(c) != -1) {
                ide += c;
                ++pos;
            }
            else return ide;
        }
    }

    //--------------------------------------------------------------------------

    private boolean check(String tk)
    {
        consumeSpaces();
        int n = pos + tk.length();
        if(n > contentModel.length())return false;
        return contentModel.substring(pos, n).equals(tk);
    }


    private void match(String tk) throws MaxException
    {
        consumeSpaces();
        int n = pos + tk.length();
        if(contentModel.substring(pos, n).equals(tk)) pos = n;
        else error("missing '" + tk + "'");
    }


    private void consumeSpaces()
    {
        while(true) {
            if(pos >= contentModel.length()) {
                return;
            }
            char c = contentModel.charAt(pos);
            if(!Character.isWhitespace(c)) {
                return;
            }
            ++pos;
        }
    }


    private void error(String err) throws MaxException
    {
        throw new MaxException(
            getClass().getName() + ": " + err
            + System.getProperty("line.separator")
            + contentModel.substring(0, contentModel.length() - 1)
            + " [" + pos + "]"
        );
    }

    private ContentModel simplify(ContentModel cm)
    {
        if((cm.type == ContentModel.T_LIST) || (cm.type == ContentModel.T_ALTERNATIVE)) {
            for(int i = 0; i < cm.children.length; ++i) {
                cm.children[i] = simplify(cm.children[i]);
            }
            if(cm.children.length == 1) {
                int qOuter = cm.qualifier;
                int qInner = cm.children[0].qualifier;
                cm = cm.children[0];

                if(qOuter != qInner) {
                    if((qOuter == ContentModel.Q_ZERO_OR_MANY) || (qInner == ContentModel.Q_ZERO_OR_MANY)) {
                        cm.qualifier = ContentModel.Q_ZERO_OR_MANY;
                    }
                    else if(qInner == ContentModel.Q_ONE) {
                        cm.qualifier = qOuter;
                    }
                    else if(qOuter == ContentModel.Q_ONE) {
                        cm.qualifier = qInner;
                    }
                    else {
                        cm.qualifier = ContentModel.Q_ZERO_OR_MANY;
                    }
                }
            }
        }
        return cm;
    }

    //--------------------------------------------------------------------------

    public static void main(String args[]) throws MaxException
    {
        ContentModelParser parser = new ContentModelParser(args[0]);
        ContentModel cm = parser.parseContentModel();
        System.out.println("" + cm);
    }
}

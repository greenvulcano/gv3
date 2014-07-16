/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:55 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/util/StringUtil.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Id: StringUtil.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2004 - All right reserved
 */
public class StringUtil
{
    /**
     * Utilizzata per la gestione delle sequenze di escape.
     */
    private static final char QUOTE = '\001';

    private static final Map SPECIAL_CHARACTERS = new HashMap();
    static {
        SPECIAL_CHARACTERS.put("\\", "\\\\");
        SPECIAL_CHARACTERS.put("\b", "\\b");
        SPECIAL_CHARACTERS.put("\t", "\\t");
        SPECIAL_CHARACTERS.put("\n", "\\n");
        SPECIAL_CHARACTERS.put("\r", "\\r");
        SPECIAL_CHARACTERS.put("\f", "\\f");
    }

    public static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
        'B', 'C', 'D', 'E', 'F'};

    /**
     * Gestisce i caratteri escape delle stringhe: sostituisce le
     * sequenze di escape con i caratteri corrispondenti.
     */
    public static String unescape(String str)
    {
        str = "" + QUOTE + str + QUOTE;
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(str));
        tokenizer.resetSyntax();
        tokenizer.quoteChar(QUOTE);
        try {
            tokenizer.nextToken();
        }
        catch(IOException e) {}
        return tokenizer.sval;
    }

    /**
     * Sostituisce i caratteri \n, \t ecc con la corrispondente sequenza di
     * escape.
     *
     * @param str
     * @return
     */
    public static String escape(String str)
    {
        StringTokenizer tokenizer = new StringTokenizer(str, "\\\b\t\n\r\f", true);
        StringBuffer ret = new StringBuffer();
        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            String escaped = (String)SPECIAL_CHARACTERS.get(token);
            if(escaped != null) {
                ret.append(escaped);
            }
            else {
                ret.append(token);
            }
        }
        return ret.toString();
    }

    /**
     * Serializza in una stringa un oggetto serializzabile.
     *
     * @param serializable
     * @return
     */
    public static String serialize(Serializable serializable) throws IOException
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outStream);
        out.writeObject(serializable);
        out.flush();

        byte[] bytes = outStream.toByteArray();
        StringBuffer buff = new StringBuffer();
        for(int i = 0; i < bytes.length; ++i) {
            buff.append(toHex(bytes[i], 2));
        }
        return buff.toString();
    }

    public static Object deserialize(String str) throws Exception
    {
        int numBytes = str.length() / 2;
        int idx = 0;
        byte[] buffer = new byte[numBytes];
        for(int i = 0; i < numBytes; ++i) {
            char h = str.charAt(idx++);
            char l = str.charAt(idx++);
            buffer[i] = (byte)(Integer.parseInt("" + h + l, 16));
        }

        ByteArrayInputStream inStream = new ByteArrayInputStream(buffer);
        ObjectInputStream in = new ObjectInputStream(inStream);
        return in.readObject();
    }

    /**
     * Ritorna una stringa esadecimale
     *
     * @param nul
     * @param digits
     * @return
     */
    public static String toHex(long num, int digits)
    {
        String ret = "";
        while(digits > 0) {
            int d = (int)(num & 0x0F);
            ret = HEX_DIGITS[d] + ret;
            num >>= 4;
            --digits;
        }
        return ret;
    }

    //----------------------------------------------------------------------------------------------
    // DEBUG
    //----------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception
    {
        boolean loop = true;
        while(loop) {
            String str = MaxConsole.input("Stringa (exit per uscire): ");
            loop = !str.equalsIgnoreCase("exit");
            MaxConsole.println("---------------------------");
            MaxConsole.println("Originale.: " + str);
            MaxConsole.println("Unescaped.: " + unescape(str));
            MaxConsole.println("Re-escaped: " + escape(unescape(str)));
            MaxConsole.println("---------------------------");
        }
    }
}
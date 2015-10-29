/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-19 13:06:37 $ $Header:
 * /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS
 * /core/webapps/gvconsole/src/main/java/max/util/PagedOutput.java,v 1.1
 * 2010-04-03 15:28:54 nlariviera Exp $ $Id: PagedOutput.java,v 1.1 2010-04-03
 * 15:28:54 nlariviera Exp $ $Name:  $ $Locker:  $ $Revision: 1.2 $ $State: Exp $
 */
package max.util;

import java.util.*;


public class PagedOutput
{
    private static int   count;
    public static int    lines         = 25;
    public static String defaultPrompt = "Command to continue or h+enter for help...";
    public static String prompt        = defaultPrompt;

    public static String input(String prompt)
    {
        System.out.print(prompt);
        try {
            byte buffer[] = new byte[2048];
            int l = System.in.read(buffer);
            return new String(buffer, 0, l);

        }
        catch (Exception exc) {
            exc.printStackTrace();
            return null;
        }
    }

    public static void println(String str)
    {
        StringTokenizer tk = new StringTokenizer(str, "\n", true);
        boolean prevEnter = true;
        while (tk.hasMoreTokens()) {
            String t = tk.nextToken();
            if (t.equals("\n")) {
                if (prevEnter)
                    printLine("");
                prevEnter = true;
            }
            else {
                if (t.endsWith("\r"))
                    t = t.substring(0, t.length() - 1);
                printLine(t);
                prevEnter = false;
            }
        }
    }

    private static void printLine(String str)
    {
        System.out.println(str);
        ++count;
        if (count >= lines) {
            count = 0;
            boolean loop = true;
            while (loop) {
                String in = input(prompt).trim();
                if (in.equals(""))
                    loop = false;
                else if (in.equals("h")) {
                    System.out.println("Commands:");
                    System.out.println("    h   : print this help");
                    System.out.println("    ##  : set the window height");
                    System.out.println("    p ##: set the prompt");
                    System.out.println("    dp  : set default prompt");
                    System.out.println("    l   : set 1 for window height and '' for prompt");
                }
                else if (in.startsWith("p")) {
                    if (in.length() < 3)
                        in = "p  ";
                    prompt = in.substring(2);
                }
                else if (in.equals("dp")) {
                    prompt = defaultPrompt;
                }
                else if (in.equals("l")) {
                    prompt = "";
                    lines = 1;
                }
                else {
                    try {
                        lines = Integer.parseInt(in.trim());
                    }
                    catch (Exception exc) {
                        System.out.println("" + exc);
                    }
                }
            }
        }
    }
}

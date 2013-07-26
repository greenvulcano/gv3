/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-14 15:28:17 $
 */
package max.config;

class Substitution
{
    int    start;
    int    end;
    String sect;
    String ide;
    String defVal;

    /**
     * Costruisce un oggetto Substitution.
     *
     * <pre>
     *          st        dp    ed
     *          |         |     |
     *          V         V     V
     * xxxxxxxxx[[sss!iiii::dddd]]xxxxxxx
     *          ^                 ^
     *          |                 |
     *        start              end
     *
     * section = sss
     * ide = iiii
     * defVal = dddd (o null se dddd non esiste)
     * </pre>
     */
    Substitution(String defSect, int st, int dp, int ed, StringBuilder val)
    {
        start = st;
        end = ed + 2;

        if (dp == -1) {
            ide = val.substring(st + 2, ed);
            defVal = null;
        }
        else {
            ide = val.substring(st + 2, dp);
            defVal = val.substring(dp + 2, ed);
        }

        int idx = ide.indexOf('!');
        if (idx == -1) {
            sect = defSect;
        }
        else {
            sect = ide.substring(0, idx);
            ide = ide.substring(idx + 1);
        }
    }
}

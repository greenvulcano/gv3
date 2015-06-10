/*
 * Creation date and time: 12-giu-2006 15.13.44
 *
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/util/StringTemplate.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 */
package max.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Sergio
 *
 * <code>$Id: StringTemplate.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $</code>
 */
public class StringTemplate
{
    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    /**
     * I frammenti di indice pari sono costanti, quelli di indice dispari sono placeholders.
     */
    private String[] fragments;

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    //----------------------------------------------------------------------------------------------

    public StringTemplate(String template)
    {
        List fragmentList = new LinkedList();

        StringBuffer constantBuffer = new StringBuffer();
        int startIdx = 0;
        while (true) {
            int idx = template.indexOf("${", startIdx);
            if (idx == -1) {
                fragmentList.add(template.substring(startIdx));
                fragments = new String[fragmentList.size()];
                fragmentList.toArray(fragments);
                return;
            }

            int endIdx = template.indexOf("}", idx + 2);
            if (endIdx == -1) {
                fragmentList.add(template.substring(startIdx));
                fragments = new String[fragmentList.size()];
                fragmentList.toArray(fragments);
                return;
            }

            String constant = template.substring(startIdx, idx);
            constantBuffer.append(constant);
            String placeholder = template.substring(idx + 2, endIdx).trim();

            if (!placeholder.equals("$")) {
                fragmentList.add(constantBuffer.toString());
                fragmentList.add(placeholder);
                constantBuffer = new StringBuffer();
            }
            else {
                constantBuffer.append("$");
            }

            startIdx = endIdx + 1;
        }
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    public String substitute(Map values)
    {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < fragments.length; ++i) {
            String str = fragments[i];
            if (i % 2 == 1) {
                buffer.append(values.get(str));
            }
            else {
                buffer.append(str);
            }
        }

        return buffer.toString();
    }

    public String substitute(String[][] values)
    {
        Map map = new HashMap();

        if (values == null) {
            return substitute(map);
        }

        for (int i = 0; i < values.length; i++) {
            String k = values[i][0];
            String v = values[i][1];
            map.put(k, v);
        }

        return substitute(map);
    }

    public String substitute(Object[][] values)
    {
        Map map = new HashMap();

        if (values == null) {
            return substitute(map);
        }

        for (int i = 0; i < values.length; i++) {
            Object k = values[i][0];
            Object v = values[i][1];
            map.put(k, v);
        }

        return substitute(map);
    }

    //----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    //----------------------------------------------------------------------------------------------
}

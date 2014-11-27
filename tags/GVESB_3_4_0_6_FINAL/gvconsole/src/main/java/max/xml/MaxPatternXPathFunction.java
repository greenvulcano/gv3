/*
 * Creation date and time: 21-giu-2006 13.42.13
 */
package max.xml;

import it.greenvulcano.configuration.XMLConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import max.xpath.XPathFunction;

import org.w3c.dom.Node;

/**
 * This function takes 2 arguments: the first argument is list of string, the
 * second is a list of pattern (regular expression). The function return true if
 * all the strings match at least one pattern, false otherwise. If the list of
 * pattern is empty, true is returned.
 *
 */
public class MaxPatternXPathFunction implements XPathFunction
{
    public MaxPatternXPathFunction()
    {
    }

    public Object evaluate(Node contextNode, Object[] params)
    {
        List strings = toList(params[0]);
        List patterns = toPatterns(toList(params[1]));
        if (patterns.isEmpty()) {
            return Boolean.TRUE;
        }

        Iterator it = strings.iterator();
        while (it.hasNext()) {
            String str = toString(it.next());
            if (!matches(str, patterns)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     *
     * @param str
     * @param patterns
     * @return true if the given string matches at least one pattern
     */
    public boolean matches(String str, List patterns)
    {
        Iterator it = patterns.iterator();
        while (it.hasNext()) {
            Pattern pattern = (Pattern) it.next();
            if (pattern.matcher(str).matches()) {
                return true;
            }
        }
        return false;
    }

    public Object getValue(Object obj)
    {
        if (obj instanceof Node) {
            return XMLConfig.getNodeValue((Node) obj);
        }
        return obj;
    }

    public List toList(Object obj)
    {
        if (obj == null) {
            return Collections.EMPTY_LIST;
        }
        if (obj instanceof List) {
            return (List) obj;
        }
        if (obj instanceof Collection) {
            Collection collection = (Collection) obj;
            return new ArrayList(collection);
        }
        return Collections.singletonList(obj);
    }

    public List toPatterns(List list)
    {
        ArrayList patterns = new ArrayList(list.size());
        Iterator it = list.iterator();
        while (it.hasNext()) {
            String str = toString(it.next());
            if (str != null) {
                patterns.add(Pattern.compile(str));
            }
        }
        return patterns;
    }

    public String toString(Object obj)
    {
        obj = getValue(obj);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }
}

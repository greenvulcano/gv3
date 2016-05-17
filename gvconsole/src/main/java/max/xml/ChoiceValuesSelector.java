/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:49 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/ChoiceValuesSelector.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Id: ChoiceValuesSelector.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.w3c.dom.Node;

/**
 * A value selector return a list of possible values for an attribute or for an
 * element.
 * <p>
 * A values selector is specified by lines with following formats:<p>
 *
 * <code>#References: {{ancestors}} xpath</code><br>
 * Select a set of values using an xpath relative to the current element.
 * <p>
 * <code>#Choice: {{ancestors}} choice</code><br>
 * Set of constant values. <code>choice</code> is a pipe-separated list that
 * specifies possible values.
 * <p>
 * <code>#Config: {{ancestors}} file : xpath</code><br>
 * Select a set of values extracting them from configuration files.
 * <p>
 * <code>{{ancestors}}</code> is a slash-separated list of ancestors of the node.<br>
 * This specify a condition that must be verified. The condition is verified is
 * the node has the specified ancestors. A values selector applies to a node if
 * the condition is verified.<br>
 * {{ancestors}} is optional and if it is not specified then the values selector
 * applies to any node.
 * <p>
 * For example:
 * <pre>
 *  #References: {{servlet-mapping}} //servlet/servlet-name
 * </pre>
 * specify a reference that apply only if the node has servelt-mapping as parent,
 * and valid values for the node are the values specified by servlet names.
 *
 */
public class ChoiceValuesSelector extends ValuesSelector
{
    private String choice[] = null;

    public ChoiceValuesSelector(Feature feature)
    {
        super(feature);
        String parameter = feature.getParameter();
        StringTokenizer tokens = new StringTokenizer(parameter, "|", false);
        choice = new String[tokens.countTokens()];
        for(int i = 0; i < choice.length; ++i) {
            choice[i] = tokens.nextToken().trim();
        }
    }

    public List getValues(Node node, String currentValue)
    {
        List list = new LinkedList();
        list.add(currentValue);

        for(int i = 0; i < choice.length; ++i) {
            if(!choice[i].equals(currentValue)) {
                list.add(choice[i]);
            }
        }

        return list;
    }

    /* (non-Javadoc)
     * @see max.xml.ValuesSelector#fillXPaths(java.util.Set)
     */
    public void fillXPaths(Set xpaths)
    {
        // do nothing
    }
}
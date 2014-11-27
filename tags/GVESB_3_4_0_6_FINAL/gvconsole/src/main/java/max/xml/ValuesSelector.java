/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:49 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/ValuesSelector.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Id: ValuesSelector.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import max.xpath.XPath;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A value selector return a list of possible values for an attribute or for an
 * element.
 * A value selector is a feature.
 */
public abstract class ValuesSelector
{
    protected static final List nullResult = new LinkedList();

    protected Feature feature;

    protected ValuesSelector(Feature feature)
    {
        this.feature = feature;
    }

    public boolean appliesTo(Element element)
    {
        return feature.appliesTo(element);
    }

    public Set getXPaths()
    {
        Set set = new HashSet();
        XPath xpath = feature.getContext();
        if(xpath != null) {
            set.add(xpath.getXPathString());
        }
        fillXPaths(set);
        return set;
    }

    public abstract List getValues(Node node, String currentValue);

    public abstract void fillXPaths(Set xpaths);
}
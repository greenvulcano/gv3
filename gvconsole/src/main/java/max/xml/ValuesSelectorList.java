/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:50 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/ValuesSelectorList.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Id: ValuesSelectorList.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.*;
import org.w3c.dom.*;

/**
 * List of ValuesSelector obtained from a Map(String, Set(Feature)).
 * <p>
 * Interesting features are:
 * <li>#References:
 * <li>#Config:
 * <li>#Document:
 * <li>#Choice:
 * <li>#CompositeRef: ...
 */
public class ValuesSelectorList
{
    private List valuesSelectorList = null;

    public ValuesSelectorList(Map features)
    {
        valuesSelectorList = new LinkedList();

        Set referencesSet = (Set)features.get("References");
        if(referencesSet != null) {
            Iterator i = referencesSet.iterator();
            while(i.hasNext()) {
                Feature feature = (Feature)i.next();
                ReferencesValuesSelector vs = new ReferencesValuesSelector(feature);
                valuesSelectorList.add(vs);
            }
        }

        Set configSet = (Set)features.get("Config");
        if(configSet != null) {
            Iterator i = configSet.iterator();
            while(i.hasNext()) {
                Feature feature = (Feature)i.next();
                ConfigValuesSelector vs = new ConfigValuesSelector(feature);
                valuesSelectorList.add(vs);
            }
        }

        Set documentSet = (Set)features.get("Document");
        if(documentSet != null) {
            Iterator i = documentSet.iterator();
            while(i.hasNext()) {
                Feature feature = (Feature)i.next();
                DocumentValuesSelector vs = new DocumentValuesSelector(feature);
                valuesSelectorList.add(vs);
            }
        }

        Set compositeRefSet = (Set)features.get("CompositeRef");
        if(compositeRefSet != null) {
            Iterator i = compositeRefSet.iterator();
            while(i.hasNext()) {
                Feature feature = (Feature)i.next();
                CompositeReferenceValuesSelector vs = new CompositeReferenceValuesSelector(feature);
                valuesSelectorList.add(vs);
            }
        }

        Set choiceSet = (Set)features.get("Choice");
        if(choiceSet != null) {
            Iterator i = choiceSet.iterator();
            while(i.hasNext()) {
                Feature feature = (Feature)i.next();
                ChoiceValuesSelector vs = new ChoiceValuesSelector(feature);
                valuesSelectorList.add(vs);
            }
        }

        if(valuesSelectorList.size() == 0) {
            valuesSelectorList = null;
        }
    }

    public Collection getValues(Element element, String currentValue)
    {
        if(valuesSelectorList == null) return null;

        Set set = null;

        Iterator i = valuesSelectorList.iterator();
        while(i.hasNext()) {
            ValuesSelector vs = (ValuesSelector)i.next();
            if(vs.appliesTo(element)) {
                if(set == null) set = new TreeSet();
                set.addAll(vs.getValues(element, currentValue));
            }
        }

        return set;
    }

    public String[] getXPaths()
    {
        if(valuesSelectorList == null) return null;

        Set xpaths = new HashSet();

        Iterator i = valuesSelectorList.iterator();
        while(i.hasNext()) {
            ValuesSelector vs = (ValuesSelector)i.next();
            xpaths.addAll(vs.getXPaths());
        }

        String[] ret = new String[xpaths.size()];
        xpaths.toArray(ret);
        return ret;
    }

    public boolean isEmpty()
    {
        if(valuesSelectorList == null) return true;
        if(valuesSelectorList.size() == 0) return true;
        return false;
    }
}
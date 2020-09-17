/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:51 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/CheckDependencies.java,v 1.1 2010-04-03 15:28:51 nlariviera Exp $
 * $Id: CheckDependencies.java,v 1.1 2010-04-03 15:28:51 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import max.xpath.XPath;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2004 - All right reserved
 */
public class CheckDependencies
{
    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    /**
     * Map[String elementName, Set[String] elementNames]
     */
    private Map dependencies = new HashMap();

    /**
     * Elementi che dipendono da tutti gli altri elementi
     * <br>
     * Set[String elementName]
     */
    private Set fullDependencies = new HashSet();

    /**
     * Map[@attr, Map[String value, Set[String] elementNames]]
     */
    private Map attributeDependencies = new HashMap();

    /**
     * Utilizzato per parsificare l'XPath
     */
    private Pattern pattern;

    private Pattern attributePattern;

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    //----------------------------------------------------------------------------------------------

    public CheckDependencies()
    {
        // L'espressione regolare seleziona token di questo tipo:
        //
        // - quelli che terminano per '(', ', '"' (funzioni e stringhe)
        // - quelli che iniziano per '@' (attributi)
        // - a quelli che contengono '::' (deve essere rimossa la parte iniziale)
        // - ..
        // - .
        // - *
        // - *[@attr='val' ... ]
        // - *[@attr="val" ... ]
        // - tutti gli altri
        //
        pattern = Pattern.compile("\".*?\"|'.*?'|(?i:[@a-z:_][a-z0-9._:-]*)(?:\\s*\\()?|\\.{1,2}|\\*\\s*(?:\\[\\s*@(?i:[a-z:_][a-z0-9._:-]*)\\s*=\\s*(?:\".*?\"|'.*?')\\s*\\])?");

        // Gruppi:
        // 1 = nome attributo
        // 3 = valore attributo
        //
        attributePattern = Pattern.compile("(?i:@([a-z:_][a-z0-9._:-]*))\\s*=\\s*(?:(['\"])(.*?)\\2)");
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    public void addDependency(String elementName, XPath xpath)
    {
        addDependency(elementName, xpath.getXPathString());
    }

    public void addDependency(String elementName, String xpath)
    {
        // E' gi� stabilito che dipende da tutti gli elementi
        //
        if(fullDependencies.contains(elementName)) {
            return;
        }

        Set names = extractElementNames(elementName, xpath);

        // Questo elemento dipende da qualsiasi altro elemento?
        //
        if(names.contains("*")) {
            fullDependencies.add(elementName);
            return;
        }

        Iterator it = names.iterator();
        while(it.hasNext()) {
            String dependFrom = (String)it.next();

            if(dependFrom.startsWith("*")) {
                // E' del tipo *[@xxx=yyy ...]
                //
                processAttributeTypeDependencies(elementName, dependFrom);
            }
            else if(!dependFrom.equals("..")) {
                Set deps = (Set)dependencies.get(dependFrom);
                if(deps == null) {
                    deps = new HashSet();
                    dependencies.put(dependFrom, deps);
                }
                deps.add(elementName);
            }
            else {
                // TODO Come si fa per ..?
            }
        }
    }

    public Set elementsToCheck(Element modifiedElement)
    {
        String modifiedElementName = modifiedElement.getNodeName();
        Set ret = new HashSet();

        // Aggiunge tutti quelli 'full'
        //
        ret.addAll(fullDependencies);

        // Aggiunge quelli direttamente dipendenti
        //
        Set dependents = (Set)dependencies.get(modifiedElementName);
        if(dependents != null) {
            ret.addAll(dependents);
        }

        // Aggiunge quelli selezionati con gli attributi
        //
        Iterator it = attributeDependencies.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String attributeName = (String)entry.getKey();
            Attr attribute = modifiedElement.getAttributeNode(attributeName);
            if(attribute != null) {
                String value = attribute.getNodeValue();
                Map deps = (Map)entry.getValue();
                dependents = (Set)deps.get(value);
                if(dependents != null) {
                    ret.addAll(dependents);
                }
            }
        }

        return ret;
    }

    public String toString()
    {
        return "CheckDependencies{deps=" + dependencies + ", attributes=" + attributeDependencies + ", full=" + fullDependencies + "}";
    }

    //----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    //----------------------------------------------------------------------------------------------

    /**
     * Estrae i nomi degli elementi nel XPath dato.
     * Scarta:
     * <ul>
     *  <li>Funzioni (tokens che terminano per '(')
     *  <li>Stringhe (tokens che terminano per '"' e apice)
     *  <li>Attributi (tokens che iniziano per '@')
     *  <li>.
     *  <li>Rimuove gli assi (prefissi con ::)
     * </ul>
     * <br>
     * TODO Come si fa con '..'?
     *
     * @return Set[String]
     */
    private Set extractElementNames(String elementName, String xpath)
    {
        Set result = new HashSet();

        Matcher matcher = pattern.matcher(xpath);
        while(matcher.find()) {
            String token = matcher.group();
            if(token.endsWith("(") || token.endsWith("\"") || token.endsWith("'") || token.startsWith("@") || token.equals(".")) {
                if(token.equals("current(")) {
                    result.add(elementName);
                }
            }
            else {
                int idx = 0;
                while((idx = token.indexOf("::")) != -1) {
                    token = token.substring(idx + 2);
                }
                result.add(token);
            }
        }

        return result;
    }

    /**
     *
     * @param elementName
     * @param dependFrom nella forma *[@xxx='yyy' ...]
     */
    private void processAttributeTypeDependencies(String elementName, String dependFrom)
    {
        Matcher matcher = attributePattern.matcher(dependFrom);
        while(matcher.find()) {
            String attribute = matcher.group(1);
            String value = matcher.group(3);
            Map valuesMap = (Map)attributeDependencies.get(attribute);
            if(valuesMap == null) {
                valuesMap = new HashMap();
                attributeDependencies.put(attribute, valuesMap);
            }
            Set deps = (Set)valuesMap.get(value);
            if(deps == null) {
                deps = new HashSet();
                valuesMap.put(value, deps);
            }
            deps.add(elementName);
        }
    }
}
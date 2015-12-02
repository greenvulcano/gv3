/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:51 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/CompositeReferenceValuesSelector.java,v 1.1 2010-04-03 15:28:51 nlariviera Exp $
 * $Id: CompositeReferenceValuesSelector.java,v 1.1 2010-04-03 15:28:51 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;

import max.xpath.XPath;
import max.xpath.XPathAPI;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Allows references to other elements or attributes using a complex value.
 * <p/>
 * The complex value is built using many xpaths.
 * <p/>
 * A CompositeReferenceValuesSelector is built from a Feature with the following parameter syntax:
 * <pre>sep nodesXpath [sep [str1] sep xpath1 sep str2 sep xpath2 sep...]</pre>
 * where:
 * <ul>
 *  <li>sep is just a separator and will be discarded. Is used in order to separates other tokens
 *      in the parameter. Can be any character that is not present in the other tokens.
 *  <li>nodesXpath is a XPath selecting referenced nodes
 *  <li>strX is a string put as is in the result
 *  <li>xpathX is a XPath relative to the selected nodes that specifies values to be put in the
 *      resulting value.
 * </ul>
 *
 */
public class CompositeReferenceValuesSelector extends ValuesSelector
{
    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    /**
     * XPath per la selezione dei nodi.
     */
    private XPath nodesXPath = null;

    private String[] tokens = null;
    private XPath[] xpaths = null;

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTOR
    //----------------------------------------------------------------------------------------------

    /**
     */
    public CompositeReferenceValuesSelector(Feature feature)
    {
        super(feature);
        String parameter = feature.getParameter().trim();

        try {
            String sep = "" + parameter.charAt(0);
            parameter = parameter.substring(1);
            List<String> tList = new ArrayList<String>();
            List<XPath> xList = new ArrayList<XPath>();

            StringTokenizer tokenizer = new StringTokenizer(parameter, sep, true);
            nodesXPath = new XPath(tokenizer.nextToken().trim());

            boolean isText = false;
            boolean isBlank = false;
            while(tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if(token.equals(sep)) {

                    // Se il precedente era vuoto
                    //
                    if(isBlank) {
                        if(isText) {
                            tList.add("");
                        }
                        else {
                            xList.add(null);
                        }
                    }

                    // Ogni volta che trova il separatore inverte il tipo
                    isText = !isText;
                    isBlank = true;
                }
                else if(isText) {
                    tList.add(token);
                    isBlank = false;
                }
                else {
                    xList.add(new XPath(token.trim()));
                    isBlank = false;
                }
            }

            if(tList.size() == 0) {
                tList.add("");
                xList.add(new XPath("."));
            }

            tokens = new String[tList.size()];
            tList.toArray(tokens);

            xpaths = new XPath[xList.size()];
            xList.toArray(xpaths);
        }
        catch(Exception exc) {
            exc.printStackTrace();
            nodesXPath = null;
        }
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    public List getValues(Node node, String currentValue)
    {
        List list = new LinkedList();
        list.add(currentValue);

        if(nodesXPath == null) {
            return list;
        }

        try {
            XPathAPI xpathAPI = feature.getXPathAPI();
            NodeList nodeList = xpathAPI.selectNodeList(node, nodesXPath);
            int N = nodeList.getLength();
            for(int i = 0; i < N; ++i) {
                Node n = nodeList.item(i);
                StringBuffer sb = new StringBuffer();

                for(int j = 0; j < tokens.length; ++j) {
                    sb.append(tokens[j]);
                    if((j < xpaths.length) && (xpaths[j] != null)) {
                        Node valNode = xpathAPI.selectSingleNode(n, xpaths[j]);
                        if(valNode != null) {
                            sb.append(XPathAPI.getNodeValue(valNode));
                        }
                    }
                }

                list.add(sb.toString().trim());
            }
        }
        catch (TransformerException e) {
            e.printStackTrace();
        }

        return list;
    }

    /* (non-Javadoc)
     * @see max.xml.ValuesSelector#fillXPaths(java.util.Set)
     */
    public void fillXPaths(Set toFill)
    {
        if(nodesXPath == null) {
            return;
        }
        toFill.add(nodesXPath.getXPathString());

        if(xpaths != null) {
            for(int i = 0; i < xpaths.length; ++i) {
                if(xpaths[i] != null) {
                    toFill.add(xpaths[i].getXPathString());
                }
            }
        }
    }
}

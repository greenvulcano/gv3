/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:50 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/CompositeLabel.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Id: CompositeLabel.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;

import max.xpath.XPath;
import max.xpath.XPathAPI;

import org.w3c.dom.Node;

/**
 * Allows composite label.
 * <p/>
 * The composite label is built using many xpaths.
 * <p/>
 * A composite label is built from a Feature with the following parameter syntax:
 * <pre>sep [str1] sep xpath1 sep str2 sep xpath2 sep...</pre>
 * where:
 * <ul>
 *  <li>sep is just a separator and will be discarded. Is used in order to separates other tokens
 *      in the parameter. Can be any character that is not present in the other tokens.
 *  <li>strX is a string put as is in the result
 *  <li>xpathX is a XPath relative to the selected nodes that specifies values to be put in the
 *      resulting value.
 * </ul>
 *
 */
public class CompositeLabel
{
    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    private String[] tokens = null;
    private XPath[] xpaths = null;
    private XPathAPI xpathAPI = null;

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTOR
    //----------------------------------------------------------------------------------------------

    /**
     */
    public CompositeLabel(Feature feature, XPathAPI xpathAPI)
    {
        this.xpathAPI = xpathAPI;
        String parameter = feature.getParameter().trim();

        try {
            String sep = "" + parameter.charAt(0);
            List<String> tList = new ArrayList<String>();
            List<XPath> xList = new ArrayList<XPath>();

            StringTokenizer tokenizer = new StringTokenizer(parameter, sep, true);

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
        }
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    public String getLabel(Node node)
    {
        if(tokens == null) {
            return null;
        }

        try {
            StringBuffer sb = new StringBuffer();

            for(int j = 0; j < tokens.length; ++j) {
                sb.append(tokens[j]);
                if((j < xpaths.length) && (xpaths[j] != null)) {
                    Node valNode = xpathAPI.selectSingleNode(node, xpaths[j]);
                    if(valNode != null) {
                        sb.append(XPathAPI.getNodeValue(valNode));
                    }
                }
            }

            return sb.toString().trim();
        }
        catch (TransformerException e) {
            e.printStackTrace();
            return null;
        }
    }
}

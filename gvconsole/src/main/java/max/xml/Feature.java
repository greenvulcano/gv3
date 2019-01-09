/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:49 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/Feature.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Id: Feature.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;

import max.xpath.XPath;
import max.xpath.XPathAPI;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * This class defines a special feature that applies to an element of the DTD.<br>
 * A feature applies to an element only if the element is in a given context
 * and the element name match.
 * The context is a xpath absolute or relative to the element.
 * The feature applies to the element only if the element is a node returned
 * from the xpath.
 * <p>
 * If the element name is null then the check for element name is not performed.<br>
 * If the context is null then the check for context is not performed.
 * <p>
 * Because features are non-standard extensions to DTDs, then they must be
 * defined in the comments. This explains why the features are declared into
 * the comments.
 * <p>
 * A feature have one of the following form:
 * <pre>
 *  #feature-name
 *  #feature-name: {{context-xpath}}
 *  #feature-name: feature-parameter
 *  #feature-name: {{context-xpath}} feature-parameter
 * </pre>
 * The feature-parameter is a string and its menaing depends on the specific
 * feature.
 * <p>
 * <b>Examples:</b>
 * <p>
 * <table>
 *  <tr valign="top">
 *   <td><b>Context</b></td><td width="15"></td><td><b>Description</b></td>
 *  </tr>
 *  <tr valign="top">
 *   <td colspan="3"><hr></td>
 *  </tr>
 *  <tr valign="top">
 *   <td><i>empty</i></td><td></td><td>Applies to all element</td>
 *  </tr>
 *  <tr valign="top">
 *   <td>{{/web-app/servlet-mapping/servlet-name}}</td>
 *   <td></td>
 *   <td>
 *      Applies to servlet-name elements that are children of servlet-mapping
 *      elements that are children of web-app root element
 *   </td>
 *  </tr>
 *  <tr valign="top">
 *   <td>{{//servlet-mapping/servlet-name}}</td>
 *   <td></td>
 *   <td>Applies to servlet-name elements that are children of servlet-mapping elements anywhere in the document</td>
 *  </tr>
 *  <tr valign="top">
 *   <td>{{../connection[@active='yes']}}</td>
 *   <td></td>
 *   <td>Applies to connection elements that have active attribute equals to 'yes'</td>
 *  </tr>
 * </table>
 * <pre>
 *  #NotNull
 *  #NotNull: {{/servlet-mapping/servlet-name}}
 *  #References: {{//servlet-mapping/servlet-name}} //servlet/servlet-name
 * </pre>
 */
public class Feature
{
    //--------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------

    private String name;
    private XPath context;
    private String parameter;
    private String elementName;
    private XPathAPI xpathAPI;
    private Object featureObject;

    //--------------------------------------------------------------------------
    // CONSTRUCTORS
    //--------------------------------------------------------------------------

    public Feature(String elementName, String name, String context, String parameter)
    {
        this.elementName = elementName;
        this.name = name;
        if(context != null) {
            try {
                this.context = new XPath(context);
            }
            catch(TransformerException exc) {
                exc.printStackTrace();
                this.context = null;
            }
        }
        else {
            this.context = null;
        }
        this.parameter = parameter;
        featureObject = null;
    }

    //--------------------------------------------------------------------------
    // FEATURE BUILDING AND INITIALIZING
    //--------------------------------------------------------------------------

    public static void setXPathAPI(Map features, XPathAPI xpathAPI)
    {
        Iterator i = features.values().iterator();
        while(i.hasNext()) {
            Iterator j = ((Set)i.next()).iterator();
            while(j.hasNext()) {
                Feature feature = (Feature)j.next();
                feature.xpathAPI = xpathAPI;
            }
        }
    }


    /**
     * @return a Feature from a definition string.
     *
     * @see max.xml.Feature
     */
    public static Feature getFeature(String elementName, String definition)
    {
        Feature feature = null;

        if(definition.startsWith("#")) {
            int idx = definition.indexOf(':');
            if(idx != -1) {

                // there are other parameters
                //
                String name = definition.substring(1, idx);
                definition = definition.substring(idx + 1).trim();

                if(definition.startsWith("{{")) {

                    // there is the context
                    //
                    idx = definition.indexOf("}}");
                    if(idx == -1) {

                        // all the rest is the context
                        //
                        String context = definition.substring(2).trim();
                        feature = new Feature(elementName, name, context, null);
                    }
                    else {

                        // extract context and parameter
                        //
                        String context = definition.substring(2, idx).trim();
                        String parameter = definition.substring(idx + 2).trim();
                        feature = new Feature(elementName, name, context, parameter);
                    }
                }
                else {

                    // no context
                    //
                    feature = new Feature(elementName, name, null, definition);
                }
            }
            else {

                // no more parameters
                //
                String name = definition.substring(1);
                feature = new Feature(elementName, name, null, null);
            }
        }

        return feature;
    }

    /**
     * Build all features defined in a comment.
     *
     * @return Map(String, Set(Feature)): the key is the feature name, the set
     *      is the features with the same name.
     */
    public static Map getFeatures(String elementName, String comment)
    {
        Map ret = new LinkedHashMap();

        StringTokenizer tokenizer = new StringTokenizer(comment, "\r\n", false);

        while(tokenizer.hasMoreTokens()) {

            String line = tokenizer.nextToken();
            line = line.trim();
            Feature feature = getFeature(elementName, line);

            if(feature != null) {
                String name = feature.getName();
                Set set = (Set)ret.get(name);
                if(set == null) {
                    set = new LinkedHashSet();
                    ret.put(name, set);
                }
                set.add(feature);
            }
        }

        return ret;
    }

    //--------------------------------------------------------------------------
    // GETTERS
    //--------------------------------------------------------------------------

    public XPathAPI getXPathAPI()
    {
        return xpathAPI;
    }

    public String getName()
    {
        return name;
    }

    public XPath getContext()
    {
        return context;
    }

    public String getParameter()
    {
        return parameter;
    }

    public String getElementName()
    {
        return elementName;
    }

    public void setFeatureObject(Object object)
    {
        this.featureObject = object;
    }

    public Object getFeatureObject()
    {
        return featureObject;
    }

    //--------------------------------------------------------------------------
    // METHODS
    //--------------------------------------------------------------------------

    /**
     * Check if a feature applies to an element.
     */
    public boolean appliesTo(Element element)
    {
        // Check for element name only if the elementName is defined
        //
        if(elementName != null) {
            String currentElementName = element.getNodeName();
            if(!elementName.equals(currentElementName)) {
                return false;
            }
        }

        if(context != null) {

            try {
                // Check the context only if if is not null
                //
                NodeList nodeList = xpathAPI.selectNodeList(element, context);
                int N = nodeList.getLength();
                for(int i = 0; i < N; ++i) {
                    if(nodeList.item(i) == element) return true;
                }
                return false;
            }
            catch(TransformerException exc) {
                exc.printStackTrace();
                return false;
            }
        }
        else {

            // Applies if the context is null
            //
            return true;
        }
    }

    public String toString()
    {
        return "Feature[" + elementName + ", " + name + ", " + context + ", " + parameter + "]";
    }

    //--------------------------------------------------------------------------
    // COMMAND LINE
    //--------------------------------------------------------------------------

    public static void main(String args[])
    {
        String definitions = "";
        for(int i = 1; i < args.length; ++i) {
            definitions += args[i] + "\r\n";
        }

        Map features = Feature.getFeatures(args[0], definitions);

        System.out.println(features);
    }
}

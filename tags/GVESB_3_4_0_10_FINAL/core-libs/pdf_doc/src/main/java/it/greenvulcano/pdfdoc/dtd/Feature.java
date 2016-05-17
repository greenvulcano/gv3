/*
 * Copyright (c) 2004 Maxime Informatica s.n.c. - All right reserved
 *
 */
package it.greenvulcano.pdfdoc.dtd;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


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
 *   <td>{{/web-app/servlet-mapping/servlet-name}}</td><td></td><td>Applies to servlet-name elements that are children of servlet-mapping elements that are children of web-app root element</td>
 *  </tr>
 *  <tr valign="top">
 *   <td>{{//servlet-mapping/servlet-name}}</td><td></td><td>Applies to servlet-name elements that are children of servlet-mapping elements anywhere in the document</td>
 *  </tr>
 *  <tr valign="top">
 *   <td>{{../connection[@active='yes']}}</td><td></td><td>Applies to connection elements that have active attribute equals to 'yes'</td>
 *  </tr>
 * </table>
 * <pre>
 *  #NotNull
 *  #NotNull: {{/servlet-mapping/servlet-name}}
 *  #References: {{//servlet-mapping/servlet-name}} //servlet/servlet-name
 * </pre>
 *
 * @author Maxime Informatica s.n.c. - Copyright (c) 2003 - All right reserved
 */
public class Feature
{
    //--------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------

    private String name;
    private String context;
    private String parameter;

    //--------------------------------------------------------------------------
    // CONSTRUCTORS
    //--------------------------------------------------------------------------

    public Feature(String name, String context, String parameter)
    {
        this.name = name;
        this.context = context;
        this.parameter = parameter;
    }

    //--------------------------------------------------------------------------
    // FEATURE BUILDING
    //--------------------------------------------------------------------------

    /**
     * @return a Feature from a definition string.
     *
     * @see max.xml.Feature
     */
    public static Feature getFeature(String definition)
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
                        feature = new Feature(name, context, null);
                    }
                    else {

                        // extract context and parameter
                        //
                        String context = definition.substring(2, idx).trim();
                        String parameter = definition.substring(idx + 2).trim();
                        feature = new Feature(name, context, parameter);
                    }
                }
                else {

                    // no context
                    //
                    feature = new Feature(name, null, definition);
                }
            }
            else {

                // no more parameters
                //
                String name = definition.substring(1);
                feature = new Feature(name, null, null);
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
    public static Map getFeatures(String comment)
    {
        Map ret = new LinkedHashMap();

        StringTokenizer tokenizer = new StringTokenizer(comment, "\r\n", false);

        while(tokenizer.hasMoreTokens()) {

            String line = tokenizer.nextToken();
            line = line.trim();
            Feature feature = getFeature(line);

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

    public String getName()
    {
        return name;
    }

    public String getContext()
    {
        return context;
    }

    public String getParameter()
    {
        return parameter;
    }

    //--------------------------------------------------------------------------
    // METHODS
    //--------------------------------------------------------------------------

    public String toString()
    {
        return "Feature[" + name + ", " + context + ", " + parameter + "]";
    }
}

/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-12 15:11:11 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/ElementModel.java,v 1.2 2010-04-12 15:11:11 nlariviera Exp $
 * $Id: ElementModel.java,v 1.2 2010-04-12 15:11:11 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.2 $
 * $State: Exp $
 */
package max.xml;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import max.core.MaxException;
import max.xpath.XPath;
import max.xpath.XPathAPI;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Defines element behaviour.
 *
 * Special features.
 * <ul>
 *     <li>support for
 *         <pre>
 *         #References: ...
 *         #Choice: ...
 *         #Config: ...
 *         #Document: ...
 *         #CompositeRef: ...
 *         #NotNull: ...
 *         #Label: ...
 *         #CompositeLabel: ...
 *         #Warn: ...
 *         #Unique: ...
 *         #ExternalData: ...
 *         #SelectOnInsert: ...
 *         #Table: ...
 *         #Pattern: ...
 *         #Graph: ...
 *         #Hidden: ...
 *         </pre>
 *     <li>if the associated comment contains a row like
 *         <pre>
 *         #Template:
 *              ...
 *              xsl templates
 *              ...
 *         </pre>
 *         then these definition will be part of a detail stylesheet.
 * </ul>
 *
 * @see max.xml.ValuesSelector
 * @see max.xml.ValuesSelectorList
 */
public class ElementModel
{
    public static final String TEMPLATE_STR = "#Template:";

    /**
     * Map(String, AttributeModels)
     */
    private Hashtable attributeModels = new Hashtable();

    /**
     * Vector(String)
     */
    private Vector attributeModelsVector = new Vector();

    /**
     * Map(String, Set(AttributeModels))
     */
    private Hashtable attributesWithFeature = new Hashtable();

    private String name;
    private String comment;
    private ContentModel contentModel;

    private String template = null;
    private ValuesSelectorList valuesSelectorList = null;
    private Map features = null;

    private XPathAPI xpathAPI;


    public ElementModel(String nm, ContentModel cm)
    {
        name = nm;
        contentModel = cm;
    }

    public void setXPathAPI(XPathAPI xpathAPI)
    {
        this.xpathAPI = xpathAPI;

        Iterator i = attributeModels.values().iterator();
        while(i.hasNext()) {
            AttributeModel attributeModel = (AttributeModel)i.next();
            attributeModel.setXPathAPI(xpathAPI);
        }

        Feature.setXPathAPI(features, xpathAPI);
    }


    public String getName()
    {
        return name;
    }


    public String getComment()
    {
        return comment;
    }


    public Collection getValues(Element currentElement)
    {
        if(valuesSelectorList == null) return null;
        return valuesSelectorList.getValues(currentElement, XPathAPI.getNodeValue(currentElement));
    }


    public void setComment(String cmnt)
    {
        comment = cmnt;

        // This call modify the comment
        //
        calculateTemplate();

        features = Feature.getFeatures(name, comment);
        valuesSelectorList = new ValuesSelectorList(features);
    }


    private void calculateTemplate()
    {
        // First we assume that no template is defined...

        template = null;
        if(comment == null) return;

        // ... then we try to calculate the template.

        StringBuilder commentBuff = new StringBuilder();
        StringBuilder templateBuff = null;
        StringBuilder currentBuffer = commentBuff;

        StringTokenizer tokenizer = new StringTokenizer(comment, "\n", true);
        while(tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            String cmd = line.trim();
            if(cmd.equals(TEMPLATE_STR)) {
                templateBuff = new StringBuilder();
                currentBuffer = templateBuff;
            }
            else {
                currentBuffer.append(line);
            }
        }

        comment = commentBuff.toString();
        if(templateBuff != null) {
            template = templateBuff.toString();
        }
    }


    public String getTemplate()
    {
        return template;
    }


    public ContentModel getContentModel()
    {
        return contentModel;
    }

    /**
     * Check if the element declares freezed attributes
     */
    public boolean declareFreezedAttributes()
    {
        Collection values = attributeModels.values();
        Iterator i = values.iterator();
        while(i.hasNext()) {
            AttributeModel am = (AttributeModel)i.next();
            if(am.isDeclaredFreezed()) return true;
        }
        return false;
    }

    /**
     * Check if the element declares freezed attributes
     */
    public boolean declareCounterAttributes()
    {
        Collection values = attributeModels.values();
        Iterator i = values.iterator();
        while(i.hasNext()) {
            AttributeModel am = (AttributeModel)i.next();
            if(am.isDeclaredCounter()) return true;
        }
        return false;
    }

    public AttributeModel getAttributeModel(String attributeName)
    {
        return (AttributeModel)attributeModels.get(attributeName);
    }

    public String[] getAttributeNames()
    {
        String attrs[] = new String[attributeModels.size()];
        int i = 0;
        for(Enumeration e = attributeModelsVector.elements(); e.hasMoreElements();) {
            attrs[i++] = (String)e.nextElement();
        }
        return attrs;
    }

    public void addAttributeModel(AttributeModel am)
    {
        attributeModels.put(am.getName(), am);
        attributeModelsVector.addElement(am.getName());
    }

    public Set getAttributesWithFeature(String feature)
    {
        Set awf = (Set) attributesWithFeature.get(feature);
        if (awf == null) {
            awf = new HashSet();
            Iterator i = attributeModels.values().iterator();
            while (i.hasNext()) {
                AttributeModel am = (AttributeModel) i.next();
                if (am.hasFeature(feature)) {
                    awf.add(am);
                }
            }
            attributesWithFeature.put(feature, awf);
        }
        return awf;
    }

    public boolean hasFeature(String feature)
    {
        return hasFeature(feature, false);
    }

    public boolean hasFeature(String feature, boolean checkAttributes)
    {
        if (features.containsKey(feature)) {
            return true;
        }
        if (checkAttributes) {
            Set awf = getAttributesWithFeature(feature);
            return !awf.isEmpty();
        }
        return false;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        if(!comment.trim().equals("")) {
            sb.append("<!--").append(comment).append("-->");
            sb.append(System.getProperty("line.separator"));
        }

        String contentModelStr = "" + contentModel;
        if(!contentModelStr.startsWith("(") && !contentModelStr.equals("EMPTY")) {
            contentModelStr = "(" + contentModelStr + ")";
        }
        sb.append("<!ELEMENT ").append(name).append(" ");
        sb.append(contentModelStr).append(">");
        sb.append(System.getProperty("line.separator"));

        for(Enumeration e = attributeModelsVector.elements(); e.hasMoreElements();) {
            String attname = (String)e.nextElement();
            AttributeModel am = (AttributeModel)attributeModels.get(attname);
            sb.append(am.toString());
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }


    public void addChecks(WarningManager wm)
    {
        // Check from the values selector
        //
        if((valuesSelectorList != null)
          && !(valuesSelectorList.isEmpty())) {
            wm.addCheck(
                new ElementValueCheck(name, valuesSelectorList)
            );
        }

        // Check defined by features
        //
        if(features != null) {

            // Not null check
            //
            Set notNullSet = (Set)features.get("NotNull");
            if(notNullSet != null) {
                Iterator i = notNullSet.iterator();
                while(i.hasNext()) {
                    Feature feature = (Feature)i.next();
                    wm.addCheck(
                        new ElementNotNullCheck(feature, name)
                    );
                }
            }

            // Pattern check
            //
            Set patternSet = (Set)features.get("Pattern");
            if(patternSet != null) {
                Iterator i = patternSet.iterator();
                while(i.hasNext()) {
                    Feature feature = (Feature)i.next();
                    wm.addCheck(
                        new ElementPatternCheck(feature, name)
                    );
                }
            }

            // Warn check
            //
            Set warnSet = (Set)features.get("Warn");
            if(warnSet != null) {
                Iterator i = warnSet.iterator();
                while(i.hasNext()) {
                    Feature feature = (Feature)i.next();
                    wm.addCheck(
                        new ElementWarningCheck(feature, name)
                    );
                }
            }

            // Unique check
            //
            Set uniqueSet = (Set)features.get("Unique");
            if(uniqueSet != null) {
                Iterator i = uniqueSet.iterator();
                while(i.hasNext()) {
                    Feature feature = (Feature)i.next();
                    wm.addCheck(
                        new ElementUniqueCheck(feature, name)
                    );
                }
            }
        }

        // Add attributes checks
        //
        Iterator i = attributeModelsVector.iterator();
        while(i.hasNext()) {
            String attributeName = (String)i.next();
            AttributeModel am = getAttributeModel(attributeName);
            am.addChecks(wm);
        }
    }

    public String getLabel(Element element)
    {
        String label = getSimpleLabel(element);
        if(label == null) {
            return getCompositeLabel(element);
        }
        return label;
    }

    private String getCompositeLabel(Element element)
    {
        Set labelSet = (Set)features.get("CompositeLabel");
        if(labelSet == null) return null;

        Iterator i = labelSet.iterator();
        while(i.hasNext()) {
            Feature feature = (Feature)i.next();
            if(feature.appliesTo(element)) {
                CompositeLabel compositeLabel = (CompositeLabel)feature.getFeatureObject();
                if(compositeLabel == null) {
                    compositeLabel = new CompositeLabel(feature, xpathAPI);
                    feature.setFeatureObject(compositeLabel);
                }
                try {
                    return compositeLabel.getLabel(element);
                }
                catch(Exception exc) {
                    exc.printStackTrace();
                }
            }
        }
        return null;
    }

    private String getSimpleLabel(Element element)
    {
        Set labelSet = (Set)features.get("Label");
        if(labelSet == null) return null;

        Iterator i = labelSet.iterator();
        while(i.hasNext()) {
            Feature feature = (Feature)i.next();
            if(feature.appliesTo(element)) {
                XPath xpath = (XPath)feature.getFeatureObject();
                if(xpath == null) {
                    try {
                        xpath = new XPath(feature.getParameter());
                        feature.setFeatureObject(xpath);
                    }
                    catch(TransformerException exc) {
                        System.out.println("ERROR: Invalid xpath: " + feature.getParameter());
                        continue;
                    }
                }
                try {
                    Node node = xpathAPI.selectSingleNode(element, xpath);
                    if(node != null) {
                        String label = XPathAPI.getNodeValue(node);
                        return label;
                    }
                }
                catch(Exception exc) {
                    exc.printStackTrace();
                }
            }
        }
        return null;
    }

    public ExternalData[] getExternalData(Element element, int when)
    {
        Set edSet = (Set)features.get("ExternalData");
        if(edSet == null) return null;

        Vector ret = new Vector();

        Iterator i = edSet.iterator();
        while(i.hasNext()) {
            Feature feature = (Feature)i.next();
            if(feature.appliesTo(element)) {
                String param = feature.getParameter();
                ExternalData ed = ExternalData.createExternalData(param);
                if((ed != null) && ((ed.getWhen() & when) != 0)) {
                    ret.addElement(ed);
                }
            }
        }

        if(ret.size() == 0) {
            return null;
        }

        ExternalData arr[] = new ExternalData[ret.size()];
        ret.toArray(arr);
        return arr;
    }

    public boolean isSelectOnInsert(Element element)
    {
        Set set = (Set)features.get("SelectOnInsert");
        if(set == null) return false;

        Iterator i = set.iterator();
        while(i.hasNext()) {
            Feature feature = (Feature)i.next();
            if(feature.appliesTo(element)) {
                return true;
            }
        }

        return false;
    }


    public boolean isHidden(Element element)
    {
        Set set = (Set)features.get("Hidden");
        if(set == null) return false;

        Iterator i = set.iterator();
        while(i.hasNext()) {
            Feature feature = (Feature)i.next();
            if(feature.appliesTo(element)) {
                return true;
            }
        }
        return false;
    }

    public boolean isHidden() {
        Set set = (Set)features.get("Hidden");
        if(set == null) return false;
        else return true;
    }


    public TableSet getTableSet(Element element) throws MaxException
    {
        TableSet tset = new TableSet();
        Set set = (Set)features.get("Table");
        if(set == null) return tset;

        Iterator i = set.iterator();
        while(i.hasNext()) {
            Feature feature = (Feature)i.next();
            if(feature.appliesTo(element)) {
                Table table = new Table(feature.getParameter());
                table.init(element, xpathAPI);
                tset.add(table);
            }
        }

        return tset;
    }

    public String[] getGraphicsParams(Element element) throws MaxException {

        String[] graphics = null;
        Set set = (Set)features.get("Graph");
        if (set != null) {
            Iterator i = set.iterator();
            while(i.hasNext()) {
                Feature feature = (Feature)i.next();
                if(feature.appliesTo(element)) {
                    graphics = new String[2];
                    String param = feature.getParameter().trim();
                    char firstChar = param.charAt(0);

                    int idx = param.indexOf(firstChar, 1);
                    if (idx != -1) {
                        graphics[0] = param.substring(1, idx).trim();
                        graphics[1] = param.substring(idx+1).trim();
                    }

                }
            }
        }

        return graphics;
    }
}

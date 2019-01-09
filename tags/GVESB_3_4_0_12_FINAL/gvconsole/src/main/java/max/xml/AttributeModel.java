package max.xml;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import max.xpath.XPath;
import max.xpath.XPathAPI;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Defines attribute behaviour.
 *
 * Special features.
 * <ul>
 *     <li>support for
 *          <pre>
 *          #References: ...
 *          #Choice: ...
 *          #Config: ...
 *          #Document: ...
 *          #CompositeRef: ...
 *          #NotNull: ...
 *          #Warn: ...
 *          #Unique: ...
 *          #Freezed: ...
 *          #Counter: ...
 *          #Pattern: ...
 *          </pre>
 * </ul>
 *
 * @see max.xml.ValuesSelector
 * @see max.xml.ValuesSelectorList
 */
public class AttributeModel
{
    //---------------------------------------------------------------------------
    // FIELDS
    //---------------------------------------------------------------------------

    public String elementName;
    public String name;

    /**
     * Can be: CDATA, ENTITY, ENTITIES, ID, IDREF, IDREFS, NMTOKEN, NMTOKENS,
     * NOTATION, or can be empty (""). If empty then is a choice.
     */
    public String type;
    public String choices[];

    /**
     * Can be: #FIXED, #REQUIRED, #IMPLIED or can be a default value.
     */
    public String defaultType;
    public String defaultValue;
    public String comment;

    private XPathAPI xpathAPI;

    //---------------------------------------------------------------------------
    // FIELDS FOR SPECIAL FEATURES
    //---------------------------------------------------------------------------

    private ValuesSelectorList valuesSelectorList;
    private Map features = null;

    //---------------------------------------------------------------------------
    // CONSTRUCTOR
    //---------------------------------------------------------------------------

    public AttributeModel(String elemName, String attributeName, String type,
                          String choices[], String defaultType, String defaultValue)
    {
        elementName = elemName;
        name = attributeName;
        this.type = type;
        this.choices = choices;
        this.defaultType = defaultType;
        if(defaultValue == null) {
            defaultValue = "";
        }
        this.defaultValue = defaultValue;
    }

    //---------------------------------------------------------------------------
    // METHODS
    //---------------------------------------------------------------------------

    public void setXPathAPI(XPathAPI xpathAPI)
    {
        this.xpathAPI = xpathAPI;
        Feature.setXPathAPI(features, xpathAPI);
    }

    /**
     * Check if the attribute is a counter in a specific element.
     */
    public boolean isCounterInto(Element element)
    {
        Set counterSet = (Set)features.get("Counter");
        if(counterSet != null) {
            Iterator i = counterSet.iterator();
            while(i.hasNext()) {
                Feature feature = (Feature)i.next();
                if(feature.appliesTo(element)) return true;
            }
        }
        return false;
    }

    /**
     * Check if the attribute is declared as #Counter
     */
    public boolean isDeclaredCounter()
    {
        return features.get("Counter") != null;
    }

    public long calculateCounter(Element element)
    {
        Set counterSet = (Set)features.get("Counter");
        if(counterSet != null) {
            Iterator i = counterSet.iterator();
            while(i.hasNext()) {
                Feature feature = (Feature)i.next();
                if(feature.appliesTo(element)) {
                    XPath xpath = (XPath)feature.getFeatureObject();
                    if(xpath == null) {
                        try {
                            xpath = new XPath(feature.getParameter());
                        }
                        catch(TransformerException exc) {
                            System.out.println("ERROR: Invalid xpath: " + feature.getParameter());
                            continue;
                        }
                        feature.setFeatureObject(xpath);
                    }
                    try {
                        long max = 0;
                        NodeList nodeList = xpathAPI.selectNodeList(element, xpath);
                        int N = nodeList.getLength();
                        for(int j = 0; j < N; ++j) {
                            String valStr = XPathAPI.getNodeValue(nodeList.item(j));
                            try {
                                if(!valStr.trim().equals("")) {
                                    long val = Long.parseLong(valStr);
                                    if(val > max) max = val;
                                }
                            }
                            catch(NumberFormatException exc) {
                                exc.printStackTrace();
                            }
                        }
                        return max + 1;
                    }
                    catch(TransformerException exc) {
                        exc.printStackTrace();
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Check if the attribute is declared as #Freezed
     */
    public boolean isDeclaredFreezed()
    {
        return features.get("Freezed") != null;
    }


    /**
     * Check if the attribute is freezed into a specific element.
     */
    public boolean isFreezedInto(Element element)
    {
        Set freezedSet = (Set)features.get("Freezed");
        if(freezedSet != null) {
            Iterator i = freezedSet.iterator();
            while(i.hasNext()) {
                Feature feature = (Feature)i.next();
                if(feature.appliesTo(element)) return true;
            }
        }

        // Se #Freezed non e' stata usata allora l'attributo non e' bloccato
        // oppure se nessua feature #Freezed si applica
        //
        return false;
    }

    public String getName()
    {
        return name;
    }

    public String getElementName()
    {
        return elementName;
    }

    public String getComment()
    {
        return comment;
    }

    public Set getFeature(String feature)
    {
        return (Set) features.get(feature);
    }

    public boolean hasFeature(String feature)
    {
        return features.containsKey(feature);
    }

    public void setComment(String cmnt)
    {
        comment = cmnt;
        features = Feature.getFeatures(elementName, comment);
        valuesSelectorList = new ValuesSelectorList(features);
    }


    public Collection getValues(Element currentElement)
    {
        if(valuesSelectorList == null) return null;
        return valuesSelectorList.getValues(currentElement, currentElement.getAttribute(name));
    }


    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("<!ATTLIST ").append(elementName).append(" ").append(name);
        sb.append(" ").append(type);
        if(choices != null) {
            sb.append(" (");
            for(int i = 0; i < choices.length; ++i) {
                sb.append(choices[i]);
                if(i < choices.length - 1) sb.append("|");
            }
            sb.append(")");
        }
        sb.append(" ").append(defaultType);
        if(!defaultValue.equals("")) {
            sb.append(" \"").append(defaultValue).append("\"");
        }
        sb.append(">").append(System.getProperty("line.separator"));
        return sb.toString();
    }


    public void addChecks(WarningManager wm)
    {
        // Check from the values selector
        //
        if((valuesSelectorList != null)
         && !valuesSelectorList.isEmpty()) {
            wm.addCheck(
                new AttributeValueCheck(elementName, name, valuesSelectorList)
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
                        new AttributeNotNullCheck(feature, elementName, name)
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
                        new AttributePatternCheck(feature, elementName, name)
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
                        new AttributeWarningCheck(feature, elementName, name)
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
                        new AttributeUniqueCheck(feature, elementName, name)
                    );
                }
            }
        }
    }
}

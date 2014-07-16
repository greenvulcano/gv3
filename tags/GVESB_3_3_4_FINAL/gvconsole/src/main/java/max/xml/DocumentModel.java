/*
 * Copyright (c) 2004 E@I Software - All right reserved
 * 
 * Created on 30-Sep-2004
 * 
 * $Date: 2010-04-12 15:11:11 $ $Header:
 * /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS
 * /core/webapps/gvconsole/src/main/java/max/xml/DocumentModel.java,v 1.2
 * 2010-04-11 14:32:08 nlariviera Exp $ $Id: DocumentModel.java,v 1.2 2010-04-11
 * 14:32:08 nlariviera Exp $ $Name: $ $Locker: $ $Revision: 1.3 $ $State: Exp $
 */
package max.xml;

import it.greenvulcano.log.GVLogger;

import java.io.StringReader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import max.xpath.XPathAPI;

import org.apache.log4j.Logger;


/**
 * Questa classe � utilizzata per la costruzione dell'interfaccia di
 * "XML Editor" guidata da DTD.
 */
public class DocumentModel
{
    private static final Logger                  logger              = GVLogger.getLogger(DocumentModel.class);

    public static final String                   DETAILS_START       = "<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>";
    public static final String                   DETAILS_END         = "</xsl:stylesheet>";

    private Hashtable                            elementModels       = new Hashtable();

    private Hashtable<String, Set<ElementModel>> elementsWithFeature = new Hashtable<String, Set<ElementModel>>();
    public String                                systemId;
    public String                                publicId;
    public StringBuffer                          templates           = new StringBuffer();
    public Transformer                           transformer;

    public DocumentModel(String pubId, String sysId)
    {
        systemId = sysId;
        publicId = pubId;
    }

    public void setXPathAPI(XPathAPI xpathAPI)
    {
        Iterator i = elementModels.values().iterator();
        while (i.hasNext()) {
            ElementModel elementModel = (ElementModel) i.next();
            elementModel.setXPathAPI(xpathAPI);
        }
    }

    /**
     * @return
     */
    public Transformer getDetailTransformer()
    {
        if (transformer != null) {
            return transformer;
        }

        StringBuilder buff = new StringBuilder();
        buff.append(DETAILS_START).append(templates).append(DETAILS_END);

        StringReader reader = new StringReader(buff.toString());

        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            transformer = tFactory.newTransformer(new StreamSource(reader));
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "US-ASCII");
        }
        catch (Exception exc) {
            logger.warn("Error retrieving detail transformer", exc);
            logger.warn("Detail transformer:\n" + buff);

            transformer = null;
        }
        return transformer;
    }


    public ElementModel getElementModel(String elementName)
    {
        return (ElementModel) elementModels.get(elementName);
    }


    public ContentModel getContentModel(String elementName)
    {
        ElementModel element = getElementModel(elementName);
        return element.getContentModel();
    }


    public AttributeModel getAttributeModel(String elementName, String attributeName)
    {
        ElementModel em = (ElementModel) elementModels.get(elementName);
        if (em == null) {
            return null;
        }
        return em.getAttributeModel(attributeName);
    }


    public String[] getAttributeNames(String elementName)
    {
        ElementModel em = (ElementModel) elementModels.get(elementName);
        if (em == null) {
            return null;
        }
        return em.getAttributeNames();
    }


    public void addElementModel(ElementModel em)
    {
        elementModels.put(em.getName(), em);
        String templateStr = em.getTemplate();
        if (templateStr != null) {
            templates.append(templateStr);
        }
    }


    public void addAttributeModel(String elementName, AttributeModel am)
    {
        ElementModel em = (ElementModel) elementModels.get(elementName);
        if (em == null) {
            return;
        }
        em.addAttributeModel(am);
    }


    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        for (Enumeration e = elementModels.elements(); e.hasMoreElements();) {
            ElementModel em = (ElementModel) e.nextElement();
            sb.append(em.toString());
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }


    public void addChecks(WarningManager wm)
    {
        Iterator i = elementModels.values().iterator();
        while (i.hasNext()) {
            ElementModel em = (ElementModel) i.next();
            em.addChecks(wm);
        }
    }

    public Set getElementsWithFeature(String feature)
    {
        return getElementsWithFeature(feature, false);
    }

    public Set getElementsWithFeature(String feature, boolean checkAttributes)
    {
        Set ewf = elementsWithFeature.get(feature);
        if (ewf == null) {
            ewf = new HashSet();
            Collection values = elementModels.values();
            Iterator i = values.iterator();
            while (i.hasNext()) {
                ElementModel em = (ElementModel) i.next();
                if (em.hasFeature(feature, checkAttributes)) {
                    ewf.add(em);
                }
            }
            elementsWithFeature.put(feature, ewf);
        }
        return ewf;
    }
}

/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.virtual.jca;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XMLResultHandler class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class XMLResultHandler
{

    private static final Logger logger = GVLogger.getLogger(XMLResultHandler.class);

    private static class PropertyDescriptor
    {
        String name;
        String xpath;
        String defaultValue;

        PropertyDescriptor(Node config) throws XMLConfigException
        {
            name = XMLConfig.get(config, "@name");
            xpath = XMLConfig.get(config, "@xpath");
            defaultValue = XMLConfig.get(config, "@default");
        }

        @Override
        public String toString()
        {
            return name + "=" + xpath + (defaultValue == null ? "" : " (" + defaultValue + ")");
        }
    }

    private String                        xpathRetCode    = null;
    private List<PropertyDescriptor> xpathProperties = null;

    /**
     * @param paramGVBuffer
     * @throws XMLConfigException
     */
    public void init(Node paramGVBuffer) throws XMLConfigException
    {
        xpathRetCode = XMLConfig.get(paramGVBuffer, "@xpath-retCode");
        if (xpathRetCode != null) {
            logger.debug("XPath return code..: " + xpathRetCode);
        }

        NodeList extendedList = XMLConfig.getNodeList(paramGVBuffer, "property");
        if (extendedList.getLength() != 0) {
            xpathProperties = new LinkedList<PropertyDescriptor>();
            for (int i = 0; i < extendedList.getLength(); ++i) {
                Node extendedNode = extendedList.item(i);
                PropertyDescriptor descriptor = new PropertyDescriptor(extendedNode);
                xpathProperties.add(descriptor);
                logger.debug("Property.....: " + descriptor);
            }
        }
    }

    /**
     * @return if is XML
     */
    public boolean hasEffects()
    {
        return (xpathRetCode != null) || (xpathProperties != null);
    }

    /**
     * Estrae i dati dall'XML e riempie l'GVBuffer di output.
     *
     * @param gvBuffer
     * @param document
     * @return the output GVBuffer
     * @throws JCACallException
     */
    public GVBuffer fillGVBuffer(GVBuffer gvBuffer, Document document) throws JCACallException
    {
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            // Se c'e' un XPath che definisce il retcode lo applica
            //
            if (xpathRetCode != null) {
                String retCode = parser.get(document, xpathRetCode, "0");
                try {
                    gvBuffer.setRetCode(Integer.parseInt(retCode));
                }
                catch (Exception e1) {
                    logger.debug("Exception", e1);
                    throw new JCACallException("GVVCL_JCA_READ_RETCODE_ERROR", new String[][]{{"exc", "" + e1}});
                }
            }

            // Se ci sono degli XPath per le properties li applica
            //
            if (xpathProperties != null) {
                Iterator<PropertyDescriptor> i = xpathProperties.iterator();
                while (i.hasNext()) {
                    PropertyDescriptor descriptor = i.next();
                    String value = parser.get(document, descriptor.xpath, descriptor.defaultValue);
                    try {
                        if (value != null) {
                            gvBuffer.setProperty(descriptor.name, value);
                        }
                    }
                    catch (GVException e1) {
                        logger.debug("GVException", e1);
                        throw new JCACallException("GVVCL_JCA_READ_PROPERTY_ERROR", new String[][]{{"exc", "" + e1}});
                    }
                }
            }

            return gvBuffer;
        }
        catch (XMLUtilsException e) {
            logger.debug("XMLUtilsException", e);
            throw new JCACallException("GVVCL_JCA_INTERNAL_ERROR", e);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }

    }
}

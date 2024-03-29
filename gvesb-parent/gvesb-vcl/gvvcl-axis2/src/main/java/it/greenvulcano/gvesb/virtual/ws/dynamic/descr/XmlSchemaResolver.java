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
package it.greenvulcano.gvesb.virtual.ws.dynamic.descr;

import it.greenvulcano.gvesb.virtual.ws.utils.NormalizeUtil;
import it.greenvulcano.log.GVLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.UnknownExtensibilityElement;

import org.apache.log4j.Logger;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.w3c.dom.Element;

import com.ibm.wsdl.extensions.schema.SchemaImpl;


/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XmlSchemaResolver
{
    private static final Logger logger = GVLogger.getLogger(XmlSchemaResolver.class);

    /**
     * Populate a List with all the top level SchemaType objects (complexTypes,
     * simpleTypes and elements) generated by parsing the schemas associated
     * with a Definition object
     *
     * @param def
     *        The Definition object representing the WSDL
     * @param baseUri
     * @param schemas
     *
     * @throws WSDLException
     */
    public static void evaluateXmlSchemas(Definition def, String baseUri, Map<String, List<XmlSchema>> schemas)
            throws WSDLException
    {
        // Get the imports out of the definitions read by the WSDL Parser
        Map<?, ?> imports = def.getImports();

        if (imports != null) {

            Iterator<?> valueIterator = imports.values().iterator();

            while (valueIterator.hasNext()) {
                List<?> importList = (List<?>) valueIterator.next();

                if (importList != null) {
                    Iterator<?> importIterator = importList.iterator();

                    while (importIterator.hasNext()) {
                        Import tempImport = (Import) importIterator.next();

                        if (tempImport != null) {
                            Definition importedDef = tempImport.getDefinition();

                            if (importedDef != null) {
                                evaluateXmlSchemas(importedDef, NormalizeUtil.normalizeUri(baseUri,
                                        tempImport.getLocationURI()), schemas);
                            }
                        }
                    }
                }
            }
        }

        Types types = def.getTypes();

        if (types != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Types in definition found: " + def.getTargetNamespace());
            }

            Iterator<?> extEleIt = types.getExtensibilityElements().iterator();

            while (extEleIt.hasNext()) {
                Object o = extEleIt.next();
                Element elem;

                if (o.getClass() == SchemaImpl.class) {
                    SchemaImpl s = (SchemaImpl) o;

                    elem = s.getElement();
                }
                else {
                    UnknownExtensibilityElement s = (UnknownExtensibilityElement) o;

                    elem = s.getElement();
                }

                XmlSchemaCollection col = new XmlSchemaCollection();

                col.setBaseUri(baseUri);

                XmlSchema root = col.read(elem);

                handleXmlSchema(root, schemas);
            }
        }
    }

    private static void handleXmlSchema(XmlSchema root, Map<String, List<XmlSchema>> schemas)
    {
        XmlSchemaObjectCollection col1 = root.getIncludes();
        Iterator<?> iter = col1.getIterator();
        String targetNamespace = root.getTargetNamespace();

        if (logger.isDebugEnabled()) {
            logger.debug("Imported schema: " + root.getTargetNamespace());
        }

        if (!schemas.containsKey(targetNamespace)) {
            ArrayList<XmlSchema> list = new ArrayList<XmlSchema>();

            list.add(root);
            schemas.put(targetNamespace, list);

            while (iter.hasNext()) {
                Object o = iter.next();
                String location;
                XmlSchema schema;

                if (o.getClass() == XmlSchemaImport.class) {
                    XmlSchemaImport schemaImp = (XmlSchemaImport) o;

                    location = schemaImp.getSchemaLocation();
                    schema = schemaImp.getSchema();

                    if ((location != null) && (location.length() > 0)) {
                        handleXmlSchema(schema, schemas);
                    }
                    else {
                        logger.warn("Schema with no schemaLocation attribute found: " + schema);
                    }
                }
                else {
                    XmlSchemaInclude schemaImp = (XmlSchemaInclude) o;

                    location = schemaImp.getSchemaLocation();
                    schema = schemaImp.getSchema();

                    logger.debug("Include: " + location);
                    if ((location != null) && (location.length() > 0)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Included schema: " + root.getTargetNamespace());
                        }

                        list.add(schema);
                    }
                }
            }
        }
    }
}

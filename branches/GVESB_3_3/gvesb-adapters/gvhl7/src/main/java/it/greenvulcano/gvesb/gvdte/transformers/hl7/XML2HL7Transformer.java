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
package it.greenvulcano.gvesb.gvdte.transformers.hl7;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.gvdte.config.DataSource;
import it.greenvulcano.gvesb.gvdte.config.DataSourceFactory;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransfException;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransformer;
import it.greenvulcano.gvesb.gvdte.util.TransformerHelper;
import it.greenvulcano.gvesb.gvdte.util.UtilsException;
import it.greenvulcano.gvesb.gvdte.util.xml.EntityResolver;
import it.greenvulcano.gvesb.gvdte.util.xml.ErrorHandler;
import it.greenvulcano.gvesb.gvdte.util.xml.ErrorListener;
import it.greenvulcano.gvesb.gvdte.util.xml.URIResolver;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.ErrHandler;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.parser.Parser;

/**
 * This class is dedicated to XSL transformations.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XML2HL7Transformer implements DTETransformer
{
    private static final Logger     logger      = GVLogger.getLogger(XML2HL7Transformer.class);

    private String                  name;
    private String                  validationType;

    private String                  xslMapName;

    private boolean                 validateMap = false;

    private String                  dataSourceSet;

    private DataSourceFactory       dsf;

    private Map<String, Templates>  templMap    = new HashMap<String, Templates>();

    private List<TransformerHelper> helpers     = new ArrayList<TransformerHelper>();

    private Parser                  hl7StringParser = null;
    private DefaultXMLParser        hl7XmlParser    = null;


    public XML2HL7Transformer()
    {
        // do nothing
    }

    /**
     * Initialize the instance.
     *
     * @see DTETransformer#init(Node, DataSourceFactory)
     */
    public void init(Node node, DataSourceFactory dsf) throws DTETransfException
    {
        logger.debug("Init start");
        try {
            this.dsf = dsf;
            name = XMLConfig.get(node, "@name", "NO_NAME");
            xslMapName = XMLConfig.get(node, "@InputXSLMapName", "");
            dataSourceSet = XMLConfig.get(node, "@DataSourceSet", "Default");

            String validateXSL = XMLConfig.get(node, "@validate");
            String validateTransformations = XMLConfig.get(node, "../@validate");
            String lvalidationType = XMLConfig.get(node, "@validationType");
            setValidationType(lvalidationType);

            if (validateTransformations != null) {
                if (validateTransformations.equals("true")) {
                    setValidate("true");
                }
                else if (validateXSL != null) {
                    if (validateXSL.equals("true")) {
                        setValidate("true");
                    }
                }
            }
            else {
                if (validateXSL != null) {
                    if (validateXSL.equals("true")) {
                        setValidate("true");
                    }
                }
            }
            logger.debug("init - loaded parameters: xslMapName = " + xslMapName + " - DataSourceSet: " + dataSourceSet
                    + " - validate = " + validateMap);

            if (!xslMapName.equals("")) {
                initTemplMap();
                initHelpers(node);
            }

            hl7StringParser = new GenericParser();
            hl7XmlParser = new DefaultXMLParser();

            logger.debug("Init stop");
        }
        catch (DTETransfException exc) {
            throw exc;
        }
        catch (XMLConfigException exc) {
            logger.error("Error while accessing configuration", exc);
            throw new DTETransfException("GVDTE_CONFIGURATION_ERROR", new String[][]{{"cause",
                    "while accessing configuration"}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, exc);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * This method initialize the Map containing templates for certain
     * dataSource and xslMapName
     *
     * @return templMap the Map templates object
     * @throws DTETransfException
     */
    private Map<String, Templates> initTemplMap() throws DTETransfException
    {
        String key = dataSourceSet + "::" + xslMapName;
        try {
            Templates templates = getTemplate(dataSourceSet, xslMapName);
            templMap.put(key, templates);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, exc);
        }
        return templMap;
    }

    /**
     * This method get the Template corresponding to the xslMapName.
     *
     * @param dss
     *        the DataSourceSet name
     * @param mn
     *        the MapName
     * @throws Exception
     */
    private Templates getTemplate(String dss, String mn) throws Exception
    {
        int idx = mn.lastIndexOf("://");
        if (idx == -1) {
            mn = "gvdte://" + mn;
        }
        DataSource reposManager = dsf.getDataSource(dss, mn);
        TransformerFactory tFactory = TransformerFactory.newInstance();
        tFactory.setURIResolver(new URIResolver(dss, dsf));
        Source source = new StreamSource(new ByteArrayInputStream(reposManager.getResourceAsByteArray(mn)));
        source.setSystemId(reposManager.getResourceURL(mn));
        return tFactory.newTemplates(source);
    }

    /**
     * @param node
     */
    private void initHelpers(Node node) throws Exception
    {
        NodeList nl = XMLConfig.getNodeList(node, "*[@type='transformer-helper']");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            TransformerHelper helper = (TransformerHelper) Class.forName(XMLConfig.get(n, "@class")).newInstance();
            helper.init(n);
            helpers.add(helper);
        }
    }

    /**
     * The <code>input</code> parameter may be a Document, an XML string, a byte
     * array containing an XML, or an <tt>InputStream</tt> from which an XML can
     * be read. The <code>buffer</code> parameter is not used. The return value
     * is a Document representing the result of the transformation.
     *
     *
     * @param input
     *        the input data of the transformation.
     * @param buffer
     *        the intermediate result of the transformation (if needed).
     * @param mapParam
     * @return a DOM representing the result of the XSL transformation.
     * @throws DTETransfException
     *         if any transformation error occurs.
     */
    public Object transform(Object input, Object buffer, Map<String, Object> mapParam) throws DTETransfException, 
        InterruptedException
    {
        logger.debug("Transform start");
        Transformer transformer = null;
        String hl7MsgXml;
        String hl7MsgString;
        try {
            if (xslMapName.equals("")) {
                hl7MsgXml = convertInputFormatForHL7(input);
            }
            else {
                transformer = getTransformer(mapParam);
                setParams(transformer, mapParam);
                Source theSource = convertInputFormatForXSL(input);
                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                StreamResult theStreamResult = new StreamResult(byteOutputStream);
                transformer.transform(theSource, theStreamResult);
                hl7MsgXml = byteOutputStream.toString();
            }

            Message hl7Msg = hl7XmlParser.parse(hl7MsgXml);
            hl7MsgString = hl7StringParser.encode(hl7Msg);

            logger.debug("Transform stop");
            return hl7MsgString;
        }
        catch (DTETransfException exc) {
            throw exc;
        }
        catch (UtilsException exc) {
            logger.error("Error while converting input object", exc);
            throw new DTETransfException("GVDTE_CONVERSION_ERROR", new String[][]{{"msg", "converting input object"}},
                    exc);
        }
        catch (TransformerException exc) {
            logger.error("Error while performing XSL transformation", exc);
            throw new DTETransfException("GVDTE_XSLT_ERROR", new String[][]{{"cause",
                    "while performing XSL transformation"}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error"}}, exc);
        }
        finally {
            if (transformer != null) {
                transformer.clearParameters();
            }
        }
    }

    /**
     * This method get or create the Transformer reading from HashMap
     *
     * @param hashMapParam
     *        The hashMap containing the dataSource and xslMapName client
     * @return transformer The Transformer object
     * @throws DTETransfException
     */
    private Transformer getTransformer(Map<String, Object> mapParam) throws DTETransfException
    {
        Transformer transformer = null;
        String key = "";
        String dss = "";
        String mn = "";
        // HashMapParam has the datasourceset/xslmapname values
        // required by client
        //
        if (mapParam != null) {
            dss = (String) mapParam.get("datasourceset");
            mn = (String) mapParam.get("xslmapname");
        }
        if ((dss == null) || (dss.equals(""))) {
            dss = dataSourceSet;
        }
        if ((mn == null) || (mn.equals(""))) {
            mn = xslMapName;
        }
        key = dss + "::" + mn;
        try {
            Templates templates = templMap.get(key);
            if (templates == null) {
                templates = getTemplate(dss, mn);
                templMap.put(key, templates);
            }
            transformer = templates.newTransformer();
            transformer.setErrorListener(new ErrorListener());
        }
        catch (Throwable exc) {
            logger.error("Error while creating XSL transformer", exc);
            throw new DTETransfException("GVDTE_XSLT_ERROR",
                    new String[][]{{"cause", "while creating XSL transformer."}}, exc);
        }

        return transformer;
    }

    /**
     * Execute the document validation
     *
     * @param docValidation
     *        The document to validate
     * @param mapParam
     *        The Map object
     * @throws DTETransfException
     */
    private void executeValidation(Document docValidation, Map<String, Object> mapParam) throws DTETransfException
    {
        XMLUtils xmlParser = null;
        try {
            xmlParser = XMLUtils.getParserInstance();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                    xmlParser.serializeDOMToByteArray(docValidation));
            logger.debug("Validate output with '" + getValidationType() + "'");
            String dss = "";
            if (mapParam != null) {
                dss = (String) mapParam.get("datasourceset");
            }
            if ((dss == null) || (dss.equals(""))) {
                dss = dataSourceSet;
            }
            XMLUtils.parseDOMValidating(getValidationType(), byteArrayInputStream, new EntityResolver(dss, dsf),
                    new ErrorHandler());
        }
        catch (XMLUtilsException exc) {
            logger.error("Error while performing XML validation", exc);
            throw new DTETransfException("GVDTE_XSLT_ERROR", new String[][]{{"cause",
                    "while performing XML validation."}}, exc);
        }
        finally {
            XMLUtils.releaseParserInstance(xmlParser);
        }
    }

    /**
     * This method checks input object type: If it's an XML string, it parses it
     * into a Document; If it's a byte array, it parses it into a Document, If
     * it's an <tt>InputStream</tt>, it reads from it returning its content as a
     * Document.
     *
     * @param input
     *        the input data object for the transformation.
     * @return the input data object converted to a DOM.
     * @throws UtilsException
     *         if any error occurs while converting input object.
     */
    private Source convertInputFormatForXSL(Object input) throws UtilsException
    {
        Source inputSrc = null;
        try {
            if (input instanceof Node) {
                ByteArrayInputStream byteArrayInputStream = null;
                if (validate()) {
                    XMLUtils xmlParser = null;
                    try {
                        logger.debug("Validate input with '" + getValidationType() + "'");
                        xmlParser = XMLUtils.getParserInstance();
                        byteArrayInputStream = new ByteArrayInputStream(xmlParser.serializeDOMToByteArray((Node) input));
                        XMLUtils.parseDOMValidating(getValidationType(), byteArrayInputStream, new EntityResolver(
                                dataSourceSet, dsf), new ErrHandler());
                    }
                    finally {
                        XMLUtils.releaseParserInstance(xmlParser);
                    }
                }
                if (input instanceof Document) {
                    logger.debug("Input object is a Document");
                    inputSrc = new DOMSource((Document) input);
                }
                else {
                    logger.debug("Input object is a Node");
                    if (byteArrayInputStream != null) {
                        byteArrayInputStream.reset();
                        inputSrc = new StreamSource(byteArrayInputStream);
                    }
                    else {
                        inputSrc = new StreamSource(new ByteArrayInputStream(
                                XMLUtils.serializeDOMToByteArray_S((Node) input)));
                    }
                }
            }
            else if (input instanceof String) {
                if (validate()) {
                    logger.debug("Validate input with '" + getValidationType() + "'");
                    String validateString = (String) input;
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(validateString.getBytes());
                    XMLUtils.parseDOMValidating(getValidationType(), byteArrayInputStream, new EntityResolver(
                            dataSourceSet, dsf), new ErrorHandler());
                }
                logger.debug("Input object is a String");
                inputSrc = new StreamSource(new StringReader((String) input));
            }
            else if (input instanceof byte[]) {
                ByteArrayInputStream validateByteArrayInputStream = new ByteArrayInputStream((byte[]) input);
                ByteArrayInputStream byteArrayInputStream = validateByteArrayInputStream;
                if (validate()) {
                    logger.debug("Validate input with '" + getValidationType() + "'");
                    byteArrayInputStream = new ByteArrayInputStream((byte[]) input);
                    XMLUtils.parseDOMValidating(getValidationType(), validateByteArrayInputStream, new EntityResolver(
                            dataSourceSet, dsf), new ErrorHandler());
                }
                logger.debug("Input object is a byte array");
                inputSrc = new StreamSource(byteArrayInputStream);
            }
            else if (input instanceof InputStream) {
                InputStream validateInputStream = (InputStream) input;
                InputStream inputStream = validateInputStream;
                if (validate()) {
                    logger.debug("Validate input with '" + getValidationType() + "'");
                    XMLUtils.parseDOMValidating(getValidationType(), validateInputStream, new EntityResolver(dsf),
                            new ErrorHandler());
                }
                logger.debug("Input object is an InputStream");
                inputSrc = new StreamSource(inputStream);
            }
            else {
                throw new UtilsException("GVDTE_GENERIC_ERROR", new String[][]{{"msg",
                        "Invalid input type: " + input.getClass()}});
            }
            return inputSrc;
        }
        catch (XMLUtilsException exc) {
            logger.error("Error while performing XML validation", exc);
            throw new UtilsException("GVDTE_XSLT_ERROR", new String[][]{{"cause", "while performing XML validation."}},
                    exc);
        }
        catch (UtilsException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new UtilsException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, exc);
        }
    }

    /**
     * This method checks input object type: If it's an XML string, it parses it
     * into a Document; If it's a byte array, it parses it into a Document, If
     * it's an <tt>InputStream</tt>, it reads from it returning its content as a
     * Document.
     *
     * @param input
     *        the input data object for the transformation.
     * @return the input data object converted to a String.
     * @throws UtilsException
     *         if any error occurs while converting input object.
     */
    private String convertInputFormatForHL7(Object input) throws UtilsException
    {
        try {
            if (input instanceof String) {
                logger.debug("Input object is a String");
                return (String) input;
            }
            else if (input instanceof byte[]) {
                logger.debug("Input object is a byte array");
                return new String((byte[]) input);
            }
            else if (input instanceof Node) {
                logger.debug("Input object is a Node");
                return XMLUtils.serializeDOM_S((Node) input);
            }
            else {
                throw new UtilsException("GVDTE_GENERIC_ERROR", new String[][]{{"msg",
                        "Invalid input type: " + input.getClass()}});
            }
        }
        catch (UtilsException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new UtilsException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, exc);
        }
    }

    /**
     * Set the parameters key:value pair as 'transformer' parameters
     *
     * @param transformer
     *        the transformer on which set parameters
     * @param mapParam
     *        the key:value pair to use as transformation parameters
     */
    private void setParams(Transformer transformer, Map<String, Object> mapParam)
    {
        if (mapParam == null) {
            return;
        }
        String parameters = "XSL parameters:";
        try {
            for (String name : mapParam.keySet()) {
                Object value = mapParam.get(name);
                parameters += "\n" + name + "=" + value;
                transformer.setParameter(name, (value != null) ? value.toString() : "NULL");
            }
        }
        finally {
            logger.debug(parameters);
        }
    }

    /**
     * Get the xslMapName object
     *
     * @return xslMapName The xslMap name
     */
    public String getMapName()
    {
        return xslMapName;
    }

    /**
     * Set the validation map attribute at true
     *
     * @param validate
     *        if true validation is ok
     */
    public void setValidate(String validate)
    {
        if (validate.equals("true")) {
            validateMap = true;
        }
    }

    /**
     * Set the type of validation
     *
     * @param type
     *        the validation type
     */
    public void setValidationType(String type)
    {
        if (type != null) {
            validationType = type;
        }
        else {
            validationType = "xsd";
        }
    }

    /**
     * Get the validation type
     *
     * @return validationType the validation type
     */
    public String getValidationType()
    {
        return validationType;
    }

    /**
     * Return the validation of map
     *
     * @return validateMap the validate map value
     */
    public boolean validate()
    {
        return validateMap;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#clean()
     */
    public void clean()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#destroy()
     */
    public void destroy()
    {
        templMap.clear();
        dsf = null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#getHelpers()
     */
    @Override
    public List<TransformerHelper> getHelpers()
    {
        return helpers;
    }
}

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
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
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
 * @version 3.0.0 Set 23, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class HL72XMLTransformer implements DTETransformer
{
    private static final Logger     logger          = GVLogger.getLogger(HL72XMLTransformer.class);

    private String                  validationType;

    private String                  xslMapName;

    private boolean                 validateMap     = false;

    private String                  dataSourceSet;

    private DataSourceFactory       dsf;

    private Map<String, Templates>  templMap        = new HashMap<String, Templates>();

    private List<TransformerHelper> helpers         = new ArrayList<TransformerHelper>();

    private Parser                  hl7StringParser = null;
    private DefaultXMLParser        hl7XmlParser    = null;

    public HL72XMLTransformer()
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
            xslMapName = XMLConfig.get(node, "@OutputXSLMapName", "");
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
    public Object transform(Object input, Object buffer, Map<String, Object> mapParam) throws DTETransfException
    {
        logger.debug("Transform start");
        Transformer transformer = null;
        try {
            String hl7MsgString = convertInputFormat(input);

            Message hl7Msg = hl7StringParser.parse(hl7MsgString);

            String hl7MsgXml = hl7XmlParser.encode(hl7Msg);

            if (!xslMapName.equals("")) {
                transformer = getTransformer(mapParam);
                setParams(transformer, mapParam);
                Source theSource = new StreamSource(new StringReader(hl7MsgXml));
                String outputType = transformer.getOutputProperty(OutputKeys.METHOD);
                if (outputType == null) {
                    outputType = "xml";
                }
                if (outputType.equals("xml")) {
                    DOMResult theDOMResult = new DOMResult();
                    transformer.transform(theSource, theDOMResult);
                    Document docValidation = (Document) theDOMResult.getNode();
                    if (validate()) {
                        executeValidation(docValidation, mapParam);
                    }
                    logger.debug("Transform stop");
                    return theDOMResult.getNode();
                }
                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                StreamResult theStreamResult = new StreamResult(byteOutputStream);
                transformer.transform(theSource, theStreamResult);
                byte[] byteResult = byteOutputStream.toByteArray();
                logger.debug("Transform stop");
                return byteResult;
            }

            logger.debug("Transform stop");
            return hl7MsgXml;
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
     *
     * @param input
     *        the input data object for the transformation.
     * @return the input data object converted to a String.
     * @throws UtilsException
     *         if any error occurs while converting input object.
     */
    private String convertInputFormat(Object input) throws UtilsException
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

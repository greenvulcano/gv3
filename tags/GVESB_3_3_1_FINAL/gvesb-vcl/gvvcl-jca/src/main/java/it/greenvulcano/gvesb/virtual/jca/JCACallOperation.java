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
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.gvesb.virtual.jca.xml.JCAXML;
import it.greenvulcano.gvesb.virtual.jca.xml.JCAXMLException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;
import javax.resource.cci.RecordFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

/**
 * This class realizes call mechanism for a JCA.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JCACallOperation implements CallOperation
{
    private static final Logger logger            = GVLogger.getLogger(JCACallOperation.class);

    private static final int    OUT_EXEC_IN       = 0;
    private static final int    EXEC_IN           = 1;
    private static final int    EXEC_IN_OUT       = 2;

    private static final int    OUT_MAPPED        = 0;
    private static final int    OUT_INDEXED       = 1;

    /**
     *
     */
    protected OperationKey      key               = null;
    private String              jndiNameEis       = null;
    private int                 execType          = OUT_EXEC_IN;
    private int                 outputType        = OUT_INDEXED;
    private String              outputName        = null;
    private ConnectionSpec      connectionSpec    = null;
    private InteractionSpec     interactionSpec   = null;
    private ConnectionFactory   connectionFactory = null;
    private RecordFactory       recordFactory     = null;
    private JCAXML              jcaxml            = null;
    private String              namespacePrefix   = null;
    private List<String>        xpathXMLData      = null;
    private DocumentBuilder     documentBuilder   = null;
    private XMLResultHandler    xmlResultHandler  = null;

    /**
     * Operation initialization
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws InitializationException
    {
        try {
            initParameters(node);
            initConnectionSpec(node);
            initInteractionSpec(node);
            initConnectionFactory(node);
            initXMLData(node);
            initParamGVBuffer(node);
        }
        catch (XMLConfigException exc) {
            logger.debug("Error during initialization", exc);
            throw new InitializationException("GVVCL_XML_CONFIG_ERROR", new String[][]{{"exc", exc.toString()},
                    {"key", "N/A"}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws InvalidDataException, JCACallException
    {
        try {
            return executeCall(gvBuffer);
        }
        catch (ResourceException exc) {
            logger.debug("Resource exception", exc);
            throw new JCACallException("GVVCL_JCA_INTERNAL_ERROR", new String[][]{});
        }
        catch (JCAXMLException exc) {
            logger.debug("JCAXMLException", exc);
            throw new InvalidDataException("GVVCL_INVALID_DATA_ERROR", new String[][]{{"exc", "" + exc}});
        }
        catch (XMLConfigException exc) {
            logger.debug("XMLConfigException", exc);
            throw new JCACallException("GVVCL_XML_CONFIG_ERROR", new String[][]{{"exc", "" + exc}, {"key", "N/A"}});
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    public OperationKey getKey()
    {
        return key;
    }

    /**
     * Does nothing for this operation.
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    public final void destroy()
    {
        // do nothing
    }

    /**
     * String to use with logs.
     *
     * @return the description
     */
    public String getDescription()
    {
        return "Java Connector: " + jndiNameEis;
    }

    /**
     * Actually executes the call.
     */
    private GVBuffer executeCall(GVBuffer gvBuffer) throws ResourceException, JCAXMLException, XMLConfigException,
            JCACallException
    {
        Connection conn = null;
        if (connectionSpec == null) {
            conn = connectionFactory.getConnection();
        }
        else {
            conn = connectionFactory.getConnection(connectionSpec);
        }

        try {
            Interaction ix = conn.createInteraction();
            Record iRec = jcaxml.buildRecord((Node) gvBuffer.getObject());
            Record oRec = null;
            switch (execType) {
                case EXEC_IN :
                    ix.execute(interactionSpec, iRec);
                    // Niente output: ritorniamo l'input.
                    return gvBuffer;

                case EXEC_IN_OUT :
                    if (outputType == OUT_INDEXED) {
                        oRec = recordFactory.createIndexedRecord(outputName);
                    }
                    else {
                        oRec = recordFactory.createMappedRecord(outputName);
                    }
                    if (!ix.execute(interactionSpec, iRec, oRec)) {
                        throw new JCACallException("GVVCL_JCA_OUTPUT_NOT_UPDATED_ERROR", new String[][]{});
                    }
                    break;

                case OUT_EXEC_IN :
                    oRec = ix.execute(interactionSpec, iRec);
                    break;

                default :
                    return gvBuffer;
            }

            return prepareOutputGVBuffer(gvBuffer, oRec);
        }
        finally {
            conn.close();
        }
    }

    /**
     * Produce l'GVBuffer di output.
     *
     * @param gvBuffer
     *        GVBuffer di input
     * @param oRec
     *        Record JCA di output
     * @return
     * @throws JCAXMLException
     * @throws XMLConfigException
     */
    private GVBuffer prepareOutputGVBuffer(GVBuffer gvBuffer, Record oRec) throws JCAXMLException, XMLConfigException,
            JCACallException
    {
        Document document = jcaxml.buildDocument(oRec);
        stringToXML(document);
        gvBuffer = xmlResultHandler.fillGVBuffer(gvBuffer, document);
        try {
            gvBuffer.setObject(document);
        }
        catch (GVException e) {
            logger.fatal("GVException", e);
        }
        return gvBuffer;
    }

    /**
     * Sostituisce gli elementi configurati per essere XML con l'XML
     * corrispondente.
     *
     * @param document
     */
    private void stringToXML(Document document) throws JCACallException
    {
        if (xpathXMLData == null) {
            return;
        }

        List<Element> elements = new ArrayList<Element>();
        for (String xpath : xpathXMLData) {
            NodeIterator ni;
            try {
                ni = XPathAPI.selectNodeIterator(document, xpath);
            }
            catch (TransformerException e) {
                logger.debug("Invalid xpath: " + xpath, e);
                throw new JCACallException("GVVCL_JCA_PARSEROUTPUT_ERROR", new String[][]{{"exc", "" + e}}, e);
            }
            Node node = ni.nextNode();
            while (node != null) {
                if (node instanceof Element) {
                    elements.add((Element) node);
                }
                node = ni.nextNode();
            }
        }

        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            for (Element element : elements) {
                String value = parser.getNodeContent(element);
                StringReader reader = new StringReader(value);
                Document res;
                element.setAttribute("type", "xml");
                try {
                    res = documentBuilder.parse(new InputSource(reader));
                    Element root = (Element) (element.getOwnerDocument().importNode(res.getDocumentElement(), true));
                    while (element.hasChildNodes()) {
                        element.removeChild(element.getFirstChild());
                    }
                    element.appendChild(root);
                }
                catch (Exception e) {
                    logger.debug("Cannot convert to xml", e);
                    throw new JCACallException("GVVCL_JCA_PARSEROUTPUT_ERROR", new String[][]{{"exc", "" + e}}, e);
                }
            }
        }
        catch (Exception e) {
            logger.debug("Cannot convert to xml", e);
            throw new JCACallException("GVVCL_JCA_PARSEROUTPUT_ERROR", new String[][]{{"exc", "" + e}}, e);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    /**
     * Checks if the attribute has a valid value (not null and not empty).
     *
     * @exception InitializationException
     *            if the attribute has an invalid value.
     */
    private void checkAttribute(String attributeName, String attributeValue) throws InitializationException
    {
        if (attributeValue == null) {
            throw new InitializationException("GVVCL_NULL_PARAMETER_ERROR", new String[][]{{"param", attributeName}});
        }
        if (attributeValue.equals("")) {
            throw new InitializationException("GVVCL_NULL_PARAMETER_ERROR", new String[][]{{"param", attributeName}});
        }
    }

    /**
     * Initialize base parameters.
     *
     * @param node
     * @throws XMLConfigException
     * @throws InitializationException
     */
    private void initParameters(Node node) throws XMLConfigException, InitializationException
    {
        jndiNameEis = XMLConfig.get(node, "@jndi-name-eis");
        checkAttribute("jndi-name-eis", jndiNameEis);
        logger.debug("JNDI name..........: " + jndiNameEis);

        String execTypeStr = XMLConfig.get(node, "@exec-type");
        logger.debug("Execution type.....: " + execTypeStr);
        if (execTypeStr.equals("out_exec_in")) {
            execType = OUT_EXEC_IN;
        }
        else if (execTypeStr.equals("exec_in_out")) {
            execType = EXEC_IN_OUT;
        }
        else if (execTypeStr.equals("exec_in")) {
            execType = EXEC_IN;
        }
        else {
            throw new InitializationException("GVVCL_INVALID_VALUE_ERROR", new String[][]{{"param", "exec-type"},
                    {"value", execTypeStr}});
        }

        if (execType == EXEC_IN_OUT) {
            outputName = XMLConfig.get(node, "@output-name");
            checkAttribute("output-name", outputName);
            logger.debug("Output name........: " + outputName);

            String outputTypeStr = XMLConfig.get(node, "@output-type");
            logger.debug("Output type........: " + outputTypeStr);
            if (outputTypeStr.equals("mapped")) {
                outputType = OUT_MAPPED;
            }
            else if (outputTypeStr.equals("indexed")) {
                outputType = OUT_INDEXED;
            }
            else {
                throw new InitializationException("GVVCL_INVALID_VALUE_ERROR", new String[][]{{"param", "output-type"},
                        {"value", outputTypeStr}});
            }
        }

        namespacePrefix = XMLConfig.get(node, "@namespace-prefix", "jca");
        logger.debug("Namespace prefix...: " + namespacePrefix);
    }

    /**
     * Initialize ConnectionFactory and JCAXML object..
     *
     * @param node
     * @throws InitializationException
     */
    private void initConnectionFactory(Node node) throws InitializationException
    {
        JNDIHelper jndiHelper = null;
        try {
            jndiHelper = new JNDIHelper(XMLConfig.getNode(node, "JNDIHelper"));
            connectionFactory = (ConnectionFactory) jndiHelper.lookup(jndiNameEis);
            recordFactory = connectionFactory.getRecordFactory();
            jcaxml = new JCAXML(recordFactory);
            String byteArrayHandling = XMLConfig.get(node, "@byteArray-handling");
            if (byteArrayHandling != null) {
                if (byteArrayHandling.equals("xml")) {
                    jcaxml.byteArrayAsXML();
                }
                else if (byteArrayHandling.equals("string")) {
                    String encoding = XMLConfig.get(node, "@byteArray-encoding");
                    if (encoding != null) {
                        jcaxml.byteArrayAsString(encoding);
                    }
                    else {
                        jcaxml.byteArrayAsString();
                    }
                }
                else if (byteArrayHandling.equals("base64")) {
                    jcaxml.byteArrayAsEncode64();
                }
            }
            if (namespacePrefix != null) {
                jcaxml.setNSPrefix(namespacePrefix);
            }
        }
        catch (Exception exc) {
            logger.debug("Exception", exc);
            throw new InitializationException("GVVCL_INSTANTIATION_ERROR", new String[][]{
                    {"className", JCACallOperation.class.getName()}, {"exc", "" + exc}, {"key", "-"}}, exc);
        }
        finally {
            if (jndiHelper != null) {
                try {
                    jndiHelper.close();

                }
                catch (NamingException exc) {
                    logger.error("An error occurred while closing InitialContext.", exc);
                }
            }
        }
    }

    /**
     * Initializes the ConnectionSpec.
     *
     * @param node
     * @throws XMLConfigException
     * @throws InitializationException
     */
    private void initConnectionSpec(Node node) throws XMLConfigException, InitializationException
    {
        Node connectionSpecNode = XMLConfig.getNode(node, "jca-connection-spec");
        if (connectionSpecNode != null) {
            connectionSpec = (ConnectionSpec) createSpecObject(connectionSpecNode);
        }
    }

    /**
     * Initializes the InteractionSpec.
     *
     * @param node
     * @throws XMLConfigException
     * @throws InitializationException
     */
    private void initInteractionSpec(Node node) throws XMLConfigException, InitializationException
    {
        Node interactionSpecNode = XMLConfig.getNode(node, "jca-interaction-spec");
        interactionSpec = (InteractionSpec) createSpecObject(interactionSpecNode);
        try {
            String functionName = XMLConfig.get(interactionSpecNode, "@function-name");
            BeanInfo beanInfo = Introspector.getBeanInfo(interactionSpec.getClass());
            PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
            if (functionName != null) {
                checkAttribute("function-name", functionName);
                logger.debug("Function name......: " + functionName);
                setPropertyValue(interactionSpec, properties, "functionName", functionName);
            }
            String interactionVerb = XMLConfig.get(interactionSpecNode, "@interaction-verb");
            if (interactionVerb != null) {
                checkAttribute("interaction-verb", interactionVerb);
                logger.debug("Interaction verb...: " + interactionVerb);
                if (interactionVerb.equals("SYNC_SEND_RECEIVE")) {
                    interactionVerb = "" + InteractionSpec.SYNC_SEND_RECEIVE;
                }
                else if (interactionVerb.equals("SYNC_SEND")) {
                    interactionVerb = "" + InteractionSpec.SYNC_SEND;
                }
                else if (interactionVerb.equals("SYNC_RECEIVE")) {
                    interactionVerb = "" + InteractionSpec.SYNC_RECEIVE;
                }
                setPropertyValue(interactionSpec, properties, "interactionVerb", interactionVerb);
            }
            String executionTimeout = XMLConfig.get(interactionSpecNode, "@execution-timeout");
            if (executionTimeout != null) {
                checkAttribute("execution-timeout", executionTimeout);
                logger.debug("Execution timeout..: " + executionTimeout);
                setPropertyValue(interactionSpec, properties, "executionTimeout", executionTimeout);
            }
        }
        catch (Exception exc) {
            logger.debug("Exception", exc);
            throw new InitializationException("GVVCL_INSTANTIATION_ERROR", new String[][]{
                    {"className", interactionSpec.getClass().getName()}, {"exc", "" + exc}, {"key", "-"}}, exc);
        }
    }

    /**
     *
     * @param node
     */
    private void initXMLData(Node node) throws XMLConfigException
    {
        Node xmlData = XMLConfig.getNode(node, "xml-data");
        if (xmlData == null) {
            return;
        }

        NodeList stringToXmlList = XMLConfig.getNodeList(xmlData, "string-to-xml");
        int length = stringToXmlList.getLength();
        if (length != 0) {
            xpathXMLData = new ArrayList<String>(length);
            for (int i = 0; i < length; ++i) {
                Node stringToXmlNode = stringToXmlList.item(i);
                String xpath = XMLConfig.get(stringToXmlNode, "@xpath");
                xpathXMLData.add(xpath);
            }
        }

        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setValidating(false);
        try {
            documentBuilder = f.newDocumentBuilder();
        }
        catch (ParserConfigurationException exc) {
            logger.debug("Cannot instantiate a document builder", exc);
        }
    }

    /**
     *
     * @param node
     */
    private void initParamGVBuffer(Node node) throws XMLConfigException
    {
        xmlResultHandler = new XMLResultHandler();

        Node paramGVBuffer = XMLConfig.getNode(node, "param-gvbuffer");
        if (paramGVBuffer != null) {
            xmlResultHandler.init(paramGVBuffer);
        }
    }

    /**
     *
     * @param node
     *        nodo di configurazione
     * @return l'oggetto creato.
     */
    private Object createSpecObject(Node node) throws XMLConfigException, InitializationException
    {
        String className = XMLConfig.get(node, "@class");
        checkAttribute("class", className);
        logger.debug("Class name.........: " + className);

        try {
            Class<?> cls = Class.forName(className);
            Node constructorNode = XMLConfig.getNode(node, "spec-constructor");
            Object obj = null;
            if (constructorNode != null) {
                obj = createObjectUsingConstructor(cls, constructorNode);
            }
            else {
                obj = cls.newInstance();
            }

            BeanInfo beanInfo = Introspector.getBeanInfo(cls);
            PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();

            NodeList propertieNodes = XMLConfig.getNodeList(node, "spec-property");
            for (int i = 0; i < propertieNodes.getLength(); ++i) {
                Node propertyNode = propertieNodes.item(i);
                String name = XMLConfig.get(propertyNode, "@name");
                String value = XMLConfig.get(propertyNode, "@value");
                setPropertyValue(obj, properties, name, value);
            }

            return obj;
        }
        catch (Exception exc) {
            logger.debug("Exception", exc);
            throw new InitializationException("GVVCL_INSTANTIATION_ERROR", new String[][]{{"className", className},
                    {"exc", "" + exc}, {"key", "-"}}, exc);
        }
    }

    /**
     *
     * @param obj
     *        JavaBean
     * @param properties
     * @param name
     * @param value
     */
    private void setPropertyValue(Object obj, PropertyDescriptor[] properties, String name, String value)
            throws Exception
    {
        for (int i = 0; i < properties.length; ++i) {
            if (name.equals(properties[i].getName())) {
                logger.debug("Setting property " + name + ": " + value);
                Method setter = properties[i].getWriteMethod();
                Class<?> type = properties[i].getPropertyType();
                Object objectValue = cast(value, type);
                setter.invoke(obj, new Object[]{objectValue});
                return;
            }
        }
    }

    /**
     *
     * @param value
     * @param cls
     * @return
     */
    private Object cast(String value, Class<?> cls) throws Exception
    {
        if (cls.equals(Byte.TYPE)) {
            return new Byte(value);
        }
        else if (cls.equals(Boolean.TYPE)) {
            return new Boolean(value);
        }
        else if (cls.equals(Character.TYPE)) {
            return new Character(value.charAt(0));
        }
        else if (cls.equals(Double.TYPE)) {
            return new Double(value);
        }
        else if (cls.equals(Float.TYPE)) {
            return new Float(value);
        }
        else if (cls.equals(Integer.TYPE)) {
            return new Integer(value);
        }
        else if (cls.equals(Long.TYPE)) {
            return new Long(value);
        }
        else if (cls.equals(Short.TYPE)) {
            return new Short(value);
        }
        else if (cls.equals(Character.class)) {
            return new Character(value.charAt(0));
        }

        Class<?>[] types = {String.class};
        Constructor<?> constr = cls.getConstructor(types);
        Object[] params = {value};
        return constr.newInstance(params);
    }

    /**
     *
     * @param objectClass
     *        classe dell'oggetto da creare
     * @param node
     *        Descrive il costruttore
     * @return
     */
    private Object createObjectUsingConstructor(Class<?> objectClass, Node node) throws Exception
    {
        NodeList paramList = XMLConfig.getNodeList(node, "parameter");
        Class<?>[] types = new Class[paramList.getLength()];
        Object[] params = new Object[types.length];

        for (int i = 0; i < types.length; ++i) {
            Node paramNode = paramList.item(i);
            String type = XMLConfig.get(paramNode, "@type");
            String value = XMLConfig.get(paramNode, "@value");
            Class<?> cls = null;
            if (type.equals("byte")) {
                cls = Byte.TYPE;
            }
            else if (type.equals("boolean")) {
                cls = Boolean.TYPE;
            }
            else if (type.equals("char")) {
                cls = Character.TYPE;
            }
            else if (type.equals("double")) {
                cls = Double.TYPE;
            }
            else if (type.equals("float")) {
                cls = Float.TYPE;
            }
            else if (type.equals("int")) {
                cls = Integer.TYPE;
            }
            else if (type.equals("long")) {
                cls = Long.TYPE;
            }
            else if (type.equals("short")) {
                cls = Short.TYPE;
            }
            else if (type.equals("String")) {
                cls = String.class;
            }
            types[i] = cls;
            params[i] = cast(value, cls);
        }

        Constructor<?> constr = objectClass.getConstructor(types);
        return constr.newInstance(params);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * Return the alias for the given service
     *
     * @param gvBuffer
     *        the input service GVBuffer
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }
}
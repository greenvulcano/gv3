/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.xpath;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is visible only internally and is used to initialize the
 * configured XPath library.
 *
 */
class XPathAPIFactory {
    // --------------------------------------------------------------------------------------
    // CONSTANTS
    // --------------------------------------------------------------------------------------

    /**
     * Configuration file for the XPah configuration.
     */
    public static final String         XML_CONF                        = "xml.xml";
    public static final String         DEFAULT_NAMESPACE_FOR_FUNCTIONS = "urn:maxime/functions";

    // --------------------------------------------------------------------------------------
    // STATIC FIELDS
    // --------------------------------------------------------------------------------------

    /**
     * Single instance of the XPath implementation.
     */
    private static XPathAPIFactoryImpl _instance;

    // --------------------------------------------------------------------------------------
    // SINGLETON IMPLEMENTATION
    // --------------------------------------------------------------------------------------

    /**
     * Obtains the XPath implementation. The first time instatiate and
     * initialize the XPath implementation according with the configuration.
     *
     * @retun the Single instance of the XPath implementation.
     */
    public static XPathAPIFactoryImpl instance() {
        try {
            ClassLoader loader = XPathAPIFactoryImpl.class.getClassLoader();
            if (loader == null) {
                loader = ClassLoader.getSystemClassLoader();
            }

            if (_instance == null) {
                Node xpathConf = XMLConfig.getNode(XML_CONF, "/xml/xpath");
                String factoryClassName = XMLConfig.get(xpathConf, "@xpath-factory");

                System.out.println("Using XPath implementation: " + factoryClassName);

                _instance = (XPathAPIFactoryImpl) loader.loadClass(factoryClassName).newInstance();

                installConfiguredNamespaces(xpathConf);
                installNamespace("max", DEFAULT_NAMESPACE_FOR_FUNCTIONS);
                installConfiguredFunctions(loader, xpathConf);
            }

            return _instance;
        }
        catch (Exception exc) {
            System.out.println("ERROR: cannot instantiate the XPathFactoryImpl");
            exc.printStackTrace();
            return null;
        }
    }

    /**
     * Private constructor: this cass cannot be instantiated.
     */
    private XPathAPIFactory() {
    }

    /**
     * Install the configured XPath extensions.
     *
     * @param loader
     *            loader used to load the extension classes
     * @param confNode
     *            configuration node for the XPath library
     * @throws XMLConfigException
     */
    private static void installConfiguredFunctions(ClassLoader loader, Node confNode) throws XMLConfigException {
        NodeList xpathFunctions = XMLConfig.getNodeList(confNode, "xpath-extension");
        for (int i = 0; i < xpathFunctions.getLength(); ++i) {
            Node node = xpathFunctions.item(i);
            String functionName = XMLConfig.get(node, "@function-name");
            String className = XMLConfig.get(node, "@class");
            String namespace = XMLConfig.get(node, "@namespace", DEFAULT_NAMESPACE_FOR_FUNCTIONS);
            try {
                Class functionClass = loader.loadClass(className);
                XPathFunction function = (XPathFunction) functionClass.newInstance();
                XPathAPI.installFunction(namespace, functionName, function);
                System.out.println("### XPath function installed....: " + functionName + ", " + className + " ("
                        + namespace + ")");
            }
            catch (Exception e) {
                System.out.println("ERROR: cannot install XPath function: " + functionName + ", " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Install the configured XPath extensions.
     *
     * @param loader
     *            loader used to load the extension classes
     * @param confNode
     *            configuration node for the XPath library
     * @throws XMLConfigException
     */
    private static void installConfiguredNamespaces(Node confNode) throws XMLConfigException {
        NodeList xpathNamespaces = XMLConfig.getNodeList(confNode, "xpath-namespace");
        for (int i = 0; i < xpathNamespaces.getLength(); ++i) {
            Node node = xpathNamespaces.item(i);
            String prefix = XMLConfig.get(node, "@prefix");
            String namespace = XMLConfig.get(node, "@namespace", "");
            installNamespace(prefix, namespace);
        }
    }

    /**
     * @param prefix
     * @param namespace
     */
    private static void installNamespace(String prefix, String namespace) {
        try {
            XPathAPI.installNamespace(prefix, namespace);
            System.out.println("### XPath namespace installed...: " + prefix + " -> " + namespace);
        }
        catch (Exception e) {
            System.out.println("ERROR: cannot install namespace: " + prefix + "->" + namespace);
            e.printStackTrace();
        }
    }
}

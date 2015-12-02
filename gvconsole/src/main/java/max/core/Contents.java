/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
package max.core;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Apr 17, 2010
 * @author maxime
 *
 */
public class Contents
{
    /**
     *
     */
    private static final String CONTENTS_CONF = "contents.xml";

    private static Contents     _instance     = null;

    private Document            contents;

    /**
     * @return the singleton instance
     * @throws MaxException
     */
    public static synchronized Contents instance() throws MaxException
    {
        if (_instance == null) {
            _instance = new Contents();
        }

        return _instance;
    }

    /**
     * @throws MaxException
     *
     */
    protected Contents() throws MaxException
    {
        try {
            contents = XMLConfig.getDocument(CONTENTS_CONF, Contents.class.getClassLoader(), true, false);
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }
    }

    // /////////////////////////////////////////////////////////////////////

    /**
     * @param providerName
     * @return the new created content provider
     * @throws MaxException
     * @throws XMLConfigException
     */
    protected ContentProvider createProvider(String providerName) throws MaxException, XMLConfigException
    {
        String alias = XMLConfig.get(contents, "/contents/alias[@alias='" + providerName + "']/@provider");
        if (alias != null) {
            providerName = alias;
        }

        Node node = XMLConfig.getNode(contents, "/contents/provider[@name='" + providerName + "']");
        if (node == null) {
            throw new MaxException("No provider '" + providerName + "' defined");
        }

        node = XMLConfig.getNode(node, "*[@type='provider']");
        String className = XMLConfig.get(node, "@class");

        try {
            Class<?> cls = Class.forName(className);

            ContentProvider provider = (ContentProvider) cls.newInstance();
            provider.init(node);
            return provider;
        }
        catch (Exception exc) {
            throw new MaxException("" + exc, exc);
        }
    }

    /**
     * @param ruleName
     * @return the new created selection rule
     * @throws MaxException
     * @throws XMLConfigException
     */
    protected ContentSelectionRule createSelectionRule(String ruleName) throws MaxException, XMLConfigException
    {
        Node node = XMLConfig.getNode(contents, "/contents/selector[@name='" + ruleName + "']");
        if (node == null) {
            throw new MaxException("No selector '" + ruleName + "' defined");
        }

        node = XMLConfig.getNode(node, "*[@type='selector']");
        String className = XMLConfig.get(node, "@class");

        try {
            Class<?> cls = Class.forName(className);

            ContentSelectionRule selector = (ContentSelectionRule) cls.newInstance();
            selector.init(node);
            return selector;
        }
        catch (Exception exc) {
            throw new MaxException("" + exc, exc);
        }
    }

    // /////////////////////////////////////////////////////////////////////

    /**
     *
     */
    protected HashMap<String, ContentProvider> providers = new HashMap<String, ContentProvider>();

    /**
     * @param providerName
     * @return the content provider
     * @throws MaxException
     * @throws XMLConfigException
     */
    public synchronized ContentProvider getProvider(String providerName) throws MaxException, XMLConfigException
    {
        ContentProvider provider = (ContentProvider) providers.get(providerName);
        if (provider == null) {
            provider = createProvider(providerName);
            providers.put(providerName, provider);
        }

        return provider;
    }

    // /////////////////////////////////////////////////////////////////////

    /**
     *
     */
    protected HashMap<String, ContentSelectionRule> rules = new HashMap<String, ContentSelectionRule>();

    /**
     * @param ruleName
     * @return the selection rule
     * @throws MaxException
     * @throws XMLConfigException
     */
    public synchronized ContentSelectionRule getSelectionRule(String ruleName) throws MaxException, XMLConfigException
    {
        ContentSelectionRule rule = (ContentSelectionRule) rules.get(ruleName);
        if (rule == null) {
            rule = createSelectionRule(ruleName);
            rules.put(ruleName, rule);
        }

        return rule;
    }
}

/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
package max.xml;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import max.core.ContentProvider;
import max.core.Contents;
import max.core.MaxException;
import max.util.ObjectPool;
import max.util.ObjectPoolCreator;
import max.util.ObjectPoolException;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;

public class MaxXMLFactory
{
    // ---------------------------------------------------------------------------
    // FIELDS
    // ---------------------------------------------------------------------------

    private static MaxXMLFactory _instance = null;

    private Document             xmlConfDocument;

    // ---------------------------------------------------------------------------
    // INSTANCE METHOD
    // ---------------------------------------------------------------------------

    public static synchronized MaxXMLFactory instance() throws MaxException
    {
        if (_instance == null) {
            _instance = new MaxXMLFactory();
        }

        return _instance;
    }

    private MaxXMLFactory() throws MaxException
    {
        try {
            xmlConfDocument = XMLConfig.getDocument(XML_CONF, MaxXMLFactory.class.getClassLoader(), true, false);
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }
    }

    // /////////////////////////////////////////////////////////////
    // VARIABILI MEMBRO PER L'XML.
    // /////////////////////////////////////////////////////////////

    static final String     XML_CONF              = "xml.xml";
    static final String     INTERFACE_XSL         = "max/xml/interface.xsl";
    static final String     WARNINGS_XSL          = "max/xml/warnings.xsl";

    private ContentProvider contentProvider       = null;
    private EntityResolver  entityResolver        = null;
    private ObjectPool      interfaceTransformers = new ObjectPool(new ObjectPoolCreator() {
                                                      public Object create(String arg) throws ObjectPoolException
                                                      {
                                                          return createInterfaceTransformer();
                                                      }
                                                  }, null, 20, 0);
    private ObjectPool      warningsTransformers  = new ObjectPool(new ObjectPoolCreator() {
                                                      public Object create(String arg) throws ObjectPoolException
                                                      {
                                                          return createWarningsTransformer();
                                                      }
                                                  }, null, 20, 0);

    // /////////////////////////////////////////////////////////////
    // CONTENT PROVIDER.
    // /////////////////////////////////////////////////////////////

    /**
     * Il provider ritornato DEVE essere utilizzato in una sezione di codice
     * sincronizzato sull'oggetto ritornato.
     *
     * @return the ContentProvider object
     * @throws MaxException
     *
     * @throws XMLConfigException
     */
    public ContentProvider getContentProvider() throws MaxException, XMLConfigException
    {
        if (contentProvider == null) {
            String providerName = XMLConfig.get(xmlConfDocument, "/xml/@provider");
            contentProvider = Contents.instance().getProvider(providerName);
        }
        return contentProvider;
    }

    // /////////////////////////////////////////////////////////////
    // ENTITY RESOLVER.
    // /////////////////////////////////////////////////////////////

    /**
     * Restituisce l'<code>EntityResolver</code> utilizzato dalla intranet.
     *
     * @return the EntityResolver
     * @throws MaxException
     */
    public final EntityResolver getEntityResolver() throws MaxException
    {
        if (entityResolver == null) {
            try {
                String clsName = XMLConfig.get(xmlConfDocument,
                        "/xml/entity-resolver/*[@type='entity-resolver']/@class");

                Class cls = Class.forName(clsName);
                if (!EntityResolver.class.isAssignableFrom(cls)) {
                    throw new MaxException("" + cls + " is not a org.xml.sax.EntityResolver class");
                }

                entityResolver = (EntityResolver) cls.newInstance();
            }
            catch (Exception exc) {
                throw new MaxException(exc);
            }
        }
        return entityResolver;
    }

    // /////////////////////////////////////////////////////////////
    // TRANSFORMERS PER L'INTERFACCIA PER L'XML EDITOR.
    // /////////////////////////////////////////////////////////////

    public final Transformer getXMLEditorXSLT() throws MaxException, XMLConfigException
    {
        try {
            if (XMLConfig.get(xmlConfDocument, "/xml/stylesheet/@use-pool").equals("true")) {
                return (Transformer) interfaceTransformers.getObject();
            }
            else {
                return createInterfaceTransformer();
            }
        }
        catch (ObjectPoolException exc) {
            throw new MaxException(exc.getNestedException());
        }
    }

    public final Transformer getXMLWarningsXSLT() throws MaxException, XMLConfigException
    {
        try {
            if (XMLConfig.get(xmlConfDocument, "/xml/stylesheet/@use-pool").equals("true")) {
                return (Transformer) warningsTransformers.getObject();
            }
            else {
                return createWarningsTransformer();
            }
        }
        catch (ObjectPoolException exc) {
            throw new MaxException(exc.getNestedException());
        }
    }

    public final void releaseXMLEditorXSLT(Transformer transformer) throws MaxException
    {
        interfaceTransformers.releaseObject(transformer);
    }

    /**
     * @param transformer
     * @throws MaxException
     */
    public final void releaseXMLWarningsXSLT(Transformer transformer) throws MaxException
    {
        warningsTransformers.releaseObject(transformer);
    }

    /**
     * @return
     * @throws ObjectPoolException
     */
    protected final Transformer createInterfaceTransformer() throws ObjectPoolException
    {
        ClassLoader loader = getClass().getClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }

        try {
            InputStream istream = null;

            String xslFile;
            try {
                xslFile = XMLConfig.get(xmlConfDocument, "/xml/stylesheet/@stylesheet");
            }
            catch (XMLConfigException exc) {
                throw new ObjectPoolException(exc);
            }

            if (xslFile != null) {
                istream = loader.getResourceAsStream(xslFile);
                if (istream == null) {
                    istream = new FileInputStream(xslFile);
                }
            }
            else {
                istream = loader.getResourceAsStream(INTERFACE_XSL);
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer(new StreamSource(istream));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            istream.close();

            return transformer;
        }
        catch (IOException exc) {
            throw new ObjectPoolException(exc);
        }
        catch (TransformerConfigurationException exc) {
            throw new ObjectPoolException(exc);
        }
    }

    /**
     * @return
     * @throws ObjectPoolException
     */
    protected final Transformer createWarningsTransformer() throws ObjectPoolException
    {
        ClassLoader loader = getClass().getClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }

        try {
            InputStream istream = null;

            String xslFile;
            try {
                xslFile = XMLConfig.get(xmlConfDocument, "/xml/stylesheet/@warnings-stylesheet");
            }
            catch (XMLConfigException exc) {
                throw new ObjectPoolException(exc);
            }

            if (xslFile != null) {
                istream = loader.getResourceAsStream(xslFile);
                if (istream == null) {
                    istream = new FileInputStream(xslFile);
                }
            }
            else {
                istream = loader.getResourceAsStream(WARNINGS_XSL);
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer(new StreamSource(istream));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            istream.close();

            return transformer;
        }
        catch (IOException exc) {
            throw new ObjectPoolException(exc);
        }
        catch (TransformerConfigurationException exc) {
            throw new ObjectPoolException(exc);
        }
    }

    /**
     * @param systemId
     * @param file
     * @throws MaxException
     * @throws XMLConfigException
     */
    public void registerXSLT(String systemId, InputStream file) throws MaxException, XMLConfigException
    {
        ContentProvider provider = getContentProvider();
        synchronized (provider) {
            String category = XMLConfig.get(xmlConfDocument, "/xml/details/@category");
            if (provider.exists(category, systemId)) {
                provider.update(category, systemId, file);
            }
            else {
                provider.insert(category, systemId, file);
            }
        }
    }

    public void removeXSLT(String systemId) throws MaxException, XMLConfigException
    {
        ContentProvider provider = getContentProvider();
        synchronized (provider) {
            String category = XMLConfig.get(xmlConfDocument, "/xml/details/@category");
            provider.remove(category, systemId);
        }
    }

    public InputStream getXSLT(String systemId) throws MaxException, XMLConfigException
    {
        ContentProvider provider = getContentProvider();
        synchronized (provider) {
            String category = XMLConfig.get(xmlConfDocument, "/xml/details/@category");
            return provider.get(category, systemId);
        }
    }
}

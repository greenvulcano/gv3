/*
 * Creation date and time: 12-giu-2006 14.31.05
 *
 *
 */
package max.documents.factory;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import max.core.MaxException;
import max.util.StringTemplate;
import max.xml.MaxXMLFactory;
import max.xml.XMLBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author Sergio
 *
 *
 */
public class DocumentFactoryDescriptor {
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    private String             id;
    private String             label;

    private StringTemplate     filenameTemplate;

    private String             rootElement;
    private String             systemId;
    private String             publicId;
    private String             namespace;

    private MergingDescriptor  mergingDescriptor;
    private RegistryDescriptor registryDescriptor;

    // ----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    // ----------------------------------------------------------------------------------------------

    public DocumentFactoryDescriptor() {
    }

    // ----------------------------------------------------------------------------------------------
    // METHODS
    // ----------------------------------------------------------------------------------------------

    /**
     * Creates a document.
     */
    public Document createDocument() throws Exception {
        EntityResolver er = getEntityResolver();
        InputSource is = er.resolveEntity(publicId, systemId);
        XMLBuilder builder = null;
        if (is != null) {
            builder = new XMLBuilder(rootElement, is, publicId, systemId, namespace);
            Document document = builder.getDocument();
            builder.close();
            return document;
        }
        else {
            throw new Exception("Invalid fpi, uri and doctype");
        }
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @return Returns the filenameTemplate.
     */
    public StringTemplate getFilenameTemplate() {
        return filenameTemplate;
    }

    /**
     * @return Returns the rootElement.
     */
    public String getRootElement() {
        return rootElement;
    }

    /**
     * @return Returns the publicId.
     */
    public String getPublicId() {
        return publicId;
    }

    /**
     * @return Returns the systemId.
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * @return Returns the namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @return Returns the mergingDescriptor.
     */
    public MergingDescriptor getMergingDescriptor() {
        return mergingDescriptor;
    }

    /**
     * @return Returns the registryDescriptor.
     */
    public RegistryDescriptor getRegistryDescriptor() {
        return registryDescriptor;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    // ----------------------------------------------------------------------------------------------
    // PACKAGE METHODS
    // ----------------------------------------------------------------------------------------------

    void init(Node configuration) throws XMLConfigException {
        id = XMLConfig.get(configuration, "@id");
        label = XMLConfig.get(configuration, "@label");

        filenameTemplate = new StringTemplate(XMLConfig.get(configuration, "@filename-template"));

        rootElement = XMLConfig.get(configuration, "doctype/@root-element");
        namespace = XMLConfig.get(configuration, "doctype/@namespace");
        systemId = XMLConfig.get(configuration, "doctype/@systemId");
        publicId = XMLConfig.get(configuration, "doctype/@publicId");

        Node multiFileNode = XMLConfig.getNode(configuration, "multi-file-configuration");
        if (multiFileNode != null) {
            mergingDescriptor = new MergingDescriptor();
            mergingDescriptor.init(multiFileNode);
        }

        Node registryNode = XMLConfig.getNode(configuration, "registry");
        if (registryNode != null) {
            registryDescriptor = new RegistryDescriptor();
            registryDescriptor.init(registryNode);
        }
    }

    // ----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    // ----------------------------------------------------------------------------------------------

    private EntityResolver getEntityResolver() throws MaxException {
        MaxXMLFactory xmlFact = MaxXMLFactory.instance();
        return xmlFact.getEntityResolver();
    }
}

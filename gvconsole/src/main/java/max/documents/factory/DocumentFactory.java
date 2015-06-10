/*
 * Creation date and time: 12-giu-2006 12.48.46
 *
 *
 */
package max.documents.factory;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import max.util.StringTemplate;
import max.xml.DOMWriter;
import max.xml.MaxXMLFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <code>DocumentFactory</code> has following responsibility:
 * <ul>
 * <li>Creation of new empty documents. New created documents are initialized
 * with required elements, attributes and DOCTYPE clause.
 * <li>Registration of the created documents in the document registry.
 * <li>Registration of a <code>&lt;merging&gt;</code> element in a multi-files
 * configuration.
 * </ul>
 *
 * @author Sergio
 *
 */
public class DocumentFactory implements ConfigurationListener {
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    public static final String     CONFIGURATION = "documentFactory.xml";

    /**
     * Map(String id, DocumentFactoryDescriptor)
     */
    private Map                    factories;

    private File                   documentRegistryFile;

    // ----------------------------------------------------------------------------------------------
    // SINGLETON
    // ----------------------------------------------------------------------------------------------

    /**
     * Unique instance.
     */
    private static DocumentFactory _instance     = null;

    /**
     * Thread safe.
     *
     * @return the unique instance of the class.
     */
    public static synchronized DocumentFactory instance() throws XMLConfigException {
        if (_instance == null) {
            _instance = new DocumentFactory();
        }
        return _instance;
    }

    // ----------------------------------------------------------------------------------------------
    // CONSTRUCTOR
    // ----------------------------------------------------------------------------------------------

    /**
     * Private constructor, so no instance can be created other than the
     * singleton.
     *
     * @throws XMLConfigException
     */
    private DocumentFactory() throws XMLConfigException {
        init();
        XMLConfig.addConfigurationListener(this);
    }

    // ----------------------------------------------------------------------------------------------
    // METHODS
    // ----------------------------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     *
     * @seemax.config.ConfigurationListener#configurationChanged(max.config.
     * ConfigurationEvent)
     */
    public void configurationChanged(ConfigurationEvent evt) {
        if (evt.getCode() == ConfigurationEvent.EVT_FILE_LOADED) {
            if (evt.getFile().equals(CONFIGURATION)) {
                try {
                    init();
                }
                catch (XMLConfigException exc) {
                    exc.printStackTrace();
                }
            }
        }
    }

    /**
     * @return Set(String)
     */
    public synchronized Set getFactoryIdentifiers() {
        return Collections.unmodifiableSet(factories.keySet());
    }

    public synchronized DocumentFactoryDescriptor getDocumentFactoryDescriptor(String factoryId) {
        return (DocumentFactoryDescriptor) factories.get(factoryId);
    }

    /**
     * Determina il filename, l'ID per il document registry, la label, crea il
     * documento, lo salva, lo registra, aggiorna il file di configurazione
     * multi-file.
     *
     * @param factoryId
     * @return
     * @throws Exception
     */
    public Document createAndRegisterDocument(String factoryId) throws Exception {
        DocumentFactoryDescriptor descriptor = getDocumentFactoryDescriptor(factoryId);
        if (descriptor == null) {
            return null;
        }

        StringTemplate filenameTemplate = descriptor.getFilenameTemplate();

        RegistryDescriptor registryDescriptor = descriptor.getRegistryDescriptor();

        if (registryDescriptor != null) {
        }

        File path = null;
        Map values = new HashMap();

        int id = 0;
        while (true) {
            ++id;
            values.put("id", "" + id);
            String filename = filenameTemplate.substitute(values);
            path = new File(filename);
            if (!path.exists()) {
                if (registryDescriptor != null) {
                    if (registryDescriptor.canRegister(values)) {
                        break;
                    }
                }
                else {
                    break;
                }
            }
        }

        Document newCreatedDocument = createAndSaveDocument(factoryId, path);
        addFragment(factoryId, "file:/" + path.getAbsolutePath());
        registerDocument(factoryId, values, path);
        return newCreatedDocument;
    }

    /**
     * Create a Document given a factory identifier. The document is not
     * registered in the document registry.
     *
     * @param factoryId
     * @param path
     * @return
     */
    public Document createAndSaveDocument(String factoryId, File path) throws Exception {
        DocumentFactoryDescriptor descriptor = getDocumentFactoryDescriptor(factoryId);
        if (descriptor == null) {
            return null;
        }
        Document newCreatedDocument = descriptor.createDocument();

        if (newCreatedDocument != null) {
            writeXMLFile(newCreatedDocument, path);
        }

        return newCreatedDocument;
    }

    /**
     * Aggiunge un frammento ad una configurazione multi-file. Salva la nuova
     * configurazione multi-file.
     *
     * @param factoryId
     *            indica la porzione di configurazione da consultare per la
     *            gestione del nuovo frammento
     * @param fragmentFile
     * @throws Exception
     */
    public void addFragment(String factoryId, String fragmentFile) throws Exception {
        DocumentFactoryDescriptor descriptor = getDocumentFactoryDescriptor(factoryId);
        if (descriptor == null) {
            return;
        }
        MergingDescriptor mergingDescriptor = descriptor.getMergingDescriptor();
        if (mergingDescriptor == null) {
            return;
        }
        File multiFileConfigFile = new File(mergingDescriptor.getMultiFileConfiguration());
        Document multiFileConfig = readXMLFile(multiFileConfigFile);

        mergingDescriptor.addFragment(multiFileConfig, fragmentFile, multiFileConfigFile);

        writeXMLFile(multiFileConfig, multiFileConfigFile);
    }

    /**
     * Registra nel document registry un nuovo documento. Il nuovo documento �
     * configurato con un FileDocumentProxy.
     *
     * @param factoryId
     *            indica la porzione di configurazione da consultare per la
     *            gestione del nuovo frammento
     * @param values
     *            valori per l'instanziazione dei templates del nome e della
     *            label
     * @param path
     * @throws Exception
     */
    public void registerDocument(String factoryId, Map values, File path) throws Exception {
        DocumentFactoryDescriptor factoryDescriptor = getDocumentFactoryDescriptor(factoryId);
        if (factoryDescriptor == null) {
            return;
        }

        RegistryDescriptor registryDescriptor = factoryDescriptor.getRegistryDescriptor();
        if (registryDescriptor == null) {
            return;
        }

        Document documentRegistry = readXMLFile(documentRegistryFile);

        registryDescriptor.registerDocument(documentRegistry, values, path);

        writeXMLFile(documentRegistry, documentRegistryFile);
    }

    // ----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    // ----------------------------------------------------------------------------------------------

    private synchronized void init() throws XMLConfigException {
        factories = new HashMap();

        NodeList descriptorList = XMLConfig.getNodeList(CONFIGURATION, "/document-factory/document");
        for (int i = 0; i < descriptorList.getLength(); ++i) {
            Node descriptorNode = descriptorList.item(i);
            DocumentFactoryDescriptor descriptor = new DocumentFactoryDescriptor();
            descriptor.init(descriptorNode);
            factories.put(descriptor.getId(), descriptor);
        }

        documentRegistryFile = new File(XMLConfig.get(CONFIGURATION, "/document-factory/@document-registry"));
    }

    private Document readXMLFile(File file) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        documentBuilder.setEntityResolver(MaxXMLFactory.instance().getEntityResolver());
        return documentBuilder.parse(file);
    }

    private void writeXMLFile(Document document, File file) throws Exception {
        FileOutputStream outputStream = new FileOutputStream(file);
        DOMWriter writer = new DOMWriter();
        writer.write(document, outputStream);
        outputStream.flush();
        outputStream.close();
    }
}

/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
package max.xml;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import max.core.MaxException;
import max.documents.DocumentRepository;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Allows to make references between documents in the DocumentRepository.
 *
 */
public class DocumentValuesSelector extends ValuesSelector
{
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    /**
     * XPath per l'accesso al documento.
     */
    private String                                      xpath         = null;

    /**
     * Nome del documento referenziato.
     */
    private String                                      documentName  = null;

    /**
     * La cache viene ripulita dal garbage collector quando un documento non è
     * più in editing.
     *
     * Map[Document in-editing, Map[documentName, Document referenced]]
     */
    private static Map<Document, Map<String, Document>> documentCache = new WeakHashMap<Document, Map<String, Document>>();

    // ----------------------------------------------------------------------------------------------
    // CONSTRUCTOR
    // ----------------------------------------------------------------------------------------------

    /**
     * The parameter of the feature must be of the form
     * <code>file : xpath</code>
     */
    public DocumentValuesSelector(Feature feature)
    {
        super(feature);
        String parameter = feature.getParameter();
        int idx = parameter.indexOf(':');
        documentName = parameter.substring(0, idx).trim();
        xpath = parameter.substring(idx + 1).trim();
    }

    // ----------------------------------------------------------------------------------------------
    // METHODS
    // ----------------------------------------------------------------------------------------------

    @Override
    public List getValues(Node node, String currentValue)
    {
        List list = new LinkedList();
        list.add(currentValue);

        Document document = null;
        try {
            document = getReferencedDocument(node);
        }
        catch (MaxException e) {
            e.printStackTrace();
            return list;
        }

        NodeList nodeList;
        try {
            nodeList = XMLConfig.getNodeList(document, xpath);
        }
        catch (XMLConfigException exc) {
            exc.printStackTrace();
            return Collections.EMPTY_LIST;
        }

        int len = nodeList.getLength();

        for (int i = 0; i < len; ++i) {
            String val = XMLConfig.getNodeValue(nodeList.item(i));
            if (!val.equals(currentValue)) {
                list.add(val);
            }
        }

        return list;
    }

    // ----------------------------------------------------------------------------------------------
    // METHODS - cache dei documenti
    // ----------------------------------------------------------------------------------------------

    /**
     * Ottiene il documento referenziato.
     *
     * @param node
     *        nodo del documento in editing.
     */
    private Document getReferencedDocument(Node node) throws MaxException
    {
        // Ottiene la mappa dei documenti referenziati
        //
        Document editingDocument = node.getOwnerDocument();
        Map<String, Document> documentMap = documentCache.get(editingDocument);
        if (documentMap == null) {
            documentMap = new HashMap<String, Document>();
            documentCache.put(editingDocument, documentMap);
        }

        // Ottiene il documento cercato
        //
        Document document = (Document) documentMap.get(documentName);
        if (document != null) {
            return document;
        }

        try {
            DocumentRepository repository = DocumentRepository.instance();
            document = repository.getDocument(documentName);
            documentMap.put(documentName, document);
            return document;
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see max.xml.ValuesSelector#fillXPaths(java.util.Set)
     */
    @Override
    public void fillXPaths(Set xpaths)
    {
        if (xpath != null) {
            xpaths.add(xpath);
        }
    }
}
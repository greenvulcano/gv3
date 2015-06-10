/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
package max.documents;

import it.greenvulcano.configuration.XMLConfigException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.transform.TransformerException;

import max.core.MaxException;
import max.xpath.XPathFunction;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Allows to reference DocumentRegistry documents within the XPath expressions.
 *
 */
public class MaxDocumentXPathFunction implements XPathFunction
{

    private static List EMPTY_LIST    = new ArrayList();

    /**
     * La cache viene ripulita dal garbage collector quando un documento non �
     * pi� in editing.
     *
     * Map[Document in-editing, Map[documentName, Document referenced]]
     */

    private static Map  documentCache = Collections.synchronizedMap(new WeakHashMap());
    /**
     * Nome del documento correntemente in editing. La mappa viene ripulita dal
     * garbage collector quando un documento non � pi� in editing.
     *
     * Map[Document in-editing, String]
     */
    private static Map  documentName  = Collections.synchronizedMap(new WeakHashMap());

    public MaxDocumentXPathFunction()
    {
    }

    public static void clearCacheForDocument(Document document)
    {
        documentCache.remove(document);
    }

    public static void setDocumentName(Document document, String name)
    {
        documentName.put(document, name);
    }

    public static String getDocumentName(Document document)
    {
        return (String) documentName.get(document);
    }

    public static void clearCache()
    {
        documentCache.clear();
    }

    public Object evaluate(Node contextNode, Object[] params) throws TransformerException
    {
        String documentName = params[0].toString();

        Object documents = null;
        try {
            try {
                documents = getReferencedDocuments(documentName, contextNode);
            }
            catch (XMLConfigException exc) {
                throw new TransformerException(exc);
            }
        }
        catch (MaxException e) {
            e.printStackTrace();
            return EMPTY_LIST;
        }

        return documents;
    }

    /**
     * Ottiene il documento referenziato.
     *
     * @param documentName
     *        nome del documento da prelevare dal DocumentRegistry
     * @param node
     *        nodo del documento in editing.
     * @throws XMLConfigException
     */
    private Object getReferencedDocuments(String documentName, Node node) throws MaxException, XMLConfigException
    {
        // Ottiene la mappa dei documenti referenziati
        //
        Document editingDocument = node.getOwnerDocument();
        Map documentMap = (Map) documentCache.get(editingDocument);
        if (documentMap == null) {
            documentMap = new HashMap();
            documentCache.put(editingDocument, documentMap);
        }

        // Ottiene il documento cercato
        //

        Object documents = documentMap.get(documentName);
        if (documents != null) {
            return documents;
        }

        DocumentRepository repository = DocumentRepository.instance();

        Map documentsMap = repository.getDocuments(documentName);
        //documentsMap.remove(getDocumentName(editingDocument));
        Collection values = documentsMap.values();

        switch (values.size()) {
            case 0 :
                documents = null;
                break;

            case 1 :
                documents = values.iterator().next();
                break;

            default :
                documents = values;
                break;
        }

        documentMap.put(documentName, documents);

        return documents;

    }
}

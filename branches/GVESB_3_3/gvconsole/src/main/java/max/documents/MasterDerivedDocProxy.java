/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.net.URL;

import max.core.MaxException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Maxime Informatica s.n.c. -
 */
public class MasterDerivedDocProxy implements DocumentProxy {
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    /**
     * DocumentProxy for the master document.
     */
    private DocumentProxy     masterDocumentProxy;

    private DerivedDocument[] derivedDocuments;

    // ----------------------------------------------------------------------------------------------
    // DocumentProxy interface
    // ----------------------------------------------------------------------------------------------

    /**
     * @see max.documents.DocumentProxy#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws MaxException {
        Node masterDocumentNode;
        String masterDocProxyClassName;
        try {
            masterDocumentNode = XMLConfig.getNode(node, "*[@type='proxy']");
            masterDocProxyClassName = XMLConfig.get(masterDocumentNode, "@class");
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }
        Class masterDocProxyClass = null;
        try {
            masterDocProxyClass = Class.forName(masterDocProxyClassName);
            masterDocumentProxy = (DocumentProxy) masterDocProxyClass.newInstance();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new MaxException("Cannot instantiate the master document proxy", e);
        }
        catch (InstantiationException e) {
            e.printStackTrace();
            throw new MaxException("Cannot instantiate the master document proxy", e);
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new MaxException("Cannot instantiate the master document proxy", e);
        }

        masterDocumentProxy.init(masterDocumentNode);

        // lettura dei derived
        //
        NodeList list;
        try {
            list = XMLConfig.getNodeList(node, "derived");
            derivedDocuments = new DerivedDocument[list.getLength()];

            for (int i = 0; i < derivedDocuments.length; ++i) {
                DerivedDocument derived = new DerivedDocument();
                derived.init(list.item(i));
                derived.setMasterURL(getURL());
                derivedDocuments[i] = derived;
            }
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see max.documents.DocumentProxy#getURL()
     */
    public URL getURL() {
        return masterDocumentProxy.getURL();
    }

    /**
     * @see max.documents.DocumentProxy#load()
     */
    public Document load() throws MaxException {
        Document document = masterDocumentProxy.load();
        return document;
    }

    /**
     * @see max.documents.DocumentProxy#save(org.w3c.dom.Document)
     */
    public void save(Document document) throws MaxException {
        masterDocumentProxy.save(document);
        for (int i = 0; i < derivedDocuments.length; ++i) {
            try {
                derivedDocuments[i].save(document);
            }
            catch (MaxException exc) {
                throw new MaxException("Cannot save correctly the derived document '"
                        + derivedDocuments[i].getDocumentName() + "'", exc);
            }
        }
    }
}

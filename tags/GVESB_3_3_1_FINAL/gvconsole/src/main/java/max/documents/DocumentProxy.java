/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.documents;

import java.net.URL;

import max.core.MaxException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This interface specifies methods for read and write documents.
 * This interface provides pluggability to the document management system.<p>
 *
 */
public interface DocumentProxy {
    /**
     * It Loads a Document
     *
     * @return the loaded Document.
     */
    public Document load() throws MaxException;

    /**
     * It saves a Document.
     */
    public void save(Document document) throws MaxException;

    /**
     * It Starts a Document.
     */
    public void init(Node node) throws MaxException;

    /**
    * Restituisce l'URL per l'accesso al documento a cui questo proxy si riferisce.
    * Non tutti i proxy possono individuare un documento tramite l'URL, in tal caso
    * sar� ritornato <code>null</code>.
    *
    * @return l'URL per l'accesso al documento o <code>null</code> se non � possibile
    *      ottenere l'URL.
    */
    public URL getURL();
}
/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
package max.documents;

import java.io.InputStream;

import max.core.MaxException;

import org.w3c.dom.Node;

/**
 * @author Maxime Informatica snc. -
 */
public interface DerivedDocumentProxy
{
    /**
     * @param node
     * @throws MaxException
     */
    public void init(Node node) throws MaxException;

    /**
     * Questo metodo deve salvare il documento. Il documento da salvare �
     * ottenuto leggendo l'input stream.
     *
     * @param document
     * @throws MaxException
     */
    public void save(InputStream document) throws MaxException;
}

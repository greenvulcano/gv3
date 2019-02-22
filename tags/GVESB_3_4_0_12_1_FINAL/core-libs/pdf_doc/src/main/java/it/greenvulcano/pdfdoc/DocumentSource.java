/*
 * Copyright (c) 2004 Maxime Informatica s.n.c. - All right reserved
 *
 */
package it.greenvulcano.pdfdoc;

import org.w3c.dom.Node;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2003 - All right reserved
 */
public interface DocumentSource
{
    /**
     * @param node
     * @throws Exception
     */
    void init(Node node) throws Exception;

    /**
     * @throws Exception
     */
    Node getFop() throws Exception;
}
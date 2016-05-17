/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.documents;

import max.core.MaxException;
import max.xml.Check;

import org.w3c.dom.Node;

/**
 * Is possible that semantic checks must be applied to the document.
 * Semantic and syntactic checks will performed by classes
 * implementing CheckContraints interface.
 *
 */
public interface CheckConstraints extends Check {
    /**
     * Inizializza il CheckConstraints
     */
    public void init(Node node) throws MaxException;
}

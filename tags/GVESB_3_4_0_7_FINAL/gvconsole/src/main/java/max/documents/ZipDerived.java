/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

import java.io.File;
import java.io.InputStream;

import max.core.MaxException;
import max.util.ZipFileEditor;

import org.w3c.dom.Node;

/**
 * @author Maxime Informatica s.n.c. -
 */
public class ZipDerived implements DerivedDocumentProxy {
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------
    private File        path;
    private String      entry;

    // ----------------------------------------------------------------------------------------------
    // DerivedDocumentProxy interface
    // ----------------------------------------------------------------------------------------------

    /**
     * @see max.documents.DerivedDocumentProxy#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws MaxException {

        try {
            String pathStr = XMLConfig.get(node, "@path");
            if (!PropertiesHandler.isExpanded(pathStr)) {
                try {
                    pathStr = PropertiesHandler.expand(pathStr, null);
                }
                catch (PropertiesHandlerException exc) {
                    exc.printStackTrace();
                }
            }
            path = new File(pathStr);
            entry = XMLConfig.get(node, "@entry");
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }
    }

    /**
     * @see max.documents.DerivedDocumentProxy#save(java.io.InputStream)
     */
    public void save(InputStream document) throws MaxException {
        try {
            ZipFileEditor zipFileEditor = new ZipFileEditor(path);
            zipFileEditor.setEntry(entry, document);
            zipFileEditor.commit();
        }
        catch (Exception ex) {
            throw new MaxException("Zip: " + path + ", entry: " + entry, ex);
        }
    }

}

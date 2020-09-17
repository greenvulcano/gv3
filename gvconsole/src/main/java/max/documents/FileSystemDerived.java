/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import max.core.MaxException;

import org.w3c.dom.Node;

/**
 * @author Maxime Informatica s.n.c. -
 */
public class FileSystemDerived implements DerivedDocumentProxy {
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    private String path;

    // ----------------------------------------------------------------------------------------------
    // DerivedDocumentProxy interface
    // ----------------------------------------------------------------------------------------------

    /**
     * @see max.documents.DerivedDocumentProxy#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws MaxException {
        try {
            path = XMLConfig.get(node, "@path");
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
            FileOutputStream stream = new FileOutputStream(path);
            BufferedInputStream in = new BufferedInputStream(document);
            BufferedOutputStream out = new BufferedOutputStream(stream);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
            out.close();
            in.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new MaxException("Cannot write to " + path, e);
        }
    }

}

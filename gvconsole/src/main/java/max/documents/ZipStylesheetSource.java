/*
 * Created on 30-Sep-2004
 *
 */
package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import max.core.MaxException;

import org.w3c.dom.Node;

/**
 * @author Maxime Informatica s.n.c. -
 */
public class ZipStylesheetSource extends StylesheetSource {
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    private File   path;
    private String entry;

    // ----------------------------------------------------------------------------------------------
    // StylesheetSource interface
    // ----------------------------------------------------------------------------------------------

    /**
     * @throws XMLConfigException
     * @see max.documents.StylesheetSource#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws MaxException, XMLConfigException {
        path = new File(XMLConfig.get(node, "@path"));
        entry = XMLConfig.get(node, "@entry");
        super.init(node);
    }

    /**
     * @see max.documents.StylesheetSource#loadInternal()
     */
    @Override
    protected Transformer loadInternal() throws MaxException {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();

            JarFile jarFile = new JarFile(path);
            ZipEntry zipEntry = jarFile.getEntry(entry);
            InputStream istream = jarFile.getInputStream(zipEntry);

            StreamSource source = new StreamSource(istream);

            Transformer transformer = factory.newTransformer(source);
            return transformer;
        }
        catch (TransformerConfigurationException e) {
            e.printStackTrace();
            throw new MaxException("Cannot load the transformer: path=" + path + ", entry=" + entry, e);
        }
        catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
            throw new MaxException("Cannot load the transformer: path=" + path + ", entry=" + entry + ". Cause: "
                    + e.getMessage());
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new MaxException("Cannot load the transformer: path=" + path + ", entry=" + entry, e);
        }
    }

}

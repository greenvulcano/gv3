/*
 * Creation date and time: 19-giu-2006 12.18.55
 *
 *
 */
package max.documents.factory;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Sergio
 *
 *
 */
public class MergingDescriptor {
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    /**
     * Lista di coppie di XPath utilizzate per il merging dei documenti XML.
     * <p/>
     * List(String[])
     */
    private ArrayList mergingXPathList;

    private String    multiFileConfiguration;

    // ----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    // ----------------------------------------------------------------------------------------------

    public MergingDescriptor() {
    }

    // ----------------------------------------------------------------------------------------------
    // METHODS
    // ----------------------------------------------------------------------------------------------

    /**
     * @return Returns the multiFileConfiguration.
     */
    public String getMultiFileConfiguration() {
        return multiFileConfiguration;
    }

    /**
     * Aggiunge un frammento ad una configurazione multi-file.
     *
     * @param multiFileConfig
     *            file di configurazione multi-file
     * @param fragmentFile
     * @param used
     *            for diagnostic purpose
     * @throws Exception
     */
    public void addFragment(Document multiFileConfig, String fragmentFile, File file) throws Exception {
        Element root = multiFileConfig.getDocumentElement();
        if (!XMLConfig.CONFIG_NS.equals(root.getNamespaceURI()) || !root.getNodeName().equals("config")) {
            throw new Exception("The file " + file.getAbsoluteFile() + " is not a multi-file configuration");
        }

        Element documentElement = multiFileConfig.createElementNS(XMLConfig.CONFIG_NS, "document");
        documentElement.setAttribute("document", fragmentFile);
        root.appendChild(documentElement);

        for (Iterator its = mergingXPathList.iterator(); its.hasNext();) {
            String[] mergingXPaths = (String[]) its.next();
            String srcXPath = mergingXPaths[0];
            String destXPath = mergingXPaths[1];
            Element mergingElement = multiFileConfig.createElementNS(XMLConfig.CONFIG_NS, "merging");
            mergingElement.setAttribute("src", srcXPath);
            mergingElement.setAttribute("dest", destXPath);
            documentElement.appendChild(mergingElement);
        }
    }

    // ----------------------------------------------------------------------------------------------
    // PACKAGE METHODS
    // ----------------------------------------------------------------------------------------------

    void init(Node configuration) throws XMLConfigException {
        multiFileConfiguration = XMLConfig.get(configuration, "@configuration");
        mergingXPathList = new ArrayList();

        NodeList mergingNodes = XMLConfig.getNodeList(configuration, "merging");
        for (int i = 0; i < mergingNodes.getLength(); ++i) {
            Node mergingNode = mergingNodes.item(i);
            String srcXPath = XMLConfig.get(mergingNode, "@src");
            String destXPath = XMLConfig.get(mergingNode, "@dest");
            String[] mergingXPaths = new String[] { srcXPath, destXPath };
            mergingXPathList.add(mergingXPaths);
        }

        mergingXPathList.trimToSize();
    }

    // ----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    // ----------------------------------------------------------------------------------------------
}

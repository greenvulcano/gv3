/*
 * Creation date and time: 19-giu-2006 13.56.38
 *
 *
 */
package max.documents.factory;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import max.documents.DocumentDescriptor;
import max.documents.DocumentRepository;
import max.documents.FileSystemDocProxy;
import max.util.StringTemplate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Sergio
 *
 *
 */
public class RegistryDescriptor {
    // ----------------------------------------------------------------------------------------------
    // FIELDS
    // ----------------------------------------------------------------------------------------------

    private String         group;
    private StringTemplate nameTemplate;
    private StringTemplate labelTemplate;
    private String         description;
    /**
     * List(String[])
     */
    private ArrayList      roleList;

    // ----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    // ----------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------
    // METHODS
    // ----------------------------------------------------------------------------------------------

    /**
     * Controlla che i templates instanziati con i dati valori non provocano
     * conflitti con il contenuto del registry.
     *
     * @return true se i valori non provocano confitti, false altrimenti.
     */
    public boolean canRegister(Map values) throws Exception {
        String name = nameTemplate.substitute(values);
        DocumentDescriptor descriptor = DocumentRepository.instance().getDocumentDescriptor(name);
        return descriptor == null;
    }

    /**
     * Registra nel document registry un nuovo documento. Il nuovo documento �
     * configurato con un FileDocumentProxy.
     *
     * @param documentRegistry
     *            DOM del document registry
     * @param values
     *            valori per l'instanziazione dei templates della label e del
     *            nome
     * @param path
     * @throws Exception
     */
    public void registerDocument(Document documentRegistry, Map values, File path) throws Exception {
        Node groupNode = XMLConfig.getNode(documentRegistry, "//group[@name='" + group + "']");

        Element documentElement = documentRegistry.createElement("document");
        documentElement.setAttribute("name", nameTemplate.substitute(values));
        documentElement.setAttribute("label", labelTemplate.substitute(values));
        groupNode.appendChild(documentElement);

        Element proxyElement = documentRegistry.createElement("FileSystemProxy");
        proxyElement.setAttribute("type", "proxy");
        proxyElement.setAttribute("class", FileSystemDocProxy.class.getName());
        proxyElement.setAttribute("path", path.getAbsolutePath());
        documentElement.appendChild(proxyElement);

        if (description != null) {
            Element descriptionElement = documentRegistry.createElement("description");
            descriptionElement.appendChild(documentRegistry.createTextNode(description));
            documentElement.appendChild(descriptionElement);
        }

        for (Iterator it = roleList.iterator(); it.hasNext();) {
            String[] roleDef = (String[]) it.next();
            String name = roleDef[0];
            String access = roleDef[1];
            Element roleElement = documentRegistry.createElement("role");
            roleElement.setAttribute("name", name);
            roleElement.setAttribute("access", access);
            documentElement.appendChild(roleElement);
        }
    }

    // ----------------------------------------------------------------------------------------------
    // PACKAGE METHODS
    // ----------------------------------------------------------------------------------------------

    void init(Node configuration) throws XMLConfigException {
        description = XMLConfig.get(configuration, "description", "");
        group = XMLConfig.get(configuration, "@group");
        labelTemplate = new StringTemplate(XMLConfig.get(configuration, "@label-template"));
        nameTemplate = new StringTemplate(XMLConfig.get(configuration, "@name-template"));

        roleList = new ArrayList();

        NodeList roleNodes = XMLConfig.getNodeList(configuration, "role");
        for (int i = 0; i < roleNodes.getLength(); ++i) {
            Node roleNode = roleNodes.item(i);
            String name = XMLConfig.get(roleNode, "@name");
            String access = XMLConfig.get(roleNode, "@access");
            String[] roleDef = new String[] { name, access };
            roleList.add(roleDef);
        }

        roleList.trimToSize();
    }

    // ----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    // ----------------------------------------------------------------------------------------------
}

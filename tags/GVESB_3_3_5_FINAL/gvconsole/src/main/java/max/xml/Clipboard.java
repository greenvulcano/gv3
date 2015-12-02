/*
 * Creation date and time: 28-lug-2006 11.57.04
 *
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/Clipboard.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $
 */
package max.xml;

import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;

/**
 * @author Sergio
 *
 * <code>$Id: Clipboard.java,v 1.1 2010-04-03 15:28:50 nlariviera Exp $</code>
 */
public class Clipboard
{
    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    public static final String SESSION_ATTRIBUTE = Clipboard.class.getName();

    private static Document    clipboardDocument;

    private String             systemID;
    private String             publicID;
    private Node               copy;

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    //----------------------------------------------------------------------------------------------

    private Clipboard() throws Exception
    {
        synchronized (Clipboard.class) {
            if (clipboardDocument == null) {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                clipboardDocument = documentBuilder.newDocument();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    public static Clipboard getClipboard(HttpSession session) throws Exception
    {
        Clipboard clipboard = (Clipboard) session.getAttribute(SESSION_ATTRIBUTE);
        if (clipboard == null) {
            clipboard = new Clipboard();
            session.setAttribute(SESSION_ATTRIBUTE, clipboard);
        }
        return clipboard;
    }

    public void copy(Node node)
    {
        synchronized (clipboardDocument) {
            copy = clipboardDocument.importNode(node, true);
            Document document = getOwnerDocument(node);
            publicID = null;
            systemID = null;
            if (document != null) {
                DocumentType documentType = document.getDoctype();
                if (documentType != null) {
                    publicID = documentType.getPublicId();
                    systemID = documentType.getSystemId();
                }
            }
        }
    }

    public Node paste(Document destinationDocument)
    {
        synchronized (clipboardDocument) {
            return destinationDocument.importNode(copy, true);
        }
    }

    public void clear()
    {
        copy = null;
        publicID = null;
        systemID = null;
    }

    /**
     * Controlla che il contenuto della clipboard sia compatibile con il
     * documento dato.<br/>
     * Il contenuto della clipboard � compatibile se:
     * <ul>
     *  <li>Il nome le nodo contenuto nella clipboard � uguale al nome specificato
     *  <li>I publicID del documento e quello della clipboard coincidono (oppure sono entrambi null)
     *  <li>Il systemID del documento e quello della clipboard coincidono (oppure sono entrambi null)
     * </ul>
     *
     * @param nodeName
     * @param destinationDocument
     * @return
     */
    public boolean isCompatible(String nodeName, Document destinationDocument)
    {
        if (copy == null) {
            return false;
        }
        if (destinationDocument == null) {
            return false;
        }

        String copyName = copy.getNodeName();
        if (!eq(copyName, nodeName)) {
            return false;
        }

        String destPublicID = null;
        String destSystemID = null;
        DocumentType documentType = destinationDocument.getDoctype();
        if (documentType != null) {
            destPublicID = documentType.getPublicId();
            destSystemID = documentType.getSystemId();
        }

        return eq(destPublicID, publicID) && eq(destSystemID, systemID);
    }

    //----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    //----------------------------------------------------------------------------------------------

    private Document getOwnerDocument(Node node)
    {
        if (node == null) {
            return null;
        }

        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            return (Document) node;
        }

        return node.getOwnerDocument();
    }

    private boolean eq(String a, String b)
    {
        if (a == null) {
            return b == null;
        }
        else {
            return a.equals(b);
        }
    }
}

package max.xml;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.gvconsole.gvcon.xquery.XQueryProcessorAction;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.crypto.CryptoHelper;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import max.core.ContentProvider;
import max.core.MaxException;
import max.documents.MaxDocumentXPathFunction;
import max.search.SearchCurrentDocumentAction;
import max.util.MaxConsole;
import max.xpath.XPath;
import max.xpath.XPathAPI;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The <code>XMLBuilder</code> manages all aspects involving the XML editing.
 * 
 */
public class XMLBuilder
{

    private static final Logger    logger                   = GVLogger.getLogger(XMLBuilder.class);

    protected DocumentModel        documentModel;

    protected Document             document;

    private Element                currentElement;

    protected Object               newNode                  = null;

    protected Object               anchorNode               = null;

    private ContentModelInstance   currentContentModelInstance;

    private Vector                 attributeOperations      = null;

    private LinkedHashMap          menuActions              = new LinkedHashMap();

    private Transformer            detailTransformer        = null;

    private DocumentBuilderFactory factory                  = null;

    private DocumentBuilder        documentBuilder          = null;

    protected ElementIndex         index                    = new ElementIndex();

    protected WarningManager       warningManager           = new WarningManager(index, this);

    protected DTreeManager         dtreeManager             = new DTreeManager();

    private boolean                autoCheck                = false;

    private boolean                needsCheck               = true;

    private boolean                readOnly                 = false;

    private boolean                graphicMode              = false;

    /**
     * Contains new created elements that have #Freezed attributes. The elements
     * in this Set have its #Freezed attributes writable.
     */
    private Set                    unlockedElements         = new HashSet();

    /**
     * Contains elements that have #Counter attributes. This List is built
     * during element creation. After the element is joined to the document, the
     * resolveCounters() method must be called.
     */
    private List                   elementsWithCounters     = new LinkedList();

    /**
     * Contains elements with content model ANY with errors and corresponding
     * errors.
     */
    private Map                    wrongANYelements         = new HashMap();

    /**
     * Contains elements that does not match the DTD. Map[Element, Warning]
     */

    private Map                    invalidStructureElements = new HashMap();

    /**
     * Object to use in order to calculate the XPATH statements.
     */
    protected XPathAPI             xpathAPI                 = new XPathAPI();

    protected Clipboard            clipboard;

    protected static int           keyGenerator             = 0;

    public static class Operation
    {
        public static final int      T_DELETE     = 0;

        public static final int      T_SELECT     = 1;

        public static final int      T_SELECT_P   = 2;

        public static final int      T_CHANGE     = 3;

        public static final int      T_INSERT_B   = 4;    // insert before

        public static final int      T_INSERT_A   = 5;    // insert after

        public static final int      T_EDIT       = 6;

        public static final int      T_EDIT_ANY   = 7;    // edit a node with

        // content type ANY

        public static final int      T_ATT_EDIT   = 8;    // edit an attribute

        public static final int      T_ATT_FIXED  = 9;

        public static final int      T_ATT_ADD    = 10;

        public static final int      T_ATT_REMOVE = 11;

        public static final int      T_COPY       = 12;

        public static final int      T_CUT        = 13;

        public static final int      T_PASTE_A    = 14;

        public static final int      T_PASTE_B    = 15;

        public static final int      T_PASTE      = 16;

        private String               operationKey;

        private int                  operationType;

        private ContentModelInstance contentModelInstance;

        private ContentModel         contentModel;

        private Node                 node;

        public String getKey()
        {
            return operationKey;
        }

        public int getType()
        {
            return operationType;
        }

        public ContentModelInstance getContentModelInstance()
        {
            return contentModelInstance;
        }

        public ContentModel getContentModel()
        {
            return contentModel;
        }

        public Node getNode()
        {
            return node;
        }

        public Operation(int type, Node nd, ContentModel cm, ContentModelInstance cmi)
        {
            node = nd;
            operationType = type;
            contentModelInstance = cmi;
            contentModel = cm;
            operationKey = "" + (++keyGenerator);
        }

        @Override
        public String toString()
        {
            String op = null;
            switch (operationType) {
                case T_DELETE :
                    op = "delete";
                    break;

                case T_SELECT :
                    op = "Select(" + node.getNodeName() + ")";
                    break;

                case T_COPY :
                    op = "Copy(" + node.getNodeName() + ")";
                    break;

                case T_CUT :
                    op = "Cut(" + node.getNodeName() + ")";
                    break;

                case T_SELECT_P :
                    op = "SelectParent(" + node.getNodeName() + ")";
                    break;

                case T_CHANGE :
                    op = "change to \"" + contentModel + "\"";
                    break;

                case T_PASTE :
                    op = "replace with \"" + contentModel + "\" in the clipboard";
                    break;

                case T_INSERT_B :
                    op = "insert before \"" + contentModel + "\"";
                    break;

                case T_INSERT_A :
                    op = "insert after \"" + contentModel + "\"";
                    break;

                case T_PASTE_B :
                    op = "paste before \"" + contentModel + "\"";
                    break;

                case T_PASTE_A :
                    op = "paste after \"" + contentModel + "\"";
                    break;

                case T_EDIT :
                    op = "edit";
                    break;

                case T_ATT_EDIT :
                    op = "edit attr '" + node.getNodeName() + "'";
                    break;

                case T_ATT_REMOVE :
                    op = "delete attr '" + node.getNodeName() + "'";
                    break;

                case T_ATT_FIXED :
                    op = "ViewAttr(" + node.getNodeName() + ")";
                    break;

                case T_ATT_ADD :
                    op = "add attr '" + node.getNodeName() + "'";
                    break;

                default :
                    op = "?" + operationType + "?";
                    break;
            }
            return op + ":" + operationKey;
        }
    };

    public class ExecOperation
    {
        public Node    deleted[];

        public Node    inserted[];

        public Node    reference;

        public Element current;

        public String  type;

        public String  description;

        public ExecOperation(String type, String description, NodeList deleted, NodeList inserted, Node reference,
                Element current)
        {
            this.type = type;
            this.deleted = arr(deleted);
            this.inserted = arr(inserted);
            this.reference = reference;
            this.current = current;
            this.description = description;
        }

        public ExecOperation(String type, String description, NodeList deleted, Node inserted, Node reference,
                Element current)
        {
            this.type = type;
            this.deleted = arr(deleted);
            this.inserted = arr(inserted);
            this.reference = reference;
            this.current = current;
            this.description = description;
        }

        public ExecOperation(String type, String description, Node deleted, NodeList inserted, Node reference,
                Element current)
        {
            this.type = type;
            this.deleted = arr(deleted);
            this.inserted = arr(inserted);
            this.reference = reference;
            this.current = current;
            this.description = description;
        }

        public ExecOperation(String type, String description, Node deleted, Node inserted, Node reference,
                Element current)
        {
            this.type = type;
            this.deleted = arr(deleted);
            this.inserted = arr(inserted);
            this.reference = reference;
            this.current = current;
            this.description = description;
        }

        public ExecOperation(String type, String description, Node deleted, Node inserted[], Node reference,
                Element current)
        {
            this.type = type;
            this.deleted = arr(deleted);
            this.inserted = inserted;
            this.reference = reference;
            this.current = current;
            this.description = description;
        }

        /**
         * @param type
         * @param description
         * @param deleted
         * @param inserted
         * @param reference
         * @param current
         */
        public ExecOperation(String type, String description, Node deleted[], Node inserted, Node reference,
                Element current)
        {
            this.type = type;
            this.deleted = deleted;
            this.inserted = arr(inserted);
            this.reference = reference;
            this.current = current;
            this.description = description;
        }

        /**
         * @param type
         * @param description
         * @param deleted
         * @param inserted
         * @param reference
         * @param current
         */
        public ExecOperation(String type, String description, Node deleted[], Node inserted[], Node reference,
                Element current)
        {
            this.type = type;
            this.deleted = deleted;
            this.inserted = inserted;
            this.reference = reference;
            this.current = current;
            this.description = description;
        }

        public Node[] arr(Node node)
        {
            if (node == null) {
                return null;
            }

            if (node instanceof DocumentFragment) {
                DocumentFragment fragment = (DocumentFragment) node;
                NodeList list = fragment.getChildNodes();
                Node[] array = new Node[list.getLength()];
                for (int i = 0; i < array.length; ++i) {
                    array[i] = list.item(i);
                }
                return array;
            }
            else {
                return new Node[]{node};
            }
        }

        public Node[] arr(NodeList list)
        {
            if (list == null) {
                return null;
            }

            int N = list.getLength();

            if (N == 0) {
                return null;
            }

            Node ret[] = new Node[N];
            for (int i = 0; i < N; ++i) {
                ret[i] = list.item(i);
            }
            return ret;
        }

        public void go()
        {
            exchange(deleted, inserted);
        }

        public void undo()
        {
            exchange(inserted, deleted);
        }

        private void exchange(Node deleted[], Node inserted[])
        {
            boolean found = false;
            if (inserted != null) {
                Node first = reference;
                if (deleted != null) {
                    first = deleted[0];
                }
                DocumentFragment fragment = document.createDocumentFragment();
                for (int i = 0; i < inserted.length; ++i) {
                    if (!found && (inserted[i] instanceof Element)) {
                        setNewNode(inserted[i]);
                        found = true;
                    }
                    fragment.appendChild(inserted[i]);
                }
                current.insertBefore(fragment, first);
            }

            if (deleted != null) {
                for (int i = 0; i < deleted.length; ++i) {
                    if (deleted[i] == anchorNode) {
                        Node newAnchor = moveLeft((Node) anchorNode);
                        if (newAnchor == anchorNode) {
                            newAnchor = moveRight((Node) anchorNode);
                            if (newAnchor == anchorNode) {
                                newAnchor = null;
                            }
                        }
                        anchorNode = newAnchor;
                    }
                    current.removeChild(deleted[i]);
                }
            }

            index.removeElements(deleted, true);
            index.addElements(inserted, true);
            warningManager.calculateAffectedElements(deleted, true);
            warningManager.calculateAffectedElements(inserted, true);
            // warningManager.calculateAffectedElements(current, false);

            // Effettuati dei cambiamenti al documento: reset dell'XPATH
            //
            xpathAPI.reset();

            if (inserted != null) {
                dtreeManager.addAllElements(inserted);
            }
            if (deleted != null) {
                dtreeManager.removeAllElements(deleted);
            }
            // Gestione degli elementi marcati come #SelectOnInsert
            //
            if (found) {
                Element element = (Element) newNode;
                ElementModel model = documentModel.getElementModel(element.getNodeName());
                if (model != null) {
                    if (model.isSelectOnInsert(element)) {
                        setCurrentElement(element);
                        setAnchorNode(null);
                    }
                    else {
                        setAnchorNode(element);
                    }
                }
            }

        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            String ret;
            ret = "ExecOperation: " + type + ", " + deleted + ", " + inserted + ", " + reference + ", " + current
                    + ", " + description;
            return ret;
        }
    };

    /**
     * Costruisce un oggetto <code>XMLBuilder</code> per un DOM gi� esistente.<br>
     * Attiva il detailer transformer
     * 
     * @param doc
     * @throws MaxException
     */
    public XMLBuilder(Document doc) throws MaxException
    {
        prepareInterfaceBuilder();

        DocumentType docType = doc.getDoctype();
        MaxXMLFactory xmlFactory = MaxXMLFactory.instance();
        EntityResolver entityResolver = xmlFactory.getEntityResolver();
        InputSource dtdSource;
        try {
            dtdSource = entityResolver.resolveEntity(docType.getPublicId(), docType.getSystemId());
        }
        catch (Exception exc) {
            throw new MaxException(exc);
        }

        MaxDTDParser parser = null;
        try {
            parser = new MaxDTDParser();
        }
        catch (SAXException exc) {
            throw new MaxException("" + exc, exc);
        }
        try {
            parser.setFeature("http://xml.org/sax/features/validation", true);
        }
        catch (SAXException exc) {
            exc.printStackTrace();
        }
        parser.setEntityResolver(entityResolver);

        try {
            parser.parseDTD(dtdSource.getByteStream());
        }
        catch (Exception exc) {
            exc.printStackTrace();
            throw new MaxException(exc);
        }
        documentModel = parser.getDocumentModel();
        documentModel.setXPathAPI(xpathAPI);
        documentModel.systemId = docType.getSystemId();
        documentModel.publicId = docType.getPublicId();
        document = doc;

        try {
            setDetailTransformer();
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }

        if (documentModel == null) {
            throw new MaxException("No DTD specified for given document");
        }

        // Decrypt attributes value
        //

        try {
            decryptDocument();
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }

        documentModel.addChecks(warningManager);
        index.init(document);

        checkDocument(false);

        dtreeManager.init(document, documentModel);
        setCurrentElement(document.getDocumentElement());
    }

    /**
     * Costruisce un oggetto <code>XMLBuilder</code> per un documento XML gi�
     * esistente.<br>
     * Attiva il detailer transformer
     */
    public XMLBuilder(String xmlFile) throws MaxException
    {
        prepareInterfaceBuilder();

        MaxXMLFactory xmlFactory = MaxXMLFactory.instance();
        EntityResolver entityResolver = xmlFactory.getEntityResolver();
        MaxDTDParser parser = null;
        try {
            parser = new MaxDTDParser();
        }
        catch (SAXException exc) {
            throw new MaxException("" + exc, exc);
        }
        try {
            parser.setFeature("http://xml.org/sax/features/validation", true);
        }
        catch (SAXException exc) {
            exc.printStackTrace();
        }
        parser.setEntityResolver(entityResolver);

        try {
            parser.parse(xmlFile);
        }
        catch (Exception exc) {
            exc.printStackTrace();
            throw new MaxException(exc);
        }
        documentModel = parser.getDocumentModel();
        documentModel.setXPathAPI(xpathAPI);
        document = parser.getDocument();
        try {
            setDetailTransformer();
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }

        if (documentModel == null) {
            throw new MaxException("No DTD specified for '" + xmlFile + "'");
        }

        // Decrypt attributes value
        //
        try {
            decryptDocument();
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }

        documentModel.addChecks(warningManager);
        index.init(document);
        checkDocument(false);
        dtreeManager.init(document, documentModel);
        setCurrentElement(document.getDocumentElement());
    }

    /**
     * Costruisce un oggetto <code>XMLBuilder</code> per un nuovo documento XML.<br>
     * Non attiva il detailer transformer.
     */
    public XMLBuilder(String root, InputStream dtdStream) throws MaxException
    {
        prepareInterfaceBuilder();

        try {
            MaxDTDParser parser = new MaxDTDParser();
            documentModel = parser.parseDTD(dtdStream);
            documentModel.setXPathAPI(xpathAPI);
            initDocument(root);
        }
        catch (SAXException exc) {
            throw new MaxException(exc);
        }
    }

    /**
     * Costruisce un oggetto <code>XMLBuilder</code> per un nuovo documento XML.<br>
     * Non attiva il detailer transformer.
     * 
     * @param root
     * @param dtdSource
     * @throws MaxException
     */
    public XMLBuilder(String root, InputSource dtdSource) throws MaxException
    {
        this(root, dtdSource.getByteStream());
    }

    /**
     * Costruisce un oggetto <code>XMLBuilder</code> per un nuovo documento XML.<br>
     * Attiva il detailer transformer.
     * 
     * @param root
     * @param dtdSource
     * @param publicId
     * @param systemId
     * @throws MaxException
     */
    public XMLBuilder(String root, InputSource dtdSource, String publicId, String systemId) throws MaxException
    {
        this(root, dtdSource, publicId, systemId, null);
    }

    /**
     * Costruisce un oggetto <code>XMLBuilder</code> per un nuovo documento XML.<br>
     * Attiva il detailer transformer.
     * 
     * @param root
     * @param dtdSource
     * @param publicId
     * @param systemId
     * @param namespaceURI
     * @throws MaxException
     */
    public XMLBuilder(String root, InputSource dtdSource, String publicId, String systemId, String namespaceURI)
            throws MaxException
    {
        prepareInterfaceBuilder();

        try {
            MaxDTDParser parser = new MaxDTDParser();
            documentModel = parser.parseDTD(dtdSource.getByteStream());
            documentModel.setXPathAPI(xpathAPI);
            documentModel.publicId = publicId;
            documentModel.systemId = systemId;
            initDocument(root, namespaceURI);
        }
        catch (SAXException exc) {
            throw new MaxException(exc);
        }
    }

    /**
     * Costruisce un oggetto <code>XMLBuilder</code> per un nuovo documento XML.<br>
     * Non attiva il detailer transformer.
     * 
     * @param root
     * @param dtdFile
     * @throws MaxException
     */
    public XMLBuilder(String root, String dtdFile) throws MaxException
    {
        prepareInterfaceBuilder();

        try {
            MaxDTDParser parser = new MaxDTDParser();
            documentModel = parser.parseDTD(new File(dtdFile));
            documentModel.setXPathAPI(xpathAPI);
            initDocument(root);
        }
        catch (SAXException exc) {
            throw new MaxException(exc);
        }
    }

    /**
     * Costruisce un oggetto <code>XMLBuilder</code> per un nuovo documento XML.
     * Attiva il detailer transformer se il DocumentModel ha system e public id
     * per cui � definito uno stylesheet.
     * 
     * @param root
     * @param documentModel
     * @throws MaxException
     */
    public XMLBuilder(String root, DocumentModel documentModel) throws MaxException
    {
        prepareInterfaceBuilder();

        this.documentModel = documentModel;
        documentModel.setXPathAPI(xpathAPI);
        initDocument(root);
    }

    /**
     * @param readOnly
     */
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    /**
     * @return
     */
    public boolean isReadOnly()
    {
        return readOnly;
    }

    private void initDocument(String root) throws MaxException
    {
        initDocument(root, null);
    }

    private void initDocument(String root, String namespaceURI) throws MaxException
    {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Element rootElement;
            if ((documentModel.publicId != null) || (documentModel.systemId != null)) {
                DOMImplementation domImpl = db.getDOMImplementation();
                DocumentType doctype = domImpl.createDocumentType(root, documentModel.publicId, documentModel.systemId);
                document = domImpl.createDocument(namespaceURI, root, doctype);
                rootElement = (Element) createElement(root);
                document.replaceChild(rootElement, document.getDocumentElement());

                // resolve the #Counter attributes
                //
                resolveCounters();
            }
            else {
                document = db.newDocument();
                rootElement = (Element) createElement(root);
                document.appendChild(rootElement);

                // resolve the #Counter attributes
                //
                resolveCounters();
            }
            dtreeManager.init(document, documentModel);
            setCurrentElement(rootElement);

            try {
                setDetailTransformer();
            }
            catch (XMLConfigException exc) {
                throw new MaxException(exc);
            }

            documentModel.addChecks(warningManager);
            index.init(document);

            checkDocument(false);
        }
        catch (ParserConfigurationException exc) {
            throw new MaxException(exc);
        }
    }

    private void prepareInterfaceBuilder() throws MaxException
    {
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            documentBuilder = factory.newDocumentBuilder();
        }
        catch (Exception exc) {
            throw new MaxException(exc);
        }
    }

    private void setDetailTransformer() throws MaxException, XMLConfigException
    {
        MaxXMLFactory xmlFact = MaxXMLFactory.instance();
        String cat = XMLConfig.get(MaxXMLFactory.XML_CONF, "/xml/details/@category");

        try {
            ContentProvider provider = xmlFact.getContentProvider();
            synchronized (provider) {
                String key = documentModel.systemId;

                InputStream istream = null;
                if (provider.exists(cat, key)) {
                    istream = provider.get(cat, key);
                }
                if (istream != null) {
                    TransformerFactory tFactory = TransformerFactory.newInstance();
                    Transformer transformer = tFactory.newTransformer(new StreamSource(istream));
                    detailTransformer = transformer;
                    detailTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                }
                else {
                    detailTransformer = documentModel.getDetailTransformer();
                }
            }
        }
        catch (MaxException exc) {
            exc.printStackTrace();
            throw exc;
        }
        catch (Exception exc) {
            exc.printStackTrace();
            throw new MaxException(exc);
        }
    }

    /**
     *
     */
    public void close()
    {
        warningManager.close();

        // Patch per liberare memoria
        // la patch introduce un loop nelle dipendenze con il package
        // max.documents
        MaxDocumentXPathFunction.clearCacheForDocument(document);

        anchorNode = null;
        attributeOperations = null;
        currentContentModelInstance = null;
        currentElement = null;
        detailTransformer = null;
        document = null;
        documentBuilder = null;
        documentModel = null;
        dtreeManager = null;
        elementsWithCounters = null;
        factory = null;
        index = null;
        invalidStructureElements = null;
        menuActions = null;
        newNode = null;
        undoList = null;
        unlockedElements = null;
        warningManager = null;
        wrongANYelements = null;
        xpathAPI = null;
    }

    /**
     * Memorizza l'XMLBulder in sessione. Chiude l'eventuale builder
     * precedentemente presente in sessione.
     * 
     * @param session
     */
    public void storeInSession(HttpSession session)
    {
        XMLBuilder oldBuilder = getFromSession(session);
        if (oldBuilder != null) {
            oldBuilder.close();
        }
        session.setAttribute("XMLBuilder", this);
        try {
            clipboard = Clipboard.getClipboard(session);
        }
        catch (Exception exc) {
            clipboard = null;
            exc.printStackTrace();
        }
    }

    /**
     * Rimuove e chiude l'eventuale XMLBuilder presente in sessione.
     * 
     * @param session
     */
    public static void removeFromSession(HttpSession session)
    {
        XMLBuilder builder = getFromSession(session);
        if (builder != null) {
            builder.close();
        }
        session.removeAttribute("XMLBuilder");
    }

    /**
     * Legge l'XMLBuilder presente in sessione.
     * 
     * @param session
     * @return l'XMLBuilder o null se non � presente in sessione.
     */
    public static XMLBuilder getFromSession(HttpSession session)
    {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("XMLBuilder");
        if ((value != null) && (value instanceof XMLBuilder)) {
            return (XMLBuilder) value;
        }
        return null;
    }

    public Document getDocument()
    {
        return document;
    }

    public DocumentModel getDocumentModel()
    {
        return documentModel;
    }

    public Element getCurrentElement()
    {
        return currentElement;
    }

    public void setCurrentElement(Element element)
    {
        currentContentModelInstance = getContentModelInstance(element);
        currentElement = element;
        attributeOperations = null;
    }

    public void setNewNode(Node node)
    {
        if (node == null) {
            newNode = null;
        }
        else if (node instanceof Element) {
            newNode = node;
        }
        else if (node instanceof DocumentFragment) {
            newNode = node.getFirstChild();
        }
        else {
            newNode = null;
        }
    }

    public void setAnchorNode(Node node)
    {
        if (node == null) {
            anchorNode = null;
        }
        else if (node instanceof Element) {
            anchorNode = node;
        }
        else if (node instanceof DocumentFragment) {
            anchorNode = node.getFirstChild();
        }
        else {
            anchorNode = null;
        }

        // Ci spostiamo un paio di fratelli prima
        // per migliorare la visualizzazione.
        // (Non va bene per i dati ordinati.
        // L'elemento corrente sar� in cima allo schermo).
        //
        // if(anchorNode != null) {
        // anchorNode = moveLeft((Node)anchorNode);
        // anchorNode = moveLeft((Node)anchorNode);
        // }
    }

    protected Node moveLeft(Node node)
    {
        Node ret = node;
        while (true) {
            Node previous = node.getPreviousSibling();
            if (previous == null) {
                return ret;
            }
            if (previous instanceof Element) {
                return previous;
            }
            node = previous;
        }
    }

    protected Node moveRight(Node node)
    {
        Node ret = node;
        while (true) {
            Node next = node.getNextSibling();
            if (next == null) {
                return ret;
            }
            if (next instanceof Element) {
                return next;
            }
            node = next;
        }
    }

    public void refreshContentModelInstance()
    {
        setCurrentElement(currentElement);
    }

    public ContentModelInstance getCurrentContentModelInstance()
    {
        return currentContentModelInstance;
    }

    public String getCurrentElementComment()
    {
        ElementModel elementModel = documentModel.getElementModel(currentElement.getNodeName());
        return elementModel.getComment();
    }

    public String[] getCurrentElementAttributeNames()
    {
        return documentModel.getAttributeNames(currentElement.getNodeName());
    }

    // ---------------------------------------------------------------------------
    // GESTIONE DE/CIFRATURA VALORI
    // ---------------------------------------------------------------------------

    /**
     * @param builder
     * @return
     * @throws XMLConfigException
     */
    public void encryptDocument() throws MaxException, XMLConfigException
    {
        handleEncryption(true);
    }

    /**
     * @param builder
     * @return
     * @throws XMLConfigException
     */
    public void decryptDocument() throws MaxException, XMLConfigException
    {
        handleEncryption(false);
    }

    private void handleEncryption(boolean encrypt) throws MaxException, XMLConfigException
    {
        Set ewf = documentModel.getElementsWithFeature("Encrypted", true);
        Iterator ewfi = ewf.iterator();

        while (ewfi.hasNext()) {
            ElementModel emodel = (ElementModel) ewfi.next();
            Set awf = emodel.getAttributesWithFeature("Encrypted");
            Iterator awfi = awf.iterator();

            while (awfi.hasNext()) {
                AttributeModel amodel = (AttributeModel) awfi.next();
                Set features = amodel.getFeature("Encrypted");
                Feature feature = (Feature) features.iterator().next();
                String parameter = feature.getParameter();
                String keyId = "";
                if ((parameter != null) && (parameter.length() > 0)) {
                    int idx = parameter.indexOf("|");
                    if (idx != -1) {
                        if (encrypt) {
                            keyId = parameter.substring(idx + 1).trim();
                        }
                        else {
                            keyId = parameter.substring(0, idx).trim();
                        }
                    }
                    else {
                        keyId = parameter;
                    }
                }
                logger.debug("XMLBuilder handleEncryption - keyid : " + keyId + "(" + parameter + ")");
                NodeList nl = XMLConfig.getNodeList(document, "//" + emodel.getName() + "/@" + amodel.getName());
                int size = nl.getLength();

                for (int i = 0; i < size; i++) {
                    Node node = nl.item(i);
                    String value = node.getNodeValue();
                    try {
                        String nvalue = "";
                        if (encrypt) {
                            nvalue = value;
                            if (!CryptoHelper.isEncrypted(keyId, nvalue) && PropertiesHandler.isExpanded(value)) {
                                nvalue = CryptoHelper.encrypt(keyId, value, true);
                            }
                        }
                        else {
                            nvalue = CryptoHelper.decrypt(keyId, value, true);
                        }
                        node.setNodeValue(nvalue);
                    }
                    catch (Exception exc) {
                        throw new MaxException("Encryption error //" + emodel.getName() + "/@" + amodel.getName(), exc);
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------------
    // CREAZIONE DI ELEMENTI
    // ---------------------------------------------------------------------------

    /**
     * Initialize also the required attributes.
     */
    public Node createElement(String elementName) throws MaxException
    {
        ElementModel elementModel = documentModel.getElementModel(elementName);
        if (elementModel == null) {
            throw new MaxException("'" + elementName + "' not declared in DTD");
        }

        Element element = document.createElement(elementName);
        ContentModel contentModel = elementModel.getContentModel();
        Node child = createElement(contentModel);
        if (child != null) {
            element.appendChild(child);
        }

        initializeAttributes(element);

        // Gestione degli attributi #Freezed.
        // Inserisce dentro unlockedElements l'ememento appena creato, cos�
        // che gli attributi #Freezed siano scrivibili la prima volta.
        //
        if (elementModel.declareFreezedAttributes()) {
            unlockedElements.add(element);
        }

        // Gestione degli attributi #Counter.
        // Inserisce dentro elementsWithCounters l'ememento appena creato, cos�
        // che gli attributi #Counter siano inizializzati dopo la creazione
        // dal metodo resolveCounters().
        //
        if (elementModel.declareCounterAttributes()) {
            elementsWithCounters.add(element);
        }

        return element;
    }

    /**
     * Does not initialize the attributes.
     */
    public Node createElement(ContentModel contentModel) throws MaxException
    {
        switch (contentModel.qualifier) {
            case ContentModel.Q_ZERO_OR_ONE :
            case ContentModel.Q_ZERO_OR_MANY :
                return null;

            default :
                return createUnqualifiedElement(contentModel);
        }
    }

    /**
     * Does not initialize the attributes.
     */
    public Node createUnqualifiedElement(ContentModel contentModel) throws MaxException
    {
        switch (contentModel.type) {
            case ContentModel.T_SIMPLE_PCDATA :
                return document.createTextNode("");

            case ContentModel.T_SIMPLE_ANY :
            case ContentModel.T_SIMPLE_EMPTY :
                return null;

            case ContentModel.T_SIMPLE_IDE :
                return createElement(contentModel.ide);

            case ContentModel.T_LIST : {
                DocumentFragment fragment = document.createDocumentFragment();
                for (int i = 0; i < contentModel.children.length; ++i) {
                    Node child = createElement(contentModel.children[i]);
                    if (child != null) {
                        fragment.appendChild(child);
                    }
                }
                return fragment;
            }

            case ContentModel.T_ALTERNATIVE :
                if (contentModel.children.length > 0) {
                    return createElement(contentModel.children[0]);
                }
                else {
                    return null;
                }
        }

        return null;
    }

    public void initializeAttributes(Element node)
    {
        String name = node.getNodeName();
        String attrs[] = documentModel.getAttributeNames(name);
        for (int i = 0; i < attrs.length; ++i) {
            AttributeModel am = documentModel.getAttributeModel(name, attrs[i]);
            Attr attrNode = createAttributeNode(am);
            if (attrNode != null) {
                node.setAttributeNode(attrNode);
            }
        }
    }

    public Attr createAttributeNode(AttributeModel model)
    {
        Attr attr = null;
        if (model.defaultType.equals("#REQUIRED") || model.defaultType.equals("#FIXED")) {
            attr = document.createAttribute(model.name);
            if (model.type.equals("")) {
                // Valore enumerato
                if ((model.defaultValue != null) && !model.defaultValue.equals("")) {
                    attr.setValue(model.defaultValue);
                }
                else {
                    attr.setValue(model.choices[0]);
                }
            }
            else if (model.defaultType.equals("#FIXED")) {
                attr.setValue(model.defaultValue);
            }
        }
        return attr;
    }

    /**
     * Trova gli elementi che hanno attributi marcati come #Counter e li
     * inserisce nel Set elementsWithCounters. Inoltre azzera gli attributi
     * #Counter.
     */
    private void findNodesWithCounters(Node start)
    {
        if (start instanceof Element) {
            Element element = (Element) start;
            ElementModel model = documentModel.getElementModel(element.getNodeName());
            if (model.declareCounterAttributes()) {
                elementsWithCounters.add(element);
                NamedNodeMap attributes = element.getAttributes();
                for (int i = 0; i < attributes.getLength(); ++i) {
                    Attr attr = (Attr) attributes.item(i);
                    if (attr.getSpecified()) {
                        String name = attr.getName();
                        AttributeModel attrModel = model.getAttributeModel(name);
                        if (attrModel.isCounterInto(element)) {
                            element.setAttribute(name, "");
                        }
                    }
                }
            }
            Node child = element.getFirstChild();
            while (child != null) {
                findNodesWithCounters(child);
                child = child.getNextSibling();
            }
        }
    }

    private void resolveCounters()
    {
        Iterator i = elementsWithCounters.iterator();
        while (i.hasNext()) {
            Element element = (Element) i.next();
            String elementName = element.getNodeName();
            NamedNodeMap attrs = element.getAttributes();
            int l = attrs.getLength();
            for (int j = 0; j < l; ++j) {
                Node attrNode = attrs.item(j);
                String attrVal = attrNode.getNodeValue();
                long val = 0;
                try {
                    val = Long.parseLong(attrVal);
                }
                catch (NumberFormatException exc) {
                    // questa eccezione non deve essere stampata
                }
                if (val <= 0) {
                    String attr = attrNode.getNodeName();
                    AttributeModel am = documentModel.getAttributeModel(elementName, attr);
                    if (am.isCounterInto(element)) {
                        val = am.calculateCounter(element);
                        element.setAttribute(attr, "" + val);
                    }
                }
            }
        }
        elementsWithCounters.clear();
    }

    // ---------------------------------------------------------------------------
    // CREAZIONE DEL ContentModelInstance
    // ---------------------------------------------------------------------------

    /**
     * Determina un <code>ContentModelInstance</code> per l'elemento dato.
     * 
     * @param element
     *        <code>Element</code> di cui si vuole il
     *        <code>ContentModelInstance</code>.
     * 
     * @return <code>ContentModelInstance</code> per l'elemento dato.
     */
    public ContentModelInstance getContentModelInstance(Element element)
    {
        ContentModelInstance cmi = getContentModelInstanceWithoutParents(element);

        if (cmi == null) {
            ContentModel cm = new ContentModel();
            cm.type = ContentModel.T_SIMPLE_ANY;
            cmi = new ContentModelInstance(cm);
            cmi.addOperation(new Operation(Operation.T_EDIT_ANY, element, null, null));

            /*******************************************************************
             * if (!invalidStructureElements.contains(element)) {
             * invalidStructureElements.add(element);
             * warningManager.addWarning(new Warning("Element '" +
             * element.getNodeName() + "' does not match the DTD", element));
             ******************************************************************/

            if (!invalidStructureElements.containsKey(element)) {
                Warning warning = new Warning("Element '" + element.getNodeName() + "' does not match the DTD", element);
                invalidStructureElements.put(element, warning);
                warningManager.addWarning(warning);

            }
        }
        else {
            Warning warning = (Warning) invalidStructureElements.remove(element);
            if (warning != null) {
                warningManager.removeWarning(warning);
            }
        }

        // Aggiunge le operazioni di selezione degli avi
        //
        Node current = element;
        while (true) {
            Node parent = current.getParentNode();
            if (parent instanceof Element) {
                cmi.addOperation(new Operation(Operation.T_SELECT_P, parent, null, cmi));
                current = parent;
            }
            else {
                break;
            }
        }

        return cmi;
    }

    public ContentModelInstance getContentModelInstanceWithoutParents(Element element)
    {
        element.normalize();
        String elementName = element.getNodeName();
        ContentModel contentModel = documentModel.getContentModel(elementName);

        NodeList children = element.getChildNodes();
        int n = children.getLength();

        ContentModelInstance cmi = getContentModelInstance(element, children, 0, n, contentModel);
        if (cmi == null) {
            return null;
        }

        // Se il content model non � ANY deve controllare che i children
        // restanti
        // non siano significativi.
        // Nel caso di ANY questo controllo non si applica.
        //
        if (contentModel.type != ContentModel.T_SIMPLE_ANY) {
            for (int i = cmi.countNodes(); i < n; ++i) {
                Node node = children.item(i);
                if (!discardable(node)) {
                    return null;
                }
                cmi.addNode(node);
            }
        }

        return cmi;
    }

    private ContentModelInstance getContentModelInstance(Element element, NodeList nodeList, int start, int end,
            ContentModel contentModel)
    {
        ContentModelInstance contentModelInstance = new ContentModelInstance(contentModel);
        ContentModelInstance cmi = null;
        ContentModelInstance prevCmi = null;

        switch (contentModel.qualifier) {

            case ContentModel.Q_ONE :
                cmi = getUnqualifiedContentModelInstance(element, nodeList, start, end, contentModel);
                return cmi;

            case ContentModel.Q_ZERO_OR_ONE :
                cmi = getUnqualifiedContentModelInstance(element, nodeList, start, end, contentModel);
                if (cmi == null) {
                    cmi = new ContentModelInstance(contentModel);
                    addInsertOperations(cmi, contentModel, Operation.T_INSERT_B);
                }
                else {
                    cmi.addOperation(new Operation(Operation.T_DELETE, null, null, cmi));
                    if (cmi.isSimple()) {
                        cmi.addOperation(new Operation(Operation.T_CUT, cmi.getElement(), null, cmi));
                    }
                }
                return cmi;

            case ContentModel.Q_ONE_OR_MANY :
                cmi = getUnqualifiedContentModelInstance(element, nodeList, start, end, contentModel);
                if (cmi == null) {
                    return null;
                }
                addInsertOperations(cmi, contentModel, Operation.T_INSERT_B);
                contentModelInstance.addNode(cmi);
                start += cmi.countNodes();
                prevCmi = cmi;

            case ContentModel.Q_ZERO_OR_MANY :
                while (start < end) {
                    cmi = getUnqualifiedContentModelInstance(element, nodeList, start, end, contentModel);
                    if (cmi != null) {
                        if (prevCmi != null) {
                            prevCmi.addOperation(new Operation(Operation.T_DELETE, null, null, prevCmi));
                            if (prevCmi.isSimple()) {
                                prevCmi.addOperation(new Operation(Operation.T_CUT, prevCmi.getElement(), null, prevCmi));
                            }
                            prevCmi = null;
                        }
                        addInsertOperations(cmi, contentModel, Operation.T_INSERT_B);
                        cmi.addOperation(new Operation(Operation.T_DELETE, null, null, cmi));
                        if (cmi.isSimple()) {
                            cmi.addOperation(new Operation(Operation.T_CUT, cmi.getElement(), null, cmi));
                        }
                        contentModelInstance.addNode(cmi);
                        start += cmi.countNodes();
                    }
                    else {
                        break;
                    }
                }
                ;
                addInsertOperations(contentModelInstance, contentModel, Operation.T_INSERT_A);
                return contentModelInstance;
        }
        return null;
    }

    private ContentModelInstance getUnqualifiedContentModelInstance(Element element, NodeList nodeList, int start,
            int end, ContentModel contentModel)
    {
        ContentModelInstance cmi = null;
        switch (contentModel.type) {

            case ContentModel.T_SIMPLE_ANY :
                cmi = new ContentModelInstance(contentModel);
                cmi.addOperation(new Operation(Operation.T_EDIT_ANY, element, null, null));
                return cmi;

            case ContentModel.T_SIMPLE_PCDATA :
            case ContentModel.T_SIMPLE_EMPTY :
            case ContentModel.T_SIMPLE_IDE :
                cmi = getSimpleContentModelInstance(element, nodeList, start, end, contentModel);
                return cmi;

            case ContentModel.T_LIST :
            case ContentModel.T_ALTERNATIVE :
                cmi = getCompositeContentModelInstance(element, nodeList, start, end, contentModel);
                return cmi;
        }
        return null;
    }

    private Vector getListContentModelInstance(Element element, NodeList nodeList, int start, int end, int idxList,
            ContentModel contentModel[])
    {
        if (idxList < (contentModel.length - 1)) {
            for (int e = end; e >= start; --e) {
                ContentModelInstance cmi = getContentModelInstance(element, nodeList, start, e, contentModel[idxList]);
                if (cmi != null) {
                    Vector v2 = getListContentModelInstance(element, nodeList, start + cmi.countNodes(), end,
                            idxList + 1, contentModel);
                    if (v2 != null) {
                        Vector v = new Vector();
                        v.addElement(cmi);
                        v.addAll(v2);
                        return v;
                    }
                }
            }
            return null;
        }
        else {
            ContentModelInstance cmi = getContentModelInstance(element, nodeList, start, end, contentModel[idxList]);
            if (cmi == null) {
                return null;
            }
            Vector v = new Vector();
            v.addElement(cmi);
            return v;
        }
    }

    private ContentModelInstance getCompositeContentModelInstance(Element element, NodeList nodeList, int start,
            int end, ContentModel contentModel)
    {
        switch (contentModel.type) {

            case ContentModel.T_LIST : {
                ContentModelInstance contentModelInstance = new ContentModelInstance(contentModel);
                Vector v = getListContentModelInstance(element, nodeList, start, end, 0, contentModel.children);
                if (v == null) {
                    return null;
                }
                for (Enumeration e = v.elements(); e.hasMoreElements();) {
                    contentModelInstance.addNode((ContentModelInstance) e.nextElement());
                }
                return contentModelInstance;
            }

            case ContentModel.T_ALTERNATIVE :
                for (int i = 0; i < contentModel.children.length; ++i) {
                    ContentModelInstance cmi = getContentModelInstance(element, nodeList, start, end,
                            contentModel.children[i]);
                    if (cmi != null) {
                        for (int j = 0; j < contentModel.children.length; ++j) {
                            ContentModel cm = contentModel.children[j];
                            if (j != i) {
                                cmi.addOperation(new Operation(Operation.T_CHANGE, null, cm, cmi));
                                if (cm.type == ContentModel.T_SIMPLE_IDE) {

                                    if ((clipboard != null) && clipboard.isCompatible(cm.ide, getDocument())) {

                                        cmi.addOperation(new Operation(Operation.T_PASTE, null, cm, cmi));
                                    }
                                }
                            }
                        }
                        return cmi;
                    }
                }
                return null;
        }
        return null;
    }

    private ContentModelInstance getSimpleContentModelInstance(Element element, NodeList nodeList, int start, int end,
            ContentModel contentModel)
    {
        // Se l'intervallo � vuoto, allora va bene se si tratta
        // di un #PCDATA o EMPTY o ANY
        if (start == end) {
            if (contentModel.type == ContentModel.T_SIMPLE_PCDATA) {
                ContentModelInstance contentModelInstance = new ContentModelInstance(contentModel);
                Text textNode = document.createTextNode("");
                contentModelInstance.addNode(textNode, true);
                element.appendChild(textNode);
                contentModelInstance.addOperation(new Operation(Operation.T_EDIT, textNode, null, null));
                return contentModelInstance;
            }
            else if (contentModel.type == ContentModel.T_SIMPLE_EMPTY) {
                ContentModelInstance contentModelInstance = new ContentModelInstance(contentModel);
                return contentModelInstance;
            }
            else if (contentModel.type == ContentModel.T_SIMPLE_ANY) {
                ContentModelInstance contentModelInstance = new ContentModelInstance(contentModel);
                return contentModelInstance;
            }
        }

        ContentModelInstance contentModelInstance = new ContentModelInstance(contentModel);
        for (int idx = start; idx < end; ++idx) {
            Node node = nodeList.item(idx);

            if (node instanceof Element) {
                // Un Element deve corrispondere ad un contentModel di tipo
                // T_SIMPLE_IDE
                if (contentModel.type == ContentModel.T_SIMPLE_IDE) {
                    if (contentModel.ide.equals(node.getNodeName())) {
                        contentModelInstance.addNode(node);
                        contentModelInstance.addOperation(new Operation(Operation.T_SELECT, node, null,
                                contentModelInstance));
                        contentModelInstance.addOperation(new Operation(Operation.T_COPY, node, null,
                                contentModelInstance));

                        if ((clipboard != null) && clipboard.isCompatible(contentModel.ide, getDocument())) {

                            contentModelInstance.addOperation(new Operation(Operation.T_PASTE, null, contentModel,
                                    contentModelInstance));
                        }
                        return contentModelInstance;
                    }
                }
                return null;
            }
            else if (node instanceof Text) {
                // Un Text deve corrispondere ad un contentModel di tipo
                // T_SIMPLE_PCDATA...
                if (contentModel.type == ContentModel.T_SIMPLE_PCDATA) {
                    contentModelInstance.addNode(node);
                    contentModelInstance.addOperation(new Operation(Operation.T_EDIT, node, null, null));
                    return contentModelInstance;
                }
                String txt = ((Text) node).getNodeValue().trim();
                // ...oppure essere vuoto
                if (txt.equals("")) {
                    contentModelInstance.addNode(node);
                }
                else {
                    return null;
                }
            }
            else {
                contentModelInstance.addNode(node);
            }
        }
        return null;
    }

    private void addInsertOperations(ContentModelInstance cmi, ContentModel cm, int type)
    {
        if (cm.type == ContentModel.T_ALTERNATIVE) {
            for (int i = 0; i < cm.children.length; ++i) {
                addInsertOperations(cmi, cm.children[i], type);
            }
        }
        else if (cm.type == ContentModel.T_SIMPLE_IDE) {

            /*******************************************************************
             * String contentModel = cm.ide; ElementModel model =
             * documentModel.getElementModel(contentModel); if (model == null ||
             * (model != null && !model.isHidden())) { if ((copy != null) &&
             * (copy.getNodeName().equals(contentModel))) { int pasteType =
             * Operation.T_PASTE_A; if (type == Operation.T_INSERT_B) {
             * pasteType = Operation.T_PASTE_B; } cmi.addOperation(new
             * Operation(pasteType, null, cm, cmi)); } cmi.addOperation(new
             * Operation(type, null, cm, cmi)); }
             ******************************************************************/

            if ((clipboard != null) && clipboard.isCompatible(cm.ide, getDocument())) {
                int pasteType = Operation.T_PASTE_A;
                if (type == Operation.T_INSERT_B) {
                    pasteType = Operation.T_PASTE_B;
                }
                cmi.addOperation(new Operation(pasteType, null, cm, cmi));
            }
            cmi.addOperation(new Operation(type, null, cm, cmi));

        }
        else {
            cmi.addOperation(new Operation(type, null, cm, cmi));
        }
    }

    private boolean discardable(Node node)
    {
        if (node instanceof Element) {
            return false;
        }
        else if (node instanceof Text) {
            String txt = ((Text) node).getNodeValue().trim();
            if (!txt.equals("")) {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------------------------
    // GESTIONE DELLA UNDO-LIST
    // ---------------------------------------------------------------------------

    public final static int UNDO_SIZE       = 50;

    private ExecOperation   undoList[]      = new ExecOperation[UNDO_SIZE];

    private int             startPosition   = 0;

    private int             currentPosition = 0;

    private int             endPosition     = 0;

    private void updateUndoList(ExecOperation exec)
    {
        undoList[currentPosition % UNDO_SIZE] = exec;
        ++currentPosition;
        endPosition = currentPosition;
        if ((endPosition - startPosition) > UNDO_SIZE) {
            startPosition = endPosition - UNDO_SIZE;
        }
    }

    public void undo()
    {
        if (startPosition == currentPosition) {
            return;
        }
        --currentPosition;
        ExecOperation exec = undoList[currentPosition % UNDO_SIZE];
        exec.undo();
        setCurrentElement(exec.current);
        checkDocument(false);
    }

    public void redo()
    {
        if (endPosition == currentPosition) {
            return;
        }
        ExecOperation exec = undoList[currentPosition % UNDO_SIZE];
        exec.go();
        ++currentPosition;
        setCurrentElement(exec.current);
        checkDocument(false);
    }

    // ---------------------------------------------------------------------------
    // METODI PER LA REALIZZAZIONE DELL'INTERFACCIA UMANA
    // ---------------------------------------------------------------------------

    /**
     * Attiva il tree
     */
    public void activateTree()
    {
        dtreeManager.setActivationStatus(true);
    }

    /**
     * Disattiva il tree
     */
    public void deactivateTree()
    {
        dtreeManager.setActivationStatus(false);
    }

    /**
     * Restituisce un'enumerazione di tutte le operazioni disponibili per gli
     * elementi.
     */
    public Operation[] getAvailableOperations()
    {
        ContentModelInstance cmi = getCurrentContentModelInstance();

        Vector v = new Vector();
        cmi.getAllOperations(v, readOnly);

        Operation ret[] = new Operation[v.size()];
        v.copyInto(ret);
        return ret;
    }

    /**
     * Restituisce un'enumerazione di tutte le operazioni disponibili per gli
     * attributi.
     */
    public Operation[] getAttributeOperations()
    {
        if (attributeOperations == null) {
            attributeOperations = new Vector();

            String elementName = currentElement.getNodeName();
            String attrs[] = documentModel.getAttributeNames(elementName);

            boolean withoutErrors = warningManager.getWarnings(currentElement) == null;

            // Operazioni per gli attributi validi
            //
            for (int i = 0; i < attrs.length; ++i) {

                Attr attr = currentElement.getAttributeNode(attrs[i]);
                if (attr == null) {
                    if (!readOnly) {
                        attr = document.createAttribute(attrs[i]);
                        attributeOperations.addElement(new Operation(Operation.T_ATT_ADD, attr, null, null));
                    }
                }
                else {
                    AttributeModel model = documentModel.getAttributeModel(elementName, attrs[i]);

                    boolean isFreezed = (model.isFreezedInto(currentElement)
                            && !(unlockedElements.contains(currentElement)) && withoutErrors)
                            || readOnly;
                    boolean isCounter = model.isCounterInto(currentElement);
                    boolean isFixed = model.defaultType.equals("#FIXED") || readOnly;
                    boolean isRequired = model.defaultType.equals("#REQUIRED");

                    if (!isRequired && !isFixed) {
                        attributeOperations.addElement(new Operation(Operation.T_ATT_REMOVE, attr, null, null));
                    }

                    if (isFixed || isFreezed || isCounter) {
                        attributeOperations.addElement(new Operation(Operation.T_ATT_FIXED, attr, null, null));
                    }
                    else {
                        attributeOperations.addElement(new Operation(Operation.T_ATT_EDIT, attr, null, null));
                    }
                }
            }

            // Operazioni per gli attributi non validi
            //
            Set validAttributes = new HashSet();
            for (int i = 0; i < attrs.length; ++i) {
                validAttributes.add(attrs[i]);
            }

            NamedNodeMap nnm = currentElement.getAttributes();
            for (int i = 0; i < nnm.getLength(); ++i) {
                Node node = nnm.item(i);
                if (!validAttributes.contains(node.getNodeName())) {
                    if (!readOnly) {
                        attributeOperations.addElement(new Operation(Operation.T_ATT_REMOVE, node, null, null));
                    }
                    attributeOperations.addElement(new Operation(Operation.T_ATT_FIXED, node, null, null));
                }
            }
        }

        Operation ret[] = new Operation[attributeOperations.size()];
        attributeOperations.copyInto(ret);
        return ret;
    }

    /**
     * Seleziona un nodo tramite il suo ID ottenuto da DTree
     * 
     * @param id
     * @throws MaxException
     */
    public void doTreeSelect(int id)
    {
        setNewNode(null);

        Element selectedElement = dtreeManager.getElement(id);
        if (selectedElement == null) {
            return;
        }

        // No anchor nodes so the HTML page will present the whole form.
        //
        setAnchorNode(null);

        setCurrentElement(selectedElement);
    }

    /**
     * Esegue un'operazione dato il suo operation key.<br>
     * L'operation key permette di eseguire solo ed esclusivamente le operazioni
     * ammissibili per il nodo corrente.
     */
    public void doOperation(String opKey, String param)
    {
        setNewNode(null);

        Warning warn = getWarning(opKey);
        if (warn != null) {
            goToWarnedElement(warn);
            return;
        }

        Operation op = currentContentModelInstance.getOperation(opKey);
        if (op == null) {
            doAttributeOperation(opKey, param);
            setAnchorNode(null);
            dtreeManager.updateName(currentElement);
            return;
        }

        doOperation(op, param);
        dtreeManager.updateName(currentElement);
    }

    private void doOperation(Operation op, String param)
    {
        try {
            switch (op.getType()) {
                case Operation.T_SELECT :
                    doSelect(op);
                    break;
                case Operation.T_COPY :
                    doCopy(op);
                    break;
                case Operation.T_CUT :
                    doCut(op);
                    break;
                case Operation.T_SELECT_P :
                    doSelectParent(op);
                    break;
                case Operation.T_DELETE :
                    doDelete(op);
                    break;
                case Operation.T_CHANGE :
                    doChange(op);
                    break;
                case Operation.T_PASTE :
                    doPaste(op);
                    break;
                case Operation.T_INSERT_B :
                    doInsertBefore(op);
                    break;
                case Operation.T_INSERT_A :
                    doInsertAfter(op);
                    break;
                case Operation.T_PASTE_B :
                    doPasteBefore(op);
                    break;
                case Operation.T_PASTE_A :
                    doPasteAfter(op);
                    break;
                case Operation.T_EDIT :
                    doEdit(op, param);
                    break;
                case Operation.T_EDIT_ANY :
                    doEditAny(op, param);
                    break;
            }

            // Dopo un'operazione di modifica
            // verifichiamo di nuovo il documento
            //
            switch (op.getType()) {
                case Operation.T_DELETE :
                case Operation.T_CHANGE :
                case Operation.T_PASTE :
                case Operation.T_INSERT_B :
                case Operation.T_INSERT_A :
                case Operation.T_PASTE_B :
                case Operation.T_PASTE_A :
                case Operation.T_CUT :
                case Operation.T_EDIT :
                case Operation.T_EDIT_ANY :
                    checkDocument(false);
                    break;
            }
        }
        catch (MaxException exc) {
            exc.printStackTrace();
        }
    }

    public void doSaveGraph(Node node)
    {

        node = document.importNode(((Document) node).getDocumentElement(), true);

        ExecOperation exec = new ExecOperation("edit", "edit \"" + currentElement.getNodeName() + "\"",
                currentElement.getChildNodes(), node.getChildNodes(), null, currentElement);
        exec.go();
        updateUndoList(exec);
        refreshContentModelInstance();

        NamedNodeMap attrs = node.getAttributes();
        int len = (attrs != null) ? attrs.getLength() : 0;
        for (int i = 0; i < len; i++) {
            Attr attr = (Attr) attrs.item(i);
            currentElement.setAttribute(attr.getNodeName(), attr.getNodeValue());
        }
        checkDocument(true);
    }

    /**
     * Normalizza l'input togliendo le sequenze \r\n e le sostituisce con \n
     * singoli
     */
    private String stripSlashR(String str)
    {
        StringBuffer buf = new StringBuffer();
        int idx = 0;
        int prev = 0;

        while ((idx = str.indexOf("\r\n", prev)) != -1) {
            buf.append(str.substring(prev, idx)).append("\n");
            prev = idx + 2;
        }
        buf.append(str.substring(prev));
        return buf.toString();
    }

    private void doEdit(Operation op, String param)
    {
        param = stripSlashR(param);

        Node txt = op.getNode();
        txt.setNodeValue(param);
        Node node = txt.getParentNode(); // elemento che contiene il nodo di
        // tipo testo
        if (node != null) {
            if (node instanceof Element) {
                setNewNode(node);
                warningManager.calculateAffectedElements(node, true);
                Node parent = node.getParentNode(); // elemento parent
                // dell'elemento #PCDATA
                if (parent != null) {
                    if (parent instanceof Element) {
                        setCurrentElement((Element) parent);
                        setAnchorNode(node);
                    }
                }
            }
        }
    }

    private void doEditAny(Operation op, String param)
    {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            Map namespaces = new HashMap();
            readNamespaceAttributes(currentElement, namespaces);
            String namespacesAttrs = prepareNamespaceAttributes(namespaces);

            StringReader reader = new StringReader("<root" + namespacesAttrs + ">" + param + "</root>");

            Document doc = db.parse(new InputSource(reader));

            Node toInsert = document.importNode(doc.getDocumentElement(), true);

            ExecOperation exec = new ExecOperation("edit", "ANY", currentElement.getChildNodes(),
                    toInsert.getChildNodes(), null, currentElement);
            exec.go();
            updateUndoList(exec);

            Warning warning = (Warning) wrongANYelements.remove(currentElement);
            if (warning != null) {
                warningManager.removeWarning(warning);
            }

        }
        catch (Exception exc) {
            // exc.printStackTrace();

            Text toInsert = document.createTextNode(stripSlashR(param));

            ExecOperation exec = new ExecOperation("edit", "ANY", currentElement.getChildNodes(), toInsert, null,
                    currentElement);
            exec.go();
            updateUndoList(exec);

            String elementName = currentElement.getNodeName();

            Warning warning = new Warning("Element '" + elementName + "': " + exc.getMessage(), currentElement);
            Warning oldWarning = (Warning) wrongANYelements.put(currentElement, warning);
            if (oldWarning != null) {
                warningManager.removeWarning(oldWarning);
            }
            warningManager.addWarning(warning);

        }
        refreshContentModelInstance();
    }

    private void readNamespaceAttributes(Node contextNode, Map toFill)
    {
        if (contextNode == null) {
            return;
        }

        NamedNodeMap attributes = contextNode.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); ++i) {
                Attr attr = (Attr) attributes.item(i);
                String name = attr.getName();
                if (name.equals("xmlns") || name.startsWith("xmlns:")) {
                    if (!toFill.containsKey(name)) {
                        toFill.put(name, attr.getNodeValue());
                    }
                }
            }
        }
        readNamespaceAttributes(contextNode.getParentNode(), toFill);
    }

    private String prepareNamespaceAttributes(Map map)
    {
        StringBuffer buffer = new StringBuffer();

        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String attribute = (String) entry.getKey();
            String namespace = (String) entry.getValue();
            buffer.append(" ");
            buffer.append(attribute);
            buffer.append('=');
            buffer.append('"').append(encode(namespace)).append('"');
        }
        return buffer.toString();
    }

    private void doSelect(Operation op)
    {
        // No anchor nodes so the HTML page will present the whole form.
        //
        setAnchorNode(null);

        setCurrentElement((Element) op.getNode());
    }

    private void doCopy(Operation op)
    {

        /***********************************************************************
         * Element toCopy = (Element) op.getNode(); copy = (Element)
         * toCopy.cloneNode(true); refreshContentModelInstance();
         * setAnchorNode(toCopy);
         **********************************************************************/

        if (clipboard != null) {
            Element toCopy = (Element) op.getNode();
            clipboard.copy(toCopy);
            refreshContentModelInstance();
            setAnchorNode(toCopy);
        }
    }

    private void doSelectParent(Operation op)
    {
        // Determinazione dell'anchor
        //
        Element selected = (Element) op.getNode();

        Node element = currentElement;
        Node parent = element.getParentNode();
        while (parent != selected) {
            element = parent;
            parent = element.getParentNode();
        }
        setAnchorNode(element);

        // Impostazione dell'elemento corrente
        //
        setCurrentElement(selected);
    }

    private void doCut(Operation op)
    {

        /***********************************************************************
         * Element toCopy = (Element) op.getNode(); copy = (Element)
         * toCopy.cloneNode(true);
         * 
         * ContentModelInstance cmi = op.getContentModelInstance(); Node
         * toDelete[] = cmi.getDOMNodes(); Node reference = null; if (toDelete
         * != null) { reference = toDelete[toDelete.length -
         * 1].getNextSibling(); } ExecOperation exec = new ExecOperation("cut",
         * "cut \"" + cmi.getContentModel() + "\"", toDelete, (Node[]) null,
         * reference, currentElement); exec.go(); updateUndoList(exec);
         * refreshContentModelInstance();
         **********************************************************************/

        if (clipboard != null) {
            Element toCopy = (Element) op.getNode();
            clipboard.copy(toCopy);
            ContentModelInstance cmi = op.getContentModelInstance();
            Node toDelete[] = cmi.getDOMNodes();
            Node reference = null;
            if (toDelete != null) {
                reference = toDelete[toDelete.length - 1].getNextSibling();
            }
            ExecOperation exec = new ExecOperation("cut", "cut \"" + cmi.getContentModel() + "\"", toDelete,
                    (Node[]) null, reference, currentElement);
            exec.go();
            updateUndoList(exec);
            refreshContentModelInstance();
        }

    }

    private void doDelete(Operation op)
    {
        ContentModelInstance cmi = op.getContentModelInstance();
        Node toDelete[] = cmi.getDOMNodes();
        Node reference = null;
        if (toDelete != null) {
            reference = toDelete[toDelete.length - 1].getNextSibling();
        }
        ExecOperation exec = new ExecOperation("delete", "delete \"" + cmi.getContentModel() + "\"", toDelete,
                (Node[]) null, reference, currentElement);
        exec.go();
        updateUndoList(exec);
        refreshContentModelInstance();
    }

    private void doChange(Operation op) throws MaxException
    {
        ContentModelInstance cmi = op.getContentModelInstance();
        ContentModel cm = op.getContentModel();
        Node changeTo = createUnqualifiedElement(cm);

        Node toChange[] = cmi.getDOMNodes();
        Node reference = null;
        if (toChange != null) {
            reference = toChange[toChange.length - 1].getNextSibling();
        }

        ExecOperation exec = new ExecOperation("change", "change to \"" + cm + "\"", cmi.getDOMNodes(), changeTo,
                reference, currentElement);
        exec.go();
        updateUndoList(exec);

        // resolve the #Counter attributes
        //
        resolveCounters();

        refreshContentModelInstance();
    }

    private void doPaste(Operation op)
    {

        if (clipboard != null) {
            Node changeTo = clipboard.paste(getDocument());

            ContentModelInstance cmi = op.getContentModelInstance();
            ContentModel cm = op.getContentModel();

            Node toChange[] = cmi.getDOMNodes();
            Node reference = null;
            if (toChange != null) {
                reference = toChange[toChange.length - 1].getNextSibling();
            }

            ExecOperation exec = new ExecOperation("paste", "replace \"" + cm + "\" with the \""
                    + changeTo.getNodeName() + "\"in the clipboard", cmi.getDOMNodes(), changeTo, reference,
                    currentElement);
            exec.go();
            updateUndoList(exec);

            // Resolve the #Counter attributes and recalculates the counters.
            //
            findNodesWithCounters(changeTo);
            resolveCounters();

            refreshContentModelInstance();

        }
    }

    private void doInsertBefore(Operation op) throws MaxException
    {
        ContentModelInstance cmi = op.getContentModelInstance();
        Node first = cmi.getFirstDOMNodeBefore(cmi);
        doInsert(op, first, cmi);
    }

    private void doInsertAfter(Operation op) throws MaxException
    {
        ContentModelInstance cmi = op.getContentModelInstance();
        Node first = cmi.getFirstDOMNodeAfter(cmi);
        doInsert(op, first, cmi);
    }

    private void doInsert(Operation op, Node first, ContentModelInstance cmi) throws MaxException
    {
        ContentModel cm = op.getContentModel();
        Node toInsert = createUnqualifiedElement(cm);

        ExecOperation exec = new ExecOperation("insert", "insert \"" + cmi.getContentModel() + "\"", (Node[]) null,
                toInsert, first, currentElement);
        exec.go();
        updateUndoList(exec);

        if (newNode != null) {
            Element element = (Element) newNode;
            ElementModel model = documentModel.getElementModel(element.getNodeName());
            if (model != null) {
                if (model.isSelectOnInsert(element)) {
                    setCurrentElement(element);
                    setAnchorNode(null);
                }
                else {
                    setAnchorNode(element);
                }
            }
        }

        // resolve the #Counter attributes
        //
        resolveCounters();

        refreshContentModelInstance();
    }

    private void doPasteBefore(Operation op)
    {
        ContentModelInstance cmi = op.getContentModelInstance();
        Node first = cmi.getFirstDOMNodeBefore(cmi);
        doPaste(first);
    }

    private void doPasteAfter(Operation op)
    {
        ContentModelInstance cmi = op.getContentModelInstance();
        Node first = cmi.getFirstDOMNodeAfter(cmi);
        doPaste(first);
    }

    private void doPaste(Node first)
    {

        if (clipboard != null) {
            Node toInsert = clipboard.paste(getDocument());

            ExecOperation exec = new ExecOperation("paste", "paste \"" + toInsert.getNodeName() + "\"", (Node[]) null,
                    toInsert, first, currentElement);
            exec.go();
            updateUndoList(exec);

            // resolve the #Counter attributes
            //
            findNodesWithCounters(toInsert);
            resolveCounters();

            refreshContentModelInstance();

        }

    }

    private void doAttributeOperation(String opKey, String param)
    {
        if (attributeOperations == null) {
            return;
        }

        for (Enumeration e = attributeOperations.elements(); e.hasMoreElements();) {
            Operation op = (Operation) e.nextElement();
            if (op.getKey().equals(opKey)) {
                doAttributeOperation(op, param);
                return;
            }
        }
    }

    private void doAttributeOperation(Operation op, String param)
    {
        attributeOperations = null;
        boolean changed = false;

        switch (op.getType()) {

            case Operation.T_ATT_ADD : {
                Attr attr = (Attr) op.getNode();
                currentElement.setAttributeNode(attr);
                AttributeModel model = documentModel.getAttributeModel(currentElement.getNodeName(), attr.getNodeName());
                if (model.isCounterInto(currentElement)) {
                    elementsWithCounters.add(currentElement);
                    resolveCounters();
                }
                else if (model.defaultType.equals("#FIXED")) {
                    attr.setValue(model.defaultValue);
                }
                else if (model.defaultType.equals("#REQUIRED") || model.defaultType.equals("#IMPLIED")) {
                    if (model.type.equals("")) {
                        attr.setValue(model.choices[0]);
                    }
                }
                else {
                    attr.setValue(model.defaultValue);
                }

                // Se l'attributo � #Freezed allora sblocca l'elemento.
                // ATTENZIONE: TUTTI GLI ATTRIBUTI FREEZED SONO SBLOCCATI
                //
                if (model.isDeclaredFreezed()) {
                    unlockedElements.add(currentElement);
                }
                changed = true;
            }
                break;

            case Operation.T_ATT_REMOVE :
                currentElement.removeAttribute(op.getNode().getNodeName());
                changed = true;
                break;

            case Operation.T_ATT_EDIT : {
                Attr attr = (Attr) op.getNode();
                attr.setValue(param);
                changed = true;
            }
                break;
        }

        if (changed) {

            // Effettuati dei cambiamenti al documento: reset dell'XPATH e
            // check.
            //
            xpathAPI.reset();
            warningManager.calculateAffectedElements(currentElement, false);
            checkDocument(false);
        }
    }

    /**
     * Imposta i valori degli attributi esistenti dell'elemento corrente.
     */
    public void setAttributeValues(Map values)
    {
        attributeOperations = null;

        Set attrSet = values.keySet();
        String attrs[] = new String[attrSet.size()];
        attrSet.toArray(attrs);
        for (int i = 0; i < attrs.length; ++i) {
            String attr = attrs[i];
            if (currentElement.hasAttribute(attr)) {
                currentElement.setAttribute(attr, (String) values.get(attr));
            }
        }

        // modifica avvenuta: resettiamo l'oggetto per gli XPATH
        //
        xpathAPI.reset();
        warningManager.calculateAffectedElements(currentElement, false);
        checkDocument(false);

        // Se l'elemento corrente non presenta errori, allora possiamo
        // bloccare gli attributi #Freezed
        //
        if (warningManager.getWarnings(currentElement) == null) {
            unlockedElements.remove(currentElement);
        }

        dtreeManager.updateName(currentElement);
    }

    // ---------------------------------------------------------------------------
    // METODI PER LA GESTIONE DEL MENU DELLE OPERAZIONI
    // ---------------------------------------------------------------------------

    public void setMenuAction(MenuAction action)
    {
        menuActions.put(action.getKey(), action);
    }

    public MenuAction getMenuAction(String key)
    {
        return (MenuAction) menuActions.get(key);
    }

    public void addDefaultMenuActions()
    {

        SearchCurrentDocumentAction searchAction = new SearchCurrentDocumentAction("search", "Search",
                "Search into current document", null);
        setMenuAction(searchAction);

        XQueryProcessorAction xQueryProcessorAction = new XQueryProcessorAction("xquery", "XQuery",
                "XQuery Process into current document", null);
        setMenuAction(xQueryProcessorAction);

        DownloadMenuAction action;

        action = new DownloadMenuAction("dl", "Download document", "Download the document in XML format", "_max_xml_");
        action.setWithDoctype(true);
        action.setWholeDocument(true);
        action.setContentType("text/xml");
        setMenuAction(action);

        action = new DownloadMenuAction("vw", "View document", "View the document in XML format", "_max_xml_");
        action.setWithDoctype(false);
        action.setWholeDocument(true);
        action.setContentType("text/xml");
        setMenuAction(action);

        action = new DownloadMenuAction("ve", "View element", "View the current element in XML format", "_max_xml_");
        action.setWithDoctype(false);
        action.setWholeDocument(false);
        action.setContentType("text/xml");
        setMenuAction(action);
    }

    // ---------------------------------------------------------------------------
    // GESTIONE DEI CHECK
    // ---------------------------------------------------------------------------

    public void setAutoCheck(boolean value)
    {
        autoCheck = value;
        if (autoCheck) {
            checkDocument(true);
        }
    }

    /**
     * Equivalente a <code>checkDocument(true)</code>
     * 
     * @deprecated replaced with <i>checkDocument(boolean)</i>
     * @see #checkDocument(boolean)
     */
    @Deprecated
    public void checkDocument()
    {
        checkDocument(true);
    }

    public void checkDocument(boolean force)
    {

        /***********************************************************************
         * if (!force && !autoCheck) { needsCheck = true; return; }
         * 
         * needsCheck = false; // Configured warnings //
         * warningManager.check(document); // Invalid ANY warnings // Iterator
         * iterator = wrongANYelements.entrySet().iterator(); while
         * (iterator.hasNext()) { Map.Entry entry = (Map.Entry) iterator.next();
         * warningManager.addWarning(new Warning((String) (entry.getValue()),
         * ((Element) (entry.getKey())))); } // Invalid structure warnings //
         * iterator = invalidStructureElements.iterator(); while
         * (iterator.hasNext()) { Element element = (Element) iterator.next();
         * warningManager.addWarning(new Warning("Element '" +
         * element.getNodeName() + "' does not match the DTD", element)); }
         **********************************************************************/

        if (!warningManager.isStarted()) {
            warningManager.startChecks(getDocument());
        }
        // Configured warnings
        //
        warningManager.check(force || autoCheck);

    }

    public void addCheck(Check check)
    {
        warningManager.addCheck(check);
    }

    // ---------------------------------------------------------------------------
    // GESTIONE DELLA MODALITA' GRAFICA
    // ---------------------------------------------------------------------------
    public void setGraphicMode(boolean graphicMode)
    {
        this.graphicMode = graphicMode;
    }

    // ---------------------------------------------------------------------------
    // GESTIONE DEI WARNINGS
    // ---------------------------------------------------------------------------

    public Warning getWarning(String key)
    {
        return warningManager.getWarning(key);
    }

    /**
     * As isDocumentInError(true).
     * 
     * @deprecated replaced by isDocumentInError(boolean)
     * @return true if the document has some errors
     */
    @Deprecated
    public boolean isDocumentInError()
    {
        return isDocumentInError(true);
    }

    public boolean isDocumentInError(boolean forceCheck)
    {

        if (forceCheck) {

            checkDocument(true);
        }

        return warningManager.getAllWarnings(document).size() > 0;

    }

    public void goToWarnedElement(Warning warning)
    {
        setAnchorNode(null);
        Element warnedElement = warning.getElement();
        Node parent = warnedElement.getParentNode();
        while (parent != null) {
            if (parent == document) {
                setCurrentElement(warnedElement);
                return;
            }
            parent = parent.getParentNode();
        }

        // Il warning si riferisce ad un elemento non pi� nel documento
        //
        warningManager.removeWarning(warning);
    }

    // ---------------------------------------------------------------------------
    // METODI PER LA PRODUZIONE DELL'XML PER L'INTERFACCIA per i WARNINGS
    // ---------------------------------------------------------------------------

    /**
     * Restituisce un DOM che descrive l'interfaccia.
     * 
     * @throws XMLConfigException
     */
    public Document getWarningsInterface() throws XMLConfigException
    {
        Document intfc = documentBuilder.newDocument();
        Element root = intfc.createElement("warnings");
        intfc.appendChild(root);
        String unique = "unique=" + Math.random();
        root.setAttribute("invoke", XMLConfig.get(MaxXMLFactory.XML_CONF, "/xml/context/@invoke") + "?operation=ui&"
                + unique);
        root.setAttribute("context", XMLConfig.get(MaxXMLFactory.XML_CONF, "/xml/context/@context"));

        Collection warnings = warningManager.getAllWarnings(document);
        if ((warnings != null) && (warnings.size() > 0)) {
            Iterator iterator = warnings.iterator();
            while (iterator.hasNext()) {
                Warning warn = (Warning) iterator.next();
                Element warnElement = intfc.createElement("warning");
                warnElement.setAttribute("key", warn.getKey());
                warnElement.setAttribute("text", warn.getWarning());
                root.appendChild(warnElement);
            }
        }
        return intfc;
    }

    // ---------------------------------------------------------------------------
    // METODI PER LA PRODUZIONE DELL'XML PER L'INTERFACCIA
    // ---------------------------------------------------------------------------

    private boolean isInsertRow = false;

    private boolean isSelectRow = true;

    /**
     * Restituisce un DOM che descrive l'interfaccia.
     */
    public Document getInterface() throws MaxException, XMLConfigException
    {
        isInsertRow = false;
        isSelectRow = true;

        Document intfc = documentBuilder.newDocument();
        Element root = intfc.createElement("interface");
        intfc.appendChild(root);

        if (anchorNode == null) {
            root.setAttribute("anchor", "yes");
        }
        String unique = "unique=" + Math.random();
        root.setAttribute("invoke", XMLConfig.get(MaxXMLFactory.XML_CONF, "/xml/context/@invoke") + "?operation=ui&"
                + unique);
        root.setAttribute("invoke-visual", XMLConfig.get(MaxXMLFactory.XML_CONF, "/xml/context/@invoke")
                + "?operation=graphic&" + unique);
        root.setAttribute("context", XMLConfig.get(MaxXMLFactory.XML_CONF, "/xml/context/@context"));
        root.setAttribute("autocheck", autoCheck ? "yes" : "no");
        if (isDocumentInError(false)) {
            root.setAttribute("warnings", "yes");
        }

        Operation operations[] = getAvailableOperations();

        interfaceForActions(intfc, root);

        if (!readOnly) {
            interfaceForUndoRedo(intfc, root);
        }
        interfaceForAncestors(intfc, root, operations);
        interfaceForElement(intfc, root);
        interfaceForExternalData(intfc, root);
        interfaceForAttributes(intfc, root);
        if (!interfaceForGraphics(intfc, root)) {
            interfaceForOperations(intfc, root, operations);
        }
        interfaceForTree(intfc, root);

        if (isDocumentInError(false)) {
            root.setAttribute("warnings", "yes");
        }

        return intfc;
    }

    private boolean interfaceForGraphics(Document intfc, Element root) throws MaxException
    {
        ElementModel elementModel = documentModel.getElementModel(currentElement.getNodeName());
        String[] graphicParams = elementModel.getGraphicsParams(currentElement);
        if (graphicParams != null) {
            root.setAttribute("graphic-mode", graphicMode ? "yes" : "no");
            if (graphicMode) {
                Element graphics = intfc.createElement("graphics");
                graphics.setAttribute("xsl-in", graphicParams[0]);
                graphics.setAttribute("xsl-out", graphicParams[1]);
                root.appendChild(graphics);
            }
            return graphicMode;
        }
        return false;
    }

    private void interfaceForTree(Document intfc, Element root)
    {
        dtreeManager.setCurrentElement(currentElement);
        Element tree = dtreeManager.getTreeFor(intfc);
        root.appendChild(tree);
    }

    private void interfaceForActions(Document intfc, Element root)
    {
        Iterator i = menuActions.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            MenuAction act = (MenuAction) e.getValue();
            Element node = intfc.createElement("action");
            node.setAttribute("label", act.getLabel());
            node.setAttribute("text", act.getDescription());
            node.setAttribute("key", act.getKey());
            String target = act.getTarget();
            if (target != null) {
                node.setAttribute("target", target);
            }
            root.appendChild(node);
        }
    }

    private void interfaceForUndoRedo(Document intfc, Element root)
    {
        if (startPosition < currentPosition) {
            Element undo = intfc.createElement("undo");
            ExecOperation exec = undoList[(currentPosition - 1) % UNDO_SIZE];
            undo.setAttribute("text", exec.description);
            undo.setAttribute("operation", exec.type);
            root.appendChild(undo);
        }
        if (currentPosition < endPosition) {
            Element redo = intfc.createElement("redo");
            ExecOperation exec = undoList[currentPosition % UNDO_SIZE];
            redo.setAttribute("text", exec.description);
            redo.setAttribute("operation", exec.type);
            root.appendChild(redo);
        }
    }

    private void interfaceForElement(Document intfc, Node root)
    {
        String elementName = currentElement.getNodeName();
        String contentModel = documentModel.getContentModel(currentElement.getNodeName()).toString();

        Element element = intfc.createElement("element");
        root.appendChild(element);
        element.setAttribute("element-name", elementName);
        element.setAttribute("content-model", contentModel);

        Element description = intfc.createElement("description");
        String comment = getCurrentElementComment();
        Text txt = intfc.createTextNode(comment);
        description.appendChild(txt);
        description.setAttribute("popup-style", makePopupString(elementName, contentModel, comment));
        element.appendChild(description);

        Collection warnings = warningManager.getWarnings(currentElement);
        if ((warnings != null) && (warnings.size() > 0)) {
            Iterator iterator = warnings.iterator();
            while (iterator.hasNext()) {
                Warning warning = (Warning) iterator.next();
                Element warnElement = intfc.createElement("warning");
                warnElement.setAttribute("warn", warning.getWarning());
                warnElement.setAttribute("key", warning.getKey());
                element.appendChild(warnElement);
            }
        }
    }

    private void interfaceForExternalData(Document intfc, Node root)
    {
        Element externalData = produceExternalDataElement(intfc, currentElement, ExternalData.ALL
                | ExternalData.ELEMENT);
        if (externalData != null) {
            root.appendChild(externalData);
        }
    }

    private void interfaceForAncestors(Document intfc, Node root, Operation ops[])
    {
        Element ancestors = intfc.createElement("ancestors");
        boolean found = false;

        for (int i = ops.length - 1; i >= 0; --i) {
            if (ops[i].getType() == Operation.T_SELECT_P) {
                found = true;
                Element element = intfc.createElement("ancestor");
                ancestors.appendChild(element);
                element.setAttribute("element-name", ops[i].getNode().getNodeName());
                element.setAttribute("key", ops[i].getKey());

                Node node = ops[i].getNode();
                if (node instanceof Element) {
                    Element parentElement = (Element) node;
                    ElementModel elementModel = documentModel.getElementModel(parentElement.getNodeName());
                    String label = elementModel.getLabel(parentElement);
                    if (label != null) {
                        element.setAttribute("label", label);
                    }
                }
            }
        }
        if (found) {
            root.appendChild(ancestors);
        }
    }

    private void interfaceForOperations(Document intfc, Node root, Operation ops[]) throws MaxException
    {
        Element operations = intfc.createElement("operations");
        RowDescriptor row = new RowDescriptor(intfc, "row");
        boolean found = false;

        ElementModel elementModel = documentModel.getElementModel(currentElement.getNodeName());
        TableSet tableSet = elementModel.getTableSet(currentElement);

        for (int i = 0; i < ops.length; ++i) {
            if (!tableSet.assignToTable(ops[i])) {

                // Nodo non dentro una tabella.
                // Interfaccia classica.
                //
                buildOperationElement(intfc, ops[i], operations, row);
                found = true;
            }
        }

        row.reset(operations);

        // Gestione #Table
        //
        found |= buildTables(intfc, tableSet, operations);

        if (found) {
            root.appendChild(operations);
        }
    }

    private boolean buildTables(Document intfc, TableSet tableSet, Element operations) throws MaxException
    {
        boolean exists = false;

        Iterator i = tableSet.iterator();
        while (i.hasNext()) {
            exists = true;
            Table table = (Table) i.next();
            Element tableElement = intfc.createElement("table");
            tableElement.setAttribute("description", table.getDescription());
            operations.appendChild(tableElement);

            Element header = intfc.createElement("theader");
            tableElement.appendChild(header);

            for (int j = 0; j < table.getNumColumns(); ++j) {
                Element tcell = intfc.createElement("hcell");
                header.appendChild(tcell);
                tcell.setAttribute("label", table.getLabel(j));
            }

            buildTableRows(intfc, table, tableElement);
        }

        return exists;
    }

    private void buildTableRows(Document intfc, Table table, Element tableElement) throws MaxException
    {
        RowDescriptor row = new RowDescriptor(intfc, "trow");

        Iterator i = table.operationIterator();
        while (i.hasNext()) {
            Operation operation = (Operation) i.next();
            Node node = buildOperationElement(intfc, operation, tableElement, row);
            if (node != null) {
                if (operation.getType() == Operation.T_SELECT) {
                    buildTableCells(intfc, table, row, operation.getNode(), (Element) node);
                }
            }
        }
        row.reset(tableElement);

        i = table.insertOperationIterator();
        boolean found = false;
        while (i.hasNext()) {
            Operation operation = (Operation) i.next();
            buildOperationElement(intfc, operation, tableElement, row);
            found = true;
        }
        if (found) {
            buildEmptyCell(intfc, table, row);
        }
        row.reset(tableElement);
    }

    private void buildTableCells(Document intfc, Table table, RowDescriptor row, Node node, Element selectElement)
    {
        NodeList list = selectElement.getElementsByTagName("external-data");
        Node externalData = null;
        if (list.getLength() > 0) {
            externalData = list.item(0);
        }
        if (externalData == null) {
            externalData = node;
        }

        for (int i = 0; i < table.getNumColumns(); ++i) {
            Element cell = intfc.createElement("tcell");
            cell.setAttribute("colspan", "1");
            row.appendChild(cell);
            XPath xpath = table.getXPath(i);
            try {
                boolean isExternal = table.isExternal(i);
                NodeList nodeList = xpathAPI.selectNodeList(isExternal ? externalData : node, xpath);
                for (int j = 0; j < nodeList.getLength(); ++j) {
                    Element value = intfc.createElement("value");
                    cell.appendChild(value);
                    value.setAttribute("value", XPathAPI.getNodeValue(nodeList.item(j)));
                }
            }
            catch (Exception exc) {
                Element value = intfc.createElement("value");
                cell.appendChild(value);
                value.setAttribute("value", "" + exc);
            }
        }
    }

    private void buildEmptyCell(Document intfc, Table table, RowDescriptor row)
    {
        int n = table.getNumColumns();
        Element cell = intfc.createElement("tcell");
        row.appendChild(cell);
        cell.setAttribute("colspan", "" + n);
    }

    private Node buildOperationElement(Document intfc, Operation operation, Element parentElement, RowDescriptor row)
            throws MaxException
    {
        Element elmnt = null;
        int type = operation.getType();

        // Gestione righe.
        // SELECT vanno su righe nuove se la precedente � una riga dello
        // stesso tipo o una riga con un'operazione INSERT.
        // Le operazioni INSERT vanno su una nuova riga se la precedente non
        // � INSERT.
        // Le operazioni DELETE e CHANGE cambiano riga se le precedenti sono
        // SELECT o INSERT.
        // (EDIT e EDIT_ANY sono gestite come SELECT).
        //
        switch (type) {
            case Operation.T_SELECT :
            case Operation.T_EDIT :
            case Operation.T_EDIT_ANY :
                if (isSelectRow || isInsertRow) {
                    row.reset(parentElement);
                }
                isInsertRow = false;
                isSelectRow = true;
                break;

            case Operation.T_DELETE :
            case Operation.T_CHANGE :
            case Operation.T_PASTE :
            case Operation.T_COPY :
            case Operation.T_CUT :
                if (isSelectRow || isInsertRow) {
                    row.reset(parentElement);
                }
                isInsertRow = false;
                isSelectRow = false;
                break;

            case Operation.T_INSERT_A :
            case Operation.T_INSERT_B :
            case Operation.T_PASTE_A :
            case Operation.T_PASTE_B :
                if (!isInsertRow) {
                    row.reset(parentElement);
                }
                isInsertRow = true;
                isSelectRow = false;
                break;
        }

        // Inserimento operazioni
        //
        switch (type) {

            case Operation.T_DELETE : {
                row.addMenuItem("Delete", operation.getKey(), "delete", "Delete the '"
                        + operation.getContentModelInstance().getContentModel().toString() + "'");
            }
                break;

            case Operation.T_COPY : {
                row.addMenuItem("Copy", operation.getKey(), "copy", "Copy the '"
                        + operation.getContentModelInstance().getContentModel().toString() + "' into the clipboard");
            }
                break;

            case Operation.T_CUT : {
                row.addMenuItem("Cut", operation.getKey(), "cut", "Cut the '"
                        + operation.getContentModelInstance().getContentModel().toString() + "' into the clipboard");
            }
                break;

            case Operation.T_SELECT : {
                elmnt = intfc.createElement("select");
                elmnt.setAttribute("key", operation.getKey());
                Element element = (Element) operation.getNode();
                elmnt.setAttribute("element-name", element.getNodeName());
                if (operation.getNode() == newNode) {
                    elmnt.setAttribute("new-element", "yes");
                }
                if (operation.getNode() == anchorNode) {
                    row.setAttribute("anchor", "yes");
                }

                ElementModel elementModel = documentModel.getElementModel(element.getNodeName());
                String label = elementModel.getLabel(element);
                if (label != null) {
                    elmnt.setAttribute("label", label);
                }

                if (elementModel.isHidden(element)) {
                    elmnt.setAttribute("is-hidden", "yes");
                }
                else {
                    elmnt.setAttribute("is-hidden", "no");
                }

                Collection warnings = warningManager.getAllWarnings(element);
                if (warnings.size() > 0) {
                    Iterator iterator = warnings.iterator();
                    while (iterator.hasNext()) {
                        Warning warning = (Warning) iterator.next();
                        Element warnElement = intfc.createElement("warning");
                        warnElement.setAttribute("warn", warning.getWarning());
                        warnElement.setAttribute("key", warning.getKey());
                        elmnt.appendChild(warnElement);
                    }
                }
                details(intfc, elmnt, operation);
                row.appendChild(elmnt);
            }
                break;

            case Operation.T_CHANGE : {
                row.addMenuItem(operation.getContentModel().toString(), operation.getKey(), "change", "Change to '"
                        + operation.getContentModel().toString() + "'");
            }
                break;

            case Operation.T_PASTE : {
                row.addMenuItem(operation.getContentModel().toString(), operation.getKey(), "paste",
                        "Replace with the '" + operation.getContentModel().toString() + "' from the clipboard");
            }
                break;

            case Operation.T_INSERT_A :
            case Operation.T_INSERT_B : {
                row.addMenuItem(operation.getContentModel().toString(), operation.getKey(), "insert", "Insert a new '"
                        + operation.getContentModel().toString() + "'");
            }
                break;

            case Operation.T_PASTE_A :
            case Operation.T_PASTE_B : {
                row.addMenuItem(operation.getContentModel().toString(), operation.getKey(), "insert-paste", "Paste a '"
                        + operation.getContentModel().toString() + "' from the clipboard");
            }
                break;

            case Operation.T_EDIT : {
                elmnt = intfc.createElement("edit");
                elmnt.setAttribute("key", operation.getKey());
                if (readOnly) {
                    elmnt.setAttribute("readOnly", "true");
                }

                String elementName = currentElement.getNodeName();
                ElementModel elementModel = documentModel.getElementModel(elementName);
                Collection values = elementModel.getValues(currentElement);

                if (values == null) {
                    Element freeText = intfc.createElement("free-text");
                    Text txt = intfc.createTextNode(getPCDATA(currentElement));
                    freeText.appendChild(txt);
                    elmnt.appendChild(freeText);
                }
                else {
                    Element choiceElement = intfc.createElement("choice-element");

                    String value = XPathAPI.getNodeValue(currentElement);
                    Element opt = intfc.createElement("choice");
                    opt.setAttribute("value", value);
                    choiceElement.appendChild(opt);

                    Iterator it = values.iterator();
                    while (it.hasNext()) {
                        String val = (String) it.next();
                        if (!val.equals(value)) {
                            opt = intfc.createElement("choice");
                            opt.setAttribute("value", val);
                            choiceElement.appendChild(opt);
                        }
                    }
                    elmnt.appendChild(choiceElement);
                }

                row.appendChild(elmnt);
            }
                break;

            case Operation.T_EDIT_ANY : {
                elmnt = intfc.createElement("edit");
                elmnt.setAttribute("key", operation.getKey());
                elmnt.setAttribute("isXML", "true");
                if (readOnly) {
                    elmnt.setAttribute("readOnly", "true");
                }

                String elementName = currentElement.getNodeName();
                ElementModel elementModel = documentModel.getElementModel(elementName);
                Collection values = elementModel.getValues(currentElement);

                if (values == null) {
                    Element freeText = intfc.createElement("free-text");

                    if (wrongANYelements.containsKey(currentElement)) {
                        Text txt = intfc.createTextNode(getPCDATA(currentElement));
                        freeText.appendChild(txt);
                    }
                    else {
                        StringWriter out = new StringWriter();
                        DOMWriter dw = new DOMWriter();
                        try {
                            dw.write(currentElement.getChildNodes(), new PrintWriter(out));
                        }
                        catch (IOException exc) {
                            exc.printStackTrace();
                            throw new MaxException(exc);
                        }

                        String elementRepresentation = stripSlashR(out.toString());

                        Text txt = intfc.createTextNode(elementRepresentation);
                        freeText.appendChild(txt);
                    }
                    elmnt.appendChild(freeText);
                }
                else {
                    Element choiceElement = intfc.createElement("choice-element");

                    String value = XPathAPI.getNodeValue(currentElement);
                    Element opt = intfc.createElement("choice");
                    opt.setAttribute("value", value);
                    choiceElement.appendChild(opt);

                    Iterator it = values.iterator();
                    while (it.hasNext()) {
                        String val = (String) it.next();
                        if (!val.equals(value)) {
                            opt = intfc.createElement("choice");
                            opt.setAttribute("value", val);
                            choiceElement.appendChild(opt);
                        }
                    }
                    elmnt.appendChild(choiceElement);
                }

                row.appendChild(elmnt);
            }
                break;
        }
        return elmnt;
    }

    private void interfaceForAttributes(Document intfc, Node root)
    {
        String elementName = currentElement.getNodeName();
        Element attributes = intfc.createElement("attributes");
        if (readOnly) {
            attributes.setAttribute("readOnly", "true");
        }
        boolean found = false;
        Operation ops[] = getAttributeOperations();
        Hashtable attrs = new Hashtable();
        Element attrOp;

        for (int i = 0; i < ops.length; ++i) {
            String name = ops[i].getNode().getNodeName();
            String value = ops[i].getNode().getNodeValue();
            AttributeModel model = documentModel.getAttributeModel(elementName, name);

            // Caso di attributo non valido
            //
            if (model == null) {
                model = new AttributeModel(elementName, name, "CDATA", null, "#FIXED", value);
            }

            int type = ops[i].getType();
            attrOp = null;

            switch (type) {

                case Operation.T_ATT_REMOVE :
                    attrOp = intfc.createElement("remove-attribute");
                    break;

                case Operation.T_ATT_EDIT : {
                    if (model.type.equals("")) {
                        attrOp = intfc.createElement("choice-attribute");
                        for (int j = 0; j < model.choices.length; ++j) {
                            if (!model.choices[j].equals(value)) {
                                Element opt = intfc.createElement("choice");
                                opt.setAttribute("value", model.choices[j]);
                                attrOp.appendChild(opt);
                            }
                        }
                    }
                    else {
                        Collection values = model.getValues(currentElement);
                        if (values == null) {
                            attrOp = intfc.createElement("edit-attribute");
                        }
                        else {
                            attrOp = intfc.createElement("choice-attribute");
                            Iterator it = values.iterator();
                            while (it.hasNext()) {
                                String val = (String) it.next();
                                if (!val.equals(value)) {
                                    Element opt = intfc.createElement("choice");
                                    opt.setAttribute("value", val);
                                    attrOp.appendChild(opt);
                                }
                            }
                        }
                    }
                }
                    break;

                case Operation.T_ATT_FIXED :
                    attrOp = intfc.createElement("view-attribute");
                    break;

                case Operation.T_ATT_ADD :
                    attrOp = intfc.createElement("add-attribute");
                    break;
            }
            if (attrOp != null) {
                found = true;

                attrOp.setAttribute("key", ops[i].getKey());

                Element attr = (Element) attrs.get(name);
                if (attr == null) {
                    attr = intfc.createElement("attribute");
                    attributes.appendChild(attr);
                    attr.setAttribute("attribute-name", name);
                    attr.setAttribute("attribute-value", value);
                    attr.setAttribute("attribute-model", model.toString());
                    Element description = intfc.createElement("description");
                    String comment = model.getComment();
                    Text text = intfc.createTextNode(comment);
                    description.appendChild(text);
                    description.setAttribute("popup-style", makePopupString(name, model.toString(), comment));
                    attr.appendChild(description);
                    attrs.put(name, attr);
                }
                attr.appendChild(attrOp);
            }
        }
        if (found) {
            root.appendChild(attributes);
        }
    }

    private Element produceExternalDataElement(Document intfc, Element element, int when)
    {
        ElementModel elementModel = documentModel.getElementModel(element.getNodeName());
        if (element == null) {
            return null;
        }
        ExternalData ed[] = elementModel.getExternalData(element, when);
        if (ed == null) {
            return null;
        }

        Element externalData = intfc.createElement("external-data");

        for (int i = 0; i < ed.length; ++i) {
            Node externalNode = ed[i].getData(intfc, this, element);
            if (externalNode != null) {
                externalData.appendChild(externalNode);
            }
        }

        return externalData;
    }

    private void details(Document intfc, Element elmnt, Operation op) throws MaxException
    {
        String detailsStr = applyDetailTransformer(op.getNode()).trim();

        if (!detailsStr.equals("")) {
            Element details = intfc.createElement("details");
            details.appendChild(intfc.createTextNode(detailsStr));
            elmnt.appendChild(details);
        }

        if (op.getNode() instanceof Element) {
            Element element = (Element) op.getNode();
            Element externalData = produceExternalDataElement(intfc, element, ExternalData.ALL | ExternalData.CHILD);
            if (externalData != null) {
                elmnt.appendChild(externalData);
            }
        }
    }

    public String applyDetailTransformer(Node nd) throws MaxException
    {
        if (detailTransformer == null) {
            ContentModel cm = documentModel.getContentModel(nd.getNodeName());
            if (cm.type == ContentModel.T_SIMPLE_PCDATA) {
                if (nd instanceof Element) {
                    return getPCDATA((Element) nd);
                }
                else {
                    return nd.getNodeValue();
                }
            }
            return "";
        }
        else {
            synchronized (detailTransformer) {
                DOMSource source = new DOMSource(nd);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                StreamResult result = new StreamResult(out);
                try {
                    detailTransformer.transform(source, result);
                    return out.toString();
                }
                catch (Exception exc) {
                    throw new MaxException(exc);
                }
            }
        }
    }

    public String getPCDATA(Element elmnt)
    {
        NodeList nodes = elmnt.getChildNodes();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node n = nodes.item(i);
            if (n instanceof Text) {
                sb.append(n.getNodeValue());
            }
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------------------
    // METODI PER LA SCRITTURA DEL FILE
    // ---------------------------------------------------------------------------

    /**
     * Scrive il Document su file.
     */
    public void writeTo(String file) throws MaxException
    {
        writeTo(new StreamResult(new File(file)));
    }

    /**
     * Scrive il Document su un dato Result.
     */
    public void writeTo(Result result) throws MaxException
    {
        try {
            DocumentType doctype = document.getDoctype();
            String publicId = doctype.getPublicId();
            String systemId = doctype.getSystemId();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
            if (publicId != null) {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, publicId);
            }
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemId);
            transformer.transform(new DOMSource(document), result);
        }
        catch (TransformerConfigurationException exc) {
            throw new MaxException(exc);
        }
        catch (TransformerException exc) {
            throw new MaxException(exc);
        }
    }

    // ---------------------------------------------------------------------------
    // SUPPORTO ALLE FINESTRE DI POPUP
    // ---------------------------------------------------------------------------

    private String makePopupString(String title, String subtitle, String content)
    {
        StringBuffer sb = new StringBuffer();
        if (title != null) {
            sb.append("<h2>").append(encode(title)).append("</h2>\n");
        }
        if (subtitle != null) {
            sb.append("<b>").append(encode(subtitle)).append("</b><p>\n");
        }
        if (content != null) {
            sb.append("<pre>").append(encode(content)).append("</pre>");
        }

        StringBuffer jscontent = new StringBuffer();
        StringTokenizer tk = new StringTokenizer(sb.toString(), "\r\n\t'\\", true);
        while (tk.hasMoreTokens()) {
            String s = tk.nextToken();
            if (s.length() == 1) {
                if (s.equals("\r")) {
                    jscontent.append("\\r");
                }
                else if (s.equals("\n")) {
                    jscontent.append("\\n");
                }
                else if (s.equals("\t")) {
                    jscontent.append("\\t");
                }
                else if (s.equals("'")) {
                    jscontent.append("\\'");
                }
                else if (s.equals("\\")) {
                    jscontent.append("\\\\");
                }
                else {
                    jscontent.append(s);
                }
            }
            else {
                jscontent.append(s);
            }
        }
        return jscontent.toString();
    }

    private String encode(String str)
    {
        StringBuffer res = new StringBuffer();
        StringTokenizer tk = new StringTokenizer(str, "<>&", true);
        while (tk.hasMoreTokens()) {
            String s = tk.nextToken();
            if (s.equals("<")) {
                res.append("&lt;");
            }
            else if (s.equals(">")) {
                res.append("&gt;");
            }
            else if (s.equals("&")) {
                res.append("&amp;");
            }
            else {
                res.append(s);
            }
        }
        return res.toString();
    }

    // ---------------------------------------------------------------------------
    // METODI PER LA LINEA DI COMANDO
    // ---------------------------------------------------------------------------

    private void printCurrent()
    {
        ContentModelInstance cmi = getCurrentContentModelInstance();
        MaxConsole.println("---------------------------------------------------------------------");
        MaxConsole.println(currentElement.getNodeName());
        MaxConsole.println("---------------------------------------------------------------------");
        MaxConsole.println("" + cmi);
        MaxConsole.println("- - - - - - - - - - - - - - - -");
        Operation ops[] = getAvailableOperations();
        for (int i = 0; i < ops.length; ++i) {
            MaxConsole.println("" + ops[i].getKey() + ": " + ops[i]);
        }
        MaxConsole.println("---------------------------------------------------------------------");

        try {
            Node intfc = getInterface();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.transform(new DOMSource(intfc), new StreamResult(out));

            MaxConsole.println(out.toString());
        }
        catch (Exception exc) {
            MaxConsole.println("" + exc);
        }

        MaxConsole.println("");
    }

    public void workOnDocument() throws Exception
    {
        printCurrent();
        while (true) {
            String operation = MaxConsole.input("Operation or !command (!help for help).....: ");
            if (operation.startsWith("!")) {
                String cmd = operation.substring(1);
                if (cmd.equals("help")) {
                    MaxConsole.println("");
                    MaxConsole.println("Commands");
                    MaxConsole.println("-------------------------------------------------------");
                    MaxConsole.println("!help           - shows this help");
                    MaxConsole.println("!write filename - write the XML to the specified file");
                    MaxConsole.println("!show           - show the current element");
                    MaxConsole.println("-------------------------------------------------------");
                    MaxConsole.println("");
                }
                else if (cmd.startsWith("write ")) {
                    String fileName = cmd.substring(6).trim();
                    writeTo(fileName);
                }
                else if (cmd.startsWith("show")) {
                    printCurrent();
                }
            }
            else {
                String param = "";
                int idx = operation.indexOf(" ");
                if (idx != -1) {
                    param = operation.substring(idx).trim();
                    operation = operation.substring(0, idx).trim();
                }
                doOperation(operation, param);
                printCurrent();
            }
        }
    }

    public static void main(String args[]) throws Exception
    {
        String xml = MaxConsole.input("XML file....: ");
        XMLBuilder builder = new XMLBuilder(xml);
        builder.workOnDocument();
    }
}
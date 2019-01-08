package max.documents;

import it.greenvulcano.catalog.GVCatalogResolver;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.gvconsole.deploy.GVDeploy;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import max.core.ContentProvider;
import max.core.Contents;
import max.core.MaxException;
import max.xml.Check;
import max.xml.XMLBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class is a main class used to manage document and their versions.
 * Provides operations for navigate the repository, start edit operation, shows
 * the full document list, roll-back to a previous version. The
 * DocumentRepository is a singleton, this mean that no instance can be created
 * using constructors. The only way to obtains a DocumentRepository object is
 * using instance() operation. This operation instantiate a DocumentRepository
 * the first call, subsequent call will return the same object. The instance()
 * operation is a static operation. To enforce data integrity in a multi-thread
 * environment, all public operations are synchronized.
 *
 * <br/>
 */
public class DocumentRepository
{

    /**
     * This variable indicates the name of the file xml to work
     */
    private static final String             CONFIGURATION_FILENAME = "documentRepository.xml";

    /**
     * DocumentRepository object to invoke the class
     */
    private static DocumentRepository       _instance              = null;

    /**
     * This variable contains Name-Value couples, where NAME is the name of the
     * document and VALUE is the DocumentDescriptor object(name, label, descr,
     * rwRoles, rrRoles, extRoles).
     */
    private Map<String, DocumentDescriptor> docDescrMap            = null;

    /**
     * Provider name for filters.
     */
    private String                          filterProviderName     = null;

    private Document                        documentRepository;

    /**
     * DocumentRepository Constructor
     *
     * @throws XMLConfigException
     */
    private DocumentRepository() throws XMLConfigException
    {
        documentRepository = XMLConfig.getDocument(CONFIGURATION_FILENAME, DocumentRepository.class.getClassLoader(),
                true, false);
    }

    private ContentProvider getFilterProvider() throws MaxException, XMLConfigException
    {
        if (filterProviderName == null) {
            filterProviderName = XMLConfig.get(documentRepository, "/documents/@filter-provider");
        }
        return Contents.instance().getProvider(filterProviderName);
    }

    /**
     * It used to instantiate the class.
     *
     * @return the DocumentRepository instance
     * @throws XMLConfigException
     */
    public static synchronized DocumentRepository instance() throws XMLConfigException
    {
        if (_instance == null) {
            _instance = new DocumentRepository();
        }

        return _instance;
    }

    /**
     * This method serves to take all Document names.
     *
     * @return String[] The list of the names.
     */

    public String[] getDocumentNames() throws MaxException, XMLConfigException
    {
        Map docDescrMap = createDescriptors();

        String documentNames[] = new String[docDescrMap.size()];
        Set set = docDescrMap.keySet();

        set.toArray(documentNames);
        return documentNames;
    }

    /**
     * This method serves to find all the characteristics of a generic Document
     * name.
     *
     * @return DocumentDescriptor The DocumentDescriptor object to find its
     *         details.
     */
    public DocumentDescriptor getDocumentDescriptor(String name) throws MaxException, XMLConfigException
    {
        Map docDescrMap = createDescriptors();

        return (DocumentDescriptor) docDescrMap.get(name);
    }

    /**
     * This method will construct a file XML of the version the latest of the
     * category.
     *
     * @param RoleCheck
     *        Object to control like role has the user.
     * @param group
     *        the document group.
     * @param checkWarnings
     *        if true the warnings must be check.
     *
     * @return Document The file XML as soon as sconstructed.
     */
    public Document showRepository(RoleCheck roleCheck, String group, boolean checkWarnings) throws MaxException,
            XMLConfigException
    {
        if (group == null) {
            group = "";
        }
        // XMLConfig.reload(CONFIGURATION_FILE);
        Map docDescrMap = createDescriptors();

        Set groups = new HashSet();
        Document document = createDocument();
        Element root = createTag(document, "document-list");
        document.appendChild(root);
        Iterator docDescrMapIterator = (docDescrMap.values()).iterator();
        while (docDescrMapIterator.hasNext()) {
            DocumentDescriptor descriptor = (DocumentDescriptor) docDescrMapIterator.next();
            Element documentItem = prepareDocumentItem(document, descriptor, roleCheck);
            if (documentItem != null) {
                groups.add(descriptor.getGroup());
                if (descriptor.getGroup().equals(group)) {
                    if (checkWarnings) {
                        checkWarnings(documentItem);
                    }

                    root.appendChild(documentItem);
                }
            }
        }

        // Costruisce l'elemento 'groups'.
        // L'insieme 'groups' contiene tutti e soli i gruppi che contengono
        // almeno un documento
        // per cui l'utente ha qualche diritto di accesso.
        //
        if (groups.size() > 0) {
            Element groupsElement = document.createElement("groups");
            root.appendChild(groupsElement);
            buildGroupNode(group, groupsElement);
            groups.remove(group);
            Iterator i = groups.iterator();
            while (i.hasNext()) {
                String groupName = (String) i.next();
                buildGroupNode(groupName, groupsElement);
            }
        }

        return document;
    }

    /**
     * This method will construct a file XML of the version the latest of the
     * category.
     *
     * @param descriptors
     *        .
     *
     * @param roleCheck
     *        Object to control like role has the user.
     *
     * @return Document The file XML as soon as constructed.
     * @throws MaxException
     *         if an error occurs.
     * @throws XMLConfigException
     */
    public Document showDocuments(RoleCheck roleCheck, Collection<?> descriptors) throws MaxException,
            XMLConfigException
    {
        Document document = createDocument();
        Element root = createTag(document, "document-list");
        document.appendChild(root);
        Iterator<?> it = descriptors.iterator();
        while (it.hasNext()) {
            DocumentDescriptor descriptor = (DocumentDescriptor) it.next();
            Element documentItem = prepareDocumentItem(document, descriptor, roleCheck);
            if (documentItem != null) {
                root.appendChild(documentItem);
            }
        }

        return document;
    }

    private void buildGroupNode(String groupName, Node parent) throws XMLConfigException
    {
        Document doc = parent.getOwnerDocument();
        String label;
        if (groupName.equals("")) {
            label = "< default >";
        }
        else {
            Node groupNode = XMLConfig.getNode(documentRepository, "/documents/group[@name='" + groupName + "']");
            label = XMLConfig.get(groupNode, "@label");
        }
        Element groupElement = doc.createElement("group");
        groupElement.setAttribute("name", groupName);
        groupElement.setAttribute("label", label);
        parent.appendChild(groupElement);
    }

    /**
     * This method will construct a file XML of all the versions of the
     * category.
     *
     * @param RoleCheck
     *        Object to control like role has the user.
     *
     * @return Document The file XML as soon as constructed.
     */
    public Document showHistory(String name, RoleCheck roleCheck) throws MaxException, XMLConfigException
    {
        Document document = createDocument();
        Element root = createTag(document, "document-history");
        document.appendChild(root);

        prepareDocumentHistory(document, getDocumentDescriptor(name), roleCheck);

        return document;
    }

    /**
     * Starts an edit operation.
     *
     * @param name
     *        document to edit
     *
     * @return the XMLBuilder object that manages the editing.
     */
    public XMLBuilder editDocument(String name, ServletContext servletContext, boolean readOnly) throws MaxException,
            XMLConfigException
    {
        Map docDescrMap = createDescriptors();
        DocumentProxy proxy = getDocumentProxy(name);

        SaveDocumentAction save = new SaveDocumentAction("save", "Save", "Save the document and terminate the editing",
                null);

        save.setDocumentProxy(proxy);
        save.setDocumentDescriptor((DocumentDescriptor) docDescrMap.get(name));

        // Per le specifiche servlet 2.2
        save.setServletContext(servletContext);

        Document doc = proxy.load();
        DiscardDocumentAction discard = new DiscardDocumentAction("discard", "Discard", "Discard the document", null);

        // Per le specifiche servlet 2.2
        discard.setServletContext(servletContext);

        XMLBuilder builder = new XMLBuilder(doc);
        builder.setReadOnly(readOnly);
        if (!readOnly) {
            builder.setMenuAction(save);
            builder.setMenuAction(discard);
        }
        builder.addDefaultMenuActions();

        Check check = getCheckClass(name);
        if (check != null) {
            builder.addCheck(check);
            builder.checkDocument(false);

        }
        MaxDocumentXPathFunction.setDocumentName(doc, name);

        return builder;
    }

    /**
     *
     *
     *
     */
    public Document getDocument(String name) throws MaxException
    {
        DocumentProxy proxy = getDocumentProxy(name);
        Document document = proxy.load();
        URL baseUrl = proxy.getURL();
        if ((baseUrl != null) && XMLConfig.isCompositeXMLConfig(document)) {
            try {
                document = XMLConfig.readCompositeXMLConfig(baseUrl, document);
            }
            catch (XMLConfigException exc) {
                throw new MaxException(exc);
            }
        }
        return document;

    }

    /**
     * Read all documents with name matching the given regular expression.
     *
     * @param reName
     *        regular expression used to match the document names
     * @return a new Map containing all documents with name matching the given
     *         regular expression. The keys are the actual document names the
     *         values are the documents.
     */
    public Map getDocuments(String reName) throws MaxException, XMLConfigException
    {
        Map docDescrMap = createDescriptors();

        Map result = new HashMap();

        Pattern re = Pattern.compile(reName);

        for (Iterator it = docDescrMap.keySet().iterator(); it.hasNext();) {
            String name = (String) it.next();
            Matcher matcher = re.matcher(name);
            if (matcher.matches()) {
                result.put(name, getDocument(name));
            }
        }

        return result;
    }

    /**
     *
     *
     *
     */
    public DocumentProxy getDocumentProxy(String name) throws MaxException
    {
        DocumentProxy proxy = null;
        try {
            Node proxyNode = XMLConfig.getNode(documentRepository, "//document[@name='" + name + "']/*[@type='proxy']");
            String proxyClass = XMLConfig.get(proxyNode, "@class");
            proxy = (DocumentProxy) Class.forName(proxyClass).newInstance();
            proxy.init(proxyNode);
        }
        catch (Exception exc) {
            throw new MaxException("document=" + name, exc);
        }

        return proxy;
    }

    /**
     * @throws XMLConfigException
     *
     *
     *
     */
    public Document getDocumentVersion(String name, int version) throws MaxException, XMLConfigException
    {

        InputStream inputStream = VersionManager.instance().getDocument(name, version);
        Document document = createDocument(inputStream);
        return document;
    }

    public boolean hasFilter(String name) throws MaxException, XMLConfigException
    {
        return getFilterProvider().exists("filters", name);
    }

    /**
     * This method serves to applicate visualization filter from using for the
     * exsternal systems.
     *
     * @return Document The Document the as soon as filtrate
     */
    public Document applySecurityFilter(String name, Document document, RoleCheck roleCheck) throws MaxException,
            XMLConfigException
    {
        Map docDescrMap = createDescriptors();

        DocumentDescriptor dd = (DocumentDescriptor) docDescrMap.get(name);
        if (roleCheck.isUserInSomeRole(dd.getExternalSystemRoles())) {
            document = performFiltering(dd, document, roleCheck);
        }
        else if (!(roleCheck.isUserInSomeRole(dd.getReadOnlyRoles()))
                && (!(roleCheck.isUserInSomeRole(dd.getReadWriteRoles())))) {

            throw new MaxException("Exception in the method applySecurityFilter");
        }

        return document;
    }

    /**
     *
     *
     *
     */
    public Check getCheckClass(String name) throws MaxException, XMLConfigException
    {

        CheckConstraints check = null;

        try {
            Node checkNode = XMLConfig.getNode(documentRepository, "//document[@name='" + name + "']/check");
            if (checkNode == null) {
                return null;
            }
            String checkClass = XMLConfig.get(checkNode, "@class");
            check = (CheckConstraints) Class.forName(checkClass).newInstance();
            check.init(checkNode);
        }
        catch (ClassNotFoundException exc) {
            throw new MaxException(exc);
        }
        catch (InstantiationException exc) {
            throw new MaxException(exc);
        }
        catch (IllegalAccessException exc) {
            throw new MaxException(exc);
        }

        return check;
    }

    /**
     * This methos applies the transformation of the Document if it must be
     * filtered.
     *
     * @return Document The Document filtrate.
     */
    private Document performFiltering(DocumentDescriptor dd, Document document, RoleCheck roleCheck)
            throws MaxException, XMLConfigException
    {

        try {
            InputStream filterStream = getFilterProvider().get("filters", dd.getName());
            Transformer transformer = null;
            if (filterStream == null) {
                transformer = TransformerFactory.newInstance().newTransformer();
            }
            else {
                transformer = (TransformerFactory.newInstance()).newTransformer(new StreamSource(filterStream));
            }
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            transformer.setParameter("roleCheck", roleCheck);

            DOMResult result = new DOMResult();
            DOMSource source = new DOMSource(document);
            transformer.transform(source, result);

            document = (Document) result.getNode();
        }
        catch (TransformerException exc) {
            throw new MaxException(exc);
        }

        return document;
    }

    /**
     * Setter method to insert a general filter foe a given document
     *
     * @param name
     *        The name of the category
     * @param filter
     *        The content of the filter(InputStream)
     */
    public synchronized void setSecurityFilter(String name, InputStream filter) throws MaxException, XMLConfigException
    {
        ContentProvider filters = getFilterProvider();
        if (filters.exists("filters", name)) {
            filters.update("filters", name, filter);
        }
        else {
            filters.insert("filters", name, filter);
        }
    }

    /**
     * @param name
     *        The name of the category
     */
    public synchronized void removeSecurityFilter(String name) throws MaxException, XMLConfigException
    {
        ContentProvider filters = getFilterProvider();
        if (filters.exists("filters", name)) {
            filters.remove("filters", name);
        }
    }

    /**
     * This method serves to cancel the nth category.
     *
     * @param name
     *        The name of the nth category;
     * @param version
     *        The version of the category;
     * @param notes
     *        The description of the version of category;
     * @param author
     *        The author of the operation.
     * @throws Exception 
     */
    public synchronized void rollback(String name, int version, String notes, String author) throws Exception
    {

    	VersionManager vm = VersionManager.instance();

    	DocumentProxy proxy = getDocumentProxy(name);
    	InputStream inputStream = vm.getDocument(name, version);
    	
    	String gvDir = PropertiesHandler.expand("${{gv.app.home}}", null);
    	
    	ZipInputStream zipFile = new ZipInputStream(inputStream);
        ZipEntry zipEntry = null;
        while((zipEntry=zipFile.getNextEntry())!=null) {
        	if (zipEntry.isDirectory()) {
        		String targetSubdirPathname = zipEntry.getName();
        		FileUtils.forceMkdir(new File(gvDir, targetSubdirPathname));
        	}
        	else {
        		OutputStream os = null;
        		FileUtils.forceMkdir(new File(gvDir, zipEntry.getName()).getParentFile());
        		os = new FileOutputStream(new File(gvDir, zipEntry.getName()));
        		IOUtils.copy(zipFile, os);
        		os.flush();
        		os.close();
        	}	
        }
        GVDeploy gvParser = new GVDeploy();
        ByteArrayInputStream in = gvParser.copyFileForBackupZip();
    	vm.newDocumentVersion(name, in, notes, author, new Date());
    	gvParser.deleteFileZip();
    }

    /**
     * This method serves to convert a InputStream in String.
     *
     * @param inputStream
     *        The InputStream to convert
     *
     * @return String Converted string.
     */
    private String convertToString(InputStream inputStream) throws MaxException
    {
        StringBuffer strb = new StringBuffer();

        try {
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);
            String str = null;
            while ((str = br.readLine()) != null) {
                strb.append(str).append("\n");
            }

        }
        catch (IOException ex) {
            throw new MaxException(ex);
        }
        return strb.toString();
    }

    /**
     * If necessary, this method reads the configuration file XML and it inserts
     * in the docDescrMap Map a Name-Value couples, where NAME is the name of
     * the document and VALUE is the DocumentDescriptor object (name, label,
     * descr, rwRoles, rrRoles, extRoles).
     */
    private synchronized Map createDescriptors() throws MaxException, XMLConfigException
    {

        // If the docDescrMap exists, does nothing.
        //
        if (docDescrMap != null) {
            return docDescrMap;
        }

        docDescrMap = Collections.synchronizedMap(new TreeMap());

        try {
            NodeList nList = XMLConfig.getNodeList(documentRepository, "//document");
            for (int i = 0; i < nList.getLength(); i++) {
                DocumentDescriptor docDescriptor = readDocumentDescriptor(nList.item(i));
                docDescrMap.put(docDescriptor.getName(), docDescriptor);
            }
        }
        catch (RuntimeException exc) {
            throw new MaxException(exc);
        }
        return docDescrMap;

    }

    /**
     * This method loads the object DocumentDescriptor reading and finding all
     * the data from the configuration file.
     *
     * @param Node
     *        The Node where find find the data.
     *
     * @return DocumentDescriptor The loaded object.
     */
    private DocumentDescriptor readDocumentDescriptor(Node node) throws XMLConfigException
    {
        String[] readWriteRoles = null;
        String[] readOnlyRoles = null;
        String[] externalSystemRoles = null;

        String attributeName = null;
        String attributeLabel = null;
        String nodeDescriptionValue = null;

        attributeName = XMLConfig.get(node, "@name");
        attributeLabel = XMLConfig.get(node, "@label");
        nodeDescriptionValue = XMLConfig.get(node, "description");

        NodeList nList = XMLConfig.getNodeList(node, "role");
        Vector roleRWVector = new Vector();
        Vector roleROVector = new Vector();
        Vector roleESVector = new Vector();
        for (int i = 0; i < nList.getLength(); i++) {
            String role = XMLConfig.get(nList.item(i), "@name");
            String access = XMLConfig.get(nList.item(i), "@access");

            if (access.equals("ex")) {
                roleESVector.add(role);
            }
            else if (access.equals("rw")) {
                roleRWVector.add(role);
            }
            else if (access.equals("ro")) {
                roleROVector.add(role);
            }
        }
        readWriteRoles = new String[roleRWVector.size()];
        roleRWVector.toArray(readWriteRoles);
        readOnlyRoles = new String[roleROVector.size()];
        roleROVector.toArray(readOnlyRoles);
        externalSystemRoles = new String[roleESVector.size()];
        roleESVector.toArray(externalSystemRoles);

        String group = XMLConfig.get(node, "ancestor::group/@name");

        return new DocumentDescriptor(attributeName, nodeDescriptionValue.trim(), group, attributeLabel,
                readWriteRoles, readOnlyRoles, externalSystemRoles);
    }

    /**
     *
     *
     *
     */

    /**
     * Creates a new Document.
     *
     * @return Document The created document.
     */
    private Document createDocument() throws MaxException
    {
        DocumentBuilder db = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            db = dbf.newDocumentBuilder();
        }
        catch (ParserConfigurationException exc) {
            throw new MaxException(exc);
        }
        return db.newDocument();
    }

    /**
     * Creates a new Document from a InputStream .
     *
     * @param Inputstream
     *        The InputStream from changing.
     *
     * @return Document The created document.
     * @throws XMLConfigException
     */
    private Document createDocument(InputStream inStream) throws MaxException, XMLConfigException
    {
        Document document = null;

        try {
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inStream));
            InputSource source = new InputSource(bufferReader);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setEntityResolver(new GVCatalogResolver());
            document = db.parse(source);
        }
        catch (ParserConfigurationException exc) {
            throw new MaxException(exc);
        }
        catch (SAXException exc) {
            throw new MaxException(exc);
        }
        catch (IOException exc) {
            throw new MaxException(exc);
        }
        return document;
    }

    /**
     * @throws XMLConfigException
     *
     *
     *
     */
    protected Element prepareDocumentItem(Document document, DocumentDescriptor dd, RoleCheck roleCheck)
            throws XMLConfigException
    {

        boolean rwFlag = roleCheck.isUserInSomeRole(dd.getReadWriteRoles());
        boolean roFlag = roleCheck.isUserInSomeRole(dd.getReadOnlyRoles());
        boolean exFlag = roleCheck.isUserInSomeRole(dd.getExternalSystemRoles());

        if (!(rwFlag || roFlag || exFlag)) {
            return null;
        }

        return prepareElement(document, dd, !rwFlag, exFlag && !roFlag && !rwFlag);
    }

    /**
     * @throws XMLConfigException
     *
     *
     *
     */
    protected Element prepareDocumentHistory(Document document, DocumentDescriptor dd, RoleCheck roleCheck)
            throws MaxException, XMLConfigException
    {

        boolean rwFlag = roleCheck.isUserInSomeRole(dd.getReadWriteRoles());
        boolean roFlag = roleCheck.isUserInSomeRole(dd.getReadOnlyRoles());

        if (!(rwFlag || roFlag)) {
            return null;
        }

        return prepareElementHistory(document, dd, roFlag && !rwFlag);
    }

    /**
     * Creates a new tag element without a value.
     *
     * @param Document
     *        The Document on which create the new tag;
     * @param Name
     *        The name to associate to the tag;
     *
     * @return Element A manipulator element
     */
    private Element createTag(Document dom, String name)
    {
        return dom.createElement(name);
    }

    /**
     * Creates a new tag element with a value.
     *
     * @param Document
     *        The Document on which create the new tag;
     * @param Name
     *        The name to associate to the tag;
     * @param Value
     *        The tag value.
     *
     * @return Element A manipulator element
     */
    private Element createTag(Document dom, String name, String value)
    {
        Element element = dom.createElement(name);
        Text text = dom.createTextNode(value);
        element.appendChild(text);
        return element;
    }

    /**
     * Creates a new tag element with a value and attributes.
     *
     * @param Document
     *        The Document on which create the new tag;
     * @param Name
     *        The name to associate to the tag;
     * @param Value
     *        The tag value.
     * @param info
     *        the lock info.
     *
     * @return Element A manipulator element
     */
    private Element createTag(Document dom, String name, String value, LockInfo info)
    {
        Element element = dom.createElement(name);
        element.setAttribute("user", info.getUser());
        element.setAttribute("host", info.getHostName());
        element.setAttribute("address", info.getIpAddress());

        Text text = dom.createTextNode(value);
        element.appendChild(text);
        return element;
    }

    /**
     * @throws XMLConfigException
     *
     *
     *
     */
    protected Element prepareElement(Document dom, DocumentDescriptor dd, boolean readOnly, boolean isExternalSystem)
            throws XMLConfigException
    {

        Element elementDoc = createTag(dom, "document");
        elementDoc.setAttribute("name", dd.getName());

        prepareElementInformation(dom, elementDoc, dd, readOnly, isExternalSystem);

        return elementDoc;
    }

    /**
     * @throws XMLConfigException
     *
     *
     *
     */
    protected Element prepareElementHistory(Document dom, DocumentDescriptor dd, boolean readOnly) throws MaxException,
            XMLConfigException
    {

        Element root = dom.getDocumentElement();

        Element nameElement = createTag(dom, "name", dd.getName());
        root.appendChild(nameElement);

        Element elementHisLabel = createTag(dom, "label", dd.getLabel());
        root.appendChild(elementHisLabel);

        Element elementHisDesc = createTag(dom, "description", dd.getDescription());
        root.appendChild(elementHisDesc);

        prepareVersionInformation(dom, root, dd, readOnly);

        return root;
    }

    /**
     * @throws XMLConfigException
     *
     *
     *
     */
    private void prepareElementInformation(Document dom, Element element, DocumentDescriptor dd, boolean readOnly,
            boolean isExternalSystem) throws XMLConfigException
    {

        Element elementLabel = createTag(dom, "label", dd.getLabel());
        element.appendChild(elementLabel);

        Element elementDescription = createTag(dom, "description", dd.getDescription());
        element.appendChild(elementDescription);

        if (readOnly) {
            Element elementOperationView = createTag(dom, "permission", "R");
            element.appendChild(elementOperationView);
        }
        else {
            Element elementOperationEdit = null;
            String name = dd.getName();
            if (LocksManager.isLocked(name)) {
                elementOperationEdit = createTag(dom, "permission", "LCK", LocksManager.getLockInfo(name));
            }
            else {
                elementOperationEdit = createTag(dom, "permission", "RW");
            }
            element.appendChild(elementOperationEdit);
        }

        if (isExternalSystem) {
            Element elementHistory = createTag(dom, "history", "no");
            element.appendChild(elementHistory);
        }
        else {
            Element elementHistory = createTag(dom, "history", "yes");
            element.appendChild(elementHistory);
        }
    }

    /**
     * @throws XMLConfigException
     *
     *
     *
     */
    private void prepareVersionInformation(Document dom, Element element, DocumentDescriptor dd, boolean readOnly)
            throws MaxException, XMLConfigException
    {

        VersionManager vm = VersionManager.instance();
        int olderVersion = vm.getOlderVersion(dd.getName());
        int lastVersion = vm.getLastVersion(dd.getName());
        for (int i = lastVersion; i >= olderVersion; i--) {
            if (vm.exists(dd.getName(), i)) {
                Element elementVersion = prepareVersionDetail(dom, dd, readOnly, i);
                element.appendChild(elementVersion);
            }
        }
    }

    /**
     * @throws XMLConfigException
     *
     *
     *
     */
    private Element prepareVersionDetail(Document dom, DocumentDescriptor dd, boolean readOnly, int version)
            throws MaxException, XMLConfigException
    {

        VersionManager vm = VersionManager.instance();

        Element elementVersion = createTag(dom, "version");
        elementVersion.setAttribute("id", "" + version);

        Element elementAuthor = createTag(dom, "author", vm.getAuthor(dd.getName(), version));
        elementVersion.appendChild(elementAuthor);

        Element elementNotes = createTag(dom, "notes", vm.getNotes(dd.getName(), version));
        elementVersion.appendChild(elementNotes);

        if(vm.getDate(dd.getName(), version)!=null){
        	Element elementDate = createTag(dom, "date", (vm.getDate(dd.getName(), version)).toString());
           elementVersion.appendChild(elementDate);
        }

        Element elementOperationView = createTag(dom, "permission", "R");
        elementVersion.appendChild(elementOperationView);

        if (!readOnly) {

            Element elementOperationRollback = createTag(dom, "permission", "RW");
            elementVersion.appendChild(elementOperationRollback);
        }
        return elementVersion;
    }

    private void checkWarnings(Element documentItem)
    {

        try {
            String name = documentItem.getAttribute("name");

            DocumentProxy proxy = getDocumentProxy(name);

            Document doc = proxy.load();

            XMLBuilder builder = new XMLBuilder(doc);
            MaxDocumentXPathFunction.setDocumentName(doc, name);

            Check check = getCheckClass(name);
            if (check != null) {
                builder.addCheck(check);
                builder.checkDocument(true);
            }
            if (builder.isDocumentInError(true)) {
                documentItem.setAttribute("isInError", "true");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
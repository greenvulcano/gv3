/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
package max.documents;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import max.core.ContentProvider;
import max.core.Contents;
import max.core.MaxException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class has been created to keep the historicization of a file. It will
 * serve for track all the brought modifications on the file in question through
 * an operation of versioning.
 *
 * WARNING - modified to use a fixed value for name parameter : GVConfig
 */
public class VersionManager
{

    // --------------------------------------------------------------------------
    // FIELDS
    // --------------------------------------------------------------------------

    public final static String    VM_NOTES           = "VM_NOTES";
    public final static String    VM_AUTHOR          = "VM_AUTHOR";
    public final static String    VM_DATE            = "VM_DATE";
    
    private static final String   FIXED_NAME         = "GVConfig";

    /**
     * Configuration file name used with <code>XMLConfig</code>.
     *
     * @see max.config.XMLConfig
     */
    public final static String    CONFIGURATION_FILE = "versionManager.xml";

    /**
     * The <code>VersionManager</code> uses a content provider to store its
     * data. This is the content provider name.
     */
    private String                providerName;

    /**
     * Unique instance of the <code>VersionManager</code>.
     */
    private static VersionManager _instance          = null;

    // --------------------------------------------------------------------------
    // CONSTRUCTION
    // --------------------------------------------------------------------------

    /**
     * Return the unique instance of the <code>VersionManager</code>.
     *
     * @throws XMLConfigException
     */
    public static synchronized VersionManager instance() throws MaxException, XMLConfigException
    {
        if (_instance == null) {
            _instance = new VersionManager();
        }

        return _instance;
    }

    /**
     * The private constructor avoid multiple copies of the VersionManager.
     *
     * @throws XMLConfigException
     */
    private VersionManager() throws MaxException, XMLConfigException
    {
        Document versionManagerDocument = XMLConfig.getDocument(CONFIGURATION_FILE,
                VersionManager.class.getClassLoader(), true, false);
        Node node = XMLConfig.getNode(versionManagerDocument, "/version-manager/content-provider");
        providerName = XMLConfig.get(node, "@name");
    }

    // --------------------------------------------------------------------------
    // INSERT
    // --------------------------------------------------------------------------

    /**
     * Insert a new version of a file.
     *
     * @param name
     *        The name of the file
     * @param document
     *        The contents of the file
     * @param notes
     *        notes
     * @param author
     *        The name of the author
     * @param date
     *        Change time
     *
     * @return The version number of the file.
     * @throws XMLConfigException
     */
    public synchronized int newDocumentVersion(String name, InputStream document, String notes, String author, Date date)
            throws MaxException, XMLConfigException
    {
        // TEMPORARY
        name = FIXED_NAME;
        
        ContentProvider provider = Contents.instance().getProvider(providerName);
        int newVersion = getLastVersion(name) + 1;

        String newVersionStr = "" + newVersion;

        provider.insert(name, newVersionStr, document);

        provider.setContentAttribute(name, newVersionStr, VM_NOTES, notes);
        provider.setContentAttribute(name, newVersionStr, VM_AUTHOR, author);
        provider.setContentAttribute(name, newVersionStr, VM_DATE, date);

        return newVersion;
    }

    // --------------------------------------------------------------------------
    // GETTERS
    // --------------------------------------------------------------------------

    /**
     * Getter method to find the notes associated to the given file and version.
     *
     * @param name
     *        The name of the file;
     * @param name
     *        The version of the file;
     * @return The notes associated to the given file and version.
     * @throws XMLConfigException
     */
    public synchronized String getNotes(String name, int version) throws MaxException, XMLConfigException
    {
     // TEMPORARY
        name = FIXED_NAME;
        
        String value = "" + version;
        ContentProvider provider = Contents.instance().getProvider(providerName);
        Map mp = provider.getContentAttributes(name, value);
        String note = (String) mp.get(VM_NOTES);
        return note;
    }

    /**
     * Getter method to find the author associated to the given file and
     * version.
     *
     * @param name
     *        The name of the file;
     * @param name
     *        The version of the file;
     * @return The name of the author associated to the given file and version.
     * @throws XMLConfigException
     */
    public synchronized String getAuthor(String name, int version) throws MaxException, XMLConfigException
    {
     // TEMPORARY
        name = FIXED_NAME;
        
        String value = "" + version;
        ContentProvider provider = Contents.instance().getProvider(providerName);
        Map mp = provider.getContentAttributes(name, value);
        String autore = (String) mp.get(VM_AUTHOR);
        return autore;
    }

    /**
     * Getter method to find the date associated to the given file and version.
     *
     * @param name
     *        The name of the file;
     * @param name
     *        The version of the file;
     * @return The date of the associated to the given file and version.
     * @throws XMLConfigException
     */
    public synchronized Date getDate(String name, int version) throws MaxException, XMLConfigException
    {
     // TEMPORARY
        name = FIXED_NAME;
        
        String value = "" + version;
        ContentProvider provider = Contents.instance().getProvider(providerName);
        Map mp = provider.getContentAttributes(name, value);
        Date data = (Date) mp.get(VM_DATE);
        return data;
    }

    /**
     * Getter method to find the contents of a specific version of the file.
     *
     * @param name
     *        The name of the file;
     * @param name
     *        The version of the file;
     * @return The file.
     * @throws XMLConfigException
     */
    public synchronized InputStream getDocument(String name, int version) throws MaxException, XMLConfigException
    {
     // TEMPORARY
        name = FIXED_NAME;
        
        ContentProvider provider = Contents.instance().getProvider(providerName);
        return provider.get(name, "" + version);
    }

    /**
     * Getter method to find all the file names.
     *
     * @return All file names.
     * @throws XMLConfigException
     */
    public synchronized String[] getDocumentNames() throws MaxException, XMLConfigException
    {
        ContentProvider provider = Contents.instance().getProvider(providerName);
        return provider.getCategories();
    }

    /**
     * Getter method to find the latest version of the file.
     *
     * @param name
     *        The name of the file
     * @return The file.
     * @throws XMLConfigException
     */
    public synchronized InputStream getLastVersionDocument(String name) throws MaxException, XMLConfigException
    {
     // TEMPORARY
        name = FIXED_NAME;
        
        return getDocument(name, getLastVersion(name));
    }

    /**
     * Getter method to find the last version number for a file.
     *
     * @param name
     *        The name of the file;
     * @return The last version number of the file. Return 0 if the document
     *         does not exists.
     * @throws XMLConfigException
     */
    public synchronized int getLastVersion(String name) throws MaxException, XMLConfigException
    {
     // TEMPORARY
        name = FIXED_NAME;
        
        ContentProvider provider = Contents.instance().getProvider(providerName);
        String names[] = provider.getContentNames(name);

        if (names == null) {
            return 0;
        }
        if (names.length == 0) {
            return 0;
        }

        int versioni[] = new int[names.length];
        for (int i = 0; i < versioni.length; ++i) {
            versioni[i] = Integer.parseInt(names[i]);
        }
        Arrays.sort(versioni);
        return versioni[versioni.length - 1];
    }

    /**
     * Getter method to find the older version number for a file.
     *
     * @param name
     *        The name of the file;
     * @return The older version number of the file. Return 0 if the document
     *         does not exists.
     * @throws XMLConfigException
     */
    public synchronized int getOlderVersion(String name) throws MaxException, XMLConfigException
    {
     // TEMPORARY
        name = FIXED_NAME;
        
        ContentProvider provider = Contents.instance().getProvider(providerName);
        String names[] = provider.getContentNames(name);

        if (names == null) {
            return 0;
        }
        if (names.length == 0) {
            return 0;
        }

        int versioni[] = new int[names.length];
        for (int i = 0; i < versioni.length; ++i) {
            versioni[i] = Integer.parseInt(names[i]);
        }
        Arrays.sort(versioni);
        return versioni[0];
    }

    // --------------------------------------------------------------------------
    // REMOVE
    // --------------------------------------------------------------------------

    /**
     * This method removes all versions greater than the given version.
     *
     * @param name
     *        The name of the file
     * @param name
     *        The version to rollback to. If &lt;= 0 than the file is completely
     *        removed from the VersionManager
     * @throws XMLConfigException
     */
    public synchronized void rollback(String name, int version) throws MaxException, XMLConfigException
    {
     // TEMPORARY
        name = FIXED_NAME;
        
        ContentProvider provider = Contents.instance().getProvider(providerName);

        int lastVersion = getLastVersion(name);
        removeRange(name, version + 1, lastVersion + 1);
    }

    /**
     * Removes older versions.
     *
     * @throws XMLConfigException
     */
    public synchronized void removeBeforeVersion(String name, int version) throws MaxException, XMLConfigException
    {
     // TEMPORARY
        name = FIXED_NAME;
        
        removeRange(name, getOlderVersion(name), version);
    }

    /**
     * Removes all versions of a file created before the given date.
     *
     * @throws XMLConfigException
     */
    public synchronized void removeBefore(String name, Date date) throws MaxException, XMLConfigException
    {
     // TEMPORARY
        name = FIXED_NAME;
        
        ContentProvider provider = Contents.instance().getProvider(providerName);
        String versions[] = provider.getContentNames(name);
        Map attributes[] = provider.getContentsAttributes(name, versions);

        for (int i = 0; i < attributes.length; ++i) {
            Date d = (Date) attributes[i].get(VM_DATE);
            if (d.before(date)) {
                String version = (String) attributes[i].get(ContentProvider.ATTR_NAME);
                provider.remove(name, version);
            }
        }
    }

    /**
     * Removes a range of versions (including the lower and excluding the
     * higher).
     *
     * @throws XMLConfigException
     */
    public synchronized void removeRange(String name, int lowVersion, int highVersion) throws MaxException,
            XMLConfigException
    {
     // TEMPORARY
        name = FIXED_NAME;
        
        int olderVersion = getOlderVersion(name);
        if (lowVersion < olderVersion) {
            lowVersion = olderVersion;
        }
        int lastVersion = getLastVersion(name);
        if (highVersion > lastVersion) {
            highVersion = lastVersion + 1;
        }

        ContentProvider provider = Contents.instance().getProvider(providerName);
        while (lowVersion < highVersion) {
            if (exists(name, lowVersion)) {
                provider.remove(name, "" + lowVersion);
            }
            ++lowVersion;
        }
    }

    // --------------------------------------------------------------------------
    // CHECKS
    // --------------------------------------------------------------------------

    /**
     * Check for existence of a file.
     *
     * @param name
     *        The file name
     * @return true if the file exists, false otherwise
     * @throws XMLConfigException
     */
    public synchronized boolean exists(String name) throws MaxException, XMLConfigException
    {
     // TEMPORARY
        name = FIXED_NAME;
        
        if (getLastVersion(name) == 0) {
            return false;
        }

        return true;
    }

    /**
     * Check for existence of a specific version of a file.
     *
     * @param name
     *        The file name
     * @return true if the file exists, false otherwise
     * @throws XMLConfigException
     */
    public synchronized boolean exists(String name, int version) throws MaxException, XMLConfigException
    {
     // TEMPORARY
        name = FIXED_NAME;
        
        ContentProvider provider = Contents.instance().getProvider(providerName);
        return provider.exists(name, "" + version);
    }
}
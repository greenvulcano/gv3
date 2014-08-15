/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
package max.def;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import max.config.Config;
import max.core.ContentProvider;
import max.core.Contents;
import max.core.MaxException;
import max.util.ObjectCache;
import max.util.StringUtil;

import org.w3c.dom.Node;

/**
 * @version 3.0.0 Apr 17, 2010
 * @author maxime
 *
 */
public class MaxASCIIContentProvider implements ContentProvider
{
    private static class ContFilenameFilter implements FilenameFilter
    {
        boolean dirs;

        public ContFilenameFilter(boolean dirs)
        {
            this.dirs = dirs;
        }

        public boolean accept(File dir, String name)
        {
            File f = new File(dir, name);
            if (dirs) {
                return f.isDirectory();
            }
            else {
                return !f.isDirectory();
            }
        }
    }

    private static final ContFilenameFilter listDirectories = new ContFilenameFilter(true);
    private static final ContFilenameFilter listFiles       = new ContFilenameFilter(false);

    private static class RegistryItem implements Serializable
    {
        /**
         *
         */
        private static final long      serialVersionUID = -2538940490848489379L;
        public String                  category;
        public String                  contentName;
        public String                  fileName;
        public Date                    lastUpdate;
        public HashMap<String, Object> attributes       = new HashMap<String, Object>();

        static String encodeForFileName(String s)
        {
            try {
                return URLEncoder.encode(s, "UTF-8");
            }
            catch (UnsupportedEncodingException exc) {
                return s;
            }
        }

        public static String decodeFileName(String s)
        {
            try {
                return URLDecoder.decode(s, "UTF-8");
            }
            catch (UnsupportedEncodingException exc) {
                return s;
            }
        }

        public RegistryItem()
        {
        }

        public RegistryItem(String category, String contentName)
        {
            this.category = category;
            this.contentName = contentName;

            fileName = encodeForFileName(category) + System.getProperty("file.separator")
                    + encodeForFileName(contentName);

            lastUpdate = new Date();
            attributes.put(ATTR_CATEGORY, category);
            attributes.put(ATTR_NAME, contentName);
            attributes.put(ATTR_LASTUPDATE, lastUpdate);
        }

        public String key()
        {
            return key(category, contentName);
        }

        public static String key(String cat, String cont)
        {
            return "max." + encodeForFileName(cat) + "." + encodeForFileName(cont) + ".registry";
        }

        public InputStream getInputStream(File workDir) throws MaxException
        {
            try {
                return new BufferedInputStream(new FileInputStream(new File(workDir, fileName)), 4096);
            }
            catch (IOException exc) {
                throw new MaxException(exc);
            }
        }
    }

    /**
     *
     */
    protected String      providerName;
    /**
     *
     */
    protected File        rootDir;
    /**
     *
     */
    protected File        workingDir;
    /**
     *
     */
    protected File        registryDir;
    /**
     *
     */
    protected ObjectCache registryCache;

    /**
     *
     */
    public MaxASCIIContentProvider()
    {
    }

    /**
     * @see max.core.ContentProvider#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws MaxException
    {
        String rootDirStr = null;
        try {
            providerName = XMLConfig.get(node, "../@name");
            rootDirStr = XMLConfig.get(node, "@directory");
        }
        catch (XMLConfigException exc) {
            throw new MaxException(exc);
        }
        if (rootDirStr == null) {
            throw new MaxException("No @directory attribute defined for provider '" + providerName + "'");
        }

        if (!PropertiesHandler.isExpanded(rootDirStr)) {
            try {
                rootDirStr = PropertiesHandler.expand(rootDirStr);
            }
            catch (PropertiesHandlerException exc) {
                exc.printStackTrace();
            }
        }

        try {
            rootDir = new File(rootDirStr);
            workingDir = new File(rootDir, "contents");
            workingDir.mkdirs();
            registryDir = new File(rootDir, "registry");
            registryDir.mkdirs();

            int regSize = XMLConfig.getInteger(node, "@cache-size", 128);
            registryCache = new ObjectCache(regSize, new File(Config.get("", "max.tmp.dir")), "MaxContents_registry_"
                    + providerName, ".tmp");
        }
        catch (Exception exc) {
            throw new MaxException(exc);
        }
    }

    private synchronized void setRegistry(RegistryItem item) throws MaxException
    {
        try {
            String key = item.key();
            registryCache.set(key, item);

            File fileReg = new File(registryDir, key);

            persistRegistryItem(fileReg, item);
        }
        catch (Exception exc) {
            throw new MaxException(exc);
        }
    }

    private synchronized RegistryItem getRegistry(String cat, String cont) throws MaxException
    {
        try {
            String key = RegistryItem.key(cat, cont);
            RegistryItem ret = (RegistryItem) registryCache.get(key);
            if (ret != null) {
                return ret;
            }

            File fileReg = new File(registryDir, key);
            if (!fileReg.exists()) {
                RegistryItem keyItem = new RegistryItem(cat, cont);
                File contFile = new File(workingDir, keyItem.fileName);
                if (contFile.exists() && !contFile.isDirectory()) {
                    setRegistry(keyItem);
                    return keyItem;
                }
                return null;
            }

            ret = readRegistryItem(fileReg);
            return ret;
        }
        catch (Exception exc) {
            throw new MaxException(exc);
        }
    }

    private synchronized void removeRegistry(String cat, String cont)
    {
        String key = RegistryItem.key(cat, cont);
        registryCache.remove(key);

        File fileReg = new File(registryDir, key);
        fileReg.delete();
    }

    private void persistRegistryItem(File file, RegistryItem item) throws IOException
    {
        FileOutputStream outStream = new FileOutputStream(file);
        OutputStreamWriter outWriter = new OutputStreamWriter(outStream, "UTF-8");
        PrintWriter writer = new PrintWriter(outWriter);

        writer.println("BEGIN");
        writer.println("category=" + StringUtil.escape(item.category));
        writer.println("contentName=" + StringUtil.escape(item.contentName));
        writer.println("fileName=" + StringUtil.escape(item.fileName));
        writer.println("lastUpdate=" + item.lastUpdate.getTime());
        writer.println("lastUpdate.str=" + item.lastUpdate);
        writer.println("ENTRIES");

        for (Entry<String, Object> entry : item.attributes.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key != null) {
                writer.println("K." + StringUtil.escape(key.toString()));
            }
            else {
                writer.println("k.");
            }
            if (value != null) {
                if (value instanceof String) {
                    writer.println("V.s." + StringUtil.escape(value.toString()));
                }
                else if (value instanceof Boolean) {
                    writer.println("V.b." + value);
                }
                else if (value instanceof Integer) {
                    writer.println("V.i." + value);
                }
                else if (value instanceof Float) {
                    writer.println("V.f." + value);
                }
                else if (value instanceof Long) {
                    writer.println("V.l." + value);
                }
                else if (value instanceof Date) {
                    writer.println("V.d." + ((Date) value).getTime());
                }
                else {
                    writer.println("V.o." + StringUtil.serialize((Serializable) value));
                }
            }
            else {
                writer.println("v.");
            }
        }
        writer.println("END");

        writer.flush();
        writer.close();
    }

    private RegistryItem readRegistryItem(File file) throws Exception
    {
        RegistryItem item = new RegistryItem();

        FileInputStream inStream = new FileInputStream(file);
        InputStreamReader inReader = new InputStreamReader(inStream, "UTF-8");
        BufferedReader reader = new BufferedReader(inReader);

        String line;
        String key = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("category=")) {
                item.category = StringUtil.unescape(line.substring(9));
            }
            else if (line.startsWith("contentName=")) {
                item.contentName = StringUtil.unescape(line.substring(12));
            }
            else if (line.startsWith("fileName=")) {
                item.fileName = StringUtil.unescape(line.substring(9));
            }
            else if (line.startsWith("lastUpdate=")) {
                item.lastUpdate = new Date(Long.parseLong(line.substring(11)));
            }
            else if (line.startsWith("K.")) {
                key = StringUtil.unescape(line.substring(2));
            }
            else if (line.startsWith("k.")) {
                key = null;
            }
            else if (line.startsWith("V.o.")) {
                Object value = StringUtil.deserialize(line.substring(4));
                item.attributes.put(key, value);
            }
            else if (line.startsWith("V.s.")) {
                Object value = StringUtil.unescape(line.substring(4));
                item.attributes.put(key, value);
            }
            else if (line.startsWith("V.b.")) {
                Object value = new Boolean(line.substring(4));
                item.attributes.put(key, value);
            }
            else if (line.startsWith("V.i.")) {
                Object value = new Integer(line.substring(4));
                item.attributes.put(key, value);
            }
            else if (line.startsWith("V.f.")) {
                Object value = new Float(line.substring(4));
                item.attributes.put(key, value);
            }
            else if (line.startsWith("V.l.")) {
                Object value = new Long(line.substring(4));
                item.attributes.put(key, value);
            }
            else if (line.startsWith("V.d.")) {
                Object value = new Date(Long.parseLong(line.substring(4)));
                item.attributes.put(key, value);
            }
            else if (line.startsWith("v.")) {
                item.attributes.put(key, null);
            }
        }
        reader.close();

        return item;
    }

    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////

    /**
     * Fornisce un contenuto indipendentemente dallo stato di abilitazione
     *
     * @return null se il contenuto non esiste.
     */
    public InputStream get(String category, String contentName) throws MaxException
    {
        RegistryItem item = getRegistry(category, contentName);
        if (item == null) {
            return null;
        }
        return item.getInputStream(workingDir);
    }

    /**
     * Restituisce tutte le categorie di contenuti.
     */
    public String[] getCategories() throws MaxException
    {
        return workingDir.list(listDirectories);
    }

    /**
     * Resituisce tutti i nomi dei contenuti nella categoria data,
     * indipendentemente dallo stato di abilitazione.
     */
    public String[] getContentNames(String category) throws MaxException
    {
        File catDir = new File(workingDir, RegistryItem.encodeForFileName(category));
        String ret[] = catDir.list(listFiles);
        if (ret == null) {
            return new String[0];
        }
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = RegistryItem.decodeFileName(ret[i]);
        }
        return ret;
    }

    /**
     * Restituisce true se il contenuto esiste, indipendentemente dallo stato di
     * abilitazione.
     */
    public boolean exists(String category, String contentName) throws MaxException
    {
        RegistryItem item = getRegistry(category, contentName);
        if (item == null) {
            return false;
        }
        return true;
    }

    private void put(String category, String contentName, InputStream content) throws MaxException
    {
        try {
            File categoryDir = new File(workingDir, RegistryItem.encodeForFileName(category));
            categoryDir.mkdirs();

            RegistryItem item = getRegistry(category, contentName);
            if (item == null) {
                item = new RegistryItem(category, contentName);
            }

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(
                    new File(workingDir, item.fileName)), 4096);
            byte buf[] = new byte[2048];
            int l;
            while ((l = content.read(buf)) != -1) {
                out.write(buf, 0, l);
            }
            out.flush();
            out.close();
            content.close();

            item.lastUpdate = new Date();
            setRegistry(item);
        }
        catch (MaxException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new MaxException(exc);
        }
    }

    /**
     * I nuovi contenuti non sono abilitati, il periodo non � definito
     * (disponibili in ogni momento) e disponibili solo ad utenti autenticati.
     * <p>
     *
     * @see #update(java.lang.String, java.lang.String, java.io.InputStream)
     *
     * @exception MaxException
     *            se il contenuto � gia esistente
     */
    public void insert(String category, String contentName, InputStream content) throws MaxException
    {
        if (exists(category, contentName)) {
            throw new MaxException("Content '" + contentName + "' in category '" + category + "' already exists");
        }
        put(category, contentName, content);
    }

    /**
     * Gli aggiornamenti non modificano lo stato di abilitazione, il periodo ed
     * i grant richiesti.
     * <p>
     *
     * @see #insert(java.lang.String, java.lang.String, java.io.InputStream)
     *
     * @exception MaxException
     *            se il contenuto non esiste
     */
    public void update(String category, String contentName, InputStream content) throws MaxException
    {
        if (!exists(category, contentName)) {
            throw new MaxException("Content '" + contentName + "' in category '" + category + "' does not exists");
        }
        put(category, contentName, content);
    }

    /**
     * Rimuove un contenuto.
     */
    public void remove(String category, String contentName) throws MaxException
    {
        RegistryItem item = getRegistry(category, contentName);
        if (item == null) {
            return;
        }
        removeRegistry(category, contentName);
        File content = new File(workingDir, item.fileName);
        content.delete();
    }

    public Map[] getContentsAttributes(String category, String[] contentNames) throws MaxException
    {
        Map ret[] = new Map[contentNames.length];
        for (int i = 0; i < contentNames.length; ++i) {
            ret[i] = getContentAttributes(category, contentNames[i]);
        }
        return ret;
    }

    public Map getContentAttributes(String category, String contentName) throws MaxException
    {
        RegistryItem item = getRegistry(category, contentName);
        if (item == null) {
            throw new MaxException("Content '" + contentName + "' in category '" + category + "' does not exists");
        }
        item.attributes.put(ATTR_PROVIDER, providerName);
        return item.attributes;
    }

    public void setContentAttribute(String category, String contentName, String attribute, Serializable value)
            throws MaxException
    {
        RegistryItem item = getRegistry(category, contentName);
        if (item == null) {
            return;
        }
        item.attributes.put(attribute, value);
        setRegistry(item);
    }

    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////

    public static void main(String args[]) throws Exception
    {
        String cmd = args[0];
        String prov = args[1];

        ContentProvider provider = Contents.instance().getProvider(prov);

        if (cmd.equals("get")) {
            String cat = args[2];
            String cont = args[3];
            InputStream is = provider.get(cat, cont);
            if (is == null) {
                System.out.println("not found");
                return;
            }
            byte buf[] = new byte[2048];
            int l;
            while ((l = is.read(buf)) != -1) {
                System.out.write(buf, 0, l);
            }
            is.close();
        }
        else if (cmd.equals("insert")) {
            String cat = args[2];
            String cont = args[3];
            String fileName = args[4];
            FileInputStream in = new FileInputStream(fileName);
            provider.insert(cat, cont, in);
        }
        else if (cmd.equals("update")) {
            String cat = args[2];
            String cont = args[3];
            String fileName = args[4];
            FileInputStream in = new FileInputStream(fileName);
            provider.update(cat, cont, in);
        }
        else if (cmd.equals("remove")) {
            String cat = args[2];
            String cont = args[3];
            provider.remove(cat, cont);
        }
        else if (cmd.equals("cats")) {
            String cats[] = provider.getCategories();
            for (int i = 0; i < cats.length; ++i) {
                System.out.println(cats[i]);
            }
        }
        else if (cmd.equals("cont")) {
            String cat = args[2];
            String conts[] = provider.getContentNames(cat);
            for (int i = 0; i < conts.length; ++i) {
                System.out.println(conts[i]);
            }
        }
        else if (cmd.equals("attr")) {
            String cat = args[2];
            String conts[] = provider.getContentNames(cat);
            Map attrs[] = provider.getContentsAttributes(cat, conts);

            for (int i = 0; i < attrs.length; ++i) {
                System.out.println("" + i + ": ----------------------------------------");
                Set keys = attrs[i].keySet();
                Object k[] = keys.toArray();
                for (int j = 0; j < k.length; ++j) {
                    Object o = attrs[i].get(k[j]);
                    System.out.print("" + k[j] + "=" + o + "\t");
                }
                System.out.println();
            }
        }
    }
}

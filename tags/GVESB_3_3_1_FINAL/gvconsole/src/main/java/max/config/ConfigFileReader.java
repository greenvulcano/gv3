/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
package max.config;

import it.greenvulcano.log.GVLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * Legge un files di properties.
 * <p>
 * Le definizioni delle properties devono avere il seguente formato:
 *
 * <pre>
 *     chiave1 = valore1
 *     ...
 *     chiave_I = valore_I
 *     ...
 *     chiave_N = valore_N
 * </pre>
 *
 * E' possibile specificare valori multi-riga:
 *
 * <pre>
 *     chiave = {{
 *         linea1
 *         ...
 *         lineaN
 *     }}
 * </pre>
 *
 * Il valore di chiave � ottenuto dalla concatenazione di tutte le linee
 * <code>linea1</code>...<code>lineaN</code>.<br>
 * Se le linee iniziano con il carattere # sono ignorate.<br>
 * Per inserire il carattere # ad inizio di una linea utilizzare il doppio apice
 * (<code>"#...</code>) per evitare che # sia interpretato come commento.
 * <p>
 *
 * Le properties sono raggruppate in sezioni. Una sezione inizia da una riga con
 * il seguente formato:
 *
 * <pre>
 *     [NOME_SEZIONE]
 * </pre>
 *
 * Se le chiavi si ripetono, l'ultimo valore letto sovrascrive il valore
 * precedente.
 *
 * Alle chiavi ed ai valori � applicato un trimming. <br>
 * Se si ha necessit� di includere degli spazi nei <u>valori</u>, � possibile
 * utilizzare i doppi apici. <br>
 * Ad esempio in
 *
 * <pre>
 * key = &quot;valore1 &quot;
 * </pre>
 *
 * Il valore di <code>key</code> � '<b><code>valore1 </code></b>' (con uno
 * spazio e senza apicetti). <br>
 * Gli apici iniziali e finali sono eliminati.
 * <p>
 *
 * Sono gestiti i caratteri escape (<code>\n</code>, <code>\t</code> ecc.) sia
 * nelle chiavi sia nei valori.
 * <p>
 *
 * Non � significativo l'ordine di specifica delle properties. <br>
 */
public class ConfigFileReader
{
    private static final Logger logger      = GVLogger.getLogger(ConfigFileReader.class);

    private ConfigStore         properties  = new ConfigStore();

    private static final String SECTION_KEY = "$max$section$";
    private static final int    SINGLE_LINE = 0;
    private static final int    MULTI_LINE  = 1;

    private String              sect;
    private String              key;
    private String              value;

    private int                 mode;

    private URL                 currentUrl;
    private URL                 lastUrl;

    /**
     * Costruisce un lettore per i files di properties.<br>
     */
    public ConfigFileReader()
    {
    }

    public ConfigStore getProperties()
    {
        return properties;
    }

    /**
     * Legge il file specificato.
     *
     * @param fileName
     *        file da leggere
     *
     * @exception FileNotFoundException
     *            se <code>file</code> non esiste.
     */
    public void read(String fileName) throws FileNotFoundException
    {
        String ext = "";
        int idx = fileName.lastIndexOf('.');
        if (idx != -1) {
            ext = fileName.substring(idx);
            fileName = fileName.substring(0, idx);
        }

        Locale lc = Locale.getDefault();
        String language = lc.getLanguage();
        String country = lc.getCountry();
        String variant = lc.getVariant();

        String fileNames[] = {fileName + ext, fileName + "_" + language + ext,
                fileName + "_" + language + "_" + country + ext,
                fileName + "_" + language + "_" + country + "_" + variant + ext};

        int r = 0;
        for (int i = 0; i < fileNames.length; ++i) {
            try {
                readFile(fileNames[i]);
                ++r;
            }
            catch (IOException exc) {
            }
        }

        if (r == 0) {
            throw new FileNotFoundException(fileName + ext + " or localized version (" + Locale.getDefault()
                    + ") not found.");
        }
    }

    /**
     * Legge il file specificato. <br>
     *
     * @param file
     *        file da leggere
     *
     * @exception FileNotFoundException
     *            se <code>file</code> non esiste.
     */
    protected void readFile(String file) throws IOException
    {
        if (file == null) {
            return;
        }
        logger.debug("Reading File: " + file);
        currentUrl = ConfigFileReader.class.getClassLoader().getResource(file);

        if (currentUrl == null) {
            logger.debug("File Not Found: " + file);
            return;
        }
        InputStream is = currentUrl.openStream();

        lastUrl = currentUrl;

        logger.debug("Reading: " + currentUrl);
        BufferedReader in = new BufferedReader(new InputStreamReader(is), 4096);

        try {
            sect = "";
            memSection(sect);
            mode = SINGLE_LINE;
            String line = null;
            while ((line = in.readLine()) != null) {
                switch (mode) {
                    case SINGLE_LINE :
                        processLine(line);
                        break;
                    case MULTI_LINE :
                        processMultiLine(line);
                        break;
                    default :
                        System.err.println("ConfigFileReader.readFile: INTERNAL ERROR/01");
                        break;
                }
            }
        }
        catch (IOException e) {
        }

        try {
            in.close();
        }
        catch (IOException e) {
        }
    }

    /**
     * Interpreta una singola linea del file di properties. <br>
     * La linea deve essere del formato
     *
     * <pre>
     * key = value
     * </pre>
     *
     * e non deve iniziare per '<b><code>#</code></b>'. <br>
     * Tutte le linee che non hanno questo formato sono scartate.
     *
     * @param line
     *        linea letta dal file di properties.
     */
    protected void processLine(String line)
    {
        line = line.trim();
        if (line.startsWith("#")) {
            return;
        }

        if (line.startsWith("[") && line.endsWith("]")) {
            sect = line.substring(1, line.length() - 1);
            sect = unquote(sect.trim());
            sect = unescape(sect);
            memSection(sect);
            return;
        }

        int idx = line.indexOf('=');
        if (idx == -1) {
            return;
        }

        key = unquote(line.substring(0, idx).trim());
        value = unquote(line.substring(idx + 1).trim());
        key = unescape(key);
        value = unescape(value);

        if (value.equals("{{")) {
            mode = MULTI_LINE;
            value = "";
        }
        else {
            set(sect, key, value);
        }
    }

    protected void processMultiLine(String line)
    {
        line = line.trim();
        if (line.startsWith("#")) {
            return;
        }

        String val = unquote(line);
        val = unescape(val);

        if (val.equals("}}")) {
            mode = SINGLE_LINE;
            set(sect, key, value);
        }
        else {
            value += val + "\n";
        }
    }

    /**
     * Utilizzata per la gestione delle sequenze di escape.
     */
    protected static final char quote = '\001';

    /**
     * Gestisce i caratteri escape delle stringhe: sostituisce le sequenze di
     * escape con i caratteri corrispondenti.
     */
    protected String unescape(String str)
    {
        str = "" + quote + str + quote;
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(str));
        tokenizer.resetSyntax();
        tokenizer.quoteChar(quote);
        try {
            tokenizer.nextToken();
        }
        catch (IOException e) {
        }
        return tokenizer.sval;
    }

    protected String unquote(String str)
    {
        if (str.startsWith("\"")) {
            str = str.substring(1);
        }
        if (str.endsWith("\"")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    protected void set(String sect, String key, String value)
    {
        properties.set(sect, key, value);
        Hashtable ht = (Hashtable) references.get(sect);
        if (ht == null) {
            ht = new Hashtable();
            references.put(sect, ht);
        }
        ht.put(key, currentUrl);
    }

    protected void memSection(String sect)
    {
        Hashtable ht = (Hashtable) references.get(sect);
        if (ht == null) {
            ht = new Hashtable();
            references.put(sect, ht);
        }
        ht.put(SECTION_KEY, currentUrl);
    }

    // /////////////////////////////////////////////////////////////////////
    // WRITE
    // /////////////////////////////////////////////////////////////////////

    private Hashtable references = new Hashtable();

    /**
     * @param value
     *        se null la property � cancellata
     */
    public void write(String section, String property, String value) throws ProtocolException, IOException
    {
        synchronized (ConfigFileReader.class) {
            Hashtable ht = (Hashtable) references.get(section);
            if (ht == null) {
                ht = new Hashtable();
                references.put(section, ht);
                ht.put(SECTION_KEY, lastUrl);
            }
            URL url = (URL) ht.get(property);
            if (url == null) {
                url = (URL) ht.get(SECTION_KEY);
            }

            write(url, section, property, value);
        }
    }

    private void write(URL url, String section, String property, String value) throws ProtocolException, IOException
    {
        currentUrl = url;

        String protocol = url.getProtocol();
        if (!protocol.equals("file")) {
            throw new ProtocolException("Invalid protocol: '" + protocol + "'. Only 'file' is implemented");
        }

        File fileIn = new File(url.getFile());
        InputStream is = url.openStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(is), 4096);

        File fileOut = new File("ConfigFileReader.temp");
        PrintWriter out = new PrintWriter(new FileWriter(fileOut));

        File fileBak = new File(fileIn.getAbsolutePath() + ".bak");
        fileBak.delete();

        sect = "";
        key = null;
        String chunk = null;
        boolean found = false;
        while ((chunk = nextChunk(in)) != null) {
            if (property.equals(key) && section.equals(sect)) {
                if (value != null) {
                    writeProperty(out, property, value);
                }
                found = true;
            }
            else {
                out.print(chunk);
            }
            key = null;
        }

        if (!found) {
            if (!section.equals(sect)) {
                out.println("");
                out.println("# Inserted at " + (new Date()));
                out.println("#");
                out.println("[" + section + "]");
            }
            writeProperty(out, property, value);
        }

        out.flush();
        out.close();
        in.close();

        fileIn.renameTo(fileBak);
        fileOut.renameTo(fileIn);
        fileBak.delete();
    }

    protected String nextChunk(BufferedReader in) throws IOException
    {
        String line = null;
        while ((line = in.readLine()) != null) {
            boolean multilinea = processLineWriting(line);
            if (!multilinea) {
                return line + "\n";
            }

            StringBuffer buf = new StringBuffer();
            buf.append(line).append("\n");
            while ((line = in.readLine()) != null) {
                buf.append(line).append("\n");
                line = line.trim();
                line = unquote(line);
                line = unescape(line);

                if (line.equals("}}")) {
                    return buf.toString();
                }
            }
            return buf.toString();
        }
        return line;
    }

    protected boolean processLineWriting(String ln)
    {
        String line = ln.trim();
        if (line.startsWith("#")) {
            return false;
        }

        if (line.startsWith("[") && line.endsWith("]")) {
            sect = line.substring(1, line.length() - 1);
            sect = unquote(sect.trim());
            sect = unescape(sect);
            return false;
        }

        int idx = line.indexOf('=');
        if (idx == -1) {
            return false;
        }

        key = unquote(line.substring(0, idx).trim());
        value = unquote(line.substring(idx + 1).trim());
        key = unescape(key);
        value = unescape(value);

        return value.equals("{{");
    }

    private void writeProperty(PrintWriter out, String property, String value) throws IOException
    {
        writeString(out, property);
        out.print(" = ");
        int idx = value.indexOf('\n');
        if (idx == -1) {
            writeString(out, value);
            out.println("");
        }
        else {
            out.println("{{");
            StringTokenizer st = new StringTokenizer(value, "\n", false);
            while (st.hasMoreTokens()) {
                String tk = st.nextToken();
                out.print("\t");
                writeString(out, tk);
                out.println("");
            }
            out.println("}}");
        }
    }

    private void writeString(PrintWriter out, String str) throws IOException
    {
        if (str.startsWith(" ") || str.startsWith("\"")) {
            out.print("\"");
        }
        out.print(str);
        if (str.endsWith(" ") || str.endsWith("\"")) {
            out.print("\"");
        }
    }

    // /////////////////////////////////////////////////////////////////////

    public static void main(String args[]) throws Exception
    {
        String conn = Config.get(args[0], args[1]);
        System.out.println("-----> " + conn);

        if (args.length >= 3) {
            String val = args[2];
            for (int i = 3; i < args.length; ++i) {
                val += "\n" + args[i];
            }
            Config.write(args[0], args[1], val);
        }
        else {
            Config.remove(args[0], args[1]);
        }

        conn = Config.get(args[0], args[1]);
        System.out.println("-----> " + conn);
    }
}

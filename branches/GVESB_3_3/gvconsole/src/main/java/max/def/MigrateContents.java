/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.def;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import max.core.ContentProvider;
import max.core.Contents;

/**
 * Questa classe migra tutto il contenuto di un content provider in un altro.
 *
 * @author Sergio
 *
 */
public class MigrateContents {
    // -----------------------------------------------------------------------------------------
    // FIELDS
    // -----------------------------------------------------------------------------------------

    private long        counter;
    private String      sourceName;
    private String      destinationName;
    private PrintWriter out;
    private boolean     outputHtml;

    // -----------------------------------------------------------------------------------------
    // CONSTRUCTOR
    // -----------------------------------------------------------------------------------------

    public MigrateContents(PrintStream out) {
        sourceName = "SRC";
        destinationName = "DEST";
        this.out = new PrintWriter(out);
    }

    public MigrateContents(PrintWriter out) {
        sourceName = "SRC";
        destinationName = "DEST";
        this.out = out;
    }

    public MigrateContents(String source, String destination) {
        sourceName = source;
        destinationName = destination;
        out = new PrintWriter(System.out);
    }

    public MigrateContents(String source, String destination, PrintWriter out) {
        sourceName = source;
        destinationName = destination;
        this.out = out;
    }

    // -----------------------------------------------------------------------------------------
    // METHODS
    // -----------------------------------------------------------------------------------------

    /**
     * @return Returns the destination.
     */
    public String getDestination() {
        return destinationName;
    }

    /**
     * @return Returns the source.
     */
    public String getSource() {
        return sourceName;
    }

    /**
     * @return Returns the outputHtml.
     */
    public boolean isOutputHtml() {
        return outputHtml;
    }

    /**
     * @param destination
     *        The destination to set.
     */
    public void setDestination(String destination) {
        destinationName = destination;
    }

    /**
     * @param source
     *        The source to set.
     */
    public void setSource(String source) {
        sourceName = source;
    }

    /**
     * @param outputHtml
     *        The outputHtml to set.
     */
    public void setOutputHtml(boolean outputHtml) {
        this.outputHtml = outputHtml;
    }

    public void migrate() {
        long startTime = System.currentTimeMillis();
        counter = 0;

        try {
            migrateContents();
        }
        catch (Exception exc) {
            out.println();
            line();
            out.println();
            out.println(encode("" + exc));
            out.println();
            exc.printStackTrace(out);
            line();
            out.println();
        }

        long endTime = System.currentTimeMillis();

        line();
        out.println("Migrated " + counter + " contents in " + (endTime - startTime) + " ms");
        line();
    }

    // -----------------------------------------------------------------------------------------
    // PRIVATE METHODS
    // -----------------------------------------------------------------------------------------

    private String encode(String str) {
        if (!outputHtml) {
            return str;
        }
        StringTokenizer tkzr = new StringTokenizer(str, "<>\"", true);
        StringBuffer ret = new StringBuffer();
        while (tkzr.hasMoreTokens()) {
            String tk = tkzr.nextToken();
            if (tk.equals("<")) {
                ret.append("&lt;");
            }
            else if (tk.equals(">")) {
                ret.append("&gt;");
            }
            else if (tk.equals("\"")) {
                ret.append("&quot;");
            }
            else {
                ret.append(tk);
            }
        }
        return ret.toString();
    }

    private void help() {
        line();
        out.println("Usage: " + MigrateContents.class.getName() + " ORIGIN DESTINATION");
        line();
    }

    private void line() {
        out.println("--------------------------------------------------------------------");
    }

    private void migrateContents() throws Exception {
        line();
        out.println("Migrating from " + encode(sourceName) + " to " + encode(destinationName));
        line();

        ContentProvider origin = Contents.instance().getProvider(sourceName);
        ContentProvider destination = Contents.instance().getProvider(destinationName);

        String[] categories = origin.getCategories();
        for (int i = 0; i < categories.length; ++i) {
            migrateCategory(categories[i], origin, destination);
        }
    }

    private void migrateCategory(String category, ContentProvider origin, ContentProvider destination) throws Exception {
        String[] contentNames = origin.getContentNames(category);
        if (contentNames != null) {
            for (int i = 0; i < contentNames.length; ++i) {
                migrateContent(category, contentNames[i], origin, destination);
            }
        }
    }

    private void migrateContent(String category, String contentName, ContentProvider origin, ContentProvider destination)
            throws Exception {
        try {
            ++counter;
            out.print("" + counter + ":\tmigrating " + encode(category) + "::" + encode(contentName) + "... ");

            InputStream content = origin.get(category, contentName);
            if (destination.exists(category, contentName)) {
                destination.update(category, contentName, content);
            }
            else {
                destination.insert(category, contentName, content);
            }

            Map attributes = origin.getContentAttributes(category, contentName);
            if (attributes != null) {
                Iterator it = attributes.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    String attribute = (String) entry.getKey();
                    Serializable value = (Serializable) entry.getValue();
                    destination.setContentAttribute(category, contentName, attribute, value);
                }
            }

            out.print("done.");
        }
        finally {
            out.println("");
        }
    }

    // -----------------------------------------------------------------------------------------
    // COMMAND LINE
    // -----------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        MigrateContents migrateContents = new MigrateContents(System.out);

        if (args.length < 2) {
            migrateContents.help();
            return;
        }

        migrateContents.setSource(args[0]);
        migrateContents.setDestination(args[1]);

        migrateContents.migrate();
    }
}

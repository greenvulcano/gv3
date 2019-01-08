/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:54 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/util/MultipartFormDataParser.java,v 1.1 2010-04-03 15:28:54 nlariviera Exp $
 * $Id: MultipartFormDataParser.java,v 1.1 2010-04-03 15:28:54 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;


/**
 * Classe di utility per leggere i dati inviati dal client con tramite un form
 * HTML con <code>method="POST"</code> ed <code>enctype="multipart/form-data"</code>.<p>
 * Un form cos� configurato pu� avere degli input con <code>type="file"</code>.<p>
 *
 * Ad esempio la seguente form esegue il post verso <code>/some/url</code>
 * di un file e di un testo:
 * <pre>
 *   &lt;form action="/some/url" <b>method="post"</b> <b>enctype="multipart/form-data"</b>&gt;
 *       &lt;input <b>type="file"</b> name="photo"&gt;&lt;br&gt;
 *       &lt;input type="text" name="who"&gt;&lt;br&gt;
 *       &lt;input type="submit" value=" Submit "&gt;
 *   &lt;/form&gt;
 * </pre>
 * <p>
 * Per ricevere i dati � necessario instanziare un oggetto <code>MultipartFormDataParser</code>,
 * passandogli un oggetto <code>ServletRequest</code>,
 * ed utilizzarlo per accedere ai dati come nel codice seguente:
 * <pre>
 *   ...
 *   MultipartFormDataParser mp = new MultipartFormDataParser(request);
 *   ...
 *   InputStream is = mp.getInputStream("photo");
 *   String text = mp.getString("who");
 *   ...
 * </pre>
 * <p>
 * Il client pu� inviare pi� occorrenze per lo stesso dato.<br>
 * <code>MultipartFormDataParser</code> deposita i dati inviati dal client in files
 * locali al server.<br>
 * Fornisce diversi metodi per accedere ai dati inviati dal client.<p>
 *
 * E' opportuno eliminare i files temporanei con il metodo <code>close()</code>
 * quando non sono pi� necessari.<br>
 * I files potranno essere eliminati dal metodo <code>finalize()</code>
 * invocato dal garbage collector se l'oggetto non � pi� referenziato.<br>
 * In ogni caso i files saranno eliminati all'uscita della JVM.
 */
public class MultipartFormDataParser
{
    private ServletInputStream in;
    private String boundary;
    private String endBoundary;
    private boolean endData;

    /**
     * Per ogni nome memorizza un Vector di File contenenti gli oggetti in upload.
     */
    private Hashtable bodies = new Hashtable();
    private File currentFile;


    /**
     * Legge lo stream di input della richiesta e deposita in file temporanei locali
     * al server i dati inviati dal client.<p>
     *
     * @param request richiesta effettuata dal client.
     * @exception IOException se non riesce a depositare i dati sui files temporanei.
     * @exception MessagingException se i dati in input non sono in un formato mime
     *      valido.
     */
    public MultipartFormDataParser(ServletRequest request) throws IOException, MessagingException
    {
        in = request.getInputStream();
        endData = false;

        boundary = readString().trim();
        endBoundary = boundary + "--";

        while(!endData) {
            MimeBodyPart bp = readBodyPart();
            put(getName(bp), currentFile);
        }

        in = null;
        boundary = null;
        endBoundary = null;
        currentFile = null;
    }


    /**
     * Verifica che il client abbia inviato un parametro con il nome dato.
     *
     * @param name nome del parametro.
     * @return <code>true</code> se il client ha inviato un parametro con il
     *       nome dato.
     */
    public boolean exists(String name)
    {
        Object o = bodies.get(name);
        return o != null;
    }


    /**
     * Restituisce il numero di occorrenze di un parametro.
     *
     * @param name nome del parametro.
     * @return numero di occorrenze del parametro <code>name</code>.
     */
    public int getCount(String name)
    {
        Vector v = (Vector)bodies.get(name);
        if(v == null) return 0;
        return v.size();
    }


    /**
     * Restituisce i nomi di tutti i parametri.
     *
     * @return nomi dei parametri.
     */
    public String[] getParameterNames()
    {
        int n = bodies.size();
        String ret[] = new String[n];
        int i = 0;
        for(Enumeration e = bodies.keys(); e.hasMoreElements(); ++i) {
            ret[i] = e.nextElement().toString();
        }
        return ret;
    }


    /**
     * Restituisce l'oggetto <code>MimeBodyPart</code> che descrive il parametro.
     *
     * @param name nome del parametro.
     * @param occ numero di occorrenza del parametro.
     * @return oggetto <code>MimeBodyPart</code> che descrive il parametro.<br>
     *      Restituisce <code>null</code> se il parametro non esiste o l'occorrenza
     *      non esiste.
     * @exception FileNotFoundException se non riesce a trovare il file temporaneo.
     * @exception MessagingException se il file temporaneo � corrotto e non contiene
     *      dati in un formato mime valido.
     */
    public MimeBodyPart getBodyPart(String name, int occ) throws FileNotFoundException, MessagingException
    {
        Vector v = (Vector)bodies.get(name);
        if(v == null) return null;
        if(v.size() <= occ) return null;
        File f = (File)v.elementAt(occ);
        FileInputStream in = new FileInputStream(f);
        return new MimeBodyPart(in);
    }


    /**
     * Restituisce l'oggetto <code>MimeBodyPart</code> che descrive la prima
     * occorrenza del parametro.
     *
     * @param name nome del parametro.
     * @return oggetto <code>MimeBodyPart</code> che descrive la prima
     *      occorrenza del parametro.<br>
     *      Restituisce <code>null</code> se il parametro non esiste.
     * @exception FileNotFoundException se non riesce a trovare il file temporaneo.
     * @exception MessagingException se il file temporaneo � corrotto e non contiene
     *      dati in un formato mime valido.
     */
    public MimeBodyPart getBodyPart(String name) throws FileNotFoundException, MessagingException
    {
        return getBodyPart(name, 0);
    }


    /**
     * Restituisce gli oggetti <code>MimeBodyPart</code> che descrivono tutte
     * le occorrenze per il parametro dato.
     *
     * @param name nome del parametro.
     * @return oggetti <code>MimeBodyPart</code> che descrivono le occorrenze del parametro.
     * @exception FileNotFoundException se non riesce a trovare il file temporaneo.
     * @exception MessagingException se il file temporaneo � corrotto e non contiene
     *      dati in un formato mime valido.
     */
    public MimeBodyPart[] getBodyParts(String name) throws FileNotFoundException, MessagingException
    {
        MimeBodyPart ret[] = new MimeBodyPart[getCount(name)];
        for(int i = 0; i < ret.length; ++i) {
            ret[i] = getBodyPart(name, i);
        }
        return ret;
    }


    /**
     * Restituisce il nome del file locale al client per il parametro.
     *
     * @param name nome del parametro.
     * @param occ numero di occorrenza del parametro.
     * @return nome del file locale al client per il parametro.<br>
     *      Restituisce <code>null</code> se il parametro non esiste o l'occorrenza
     *      non esiste o il parametro non � di tipo <code>type="file"</code>.
     * @exception FileNotFoundException se non riesce a trovare il file temporaneo.
     * @exception MessagingException se il file temporaneo � corrotto e non contiene
     *      dati in un formato mime valido.
     */
    public String getFileName(String name, int occ) throws MessagingException, FileNotFoundException
    {
        MimeBodyPart bp = getBodyPart(name, occ);
        if(bp == null) return null;
        return getFileName(bp);
    }


    /**
     * Restituisce il nome del file locale al client per la prima occorrenza del parametro.
     *
     * @param name nome del parametro.
     * @return nome del file locale al client per il parametro.<br>
     *      Restituisce <code>null</code> se il parametro non esiste o il parametro non
     *      � di tipo <code>type="file"</code>.
     * @exception FileNotFoundException se non riesce a trovare il file temporaneo.
     * @exception MessagingException se il file temporaneo � corrotto e non contiene
     *      dati in un formato mime valido.
     */
    public String getFileName(String name) throws MessagingException, FileNotFoundException
    {
        return getFileName(name, 0);
    }


    /**
     * Restituisce i nomi dei files locali al client per tutte le occorrenze del parametro.
     *
     * @param name nome del parametro.
     * @return nomi dei files locali al client per il parametro.<br>
     *      Gli elementi dell'array corrispondenti ad occorrenze di tipo diverso da
     *      <code>type="file"</code> sono impostati a <code>null</code>.
     * @exception FileNotFoundException se non riesce a trovare il file temporaneo.
     * @exception MessagingException se il file temporaneo � corrotto e non contiene
     *      dati in un formato mime valido.
     */
    public String[] getFileNames(String name) throws MessagingException, FileNotFoundException
    {
        String ret[] = new String[getCount(name)];
        for(int i = 0; i < ret.length; ++i) {
            ret[i] = getFileName(name, i);
        }
        return ret;
    }


    /**
     * Restituisce l'<code>Object</code> contenente il valore del parametro.
     *
     * @param name nome del parametro.
     * @param occ numero di occorrenza del parametro.
     * @return <code>Object</code> contenente il valore del parametro.<br>
     *      Restituisce <code>null</code> se il parametro non esiste o l'occorrenza
     *      non esiste.
     * @exception IOException se non riesce a leggere il file temporaneo.
     * @exception MessagingException se il file temporaneo � corrotto e non contiene
     *      dati in un formato mime valido.
     */
    public Object getContent(String name, int occ) throws MessagingException, IOException
    {
        MimeBodyPart bp = getBodyPart(name, occ);
        if(bp == null) return null;
        return bp.getContent();
    }


    /**
     * Restituisce l'<code>Object</code> contenente il valore della
     * prima occorrenza del parametro.
     *
     * @param name nome del parametro.
     * @return <code>Object</code> contenente il valore della prima
     *      occorrenza del parametro.<br>
     *      Restituisce <code>null</code> se il parametro non esiste.
     * @exception IOException se non riesce a leggere il file temporaneo.
     * @exception MessagingException se il file temporaneo � corrotto e non contiene
     *      dati in un formato mime valido.
     */
    public Object getContent(String name) throws MessagingException, IOException
    {
        return getContent(name, 0);
    }


    /**
     * Restituisce gli <code>Object</code> contenenti i valori di tutte
     * le occorrenze del parametro.
     *
     * @param name nome del parametro.
     * @return array di <code>Object</code> contenenti i valori di tutte le
     *      occorrenze del parametro.
     * @exception IOException se non riesce a leggere il file temporaneo.
     * @exception MessagingException se il file temporaneo � corrotto e non contiene
     *      dati in un formato mime valido.
     */
    public Object[] getContents(String name) throws MessagingException, IOException
    {
        Object ret[] = new Object[getCount(name)];
        for(int i = 0; i < ret.length; ++i) {
            ret[i] = getContent(name, i);
        }
        return ret;
    }


    /**
     * Restituisce il valore del parametro.
     *
     * @param name nome del parametro.
     * @param occ numero di occorrenza del parametro.
     * @return valore del parametro.<br>
     *      Restituisce <code>null</code> se il parametro non esiste o l'occorrenza
     *      non esiste.
     * @exception IOException se non riesce a leggere il file temporaneo.
     * @exception MessagingException se il file temporaneo � corrotto e non contiene
     *      dati in un formato mime valido.
     */
    public String getString(String name, int occ) throws MessagingException, IOException
    {
        Object ret = getContent(name, occ);
        if(ret == null) return null;
        return ret.toString();
    }


    /**
     * Restituisce il valore della prima occorrenza del parametro.
     *
     * @param name nome del parametro.
     * @return valore della prima occorrenza del parametro.<br>
     *      Restituisce <code>null</code> se il parametro non esiste.
     * @exception IOException se non riesce a leggere il file temporaneo.
     * @exception MessagingException se il file temporaneo � corrotto e non contiene
     *      dati in un formato mime valido.
     */
    public String getString(String name) throws MessagingException, IOException
    {
        return getString(name, 0);
    }


    /**
     * Restituisce il valore di tutte le occorrenze del parametro.
     *
     * @param name nome del parametro.
     * @return valori di tutte le occorrenze del parametro.
     * @exception IOException se non riesce a leggere il file temporaneo.
     * @exception MessagingException se il file temporaneo � corrotto e non contiene
     *      dati in un formato mime valido.
     */
    public String[] getStrings(String name) throws MessagingException, IOException
    {
        String ret[] = new String[getCount(name)];
        for(int i = 0; i < ret.length; ++i) {
            ret[i] = getString(name, i);
        }
        return ret;
    }


    /**
     * Restituisce un <code>InputStream</code> per la lettura del valore di una
     * occorrenza del parametro.
     *
     * @param name nome del parametro.
     * @param occ numero di occorrenza del parametro.
     * @return oggetto <code>InputStream</code> per la lettura del valore.<br>
     *      Restituisce <code>null</code> se il parametro non esiste o l'occorrenza
     *      non esiste.
     * @exception IOException se non riesce a leggere il file temporaneo.
     * @exception MessagingException se il file temporaneo � corrotto e non contiene
     *      dati in un formato mime valido.
     */
    public InputStream getInputStream(String name, int occ) throws MessagingException, IOException
    {
        MimeBodyPart bp = getBodyPart(name, occ);
        if(bp == null) return null;
        return bp.getInputStream();
    }


    /**
     * Restituisce un <code>InputStream</code> per la lettura del valore della
     * prima occorrenza del parametro.
     *
     * @param name nome del parametro.
     * @return oggetto <code>InputStream</code> per la lettura del valore.<br>
     *      Restituisce <code>null</code> se il parametro non esiste.
     * @exception IOException se non riesce a leggere il file temporaneo.
     * @exception MessagingException se il file temporaneo � corrotto e non contiene
     *      dati in un formato mime valido.
     */
    public InputStream getInputStream(String name) throws MessagingException, IOException
    {
        return getInputStream(name, 0);
    }


    /**
     * Restituisce gli <code>InputStream</code> per la lettura del valore di tutte
     * le occorrenze del parametro.
     *
     * @param name nome del parametro.
     * @return oggetto <code>InputStream</code> per la lettura dei valori.
     * @exception IOException se non riesce a leggere il file temporaneo.
     * @exception MessagingException se il file temporaneo � corrotto e non contiene
     *      dati in un formato mime valido.
     */
    public InputStream[] getInputStreams(String name) throws MessagingException, IOException
    {
        InputStream in[] = new InputStream[getCount(name)];
        for(int i = 0; i < in.length; ++i) {
            in[i] = getInputStream(name, i);
        }
        return in;
    }


    private void put(String name, File file)
    {
        Vector v = (Vector)bodies.get(name);
        if(v == null) {
            v = new Vector();
            bodies.put(name, v);
        }
        v.addElement(file);
    }


    private String getName(MimeBodyPart bp) throws MessagingException
    {
        String disposition = bp.getDisposition();
        if(disposition.toLowerCase().equals("form-data")) {
            return getAttribute(bp.getHeader("Content-disposition", null), "name");
        }
        else return null;
    }


    private String getFileName(MimeBodyPart bp) throws MessagingException
    {
        String disposition = bp.getDisposition();
        if(disposition.toLowerCase().equals("form-data")) {
            return getAttribute(bp.getHeader("Content-disposition", null), "filename");
        }
        else return null;
    }


    private String getAttribute(String str, String attr)
    {
        int idx = str.indexOf(attr + "=\"");
        if(idx == -1) return null;
        idx = idx + attr.length() + 2;
        int idx2 = str.indexOf("\"", idx);
        if(idx2 == -1) return null;
        return str.substring(idx, idx2);
    }


    private MimeBodyPart readBodyPart() throws IOException, MessagingException
    {
        currentFile = File.createTempFile("MPFormDataParser", ".tmp");
        currentFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(currentFile);
        byte b[] = null;
        byte prev[] = null;
        byte n[] = {'\n'};
        byte rn[] = {'\r', '\n'};
        while((b = readBytes()) != null) {
            try {
                String s = new String(b);
                if(s.startsWith(boundary)) {
                    endData = s.startsWith(endBoundary);
                    break;
                }
            }
            catch(Exception exc) {
                // Eccezione di encoding: non � un boundary!!!
            }
            if(prev != null) out.write(prev);
            int l = b.length;
            if((l >= 2) && (b[l - 2] == '\r') && (b[l - 1] == '\n')) {
                prev = rn;
                out.write(b, 0, l - 2);
            }
            else if((l >= 1) && (b[l - 1] == '\n')) {
                prev = n;
                out.write(b, 0, l - 1);
            }
            else {
                prev = null;
                out.write(b);
            }
        }
        out.flush();
        out.close();
        FileInputStream in = new FileInputStream(currentFile);
        MimeBodyPart bp = new MimeBodyPart(in);
        in.close();
        return bp;
    }


    private String readString() throws IOException
    {
        byte buf[] = readBytes();
        if(buf == null) return null;
        return new String(buf);
    }

    private byte[] readBytes() throws IOException
    {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(2048);
        byte b[] = new byte[1024];
        int l;
        while((l = in.readLine(b, 0, 1024)) != -1) {
            buf.write(b, 0, l);
            if(b[l - 1] == '\n') {
                return buf.toByteArray();
            }
        }
        return null;
    }


    /**
     * Rimuove i files temporanei contenenti i dati inviati dal client.
     */
    public void close()
    {
        removeFiles();
        bodies = new Hashtable();
    }


    /**
     * Elimina i files temporanei.
     */
    private void removeFiles()
    {
        for(Enumeration b = bodies.elements(); b.hasMoreElements();) {
            Vector v = (Vector)b.nextElement();
            for(Enumeration e = v.elements(); e.hasMoreElements();) {
                File f = (File)e.nextElement();
                f.delete();
            }
        }
    }


    /**
     * Elimina i files temporanei contenenti i valori dei parametri se non sono
     * stati esplicitamente eliminati.<br>
     * Questo metodo non dovrebbe essere esplicitamente invocato.<br>
     * Utilizzare <code>close()</code>.
     * @see #close()
     */
    public void finalize()
    {
        removeFiles();
    }
}

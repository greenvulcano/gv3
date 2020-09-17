/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.db;

import java.sql.Types;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class CachedResultSet implements ResultSetEx {
    // inizializzato a -1 cos� se vi sono errori rimane a -1
    private int              numcol          = -1;

    // indica la riga corrente: va da 1 a N
    private int              indice_corrente = 0;

    // vettore che memorizza le label delle colonne
    // va da 1 a N
    private String           labels[];

    // vettore che memorizza i tipi delle colonne
    // va da 1 a N
    private int              types[];

    protected Vector         cache           = new Vector();
    private Object           buffer[];

    private SimpleDateFormat dateFormat      = new SimpleDateFormat("y-M-d H:m:s");

    /**
     * Mette in cache i dati di rs e <u>chiude</u> rs.
     */
    public CachedResultSet(ResultSetEx rs) {
        numcol = rs.getColumnCount();
        labels = new String[numcol + 1];
        types = new int[numcol + 1];

        for (int i = 1; i <= numcol; ++i) {
            labels[i] = rs.getColumnLabel(i);
            types[i] = rs.getColumnType(i);
        }

        while (rs.next()) {
            String riga[] = new String[numcol + 1];
            for (int i = 1; i <= numcol; ++i) {
                riga[i] = rs.getString(i);
            }
            cache.addElement(riga);
        }
        rs.close();
    }

    /**
     * Chiude il result set e si riposiziona sul primo record.
     * Dopo <code>close()</code> � possibile scorrere di nuovo il result set.
     */
    public void close() {
        reset();
    }

    /**
     * Chiude il result set e si riposiziona sul primo record.
     * Dopo <code>reset()</code> � possibile scorrere di nuovo il result set.
     */
    public void reset() {
        indice_corrente = 0;
    }

    /**
     * Posiziona il puntatore sul record successivo. <br>
     * Invocare prima di leggere il primo record.
     *
     * @return true se il record corrente � significativo
     */
    public boolean next() {
        indice_corrente++;
        if (cache.size() >= indice_corrente) {
            buffer = (Object[]) cache.elementAt(indice_corrente - 1);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Restituisce il numero di record nel result set.
     *
     * @return numero di righe nel result set.
     */
    public int getRowsCount() {
        return cache.size();
    }

    /**
     * Restituisce il numero di colonne del result set.
     *
     * @return -1 se si verificano problemi
     */
    public int getColumnCount() {
        return numcol;
    }

    /**
     * Restituisce la label per la colonna di indice dato.
     *
     * @return null se si verificano problemi
     */
    public String getColumnLabel(int i) {
        if ((i > 0) && (i <= numcol)) {
            return labels[i];
        }
        else {
            return "";
        }
    }

    /**
     * Restituisce l'indice di una colonna dato il suo nome.
     *
     * @return -1 se si verificano problemi
     */
    public int findColumn(String s) {
        String appo = "";
        for (int i = 1; i <= numcol; ++i) {
            appo = getColumnLabel(i);
            if (appo.equals(s)) {
                return i;
            }
        }
        return -1;
    }

    public Object getObject(int i) {
        if ((i <= 0) || (i > numcol)) {
            return "";
        }
        else {
            return buffer[i];
        }
    }

    public Object getObject(String field) {
        int i;
        i = findColumn(field);
        return getObject(i);
    }

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato
     * come stringa.
     *
     * @param field nome del campo
     *
     * @return stringa vuota ("") se il campo specificato non esiste.
     */
    public String getString(String field) {
        int i;
        i = findColumn(field);
        return getString(i);
    }

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato
     * come stringa. <br>
     * Gli indici iniziano da 1.
     *
     * @param i indice del campo
     *
     * @return stringa vuota ("") se il campo specificato non esiste.
     */
    public String getString(int i) {
        if ((i <= 0) || (i > numcol)) {
            return "";
        }
        else {
            return "" + buffer[i];
        }
    }

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato
     * come intero.
     *
     * @param field nome del campo
     *
     * @exception NumberFormatException se il valore del campo non pu�
     *		essere interpretato come intero.
     */
    public int getInteger(String s) throws NumberFormatException {
        return Integer.parseInt(getString(s));
    }

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato
     * come intero.
     *
     * @exception NumberFormatException se il valore del campo non pu�
     *		essere interpretato come intero.
     */
    public int getInteger(int i) throws NumberFormatException {
        return Integer.parseInt(getString(i));
    }

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato
     * come double.
     *
     * @exception NumberFormatException se il valore del campo non pu�
     *		essere interpretato come double.
     */
    public double getDouble(String s) throws NumberFormatException {
        return Double.parseDouble(getString(s));
    }

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato
     * come double.
     *
     * @exception NumberFormatException se il valore del campo non pu�
     *		essere interpretato come double.
     */
    public double getDouble(int i) throws NumberFormatException {
        return Double.parseDouble(getString(i));
    }

    /**
     * Indica se un campo � null. Il campo � identificato can il suo nome.
     *
     * @return true se il campo � null.
     */
    public boolean isNull(String s) {
        return (((getString(s)).trim()).equals(""));
    }

    /**
     * Indica se un campo � null. Il campo � identificato con il suo nome.
     *
     * @return true se il campo � null.
     */
    public boolean isNull(int i) {
        return (((getString(i)).trim()).equals(""));
    }

    /**
     * Indica se il CachedResultSet � vuoto.
     */
    public boolean isEmpty() {
        if (getColumnCount() <= 0) {
            return true;
        }
        if (getRowsCount() <= 0) {
            return true;
        }
        return false;
    }

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato
     * come Date.
     *
     * @return null se non � possibile convertire la stringa in data
     */
    public Date getDate(String s) {
        return dateFormat.parse(getString(s), new ParsePosition(0));
    }

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato
     * come Date.
     *
     * @return null se non � possibile convertire la stringa in data
     */
    public Date getDate(int i) {
        return dateFormat.parse(getString(i), new ParsePosition(0));
    }

    /**
     * Restituisce una codifica numerica del tipo di dato
     * contenuto nella colonna i
     *
     * @return Types.NULL se vi � qualche errore
     *
     */
    public int getColumnType(int i) {
        if ((i < 1) || (i > numcol)) {
            return Types.NULL;
        }
        else {
            return types[i];
        }
    }

    /**
     * Restituisce una codifica numerica del tipo di dato
     * contenuto nel campo field
     *
     * @return Types.NULL se vi � qualche errore
     *
     */
    public int getColumnType(String field) {
        int i = findColumn(field);
        return getColumnType(i);
    }

    /**
     * Permette di assegnare dall'esterno il tipo alla colonna
     *
     * @param i rappresenta il numero di colonna
     * @param tipo rappresenta il tipo che si vuole assegnare
     *
     */
    public void setColumnType(int i, int tipo) {
        if (i < 1) {
            return;
        }
        types[i] = tipo;
    }

    /**
     * Permette di assegnare dall'esterno il tipo alla colonna
     *
     * @param field rappresenta il nome della colonna
     * @param tipo rappresenta il tipo che si vuole assegnare
     *
     */
    public void setColumnType(String field, int tipo) {
        int i = findColumn(field);
        setColumnType(i, tipo);
    }
}
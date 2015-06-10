/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.db;

import java.util.Date;

/**
 * Interfaccia per i risultati dal DB.
 *
 * @see max.db.DBResultSet
 * @see max.db.CachedResultSet
 * @see max.db.OrderedResultSet
 * @see max.db.VectorResultSet
 *
 */
public interface ResultSetEx {
    /**
     * Chiude il result set. Da invocarsi quando il result set non �
     * pi� necessario.
     */
    public void close();

    /**
     * Posiziona il puntatore sul record successivo. <br>
     * Invocare prima di leggere il primo record.
     *
     * @return true se il record corrente � significativo
     */
    public boolean next();

    /**
     * Restituisce il valore di un campo dato il suo nome.
     *
     * @param field nome del campo
     *
     * @return stringa vuota ("") se il campo specificato non esiste.
     */
    public Object getObject(String field);

    /**
     * Restituisce il valore di un campo dato il suo indice.<br>
     * Gli indici iniziano da 1.
     *
     * @param i indice del campo
     *
     * @return stringa vuota ("") se il campo specificato non esiste.
     */
    public Object getObject(int i);

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato
     * come stringa.
     *
     * @param field nome del campo
     *
     * @return stringa vuota ("") se il campo specificato non esiste.
     */
    public String getString(String field);

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato
     * come stringa. <br>
     * Gli indici iniziano da 1.
     *
     * @param i indice del campo
     *
     * @return stringa vuota ("") se il campo specificato non esiste.
     */
    public String getString(int i);

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato
     * come intero.
     *
     * @param field nome del campo
     *
     * @exception NumberFormatException se il valore del campo non pu�
     *		essere interpretato come intero.
     */
    public int getInteger(String field) throws NumberFormatException;

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato
     * come intero.
     *
     * @exception NumberFormatException se il valore del campo non pu�
     *		essere interpretato come intero.
     */
    public int getInteger(int i) throws NumberFormatException;

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato
     * come double.
     *
     * @exception NumberFormatException se il valore del campo non pu�
     *		essere interpretato come double.
     */
    public double getDouble(String s) throws NumberFormatException;

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato
     * come double.
     *
     * @exception NumberFormatException se il valore del campo non pu�
     *		essere interpretato come double.
     */
    public double getDouble(int i) throws NumberFormatException;

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato
     * come Date.
     *
     * @return null se non � possibile convertire la stringa in data
     */
    public Date getDate(String s);

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato
     * come Date.
     *
     * @return null se non � possibile convertire la stringa in data
     */
    public Date getDate(int i);

    /**
     * Indica se un campo � null. Il campo � identificato can il suo nome.
     *
     * @return true se il campo � null.
     */
    public boolean isNull(String s);

    /**
     * Indica se un campo � null. Il campo � identificato can il suo indice.
     *
     * @return true se il campo � null.
     */
    public boolean isNull(int i);

    /**
     * Restituisce l'indice di una colonna dato il suo nome.
     *
     * @return -1 se si verificano problemi
     */
    public int findColumn(String s);

    /**
     * Restituisce il numero di colonne del result set.
     *
     * @return -1 se si verificano problemi
     */
    public int getColumnCount();

    /**
     * Restituisce la label per la colonna di indice dato.
     *
     * @return null se si verificano problemi
     */
    public String getColumnLabel(int i);

    /**
     * Tipo della colonna i-esima. <br>
     * Le codifiche sono in <code>java.sql.Types</code>.
     *
     * @return tipo della colonna. Restituisce <code>Types.NULL</code>
     *			se si verificano problemi.
     */
    public int getColumnType(int column);
}

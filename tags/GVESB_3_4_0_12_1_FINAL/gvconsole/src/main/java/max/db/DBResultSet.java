/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

//import logs.*;

/**
 * Incapsula un oggetto <code>java.sql.ResultSet</code> ed l'oggetto
 * <code>java.sql.Statement</code> usato per eseguire la query che
 * ha prodotto il result set.<p>
 *
 * Inoltre risolve alcuni problemi legati al result set:
 * <ul>
 *   <li>Permette di leggere pi� volte lo stesso campo (cosa che alcuni
 *       result set non permettono)
 *   <li>Permette di <i>"dimenticarsi"</i> dello statement; alla
 *       chiusura del result set � chiuso anche lo statement (se cio'
 *       non avvenisse si avrebbe un consumo di risorse).
 * </ul>
 */
public class DBResultSet implements ResultSetEx {
    protected ResultSet         rs;
    protected ResultSetMetaData md;
    protected Statement         stmt;
    protected Hashtable         hdata;
    protected Vector            vdata;

    public DBResultSet(ResultSet _rs, Statement _stmt) throws SQLException {
        rs = _rs;
        stmt = _stmt;
        md = rs.getMetaData();
    }

    public DBResultSet(ResultSet _rs) throws SQLException {
        this(_rs, null);
    }

    /**
     * Chiude il result set. Da invocarsi quando il result set non �
     * pi� necessario.
     */
    public void close() {
        try {
            rs.close();
        }
        catch (SQLException e) {
            //Logs.logException(e);
            e.printStackTrace();
        }

        try {
            if (stmt != null) {
                stmt.close();
            }
        }
        catch (SQLException e) {
            //Logs.logException(e);
            e.printStackTrace();
        }
    }

    /**
     * Mette in una cache il record corrente
     *
     * @return true se il record corrente � significativo
     */
    public boolean next() {
        vdata = new Vector();
        hdata = new Hashtable();

        boolean more = false;
        int N = 0;
        try {
            more = rs.next();
            if (!more) {
                return false;
            }

            N = md.getColumnCount();
        }
        catch (SQLException e) {
            e.printStackTrace();
            //Logs.logException(e);
            return false;
        }

        for (int i = 1; i <= N; ++i) {
            Object o = null;

            try {
                o = rs.getObject(i);
            }
            catch (SQLException e) {
                //Logs.logException(e);
                e.printStackTrace();
            }

            if (o == null) {
                vdata.addElement("");
            }
            else {
                vdata.addElement(o);
            }

            try {
                if (o == null) {
                    hdata.put(md.getColumnLabel(i), "");
                }
                else {
                    hdata.put(md.getColumnLabel(i), o);
                }
            }
            catch (Exception e) {
                //Logs.logException(e);
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public Object getObject(String s) {
        return hdata.get(s);
    }

    public Object getObject(int i) {
        return vdata.elementAt(i - 1);
    }

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato
     * come stringa.
     */
    public String getString(String s) {
        return "" + hdata.get(s);
    }

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato
     * come stringa. <br>
     * Gli indici iniziano da 1.
     */
    public String getString(int i) {
        return "" + vdata.elementAt(i - 1);
    }

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato
     * come intero.
     *
     * @exception NumberFormatException se il valore del campo non pu�
     *		essere interpretato come intero.
     */
    public int getInteger(String s) throws NumberFormatException {
        return Integer.parseInt("" + hdata.get(s));
    }

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato
     * come intero.
     *
     * @exception NumberFormatException se il valore del campo non pu�
     *		essere interpretato come intero.
     */
    public int getInteger(int i) throws NumberFormatException {
        return Integer.parseInt("" + vdata.elementAt(i - 1));
    }

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato
     * come double.
     *
     * @exception NumberFormatException se il valore del campo non pu�
     *		essere interpretato come double.
     */
    public double getDouble(String s) throws NumberFormatException {
        return new Double("" + hdata.get(s)).doubleValue();
    }

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato
     * come double.
     *
     * @exception NumberFormatException se il valore del campo non pu�
     *		essere interpretato come double.
     */
    public double getDouble(int i) throws NumberFormatException {
        return new Double("" + vdata.elementAt(i - 1)).doubleValue();
    }

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato
     * come Date.
     *
     * @return null se non � possibile convertire la stringa in data
     */
    public Date getDate(String s) {
        SimpleDateFormat sdf = new SimpleDateFormat("y-M-d H:m:s");
        return sdf.parse(getString(s), new ParsePosition(0));
    }

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato
     * come Date.
     *
     * @return null se non � possibile convertire la stringa in data
     */
    public Date getDate(int i) {
        SimpleDateFormat sdf = new SimpleDateFormat("y-M-d H:m:s");
        return sdf.parse(getString(i), new ParsePosition(0));
    }

    /**
     * Indica se un campo � null. Il campo � identificato can il suo nome.
     *
     * @return true se il campo � null.
     */
    public boolean isNull(String s) {
        return ("" + hdata.get(s)).trim().equals("");
    }

    /**
     * Indica se un campo � null. Il campo � identificato can il suo indice.
     *
     * @return true se il campo � null.
     */
    public boolean isNull(int i) {
        return ("" + vdata.elementAt(i - 1)).trim().equals("");
    }

    /**
     * Restituisce l'indice di una colonna dato il suo nome.
     *
     * @return -1 se si verificano problemi
     */
    public int findColumn(String s) {
        try {
            return rs.findColumn(s);
        }
        catch (SQLException e) {
            //Logs.logException(e);
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Restituisce il numero di colonne del result set.
     *
     * @return -1 se si verificano problemi
     */
    public int getColumnCount() {
        try {
            return md.getColumnCount();
        }
        catch (SQLException e) {
            //Logs.logException(e);
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Restituisce la label per la colonna di indice dato.
     *
     * @return null se si verificano problemi
     */
    public String getColumnLabel(int i) {
        try {
            return md.getColumnLabel(i);
        }
        catch (SQLException e) {
            //Logs.logException(e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tipo della colonna i-esima. <br>
     * Le codifiche sono in <code>java.sql.Types</code>.
     *
     * @return tipo della colonna. Restituisce <code>Types.NULL</code>
     *			se si verificano problemi.
     */
    public int getColumnType(int column) {
        try {
            return md.getColumnType(column);
        }
        catch (SQLException e) {
            //Logs.logException(e);
            e.printStackTrace();
        }

        return Types.NULL;
    }
}

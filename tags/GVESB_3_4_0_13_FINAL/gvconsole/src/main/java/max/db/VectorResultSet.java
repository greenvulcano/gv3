/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
package max.db;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

// import logs.*;

/**
 * Interfaccia per i risultati contenuti in generiche strutture dati.
 * <p>
 * Gli oggetti devono essere <u>tutti dello stesso tipo</u>.
 */
public class VectorResultSet implements ResultSetEx, Comparator
{
    private Object[] array;
    private Field[]  campi;
    private int      index;

    public int compare(Object o1, Object o2)
    {
        // Compares its two arguments for order.
        String n1;
        if (o1 instanceof Field) {
            n1 = ((Field) o1).getName();
        }
        else {
            n1 = "" + o1;
        }

        String n2;
        if (o2 instanceof Field) {
            n2 = ((Field) o2).getName();
        }
        else {
            n2 = "" + o2;
        }

        return n1.compareTo(n2);
    }

    @Override
    public boolean equals(Object obj)
    {
        // Indicates whether some other object is "equal to" this Comparator.
        return obj == this;
    }

    public VectorResultSet(Object[] _array)
    {
        init(_array);
    }

    public VectorResultSet(Vector vector)
    {
        Object _array[] = new Object[vector.size()];
        int i = 0;
        for (Enumeration e = vector.elements(); e.hasMoreElements();) {
            _array[i++] = e.nextElement();
        }
        init(_array);
    }

    public VectorResultSet(Enumeration elems)
    {
        Vector vector = new Vector();
        while (elems.hasMoreElements()) {
            vector.addElement(elems.nextElement());
        }

        Object _array[] = new Object[vector.size()];
        int i = 0;
        for (Enumeration e = vector.elements(); e.hasMoreElements();) {
            _array[i++] = e.nextElement();
        }
        init(_array);
    }

    private void init(Object[] _array)
    {
        array = _array;
        index = -1;
        if (array.length > 0) {
            campi = array[0].getClass().getFields();
            Arrays.sort(campi, this);
        }
        else {
            campi = new Field[0];
        }
    }

    /**
     * Chiude il result set. Da invocarsi quando il result set non � pi�
     * necessario.
     */
    public void close()
    {
        index = -1;
    }

    /**
     * Chiude il result set e resetta il puntatore al record corrente. Da
     * invocarsi quando di vuole ricominciare a leggere da capo il result set.
     */
    public void reset()
    {
        index = -1;
    }

    /**
     * Posiziona il puntatore sul record successivo. <br>
     * Invocare prima di leggere il primo record.
     *
     * @return true se il record corrente � significativo
     */
    public boolean next()
    {
        ++index;
        if (index < array.length) {
            return true;
        }
        else {
            return false;
        }
    }

    private Object getObject(Class cls, Object obj, String fldName) throws Exception
    {
        int idx = fldName.indexOf('.');
        if (idx == -1) {
            try {
                Field fld = cls.getField(fldName);
                return fld.get(obj);
            }
            catch (NoSuchFieldException exc) {
                // se non esiste un campo prova ad utilizzare un metodo
                // senza parametri con il nome dato.
                Method mth = cls.getMethod(fldName, null);
                return mth.invoke(obj, null);
            }
        }

        String pre = fldName.substring(0, idx);
        String post = fldName.substring(idx + 1);
        try {
            Field fld = cls.getField(pre);
            return getObject(fld.getType(), fld.get(obj), post);
        }
        catch (NoSuchFieldException exc) {
            // se non esiste un campo prova ad utilizzare un metodo
            // senza parametri con il nome dato.
            Method mth = cls.getMethod(pre, null);
            return getObject(mth.getReturnType(), mth.invoke(obj, null), post);
        }
    }

    public Object getObject(String field)
    {
        Object record = array[index];
        Class cls = record.getClass();
        try {
            return getObject(cls, record, field);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object getObject(int fieldIdx)
    {
        Object record = array[index];
        try {
            Field fld = campi[fieldIdx - 1];
            return fld.get(record);
        }
        catch (Exception e) {
            // Logs.log("sql", "Warning VectorResultSet: Campo di indice " +
            // fieldIdx + " non presente nella classe '" + record.getClass() +
            // "'");
            System.out.println("sql: Warning VectorResultSet: Campo di indice " + fieldIdx
                    + " non presente nella classe '" + record.getClass() + "'");
            return null;
        }
    }

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato come
     * stringa.
     *
     * @param field
     *        nome del campo
     *
     * @return stringa vuota ("") se il campo specificato non esiste.
     */
    public String getString(String field)
    {
        Object obj = getObject(field);
        if (obj == null) {
            return "";
        }

        if (obj instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format((Date) obj);
        }

        return "" + obj;
    }

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato come
     * stringa. <br>
     * Gli indici iniziano da 1.
     *
     * @param i
     *        indice del campo
     *
     * @return stringa vuota ("") se il campo specificato non esiste.
     */
    public String getString(int i)
    {
        Object obj = getObject(i);
        if (obj == null) {
            return "";
        }

        if (obj instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format((Date) obj);
        }

        return "" + obj;
    }

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato come
     * intero.
     *
     * @param field
     *        nome del campo
     *
     * @exception NumberFormatException
     *            se il valore del campo non pu� essere interpretato come
     *            intero.
     */
    public int getInteger(String field) throws NumberFormatException
    {
        return Integer.parseInt(getString(field));
    }

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato come
     * intero.
     *
     * @exception NumberFormatException
     *            se il valore del campo non pu� essere interpretato come
     *            intero.
     */
    public int getInteger(int i) throws NumberFormatException
    {
        return Integer.parseInt(getString(i));
    }

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato come
     * double.
     *
     * @exception NumberFormatException
     *            se il valore del campo non pu� essere interpretato come
     *            double.
     */
    public double getDouble(String s) throws NumberFormatException
    {
        Double dbl = new Double(getString(s));
        return dbl.doubleValue();
    }

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato come
     * double.
     *
     * @exception NumberFormatException
     *            se il valore del campo non pu� essere interpretato come
     *            double.
     */
    public double getDouble(int i) throws NumberFormatException
    {
        Double dbl = new Double(getString(i));
        return dbl.doubleValue();
    }

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato come
     * Date.
     *
     * @return null se non � possibile convertire la stringa in data
     */
    public Date getDate(String s)
    {
        int idx = findColumn(s);
        if (idx == -1) {
            return null;
        }
        return getDate(idx);
    }

    /**
     * Restituisce il valore di un campo dato il suo indice, interpretato come
     * Date.
     *
     * @return null se non � possibile convertire la stringa in data
     */
    public Date getDate(int i)
    {
        Object obj = getObject(i);
        if (obj == null) {
            return null;
        }

        if (obj instanceof Date) {
            return (Date) obj;
        }
        else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("y-M-d H:m:s");
                return sdf.parse("" + obj);
            }
            catch (ParseException exc) {
                exc.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Indica se un campo � null. Il campo � identificato con il suo nome.
     *
     * @return true se il campo � null.
     */
    public boolean isNull(String s)
    {
        return (getObject(s) == null);
    }

    /**
     * Indica se un campo � null. Il campo � identificato con il suo indice.
     *
     * @return true se il campo � null.
     */
    public boolean isNull(int i)
    {
        return (getObject(i) == null);
    }

    /**
     * Restituisce l'indice di una colonna dato il suo nome.
     *
     * @return -1 se si verificano problemi
     */
    public int findColumn(String s)
    {
        for (int i = 0; i < campi.length; ++i) {
            if (campi[i].getName().equals(s)) {
                return i + 1;
            }
        }

        return -1;
    }

    /**
     * Restituisce il numero di colonne del result set.
     *
     * @return -1 se si verificano problemi
     */
    public int getColumnCount()
    {
        return campi.length;
    }

    /**
     * Restituisce la label per la colonna di indice dato.
     *
     * @return null se si verificano problemi
     */
    public String getColumnLabel(int i)
    {
        try {
            return campi[i - 1].getName();
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Tipo della colonna i-esima. <br>
     * Le codifiche sono in <code>java.sql.Types</code>.
     *
     * @return tipo della colonna. Restituisce <code>Types.NULL</code> se si
     *         verificano problemi.
     */
    public int getColumnType(int column)
    {
        try {
            Class cls = campi[column - 1].getType();
            if (cls == Integer.TYPE) {
                return Types.INTEGER;
            }
            else if (cls == Double.TYPE) {
                return Types.DOUBLE;
            }
            else if (cls == Float.TYPE) {
                return Types.DOUBLE;
            }
            else if (cls == Byte.TYPE) {
                return Types.INTEGER;
            }
            else if (cls == Short.TYPE) {
                return Types.INTEGER;
            }
            else if (cls == Character.TYPE) {
                return Types.VARCHAR;
            }
            else if (cls == java.util.Date.class) {
                return Types.DATE;
            }
            else if (cls == Boolean.TYPE) {
                return Types.VARCHAR;
            }
            else if (cls == Long.TYPE) {
                return Types.NUMERIC;
            }
            else {
                return Types.VARCHAR;
            }
        }
        catch (Exception e) {
            return Types.NULL;
        }
    }
}

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

public class OrderedResultSet extends CachedResultSet {
    private int                     colonna1;
    private int                     colonna2;

    private int                     t_colonna1;
    private int                     t_colonna2;

    private int                     num_righe;

    private SimpleDateFormat        dateFormat[];

    private static SimpleDateFormat defaultDateFormat = new SimpleDateFormat("y-M-d H:m:s");

    public OrderedResultSet(ResultSetEx rs) {
        super(rs);
        num_righe = cache.size();
        dateFormat = new SimpleDateFormat[getColumnCount() + 1];
        for (int i = 0; i < dateFormat.length; ++i) {
            dateFormat[i] = null;
        }
    }

    /**
     * Imposta il tipo del campo a <code>Types.DATE</code> ed imposta
     * il formato.
     */
    public void setDateFormat(String field, String format) {
        int i = findColumn(field);
        if (i < 1) {
            return;
        }
        setColumnType(i, Types.DATE);
        dateFormat[i] = new SimpleDateFormat(format);
    }

    /**
     * Imposta il tipo del campo a <code>Types.DATE</code> ed imposta
     * il formato.
     */
    public void setDateFormat(int column, String format) {
        setColumnType(column, Types.DATE);
        dateFormat[column] = new SimpleDateFormat(format);
    }

    /**
     * Ordina attraverso un meccanismo di Quicksorting
     * Se per qualche motivo vi � un errore, es. si tenta di ordinare su
     * colonne che non esistono, l'ordinamento viene abbandonato
     *
     * @param p � la prima colonna presa in considerazione
     * @param s � la seconda colonna presa in considerazione. Se fosse 0 si ordina solo rispetto alla prima
     *
     *
     */
    public void sort(int p, int s) {
        // nel caso in cui la seconda colonna sia 0, si ordina solo
        // rispetto alla prima
        if (s == 0) {
            s = p;
        }

        colonna1 = p;
        colonna2 = s;

        t_colonna1 = getColumnType(colonna1);
        t_colonna2 = getColumnType(colonna2);

        if ((t_colonna1 == Types.NULL) || (t_colonna2 == Types.NULL)) {
            close();
            return;
        }
        else {
            quickSort(0, num_righe - 1);
            close();
        }
    }

    /**
     * Ordina attraverso un meccanismo di Quicksorting
     * Se per qualche motivo vi � un errore, es. si tenta di ordinare su
     * colonne che non esistono, l'ordinamento viene abbandonato
     *
     * @param nome_colonna_1 � la label della prima colonna presa in considerazione
     * @param nome_colonna_2 � la label della seconda colonna presa in considerazione. Se fosse 0 si ordina solo rispetto alla prima
     *
     *
     */
    public void sort(String nome_colonna_1, String nome_colonna_2) {
        int colonna1 = findColumn(nome_colonna_1);
        int colonna2 = findColumn(nome_colonna_2);
        sort(colonna1, colonna2);
    }

    /**
     * Implementa Ricorsione del Quick-Sort
     */
    private void quickSort(int a, int z) {
        int pivot;

        if (a >= z) {
            return;
        }
        pivot = partition(a, z);
        quickSort(a, pivot);
        quickSort(pivot + 1, z);
    }

    /**
     * Implementa Quick-Sort
     */
    private int partition(int a, int z) {
        int i = a - 1;
        int j = z + 1;
        int p = ((i + j) / 2);

        do {
            do {
                --j;
            } while (compareRows(j, p) > 0);

            do {
                ++i;
            } while (compareRows(i, p) < 0);

            if (i < j) {
                swapRows(i, j);
                if (i == p) {
                    p = j;
                }
                else if (j == p) {
                    p = i;
                }
            }
            else {
                return j;
            }
        } while (true);
    }

    /**
     * Implementa l'ordinamento sulla prima colonna e successivamente sulla seconda
     */
    private int compareRows(int a, int z) {
        int r = compareCol(a, z, colonna1, t_colonna1);
        if (r == 0) {
            return compareCol(a, z, colonna2, t_colonna2);
        }
        else {
            return r;
        }
    }

    /**
     * Comparazione tra colonne nei diversi casi: Numero,Data,Stringa
     */
    private int compareCol(int a, int z, int col, int t_col) {
        String r_1[] = new String[getColumnCount()];
        String r_2[] = new String[getColumnCount()];
        r_1 = (String[]) cache.elementAt(a);
        r_2 = (String[]) cache.elementAt(z);

        if ((r_1[col].equals("")) && (r_2[col].equals(""))) {
            return 0;
        }
        else {
            if (r_1[col].equals("")) {
                return -1;
            }
            if (r_2[col].equals("")) {
                return 1;
            }
        }

        if ((t_col == Types.BIGINT) || (t_col == Types.BINARY) || (t_col == Types.BIT) || (t_col == Types.DECIMAL)
                || (t_col == Types.DOUBLE) || (t_col == Types.FLOAT) || (t_col == Types.INTEGER)
                || (t_col == Types.LONGVARBINARY) || (t_col == Types.NUMERIC) || (t_col == Types.REAL)
                || (t_col == Types.SMALLINT) || (t_col == Types.TINYINT) || (t_col == Types.VARBINARY)) {
            // siamo in caso di confronti numerici - assumiamo tutti double
            double d_1 = Double.parseDouble(r_1[col]);
            double d_2 = new Double(r_2[col]).doubleValue();

            if (d_1 < d_2) {
                return -1;
            }
            if (d_1 > d_2) {
                return 1;
            }
            return 0;
        }

        if ((t_col == Types.DATE) || (t_col == Types.TIME) || (t_col == Types.TIMESTAMP)) {
            // siamo nel caso in cui bisogna fare dei confronti tra date
            SimpleDateFormat sdf = (dateFormat[col] != null ? dateFormat[col] : defaultDateFormat);

            Date d_1 = sdf.parse(r_1[col], new ParsePosition(0));
            Date d_2 = sdf.parse(r_2[col], new ParsePosition(0));
            if (d_1 == null) {
                if (d_2 == null) {
                    return 0;
                }
                else {
                    return -1;
                }
            }
            if (d_2 == null) {
                return 1;
            }

            if (d_1.before(d_2)) {
                return 1;
            }
            if (d_1.after(d_2)) {
                return -1;
            }
            return 0;
        }

        // in tutti gli altri casi si tratta tutto come Stringhe

        String s_1 = r_1[col];
        String s_2 = r_2[col];
        if (s_1.compareTo(s_2) < 0) {
            return -1;
        }
        if (s_1.compareTo(s_2) > 0) {
            return 1;
        }
        return 0;
    }

    /**
     * Scambia le righe di cache
     */
    private void swapRows(int a, int z) {
        if (a == z) {
            return;
        }

        String r_1[] = new String[getColumnCount()];
        String r_2[] = new String[getColumnCount()];

        r_1 = (String[]) cache.elementAt(a);
        r_2 = (String[]) cache.elementAt(z);

        cache.setElementAt(r_1, z);
        cache.setElementAt(r_2, a);
    }
}

/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.db;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class ResultSetExHelper {
    public static class Column {
        public String NAME;
        public int    TYPE;

        public Column(String name, int type) {
            NAME = name;
            TYPE = type;
        }
    }

    public static ResultSetEx getColumns(ResultSetEx rs) {
        int N = rs.getColumnCount();
        Vector v = new Vector();
        for (int i = 1; i <= N; ++i) {
            v.add(new Column(rs.getColumnLabel(i), rs.getColumnType(i)));
        }
        return new VectorResultSet(v);
    }

    public static ResultSetEx getColumns(Vector rs) {
        if (rs.size() == 0) {
            return new VectorResultSet(new Vector());
        }
        Object o = rs.elementAt(0);
        return getObjectColumns(o);
    }

    public static ResultSetEx getColumns(Object[] rs) {
        if (rs.length == 0) {
            return new VectorResultSet(new Vector());
        }
        return getObjectColumns(rs[0]);
    }

    public static ResultSetEx getObjectColumns(Object o) {
        Vector v = new Vector();
        if (o instanceof Map) {
            Map m = (Map) o;
            Iterator i = m.keySet().iterator();
            while (i.hasNext()) {
                v.addElement(new Column(i.next().toString(), Types.VARCHAR));
            }
        }
        else {
            Field fields[] = o.getClass().getFields();
            for (int i = 0; i < fields.length; ++i) {
                v.addElement(new Column(fields[i].getName(), Types.VARCHAR));
            }
        }
        return new VectorResultSet(v);
    }
}

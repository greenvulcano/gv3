/*
 * Copyright (c) 2004 Maxime Informatica s.n.c. - All right reserved
 *
 */
package max.documentation.dtd;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author
 */
public class TextBlock {
    //---------------------------------------------------------------
    // FIELDS
    //---------------------------------------------------------------

    private String    type;
    private ArrayList rows;

    //---------------------------------------------------------------
    // CONSTRUCTORS
    //---------------------------------------------------------------

    public TextBlock(String type) {
        this.type = type;
        rows = new ArrayList();
    }

    //---------------------------------------------------------------
    // GETTERS/SETTERS METHODS
    //---------------------------------------------------------------

    public boolean isA(String type) {
        return this.type.equals(type);
    }

    /**
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    //---------------------------------------------------------------
    // METHODS
    //---------------------------------------------------------------

    public void insert(int idx, String row) {
        rows.add(idx, row);
    }

    public void append(String row) {
        rows.add(row);
    }

    public void remove(int idx) {
        rows.remove(idx);
    }

    public int size() {
        return rows.size();
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    public String get(int idx) {
        return (String) rows.get(idx);
    }

    public Iterator iterator() {
        return rows.iterator();
    }

    public void deleteStartingSpaces() {
        // Determinazione degli spazi da eliminare
        //
        int startingSpaces = Integer.MAX_VALUE;
        Iterator i = rows.iterator();
        while (i.hasNext()) {
            String row = (String) i.next();
            int spaces = 0;
            boolean isEmpty = true;
            for (int j = 0; j < row.length(); ++j) {
                char c = row.charAt(j);
                if (Character.isWhitespace(c)) {
                    ++spaces;
                }
                else {
                    isEmpty = false;
                    break;
                }
            }
            if (!isEmpty && (spaces < startingSpaces)) {
                startingSpaces = spaces;
            }
        }

        // Rimozione
        //
        if ((startingSpaces == Integer.MAX_VALUE) || (startingSpaces == 0)) {
            return;
        }

        ArrayList newRows = new ArrayList();
        i = rows.iterator();
        while (i.hasNext()) {
            String row = (String) i.next();
            row = row.substring(startingSpaces);
            newRows.add(row);
        }

        rows = newRows;
    }

    public String toString(int startIdx) {
        StringBuffer out = new StringBuffer();
        int n = size();
        for (int i = startIdx; i < n; ++i) {
            out.append(get(i)).append("\n");
        }
        return out.toString();

    }

    @Override
    public String toString() {
        return toString(0);
    }
}

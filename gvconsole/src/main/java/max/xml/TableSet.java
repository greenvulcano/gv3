/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:49 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/TableSet.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Id: TableSet.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Node;


public class TableSet
{
    private List tables;
    private boolean empty;

    public TableSet()
    {
        tables = new LinkedList();
        empty = true;
    }

    public void add(Table table)
    {
        tables.add(table);
        empty = false;
    }

    /**
     * Iterates over contained Table
     */
    public Iterator iterator()
    {
        return tables.iterator();
    }

    public Table findTable(Node node)
    {
        if(empty) return null;

        Iterator i = tables.iterator();
        while(i.hasNext()) {
            Table table = (Table)i.next();
            if(table.contains(node)) {
                return table;
            }
        }
        return null;
    }

    public Table findTable(Collection nodes)
    {
        if(empty) return null;

        Iterator i = nodes.iterator();
        while(i.hasNext()) {
            Node node = (Node)i.next();
            Table table = findTable(node);
            if(table != null) {
                return table;
            }
        }
        return null;
    }

    // Array di lavoro
    private Vector nodeVector = new Vector();

    /**
     * @return true if the ContentModelInstance is assigned to a table.
     */
    public boolean assignToTable(XMLBuilder.Operation operation)
    {
        ContentModelInstance cmi = operation.getContentModelInstance();
        if(cmi == null) return false;

        nodeVector.clear();
        cmi.getDOMNodes(nodeVector);
        Table table = findTable(nodeVector);
        if(table == null) {
            return false;
        }

        table.assignOperation(operation);
        return true;
    }
}

/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:49 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/RowDescriptor.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Id: RowDescriptor.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Contains the elements that represent a row and the menu of that row, while the
 * XMLBuilder is building the interface.
 */
public class RowDescriptor
{
    //------------------------------------------------------------------------------------
    // FIELDS
    //------------------------------------------------------------------------------------

    /**
     * Contatore per la generazione dei nomi dei menu.
     */
    private static int menuId = 0;

    /**
     * Element describing the row of the interface.
     */
    private Element row;

    /**
     * Element describing the menu of the row.
     */
    private Element menu;

    /**
     * Name of the row (for normal row is "row", for table row is "trow").
     */
    private String rowName;

    /**
     * Document to use in order to create Nodes.
     */
    private Document intfc;

    /**
     * Indicates if the row is empty or not.
     */
    private boolean empty;

    //------------------------------------------------------------------------------------
    // CONSTRUCTOR
    //------------------------------------------------------------------------------------

    /**
     * Build an InterfaceRow.
     */
    public RowDescriptor(Document intfc, String rowName)
    {
        this.rowName = rowName;
        this.intfc = intfc;
        reset(null);
    }

    //------------------------------------------------------------------------------------
    // METHODS
    //------------------------------------------------------------------------------------

    /**
     * Initialize the row and the menu.
     * If the previous row is not empty it is possible to attach it to a parent element.
     * After this call, also the id change.
     *
     * @param appendTo if the row is not empty, then is appended to the given node.
     *      If null, the row is not appended to any parent.
     */
    public void reset(Node appendTo)
    {
        if(!empty) {
            if(appendTo != null) {
                appendTo.appendChild(row);
            }
            row = (Element)intfc.createElement(rowName);
            menu = null;
            empty = true;
        }
    }

    /**
     * Append a child to the row.
     *
     * @param node
     */
    public void appendChild(Node node)
    {
        empty = false;
        row.appendChild(node);
    }

    /**
     * Set an attribute of the row.
     *
     * @param attr
     * @param value
     */
    public void setAttribute(String attr, String value)
    {
        row.setAttribute(attr, value);
    }

    /**
     * Aggiunge un item al menu.
     *
     * @param label
     * @param key
     * @param type
     * @param descr
     */
    public void addMenuItem(String label, String key, String type, String descr)
    {
        if(menu == null) {
            menu = (Element)intfc.createElement("menu");
            menu.setAttribute("name", "menu" + (++menuId));
            row.appendChild(menu);
        }

        empty = false;

        Element menuItem = intfc.createElement("menu-item");
        menu.appendChild(menuItem);
        menuItem.setAttribute("label", escape(label));
        menuItem.setAttribute("key", escape(key));
        menuItem.setAttribute("type", escape(type));
        menuItem.setAttribute("description", escape(descr));
    }

    //------------------------------------------------------------------------------------
    // HELPERS
    //------------------------------------------------------------------------------------

    /**
     * Sostituisce gli apici (') con la sequenza "\'"
     *
     * @param s
     * @return
     */
    protected String escape(String s)
    {
        StringBuffer str = new StringBuffer();

        int len = (s != null) ? s.length() : 0;
        for ( int i = 0; i < len; i++ ) {
            char ch = s.charAt(i);
            switch ( ch ) {
            case '\'': {
                    str.append("\\'");
                    break;
                }
            default: {
                    str.append(ch);
                }
            }
        }

        return(str.toString());
    }
}

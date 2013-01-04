/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:49 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/Table.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Id: Table.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;

import max.core.MaxException;
import max.xpath.XPath;
import max.xpath.XPathAPI;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Table
{
    //--------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------

    private XPath nodesXPath = null;
    private String description = "";
    private int numColumns = 0;
    private XPath xpaths[] = null;
    private String labels[] = null;
    private boolean external[] = null;
    private Set nodes = null;
    private List operations = null;

    /**
     * Map<ContentModel.toString(), Operation>
     */
    private Map insertOperations = null;

    //--------------------------------------------------------------------------
    // CONSTRUCTOR
    //--------------------------------------------------------------------------

    /**
     * Costruisce un oggetto Table a partire dalla sua definizione.
     * La definizione � una stringa nel formato:
     * <pre>
     * | Description | nodesXpath | Label-1 | xpath-1 | ... | Label-N | xpath-N
     * </pre>
     * Dove il separatore dei tokens � determinato dal primo carattere
     * della definizione. Nell'esempio il separatore � | ma pu� essere un
     * altro qualsiasi carattere.
     */
    public Table(String definition)
    {
        // Il delimitatore � il primo carattere della definizione
        //
        String delim = definition.substring(0, 1);

        // Il resto della definizione � la definizione della tabella
        //
        definition = definition.substring(1).trim();

        StringTokenizer tokens = new StringTokenizer(definition, delim);

        description = tokens.nextToken().trim();
        String xpathStr = tokens.nextToken().trim();
        try {
            nodesXPath = new XPath(xpathStr);
        }
        catch(TransformerException exc) {
            System.out.println("ERROR: Invalid xpath: " + xpathStr);
            // TO DO: che deve succedere qui?
        }

        List labelList = new LinkedList();
        List xpathList = new LinkedList();

        while(tokens.hasMoreTokens()) {
            String label = tokens.nextToken().trim();
            xpathStr = tokens.nextToken().trim();
            XPath xpath = null;
            try {
                xpath = new XPath(xpathStr);
            }
            catch(TransformerException exc) {
                System.out.println("ERROR: Invalid xpath: " + xpathStr);
                // TO DO: che deve succedere qui?
            }
            labelList.add(label);
            xpathList.add(xpath);
        }

        numColumns = labelList.size();
        labels = new String[numColumns];
        xpaths = new XPath[numColumns];
        external = new boolean[numColumns];

        labelList.toArray(labels);
        xpathList.toArray(xpaths);

        for(int i = 0; i < labels.length; ++i) {
            if(labels[i].startsWith("*")) {
                external[i] = true;
                labels[i] = labels[i].substring(1);
            }
            else {
                external[i] = false;
            }
        }
    }


    /**
     * Calculates the table nodes.
     */
    public void init(Node prentNode, XPathAPI xpathAPI) throws MaxException
    {
        operations = new LinkedList();
        insertOperations = new LinkedHashMap();

        try {
            NodeList list = xpathAPI.selectNodeList(prentNode, nodesXPath);
            int N = list.getLength();
            nodes = new LinkedHashSet();
            for(int i = 0; i < N; ++i) {
                nodes.add(list.item(i));
            }
        }
        catch(TransformerException exc) {
            throw new MaxException(exc);
        }
    }

    /**
     * Checks if a node is into the table.
     */
    public boolean contains(Node node)
    {
        return nodes.contains(node);
    }

    public boolean contains(Collection coll)
    {
        Iterator i = coll.iterator();
        while(i.hasNext()) {
            Node node = (Node)i.next();
            if(nodes.contains(node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Assign a content model instance to the table.
     */
    public void assignOperation(XMLBuilder.Operation operation)
    {
        switch(operation.getType()) {

            case XMLBuilder.Operation.T_DELETE:
            case XMLBuilder.Operation.T_COPY:
            case XMLBuilder.Operation.T_CUT:
            case XMLBuilder.Operation.T_SELECT:
            case XMLBuilder.Operation.T_CHANGE:
			case XMLBuilder.Operation.T_PASTE:
            case XMLBuilder.Operation.T_SELECT_P:
                operations.add(operation);
                break;

            case XMLBuilder.Operation.T_INSERT_B:
            case XMLBuilder.Operation.T_INSERT_A:
				insertOperations.put(operation.getContentModel().toString() + "i", operation);
				break;

            case XMLBuilder.Operation.T_PASTE_A:
            case XMLBuilder.Operation.T_PASTE_B:
                insertOperations.put(operation.getContentModel().toString() + "p", operation);
                break;
        }
    }

    /**
     * Return an iterator to the table nodes.
     */
    public Iterator iterator()
    {
        return nodes.iterator();
    }

    /**
     * Return an iterator to the table nodes.
     */
    public Iterator operationIterator()
    {
        return operations.iterator();
    }

    /**
     * Return an iterator to the table nodes.
     */
    public Iterator insertOperationIterator()
    {
        return insertOperations.values().iterator();
    }

    public String getDescription()
    {
        return description;
    }

    public int getNumColumns()
    {
        return numColumns;
    }

    public String getLabel(int i)
    {
        return labels[i];
    }

    public XPath getXPath(int i)
    {
        return xpaths[i];
    }

    public boolean isExternal(int i)
    {
        return external[i];
    }
}

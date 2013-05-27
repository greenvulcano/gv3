/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:56 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xpath/XPathFunction.java,v 1.1 2010-04-03 15:28:56 nlariviera Exp $
 * $Id: XPathFunction.java,v 1.1 2010-04-03 15:28:56 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xpath;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;

/**
 * Interfaccia per una funzione estesa dell'XPath.
 * Le classi implementative dovranno avere un costruttore senza parametri.
 *
 */
public interface XPathFunction
{
    /**
     * Calcola il valore della funzione che estende l'XPath.
     *
     * @param contextNode nodo di contesto per la valutazione dell'XPath.
     * @param params argomenti della funzione.
     *      Gli elementi dell'array possono essere dei seguenti tipi:
     *      Double, String, Boolean, NodeList, DocumentFragment, List.
     *      In casi particolari (esempio composizioni di pi� funzioni estese)
     *      � possibile avere un altro generico Object o null.
     *      Anche nel caso in cui ci si aspetta un Node, si avr� un NodeList
     *      contenente un solo elemento.
     * @return � possibile ritornare:
     *  <ul>
     *      <li>Un Node
     *      <li>Un String
     *      <li>Un Number
     *      <li>Un Boolean
     *      <li>null
     *      <li>Un Node[]
     *      <li>Un List di Node
     *      <li>Un Set di Node
     *  </ul>
     */
    public Object evaluate(Node contextNode, Object[] params) throws TransformerException;
}

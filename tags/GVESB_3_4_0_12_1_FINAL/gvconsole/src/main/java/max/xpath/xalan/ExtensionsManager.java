/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:55 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xpath/xalan/ExtensionsManager.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Id: ExtensionsManager.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xpath.xalan;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import max.xpath.XPathFunction;

import org.apache.xpath.ExtensionsProvider;
import org.apache.xpath.NodeSet;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Node;

/**
 * Gestisce tutte le operazioni per la gestione delle estensioni all'XPath. E'
 * implementato come singleton.
 *
 */
class ExtensionsManager implements ExtensionsProvider {
    // --------------------------------------------------------------------------
    // FIELDS
    // --------------------------------------------------------------------------

    /**
     * Unica istanza esistente.
     */
    private static ExtensionsManager _instance          = null;

    /**
     * Funzioni installate. <br/>
     * Map[String namespace, Map[String name, XPathFunciton]]
     */
    private Map                      installedFunctions = new HashMap();

    /**
     * Memorizza per ogni thread il nodo di contesto da dove � iniziata la
     * valutazione del XPath. La classe XPathAPI si preoccupa di
     * inserire/eliminare il record per il thread quando la valutazione inizia e
     * termina. <br/>
     * Map[Thread, Stack[Node]]
     */
    private Map                      contextNodes       = new HashMap();

    // --------------------------------------------------------------------------
    // SINGLETON
    // --------------------------------------------------------------------------

    /**
     * Ottiene l'unica instanza esistente.
     */
    static synchronized ExtensionsManager instance() {
        if (_instance == null) {
            _instance = new ExtensionsManager();
        }
        return _instance;
    }

    /**
     * Costruttore privato.
     */
    private ExtensionsManager() {
    }

    // --------------------------------------------------------------------------
    // METHODS
    // --------------------------------------------------------------------------

    /**
     * Registra una funzione per estendere l'XPath.
     *
     * @param name
     *            nome della funzione.
     * @param function
     *            implementazione della funzione.
     */
    synchronized void installFunction(String namespace, String name, XPathFunction function) {
        Map functions = (Map) installedFunctions.get(namespace);
        if (functions == null) {
            functions = new HashMap();
            installedFunctions.put(namespace, functions);
        }
        functions.put(name, function);
    }

    /**
     * Associa al Thread corrente il nodo di contesto, per recuperarlo al
     * momento dell'esecuzione delle funzioni estese.
     *
     * @param contextNode
     *            nodo di contesto.
     */
    synchronized void startEvaluation(Node contextNode) {
        Thread thread = Thread.currentThread();
        Stack stack = (Stack) contextNodes.get(thread);
        if (stack == null) {
            stack = new Stack();
            contextNodes.put(thread, stack);
        }
        stack.push(contextNode);
    }

    /**
     * Elimina l'associazione tra il Thread ed il nodo di contesto.
     */
    synchronized void endEvaluation() {
        Thread thread = Thread.currentThread();
        Stack stack = (Stack) contextNodes.get(thread);
        if (stack != null) {
            stack.pop();
            if (stack.empty()) {
                contextNodes.remove(thread);
            }
        }
    }

    /**
     * Ottiene il nodo di contesto associato al thread corrente.
     *
     * @return il nodo di contesto associato al thread corremte.
     */
    private synchronized Node getContextNode() {
        Thread thread = Thread.currentThread();
        Stack stack = (Stack) contextNodes.get(thread);
        if (stack == null) {
            return null;
        }
        return (Node) stack.peek();
    }

    // --------------------------------------------------------------------------
    // ExtensionsProvider
    // --------------------------------------------------------------------------

    public boolean elementAvailable(String ns, String elemName) {
        return false;
    }

    public Object extFunction(String ns, String funcName, Vector argVec, Object methodKey) throws TransformerException {
        Map functions = (Map) installedFunctions.get(ns);
        if (functions == null) {
            throw new TransformerException("Invalid namespace: " + ns);
        }

        // Preleva la funzione
        //
        XPathFunction function = (XPathFunction) functions.get(funcName);
        if (function == null) {
            throw new TransformerException("Invalid function: " + funcName + " for namespace " + ns);
        }

        // Prepara i parametri convertendoli opportunamente in oggetti Java
        //
        Object[] params = new Object[argVec.size()];
        for (int i = 0; i < params.length; ++i) {
            XObject xobject = (XObject) argVec.elementAt(i);
            switch (xobject.getType()) {

            case XObject.CLASS_STRING:
                params[i] = xobject.str();
                break;

            case XObject.CLASS_NODESET:
                params[i] = xobject.nodelist();
                break;

            case XObject.CLASS_NUMBER:
                params[i] = new Double(xobject.num());
                break;

            case XObject.CLASS_BOOLEAN:
                params[i] = new Boolean(xobject.bool());
                break;

            case XObject.CLASS_NULL:
                params[i] = null;
                break;

            case XObject.CLASS_RTREEFRAG:
                params[i] = xobject.rtree();
                break;

            default:
                params[i] = xobject.object();
                break;
            }
        }

        // Invoca la funzione
        //
        Node contextNode = getContextNode();
        Object object = function.evaluate(contextNode, params);

        if (object == null) {
            return null;
        }

        // Prepara i NodeSet nel caso di collezioni di Nodi
        //
        if (object instanceof List) {
            NodeSet nodeSet = new NodeSet();
            List list = (List) object;
            Iterator i = list.iterator();
            while (i.hasNext()) {
                nodeSet.addNode((Node) i.next());
            }
            return nodeSet;
        }
        else if (object instanceof Set) {
            NodeSet nodeSet = new NodeSet();
            Set set = (Set) object;
            Iterator i = set.iterator();
            while (i.hasNext()) {
                nodeSet.addNode((Node) i.next());
            }
            return nodeSet;
        }
        else {
            Class cls = object.getClass();
            if (cls.isArray()) {
                NodeSet nodeSet = new NodeSet();
                Node[] nodes = (Node[]) object;
                for (int i = 0; i < nodes.length; ++i) {
                    nodeSet.addNode(nodes[i]);
                }
                return nodeSet;
            }
        }

        return object;
    }

    public Object extFunction(FuncExtFunction function, Vector params) throws TransformerException {
        return extFunction(function.getNamespace(), function.getFunctionName(), params, null);
    }

    public boolean functionAvailable(String ns, String funcName) {
        return installedFunctions.containsKey(funcName);
    }
}

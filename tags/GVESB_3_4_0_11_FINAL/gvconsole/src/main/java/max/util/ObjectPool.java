/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-19 13:06:37 $ $Header:
 * /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS
 * /core/webapps/gvconsole/src/main/java/max/util/ObjectPool.java,v 1.1
 * 2010-04-03 15:28:55 nlariviera Exp $ $Id: ObjectPool.java,v 1.1 2010-04-03
 * 15:28:55 nlariviera Exp $ $Name:  $ $Locker:  $ $Revision: 1.2 $ $State: Exp $
 */
package max.util;

import java.util.Hashtable;
import java.util.Stack;

/**
 * Gestisce un pool di oggetti.
 */
public class ObjectPool
{
    /**
	 *
	 */
    protected Hashtable<Object, Hashtable> usedObjects;
    /**
	 *
	 */
    protected Stack<Object>                availableObjects;
    /**
	 *
	 */
    protected Object                       waiting;
    /**
	 *
	 */
    protected Class<?>                     cls;
    /**
	 *
	 */
    protected String                       argument;
    /**
	 *
	 */
    protected int                          maxObjects;
    /**
	 *
	 */
    protected long                         timeout;
    /**
	 *
	 */
    protected ObjectPoolCreator            objectCreator;

    /**
     * Costruisce un <code>ObjectPool</code>.
     *
     * @param creator
     *        questo oggetto � invocato quando il pool deve costruire altri
     *        oggetti.
     * @param arg
     *        questo argomento � passato all'oggetto <code>creator</code> al
     *        momento della creazione.
     * @param maxObjs
     *        non pi� di <code>maxObjs</code> oggetti saranno creati da questo
     *        <code>ObjectPool</code>.
     * @param tmout
     *        timeout per l'attesa della disponibilit� di un oggetto. Se 0
     *        l'attesa � indefinita.
     */
    public ObjectPool(ObjectPoolCreator creator, String arg, int maxObjs, long tmout)
    {
        usedObjects = new Hashtable<Object, Hashtable>();
        availableObjects = new Stack<Object>();

        waiting = new Object();

        objectCreator = creator;
        argument = arg;
        maxObjects = maxObjs;
        timeout = tmout;
    }


    /**
     * Restituisce il numero di oggetti correntemente disponibili.
     *
     * @return
     */
    public int getAvailableCount()
    {
        return availableObjects.size();
    }


    /**
     * Restituisce il numero di oggetti correntemente assegnati.
     *
     * @return
     */
    public int getAssignedCount()
    {
        return usedObjects.size();
    }


    /**
     * Ottiene un oggetto.
     * <p>
     * Se non sono disponibili oggetti e il numero massimo di oggetti da creare
     * non � stato raggiunto, ne crea uno nuovo utilizzando l'
     * <code>ObjectPoolCreator</code> associato.
     * <p>
     * Se non sono disponibili oggetti e il numero massimo di oggetti da creare
     * � stato raggiunto, il thread � messo in attesa per un tempo non superiore
     * al timeout. Se dopo il timeout l'oggetto non � ancora disponibile �
     * sollevata un'eccezione <code>ObjectPoolException</code>.
     *
     * @return
     *
     * @exception ObjectPoolException
     *            non � riuscito ad ottenere un oggetto per il thread chiamante
     *            entro il timeout impostato.
     */
    public Object getObject() throws ObjectPoolException
    {
        Object obj = null;
        boolean first = true;
        while (obj == null) {
            synchronized (waiting) {
                if (!availableObjects.empty()) {
                    obj = availableObjects.pop();
                }
                else if (usedObjects.size() == maxObjects) {
                    try {
                        if (!first) {
                            throw new ObjectPoolException("No objects available: " + cls.getName());
                        }
                        waiting.wait(timeout);
                        first = false;
                    }
                    catch (InterruptedException exc) {
                        throw new ObjectPoolException("No object available: " + cls.getName());
                    }
                }
                else {
                    obj = objectCreator.create(argument);
                }

                if (obj != null) {
                    usedObjects.put(obj, usedObjects);
                }
            }
        }
        return obj;
    }


    /**
     * Rilascia un oggetto precedentemente assegnato.
     *
     * @param obj
     */
    public void releaseObject(Object obj)
    {
        if (obj == null)
            return;

        synchronized (waiting) {
            Object o = usedObjects.get(obj);
            if (o == null)
                return;

            availableObjects.push(obj);
            usedObjects.remove(obj);
            waiting.notify();
        }
    }


    /**
     * Rimuove tutti gli oggetti.
     */
    public void resetPool()
    {
        synchronized (waiting) {
            usedObjects = new Hashtable<Object, Hashtable>();
            availableObjects = new Stack<Object>();
        }
    }
}

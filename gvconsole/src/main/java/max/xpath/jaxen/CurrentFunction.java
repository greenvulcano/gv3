/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:48 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xpath/jaxen/CurrentFunction.java,v 1.1 2010-04-03 15:28:48 nlariviera Exp $
 * $Id: CurrentFunction.java,v 1.1 2010-04-03 15:28:48 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xpath.jaxen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

/**
 * This class implements the current() XPath function. This function is not provided by Jaxen
 * itself.
 * The starting context is put into a Map and associate with the Thread executing the XPath.
 *
 */
public class CurrentFunction implements Function
{
    //--------------------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------------------

    /**
     * Map[Thread, Stack[Object]]
     */
    private static Map<Thread, Stack<Object>> currentMap = new HashMap<Thread, Stack<Object>>();

    //--------------------------------------------------------------------------------------
    // METHODS
    //--------------------------------------------------------------------------------------

    /**
     * Associate to the current Thread the context object.
     */
    public static synchronized void putCurrent(Object object)
    {
        //currentMap.put(Thread.currentThread(), object);
        Thread thread = Thread.currentThread();
        Stack stack = (Stack)currentMap.get(thread);
        if(stack == null) {
            stack = new Stack();
            currentMap.put(thread, stack);
        }
        stack.push(object);
    }

    /**
     * @return the context object for the current Thread.
     */
    public static synchronized Object getCurrent()
    {
        //return currentMap.get(Thread.currentThread());
        Thread thread = Thread.currentThread();
        Stack stack = (Stack)currentMap.get(thread);
        if(stack == null) {
            return null;
        }
        return stack.peek();
    }

    /**
     * Dissociates the current Thread from the context object.
     */
    public static synchronized void removeCurrent()
    {
        Thread thread = Thread.currentThread();
        Stack<Object> stack = currentMap.get(thread);
        if (stack != null) {
            stack.pop();
            if (stack.empty()) {
                currentMap.remove(thread);
            }
        }
    }

    //--------------------------------------------------------------------------------------
    // CALLBACK
    //--------------------------------------------------------------------------------------

    /**
     * Called by Jaxen when the current() function is invoked.
     */
    public Object call(Context context, List args) throws FunctionCallException
    {
        return getCurrent();
    }
}

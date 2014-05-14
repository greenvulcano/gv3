/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.xpath.jaxen;

import org.jaxen.SimpleNamespaceContext;

/**
 * A dummy namespace context.
 * Always return the default namespace.
 *
 */
public class NamespaceContext extends SimpleNamespaceContext
{
    //--------------------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------------------

    private static NamespaceContext _instance;

    //--------------------------------------------------------------------------------------
    // METHODS - Singleton
    //--------------------------------------------------------------------------------------

    public static synchronized NamespaceContext instance()
    {
        if (_instance == null) {
            _instance = new NamespaceContext();
        }
        return _instance;
    }

    private NamespaceContext()
    {
    }
}
/*
 * Creation date and time: 14-ott-2005 17.50.13
 *
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xpath/xalan/PrefixResolver.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 */
package max.xpath.xalan;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * @author Sergio
 *
 * <code>$Id: PrefixResolver.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $</code>
 */
public class PrefixResolver implements org.apache.xml.utils.PrefixResolver
{
    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    private static PrefixResolver _instance;

    private Map                   namespaces = new HashMap();

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    //----------------------------------------------------------------------------------------------

    private PrefixResolver()
    {
    }

    public static PrefixResolver instance()
    {
        if (_instance == null) {
            _instance = new PrefixResolver();
        }
        return _instance;
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.apache.xml.utils.PrefixResolver#handlesNullPrefixes()
     */
    public boolean handlesNullPrefixes()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.xml.utils.PrefixResolver#getBaseIdentifier()
     */
    public String getBaseIdentifier()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.xml.utils.PrefixResolver#getNamespaceForPrefix(java.lang.String)
     */
    public String getNamespaceForPrefix(String prefix)
    {
        String namespace = (String) namespaces.get(prefix);
        if (namespace == null) {
            return "";
        }
        return namespace;
    }

    /* (non-Javadoc)
     * @see org.apache.xml.utils.PrefixResolver#getNamespaceForPrefix(java.lang.String, org.w3c.dom.Node)
     */
    public String getNamespaceForPrefix(String prefix, Node context)
    {
        return getNamespaceForPrefix(prefix);
    }

    public void installNamespace(String prefix, String namespace)
    {
        namespaces.put(prefix, namespace);
    }
}

/*
 * Creation date and time: 3-nov-2005 19.19.18
 *
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/Pair.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 */
package max.xml;

/**
 * @author Sergio
 *
 * <code>$Id: Pair.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $</code>
 */
public class Pair
{
    //----------------------------------------------------------------------------------------------
    // FIELDS
    //----------------------------------------------------------------------------------------------

    private Object first;
    private Object second;

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    //----------------------------------------------------------------------------------------------

    /**
     * @param first
     * @param second
     */
    public Pair(Object first, Object second)
    {
        this.first = first;
        this.second = second;
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------

    /**
     * @return Returns the first.
     */
    public Object getFirst()
    {
        return first;
    }

    /**
     * @return Returns the second.
     */
    public Object getSecond()
    {
        return second;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        int h1 = first == null ? 0 : first.hashCode();
        int h2 = second == null ? 0 : second.hashCode();
        return h1 ^ h2;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Pair)) {
            return false;
        }

        Pair other = (Pair) obj;
        boolean eq1 = first == null ? other.first == null : first.equals(other.first);
        if (!eq1) {
            return false;
        }
        return second == null ? other.second == null : second.equals(other.second);
    }
}

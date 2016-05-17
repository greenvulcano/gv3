/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:49 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/Check.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Id: Check.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import org.w3c.dom.*;

public interface Check
{
    /**
     * Return a list of warnings for the given element.
     *
     * @return <code>null</code> if there are not warnings.
     */
    public Warning[] getWarning(Element element);

    /**
     * Restituisce una lista di XPath coinvolti nel check.
     * Questi XPath saranno utilizzati per la determinazione delle dipendenze.
     *
     * @return
     */
    public String[] getXPaths();

    /**
     * Nome dell'elemento a cui si applica questo check.
     *
     * @return
     */
    public String getElementName();
}

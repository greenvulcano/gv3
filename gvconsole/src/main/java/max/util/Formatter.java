/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:54 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/util/Formatter.java,v 1.1 2010-04-03 15:28:54 nlariviera Exp $
 * $Id: Formatter.java,v 1.1 2010-04-03 15:28:54 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.util;

import max.core.*;

public interface Formatter
{
	public void init(String name) throws MaxException;
	public String format(Object args[]) throws MaxException;
}

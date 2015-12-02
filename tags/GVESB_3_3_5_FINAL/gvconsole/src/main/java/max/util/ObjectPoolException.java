/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:55 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/util/ObjectPoolException.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Id: ObjectPoolException.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.util;

public class ObjectPoolException extends Exception
{
    /**
     *
     */
    private static final long serialVersionUID = 1024763390448625848L;
    private Exception nested;

	public ObjectPoolException(String msg)
	{
		super(msg);
	}

	public ObjectPoolException(String msg, Exception cause)
	{
	    super(msg);
	    nested = cause;
	}

	public ObjectPoolException(Exception cause)
	{
	    nested = cause;
	}

	public Exception getNestedException()
	{
	    return nested;
	}
}

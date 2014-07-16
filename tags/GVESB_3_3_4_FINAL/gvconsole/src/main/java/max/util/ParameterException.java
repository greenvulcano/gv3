/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:55 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/util/ParameterException.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Id: ParameterException.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.util;

/**
 * Eccezione lanciata dalla classe <code>Parameter</code> se si genera un'eccezione
 * durante la lettura dei parametri da una <code>HttpServletRequest</code>.
 */
public class ParameterException extends Exception
{
	/**
     *
     */
    private static final long serialVersionUID = -6802157307433468606L;

    /**
	 * Costruisce un <code>ParameterException</code> e vi associa un messaggio.
	 */
	public ParameterException(String msg)
	{
		super(msg);
	}
}

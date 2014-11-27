/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:55 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/util/FormatManager.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Id: FormatManager.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.util;

import java.util.*;

import max.config.*;
import max.core.*;


public class FormatManager
{
	private static FormatManager _instance = null;

	public static synchronized FormatManager instance()
	{
		try {
			if(_instance == null) {
				_instance = new FormatManager();
			}
			return _instance;
		}
		catch(Exception exc) {
			System.out.println("FormatManager: " + exc);
			return null;
		}
	}

	///////////////////////////////////////////////////////////////////////

	private FormatManager()
	{
	}

	///////////////////////////////////////////////////////////////////////


	private Hashtable formatters = new Hashtable();

	public Formatter get(String name) throws MaxException
	{
		Formatter fmtr = (Formatter)formatters.get(name);
		if(fmtr != null) return fmtr;

		String fmtrClsName = Config.get(name, "max.class");

		try {
			Class fmtrCls = Class.forName(fmtrClsName);
			fmtr = (Formatter)fmtrCls.newInstance();
			fmtr.init(name);

			formatters.put(name, fmtr);
			return fmtr;
		}
		catch(Exception exc) {
			System.out.println("FormatManager.get(): " + exc);
			return null;
		}
	}

	public void remove(String name)
	{
		formatters.remove(name);
	}
}

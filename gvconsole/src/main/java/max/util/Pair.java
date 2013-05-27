/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:55 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/util/Pair.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Id: Pair.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.util;

public class Pair
{
	public Object a;
	public Object b;
	
	public Pair(Object a, Object b)
	{
		this.a = a;
		this.b = b;
	}
	
	public int hashCode()
	{
		int hca = (a == null ? 0 : a.hashCode());
		int hcb = (b == null ? 0 : b.hashCode());
		return hca + hcb;
	}
	
	public boolean equals(Object o)
	{
		if(o == null) return false;
		if(!(o instanceof Pair)) return false;
		if(o == this) return true;
		
		Pair p = (Pair)o;
		return eq(p.a, a) && eq(p.b, b);
	}
	
	private boolean eq(Object a, Object b)
	{
		if(a == null) return b == null;
		return a.equals(b);
	}
}

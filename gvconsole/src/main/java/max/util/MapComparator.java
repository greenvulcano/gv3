/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:55 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/util/MapComparator.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Id: MapComparator.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.util;

import java.util.*;
import java.text.*;

public class MapComparator implements Comparator
{
	private Collator collator = Collator.getInstance();

	private Object keys[];
	private int mode[];


	public MapComparator(Object keys[], int mode[])
	{
		this.keys = keys;
		this.mode = mode;
	}


	public int compare(Object o1, Object o2)
	{
		if(o1 == o2) return 0;
		if(o1 == null) {
			return -1;
		}
		if(o2 == null) return 1;

		if((o1 instanceof Map) && (o2 instanceof Map))
			return compareMaps((Map)o1, (Map)o2);

		return compareObjects(o1, o2);
	}

	private int compareObjects(Object o1, Object o2)
	{
		if(o1 instanceof Number) {
			if(o2 instanceof Number) {
				double d1 = ((Number)o1).doubleValue();
				double d2 = ((Number)o2).doubleValue();
				return d1 < d2 ? -1
				     : d1 > d2 ? 1
				     : 0;
			}
			if(o2 instanceof String) {
				try {
					Double d = new Double((String)o2);
					return compareObjects(o1, d);
				}
				catch(Exception exc) {
					return -1;
				}
			}
			return -1;
		}

		if(o2 instanceof Number) return -compareObjects(o2, o1);

		if((o1 instanceof Date) && (o2 instanceof Date)) {
			return ((Date)o1).compareTo((Date)o2);
		}

        CollationKey key1 = collator.getCollationKey(o1.toString());
        CollationKey key2 = collator.getCollationKey(o2.toString());
        return key1.compareTo(key2);
	}


	private int compareMaps(Map m1, Map m2)
	{
		if(keys == null) return 0;
		for(int i = 0; i < keys.length; ++i) {
			Object v1 = m1.get(keys[i]);
			Object v2 = m2.get(keys[i]);
			int r = 0;
			if(v1 == null) {
				if(v2 != null) r = -1;
			}
			else if(v2 == null) r = 1;
			else if(v1 != v2) r = compareObjects(v1, v2);
			if(r != 0) return r * mode[i];
		}
		return 0;
	}
}

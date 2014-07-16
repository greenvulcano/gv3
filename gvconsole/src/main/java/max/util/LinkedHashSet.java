/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:55 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/util/LinkedHashSet.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Id: LinkedHashSet.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.util;

import java.util.*;

/**
 * Non utilizzare questa classe con Java 1.4.
 * Workaround per JDK1.3.
 */

public class LinkedHashSet implements Set
{
   private LinkedList list;
   private HashSet set;

   public LinkedHashSet()
   {
      list = new LinkedList();
      set = new HashSet();
   }

   public LinkedHashSet(Collection c)
   {
      this();
      addAll(c);
   }

   public LinkedHashSet(int initialCapacity)
   {
      list = new LinkedList();
      set = new HashSet(initialCapacity);
   }

   public LinkedHashSet(int initialCapacity, float loadFactor)
   {
      list = new LinkedList();
      set = new HashSet(initialCapacity, loadFactor);
   }

   public boolean add(Object o)
   {
      if(set.contains(o)) return false;
      set.add(o);
      list.add(o);
      return true;
   }

   public boolean addAll(Collection c)
   {
      Iterator i = c.iterator();
      boolean ret = false;
      while(i.hasNext()) {
         Object o = i.next();
         ret |= add(o);
      }
      return ret;
   }

   public void clear()
   {
      set.clear();
      list.clear();
   }

   public boolean contains(Object o)
   {
      return set.contains(o);
   }

   public boolean containsAll(Collection c)
   {
      Iterator i = c.iterator();
      while(i.hasNext()) {
         Object o = i.next();
         if(!set.contains(o)) return false;
      }
      return true;
   }

   public boolean equals(Object o)
   {
      return set.equals(o);
   }

   public int hashCode()
   {
      return set.hashCode();
   }

   public boolean isEmpty()
   {
      return set.isEmpty();
   }

   public Iterator iterator()
   {
      return list.listIterator();
   }

   public boolean remove(Object o)
   {
      if(set.contains(o)) {
         set.remove(o);
         list.remove(o);
         return true;
      }
      return false;
   }

   public boolean removeAll(Collection c)
   {
      Iterator i = c.iterator();
      boolean ret = false;
      while(i.hasNext()) {
         Object o = i.next();
         ret |= remove(o);
      }
      return ret;
   }

   public boolean retainAll(Collection c)
   {
      Object a[] = list.toArray();
      if(a == null) return false;

      boolean ret = false;
      for(int i = 0; i < a.length; ++i) {
         if(!c.contains(a[i])) {
            ret = true;
            remove(a[i]);
         }
      }
      return ret;
   }

   public int size()
   {
      return set.size();
   }

   public Object[] toArray()
   {
      return list.toArray();
   }

   public Object[] toArray(Object[] a)
   {
      return list.toArray(a);
   }
}

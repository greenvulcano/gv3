/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:55 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/util/LinkedHashMap.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Id: LinkedHashMap.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Non utilizzare questa classe con Java 1.4.
 * Workaround per JDK1.3.
 */

public class LinkedHashMap implements Map
{
   private HashMap<Object, Object> map;
   private LinkedList<Object> list;

   public LinkedHashMap()
   {
      map = new HashMap<Object, Object>();
      list = new LinkedList<Object>();
   }

   public LinkedHashMap(int initialCapacity)
   {
      map = new HashMap<Object, Object>(initialCapacity);
      list = new LinkedList<Object>();
   }

   public LinkedHashMap(int initialCapacity, float loadFactor)
   {
      map = new HashMap<Object, Object>(initialCapacity, loadFactor);
      list = new LinkedList<Object>();
   }

   public LinkedHashMap(Map m)
   {
      this();
      putAll(m);
   }


   public void clear()
   {
      map.clear();
      list.clear();
   }

   public boolean containsKey(Object key)
   {
      return map.containsKey(key);
   }

   public boolean containsValue(Object value)
   {
      return map.containsValue(value);
   }

   class MapEntry implements Map.Entry
   {
      Object key;
      Object value;

      MapEntry(Object k, Object v)
      {
         key = k;
         value = v;
      }

      public boolean equals(Object o)
      {
         if(o == null) return false;
         if(!(o instanceof Map.Entry)) return false;

         Map.Entry e = (Map.Entry)o;

         return
            (e.getKey()==null ?
            key==null : e.getKey().equals(key))  &&
            (e.getValue()==null ?
            value==null : e.getValue().equals(value));
      }

      public Object getKey()
      {
         return key;
      }

      public Object getValue()
      {
         return value;
      }

      public int hashCode()
      {
         return
            (key==null   ? 0 : key.hashCode()) ^
            (value==null ? 0 : value.hashCode());
      }

      public Object setValue(Object value)
      {
         Object ret = this.value;
         this.value = value;
         return ret;
      }
   }


   class ListSet extends AbstractSet<Object>
   {
      List<Object> l;

      ListSet(List<Object> l)
      {
         this.l = l;
      }

      public int size()
      {
         return l.size();
      }

      public Iterator<Object> iterator()
      {
         return l.listIterator();
      }
   }

   public Set<?> entrySet()
   {
      List<Object> entries = new LinkedList<Object>();
      Iterator<Object> i = list.listIterator();
      while(i.hasNext()) {
         Object key = i.next();
         Object value = map.get(key);
         entries.add(new MapEntry(key, value));
      }
      return new ListSet(entries);
   }

   public boolean equals(Object o)
   {
      return map.equals(o);
   }

   public Object get(Object key)
   {
      return map.get(key);
   }

   public int hashCode()
   {
      return map.hashCode();
   }

   public boolean isEmpty()
   {
      return map.isEmpty();
   }

   public Set<?> keySet()
   {
      return new ListSet(list);
   }

   public Object put(Object key, Object value)
   {
      if(!map.containsKey(key)) {
         list.add(key);
         return map.put(key, value);
      }
      else {
         return map.put(key, value);
      }
   }

   public void putAll(Map t)
   {
      Set<?> set = t.entrySet();
      Iterator<?> i = set.iterator();
      while(i.hasNext()) {
         Map.Entry entry = (Map.Entry)i.next();
         put(entry.getKey(), entry.getValue());
      }
   }

   public Object remove(Object key)
   {
      list.remove(key);
      return map.remove(key);
   }

   public int size()
   {
      return map.size();
   }

   public Collection<Object> values()
   {
      Collection<Object> res = new Vector<Object>();
      Iterator<Object> i = list.listIterator();
      while(i.hasNext()) {
         Object k = i.next();
         res.add(map.get(k));
      }
      return res;
   }
}

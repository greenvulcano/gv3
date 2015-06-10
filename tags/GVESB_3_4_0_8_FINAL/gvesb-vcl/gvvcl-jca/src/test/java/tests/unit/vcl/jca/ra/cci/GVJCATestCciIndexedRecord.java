/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package tests.unit.vcl.jca.ra.cci;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import javax.resource.cci.IndexedRecord;

/**
 * @version 3.0.0 Mar 23, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class GVJCATestCciIndexedRecord implements IndexedRecord
{
    private static final long serialVersionUID = 210L;
    private String            recordName;
    private String            description;
    private Vector<Object>    indexedRecord;

    /**
     *
     */
    public GVJCATestCciIndexedRecord()
    {
        indexedRecord = new Vector<Object>();
    }

    /**
     * @param name
     */
    public GVJCATestCciIndexedRecord(String name)
    {
        indexedRecord = new Vector<Object>();
        recordName = name;
    }

    /**
     * @see javax.resource.cci.Record#getRecordName()
     */
    @Override
    public String getRecordName()
    {
        return recordName;
    }

    /**
     * @see javax.resource.cci.Record#getRecordShortDescription()
     */
    @Override
    public String getRecordShortDescription()
    {
        return description;
    }

    /**
     * @see javax.resource.cci.Record#setRecordName(java.lang.String)
     */
    @Override
    public void setRecordName(String s)
    {
        recordName = s;
    }

    /**
     * @see javax.resource.cci.Record#setRecordShortDescription(java.lang.String)
     */
    @Override
    public void setRecordShortDescription(String s)
    {
        description = s;
    }

    /**
     * @see java.util.List#add(java.lang.Object)
     */
    @Override
    public boolean add(Object e)
    {
        return indexedRecord.add(e);
    }

    /**
     * @see java.util.List#add(int, java.lang.Object)
     */
    @Override
    public void add(int index, Object element)
    {
        indexedRecord.add(index, element);
    }

    /**
     * @see java.util.List#addAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(Collection c)
    {
        return indexedRecord.addAll(c);
    }

    /**
     * @see java.util.List#addAll(int, java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(int index, Collection c)
    {
        return indexedRecord.addAll(index, c);
    }

    /**
     * @see java.util.List#clear()
     */
    @Override
    public void clear()
    {
        indexedRecord.clear();
    }

    /**
     * @see java.util.List#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object o)
    {
        return indexedRecord.contains(o);
    }

    /**
     * @see java.util.List#containsAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean containsAll(Collection c)
    {
        return indexedRecord.containsAll(c);
    }

    /**
     * @see java.util.List#get(int)
     */
    @Override
    public Object get(int index)
    {
        return indexedRecord.get(index);
    }

    /**
     * @see java.util.List#indexOf(java.lang.Object)
     */
    @Override
    public int indexOf(Object o)
    {
        return indexedRecord.indexOf(o);
    }

    /**
     * @see java.util.List#isEmpty()
     */
    @Override
    public boolean isEmpty()
    {
        return indexedRecord.isEmpty();
    }

    /**
     * @see java.util.List#iterator()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterator iterator()
    {
        return indexedRecord.iterator();
    }

    /**
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    @Override
    public int lastIndexOf(Object o)
    {
        return indexedRecord.lastIndexOf(o);
    }

    /**
     * @see java.util.List#listIterator()
     */
    @SuppressWarnings("unchecked")
    @Override
    public ListIterator listIterator()
    {
        return indexedRecord.listIterator();
    }

    /**
     * @see java.util.List#listIterator(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public ListIterator listIterator(int index)
    {
        return indexedRecord.listIterator(index);
    }

    /**
     * @see java.util.List#remove(java.lang.Object)
     */
    @Override
    public boolean remove(Object o)
    {
        return indexedRecord.remove(o);
    }

    /**
     * @see java.util.List#remove(int)
     */
    @Override
    public Object remove(int index)
    {
        return indexedRecord.remove(index);
    }

    /**
     * @see java.util.List#removeAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean removeAll(Collection c)
    {
        return indexedRecord.removeAll(c);
    }

    /**
     * @see java.util.List#retainAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean retainAll(Collection c)
    {
        return indexedRecord.retainAll(c);
    }

    /**
     * @see java.util.List#set(int, java.lang.Object)
     */
    @Override
    public Object set(int index, Object element)
    {
        return indexedRecord.set(index, element);
    }

    /**
     * @see java.util.List#size()
     */
    @Override
    public int size()
    {
        return indexedRecord.size();
    }

    /**
     * @see java.util.List#subList(int, int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List subList(int fromIndex, int toIndex)
    {
        return indexedRecord.subList(fromIndex, toIndex);
    }

    /**
     * @see java.util.List#toArray()
     */
    @Override
    public Object[] toArray()
    {
        return indexedRecord.toArray();
    }

    /**
     * @see java.util.List#toArray(Object[])
     */
    @Override
    public Object[] toArray(Object[] a)
    {
        return indexedRecord.toArray(a);
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        return this.clone();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return ("" + recordName).hashCode();
    }
}

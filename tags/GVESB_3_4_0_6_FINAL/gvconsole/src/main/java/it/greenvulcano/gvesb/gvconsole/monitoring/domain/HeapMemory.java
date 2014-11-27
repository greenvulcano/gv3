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
package it.greenvulcano.gvesb.gvconsole.monitoring.domain;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author Angelo
 *
 */
public class HeapMemory
{
    private long heapMemoryInit;
    private long heapMemoryCommitted;
    private long heapMemoryUsed;
    private long heapMemoryMax;

    /**
     * @return the initial heap memory set
     */
    public long getHeapMemoryInit()
    {
        return heapMemoryInit;
    }

    /**
     * @param heapMemoryInit
     */
    public void setHeapMemoryInit(long heapMemoryInit)
    {
        this.heapMemoryInit = heapMemoryInit;
    }

    /**
     * @return the heap memory committed
     */
    public long getHeapMemoryCommitted()
    {
        return heapMemoryCommitted;
    }

    /**
     * @param heapMemoryCommitted
     */
    public void setHeapMemoryCommitted(long heapMemoryCommitted)
    {
        this.heapMemoryCommitted = heapMemoryCommitted;
    }

    /**
     * @return the heap memory used
     */
    public long getHeapMemoryUsed()
    {
        return heapMemoryUsed;
    }

    /**
     * @param heapMemoryUsed
     */
    public void setHeapMemoryUsed(long heapMemoryUsed)
    {
        this.heapMemoryUsed = heapMemoryUsed;
    }

    /**
     * @return the maximum heap memory set
     */
    public long getHeapMemoryMax()
    {
        return heapMemoryMax;
    }

    /**
     * @param heapMemoryMax
     */
    public void setHeapMemoryMax(long heapMemoryMax)
    {
        this.heapMemoryMax = heapMemoryMax;
    }

}

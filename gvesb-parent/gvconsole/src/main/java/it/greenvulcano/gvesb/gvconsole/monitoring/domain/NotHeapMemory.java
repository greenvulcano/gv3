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
public class NotHeapMemory
{
    private long nonHeapMemoryInit;
    private long nonHeapMemoryCommitted;
    private long nonHeapMemoryUsed;
    private long nonHeapMemoryMax;

    /**
     * @return the initial non heap memory set
     */
    public long getHeapMemoryInit()
    {
        return nonHeapMemoryInit;
    }

    /**
     * @param nonHeapMemoryInit
     */
    public void setHeapMemoryInit(long nonHeapMemoryInit)
    {
        this.nonHeapMemoryInit = nonHeapMemoryInit;
    }

    /**
     * @return the non heap memory committed
     */
    public long getHeapMemoryCommitted()
    {
        return nonHeapMemoryCommitted;
    }

    /**
     * @param nonHeapMemoryCommitted
     */
    public void setHeapMemoryCommitted(long nonHeapMemoryCommitted)
    {
        this.nonHeapMemoryCommitted = nonHeapMemoryCommitted;
    }

    /**
     * @return the non heap memory used
     */
    public long getHeapMemoryUsed()
    {
        return nonHeapMemoryUsed;
    }

    /**
     * @param nonHeapMemoryUsed
     */
    public void setHeapMemoryUsed(long nonHeapMemoryUsed)
    {
        this.nonHeapMemoryUsed = nonHeapMemoryUsed;
    }

    /**
     * @return the maximum non heap memory set
     */
    public long getHeapMemoryMax()
    {
        return nonHeapMemoryMax;
    }

    /**
     * @param nonHeapMemoryMax
     */
    public void setHeapMemoryMax(long nonHeapMemoryMax)
    {
        this.nonHeapMemoryMax = nonHeapMemoryMax;
    }

}

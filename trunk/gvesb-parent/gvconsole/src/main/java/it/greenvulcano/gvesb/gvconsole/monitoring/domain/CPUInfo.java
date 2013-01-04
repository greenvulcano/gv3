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
public class CPUInfo
{

    private long       upTime;
    private long       processCpuTime;
    /**
     *
     */
    public static long prevUpTime         = 0L;
    /**
     *
     */
    public static long prevProcessCpuTime = 0L;
    private double     cpuUsage;

    /**
     * @return the upTime
     */
    public long getUpTime()
    {
        return upTime;
    }

    /**
     * @param upTime
     *        the upTime to set
     */
    public void setUpTime(long upTime)
    {
        this.upTime = upTime;
    }

    /**
     * @return the processCpuTime
     */
    public long getProcessCpuTime()
    {
        return processCpuTime;
    }

    /**
     * @param processCpuTime
     *        the processCpuTime to set
     */
    public void setProcessCpuTime(long processCpuTime)
    {
        this.processCpuTime = processCpuTime;
    }

    /**
     * @return the cpuUsage
     */
    public double getCpuUsage()
    {
        return cpuUsage;
    }

    /**
     * @param cpuUsage
     *        the cpuUsage to set
     */
    public void setCpuUsage(double cpuUsage)
    {
        this.cpuUsage = cpuUsage;
    }

}

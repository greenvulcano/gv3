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
package tests.unit.task;

import it.greenvulcano.jmx.JMXEntryPoint;

import java.util.Date;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.quartz.TriggerUtils;

/**
 * @version 3.2.0 09/11/2011
 * @author GreenVulcano Developer Team
 */
public class ServiceCallerTaskTestCase extends TestCase
{
    /**
     * @throws Exception
     */
    public void testServiceCaller() throws Exception
    {
        JMXEntryPoint jmx = JMXEntryPoint.instance();
        MBeanServer server = jmx.getServer();
        long timeout = (TriggerUtils.getEvenMinuteDate(new Date()).getTime() - System.currentTimeMillis()) + 20000;
        Thread.sleep(timeout);
        Set<ObjectName> set = server.queryNames(
                new ObjectName(
                        "GreenVulcano:Type=ServiceOperationInfo,Component=ServiceOperationInfo,IDService=TOUPPER,Internal=No,*"),
                null);
        assertTrue(
                "No Service/Operation info available in JMX object returned from GreenVulcano domain for service TOUPPER",
                (set != null) && !set.isEmpty());
        ObjectName svcInfo = set.iterator().next();
        assertEquals(4L, server.getAttribute(svcInfo, "totalHints"));
    }
}

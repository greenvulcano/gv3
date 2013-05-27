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
package it.greenvulcano.gvesb.gvconsole.monitoring.service;

import static java.lang.management.ManagementFactory.CLASS_LOADING_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.getClassLoadingMXBean;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;
import it.greenvulcano.gvesb.gvconsole.monitoring.domain.ClassInfo;

import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

/**
 * @version 3.0.0 Apr 12, 2010
 * @author Angelo
 *
 */
public class ClassLoadingServiceImpl implements ClassLoadingService
{
    private ClassLoadingMXBean mbean = null;

    /**
     *
     */
    public ClassLoadingServiceImpl()
    {
        mbean = getClassLoadingMXBean();
    }

    /**
     * @param mBeanServerConnection
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     * @throws MalformedObjectNameException
     * @throws ReflectionException
     * @throws NullPointerException
     * @throws IOException
     */
    public ClassLoadingServiceImpl(MBeanServerConnection mBeanServerConnection) throws Exception
    {
        mbean = (ClassLoadingMXBean) newPlatformMXBeanProxy(mBeanServerConnection, CLASS_LOADING_MXBEAN_NAME,
                ClassLoadingMXBean.class);
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.monitoring.service.ClassLoadingService#getClassInfo()
     */
    @Override
    public ClassInfo getClassInfo()
    {
        ClassInfo classInfo = new ClassInfo();
        // Returns the number of classes that are currently loaded in the Java
        // virtual machine.
        classInfo.setClassesLoad(mbean.getLoadedClassCount());

        // Returns the total number of classes that have been loaded since the
        // Java virtual machine has started execution.
        classInfo.setClassesTotal(mbean.getTotalLoadedClassCount());

        // Returns the total number of classes unloaded since the Java virtual
        // machine has started execution.
        classInfo.setClassesUnload(mbean.getUnloadedClassCount());
        return classInfo;
    }

}

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
package it.greenvulcano.gvesb.gvconsole.monitoring.action;

import it.greenvulcano.gvesb.gvconsole.monitoring.domain.ClassInfo;
import it.greenvulcano.gvesb.gvconsole.monitoring.service.ClassLoadingService;
import it.greenvulcano.gvesb.gvconsole.monitoring.service.ClassLoadingServiceImpl;

import javax.management.MBeanServerConnection;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;

/**
 * @version 3.0.0 Apr 12, 2010
 * @author Angelo
 *
 */
public class ClassAction extends GeneralAction
{

    private static final long     serialVersionUID = 1738011103001826026L;
    private static final Logger   logger           = Logger.getLogger(ClassAction.class);

    private MBeanServerConnection mBeanServerConnection;

    private ClassLoadingService   classLoadingService;

    public JSONArray execute(String server)
    {
        ClassInfo classInfo = null;
        try {
            mBeanServerConnection = getmBeanServerConnection(server);
            classLoadingService = new ClassLoadingServiceImpl(mBeanServerConnection);

            classInfo = classLoadingService.getClassInfo();

            return JSONArray.fromObject(classInfo);
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

}
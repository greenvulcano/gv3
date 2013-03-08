/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.scheduler.util.quartz.impl.trigger;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.scheduler.TaskException;
import it.greenvulcano.util.txt.DateUtils;

import java.util.Date;

import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 20/nov/2011
 * @author GreenVulcano Developer Team
 */
public class SimpleTriggerBuilder extends BaseTriggerBuilder
{
    private String startTime      = null;
    private String endTime        = null;
    private long   repeatInterval = 0;
    private int    repeatCount    = 0;

    /* (non-Javadoc)
     * @see it.greenvulcano.scheduler.util.quartz.impl.BaseTriggerBuilder#initTB(org.w3c.dom.Node)
     */
    @Override
    public void initTB(Node node) throws TaskException
    {
        try {
            startTime = XMLConfig.get(node, "@startTime", null);
            endTime = XMLConfig.get(node, "@endTime", null);
            repeatInterval = XMLConfig.getLong(node, "@repeatInterval", 0);
            repeatCount = XMLConfig.getInteger(node, "@repeatCount", 0);
            properties.put(TRIGGER_TYPE, "SimpleTrigger");
            properties.put(TRIGGER_NAME, name);
        }
        catch (Exception exc) {
            throw new TaskException("Error initializing SimpleTriggerBuilder", exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.scheduler.util.quartz.impl.BaseTriggerBuilder#createTrigger()
     */
    @Override
    protected Trigger createTrigger() throws TaskException
    {
        try {
            Date st = TriggerUtils.getEvenMinuteDate(java.util.Calendar.getInstance(timeZone).getTime());
            Date et = null;
            if (startTime != null) {
                st = DateUtils.stringToDate(startTime, DateUtils.FORMAT_ISO_DATETIME_L, timeZone.getID());
            }
            if (endTime != null) {
                et = DateUtils.stringToDate(endTime, DateUtils.FORMAT_ISO_DATETIME_L, timeZone.getID());
            }
            return new SimpleTrigger(name, group, taskName, group, st, et, repeatCount, repeatInterval);
        }
        catch (Exception exc) {
            throw new TaskException("Error creating SimpleTrigger[" + group + "." + name + "]", exc);
        }
    }

    @Override
    public String toString()
    {
        return "SimpleTriggerBuilder[" + group + "." + name + "]: startTime[" + startTime + "] - endTime[" + endTime
                + "] - repeatInterval[" + repeatInterval + "] - repeatCount[" + repeatCount + "] - properties["
                + properties + "]";
    }
}

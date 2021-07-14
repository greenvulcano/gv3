/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.greenvulcano.gvesb.statistics.plugin;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import it.greenvulcano.gvesb.statistics.GVStatisticsException;
import it.greenvulcano.gvesb.statistics.IStatisticsWriter;
import it.greenvulcano.gvesb.statistics.StatisticsData;
import it.greenvulcano.log.GVLogger;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author  GreenVulcano Developer Team
 *
 *
 */

public class LogFileStatisticsWriter implements IStatisticsWriter {
    private static Logger logger = GVLogger.getLogger(LogFileStatisticsWriter.class);

    /**
     *
     * @param node
     * @throws GVStatisticsException
     */
    @Override
    public void init(Node node) throws GVStatisticsException {
        logger.debug("LogFileStatisticsWriter init");
    }

    /**
     *
     * @param statisticsData
     *            StatisticsData object
     * @return boolean value
     * @throws GVStatisticsException
     */
    @Override
    public boolean writeStatisticsData(StatisticsData statisticsData) throws GVStatisticsException {
        logger.debug("BEGIN DUMMY StatisticsData");
        logger.debug(statisticsData);
        logger.debug("END DUMMY StatisticsData");
        return true;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.statistics.IStatisticsWriter#destroy()
     */
    @Override
    public void destroy()
    {
        logger.debug("LogFileStatisticsWriter");
    }
}

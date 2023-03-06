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
package it.greenvulcano.gvesb.datahandling.dbo;

import java.sql.Connection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.datahandling.DBOException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.TextUtils;

/**
 * IDBO Class specialized in reading data from a file.
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 */
public class DBOSelectFromFile extends AbstractDBO
{

    private String              fileName   = null;

    private boolean             readFromCP = true;

    private static final Logger logger     = GVLogger.getLogger(DBOSelectFromFile.class);

    /**
     *
     */
    public DBOSelectFromFile()
    {
        super();
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node config) throws DBOException
    {
        super.init(config);
        try {
            this.forcedMode = XMLConfig.get(config, "@force-mode", MODE_DB2XML);
            this.isReturnData = XMLConfig.getBoolean(config, "@return-data", true);
            this.fileName = XMLConfig.get(config, "@file-name");
            this.readFromCP = XMLConfig.getBoolean(config, "@read-from-cp", true);
        }
        catch (Exception exc) {
            logger.error("Error reading configuration of [" + getName() + "/" + this.dboclass + "]", exc);
            throw new DBOException("Error reading configuration of [" + getName() + "/" + this.dboclass + "]", exc);
        }
    }

    /**
     * Unsupported method for this IDBO.
     *
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#executeIn(java.lang.Object,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public void executeIn(Object input, Connection conn, Map<String, Object> props) throws DBOException
    {
        prepare();
        throw new DBOException("Unsupported method - DBOSelectFromFile::executeIn(Object, Connection, Map)");
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#executeOut(java.sql.Connection, java.util.Map)
     */
    @Override
    public Object executeOut(Connection conn, Map<String, Object> props) throws DBOException
    {
        try {
            prepare();
            this.rowCounter = 0;
            logger.debug("Begin execution of file [" + this.fileName + "] data read through " + this.dboclass);

            String fileData = null;
            if (this.readFromCP) {
                fileData = TextUtils.readFileFromCP(this.fileName);
            }
            else {
                fileData = TextUtils.readFile(this.fileName);
            }

            fileData = PropertiesHandler.expand(fileData, props, conn, null);

            logger.debug("End execution of file [" + this.fileName + "] data read through " + this.dboclass);
            this.dhr.setRead(this.rowCounter);

            return fileData;
        }
        catch (Exception exc) {
            logger.error("Error on execution of " + this.dboclass + " with name [" + getName() + "]", exc);
            throw new DBOException("Error on execution of " + this.dboclass + " with name [" + getName() + "]", exc);
        }
    }

}

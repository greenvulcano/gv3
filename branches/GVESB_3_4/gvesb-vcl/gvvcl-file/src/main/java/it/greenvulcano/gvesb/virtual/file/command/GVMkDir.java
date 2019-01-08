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
package it.greenvulcano.gvesb.virtual.file.command;

import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.file.FileManager;
import it.greenvulcano.util.metadata.PropertiesHandler;

/**
 *
 * Copy file/directory.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVMkDir implements GVFileCommand
{
    private static Logger logger = GVLogger.getLogger(GVMkDir.class);

    private String        targetPath;
    private boolean       isCritical;

    /**
     *
     */
    public GVMkDir()
    {
        // do nothing
    }

    /**
     * @param node
     * @throws Exception
     */
    @Override
    public void init(Node node) throws Exception
    {
        this.targetPath = XMLConfig.get(node, "@targetPath");
        this.isCritical = XMLConfig.getBoolean(node, "@isCritical", true);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return ((this.isCritical ? "[CRITICAL] " : "") + "MKDIR directory " + this.targetPath);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.file.command.GVFileCommand#execute(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public void execute(GVBuffer gvBuffer) throws Exception
    {
        try {
            PropertiesHandler.enableExceptionOnErrors();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            String currTargetPath = PropertiesHandler.expand(this.targetPath, params, gvBuffer);

            FileManager.mkdir(currTargetPath);
            logger.debug("Directory " + currTargetPath + " successfully created");
        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.file.command.GVFileCommand#isCritical()
     */
    @Override
    public boolean isCritical()
    {
        return this.isCritical;
    }
}

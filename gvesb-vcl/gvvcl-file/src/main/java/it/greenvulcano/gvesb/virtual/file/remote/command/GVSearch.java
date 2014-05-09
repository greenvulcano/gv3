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
package it.greenvulcano.gvesb.virtual.file.remote.command;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.file.RegExFileFilter;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.remotefs.RemoteManager;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * Search for files.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 *         REVISON OK
 */
public class GVSearch implements GVRemoteCommand
{
    private static Logger logger = GVLogger.getLogger(GVSearch.class);

    private String        sourcePath;
    private String        filePattern;
    private boolean       isCritical;

    /**
     *
     */
    public GVSearch()
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
        sourcePath = XMLConfig.get(node, "@sourcePath");
        filePattern = XMLConfig.get(node, "@filePattern", "");
        isCritical = XMLConfig.getBoolean(node, "@isCritical", true);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return ((isCritical ? "[CRITICAL] " : "") + "CHECK for existence of file '" + filePattern
                + "' in remote directory " + sourcePath);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.file.remote.command.GVRemoteCommand#executeOperation(it.greenvulcano.util.remotefs.RemoteManager,
     *      it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public void execute(RemoteManager ftpAccess, GVBuffer gvBuffer) throws Exception
    {
        try {
            PropertiesHandler.enableExceptionOnErrors();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            String currSourcePath = PropertiesHandler.expand(sourcePath, params, gvBuffer);
            String currFile = PropertiesHandler.expand(filePattern, params, gvBuffer);

            Set<FileProperties> results = ftpAccess.ls(currSourcePath, currFile, null, RegExFileFilter.FILES_ONLY);

            int resultsSize = results.size();

            gvBuffer.setProperty(GVRM_FOUND_FILES_NUM, "" + resultsSize);

            boolean result = (resultsSize > 0);
            if (result) {
                StringBuffer buf = new StringBuffer();
                for (FileProperties currFileInfo : results) {
                    String filename = currFileInfo.getName();
                    buf.append(filename).append(";");
                }
                if (buf.length() > 1) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                logger.debug("File (" + resultsSize + ") matching the pattern '" + currFile
                        + "' found within remote directory " + currSourcePath);
                logger.debug(buf.toString());
                gvBuffer.setProperty(GVRM_FOUND_FILES_LIST, buf.toString());
            }
        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.file.remote.command.GVRemoteCommand#isCritical()
     */
    @Override
    public boolean isCritical()
    {
        return isCritical;
    }
}

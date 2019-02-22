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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.file.FileManager;
import it.greenvulcano.util.file.FileNameSorter;
import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.metadata.PropertiesHandler;

/**
 *
 * Search for files.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVSearch implements GVFileCommand
{
    private static Logger logger = GVLogger.getLogger(GVSearch.class);

    private String        sourcePath;
    private String        filePattern;
    private boolean       returnFullPath;
    private int 		  maxFileList = -1;
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
        this.sourcePath = XMLConfig.get(node, "@sourcePath");
        this.filePattern = XMLConfig.get(node, "@filePattern", "");
        this.returnFullPath = XMLConfig.getBoolean(node, "@returnFullPath", false);
        this.maxFileList = XMLConfig.getInteger(node, "@maxFileList", -1);
        this.isCritical = XMLConfig.getBoolean(node, "@isCritical", true);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return ((this.isCritical ? "[CRITICAL] " : "") + "CHECK for existence of file[" + this.maxFileList + "] '" + this.filePattern + "' in directory " + this.sourcePath);
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
            String currSourcePath = PropertiesHandler.expand(this.sourcePath, params, gvBuffer);
            String currFile = PropertiesHandler.expand(this.filePattern, params, gvBuffer);

            Set<FileProperties> results = FileManager.ls(currSourcePath, currFile, this.maxFileList);
            int resultsSize = results.size();

            gvBuffer.setProperty(GVFM_FOUND_FILES_NUM, "" + resultsSize);

            boolean result = (resultsSize > 0);
            if (result) {
                StringBuilder buf = new StringBuilder();
                List<FileProperties> fm_files = new ArrayList<FileProperties>(resultsSize);
                fm_files.addAll(results);
                Collections.sort(fm_files, new FileNameSorter(true));
                for (FileProperties currFileInfo : fm_files) {
                    String filename = currFileInfo.getName();
                    if (this.returnFullPath) {
                        buf.append(currSourcePath).append(File.separator);
                    }
                    buf.append(filename).append(";");
                }
                if (buf.length() > 1) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                logger.debug("File (" + resultsSize + ") matching the pattern '" + currFile + "' found within directory " + currSourcePath);
                logger.debug(buf.toString());
                gvBuffer.setProperty(GVFM_FOUND_FILES_LIST, buf.toString());
            }
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

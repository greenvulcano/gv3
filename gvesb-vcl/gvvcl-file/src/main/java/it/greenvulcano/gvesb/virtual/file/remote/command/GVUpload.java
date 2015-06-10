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
import it.greenvulcano.expression.ognl.OGNLExpressionEvaluator;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.remotefs.RemoteManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * Upload a local file/directory, or the GVBuffer body, on a remote file system.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 * 
 */
public class GVUpload implements GVRemoteCommand
{
    private static final Logger logger = GVLogger.getLogger(GVUpload.class);

    private String              sourcePath;
    private String              sourceFilePattern;
    private String              remotePath;
    private String              fromGVBufferExpression;
    private boolean             isCritical;


    /**
     *
     */
    public GVUpload()
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
        sourceFilePattern = XMLConfig.get(node, "@sourceFilePattern");
        remotePath = XMLConfig.get(node, "@remotePath");
        fromGVBufferExpression = XMLConfig.get(node, "@fromGVBufferExpression");
        isCritical = XMLConfig.getBoolean(node, "@isCritical", true);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        if (fromGVBufferExpression != null) {
            return ((isCritical ? "[CRITICAL] " : "") + "UPLOAD GVBuffer body to file '" + sourceFilePattern
                    + "' in directory " + remotePath);
        }
        return ((isCritical ? "[CRITICAL] " : "") + "UPLOAD  file(s) '" + sourceFilePattern + "' from directory "
                + sourcePath + " to " + remotePath);
    }


    /**
     * @see it.greenvulcano.gvesb.virtual.file.remote.command.GVRemoteCommand#executeOperation(it.greenvulcano.util.remotefs.RemoteManager,
     *      it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public void execute(RemoteManager manager, GVBuffer gvBuffer) throws Exception
    {
        try {
            PropertiesHandler.enableExceptionOnErrors();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            String currSourcePath = PropertiesHandler.expand(sourcePath, params, gvBuffer);
            String currSourceFile = PropertiesHandler.expand(sourceFilePattern, params, gvBuffer);
            String currRemotePath = PropertiesHandler.expand(remotePath, params, gvBuffer);

            boolean result = false;
            if ((fromGVBufferExpression != null) && (fromGVBufferExpression.length() > 0)) {
                OGNLExpressionEvaluator ognl = new OGNLExpressionEvaluator();
                ognl.addToContext("gvbuffer", gvBuffer);
                Object obj = ognl.getValue(fromGVBufferExpression, gvBuffer);
                InputStream is = new ByteArrayInputStream((byte[]) obj);
                result = manager.put(is, currRemotePath, currSourceFile);
            }
            else {
                result = manager.put(currSourcePath, currSourceFile, currRemotePath);
            }
            if (result) {
                logger.debug("File(s) " + currSourceFile + " successfully uploaded from local directory "
                        + currSourcePath + " to remote directory " + currRemotePath);
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

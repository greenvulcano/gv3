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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Download a remote file/directory on local file system, or in the GVBuffer
 * body.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class GVDownload implements GVRemoteCommand
{
    private static final Logger logger = GVLogger.getLogger(GVDownload.class);

    private String              remotePath;
    private String              remoteFilePattern;
    private String              toGVBufferExpression;
    private String              targetPath;
    private boolean             isCritical;
    private Map<String, String> optProperties = new HashMap<String, String>();

    /**
     *
     */
    public GVDownload()
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
        remotePath = XMLConfig.get(node, "@remotePath");
        remoteFilePattern = XMLConfig.get(node, "@remoteFilePattern");
        toGVBufferExpression = XMLConfig.get(node, "@toGVBufferExpression");
        targetPath = XMLConfig.get(node, "@targetPath");
        isCritical = XMLConfig.getBoolean(node, "@isCritical", true);

        NodeList nl = XMLConfig.getNodeList(node, "PropertyDef");
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                String name = XMLConfig.get(nl.item(i), "@name");
                String value = XMLConfig.get(nl.item(i), "@value", "");
                optProperties.put(name, value);
            }
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        if (toGVBufferExpression != null) {
            return ((isCritical ? "[CRITICAL] " : "") + "DOWNLOAD remote file '" + remoteFilePattern
                    + "' from directory " + remotePath + " to GVBuffer body");
        }
        return ((isCritical ? "[CRITICAL] " : "") + "DOWNLOAD remote file(s) '" + remoteFilePattern
                + "' from directory " + remotePath + " to " + targetPath);
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
            String currRemotePath = PropertiesHandler.expand(remotePath, params, gvBuffer);
            String currRemoteFile = PropertiesHandler.expand(remoteFilePattern, params, gvBuffer);
            String currTargetPath = PropertiesHandler.expand(targetPath, params, gvBuffer);

            Map<String, String> localOptProperties = new HashMap<String, String>();
            for (String prop : optProperties.keySet()) {
                localOptProperties.put(prop, PropertiesHandler.expand(optProperties.get(prop), params, gvBuffer));
            }
            
            boolean result = true;
            if ((toGVBufferExpression != null) && (toGVBufferExpression.length() > 0)) {
                ByteArrayOutputStream outputDataStream = new ByteArrayOutputStream();
                result = ftpAccess.get(currRemotePath, currRemoteFile, outputDataStream, localOptProperties);
                outputDataStream.close();
                OGNLExpressionEvaluator ognl = new OGNLExpressionEvaluator();
                ognl.addToContext("gvbuffer", gvBuffer);
                ognl.addToContext("input", outputDataStream.toByteArray());
                ognl.getValue(toGVBufferExpression, gvBuffer);
            }
            else {
                result = ftpAccess.get(currRemotePath, currRemoteFile, currTargetPath, localOptProperties);
            }
            if (result) {
                logger.debug("File(s) " + currRemoteFile + " successfully downloaded from remote directory "
                        + currRemotePath + " into local directory " + currTargetPath);
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

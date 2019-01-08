/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.virtual.rsh;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.rsh.client.RSHServiceClient;
import it.greenvulcano.gvesb.rsh.client.RSHServiceClientManager;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * Write GVBuffer.object (if contains byte array, String or Node) in a (remote)
 * file.
 * 
 * @version 3.2.0 22/10/2011
 * @author GreenVulcano Developer Team
 */
public class RemoteFileWriterCallOperation implements CallOperation
{

    private static final Logger logger       = GVLogger.getLogger(RemoteFileWriterCallOperation.class);

    /**
     * The instance name.
     */
    private String              name         = null;

    /**
     * The path name for the target file. Can contain placeholders that will
     * be replaced at call time.
     */
    private String              filePathName = null;

    private String              clientName   = "";

    /**
     * the operation key
     */
    protected OperationKey      key          = null;

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            name = XMLConfig.get(node, "@name");
            clientName = XMLConfig.get(node, "@rsh-client-name");
            filePathName = XMLConfig.get(node, "@filePathName");

            logger.debug("clientName: " + clientName + " - filePathName : " + filePathName);

            logger.debug("RemoteFileWriterCallOperation " + name + " configured");
        }
        catch (XMLConfigException exc) {
            logger.error("An error occurred while reading configuration", exc);
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
        catch (Exception exc) {
            logger.error("A generic error occurred while initializing", exc);
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        RSHServiceClient svcClient = null;
        try {
            String targetFile = buildTargetPath(gvBuffer);

            svcClient = RSHServiceClientManager.instance().getRSHServiceClient(clientName);

            logger.debug("Writing file: " + targetFile);
            Object data = gvBuffer.getObject();
            if (data instanceof byte[]) {
                svcClient.sendFileB(targetFile, (byte[]) data);
            }
            else if (data instanceof String) {
                svcClient.sendFileB(targetFile, ((String) data).getBytes());
            }
            else if (data instanceof Node) {
                svcClient.sendFileB(targetFile, XMLUtils.serializeDOMToByteArray_S((Node) data));
            }
            else {
                throw new InvalidDataException("Invalid GVBuffer content: " + data.getClass().getName());
            }

            return gvBuffer;
        }
        catch (Exception exc) {
            logger.error("An error occurred while writing file", exc);
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
        finally {
            try {
                RSHServiceClientManager.instance().releaseRSHServiceClient(svcClient);
            }
            catch (Exception exc) {
                // do nothing
            }
        }
    }


    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    @Override
    public OperationKey getKey()
    {
        return key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

    private String buildTargetPath(GVBuffer gvBuffer) throws Exception
    {
        try {
            PropertiesHandler.enableExceptionOnErrors();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);

            String targetFilePathName = gvBuffer.getProperty("GVFW_FILE_NAME");
            if (targetFilePathName == null) {
                if (filePathName != null) {
                    targetFilePathName = filePathName;
                }
                else {
                    throw new IllegalArgumentException("Target file path name NOT available");
                }
            }

            targetFilePathName = PropertiesHandler.expand(targetFilePathName, params, gvBuffer);
            return targetFilePathName;
        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
        }
    }
}
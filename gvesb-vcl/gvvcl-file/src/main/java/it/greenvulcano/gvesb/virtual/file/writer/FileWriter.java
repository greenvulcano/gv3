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
package it.greenvulcano.gvesb.virtual.file.writer;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.bin.BinaryUtils;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * Write GVBuffer.object (if contains byte array, String or Node) in a file.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class FileWriter implements CallOperation
{

    private static final Logger logger     = GVLogger.getLogger(FileWriter.class);

    /**
     * The instance name.
     */
    private String              name       = null;

    /**
     * The pathname for the target directory. Can contain placeholders that will
     * be replaced at call time. Must evaluate to an absolute pathname.
     */
    private String              targetPath = null;

    /**
     * Source file name. Can contain placeholders that will be expanded at call
     * time.
     */
    private String              filename   = null;

    /**
     * Enable append data mode.
     */
    private boolean             append     = false;

    /**
     * The optional EOL to append if 'append' is true.
     */
    private String              appendEOL  = null;

    /**
     * the operation key
     */
    protected OperationKey      key        = null;

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            name = XMLConfig.get(node, "@name");
            targetPath = XMLConfig.get(node, "@targetPath");
            filename = XMLConfig.get(node, "@fileName");
            append = XMLConfig.getBoolean(node, "@append", false);
            String eol = XMLConfig.get(node, "@appendEOL", null);

            logger.debug("targetPath : " + targetPath);
            logger.debug("filename   : " + filename);
            logger.debug("append     : " + append);
            if (append && (eol != null)) {
                logger.debug("appendEOL  : " + eol);
                appendEOL = TextUtils.getEOL(eol);
                if (appendEOL == null) {
                    throw new Exception("Invalid appendEOL value [" + eol + "]");
                }
            }

            logger.debug("FileWriter " + name + " configured");
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
        try {
            File targetFile = buildTargetPathname(gvBuffer);

            logger.debug("Writing " + (append ? "(in append mode) " : "") + "file: " + targetFile.getAbsolutePath());
            Object data = gvBuffer.getObject();
            if (data == null) {
                throw new InvalidDataException("The GVBuffer content is NULL");
            }
            if (data instanceof byte[]) {
                BinaryUtils.writeBytesToFile((byte[]) data, targetFile, append);
            }
            else if (data instanceof String) {
                TextUtils.writeFile((String) data, targetFile, append);
            }
            else if (data instanceof Node) {
                BinaryUtils.writeBytesToFile(XMLUtils.serializeDOMToByteArray_S((Node) data), targetFile, append);
            }
            else {
                throw new InvalidDataException("Invalid GVBuffer content: " + data.getClass().getName());
            }

            if (append && (appendEOL != null)) {
                TextUtils.writeFile(appendEOL, targetFile, append);
            }

            return gvBuffer;
        }
        catch (Exception exc) {
            logger.error("An error occurred while writing file", exc);
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
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

    private File buildTargetPathname(GVBuffer gvBuffer) throws Exception
    {
        try {
            PropertiesHandler.enableExceptionOnErrors();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);

            String targetDirectory = gvBuffer.getProperty("GVFW_DIRECTORY");
            if (targetDirectory == null) {
                if (targetPath != null) {
                    targetDirectory = targetPath;
                }
                else {
                    throw new IllegalArgumentException("Pathname NOT available");
                }
            }


            targetDirectory = PropertiesHandler.expand(targetDirectory, params, gvBuffer);

            String targetFilename = gvBuffer.getProperty("GVFW_FILE_NAME");
            if (targetFilename == null) {
                if (filename != null) {
                    targetFilename = filename;
                }
                else {
                    throw new IllegalArgumentException("File NOT available");
                }
            }

            targetFilename = PropertiesHandler.expand(targetFilename, params, gvBuffer);

            File targetPathname = new File(targetDirectory, targetFilename);
            if (targetPathname.isAbsolute()) {
                if (!targetPathname.exists() || (targetPathname.exists() && targetPathname.isFile())) {
                    return targetPathname;
                }
                throw new IllegalArgumentException("Pathname " + targetPathname.getPath() + " not a file");
            }
            throw new IllegalArgumentException("Pathname " + targetPathname.getPath() + " is not absolute");
        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
        }
    }
}
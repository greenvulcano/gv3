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
package it.greenvulcano.gvesb.virtual.excel.reader;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.excel.exception.ExcelException;
import it.greenvulcano.excel.reader.BaseReader;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 *
 * @version 3.0.0 14/ott/2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVExcelReaderCallOperation implements CallOperation
{
    private static final Logger logger      = GVLogger.getLogger(GVExcelReaderCallOperation.class);

    protected OperationKey      key         = null;

    private   BaseReader        excelReader = null;
    
    /**
     * Source file name. Can contain placeholders that will be expanded at call
     * time.
     */
    private String              filename = null;

    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            filename = XMLConfig.get(node, "@fileName", "");
            Node rNode = XMLConfig.getNode(node, "*[@type='excel-reader']");
            if (rNode == null) {
                throw new ExcelException("Missing ExcelReader node");
            }
            excelReader = (BaseReader) Class.forName(XMLConfig.get(rNode, "@class")).newInstance();
            excelReader.init(rNode);
        }
        catch (Exception exc) {
            throw new InitializationException("GV_INIT_SERVICE_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
    }

    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException, 
            InterruptedException {
        InputStream in = null;
        try {
            PropertiesHandler.enableExceptionOnErrors();
            if ("".equals(filename)) {
                in = new ByteArrayInputStream((byte[]) gvBuffer.getObject());                
            }
            else {
                Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
                String xlsFile = PropertiesHandler.expand(filename, params, gvBuffer);
                logger.debug("Reading Excel file: " + xlsFile);
                in = new BufferedInputStream(new FileInputStream(xlsFile));
            }
            excelReader.processExcel(in);

            Object data = excelReader.getAsObject();
            gvBuffer.setObject(data);
        }
        catch (Exception exc) {
            ThreadUtils.checkInterrupted(exc);
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);

        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
        return gvBuffer;
    }


    @Override
    public void cleanUp()
    {
        if (excelReader != null) {
            excelReader.cleanUp();
        }
    }

    @Override
    public void destroy()
    {
        if (excelReader != null) {
            excelReader.destroy();
        }
        excelReader = null;
    }

    /**
     * Return the alias for the given service
     *
     * @param data
     *        the input service data
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer data)
    {
        return data.getService();
    }


    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    public OperationKey getKey()
    {
        return key;
    }
}

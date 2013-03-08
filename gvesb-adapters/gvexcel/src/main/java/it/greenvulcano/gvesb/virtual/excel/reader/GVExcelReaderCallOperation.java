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
import it.greenvulcano.excel.reader.ToXMLReader;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;

import java.io.ByteArrayInputStream;

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

    private ToXMLReader         excelReader = null;
    private boolean             onlyData    = true;

    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            excelReader = new ToXMLReader();
            onlyData = XMLConfig.getBoolean(node, "@onlyData", true);
        }
        catch (Exception exc) {
            throw new InitializationException("GV_INIT_SERVICE_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
    }

    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream((byte[]) gvBuffer.getObject());
            excelReader.processWorkBook(in, onlyData);

            //byte[] data = excelReader.getXMLAsBytes();
            String data = excelReader.getXMLAsString();
            gvBuffer.setObject(data);
        }
        catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);

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

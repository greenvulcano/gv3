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
package it.greenvulcano.gvesb.virtual.excel;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.excel.config.ConfigurationHandler;
import it.greenvulcano.excel.config.ExcelReport;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;

import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class GVExcelCallOperation implements CallOperation
{
    private static final Logger logger      = GVLogger.getLogger(GVExcelCallOperation.class);

    protected OperationKey      key         = null;

    private String              group       = null;
    private String              report      = null;
    private ExcelReport         excelReport = null;

    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            group = XMLConfig.get(node, "@group", "");
            report = XMLConfig.get(node, "@report", "");

            Node en = XMLConfig.getNode(node, "GVExcelReport");
            if (en != null) {
                excelReport = new ExcelReport(en);
                group = excelReport.getGroup();
                report = excelReport.getName();
                logger.debug("Configured for Local Report: " + group + "::" + report);
            }
            else {
                logger.debug("Configured for Generic Report: " + group + "::" + report);
            }
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
            Map<String, Object> props = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            ExcelReport rep = excelReport;
            if (rep == null) {
                rep = ConfigurationHandler.getInstance().getExcelReport(group, report, new HashSet<String>());
            }
            byte[] data = rep.getExcelReportAsByteArray(props);
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
        // do nothing
    }

    @Override
    public void destroy()
    {
        // do nothing
    }

    /**
     * Return the alias for the given service
     * 
     * @param data
     *        the input service data
     * @return the configured alias
     */
    @Override
    public String getServiceAlias(GVBuffer data)
    {
        return data.getService();
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

}

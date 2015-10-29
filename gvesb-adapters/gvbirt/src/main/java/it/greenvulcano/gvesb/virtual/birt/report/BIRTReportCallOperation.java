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
package it.greenvulcano.gvesb.virtual.birt.report;

import it.greenvulcano.birt.report.Report;
import it.greenvulcano.birt.report.ReportManager;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;

import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.0.0 19/dic/2010
 * @author GreenVulcano Developer Team
 */
public class BIRTReportCallOperation implements CallOperation
{
    private static Logger logger     = GVLogger.getLogger(BIRTReportCallOperation.class);

    private OperationKey  key        = null;
    //private Report report = null;
    private String        groupName  = null;
    private String        reportName = null;
    private String        reportType = null;

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            reportType = XMLConfig.get(node, "@reportType", "pdf");
            /*Node rn = XMLConfig.getNode(node, "Report");
            if (rn != null) {
                report = new Report();
                report.init(rn);
                logger.debug("Configured for Local BIRT Report[" + reportType + "]: " + report.getName());
            }
            else {*/
            groupName = XMLConfig.get(node, "@groupName");
            reportName = XMLConfig.get(node, "@reportName");
            logger.debug("Configured for Generic BIRT Report[" + reportType + "]: " + groupName + "/" + reportName);
            //}
        }
        catch (Exception exc) {
            throw new InitializationException("GV_INIT_SERVICE_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }

    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        try {
            Map<String, Object> props = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            Report rep = ReportManager.instance().getReport(groupName, reportName);
            String repType = gvBuffer.getProperty("BIRT_REPORT_TYPE");
            if ((repType == null) || "".equals(repType)) {
                repType = reportType;
            }
            byte[] data = rep.generate(props, repType);
            gvBuffer.setObject(data);
            gvBuffer.setProperty("BIRT_REPORT_TYPE", repType);
        }
        catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);

        }
        return gvBuffer;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
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

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        // TODO Auto-generated method stub

    }
}

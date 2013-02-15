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
package it.greenvulcano.gvesb.core.bpel;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.bpel.manager.GVBpelEngineServer;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.flow.GVFlow;
import it.greenvulcano.gvesb.core.jmx.OperationInfo;
import it.greenvulcano.gvesb.core.jmx.ServiceOperationInfoManager;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.gvesb.statistics.StatisticsData;
import it.greenvulcano.gvesb.statistics.StatisticsDataManager;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * GVFlow ODE.
 *
 * @version 3.3.0 21-oct-2012
 * @author GreenVulcano Developer Team
 */
public class GVFlowODE implements GVFlow
{
    private static Logger           logger                = GVLogger.getLogger(GVFlowODE.class);

    /**
     * the flow name
     */
    private String                  flowName              = "";
    /**
     * the string representation of the output check
     */
    private String                  outCheckType          = "";
    /**
     * the flow's service name
     */
    private String                  serviceName           = "";
    /**
     * the input system name, used for output check
     */
    private String                  inSystem              = "";
    /**
     * the input service name, used for output check
     */
    private String                  inService             = "";
    /**
     * the input id, used for output check
     */
    private String                  inID                  = "";
    /**
     * the operation activation flag
     */
    private boolean                 operationActivation   = true;
    /**
     * the jmx operation info instance
     */
    private OperationInfo           operationInfo         = null;
    /**
     * the statistics data manager instance
     */
    private StatisticsDataManager   statisticsDataManager = null;
    /**
     * the statistics activation flag
     */
    private boolean                 statisticsEnabled     = false;
    /**
     * if true the flow terminate a business flow
     */
    private boolean                 businessFlowTerminated;
    /**
     *  type flow: VL or BPEL
     */
    private String typeFlow = "VL";
    /**
     *  namespace Bpel process
     */
    private String bpelNameSpace;
    /**
     *  name Bpel process
     */
    private String bpelProcessName;

    /**
     * Initialize the instance
     *
     * @param gvopNode
     *        the node from which read configuration data
     * @throws GVCoreConfException
     *         if errors occurs
     */
    public void init(Node gvopNode) throws GVCoreConfException
    {
        logger.debug("BEGIN - GVFlow init");
        logger.debug("gvopNode=" + gvopNode.toString());
        try {
            serviceName = XMLConfig.get(gvopNode, "../@id-service", "NO_SERVICE");

            flowName = XMLConfig.get(gvopNode, "@name", "");
            if (flowName.equals("")) {
                throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'name'"},
                        {"node", XPathFinder.buildXPath(gvopNode)}});
            }
            if (flowName.equals("Forward")) {
                flowName = XMLConfig.get(gvopNode, "@forward-name", "");
                if (flowName.equals("")) {
                    throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{
                            {"name", "'forward-name'"}, {"node", XPathFinder.buildXPath(gvopNode)}});
                }
            }

            outCheckType = XMLConfig.get(gvopNode, "@out-check-type", OUT_CHECK_NONE);
            operationActivation = XMLConfig.getBoolean(gvopNode, "@operation-activation", true);

            Node instNode = XMLConfig.getNode(gvopNode, "BpelFlow");
          	if (instNode == null) {
          		throw new GVCoreConfException("GVCORE_MISSED_FLOW_INSTANCE_ERROR", new String[][]{{"node", XPathFinder.buildXPath(gvopNode)}});
            }
            initBpelFlowNodes(instNode);	
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_MISSED_FLOW_INSTANCE_ERROR", new String[][]{{"node",
                XPathFinder.buildXPath(gvopNode)}});
        }

        logger.debug("END - GVFlow init");
    }
    /**
    * @param instNode
    *        the flow instantiation node
    * @throws GVCoreConfException
    *         if errors occurs
    */
    private void initBpelFlowNodes(Node instNode) throws GVCoreConfException
    {
        try {
            typeFlow = "BPEL";
            logger.debug("instNode=" + instNode.toString());
            bpelNameSpace = XMLConfig.get(instNode, "@namespace");
        	logger.debug("bpelNameSpace=" + bpelNameSpace);
        	bpelProcessName = XMLConfig.get(instNode, "@processname");
        	logger.debug("bpelProcessName=" + bpelProcessName);

            if ((bpelNameSpace == null) || (bpelNameSpace.length() == 0)) {
                throw new GVCoreConfException("GVCORE_EMPTY_NAMESPACE_DEFITION_ERROR", new String[][]{{"name", flowName}});
            }
            if ((bpelProcessName == null) || (bpelProcessName.length() == 0)) {
                throw new GVCoreConfException("GVCORE_EMPTY_PROCESS_DEFITION_ERROR", new String[][]{{"name", flowName}});
            }
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_EMPTY_FLOW_DEFITION_ERROR", new String[][]{{"name", flowName}});
        }
    }

    /**
     * Execute the flow
     *
     * @param gvBuffer
     *        the input data
     * @param onDebug
     *        unused
     * @return the output data
     * @throws GVCoreException
     *         if errors occurs
     */
    public GVBuffer perform(GVBuffer gvBuffer, boolean onDebug) throws GVCoreException
    {
        logger.error("Debug no enabled for " + flowName + "/"+bpelProcessName );
        throw new GVCoreException("GVCORE_INVOCATION_ERROR", new String[][]{{"operation", flowName},
                    {"bpelProcessName", bpelProcessName}, {"message", "Debug mode non enabled"}});
    }

    /**
     * Execute the flow
     *
     * @param gvBuffer
     *        the input data
     * @return the output data
     * @throws GVCoreException
     *         if errors occurs
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws GVCoreException
    {
        boolean useStatistics = statisticsEnabled && (statisticsDataManager != null);
        StatisticsData sd = null;

        try {
            businessFlowTerminated = false;

            if (useStatistics) {
                sd = statisticsDataManager.startStatistics(gvBuffer, "core", flowName);
            }
            GVBuffer outData = internalBpelPerform(gvBuffer);
           
            if (useStatistics) {
                try {
                    if (businessFlowTerminated) {
                        statisticsDataManager.stopStatistics(sd, outData, 1);
                    }
                    else {
                        statisticsDataManager.stopStatistics(sd, outData);
                    }
                }
                catch (Exception exc) {
                    logger.error("Error during statistics management", exc);
                }
            }
            return outData;
        }
        catch (GVCoreException exc) {
            if (useStatistics) {
                try {
                    if (businessFlowTerminated) {
                        statisticsDataManager.stopStatistics(sd, exc, 0);
                    }
                    else {
                        statisticsDataManager.stopStatistics(sd, exc);
                    }
                }
                catch (Exception exc1) {
                    logger.error("Error during statistics management", exc1);
                }
            }
            throw exc;
        }
        finally {
            cleanUp();
        }
    }

    /**
     * Execute the flow
     *
     * @param gvBuffer
     *        the input data
     * @return the output data
     * @throws GVCoreException
     *         if errors occurs
     */
    public GVBuffer recover(String recoveryNode, Map<String, Object> environment) throws GVCoreException
    {
        logger.error("Unrecoverable Operation " + flowName + "/"+bpelProcessName );
        throw new GVCoreException("GVCORE_CONFIGURATION_NODE_ERROR", new String[][]{{"operation", flowName},
                    {"bpelProcessName", bpelProcessName}});
    }
    /**
     * Execute the flow
     *
     * @param gvBuffer
     *        the input data
     * @return the output data
     * @throws GVCoreException
     *         if errors occurs
     */
    private GVBuffer internalBpelPerform(GVBuffer gvBuffer) throws GVCoreException
    {
    	GVBuffer output = gvBuffer;

    	if (logger.isDebugEnabled()) {
    		logger.debug(GVFormatLog.formatBEGIN(flowName, gvBuffer));
    	}

    	inSystem = gvBuffer.getSystem();
    	inService = gvBuffer.getService();
    	inID = gvBuffer.getId().toString();

    	getGVOperationInfo();

		try {
			QName processQName = new QName(bpelNameSpace, bpelProcessName);
			Object input = gvBuffer.getObject();
			Document doc = (Document) XMLUtils.parseObject_S(input, false, true);
			logger.info("flowName="+flowName);
			GVBpelEngineServer gvBpelManager = GVBpelEngineServer.instance();
			if(flowName.equals("RequestReply")||
					flowName.equals("GetReply")	||
					flowName.equals("GetRequest")){
				output = gvBpelManager.invokeASYNCRR(gvBuffer, processQName, "process", doc.getDocumentElement());
				if (output!=null && output.getObject() instanceof Throwable) {
					if (output.getObject() instanceof GVCoreException) {
						throw (GVCoreException) output.getObject();
					}
					throw new GVCoreException("GVCORE_FLOW_EXCEPTION_ERROR", new String[][]{{"operation", flowName}},
							(Throwable) output.getObject());
				}
			}else{
				gvBpelManager.invokeASYNCR(gvBuffer, processQName, "process", doc.getDocumentElement());
			}
		    if (logger.isDebugEnabled()) {
				logger.debug(GVFormatLog.formatEND(flowName, (GVBuffer) output));
			}
			
		} catch (Exception exc) {
			logger.error("Error executing BPEL flow", exc);
			throw new GVCoreException("GVCORE_EXECBPEL_ERROR", new String[][]{{"name", flowName}, {"message", "" + exc}});
		}

    	return (GVBuffer) output;
    }

    /**
     * Initialize the associated OperationInfo instance
     */
    private void getGVOperationInfo()
    {
        if (operationInfo == null) {
            try {
                operationInfo = ServiceOperationInfoManager.instance().getOperationInfo(serviceName, flowName, true);
            }
            catch (Exception exc) {
                logger.warn("Error on MBean registration: " + exc);
                operationInfo = null;
            }
        }
    }

    /**
     * @return the statistics data manager
     */
    public StatisticsDataManager getStatisticsDataManager()
    {
        return statisticsDataManager;
    }

    /**
     * @param manager
     *        the statistics data manager
     */
    public void setStatisticsDataManager(StatisticsDataManager manager)
    {
        statisticsDataManager = manager;
    }

    /**
     * @return the statistics activation flag value
     */
    public boolean isStatisticsEnabled()
    {
        return statisticsEnabled;
    }

    /**
     * @param b
     *        set the statistics activation flag
     */
    public void setStatisticsEnabled(boolean b)
    {
        statisticsEnabled = b;
    }

    /**
     * @return the flow activation flag value
     */
    public boolean getActivation()
    {
        getGVOperationInfo();
        if (operationInfo != null) {
            return operationInfo.getOperationActivation();
        }
        return operationActivation;
    }

    /**
     * Execute cleanup operations
     */
    private void cleanUp()
    {
        // do nothing
    }

    /**
     * Execute destroy operations
     */
    public void destroy()
    {
    	// do nothing
    }
}
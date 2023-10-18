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
package it.greenvulcano.gvesb.core.flow;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.config.InvocationContext;
import it.greenvulcano.gvesb.core.config.ServiceLoggerLevelManager;
import it.greenvulcano.gvesb.core.debug.DebugSynchObject;
import it.greenvulcano.gvesb.core.debug.DebuggingInvocationHandler;
import it.greenvulcano.gvesb.core.debug.ExecutionInfo;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.exc.GVCoreWrongInterfaceException;
import it.greenvulcano.gvesb.core.jmx.OperationInfo;
import it.greenvulcano.gvesb.core.jmx.ServiceOperationInfoManager;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.gvesb.statistics.StatisticsData;
import it.greenvulcano.gvesb.statistics.StatisticsDataManager;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xpath.XPathFinder;

/**
 * GVFlow Workflow.
 *
 * @version 3.3.0 Nov 19, 2012
 * @author GreenVulcano Developer Team
 */
public class GVFlowWF implements GVFlow
{
    private static Logger           logger                = GVLogger.getLogger(GVFlowWF.class);

    /**
     * Holds the flow node instances
     */
    private Map<String, GVFlowNode> flowNodes             = new LinkedHashMap<String, GVFlowNode>();
    /**
     * the first node id
     */
    private String                  firstNode             = "";
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
     * The default logger level.
     */
    private Level                   loggerLevel           = Level.ALL;


    /**
     * Initialize the instance
     *
     * @param gvopNode
     *        the node from which read configuration data
     * @throws GVCoreConfException
     *         if errors occurs
     */
    @Override
    public void init(Node gvopNode) throws GVCoreConfException {
        logger.debug("BEGIN - GVFlow init");
        logger.debug("gvopNode=" + gvopNode.toString());
        try {
            this.serviceName = XMLConfig.get(gvopNode, "../@id-service", "NO_SERVICE");

            this.flowName = XMLConfig.get(gvopNode, "@name", "");
            if (this.flowName.equals("")) {
                throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'name'"},
                        {"node", XPathFinder.buildXPath(gvopNode)}});
            }
            if (this.flowName.equals("Forward")) {
                this.flowName = XMLConfig.get(gvopNode, "@forward-name", "");
                if (this.flowName.equals("")) {
                    throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{
                            {"name", "'forward-name'"}, {"node", XPathFinder.buildXPath(gvopNode)}});
                }
            }

            this.outCheckType = XMLConfig.get(gvopNode, "@out-check-type", OUT_CHECK_NONE);
            this.operationActivation = XMLConfig.getBoolean(gvopNode, "@operation-activation", true);

            this.loggerLevel = ServiceLoggerLevelManager.instance().getLoggerLevel(this.serviceName, this.flowName);

            Node instNode = XMLConfig.getNode(gvopNode, "Flow");
            if (instNode == null) {
                throw new GVCoreConfException("GVCORE_MISSED_FLOW_INSTANCE_ERROR", new String[][]{{"node",
                        XPathFinder.buildXPath(gvopNode)}});
            }
            initFlowNodes(instNode);
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
    private void initFlowNodes(Node instNode) throws GVCoreConfException {
        Level level = null;
        try {
            level = GVLogger.setThreadMasterLevel(getLoggerLevel());

            try {
                this.firstNode = XMLConfig.get(instNode, "@first-node");

                NodeList nl = null;
                nl = XMLConfig.getNodeList(instNode, "*[@type='flow-node']");
                if ((nl == null) || (nl.getLength() == 0)) {
                    throw new GVCoreConfException("GVCORE_EMPTY_FLOW_DEFITION_ERROR", new String[][]{{"name", this.flowName}});
                }

                Node defNode = null;
                try {
                    for (int i = 0; i < nl.getLength(); i++) {
                        GVFlowNode flowNode = null;
                        defNode = nl.item(i);
                        String nodeClass = XMLConfig.get(defNode, "@class");
                        String nodeId = XMLConfig.get(defNode, "@id");
                        logger.debug("creating GVFlowNode(" + i + ")[" + nodeId + "] of class " + nodeClass);
                        flowNode = (GVFlowNode) Class.forName(nodeClass).newInstance();
                        flowNode.init(defNode);
                        this.flowNodes.put(flowNode.getId(), flowNode);
                    }
                }
                catch (GVCoreConfException exc) {
                    throw exc;
                }
                catch (Exception exc) {
                    logger.error("Error initiliazing FlowNode " + this.flowName, exc);
                    throw new GVCoreConfException("GVCORE_INIT_FLOW_NODE_ERROR", new String[][]{{"name", this.flowName},
                            {"node", XPathFinder.buildXPath(defNode)}}, exc);
                }
            }
            catch (XMLConfigException exc) {
                throw new GVCoreConfException("GVCORE_EMPTY_FLOW_DEFITION_ERROR", new String[][]{{"name", this.flowName}});
            }
        }
        finally {
            GVLogger.removeThreadMasterLevel(level);
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
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws GVCoreException, InterruptedException {
        return perform(gvBuffer, false);
    }

    /**
     * Execute the flow
     *
     * @param gvBuffer
     *        the input data
     * @param onDebug
     * @return the output data
     * @throws GVCoreException
     *         if errors occurs
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer, boolean onDebug) throws GVCoreException, InterruptedException {

        if (!onDebug) {
            onDebug = "true".equalsIgnoreCase(gvBuffer.getProperty("GV_FLOW_DEBUG"));
            if (!onDebug) {
                ExecutionInfo execInfo = new ExecutionInfo(this.serviceName, this.flowName, null, null, null);
                onDebug = DebugSynchObject.checkWaitingDebug(execInfo);
            }
        }
        boolean useStatistics = this.statisticsEnabled && (this.statisticsDataManager != null) && !onDebug;
        StatisticsData sd = null;

        try {
            this.businessFlowTerminated = false;

            if (useStatistics) {
                sd = this.statisticsDataManager.startStatistics(gvBuffer, "core", this.flowName);
            }
            GVBuffer outData = internalPerform(gvBuffer, onDebug);

            if (useStatistics) {
                try {
                    if (this.businessFlowTerminated) {
                        this.statisticsDataManager.stopStatistics(sd, outData, 1);
                    }
                    else {
                        this.statisticsDataManager.stopStatistics(sd, outData);
                    }
                }
                catch (Exception exc) {
                    logger.error("Error during statistics management", exc);
                }
            }
            return outData;
        }
        catch (InterruptedException exc) {
            logger.error("GVFlowWF[" + this.flowName + "] interrupted.");
            throw exc;
        }
        catch (GVCoreException exc) {
            if (useStatistics) {
                try {
                    if (this.businessFlowTerminated) {
                        this.statisticsDataManager.stopStatistics(sd, exc, 0);
                    }
                    else {
                        this.statisticsDataManager.stopStatistics(sd, exc);
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
    @Override
    public GVBuffer recover(String recoveryNode, Map<String, Object> environment) throws GVCoreException, InterruptedException {
        boolean useStatistics = this.statisticsEnabled && (this.statisticsDataManager != null);
        StatisticsData sd = null;

        try {
            this.businessFlowTerminated = false;

            GVBuffer gvBuffer = (GVBuffer) environment.get(this.flowNodes.get(recoveryNode).getInput());

            if (useStatistics) {
                sd = this.statisticsDataManager.startStatistics(gvBuffer, "core", this.flowName);
            }
            GVBuffer outData = internalRecover(gvBuffer, recoveryNode, environment);

            if (useStatistics) {
                try {
                    if (this.businessFlowTerminated) {
                        this.statisticsDataManager.stopStatistics(sd, outData, 1);
                    }
                    else {
                        this.statisticsDataManager.stopStatistics(sd, outData);
                    }
                }
                catch (Exception exc) {
                    logger.error("Error during statistics management", exc);
                }
            }
            return outData;
        }
        catch (InterruptedException exc) {
            logger.error("GVFlowWF[" + this.flowName + "] interrupted.");
            throw exc;
        }
        catch (GVCoreException exc) {
            if (useStatistics) {
                try {
                    if (this.businessFlowTerminated) {
                        this.statisticsDataManager.stopStatistics(sd, exc, 0);
                    }
                    else {
                        this.statisticsDataManager.stopStatistics(sd, exc);
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
    private GVBuffer internalPerform(GVBuffer gvBuffer, boolean onDebug) throws GVCoreException, InterruptedException {
        if (logger.isDebugEnabled()) {
            logger.debug(GVFormatLog.formatBEGIN(this.flowName, gvBuffer));
        }

        this.inSystem = gvBuffer.getSystem();
        this.inService = gvBuffer.getService();
        this.inID = gvBuffer.getId().toString();

        getGVOperationInfo();

        GVFlowNode flowNode = this.flowNodes.get(this.firstNode);
        if (flowNode == null) {
            logger.error("FlowNode " + this.firstNode + " not configured. Check configuration.");
            throw new GVCoreException("GVCORE_MISSED_FLOW_NODE_ERROR", new String[][]{{"operation", this.flowName},
                    {"flownode", this.firstNode}});
        }

        Map<String, Object> environment = new HashMap<String, Object>();
        environment.put(flowNode.getInput(), gvBuffer);
        String nextNode = this.firstNode;

        if (onDebug) {
            ExecutionInfo info = new ExecutionInfo(this.serviceName, this.flowName, nextNode, null, environment);
            DebugSynchObject synchObj = DebugSynchObject.getSynchObject(this.inID, info);
            if (synchObj == null) {
                synchObj = DebugSynchObject.createNew(Thread.currentThread().getName(), this.inID, info);
            }
            while (!nextNode.equals("") && !isInterrupted()) {
                this.operationInfo.setFlowStatus(this.inID, nextNode);
                flowNode = this.flowNodes.get(nextNode);
                if (flowNode == null) {
                    logger.error("FlowNode " + nextNode + " not configured. Check configuration.");
                    throw new GVCoreException("GVCORE_MISSED_FLOW_NODE_ERROR", new String[][]{{"operation", this.flowName},
                            {"flownode", nextNode}});
                }
                GVFlowNodeIF proxy = DebuggingInvocationHandler.getProxy(GVFlowNodeIF.class, flowNode, synchObj);
                nextNode = proxy.execute(environment, onDebug);
            }
            synchObj.terminated();
        }
        else {
            while (!nextNode.equals("") && !isInterrupted()) {
                if (this.operationInfo != null) {
                    this.operationInfo.setFlowStatus(this.inID, nextNode);
                }
                flowNode = this.flowNodes.get(nextNode);
                if (flowNode == null) {
                    logger.error("FlowNode " + nextNode + " not configured. Check configuration.");
                    throw new GVCoreException("GVCORE_MISSED_FLOW_NODE_ERROR", new String[][]{{"operation", this.flowName},
                            {"flownode", nextNode}});
                }
                nextNode = flowNode.execute(environment);
            }
        }

        if (isInterrupted()) {
            logger.error("GVFlowWF[" + this.flowName + "] interrupted.");
            throw new InterruptedException("GVFlowWF[" + this.flowName + "] interrupted.");
        }

        Object output = environment.get(flowNode.getOutput());
        this.businessFlowTerminated = flowNode.isBusinessFlowTerminated();

        if (output instanceof Throwable) {
            if (output instanceof GVCoreException) {
                throw (GVCoreException) output;
            }
            throw new GVCoreException("GVCORE_FLOW_EXCEPTION_ERROR", new String[][]{{"operation", this.flowName},
                    {"message", "" + output}}, (Throwable) output);
        }

        performOutputCheck(output);

        if (logger.isDebugEnabled()) {
            logger.debug(GVFormatLog.formatEND(this.flowName, (GVBuffer) output));
        }

        return (GVBuffer) output;
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
    private GVBuffer internalRecover(GVBuffer gvBuffer, String recoveryNode, Map<String, Object> environment)
            throws GVCoreException, InterruptedException {
        if (logger.isDebugEnabled()) {
            logger.debug(GVFormatLog.formatBEGIN(this.flowName, gvBuffer));
        }

        this.inSystem = gvBuffer.getSystem();
        this.inService = gvBuffer.getService();
        this.inID = gvBuffer.getId().toString();

        getGVOperationInfo();

        GVFlowNode flowNode = this.flowNodes.get(recoveryNode);
        if (flowNode == null) {
            logger.error("FlowNode " + recoveryNode + " not configured. Check configuration.");
            throw new GVCoreException("GVCORE_MISSED_FLOW_NODE_ERROR", new String[][]{{"operation", this.flowName},
                    {"flownode", recoveryNode}});
        }

        // tipically flowNode is an instances of GVSavePointNode
        String nextNode = flowNode.getDefaultNextNodeId();

        while (!nextNode.equals("") && !isInterrupted()) {
            if (this.operationInfo != null) {
                this.operationInfo.setFlowStatus(this.inID, nextNode);
            }
            flowNode = this.flowNodes.get(nextNode);
            if (flowNode == null) {
                logger.error("FlowNode " + nextNode + " not configured. Check configuration.");
                throw new GVCoreException("GVCORE_MISSED_FLOW_NODE_ERROR", new String[][]{{"operation", this.flowName},
                        {"flownode", nextNode}});
            }
            nextNode = flowNode.execute(environment);
        }

        if (isInterrupted()) {
            logger.error("GVFlowWF[" + this.flowName + "] interrupted.");
            throw new InterruptedException("GVFlowWF[" + this.flowName + "] interrupted.");
        }

        Object output = environment.get(flowNode.getOutput());
        this.businessFlowTerminated = flowNode.isBusinessFlowTerminated();

        if (output instanceof Throwable) {
            if (output instanceof GVCoreException) {
                throw (GVCoreException) output;
            }
            throw new GVCoreException("GVCORE_FLOW_EXCEPTION_ERROR", new String[][]{{"operation", this.flowName}},
                    (Throwable) output);
        }

        performOutputCheck(output);

        if (logger.isDebugEnabled()) {
            logger.debug(GVFormatLog.formatEND(this.flowName, (GVBuffer) output));
        }

        return (GVBuffer) output;
    }

    /**
     * @param output
     *        the flow output
     * @throws GVCoreWrongInterfaceException
     *         if check fail
     */
    private void performOutputCheck(Object output) throws GVCoreWrongInterfaceException {
        if (!this.outCheckType.equals(OUT_CHECK_NONE)) {
            GVBuffer outData = (GVBuffer) output;
            boolean checkFailed = false;
            if (this.outCheckType.equals(OUT_CHECK_SYS_SVC)) {
                checkFailed = !(this.inSystem.equals(outData.getSystem()) && this.inService.equals(outData.getService()));
            }
            else if (this.outCheckType.equals(OUT_CHECK_SYS_SVC_ID)) {
                checkFailed = !(this.inSystem.equals(outData.getSystem()) && this.inService.equals(outData.getService()) && this.inID.equals(outData.getId().toString()));
            }
            if (checkFailed) {
                throw new GVCoreWrongInterfaceException("GVCORE_FAILED_OUTPUT_CHECK_ERROR", new String[][]{
                        {"in-service", this.inService}, {"in-system", this.inSystem}, {"in-id", this.inID},
                        {"out-service", outData.getService()}, {"out-system", outData.getSystem()},
                        {"out-id", outData.getId().toString()}, {"check-type", this.outCheckType}});
            }
        }
    }

    /**
     * Initialize the associated OperationInfo instance
     */
    private void getGVOperationInfo() {
        if (this.operationInfo == null) {
            try {
                this.operationInfo = ServiceOperationInfoManager.instance().getOperationInfo(this.serviceName, this.flowName, true);
            }
            catch (Exception exc) {
                logger.warn("Error on MBean registration: " + exc);
                this.operationInfo = null;
            }
        }
    }

    /**
     * @return the statistics data manager
     */
    @Override
    public StatisticsDataManager getStatisticsDataManager() {
        return this.statisticsDataManager;
    }

    /**
     * @param manager
     *        the statistics data manager
     */
    @Override
    public void setStatisticsDataManager(StatisticsDataManager manager) {
        this.statisticsDataManager = manager;
    }

    /**
     * @return the statistics activation flag value
     */
    @Override
    public boolean isStatisticsEnabled() {
        return this.statisticsEnabled;
    }

    /**
     * @param b
     *        set the statistics activation flag
     */
    @Override
    public void setStatisticsEnabled(boolean b) {
        this.statisticsEnabled = b;
    }

    /**
     * @return the actual logger level
     */
    @Override
    public Level getLoggerLevel() {
        getGVOperationInfo();
        if (this.operationInfo != null) {
            return this.operationInfo.getLoggerLevelj();
        }
        return this.loggerLevel;
    }

    /**
     *
     * @param loggerLevel
     *        the logger level to set
     */
    @Override
    public void setLoggerLevel(Level loggerLevel) {
        this.loggerLevel = loggerLevel;
    }

    /**
     * @return the flow activation flag value
     */
    @Override
    public boolean getActivation() {
        getGVOperationInfo();
        if (this.operationInfo != null) {
            return this.operationInfo.getOperationActivation();
        }
        return this.operationActivation;
    }

    /**
     * Execute cleanup operations
     */
    private void cleanUp() {
        for (GVFlowNode node : this.flowNodes.values()) {
            try {
                node.cleanUp();
            }
            catch (Exception exc) {
                logger.warn("Failed cleanUp() operation on GVFlowNode " + node.getId(), exc);
            }
        }
        try {
            ((InvocationContext) it.greenvulcano.gvesb.internal.InvocationContext.getInstance()).cleanup();
        }
        catch (Exception exc) {
            logger.warn("Failed cleanUp() of InvocationContext on GVFlow " + this.flowName, exc);
        }
    }

    /**
     * Execute destroy operations
     */
    @Override
    public void destroy() {
        for (GVFlowNode node : this.flowNodes.values()) {
            try {
                node.destroy();
            }
            catch (Exception exc) {
                logger.warn("Failed destroy() operation on GVFlowNode " + node.getId(), exc);
            }
        }
        this.flowNodes.clear();
    }

    /**
     *
     * @return
     *        the current Thread interrupted state
     */
    public boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }
}
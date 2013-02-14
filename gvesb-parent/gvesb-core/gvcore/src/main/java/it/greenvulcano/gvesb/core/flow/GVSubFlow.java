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
package it.greenvulcano.gvesb.core.flow;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.config.InvocationContext;
import it.greenvulcano.gvesb.core.debug.DebugSynchObject;
import it.greenvulcano.gvesb.core.debug.DebuggingInvocationHandler;
import it.greenvulcano.gvesb.core.debug.ExecutionInfo;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.jmx.OperationInfo;
import it.greenvulcano.gvesb.core.jmx.ServiceOperationInfoManager;
import it.greenvulcano.gvesb.gvdte.controller.DTEController;
import it.greenvulcano.gvesb.internal.GVInternalException;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * GVSubFlow.
 * 
 * @version 3.2.0 Mar 01, 2011
 * @author GreenVulcano Developer Team
 * 
 */
public class GVSubFlow
{
    private static Logger           logger         = GVLogger.getLogger(GVSubFlow.class);

    /**
     * Holds the flow node instances
     */
    private Map<String, GVFlowNode> flowNodes      = new HashMap<String, GVFlowNode>();
    /**
     * the first node id
     */
    private String                  firstNode      = "";
    /**
     * the flow name
     */
    private String                  flowName       = "";
    /**
     * the jmx operation info instance
     */
    private OperationInfo           operationInfo  = null;

    private DTEController           dteController  = null;

    /**
     * The ESB context instance.
     */
    private InvocationContext       gvContext      = null;
    /**
     * the SubFlow is executed in the same thread of the caller Flow
     */
    private boolean                 isSingleThread = false;

    /**
     * the service name
     */
    private String                  serviceName;

    private String                  operationName;

    /**
     * Initialize the instance
     * 
     * @param gvsfNode
     *        the node from which read configuration data
     * @throws GVCoreConfException
     *         if errors occurs
     */
    public void init(Node gvsfNode, boolean isSingleThread) throws GVCoreConfException
    {
        logger.debug("BEGIN - GVSubFlow init");

        serviceName = XMLConfig.get(gvsfNode, "../../@id-service", "");
        operationName = XMLConfig.get(gvsfNode, "../@name", "");
        if ("Forward".equals(operationName)) {
            operationName = XMLConfig.get(gvsfNode, "../@forward-name", "");
        }
        flowName = XMLConfig.get(gvsfNode, "@name", "");
        if (flowName.equals("")) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'name'"},
                    {"node", XPathFinder.buildXPath(gvsfNode)}});
        }

        this.isSingleThread = isSingleThread;

        if (!isSingleThread) {
            String dteConfFileName = "GVDataTransformation.xml";
            logger.debug("DTE configuration file: " + dteConfFileName + ".");
            try {
                dteController = new DTEController(dteConfFileName);
            }
            catch (Exception exc) {
                logger.error("Error initializing DTEController from file: " + dteConfFileName, exc);
            }
            gvContext = new InvocationContext();
        }

        initFlowNodes(gvsfNode);

        logger.debug("END - GVSubFlow init");
    }

    /**
     * @param instNode
     *        the flow instantiation node
     * @throws GVCoreConfException
     *         if errors occurs
     */
    private void initFlowNodes(Node instNode) throws GVCoreConfException
    {
        try {
            firstNode = XMLConfig.get(instNode, "@first-node");

            NodeList nl = null;
            nl = XMLConfig.getNodeList(instNode, "*[@type='flow-node']");
            if ((nl == null) || (nl.getLength() == 0)) {
                throw new GVCoreConfException("GVCORE_EMPTY_FLOW_DEFITION_ERROR", new String[][]{{"name", flowName}});
            }

            Node defNode = null;
            try {
                for (int i = 0; i < nl.getLength(); i++) {
                    GVFlowNode flowNode = null;
                    defNode = nl.item(i);
                    String nodeClass = XMLConfig.get(defNode, "@class");
                    logger.debug("creating GVFlowNode(" + i + ") of class " + nodeClass);
                    flowNode = (GVFlowNode) Class.forName(nodeClass).newInstance();
                    flowNode.init(defNode);
                    flowNodes.put(flowNode.getId(), flowNode);
                }
            }
            catch (GVCoreConfException exc) {
                throw exc;
            }
            catch (Exception exc) {
                logger.error("Error initiliazing FlowNode " + flowName, exc);
                throw new GVCoreConfException("GVCORE_INIT_FLOW_NODE_ERROR", new String[][]{{"name", flowName},
                        {"node", XPathFinder.buildXPath(defNode)}}, exc);
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
     * @return the output data
     * @throws GVCoreException
     *         if errors occurs
     */
    public GVBuffer perform(GVBuffer gvBuffer, boolean onDebug) throws GVCoreException
    {
        onDebug = onDebug || "true".equalsIgnoreCase(gvBuffer.getProperty("GV_FLOW_DEBUG"));
        if (!onDebug) {
            ExecutionInfo execInfo = new ExecutionInfo(serviceName, flowName, null, null, null);
            onDebug = DebugSynchObject.checkWaitingDebug(execInfo);
        }

        try {
            if (!isSingleThread) {
                InvocationContext mainCtx = (InvocationContext) InvocationContext.getInstance();
                gvContext.setContext(flowName, gvBuffer);
                gvContext.setGVServiceConfigManager(mainCtx.getGVServiceConfigManager());
                gvContext.setStatisticsDataManager(mainCtx.getStatisticsDataManager());
                // gvContext.setExtraField("DTE_CONTROLLER",
                // mainCtx.getExtraField("DTE_CONTROLLER"));
                gvContext.setExtraField("DTE_CONTROLLER", dteController);
            }
        }
        catch (GVInternalException exc) {
            throw new GVCoreException("GVCORE_FLOW_EXCEPTION_ERROR", new String[][]{{"operation", flowName}}, exc);
        }

        try {
            if (!isSingleThread) {
                gvContext.push();
            }
            GVBuffer outData = internalPerform(gvBuffer, onDebug);
            return outData;
        }
        catch (GVCoreException exc) {
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
    public GVBuffer internalPerform(GVBuffer gvBuffer, boolean onDebug) throws GVCoreException
    {
        if (logger.isDebugEnabled()) {
            logger.debug(GVFormatLog.formatBEGIN(flowName, gvBuffer));
        }
        getGVOperationInfo();

        GVFlowNode flowNode = flowNodes.get(firstNode);
        if (flowNode == null) {
            logger.error("FlowNode " + firstNode + " not configured. Check configuration.");
            throw new GVCoreException("GVCORE_MISSED_FLOW_NODE_ERROR", new String[][]{{"operation", flowName},
                    {"flownode", firstNode}});
        }

        Map<String, Object> environment = new HashMap<String, Object>();
        environment.put(flowNode.getInput(), gvBuffer);
        String nextNode = firstNode;

        if (onDebug) {
            String inID = gvBuffer.getId().toString();
            DebugSynchObject synchObj = DebugSynchObject.getSynchObject(inID, null);
            ExecutionInfo parent = synchObj.getExecutionInfo();
            ExecutionInfo info = new ExecutionInfo(parent);
            info.setSubflow(flowName);
            synchObj.setExecutionInfo(info);
            while (!nextNode.equals("")) {
                operationInfo.setFlowStatus(inID, flowName, nextNode);
                flowNode = flowNodes.get(nextNode);
                if (flowNode == null) {
                    logger.error("FlowNode " + nextNode + " not configured. Check configuration.");
                    throw new GVCoreException("GVCORE_MISSED_FLOW_NODE_ERROR", new String[][]{{"operation", flowName},
                            {"flownode", nextNode}});
                }
                GVFlowNodeIF proxy = DebuggingInvocationHandler.getProxy(GVFlowNodeIF.class, flowNode, synchObj);
                nextNode = proxy.execute(environment, onDebug);
            }
            synchObj.terminated();
        }
        else {
            while (!nextNode.equals("")) {
                /*
                 * if (operationInfo != null) {
                 * operationInfo.setFlowStatus(inID, nextNode); }
                 */
                flowNode = flowNodes.get(nextNode);
                if (flowNode == null) {
                    logger.error("FlowNode " + nextNode + " not configured. Check configuration.");
                    throw new GVCoreException("GVCORE_MISSED_FLOW_NODE_ERROR", new String[][]{{"operation", flowName},
                            {"flownode", nextNode}});
                }
                nextNode = flowNode.execute(environment);
            }
        }

        Object output = environment.get(flowNode.getOutput());
        if (output instanceof Throwable) {
            if (output instanceof GVCoreException) {
                throw (GVCoreException) output;
            }
            throw new GVCoreException("GVCORE_FLOW_EXCEPTION_ERROR", new String[][]{{"operation", flowName}},
                    (Throwable) output);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(GVFormatLog.formatEND(flowName, (GVBuffer) output));
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
                operationInfo = ServiceOperationInfoManager.instance().getOperationInfo(serviceName, operationName,
                        true);
            }
            catch (Exception exc) {
                logger.warn("Error on MBean registration: " + exc);
                operationInfo = null;
            }
        }
    }

    /**
     * Execute cleanup operations
     */
    private void cleanUp()
    {
        for (GVFlowNode node : flowNodes.values()) {
            try {
                node.cleanUp();
            }
            catch (Exception exc) {
                logger.warn("Failed cleanUp() operation on GVFlowNode " + node.getId(), exc);
            }
        }
        if (!isSingleThread) {
            try {
                gvContext.pop();
                gvContext.cleanup();
            }
            catch (Exception exc) {
                logger.warn("Failed cleanUp() of InvocationContext on GVSubFlow " + this.flowName, exc);
            }
        }
    }

    /**
     * Execute destroy operations
     */
    public void destroy()
    {
        for (GVFlowNode node : flowNodes.values()) {
            try {
                node.destroy();
            }
            catch (Exception exc) {
                logger.warn("Failed destroy() operation on GVFlowNode " + node.getId(), exc);
            }
        }
        flowNodes.clear();
        if (!isSingleThread) {
            gvContext.destroy();
            gvContext = null;
            if (dteController != null) {
                dteController.destroy();
                dteController = null;
            }
        }
    }
}
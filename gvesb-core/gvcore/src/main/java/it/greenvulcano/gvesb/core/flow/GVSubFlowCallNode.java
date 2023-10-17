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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.xpath.XPathFinder;

/**
 * GVSubFlowCallNode class.
 *
 * @version 3.2.0 Mar 02, 2011
 * @author GreenVulcano Developer Team
 */
public class GVSubFlowCallNode extends GVFlowNode
{
    private static final Logger logger           = GVLogger.getLogger(GVSubFlowCallNode.class);
    /**
     * the default flow node id
     */
    private String              defaultId        = "";
    /**
     * the onException flow node id
     */
    private String              onExceptionId    = "";
    /**
     * the routing condition vector
     */
    private Vector<GVRouting>   routingVector    = new Vector<GVRouting>();
    /**
     * Definition node.
     */
    private Node                defNode          = null;
    /**
     * The SubFlow name to invoke.
     */
    private String              flowOp           = "";
    /**
     * If true the input SubFlow name are handled as metadata and resolved at runtime.
     */
    private boolean             isSubFlowNameDynamic = false;
    /**
     * The current SubFlow instance.
     */
    private GVSubFlow           subFlow          = null;
    /**
     * The SubFlow instances cache.
     */
    private Map<String, GVSubFlow> subFlowMap       = null;
    /**
     * the input services
     */
    private GVInternalServiceHandler inputServices  = new GVInternalServiceHandler();
    /**
     * the output services
     */
    private GVInternalServiceHandler outputServices = new GVInternalServiceHandler();

    /**
     * If true update the log context.
     */
    private boolean             changeLogContext = false;

    private String              inputRefDP       = null;
    private String              outputRefDP      = null;

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node defNode) throws GVCoreConfException
    {
        super.init(defNode);

        this.defaultId = XMLConfig.get(defNode, "@default-id", "");
        try {
            this.onExceptionId = XMLConfig.get(defNode, "@on-exception-id");
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{
                    {"name", "'on-exception-id'"}, {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }

        NodeList nl = null;
        try {
            nl = XMLConfig.getNodeList(defNode, "GVRouting");
        }
        catch (XMLConfigException exc) {
            // do nothing
        }
        if ((nl != null) && (nl.getLength() > 0)) {
            for (int i = 0; i < nl.getLength(); i++) {
                GVRouting routing = new GVRouting();
                routing.init(nl.item(i), defNode);
                this.routingVector.add(routing);
            }
        }

        if (this.defaultId.equals("") && (this.routingVector.size() == 0)) {
            throw new GVCoreConfException("GVCORE_BAD_ROUTING_CFG_ERROR", new String[][]{{"id", getId()}});
        }

        initSubFlow(defNode);
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#execute(java.util.Map,
     *      boolean)
     */
    @Override
    public String execute(Map<String, Object> environment, boolean onDebug) throws GVCoreException, InterruptedException
    {
        Level level = GVLogger.getThreadMasterLevel();
    	if (isDumpInOut()) {
            GVLogger.setThreadMasterLevel(Level.DEBUG);
        }
    	try {
	    	long startTime = System.currentTimeMillis();
	        GVBuffer internalData = null;
	        String input = getInput();
	        String output = getOutput();
	        logger.info("Executing GVSubFlowCallNode '" + getId() + "'");
	        checkInterrupted("GVSubFlowCallNode");
	        dumpEnvironment(true, environment);

	        GVBuffer data = null;
	        Object inData = environment.get(input);
	        if (Throwable.class.isInstance(inData)) {
	            environment.put(output, inData);
	            logger.debug("END - Execute GVSubFlowCallNode '" + getId() + "' with Exception input -> " + this.onExceptionId);
	            return this.onExceptionId;
	        }
	        try {
	            data = (GVBuffer) inData;
	            if (logger.isDebugEnabled() || isDumpInOut()) {
	                logger.info(GVFormatLog.formatINPUT(data, false, false));
	            }
	            if (input.equals(output)) {
	                internalData = data;
	            }
	            else {
	                internalData = new GVBuffer(data);
	            }

	            try {
	                NMDC.push();

	                String localFlowOp = createSubFlow(internalData);

	                if (this.changeLogContext) {
	                    NMDC.setOperation(localFlowOp);
	                    GVBufferMDC.put(internalData);
	                }

	                DataProviderManager dataProviderManager = DataProviderManager.instance();
	                if ((this.inputRefDP != null) && (this.inputRefDP.length() > 0)) {
	                    IDataProvider dataProvider = dataProviderManager.getDataProvider(this.inputRefDP);
	                    try {
	                        logger.debug("Working on Input data provider: " + dataProvider);
	                        dataProvider.setObject(internalData);
	                        Object inputCall = dataProvider.getResult();
	                        internalData.setObject(inputCall);
	                    }
	                    finally {
	                        dataProviderManager.releaseDataProvider(this.inputRefDP, dataProvider);
	                    }
	                }
	                internalData = this.inputServices.perform(internalData);
	                internalData = this.subFlow.perform(internalData, onDebug);
	                internalData = this.outputServices.perform(internalData);
	                if ((this.outputRefDP != null) && (this.outputRefDP.length() > 0)) {
	                    IDataProvider dataProvider = dataProviderManager.getDataProvider(this.outputRefDP);
	                    try {
	                        logger.debug("Working on Output data provider: " + dataProvider);
	                        dataProvider.setObject(internalData);
	                        Object outputCall = dataProvider.getResult();
	                        internalData.setObject(outputCall);
	                    }
	                    finally {
	                        dataProviderManager.releaseDataProvider(this.outputRefDP, dataProvider);
	                    }
	                }
	            }
	            finally {
	                NMDC.pop();
	            }
	            environment.put(output, internalData);
	            if (logger.isDebugEnabled() || isDumpInOut()) {
	                logger.info(GVFormatLog.formatOUTPUT(internalData, false, false));
	            }
	        }
	        catch (InterruptedException exc) {
	            logger.error("GVSubFlowCallNode [" + getId() + "] interrupted!", exc);
	            throw exc;
	        }
	        catch (Exception exc) {
                logger.error("Error in GVSubFlowCallNode[" + getId() + "]", exc);
                if (this.isDumpEnvOnError()) {
                    dumpEnvironment(Level.ERROR, true, environment);
                }
                else {
                    if (!(logger.isDebugEnabled() || isDumpInOut())) {
                        logger.error(GVFormatLog.formatINPUT(data, true, false));
                    }
                }
	            environment.put(output, exc);
	        }

	        String nextNodeId = "";
	        String conditionName = "";
	        int i = 0;
	        Throwable lastException = (Throwable) environment.get(GVNodeCheck.LAST_GV_EXCEPTION);
	        Object outputObject = environment.get(output);

	        try {
	            while ((i < this.routingVector.size()) && nextNodeId.equals("")) {
	                GVRouting routing = this.routingVector.elementAt(i);
	                nextNodeId = routing.getNodeId(output, environment);
	                conditionName = routing.getConditionName();
	                i++;
	            }
	        }
	        catch (Exception exc) {
	            logger.error("Exception caught while checking routing condition - GVSubFlowCallNode '" + getId() + "'", exc);
	            nextNodeId = this.onExceptionId;
	            lastException = exc;
	            conditionName = "EXCEPTION";
                if (this.isDumpEnvOnError()) {
                    dumpEnvironment(Level.ERROR, true, environment);
                }
                else {
                    if (!(logger.isDebugEnabled() || isDumpInOut())) {
                        logger.error(GVFormatLog.formatINPUT(data, true, false));
                    }
                }
	        }

	        if (nextNodeId.equals("")) {
	            if (!Throwable.class.isInstance(outputObject)) {
	                if (this.defaultId.equals("")) {
	                    lastException = new GVCoreConfException("GVCORE_BAD_ROUTING_CFG_ERROR", new String[][]{{"id",
	                            getId()}});
                        logger.error("Error in GVSubFlowCallNode[" + getId() + "]", lastException);
                        if (this.isDumpEnvOnError()) {
                            dumpEnvironment(Level.ERROR, true, environment);
                        }
                        else {
                            if (!(logger.isDebugEnabled() || isDumpInOut())) {
                                logger.error(GVFormatLog.formatINPUT(data, true, false));
                            }
                        }
	                    environment.put(output, lastException);
	                    nextNodeId = this.onExceptionId;
	                    conditionName = "EXCEPTION";
	                }
	                else {
	                    nextNodeId = this.defaultId;
	                    conditionName = "DEFAULT";
	                }
	            }
	            else {
	                nextNodeId = this.onExceptionId;
	                lastException = (Throwable) outputObject;
	                conditionName = "EXCEPTION";
	            }
	        }
	        environment.put(GVNodeCheck.LAST_GV_EXCEPTION, lastException);
	        logger.info("Executing GVSubFlowCallNode '" + getId() + "' - '" + conditionName + "' -> '" + nextNodeId + "'");

	        dumpEnvironment(false, environment);
	        long endTime = System.currentTimeMillis();
	        logger.info("END - Execute GVSubFlowCallNode '" + getId() + "' - ExecutionTime (" + (endTime - startTime) + ")");
	        return nextNodeId;
    	}
        finally {
        	if (!level.equals(Level.ALL)) {
        		GVLogger.setThreadMasterLevel(level);
        	}
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#getDefaultNextNodeId()
     */
    @Override
    public String getDefaultNextNodeId()
    {
        return this.defaultId;
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#cleanUp()
     */
    @Override
    public void cleanUp() throws GVCoreException
    {
        this.inputServices.cleanUp();
        this.outputServices.cleanUp();
        for (GVRouting r : this.routingVector) {
            r.cleanUp();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#destroy()
     */
    @Override
    public void destroy() throws GVCoreException
    {
        this.defNode = null;
        this.subFlow = null;
        this.inputServices = null;
        this.outputServices = null;
        this.routingVector.clear();
        if (this.subFlowMap != null) {
            Iterator<String> i = this.subFlowMap.keySet().iterator();
            while (i.hasNext()) {
                this.subFlowMap.get(i.next()).destroy();
            }
            this.subFlowMap.clear();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#getLogger()
     */
    @Override
    protected Logger getLogger() {
        return logger;
    }

    /**
     * @param defNode
     *        the flow node definition
     * @throws CoreConfigException
     *         if errors occurs
     */
    private void initSubFlow(Node defNode) throws GVCoreConfException
    {
        try {
            this.subFlowMap = new HashMap<String, GVSubFlow>();
            this.defNode = defNode;
            this.changeLogContext = XMLConfig.getBoolean(defNode, "@change-log-context", true);
            this.isSubFlowNameDynamic = XMLConfig.getBoolean(defNode, "@dynamic", false);
            logger.debug("isSubFlowNameDynamic  = " + this.isSubFlowNameDynamic);
            this.flowOp = XMLConfig.get(defNode, "@subflow");
            logger.debug("subflow  = " + this.flowOp);
            this.inputRefDP = XMLConfig.get(defNode, "@input-ref-dp", "");
            this.outputRefDP = XMLConfig.get(defNode, "@output-ref-dp", "");

            if (this.flowOp.equals("")) {
                throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'subflow'"},
                        {"node", XPathFinder.buildXPath(defNode)}});
            }

            createSubFlow(null);

            Node intSvcNode = XMLConfig.getNode(defNode, "InputServices");
            if (intSvcNode != null) {
                this.inputServices.init(intSvcNode, this, true);
            }
            intSvcNode = XMLConfig.getNode(defNode, "OutputServices");
            if (intSvcNode != null) {
                this.outputServices.init(intSvcNode, this, false);
            }
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_SUB_FLOW_SEARCH_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }
        catch (PropertiesHandlerException exc) {
            throw new GVCoreConfException("GVCORE_SUB_FLOW_SEARCH_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }
        catch (GVException exc) {
            throw new GVCoreConfException("GVCORE_SUB_FLOW_INIT_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }
    }

    /**
     * @param data
     * @throws XMLConfigException
     * @throws GVCoreConfException
     * @throws PropertiesHandlerException
     */
    private String createSubFlow(GVBuffer data) throws XMLConfigException, GVCoreConfException, PropertiesHandlerException {
        String localFlowOp = this.flowOp;
        if (this.isSubFlowNameDynamic) {
            if (data == null) {
                return localFlowOp;
            }
            Map<String, Object> props = GVBufferPropertiesHelper.getPropertiesMapSO(data, true);
            localFlowOp = PropertiesHandler.expand(localFlowOp, props, data);
            logger.debug("Calling SubFlow: " + localFlowOp);
        }
        this.subFlow = this.subFlowMap.get(localFlowOp);
        if (this.subFlow == null) {
            Node fNode = XMLConfig.getNode(this.defNode, "ancestor::Operation/SubFlow[@name='" + localFlowOp + "']");
            if (fNode == null) {
                throw new GVCoreConfException("GVCORE_INVALID_CFG_PARAM_ERROR", new String[][]{{"name", "'subflow'"},
                        {"subflow", localFlowOp}, {"node", XPathFinder.buildXPath(this.defNode)}});
            }

            this.subFlow = new GVSubFlow();
            this.subFlow.init(fNode, true);
            this.subFlowMap.put(localFlowOp, this.subFlow);
        }
        return localFlowOp;
    }

}

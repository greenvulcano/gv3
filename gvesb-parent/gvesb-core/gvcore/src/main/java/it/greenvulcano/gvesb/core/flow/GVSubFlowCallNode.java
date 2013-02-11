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
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
     * The SubFlow name to invoke.
     */
    private String              flowOp           = "";
    /**
     * The SubFlow instance.
     */
    private GVSubFlow           subFlow          = null;

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

        defaultId = XMLConfig.get(defNode, "@default-id", "");
        try {
            onExceptionId = XMLConfig.get(defNode, "@on-exception-id");
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
                routingVector.add(routing);
            }
        }

        if (defaultId.equals("") && (routingVector.size() == 0)) {
            throw new GVCoreConfException("GVCORE_BAD_ROUTING_CFG_ERROR", new String[][]{{"id", getId()}});
        }

        initSubFlow(defNode);
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#execute(java.util.Map)
     */
    @Override
    public String execute(Map<String, Object> environment) throws GVCoreException
    {
        GVBuffer internalData = null;
        String input = getInput();
        String output = getOutput();
        logger.info("Executing GVSubFlowCallNode '" + getId() + "'");
        dumpEnvironment(logger, true, environment);

        Object inData = environment.get(input);
        if (Throwable.class.isInstance(inData)) {
            environment.put(output, inData);
            logger.debug("END - Execute GVSubFlowCallNode '" + getId() + "' with Exception input -> " + onExceptionId);
            return onExceptionId;
        }
        try {
            GVBuffer data = (GVBuffer) inData;
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

                if (changeLogContext) {
                    NMDC.setOperation(flowOp);
                    GVBufferMDC.put(internalData);
                }
                DataProviderManager dataProviderManager = DataProviderManager.instance();
                if ((inputRefDP != null) && (inputRefDP.length() > 0)) {
                    IDataProvider dataProvider = dataProviderManager.getDataProvider(inputRefDP);
                    try {
                        logger.debug("Working on Input data provider: " + dataProvider.getClass());
                        dataProvider.setObject(internalData);
                        Object inputCall = dataProvider.getResult();
                        internalData.setObject(inputCall);
                    }
                    finally {
                        dataProviderManager.releaseDataProvider(inputRefDP, dataProvider);
                    }
                }
                data = subFlow.perform(internalData);
                if ((outputRefDP != null) && (outputRefDP.length() > 0)) {
                    IDataProvider dataProvider = dataProviderManager.getDataProvider(outputRefDP);
                    try {
                        logger.debug("Working on Output data provider: " + dataProvider.getClass());
                        dataProvider.setObject(data);
                        Object outputCall = dataProvider.getResult();
                        data.setObject(outputCall);
                    }
                    finally {
                        dataProviderManager.releaseDataProvider(outputRefDP, dataProvider);
                    }
                }
            }
            finally {
                NMDC.pop();
            }
            environment.put(output, data);
            if (logger.isDebugEnabled() || isDumpInOut()) {
                logger.info(GVFormatLog.formatOUTPUT(data, false, false));
            }
        }
        catch (Exception exc) {
            environment.put(output, exc);
        }

        String nextNodeId = "";
        String conditionName = "";
        int i = 0;
        Throwable lastException = (Throwable) environment.get(GVNodeCheck.LAST_GV_EXCEPTION);
        Object outputObject = environment.get(output);

        try {
            while ((i < routingVector.size()) && nextNodeId.equals("")) {
                GVRouting routing = routingVector.elementAt(i);
                nextNodeId = routing.getNodeId(output, environment);
                conditionName = routing.getConditionName();
                i++;
            }
        }
        catch (Exception exc) {
            logger.error("Exception caught while checking routing condition - GVSubFlowCallNode '" + getId() + "'", exc);
            nextNodeId = onExceptionId;
            lastException = exc;
            conditionName = "EXCEPTION";
        }

        if (nextNodeId.equals("")) {
            if (!Throwable.class.isInstance(outputObject)) {
                if (defaultId.equals("")) {
                    lastException = new GVCoreConfException("GVCORE_BAD_ROUTING_CFG_ERROR", new String[][]{{"id",
                            getId()}});
                    environment.put(output, lastException);
                    nextNodeId = onExceptionId;
                    conditionName = "EXCEPTION";
                }
                else {
                    nextNodeId = defaultId;
                    conditionName = "DEFAULT";
                }
            }
            else {
                nextNodeId = onExceptionId;
                lastException = (Throwable) outputObject;
                conditionName = "EXCEPTION";
            }
        }
        environment.put(GVNodeCheck.LAST_GV_EXCEPTION, lastException);
        logger.info("Executing GVSubFlowCallNode '" + getId() + "' - '" + conditionName + "' -> '" + nextNodeId + "'");

        dumpEnvironment(logger, false, environment);
        logger.debug("END - Execute GVSubFlowCallNode '" + getId() + "'");
        return nextNodeId;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#getDefaultNextNodeId()
     */
    @Override
    public String getDefaultNextNodeId()
    {
        return defaultId;
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#cleanUp()
     */
    @Override
    public void cleanUp() throws GVCoreException
    {
        for (GVRouting r : routingVector) {
            r.cleanUp();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#destroy()
     */
    @Override
    public void destroy() throws GVCoreException
    {
        routingVector.clear();
        if (subFlow != null) {
            subFlow.destroy();
        }
        subFlow = null;
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
            changeLogContext = XMLConfig.getBoolean(defNode, "@change-log-context", true);
            flowOp = XMLConfig.get(defNode, "@subflow");
            logger.debug("subflow  = " + flowOp);
            inputRefDP = XMLConfig.get(defNode, "@input-ref-dp", "");
            outputRefDP = XMLConfig.get(defNode, "@output-ref-dp", "");

            if (flowOp.equals("")) {
                throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'subflow'"},
                        {"node", XPathFinder.buildXPath(defNode)}});
            }

            Node fNode = XMLConfig.getNode(defNode, "ancestor::Operation/SubFlow[@name='" + flowOp + "']");
            if (fNode == null) {
                throw new GVCoreConfException("GVCORE_INVALID_CFG_PARAM_ERROR", new String[][]{{"name", "'operation'"},
                        {"node", XPathFinder.buildXPath(defNode)}});
            }
            subFlow = new GVSubFlow();
            subFlow.init(fNode, true);
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_SUB_FLOW_SEARCH_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }
        catch (GVException exc) {
            throw new GVCoreConfException("GVCORE_SUB_FLOW_SEARCH_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }
    }

}

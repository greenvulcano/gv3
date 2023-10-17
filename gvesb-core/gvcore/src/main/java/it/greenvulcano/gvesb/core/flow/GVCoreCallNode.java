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

import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.config.GVServiceConf;
import it.greenvulcano.gvesb.core.config.InvocationContext;
import it.greenvulcano.gvesb.core.config.ServiceConfigManager;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.exc.GVCoreSecurityException;
import it.greenvulcano.gvesb.core.jmx.ServiceOperationInfo;
import it.greenvulcano.gvesb.core.jmx.ServiceOperationInfoManager;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.identity.GVIdentityHelper;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.gvesb.policy.ACLManager;
import it.greenvulcano.gvesb.policy.impl.GVCoreServiceKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xpath.XPathFinder;

/**
 * GVOperationNode class.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class GVCoreCallNode extends GVFlowNode
{
    private static final Logger logger                = GVLogger.getLogger(GVCoreCallNode.class);
    /**
     * the next flow node id
     */
    private String              nextNodeId            = "";
    /**
     * The id_system for the flow to invoke.
     */
    private String              system                = "";
    /**
     * The id_service for the flow to invoke.
     */
    private String              service               = "";
    /**
     * The operation for the flow to invoke.
     */
    private String              flowOp                = "";
    /**
     * If true overwrite the input id_system and id_service.
     */
    private boolean             isFlowSysSvcSet       = false;
    /**
     * If true the input id_system, id_service and operation are handled as
     * metadata and resolved at runtime.
     */
    private boolean             isFlowSysSvcOpDynamic = false;
    /**
     * If true update the log context.
     */
    private boolean             changeLogContext      = false;
    /**
     * If true update the log master service file.
     */
    private boolean             changeLogMasterService = false;
    /**
     * GVBuffer instance to be used only for accessing to ServiceConfigManager.
     */
    private GVBuffer            flowGVBuffer          = null;

    private String              inputRefDP            = null;
    private String              outputRefDP           = null;
    /**
     * the input services
     */
    private GVInternalServiceHandler inputServices    = new GVInternalServiceHandler();
    /**
     * the output services
     */
    private GVInternalServiceHandler outputServices   = new GVInternalServiceHandler();

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node defNode) throws GVCoreConfException
    {
        super.init(defNode);

        this.nextNodeId = XMLConfig.get(defNode, "@next-node-id", "");
        if (this.nextNodeId.equals("")) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'next-node-id'"},
                    {"node", XPathFinder.buildXPath(defNode)}});
        }

        initNode(defNode);
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#cleanUp()
     */
    @Override
    public void cleanUp() throws GVCoreException
    {
        this.inputServices.cleanUp();
        this.outputServices.cleanUp();
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#destroy()
     */
    @Override
    public void destroy() throws GVCoreException
    {
        this.inputServices = null;
        this.outputServices = null;
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
            GVServiceConf gvsConfig = null;
            String localSystem = null;
            String localService = null;
            String localFlowOp = null;
            String localId = null;
            boolean localSuccess = false;
	        logger.info("Executing GVCoreCallNode '" + getId() + "'");
	        checkInterrupted("GVCoreCallNode");
	        dumpEnvironment(true, environment);

	        Object inData = environment.get(input);
	        if (Throwable.class.isInstance(inData)) {
	            environment.put(output, inData);
	            logger.info("END - Skip Execute GVCoreCallNode '" + getId() + "'");
	            return this.nextNodeId;
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
	            String origSystem = internalData.getSystem();
	            String origService = internalData.getService();
	            logger.debug("origSystem  = " + origSystem);
	            logger.debug("origService = " + origService);

	            localSystem = (GVBuffer.DEFAULT_SYS.equals(this.system) ? origSystem : this.system);
	            localService = this.service;
	            localFlowOp = this.flowOp;

	            if (this.isFlowSysSvcOpDynamic) {
	                Map<String, Object> props = GVBufferPropertiesHelper.getPropertiesMapSO(internalData, true);
	                localSystem = PropertiesHandler.expand(localSystem, props, internalData);
	                localService = PropertiesHandler.expand(localService, props, internalData);
	                this.flowGVBuffer.setService(localService);
	                this.flowGVBuffer.setSystem(localSystem);
	                localFlowOp = PropertiesHandler.expand(localFlowOp, props, internalData);
	            }
	            InvocationContext gvCtx = (InvocationContext) it.greenvulcano.gvesb.internal.InvocationContext.getInstance();
	            ServiceConfigManager svcMgr = gvCtx.getGVServiceConfigManager();
	            gvsConfig = svcMgr.getGVSConfig(this.flowGVBuffer);
	            if (!ACLManager.canAccess(new GVCoreServiceKey(gvsConfig.getGroupName(), gvsConfig.getServiceName(),
	                    localFlowOp))) {
	                throw new GVCoreSecurityException("GV_SERVICE_POLICY_ERROR", new String[][]{
	                        {"service", this.flowGVBuffer.getService()}, {"system", this.flowGVBuffer.getSystem()},
	                        {"id", this.flowGVBuffer.getId().toString()}, {"user", GVIdentityHelper.getName()}});
	            }
	            GVFlow gvOp = gvsConfig.getGVOperation(this.flowGVBuffer, localFlowOp);

	            try {
	                NMDC.push();

	                if (this.isFlowSysSvcSet) {
	                    internalData.setService(localService);
	                    internalData.setSystem(localSystem);
	                }

	                if (this.changeLogContext) {
	                    GVBufferMDC.put(internalData);
	                    NMDC.setOperation(localFlowOp);
	                    NMDC.put(GVBuffer.Field.SERVICE.toString(), localService);
	                    NMDC.put(GVBuffer.Field.SYSTEM.toString(), localSystem);
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
	                localId = internalData.getId().toString();
	                internalData = this.inputServices.perform(internalData);
	            	String masterService = null;
	            	Level locLevel = null;
	                try {
	                	if (this.changeLogMasterService) {
	                		masterService = GVBufferMDC.changeMasterService(localService);
	                	}
	               		locLevel = GVLogger.setThreadMasterLevel(gvOp.getLoggerLevel());

	                    internalData = gvOp.perform(internalData, onDebug);
	                }
	                finally {
	                	GVLogger.removeThreadMasterLevel(locLevel);
	                    if (this.changeLogMasterService) {
	                		GVBufferMDC.changeMasterService(masterService);
	                	}
	                }
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
	                if (this.isFlowSysSvcSet) {
	                    internalData.setSystem(origSystem);
	                    internalData.setService(origService);
	                }
	            }
	            localSuccess = true;
	            environment.put(output, internalData);
	            if (logger.isDebugEnabled() || isDumpInOut()) {
	                logger.info(GVFormatLog.formatOUTPUT(internalData, false, false));
	            }
	        }
	        catch (InterruptedException exc) {
	            logger.error("GVCoreCallNode [" + getId() + "] interrupted!", exc);
	            throw exc;
	        }
	        catch (Exception exc) {
                logger.error("Error in GVCoreCallNode[" + getId() + "]", exc);
                if (this.isDumpEnvOnError()) {
                    dumpEnvironment(Level.ERROR, true, environment);
                }
                else {
                    if (!(logger.isDebugEnabled() || isDumpInOut())) {
                        logger.error(GVFormatLog.formatINPUT((GVBuffer) inData, true, false));
                    }
                }
	            environment.put(output, exc);
	        }

            if (gvsConfig != null) {
            	try {
            		ServiceOperationInfo serviceInfo = ServiceOperationInfoManager.instance().getServiceOperationInfo(
                        gvsConfig.getServiceName(), true);
            		serviceInfo.flowTerminated(localFlowOp, localId, localSuccess);
            	}
            	catch(Exception exc) {
            		logger.error("Error cleaning-up Flow JMX status", exc);
            	}
            }

	        dumpEnvironment(false, environment);
	        long endTime = System.currentTimeMillis();
	        logger.info("END - Execute GVCoreCallNode '" + getId() + "' - ExecutionTime (" + (endTime - startTime) + ")");
	        return this.nextNodeId;
        }
        finally {
        	if (isDumpInOut() && !level.equals(Level.ALL)) {
        		GVLogger.setThreadMasterLevel(level);
        	}
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#getDefaultNextNodeId()
     */
    @Override
    public String getDefaultNextNodeId()
    {
        return this.nextNodeId;
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
    private void initNode(Node defNode) throws GVCoreConfException
    {
        this.changeLogContext = XMLConfig.getBoolean(defNode, "@change-log-context", true);
        this.changeLogMasterService = this.changeLogContext && XMLConfig.getBoolean(defNode, "@change-log-master-service", false);
        try {
            this.system = XMLConfig.get(defNode, "@id-system", GVBuffer.DEFAULT_SYS);
            logger.debug("system  = " + this.system);
            this.service = XMLConfig.get(defNode, "@id-service");
            logger.debug("service = " + this.service);
            this.flowOp = XMLConfig.get(defNode, "@operation");
            logger.debug("flowOp  = " + this.flowOp);
            this.isFlowSysSvcOpDynamic = XMLConfig.getBoolean(defNode, "@dynamic", false);
            logger.debug("isFlowSysSvcOpDynamic  = " + this.isFlowSysSvcOpDynamic);
            this.isFlowSysSvcSet = XMLConfig.getBoolean(defNode, "@overwrite-sys-svc", false);
            logger.debug("isFlowSysSvcSet = " + this.isFlowSysSvcSet);
            this.flowGVBuffer = new GVBuffer(this.system, this.service);
            this.outputRefDP = XMLConfig.get(defNode, "@output-ref-dp", "");
            this.inputRefDP = XMLConfig.get(defNode, "@input-ref-dp", "");

            if (this.service.equals("")) {
                throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'id-service'"},
                        {"node", XPathFinder.buildXPath(defNode)}});
            }
            if (this.flowOp.equals("")) {
                throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'operation'"},
                        {"node", XPathFinder.buildXPath(defNode)}});
            }

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
            throw new GVCoreConfException("GVCORE_VCL_OPERATION_SEARCH_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}});
        }
        catch (GVException exc) {
            throw new GVCoreConfException("GVCORE_VCL_OPERATION_SEARCH_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}});
        }
    }
}

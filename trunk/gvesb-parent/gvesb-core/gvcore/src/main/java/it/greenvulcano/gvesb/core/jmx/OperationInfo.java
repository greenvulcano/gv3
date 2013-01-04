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
package it.greenvulcano.gvesb.core.jmx;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.JMXUtils;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.management.DomainAction;
import it.greenvulcano.util.Stats;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * OperationInfo class.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 */
public class OperationInfo
{
    private static Logger                    logger                  = GVLogger.getLogger(OperationInfo.class);

    /**
     * the object JMX descriptor
     */
    public static final String               DESCRIPTOR_NAME         = "OperationInfo";
    /**
     * the status of the currently running associated flow, by ID and Thread
     */
    private Map<String, Map<String, String>> flowsStatusMap          = new ConcurrentHashMap<String, Map<String, String>>();
    /**
     * the operation name
     */
    private String                           op                      = "";
    /**
     * the total successful invocation
     */
    private long                             totalSuccess            = 0;
    /**
     * the total failed invocation
     */
    private long                             totalFailure            = 0;
    /**
     * the activation flag
     */
    private boolean                          opActivation            = true;
    /**
     * the service activation flag
     */
    private boolean                          serviceActivation       = true;
    /**
     * the previous activation flag
     */
    private boolean                          oldActivation           = true;
    /**
     * the jmx filter for inter-instances communication
     */
    private String                           jmxFilter               = "";
    /**
     * if tru the instance runs on administration/support server
     */
    private boolean                          isAdministrator         = false;
    /**
     * If true the instance call the administration server for objects
     * initialization
     */
    private boolean                          callAdministratorOnInit = false;
    /**
     * The Object to calculate the GreenVulcano throughput
     */
    private static Stats                     statNodes               = null;
    /**
     * The Object to calculate the Operation failures throughput
     */
    private Stats                            statFailures            = null;
    private int                              maxFailuresRateo        = Integer.MAX_VALUE;
    private DomainAction                     failureAction           = null;
    private DomainAction                     enableAction            = null;
    private DomainAction                     disableAction           = null;

    /*
     * DEBUG FIELDS
     */

    private enum BreakpointType {
        PERSISTENT, TEMPORARY, REMOVED
    }

    /**
     * If true, the next flow is executed in debug mode.
     */
    private boolean                                               markForDebug             = false;
    private String                                                lastIDOnDebug            = null;
    /**
     * The list of ID/Thread in debug mode.
     */
    private Map<String, Set<String>>                              onDebugIDs               = new HashMap<String, Set<String>>();
    /**
     * The flow's nodes history of the currently debugging associated flows.
     */
    private Map<String, Map<String, List<String>>>                debugFlowsHistoryMap     = new ConcurrentHashMap<String, Map<String, List<String>>>();
    /**
     * The execution environments of the currently debugging associated flows.
     */
    private Map<String, Map<String, Map<String, Object>>>         debugFlowsEnvironmentMap = new ConcurrentHashMap<String, Map<String, Map<String, Object>>>();
    /**
     * The next flow node before which suspend execution of the currently
     * debugging associated flows.
     */
    private Map<String, Map<String, Map<String, BreakpointType>>> debugFlowsStopNodeMap    = new ConcurrentHashMap<String, Map<String, Map<String, BreakpointType>>>();


    /**
     * Static initializer
     */
    static {
        // perform calculation with: 1 sec, 1 sec, 30 sec
        statNodes = new Stats(1000, 1000, 30);
    }

    /**
     * Constructor
     * 
     * @param oper
     *        the operation name
     * @param jmxSrvcKey
     *        the holding service jmx key
     */
    public OperationInfo(String oper, String jmxSrvcKey)
    {
        op = oper;
        jmxFilter = "GreenVulcano:*,Component=" + DESCRIPTOR_NAME + jmxSrvcKey + ",IDOperation=" + op;
        statFailures = new Stats(1000, 1000, 1);
    }

    /**
     * Initialize the instance
     * 
     * @param initData
     *        initialization data
     */
    public void init(Map<String, Object> initData)
    {
        if (initData == null) {
            return;
        }
        failureAction = (DomainAction) initData.get("failureAction");
        enableAction = (DomainAction) initData.get("enableAction");
        disableAction = (DomainAction) initData.get("disableAction");
        Integer integer = (Integer) initData.get("failureRateo");
        if (integer != null) {
            maxFailuresRateo = integer.intValue();
        }
        else {
            maxFailuresRateo = Integer.MAX_VALUE;
        }
        setOperationActivation(((Boolean) initData.get("operationActivation")).booleanValue());
    }

    /**
     * Register the instance on JMX server
     * 
     * @param properties
     *        the object name properties
     * @param register
     *        the registration flag
     * @throws Exception
     *         if errors occurs
     */
    public void register(Map<String, String> properties, boolean register) throws Exception
    {
        if (register) {
            String key = properties.get("IDService") + ":" + properties.get("IDGroup") + "#" + op;
            properties = getJMXProperties(properties);
            deregister(properties);
            JMXEntryPoint jmx = JMXEntryPoint.instance();

            logger.debug("Adding " + DESCRIPTOR_NAME + " for " + key);
            jmx.registerObject(this, DESCRIPTOR_NAME, properties);
            logger.debug("Adding " + DESCRIPTOR_NAME + "_Internal for " + key);
            jmx.registerObject(this, DESCRIPTOR_NAME + "_Internal", properties);
        }
    }

    /**
     * Deregister the instance from JMX server
     * 
     * @param properties
     *        the object name properties
     * @throws Exception
     *         if errors occurs
     */
    public void deregister(Map<String, String> properties) throws Exception
    {
        properties = getJMXProperties(properties);
        JMXEntryPoint jmx = JMXEntryPoint.instance();
        try {
            jmx.unregisterObject(this, DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            // do nothing
        }
        try {
            jmx.unregisterObject(this, DESCRIPTOR_NAME + "_Internal", properties);
        }
        catch (Exception exc) {
            // do nothing
        }
    }

    /**
     * Return the instance properties
     * 
     * @param properties
     *        properties list to enrich
     * @param full
     *        if true insert also status data
     * @return the property list
     */
    public Map<String, Object> getProperties(Map<String, Object> properties, boolean full)
    {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        properties.put("IDOperation", op);
        if (full) {
            properties.put("operationActivation", new Boolean(opActivation));
            if (failureAction != null) {
                properties.put("failureAction", failureAction);
                properties.put("failureRateo", new Integer(maxFailuresRateo));
            }
            if (enableAction != null) {
                properties.put("enableAction", enableAction);
            }
            if (disableAction != null) {
                properties.put("disableAction", disableAction);
            }
        }
        return properties;
    }

    private Map<String, String> getJMXProperties(Map<String, String> properties)
    {
        if (properties == null) {
            properties = new HashMap<String, String>();
        }
        properties.put("IDOperation", op);
        return properties;
    }

    /**
     * @param node
     *        the node from wich read configuration data
     * @throws Exception
     *         if errors occurs
     */
    public void synchronizeStatus(Node node) throws Exception
    {
        setOperationActivation(XMLConfig.getBoolean(node, "@operationActivation", true));
    }

    /**
     * @param initData
     *        the hashtable from wich read configuration data
     * @throws Exception
     *         if errors occurs
     */
    public void synchronizeStatus(Map<String, Object> initData) throws Exception
    {
        init(initData);
    }

    /**
     * @return the total successfull invocations
     */
    public long getTotalSuccess()
    {
        return totalSuccess;
    }

    /**
     * @return the total failed invocations
     */
    public long getTotalFailure()
    {
        return totalFailure;
    }

    /**
     * @return the total invocations
     */
    public long getTotal()
    {
        return (totalSuccess + totalFailure);
    }

    /**
     * reset the internal counter
     */
    public void resetCounter()
    {
        totalSuccess = 0;
        totalFailure = 0;
    }

    /**
     * Return a matrix of Id/GVFlowNode id for the associated flow status, by ID
     * and Thread
     * 
     * @return the current associated flow status
     */
    public Map<String, Map<String, String>> getFlowsStatus()
    {
        return Collections.unmodifiableMap(flowsStatusMap);
    }

    /**
     * Set the status of an associated flow
     * 
     * @param flowId
     *        the flow id
     * @param id
     *        the flow node id
     */
    public void setFlowStatus(String flowId, String id)
    {
        statNodes.hint();
        synchronized (flowsStatusMap) {
            Map<String, String> thFlows = flowsStatusMap.get(flowId);
            if (thFlows == null) {
                thFlows = new ConcurrentHashMap<String, String>();
                flowsStatusMap.put(flowId, thFlows);
            }
            String threadName = Thread.currentThread().getName();
            thFlows.put(threadName, id);
            getFlowHistory(threadName, flowId).add(id);
        }
    }

    /**
     * Gets the status of an associated flow
     * 
     * @param flowId
     *        the flow id
     * @return the flow node id
     */
    public String getFlowStatus(String threadName, String flowId)
    {
        synchronized (flowsStatusMap) {
            Map<String, String> thFlows = flowsStatusMap.get(flowId);
            if (thFlows == null) {
                thFlows = new ConcurrentHashMap<String, String>();
                flowsStatusMap.put(flowId, thFlows);
            }
            return thFlows.get(threadName);
        }
    }

    /**
     * Mark an associated flow as terminated
     * 
     * @param flowId
     *        the flow id
     * @param success
     *        the termination status
     */
    public void flowTerminated(String flowId, boolean success)
    {
        if (success) {
            totalSuccess++;
        }
        else {
            statFailures.hint();
            totalFailure++;
            execFailureAction();
        }
        String threadName = Thread.currentThread().getName();
        synchronized (flowsStatusMap) {
            Map<String, String> thFlows = flowsStatusMap.get(flowId);
            if (thFlows != null) {
                thFlows.remove(threadName);
                if (thFlows.isEmpty()) {
                    flowsStatusMap.remove(flowId);
                }
            }
        }

        // DEBUG
        synchronized (debugFlowsHistoryMap) {
            Map<String, List<String>> thFlows = debugFlowsHistoryMap.get(flowId);
            if (thFlows != null) {
                thFlows.remove(threadName);
                if (thFlows.isEmpty()) {
                    debugFlowsHistoryMap.remove(flowId);
                }
            }
        }
        synchronized (debugFlowsEnvironmentMap) {
            Map<String, Map<String, Object>> thFlows = debugFlowsEnvironmentMap.get(flowId);
            if (thFlows != null) {
                thFlows.remove(threadName);
                if (thFlows.isEmpty()) {
                    debugFlowsEnvironmentMap.remove(flowId);
                }
            }
        }
        synchronized (debugFlowsStopNodeMap) {
            Map<String, Map<String, BreakpointType>> thFlows = debugFlowsStopNodeMap.get(flowId);
            if (thFlows != null) {
                thFlows.remove(threadName);
                if (thFlows.isEmpty()) {
                    debugFlowsStopNodeMap.remove(flowId);
                }
            }
        }
        synchronized (onDebugIDs) {
            onDebugIDs.remove(flowId);
            Set<String> thFlows = onDebugIDs.get(flowId);
            if (thFlows != null) {
                thFlows.remove(threadName);
                if (thFlows.isEmpty()) {
                    onDebugIDs.remove(flowId);
                }
            }
        }
    }

    private void execFailureAction()
    {
        if ((failureAction != null) && (statFailures.getThroughput() > maxFailuresRateo)) {
            statFailures.reset();
            try {
                Object[] params = new Object[]{failureAction};
                String[] signature = new String[]{"it.greenvulcano.gvesb.management.DomainAction"};
                JMXUtils.invoke("*:*,Type=DomainManager", "executeDomainAction", params, signature, true, logger);
            }
            catch (Exception exc) {
                logger.error("Error occurred executing FailureAction", exc);
            }
        }
    }

    private void execEnableAction()
    {
        if ((enableAction != null) && (oldActivation != (opActivation && serviceActivation))) {
            oldActivation = (opActivation && serviceActivation);
            try {
                Object[] params = new Object[]{enableAction};
                String[] signature = new String[]{"it.greenvulcano.gvesb.management.DomainAction"};
                JMXUtils.invoke("*:*,Type=DomainManager", "executeDomainAction", params, signature, true, logger);
            }
            catch (Exception exc) {
                logger.error("Error occurred executing EnableAction", exc);
            }
        }
    }

    private void execDisableAction()
    {
        if ((disableAction != null) && (oldActivation != (opActivation && serviceActivation))) {
            oldActivation = (opActivation && serviceActivation);
            try {
                Object[] params = new Object[]{disableAction};
                String[] signature = new String[]{"it.greenvulcano.gvesb.management.DomainAction"};
                JMXUtils.invoke("*:*,Type=DomainManager", "executeDomainAction", params, signature, true, logger);
            }
            catch (Exception exc) {
                logger.error("Error occurred executing DisableAction", exc);
            }
        }
    }

    /**
     * @return the operation name
     */
    public String getOperation()
    {
        return op;
    }

    /**
     * @return the activation flag
     */
    public boolean getOperationActivation()
    {
        return opActivation;
    }

    /**
     * @param operActivation
     *        the activation flag value
     */
    public void setOperationActivation(boolean operActivation)
    {
        opActivation = operActivation;
        if (operActivation) {
            execEnableAction();
        }
        else {
            execDisableAction();
        }
    }

    /**
     * @param sActivation
     *        the activation flag value
     */
    public void setServiceActivation(boolean sActivation)
    {
        serviceActivation = sActivation;
        if (sActivation) {
            execEnableAction();
        }
        else {
            execDisableAction();
        }
    }

    /**
     * @return true if the instance run on administrator/support server
     */
    public boolean isAdministrator()
    {
        return isAdministrator;
    }

    /**
     * @param isAdmin
     *        the flag value
     */
    public void setAdministrator(boolean isAdmin)
    {
        isAdministrator = isAdmin;
    }

    /**
     * @return True if the instance can call the Administration Server on
     *         objects initialization
     */
    public boolean canCallAdministratorOnInit()
    {
        return callAdministratorOnInit;
    }

    /**
     * @param call
     *        If true the instance can call the Administration Server on object
     *        initialization
     */
    public void setCallAdministratorOnInit(boolean call)
    {
        callAdministratorOnInit = call;
    }

    /**
     * Set the activation status at true for the given operation on every server
     * 
     * @throws Exception
     *         if errors occurs
     */
    public void on() throws Exception
    {
        JMXUtils.set(jmxFilter, "operationActivation", new Boolean(true), false, logger);
    }

    /**
     * Set the activation status at false for the given operation on every
     * server
     * 
     * @throws Exception
     *         if errors occurs
     */
    public void off() throws Exception
    {
        JMXUtils.set(jmxFilter, "operationActivation", new Boolean(false), false, logger);
    }

    /**
     * Get the history average throughput for Nodes.
     * 
     * @return the history average throughput value
     */
    public static float getHistoryThroughputNod()
    {
        return statNodes.getHistoryThroughput();
    }

    /**
     * Get the maximum throughput for Nodes.
     * 
     * @return the maximum throughput value
     */
    public static float getMaxThroughputNod()
    {
        return statNodes.getMaxThroughput();
    }

    /**
     * Get the minimum average throughput for Nodes.
     * 
     * @return minimum average throughput value
     */
    public static float getMinThroughputNod()
    {
        return statNodes.getMinThroughput();
    }

    /**
     * Get the throughput for Nodes.
     * 
     * @return throughput value
     */
    public static float getThroughputNod()
    {
        return statNodes.getThroughput();
    }

    /**
     * Get the total hints for Nodes.
     * 
     * @return total hints value
     */
    public static long getTotalHintsNod()
    {
        return statNodes.getTotalHints();
    }


    /*
     * DEBUG FUNCTIONS
     */

    /**
     * Mark the new flow ID as on debug. Wait till the next flow start in debug
     * mode.
     * 
     * @param flowId
     * @return the new flow ID
     */
    public String markForDebug()
    {
        lastIDOnDebug = null;
        markForDebug = true;
        synchronized (this) {
            try {
                this.wait(60000);
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        markForDebug = false;
        return lastIDOnDebug;
    }

    /**
     * @return the markForDebug
     */
    public boolean isMarkForDebug()
    {
        return markForDebug;
    }

    /**
     * Mark a flowId as on debug.
     * 
     * @param flowId
     */
    public void startDebug(String threadName, String flowId)
    {
        markForDebug = false;
        synchronized (onDebugIDs) {
            Set<String> thFlows = onDebugIDs.get(flowId);
            if (thFlows == null) {
                thFlows = new HashSet<String>();
                onDebugIDs.put(flowId, thFlows);
            }
            thFlows.add(threadName);
        }
        lastIDOnDebug = flowId;
        synchronized (this) {
            this.notifyAll();
        }
    }

    /**
     * Get the list of on debug flow's ID and threads.
     */
    public Map<String, Set<String>> getOnDebugIDs()
    {
        return Collections.unmodifiableMap(onDebugIDs);
    }

    /**
     * Return a navigable list of flowId's flow nodes, from the given Thread,
     * ordered by execution sequence.
     * 
     * @param flowId
     * @return a navigable list of flowId's flow nodes
     */
    public List<String> getFlowHistory(String threadName, String flowId)
    {
        synchronized (debugFlowsHistoryMap) {
            Map<String, List<String>> thFlows = debugFlowsHistoryMap.get(flowId);
            if (thFlows == null) {
                thFlows = new HashMap<String, List<String>>();
                debugFlowsHistoryMap.put(flowId, thFlows);
            }
            List<String> fh = thFlows.get(threadName);
            if (fh == null) {
                fh = new LinkedList<String>();
                thFlows.put(threadName, fh);
            }
            return fh;
        }
    }

    /**
     * @param flowId
     * @param environment
     */
    public void setFlowEnvironment(String threadName, String flowId, Map<String, Object> environment)
    {
        synchronized (debugFlowsEnvironmentMap) {
            Map<String, Map<String, Object>> thFlows = debugFlowsEnvironmentMap.get(flowId);
            if (thFlows == null) {
                thFlows = new HashMap<String, Map<String, Object>>();
                debugFlowsEnvironmentMap.put(flowId, thFlows);
            }
            thFlows.put(threadName, environment);
        }
    }

    /**
     * @param flowId
     * @param environment
     */
    private Map<String, Object> getFlowEnvironment(String threadName, String flowId)
    {
        synchronized (debugFlowsEnvironmentMap) {
            Map<String, Map<String, Object>> thFlows = debugFlowsEnvironmentMap.get(flowId);
            if (thFlows != null) {
                return thFlows.get(threadName);
            }
        }
        return null;
    }

    /**
     * Return the keys list of flowId's flow execution environment.
     * 
     * @param flowId
     * @return
     */
    public Set<String> getEnvEntryKeys(String threadName, String flowId)
    {
        Map<String, Object> fe = getFlowEnvironment(threadName, flowId);
        if (fe != null) {
            return Collections.unmodifiableSet(fe.keySet());
        }
        return Collections.unmodifiableSet(new HashSet<String>());
    }

    /**
     * Return the flowId's flow execution environment key's associated value.
     * 
     * @param flowId
     * @param key
     * @return
     */
    public Object getEnvEntry(String threadName, String flowId, String key)
    {
        Map<String, Object> fe = getFlowEnvironment(threadName, flowId);
        if (fe != null) {
            return fe.get(key);
        }
        return null;
    }

    /**
     * Set in the id's flow execution environment the key's associated value.
     * 
     * @param flowId
     * @param key
     * @param value
     */
    public void setEnvEntry(String threadName, String flowId, String key, Object value)
    {
        Map<String, Object> fe = getFlowEnvironment(threadName, flowId);
        if (fe != null) {
            fe.put(key, value);
        }
    }

    /**
     * Return true if the flow flowId must suspend execution on node nodeId.
     * 
     * @param flowId
     * @param nodeId
     * @return
     */
    public boolean mustStop(String threadName, String flowId, String nodeId)
    {
        boolean res = true;
        Map<String, Map<String, BreakpointType>> thFlows = debugFlowsStopNodeMap.get(flowId);
        if (thFlows != null) {
            Map<String, BreakpointType> sn = thFlows.get(threadName);
            if (sn != null) {
                BreakpointType bt = sn.get(nodeId);
                if (bt != null) {
                    switch (bt) {
                        case PERSISTENT :
                            res = true;
                            break;
                        case TEMPORARY :
                            sn.put(nodeId, BreakpointType.REMOVED);
                            res = true;
                            break;
                        default :
                            res = false;
                    }
                }
                else {
                    res = false;
                }
            }
        }
        return res;
    }

    /**
     * Execute the flowId's flow next node.
     * 
     * @param flowId
     */
    public void step(String threadName, String flowId)
    {
        synchronized (debugFlowsStopNodeMap) {
            Map<String, Map<String, BreakpointType>> thFlows = debugFlowsStopNodeMap.get(flowId);
            if (thFlows != null) {
                thFlows.remove(threadName);
            }
            Map<String, Object> fe = getFlowEnvironment(threadName, flowId);
            if (fe != null) {
                synchronized (fe) {
                    fe.notifyAll();
                }
            }
        }
    }

    /**
     * Resume normal execution of the flowId's flow.
     * 
     * @param flowId
     */
    public void resume(String threadName, String flowId)
    {
        Map<String, Map<String, BreakpointType>> thFlows = debugFlowsStopNodeMap.get(flowId);
        if (thFlows == null) {
            thFlows = new HashMap<String, Map<String, BreakpointType>>();
            debugFlowsStopNodeMap.put(flowId, thFlows);
            thFlows.put(threadName, new HashMap<String, BreakpointType>());
        }
        Map<String, Object> fe = getFlowEnvironment(threadName, flowId);
        if (fe != null) {
            synchronized (fe) {
                fe.notifyAll();
            }
        }
    }

    /**
     * Sets a breakpoint on node nodeId.
     * 
     * @param threadName
     * @param flowId
     * @param nodeId
     */
    public void setBreakpoint(String threadName, String flowId, String nodeId)
    {
        Map<String, Map<String, BreakpointType>> thFlows = debugFlowsStopNodeMap.get(flowId);
        if (thFlows == null) {
            thFlows = new HashMap<String, Map<String, BreakpointType>>();
            debugFlowsStopNodeMap.put(flowId, thFlows);
        }
        Map<String, BreakpointType> nodeMap = thFlows.get(threadName);
        if (nodeMap == null) {
            nodeMap = new HashMap<String, BreakpointType>();
            thFlows.put(threadName, nodeMap);
        }
        nodeMap.put(nodeId, BreakpointType.PERSISTENT);
        System.out.println("set breakpoint: " + nodeId);
    }

    /**
     * Clears a breakpoint on node nodeId.
     * 
     * @param threadName
     * @param flowId
     * @param nodeId
     */
    public void clearBreakpoint(String threadName, String flowId, String nodeId)
    {
        Map<String, Map<String, BreakpointType>> thFlows = debugFlowsStopNodeMap.get(flowId);
        if (thFlows == null) {
            thFlows = new HashMap<String, Map<String, BreakpointType>>();
            debugFlowsStopNodeMap.put(flowId, thFlows);
        }
        Map<String, BreakpointType> nodeMap = thFlows.get(threadName);
        if (nodeMap == null) {
            nodeMap = new HashMap<String, BreakpointType>();
            thFlows.put(threadName, nodeMap);
        }
        nodeMap.put(nodeId, BreakpointType.REMOVED);
    }

    /**
     * Resume normal execution of the flowId's flow, till node nodeId, then
     * suspend execution.
     * 
     * @param flowId
     * @param nodeId
     */
    public void resumeTo(String threadName, String flowId, String nodeId)
    {
        Map<String, Map<String, BreakpointType>> thFlows = debugFlowsStopNodeMap.get(flowId);
        if (thFlows == null) {
            thFlows = new HashMap<String, Map<String, BreakpointType>>();
            debugFlowsStopNodeMap.put(flowId, thFlows);
        }
        Map<String, BreakpointType> nodeMap = thFlows.get(threadName);
        if (nodeMap == null) {
            nodeMap = new HashMap<String, BreakpointType>();
            thFlows.put(threadName, nodeMap);
        }
        BreakpointType breakpointType = nodeMap.get(nodeId);
        if (breakpointType != BreakpointType.PERSISTENT) {
            nodeMap.put(nodeId, BreakpointType.TEMPORARY);
        }
        Map<String, Object> fe = getFlowEnvironment(threadName, flowId);
        if (fe != null) {
            synchronized (fe) {
                fe.notifyAll();
            }
        }
    }

}

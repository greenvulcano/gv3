/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.JMXUtils;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.Stats;
import it.greenvulcano.util.thread.ThreadUtils;

/**
 * SubFlowInfo class.
 *
 * @version 3.4.0 Jan 17, 2014
 * @author GreenVulcano Developer Team
 *
 */
public class SubFlowInfo
{
    private static Logger                    logger                  = GVLogger.getLogger(SubFlowInfo.class);

    /**
     * the object JMX descriptor
     */
    public static final String               DESCRIPTOR_NAME         = "SubFlowInfo";
    /**
     * the status of the currently running associated flow, by ID and Thread
     */
    private Map<String, Map<String, String>> flowsStatusMap          = new ConcurrentHashMap<String, Map<String, String>>();
    /**
     * the flow name
     */
    private String                           flow                    = "";
    /**
     * the total successful invocation
     */
    private long                             totalSuccess            = 0;
    /**
     * the total failed invocation
     */
    private long                             totalFailure            = 0;
    /**
     * the master logger level
     */
    private String                           loggerLevel             = "ALL";
    private Level                            loggerLevelj            = Level.ALL;
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
     * @param flow
     *        the subflow name
     * @param jmxSrvcKey
     *        the holding service/operation jmx key
     */
    public SubFlowInfo(String flow, String jmxOperKey)
    {
        this.flow= flow;
        this.jmxFilter = "GreenVulcano:*,Component=" + DESCRIPTOR_NAME + jmxOperKey + ",IDSubFlow=" + flow;
        this.statFailures = new Stats(1000, 1000, 1);
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
        Integer integer = (Integer) initData.get("failureRateo");
        if (integer != null) {
            this.maxFailuresRateo = integer;
        }
        else {
            this.maxFailuresRateo = Integer.MAX_VALUE;
        }
        this.loggerLevel = (String) initData.get("loggerLevel");
        this.loggerLevelj = Level.toLevel(this.loggerLevel);
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
            String key = properties.get("IDService") + ":" + properties.get("IDGroup") + "#" + properties.get("IDOperation") + "#" + this.flow;
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
        properties.put("IDSubFlow", this.flow);
        if (full) {
            /*if (failureAction != null) {
                properties.put("failureAction", failureAction);
                properties.put("failureRateo", new Integer(maxFailuresRateo));
            }*/
        }
        return properties;
    }

    private Map<String, String> getJMXProperties(Map<String, String> properties)
    {
        if (properties == null) {
            properties = new HashMap<String, String>();
        }
        properties.put("IDSubFlow", this.flow);
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
        //loggerLevel = XMLConfig.get(node, "@loggerLevel", "ALL");
        //loggerLevelj = Level.toLevel(loggerLevel);
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
        return this.totalSuccess;
    }

    /**
     * @return the total failed invocations
     */
    public long getTotalFailure()
    {
        return this.totalFailure;
    }

    /**
     * @return the total invocations
     */
    public long getTotal()
    {
        return (this.totalSuccess + this.totalFailure);
    }

    /**
     * reset the internal counter
     */
    public void resetCounter()
    {
        this.totalSuccess = 0;
        this.totalFailure = 0;
    }

    /**
     * Return a matrix of Id/GVFlowNode id for the associated flow status, by ID
     * and Thread
     *
     * @return the current associated flow status
     */
    public Map<String, Map<String, String>> getFlowsStatus()
    {
        return Collections.unmodifiableMap(this.flowsStatusMap);
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
        synchronized (this.flowsStatusMap) {
            Map<String, String> thFlows = this.flowsStatusMap.get(flowId);
            if (thFlows == null) {
                thFlows = new ConcurrentHashMap<String, String>();
                this.flowsStatusMap.put(flowId, thFlows);
            }
            String threadName = Thread.currentThread().getName();
            thFlows.put(threadName, id);
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
        synchronized (this.flowsStatusMap) {
            Map<String, String> thFlows = this.flowsStatusMap.get(flowId);
            if (thFlows == null) {
                thFlows = new ConcurrentHashMap<String, String>();
                this.flowsStatusMap.put(flowId, thFlows);
            }
            String flowStatus = thFlows.get(threadName);
            return flowStatus;
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
            this.totalSuccess++;
        }
        else {
            this.statFailures.hint();
            this.totalFailure++;
        }
        String threadName = Thread.currentThread().getName();
        synchronized (this.flowsStatusMap) {
            Map<String, String> thFlows = this.flowsStatusMap.get(flowId);
            if (thFlows != null) {
                thFlows.remove(threadName);
                if (thFlows.isEmpty()) {
                    this.flowsStatusMap.remove(flowId);
                }
            }
        }
    }

    public boolean interruptFlow(String threadName, String flowId) {
        try {
            logger.info("Interrupting flow [" + flowId + "/" + threadName + "] on SubFlow [" + this.flow + "]");
            boolean found = false;
            synchronized (this.flowsStatusMap) {
                Map<String, String> thFlows = this.flowsStatusMap.get(flowId);
                if (thFlows != null) {
                    found = thFlows.containsKey(threadName);
                }
            }

            if (found) {
                Thread th = ThreadUtils.getThread(threadName);
                if (th != null) {
                    th.interrupt();
                    logger.info("Interrupted flow [" + flowId + "/" + threadName + "] on SubFlow [" + this.flow + "]");
                    return true;
                }
            }
            logger.info("Failed interruption of flow [" + flowId + "/" + threadName + "] on SubFlow [" + this.flow + "] - Not found active flows");
        }
        catch (Exception exc) {
            logger.error("Error occurred executing Flow interruption", exc);
        }
        return false;
    }

    /**
     * @return the subflw name
     */
    public String getSubFlow()
    {
        return this.flow;
    }


    /**
     * @param loggerLevel
     *        the master logger level value
     */
    public void setLoggerLevel(String loggerLevel) throws Exception
    {
        this.loggerLevel = loggerLevel;
        this.loggerLevelj = Level.toLevel(loggerLevel);
        JMXUtils.set(this.jmxFilter, "loggerLevelj", this.loggerLevelj, false, logger);
    }

    /**
     * @return the master logger level value
     */
    public String getLoggerLevel()
    {
        return this.loggerLevel;
    }

    public void setLoggerLevelj(Level loggerLevelj) throws Exception
    {
        this.loggerLevelj = loggerLevelj;
        this.loggerLevel = loggerLevelj.toString();
    }

    public Level getLoggerLevelj()
    {
        return this.loggerLevelj;
    }

    /**
     * @return true if the instance run on administrator/support server
     */
    public boolean isAdministrator()
    {
        return this.isAdministrator;
    }

    /**
     * @param isAdmin
     *        the flag value
     */
    public void setAdministrator(boolean isAdmin)
    {
        this.isAdministrator = isAdmin;
    }

    /**
     * @return True if the instance can call the Administration Server on
     *         objects initialization
     */
    public boolean canCallAdministratorOnInit()
    {
        return this.callAdministratorOnInit;
    }

    /**
     * @param call
     *        If true the instance can call the Administration Server on object
     *        initialization
     */
    public void setCallAdministratorOnInit(boolean call)
    {
        this.callAdministratorOnInit = call;
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

}

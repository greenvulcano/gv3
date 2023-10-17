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
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.log.GVBufferDump;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xpath.XPathFinder;

/**
 * GVFlowNode base class.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public abstract class GVFlowNode implements GVFlowNodeIF
{
    /**
     * the flow node id
     */
    private String  id                     = "";
    /**
     * the input object name
     */
    private String  input                  = "";
    /**
     * the output object name
     */
    private String  output                 = "";
    /**
     * if true the flow node terminate a business flow
     */
    private boolean businessFlowTerminated = false;
    /**
     * dump input/output
     */
    private boolean dumpInOut              = false;
    /**
     * dump environment input/output
     */
    private boolean dumpEnvInOut           = false;
    /**
     * dump environment on error
     */
    private boolean dumpEnvOnError         = false;

    /**
     * Initialize the instance
     *
     * @param defNode
     *        the flow node definition
     * @throws GVCoreConfException
     *         if errors occurs
     */
    @Override
    public void init(Node defNode) throws GVCoreConfException
    {
        try {
            this.id = XMLConfig.get(defNode, "@id");
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'id'"},
                    {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }
        this.input = XMLConfig.get(defNode, "@input", "");
        this.output = XMLConfig.get(defNode, "@output", this.input);

        if (this.input.equals("") && this.output.equals("")) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{
                    {"name", "'input' or 'output'"}, {"node", XPathFinder.buildXPath(defNode)}});
        }

        this.dumpInOut = XMLConfig.getBoolean(defNode, "@dump-in-out", false);
        this.dumpEnvInOut = XMLConfig.getBoolean(defNode, "@dump-env-in-out", false);
        this.dumpEnvOnError = XMLConfig.getBoolean(defNode, "@dump-env-on-error", false);
    }

    /**
     * @return the flow node id
     */
    @Override
    public String getId()
    {
        return this.id;
    }

    /**
     * @return the input object name
     */
    public String getInput()
    {
        return this.input;
    }

    /**
     * @return the output object name
     */
    @Override
    public String getOutput()
    {
        return this.output;
    }

    /**
     * @return the node default nextNodeID
     */
    public abstract String getDefaultNextNodeId();

    /**
     * @return the flag value
     */
    public boolean isBusinessFlowTerminated()
    {
        return this.businessFlowTerminated;
    }

    /**
     * @param b
     *        the flag value
     */
    public void setBusinessFlowTerminated(boolean b)
    {
        this.businessFlowTerminated = b;
    }

    /**
     * Perform the flow node work
     *
     * @param environment
     *        the flow execution environment
     * @return the next flow node id
     * @throws GVCoreException
     *         if errors occurs
     * @throws InterruptedException
     *         if the current Thread is interrupted
     */
    @Override
    public String execute(Map<String, Object> environment) throws GVCoreException, InterruptedException {
        return execute(environment, false);
    }

    /**
     * Perform the flow node work
     *
     * @param environment
     *        the flow execution environment
     * @return the next flow node id
     * @throws GVCoreException
     *         if errors occurs
     * @throws InterruptedException
     *         if the current Thread is interrupted
     */
    @Override
    public abstract String execute(Map<String, Object> environment, boolean onDebug) throws GVCoreException,
        InterruptedException;

    /**
     * @return if GVBuffer should be dumped for input and output logging
     */
    public boolean isDumpInOut()
    {
        return this.dumpInOut;
    }

    /**
     * @return if input and output Execution Environment should be dumped for
     *         logging
     */
    public boolean isDumpEnvInOut()
    {
        return this.dumpEnvInOut;
    }

    /**
     * @return if Execution Environment should be dumped for error
     */
    public boolean isDumpEnvOnError()
    {
        return this.dumpEnvOnError;
    }

    /**
     * Perform the flow node cleanup operation
     *
     * @throws GVCoreException
     *         if errors occurs
     */
    @Override
    public abstract void cleanUp() throws GVCoreException;

    /**
     * Perform the flow node destroy operation
     *
     * @throws GVCoreException
     *         if errors occurs
     */
    @Override
    public abstract void destroy() throws GVCoreException;

    /**
     *
     * @return
     *        the current Thread interrupted state
     */
    public boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    public void checkInterrupted(String type) throws InterruptedException {
        ThreadUtils.checkInterrupted(type, getId(), getLogger());
    }

    protected abstract Logger getLogger();

    /**
     * @param logger
     * @param isInput
     * @param environment
     */
    protected void dumpEnvironment(boolean isInput, Map<String, Object> environment)
    {
        dumpEnvironment(Level.INFO, isInput, environment);
    }

    /**
     * @param level
     * @param isInput
     * @param environment
     */
    protected void dumpEnvironment(Level level, boolean isInput, Map<String, Object> environment)
    {
        /*if (getLogger().isDebugEnabled()) {
            // at debug in/out are already dumped
            return;
        }*/
        if (isDumpEnvInOut()) {
            StringBuffer msg = new StringBuffer(10000);
            if (isInput) {
                msg.append("\nBEGIN INPUT - Node[" + getId() + "] Execution Environment dump\n");
            }
            else {
                msg.append("\nBEGIN OUTPUT - Node[" + getId() + "] Execution Environment dump\n");
            }
            for (Map.Entry<String, Object> element : environment.entrySet()) {
                Object value = element.getValue();
                if (value instanceof GVBuffer) {
                    msg.append("***** Entry [").append(element.getKey()).append("] of class: ").append(
                            value.getClass().getName()).append("\n").append(new GVBufferDump((GVBuffer) value)).append(
                            "\n\n");
                }
                else if (value instanceof Throwable) {
                    msg.append("***** Entry [").append(element.getKey()).append("] of class: ").append(
                            value.getClass().getName()).append("\n").append(value).append("\n").append(
                            ThreadUtils.getStackTrace((Throwable) value)).append("\n\n");
                }
                else if (value == null) {
                    msg.append("***** Entry [").append(element.getKey()).append("] IS NULL\n\n");
                }
                else {
                    msg.append("***** Entry [").append(element.getKey()).append("] of class: ").append(
                            value.getClass().getName()).append("\n").append(value).append("\n\n");
                }
            }
            if (isInput) {
                msg.append("END INPUT - Node[" + getId() + "] Execution Environment dump\n");
            }
            else {
                msg.append("END OUTPUT - Node[" + getId() + "] Execution Environment dump\n");
            }
            getLogger().log(level, msg);
        }
    }

}
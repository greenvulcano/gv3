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
package it.greenvulcano.gvesb.virtual.jbpm;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.expression.ExpressionEvaluator;
import it.greenvulcano.expression.ExpressionEvaluatorHelper;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.process.Process;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Executes call to JBPM.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *         REVISION OK
 */
public class JbpmCallOperation implements CallOperation
{
    private static Logger       logger     = GVLogger.getLogger(JbpmCallOperation.class);
    private Map<String, String> parameters = new HashMap<String, String>();
    private String              operation  = null;

    /**
     * the operation key
     */
    protected OperationKey      key        = null;

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            operation = XMLConfig.get(node, "@operation");
            logger.debug("operation=" + operation);
            NodeList pnl = XMLConfig.getNodeList(node, "ParamsJbpm/ParamJbpm");
            if ((pnl != null) && (pnl.getLength() > 0)) {
                for (int i = 0; i < pnl.getLength(); i++) {
                    Node n = pnl.item(i);
                    String name = XMLConfig.get(n, "@name");
                    String expression = XMLConfig.get(n, "@expression");
                    parameters.put(name, expression);
                    logger.debug("name " + name + "=" + expression);
                }
            }
        }
        catch (Exception exc) {
            logger.error("ERROR JbpmCall initialization", exc);
            throw new InitializationException("GV_CONF_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
    }

    private void startProcess(String processId, Map<String, Object> params) throws Exception
    {
        StatefulKnowledgeSession ksession = JBPMSessionManager.instance().getSession();
        ksession.startProcess(processId, params);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        try {
            if (operation.equals("startProcess")) {
                startProcess(gvBuffer);
            }
            else if (operation.equals("getProcesses")) {
                gvBuffer.setObject(getProcesses());
            }
            else if (operation.equals("getProcess")) {
                String processId = gvBuffer.getProperty("processId");
                getProcess(processId);
            }
            else if (operation.equals("getProcessByName")) {
                String processName = gvBuffer.getProperty("processName");
                gvBuffer.setObject(getProcessByName(processName));
            }
            else if (operation.equals("removeProcess")) {
                String processId = gvBuffer.getProperty("processId");
                removeProcess(processId);
            }
            else if (operation.equals("abortProcessInstance")) {
                String processId = gvBuffer.getProperty("processId");
                abortProcessInstance(processId);
            }
            else if (operation.equals("getProcessInstanceVariables")) {
                String processId = gvBuffer.getProperty("processId");
                gvBuffer.setObject(getProcessInstanceVariables(processId));
            }
            else if (operation.equals("setProcessInstanceVariables")) {
                String processId = gvBuffer.getProperty("processId");
                setProcessInstanceVariables(processId, gvBuffer.getObject());
            }
            else if (operation.equals("signalExecution")) {
                String processId = gvBuffer.getProperty("processId");
                String signal = gvBuffer.getProperty("signal");
                signalExecution(processId, signal);
            }
        }
        catch (Exception exc) {
            logger.error("ERROR jbpm execution", exc);
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
        return gvBuffer;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#set9Key(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    @Override
    public OperationKey getKey()
    {
        return key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

    private void startProcess(GVBuffer gvBuffer) throws Exception
    {
        Map<String, Object> params = new HashMap<String, Object>();
        String processId = gvBuffer.getProperty("processId");
        if (!parameters.isEmpty()) {
            ExpressionEvaluatorHelper.startEvaluation();
            try {
                ExpressionEvaluatorHelper.addToContext("params", gvBuffer);
                for (Map.Entry<String, String> p : parameters.entrySet()) {
                    String value = p.getValue();
                    ExpressionEvaluator expressionEvaluator = ExpressionEvaluatorHelper.getExpressionEvaluator(ExpressionEvaluatorHelper.OGNL_EXPRESSION_LANGUAGE);
                    String nomeParam = p.getKey();
                    Object obj = expressionEvaluator.getValue(value, gvBuffer);
                    params.put(p.getKey(), obj);
                    System.out.println("param " + nomeParam + "=" + obj.toString());
                }
            }
            finally {
                ExpressionEvaluatorHelper.endEvaluation();
            }
        }
        startProcess(processId, params);
    }

    private List<Process> getProcesses() throws Exception
    {
        List<Process> result = new ArrayList<Process>();
        for (KnowledgePackage kpackage : JBPMSessionManager.instance().getSession().getKnowledgeBase().getKnowledgePackages()) {
            result.addAll(kpackage.getProcesses());
        }
        return result;
    }

    private Process getProcess(String processId) throws Exception
    {
        for (KnowledgePackage kpackage : JBPMSessionManager.instance().getSession().getKnowledgeBase().getKnowledgePackages()) {
            for (Process process : kpackage.getProcesses()) {
                if (processId.equals(process.getId())) {
                    return process;
                }
            }
        }
        return null;
    }

    private Process getProcessByName(String name) throws Exception
    {
        for (KnowledgePackage kpackage : JBPMSessionManager.instance().getSession().getKnowledgeBase().getKnowledgePackages()) {
            for (Process process : kpackage.getProcesses()) {
                if (name.equals(process.getName())) {
                    return process;
                }
            }
        }
        return null;
    }

    private void removeProcess(String processId)
    {
        throw new UnsupportedOperationException();
    }


    private void abortProcessInstance(String processInstanceId) throws Exception
    {
        ProcessInstance processInstance;
        processInstance = JBPMSessionManager.instance().getSession().getProcessInstance(new Long(processInstanceId));

        if (processInstance != null) {
            JBPMSessionManager.instance().getSession().abortProcessInstance(new Long(processInstanceId));
        }
        else {
            throw new IllegalArgumentException("Could not find process instance " + processInstanceId);
        }

    }

    private Map<String, Object> getProcessInstanceVariables(String processInstanceId) throws Exception
    {
        ProcessInstance processInstance = JBPMSessionManager.instance().getSession().getProcessInstance(
                new Long(processInstanceId));
        if (processInstance != null) {
            Map<String, Object> variables = ((WorkflowProcessInstanceImpl) processInstance).getVariables();
            if (variables == null) {
                return new HashMap<String, Object>();
            }
            // filter out null values
            Map<String, Object> result = new HashMap<String, Object>();
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                if (entry.getValue() != null) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        }
        else {
            throw new IllegalArgumentException("Could not find process instance " + processInstanceId);
        }
    }

    private void setProcessInstanceVariables(String processInstanceId, Object variables) throws Exception
    {
        ProcessInstance processInstance = JBPMSessionManager.instance().getSession().getProcessInstance(
                new Long(processInstanceId));
        if (processInstance != null) {
            VariableScopeInstance variableScope = (VariableScopeInstance) ((org.jbpm.process.instance.ProcessInstance) processInstance).getContextInstance(VariableScope.VARIABLE_SCOPE);
            if (variableScope == null) {
                logger.debug("Could not find variable scope for process instance " + processInstanceId);
            }
            variableScope.setVariable("gvbuffer", variables);
        }
        else {
            throw new IllegalArgumentException("Could not find process instance " + processInstanceId);
        }
    }

    private void signalExecution(String executionId, String signal) throws Exception
    {
        JBPMSessionManager.instance().getSession().getProcessInstance(new Long(executionId)).signalEvent("signal",
                signal);
    }

}

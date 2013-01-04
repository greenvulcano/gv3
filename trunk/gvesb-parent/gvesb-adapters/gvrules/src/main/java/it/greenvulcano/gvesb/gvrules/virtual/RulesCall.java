/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.gvrules.virtual;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.gvrules.drools.config.GVRulesConfigManager;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.ThreadMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 11/feb/2012
 * @author GreenVulcano Developer Team
 */
public class RulesCall implements CallOperation
{
    private static Logger             logger       = GVLogger.getLogger(RulesCall.class);

    private String                    name         = null;
    private boolean                   isStateful   = false;
    private String                    ruleSet      = null;
    private String                    inputRefDP   = null;
    private String                    globalsRefDP = null;
    private String                    outputRefDP  = null;

    private StatelessKnowledgeSession statelessKS  = null;
    private StatefulKnowledgeSession  statefulKS   = null;

    /**
     * the operation key
     */
    protected OperationKey            key          = null;

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            name = XMLConfig.get(node, "@name");
            logger.debug("BEGIN RulesCall[" + name + "] initialization");

            isStateful = XMLConfig.getBoolean(node, "@stateful", false);
            ruleSet = XMLConfig.get(node, "@ruleSet");
            inputRefDP = XMLConfig.get(node, "@input-ref-dp");
            globalsRefDP = XMLConfig.get(node, "@globals-ref-dp");
            outputRefDP = XMLConfig.get(node, "@output-ref-dp");

            logger.debug("END RulesCall[" + name + "] initialization");
        }
        catch (Exception exc) {
            logger.error("ERROR RulesCall[" + name + "] initialization", exc);
            throw new InitializationException("GV_CONF_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        DataProviderManager dpM = null;
        IDataProvider inDP = null;
        IDataProvider gblDP = null;
        IDataProvider outDP = null;
        try {
            logger.debug("BEGIN RulesCall[" + name + "]");
            List<Command> cmds = new ArrayList<Command>();
            Map<String, Object> gblOut = new HashMap<String, Object>();

            dpM = DataProviderManager.instance();

            if ((globalsRefDP != null) && (globalsRefDP.length() > 0)) {
                gblDP = dpM.getDataProvider(globalsRefDP);
                gblDP.setObject(gvBuffer);

                @SuppressWarnings("unchecked")
                Map<Object, Object> gbl = (Map<Object, Object>) gblDP.getResult();
                for (Entry<Object, Object> entry : gbl.entrySet()) {
                    String gN = entry.getKey().toString();
                    Object gV = entry.getValue();
                    if (gN.startsWith("[[OUT]]")) {
                        gN = gN.substring(7);
                        gblOut.put(gN, gV);
                    }
                    cmds.add(CommandFactory.newSetGlobal(gN, gV));
                    logger.debug("Setting " + (gblOut.isEmpty() ? "" : "Out") + "Global[" + gN + "]: " + gV);
                }
            }

            inDP = dpM.getDataProvider(inputRefDP);
            inDP.setObject(gvBuffer);
            @SuppressWarnings("unchecked")
            Map<Object, Object> in = (Map<Object, Object>) inDP.getResult();
            for (Entry<Object, Object> entry : in.entrySet()) {
                String iN = entry.getKey().toString();
                Object iV = entry.getValue();
                if (iV instanceof Collection) {
                    cmds.add(CommandFactory.newInsertElements((Collection) iV));
                    logger.debug("Adding Coll Input[" + iN + "]: " + iV);
                }
                else {
                    cmds.add(CommandFactory.newInsert(entry.getValue(), iN));
                    logger.debug("Adding Input[" + iN + "]: " + iV);
                }
            }

            ExecutionResults results = null;
            if (isStateful) {
                statefulKS = (StatefulKnowledgeSession) ThreadMap.get("STATEFUL_RULE_SESSION");
                if (statefulKS == null) {
                    statefulKS = GVRulesConfigManager.instance().getStatefulKnowledgeSession(ruleSet);
                    ThreadMap.put("STATEFUL_RULE_SESSION", statefulKS);
                }
                cmds.add(CommandFactory.newFireAllRules());
                results = statefulKS.execute(CommandFactory.newBatchExecution(cmds));
            }
            else {
                statelessKS = GVRulesConfigManager.instance().getStatelessKnowledgeSession(ruleSet);
                results = statelessKS.execute(CommandFactory.newBatchExecution(cmds));
            }

            if ((outputRefDP != null) && (outputRefDP.length() > 0)) {
                outDP = dpM.getDataProvider(outputRefDP);
                outDP.setContext(gvBuffer);
                switch (gblOut.size()) {
                    case 0 : {
                        outDP.setObject(results);
                        break;
                    }
                    case 1 : {
                        outDP.setObject(gblOut.values().iterator().next());
                        break;
                    }
                    default : {
                        outDP.setObject(gblOut);
                    }
                }
                gvBuffer = (GVBuffer) outDP.getResult();
            }
            else {
                switch (gblOut.size()) {
                    case 0 : {
                        gvBuffer.setObject(results);
                        break;
                    }
                    case 1 : {
                        gvBuffer.setObject(gblOut.values().iterator().next());
                        break;
                    }
                    default : {
                        gvBuffer.setObject(gblOut);
                    }
                }
            }

            return gvBuffer;
        }
        catch (Exception exc) {
            logger.error("ERROR RulesCall[" + name + "] execution", exc);
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
        finally {
            logger.debug("END RulesCall[" + name + "]");
            if (dpM != null) {
                dpM.releaseDataProvider(inputRefDP, inDP);
                dpM.releaseDataProvider(globalsRefDP, gblDP);
                dpM.releaseDataProvider(outputRefDP, outDP);
            }
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        statelessKS = null;
        if (statefulKS != null) {
            ThreadMap.remove("STATEFUL_RULE_SESSION");
            statefulKS.dispose();
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    @Override
    public OperationKey getKey()
    {
        return key;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

}

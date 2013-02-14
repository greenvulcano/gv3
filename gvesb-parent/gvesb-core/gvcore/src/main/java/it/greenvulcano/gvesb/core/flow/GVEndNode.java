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
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.exc.GVCoreWrongInterfaceException;
import it.greenvulcano.gvesb.core.savepoint.SavePointController;
import it.greenvulcano.gvesb.internal.data.ChangeGVBuffer;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * GVFlow node indicating a flow end.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 * 
 */
public class GVEndNode extends GVFlowNode
{
    private static final Logger logger          = GVLogger.getLogger(GVEndNode.class);

    /**
     * the ChangeGVBuffer instance
     */
    private ChangeGVBuffer      endOpCGVBuffer  = null;
    /**
     * the GVThrowException instance
     */
    private GVThrowException    endOpTException = null;

    private boolean             keepSavepoint   = false;

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
        super.init(defNode);

        setBusinessFlowTerminated(XMLConfig.getBoolean(defNode, "@end-business-process", false));

        keepSavepoint = XMLConfig.getBoolean(defNode, "@keep-savepoint", false);

        Node endOp = null;
        try {
            endOp = XMLConfig.getNode(defNode, "*");
        }
        catch (XMLConfigException exc) {
            // do nothing
        }
        if (endOp != null) {
            if (endOp.getNodeName().equals("ChangeGVBuffer")) {
                endOpCGVBuffer = new ChangeGVBuffer();
                try {
                    endOpCGVBuffer.init(endOp);
                }
                catch (XMLConfigException exc) {
                    throw new GVCoreConfException("GVCORE_END_OPERATION_INIT_ERROR", new String[][]{{"id", getId()},
                            {"type", "ChangeGVBuffer"}, {"node", XPathFinder.buildXPath(endOp)}}, exc);
                }
            }
            else if (endOp.getNodeName().equals("GVThrowException")) {
                endOpTException = new GVThrowException();
                try {
                    endOpTException.init(endOp);
                }
                catch (XMLConfigException exc) {
                    throw new GVCoreConfException("GVCORE_END_OPERATION_INIT_ERROR", new String[][]{{"id", getId()},
                            {"type", "GVThrowException"}, {"node", XPathFinder.buildXPath(endOp)}}, exc);
                }
            }
        }
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#execute(java.util.Map,
     *      boolean)
     */
    @Override
    public String execute(Map<String, Object> environment, boolean onDebug) throws GVCoreException
    {
        logger.info("Executing GVEndNode '" + getId() + "'");
        dumpEnvironment(logger, true, environment);

        String output = getOutput();
        Object obj = environment.get(output);

        if (endOpCGVBuffer != null) {
            if (obj == null) {
                logger.error("GVCORE_INVALID_GVBUFFER_ERROR - Empty '" + output + "' environment field.");
                environment.put(output, new GVCoreWrongInterfaceException("GVCORE_INVALID_GVBUFFER_ERROR",
                        new String[][]{{"exception", "Empty '" + output + "' environment field."}}));
                return "";
            }
            if (obj instanceof GVBuffer) {
                try {
                    obj = endOpCGVBuffer.execute((GVBuffer) obj, environment);
                    environment.put(output, obj);
                }
                catch (Exception exc) {
                    environment.put(output, exc);
                }
            }
        }
        else if (endOpTException != null) {
            try {
                if (obj != null) {
                    if (obj instanceof Exception) {
                        obj = endOpTException.execute((Exception) obj, null);
                    }
                    else {
                        obj = endOpTException.execute(null, (GVBuffer) obj);
                    }
                }
                else {
                    obj = endOpTException.execute(null, null);
                }
                environment.put(output, obj);
            }
            catch (Exception exc) {
                environment.put(output, exc);
            }
        }
        else if (obj == null) {
            logger.error("GVCORE_INVALID_GVBUFFER_ERROR - Empty '" + output + "' environment field.");
            environment.put(output, new GVCoreWrongInterfaceException("GVCORE_INVALID_GVBUFFER_ERROR", new String[][]{{
                    "exception", "Empty '" + output + "' environment field."}}));
        }

        if (environment.containsKey("IS_SAVE_POINT")) {
            // must check the output object type???
            try {
                logger.debug("Handling SavePoint information for Flow");
                InvocationContext ctx = (InvocationContext) InvocationContext.getInstance();
                if (keepSavepoint) {
                    SavePointController.instance().confirm(ctx.getId().toString(), ctx.getSystem(), ctx.getService(),
                            ctx.getOperation());
                }
                else {
                    SavePointController.instance().delete(ctx.getId().toString(), ctx.getSystem(), ctx.getService(),
                            ctx.getOperation());
                }
            }
            catch (Exception exc) {
                // TODO: handle exception
            }
        }

        logger.debug("END - Execute GVEndNode '" + getId() + "'");
        return "";
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#getDefaultNextNodeId()
     */
    @Override
    public String getDefaultNextNodeId()
    {
        return "";
    }

    /**
     * 
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#cleanUp()
     */
    @Override
    public void cleanUp() throws GVCoreException
    {
        // do nothing
    }

    /**
     * 
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#destroy()
     */
    @Override
    public void destroy() throws GVCoreException
    {
        // do nothing
    }
}
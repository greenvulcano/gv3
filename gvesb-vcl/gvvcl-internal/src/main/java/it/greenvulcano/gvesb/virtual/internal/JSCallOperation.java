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
package it.greenvulcano.gvesb.virtual.internal;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.expression.ExpressionEvaluatorHelper;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;

import org.w3c.dom.Node;


/**
 *
 * ScriptCallOperation class
 *
 * @version 3.0.0 Jun 1, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */	
public class JSCallOperation implements CallOperation
{
    /**
     * The operation key.
     */
    protected OperationKey key = null;

    private String         javaScript;
    

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws InitializationException
    {
        try {
            javaScript = XMLConfig.get(node, "Script");
        }
        catch (Exception exc) {
            throw new InitializationException("GVVCL_JAVASCRIPT_INIT_ERROR", new String[][]{{"node",
                    node.getLocalName()}}, exc);
        }
    }

    /**
     * Executes the operation.
     *
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
    	ExpressionEvaluatorHelper.startEvaluation();
        try {
        	ExpressionEvaluatorHelper.getValue(ExpressionEvaluatorHelper.JSCRIPT_EXPRESSION_LANGUAGE, javaScript, gvBuffer);
            return gvBuffer;
        }
        catch (Exception exc) {
            throw new CallException("GVVCL_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
        finally{
        	ExpressionEvaluatorHelper.endEvaluation();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    public void cleanUp()
    {
//        this.script.cleanUp();
    }

    /**
     * Called when an operation is discarded from cache.
     */
    public void destroy()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    public OperationKey getKey()
    {
        return key;
    }

    /**
     * Return the alias for the given service
     *
     * @param gvBuffer
     *        the input service data
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }
}

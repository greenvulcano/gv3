/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project.
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
package it.greenvulcano.gvesb.gvnet.marshall;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvnet.NetAdapterException;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.script.ScriptExecutor;
import it.greenvulcano.script.ScriptExecutorFactory;

import org.w3c.dom.Node;

/**
 * @version 3.5.0 18/05/2014
 * @author GreenVulcano Developer Team
 */
public class ScriptMarshaller implements Decoder, Encoder
{
   /*
    * The script executor instance.
    */
   private ScriptExecutor script = null;

    /**
     * 
     */
    public ScriptMarshaller()
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.gvzmq.marshall.Decoder#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws NetAdapterException
    {
        try {
            Node scriptNode = XMLConfig.getNode(node, "Script");
            if (scriptNode != null) {
                script = ScriptExecutorFactory.createSE(scriptNode);
            }
            else {
                throw new NetAdapterException("Invalid Script content for ScriptMarshaller");
            }
        }
        catch (NetAdapterException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new NetAdapterException("GVNET_MARSHALLER_INIT_ERROR", exc);
        }

    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.gvnet.marshall.Encoder#encode(it.greenvulcano.gvesb.buffer.GVBuffer, 
     * it.greenvulcano.gvesb.gvnet.marshall.NetMessage)
     */
    @Override
    public NetMessage encode(GVBuffer gvbIn, NetMessage msgIn) throws NetAdapterException
    {
        NetMessage msgOut = new NetMessage();
        try {
            script.putProperty("msgIn", msgIn);
            script.putProperty("gvbIn", gvbIn);
            script.putProperty("msgOut", msgOut);
            script.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvbIn, true), gvbIn);
            return msgOut;
        }
        catch (Exception exc) {
            throw new NetAdapterException("GVNET_ENCODE_ERROR", exc);
        }
        finally {
            script.cleanUp();
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.gvnet.marshall.Decoder#decode(it.greenvulcano.gvesb.gvnet.marshall.NetMessage, 
     * it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer decode(NetMessage msgIn, GVBuffer gvbIn) throws NetAdapterException
    {
        GVBuffer gvbOut = new GVBuffer(gvbIn);
        try {
            script.putProperty("msgIn", msgIn);
            script.putProperty("gvbIn", gvbIn);
            script.putProperty("gvbOut", gvbOut);
            
            script.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvbIn, true), gvbIn);
            return gvbOut;
        }
        catch (Exception exc) {
            throw new NetAdapterException("GVNET_DECODE_ERROR", exc);
        }
        finally {
            script.cleanUp();
        }
    }
}

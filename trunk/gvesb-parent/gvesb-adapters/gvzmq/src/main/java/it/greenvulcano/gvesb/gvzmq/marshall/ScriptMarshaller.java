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
package it.greenvulcano.gvesb.gvzmq.marshall;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvzmq.ZMQAdapterException;
import it.greenvulcano.script.ScriptExecutor;
import it.greenvulcano.script.ScriptExecutorFactory;

import org.w3c.dom.Node;
import org.zeromq.ZMsg;

/**
 * @version 3.2.0 21/mar/2012
 * @author GreenVulcano Developer Team
 */
public class ScriptMarshaller implements Decoder, Encoder
{
    private ScriptExecutor scriptEx = null;

    /**
     * 
     */
    public ScriptMarshaller()
    {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.gvzmq.marshall.Decoder#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws ZMQAdapterException
    {
        try {
            scriptEx = ScriptExecutorFactory.createSE(XMLConfig.getNode(node, "Script"));
        }
        catch (Exception exc) {
            throw new ZMQAdapterException("GVZMQ_JSMARSHALLER_INIT_ERROR", exc);
        }

    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.gvzmq.marshall.Encoder#encode(it.greenvulcano.gvesb.buffer.GVBuffer, org.zeromq.ZMsg)
     */
    @Override
    public ZMsg encode(GVBuffer gvbIn, ZMsg msgIn) throws ZMQAdapterException
    {
        ZMsg msgOut = new ZMsg();
        try {
            scriptEx.putProperty("msgIn", msgIn);
            scriptEx.putProperty("gvbIn", gvbIn);
            scriptEx.putProperty("msgOut", msgOut);
            scriptEx.execute(null);
            return msgOut;
        }
        catch (Exception exc) {
            throw new ZMQAdapterException("GVZMQ_ENCODE_ERROR", exc);
        }

        /*ZMsg msgOut = new ZMsg();
        Object obj = gvbIn.getObject();
        if (obj instanceof ZMsg) {
            msgOut = (ZMsg) obj;
        }
        else if (obj instanceof byte[]) {
            msgOut.add((byte[]) obj);
        }
        else {
            msgOut.add("" + obj);
        }
        return msgOut;*/
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.gvzmq.marshall.Decoder#decode(org.zeromq.ZMsg, it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer decode(ZMsg msgIn, GVBuffer gvbIn) throws ZMQAdapterException
    {
        GVBuffer gvbOut = new GVBuffer(gvbIn);
        try {
            scriptEx.putProperty("msgIn", msgIn);
            scriptEx.putProperty("gvbIn", gvbIn);
            scriptEx.putProperty("gvbOut", gvbOut);
            scriptEx.execute(null);
            return gvbOut;
        }
        catch (Exception exc) {
            throw new ZMQAdapterException("GVZMQ_DECODE_ERROR", exc);
        }

        /*try {
            StringBuffer sb = new StringBuffer();
            Iterator<ZFrame> it = msgIn.iterator();
            while (it.hasNext()) {
                ZFrame zf = it.next();
                sb.append(new String(zf.getData()));
                if (it.hasNext()) {
                    sb.append("\n---------------\n");
                }
            }
            gvbIn.setObject(sb.toString());
            return gvbIn;
        }
        catch (Exception exc) {
            throw new ZMQAdapterException("GVZMQ_DECODE_ERROR", exc);
        }*/
    }
}

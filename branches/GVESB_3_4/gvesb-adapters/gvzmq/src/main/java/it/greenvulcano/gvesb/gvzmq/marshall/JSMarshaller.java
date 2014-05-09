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
import it.greenvulcano.js.initializer.JSInit;
import it.greenvulcano.js.initializer.JSInitManager;
import it.greenvulcano.js.util.JavaScriptHelper;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Node;
import org.zeromq.ZMsg;

/**
 * @version 3.2.0 21/mar/2012
 * @author GreenVulcano Developer Team
 */
public class JSMarshaller implements Decoder, Encoder
{

    private String script    = null;
    private String scopeName = null;

    /**
     * 
     */
    public JSMarshaller()
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
            scopeName = XMLConfig.get(node, "@scope-name", "gvesb");
            script = XMLConfig.get(node, "Script", "");
            if ("".equals(script)) {
                throw new ZMQAdapterException("Invalid Script content for JSMarshaller");
            }
        }
        catch (ZMQAdapterException exc) {
            throw exc;
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
        Context cx = ContextFactory.getGlobal().enterContext();
        Scriptable scope = null;
        ZMsg msgOut = new ZMsg();
        try {
            String scopeName = "gvesb";
            scope = JSInitManager.instance().getJSInit(scopeName).getScope();
            scope = JSInit.setProperty(scope, "msgIn", msgIn);
            scope = JSInit.setProperty(scope, "gvbIn", gvbIn);
            scope = JSInit.setProperty(scope, "msgOut", msgOut);
            JavaScriptHelper.executeScript(script, "encode", scope, cx);
            return msgOut;
        }
        catch (Exception exc) {
            throw new ZMQAdapterException("GVZMQ_ENCODE_ERROR", exc);
        }
        finally {
            Context.exit();
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
        Context cx = ContextFactory.getGlobal().enterContext();
        Scriptable scope = null;
        GVBuffer gvbOut = new GVBuffer(gvbIn);
        try {
            String scopeName = "gvesb";
            scope = JSInitManager.instance().getJSInit(scopeName).getScope();
            scope = JSInit.setProperty(scope, "msgIn", msgIn);
            scope = JSInit.setProperty(scope, "gvbIn", gvbIn);
            scope = JSInit.setProperty(scope, "gvbOut", gvbOut);
            JavaScriptHelper.executeScript(script, "decode", scope, cx);
            return gvbOut;
        }
        catch (Exception exc) {
            throw new ZMQAdapterException("GVZMQ_DECODE_ERROR", exc);
        }
        finally {
            Context.exit();
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

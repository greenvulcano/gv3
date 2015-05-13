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
package it.greenvulcano.gvesb.gvnet.listener;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvnet.GVNetManager;
import it.greenvulcano.gvesb.gvnet.NetAdapterException;
import it.greenvulcano.gvesb.gvnet.listener.invoker.NetInvoker;
import it.greenvulcano.gvesb.gvnet.marshall.NetMessage;
import it.greenvulcano.gvesb.gvnet.parser.Parser;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.BaseThreadFactory;
import it.greenvulcano.util.thread.ThreadMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.5.0 18/05/2014
 * @author GreenVulcano Developer Team
 */
public class NetReceiver implements Runnable
{
    private static Logger     logger     = GVLogger.getLogger(NetReceiver.class);

    /**
     * Socket timeout
     */
    private static final int  SO_TIMEOUT = 3000;

    private BaseThreadFactory tFactory   = null;
    
    private String            host;
    private int               port;
    private String            name;
    private Socket            socket     = null;
    private NetInvoker        invoker    = null;
    private Parser            parser     = null; 
    private boolean           autoStart  = false;
    private String            serverName;
    private boolean           isActive   = false;
    private Thread            thread     = null;
    private long              reconTime  = 30000; 


    public void init(Node node) throws NetAdapterException {
        try {
            serverName = JMXEntryPoint.getServerName();
            name = XMLConfig.get(node, "@name");
            host = XMLConfig.get(node, "@host");
            port = XMLConfig.getInteger(node, "@port");
            autoStart = XMLConfig.getBoolean(node, "@autoStart", true);
            reconTime = XMLConfig.getInteger(node, "@reconnectTime", 30) * 1000;

            tFactory = new BaseThreadFactory("NetReceiver#" + name, true);
            
            Node np = XMLConfig.getNode(node, "*[@type='net-parser']");
            parser = (Parser) Class.forName(XMLConfig.get(np, "@class")).newInstance();
            parser.init(np);
            
            Node ni = XMLConfig.getNode(node, "*[@type='net-invoker']");
            invoker = (NetInvoker) Class.forName(XMLConfig.get(ni, "@class")).newInstance();
            invoker.init(ni);
        }
        catch (Exception exc) {
            logger.error("Error initializing NetReceiver", exc);
            throw new NetAdapterException("GVNET_RECEIVER_INIT_ERROR", exc);
        }
    }

    @Override
    public void run()
    {
        NMDC.push();
        NMDC.clear();
        NMDC.setServer(serverName);
        NMDC.setSubSystem(GVNetManager.SUBSYSTEM);
        NMDC.put("RECEIVER", name);
         
        try {
            isActive = true;

            while (isActive()) {
                OutputStream os = connect();
            
                while (isActive()) {
                    try {
                        NetMessage msgIn = parser.getMessage();
                        if (msgIn != null) {
                            NetMessage msgOut = invoker.processMessage(msgIn);
                            if (msgOut != null) {
                                msgOut.writeTo(os);
                            }
                        }
                        else {
                            break;
                        }
                    }
                    catch (InterruptedException exc) {
                        logger.warn("NetReceiver[" + name + "] interrupted while processing data");
                    }
                    catch (Exception exc) {
                        logger.error("NetReceiver[" + name + "] error while processing data: ", exc);
                    }
                }
                logger.info("NetReceiver[" + name + "] stop listening");
                disconnect();
            }
        }
        catch (Exception exc) {
            logger.error("NetReceiver[" + name + "] listening error", exc);
        }
        finally {
            disconnect();
            isActive = false;
            NMDC.pop();
            ThreadMap.clean();
        }
    }

    /**
     * @return
     * @throws NetAdapterException
     */
    private OutputStream connect() throws NetAdapterException {
        OutputStream os = null;
        while (isActive && (os == null)) {
            try {
                socket = new Socket(host, port);
                //socket.setSoTimeout(SO_TIMEOUT);
                InputStream is  = socket.getInputStream();
                os = socket.getOutputStream();
                parser.setInputStream(is);
                logger.info("NetReceiver[" + name + "] connected to [" + host + ":" + port + "]");

                ThreadMap.put("NET_LISTENER", name);
                ThreadMap.put("NET_REMOTE_ADDR", socket.getInetAddress().getHostAddress());
                ThreadMap.put("NET_REMOTE_PORT", "" + socket.getPort());
            }
            catch (UnknownHostException exc) {
                logger.error("FATAL - Unable to connecto to [" + host + ":" + port + "]", exc);
                throw new NetAdapterException("Unable to connecto to [" + host + ":" + port + "]");
            }
            catch (IOException exc) {
                logger.error("Error connecting to [" + host + ":" + port + "]... retring", exc);
                try {
                    Thread.sleep(reconTime);
                }
                catch (InterruptedException exc1) {
                    // do nothing
                }
            }
        }
        return os;
    }
    
    /**
     * 
     */
    private void disconnect() {
        if (socket != null) {
            try {
                socket.close();
            }
            catch (Exception exc) {
                //exc.printStackTrace();
            }
        }
        socket = null;
    }

    /**
     * @return
     */
    public boolean isAutoStart() {
        return autoStart;
    }
    
    /**
     * 
     * @return
     */
    public int getPort() {
        return port;
    }
    
    /**
     * 
     * @return
     */
    public String getHost() {
        return this.host;
    }

    public void start() {
        if ((thread != null) && thread.isAlive()) {
            return;
        }
        thread = tFactory.newThread(this);
        thread.start();
        logger.info("NetReceiver[" + name + "] started");
    }
    
    public void stop() {
        isActive = false;
        disconnect();
        if ((thread != null) && thread.isAlive()) {
            thread.interrupt();
        }
        thread = null;
        logger.info("NetReceiver[" + name + "] stopped");
    }

    /**
     * 
     * @return
     */
    public boolean isActive() {
        return isActive;
    }


    public String getName() {
        return this.name;
    }

    public void destroy() {
        logger.debug("BEGIN - Destroing NetReceiver[" + name + "]");
        stop();
        if (invoker != null) {
            invoker.destroy();
        }
        invoker = null;
        parser = null;
        logger.debug("END - Destroing NetReceiver[" + name + "]");
    }
}

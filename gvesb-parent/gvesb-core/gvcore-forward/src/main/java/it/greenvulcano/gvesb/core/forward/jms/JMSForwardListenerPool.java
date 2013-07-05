/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.forward.jms;

import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.forward.JMSForwardException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.BaseThread;
import it.greenvulcano.util.thread.BaseThreadFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;


/**
 * Object Pool <code>JMSForwardListener</code>.
 * 
 * @version 3.2.0 11/gen/2012
 * @author GreenVulcano Developer Team
 */
public class JMSForwardListenerPool implements RejectedExecutionHandler
{
    private static final Logger logger      = GVLogger.getLogger(JMSForwardListenerPool.class);

    private JMSForwardData      data        = null;

    /**
     * Pool of JMSForwardListener instances.
     */
    private ThreadPoolExecutor  executor    = null;

    /**
     * Forward configuration name.
     */
    private String              name        = "";
    /**
     * Forward name invoked in GreenVulcano.
     */
    private String              forwardName = "";
    /**
     * Application server name.
     */
    private String              serverName  = "";


    /**
     * @param node
     * @throws GVCoreException
     * @throws GVPublicException
     */
    public void init(Node node) throws JMSForwardException
    {
        try {
            data = new JMSForwardData(node, this, logger);
            name = data.getName();
            forwardName = data.getForwardName();
            serverName = data.getServerName();

            logger.debug("BEGIN - Initializing JMSForwardListenerPool[" + forwardName + "]");

            NMDC.push();
            NMDC.setServer(serverName);
            NMDC.setSubSystem(JMSForwardData.SUBSYSTEM);
            try {
                BlockingQueue<Runnable> queue = new SynchronousQueue<Runnable>();
                executor = new ThreadPoolExecutor(data.getInitialSize(), data.getMaximumSize(), 10L, TimeUnit.MINUTES,
                        queue, new BaseThreadFactory("JMSForward#" + forwardName, true), this);

                // single initial listener
                executor.execute(createJMSForwardListener());

                try {
                    Thread.sleep(500);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            finally {
                NMDC.pop();
            }


            logger.info("Initialized JMSForwardListenerPool instance: " + data);
        }
        catch (JMSForwardException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new JMSForwardException("GVJMSPOOL_APPLICATION_INIT_ERROR", new String[][]{{"forward", forwardName}},
                    exc);
        }
        finally {
            logger.debug("END - Initialized JMSForwardListenerPool[" + forwardName + "]");
        }
    }

    public JMSForwardData getData()
    {
        return this.data;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return Returns the forwardName.
     */
    public String getForwardName()
    {
        return forwardName;
    }

    /**
     * @return Returns the maxCreated.
     */
    public int getMaxCreated()
    {
        return executor.getLargestPoolSize();
    }


    /**
     * @return Returns the pooled instance count.
     */
    public int getPooledCount()
    {
        return executor.getActiveCount();
    }


    public boolean isShutdown()
    {
        return data.isShutdown();
    }


    public void incrementListeners()
    {
        if (!isShutdown()) {
            synchronized (data) {
                int working = data.getWorkingCount();
                if ((working == executor.getActiveCount()) && (working < executor.getMaximumPoolSize())) {
                    try {
                        JMSForwardListener jmsFwd = createJMSForwardListener();
                        executor.execute(jmsFwd);
                        if (data.isDebug()) {
                            logger.debug("Forward [" + forwardName + "] - creating new instance ("
                                    + executor.getActiveCount() + "/" + data.getMaximumSize() + ")");
                        }
                    }
                    catch (Exception exc) {
                        logger.error("Forward [" + forwardName + "] - error creating new instance", exc);
                    }
                }
            }
        }
    }

    public void rescheduleListeners(JMSForwardListener jmsFwd)
    {
        if (!isShutdown() && data.isActive()) {
            synchronized (data) {
                if (executor.getActiveCount() <= data.getInitialSize()) {
                    Runnable rr = new Runnable() {
                        @Override
                        public void run()
                        {
                            try {
                                Thread.sleep(1000);
                            }
                            catch (InterruptedException exc) {
                                // do nothing
                            }
                            try {
                                JMSForwardListener jmsFwd = createJMSForwardListener();
                                executor.execute(jmsFwd);
                                if (data.isDebug()) {
                                    logger.debug("Forward [" + forwardName + "] - rescheduling new instance ("
                                            + executor.getActiveCount() + "/" + data.getMaximumSize() + ")");
                                }
                            }
                            catch (Exception exc) {
                                logger.error("Forward [" + forwardName + "] - error rescheduling new instance", exc);
                            }
                        }
                    };

                    BaseThread bt = new BaseThread(rr, "Forward [" + forwardName + "] - rescheduler");
                    bt.setDaemon(true);
                    bt.start();
                }
            }
        }
    }

    /**
     *
     */
    public void destroy()
    {
        logger.debug("Forward [" + forwardName + "] - Begin destroying instances");
        if (data != null) {
            data.destroy();
        }
        data = null;
        executor.shutdown();
        executor = null;
        logger.debug("Forward [" + forwardName + "] - End destroying instances");
    }

    /**
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        destroy();
    }

    private JMSForwardListener createJMSForwardListener() throws JMSForwardException
    {
        JMSForwardListener jmsFwd = new JMSForwardListener();
        jmsFwd.init(data);
        return jmsFwd;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
    {
        logger.warn("Forward [" + ((JMSForwardListener) r).getForwardName() + "] has been rejected from execution");
    }

}

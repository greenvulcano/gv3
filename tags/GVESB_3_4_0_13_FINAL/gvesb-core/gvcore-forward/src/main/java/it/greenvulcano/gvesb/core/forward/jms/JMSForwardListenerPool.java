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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.forward.JMSForwardException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.BaseThreadFactory;


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
            this.data = new JMSForwardData(node, this, logger);
            this.name = this.data.getName();
            this.forwardName = this.data.getForwardName();
            this.serverName = this.data.getServerName();

            logger.debug("BEGIN - Initializing JMSForwardListenerPool[" + this.name + "/" + this.forwardName + "]");

            NMDC.push();
            NMDC.setServer(this.serverName);
            NMDC.setSubSystem(JMSForwardData.SUBSYSTEM);
            try {
                BlockingQueue<Runnable> queue = new SynchronousQueue<Runnable>();
                this.executor = new ThreadPoolExecutor(this.data.getInitialSize(), this.data.getMaximumSize(), 10L, TimeUnit.MINUTES,
                        queue, new BaseThreadFactory("JMSForward#" + this.name + "#" + this.forwardName, true), this);

                // single initial listener
                this.executor.execute(createJMSForwardListener());

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


            logger.info("Initialized JMSForwardListenerPool instance: " + this.data);
        }
        catch (JMSForwardException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new JMSForwardException("GVJMSPOOL_APPLICATION_INIT_ERROR", new String[][]{{"forward", this.name + "/" + this.forwardName}},
                    exc);
        }
        finally {
            logger.debug("END - Initialized JMSForwardListenerPool[" + this.name + "/" + this.forwardName + "]");
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
        return this.name;
    }

    /**
     * @return Returns the forwardName.
     */
    public String getForwardName()
    {
        return this.forwardName;
    }

    /**
     * @return Returns the maxCreated.
     */
    public int getMaxCreated()
    {
        return this.executor.getLargestPoolSize();
    }


    /**
     * @return Returns the pooled instance count.
     */
    public int getPooledCount()
    {
        return this.executor.getActiveCount();
    }


    public boolean isShutdown()
    {
        return this.data.isShutdown();
    }


    public void incrementListeners()
    {
        if (!isShutdown()) {
            synchronized (this.data) {
                int working = this.data.getWorkingCount();
                if ((working < this.data.getInitialSize()) || ((working == this.executor.getActiveCount()) && (working < this.executor.getMaximumPoolSize()))) {
                    try {
                        JMSForwardListener jmsFwd = createJMSForwardListener();
                        this.executor.execute(jmsFwd);
                        if (this.data.isDebug()) {
                            logger.debug("Forward [" + this.name + "/" + this.forwardName + "] - creating new instance ("
                                    + this.executor.getActiveCount() + "/" + this.data.getMaximumSize() + ")");
                        }
                    }
                    catch (Exception exc) {
                        logger.error("Forward [" + this.name + "/" + this.forwardName + "] - error creating new instance", exc);
                    }
                }
            }
        }
    }

    public void rescheduleListeners()
    {
        if (!isShutdown() && this.data.isActive()) {
            synchronized (this.data) {
                if (this.executor.getActiveCount() <= this.data.getInitialSize()) {
                	try {
                        JMSForwardListener jmsFwd = createJMSForwardListener();
                        this.executor.execute(jmsFwd);
                        if (this.data.isDebug()) {
                            logger.debug("Forward [" + this.name + "/" + this.forwardName + "] - rescheduling new instance ("
                                    + this.executor.getActiveCount() + "/" + this.data.getMaximumSize() + ")");
                        }
                    }
                    catch (Exception exc) {
                        logger.error("Forward [" + this.name + "/" + this.forwardName + "] - error rescheduling new instance", exc);
                    }
                	/*
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
                                    logger.debug("Forward [" + name + "/" + forwardName + "] - rescheduling new instance ("
                                            + executor.getActiveCount() + "/" + data.getMaximumSize() + ")");
                                }
                            }
                            catch (Exception exc) {
                                logger.error("Forward [" + name + "/" + forwardName + "] - error rescheduling new instance", exc);
                            }
                        }
                    };

                    BaseThread bt = new BaseThread(rr, "Forward [" + name + "/" + forwardName + "] - rescheduler");
                    bt.setDaemon(true);
                    bt.start();
                    */
                }
            }
        }
    }

    /**
     *
     */
    public void destroy()
    {
        logger.debug("Forward [" + this.name + "/" + this.forwardName + "] - Begin destroying instances");
        if (this.data != null) {
            this.data.destroy();
        }
        this.data = null;
        this.executor.shutdown();
        this.executor = null;
        logger.debug("Forward [" + this.name + "/" + this.forwardName + "] - End destroying instances");
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
        jmsFwd.init(this.data);
        return jmsFwd;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
    {
        JMSForwardListener jmsFwd = (JMSForwardListener) r;
        logger.warn("Forward [" + jmsFwd.getName() + "/" + jmsFwd.getForwardName() + "] has been rejected from execution");
    }

}

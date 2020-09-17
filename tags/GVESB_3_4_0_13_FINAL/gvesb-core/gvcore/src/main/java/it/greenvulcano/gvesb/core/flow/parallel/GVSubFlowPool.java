/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.flow.parallel;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.flow.GVSubFlow;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;

/**
 * Object Pool  <code>GVSubFlow</code>.
 *
 * @version 3.3.4 Jun 19, 2013
 * @author GreenVulcano Developer Team
 *
 */
public class GVSubFlowPool
{
   private static final Logger      logger    = GVLogger.getLogger(GVSubFlowPool.class);

    /**
     * Pool of GVSubFlow instances.
     */
    private LinkedList<GVSubFlow> pool        = new LinkedList<GVSubFlow>();
    /**
     * Set assigned pool's instances.
     */
    private final Set<GVSubFlow>        assignedSF  = new HashSet<GVSubFlow>();

    private String                sfName      = null;
    private Node                  sfNode      = null;
    /**
     * The default logger level.
     */
    private Level                 loggerLevel = Level.ALL;

    private int initialSize = 1;
    private int maximumSize = 30;
    private int maximumCreation = 40;
    private int created = 0;
    private int maxCreated = 0;

    /**
     *
     */
    public GVSubFlowPool()
    {
        // do nothing
    }

    /**
     * @throws GVCoreException if errors occurs
     */
    public void init(Node node, Node sfNode) throws GVCoreException
    {
        logger.debug("Initializing the GVSubFlow Pool.");
        this.loggerLevel = GVLogger.getThreadMasterLevel();
        this.sfNode = sfNode;
        try {
            this.sfName = XMLConfig.get(sfNode, "@name");
        }
        catch (XMLConfigException exc) {
            logger.warn("GVSubFlowPool initialization error", exc);
            throw new GVCoreConfException("GVSubFlowPool initialization error", exc);
        }

        this.maximumSize = XMLConfig.getInteger(node, "@max-thread", 20);
        this.maximumCreation = this.maximumSize + 2;
        this.initialSize = this.maximumSize / 2;

        if (this.initialSize < 0) {
            throw new GVCoreConfException("GVSubFlowPool initialSize < 0");
        }
        if ((this.maximumSize > 0) && (this.initialSize > this.maximumSize)) {
            throw new GVCoreConfException("GVSubFlowPool initialSize(" + this.initialSize + ") > maximumSize(" + this.maximumSize + ")");
        }
        if ((this.maximumCreation > 0) && (this.maximumSize > this.maximumCreation)) {
            throw new GVCoreConfException("GVSubFlowPool maximumSize(" + this.maximumSize + ") > maximumCreation(" + this.maximumCreation + ")");
        }

        NMDC.push();
        try {
            for (int i = 0; i < this.initialSize; ++i) {
                this.pool.add(createSubFlow());
            }
        }
        finally {
            NMDC.pop();
        }

        logger.debug("Initialized GVSubFlowPool instance: initialSize=" + this.initialSize + ", maximumSize="
                + this.maximumSize + ", maximumCreation=" + this.maximumCreation);
    }

    public String getSubFlowName() {
        return this.sfName;
    }

    /**
     * @return Returns the initialSize.
     */
    public int getInitialSize()
    {
        return this.initialSize;
    }

    /**
     * @return Returns the maximumCreation.
     */
    public int getMaximumCreation()
    {
        return this.maximumCreation;
    }

    /**
     * @return Returns the maximumSize.
     */
    public int getMaximumSize()
    {
        return this.maximumSize;
    }


    /**
     * @return Returns the maxCreated.
     */
    public int getMaxCreated()
    {
        return this.maxCreated;
    }

    /**
     * @return Returns the pooled instance count.
     */
    public int getPooledCount()
    {
        return this.pool.size();
    }

    /**
     * @return Returns the used instance count.
     */
    public int getInUseCount()
    {
        return this.assignedSF.size();
    }

    /**
     *
     */
    public void resetCounter()
    {
        // do nothing
    }

    /**
     *
     * @return a pooled or newly created <code>DHFactory</code>
     * @throws GVCoreException
     */
    public GVSubFlow getSubFlow() throws GVCoreException
    {
        if (this.pool == null) {
            return null;
        }

        synchronized (this) {
            if (this.pool.size() > 0) {
                logger.debug("GVSubFlowPool - found instance in pool");
                GVSubFlow subFlow = this.pool.removeFirst();

                logger.debug("GVSubFlowPool - extracting instance from pool(" + this.pool.size() + "/"
                        + this.created + "/" + this.maximumCreation + ")");
                this.assignedSF.add(subFlow);
                return subFlow;
            }

            if ((this.maximumCreation == -1) || (this.created < this.maximumCreation)) {
                GVSubFlow subFlow = createSubFlow();
                logger.debug("GVSubFlowPool - not found instance in pool");
                logger.debug("GVSubFlowPool - creating new instance(" + this.pool.size() + "/" + this.created
                        + "/" + this.maximumCreation + ")");
                this.assignedSF.add(subFlow);
                return subFlow;
            }
        }
        return null;
    }

    /**
     *
     * @param subFlow
     */
    public void releaseSubFlow(GVSubFlow subFlow)
    {
        if (subFlow == null) {
            return;
        }
        synchronized (this) {
            try {
                if (this.assignedSF.remove(subFlow)) {
                    logger.debug("GVSubFlowPool - releasing instance(" + this.pool.size() + "/" + this.created + "/" + this.maximumCreation + ")");
                    if ((this.maximumSize == -1) || ((this.pool != null) && (this.pool.size() < this.maximumSize))) {
                        this.pool.addFirst(subFlow);
                        return;

                        /*long now = System.currentTimeMillis();
                        if ((shrinkDelayTime == -1) || (now < nextShrinkTime) || (pool.size() <= initialSize)) {
                            return;
                        }
                        logger.debug("GVSubFlowPool - shrink time elapsed");
                        subFlow = pool.removeLast();*/
                    }
                    destroySubFlow(subFlow);
                    logger.debug("GVSubFlowPool - destroying instance(" + this.pool.size() + "/" + this.created + "/" + this.maximumCreation + ")");
                }
                else {
                    logger.debug("GVSubFlowPool - instance not created by this pool, destroing it");
                    subFlow.destroy();
                }
            }
            finally {
                notify();
            }
        }
    }

    /**
     *
     */
    public synchronized void destroy()
    {
        if (this.pool == null) {
            return;
        }
        logger.debug("GVSubFlowPool - Begin destroying instances");
        while (this.pool.size() > 0) {
            GVSubFlow subFlow = this.pool.removeFirst();
            destroySubFlow(subFlow);
        }
        logger.debug("GVSubFlowPool - End destroying instances");
        this.assignedSF.clear();
        this.pool = null;
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


    private GVSubFlow createSubFlow() throws GVCoreException
    {
        GVSubFlow subFlow = new GVSubFlow();
        subFlow.init(this.sfNode, false);
        subFlow.setLoggerLevel(this.loggerLevel);
        ++this.created;
        if (this.created > this.maxCreated) {
            this.maxCreated = this.created;
        }
        return subFlow;
    }

    private void destroySubFlow(GVSubFlow subFlow)
    {
        subFlow.destroy();
        if (this.created > 0) {
            --this.created;
        }
    }
}

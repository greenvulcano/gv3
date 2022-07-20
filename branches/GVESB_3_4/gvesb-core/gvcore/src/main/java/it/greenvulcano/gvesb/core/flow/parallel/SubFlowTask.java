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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.flow.GVSubFlow;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.ThreadMap;

/**
 *
 * @version 3.4.0 Jun 17, 2013
 * @author GreenVulcano Developer Team
 *
 */
public class SubFlowTask implements Callable<Result>
{
    private static final Logger logger            = GVLogger.getLogger(SubFlowTask.class);

    private GVSubFlowPool       pool             = null;
    private GVSubFlow           subFlow          = null;
    private final GVBuffer            input;
    private final boolean             onDebug;
    private final boolean             changeLogContext;
    private Map<String, String> logContext;
    private final String              inputRefDP;
    private boolean             spawned           = false;
    private String              spawnedName       = null;
    private boolean             needsOutput       = true;

    public SubFlowTask(GVSubFlowPool pool, GVBuffer input, boolean onDebug, boolean changeLogContext, Map<String, String> logContext, String inputRefDP, boolean needsOutput) {
        this.pool = pool;
        this.input = input;
        this.onDebug = onDebug;
        this.logContext = logContext;
        this.changeLogContext = changeLogContext;
        this.inputRefDP = inputRefDP;
        this.needsOutput = needsOutput;
    }

    public SubFlowTask(GVSubFlow subFlow, GVBuffer input, boolean onDebug, boolean changeLogContext, Map<String, String> logContext, String inputRefDP, boolean needsOutput) {
        this.subFlow = subFlow;
        this.input = input;
        this.onDebug = onDebug;
        this.logContext = logContext;
        this.changeLogContext = changeLogContext;
        this.inputRefDP = inputRefDP;
        this.needsOutput = needsOutput;
    }

    public boolean isSpawned() {
        return this.spawned;
    }

    public void setSpawned(boolean spawned) {
        this.spawned = spawned;
    }

    public String getSpawnedName() {
        return this.spawnedName;
    }

    public void setSpawnedName(String spawnedName) {
        this.spawnedName = spawnedName;
    }

    @Override
    public Result call() throws Exception {
        try {
            NMDC.push();
            if (this.spawned && (this.spawnedName != null)) {
                Thread th = Thread.currentThread();
                String thn = th.getName();
                thn = thn.substring(thn.lastIndexOf("_"));
                th.setName(this.spawnedName + thn);
            }
            NMDC.setCurrentContext(this.logContext);

            Result result = null;
            GVSubFlow currSubFlow = null;
            try {
                GVBuffer internalData = this.input;

                if (this.pool != null) {
                    currSubFlow = this.pool.getSubFlow();
                }
                else {
                    currSubFlow = this.subFlow;
                }

                if (this.changeLogContext) {
                    NMDC.setOperation(currSubFlow.getFlowName());
                    GVBufferMDC.put(internalData);
                }

                DataProviderManager dataProviderManager = DataProviderManager.instance();
                if ((this.inputRefDP != null) && (this.inputRefDP.length() > 0)) {
                    IDataProvider dataProvider = dataProviderManager.getDataProvider(this.inputRefDP);
                    try {
                        logger.debug("Working on Input data provider: " + dataProvider);
                        internalData = new GVBuffer(this.input);
                        dataProvider.setObject(internalData);
                        Object inputCall = dataProvider.getResult();
                        internalData.setObject(inputCall);
                    }
                    finally {
                        dataProviderManager.releaseDataProvider(this.inputRefDP, dataProvider);
                    }
                }

                GVBuffer output = currSubFlow.perform(internalData, this.onDebug);
                if (this.needsOutput) {
                	result = new Result(Result.State.STATE_OK, this.input, output);
                }
                else {
                	result = new Result(Result.State.STATE_OK);
                }
            }
            catch (InterruptedException exc) {
                if (this.spawned) {
                    logger.error("SubFlow execution interrupted", exc);
                }
                if (this.needsOutput) {
                    result = new Result(Result.State.STATE_INTERRUPTED, this.input, exc);
                }
                else {
                    result = new Result(Result.State.STATE_INTERRUPTED, exc);
                }
                Thread.currentThread().interrupt();
            }
            catch (Exception exc) {
                if (this.spawned) {
                    logger.error("SubFlow execution failed", exc);
                }
                if (this.needsOutput) {
                    result = new Result(Result.State.STATE_ERROR, this.input, exc);
                }
                else {
                    result = new Result(Result.State.STATE_ERROR, exc);
                }
            }
            finally {
                if (this.pool != null) {
                    this.pool.releaseSubFlow(currSubFlow);
                }
                if (this.subFlow != null) {
                    this.subFlow.destroy();
                }
            }
            return result;
        }
        finally {
            NMDC.pop();
            ThreadMap.clean();

            this.pool = null;
            this.subFlow = null;
            //this.input = null;
            this.logContext = null;
        }
    }

    public Result getFailureResult(Throwable cause) {
        if (this.needsOutput) {
        	return new Result(Result.State.STATE_ERROR, this.input, cause);
        }
      	return new Result(Result.State.STATE_ERROR, cause);
    }

    public Result getTimeoutResult(InterruptedException cause) {
        if (this.needsOutput) {
        	return new Result(Result.State.STATE_TIMEOUT, this.input, cause);
        }
       return new Result(Result.State.STATE_TIMEOUT, cause);
    }

    public Result getCancelledResult(CancellationException cause) {
        if (this.needsOutput) {
        	return new Result(Result.State.STATE_CANCELLED, this.input, cause);
        }
        return new Result(Result.State.STATE_CANCELLED, cause);
    }
}

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

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.flow.GVSubFlow;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.ThreadMap;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

/**
 * 
 * @version 3.4.0 Jun 17, 2013
 * @author GreenVulcano Developer Team
 * 
 */
public class SubFlowTask implements Callable<Result>
{
    private GVSubFlowPool       pool;
    private GVBuffer            input;
    private boolean             onDebug;
    private boolean             changeLogContext;
    private Map<String, String> logContext;
    private String              inputRefDP;

    public SubFlowTask(GVSubFlowPool pool, GVBuffer input, boolean onDebug, boolean changeLogContext, Map<String, String> logContext, String inputRefDP) {
        this.pool = pool;
        this.input = input;
        this.onDebug = onDebug;
        this.logContext = logContext;
        this.changeLogContext = changeLogContext;
        this.inputRefDP = inputRefDP;
    }

    @Override
    public Result call() throws Exception {
        try {
            NMDC.push();
            NMDC.setCurrentContext(logContext);

            Result result = null;
            GVSubFlow subFlow = null;
            try {
                GVBuffer internalData = input;
                
                if (changeLogContext) {
                    NMDC.setOperation(pool.getSubFlowName());
                    GVBufferMDC.put(internalData);
                }
                
                DataProviderManager dataProviderManager = DataProviderManager.instance();
                if ((inputRefDP != null) && (inputRefDP.length() > 0)) {
                    IDataProvider dataProvider = dataProviderManager.getDataProvider(inputRefDP);
                    try {
                        internalData = new GVBuffer(input);
                        dataProvider.setObject(internalData);
                        Object inputCall = dataProvider.getResult();
                        internalData.setObject(inputCall);
                    }
                    finally {
                        dataProviderManager.releaseDataProvider(inputRefDP, dataProvider);
                    }
                }

                subFlow = pool.getSubFlow();
                GVBuffer output = subFlow.perform(internalData, onDebug);
                result = new Result(Result.State.STATE_OK, input, output);
            }
            catch (InterruptedException exc) {
                result = new Result(Result.State.STATE_INTERRUPTED, input, exc);
                Thread.currentThread().interrupt();
            }
            catch (Exception exc) {
                result = new Result(Result.State.STATE_ERROR, input, exc);
            }
            finally {
                if (pool != null) {
                    pool.releaseSubFlow(subFlow);
                }
            }
            return result;
        }
        finally {
            NMDC.pop();
            ThreadMap.clean();
            
            this.pool = null;
            //this.input = null;
            this.logContext = null;
        }
    }

    public Result getFailureResult(Throwable cause) {
        return new Result(Result.State.STATE_ERROR, input, cause);
    }

    public Result getTimeoutResult(InterruptedException cause) {
        return new Result(Result.State.STATE_TIMEOUT, input, cause);
    }

    public Result getCancelledResult(CancellationException cause) {
        return new Result(Result.State.STATE_CANCELLED, input, cause);
    }
}

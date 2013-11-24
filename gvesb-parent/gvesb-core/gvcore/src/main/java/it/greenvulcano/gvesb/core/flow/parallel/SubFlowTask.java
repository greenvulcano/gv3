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
    private Map<String, String> logContext;

    public SubFlowTask(GVSubFlowPool pool, GVBuffer input, boolean onDebug, Map<String, String> logContext) {
        this.pool = pool;
        this.input = input;
        this.onDebug = onDebug;
        this.logContext = logContext;
    }

    @Override
    public Result call() throws Exception {
        try {
            NMDC.push();
            NMDC.setCurrentContext(logContext);

            Result result = null;
            GVSubFlow subflow = null;
            try {
                subflow = pool.getSubFlow();
                GVBuffer output = subflow.perform(input, onDebug);
                result = new Result(Result.State.STATE_OK, output, input);
            }
            catch (InterruptedException exc) {
                result = new Result(Result.State.STATE_INTERRUPTED, exc, input);
                Thread.currentThread().interrupt();
            }
            catch (Exception exc) {
                result = new Result(Result.State.STATE_ERROR, exc, input);
            }
            finally {
                if (pool != null) {
                    pool.releaseSubFlow(subflow);
                }
            }
            return result;
        }
        finally {
            NMDC.pop();
            ThreadMap.clean();
        }
    }

    public GVBuffer getInput() {
        return this.input;
    }

    public Result getFailureResult(Throwable cause) {
        return new Result(Result.State.STATE_ERROR, cause, input);
    }

    public Result getTimeoutResult(InterruptedException cause) {
        return new Result(Result.State.STATE_TIMEOUT, cause, input);
    }

    public Result getCancelledResult(CancellationException cause) {
        return new Result(Result.State.STATE_CANCELLED, cause, input);
    }
}

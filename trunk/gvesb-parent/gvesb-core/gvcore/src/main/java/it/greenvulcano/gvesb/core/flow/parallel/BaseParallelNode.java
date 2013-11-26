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
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.flow.GVFlowNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @version 3.4.0 Jun 17, 2013
 * @author GreenVulcano Developer Team
 *
 */
public abstract class BaseParallelNode extends GVFlowNode
{

    protected GVBuffer processOutput(GVBuffer inputData, List<Result> result) throws GVException {
        List<Object> data = new ArrayList<Object>();

        Iterator<Result> itInput = result.iterator();
        while (itInput.hasNext()) {
            Result currOutput = itInput.next();
            if (currOutput.getState() == Result.State.STATE_OK) {
                Object d = currOutput.getData();
                if (d != null) {
                    data.add(((GVBuffer) d).getObject());
                }
            }
        }
        inputData.setObject(data);
        return inputData;
    }
}

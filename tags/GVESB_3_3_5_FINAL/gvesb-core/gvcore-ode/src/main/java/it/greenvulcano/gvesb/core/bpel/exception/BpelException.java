/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.bpel.exception;

import it.greenvulcano.gvesb.internal.GVInternalException;

/**
 *
 * @version 3.0.0 28/set/2010
 * @author GreenVulcano Developer Team
 */
public class BpelException extends GVInternalException
{

    /**
     * 
     */
    private static final long serialVersionUID = 7714977599042192411L;

    /**
     * @param errorId
     */
    public BpelException(String errorId)
    {
        super(errorId);
    }

    /**
     * @param idMessage
     * @param params
     */
    public BpelException(String idMessage, String[][] params)
    {
        super(idMessage, params);
    }

    /**
     * @param errorId
     * @param cause
     */
    public BpelException(String errorId, Throwable cause)
    {
        super(errorId, cause);
    }

    /**
     * @param idMessage
     * @param params
     * @param cause
     */
    public BpelException(String idMessage, String[][] params, Throwable cause)
    {
        super(idMessage, params, cause);
    }

}

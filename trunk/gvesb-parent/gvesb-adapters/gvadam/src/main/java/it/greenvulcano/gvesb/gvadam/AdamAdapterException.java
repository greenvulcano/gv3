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
package it.greenvulcano.gvesb.gvadam;

import it.greenvulcano.gvesb.internal.GVInternalException;

/**
 *
 * @version 3.4.0 30/apr/2014
 * @author GreenVulcano Developer Team
 */
public class AdamAdapterException extends GVInternalException
{

    /**
     *
     */
    private static final long serialVersionUID = 7714978899042192411L;

    /**
     * @param errorId
     */
    public AdamAdapterException(String errorId)
    {
        super(errorId);
    }

    /**
     * @param idMessage
     * @param params
     */
    public AdamAdapterException(String idMessage, String[][] params)
    {
        super(idMessage, params);
    }

    /**
     * @param errorId
     * @param cause
     */
    public AdamAdapterException(String errorId, Throwable cause)
    {
        super(errorId, cause);
    }

    /**
     * @param idMessage
     * @param params
     * @param cause
     */
    public AdamAdapterException(String idMessage, String[][] params, Throwable cause)
    {
        super(idMessage, params, cause);
    }

}

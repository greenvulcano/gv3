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
package it.greenvulcano.gvesb.gvrules.drools;

import it.greenvulcano.gvesb.internal.GVInternalException;

/**
 * Exception for general GVRules errors
 * 
 * @version 3.2.0 Feb 10, 2012
 * @author GreenVulcano Developer Team
 */
public class RulesException extends GVInternalException
{

    private static final long serialVersionUID = 230L;

    /**
     * Creates a new RulesException with error code identified by
     * <code>errorId</code> and no cause.
     * 
     * @param errorId
     *        ErrorId associated to the exception
     */
    public RulesException(String errorId)
    {
        super(errorId);
    }

    /**
     * Creates a new RulesException with error code identified by
     * <code>errorId</code> and no cause. <code>params</code> is used to
     * complete the error message.
     * 
     * @param errorId
     *        ErrorId associated to the exception
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     */
    public RulesException(String errorId, String[][] params)
    {
        super(errorId, params);
    }

    /**
     * Creates a new RulesException with error code identified by
     * <code>errorId</code> and a cause.
     * 
     * @param errorId
     *        ErrorId associated to the exception
     * @param cause
     */
    public RulesException(String errorId, Throwable cause)
    {
        super(errorId, cause);
    }

    /**
     * Creates a new RulesException with a cause.
     * 
     * @param errorId
     *        message associated to the exception
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     * @param cause
     */
    public RulesException(String errorId, String[][] params, Throwable cause)
    {
        super(errorId, params, cause);
    }
}

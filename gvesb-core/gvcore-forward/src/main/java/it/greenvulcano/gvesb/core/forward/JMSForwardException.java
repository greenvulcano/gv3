/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.core.forward;

import it.greenvulcano.gvesb.core.exc.GVCoreException;

/**
 * @version 3.2.0 11/gen/2012
 * @author GreenVulcano Developer Team
 */
public class JMSForwardException extends GVCoreException
{
    private static final long serialVersionUID = -8999073789091340478L;

    /**
     * @param id
     */
    public JMSForwardException(String id)
    {
        super(id);
    }

    /**
     * @param id
     * @param params
     */
    public JMSForwardException(String id, String[][] params)
    {
        super(id, params);
    }

    /**
     * @param id
     * @param cause
     */
    public JMSForwardException(String id, Throwable cause)
    {
        super(id, cause);
    }

    /**
     * @param id
     * @param params
     * @param cause
     */
    public JMSForwardException(String id, String[][] params, Throwable cause)
    {
        super(id, params, cause);
    }

}

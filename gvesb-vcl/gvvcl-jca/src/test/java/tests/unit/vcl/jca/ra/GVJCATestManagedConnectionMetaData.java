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
package tests.unit.vcl.jca.ra;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;

/**
 * @version 3.0.0 Mar 23, 2010
 * @author GreenVulcano Developer Team
 *
 */
class GVJCATestManagedConnectionMetaData implements ManagedConnectionMetaData
{

    /**
     * @see javax.resource.spi.ManagedConnectionMetaData#getEISProductName()
     */
    @Override
    public String getEISProductName() throws ResourceException
    {
        return "TXT File on " + System.getProperty("os.name");
    }

    /**
     * @see javax.resource.spi.ManagedConnectionMetaData#getEISProductVersion()
     */
    @Override
    public String getEISProductVersion() throws ResourceException
    {
        return "TXT File on " + System.getProperty("os.version");
    }

    /**
     * @see javax.resource.spi.ManagedConnectionMetaData#getMaxConnections()
     */
    @Override
    public int getMaxConnections() throws ResourceException
    {
        return 1;
    }

    /**
     * @see javax.resource.spi.ManagedConnectionMetaData#getUserName()
     */
    @Override
    public String getUserName() throws ResourceException
    {
        return "test user";
    }

}

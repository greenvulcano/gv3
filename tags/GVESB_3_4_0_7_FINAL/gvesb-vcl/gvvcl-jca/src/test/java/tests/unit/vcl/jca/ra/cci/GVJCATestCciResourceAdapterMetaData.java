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
package tests.unit.vcl.jca.ra.cci;

import javax.resource.cci.ResourceAdapterMetaData;

/**
 * @version 3.0.0 Mar 23, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class GVJCATestCciResourceAdapterMetaData implements ResourceAdapterMetaData
{

    /**
     * @see javax.resource.cci.ResourceAdapterMetaData#getAdapterName()
     */
    @Override
    public String getAdapterName()
    {
        return "GVJCATest";
    }

    /**
     * @see javax.resource.cci.ResourceAdapterMetaData#getAdapterShortDescription()
     */
    @Override
    public String getAdapterShortDescription()
    {
        return "GreenVulcano JCA Test Connector JCA RA -CCI compliant- to access to test files";
    }

    /**
     * @see javax.resource.cci.ResourceAdapterMetaData#getAdapterVendorName()
     */
    @Override
    public String getAdapterVendorName()
    {
        return "GreenVulcano S.r.l.";
    }

    /**
     * @see javax.resource.cci.ResourceAdapterMetaData#getAdapterVersion()
     */
    @Override
    public String getAdapterVersion()
    {
        return "1.0";
    }

    /**
     * @see javax.resource.cci.ResourceAdapterMetaData#getInteractionSpecsSupported()
     */
    @Override
    public String[] getInteractionSpecsSupported()
    {
        return new String[]{GVJCATestCciInteractionSpec.class.getName()};
    }

    /**
     * @see javax.resource.cci.ResourceAdapterMetaData#getSpecVersion()
     */
    @Override
    public String getSpecVersion()
    {
        return "SUN JCA 1.5";
    }

    /**
     * @see javax.resource.cci.ResourceAdapterMetaData#supportsExecuteWithInputAndOutputRecord()
     */
    @Override
    public boolean supportsExecuteWithInputAndOutputRecord()
    {
        return true;
    }

    /**
     * @see javax.resource.cci.ResourceAdapterMetaData#supportsExecuteWithInputRecordOnly()
     */
    @Override
    public boolean supportsExecuteWithInputRecordOnly()
    {
        return true;
    }

    /**
     * @see javax.resource.cci.ResourceAdapterMetaData#supportsLocalTransactionDemarcation()
     */
    @Override
    public boolean supportsLocalTransactionDemarcation()
    {
        return false;
    }

}

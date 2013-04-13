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
package it.greenvulcano.gvesb.identity.gvhl7.listener;

import it.greenvulcano.gvesb.identity.impl.BaseIdentityInfo;
import it.greenvulcano.log.GVLogger;

import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.apache.log4j.Logger;

import ca.uhn.hl7v2.app.Receiver;
import ca.uhn.hl7v2.app.ThreadUtils;

/**
 * @version 3.2.0 25/giu/2012
 * @author GreenVulcano Developer Team
 */
public class HL7IdentityInfo extends BaseIdentityInfo
{
    private static final Logger logger  = GVLogger.getLogger(HL7IdentityInfo.class);
    public static final String  TH_KEY  = "HL7IdentityInfo_IP";
    private String              address = null;
    private String              name    = null;

    public HL7IdentityInfo(String name)
    {
        this.name = name;
        address = ThreadUtils.getIPRef();
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    protected boolean subIsInRole(String role)
    {
        return false;
    }

    @Override
    protected boolean subMatchAddress(String address)
    {
        if (address == null) {
            return false;
        }
        boolean res = address.equals(this.address);
        if (debug) {
            logger.debug("HL7IdentityInfo[" + getName() + "]: Address[" + address + ": " + this.address + "] -> " + res);
        }
        return res;
    }

    @Override
    protected boolean subMatchAddressMask(SubnetInfo addressMask)
    {
        if (addressMask == null) {
            return false;
        }
        boolean res = addressMask.isInRange(address);
        if (debug) {
            logger.debug("HL7IdentityInfo[" + getName() + "]: AddressMask[" + addressMask.getCidrSignature() + ": "
                    + address + "] -> " + res);
        }
        return res;
    }

    @Override
    public String toString()
    {
        return "HL7IdentityInfo[" + getName() + "]";
    }
}

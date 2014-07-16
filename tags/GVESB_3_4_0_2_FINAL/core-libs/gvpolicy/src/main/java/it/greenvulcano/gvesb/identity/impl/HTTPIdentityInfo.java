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
package it.greenvulcano.gvesb.identity.impl;

import it.greenvulcano.log.GVLogger;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.apache.log4j.Logger;

/**
 * @version 3.2.0 16/feb/2012
 * @author GreenVulcano Developer Team
 */
public class HTTPIdentityInfo extends BaseIdentityInfo
{
    private static final Logger logger  = GVLogger.getLogger(HTTPIdentityInfo.class);
    private HttpServletRequest  request = null;

    public HTTPIdentityInfo(HttpServletRequest request)
    {
        this.request = request;
    }

    @Override
    public String getName()
    {
        Principal p = request.getUserPrincipal();
        return (p != null ? p.getName() : "NONE");
    }

    @Override
    protected boolean subIsInRole(String role)
    {
        if (role == null) {
            return false;
        }
        boolean res = request.isUserInRole(role);
        if (debug) {
            logger.debug("HTTPIdentityInfo[" + getName() + "]: Role[" + role + "] -> " + res);
        }
        return res;
    }

    @Override
    protected boolean subMatchAddress(String address)
    {
        if (address == null) {
            return false;
        }
        boolean res = address.equals(request.getRemoteAddr());
        if (debug) {
            logger.debug("HTTPIdentityInfo[" + getName() + "]: Address[" + address + ": " + request.getRemoteAddr()
                    + "] -> " + res);
        }
        return res;
    }

    @Override
    protected boolean subMatchAddressMask(SubnetInfo addressMask)
    {
        if (addressMask == null) {
            return false;
        }
        String address = request.getRemoteAddr();
        boolean res = addressMask.isInRange(address);
        if (debug) {
            logger.debug("HTTPIdentityInfo[" + getName() + "]: AddressMask[" + addressMask.getCidrSignature() + ": "
                    + address + "] -> " + res);
        }
        return res;
    }

    @Override
    public String toString()
    {
        return "HTTPIdentityInfo[" + getName() + "]";
    }
}

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

import javax.ejb.EJBContext;

import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.apache.log4j.Logger;

/**
 * @version 3.2.0 02/feb/2012
 * @author GreenVulcano Developer Team
 */
public class EJBIdentityInfo extends BaseIdentityInfo
{
    private static final Logger logger  = GVLogger.getLogger(EJBIdentityInfo.class);
    private EJBContext          context = null;

    public EJBIdentityInfo(EJBContext context)
    {
        super();
        this.context = context;
    }

    @Override
    public String getName()
    {
        Principal p = context.getCallerPrincipal();
        return (p != null ? p.getName() : "NONE");
    }

    @Override
    protected boolean subIsInRole(String role)
    {
        if (role == null) {
            return false;
        }
        boolean res = context.isCallerInRole(role);
        if (debug) {
            logger.debug("EJBIdentityInfo[" + getName() + "]: " + role + " -> " + res);
        }
        return res;
    }

    @Override
    protected boolean subMatchAddress(String address)
    {
        return false;
    }

    @Override
    protected boolean subMatchAddressMask(SubnetInfo addressMask)
    {
        return false;
    }

    @Override
    public String toString()
    {
        return "EJBIdentityInfo[" + getName() + "]";
    }
}

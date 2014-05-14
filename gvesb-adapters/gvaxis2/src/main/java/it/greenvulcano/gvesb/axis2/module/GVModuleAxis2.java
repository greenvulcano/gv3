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

package it.greenvulcano.gvesb.axis2.module;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.modules.Module;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;


public class GVModuleAxis2 implements Module /* , ModulePolicyExtension */  {

    public void init(ConfigurationContext configContext, AxisModule module)
            throws AxisFault {
    	// at the moment, nothing needs to be done ..
    }

    public void engageNotify(AxisDescription axisDescription) throws AxisFault {
    	// at the moment, nothing needs to be done ..
    }

    public void shutdown(ConfigurationContext configurationContext) throws AxisFault {
        // at the moment, nothing needs to be done ..
    }


    public void applyPolicy(Policy policy, AxisDescription axisDescription) throws AxisFault {
        //Do not do anything
    }

    public boolean canSupportAssertion(Assertion assertion) {
            return true;
    }
}

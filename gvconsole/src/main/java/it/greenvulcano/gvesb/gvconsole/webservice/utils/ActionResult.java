/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.greenvulcano.gvesb.gvconsole.webservice.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
/*
 * ActionResult class
 *
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
*/
public class ActionResult
{
    private Map result = null;

    public ActionResult()
    {
        result = new HashMap();
    }

    public void addDetails(String server, boolean esito, String action, Exception eccezione)
    {
        addDetails(server, new ActionDetail(esito, action, eccezione));
    }

    public void addDetails(String server, ActionDetail detail)
    {
        List detailsList = getDetailsPriv(server);
        detailsList.add(detail);
    }

    public Set getServers()
    {
        return new TreeSet(result.keySet());
    }

    public Map getDetails()
    {
        return Collections.unmodifiableMap(result);
    }

    private List getDetailsPriv(String server)
    {
        List details = (List) result.get(server);
        if (details == null) {
            details = new LinkedList();
            result.put(server, details);
        }
        return details;
    }
}
